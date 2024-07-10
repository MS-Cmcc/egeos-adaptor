package cmcc.opa.egeos.adaptor.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONException;
import org.json.JSONObject;

import cmcc.opa.egeos.adaptor.model.EgeosEvent;
import cmcc.opa.egeos.adaptor.model.Feature;

public class Utils {
	Utils() {

	}

	public static int fromEgeosShapefileToOilSpillSimulationRequest(String event)
			throws JSONException, UnsupportedEncodingException, IOException {

		EgeosEvent egeosEvent = new Gson().fromJson(event, EgeosEvent.class);

		// preparing payload
		if (egeosEvent == null) return -1;
		
		int notNullGeometryIndex = 0;

		for (int i=0; i<egeosEvent.getTotalFeatures(); i++){
			if(egeosEvent.getFeatures().get(i).getGeometry() != null) notNullGeometryIndex = i;
		}

		Feature feature = egeosEvent.getFeatures().get(notNullGeometryIndex);
		String username = egeosEvent.getUsername();
		String tenantId = egeosEvent.gettenantId();
		
		// String simName = feature.getId().replace(".", "_").replace("-", "_");
		String simName = feature.getId() + "&username=" + username + "&tenantId=" + tenantId;

		DateTime dt = new DateTime(feature.getProperties().getDateTime()).toDateTime(DateTimeZone.UTC);
		// using current DateTime in case of TEST simulation
		if (simName.contains("***TEST***")) {
			System.out.println("WARNING!!! I AM FORCING THE DATE TO TODAY!!!");
			dt = new DateTime(new Date()).toDateTime(DateTimeZone.UTC);
		}
		
		String year = String.format("%02d", dt.getYearOfCentury());
		String month = String.format("%02d", dt.getMonthOfYear());
		String day = String.format("%02d", dt.getDayOfMonth());
		String hour = String.format("%02d", dt.getHourOfDay());
		String minutes = String.format("%02d", dt.getMinuteOfHour());
		String startLat = "", startLon = "";
		Double areaKm = 1.0;
		areaKm = feature.getProperties().getArea_km();

		String subStringWithBbox = "";
		int coordinatesNumberOfEvent = feature.getGeometry().getCoordinates().size();
		
		int currentIndexI = 0, currentIndexY = 0;
		for (int i = 0; i < coordinatesNumberOfEvent; i++) {
		//for (int i = 0; i < 100; i++) {
			
			for (int y = 0; y< feature.getGeometry().getCoordinates().get(i).get(0).size(); y++) {
				ArrayList<Double> latLonPair = feature.getGeometry().getCoordinates().get(i).get(0).get(y);
				currentIndexI = i+1;
				currentIndexY = y+1;
				
				subStringWithBbox = subStringWithBbox.concat(
						",\"Slat_"+ currentIndexI +"_" + currentIndexY + "\":\"" + String.format(Locale.US, "%.5f", latLonPair.get(1)) + "\", \"Slon_"+currentIndexI+"_"
								+ currentIndexY + "\":\"" + String.format(Locale.US, "%.5f", latLonPair.get(0)) + "\"");
				if (i == 0 & y == 0) {
					startLat = String.format(Locale.US, "%.5f", latLonPair.get(1));
					startLon = String.format(Locale.US, "%.5f", latLonPair.get(0));
				}
			}
			// WARNING!!! max 50 polygons !!!
			if (i== 49) break;
		}

		String latDegree, lonDegree, latMinutes, lonMinutes;
		DecimalFormat decimalFormat = new DecimalFormat("0.00000");

		double doubleStartLat, intPartOfStartLat, decimalPartOfStartLat, doubleStartLon, intPartOfStartLon,
				decimalPartOfStartLon;
		doubleStartLat = Double.valueOf(startLat);
		doubleStartLon = Double.valueOf(startLon);
		intPartOfStartLat = (int) doubleStartLat;
		intPartOfStartLon = (int) doubleStartLon;
		decimalPartOfStartLat = doubleStartLat - intPartOfStartLat;
		decimalPartOfStartLon = doubleStartLon - intPartOfStartLon;

		latDegree = startLat.split("\\.")[0];
		latMinutes = String.valueOf(decimalFormat.format(decimalPartOfStartLat * 60));
		lonDegree = startLon.split("\\.")[0];
		lonMinutes = String.valueOf(decimalFormat.format(decimalPartOfStartLon * 60));

		String model = "", simLength = "", spillRate = "", var_02 = "", var_03 = "", var_10 = "", var_14 = "";
		String correlationId = simName, serviceId = "egeos", requestDss = "oilspill_op";
		int plotStep = 1; // 24

		Properties prop = new Properties();

		try {

			prop.load(Utils.class.getResourceAsStream("/config.properties"));

			model = prop.getProperty("model");
			
			simLength = prop.getProperty("sim_length");
			
			spillRate = String.valueOf(decimalFormat.format(Double.valueOf(prop.getProperty("spillrate_coeff"))*areaKm));
			
			var_02 = prop.getProperty("var_02");
			var_03 = prop.getProperty("var_03");
			var_10 = prop.getProperty("var_10");

			// forcing GOFS model
			if (model.equals("GLOB")) model = "GOFS";

			if (model.equals("GLOB") || model.equals("GOFS")) var_10 = "10.0";
			else if (model.equals("MED") || model.equals("BLACKSEA")) var_10 = "2.0";
			else if (model.equals("SANIFS") || model.equals("DUBAI")) var_10 = "0.4";

			var_14 = prop.getProperty("var_14");
			plotStep = Integer.valueOf(prop.getProperty("plotstep"));

		} catch (IOException ex) {
			System.out.println("Exception reading prop file -> " + ex.getMessage());
		}

		String requestPayload = "{\"sim_name\":\"" + simName + "\",\"notes\":\""+ areaKm +"\",\"start_lat\":\"" + startLat
				+ "\",\"start_lon\":\"" + startLon + "\",\"model\":\"" + model
				+ "\",\"wind\":\"ECMWF025\",\"sim_length\":\"" + simLength + "\",\"day\":\"" + day + "\",\"month\":\""
				+ month + "\",\"year\":\"" + year + "\"," + "\"hour\":\"" + hour + "\",\"minutes\":\"" + minutes
				+ "\",\"lat_degree\":\"" + latDegree + "\",\"lat_minutes\":\"" + latMinutes + "\",\"lon_degree\":\""
				+ lonDegree + "\",\"lon_minutes\":\"" + lonMinutes
				+ "\",\"spillType\":1,\"duration\":\"0000\",\"spillrate\":\"" + spillRate + "\",\"oil\":\"NAME\","
				+ "\"oiltype\":\"Aboozar\",\"var_02\":\"" + var_02 + "\",\"var_03\":\"" + var_03 + "\",\"var_10\":\""
				+ var_10 + "\",\"var_14\":\"" + var_14
				+ "\",\"var_19\":\"0.000033\",\"var_29\":\"0.000008\",\"var_39\":\"150\",\"plotStep\":" + plotStep
				+ ",\"selector\":\"witoil\"," + "\"contourSlick\":\"YES\" " + subStringWithBbox + "}";

		System.out.println();
		System.out.println("requestPayload----> " + requestPayload);
		System.out.println();
		
		int simId = -1;
		simId = sendingToSsa(serviceId, correlationId, requestDss, requestPayload);

        System.out.println();
		System.out.println("SimID: -----> " + simId);
        System.out.println();
		return simId;

	}

