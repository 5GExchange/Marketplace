package eu.tnova.nfs.ws.orchestrator;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.Logger;

import eu.tnova.nfs.entity.VNFDescriptor;
import eu.tnova.nfs.entity.VNFFile;
import eu.tnova.nfs.entity.VNFFileStatusEnum;
import eu.tnova.nfs.exception.ValidationException;
import eu.tnova.nfs.producers.EnvValue;
import eu.tnova.nfs.ws.ServiceBean;

@Singleton
@Startup
@Local({ OrchestratorBeanInterface.class, OrchestratorBeanInterface.class })
@EJB(name="OrchestratorBean", beanName="OrchestratorBean", beanInterface=OrchestratorBeanInterface.class)
@DependsOn("ServiceBean")
public class OrchestratorBean implements OrchestratorBeanInterface {
	@Inject	private static Logger log;
	@PersistenceContext(unitName = "NFS_DB") private EntityManager em;
	@EJB private ServiceBean service;
	@Resource private TimerService timerService;
	@Inject @EnvValue(EnvValue.storePath) private String storePath;
	@Inject private OrchestratorWSClient orchestratorClient;

	private static final long POLLING_TIME = 1000*60*10;
	private static final long INITIAL_POLLING_TIME = 1000*60;
	private static final long INITIAL_ALIGN_POLLING_TIME = 1000*30;
	
	public OrchestratorBean() {
	}

	@PostConstruct
	public void init() {
		log.info("init");
		alignVNF();
//		// try to align orchestrator
//		List<VNFDescriptor> vnfds = service.getVNFDescriptors();
//		OrchestratorVNF vnf = null;
//		for ( VNFDescriptor vnfd : vnfds ) {
//			if ( !vnfd.isVnfCreated() ) {
//				vnf = new OrchestratorVNF(
//						vnfd.getId(), OrchestratorOperationTypeEnum.CREATE, null);
//				startPeriodicTimer(vnf);
//				log.info("start timer to CREATE vnf {}", vnfd.getId() );
//			} else {
//				if ( !allFilesAreAvailable(vnfd) ) {
//					vnf = new OrchestratorVNF(
//							vnfd.getId(), OrchestratorOperationTypeEnum.DELETE, null);
//					startPeriodicTimer(vnf);
//					log.info("start timer to DELETE vnf {}", vnfd.getId() );
////				} else {
////					vnf = new OrchestratorVNF(
////							vnfd.getId(), OrchestratorOperationTypeEnum.UPDATE, null);
////					startPeriodicTimer(vnf);
//				}
//			}
//		}
	}
	@PreDestroy
	protected void destroy() {
		log.info("destroy");
	}

	@Override
	public boolean create(VNFDescriptor vnfDescriptor, String userToker) {
		log.info("create VNF with VNFD {}", vnfDescriptor.getId());
		if ( vnfDescriptor.isVnfCreated() || !allFilesAreAvailable(vnfDescriptor) ) {
			if (vnfDescriptor.isVnfCreated())
				log.info("dice qeu ya existe");
			if (!allFilesAreAvailable(vnfDescriptor))
				log.info("Archivos no disponibles");
			return false;
		}
		
		log.info("Como no existe, lo crea");
		OrchestratorVNF vnf = new OrchestratorVNF(
				vnfDescriptor.getId(), OrchestratorOperationTypeEnum.CREATE, userToker);
		ArrayList<Timer> timers = getTimers(vnf);
		Timer timer = null;
		if ( timers!=null && !timers.isEmpty() ) {
			log.warn("found timer already active for VNFD {}", vnfDescriptor.getId());
			timer = timers.get(0);
			OrchestratorVNF vnfTtimer = (OrchestratorVNF) timer.getInfo();
			switch ( vnfTtimer.getOperation() ) {
			case CREATE:
				break;
			default:
				return false;
			}
		}
		try {
			orchestratorClient.create_VNF(vnfDescriptor, userToker);
			if ( timer!=null ) {
				timer.cancel();
				log.info("CREATE timer removed");
			}
			em.merge(vnfDescriptor);
			em.flush();
			log.info("VNF {} created", vnfDescriptor.getId());
			return true;
		} catch (ValidationException e) {
			log.error(e.getMessage());
			if ( e.getStatus() == null || e.getStatus().getStatusCode() == 409) {
				log.info("VNF {} already found on orchestrator", vnfDescriptor.getId());
				if ( timer!=null ) {
					timer.cancel();
					log.info("timer removed");
				}
				return true;
			}
		} catch (Exception e) {
			log.error("problem create VNF on orchestrator : {}", e.getMessage());
		}
		if ( timer==null )
			startPeriodicTimer(vnf, INITIAL_POLLING_TIME);
		return false;
	}

