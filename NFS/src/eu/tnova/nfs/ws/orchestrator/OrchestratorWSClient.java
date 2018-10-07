package eu.tnova.nfs.ws.orchestrator;

import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.client.ServerWebApplicationException;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import eu.tnova.nfs.entity.VNFDescriptor;
import eu.tnova.nfs.exception.ValidationException;
import eu.tnova.nfs.producers.EnvValue;
import eu.tnova.nfs.valves.GatekeeperAuthenticationValve;

@SuppressWarnings("serial")
public class OrchestratorWSClient implements Serializable {
	@Inject	private Logger log;
	@Inject @EnvValue(EnvValue.orchestratorUrl) private String orchestrator;
	@Inject @EnvValue(EnvValue.nfsServiceKey) private String serviceKey;

	final static long CONNECTION_TIMEOUT = 15000L;
	final static long RECEIVE_TIMEOUT = 10000L;
	private final static int GET_LIST_SIZE = 100;
	
	OrchestratorWSClient () {
	}

	public Map<Integer,VNFDescriptor> get_VNFDescriptors(String userToken) 
			throws ValidationException, Exception {
		Map<Integer,VNFDescriptor> vnfds = new HashMap<Integer,VNFDescriptor>();
		int offset=0;
		while (true) {
			ArrayList<String> vnfdsList = get_VNFDescriptors(userToken, offset, GET_LIST_SIZE);
			for ( String jsonVNFD : vnfdsList ) {
				VNFDescriptor vnfd = new VNFDescriptor(jsonVNFD);
				vnfd.setVnfCreated(true);
				vnfds.put(vnfd.getId(), vnfd);
			}
			if ( vnfdsList.isEmpty() || vnfdsList.size() < GET_LIST_SIZE )
				break;
			offset += GET_LIST_SIZE;
		}
		return vnfds;
	}
	private ArrayList<String> get_VNFDescriptors(String userToken, int offset, int size) 
			throws ValidationException, Exception  {
		log.debug("get_VNFDescriptors : offset={}, limit={}",offset, size); 
		ArrayList<String> vnfds = new ArrayList<String>();
		WebClient webClient = getClient(userToken)
				.query("offset", offset).query("limit", size);
		if ( webClient==null )
			return vnfds;
		try {
			webClient.accept(MediaType.APPLICATION_JSON);
			Response response = webClient.get();
			log.debug("Response {} received from Orchestrator",response.getStatus()); 
			InputStream responseStream = (InputStream) response.getEntity();
			String responseString = IOUtils.toString(responseStream, StandardCharsets.UTF_8);
			Gson gson = new Gson();
			JsonElement[] jsonVnfs = gson.fromJson(responseString, JsonElement[].class);
			log.debug("Found {} vnf on Orchestrator from offest {}",jsonVnfs.length,offset); 			
			for ( JsonElement jsonVnf : jsonVnfs ) {
				JsonElement jsonVnfd = jsonVnf.getAsJsonObject().get("vnfd");
				vnfds.add( gson.toJson(jsonVnfd) );
			}
			offset += 100;
		} catch (ServerWebApplicationException e) {
			throw new Exception("Wrong Response ("+e.getStatus()+") : "+e.getMessage());
		} finally { 
		   webClient.reset(); 
		} 
		log.debug("get_VNFDescriptors : found {} vnf\n{}", vnfds.size(), vnfds); 
		return vnfds;
	}
	
	public void create_VNF(VNFDescriptor vnfd, String userToken) throws Exception {
		create_VNF(null, vnfd, userToken);
	}
	public void create_VNF(WebClient webClient, VNFDescriptor vnfd, String userToken) throws Exception {
		log.info("Send create VNF to Orchestrator");
		if ( webClient==null ) 
			webClient = getClient(userToken);
		if ( webClient==null )
			return;
		Response response = null;
		String responseString = null;
		try {
			String msg = getRequestMessage(vnfd);
			response = webClient.post(msg);
		} catch (ServerWebApplicationException e) {
			throw new Exception("Wrong Response ("+e.getStatus()+") : "+e.getMessage());
		} finally { 
		   webClient.reset(); 
		} 
		log.debug("Response {} received from Orchestrator",response.getStatus()); 
		InputStream responseStream = (InputStream) response.getEntity();
		responseString = IOUtils.toString(responseStream, StandardCharsets.UTF_8);
		if ( response.getStatus() != Status.CREATED.getStatusCode() )
			throw new ValidationException(
					"Error Response from Orchestrator ("+response.getStatus()+") :\n"+responseString,
					Status.fromStatusCode(response.getStatus()) );
		vnfd.setVnfCreated(true);
		log.info("VNF created");
	}
	
