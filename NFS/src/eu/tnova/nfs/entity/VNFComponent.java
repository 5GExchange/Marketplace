package eu.tnova.nfs.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.annotations.SerializedName;

@Embeddable
@XmlRootElement(name = "Vnfc")
public class VNFComponent implements Serializable {
	private static final long serialVersionUID = 1L;

	@SerializedName("id") @XmlElement(name="id")
	private String id;

	public VNFComponent() {
		super();
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
}