	@Override
	public boolean update(VNFDescriptor vnfDescriptor, String userToker) {
		log.info("update VNF with VNFD {}", vnfDescriptor.getId());
		if ( !vnfDescriptor.isVnfCreated() || !allFilesAreAvailable(vnfDescriptor) )
			return false;
		OrchestratorVNF vnf = new OrchestratorVNF(
				vnfDescriptor.getId(), OrchestratorOperationTypeEnum.UPDATE, userToker);
		ArrayList<Timer> timers = getTimers(vnf);
		Timer timer = null;
		if ( timers!=null && !timers.isEmpty() ) {
			log.warn("found timer already active for VNFD {}", vnfDescriptor.getId());
			timer = timers.get(0);
			OrchestratorVNF vnfTtimer = (OrchestratorVNF) timer.getInfo();
			switch ( vnfTtimer.getOperation() ) {
			case CREATE:
				log.info("change operation to CREATE");
				return create(vnfDescriptor, userToker);
			case UPDATE:
				break;
			default:
				return false;
			}
		}
		try {
			orchestratorClient.update_VNF(vnfDescriptor, userToker);
			if ( timer!=null ) {
				timer.cancel();
				log.info("UPDATE timer removed");
			}
			log.info("VNF {} updated", vnfDescriptor.getId());
			return true;
		} catch (ValidationException e) {
			log.error(e.getMessage());
			if ( e.getStatus().equals(Status.NOT_FOUND) ) {
				log.info("VNF {} not found on orchestrator", vnfDescriptor.getId());
				if ( timer!=null ) {
					timer.cancel();
					log.info("timer removed");
				}
				return false;
			}
		} catch (Exception e) {
			log.error("problem update VNF to orchestrator : {}", e.getMessage());
		}
		if ( timer==null )
			startPeriodicTimer(vnf, INITIAL_POLLING_TIME);
		return false;
	}

	@Override
	public boolean delete (VNFDescriptor vnfDescriptor, String userToker) {
		log.info("delete VNF with VNFD {}", vnfDescriptor.getId());
		if ( !vnfDescriptor.isVnfCreated() )
			return false;
		OrchestratorVNF vnf = new OrchestratorVNF(
				vnfDescriptor.getId(), OrchestratorOperationTypeEnum.DELETE, userToker);
		ArrayList<Timer> timers = getTimers(vnf);
		Timer timer = null;
		if ( timers!=null && !timers.isEmpty() ) {
			log.warn("found timer already active for VNFD {}", vnfDescriptor.getId());
			timer = timers.get(0);
			OrchestratorVNF vnfTtimer = (OrchestratorVNF) timer.getInfo();
			switch ( vnfTtimer.getOperation() ) {
			case CREATE:
				timer.cancel();
				log.info("{} timer removed : delete not needed", vnfTtimer.getOperation());
				return true;
			default:
				break;
			}
		}
		try {
			orchestratorClient.delete_VNF(vnfDescriptor, userToker);
			if ( timer!=null ) {
				timer.cancel();
				log.info("timer removed");
			}
			if ( em.find(VNFDescriptor.class, vnfDescriptor.getId()) != null ) {
				em.merge(vnfDescriptor);
				em.flush();
			}
			log.info("VNF {} removed", vnfDescriptor.getId());
			return true;
		} catch (ValidationException e) {
			if ( e.getStatus().equals(Status.NOT_FOUND) ) {
				log.info("VNF {} not found on orchestrator", vnfDescriptor.getId());
				if ( timer!=null ) {
					timer.cancel();
					log.info("timer removed");
				}
				return true;
			} 
			log.error(e.getMessage());
		} catch (Exception e) {
			log.error("problem remove VNF from orchestrator : {}", e.getMessage());
		}
		if ( timer==null )
			startPeriodicTimer(vnf, INITIAL_POLLING_TIME);
		return false;
	}

