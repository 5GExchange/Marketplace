package eu.tnova.nfs.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("serial")
@Entity
@XmlRootElement(name = "vdu")
public class VirtualDeploymentUnit implements Serializable {
	public static final String VDU = "vdu";
	public static final String VM_IMAGE = "vm_image";
	public static final String VM_IMAGE_MD5 = "vm_image_md5";
	public static final String VM_IMAGE_FORMAT = "vm_image_format";

	@Id @GeneratedValue
	private Integer DBId;
	@SerializedName("id") @XmlElement(name="id")
	@NotNull (message="VDU Id must be specified")
	@Size(min = 1, message="VDU Id name must be specified")
	private String id;
	@SerializedName(VM_IMAGE) @XmlElement(name=VM_IMAGE)
	@NotNull (message="vmImage must be specified")
	@Size(min = 1, message="vmImage must be specified")
	private String vmImage;
	@SerializedName("VM_IMAGE_MD5") @XmlElement(name="VM_IMAGE_MD5")
	private String vmImage_md5;
	@SerializedName("VM_IMAGE_FORMAT") @XmlElement(name="VM_IMAGE_FORMAT")
	private String vmImage_format;
	
	@SerializedName("Vnfc") @XmlElement(name="Vnfc")
	@ElementCollection private List<VNFComponent> vnfc;

	public VirtualDeploymentUnit() {
		super();
	}

	public Integer getDBId() {
		return DBId;
	}
	public void setDBId(Integer DBId) {
		this.DBId = DBId;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getVmImage() {
		return vmImage;
	}
	public void setVmImage(String vmImage) {
		this.vmImage = vmImage;
	}
	public String getVmImage_md5() {
		return vmImage_md5;
	}
	public void setVmImage_md5(String vmImage_md5) {
		this.vmImage_md5 = vmImage_md5;
	}
	public String getVmImage_format() {
		return vmImage_format;
	}
	public void setVmImage_format(String vmImage_format) {
		this.vmImage_format = vmImage_format;
	}

	public List<VNFComponent> getVnfc() {
		return vnfc;
	}
	public void setVnfc(List<VNFComponent> vnfc) {
		this.vnfc = vnfc;
	}

}