	public void update_VNF(VNFDescriptor vnfd, String userToken) 
			throws ValidationException, Exception {
		update_VNF(null, vnfd, userToken);
	}
	public void update_VNF(WebClient webClient, VNFDescriptor vnfd, String userToken) 
			throws ValidationException, Exception {
		log.info("Send update VNF to Orchestrator");
		if ( webClient==null ) 
			webClient = getClient(userToken).path(""+vnfd.getId());
		if ( webClient==null )
			return;
		Response response = null;
		String responseString = null;
		try {
			String msg = getRequestMessage(vnfd);
			response = webClient.put(msg);
		} catch (ServerWebApplicationException e) {
			throw new Exception("Wrong Response ("+e.getStatus()+") : "+e.getMessage());
		} finally { 
		   webClient.reset(); 
		} 
		log.debug("Response {} received from Orchestrator",response.getStatus()); 
		InputStream responseStream = (InputStream) response.getEntity();
		responseString = IOUtils.toString(responseStream, StandardCharsets.UTF_8);
		if ( response.getStatus() != Status.CREATED.getStatusCode() )
			throw new ValidationException(
					"Error Response from Orchestrator ("+response.getStatus()+") :\n"+responseString,
					Status.fromStatusCode(response.getStatus()) );
		vnfd.setVnfCreated(true);
		log.info("VNF updated");
	}

	public void delete_VNF(VNFDescriptor vnfd, String userToken) 
			throws ValidationException, Exception {
		delete_VNF(null, vnfd, userToken);
	}
	public void delete_VNF(WebClient webClient, VNFDescriptor vnfd, String userToken) 
			throws ValidationException, Exception {
		log.info("Send delete VNF to Orchestrator");
		if ( webClient==null ) 
			webClient = getClient(userToken).path(""+vnfd.getId()+"/");
		if ( webClient==null )
			return;
		Response response = null;
		try {
			response = webClient.delete();
		} catch (Exception e) {
			throw new Exception("No response from Orchestrator : "+e.getMessage());
		} finally { 
		   webClient.reset(); 
		} 
		if ( response.getStatus() != Status.NO_CONTENT.getStatusCode() ) {
			throw new ValidationException(
					"Error Response from Orchestrator ("+response.getStatus()+")",
					Status.fromStatusCode(response.getStatus()) );
		}
		log.debug("Response {} received from Orchestrator", response.getStatus()); 
		vnfd.setVnfCreated(false);
		log.info("VNF removed");
	}

	public WebClient getClient (String userToken) 
			throws MalformedURLException {
		if ( orchestrator.isEmpty() || orchestrator.equals("0.0.0.0" )) {
			log.info("Orchestrator host unknown, operation not done");
			return null;
		}
		log.info("Using orchestrator URL : {}", orchestrator);
		WebClient webClient = WebClient.create(orchestrator).
				type(MediaType.APPLICATION_JSON).
				accept(MediaType.APPLICATION_JSON);
		// timeout (in mSec)
		HTTPConduit conduit = WebClient.getConfig(webClient).getHttpConduit();
		conduit.getClient().setReceiveTimeout(RECEIVE_TIMEOUT);
		conduit.getClient().setConnectionTimeout(CONNECTION_TIMEOUT);
		conduit.getClient().setAllowChunking(false);
		// add user token
		if ( !serviceKey.isEmpty() && userToken!=null ) {
			webClient.getHeaders().add(GatekeeperAuthenticationValve.AUTH_TOKEN, userToken);
		}
		return webClient;
	}
	private String getRequestMessage(VNFDescriptor vnfd) {
		Gson gson = new Gson();
		JsonObject reqObj = new JsonObject();
		reqObj.addProperty("name", "TEMP");
		reqObj.addProperty("vnf-manager", "TEMP");
		JsonElement vnfdElement = gson.fromJson(vnfd.getJson(), JsonElement.class);
		reqObj.getAsJsonObject().add("vnfd", vnfdElement);
		String msg = reqObj.toString();
		log.debug("Request Message to Orchestrator:\n{}",msg);
		return msg;
	}
	
	public static void main(String [ ] args) {
		try {
			OrchestratorWSClient client = new OrchestratorWSClient();
			client.orchestrator="http://10.10.1.61:4000/vnfs";
			client.log=LogManager.getLogger();
			client.serviceKey="";
			client.get_VNFDescriptors(null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
