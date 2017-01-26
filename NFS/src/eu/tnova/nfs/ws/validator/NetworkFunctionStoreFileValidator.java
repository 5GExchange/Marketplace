package eu.tnova.nfs.ws.validator;

import java.io.File;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.Logger;

import eu.tnova.nfs.entity.VNFFile;
import eu.tnova.nfs.entity.VNFFileStatusEnum;
import eu.tnova.nfs.exception.ValidationException;
import eu.tnova.nfs.producers.EnvValue;

public class NetworkFunctionStoreFileValidator {
	@Inject	private Logger log;
	@PersistenceContext(unitName = "NFS_DB") private EntityManager em;
	@Inject @EnvValue(EnvValue.storePath) private String storePath;

	public NetworkFunctionStoreFileValidator() {
	}

	public VNFFile validateUpload (String fileName, String md5Sum, String provider, String imageType) 
			throws ValidationException {
		if ( md5Sum==null ) 
			throw new ValidationException("file checksum not set into upload request");
		if ( md5Sum.isEmpty() ) 
			throw new ValidationException("empty file checksum into upload request");
		Integer providerId = null;
		if ( provider!=null ) {
			try {
				providerId = Integer.valueOf(provider); 
			} catch (NumberFormatException e) {
				throw new ValidationException("provider Id is not a numeric value");
			}
		}
		VNFFile vnfFile = em.find(VNFFile.class, fileName);
		if ( vnfFile==null ) {
			vnfFile = new VNFFile(fileName);
			// check if file is into store (upload not possible)
			if ( vnfFile.getFile(storePath).exists() )
				throw new ValidationException("file "+vnfFile.getName()+" present but not found into db", 
						Status.INTERNAL_SERVER_ERROR, true);
		} else {
			log.debug("status={}", vnfFile.getStatus());
			// file already into db; check if available (upload not possible)
			if ( !vnfFile.getStatus().equals(VNFFileStatusEnum.NOT_AVAILABLE) )
				throw new ValidationException("file "+fileName+" already present into DB");
			// check if file is into store (upload not possible)
			if ( vnfFile.getFile(storePath).exists() )
				throw new ValidationException("file "+vnfFile.getName()+" already present", false);
		}
		vnfFile.setProviderId(providerId);
		vnfFile.setMd5Sum(md5Sum);
		if ( imageType==null || imageType.isEmpty() )
			imageType="unknown";
		vnfFile.setImageType(imageType);
		return vnfFile;
	}
	
	public VNFFile validateUpdate (String fileName, String md5Sum, String provider, String imageType) 
			throws ValidationException {
		if ( md5Sum==null ) 
			throw new ValidationException("file checksum not set into update request");
		if ( md5Sum.isEmpty() ) 
			throw new ValidationException("empty file checksum into upload request");
		if ( provider!=null ) {
			try {
				@SuppressWarnings("unused")
				Integer providerId = Integer.valueOf(provider); 
			} catch (NumberFormatException e) {
				throw new ValidationException("provider Id is not a numeric value");
			}
		}
		if ( imageType==null || imageType.isEmpty() )
			imageType="unknown";
		VNFFile vnfFile = em.find(VNFFile.class, fileName);
		if ( vnfFile==null )
			throw new ValidationException("Not found VNFFile with name "+fileName, 
					Status.NOT_FOUND);
		log.debug("status={}", vnfFile.getStatus());
		if ( vnfFile.getStatus().equals(VNFFileStatusEnum.NOT_AVAILABLE) )
			throw new ValidationException("file "+vnfFile.getName()+" not available",
					Status.NOT_FOUND);
//		validateFile(vnfFile.getFile(storePath));
		if ( !vnfFile.getStatus().equals(VNFFileStatusEnum.AVAILABLE) )
			throw new ValidationException("file "+vnfFile.getName()+" in use - operation not possible",
					Status.SERVICE_UNAVAILABLE);
		return vnfFile;
	}
	
	public VNFFile validateDelete (String fileName) 
			throws ValidationException {
		VNFFile vnfFile = validateUpdate(fileName, "-", null, null);
		return vnfFile;
	}
	
	public VNFFile validateDownload (String fileName, String contentType) 
			throws ValidationException {
		if ( contentType!=null && !contentType.equals("multipart") )
			throw new ValidationException("Unknown required content type "+contentType, 
					Status.NOT_FOUND);
		VNFFile vnfFile = em.find(VNFFile.class, fileName);
		if ( vnfFile==null )
			throw new ValidationException("Not found VNFFile with name "+fileName, 
					Status.NOT_FOUND);
		log.debug("status={}", vnfFile.getStatus());
		if ( vnfFile.getStatus().equals(VNFFileStatusEnum.NOT_AVAILABLE) )
			throw new ValidationException("file "+vnfFile.getName()+" not available",
					Status.NOT_FOUND);
		validateFile(vnfFile.getFile(storePath));
		if ( vnfFile.getStatus().equals(VNFFileStatusEnum.UPDATE) || 
				vnfFile.getStatus().equals(VNFFileStatusEnum.UPLOAD) 	)
			throw new ValidationException("file "+vnfFile.getName()+" in use - read not possible",
					Status.SERVICE_UNAVAILABLE);
		return vnfFile;
	}
	
	private void validateFile (File file) throws ValidationException {
		if ( !file.exists() )
			throw new ValidationException("file "+file.getName()+" not found",
					Status.INTERNAL_SERVER_ERROR);
		if ( !file.isFile() )
			throw new ValidationException(file.getName()+" is not a File",
					Status.INTERNAL_SERVER_ERROR);
		if ( !file.canRead() || !file.canWrite() )
			throw new ValidationException("file "+file.getName()+" not readable/writable",
					Status.INTERNAL_SERVER_ERROR);
	}
}
