
## Network Function Store server
The Network Function Store is mainly a repository for the VNFs software images and their metadata descriptions.<br/>
The NFS has been written in java as a web application running on TomEE server, a tomcat server with java EE extensions.
The NFS it's installed as a system service (nfs)

### Requirements
The build require JRE 1.8 and ant 1.9. <br/>
The server has to be run on server with Linux OS, rpm and JRE 1.8 installed.

### Tools and Packages used
* [jdk 1.8](http://www.oracle.com/technetwork/java/javase/overview/index.html) - Java virtual machine
```sh
    $ sudo add-apt-repository ppa:webupd8team/java
    $ sudo apt-get update
    $ sudo apt-get install oracle-java8-installer    
    $ sudo apt-get install oracle-java8-set-default
```
* [ant 1.9](http://http://ant.apache.org/) - build of Java applications
```sh
    sudo apt-get install ant
```
* [ivy 2.4.0](http://ant.apache.org/ivy/) - Apache dependency manager
* [redline](http://redline-rpm.org/index.html) - pure Java library for manipulating RPM
* [rpm](http://rpm5.org/) - RPM Package Manager
* [TomEE+](http://tomee.apache.org/apache-tomee.html) - Tomcat server with EE features
* [HTMLWadlGenerator](https://github.com/romiawasthy/HTMLWadlGenerator) - Help generator
* [H2](http://www.h2database.com/html/main.html) - H2 database
* [gson](https://code.google.com/p/google-gson/) - JSON library
* [log4j2](http://logging.apache.org/log4j/2.x/) - log library
* [commons-fileupload](https://commons.apache.org/proper/commons-fileupload/) - file upload library
* [robotframework](http://robotframework.org/) - python tools
* [python](https://www.python.org/) - test automation framework
* [robotframework-requests](https://github.com/bulkan/robotframework-requests) - robotframework Library for HTTP level testing 
* [wiremock](http://wiremock.org/index.html) - library for stubbing and mocking web services 

## Getting started

### Build
After you cloned the sources from the git repository and TomEE is downloaded onto the "server" directory, the build can be done running ant
```sh
    $ mkdir server
Download: https://www.apache.org/dist/tomee/tomee-1.7.4/apache-tomee-1.7.4-plus.zip
and copy the file in the 'server' folder
**NOTE:** Modify build.xml in case of using a different version of tomee
```

```sh
    $ ant -lib lib.no.deploy
```

### Installation
The build produces an rpm into directory prod/rpms containing all the code needed to install the service.<br/>
The NFStore will be installed as a standard linux SysVinit service.

After uploading the rpm on the server install it **as *root* user**

```sh
$ sudo rpm -ivh prod/rpms/nfs-1.0-0.noarch.rpm
```

### Configuration
The default configuration of NFStore is:

NAME | DESCRIPTION | DEFAULT VALUE                      
---- | ----------- | -------------
*NFS_STORE_PATH* | local store directory | /usr/local/store 	                
*NFS_URL* | NFStore URL used for set image links
*ORCHESTRATOR_URL* | orchestrator URL
*TOMCAT_PROTOCOL* | NFStore protocol | https 	                            
*TOMCAT_IP* | NFStore address | 0.0.0.0 (any address)	                        
*TOMCAT_HTTP_PORT* | NFStore port when protocol is http | 80	                                
*TOMCAT_HTTPS_PORT* | NFStore port when protocol is https | 443	                            

If changes are needed, you can modify the values inside file **/usr/local/nfs/bin/nfs.conf** using a text editor before start the server.<br/>
This file contains commented rows with this variables so you should only uncomment what you need and set required values (Orchestrator and NFS IPs)

```sh
#! /bin/bash
##################################################################
# optional variable configuration to override default values
# uncomment and set required values before start nfs service
##################################################################

#------ monitor log level (default value is notice)
# LOG_LEVEL=notice

#------ store path (default value /usr/local/store)
# NFS_STORE_PATH=/usr/local/store

#------ tomcat interface (default values https/0.0.0.0/8080/8443)
TOMCAT_PROTOCOL=http
# TOMCAT_IP=0.0.0.0
TOMCAT_HTTP_PORT=8080
TOMCAT_HTTPS_PORT=8443

#------ orchestrator url
ORCHESTRATOR_URL=http://193.136.92.205:4567/vnfs

#------ NFS url (default value https://apis.t-nova.eu/NFS)
#       url inserted into vnfd for access to image files
NFS_URL=https://83.212.108.105:8443/NFS

#------ gatekeeper NFS service key
#       if not defined incoming requests are not authenticated using gatekeeper
# NFS_SERVICE_KEY=1234567890

#------ gatekeeper url
#       if not defined user authentication using gatekeeper fails
# GATEKEEPER_URL=http://auth.piyush-harsh.info:8000

```

### Run NFS service
```sh
$ service nfs start
Starting nfs (via systemctl):                              [  OK  ]
$
```

The Network Function Store is available at default URL [http://127.0.0.1:8080/NFS](http://127.0.0.1:8080/NFS)

### Stop NFS service
```sh
$ service nfs stop
Stopping nfs (via systemctl):                              [  OK  ]
$
```

### Check NFS service
```sh
$ service nfs status
nfsMonitor active: pid 29890
tomcat active: pid 29924
tomcat manager status: running
nfs app status: running
$ 
```

### restart NFS service
```sh
$ service nfs restart
Restarting nfs (via systemctl):                            [  OK  ]
$ 
```

### NFS API Documentation
The NFS interfaces can be mainly divided in 2 parts:
* [Files Interface](files_interface.md).
* [VNF Descriptor Interface](vnfds_interface.md).

The server is also able to generated directly a basic documentation in html format using URL
[http://127.0.0.1:8080/NFS?_wadl&_type=text/html](http://127.0.0.1:8080/NFS?_wadl&_type=text/html).

Note that these informations are not complete because the used library has some restrictions.

### Tests
Tests are available in `functionalTest` directory of repository and can be run using robotframework tools loading directory `test.suite`.

Robotframework and necessary libraries can be easy installed using `pip` on your system after python installation.

```sh
$ pip install <object to install>
```
Note that some libraries are local like Http requests library that is modified to support attachments on REST requests; local libraries that should be used are available into 'functionsTest/python.lib' directory. 

### Tests configuration
Is possible to change some configuration changing values available on ServerSetting.html file.

