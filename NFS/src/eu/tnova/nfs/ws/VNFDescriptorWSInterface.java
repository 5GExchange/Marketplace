package eu.tnova.nfs.ws;

import javax.ejb.Local;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.model.wadl.Description;
import org.apache.cxf.jaxrs.model.wadl.Descriptions;
import org.apache.cxf.jaxrs.model.wadl.DocTarget;

@Path("/vnfds")
@Local
@Description(value = "Network Function Store - Virtual Network Functions Descriptors", 
	target = DocTarget.RESOURCE)
public interface VNFDescriptorWSInterface {

	@POST 
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces (MediaType.APPLICATION_JSON)
	@Descriptions({
		   @Description(value = "Upload Virtual Network Function Descriptor", target = DocTarget.METHOD),
		})
	public Response create_VNFDescriptor(
			@Context UriInfo uriInfo,	
			@Description("VNF Descriptor") @NotNull String vnfd);

	@PUT
	@Path("/{vnfdId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces (MediaType.APPLICATION_JSON)
	@Descriptions({
		   @Description(value = "Update Virtual Network Function Descriptor", target = DocTarget.METHOD),
		})
	public Response modify_VNFDescriptor(
			@Description("VNF Descriptor Id") @PathParam("vnfdId") @NotNull Integer vnfdId,
			@Description("VNF Descriptor") @NotNull String vnfd);

	@GET 
	@Path("/{vnfdId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Descriptions({
		   @Description(value = "Get Virtual Network Function Descriptor", target = DocTarget.METHOD),
		   @Description(value = "Virtual Network Function Descriptor Identifier", target = DocTarget.PARAM),
		   @Description(value = "Virtual Network Function Descriptor", target = DocTarget.RETURN),
		})
	public Response get_VNFDescriptor(
			@Description("VNF Descriptor Id") @PathParam("vnfdId") @NotNull Integer vnfdId);

	@GET 
	@Produces (MediaType.APPLICATION_JSON)
	@Descriptions({
		   @Description(value = "Get a list of all Virtual Network Function Descriptors", target = DocTarget.METHOD),
		   @Description(value = "Virtual Network Function Descriptors Identifier List", target = DocTarget.RETURN),
		})
	public Response get_VNFDescriptor_list();

	@DELETE
	@Path("/{vnfdId}")
	@Descriptions({
		   @Description(value = "Delete Virtual Network Function Descriptor", target = DocTarget.METHOD),
		   @Description(value = "Virtual Network Function Descriptor Identifier", target = DocTarget.PARAM),
		})
	public Response delete_VNFDescriptor(
			@Description("VNF Descriptor Id") @PathParam("vnfdId") @NotNull Integer vnfdId);

	@DELETE
	@Descriptions({
		   @Description(value = "Delete All Virtual Network Function Descriptors", target = DocTarget.METHOD),
		})
	public Response delete_VNFDescriptors();

}
