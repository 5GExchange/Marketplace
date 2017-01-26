
## 5GEx Marketplace + Network Function Store server

### Clone Git Repo

```sh
$ cd ~
$ git clone https://github.com/5GExchange/Marketplace.git 
```


### Build
After you cloned the sources from the git repository there will be two directories: one for the Marketplace and one for the NFS.<br/>
Inside each directory you will find instructions on how to buld and run each part.


### Config files
This is a list of the configuration files that need to be modified according to the deployment environment:
Next step is adding enviroment variables to ease the configuration phase.


#### NFS
```sh
/usr/local/nfs/bin/nfs.conf -  Correct the Orchestrator and NFS URLs
```

#### Marketplace
```sh
marketplace/vnfs/nfsapi.py - Correct the NFS IP address
marketplace/service-selection/bin/application.properties - Correct the Orchestrator/Connector IP
marketplace/service-catalog/bin/application.properties - Correct the Orchestrator/Connector IP
marketplace/mdc/mdcatalogue/settings.py - Correct the NFS IPs
```




