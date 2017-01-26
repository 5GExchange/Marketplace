package eu.tnova.nfs.ws;

import javax.ejb.Local;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.search.SearchContext;
import org.apache.cxf.jaxrs.model.wadl.Description;
import org.apache.cxf.jaxrs.model.wadl.Descriptions;
import org.apache.cxf.jaxrs.model.wadl.DocTarget;

@Path("/vnfs")
@Local
@Description(value = "Network Function Store - Virtual Network Functions", 
	target = DocTarget.RESOURCE)
public interface VNFWSInterface {

	@GET 
	@Path("/{vnfId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Descriptions({
		   @Description(value = "Get Virtual Network Function", target = DocTarget.METHOD),
		   @Description(value = "Virtual Network Function Id", target = DocTarget.PARAM),
		   @Description(value = "Virtual Network Function", target = DocTarget.RETURN),
		})
	public Response get_VNF(
		@Description("Virtual Network Function Id") @PathParam("vnfId") @NotNull String vnfId);

	@GET 
	@Produces (MediaType.APPLICATION_JSON)
	@Descriptions({
		   @Description(value = "Get a list of Virtual Network Functions", target = DocTarget.METHOD),
		   @Description(value = "list of Virtual Network Functions", target = DocTarget.RETURN),
		})
	public Response get_VNF_list(@Context SearchContext searchContext,
			@Description("billing type") @QueryParam(value = "billingType") String billingType,
			@Description("billing period") @QueryParam(value = "billingPeriod")String billingPeriod,
			@Description("min price") @QueryParam(value = "priceMin") String priceMin,
			@Description("max price") @QueryParam(value = "priceMax") String priceMax,
			@Description("price Unit") @QueryParam(value = "priceUnit") String priceUnit
			);

}
