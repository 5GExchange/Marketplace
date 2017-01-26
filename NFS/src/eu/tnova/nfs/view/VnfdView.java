package eu.tnova.nfs.view;

import eu.tnova.nfs.entity.VNFDescriptor;
import eu.tnova.nfs.exception.ValidationException;
import eu.tnova.nfs.producers.EnvValue;
import eu.tnova.nfs.ws.ServiceBean;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
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
import org.primefaces.json.JSONException;
import org.primefaces.json.JSONObject;
import org.primefaces.model.UploadedFile;

@SuppressWarnings("serial")
@ManagedBean(name="vnfdView")
@ViewScoped
public class VnfdView implements Serializable {
	@EJB private ServiceBean serviceBean;
	private List<Vnfd> vnfds = new ArrayList<Vnfd>();
	private Vnfd selectedVnfd;
	private UploadedFile file;
	@Inject @EnvValue("nfs.storePath")
	private String storePath;

	@PostConstruct
	public void init() {
		refreshVnfds();
	}

	public void refreshVnfds() {
		this.vnfds.clear();
		for (VNFDescriptor vnfDescriptor : this.serviceBean.getVNFDescriptors()) {
			this.vnfds.add(new Vnfd(vnfDescriptor,storePath));
		}
		if (this.vnfds.size() != 0) {
			this.selectedVnfd = ((Vnfd)this.vnfds.get(0));
		}
	}

	public List<Vnfd> getVnfds() {
		return this.vnfds;
	}
	public void setVnfds(List<Vnfd> vnfds) {
		this.vnfds = vnfds;
	}

	public void setSelectedVnfd(Vnfd vnfd) {
		this.selectedVnfd = vnfd;
	}
	public Vnfd getSelectedVnfd() {
		return this.selectedVnfd;
	}

	public String getSelectedVnfdJson() {
		if (this.selectedVnfd == null)
			return null;
		try	{
			String json = new JSONObject(this.selectedVnfd.getVnfd()).toString(4);
			json = json.replaceAll("\n", "<br>");
			return json.replaceAll("    ", "&nbsp;&nbsp;&nbsp;&nbsp;");
		} catch (JSONException e) {}
		return null;
	}

	public void deleteSelectedVnfd() {
		try {
			this.serviceBean.deleteVNFDescriptor(Integer.valueOf(this.selectedVnfd.getVnfdId()));
			refreshVnfds();
		} catch (ValidationException e) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
					"Error removing vnfd", e.getMessage());
			RequestContext.getCurrentInstance().showMessageInDialog(message);
		}
	}

	public UploadedFile getFile() {
		return this.file;
	}
	public void setFile(UploadedFile file) {
		this.file = file;
	}

	public void handleVnfdUpload(FileUploadEvent event) {
		this.file = event.getFile();
		String vnfd = new String(this.file.getContents(), StandardCharsets.UTF_8);
		try	{
			this.serviceBean.createVNFDescriptor(vnfd);
			refreshVnfds();
		} catch (ValidationException e) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, 
					"Error uploading vnfd", e.getMessage());
			RequestContext.getCurrentInstance().showMessageInDialog(message);
		}
	}
	
 }
