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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilderFactory;

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
	@Path("login-test")
	@Produces("application/json")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response testingLoginResponse() throws JSONException, UnsupportedEncodingException, IOException {
		System.out.println("you have received a new e-geos TEST ---------------------------- ");

		Utils.getLoginSession();

		//JSONObject jsonObject = new JSONObject(message);
		//String eventId = jsonObject.get("acquisitionId").toString();
		//int oilSpillSImulationId = Utils.fromEgeosShapefileToOilSpillSimulationRequest(eventId);
		// int oilSpillSImulationId = Utils.fromEgeosShapefileToOilSpillSimulationRequest(message);
		
		// SimulationRequest sr = new SimulationRequest(oilSpillSImulationId, 0);
		// return Response.ok(sr, MediaType.APPLICATION_JSON).build();
		return Response.ok("ok", MediaType.APPLICATION_JSON).build();
		
	}

	@POST
	@Path("create-simulation")
	@Produces("application/json")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response creatingOilSpillSimulationRequest(String message) throws JSONException, UnsupportedEncodingException, IOException {
		System.out.println("you have received a new e-geos simulation request ---------------------------- ");

		//JSONObject jsonObject = new JSONObject(message);
		//String eventId = jsonObject.get("acquisitionId").toString();
		//int oilSpillSImulationId = Utils.fromEgeosShapefileToOilSpillSimulationRequest(eventId);
		int oilSpillSImulationId = Utils.fromEgeosShapefileToOilSpillSimulationRequest(message);
		
		SimulationRequest sr = new SimulationRequest(oilSpillSImulationId, 0);
		return Response.ok(sr, MediaType.APPLICATION_JSON).build();
		
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
		String username = ""; 
		String tenantId = "";
		String[] correlationIdParts = correlationId.split("&username"); // sat_oil_poly.2***TEST***&username=cmcc&tenantId=qwerty12345qwerty
		
		if (correlationIdParts.length == 2){
			String[] tenantIdAndUsername = getUsernameAndTenantId(correlationId); 
			if (tenantIdAndUsername != null && tenantIdAndUsername.length >= 2) {
				username = tenantIdAndUsername[0];
				tenantId = tenantIdAndUsername[1];
			}
		}
		
		correlationId = correlationIdParts[0]; // 004
		String dss = jsonObject.get("dss").toString() ; // dss: sar_op oilspill_op

		String ovEnv = "prod";
		String baseEnv = "https://ov-"+ovEnv+".cmcc-opa.eu";
		String wmsURL = baseEnv + "/cgi-bin/mapserv?map=witoil_";
		if (ovEnv != ("prod")) wmsURL = baseEnv + "/cgi-bin/mapserv?map=/srv/ov/backend/support/witoil/simulations/mapfiles/"+simulationId+"/witoil_"+simulationId+".map&REQUEST=GetCapabilities&service=WMS";
		else wmsURL = wmsURL.concat(simulationId).concat("&REQUEST=GetCapabilities&service=WMS");
	
		System.out.println("simulationId: " + simulationId);
		System.out.println("username: " + username );
		System.out.println("tenantId: " + tenantId );
		System.out.println("status: " + status );
		System.out.println("serviceId: " + serviceId);
		System.out.println("correlationId: " + correlationId );
		System.out.println("dss: " + dss );
		System.out.println("wmsURL: " + wmsURL );
		
		// creating response ...
		String oilConcentrationDates = "";
		String beachedOilDates = "";

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
				// String isolinesLayer = totalOilLayer.concat("-Isolines"); 

				// N THICK Polygon
				// if (correlationId.contains("_thick")){
				// 	totalOilLayer = totalOilLayer.concat("-Thick");
				// 	beachedLayer = beachedLayer.concat("-Thick");
				// 	// isolinesLayer = isolinesLayer.concat("-Thick");
				// 	correlationId = correlationId.replace("_thick", "");
				// }
		        
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

							oilConcentrationDates = dateRange;
							beachedOilDates = dateRange;
					
						}
		            }
		        }
		        
		        simulationResponseJson.put("acquisition_id", correlationId);
		        simulationResponseJson.put("status", "SUCCEED");
				simulationResponseJson.put("processing_code", 1);
				simulationResponseJson.put("netcdf_repository", baseEnv+"/backend/support/witoil/simulations/mapfiles/"+simulationId+"/"+simulationId+".tgz");
		        JSONArray jsonArray = new JSONArray();
		        //String wmsUrl = wmsURL.replace("GetCapabilities", "GetMap");

				System.out.println("totalOilLayer  -> " +totalOilLayer);
				jsonArray.put(creatingJson(oilConcentrationDates,  totalOilLayer, "Modeled concentration of oil found at the sea surface in tons/km2", wmsURL));
		        jsonArray.put(creatingJson(beachedOilDates,  beachedLayer, "Modeled concentration of oil found permanently or temporarily attached to the coast in tons of oil per km of impacted coastline", wmsURL));
				simulationResponseJson.put("wmss", jsonArray);

				// including ossi.json

				JSONObject ossiJsonObject = new JSONObject();
				JSONObject reportJsonObject = new JSONObject();

				try {
					System.out.println("... retreiving REPORT from: "
							+ baseEnv+"/backend/support/witoil/simulations/mapfiles/" + simulationId + "/ossi.json");
							ossiJsonObject = new JSONObject(
							Utils.getJSON(baseEnv+"/backend/support/witoil/simulations/mapfiles/" + simulationId + "/ossi.json"));
							reportJsonObject = new JSONObject(
							Utils.getJSON(baseEnv+"/backend/support/witoil/simulations/mapfiles/" + simulationId + "/report/report.json"));
				} catch (Exception e) {
					// Block of code to handle errors
					System.out.println("... ossi does not exist! " + e);
				}
				System.out.println("... ossi --> " + ossiJsonObject);
				simulationResponseJson.put("oilspill_impact", ossiJsonObject);
				System.out.println("... report --> " + reportJsonObject);
				simulationResponseJson.put("report", reportJsonObject);

				// done
				
		        }else {
					// in case of failed simulation
		        	simulationResponseJson.put("acquisition_id", correlationId);
		        	System.out.println("NO RESULT DUE TO A FAILED SIMULATION ");
		        	simulationResponseJson.put("status", "FAILED");
			        JSONArray jsonArray = new JSONArray();
			        //jsonArray.put(creatingJson(oilConcentrationDates,  "_", "mdslk totaloil description", ""));
			        simulationResponseJson.put("wmss", jsonArray);
					simulationResponseJson.put("processing_code", -1);
					simulationResponseJson.put("netcdf_repository", "N.A.");
					simulationResponseJson.put("oilspill_impact", "N.A.");
					simulationResponseJson.put("report", "N.A.");
		     //   	{"status":"FAILED/OOD","wmss":[{"wmsDescription":"WMS Description OilSpill","wmsUrl":"","wmsDates":[],"layer":"OilSpill"},{"wmsDescription":"WMS Description Currents","wmsUrl":"","wmsDates":[],"layer":"Currents"}]}		        	
		        }
		        
			} catch (Exception e) {
			   System.out.println(e);
			   System.out.println("Exc in 215: " + e.getMessage());
			}
		
		// simulation response finished
		System.out.println("simulationResponseJson tostring -? " + simulationResponseJson.toString());

		//		String returningURL = "https://www.eos-viewer.com/api/analyst/post/drifting/"+simulationId;
		// String returningURL = "https://www.eos-viewer.com/api/analyst/post/drifting";
		// System.out.println();
		// System.out.println("returningURL-> " + returningURL);
		
		Utils.sendingResponseToEgeos(simulationResponseJson.toString(), tenantId, username);
		// System.out.println("WARNING!!! I AM NOT SENDING RESPONSE MESSAGE ---!!!");
		return Response.ok("ok", MediaType.APPLICATION_JSON).build();
		
	}

	private JSONObject creatingJson(String dimenstionTimeTag, String layerDescription, String descriptionLayer, String wmsUrlResponse) {
		
        JSONObject totalOilResponseJson = new JSONObject();
        totalOilResponseJson.put("layer", layerDescription);
        totalOilResponseJson.put("wms_url", wmsUrlResponse);
        totalOilResponseJson.put("wms_description", descriptionLayer);

        totalOilResponseJson.put("time_range", dimenstionTimeTag);
        
        // JSONArray wmsArray = new JSONArray();
		// wmsArray.put(wmsDatesArray);
		
        return totalOilResponseJson;
	}

	private static String[] getUsernameAndTenantId(String queryString) {
        Pattern pattern = Pattern.compile("username=([^&]+)&tenantId=([^&]+)");
        Matcher matcher = pattern.matcher(queryString);

        if (matcher.find()) {
            String username = matcher.group(1); // estrae il valore di username
            String tenantId = matcher.group(2); // estrae il valore di tenantId
            return new String[]{username, tenantId};
        } else {
            return null; // ritorna null se non trova corrispondenze
        }
    }

}