	public static int sendingToSsa(String serviceId, String correlationId, String dss, String payload)
			throws JSONException, UnsupportedEncodingException, IOException {
		String service = "newSimulation";
		String url = "http://172.16.20.14:8081/simulations/";
		String fullContent = "{\"serviceId\":\"" + serviceId + "\",\"correlationId\":\"" + correlationId
				+ "\",\"dss\":\"" + dss + "\",\"payload\":" + payload + "}";

		JSONObject jsonObj = new JSONObject(sendingRequest(url, fullContent, null, service));
		int simulationId = -1;
		if (jsonObj.has("simulation_id")) simulationId = Integer.parseInt(jsonObj.get("simulation_id").toString().replace("99999", ""));
		return simulationId;
	}

	public static String sendingRequest(String urlString, String content, String token, String service)
			throws UnsupportedEncodingException, IOException {

		String responseFromHttpRequest = "";

		URL url = new URL(urlString);

		HttpURLConnection con = (HttpURLConnection) url.openConnection();

		if (service.equals("getShapefile")) {
			con.setRequestMethod("GET");
			System.out.println("Setting cookie ... ");
			con.setRequestProperty("Cookie", token);

		} else {
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			if (service.equals("response")) con.setRequestProperty("Authorization","Bearer "+ token);
		}
		con.setRequestProperty("Accept", "application/json");
		con.setDoOutput(true);

		try (OutputStream os = con.getOutputStream()) {
			byte[] input = content.getBytes("utf-8");
			os.write(input, 0, input.length);
		}

		int code = con.getResponseCode();

		System.out.println();
		System.out.println("response code in sendingRequest -> " + code);
		System.out.println();

		try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
			StringBuilder response = new StringBuilder();
			String responseLine = null;
			while ((responseLine = br.readLine()) != null) {
				response.append(responseLine.trim());
			}
			responseFromHttpRequest = response.toString();
			System.out.println();
			System.out.println("response -> " + response.toString());

		}

