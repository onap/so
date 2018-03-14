/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

define(['angular'], function(angular) {

  var DashboardController = ["$scope", "$http", "Uri", function($scope, $http, Uri) {

    $http.get(Uri.appUri("plugin://urnMap-plugin/:engine/process-instance"))
      .success(function(data) {
        $scope.UrnDataMap = data;
      });
 
    //enable saveRow button
    $scope.enableButton=function(urnData)
    {
       	document.getElementById("btn_" + urnData.urnname + "_key").disabled = false;
    };
    
    $scope.enableAddRowBtn=function()
    {
     	if(document.getElementById("new_key").value.trim().length >0)
    		 document.getElementById("addRow_BTN").disabled = false;
    	else
    		document.getElementById("addRow_BTN").disabled = true;
    };
    
    
    $scope.addNewRow = function() 
    {
    	var newKey = document.getElementById("new_key").value.trim();
    	var newValue = document.getElementById("new_value").value.trim();
    	var x;
    	
    	for (var i=0;i<$scope.UrnDataMap.length;i++)
    	{
     		var n = $scope.UrnDataMap[i].urnname.localeCompare(newKey);
     		if(n == 0){
    			x = "match";
    		}
    	}
    	
     	if(Boolean(x))
    	{
    		alert("URN Name already exists, please check the KEY!");
    	}
    	else
    	{
    		if(newKey.length >0 )
    		{

    			var temp = newKey + "|" + newValue;
              	
          		$http.put(Uri.appUri("plugin://urnMap-plugin/:engine/process-instance"), temp);
         
                document.getElementById("new_key").value = "";
                document.getElementById("new_value").value = "";

    		}
    		
     	}
     	//this.enableAddRowBtn;
     	document.getElementById("addRow_BTN").disabled = true;
     	
     $http.get(Uri.appUri("plugin://urnMap-plugin/:engine/process-instance"))
        .success(function(data) {
          $scope.UrnDataMap = data;
        });
        
        
      }
    
    $scope.retrieveData = function() {
    	
    	 $http.get(Uri.appUri("plugin://urnMap-plugin/:engine/process-instance"))
         .success(function(data) {
           $scope.UrnDataMap = data;
         });
    }
    
     $scope.SaveRow = function(user) 
    {  	
        $http.post(Uri.appUri("plugin://urnMap-plugin/:engine/process-instance"), user);
                   
      	document.getElementById("btn_" + user.urnname + "_key").disabled = true;
        document.getElementById(user.urnname + "_status").style.display = "";
        this.enableAddRowBtn;
        
        $http.get(Uri.appUri("plugin://urnMap-plugin/:engine/process-instance"))
        .success(function(data) {
          $scope.UrnDataMap = data;
        });
        
        };
  }];

  var Configuration = ['ViewsProvider', function(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.dashboard', {
      id: 'process-definitions',
      label: 'Deployed Processes',
      url: 'plugin://urnMap-plugin/static/app/dashboard.html',
      controller: DashboardController,
       // make sure we have a higher priority than the default plugin
      priority: 12
    });
  }];
//START
//END
  
  var ngModule = angular.module('cockpit.plugin.urnMap-plugin', []);

  ngModule.config(Configuration);

  return ngModule;
});
