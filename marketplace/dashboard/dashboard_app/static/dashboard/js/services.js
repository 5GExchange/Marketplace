angular.module('dashboard').controller('ServiceListCtrl', ['Restangular', '$scope', '$rootScope', '$state', 'ModalService', 'alertService', ServiceListCtrl]);
angular.module('dashboard').controller('ServiceCreateCtrl', ['Restangular', '$scope', '$rootScope', '$state', 'ModalService', '$interval', 'alertService', ServiceCreateCtrl]);
angular.module('dashboard').controller('ServiceExchangeCtrl', ['Restangular', '$scope', '$rootScope', '$state', 'ModalService', '$interval', 'alertService', ServiceExchangeCtrl]);
angular.module('dashboard').controller('CustomerListCtrl', ['Restangular', '$scope', '$rootScope', '$state', 'ModalService', 'alertService', CustomerListCtrl]);
angular.module('dashboard').controller('CustomerBuyService', ['Restangular', '$scope', '$rootScope', '$state', '$stateParams', 'alertService', CustomerBuyService]);
angular.module('dashboard').controller('CustomerServiceList', ['Restangular', '$scope', '$rootScope', '$state', '$interval', CustomerServiceList]);
angular.module('dashboard').controller('DomainCtrl', ['Restangular', '$scope', '$rootScope', '$state', 'ModalService', '$interval', 'alertService', DomainCtrl]);


function CustomerServiceList(Restangular, $scope, $rootScope, $state, $interval) {

    $scope.services = [];
    $scope.nsds = [];

    $scope.terminateService = function (instance_id) {

        Restangular.one('service-selection/service/selection/',instance_id).one('terminate').get().then(
            function (response) {

                console.log("Service successfully terminated!");

            }, function (response) {

                console.log("Service termination error, status code " + response.status);
                console.log("Service termination error, message: " + response.data);

            });

    };

    $scope.loadNSD = function () {

        Restangular.all('service-selection/service/selection').getList().then(
            function (response) {
                $scope.services = response;
                //$scope.services = [ { "created_at": "2017-04-26T12:55:25.368636", "id": "d5774688-2a71-11e7-aca1-645106b16e7d", "name": "comp-decomp-fwd", "ns-id": "comp-decomp-fwd", "status": "start", "updated_at": "2017-04-26T13:16:45.912582", "vnf_addresses": { "compressor": { "private-1": "192.168.0.1", "public-1": "1.2.3.4" }, "forwarder": { "public-1": "5.6.7.8" } } } ]; 

                Restangular.all('service-catalog/service/catalog').getList().then(
                    function (response2) {
                        $scope.nsds = response2;
                        console.log("GetNSDList " + response2.length + " NSDs found");
                    }, function (response) {
                        console.log("GetNSDList error with status code " + response2.status);
                        console.log("GetNSDList error message: " + response2.data);
                    });


                console.log("GetServiceList " + response.length + " Services found");
            }, function (response) {
                console.log("GetServiceList error with status code " + response.status);
                console.log("GetServiceList error message: " + response.data);
            });

    };

    $scope.loadNSD();

    var service_interval = $interval($scope.loadNSD, 10000);

    // Cancel interval on page changes
    $scope.$on('$destroy', function () {
        if (angular.isDefined(service_interval)) {
            $interval.cancel(service_interval);
            service_interval = undefined;
        }
    });

}

function CustomerBuyService(Restangular, $scope, $rootScope, $state, $stateParams, alertService) {

    $scope.nsd = {};
    $scope.selected_flavor = {};
    $scope.vnfPlacement = []
    $scope.service = {
            customer_id: 2,
            nap_id: '',
            ns_id: '',
            flavor_id: 'sla0', 
	    placement: [], 
	    ports: [], 
	    params: []
        };
    $scope.locations = ["Automatic"];
    $scope.components_list = [];

    $scope.addInstantiationParam = function (){
        $scope.service.params.push({});
    };

    $scope.removeInstantiationParam = function (param){
        $scope.service.params.splice($scope.service.params.indexOf(param), 1);
    };



    $scope.loadNSD = function () {

        Restangular.one("service-catalog/service/catalog", $stateParams.nsdID).get().then(
            function (response) {
                $scope.nsd = response;
				//push the external ports
				angular.forEach($scope.nsd.nsd.vld.virtual_links, function (vl, key) {
					$scope.service.ports.push({port: vl.alias, ip:""})
				});
                console.log("GetNSD " + response.name + " has been successfully loaded");
            }, function (response) {
                console.log("GetNSD error with status code " + response.status);
                console.log("GetNSD error message: " + response.data.detail);
            });
    };


    $scope.loadNSComponents= function (flavor) {
		console.log("Flavor: ", flavor);
		angular.forEach($scope.nsd.nsd.sla, function (fla, fla_key) {
            if (fla.id == flavor) {
				//retrieve the VNF from the selected flavor
				angular.forEach(fla.constituent_vnf, function (comp, comp_key) {
		    		for (i = 0; i < comp.number_of_instances; i++) 
		        		$scope.components_list.push(comp.vnf_reference+"-"+i)
				});
				return;
	    	}
		});
    };


    $scope.loadLocations = function () {
        //$scope.locations = ["Automatic", "INTERNET:152.66.244.0/24", "INTERNET:172.80.244.0/24"];
        Restangular.all('tnovaconnector/placement-info').getList().then(
            function (response) {
                $scope.locations= response;
                console.log("GetLocationsList " + response.length + " locations found");
            }, function (response) {
                console.log("GetLocationsList error with status code " + response.status);
                console.log("GetLocationsList error message: " + response.data);
            });

    };



    $scope.loadNSD();
    $scope.loadLocations();



    $scope.buy=function(nsd_id){

        var selected_flavor = '';
        angular.forEach($scope.selected_flavor, function (isflavorselected, flavor_id) {
            if (isflavorselected){
                selected_flavor=flavor_id;
            }

        });
        angular.forEach($scope.components_list, function (comp, comp_id) {
            $scope.service.placement.push({vnf: comp, subnet: $scope.vnfPlacement[comp]});
	    console.log($scope.vnfPlacement[comp]);
	});

	$scope.service.ns_id=nsd_id;

        console.log($scope.service);

        $rootScope.root_loading = true;
        Restangular.all('service-selection/service/selection').post($scope.service).then(
            function (response) {
                console.log("CreateService " + response.id + " has been successfully completed");
                console.log(response);

                $rootScope.root_loading = false;

                $state.go('index.customer-services');
            }, function (response) {
                console.log("CreateService error with status code " + response.status);
                console.log("CreateService error message: " + response.data);
                alertService.add('danger', 'Failed to instantiate NSD');

                $rootScope.root_loading = false;
            });
    };
    //
    //$scope.loadUser = function () {
    //    $scope.loading_edit_user = true;
    //    Restangular.one("user-management/users", $stateParams.userID).get().then(
    //        function (response) {
    //            $scope.user = response;
    //            $scope.loading_edit_user = false;
    //            console.log("GetUser " + response.username + " has been successfully loaded");
    //        }, function (response) {
    //            console.log("GetUser error with status code " + response.status);
    //            console.log("GetUser error message: " + response.data.detail);
    //            $scope.loading_edit_user = false;
    //        });
    //};


}



