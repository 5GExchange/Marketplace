package eu.tnova.nfs.entity;

import java.io.Serializable;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("serial")
@Embeddable
@XmlRootElement(name = "constituent_vdu")
public class ConstituentVDU implements Serializable {

	@SerializedName("vdu_reference") @XmlElement(name="vdu_reference")
	private String vduId;
	@SerializedName("number_of_instances") @XmlElement(name="number_of_instances")
	private Integer instancesNumber;
	@SerializedName("constituent_vnfc") @XmlElement(name="constituent_vnfc")
	private String vnfc;
	
	public ConstituentVDU() {
		super();
	}

	public String getVduId() {
		return vduId;
	}
	public void setVduId(String vduId) {
		this.vduId = vduId;
	}
	public Integer getInstancesNumber() {
		return instancesNumber;
	}
	public void setInstancesNumber(Integer instancesNumber) {
		this.instancesNumber = instancesNumber;
	}
	public String getVnfc() {
		return vnfc;
	}
	public void setVnfc(String vnfc) {
		this.vnfc = vnfc;
	}

}
