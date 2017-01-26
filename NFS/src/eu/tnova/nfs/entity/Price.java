package eu.tnova.nfs.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.annotations.SerializedName;

@Embeddable
@XmlRootElement(name = "price")
public class Price implements Serializable {
	private static final long serialVersionUID = 1L;

	@SerializedName("unit") @XmlElement(name="unit")
	private String unit;
	@SerializedName("min_per_period") @XmlElement(name="min_per_period")
	private Integer minPerPeriod;
	@SerializedName("max_per_period") @XmlElement(name="max_per_period")
	private Integer maxPerPeriod;
	@SerializedName("setup") @XmlElement(name="setup")
	private Integer setup;

	public Price() {
		super();
	}
	public Price(String unit, Integer minPerPeriod, Integer maxPerPeriod, Integer setup) {
		super();
		this.unit = unit;
		this.minPerPeriod = minPerPeriod;
		this.maxPerPeriod = maxPerPeriod;
		this.setup = setup;
	}
	public Price(String unit, String minPerPeriod, String maxPerPeriod, String setup) {
		super();
		this.unit = unit;
		if ( minPerPeriod!=null )
			this.minPerPeriod = Integer.valueOf(minPerPeriod);
		if ( maxPerPeriod!=null )
			this.maxPerPeriod = Integer.valueOf(maxPerPeriod);
		if ( setup!=null )
			this.setup = Integer.valueOf(setup);
	}

	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public Integer getMinPerPeriod() {
		return minPerPeriod;
	}
	public void setMinPerPeriod(Integer minPerPeriod) {
		this.minPerPeriod = minPerPeriod;
	}
	public Integer getMaxPerPeriod() {
		return maxPerPeriod;
	}
	public void setMaxPerPeriod(Integer maxPerPeriod) {
		this.maxPerPeriod = maxPerPeriod;
	}
	public Integer getSetup() {
		return setup;
	}
	public void setSetup(Integer setup) {
		this.setup = setup;
	}

	public boolean match (Price price) {
		if ( price.getUnit()!=null && unit!=null && 
				!price.getUnit().equals(unit) )
			return false;
		if ( minPerPeriod!=null && price.getMinPerPeriod()!=null && 
				minPerPeriod<price.getMinPerPeriod() )
			return false;
		if ( maxPerPeriod!=null && price.getMaxPerPeriod()!=null && 
				maxPerPeriod<price.getMaxPerPeriod() )
			return false;
		return true;
	}
}