function ServiceListCtrl(Restangular, $scope, $rootScope, $state, ModalService, alertService) {

    $scope.services = {};

    $scope.loadNSDs = function () {
        //Restangular.all('service-catalog/service/catalog').getList().then(
        Restangular.all('mdc/mdc').getList().then(
            function (response) {
                if (response==''){
                    $scope.services = [];
                }else{
                    $scope.services = response;
                }
                console.log("GetNSDList " + response.length + " NSDs found");
            }, function (response) {
                console.log("GetNSDList error with status code " + response.status);
                console.log("GetNSDList error message: " + response.data.detail);
            });
    };

    $scope.loadNSDs();

    $scope.deleteNSD_MdC = function (id, nsd_name) {
        $rootScope.root_loading=true;
        Restangular.one("mdc/mdc/", id).remove().then(
            function () {
                console.log("NSD " + nsd_name + " has been successfully deleted");
                alertService.add('success', 'NSD '+nsd_name+' has been successfully deleted from the MdC.');
                $rootScope.root_loading=false;
                $scope.loadNSDs();
            }, function (response) {
                console.log("DeleteNSD error with status code " + response.status);
                console.log("DeleteNSD error message: " + response.data);

                alertService.add('danger', 'Failed to delete NSD.');
                $rootScope.root_loading=false;
            });
    };

    $scope.deleteNSD_BSC= function (id, nsd_name) {
        $rootScope.root_loading=true;
        Restangular.one("service-catalog/service/catalog", id).remove().then(
            function () {
                console.log("NSD " + nsd_name + " has been successfully deleted from the BSC");
                $rootScope.root_loading=false;
            }, function (response) {
                console.log("DeleteNSD BSC error with status code " + response.status);
                console.log("DeleteNSD BSC error message: " + response.data);
                $rootScope.root_loading=false;
            });
    };

    $scope.NSDDeleteConfirm = function (id, nsd_name) {
        var productId;
        var itemDomain;
        $rootScope.root_loading=true;
        Restangular.one("mdc/mdc/", id).get().then(
            function (response) {
                productId = response.productId;
                domain = response.domainId;
                console.log("MdC Delete " + nsd_name + ": MdC ID: " + id + "; ProductId: " + productId);

                //delete the NSD from the MdC
                $scope.deleteNSD_MdC(id, nsd_name);
                //If it's the local domain, delete also from the BSC
                Restangular.one("mdc/domain/", domain).get().then(
                    function (response) {
                        if (response.localDomain)
                            $scope.deleteNSD_BSC(productId, nsd_name);
                        else
                            console.log("Delete NSD: External service. Deleted only from the local MdC");
                        $rootScope.root_loading=false;
                    }, function (response) {
                            console.log("Fallo gordo: dominio: ", domain);
                            $rootScope.root_loading=false;
                });
            }, function (response) {
                console.log("DeleteNSD error with status code " + response.status);
                console.log("DeleteNSD error message: " + response.data);

                alertService.add('danger', 'Failed to delete NSD.');
                $rootScope.root_loading=false;
            });
    };



	$scope.domainsList = [];
    
	$scope.getDomains = function () {
		var domains = [];
        $rootScope.root_loading=true;
        Restangular.all('mdc/domain/neighbours').getList().then(
            function (response) {
                //$scope.availableDomains = response;
                console.log("GetDomainList " + response.length + " Domains found");
                domains=response;
                //$scope.domainList.push(;
				$scope.domainsList = domains;
                $rootScope.root_loading=false;
            }, function (response) {
                console.log("GetDomainList error with status code " + response.status);
                console.log("GetDomainList error message: " + response.data);
                $rootScope.root_loading=false;
            });

    };

	$scope.updateDomains = function (item) {
		var domains = $scope.domainsList;
		angular.forEach(domains, function (domain, id) {
			var present = false;
			angular.forEach(item.sharedWith, function (shared, id2) {
				if (domain.domain == shared.domainId)
					present = true;
			});
			if (!present)
				item.sharedWith.push( { "domainId": domain.domain, "share": false } )
		});
		return item;
	};

	$scope.saveCatalogue = function (item) {
        $rootScope.root_loading=true;
		Restangular.all('mdc/mdc/' + item.id +"/").customPUT(item).then(
            function (response) {
                //$scope.availableDomains = response;
                console.log("SaveCatalogue: " + response.status);
                alertService.add('success', 'NS '+item.id+'#'+item.name+' has been successfully modified');
                $rootScope.root_loading=false;
            }, function (response) {
                console.log("SaveCatalogue: ERROR - " + response.status);
                console.log("SaveCatalogue: ERROR - " + response.data);
                alertService.add('danger', 'A error occurred while saving NS '+item.id+'#'+item.name);
                $rootScope.root_loading=false;
            });

    };


    $scope.editNSModal = function (item, backup_item) {

		$scope.getDomains();
        ModalService.showModal({
            templateUrl: "/static/dashboard/templates/modals/edit_catalogue_ns.html",
            controller: function () {
                this.item = $scope.updateDomains(item);
                this.backup_item = backup_item;
               	//this.domains = $scope.domainsList;
				this.closeModal = function () {
					console.log("Closing edition");
					this.item=item;
         			//$scope.loadMdCServices();
                    close(); // close, but give 500ms for bootstrap to animate
                };
				this.saveItem = function (it) {
					console.log("item to save: ", JSON.stringify(it));
					$scope.saveCatalogue(it);
         			//$scope.loadMdCServices();
                    close(); // close, but give 500ms for bootstrap to animate
                };
            },
            controllerAs: "EditCatalogueCtrl"
        }).then(function (modal) {
            // only called on success...
            modal.element.modal();
            modal.item = item;
            modal.backup_item = backup_item;
            //modal.vnfd_id = vnfd_id
        }).catch(function (error) {
            // error contains a detailed error message.
            console.log(error);
        });

    };

    $scope.showNSDEditor = function (nsd_id) {

        ModalService.showModal({
            animation: false,
            templateUrl: "/static/dashboard/templates/modals/nsd-yaml-editor.html",
            controller: "YAMLEditController",
            inputs: {
                nsd_id: nsd_id
            }
        }).then(function (modal) {
            // only called on success...
            modal.element.modal();
        }).catch(function (error) {
            // error contains a detailed error message.
            console.log(error);
        });

  };


}


function CustomerListCtrl(Restangular, $scope, $rootScope, $state, ModalService, alertService) {

    $scope.services = {};

    $scope.loadNSDs = function () {
        Restangular.all('service-catalog/service/catalog').getList().then(
            function (response) {
                if (response==''){
                    $scope.services = [];
                }else{
                    $scope.services = response;
                }
                console.log("GetNSDList " + response.length + " NSDs found");
            }, function (response) {
                console.log("GetNSDList error with status code " + response.status);
                console.log("GetNSDList error message: " + response.data.detail);
            });
    };

    $scope.loadNSDs();


	$scope.domainsList = [];
    
	$scope.getDomains = function () {
		var domains = [];
        $rootScope.root_loading=true;
        Restangular.all('mdc/domain/neighbours').getList().then(
            function (response) {
                //$scope.availableDomains = response;
                console.log("GetDomainList " + response.length + " Domains found");
                domains=response;
                //$scope.domainList.push(;
				$scope.domainsList = domains;
                $rootScope.root_loading=false;
            }, function (response) {
                console.log("GetDomainList error with status code " + response.status);
                console.log("GetDomainList error message: " + response.data);
                $rootScope.root_loading=false;
            });

    };


}



angular.module('dashboard').controller('YAMLEditController', ['$scope', '$element', '$http', 'close', 'nsd_id', function ($scope, $element, $http, close, nsd_id) {

        $scope.code = '';
        $http({
            method: 'GET',
            url: '/nsds/' + nsd_id + '/yaml'
        }).then(function successCallback(response) {
            //alert('ggg');
            //console.log(response.data);
            $scope.code = response.data;
            // this callback will be called asynchronously
            // when the response is available
        }, function errorCallback(response) {
            // called asynchronously if an error occurs
            // or server returns response with an error status.
        });

    }]);

