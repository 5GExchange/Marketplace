package eu.tnova.nfs.view;

import eu.tnova.nfs.entity.VNFDescriptor;
import eu.tnova.nfs.entity.VNFFile;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

@SuppressWarnings("serial")
public class ImageFile implements Serializable {
	private String name;
	private String md5Sum;
	private String type;
	private Integer providerId;
	private Long size;
	private Date lastModifiedDate;
	private List<VNFDescriptor> vnfds = new ArrayList<VNFDescriptor>();

	public ImageFile(VNFFile vnfFile, String storePath) {
		this.name = vnfFile.getName();
		this.md5Sum = vnfFile.getMd5Sum();
		this.providerId = vnfFile.getProviderId();
		File file = vnfFile.getFile(storePath);
		this.size = Long.valueOf(file.length());
		this.lastModifiedDate = new Date(file.lastModified());
		this.vnfds = vnfFile.getVnfDescriptors();
		type = vnfFile.getImageType();
//		for (VNFDescriptor vnfDescriptor : vnfFile.getVnfDescriptors()) {
//			this.vnfds.add(vnfDescriptor.getId());
//		}
	}

	public String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getMd5Sum() {
		return this.md5Sum;
	}
	public void setMd5Sum(String md5Sum) {
		this.md5Sum = md5Sum;
	}

	public List<VNFDescriptor> getVnfds() {
		return this.vnfds;
	}
	public void setVnfds(List<VNFDescriptor> vnfds) {
		this.vnfds = vnfds;
	}

	public Long getSize() {
		return this.size;
	}
	public void setSize(Long size) {
		this.size = size;
	}

	public Integer getProviderId() {
		return providerId;
	}
	public void setProviderId(Integer providerId) {
		this.providerId = providerId;
	}

	public Date getLastModifiedDate() {
		if (this.size.longValue() == 0L)
			return null;
		return this.lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((md5Sum == null) ? 0 : md5Sum.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((providerId == null) ? 0 : providerId.hashCode());
		result = prime * result + ((size == null) ? 0 : size.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImageFile other = (ImageFile) obj;
		if (md5Sum == null) {
			if (other.md5Sum != null)
				return false;
		} else if (!md5Sum.equals(other.md5Sum))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (providerId == null) {
			if (other.providerId != null)
				return false;
		} else if (!providerId.equals(other.providerId))
			return false;
		if (size == null) {
			if (other.size != null)
				return false;
		} else if (!size.equals(other.size))
			return false;
		return true;
	}

    public TreeNode getVnfdRoot() {
    	TreeNode root = new DefaultTreeNode("ROOT", null);
    	TreeNode vnfdRoot = new DefaultTreeNode("VNFD", root);
    	for ( VNFDescriptor vnfd : vnfds ) {
        	TreeNode vnfNode = new DefaultTreeNode(vnfd.getId(), vnfdRoot);
           	TreeNode vnfNodeDesc = new DefaultTreeNode(vnfd.getVnfd().getDescription());
           	vnfNode.getChildren().add(vnfNodeDesc);
    	}
    	return root;
    }

}
