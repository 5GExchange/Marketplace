package eu.tnova.nfs.entity;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

@Entity
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "VNFDescriptor")
@NamedQueries({
	@NamedQuery(name = VNFDescriptor.QUERY_READ_ALL, query = "select d from VNFDescriptor d"),
	@NamedQuery(name = VNFDescriptor.QUERY_READ_BY_ID, query = "select d from VNFDescriptor d where d.id=:id"),
	})

public class VNFDescriptor implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final String QUERY_READ_ALL   = "findAllVNFDescriptors";
	public static final String QUERY_READ_BY_ID = "findVNFDescriptorById";

	@Id @GeneratedValue 
	private Integer id;
	private boolean vnfCreated;
	@NotNull private VNFD vnfd;
	@Lob @Basic(fetch=FetchType.EAGER)
	private String json;
	@ManyToMany(
		fetch=FetchType.EAGER,
		cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH}
	)
	private List<VNFFile> files = new ArrayList<VNFFile>();

	public VNFDescriptor() {
		super();
	    vnfCreated=false;
	}
	public VNFDescriptor(String jsonVNFD) throws JsonSyntaxException {
		json = jsonVNFD;
		Gson gson = new Gson();
		vnfd = gson.fromJson(json, VNFD.class);
	    JsonElement jsonElement = (JsonElement) gson.fromJson(jsonVNFD, JsonElement.class);
	    JsonElement idElement = jsonElement.getAsJsonObject().get("id");
	    if (idElement != null)
	    	id = Integer.valueOf(idElement.getAsInt());
	    vnfCreated=false;
	}

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
//	public String getVnfId() {
//		return vnfId;
//	}
//	public void setVnfId(String vnfId) {
//		this.vnfId = vnfId;
//	}
	public boolean isVnfCreated() {
		return vnfCreated;
	}
	public void setVnfCreated(boolean vnfCreated) {
		this.vnfCreated = vnfCreated;
	}
	public VNFD getVnfd() {
		return vnfd;
	}
	public void setVnfd(VNFD vnfd) {
		this.vnfd = vnfd;
	}
	public List<VNFFile> getFiles() {
		return files;
	}
	public void setFiles(List<VNFFile> files) {
		this.files = files;
	}
	public String getJson() {
		return json;
	}
	public void setJson(String json) {
		this.json = json;
	}

	public Map<String,VNFFile> getFilesMap() {
		Map <String,VNFFile> fileMap = new HashMap<String,VNFFile>();
		for (VNFFile file: files)
			fileMap.put(file.getName(), file);
		return fileMap;
	}
	
	public List<String> getvmImages() {
		List<String> images = new ArrayList<String>();
		if ( vnfd.getVdu()!=null && !vnfd.getVdu().isEmpty() ) {
			for (VirtualDeploymentUnit vdu: vnfd.getVdu()) {
				images.add(vdu.getVmImage());
			}
		}
		return images;
	}
	public List<String> getvmImagesFileNames() {
		List<String> files = new ArrayList<String>();
		for ( String image : getvmImages() ) {
			if ( image==null )
				continue;
			String[] parts = image.split("/");
			files.add(parts[parts.length-1]);
		}
		return files;
	}
	
	public void addIdToJson() {
		Gson gson = new Gson();
		JsonElement jsonElement = gson.fromJson(json, JsonElement.class);
		jsonElement.getAsJsonObject().addProperty("id", id);
		json=gson.toJson(jsonElement);
	}
	
	public void changeVmImagesToURL(String NfsUrl) throws MalformedURLException {
		Gson gson = new Gson();
		JsonElement jsonElement = gson.fromJson(json, JsonElement.class);
		JsonArray vdus = jsonElement.getAsJsonObject().getAsJsonArray(VirtualDeploymentUnit.VDU);
		for ( int i=0; i<vdus.size(); i++ ) {
			JsonObject obj = vdus.get(i).getAsJsonObject();
			JsonElement imageElement = obj.get(VirtualDeploymentUnit.VM_IMAGE);
			if ( imageElement==null )
				continue;
			String image = imageElement.getAsString();
			if ( image==null || image.isEmpty())
				continue;
			//String[] parts = image.split("/");
			//String uri = new URL(NfsUrl)+"/files/"+parts[parts.length-1];
			//obj.addProperty(VirtualDeploymentUnit.VM_IMAGE, uri);
			obj.addProperty(VirtualDeploymentUnit.VM_IMAGE, image);
		}
		json=gson.toJson(jsonElement);
		vnfd=gson.fromJson(json, VNFD.class);
	}
	public boolean setJsonImageProperty(String imageName, String md5Sum, String imageType) {
		boolean result = false;
		Gson gson = new Gson();
		JsonElement jsonElement = gson.fromJson(json, JsonElement.class);
		JsonArray vduList = jsonElement.getAsJsonObject().getAsJsonArray(VirtualDeploymentUnit.VDU);
		for ( JsonElement vduElement : vduList) {
			JsonElement imageElement = vduElement.getAsJsonObject().get(VirtualDeploymentUnit.VM_IMAGE);
			if ( imageElement==null )
				continue;
			String image = imageElement.getAsString();
			if ( image==null || image.isEmpty())
				continue;
		    if ( image.endsWith("/files/"+imageName) ) {
		    	if ( md5Sum==null || md5Sum.isEmpty() ) {
		    		vduElement.getAsJsonObject().remove(VirtualDeploymentUnit.VM_IMAGE_MD5);
		    	} else {
		    		vduElement.getAsJsonObject().addProperty(
		    				VirtualDeploymentUnit.VM_IMAGE_MD5, md5Sum);
		    	}
		    	if ( imageType==null || imageType.isEmpty() ) {
		    		vduElement.getAsJsonObject().remove(VirtualDeploymentUnit.VM_IMAGE_FORMAT);
		    	} else {
		    		vduElement.getAsJsonObject().addProperty(
		    				VirtualDeploymentUnit.VM_IMAGE_FORMAT, imageType);
		    	}
		    	result=true;
		    }
		}
		if ( result ) {
	    	json=gson.toJson(jsonElement);
	    	vnfd=gson.fromJson(json, VNFD.class);
		}
    	return result;
	}
	
}