	@Timeout
	private void timerHandler (Timer timer) {
		OrchestratorVNF vnf = (OrchestratorVNF) timer.getInfo();
		log.info("time expired for {} at {}", 
				vnf, Calendar.getInstance().getTime() );
		VNFDescriptor vnfd = null;
		switch (vnf.getOperation()) {
		case CREATE:
		case UPDATE:
			try {
				vnfd = service.getVNFDescriptor(vnf.getVnfdId());
				log.info("vnf created = {}",vnfd.isVnfCreated());
			} catch (ValidationException e) {
				log.warn("timerHandler {} : not found vnfd {} -> remove timer", 
						vnf.getOperation(), vnf.getVnfdId());
				timer.cancel();
				return;
			}
		default:
			break;
		}
		try {
			switch (vnf.getOperation()) {
			case CREATE:
				if ( !vnfd.isVnfCreated() ) {
					orchestratorClient.create_VNF(vnfd, vnf.getUserToker());
					service.updateVNFDescriptor(vnfd);
				}
				break;
			case UPDATE:
				if ( vnfd.isVnfCreated() ) {
					orchestratorClient.update_VNF(vnfd, vnf.getUserToker());
				}
				break;
			case DELETE:
				vnfd = new VNFDescriptor();
				vnfd.setId(vnf.getVnfdId());
				orchestratorClient.delete_VNF(vnfd, vnf.getUserToker());
				try {
					vnfd = service.getVNFDescriptor(vnf.getVnfdId());
					vnfd.setVnfCreated(false);
					service.updateVNFDescriptor(vnfd);
				} catch (ValidationException e) {
					log.info("timerHandler DELETE : not found vnfd {}", vnf.getVnfdId());
				}
			default:
				break;
			}
			log.info("timerHandler : remove timer for vnfd {}", vnf.getVnfdId());
			timer.cancel();
		} catch (ValidationException e) {
			if ( e.getStatus()==null && !vnf.getOperation().equals(OrchestratorOperationTypeEnum.DELETE)) {
				log.info("timerHandler : VNF {} already found on orchestrator", vnf.getVnfdId());
				if ( timer!=null ) {
					timer.cancel();
					log.info("timerHandler : timer removed");
				}
			} else if ( e.getStatus().equals(Status.NOT_FOUND) ) {
				log.info("timerHandler : VNF {} not found on orchestrator", vnf.getVnfdId());
				if ( timer!=null ) {
					timer.cancel();
					log.info("timerHandler : timer removed");
				}
			}
			log.error(e.getMessage());
		} catch (Exception e) {
			log.error("timerHandler : problem notification {} VNF to orchestrator : {}", 
					vnf.getOperation(), e.getMessage());
		}
	}

	private boolean allFilesAreAvailable(VNFDescriptor vnfDescriptor) {
		Map<String, VNFFile> files = vnfDescriptor.getFilesMap();
		for ( String imageName : vnfDescriptor.getvmImagesFileNames() ) {
			VNFFile image = files.get(imageName);
			if ( image.getStatus().equals(VNFFileStatusEnum.NOT_AVAILABLE) || 
					image.getStatus().equals(VNFFileStatusEnum.UPLOAD) )
				//return false;
				return true;
			File imageFile = image.getFile(storePath);
			if ( !imageFile.exists() )
				//return false;
				return true;
		}
		return true;
	}
	
	private ArrayList<Timer> getTimers(OrchestratorVNF vnf) {
		Collection<Timer> timers = timerService.getTimers();
		if ( timers==null || timers.isEmpty() )
			return null;
		ArrayList<Timer> vnfTimers = new ArrayList<Timer>();
		for ( Timer timer: timers ) {
			OrchestratorVNF vnfTimer = (OrchestratorVNF) timer.getInfo();
			if ( vnfTimer.getVnfdId().equals(vnf.getVnfdId()) )
				vnfTimers.add(timer);
		}
		return vnfTimers;
	}

