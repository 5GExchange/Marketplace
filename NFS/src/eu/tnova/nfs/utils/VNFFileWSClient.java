package eu.tnova.nfs.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.cxf.transport.http.HTTPConduit;

public class VNFFileWSClient {
//	private final static int receiveTimeout = 5*60*1000;
	private final static int receiveTimeout = 0;
	private final static int connectTimeout = 10000;
	private final static String lineEnd = "\r\n";
	private final static String twoHyphens = "--";
	private final static String boundary = "***232404jkg4220957934FW**";
	
	private static TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
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
	private static HostnameVerifier allHostsValid = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };
	
	public static void main(String[] args) throws Exception {
		if ( args.length<5 ) {
			System.out.println("Input error : less that 3 parameters");
			System.out.println("Arguments : ");
			System.out.println("  command (upload|update|delete)");
			System.out.println("  URL");
			System.out.println("  Path file");
			System.out.println("  MD5 checksum");
			System.out.println("  Provider Id");
			System.exit(-1);
		}
		String url = args[1];
		String filePath = args[2];
		String md5sum = args[3];
		String providerId = args[4];
		File file = new File(filePath);
		ContentDisposition cd = new ContentDisposition("form-data; name=\"file\"; filename=\""+file.getName()+"\"");
		InputStream stream;
		Response response = null;
		Integer status = null;
		try {
			stream = new FileInputStream(file);
			Attachment att = new Attachment(file.getName(), stream, cd);
			MultipartBody body = new MultipartBody(att);
			System.out.println("Body ready");
			//		String responseString = null;
			switch (args[0]) {
			case "upload":
				response = getClient(
						url, MediaType.MULTIPART_FORM_DATA_TYPE)
						.post(body); 
				if ( response!=null )
					status = response.getStatus();
				break;
			case "upload2":
				try {
					status = sendFile(url, file, "POST", md5sum, providerId);
				} catch (Exception e) {
				}
				break;
			case "update":
				response = getClient(
						url+"/"+file.getName(), MediaType.MULTIPART_FORM_DATA_TYPE)
						.put(body);
				if ( response!=null )
					status = response.getStatus();
				break;
			case "update2":
				try {
					status = sendFile(url+"/"+file.getName(), file, "PUT", md5sum, providerId);
				} catch (Exception e) {
				}
				break;
			case "delete":
				response = getClient(
						url+"/"+file.getName(),MediaType.MULTIPART_FORM_DATA_TYPE)
						.delete();
				if ( response!=null )
					status = response.getStatus();
				break;
			case "get":
				response = getClient(
						url+"/"+file.getName(),MediaType.MULTIPART_FORM_DATA_TYPE)
						.get();
				if ( response!=null )
					status = response.getStatus();
				break;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-2);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.exit(-3);
		}
		if ( status==null )
			System.exit(-4);
		System.out.println(status);
		System.exit(status);
	}

	private static WebClient getClient (String url, MediaType type) 
			throws MalformedURLException {
		WebClient webClient = WebClient.create(url).type(type).accept(MediaType.APPLICATION_JSON);
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
		
		System.out.println("Client ready : url="+url);
		return webClient;
	}
	
    private static int sendFile (String serverURL, File file, String method,
    		String md5sum, String providerId) throws Exception {
		System.out.println("upload "+file.getName()+" to "+serverURL);
        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        // creates a HTTP connection
        HttpURLConnection conn = (HttpURLConnection) new URL(serverURL).openConnection();
        conn.setUseCaches(false);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestMethod(method);
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        // write body
        OutputStream output = conn.getOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"), true);
        // Send binary file.
        writer.append(twoHyphens + boundary).append(lineEnd);
        writer.append("Content-Disposition: form-data; name=\"file\";"
        		+ " filename=\"" + file.getName() + "\"").append(lineEnd);
        writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(file.getName())).append(lineEnd);
        writer.append("Content-Transfer-Encoding: binary").append(lineEnd);
        writer.append("MD5SUM: "+md5sum).append(lineEnd);
        writer.append("Provider-ID: "+providerId).append(lineEnd);
        writer.append(lineEnd).flush();
        
		//////////////
		FileInputStream is = new FileInputStream(file); 
		byte[] buffer = new byte[1024*100];
		int bytes = 0;
		try {
			while ((bytes = is.read(buffer)) != -1) {
				output.write(buffer, 0, bytes);
				output.flush();
			}
		} finally {
			is.close();
		}       
		//////////////
//        Files.copy(file.toPath(), output);
//        output.flush(); 						// Important before continuing with writer!
		//////////////
        
        writer.append(lineEnd).flush(); 		// CRLF is important! It indicates end of boundary.
        // End of multipart/form-data.
        writer.append(twoHyphens + boundary + twoHyphens).append(lineEnd).flush();
        // read response
        int code = conn.getResponseCode();
        String message = conn.getResponseMessage();
        System.out.println("Response = "+code+" : "+message);
        conn.disconnect();
        return code;
    }

}
