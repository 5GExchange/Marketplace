package eu.tnova.nfs.ws.entity;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.annotations.SerializedName;

import eu.tnova.nfs.entity.VNFDescriptor;
import eu.tnova.nfs.entity.VNFFile;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "file")
public class VNFFileResponse {
	@SerializedName("name") @XmlElement(name = "name")
	private String name;
	@SerializedName("vnfd_id") @XmlElement(name = "vnfd_id")
	private List<Integer> vnfdId = new ArrayList<Integer>();

	public VNFFileResponse() {
	}

	public VNFFileResponse(VNFFile vnfFile) {
		name = vnfFile.getName();
		if ( vnfFile.getVnfDescriptors()==null )
			return;
		for (VNFDescriptor vnfd: vnfFile.getVnfDescriptors() ) {
			if ( !vnfdId.contains(vnfd.getId()) )
				vnfdId.add(vnfd.getId());
		}
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Integer> getVnfdId() {
		return vnfdId;
	}
	public void setVnfdId(List<Integer> vnfdId) {
		this.vnfdId = vnfdId;
	}

}
