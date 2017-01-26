package eu.tnova.nfs.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.annotations.SerializedName;

@Embeddable
@XmlRootElement(name = "penalty")
public class Penalty implements Serializable {
	private static final long serialVersionUID = 1L;

	@SerializedName("type") @XmlElement(name="type")
	private String penaltyType;
	@SerializedName("value") @XmlElement(name="value") 
	private Integer penaltyValue;
	@SerializedName("unit") @XmlElement(name="unit")
	private String penaltyUnit;
	@SerializedName("validity") @XmlElement(name="validity")
	private String penaltyValidity;

	public Penalty() {
		super();
	}

	public String getPenaltyType() {
		return penaltyType;
	}

	public void setPenaltyType(String penaltyType) {
		this.penaltyType = penaltyType;
	}

	public Integer getPenaltyValue() {
		return penaltyValue;
	}

	public void setPenaltyValue(Integer penaltyValue) {
		this.penaltyValue = penaltyValue;
	}

	public String getPenaltyUnit() {
		return penaltyUnit;
	}

	public void setPenaltyUnit(String penaltyUnit) {
		this.penaltyUnit = penaltyUnit;
	}

	public String getPenaltyValidity() {
		return penaltyValidity;
	}

	public void setPenaltyValidity(String penaltyValidity) {
		this.penaltyValidity = penaltyValidity;
	}

}
