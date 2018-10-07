<<<<<<< HEAD

## 5GEx Marketplace + Network Function Store server

### Clone Git Repo

```sh
$ cd ~
$ git clone git@5gexgit.tmit.bme.hu:melian/T_NOVA_Marketplace.git
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

### License
All Marketplace modules have a different license type contained within the module folder:
/NFS                           - Apache 2.0
/marketplace/aggregator        - Apache 2.0
/marketplace/dashboard         - EUPL Draft V.1.2
/marketplace/aggregator        - Apache 2.0
/marketplace/mdc               - Apache 2.0
/marketplace/service-catalog   - Apache 2.0
/marketplace/service-selection - Apache 2.0
/marketplace/sla-core          - Apache 2.0
/marketplace/umaa              - EUPL Draft V.1.2
/marketplace/vnfs              - EUPL Draft V.1.2


=======
# angular-influxdb
Angular provider to connect to an Influx DB. It supports both, the InfluxDB version 0.9 API as well as older versions (tested with version 0.8). Feel free to contribute.

### Installation:
```
bower install angular-influxdb
```

### Usage:
```
angular
  .module('yourModuleName', ['influxdb'])
  .config(function(influxdbProvider){
    influxdbProvider
      .setUsername('root')
      .setPassword('root')
      .setHost('localhost')
      .setPort('8086')
      .setVersion(0.9);
  })
  .controller('yourCtrlName', ['influxdb', 'iq',
    function(influxdb, iq){
      // direct query on resource
      influxdb.query(query_str, db)
        .$promise.then(function (result) {
        /* do something with result object */
        });
        
      var callback = function(values, columns){
        /* do something with values and column names */
      };
      // raw query
      iq.raw(query_str, db, callback);
      // select queries
      iq.selectAll(measurement, db, callback);
      iq.selectAllSince(measurement, startdate, db, callback);
      var since = '1h'; // returns values from last hour
      iq.selectAllRecent(measurement, since, db, callback);
    }
  ]);

```
>>>>>>> 4c2324be2460dac7bde39bfd937053e6c6a7eeda