		return responseFromHttpRequest;
	}
	
	public static String sendingResponseToEgeos(String egeosParameters, String username, String tenantId)
			throws JSONException, UnsupportedEncodingException, IOException {
		String service = "response";
		String token = getLoginSession();
		System.out.println();
		System.out.println("token ----> " + token);
		String egeosURL = "https://seonse-demo.egeos-services.it/services/api/cmcc/1/"+tenantId+"/"+username+"/cmcc-publication";
		System.out.println("Sending result to this url: " + egeosURL);

		return sendingRequest(egeosURL, egeosParameters, token, service);

	}

	protected static String getLoginSession() throws IOException {
		String loginURL = "https://seonse-demo.egeos-services.it/token";

		String clientId = "Lw_ayNeObduoR2UBCoce82YFo8Ia";
		String clientSecret = "asVSh_FuJY0bn0DUWOg0FerYOrEa";
		String clientIdClientSecret = "THdfYXlOZU9iZHVvUjJVQkNvY2U4MllGbzhJYTphc1ZTaF9GdUpZMGJuMERVV09nMEZlcllPckVh";
		String username= "cmcc";
		String password= "_Yw:B_?M#t6cgxV@";


		String usernamePassword = "grant_type=password&username=" + username + "&password=" + password + "&scope=openid";

		URL url = new URL(loginURL);

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		con.setRequestProperty("Accept", "application/json");
		con.setRequestProperty("Authorization", "Basic "+clientIdClientSecret);
		con.setDoOutput(true);

		try (OutputStream os = con.getOutputStream()) {
			byte[] input = usernamePassword.getBytes("utf-8");
			os.write(input, 0, input.length);
		}

		 // Leggi la risposta
		 int responseCode = con.getResponseCode();
		 String accessToken = "";
		 if (responseCode == HttpURLConnection.HTTP_OK) {
			 // reading response from the server
			 BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			 String inputLine;
			 StringBuilder response = new StringBuilder();
			 
			 while ((inputLine = in.readLine()) != null) {
				 response.append(inputLine);
			 }
			 in.close();
		 
			 // Converting and printing response
			 String responseString = response.toString();
			 System.out.println("Response: " + responseString);
		 
			 // Parsing the resonse for getting the access_token
			 JSONObject jsonResponse = new JSONObject(responseString);
			 accessToken = jsonResponse.getString("access_token");
			 System.out.println("Access Token: " + accessToken);
		 } else {
			 // Gestisci gli errori
			 System.out.println("Response not OK. Response code: " + responseCode);
		 }
		
		 return accessToken;
	}

	protected static String readingCookie(HttpURLConnection httpConnection) {

		Map<String, List<String>> headerFields = httpConnection.getHeaderFields();
		// HashMap<String, String> cookieList = new HashMap<String, String>();

		Set<String> headerFieldsSet = headerFields.keySet();
		Iterator<String> hearerFieldsIter = headerFieldsSet.iterator();
		String cookieString = "";

		while (hearerFieldsIter.hasNext()) {

			String headerFieldKey = hearerFieldsIter.next();
			System.out.println("headerFieldKeyheaderFieldKeyheaderFieldKey " + headerFieldKey);
			if ("Set-Cookie".equalsIgnoreCase(headerFieldKey)) {

				List<String> headerFieldValue = headerFields.get(headerFieldKey);

				for (String headerValue : headerFieldValue) {

					System.out.println("Cookie Found...");

					String[] fields = headerValue.split(";s*");

					String cookieValue = fields[0];
					// cookieList.put(fields[0].split("=")[0], fields[0].split("=")[1]);
					if (cookieString.length() < 1)
						cookieString = cookieValue;
					else
						cookieString = cookieString + ";" + cookieValue;

				}

			} else System.out.println("NO NO Set-Cookie");

		}
		System.out.println("cookieString ->>>>>> " + cookieString);
		return cookieString;
	}

	//public static String getJSON(String url, String cookie) {
	public static String getJSON(String url) {
		HttpURLConnection c = null;
		try {
			URL u = new URL(url);
			c = (HttpURLConnection) u.openConnection();
			c.setRequestMethod("GET");
			c.setRequestProperty("Content-length", "0");
			c.setUseCaches(false);
			c.setAllowUserInteraction(false);
			//c.setRequestProperty("Cookie", "cookie");
			c.setRequestProperty("Accept", "application/json");
			c.connect();
			int status = c.getResponseCode();

			switch (status) {
			case 200:
			case 201:
				BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line + "\n");
				}
				br.close();
				return sb.toString();
			}

		} catch (MalformedURLException ex) {
			// Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			// Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
		} finally {
			if (c != null) {
				try {
					c.disconnect();
				} catch (Exception ex) {
					// Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
		return null;
	}
}
