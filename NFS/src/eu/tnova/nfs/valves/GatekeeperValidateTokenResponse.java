package eu.tnova.nfs.valves;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.annotations.SerializedName;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class GatekeeperValidateTokenResponse {
	
	public class GatekeeperValidateTokenResponseMsg {
		@SerializedName("msg") @XmlElement(name = "msg")
		private String msg;

		public String getMsg() {
			return msg;
		}
		public void setMsg(String msg) {
			this.msg = msg;
		}
	}
	
	@SerializedName("metadata") @XmlElement(name = "metadata")
	private String metadata;
	@SerializedName("info") @XmlElement(name = "info")
	private List<GatekeeperValidateTokenResponseMsg> info;
	
	public String getMetadata() {
		return metadata;
	}
	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}
	public List<GatekeeperValidateTokenResponseMsg> getInfo() {
		return info;
	}
	public void setInfo(List<GatekeeperValidateTokenResponseMsg> info) {
		this.info = info;
	}
}
