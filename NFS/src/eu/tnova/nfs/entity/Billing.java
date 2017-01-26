package eu.tnova.nfs.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("serial")
@Embeddable
@XmlRootElement(name = "Billing")
public class Billing implements Serializable {

	@SerializedName("model") @XmlElement(name="model")
	@NotNull (message="Billing type must be specified")
	private String model;
	@SerializedName("period") @XmlElement(name="period")
	private String period;
	@SerializedName("price") @XmlElement(name="price")
	@Embedded private Price price;

	public Billing() {
		super();
	}
	public Billing(String model, String period, Price price) {
		super();
		this.model = model;
		this.period = period;
		this.price = price;
	}

	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public String getPeriod() {
		return period;
	}
	public void setPeriod(String period) {
		this.period = period;
	}
	public Price getPrice() {
		return price;
	}
	public void setPrice(Price price) {
		this.price = price;
	}

	public boolean match (Billing billing) {
		if ( model!=null && billing.getModel()!=null && 
				!billing.getModel().equals(model) )
			return false;
		if ( period!=null && billing.getPeriod()!=null && 
				!billing.getPeriod().equals(period) )
	    	return false;
		if ( !price.match(billing.getPrice()) )
	      return false;
	    return true;
	  }

}
