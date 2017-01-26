package eu.tnova.nfs.ws.validator;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.Logger;

import com.google.gson.JsonSyntaxException;

import eu.tnova.nfs.entity.VNFDescriptor;
import eu.tnova.nfs.entity.VirtualDeploymentUnit;
import eu.tnova.nfs.exception.ValidationException;

public class NetworkFunctionStoreDescriptorValidator {
	@PersistenceContext(unitName = "NFS_DB") private EntityManager em;
	@Inject	private Logger log;

	public NetworkFunctionStoreDescriptorValidator() {
	}

	public VNFDescriptor validateCreate (String jsonVNFD) 
			throws ValidationException {
	    VNFDescriptor vnfDescriptor = parseVnfDescriptor(jsonVNFD);
	    if (vnfDescriptor.getId() != null)
	    	throw new ValidationException("Not Possibile insert VNF Descriptor with id already set");
	    return vnfDescriptor;
	}
	public VNFDescriptor validateUpdate (Integer vnfd_Id, String jsonVNFD) 
			throws ValidationException {
	    VNFDescriptor vnfDescriptor = parseVnfDescriptor(jsonVNFD);
	    if (vnfDescriptor.getId() == null)
	    	throw new ValidationException("Update VNF Descriptor without id");
	    if (vnfd_Id.intValue() != vnfDescriptor.getId().intValue())
	    	throw new ValidationException("VNF Descriptor with id different from request");
		VNFDescriptor vnfd = em.find(VNFDescriptor.class, vnfd_Id);
		if ( vnfd==null ) 
			throw new ValidationException("Not found VNF Descriptor with id "+vnfd_Id, 
					Status.NOT_FOUND);
		vnfd.setJson(vnfDescriptor.getJson());
		vnfd.setVnfd(vnfDescriptor.getVnfd());
		return vnfd;
	}
	public VNFDescriptor validateGet (Integer vnfd_Id) 
			throws ValidationException {
		if ( vnfd_Id==null )
			throw new ValidationException("validate VNF Descriptor without id");
		VNFDescriptor vnfd = em.find(VNFDescriptor.class, vnfd_Id);
		if ( vnfd==null ) 
			throw new ValidationException("Not found VNF Descriptor with id "+vnfd_Id, 
					Status.NOT_FOUND);
		return vnfd;
	}
	public VNFDescriptor validateDelete (Integer vnfd_Id)
			throws ValidationException {
		return validateGet(vnfd_Id);
	}

	private VNFDescriptor parseVnfDescriptor (String jsonVNFD) 
			throws ValidationException {
		try {
			VNFDescriptor vnfDescriptor = new VNFDescriptor(jsonVNFD);
			if ( vnfDescriptor.getVnfd().getVdu()==null || vnfDescriptor.getVnfd().getVdu().isEmpty() )
				throw new ValidationException("VNF Descriptor without "+VirtualDeploymentUnit.VDU);
			if ( vnfDescriptor.getvmImages().size() !=  vnfDescriptor.getVnfd().getVdu().size() )
				throw new ValidationException("At least one "+VirtualDeploymentUnit.VDU+" without image");
			return vnfDescriptor;
		} catch (JsonSyntaxException e) {
			log.error(e.getMessage());
			throw new ValidationException("Error parsing VNF Descriptor");
		}
	}

}
