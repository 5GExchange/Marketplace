package eu.tnova.nfs.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;

public class NFSFile {
	private static WSClient wsClient = new WSClient();

	public static void main(String[] args) throws Exception {
		if ( args.length<2 )
			help();
		int exitValue = -1;
		String url = args[0]+"/NFS/files";
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
		case "upload":
			if ( args.length!=4 )
				help();
			File ulFile = checkFile(args[3]);
			if ( ulFile == null )
				break;
			exitValue = upload(url, args[2], ulFile);
			break;
		case "update":
			if ( args.length!=4 )
				help();
			File upFile = checkFile(args[3]);
			if ( upFile == null )
				break;
			url += "/"+args[2];
			exitValue = update(url, args[2], upFile);
			break;
		case "download":
			// example cmd : http://<host>:<port> get <filename> <file>
			if ( args.length!=4 )
				help();
			url += "/"+args[2];
			exitValue = download(url, args[2], args[3]);
			break;
		case "test":
			if ( args.length!=4 )
				help();
			File testFile = checkFile(args[3]);
			if ( testFile == null )
				break;
			String urlFile = url+"/"+args[2];
			System.out.println("\n>>>> list ");
			list(url);
			System.out.println("\n>>>> upload ");
			upload(url, args[2], testFile);
			System.out.println("\n>>>> list ");
			list(url);
			System.out.println("\n>>>> download ");
			download(urlFile, args[2], args[3]+".read");
			System.out.println("\n>>>> update ");
			update(urlFile, args[2], testFile);
			System.out.println("\n>>>> delete ");
			delete(urlFile);
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
	
	private static int upload(String url, String filename, File file)
			throws FileNotFoundException, MalformedURLException {
		MultipartBody body = new MultipartBody(
				new Attachment(
					filename, 
					new FileInputStream(file), 
					new ContentDisposition("form-data; name=\"file\"; filename=\""+filename+"\"")
					) );
		WebClient webClient = wsClient.getClient(url, MediaType.MULTIPART_FORM_DATA_TYPE);
		String responseBody = webClient.post(body, String.class);
		return (wsClient.printResponse(webClient.getResponse(), responseBody));
	}

	private static int update(String url, String filename, File file) 
			throws MalformedURLException, FileNotFoundException {
		MultipartBody body = new MultipartBody(
				new Attachment(
						filename, 
						new FileInputStream(file), 
						new ContentDisposition("form-data; name=\"file\"; filename=\""+filename+"\"")
						) );
		WebClient webClient = wsClient.getClient(url, MediaType.MULTIPART_FORM_DATA_TYPE);
		String responseBody = webClient.put(body, String.class);
		return (wsClient.printResponse(webClient.getResponse(), responseBody));
	}

	private static int download(String url, String filename, String filePath) 
			throws MalformedURLException, FileNotFoundException, IOException {
		WebClient webClient = wsClient.getClient(url, MediaType.MULTIPART_FORM_DATA_TYPE);
		MultipartBody body = webClient.get(MultipartBody.class);
		FileOutputStream outputStream = new FileOutputStream(filePath);
		if ( body.getAllAttachments().size()!=1 ) {
			System.out.println("ERROR : attachment != 1 ("+body.getAllAttachments().size()+")");
			outputStream.close();
			return -1;
		}
		Attachment att = body.getAllAttachments().get(0);
		if ( !att.getContentType().equals(MediaType.APPLICATION_OCTET_STREAM_TYPE) ) {
			System.out.println("ERROR : ContentType is not "+att.getContentType());
			outputStream.close();
			return -1;
		}
		if ( !att.getDataHandler().getName().equals("file")) {
			System.out.println("ERROR : Content Name is "+att.getDataHandler().getName());
			outputStream.close();
			return -1;
		}
		att.getDataHandler().writeTo(outputStream);
		outputStream.flush();
		outputStream.close();
		return ( wsClient.printResponse(webClient.getResponse(), null) );
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
		System.out.println("  command     - (list|upload|download|update|delete)");
		System.out.println("  (filename)  - (cmd delete|upload|download|update) - ex. imageTest  ");
		System.out.println("  (Path file) - (cmd upload|download|update) - ex. D:\\tmp\\imagefile.img  ");
		System.out.println("Example: ");
		System.out.println("  java -jar NFSFile.jar https://83.212.108.105:8443 upload IMAGETEST D:\\tmp\\imagefile.img");
		System.out.println("  java -jar NFSFile.jar https://83.212.108.105:8443 list");
		System.out.println("  java -jar NFSFile.jar https://83.212.108.105:8443 download IMAGETEST D:\\tmp\\imagefileUploaded.img");
		System.out.println("  java -jar NFSFile.jar https://83.212.108.105:8443 update IMAGETEST D:\\tmp\\imagefile2.img");
		System.out.println("  java -jar NFSFile.jar https://83.212.108.105:8443 delete IMAGETEST");
		System.exit(-1);
	}




}
