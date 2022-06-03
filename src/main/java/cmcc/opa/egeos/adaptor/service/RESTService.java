package cmcc.opa.egeos.adaptor.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilderFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import cmcc.opa.egeos.adaptor.model.SimulationRequest;

@Path("/services")
public class RESTService {
	
	@POST
	@Path("create-simulation")
	@Produces("application/json")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response creatingOilSpillSimulationRequest(String message) throws JSONException, UnsupportedEncodingException, IOException {
		System.out.println("received message ----------------------------> " + message);

		JSONObject jsonObject = new JSONObject(message);
		String eventId = jsonObject.get("acquisitionId").toString();
		int oilSpillSImulationId = Utils.fromEgeosShapefileToOilSpillSimulationRequest(eventId);
		
		return Response.ok("OK", MediaType.APPLICATION_JSON).build();

		// SimulationRequest sr = new SimulationRequest(oilSpillSImulationId);
		// return Response.ok(sr, MediaType.APPLICATION_JSON).build();
		
	}
	
	@POST
	@Path("simulation-result")
	@Produces("application/json")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response receivingSimulationResponse(String message) throws JSONException, UnsupportedEncodingException, IOException {
		
		System.out.println("received message -> " + message);
		// starting simulation response 

		JSONObject jsonObject = new JSONObject(message);
		
		String simulationId = jsonObject.get("simulationId").toString(); // simulationId: 99999201
		String status = jsonObject.get("status").toString() ; // status: C
		String serviceId = jsonObject.get("serviceId").toString() ; // serviceId: legacysystem.test1.pullrequest
		String correlationId = jsonObject.get("correlationId").toString() ; // correlationId: 56cf6e61-bceb-4d65-9a65-6c6068aad456
		String dss = jsonObject.get("dss").toString() ; // dss: sar_op oilspill_op

		String dssPath = "WITOIL";
		
		// String wmsURL = "https://wms-dev.cmcc-opa.eu/cache/mapsrvn8?map=/var/www/html/cache/DSS/";
		// //String wmsURL = "http://193.204.199.193/cache/mapsrvn8?map=/var/www/html/cache/DSS/";
		// wmsURL = wmsURL.concat(dssPath).concat("/simulations/").concat(simulationId).concat("/").concat(simulationId)
		// 			.concat(".map&REQUEST=GetCapabilities&service=WMS");

		String wmsURL = "https://ov-prod.cmcc-opa.eu/cgi-bin/mapserv?map=witoil_";
		//String wmsURL = "http://193.204.199.193/cache/mapsrvn8?map=/var/www/html/cache/DSS/";
		wmsURL = wmsURL.concat(simulationId).concat("&REQUEST=GetCapabilities&service=WMS");

		// https://ov-prod.cmcc-opa.eu/cgi-bin/mapserv?map=witoil_999994309&REQUEST=GetMap&service=WMS
		// https://wms-dev.cmcc-opa.eu/cache/mapsrvn8?map=/var/www/html/cache/DSS/WITOIL/simulations/999992759/999992759.map&REQUEST=GetMap&service=WMS
			
		System.out.println("simulationId: " + simulationId);
		System.out.println("status: " + status );
		System.out.println("serviceId: " + serviceId);
		System.out.println("correlationId: " + correlationId );
		System.out.println("dss: " + dss );
		System.out.println("wmsURL: " + wmsURL );
		
		// creating response ...
		ArrayList<String> oilConcentrationDates = new ArrayList<String>();
		ArrayList<String> beachedOilDates = new ArrayList<String>();
		ArrayList<String> tempeartureDates = new ArrayList<String>();
		ArrayList<String> waveHeightDates = new ArrayList<String>();
		ArrayList<String> wavePeriodDates = new ArrayList<String>();
		ArrayList<String> waveDirectionsDates = new ArrayList<String>();
		ArrayList<String> currentsDates = new ArrayList<String>();
		ArrayList<String> currentsDirectionDates = new ArrayList<String>();
		ArrayList<String> windDates = new ArrayList<String>();
		ArrayList<String> windDirectionsDates = new ArrayList<String>();
		JSONObject simulationResponseJson = new JSONObject();
		
		try {
		 URL obj = new URL(wmsURL);
		 HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		 int responseCode = con.getResponseCode();
		 System.out.println("Response Code : " + responseCode);
		  BufferedReader in = new BufferedReader(
			 new InputStreamReader(con.getInputStream()));
			 String inputLine;
			 StringBuffer response = new StringBuffer();
			 while ((inputLine = in.readLine()) != null) {
			   response.append(inputLine);
			 }
			in.close();
			//print in String
		        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
		         .parse(new InputSource(new StringReader(response.toString())));
		        NodeList errNodes = doc.getElementsByTagName("Layer");
		        
		        System.out.println();
		        System.out.println("errNodes.getLength()-> " + errNodes.getLength());
		        System.out.println();
		        
		        // String totalOilLayer = "mdslk_totaloil_", beachedLayer = "mdslk_beached_", 
				String totalOilLayer = "Witoil-Total-Oil", beachedLayer = "Witoil-Beached";
				String isolinesLayer = totalOilLayer.concat("-Isolines"); 

				if (correlationId.contains("_thick")){
					totalOilLayer = totalOilLayer.concat("-Thick");
					beachedLayer = beachedLayer.concat("-Thick");
					isolinesLayer = isolinesLayer.concat("-Thick");
					correlationId = correlationId.replace("_thick", "");
				}

				String sstLayer = "sst_", vhmLayer = "vhm0_";
		        String vtzaLayer = "vtza_", vdirLayer = "vdir_", currLayer = "curr_", currDirectionLayer = "curr-direction_";
		        String windLayer = "wind_", windDirectionLayer = "wind-direction_";

				
		        
		        if (errNodes.getLength() >= 2) {
		        
		        for (int temp = 0; temp < errNodes.getLength(); temp++) {
		        	Node nNode = errNodes.item(temp);
		        	System.out.println("\nCurrent Element :" + nNode.getNodeName());
		        	
		        	if (nNode.getNodeType() == Node.ELEMENT_NODE) {

		                Element eElement = (Element) nNode;

		                System.out.println("Name: " + eElement.getElementsByTagName("Name").item(0).getTextContent());
		                System.out.println("title : " + eElement.getElementsByTagName("Title").item(0).getTextContent());

		                if (eElement.getElementsByTagName("Name").item(0).getTextContent().contains(totalOilLayer)) {
							// oilConcentrationDates.add(eElement.getElementsByTagName("Name").item(0).getTextContent());
							// reading Dimension attribute in the xml GetCapabilities response
							System.out.println();
							//System.out.println("------> " + eElement.getElementsByTagName("Dimension").item(0).getTextContent());
							String dateRange = eElement.getElementsByTagName("Dimension").item(0).getTextContent();
							System.out.println("wms layers dateRange -----> " +  dateRange );

							// 2022-01-18T15:00:00Z/2022-01-20T14:00:00Z/PT1H
							String[] dateRangeParts = dateRange.split("/");
							
							if (dateRangeParts.length > 2){
								String dateFromString = dateRangeParts[0];
								String dateToString = dateRangeParts[1];

								SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								Date startDate = formatter.parse(dateFromString.replace("T", " ").replace("Z", ""));
								Date endDate = formatter.parse(dateToString.replace("T", " ").replace("Z", ""));
								
								Calendar start = Calendar.getInstance();
								start.setTime(startDate);
								
								Calendar end = Calendar.getInstance();
								end.setTime(endDate);
								// adding one hour in order to inclue the last step
								end.add(Calendar.HOUR_OF_DAY, 1);
								
								for (Date date = start.getTime(); start.before(end); start.add(Calendar.HOUR, 1), date = start.getTime()) {
									// Do your job here with `date`.
									DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");  
                					String dateToPush = dateFormat.format(date);  
									
									oilConcentrationDates.add(dateToPush);
									beachedOilDates.add(dateToPush);
								}

							}
						
						}
		                // else if (eElement.getElementsByTagName("Name").item(0).getTextContent().contains(beachedLayer)) beachedOilDates.add(eElement.getElementsByTagName("Name").item(0).getTextContent());
		                // else if (eElement.getElementsByTagName("Name").item(0).getTextContent().contains(sstLayer)) tempeartureDates.add(eElement.getElementsByTagName("Name").item(0).getTextContent());
		                // else if (eElement.getElementsByTagName("Name").item(0).getTextContent().contains(vhmLayer)) waveHeightDates.add(eElement.getElementsByTagName("Name").item(0).getTextContent());
		                // else if (eElement.getElementsByTagName("Name").item(0).getTextContent().contains(vtzaLayer)) wavePeriodDates.add(eElement.getElementsByTagName("Name").item(0).getTextContent());
		                // else if (eElement.getElementsByTagName("Name").item(0).getTextContent().contains(vdirLayer)) waveDirectionsDates.add(eElement.getElementsByTagName("Name").item(0).getTextContent());
		                // else if (eElement.getElementsByTagName("Name").item(0).getTextContent().contains(currLayer)) currentsDates.add(eElement.getElementsByTagName("Name").item(0).getTextContent());
		                // else if (eElement.getElementsByTagName("Name").item(0).getTextContent().contains(currDirectionLayer)) currentsDirectionDates.add(eElement.getElementsByTagName("Name").item(0).getTextContent());
		                // else if (eElement.getElementsByTagName("Name").item(0).getTextContent().contains(windLayer)) windDates.add(eElement.getElementsByTagName("Name").item(0).getTextContent());
		                // else if (eElement.getElementsByTagName("Name").item(0).getTextContent().contains(windDirectionLayer)) windDirectionsDates.add(eElement.getElementsByTagName("Name").item(0).getTextContent());
		                
		            }
		        }
		        
		        simulationResponseJson.put("acquisitionId", correlationId);
		        simulationResponseJson.put("status", "SUCCEED");
		        JSONArray jsonArray = new JSONArray();
		        String wmsUrl = wmsURL.replace("GetCapabilities", "GetMap");

				System.out.println();
				System.out.println("--------------checking if some layers are already pushed -------------");
				System.out.println();

				//String event = Utils.getJSON("https://www.eos-viewer.com/api/okeanos/" + correlationId,  Utils.getLoginSession());
				String event = Utils.getJSON("https://www.eos-viewer.com/api/okeanos/" + correlationId);

				JsonElement root = new JsonParser().parse(event.substring(1, event.length() - 2));

				if (!root.getAsJsonObject().get("drifting_model").isJsonNull()){
					JsonElement drifting_model = new JsonParser().parse(root.getAsJsonObject().get("drifting_model").toString());
					JsonElement json_data = new JsonParser().parse(drifting_model.getAsJsonObject().get("json_data").toString());
					JsonElement wmss = new JsonParser().parse(json_data.getAsJsonObject().get("wmss").toString());
					
					int existingLayers = 0; 
					JsonArray jsArray = wmss.getAsJsonArray();
					existingLayers = jsArray.size();

					if (existingLayers == 2){
						
						JsonElement layerName = new JsonParser().parse(jsArray.get(0).getAsJsonObject().get("layer").toString());
						String lName = layerName.toString().replace("\"", "");
						if (!lName.equals(totalOilLayer) && !lName.equals(beachedLayer)) {

							System.out.println("!!! adding existing layers to our result !!!");
							JsonElement firstLayer = jsArray.get(0);
							JsonElement secondLayer = jsArray.get(1);

							JsonElement layerName1 = new JsonParser().parse(firstLayer.getAsJsonObject().get("layer").toString());
							JsonElement layerName2 = new JsonParser().parse(secondLayer.getAsJsonObject().get("layer").toString());

							JsonElement wmsDescription1 = new JsonParser().parse(firstLayer.getAsJsonObject().get("wmsDescription").toString());
							JsonElement wmsDescription2 = new JsonParser().parse(secondLayer.getAsJsonObject().get("wmsDescription").toString());

							JsonElement wmsUrl1 = new JsonParser().parse(firstLayer.getAsJsonObject().get("wmsUrl").toString());
							JsonElement wmsUrl2 = new JsonParser().parse(secondLayer.getAsJsonObject().get("wmsUrl").toString());
							
							JSONObject newResponseJson = new JSONObject();
							newResponseJson.put("layer", layerName1.toString().replace("\"", ""));
							newResponseJson.put("wmsUrl", wmsUrl1.toString().replace("\"", ""));
							newResponseJson.put("wmsDescription", wmsDescription1.toString().replace("\"", ""));
							newResponseJson.put("wmsDates", oilConcentrationDates);
							
							JSONObject newResponseJson2 = new JSONObject();
							newResponseJson2.put("layer", layerName2.toString().replace("\"", ""));
							newResponseJson2.put("wmsUrl", wmsUrl2.toString().replace("\"", ""));
							newResponseJson2.put("wmsDescription", wmsDescription2.toString().replace("\"", ""));
							newResponseJson2.put("wmsDates", oilConcentrationDates);

							jsonArray.put(newResponseJson);
							jsonArray.put(newResponseJson2);
							
						} else {
							System.out.println("The existing layers and the new one are the same");
							System.out.println("So we proceed with overwriting");
						}
					}
					System.out.println();

				} else {
					System.out.println("--------------NO layers are previously pushed -------------");
					System.out.println();
				}				

				System.out.println("totalOilLayer  -> " +totalOilLayer);
				System.out.println("totalOilLayer ISO  -> " +totalOilLayer.concat("-Isolines"));
		        jsonArray.put(creatingJson(oilConcentrationDates,  totalOilLayer, "Modeled concentration of oil found at the sea surface in tons/km2", wmsUrl));
		        jsonArray.put(creatingJson(beachedOilDates,  beachedLayer, "Modeled concentration of oil found permanently or temporarily attached to the coast in tons of oil per km of impacted coastline", wmsUrl));
				jsonArray.put(creatingJson(oilConcentrationDates,  isolinesLayer, "Isolines for modeled concentration of oil found permanently or temporarily attached to the coast in tons of oil per km of impacted coastline", wmsUrl));
				// jsonArray.put(creatingJson(currentsDates,  currLayer, "Movement of water from one location to another measured in meters per second (m/s). Ocean currents are usually generated by tidal changes, winds and variations in salinity and temperature. ", wmsUrl));
		        // jsonArray.put(creatingJson(currentsDirectionDates,  currDirectionLayer, "Direction of the water motion in degrees referenced to the geographic North", wmsURL.replace("GetCapabilities", "GetMap")));
		        // jsonArray.put(creatingJson(windDirectionsDates, windDirectionLayer, "direction from which the wind blows (in degrees) referenced to the North (0 degrees) and increasing clockwise", wmsUrl));
		        // jsonArray.put(creatingJson(windDates, windLayer, "wind speed is the rate (in meters per second) at which air moves from high to low pressure areas. Wind speed values are typically obtained at (or extrapolated to) 10m above the sea level", wmsUrl));
		        simulationResponseJson.put("wmss", jsonArray);


//		        System.out.println(tempeartureDates );
//		        System.out.println(waveHeightDates );
//		        System.out.println(wavePeriodDates );
//				System.out.println(waveDirectionsDates);
//				System.out.println(currentsDates );
//				System.out.println(currentsDirectionDates );
//				System.out.println(windDates );
//				System.out.println(windDirectionsDates );
				
				
				
		        }else {
		        	simulationResponseJson.put("acquisitionId", correlationId);
		        	System.out.println("NO RESULT ------>>>> ");
		        	simulationResponseJson.put("status", "FAILED/OOD");
			        JSONArray jsonArray = new JSONArray();
			        jsonArray.put(creatingJson(oilConcentrationDates,  "_", "mdslk totaloil description", ""));
			        simulationResponseJson.put("wmss", jsonArray);
		     //   	{"status":"FAILED/OOD","wmss":[{"wmsDescription":"WMS Description OilSpill","wmsUrl":"","wmsDates":[],"layer":"OilSpill"},{"wmsDescription":"WMS Description Currents","wmsUrl":"","wmsDates":[],"layer":"Currents"}]}		        	
		        }
		        
			} catch (Exception e) {
			   System.out.println(e);
			   System.out.println("Exc in 215: " + e.getMessage());
			}
		
		// simulation response finished
		System.out.println("simulationResponseJson tostring -? " + simulationResponseJson.toString());

		//		String returningURL = "https://www.eos-viewer.com/api/analyst/post/drifting/"+simulationId;
		String returningURL = "https://www.eos-viewer.com/api/analyst/post/drifting";
		System.out.println();
		System.out.println("returningURL-> " + returningURL);
		
		Utils.sendingResponseToEgeos(returningURL, simulationResponseJson.toString());
		//System.out.println("WARNING!!! I AM NOT SENDING RESPONSE MESSAGE ---!!!");
		return Response.ok("ok", MediaType.APPLICATION_JSON).build();
		
	}

	private JSONObject creatingJson(ArrayList<String> datesList, String layerDescription, String descriptionLayer, String wmsUrlResponse) {
		
        JSONObject totalOilResponseJson = new JSONObject();
        totalOilResponseJson.put("layer", layerDescription);
        totalOilResponseJson.put("wmsUrl", wmsUrlResponse);
        totalOilResponseJson.put("wmsDescription", descriptionLayer);
        
        // JSONArray wmsDatesArray = new JSONArray();
        
        // for (String s: datesList) {
        // 	String tmp = s.replace(layerDescription, "");
        // 	wmsDatesArray.put(tmp.substring(6,8) + "-" + tmp.substring(4,6) + "-" + tmp.substring(0,4) + " " + tmp.substring(9,11) + ":00");
        // }
        totalOilResponseJson.put("wmsDates", datesList);
        
        // JSONArray wmsArray = new JSONArray();
		// wmsArray.put(wmsDatesArray);
		
        return totalOilResponseJson;
	}
	
	
}
