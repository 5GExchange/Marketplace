package eu.tnova.nfs.ws;

import javax.ejb.Local;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.apache.cxf.jaxrs.model.wadl.Description;
import org.apache.cxf.jaxrs.model.wadl.Descriptions;
import org.apache.cxf.jaxrs.model.wadl.DocTarget;

@Path("/files")
@Local
@Description(value = "Network Function Store - Virtual Network Functions Files", 
	target = DocTarget.RESOURCE)
public interface VNFFileWSInterface {

	@HEAD
	@Path("/{fileName}")
	@Consumes(MediaType.MEDIA_TYPE_WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@Descriptions({
		   @Description(value = "Head Virtual Network Function Image file", target = DocTarget.METHOD),
		   @Description(value = "Name of Virtual Network Function image file", target = DocTarget.PARAM),
		})
	public Response head_VNFFile(
		@Description("name of file") @PathParam("fileName") @NotNull String fileName );

	@POST 
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces (MediaType.APPLICATION_JSON)
	@Descriptions({
		   @Description(value = "Upload Virtual Network Function Image file", target = DocTarget.METHOD),
		   @Description(value = "Image name and list of vnfds that use this image", target = DocTarget.RETURN),
		})
	public Response upload_VNFFile( 
		@Context UriInfo uriInfo,	
		@Multipart(value="file", type=MediaType.APPLICATION_OCTET_STREAM, required=true) 
		@Description("file to upload") Attachment attachment);

	@PUT
	@Path("/{fileName}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces (MediaType.APPLICATION_JSON)
	@Descriptions({
		   @Description(value = "Update Virtual Network Function Image file", target = DocTarget.METHOD),
		   @Description(value = "Name of Virtual Network Function image file", target = DocTarget.PARAM),
		   @Description(value = "Image name and list of vnfds that use this image", target = DocTarget.RETURN),
	})
	public Response update_VNFFile( 
		@Description("name of file to update") @PathParam("fileName") @NotNull String fileName,
		@Multipart(value="file", type=MediaType.APPLICATION_OCTET_STREAM, required=true) 
		@Description("file to update") Attachment attachment);

	@GET 
	@Path("/{fileName}")
	@Produces(MediaType.MULTIPART_FORM_DATA)
	@Descriptions({
		   @Description(value = "Get Virtual Network Function Image file", target = DocTarget.METHOD),
		   @Description(value = "Name of Virtual Network Function image file", target = DocTarget.PARAM),
		   @Description(value = "Virtual Network Function Image file", target = DocTarget.RETURN),
		})
	public Response download_VNFFile(
		@Description("name of file to download") @PathParam("fileName") @NotNull String fileName,
		@Description("name of file to download") @QueryParam("contentType") String contentType);

	@GET 
	@Produces (MediaType.APPLICATION_JSON)
	@Descriptions({
		   @Description(value = "Get a list of all Virtual Network Function Image files", target = DocTarget.METHOD),
		   @Description(value = "lis of image name and of vnfds that use every image", target = DocTarget.RETURN),
		})
	public Response get_VNFFile_list(
			@Description("name of profider") @QueryParam("provider") Integer providerId);

	@DELETE
	@Path("/{fileName}")
	@Descriptions({
		   @Description(value = "Delete Virtual Network Function Image file", target = DocTarget.METHOD),
		   @Description(value = "Name of Virtual Network Function image file", target = DocTarget.PARAM),
		})
	public Response delete_VNFFile(
		@Description("name of file to delete") @PathParam("fileName") @NotNull String fileName);

	@DELETE
	@Descriptions({
		   @Description(value = "Delete All Virtual Network Function Image files", target = DocTarget.METHOD),
		})
	public Response delete_VNFFiles();

}