	private boolean startPeriodicTimer(OrchestratorVNF vnf, long initialDuration) {
		try {
			TimerConfig timerConfig=new TimerConfig(vnf, false);
			timerService.createIntervalTimer( initialDuration, POLLING_TIME, timerConfig);
			log.info("periodic timer started for {}", vnf);
			return true;
		} catch (Exception e) {
			log.error("Error starting periodic timer for {}", vnf); 
			return false;
		}
	}

	private void alignVNF() {
		Map<Integer,VNFDescriptor> vnfds = service.getVNFDescriptorsMap();
		OrchestratorVNF vnf = null;
		// get orchestrator VNF list
		Map<Integer,VNFDescriptor> orchestratorVnfds = new HashMap<Integer,VNFDescriptor>();
		try {
			orchestratorVnfds = orchestratorClient.get_VNFDescriptors(null);
		} catch (Exception e) {
			log.error("alignVNF : error get VNF list from orchestrator : {}", 
					e.getMessage());
		}
		log.debug("alignVNF : {} NFStore VNFD = {}",
				vnfds.size(), vnfds.keySet());
		log.debug("alignVNF : {} orchestartor VNFD = {}",
				orchestratorVnfds.size(), orchestratorVnfds.keySet());
		// check descriptors
		for ( VNFDescriptor vnfd : vnfds.values() ) {
			boolean allFilesAvailable = allFilesAreAvailable(vnfd);
			if ( vnfd.isVnfCreated() ) {
				// VNF created into DB
				if ( orchestratorVnfds.containsKey(vnfd.getId()) ) {
					// VNF available on orchestrator
					if ( !allFilesAvailable ) {
						vnf = new OrchestratorVNF(
								vnfd.getId(), OrchestratorOperationTypeEnum.DELETE, null);
						startPeriodicTimer(vnf, INITIAL_ALIGN_POLLING_TIME);
						log.info("alignVNF : start timer to DELETE vnf {}", vnfd.getId() );
					}
				} else {
					// VNF not available on orchestrator
					vnfd.setVnfCreated(false);
					service.updateVNFDescriptor(vnfd);
					vnf = new OrchestratorVNF(
							vnfd.getId(), OrchestratorOperationTypeEnum.CREATE, null);
					startPeriodicTimer(vnf, INITIAL_ALIGN_POLLING_TIME);
					log.info("alignVNF : start timer to CREATE vnf {}", vnfd.getId() );
				}
			} else {
				// VNF non created into DB
				if ( orchestratorVnfds.containsKey(vnfd.getId()) ) {
					// VNF available on orchestrator
					if ( allFilesAvailable ) {
						// all files available
						vnfd.setVnfCreated(true);
						service.updateVNFDescriptor(vnfd);
					} else {
						// NOT all files available
						vnf = new OrchestratorVNF(
								vnfd.getId(), OrchestratorOperationTypeEnum.DELETE, null);
						startPeriodicTimer(vnf, INITIAL_ALIGN_POLLING_TIME);
						log.info("alignVNF : start timer to DELETE vnf {}", vnfd.getId() );
					}
				} else {
					// VNF NOT available on orchestrator
					vnf = new OrchestratorVNF(
							vnfd.getId(), OrchestratorOperationTypeEnum.CREATE, null);
					startPeriodicTimer(vnf, INITIAL_ALIGN_POLLING_TIME);
					log.info("alignVNF : start timer to CREATE vnf {}", vnfd.getId() );
				}
			}
			// remove vnfd from orchestrator map
			orchestratorVnfds.remove(vnfd.getId());
		}
		// remove from orchestrator vnfds not mapped into NFStore
		for ( Integer vnfdId : orchestratorVnfds.keySet() ) {
			log.info("alignVNF : start timer to DELETE vnf {} not found on NFStore", vnfdId );
			startPeriodicTimer(
					new OrchestratorVNF(vnfdId, OrchestratorOperationTypeEnum.DELETE, null), 
					INITIAL_ALIGN_POLLING_TIME);
		}
	}

}
