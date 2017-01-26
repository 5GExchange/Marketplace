package eu.tnova.nfs.client;

import java.net.MalformedURLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;

public class WSClient {
//	private final static int receiveTimeout = 5*60*1000;
	private final static int receiveTimeout = 0;
	private final static int connectTimeout = 10000;
	
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

	public WSClient() {
		super();
	}

	public WebClient getClient (String url, MediaType type) 
			throws MalformedURLException {
		System.out.println("  url : "+url);
		WebClient webClient = WebClient.create(url)
				.type(type)
				.accept(MediaType.APPLICATION_JSON)
				.accept(MediaType.MULTIPART_FORM_DATA_TYPE);
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

	public int printResponse(Response response, String responseBody) {
		if ( response==null )
			return(-3);
		Integer status = response.getStatus();
		System.out.println("  "+status+" : "+Status.fromStatusCode(status)+"\n");
		if ( responseBody!=null )
			System.out.println("  "+responseBody); 
		return(status);
	}
	
}
