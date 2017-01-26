package eu.tnova.nfs.ws;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.jaxrs.ext.search.SearchContext;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import eu.tnova.nfs.entity.Billing;
import eu.tnova.nfs.entity.DeploymentFlavour;
import eu.tnova.nfs.entity.Price;
import eu.tnova.nfs.entity.VNFDescriptor;
import eu.tnova.nfs.ws.entity.VNFBrokarageListResponse;
import eu.tnova.nfs.ws.entity.VNFBrokerageResponse;

@Stateless
@DependsOn("ServiceBean")
public class VNFWS implements VNFWSInterface {
	@Inject	private Logger log;
	@EJB private ServiceBean service;
	@PersistenceContext(unitName = "NFS_DB") private EntityManager em;
	
	@PostConstruct
	public void init() {
	}
	@PreDestroy
	public void destroy() {
	}
	
	@Override
	public Response get_VNF(String vnfId) {
		log.info("Get VNF : {}",vnfId);
		return Response.status(Status.OK).build();
	}
	@Override
	public Response get_VNF_list(SearchContext searchContext,
			String billingType, String billingPeriod, String priceMin,
			String priceMax, String priceUnit) {
		log.info("Get VNF list");
		log.debug("billingType={}, billingPeriod={}, priceMin={}, priceMax={}, priceUnit={}",
				billingType, billingPeriod, priceMin, priceMax, priceUnit);

//		log.info("searchContext = {}",searchContext);
//		log.info("expression = {}",searchContext.getSearchExpression());
//		Map<String, String> map = new HashMap<String, String>();
//		map.put("billingType", 	"vnfd.billing.type"); 
//		map.put("billingPeriod","vnfd.billing.period"); 
//		map.put("priceMin", 	"vnfd.billing.price.minPerPeriod"); 
//		map.put("priceMax", 	"vnfd.billing.price.maxPerPeriod"); 
//		map.put("priceUnit",	"vnfd.billing.price.unit"); 

		List<VNFBrokerageResponse> responseList = new ArrayList<VNFBrokerageResponse>();
		try {
			List<VNFDescriptor> vnfds = service.getVNFDescriptors();
			for (VNFDescriptor vnfd : vnfds ) {
				if ( !matchBillngs(vnfd.getVnfd().getBilling(), 
						billingType, billingPeriod, priceMin, priceMax, priceUnit) )
					continue;
				if ( !matchFlavours(vnfd.getVnfd().getDeploymentFlavour() ) )
						continue;
				responseList.add( new VNFBrokerageResponse(vnfd) );
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		VNFBrokarageListResponse resp = new VNFBrokarageListResponse(responseList);
        String jsonResp = new Gson().toJson(resp);
        log.debug("{}",jsonResp);
        return Response.status(Status.OK).entity(jsonResp).build();
		
//		SearchCondition<VNFDescriptor> sc1 = searchContext.getCondition(VNFDescriptor.class); 	
//		SearchCondition<VNFDescriptor> sc = searchContext.getCondition(VNFDescriptor.class, map); 	
//		List<VNFDescriptor> found = vnfds;
//		if ( sc!=null ) {
//			List<SearchCondition<VNFDescriptor>> searchConditions = sc.getSearchConditions();
//			log.debug("searchConditions = {}",searchConditions);
//			found = sc.findAll(vnfds);
//		}
//		return Response.status(Status.OK).build();
//		return Response.status(Status.OK).entity(found).build();

//		SearchCondition<VNFDescriptor> filter = new FiqlParser<VNFDescriptor>(VNFDescriptor.class).parse("reviews.review==good;reviews.authors==Ted");
//		// in practice, map "reviews.review" to "review", "reviews.authors" to "reviewAuthor" 
//		// and have a simple query like "review==good;reviewAuthor==Ted" instead
//		 
//		SearchConditionVisitor<VNFDescriptor, TypedQuery<VNFDescriptor>> jpa = new JPATypedQueryVisitor<VNFDescriptor>(em, VNFDescriptor.class);
//		filter.accept(jpa);
//		TypedQuery<VNFDescriptor> query = jpa.getQuery();
//		return query.getResultList();
	}

	
	private boolean matchBillngs(Billing billing, String billingType,
			String billingPeriod, String priceMin, String priceMax,
			String priceUnit) throws Exception {
		Billing matchBilling = new Billing(
				billingType, billingPeriod, new Price(priceUnit, priceMin, priceMax ,null) );
		if ( matchBilling.match(billing) )
			return true;
		return false;
	}
	private boolean matchFlavours(List<DeploymentFlavour> deploymentFlavours) 
			throws Exception {
		// TODO Auto-generated method stub
		return true;
	}
	
}
