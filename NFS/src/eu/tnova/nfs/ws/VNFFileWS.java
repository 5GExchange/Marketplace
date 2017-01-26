package eu.tnova.nfs.ws;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.activation.FileDataSource;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import eu.tnova.nfs.entity.VNFFile;
import eu.tnova.nfs.exception.ValidationException;
import eu.tnova.nfs.producers.EnvValue;
import eu.tnova.nfs.valves.GatekeeperAuthenticationValve;
import eu.tnova.nfs.ws.entity.VNFFileResponse;
import eu.tnova.nfs.ws.orchestrator.OrchestratorOperationTypeEnum;

//import org.apache.cxf.interceptor.InInterceptors;
//import org.apache.cxf.interceptor.OutInterceptors;

@Stateless
//@InInterceptors(interceptors={"org.apache.cxf.interceptor.LoggingInInterceptor"})
//@OutInterceptors(interceptors={"org.apache.cxf.interceptor.LoggingOutInterceptor"})
@DependsOn("FileServiceBean")
public class VNFFileWS implements VNFFileWSInterface {
	@Inject	private Logger log;
	@Inject @EnvValue(EnvValue.storePath) private String storePath;
	@EJB private ServiceBean serviceBean;
	@Context HttpHeaders headers;
	private ExecutorService executorService;
	
	@PostConstruct
	public void init() {
		executorService = Executors.newCachedThreadPool();
	}
	@PreDestroy
	public void destroy() {
		executorService.shutdownNow();
	}

	@Override
	public Response head_VNFFile(String fileName) {
		try {
			log.info("Head VNF File : {}",fileName);
			VNFFile vnfFile = serviceBean.getVNFFile(fileName);
			String body = "";
			ResponseBuilder builder = Response.status(Status.OK)
					.header("Content-Type", MediaType.TEXT_PLAIN)
					.header("Content-Length", vnfFile.getFile(storePath).length());
			return builder.entity(body).build();
		} catch (ValidationException e) {
			log.warn(e.getMessage());
			return Response.status(e.getStatus()).entity(e.getMessage()).
					type(MediaType.TEXT_PLAIN).build();
		}
	}

