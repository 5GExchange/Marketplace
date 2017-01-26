package eu.tnova.nfs.valves;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;

import eu.tnova.nfs.exception.ValidationException;
import eu.tnova.nfs.producers.EnvDefaulValueEnum;
import eu.tnova.nfs.producers.EnvValue;

public final class GatekeeperAuthenticationValve extends ValveBase {
	public final static String AUTH_TOKEN = "X-Auth-Token";
	public final static String AUTH_SERVICE_KEY = "X-Auth-Service-Key";
	public final static String AUTH_UID = "X-Auth-Uid";	
	public final static String AUTH_PASSWORD = "X-Auth-Password";	

    private final static String info = "eu.tnova.nfs.valves.gatekeeper/1.0";
	private final static int receiveTimeout = 10000;
	private final static int connectTimeout = 10000;

	private static Log log;
	private static String gatekeeper;
	private static String serviceKey;

    public GatekeeperAuthenticationValve() {
		super();
	}

	@Override
    public String getInfo() {
        return (info);
    }
	@Override
	protected synchronized void startInternal() throws LifecycleException {
		super.startInternal();
		if ( log==null )
			log = LogFactory.getLog(GatekeeperAuthenticationValve.class);
		if ( gatekeeper==null )
			gatekeeper = getConfigValue(EnvValue.gatekeeperUrl);
		if ( serviceKey==null )
			serviceKey = getConfigValue(EnvValue.nfsServiceKey);
		log.info("gatekeeperUrl="+gatekeeper+", serviceKey="+serviceKey);
	}


	@Override
	public boolean isAsyncSupported() {
		return false;
	}
	@Override
	public void setAsyncSupported(boolean asyncSupported) {
	}
	

	public void invoke(Request request, Response response) throws IOException, ServletException {
		if ( !serviceKey.isEmpty() && !response.isError() ) {
			try {
				String authToken = checkRequest(request);
				log.info("User Token : " + authToken);
				HttpSession session = request.getSession();
				log.info("Session ID : " + session.getId());
				String sessionAuthToken = (String) session.getAttribute(AUTH_TOKEN);
				if ( sessionAuthToken==null || !sessionAuthToken.equals(authToken) ) {
					URL gatekeeperURL = checkGatekeeperURL(authToken);
					log.info("Using gatekeeper : " + gatekeeperURL.toString());
					WebClient webClient = getClient(gatekeeperURL);
					try {
						webClient.get(GatekeeperValidateTokenResponse.class);
					} catch (Exception e) {
						responseError(response, Status.INTERNAL_SERVER_ERROR.getStatusCode(), 
								"Failed connection with gatekeeper "+gatekeeper);
						return;
					}
					checkGatekeeperResponse(webClient);
					log.info("authenticated user "+authToken);
					session.setAttribute(AUTH_TOKEN, authToken);
				} else {
					log.info("user "+authToken+" already authenticated");
				}
			} catch (ValidationException e) {
				responseError(response, e.getStatus().getStatusCode(), e.getMessage());
				return;
			} catch (Exception e) {
				responseError(response, Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage());
				return;
			}
		}
		getNext().invoke(request, response); 
	}

	private String checkRequest(Request request) throws ValidationException {
		// check authentication token field into request
		String authToken = request.getHeader(AUTH_TOKEN);
		if ( authToken==null ) 
			throw new ValidationException(
					"missing header field "+AUTH_TOKEN, Status.UNAUTHORIZED);
		return authToken;
	}
	private URL checkGatekeeperURL(String authToken) throws ValidationException {
		// check gatekeeper address
		if ( gatekeeper==null || gatekeeper.isEmpty() )
			throw new ValidationException(
					"gatekeeper not configured", Status.INTERNAL_SERVER_ERROR);
		URL gatekeeperURL = null;
		try {
			gatekeeperURL = new URL(gatekeeper+"/token/validate/"+authToken); 
		} catch (MalformedURLException e) {
			throw new ValidationException(
					"invalid gatekeeper URL : "+e.getMessage(), Status.INTERNAL_SERVER_ERROR);
		}
		return gatekeeperURL;
	}
	private WebClient getClient (URL url) {
		System.out.println("  url : "+url);
		WebClient webClient = WebClient.create(url.toString())
				.type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
		webClient.getHeaders().add(AUTH_SERVICE_KEY, serviceKey);
		HTTPConduit conduit = WebClient.getConfig(webClient).getHttpConduit();
		// timeout (in mSec)
		conduit.getClient().setReceiveTimeout(receiveTimeout);
		conduit.getClient().setConnectionTimeout(connectTimeout);
		TLSClientParameters tlsParams = conduit.getTlsClientParameters();
		if (tlsParams == null) {
			tlsParams = new TLSClientParameters();
			conduit.setTlsClientParameters(tlsParams);
		}
		tlsParams.setTrustManagers(trustAllCerts);
		//disable CN check
		tlsParams.setDisableCNCheck(true);
		conduit.setTlsClientParameters(tlsParams);
		return webClient;
	}
	private void checkGatekeeperResponse(WebClient webClient) 
			throws ValidationException {
//		GatekeeperValidateTokenResponse resp = 
//				(GatekeeperValidateTokenResponse) webClient.getResponse().getEntity();
		int status = webClient.getResponse().getStatus();
//		if ( resp==null )
//			throw new ValidationException(
//					"gatekeeper error "+status, Status.INTERNAL_SERVER_ERROR);
//		if ( resp.getInfo().size()==0 )
//			throw new ValidationException(
//					"gatekeeper response error : missing info data", Status.INTERNAL_SERVER_ERROR);
		if ( status != Status.OK.getStatusCode() ) {
//			if ( !resp.getInfo().get(0).getMsg().contains("Failed") )
//				throw new ValidationException(
//						"gatekeeper response error : "+status+" without Failed message", 
//						Status.INTERNAL_SERVER_ERROR);
			throw new ValidationException(
					"user unauthorized", Status.UNAUTHORIZED);
//		} else {
//			if ( !resp.getInfo().get(0).getMsg().contains("Successful") )
//				throw new ValidationException(
//						"gatekeeper response error : "+status+" without Successful message", 
//						Status.INTERNAL_SERVER_ERROR);
		}
		
	}

	private void responseError(Response response, int status, String message) {
    	log.warn(""+message);
		try {
	        response.setError();
	        response.setErrorReported();
			response.setStatus(status);
			response.setContentType(MediaType.TEXT_PLAIN);
			response.getWriter().print(message);
		} catch (IOException e) {
		}
	}

	private String getConfigValue(String nameValue) {
		if ( nameValue==null || nameValue.isEmpty() )
			return null;
		String var=System.getProperty(nameValue);
		if ( var!=null )
			return var;
		var=System.getenv(nameValue);
		if ( var!=null )
			return var;
		return EnvDefaulValueEnum.getValueByName(nameValue);
	}

	private TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
		public void checkClientTrusted(
				java.security.cert.X509Certificate[] certs, String authType) {
		}
		public void checkServerTrusted(
				java.security.cert.X509Certificate[] certs, String authType) {
		}
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	}};

	public String getGatekeeper() {
		return gatekeeper;
	}
	public String getServiceKey() {
		return serviceKey;
	}

}
