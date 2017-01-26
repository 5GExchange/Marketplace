package eu.tnova.nfs.ws.entity;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.annotations.SerializedName;

@XmlRootElement(name = "vnfs")
public class VNFOrchestratorListResponse {
	@SerializedName("vnf") @XmlElement(name = "vnf")
	private List<VNFOrchestratorResponse> vnfs = new ArrayList<VNFOrchestratorResponse>();

	public VNFOrchestratorListResponse() {
	}

	public List<VNFOrchestratorResponse> getVnfs() {
		return vnfs;
	}
	public void setVnfs(List<VNFOrchestratorResponse> vnfs) {
		this.vnfs = vnfs;
	}


}
