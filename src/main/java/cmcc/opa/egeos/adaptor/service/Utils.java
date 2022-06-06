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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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

	public static int fromEgeosShapefileToOilSpillSimulationRequest(String eventId)
			throws JSONException, UnsupportedEncodingException, IOException {

		// no session required for first version
		//String session = getLoginSession();
		//String event = getJSON("https://www.eos-viewer.com/api/okeanos/" + eventId, session);

		String event = getJSON("https://ov-matteo.cmcc-opa.eu/backend/egeos/?uniqueId=" + eventId);

		// EgeosEvent egeosEvent = new Gson().fromJson(event.substring(1, event.length() - 2),
		// EgeosEvent.class);

		EgeosEvent egeosEvent = new Gson().fromJson(event, EgeosEvent.class);

		// preparing payload
		if (egeosEvent == null) return -1;
		
		int notNullGeometryIndex = 0;

		for (int i=0; i<egeosEvent.getTotalFeatures(); i++){
			System.out.println("egeosEvent.getFeatures() -> " + egeosEvent.getFeatures());
			if(egeosEvent.getFeatures().get(i).getGeometry() != null) notNullGeometryIndex = i;
		}

		Feature feature = egeosEvent.getFeatures().get(notNullGeometryIndex);
		
		String simName = feature.getId().replace(".", "_");
		System.out.println();
		System.out.println("simName -> " + simName);
		System.out.println();

		// 2020-02-25T00:41:00+01:00
		// WARNING !!! FORCING TO IGNORE +02 TIMEZONE INFO

		DateTime dt = new DateTime(feature.getProperties().getDateTime()).toDateTime(DateTimeZone.UTC);

		System.out.println("WARNING: forcing to year 2022!!!");
		//String year = String.format("%02d", dt.getYearOfCentury());
		String year = String.format("%02d", 22);
		String month = String.format("%02d", dt.getMonthOfYear());
		String day = String.format("%02d", dt.getDayOfMonth());
		String hour = String.format("%02d", dt.getHourOfDay());
		String minutes = String.format("%02d", dt.getMinuteOfHour());
		String startLat = "", startLon = "";
		double minVolume = 1.0;

		//minVolume = egeosEvent.getMinVolume();
		minVolume = 1.522;

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
		String correlationId = simName, serviceId = "egeos.orbitaleos", requestDss = "oilspill_op";
		int plotStep = 1; // 24

		Properties prop = new Properties();

		try {

			prop.load(Utils.class.getResourceAsStream("/config.properties"));

			model = prop.getProperty("model");
			URL url = new URL ("http://localhost:8080/utils/rest/gethighestresolutionmodel/lat="+doubleStartLat+ "&lon="+doubleStartLon+ "");
			//URL url = new URL ("http://172.16.20.2:8080/utils/rest/gethighestresolutionmodel/lat="+doubleStartLat+ "&lon="+doubleStartLon+ "");
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setRequestMethod("GET");
			//con.setRequestProperty("Content-Type", "application/json; utf-8");
			con.setRequestProperty("Accept", "application/json");
			con.setDoOutput(true);

			try(BufferedReader br = new BufferedReader(
					  new InputStreamReader(con.getInputStream(), "utf-8"))) {
					    StringBuilder response = new StringBuilder();
					    String responseLine = null;
					    while ((responseLine = br.readLine()) != null) {
					        response.append(responseLine.trim());
					    }
					    System.out.println();
					    //System.out.println("model ----> " + response.toString());
					    JSONObject jsonObject = new JSONObject(response.toString());
					    String modelFromWebService = jsonObject.getString("model");
					    System.out.println("modelFromWebService -> " + modelFromWebService);
					    model = modelFromWebService;
					    System.out.println();
			}

			
			simLength = prop.getProperty("sim_length");
			
			spillRate = String.valueOf(decimalFormat.format(Double.valueOf(prop.getProperty("spillrate_coeff"))*minVolume));
			
			// spillRate = "5.0";
			var_02 = prop.getProperty("var_02");
			var_03 = prop.getProperty("var_03");
			var_10 = prop.getProperty("var_10");

			// forcing GOFS instead GLOB
			if (model.equals("GLOB")) model = "GOFS";
			
			if (model.equals("GLOB") || model.equals("GOFS")) var_10 = "10.0";
			else if (model.equals("MED") || model.equals("BLACKSEA")) var_10 = "2.0";
			else if (model.equals("SANIFS") || model.equals("DUBAI")) var_10 = "0.4";

			var_14 = prop.getProperty("var_14");
			plotStep = Integer.valueOf(prop.getProperty("plotstep"));

			System.out.println("var_10/Horizontal diffusivity : "+ var_10);

		} catch (IOException ex) {
			System.out.println("Exception reading prop file -> " + ex.getMessage());
		}

		String requestPayload = "{\"sim_name\":\"" + simName + "\",\"notes\":\"\",\"start_lat\":\"" + startLat
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
		if (jsonObj.has("simulation_id")) simulationId = jsonObj.getInt("simulation_id");
		System.out.println("simulation_id-> " + simulationId);
		return simulationId;
	}

	public static String sendingRequest(String urlString, String content, String token, String service)
			throws UnsupportedEncodingException, IOException {

		String responseFromHttpRequest = "";
//		System.out.println();
//		System.out.println("fullContent--- " + content);
//		System.out.println("urlString--- " + urlString);
//
//		System.out.println();

		URL url = new URL(urlString);

		HttpURLConnection con = (HttpURLConnection) url.openConnection();

		if (service.equals("getShapefile")) {
			con.setRequestMethod("GET");
			System.out.println("Setting cookie ... ");
			con.setRequestProperty("Cookie", token);

		} else {
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			if (service.equals("response")) con.setRequestProperty("Cookie", token);

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
	
	public static String sendingResponseToEgeos(String egeosURL, String egeosParameters)
			throws JSONException, UnsupportedEncodingException, IOException {
		String service = "response";
		String token = getLoginSession();
		System.out.println();
		System.out.println("token ----> " + token);
		return sendingRequest(egeosURL, egeosParameters, token, service);

	}

	protected static String getLoginSession() throws IOException {
		String loginURL = "https://www.eos-viewer.com/analyst";
		String username = "okeanos";
		String password = "password";

		String usernamePassword = "username=" + username + "&password=" + password + "";

		URL url = new URL(loginURL);

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		con.setRequestProperty("Accept", "application/json");
		con.setDoOutput(true);

		try (OutputStream os = con.getOutputStream()) {
			byte[] input = usernamePassword.getBytes("utf-8");
			os.write(input, 0, input.length);
		}
		return readingCookie(con);
	}

	protected static String readingCookie(HttpURLConnection httpConnection) {

		Map<String, List<String>> headerFields = httpConnection.getHeaderFields();
		// HashMap<String, String> cookieList = new HashMap<String, String>();

		Set<String> headerFieldsSet = headerFields.keySet();
		Iterator<String> hearerFieldsIter = headerFieldsSet.iterator();
		String cookieString = "";

		while (hearerFieldsIter.hasNext()) {

			String headerFieldKey = hearerFieldsIter.next();

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

			}

		}
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
			//c.setRequestProperty("Cookie", cookie);
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
