package eu.tnova.nfs.ws.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.annotations.SerializedName;

import eu.tnova.nfs.entity.VNFDescriptor;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "vnfd")
public class VNFDResponse {
	@SerializedName("vnfd_id") @XmlElement(name = "vnfd_id")
	private Integer vnfdId;

	public VNFDResponse() {
	}

	public VNFDResponse(VNFDescriptor vnfd) {
		vnfdId = vnfd.getId();
	}
	
	public Integer getVnfdId() {
		return vnfdId;
	}

	public void setVnfdId(Integer vnfdId) {
		this.vnfdId = vnfdId;
	}

}
