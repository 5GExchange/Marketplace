package eu.tnova.nfs.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("serial")
@Entity
@XmlRootElement(name = "deployment_flavour")
public class DeploymentFlavour implements Serializable {
	
	@Id @GeneratedValue
	private Integer DBId;
	@SerializedName("flavour_id") @XmlElement(name="flavour_id")
	private String flavourId;
	@SerializedName("flavour_key") @XmlElement(name="flavour_key")
	private String flavourKey;	
	@SerializedName("assurance_parameters") @XmlElement(name="assurance_parameters")
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<AssuranceParameters> assuranceParams;
	@SerializedName("constraint") @XmlElement(name="constraint")
	@ElementCollection private List<String> constraints;

	@SerializedName("constituent_vdu") @XmlElement(name="constituent_vdu")
	@Embedded private ConstituentVDU constituentVDU;
	@SerializedName("Available") @XmlElement(name="Available")
	private String available;
	
	public DeploymentFlavour() {
		super();
	}

	public Integer getDBId() {
		return DBId;
	}
	public void setDBId(Integer DBId) {
		this.DBId = DBId;
	}

	public List<AssuranceParameters> getAssuranceParameters() {
		return assuranceParams;
	}

	public String getFlavourId() {
		return flavourId;
	}

	public void setFlavourId(String flavourId) {
		this.flavourId = flavourId;
	}

	public String getFlavourKey() {
		return flavourKey;
	}

	public void setFlavourKey(String flavourKey) {
		this.flavourKey = flavourKey;
	}

	public List<AssuranceParameters> getAssuranceParams() {
		return assuranceParams;
	}

	public void setAssuranceParams(List<AssuranceParameters> assuranceParams) {
		this.assuranceParams = assuranceParams;
	}

	public List<String> getConstraints() {
		return constraints;
	}

	public void setConstraints(List<String> constraints) {
		this.constraints = constraints;
	}

	public ConstituentVDU getConstituentVDU() {
		return constituentVDU;
	}

	public void setConstituentVDU(ConstituentVDU constituentVDU) {
		this.constituentVDU = constituentVDU;
	}

	public String getAvailable() {
		return available;
	}

	public void setAvailable(String available) {
		this.available = available;
	}


}