function ServiceCreateCtrl(Restangular, $scope, $rootScope, $state, ModalService, $interval, alertService) {

    $scope.dynamicPopover = {
        templateUrl: 'myPopoverTemplate.html'
    };

    $scope.generic_monitoring_parameters = [
        {metric: "mem_used", desc: "Memory consumed", unit: 'Bytes'},
        {metric: "mem_percent", desc: "Memory consumed", unit: '%'},
        {metric: "cpu_percent", desc: "CPU", unit: '%'},
        {metric: "tx_bytes", desc: "Bytes transmitted", unit: 'Bytes'},
        {metric: "rx_bytes", desc: "Bytes received", unit: 'Bytes'}
      //{metric: "availability", desc: "Availability", unit: '%'},
      //{metric: "cpu_usage", desc: "CPU Usage", unit: '%'},
      //{metric: "memory_usage", desc: "Memory Usage", unit: '%'},
      //{metric: "bps_in", desc: "Incoming Bandwidth", unit: 'Mbps'},
      //{metric: "bps_out", desc: "Outgoing Bandwidth", unit: 'Mbps'}
    ];

    $scope.sla_expressions = [
        {code: "EQ", desc: "Equal To", sign: "=="},
        {code: "NE", desc: "Not Equal To", sign: "!="},
        {code: "LT", desc: "Less Than", sign: "<"},
        {code: "LE", desc: "Less Than or Equal", sign: "<="},
        {code: "GT", desc: "Greater Than", sign: ">"},
        {code: "GE", desc: "Greater Than or Equal", sign: ">="}
    ];

    $scope.available_periods = [
        {code: "D", desc: "Day"},
        {code: "W", desc: "Week"},
        {code: "M", desc: "Month"},
        {code: "Y", desc: "Year"}
    ];

    $scope.billing_model_types = [
        {code: "PAYG", desc: "Pay-As-You-Go"}
    ];

    $scope.billing_currencies = [
        {code: "EUR", desc: "Euro"},
        {code: "USD", desc: "US Dollars"}
    ];

    $scope.penalty_types = [
        {type: "Discount"},
        {type: "Scale In"},
        {type: "Scale Out"}
    ];

    $scope.redundancy_models = [
        {type: "Active"},
        {type: "Standby"}
    ];

    $scope.sla_expressions2 = [
        {code: "MIN", desc: "Minimum"},
        {code: "MAX", desc: "Maximum"},
        {code: "AVG", desc: "Average"},
        {code: "SUM", desc: "Sum"}
    ];

    $scope.connection_link_types = [
        {type:'E-LINE', description:'Point-2-Point (E-LINE)'},
        {type:'E-TREE', description:'Point-2-Multipoint (E-TREE)'},
        {type:'E-LAN', description:'Lan (E-LAN)'},
        {type:'INTERNET', description:'INTERNET'}
    ];

    $scope.available_bandwidths = [
        "10Mbps",
        "100Mbps",
        "1Gbps"
    ];

    $scope.vnf_events = [
        {type:'start', description:'Start'},
        {type:'stop', description:'Stop'},
        {type:'scale_in', description:'Scale In'},
        {type:'scale_out', description:'Scale Out'}
    ];

    $scope.generic_monitoring_parameters = [
        {metric: "mem_used", desc: "Memory consumed", unit: 'Bytes'},
        {metric: "mem_percent", desc: "Memory consumed", unit: '%'},
        {metric: "cpu_percent", desc: "CPU", unit: '%'},
        {metric: "tx_bytes", desc: "Bytes transmitted", unit: 'Bytes'},
        {metric: "rx_bytes", desc: "Bytes received", unit: 'Bytes'}
        //{metric: "availability", desc: "Availability", unit: '%'},
        //{metric: "end-to-end bandwidth", desc: "End-to-End Bandwidth", unit: 'Mbps'}
    ];

    $scope.monitoring_parameters_selected = [
        {metric: "mem_used", desc: "Memory consumed", unit: 'Bytes'},
        {metric: "mem_percent", desc: "Memory consumed", unit: '%'},
        {metric: "cpu_percent", desc: "CPU", unit: '%'},
        {metric: "tx_bytes", desc: "Bytes transmitted", unit: 'Bytes'},
        {metric: "rx_bytes", desc: "Bytes received", unit: 'Bytes'}
        //{metric: "availability", desc: "Availability", unit: '%'},
        //{metric: "end-to-end bandwidth", desc: "End-to-End Bandwidth", unit: 'Mbps'}
    ];

    $scope.loading_catalogue_services = false;
    $scope.vnfs = {};


    $scope.loadMdCServices = function () {
        $scope.loading_catalogue_services = true;
        Restangular.all('mdc/mdc').getList().then(
            function (response) {
                $scope.vnfs = response;
                //$scope.vnfs = [];
                console.log("MDC::GetItemsList " + response.length + " items found");
                    $scope.loading_catalogue_services = false;
                    $scope.updateFilteredVNFs('ALL');

            }, function (response) {
                console.log("MDC::GetVNFList error with status code " + response.status);
                console.log("MDC::GetVNFList error message: " + response.data.detail);
                $scope.loading_catalogue_services = false;
            });
    };


    $scope.getDomainsList= function () {
        Restangular.all('mdc/domain').getList().then(
            function (response) {
                //$scope.availableDomains = response;
                console.log("GetDomainList " + response.length + " Domains found");
                $scope.domainList=response;
                                //$scope.domainList.push(;
                                $scope.domainList.splice(0, 0, {"domain": "ALL"});
            }, function (response) {
                console.log("GetDomainList error with status code " + response.status);
                console.log("GetDomainList error message: " + response.data);
            });

    };


    $scope.init = function () {
         $scope.loadMdCServices();
         $scope.getDomainsList();
	 //download descriptors for each of the NS/VNF in the catalogue
	
    };

    $scope.init();

    $scope.nsd = {
        vnfds: [],
        vld: {},
        vnffgd: {
            vnffgs: []
        },
        vnf_dependency: [],
        auto_scale_policy: {},
        lifecycle_events: {},
        monitoring_parameters: []
    };

    $scope.flavors = [{
        constituent_vnf:[],
        assurance_parameters:[],
        billing_model:{},
        virtual_links:[],
        lifecycle_events:{
            start:[],
            stop:[],
            scale_in:[],
            scale_out:[]
        }
    }];

    $scope.addFlavor = function () {
        $scope.flavors.push({
            constituent_vnf:[],
            assurance_parameters:[],
            billing_model:{},
            virtual_links:[],
            lifecycle_events:{
                start:[],
                stop:[],
                scale_in:[],
                scale_out:[]
            }
        });
    };

    $scope.removeFlavor = function (flavor) {
        $scope.flavors.splice($scope.flavors.indexOf(flavor), 1);
    };

    $scope.getIndexFlavor = function (flavor) {
        return $scope.flavors.indexOf(flavor);
    };

    $scope.addConstituentVNF = function (flavor){
        flavor.constituent_vnf.push({

        });
    };

    $scope.removeConstituentVNF = function (flavor, vnf){
        flavor.constituent_vnf.splice(flavor.constituent_vnf.indexOf(vnf), 1);
    };

    $scope.addAssuranceParameter = function (flavor) {
        flavor.assurance_parameters.push({
            constituent_vnfs:[{}],
            violation:[
                {breaches_count:2,interval:360}
            ],
            //penalty:{type:{type: "Discount"}}
            penalty:{type: "Discount"}

        });
    };

    $scope.removeAssuranceParameter = function (flavor, aparam) {
        flavor.assurance_parameters.splice(flavor.assurance_parameters.indexOf(aparam), 1);
    };


    $scope.getIndexAssuranceParameter = function (flavor, aparam) {
        return flavor.assurance_parameters.indexOf(aparam)
    };

    $scope.addConstituentVNFAssuranceParam = function (constituent_vnfs) {
        constituent_vnfs.push({

        });
    };

    $scope.removeConstituentVNFAssuranceParam = function (constituent_vnfs, vnf) {
        constituent_vnfs.splice(constituent_vnfs.indexOf(vnf), 1);
    };

    $scope.addAssuranceParamViolation = function (violation) {
        violation.push({});
    };

    $scope.removeAssuranceParamViolation = function (violation_list, violation) {
        violation_list.splice(violation_list.indexOf(violation), 1);
    };

    $scope.addVL = function (flavor) {
        flavor.virtual_links.push({
            connection_points_reference:[],
            vdu_reference:[],
            connection_points:[],
            bandwidth: "",
            type: {type:'E-LINE', description:'Point-2-Point (E-LINE)'}
        });
    };

    $scope.removeVL = function (flavor, vl) {
        flavor.virtual_links.splice(flavor.virtual_links.indexOf(vl), 1);
    };

    $scope.getIndexVL = function (flavor, vl) {
        return flavor.virtual_links.indexOf(vl);
    };

    $scope.addConnectionPoint = function (vl) {
        vl.connection_points.push({});
    };

    $scope.removeConnectionPoint = function (vl, cp) {
        var cp_index = vl.connection_points.indexOf(cp);
        vl.connection_points.splice(cp_index, 1);
    };

    $scope.addLifeCycleEvent = function (flavor, event) {
        flavor.lifecycle_events[event].push({
            vnf_id:null,
            vnf_event:null
        });
    };

    $scope.removeLifeCycleEvent = function (flavor, event, le) {
        flavor.lifecycle_events[event].splice(flavor.lifecycle_events[event].indexOf(le), 1);
    };

    $scope.getVNFexternalPoints = function(flavor){
        var points=[];
        angular.forEach(flavor.constituent_vnf, function (vnf, vnf_key) {
            for (var i=0; i<vnf.number_of_instances; i++) {
                var descriptor = $scope.getVNFbyRef(vnf.vnf_reference);
                if (descriptor.productType == "ns") {
                    angular.forEach(descriptor.vld.virtual_links, function (link, link_key) {
                         if (link.external_access){
                            points.push('domain#'+descriptor.domain+':'+descriptor.productType+'#'+descriptor.id+"-"+i+":ext_"+link.alias);
                        }
                    });
                }
                else {
                    angular.forEach(descriptor.deployment_flavours, function (flavor, flavor_key) {
                        if (flavor.flavour_key == vnf.vnf_flavour_id_reference) {
                            var links_ref = flavor.vlink_reference;
                             angular.forEach(descriptor.vlinks, function (link, link_key) {
                                if (_.contains( links_ref, link.id ) && link.external_access){
                                    points.push('domain#'+descriptor.domain+':'+descriptor.productType+'#'+descriptor.id+"-"+i+":ext_"+link.alias);
                                }
                             });
                        }
                    });
                } //if productType
            } //for
        });
        return points;
    };

    $scope.executed = false;
    $scope.AllVNFs = [];

    $scope.getAllVNFs = function (flavor) {
        var vnfs=[];
	if ($scope.executed == true)
	    return $scope.allVNFs;
        angular.forEach(flavor.constituent_vnf, function (vnf, vnf_key) {
            for (var i=0; i<vnf.number_of_instances; i++) {
                var descriptor = $scope.getVNFbyRef(vnf.vnf_reference);
                vnfs.push({"id": descriptor.id + "-" + i + "@" + descriptor.domain, "productType": descriptor.productType});
            };
        });
        $scope.executed = true;
	$scope.allVNFs = vnfs;
        return vnfs;
    };

    $scope.getConstituentVNFs= function(flavor){
        var vnfs=[];
        angular.forEach(flavor.constituent_vnf, function (vnf, vnf_key) {
            var vnfd = $scope.getVNFbyRef(vnf.vnf_reference);
            vnfs.push(vnfd);
        });
        return vnfs;
    };

    $scope.getMonitoringParamaters = function (flavor) {
        var mons = [];
	if ($scope.active_step > 5) {
            angular.forEach(flavor.constituent_vnf, function (vnf, vnf_key) {
                var descriptor = $scope.getVNFbyRef(vnf.vnf_reference);
		if (descriptor.productType == "ns") {
                    //for assurance parameters
                    angular.forEach(descriptor.sla, function (sla, sla_key) {
                        if (sla.sla_key == vnf.vnf_flavour_id_reference)
                            angular.forEach(sla.assurance_parameters, function (mon, mon_key) {
                                if (!_.contains(mons, mon)) {
                                    //mons.push({"desc": mon.name, "metric": mon.id, "unit": mon.unit});
                                    mon.desc = mon.name;
				    mon.metric = mon.id;
                                    mons.push(mon);
                                }
                            });
			    
		    });
		
		}
		else {	   
                    //for all vdus
                    angular.forEach(descriptor.vdu, function (vdu, vdu_key) {

                        //generic
                        angular.forEach(vdu.monitoring_parameters, function (mon, mon_key) {
                            if (!_.contains(mons, mon)) {
                                mons.push(mon);
                            }
                        });
                        //specific
                        angular.forEach(vdu.monitoring_parameters_specific, function (mon, mon_key) {
    
                            if (!_.contains(mons, mon)) {
                                mons.push(mon);
                            }
                        });

                    });
		};
            });
	};
        return mons;
    };

    $scope.selected_vnfs = {};
    $scope.selected_vnfs_trade = {};

    $scope.filtered_vnfs = $scope.vnfs;
    $scope.selected_vnf_filter_type = 'ALL';
    $scope.selected_price_range={min:0, max:300};


    $scope.updateFilteredVNFs = function(selected_vnf_filter_type){
        console.log("GetDomainList " + JSON.stringify($scope.domainList));
        // reset selected vnfds trade
        angular.forEach($scope.selected_vnfs_trade, function (vnfd_selected, vnfd_id) {
                $scope.selected_vnfs_trade[vnfd_id] = false;
        });

        if (selected_vnf_filter_type) {
            $scope.selected_vnf_filter_type = selected_vnf_filter_type;
        }

        var filtered_vnfs = [];

        angular.forEach($scope.vnfs, function (vnfd, vnfd_key) {

            if ($scope.selected_vnf_filter_type == vnfd.domainId || $scope.selected_vnf_filter_type == 'ALL') {

                //if (vnfd.billing_model.price.max_per_period >= $scope.selected_price_range.min && vnfd.billing_model.price.max_per_period <= $scope.selected_price_range.max) {
                if ($scope.selected_price_range.min <= vnfd.price_per_period && vnfd.price_per_period <= $scope.selected_price_range.max) {
                    filtered_vnfs.push(vnfd);
                }
            }
        });


        $scope.filtered_vnfs = filtered_vnfs;

        //check if a $digest is already in progress by checking $scope.$$phase.
        if (!$scope.$$phase) {
            $scope.$apply(); //this triggers a $digest
        }

    };

    $scope.updateFilteredVNFsPrice = function () {
        console.log($scope.selected_price_range);
    };

    $scope.vnf_filter_types = [
        {code: "ALL", desc: "All Types"},
        {code:"vTC", desc: "Traffic Classification"},
        {code:"vSBC", desc: "Session Border Controller"},
        {code:"vTU", desc: "Transcoder Unit"},
        {code:"vHG", desc: "Home Gateway"},
        {code:"vSA", desc: "Security Appliance"},
        {code:"vPXAAS", desc: "Proxy"}
    ];

    $scope.getDeploymentCost = function (vnf, flavor_name) {
        var vdu_cost = 0.03425;
        var cpu_cost = 0.034;
        var ram_gb_cost = 0.02125;
        var storage_gb_cost = 0.0003;

        var number_of_vdus = 0;
        var number_of_cores = 0;
        var number_of_ram_gb = 0;
        var number_of_storage_gb = 0;

        var vdu_reference = [];
        angular.forEach(vnf.deployment_flavours, function (flavor, flavor_key) {
            if (flavor.flavour_key==flavor_name){
                vdu_reference = flavor.vdu_reference;
            }
        });

        angular.forEach(vnf.vdu, function (vdu, vdu_key) {
            if (_.contains( vdu_reference, vdu.id )){
                number_of_vdus += vdu.resource_requirements.vcpus || 0;
                number_of_ram_gb += vdu.resource_requirements.memory || 0;
                number_of_storage_gb += vdu.resource_requirements.storage.size || 0;
                number_of_vdus += 1;
            }

        });

        var total = (number_of_vdus * vdu_cost) + (number_of_cores * cpu_cost) + (number_of_ram_gb * ram_gb_cost) + (number_of_storage_gb * storage_gb_cost);
        return (total.toFixed(2));
    };


    $scope.postNewNSD = function (json_nsd) {

        $rootScope.root_loading = true;
        Restangular.all('mdc/domain/').getList().then(
            function (response) {
        	angular.forEach(response, function (domain, id) {
                	if (domain.localDomain){
                		console.log("Vendor domain: ", domain.domain);
        				$scope.nsd.vendor=domain.domain;
        				json_nsd["nsd"].vendor=domain.domain;
					}
			});
        	Restangular.all('service-catalog/service/catalog').post(json_nsd).then(
            	function (response) {
                	console.log("CreateNSD " + response.name + " has been successfully completed");
                	console.log(response);
                	$rootScope.root_loading = false;

                	$state.go('index.services.list');

                	alertService.add('success', 'NSD successfully created!')
            	}, function (response) {
                	$rootScope.root_loading = false;
                	console.log("CreateNSD error with status code " + response.status);
                	console.log("CreateNSD error message: " + response.data);

                	alertService.add('danger', 'Failed to create NSD, '+ response.plain())
            	}
			);
            }, function (response) {
            	console.log("MDC:: Cannot find local domain: ", domain);
        	}
		);
    };

    $scope.createNSD = function(){

        $scope.nsd.vnfds = [];
        angular.forEach($scope.getSelectedVNFs, function (vnf, vnfd_id) {
            //$scope.nsd.vnfds.push("domain#"+$scope.selectedItems[vnfd_id].domainId+":"+$scope.selectedItems[vnfd_id].productType+"#"+vnf.id);
            $scope.nsd.vnfds.push("domain#"+vnf.domain+":"+vnf.productType+"#"+vnf.id);
        });

        $scope.nsd.sla = [];
        $scope.nsd.vld = {
            virtual_links: []
        };
        $scope.nsd.lifecycle_events = {
            start: [],
            stop:[],
            scale_in:[],
            scale_out:[]
        };
        $scope.nsd.monitoring_parameters = $scope.monitoring_parameters_selected;

        $scope.nsd.vendor="3";
        $scope.nsd.provider="5GEx";
        $scope.nsd.provider_id="3";

     
	

        var flavor_index=0;
        var vl_link_index = 0;
        var le_index = 0;
        var vnffg_index = 0;

        angular.forEach($scope.flavors, function (flavor, flavor_key) {
            var le_rel_index = 0;

            // need this var to keep the virtual links per flavor for the vnffg
            var vnffg_flavor_vlinks = [];

            angular.forEach(flavor.virtual_links, function (virtual_link, virtual_link_key) {

                var connections = [];

                angular.forEach(virtual_link.connection_points, function (cp, cp_key) {
                    connections.push(cp.vdu_ref);
                });

                if (virtual_link.delay == undefined){
                    virtual_link.delay = '';
                }
                if (virtual_link.flowclass == undefined){
                    virtual_link.flowclass = '';
                }
                if (virtual_link.qos_params == undefined){
                    virtual_link.qos_params = '';
                }

                var vl = {
                    vld_id: 'vld'+ vl_link_index,
                    alias:virtual_link.alias,
                    sla_ref_id:'sla'+flavor_index,
                    root_requirements: virtual_link.bandwidth,
                    leaf_requirement: virtual_link.bandwidth,
                    //qos:{params: virtual_link.qos_params, peak: '', burst: ''},
                    // qos: { average: virtual_link.delay,
		    // 	   peak: virtual_link.flowclass,
		    // 	   burst: ''},
                    qos: { params: virtual_link.qos_params, 
                           delay: virtual_link.delay,
			   flowclass: virtual_link.flowclass,
			   peak: '',
			   burst: ''},
                    connections: connections,
                    connectivity_type: virtual_link.type.type,
                    net_segment: virtual_link.net_segment,
                    external_access: virtual_link.external_access,
                    merge: virtual_link.merge
                };

                $scope.nsd.vld.virtual_links.push(vl);
                vnffg_flavor_vlinks.push(vl);

                vl_link_index++;

            });


            // VNFFG GENERATION
            var number_of_endpoints = 0;
            var number_of_virtual_links = 0;
            var dependent_virtual_links = [];

            var connection_points = [];

            angular.forEach(vnffg_flavor_vlinks, function (virtual_link, virtual_link_key) {

                //ignore storage and management network
                if (virtual_link.alias != 'management' && virtual_link.alias != 'storage') {


                    if (virtual_link.external_access) {
                        number_of_endpoints++;

                        connection_points.push('ns_ext_'+virtual_link.alias)
                    }

                    number_of_virtual_links++;
                    dependent_virtual_links.push(virtual_link.vld_id);

                    angular.forEach(virtual_link.connections, function (conn, conn_key) {
			console.log(conn);

			//conn: vnf#303:ext_1
			var link = conn.split(":"); //link[0]='vnf#303' - link[1]='ext_1'
			var desc_id = link[0].split("#"); //desc_id[0]='vnf' - desc_id[1]='303'
			var descriptorType = desc_id[0]; 
			var ns_id = desc_id[1];
        		//angular.forEach($scope.selectedItems, function (item, item_id) {
            		//    angular.forEach($scope.getSelectedVNFs, function (vnfd, vnfd_id) {
			//	if ((item.productId == ns_id) && (item.productType == descriptorType))
                       // 	    connection_points.push("domain#"+item.domainId+":"+conn)
                        	    connection_points.push(conn)
	    		//    });
        		//});
			

                    });

                }

            });

            var con_vnfs = [];
            angular.forEach(flavor.constituent_vnf, function (conv, conv_key) {
                for (i = 0; i < conv.number_of_instances; i++) 
                    con_vnfs.push({vnf_ref_id:conv.vnf_reference+"-"+i, vnf_flavor_key_ref:conv.vnf_flavour_id_reference});
            });

            $scope.nsd.vnffgd.vnffgs.push(
                {
                    vnffg_id: 'vnffg' + vnffg_index,
                    number_of_endpoints: number_of_endpoints,
                    number_of_virtual_links: number_of_virtual_links,
                    dependent_virtual_links: dependent_virtual_links,
                    network_forwarding_path:[
                        {
                            nfp_id:'nfp0',
                            graph: dependent_virtual_links,
                            connection_points:connection_points,
                            constituent_vnfs:con_vnfs
                        }
                    ]
                }
            );

            vnffg_index++;

            angular.forEach(flavor.lifecycle_events.start, function (le, le_key) {

                $scope.nsd.lifecycle_events.start.push({
                    event_id: 'le'+le_index,
                    event_rel_id: 'rel_le'+le_rel_index,
                    sla_ref_id:'sla'+flavor_index,
                    vnf_id:le.vnf_id,
                    vnf_event:le.vnf_event
                });
                le_index++;
                le_rel_index++;
            });

	    if (flavor.flavor_key === undefined)
		flavor.flavor_key = 'sla'+flavor_index;
            var nsd_flavor = {
                id:'sla'+flavor_index,
                sla_key:flavor.flavor_key,
                constituent_vnf: flavor.constituent_vnf,
                assurance_parameters:[],
                billing:flavor.billing_model
            };

            $scope.nsd.auto_scale_policy[flavor.flavor_key]=[];

            var x=0;
            angular.forEach(flavor.assurance_parameters, function (assurance_parameter, assurance_parameter_key) {

                        var formula_in = '';

                        angular.forEach(assurance_parameter.constituent_vnfs, function (vnf, vnf_key) {
                            formula_in+=vnf.vnf.productType+':'+ vnf.vnf.id +'*'+ assurance_parameter.monitoring_parameter.metric+',';
                        });

                        var formula = assurance_parameter.expression2.code + '('+formula_in.replace(/(^[,\s]+)|([,\s]+$)/g, '')+')';

                        var aparam = {
                            uid: "ap"+x++,
                            id:assurance_parameter.monitoring_parameter.metric,
                            name:assurance_parameter.monitoring_parameter.metric,
                            value: assurance_parameter.expression.code+'('+assurance_parameter.value+')',
                            unit: assurance_parameter.monitoring_parameter.unit,
                            //formula: assurance_parameter.expression2.code+'('+')',
                            formula: formula,
                            violations:assurance_parameter.violation
                            //penalty:assurance_param.penalty,

                        };



                if (assurance_parameter.penalty.type == "Discount") {

                        aparam.penalty = {};
                        aparam.penalty.type = assurance_parameter.penalty.type;
                        aparam.penalty.value = assurance_parameter.penalty.value;
                        aparam.penalty.unit = assurance_parameter.monitoring_parameter.unit;
                        aparam.penalty.validity = 'P'+ assurance_parameter.penalty.validity.value + assurance_parameter.penalty.validity.period;

                        //nsd_flavor.billing = flavor.billing_model;
                        nsd_flavor.assurance_parameters.push(aparam);

                } else {

                    aparam.penalty = {};
                    aparam.penalty.type = assurance_parameter.penalty.type;
                    aparam.penalty.value = assurance_parameter.penalty.value;
                    aparam.penalty.unit = assurance_parameter.monitoring_parameter.unit;
                    aparam.penalty.validity = 'P' + assurance_parameter.penalty.validity.value + assurance_parameter.penalty.validity.period;

                    //nsd_flavor.billing = flavor.billing_model;
                    nsd_flavor.assurance_parameters.push(aparam);

                    $scope.nsd.auto_scale_policy[flavor.flavor_key].push({
                        criteria: [{"assurance_parameter_id": aparam.uid}],
                        actions: [{"type": assurance_parameter.penalty.type}]
                    });

                }

            });

            $scope.nsd.sla.push(nsd_flavor);

            flavor_index++;
        });



        //$scope.nsd
        $scope.postNewNSD({nsd:$scope.nsd});
        //$state.go('index.services.list');
        console.log({nsd:$scope.nsd});

    };

    $scope.getSelectedDescriptors = function(){
	$scope.getSelectedVNFs = [];
	$scope.selectedItems = [];
        var selected_vnfs = [];
        var selected_vnf_ids = [];

        angular.forEach($scope.selected_vnfs, function (vnfd_selected, vnfd_id) {
                if (vnfd_selected){
                    selected_vnf_ids.push(parseInt(vnfd_id));
                }
        });

        angular.forEach($scope.vnfs, function (item, item_key) {
                if (selected_vnf_ids.indexOf(parseInt(item.id)) !== -1){
		     Restangular.one('mdc/external/descriptor/', item.id).get().then(
        		function (response) {
            		    //console.log("MDC::GetDescriptor: " + JSON.stringify(response));
			    if (response.nsd) {
			        var descriptor = response.nsd;
				console.log("NSD present");
			    }
			    else {
			        var descriptor = response;
				console.log("VNF present");
			    }
			    descriptor.domain=item.domainId;
			    descriptor.reference=item.productId+"@"+item.domainId;
			    descriptor.productType=item.productType;
            		    $scope.getSelectedVNFs.push(descriptor);
            		    $scope.selectedItems.push(item);
        		}, function (response) {
            		    console.log("MDC::GetDescriptor error with status code " + response.status);
            		    console.log("MDC::GetDescriptor error message: " + response.data.detail);
        	        }
    		    );
                    //selected_vnfs.push(item);
                }
        });

        //console.log(selected_vnfs);
    };

    $scope.getVNFbyID = function(vnf_id){
        var v;
        angular.forEach($scope.getSelectedVNFs, function (vnfd, vnfd_key) {
                if (vnfd.id == vnf_id){

                    v = vnfd;

                }
        });
        return v;
    };

    $scope.getVNFbyRef = function(vnf_ref){
        var v;
        angular.forEach($scope.getSelectedVNFs, function (vnfd, vnfd_key) {
                if (vnfd.reference == vnf_ref){

                    v = vnfd;

                }
        });
        return v;
    };

    $scope.getVNFbyIDandDomain = function(vnf_id, domain){
        var v;
        angular.forEach($scope.getSelectedVNFs, function (vnfd, vnfd_key) {
                if ((vnfd.id == vnf_id) && (vnfd.domain == domain)){

                    v = vnfd;

                }
        });
        return v;
    };



    $scope.getSelectedVNFsTrade = function(){
        var selected_vnfs = [];
        var selected_vnf_ids = [];

        angular.forEach($scope.selected_vnfs_trade, function (vnfd_selected, vnfd_id) {
                if (vnfd_selected){
                    selected_vnf_ids.push(parseInt(vnfd_id));
                }
        });

        angular.forEach($scope.vnfs, function (vnfd, vnfd_key) {
                if (selected_vnf_ids.indexOf(parseInt(vnfd.id)) !== -1){
                    selected_vnfs.push(vnfd);
                }
        });

        //console.log(selected_vnfs);
        return selected_vnfs;
    };

    $scope.getVNFFlavors = function(vnf_ref){

        var flavors = [];
	if (vnf_ref) {
	    var vnf_id = vnf_ref.split("@")[0];
	    var domain_id = vnf_ref.split("@")[1];
            angular.forEach($scope.getSelectedVNFs, function (descriptor, descriptor_key) {
                if (descriptor.id == vnf_id){
                    if (descriptor.productType == "ns"){
                        angular.forEach(descriptor.sla, function (flavor, flavor_key) {
                                flavors.push(flavor.sla_key);
                        });
		    }
  		    else {
                        angular.forEach(descriptor.deployment_flavours, function (flavor, flavor_key) {
                                flavors.push(flavor.flavour_key);
                        });
		    }

                }
        });

        //console.log(selected_vnfs);
	};
        return flavors;

    };

    //STEPS
    $scope.active_step = 1;
    $scope.steps = {
        1:{
            enable:true
        },
        2:{
            enable:false
        },
        3:{
            enable:false
        },
        4:{
            enable:false
        },
        5:{
            enable:false
        },
        6:{
            enable:false
        },
        7:{
            enable:false
        },
        8:{
            enable:false
        }
    };

    $scope.gotoStep = function(target_step){
        if ($scope.steps[target_step].enable) {
            $scope.active_step = target_step;
        }
	if (target_step == 2) {
	    $scope.getSelectedDescriptors();
	    //console.log("selected vnfs: ", $scope.selected_vnfs);
	}
    };

    $scope.nextStep = function(target_step){

        if (target_step == target_step && $scope.active_step == target_step - 1) {
            $scope.steps[target_step].enable = true;
            $scope.active_step = target_step;
            console.log("STEP:"+$scope.active_step);
        }
	if (target_step == 2) {
	    $scope.getSelectedDescriptors();
	    //check if the there is at least one VNF selected before moving on to the next step
	    //console.log("selected vnfs: ", $scope.selected_vnfs);
            if ($scope.emptyVNFList($scope.selected_vnfs)) {
                alertService.add('danger', "You need to select at least one VNF");
                $scope.active_step=1; //go back to the previous step
                return;
            }
	}

        if ($scope.active_step==3){
            if($scope.nsd.name.length == 0) {
                alertService.add('danger', "NS name required");
                $scope.active_step=2; //go back to the previous step
                return;
            }
            if($scope.nsd.description.length == 0) {
                alertService.add('danger', "NS description required");
                $scope.active_step=2; //go back to the previous step
                return;
            }
        }

    };

    $scope.emptyVNFList = function (list) {
	empty=true;
        angular.forEach(list, function (item, index) {
	    if (item==true)
		empty=false;
	});
	return empty;
    };

    $scope.emptySLAKey = function (list) {
	empty=true;
        angular.forEach(list, function (item, index) {
	    if (item==true)
		empty=false;
	});
	return empty;
    };
/*
    $scope.trades ={};

    $scope.showTrade = function() {

         ModalService.showModal({
          animation: false,
          templateUrl: "/static/dashboard/templates/modals/trade.html",
          controller: "TradeRequestController",
          inputs: {
            vnfds: $scope.getSelectedVNFsTrade()
          }
        }).then(function(modal) {
          // only called on success...
             modal.element.modal();
             modal.close.then(function (trades) {

                angular.forEach(trades, function (trade, trade_key) {

                     $scope.trades[trade.vnfd_id] ={};
                     $scope.trades[trade.vnfd_id].inter = $interval(function () {

                        Restangular.one("/broker/vnfs/trade/", trade.id).get().then(
                            function (response) {
                                $scope.trades[trade.vnfd_id].status=response.status;
                                $scope.trades[trade.vnfd_id].price=response.price_override;
                                $scope.trades[trade.vnfd_id].setup_price=response.setup_price_override;

                                if (response.status!='pending'){
                                    $interval.cancel($scope.trades[trade.vnfd_id].inter);
                                }
                                console.log("GetTrade Update Status:" + response.status);
                            }, function (response) {
                                //console.log("GetNSD error with status code " + response.status);
                                //console.log("GetNSD error message: " + response.data.detail);
                            });

                     }, 2000);

                });

             });

        }).catch(function(error) {
          // error contains a detailed error message.
          console.log(error);
        });

  };

*/
}

