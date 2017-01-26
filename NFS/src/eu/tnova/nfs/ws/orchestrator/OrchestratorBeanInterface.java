package eu.tnova.nfs.ws.orchestrator;

import javax.ejb.Local;
import eu.tnova.nfs.entity.VNFDescriptor;

@Local
public interface OrchestratorBeanInterface {
	public boolean create(VNFDescriptor vnfDescriptor, String userToker);
	public boolean update(VNFDescriptor vnfDescriptor, String userToker);
	public boolean delete(VNFDescriptor vnfDescriptor, String userToker);
}
