package eu.tnova.nfs.entity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlRootElement;

import eu.tnova.nfs.exception.ValidationException;

@SuppressWarnings("serial")
@Entity
@XmlRootElement(name = "file")
@NamedQueries({
	@NamedQuery(name = VNFFile.QUERY_READ_ALL, query = "select f from VNFFile f"),
	@NamedQuery(name = VNFFile.QUERY_READ_BY_NAME, query = "select f from VNFFile f where f.name=:name"),
	@NamedQuery(name = VNFFile.QUERY_READ_BY_PROVIDER, query = "select f from VNFFile f where f.providerId=:providerId"),
	})

public class VNFFile implements Serializable {
	public static final String QUERY_READ_ALL     = "findAllVNFFiles";
	public static final String QUERY_READ_BY_NAME = "findVNFFilesByName";
	public static final String QUERY_READ_BY_PROVIDER = "findVNFFilesByProvider";

	@Id
	private String name;
	private String md5Sum;
	private Integer providerId;
	private String imageType;
	@Enumerated(EnumType.STRING)
	private VNFFileStatusEnum status;
	@ManyToMany(
		fetch=FetchType.EAGER,
		cascade = {CascadeType.PERSIST, CascadeType.MERGE,CascadeType.REFRESH, CascadeType.DETACH},
		mappedBy="files"
	)
	private List<VNFDescriptor> vnfDescriptors = new ArrayList<VNFDescriptor>();
	
	public VNFFile() {
	}
	public VNFFile(String name) {
		this(name,VNFFileStatusEnum.NOT_AVAILABLE);
	}
	public VNFFile(String name, VNFFileStatusEnum status) {
		this(name, status, null);
	}
	public VNFFile(String name, VNFFileStatusEnum status, VNFDescriptor vnfDescriptor) {
		this.name = name;
		this.status = status;
		if ( vnfDescriptor!=null )
			vnfDescriptors.add(vnfDescriptor);
		md5Sum = "";
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<VNFDescriptor> getVnfDescriptors() {
		return vnfDescriptors;
	}
	public void setVnfDescriptors(List<VNFDescriptor> vnfDescriptors) {
		this.vnfDescriptors = vnfDescriptors;
	}
	public String getMd5Sum() {
		return md5Sum;
	}
	public void setMd5Sum(String md5Sum) {
		this.md5Sum = md5Sum;
	}
	public VNFFileStatusEnum getStatus() {
		return status;
	}
	public void setStatus(VNFFileStatusEnum status) {
		this.status = status;
	}
	public File getFile(String storePath) {
		return new File(storePath+File.separator+name);
	}
	public Integer getProviderId() {
		return providerId;
	}
	public void setProviderId(Integer providerId) {
		this.providerId = providerId;
	}	
	public String getImageType() {
		return imageType;
	}
	public void setImageType(String imageType) {
		this.imageType = imageType;
	}

	@Override
	public String toString() {
		return "VNFFile [name=" + name + ", md5Sum=" + md5Sum + 
				", providerId=" + providerId + ", imageType=" + imageType +
				", status=" + status
				+ "]";
	}

	public Long writeFile(InputStream inputStream, String storePath, boolean checkMd5Sum) 
			throws IOException,ValidationException {
		FileOutputStream os = new FileOutputStream(getFile(storePath));
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
		}
		byte[] buffer = new byte[51200];
		Long len = Long.valueOf(0L);
		int bytes = 0;
		try {
			while ((bytes = inputStream.read(buffer)) != -1) {
				os.write(buffer, 0, bytes);
				digest.update(buffer, 0, bytes);
				len = Long.valueOf(len.longValue() + bytes);
			}
			os.flush();
		} finally {
			os.close();
			inputStream.close();
		}
		String md5Sum = convertByteArrayToHexString(digest.digest());
		if ( checkMd5Sum ) {
			if ( !this.md5Sum.toUpperCase().equals(md5Sum.toUpperCase()) ) {
				throw new ValidationException(
						"wrong checksum : "+md5Sum+" instead "+this.md5Sum,
						Status.CONFLICT, true);
			}
		} else {
			this.md5Sum = md5Sum.toUpperCase();
		}
		return len;
	}

	private static String convertByteArrayToHexString (byte[] arrayBytes) { 
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < arrayBytes.length; i++) {
			stringBuffer.append(Integer.toString((arrayBytes[i] & 0xFF) + 256, 16).substring(1));
		}
		return stringBuffer.toString();
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		VNFFile vnfFile = new VNFFile(name, status);
		vnfFile.setMd5Sum(md5Sum);
		vnfFile.setProviderId(providerId);
		for ( VNFDescriptor vnfd: vnfDescriptors ) 
			vnfFile.getVnfDescriptors().add(vnfd);
		return vnfFile;
	}
}
