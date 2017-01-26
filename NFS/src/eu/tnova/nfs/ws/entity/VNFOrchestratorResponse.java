package eu.tnova.nfs.ws.entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

@XmlRootElement(name = "vnf")
public class VNFOrchestratorResponse {
	@SerializedName("name") @XmlElement(name="name")
	private String namei;
	@SerializedName("vnf-manager") @XmlElement(name="vnf-manager")
	private String vnfManageri;
	@SerializedName("created_at") @XmlElement(name="created_at")
	private String creationDatei;
	@SerializedName("updated_at") @XmlElement(name="updated_at")
	private String lastModificationDatei;
	@SerializedName("vnfd") @XmlElement(name="vnfd")
	private JsonElement vnfdi ;
	
	public VNFOrchestratorResponse() {
	}

	public String getName() {
		return namei;
	}
	public void setName(String name) {
		this.namei = name;
	}
	public String getVnfManager() {
		return vnfManageri;
	}
	public void setVnfManager(String vnfManager) {
		this.vnfManageri = vnfManager;
	}
	public String getCreationDate() {
		return creationDatei;
	}
	public void setCreationDate(String creationDate) {
		this.creationDatei = creationDate;
	}
	public String getLastModificationDate() {
		return lastModificationDatei;
	}
	public void setLastModificationDate(String lastModificationDate) {
		this.lastModificationDatei = lastModificationDate;
	}
	public JsonElement getVnfd() {
		return vnfdi;
	}
	public void setVnfd(JsonElement vnfd) {
		this.vnfdi = vnfd;
	}

}
