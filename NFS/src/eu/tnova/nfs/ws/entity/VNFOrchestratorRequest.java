package eu.tnova.nfs.ws.entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.annotations.SerializedName;

@XmlRootElement(name = "vnf")
public class VNFOrchestratorRequest {
	@SerializedName("name") @XmlElement(name="name")
	private String name;
	@SerializedName("vnf-manager") @XmlElement(name="vnf-manager")
	private String vnfManager;
	@SerializedName("vnfd") @XmlElement(name="vnfd")
	private String vnfd;

	public VNFOrchestratorRequest() {
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getVnfManager() {
		return vnfManager;
	}
	public void setVnfManager(String vnfManager) {
		this.vnfManager = vnfManager;
	}
	public String getVnfd() {
		return vnfd;
	}
	public void setVnfd(String vnfd) {
		this.vnfd = vnfd;
	}

}
