package eu.tnova.nfs.ws.entity;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.annotations.SerializedName;

import eu.tnova.nfs.entity.VNFFile;
import eu.tnova.nfs.entity.VNFFileStatusEnum;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "files")
public class VNFFileListResponse {
	@SerializedName("files") @XmlElement(name = "files")
	private List<VNFFileResponse> responseFiles = new ArrayList<VNFFileResponse>();

	public VNFFileListResponse() {
	}

	public VNFFileListResponse (List<VNFFile> vnfFiles, String storePath) {
		if ( vnfFiles!=null ) {
			for (VNFFile vnfFile: vnfFiles ) {
				if ( !vnfFile.getStatus().equals(VNFFileStatusEnum.NOT_AVAILABLE) && 
					 !vnfFile.getStatus().equals(VNFFileStatusEnum.UPLOAD) &&
						vnfFile.getFile(storePath).exists() )
					responseFiles.add(new VNFFileResponse(vnfFile));
			}
		}
	}
	
	public List<VNFFileResponse> getResponseFiles() {
		return responseFiles;
	}
	public void setResponseFiles(List<VNFFileResponse> responseFiles) {
		this.responseFiles = responseFiles;
	}

}
