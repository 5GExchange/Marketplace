package eu.tnova.nfs.ws.entity;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.annotations.SerializedName;

import eu.tnova.nfs.entity.Billing;
import eu.tnova.nfs.entity.DeploymentFlavour;
import eu.tnova.nfs.entity.VNFDescriptor;

@XmlRootElement(name = "vnf")
public class VNFBrokerageResponse {
// Required fields:	
//		Provider name
//		Description 
//		Billing model
//		Key-flavor/Assurance parameters
//		Constituent VDU
//	  @SerializedName("vnf_id") @XmlElement(name="vnf_id")
//	  private String vnfId;
	  @SerializedName("vnfd_id") @XmlElement(name="vnfd_id")
	  private Integer vnfdId;
	  @SerializedName("available") @XmlElement(name="available")
	  private boolean vnfAvailable;
	  @SerializedName("provider_id")  @XmlElement(name="provider_id")
	  private String provider_id;
	  @SerializedName("provider") @XmlElement(name="provider")
	  private String provider;
	  @SerializedName("description") @XmlElement(name="description")
	  private String description;
	  @SerializedName("Billing") @XmlElement(name="Billing")
	  private Billing billing;
	  @SerializedName("deployment_flavour") @XmlElement(name="deployment_flavour")
	  private List<DeploymentFlavour> deploymentFlavour;
	
	public VNFBrokerageResponse() {
	}

	public VNFBrokerageResponse(VNFDescriptor vnfd) {
//		this.vnfId = vnfd.getVnfId();
		this.vnfAvailable = vnfd.isVnfCreated();
		this.vnfdId = vnfd.getId();
		this.provider_id = vnfd.getVnfd().getProvider_id();
		this.provider = vnfd.getVnfd().getProvider();
		this.description = vnfd.getVnfd().getDescription();
		this.billing = vnfd.getVnfd().getBilling();
		this.deploymentFlavour = vnfd.getVnfd().getDeploymentFlavour();
	}

//	public String getVnfId() {
//		return vnfId;
//	}
//
//	public void setVnfId(String vnfId) {
//		this.vnfId = vnfId;
//	}

	public Integer getVnfdId() {
		return vnfdId;
	}

	public void setVnfdId(Integer vnfdId) {
		this.vnfdId = vnfdId;
	}

	public String getProvider_id() {
		return provider_id;
	}

	public void setProvider_id(String provider_id) {
		this.provider_id = provider_id;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Billing getBilling() {
		return billing;
	}

	public void setBilling(Billing billing) {
		this.billing = billing;
	}

	public List<DeploymentFlavour> getDeploymentFlavour() {
		return deploymentFlavour;
	}

	public void setDeploymentFlavour(List<DeploymentFlavour> deploymentFlavour) {
		this.deploymentFlavour = deploymentFlavour;
	}

	public boolean isVnfAvailable() {
		return vnfAvailable;
	}

	public void setVnfAvailable(boolean vnfAvailable) {
		this.vnfAvailable = vnfAvailable;
	}


}
