package eu.tnova.nfs.ws.entity;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.annotations.SerializedName;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "vnfs")
public class VNFBrokarageListResponse {
	@SerializedName("vnfs") @XmlElement(name = "vnfs")
	private List<VNFBrokerageResponse> vnfs = new ArrayList<VNFBrokerageResponse>();

	public VNFBrokarageListResponse() {
	}

	public VNFBrokarageListResponse(List<VNFBrokerageResponse> vnfs) {
		this.vnfs = vnfs;
	}

	public List<VNFBrokerageResponse> getVnfs() {
		return vnfs;
	}

	public void setVnfs(List<VNFBrokerageResponse> vnfs) {
		this.vnfs = vnfs;
	}
	

}
