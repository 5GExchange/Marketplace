package eu.tnova.nfs.ws;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import eu.tnova.nfs.entity.VNFDescriptor;
import eu.tnova.nfs.exception.ValidationException;
import eu.tnova.nfs.valves.GatekeeperAuthenticationValve;
import eu.tnova.nfs.ws.entity.VNFDListResponse;
import eu.tnova.nfs.ws.entity.VNFDResponse;
import eu.tnova.nfs.ws.orchestrator.OrchestratorOperationTypeEnum;

//import org.apache.cxf.interceptor.InInterceptors;
//import org.apache.cxf.interceptor.OutInterceptors;

@Stateless
//@InInterceptors(interceptors={"org.apache.cxf.interceptor.LoggingInInterceptor"})
//@OutInterceptors(interceptors={"org.apache.cxf.interceptor.LoggingOutInterceptor"})
@DependsOn("FileServiceBean")
public class VNFDescriptorWS implements VNFDescriptorWSInterface {
	@Inject	private Logger log;
	@EJB private ServiceBean service;
	@PersistenceContext(unitName = "NFS_DB") private EntityManager em;
	@Context HttpHeaders headers;
	
	@PostConstruct
	public void init() {
	}
	@PreDestroy
	public void destroy() {
	}
	
	@Override
	public Response create_VNFDescriptor(UriInfo uriInfo, String vnfd) {
		log.info("Create new VNF Descriptor");
		log.debug("vnfd = {}",vnfd);
		try {
			VNFDescriptor vnfDescriptor = service.createVNFDescriptor(vnfd);
			service.sendNotificationToOrchestrator(
					OrchestratorOperationTypeEnum.CREATE, vnfDescriptor, getAuthToken(headers) );
			VNFDResponse resp = new VNFDResponse(vnfDescriptor);
			String jsonResp = new Gson().toJson(resp);
			log.debug("{}",jsonResp);
			Response response = Response.status(Status.CREATED).entity(jsonResp).build();
			response.getMetadata().add("Location", uriInfo.getAbsolutePath()+"/"+vnfDescriptor.getId());
			return response;
		} catch (ValidationException e) {
			log.error(e.getMessage());
			return e.getResponse();
		} catch (Exception e) {
			log.error(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).
					type(MediaType.TEXT_PLAIN).build();
		}
	}
	@Override
	public Response modify_VNFDescriptor(Integer vnfdId, String vnfd) {
		log.info("Modify VNF Descriptor : {}",vnfdId);
		log.debug("vnfd = {}",vnfd);
		try {
			VNFDescriptor vnfDescriptor = service.modifyVNFDescriptor(vnfdId, vnfd);
			service.sendNotificationToOrchestrator(
					OrchestratorOperationTypeEnum.UPDATE, vnfDescriptor, getAuthToken(headers) );
			return Response.status(Status.NO_CONTENT).build();				
		} catch (ValidationException e) {
			log.error(e.getMessage());
			return Response.status(e.getStatus()).entity(e.getMessage()).
					type(MediaType.TEXT_PLAIN).build();
		} catch (Exception e) {
			log.error(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).
					type(MediaType.TEXT_PLAIN).build();
		}
	}
	@Override
	public Response get_VNFDescriptor(Integer vnfdId) {
		log.info("Get VNF Descriptor : {}",vnfdId);
		try {
			String vnfd = service.getVNFDescriptor(vnfdId).getJson();
			log.debug("{}",vnfd);
			return Response.status(Status.OK).entity(vnfd).build();				
		} catch (ValidationException e) {
			log.error(e.getMessage());
			return Response.status(e.getStatus()).entity(e.getMessage()).
					type(MediaType.TEXT_PLAIN).build();
		}
	}
	@Override
	public Response delete_VNFDescriptor(Integer vnfdId) {
		log.info("Delete VNF Descriptor : {}",vnfdId);
		try {
			List<VNFDescriptor> vnfDescriptors = service.deleteVNFDescriptor(vnfdId);
			service.sendNotificationToOrchestrator(OrchestratorOperationTypeEnum.DELETE, 
					vnfDescriptors, getAuthToken(headers));
			return Response.status(Status.NO_CONTENT).build();				
		} catch (ValidationException e) {
			log.error(e.getMessage());
			return Response.status(e.getStatus()).entity(e.getMessage()).
					type(MediaType.TEXT_PLAIN).build();
		}
	}
	@Override
	public Response delete_VNFDescriptors() {
		log.info("Delete All VNF Descriptors");
		try {
			List<VNFDescriptor> vnfDescriptors = service.deleteVNFDescriptor(null);
			service.sendNotificationToOrchestrator(OrchestratorOperationTypeEnum.DELETE, 
					vnfDescriptors, getAuthToken(headers) );
			return Response.status(Status.NO_CONTENT).build();				
		} catch (ValidationException e) {
			log.error(e.getMessage());
			return Response.status(e.getStatus()).entity(e.getMessage()).
					type(MediaType.TEXT_PLAIN).build();
		}
	}
	@Override
	public Response get_VNFDescriptor_list() {
		log.info("Get VNF Descriptor list");
		try {
			VNFDListResponse resp = new VNFDListResponse(service.getVNFDescriptors());
			log.debug("{}",resp.getJson());
			return Response.status(Status.OK).entity(resp.getJson()).build();
		} catch (Exception e) {
			log.error(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).
					type(MediaType.TEXT_PLAIN).build();
		}
	}

	private String getAuthToken(HttpHeaders headers) {
		List<String> tokens = headers.getRequestHeader(GatekeeperAuthenticationValve.AUTH_TOKEN);
		if ( tokens==null || tokens.size()==0 )
			return null;
		return tokens.get(0);
	}

}
