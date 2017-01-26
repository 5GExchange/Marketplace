package eu.tnova.nfs.ws.entity;

import java.util.Iterator;
import java.util.List;

import eu.tnova.nfs.entity.VNFDescriptor;

public class VNFDListResponse {
	private String json;

	public VNFDListResponse() {
	}

	public VNFDListResponse(List<VNFDescriptor> vnfds) {
		json = "{\"vnfds\":[";
		Iterator<VNFDescriptor> iterator = vnfds.iterator();
	    while ( iterator.hasNext() ) {
	    	json += iterator.next().getJson();
	    	if ( iterator.hasNext() )
	    		json += ",";
	      }			
	    json += "]}";
	}

	public String getJson() {
		return json;
	}
	public void setJson(String json) {
		this.json = json;
	}
}
