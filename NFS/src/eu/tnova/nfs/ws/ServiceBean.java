package eu.tnova.nfs.ws;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.naming.NoPermissionException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.Logger;

import eu.tnova.nfs.entity.VNFDescriptor;
import eu.tnova.nfs.entity.VNFFile;
import eu.tnova.nfs.entity.VNFFileStatusEnum;
import eu.tnova.nfs.exception.ValidationException;
import eu.tnova.nfs.producers.EnvValue;
import eu.tnova.nfs.ws.entity.VNFFileListResponse;
import eu.tnova.nfs.ws.orchestrator.OrchestratorBeanInterface;
import eu.tnova.nfs.ws.orchestrator.OrchestratorOperationTypeEnum;
import eu.tnova.nfs.ws.validator.NetworkFunctionStoreDescriptorValidator;
import eu.tnova.nfs.ws.validator.NetworkFunctionStoreFileValidator;

@Singleton
@Startup
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class ServiceBean {
	@Inject	private Logger log;
	@PersistenceContext(unitName = "NFS_DB") private EntityManager em;
	@Inject @EnvValue(EnvValue.storePath) private String storePath;
	@Inject @EnvValue(EnvValue.nfsUrl) private String nfs;
	@Inject private NetworkFunctionStoreDescriptorValidator vnfdValidator;
	@Inject private NetworkFunctionStoreFileValidator fileValidator;
//	@Inject private OrchestratorWSClient orchestratorClient;
	@EJB(beanName="OrchestratorBean") private OrchestratorBeanInterface orchestratorBean;

	public ServiceBean() {
	}

	@PostConstruct
	@SuppressWarnings("unchecked")
	public void init() {
		log.debug("ServiceBean started");
		// check files and align db availability
		log.debug("Check files availability");
		List<String> storeFiles = getStoreFiles();
		List<VNFFile> vnfFiles = em.createNamedQuery(VNFFile.QUERY_READ_ALL).getResultList();
		for ( VNFFile vnfFile: vnfFiles ) {
			if ( vnfFile.getStatus().equals(VNFFileStatusEnum.AVAILABLE) && 
					!storeFiles.contains(vnfFile.getName()) ) {
				if ( vnfFile.getVnfDescriptors().isEmpty() ) {
					log.warn("file {} not found into store; remove from DB", vnfFile.getName());
					em.remove(vnfFile);
				} else {
					log.warn("file {} not found into store; set not available into DB", vnfFile.getName());
					vnfFile.setStatus(VNFFileStatusEnum.NOT_AVAILABLE);
					em.merge(vnfFile);
				}
			} else {
				storeFiles.remove(vnfFile.getName());
			}
		}
		// files not present into DB
		for ( String fileName: storeFiles ) {
			log.warn("found into store file {} not described into DB; insert new object", fileName);
			em.persist( new VNFFile(fileName, VNFFileStatusEnum.AVAILABLE) );
		}
		em.flush();
	}

	@PreDestroy
	protected void destroy() {
		log.debug("ServiceBean stopped");
	}

	public VNFFile uploadVNFFile(String fileName, String md5Sum, String provider, String imageType) 
			throws ValidationException {
		log.debug("uploadVNFFile {}, md5Sum={}, provider={}, imageType={}", 
				fileName,md5Sum,provider,imageType);
		VNFFile vnfFile = fileValidator.validateUpload(fileName, md5Sum, provider, imageType);
		vnfFile.setStatus(VNFFileStatusEnum.UPLOAD);
		VNFFile vnfFileR = em.merge(vnfFile);
		em.flush();
		return vnfFileR;
	}
	public VNFFile endUploadVNFFile(VNFFile vnfFile) {
		log.debug("End Upload VNF File : {}",vnfFile.getName());
		if ( vnfFile.getVnfDescriptors()!=null ) {
			for ( VNFDescriptor vnfd: vnfFile.getVnfDescriptors() )
				vnfd.setJsonImageProperty(vnfFile.getName(), vnfFile.getMd5Sum(), vnfFile.getImageType() );
		}
		vnfFile.setStatus(VNFFileStatusEnum.AVAILABLE);
		em.merge(vnfFile);
		em.flush();
		return vnfFile;
	}

	public VNFFile updateVNFFile(String fileName, String md5Sum, String provider, String imageType) 
			throws ValidationException, CloneNotSupportedException {
		log.debug("updateVNFFile {}, md5Sum={}, provider={}, imageType={}", 
				fileName,md5Sum,provider,imageType);
		VNFFile vnfFile = fileValidator.validateUpdate(fileName, md5Sum, provider, imageType);
		vnfFile.setStatus(VNFFileStatusEnum.UPDATE);
		VNFFile vnfFileR = em.merge(vnfFile);
		em.flush();
		VNFFile vnfFileClone = (VNFFile) vnfFileR.clone();
		vnfFileClone.setMd5Sum(md5Sum);
		vnfFileClone.setProviderId(Integer.valueOf(provider));
		vnfFileClone.setImageType(imageType);
		vnfFileClone.setName(fileName);
		return vnfFileClone;
	}
	public VNFFile endUpdateVNFFile(VNFFile vnfFile) {
		log.debug("End Update VNF File : {}",vnfFile.getName());
		if ( vnfFile.getVnfDescriptors()!=null ) {
			for ( VNFDescriptor vnfd: vnfFile.getVnfDescriptors() )
				vnfd.setJsonImageProperty(vnfFile.getName(), vnfFile.getMd5Sum(), vnfFile.getImageType() );
		}
		vnfFile.setStatus(VNFFileStatusEnum.AVAILABLE);
		em.merge(vnfFile);
		em.flush();
		return vnfFile;
	}

	public VNFFile downloadVNFFile(String fileName, String contentType) 
			throws ValidationException {
		log.debug("downloadVNFFile {}", fileName);
		VNFFile vnfFile = fileValidator.validateDownload(fileName, contentType);
		em.flush();
		//		log.debug("end downloadVNFFile {}", fileName);
		return vnfFile;
	}
	public VNFFile endDownloadOfVNFFile(VNFFile vnfFile) {
		log.debug("End Write VNF File : {}",vnfFile.getName());
		vnfFile.setStatus(VNFFileStatusEnum.AVAILABLE);
		em.merge(vnfFile);
		em.flush();
		return vnfFile;
	}
	
	public VNFFile getVNFFile(String fileName) throws ValidationException {
		log.debug("getVNFFile {}", fileName);
		return fileValidator.validateDownload(fileName, null);
	}

	@SuppressWarnings("unchecked")
	public List<VNFFile> deleteVNFFile(String fileName) 
			throws ValidationException, Exception {
		log.debug("deleteVNFFile {}", fileName);
		List<VNFFile> vnfFiles = new ArrayList<VNFFile>();
		if ( fileName==null ) {
			vnfFiles = em.createNamedQuery(VNFFile.QUERY_READ_ALL).getResultList();
			for ( VNFFile vnfFile: vnfFiles)
				deleteFile(vnfFile);
			List<String> files = getStoreFiles();
			for (String file : files) 
				deleteFile(file);
		} else {
			VNFFile vnfFile = fileValidator.validateDelete(fileName);
			deleteFile(vnfFile);
			vnfFiles.add(vnfFile);
		}
		em.flush();
		return vnfFiles;
	}

	public VNFFile endUseOfVNFFileOnError(String fileName, boolean removeEntity, boolean removeFile) {
		log.debug("End Write VNF File on error : {}, removeEntity={}, removeFile={}", 
				fileName, removeEntity,removeFile);
//		if ( vnfFile!=null && !em.contains(vnfFile) )
		VNFFile vnfFile = em.find(VNFFile.class, fileName);
		if ( vnfFile!=null ) {
			if ( removeEntity ) {
				em.remove(vnfFile);
			} else {
				vnfFile.setStatus(VNFFileStatusEnum.AVAILABLE);
				em.merge(vnfFile);
			}	
			em.flush();
		}
		if ( removeFile ) {
			deleteFile(fileName);
			return null;
		}
		return vnfFile;
	}

	@SuppressWarnings("unchecked")
	public List<VNFFile> getVNFFiles() {
		return em.createNamedQuery(VNFFile.QUERY_READ_ALL).getResultList();
	}
	 
	public VNFFileListResponse getVNFFileList(Integer providerId) {
		List<VNFFile> files;
		if ( providerId==null )
			files = getVNFFiles();
		else 
			files = em.createNamedQuery(VNFFile.QUERY_READ_BY_PROVIDER, VNFFile.class).
				setParameter(providerId, providerId).getResultList();
		return new VNFFileListResponse(files, storePath); 
	}

	public VNFDescriptor createVNFDescriptor(String jsonVNFD) 
			throws ValidationException {
		log.debug("createVNFDescriptor");
		try {
			VNFDescriptor vnfDescriptor = vnfdValidator.validateCreate(jsonVNFD);
			em.persist(vnfDescriptor);
			vnfDescriptor.changeVmImagesToURL(nfs);
			vnfDescriptor.addIdToJson();
			setVnfDescriptorFiles(vnfDescriptor);
			VNFDescriptor vnfd = em.merge(vnfDescriptor);
			em.flush();
			return vnfd;
		} catch (ConstraintViolationException e) {
			throw (getValidationException(e));
		} catch (MalformedURLException e) {
			throw new ValidationException("Error generating file URL", Status.INTERNAL_SERVER_ERROR);
		}
	}

	public VNFDescriptor modifyVNFDescriptor(Integer vnfdId, String jsonVNFD) 
			throws ValidationException {
		log.debug("modifyVNFDescriptor");
		try {
			VNFDescriptor vnfDescriptor = vnfdValidator.validateUpdate(vnfdId, jsonVNFD);
			vnfDescriptor.changeVmImagesToURL(nfs);
			VNFDescriptor vnfd1 = em.merge(vnfDescriptor);
			setVnfDescriptorFiles(vnfd1);
			VNFDescriptor vnfd = em.merge(vnfd1);
			em.flush();
			return vnfd;
		} catch (ConstraintViolationException e) {
			throw (getValidationException(e));
		} catch (MalformedURLException e) {
			throw new ValidationException("Error generating file URL", Status.INTERNAL_SERVER_ERROR);
		}
	}
	public VNFDescriptor updateVNFDescriptor(VNFDescriptor vnfDescriptor) {
		VNFDescriptor vnfd = em.merge(vnfDescriptor);
		em.flush();
		return vnfd;
	}

	@SuppressWarnings("unchecked")
	public List<VNFDescriptor> deleteVNFDescriptor(Integer vnfdId) 
			throws ValidationException {
		log.debug("deleteVNFDescriptor {}", vnfdId);
		List<VNFDescriptor> vnfds = new ArrayList<VNFDescriptor>();
		if ( vnfdId==null ) {
			vnfds = em.createNamedQuery(VNFDescriptor.QUERY_READ_ALL).getResultList();
			for ( VNFDescriptor vnfDescriptor: vnfds)
				deleteVnfDescriptor(vnfDescriptor);
		} else {
			VNFDescriptor vnfDescriptor = vnfdValidator.validateDelete(vnfdId);
			deleteVnfDescriptor(vnfDescriptor);
			vnfds.add(vnfDescriptor);
		}
		em.flush();
		return vnfds;
	}
	public VNFDescriptor getVNFDescriptor(Integer vnfdId) 
			throws ValidationException {
		log.debug("getVNFDescriptor {}", vnfdId);
		VNFDescriptor vnfDescriptor = vnfdValidator.validateGet(vnfdId);
		return vnfDescriptor;
	}
	@SuppressWarnings("unchecked")
	public List<VNFDescriptor> getVNFDescriptors() {
		return em.createNamedQuery(VNFDescriptor.QUERY_READ_ALL).getResultList();
	}
	public Map<Integer,VNFDescriptor> getVNFDescriptorsMap() {
		List<VNFDescriptor> vnfds = getVNFDescriptors();
		Map<Integer,VNFDescriptor> vnfdsMap = new HashMap<Integer,VNFDescriptor>();
		for ( VNFDescriptor vnfd: vnfds )
			vnfdsMap.put(vnfd.getId(), vnfd);
		return vnfdsMap;
	}

	public void sendNotificationToOrchestrator(
			OrchestratorOperationTypeEnum operation, VNFFile vnfFile, String userToker) {
		sendNotificationToOrchestrator(operation, vnfFile.getVnfDescriptors(), userToker);
	}
	@SuppressWarnings("unchecked")
	public void sendNotificationToOrchestrator(
			OrchestratorOperationTypeEnum operation, List<?> objects, String userToker) {
		List<VNFDescriptor> vnfDescriptors = new ArrayList<VNFDescriptor>();
		if ( objects==null || objects.isEmpty() )
			return;
		if ( objects.get(0).getClass().equals(VNFFile.class) ) {
			for ( VNFFile vnfFile: (List<VNFFile>)objects ) {
				vnfDescriptors.removeAll(vnfFile.getVnfDescriptors());
				vnfDescriptors.addAll(vnfFile.getVnfDescriptors());
			}
		} else if ( objects.get(0).getClass().equals(VNFDescriptor.class) ) {
			vnfDescriptors = (List<VNFDescriptor>) objects;
		}
		for ( VNFDescriptor vnfDescriptor: vnfDescriptors )
			sendNotificationToOrchestrator(operation, vnfDescriptor, userToker);
	}
	public void sendNotificationToOrchestrator(
			OrchestratorOperationTypeEnum operation, VNFDescriptor vnfDescriptor, String userToker) {
		try {
			switch (operation) {
			case CREATE:
				orchestratorBean.create(vnfDescriptor, userToker);
//				if ( vnfDescriptor.isVnfCreated() ) {
//					log.error("VNF already created for {} operation", operation);
//					return;
//				}
//				if ( allFilesAreAvailable(vnfDescriptor) ) {
//					orchestratorClient.create_VNF(vnfDescriptor, userToker);
//				}
				break;
			case UPDATE:
				orchestratorBean.update(vnfDescriptor, userToker);
//				if ( !vnfDescriptor.isVnfCreated() ) {
//					log.error("VNF not created for {} operation", operation);
//					return;
//				}
//				if ( allFilesAreAvailable(vnfDescriptor) ) {
//					orchestratorClient.update_VNF(vnfDescriptor, userToker);
//				}
				break;
			case DELETE:
				orchestratorBean.delete(vnfDescriptor, userToker);
//				if ( !vnfDescriptor.isVnfCreated() ) {
//					log.error("VNF not created for {} operation", operation);
//					return;
//				}
//				if ( !allFilesAreAvailable(vnfDescriptor) ) {
//					orchestratorClient.delete_VNF(vnfDescriptor, userToker);
//				}
				break;
			}
//			try {
//				em.merge(vnfDescriptor);
//			} catch (Exception e) {
//			}
		} catch (Exception e) {
			log.error("problem notification of VNF {} to orchestrator : {}",
					operation, e.getMessage());
		}
	}

	public void deleteFile(VNFFile vnfFile) {
		log.debug("deleteFile VNFFile {}",vnfFile.getName());
		vnfFile.getFile(storePath).delete();
		if ( vnfFile.getVnfDescriptors().isEmpty() ) {
			em.remove(vnfFile);
		} else {
			vnfFile.setStatus(VNFFileStatusEnum.NOT_AVAILABLE);
			for ( VNFDescriptor vnfd: vnfFile.getVnfDescriptors() )
				vnfd.setJsonImageProperty(vnfFile.getName(), null, null );
			em.merge(vnfFile);
		}
	}
	public boolean deleteFile(String fileName) {
		log.debug("deleteFile {}",fileName);
		try {
			return getFile(fileName).delete();
		} catch (Exception e) {
			log.debug(e.getMessage());
//			e.printStackTrace();
			return false;
		}
	}
	public File getFile(String fileName) 
			throws FileNotFoundException, NoPermissionException {
		File file = new File(storePath+File.separator+fileName);
		if ( !file.exists() ) {
			log.warn("file "+fileName+" not found");
			throw new FileNotFoundException("file "+fileName+" not found");
		}
		if ( !file.isFile() ) {
			log.warn(fileName+" is not a File");
			throw new FileNotFoundException(fileName+" is not a File");
		}
		if ( !file.canRead() || !file.canWrite() ) {
			log.warn("file "+fileName+" not readable/writable");
			throw new NoPermissionException("file "+fileName+" not readable/writable");
		}
		return file;
	}
	private void setVnfDescriptorFiles(VNFDescriptor vnfDescriptor) {
		vnfDescriptor.getFiles().clear();
		for ( String imageName : vnfDescriptor.getvmImagesFileNames() ) {
			VNFFile vnfFile = em.find(VNFFile.class, imageName);
			if ( vnfFile==null ) {
				vnfFile = new VNFFile(imageName);
				em.persist(vnfFile);
			} else {
				vnfDescriptor.setJsonImageProperty(vnfFile.getName(), 
						vnfFile.getMd5Sum(), vnfFile.getImageType() );
			}
			vnfDescriptor.getFiles().add(vnfFile);
		}
	}
//	private boolean allFilesAreAvailable(VNFDescriptor vnfDescriptor) {
//		Map<String, VNFFile> files = vnfDescriptor.getFilesMap();
//		for ( String imageName : vnfDescriptor.getvmImagesFileNames() ) {
//			VNFFile image = files.get(imageName);
//			if ( image.getStatus().equals(VNFFileStatusEnum.NOT_AVAILABLE) || 
//					image.getStatus().equals(VNFFileStatusEnum.UPLOAD) )
//				return false;
//			File imageFile = image.getFile(storePath);
//			if ( !imageFile.exists() )
//				return false;
//		}
//		return true;
//	}

	private void deleteVnfDescriptor (VNFDescriptor vnfDescriptor) 
			throws ValidationException {
		for ( VNFFile vnfFile : vnfDescriptor.getFiles() ) {
			if ( vnfFile.getVnfDescriptors().size()==1 ) {
				if ( !vnfFile.getStatus().equals(VNFFileStatusEnum.NOT_AVAILABLE) )
					deleteFile(vnfFile.getName());
				em.remove(vnfFile);
			} else {
				vnfFile.getVnfDescriptors().remove(vnfDescriptor);
				em.merge(vnfFile);
			}
		}
		em.remove(vnfDescriptor);
	}
	private List<String> getStoreFiles () {
		List<String> list = new ArrayList<String>();
		File[] files = new File(storePath).listFiles();
		for (File file : files) {
			if (file.isFile())
				list.add(file.getName());
		}
		return list;
	}

	@SuppressWarnings({ "rawtypes" })
	private ValidationException getValidationException (
			ConstraintViolationException constraintViolationException ) {
		String message = new String();
		for ( ConstraintViolation constraintViolation : constraintViolationException.getConstraintViolations() )
			message += constraintViolation.getMessage()+"\n";
		return new ValidationException(message, Status.BAD_REQUEST);
	}

}
