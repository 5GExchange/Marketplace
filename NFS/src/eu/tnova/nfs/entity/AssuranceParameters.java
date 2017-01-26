package eu.tnova.nfs.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.annotations.SerializedName;

@Entity
@XmlRootElement(name = "assurance_parameters")
public class AssuranceParameters implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue
	private Integer DBId;
	@SerializedName("param_id")	@XmlElement(name="param_id")
	private String paramId;
	@SerializedName("value") @XmlElement(name="value")
	private Integer value;
	@SerializedName("unit")	@XmlElement(name="unit")
	private String unit;
	@SerializedName("formula") @XmlElement(name="formula")
	private String formula;
	@SerializedName("violation") @XmlElement(name="violation")
	@ElementCollection private List<Violation> violations;
	@SerializedName("penalty") 	@XmlElement(name="penalty")
	@Embedded private Penalty penalty;

	public AssuranceParameters() {
		super();
	}

	public Integer getDBId() {
		return DBId;
	}
	public void setDBId(Integer DBId) {
		this.DBId = DBId;
	}
	public String getParamId() {
		return paramId;
	}
	public void setParamId(String paramId) {
		this.paramId = paramId;
	}
	public Integer getValue() {
		return value;
	}
	public void setValue(Integer value) {
		this.value = value;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public Penalty getPenalty() {
		return penalty;
	}
	public void setPenalty(Penalty penalty) {
		this.penalty = penalty;
	}
	public List<Violation> getViolations() {
		return violations;
	}
	public void setViolations(List<Violation> violations) {
		this.violations = violations;
	}
	public String getFormula() {
		return formula;
	}
	public void setFormula(String formula) {
		this.formula = formula;
	}

}