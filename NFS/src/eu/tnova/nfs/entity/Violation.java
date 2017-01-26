package eu.tnova.nfs.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.annotations.SerializedName;

@Embeddable
@XmlRootElement(name = "violation")
public class Violation implements Serializable {
	private static final long serialVersionUID = 1L;

	@SerializedName("breaches_count") @XmlElement(name="breaches_count")
	private Integer breachesCount;
	@SerializedName("interval") @XmlElement(name="interval")
	private Integer interval;

	public Violation() {
		super();
	}

	public Integer getBreachesCount() {
		return breachesCount;
	}
	public void setBreachesCount(Integer breachesCount) {
		this.breachesCount = breachesCount;
	}
	public Integer getInterval() {
		return interval;
	}
	public void setInterval(Integer interval) {
		this.interval = interval;
	}
}