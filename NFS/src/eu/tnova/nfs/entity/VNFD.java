package eu.tnova.nfs.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
//import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
//import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("serial")
@Embeddable
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "vnfd")

public class VNFD implements Serializable {
	public static final String VNFD_ID = "id";

	@SerializedName("provider_id") @XmlElement(name="provider_id")
	private String provider_id;
	@SerializedName("provider")	@XmlElement(name="provider")
	private String provider;
	@SerializedName("description") @XmlElement(name="description")
//	@Lob
//	@Column(columnDefinition = "TEXT", length = 65535)	
//	@Size(max = 65535, message="Description lenght over 65553 bytes")
	@Size(max = 256, message="Description length must be less than 256 char")
	private String description;
	@SerializedName("type")	@XmlElement(name="type")
	private String type;
	@SerializedName("date_created")	@XmlElement(name="date_created")
	private Date creationDate;
	@SerializedName("date_modified") @XmlElement(name="date_modified")
	private Date modificationDate;
	@SerializedName("descriptor_version") @XmlElement(name="descriptor_version")
	private String descriptorVersion;
	@SerializedName("version") @XmlElement(name="version")
	private String version;
	@SerializedName("manifest_file_md5") @XmlElement(name="manifest_file_md5")
	private String manifest_file_md5;
	@SerializedName("Trade") @XmlElement(name="Trade")
	private Boolean trade;
	
	@SerializedName(VirtualDeploymentUnit.VDU) @XmlElement(name=VirtualDeploymentUnit.VDU) 
	@NotNull (message="At least one VDU must be present")
	@Size(min = 1, message="At least one VDU must be present")
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<VirtualDeploymentUnit> vdu;
	@SerializedName("billing_model") @XmlElement(name="billing_model")
	@NotNull (message="Billing must be present")
	@Embedded private Billing billing;
	@SerializedName("deployment_flavour") @XmlElement(name="deployment_flavour")
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<DeploymentFlavour> deploymentFlavour;

	public VNFD() {
		super();
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
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	public Date getModificationDate() {
		return modificationDate;
	}
	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}
	public String getDescriptorVersion() {
		return descriptorVersion;
	}
	public void setDescriptorVersion(String descriptorVersion) {
		this.descriptorVersion = descriptorVersion;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public List<DeploymentFlavour> getDeploymentFlavour() {
		return deploymentFlavour;
	}
	public void setDeploymentFlavour(List<DeploymentFlavour> deploymentFlavour) {
		this.deploymentFlavour = deploymentFlavour;
	}
	public List<VirtualDeploymentUnit> getVdu() {
		return vdu;
	}
	public void setVdu(List<VirtualDeploymentUnit> vdu) {
		this.vdu = vdu;
	}
	public Boolean getTrade() {
		return trade;
	}
	public void setTrade(Boolean trade) {
		this.trade = trade;
	}

	public String getProvider_id() {
		return provider_id;
	}

	public void setProvider_id(String provider_id) {
		this.provider_id = provider_id;
	}

	public String getManifest_file_md5() {
		return manifest_file_md5;
	}

	public void setManifest_file_md5(String manifest_file_md5) {
		this.manifest_file_md5 = manifest_file_md5;
	}

	public Billing getBilling() {
		return billing;
	}

	public void setBilling(Billing billing) {
		this.billing = billing;
	}

}