function ServiceExchangeCtrl(Restangular, $scope, $rootScope, $state, ModalService, $interval, alertService) {

    $scope.dynamicPopover = {
        templateUrl: 'myPopoverTemplate.html'
    };

	$scope.updateDomains = function (item) {
		var domains = $scope.domainList;
		item.sharedWith = [];
		angular.forEach(domains, function (domain, id) {
			var present = false;
			angular.forEach(item.sharedWith, function (shared, id2) {
				if (domain.domain == shared.domainId)
					present = true;
			});
			if (!present)
				if ((domain.domain != "ALL") && (domain.domain != item.domainId))
					item.sharedWith.push( { "domainId": domain.domain, "share": false } )
		});
		console.log("updated item: ", JSON.stringify(item));
		return item;
	};

	$scope.saveCatalogue = function (item) {
        $rootScope.root_loading=true;
		Restangular.all('mdc/mdc/').customPOST(item).then(
            function (response) {
                //$scope.availableDomains = response;
                //console.log("SaveCatalogue: " + response.status);
                alertService.add('success', 'Item added successfully to the catalogue');
                $rootScope.root_loading=false;
            }, function (response) {
                console.log("SaveCatalogue: ERROR - " + response.status);
                console.log("SaveCatalogue: ERROR - " + response.data);
				if (response.status == 409)
                	alertService.add('danger', 'Item already present in the catalogue');
                $rootScope.root_loading=false;
            });

    };


    $scope.editNSModal = function (item, backup_item) {

		//$scope.getDomains();
        ModalService.showModal({
            templateUrl: "/static/dashboard/templates/modals/edit_catalogue_ns.html",
            controller: function () {
                this.item = $scope.updateDomains(item);
                this.backup_item = backup_item;
               	//this.domains = $scope.domainsList;
				this.closeModal = function () {
					console.log("Closing edition");
					this.item=item;
         			//$scope.loadMdCServices();
                    close(); // close, but give 500ms for bootstrap to animate
                };
				this.saveItem = function (it) {
					console.log("item to save: ", JSON.stringify(it));
					$scope.saveCatalogue(it);
         			//$scope.loadMdCServices();
                    close(); // close, but give 500ms for bootstrap to animate
                };
            },
            controllerAs: "EditCatalogueCtrl"
        }).then(function (modal) {
            // only called on success...
            modal.element.modal();
            modal.item = item;
            modal.backup_item = backup_item;
            //modal.vnfd_id = vnfd_id
        }).catch(function (error) {
            // error contains a detailed error message.
            console.log(error);
        });

    };

    $scope.loading_catalogue_services = false;
    $scope.vnfs = {};

    $scope.loadMdCServices = function () {
        $scope.loading_catalogue_services = true;
        Restangular.all('mdc/external/sharedwithme/').getList().then(
            function (response) {
                $scope.vnfs = response;
                console.log("MDC::GetItemsList " + response.length + " items found");
                $scope.loading_catalogue_services = false;

                $scope.updateFilteredVNFs('ALL');

            }, function (response) {
                console.log("MDC::GetVNFList error with status code " + response.status);
                console.log("MDC::GetVNFList error message: " + response.data.detail);
                $scope.loading_catalogue_services = false;
            });
    };



    $scope.localDomain = {};

    $scope.getDomainsList= function () {
        Restangular.all('mdc/domain').getList().then(
            function (response) {
                //$scope.availableDomains = response;
                console.log("GetDomainList " + response.length + " Domains found");
                //$scope.domainList=response;
				$scope.domainList=[{"domain": "ALL"}];
				angular.forEach(response, function(domain, id) {
		    		if (domain.localDomain)
						$scope.localDomain=domain;
		    		else
                        $scope.domainList.push(domain);

			});
                console.log("local Domain: " + JSON.stringify($scope.localDomain));
                //$scope.domainList.splice(0, 0, {"domain": "ALL"});
                console.log("GetDomainList " + JSON.stringify($scope.domainList));
            }, function (response) {
                console.log("GetDomainList error with status code " + response.status);
                console.log("GetDomainList error message: " + response.data);
            });

    };

    $scope.init = function () {
         $scope.loadMdCServices();
         $scope.getDomainsList();
    };

    $scope.init();



    $scope.selected_vnfs = {};
    $scope.selected_vnfs_trade = {};

    $scope.filtered_vnfs = $scope.vnfs;
    $scope.selected_vnf_filter_type = 'ALL';
    $scope.selected_price_range={min:0, max:300};
    $scope.domainList = [];




    $scope.updateFilteredVNFs = function(selected_vnf_filter_type){
        console.log("GetDomainList " + JSON.stringify($scope.domainList));
        // reset selected vnfds trade
        angular.forEach($scope.selected_vnfs_trade, function (vnfd_selected, vnfd_id) {
                $scope.selected_vnfs_trade[vnfd_id] = false;
        });

        if (selected_vnf_filter_type) {
            $scope.selected_vnf_filter_type = selected_vnf_filter_type;
        }

        var filtered_vnfs = [];

        angular.forEach($scope.vnfs, function (vnfd, vnfd_key) {

            if ($scope.selected_vnf_filter_type == vnfd.domainId || $scope.selected_vnf_filter_type == 'ALL') {

                //if (vnfd.billing_model.price.max_per_period >= $scope.selected_price_range.min && vnfd.billing_model.price.max_per_period <= $scope.selected_price_range.max) {
                if ($scope.selected_price_range.min <= vnfd.price_per_period && vnfd.price_per_period <= $scope.selected_price_range.max) {
                    filtered_vnfs.push(vnfd);
                }

            }

        });

        $scope.filtered_vnfs = filtered_vnfs;

        //check if a $digest is already in progress by checking $scope.$$phase.
        if (!$scope.$$phase) {
            $scope.$apply(); //this triggers a $digest
        }

    };

    $scope.updateFilteredVNFsPrice = function () {
        console.log($scope.selected_price_range);
    };


    $scope.getSelectedVNFs = function(){
        var selected_vnfs = [];
        var selected_vnf_ids = [];

        angular.forEach($scope.selected_vnfs, function (vnfd_selected, vnfd_id) {
                if (vnfd_selected){
                    selected_vnf_ids.push(parseInt(vnfd_id));
                }
        });

        angular.forEach($scope.vnfs, function (vnfd, vnfd_key) {
                if (selected_vnf_ids.indexOf(parseInt(vnfd.id)) !== -1){
                    selected_vnfs.push(vnfd);
                }
        });

        //console.log(selected_vnfs);
        return selected_vnfs;
    };

    $scope.getVNFbyID = function(vnf_id){
        var v;
        angular.forEach($scope.vnfs, function (vnfd, vnfd_key) {
                if (vnfd.id == vnf_id){

                    v = vnfd;

                }
        });
        return v;
    };


    $scope.getSelectedVNFsTrade = function(){
        var selected_vnfs = [];
        var selected_vnf_ids = [];

        angular.forEach($scope.selected_vnfs_trade, function (vnfd_selected, vnfd_id) {
                if (vnfd_selected){
                    selected_vnf_ids.push(parseInt(vnfd_id));
                }
        });

        angular.forEach($scope.vnfs, function (vnfd, vnfd_key) {
                if (selected_vnf_ids.indexOf(parseInt(vnfd.id)) !== -1){
                    selected_vnfs.push(vnfd);
                }
        });

        //console.log(selected_vnfs);
        return selected_vnfs;
    };

/*
    $scope.getVNFFlavors = function(vnf_id){

        var flavors = [];
        angular.forEach($scope.vnfs, function (vnfd, vnfd_key) {
                if (vnfd.id == vnf_id){

                    angular.forEach(vnfd.deployment_flavours, function (flavor, flavor_key) {
                            flavors.push(flavor.flavour_key);
                    });

                }
        });

        //console.log(selected_vnfs);
        return flavors;

    };
*/
    //STEPS
    $scope.active_step = 1;
    $scope.steps = {
        1:{
            enable:true
        },
        2:{
            enable:false
        },
        3:{
            enable:false
        },
        4:{
            enable:false
        },
        5:{
            enable:false
        },
        6:{
            enable:false
        },
        7:{
            enable:false
        },
        8:{
            enable:false
        }
    };

    $scope.gotoStep = function(target_step){
        if ($scope.steps[target_step].enable) {
            $scope.active_step = target_step;
        }
    };

    $scope.nextStep = function(target_step){

        if (target_step == target_step && $scope.active_step == target_step - 1) {
            $scope.steps[target_step].enable = true;
            $scope.active_step = target_step;
            console.log("STEP:"+$scope.active_step);

        }
    };
/*

    $scope.trades ={};

    $scope.showTrade = function() {

         ModalService.showModal({
          animation: false,
          templateUrl: "/static/dashboard/templates/modals/trade.html",
          controller: "TradeRequestController",
          inputs: {
            vnfds: $scope.getSelectedVNFsTrade()
          }
        }).then(function(modal) {
          // only called on success...
             modal.element.modal();
             modal.close.then(function (trades) {

                angular.forEach(trades, function (trade, trade_key) {

                     $scope.trades[trade.vnfd_id] ={};
                     $scope.trades[trade.vnfd_id].inter = $interval(function () {

                        Restangular.one("/broker/vnfs/trade/", trade.id).get().then(
                            function (response) {
                                $scope.trades[trade.vnfd_id].status=response.status;
                                $scope.trades[trade.vnfd_id].price=response.price_override;
                                $scope.trades[trade.vnfd_id].setup_price=response.setup_price_override;

                                if (response.status!='pending'){
                                    $interval.cancel($scope.trades[trade.vnfd_id].inter);
                                }
                                console.log("GetTrade Update Status:" + response.status);
                            }, function (response) {
                                //console.log("GetNSD error with status code " + response.status);
                                //console.log("GetNSD error message: " + response.data.detail);
                            });

                     }, 2000);

                });

             });

        }).catch(function(error) {
          // error contains a detailed error message.
          console.log(error);
        });

  };
*/
}

