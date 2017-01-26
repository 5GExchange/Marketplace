package eu.tnova.nfs.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.cxf.jaxrs.client.WebClient;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class NFSVnfd {
	private static WSClient wsClient = new WSClient();
	private static int vnfdId = -1;

	public static void main(String[] args) throws Exception {
		if ( args.length<2 )
			help();
		int exitValue = -1;
		String url = args[0]+"/NFS/vnfds";
		System.out.println("op. : "+args[1]);
		switch (args[1]) {
		case "list":
			exitValue = list(url);
			break;
		case "delete":
			if ( args.length>3 )
				help();
			if ( args.length==3 )
				url += "/"+args[2];
			exitValue = delete(url);
			break;
		case "add":
			if ( args.length!=3 )
				help();
			File ulFile = checkFile(args[2]);
			if ( ulFile == null )
				break;
			exitValue = add(url, ulFile);
			break;
		case "modify":
			if ( args.length!=4 )
				help();
			File upFile = checkFile(args[3]);
			if ( upFile == null )
				break;
			url += "/"+args[2];
			exitValue = modify(url, upFile);
			break;
		case "get":
			if ( args.length!=4 )
				help();
			url += "/"+args[2];
			exitValue = get(url, args[3]);
			break;
		case "test":
			if ( args.length!=3 )
				help();
			File testFile = checkFile(args[2]);
			if ( testFile == null )
				break;
			System.out.println("\n>>>> list ");
			list(url);
			System.out.println("\n>>>> add ");
			add(url, testFile);
			String urlVnfd = url+"/"+vnfdId;
			System.out.println("\n>>>> list ");
			list(url);
			System.out.println("\n>>>> get ");
			get(urlVnfd, args[2]+".read");
			System.out.println("\n>>>> update ");
			testFile = checkFile(args[2]+".read");
			modify(urlVnfd, testFile);
			System.out.println("\n>>>> delete ");
			delete(urlVnfd);
			System.out.println("\n>>>> list ");
			list(url);
			exitValue=0;
			break;
		default:
			help();
		}
		System.exit(exitValue); 
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	
	private static int add (String url, File file)
			throws MalformedURLException, FileNotFoundException, IOException, Exception {
		WebClient webClient = wsClient.getClient(url, MediaType.APPLICATION_JSON_TYPE);
		String str = FileUtils.readFileToString(file);
		String responseBody = webClient.post(str, String.class);
		JsonElement idElement = new Gson().fromJson(responseBody, JsonElement.class)
				.getAsJsonObject().get("vnfd_id");
//				.getAsJsonObject().get(VNFD.VNFD_ID);
		if ( idElement==null )
			throw new Exception("Vnfd Id not set into response :\n  "+responseBody);
		vnfdId = idElement.getAsInt();
		File responseFile = new File(file.getAbsolutePath()+".addResponse");
		FileUtils.writeStringToFile(responseFile, responseBody);
		return (wsClient.printResponse(webClient.getResponse(), responseBody));
	}

	private static int modify (String url, File file) 
			throws MalformedURLException, FileNotFoundException, IOException {
		WebClient webClient = wsClient.getClient(url, MediaType.APPLICATION_JSON_TYPE);
		String str = FileUtils.readFileToString(file);
		String responseBody = webClient.put(str, String.class);
		File responseFile = new File(file.getAbsolutePath()+".modifyResponse");
		FileUtils.writeStringToFile(responseFile, responseBody);
		return (wsClient.printResponse(webClient.getResponse(), responseBody));
	}

	private static int get(String url, String filePath) 
			throws MalformedURLException, FileNotFoundException, IOException {
		WebClient webClient = wsClient.getClient(url, MediaType.APPLICATION_JSON_TYPE);
		String responseBody = webClient.get(String.class);
		File responseFile = new File(filePath);
		FileUtils.writeStringToFile(responseFile, responseBody);
		return (wsClient.printResponse(webClient.getResponse(), responseBody));
	}
	
	private static int delete(String url) throws MalformedURLException {
		WebClient webClient = wsClient.getClient(url, MediaType.APPLICATION_JSON_TYPE);
		Response response = webClient.delete();
		return (wsClient.printResponse(response, null));
	}

	private static int list(String url) throws MalformedURLException {
		WebClient webClient = wsClient.getClient(url, MediaType.APPLICATION_JSON_TYPE);
		String responseBody = webClient.get(String.class);
		Response response = webClient.getResponse();
		return (wsClient.printResponse(response, responseBody));
	}

	private static File checkFile (String filePath) {
		File file = new File(filePath);
		if ( !file.exists() || !file.isFile() ) {
			System.out.println("ERROR : file "+filePath+" not found or is not a file");
			return null;
		}
		return file;
	}

	private static void help() {
		System.out.println("Arguments : ");
		System.out.println("  URL         - ex. https://83.212.108.105:8443");
		System.out.println("  command     - (list|add|get|modify|delete)");
		System.out.println("  (vnfdId)    - (cmd get|modify|delete) - ex. 1132  ");
		System.out.println("  (Path file) - (cmd upload|download|update) - ex. D:\\tmp\\descfile.vnfd");
		System.out.println("Example: ");
		System.out.println("  java -jar NFSVnfd.jar https://83.212.108.105:8443 add D:\\tmp\\descfile.vnfd");
		System.out.println("  java -jar NFSVnfd.jar https://83.212.108.105:8443 list");
		System.out.println("  java -jar NFSVnfd.jar https://83.212.108.105:8443 get 1132 D:\\tmp\\descfileRead.vnfd");
		System.out.println("  java -jar NFSVnfd.jar https://83.212.108.105:8443 modify 1132 D:\\tmp\\descfile2.vnfd");
		System.out.println("  java -jar NFSVnfd.jar https://83.212.108.105:8443 delete 1132");
		System.exit(-1);
	}




}