	@Override
	public Response upload_VNFFile(
			UriInfo uriInfo, Attachment attachment) {
		VNFFile vnfFile = null;
		String fileName = null;
		try {
			// get data from attachment header
			fileName = attachment.getContentDisposition().getParameter("filename");
			log.info("Upload new VNF File : {}",fileName);
			String provider = attachment.getHeader("Provider-ID");
			String md5Sum = attachment.getHeader("MD5SUM");
			String imageType = attachment.getHeader("Image-Type");

			// temporary check inside header if not found inside attachments
			if ( provider==null )
				provider = getRequestHeader("Provider-ID");
			if ( md5Sum==null )
				md5Sum = getRequestHeader("MD5SUM");
			if ( imageType==null )
				imageType = getRequestHeader("Image-Type");
			
			// upload file
			vnfFile = serviceBean.uploadVNFFile(fileName, md5Sum, provider, imageType);
			writeFile(vnfFile, attachment.getDataHandler().getInputStream());
			serviceBean.endUploadVNFFile(vnfFile);
			// notify upload to orchestrator
			serviceBean.sendNotificationToOrchestrator(
					OrchestratorOperationTypeEnum.CREATE, vnfFile, getAuthToken(headers) );
			Response response = getResponse(Status.CREATED, new VNFFileResponse(vnfFile) );
			response.getMetadata().add("Location", uriInfo.getAbsolutePath()+"/"+fileName);
			return response;
		} catch (ValidationException e) {
			log.warn(e.getMessage());
			serviceBean.endUseOfVNFFileOnError(fileName, e.isParam(), e.isParam() );
			return Response.status(e.getStatus()).entity(e.getMessage()).
					type(MediaType.TEXT_PLAIN).build();
		} catch (Exception e) {
			log.error(e.getMessage());
			serviceBean.endUseOfVNFFileOnError(fileName, true, true);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).
					type(MediaType.TEXT_PLAIN).build();
		}
	}
	@Override
	public Response update_VNFFile(String fileName, Attachment attachment) {
		File file = null, tmpFile = null;
		try {
			log.info("Update VNF File : {}",fileName);
			// get data from attachment header
			String provider = attachment.getHeader("Provider-ID");
			String md5Sum = attachment.getHeader("MD5SUM");
			String imageType = attachment.getHeader("Image-Type");
			
			// temporary check inside header if not found inside attachments
			if ( provider==null )
				provider = getRequestHeader("Provider-ID");
			if ( md5Sum==null )
				md5Sum = getRequestHeader("MD5SUM");
			if ( imageType==null )
				imageType = getRequestHeader("Image-Type");

			// rename file to temporary file for restore it in case of error
			file = serviceBean.getFile(fileName);
			tmpFile = new File(storePath+File.separator+fileName+".upload_tmp");
			file.renameTo(tmpFile);
			// update file (only status is changed, the other fields are changed id chksum will be ok)
			VNFFile vnfFile = serviceBean.updateVNFFile(fileName, md5Sum, provider, imageType);
			writeFile(vnfFile, attachment.getDataHandler().getInputStream());
			serviceBean.endUpdateVNFFile(vnfFile);
			// notify update to orchestrator
			serviceBean.sendNotificationToOrchestrator(
					OrchestratorOperationTypeEnum.UPDATE, vnfFile, getAuthToken(headers));
			// remove temporary file
			tmpFile.delete();
			return getResponse(Status.OK, new VNFFileResponse(vnfFile) );
		} catch (ValidationException e) {
			log.warn(e.getMessage());
			serviceBean.endUseOfVNFFileOnError(fileName, false, true);
			tmpFile.renameTo(file);
			return Response.status(e.getStatus()).entity(e.getMessage()).
					type(MediaType.TEXT_PLAIN).build();
		} catch (Exception e) {
			log.error(e.getMessage());
			serviceBean.endUseOfVNFFileOnError(fileName, false, true);
			tmpFile.renameTo(file);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).
					type(MediaType.TEXT_PLAIN).build();
		}
	}
	@Override
	public Response delete_VNFFile(String fileName) {
		try {
			log.info("Delete VNF File : {}",fileName);
			List<VNFFile> vnfFiles = serviceBean.deleteVNFFile(fileName);
			serviceBean.sendNotificationToOrchestrator(
					OrchestratorOperationTypeEnum.DELETE, vnfFiles, getAuthToken(headers));
			return getResponse(Status.NO_CONTENT, null );
		} catch (ValidationException e) {
			log.warn(e.getMessage());
			return Response.status(e.getStatus()).entity(e.getMessage()).
					type(MediaType.TEXT_PLAIN).build();
		} catch (Exception e) {
			log.error(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).
					type(MediaType.TEXT_PLAIN).build();
		}
	}
	@Override
	public Response delete_VNFFiles() {
		log.info("Delete All VNF Files");
		try {
			List<VNFFile> vnfFiles = serviceBean.deleteVNFFile(null);
			serviceBean.sendNotificationToOrchestrator(
					OrchestratorOperationTypeEnum.DELETE, vnfFiles, getAuthToken(headers) );
			return Response.status(Status.NO_CONTENT).build();				
		} catch (ValidationException e) {
			log.warn(e.getMessage());
			return Response.status(e.getStatus()).entity(e.getMessage()).
					type(MediaType.TEXT_PLAIN).build();
		} catch (Exception e) {
			log.error(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).
					type(MediaType.TEXT_PLAIN).build();
		}
	}
	@Override
	public Response download_VNFFile(String fileName, String contentType) {
		try {
			log.info("Download VNF File : {}",fileName);
			VNFFile vnfFile = serviceBean.downloadVNFFile(fileName, contentType);
			ResponseBuilder builder = Response.status(Status.OK);
			builder.header("Content-Disposition", "attachment; filename=\""+fileName+"\"");

			if ( contentType==null ) {
				builder.header("Content-Type", MediaType.APPLICATION_OCTET_STREAM);
				builder.header("Content-Length", vnfFile.getFile(storePath).length());
				builder.header("Content-MD5", vnfFile.getMd5Sum());
				builder.entity((Object) vnfFile.getFile(storePath));
			} else {
				FileDataSource dataSource = new FileDataSource(vnfFile.getFile(storePath));
				MultivaluedMap<String, String> header = new MetadataMap<String, String>();
				header.add("Content-Type", MediaType.APPLICATION_OCTET_STREAM);
				header.add("Content-Transfer-Encoding", "binary");
				header.add("Content-Disposition", "form-data;name=file;filename="+fileName);
				header.add("MD5SUM", vnfFile.getMd5Sum());
				header.add("Provider-ID", vnfFile.getProviderId().toString());
				header.add("Image-Type", vnfFile.getImageType());
				Attachment attachment = new Attachment(vnfFile.getName(), dataSource, header);
				MultipartBody body = new MultipartBody(attachment);  
				builder.entity(body);
			}
			return builder.build();
		} catch (ValidationException e) {
			log.warn(e.getMessage());
			return Response.status(e.getStatus()).entity(e.getMessage()).
					type(MediaType.TEXT_PLAIN).build();
		} catch (Exception e) {
			log.error(e.getMessage());
			serviceBean.endUseOfVNFFileOnError(fileName, false, false);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).
					type(MediaType.TEXT_PLAIN).build();
		}
	}

	@Override
	public Response get_VNFFile_list(Integer providerId) {
		log.info("Get VNF File list");
		try {
			return getResponse(Status.OK, serviceBean.getVNFFileList(providerId) );
		} catch (Exception e) {
			log.error(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).
					type(MediaType.TEXT_PLAIN).build();
		}
	}
	
	public Long writeFile(VNFFile vnfFile, InputStream inputStream)
			throws IOException, ValidationException {
		log.debug("writeFile {}", vnfFile.getName() );
		Long size = vnfFile.writeFile(inputStream, storePath, true);
		log.info("end writeFile {}, size={}, MD5={}",
				vnfFile.getName(), size, vnfFile.getMd5Sum() );
		return size;
	}

	  private Response getResponse(Status status, Object responseObject) {
		ResponseBuilder respBuilder = Response.status(status);
		if ( responseObject!=null ) {
			String jsonResp = new Gson().toJson(responseObject);
			log.debug("{}",jsonResp);
			respBuilder.entity(jsonResp);
		}
		return respBuilder.build();				
	}
	
	private String getAuthToken(HttpHeaders headers) {
		List<String> tokens = headers.getRequestHeader(GatekeeperAuthenticationValve.AUTH_TOKEN);
		if ( tokens==null || tokens.size()==0 )
			return null;
		return tokens.get(0);
	}

	private String getRequestHeader(String header) {
		List<String> headerList = headers.getRequestHeader(header);
		if ( headerList!=null && headerList.size()!=0 ) 
			return headerList.get(0);
		return null;
	}
}
