package eu.tnova.nfs.view;

import eu.tnova.nfs.entity.VNFDescriptor;
import eu.tnova.nfs.entity.VNFFile;
import eu.tnova.nfs.producers.EnvValue;
import eu.tnova.nfs.ws.ServiceBean;
import eu.tnova.nfs.ws.orchestrator.OrchestratorOperationTypeEnum;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;
import org.primefaces.model.UploadedFile;

@SuppressWarnings("serial")
@ManagedBean(name="imageFileView")
@ViewScoped
public class ImageFileView implements Serializable {
	@EJB private ServiceBean serviceBean;
	@Inject @EnvValue("nfs.storePath")
	private String storePath;
	private List<ImageFile> imageFiles = new ArrayList<ImageFile>();
	private ImageFile selectedImageFile;
	private UploadedFile file;
	VNFFile donwloadVNFFile;

	@PostConstruct
	public void init()
	{
		refreshImageFiles();
	}

	public void refreshImageFiles()
	{
		imageFiles.clear();
		for (VNFFile vnfFile : serviceBean.getVNFFiles()) {
			imageFiles.add(new ImageFile(vnfFile, storePath));
		}
		if (imageFiles.size() != 0){
			if ((selectedImageFile == null) || (!imageFiles.contains(selectedImageFile))) {
				selectedImageFile = ((ImageFile)imageFiles.get(0));
			}
		} else {
			selectedImageFile = null;
		}
	}

	public List<ImageFile> getImageFiles() {
		if ( imageFiles.isEmpty() )
			refreshImageFiles();
		return imageFiles;
	}
	public void setImageFiles(List<ImageFile> imageFiles) {
		this.imageFiles = imageFiles;
	}
	public void setSelectedImageFile(ImageFile imageFile) {
		selectedImageFile = imageFile;
	}

	public ImageFile getSelectedImageFile() {
		return selectedImageFile;
	}

	public void deleteSelectedImageFile() {
		try {
			List<VNFFile> vnfFiles = serviceBean.deleteVNFFile(selectedImageFile.getName());
			serviceBean.sendNotificationToOrchestrator(
					OrchestratorOperationTypeEnum.DELETE, vnfFiles, null);
			refreshImageFiles();
		} catch (Exception e) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error removing Image File", e.getMessage());
			RequestContext.getCurrentInstance().showMessageInDialog(message);
		}
	}

	public UploadedFile getFile() {
		return file;
	}
	public void setFile(UploadedFile file) {
		this.file = file;
	}

	public void handleImageFileUpload(FileUploadEvent event) {
		file = event.getFile();
		VNFFile vnfFile = null;
		try {
			vnfFile = serviceBean.uploadVNFFile(file.getFileName(),"---", null, null);
			vnfFile.writeFile(file.getInputstream(), storePath, false);
			serviceBean.endUploadVNFFile(vnfFile);
			serviceBean.sendNotificationToOrchestrator(
					OrchestratorOperationTypeEnum.CREATE, vnfFile, null);
			refreshImageFiles();
		} catch (Exception e) {
			serviceBean.endUseOfVNFFileOnError(file.getFileName(), true, true);
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error Uploading ImageFile", e.getMessage());
			RequestContext.getCurrentInstance().showMessageInDialog(message);
		}
	}

	public boolean isSelectedFileNotPresent() {
		if ( selectedImageFile == null ||  selectedImageFile.getSize()==0 )
			return true;
		return false;
	}

	public void onRowSelect(SelectEvent event) {
		selectedImageFile = ((ImageFile)event.getObject());
	}

	public StreamedContent getDownloadFile() {
		if ((donwloadVNFFile != null) || (selectedImageFile == null) || (selectedImageFile.getSize().longValue() == 0L)) {
			return null;
		}
		try {
			donwloadVNFFile = serviceBean.downloadVNFFile(selectedImageFile.getName(),null);
			File file = donwloadVNFFile.getFile(storePath);
			return new DefaultStreamedContent(new FileInputStream(file.getPath()), "application/octet-stream", selectedImageFile.getName());
		} catch (Exception e) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error Download File ImageFile", e.getMessage());

			RequestContext.getCurrentInstance().showMessageInDialog(message);
			endDownloadFile();
		}
		return null;
	}

	public void endDownloadFile() {
		if (donwloadVNFFile != null) {
			serviceBean.endDownloadOfVNFFile(donwloadVNFFile);
			donwloadVNFFile = null;
		}
	}
	
}