/*
angular.module('dashboard').controller('TradeRequestController', [
    '$scope', '$element', '$http', 'Restangular', 'close', 'vnfds',
    function ($scope, $element, $http, Restangular, close, vnfds) {

        $scope.new_price = 0;
        $scope.new_setup_price = 0;
        $scope.vnfds = vnfds;


        var trades = [];
        $scope.sendTradeRequest = function () {

            angular.forEach(vnfds, function (vnfd, vnfd_key) {
                var data = {
                    vnfd_id: vnfd.id,
                    provider_id: vnfd.provider_id,
                    price_override: $scope.new_price,
                    setup_price_override: $scope.new_setup_price
                };
                Restangular.all('/broker/vnfs/trade/').post(data).then(
                    function (response) {
                        console.log("Trade Request " + response.id + " has been successfully completed");


                        trades.push(response);
                        if (trades.length == 2) {
                            $('.modal-backdrop').remove();
                            close(trades);
                        }


                    }, function (response) {
                        console.log("Trade Request error with status code " + response.status);
                        console.log("Trade Request error message: " + response.data.detail);
                    });

            });

        };

    }]);
*/
function DomainCtrl(Restangular, $scope, $rootScope, $state, ModalService, $interval, alertService) {

    $scope.dynamicPopover = {
        templateUrl: 'myPopoverTemplate.html'
    };


	$scope.saveDomain= function (domain) {
        $rootScope.root_loading=true;
		Restangular.all('mdc/domain/'+domain.domain+'/').customPUT(domain).then(
            function (response) {
                alertService.add('success', 'Domain successfully modified');
                $rootScope.root_loading=false;
            }, function (response) {
                console.log("SaveDomain: ERROR - " + response.status);
                console.log("SaveDomain: ERROR - " + response.data);
                	alertService.add('danger', 'Domain update failed');
                $rootScope.root_loading=false;
            });

    };


    $scope.editDomainModal = function (domain) {

		//$scope.getDomains();
        ModalService.showModal({
            templateUrl: "/static/dashboard/templates/modals/edit_domain.html",
            controller: function () {
                this.domain= domain;
               	//this.domains = $scope.domainsList;
				this.closeModal = function () {
					console.log("Closing edition");
					this.domain=domain;
         			//$scope.loadMdCServices();
                    close(); // close, but give 500ms for bootstrap to animate
                };
				this.saveDomain= function (dom) {
					console.log("domain to save: ", JSON.stringify(dom));
					$scope.saveDomain(dom);
         			//$scope.loadMdCServices();
                    close(); // close, but give 500ms for bootstrap to animate
                };
            },
            controllerAs: "DomainCtrl"
        }).then(function (modal) {
            // only called on success...
            modal.element.modal();
            modal.domain= domain;
            //modal.vnfd_id = vnfd_id
        }).catch(function (error) {
            // error contains a detailed error message.
            console.log(error);
        });

    };



    $scope.localDomain = {};

    $scope.getDomainsList= function () {
        Restangular.all('mdc/domain').getList().then(
            function (response) {
                //$scope.availableDomains = response;
                console.log("GetDomainList " + response.length + " Domains found");
                //$scope.domainList=response;
				$scope.domainList=[];
				angular.forEach(response, function(domain, id) {
		    		if (domain.localDomain)
						$scope.localDomain=domain;
		    		else
                        $scope.domainList.push(domain);

			});
                console.log("local Domain: " + JSON.stringify($scope.localDomain));
                //$scope.domainList.splice(0, 0, {"domain": "ALL"});
                console.log("GetDomainList " + JSON.stringify($scope.domainList));
            }, function (response) {
                console.log("GetDomainList error with status code " + response.status);
                console.log("GetDomainList error message: " + response.data);
            });

    };

    $scope.init = function () {
         $scope.getDomainsList();
    };

    $scope.init();


    $scope.domainList = [];





        //check if a $digest is already in progress by checking $scope.$$phase.
        if (!$scope.$$phase) {
            $scope.$apply(); //this triggers a $digest
        }





    //STEPS
    $scope.active_step = 1;
    $scope.steps = {
        1:{
            enable:true
        },
        2:{
            enable:true
        }
    };

    $scope.gotoStep = function(target_step){
        if ($scope.steps[target_step].enable) {
            $scope.active_step = target_step;
        }
    };

    $scope.nextStep = function(target_step){

        if (target_step == target_step && $scope.active_step == target_step - 1) {
            //$scope.steps[target_step].enable = true;
            $scope.active_step = target_step;
            console.log("STEP:"+$scope.active_step);

        }
    };
}

