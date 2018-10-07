angular.module('dashboard').controller('DomainsCtrl', ['Restangular', '$scope', '$rootScope', '$state', 'ModalService', '$interval', 'alertService', DomainsCtrl]);


function DomainsCtrl(Restangular, $scope, $rootScope, $state, ModalService, $interval, alertService) {

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

    //STEPS
    $scope.active_step = 1;
    $scope.steps = {
        1:{
            enable:true
        },
        2:{
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
}

