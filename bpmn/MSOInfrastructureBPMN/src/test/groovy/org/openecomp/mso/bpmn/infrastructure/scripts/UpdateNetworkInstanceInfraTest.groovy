/*- 
 * ============LICENSE_START======================================================= 
 * OPENECOMP - MSO 
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

package org.openecomp.mso.bpmn.infrastructure.scripts;

import static org.junit.Assert.*
import static org.mockito.Mockito.*
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetCloudRegion
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetNetwork
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPutNetwork
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetNetworkPolicy
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetNetworkRouteTable
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetNetworkVpnBinding

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.camunda.bpm.engine.runtime.Execution
import org.junit.Before
import org.junit.Ignore;
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner
import org.openecomp.mso.bpmn.common.scripts.MsoUtils
import org.openecomp.mso.bpmn.core.WorkflowException

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit.WireMockRule


@RunWith(MockitoJUnitRunner.class)
class UpdateNetworkInstanceInfraTest  {
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(28090);
	
		def utils = new MsoUtils()

		String jsonIncomingRequest =
		"""{ "requestDetails": {
	      "modelInfo": {
			"modelType": "networkTyp",
  			"modelId": "modelId",
  			"modelNameVersionId": "modelNameVersionId",
  			"modelName": "CONTRAIL_EXTERNAL",
  			"modelVersion": "1"
		  },
		  "cloudConfiguration": {
  			"lcpCloudRegionId": "RDM2WAGPLCP",
  			"tenantId": "7dd5365547234ee8937416c65507d266"
		  },
		  "requestInfo": {
  			"instanceName": "MNS-25180-L-01-dmz_direct_net_1",
  			"source": "VID",
  			"callbackUrl": "",
            "suppressRollback": true,
	        "productFamilyId": "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb"
		  },
		  "relatedInstanceList": [ 
		  	{  
    	  		"relatedInstance": {
       				"instanceId": "f70e927b-6087-4974-9ef8-c5e4d5847ca4",
       				"modelInfo": {   
          				"modelType": "serviceT",
          				"modelId": "modelI",
          				"modelNameVersionId": "modelNameVersionI",
          				"modelName": "modleNam",
          				"modelVersion": "1"
       	  			}
        		}
     		}
		  ],
		  "requestParameters": {
  			"userParams": [	
               {
				 "name": "someUserParam1",
				 "value": "someValue1"
			   }
            ]
		  }
  }}"""		
  	
		  String jsonIncomingRequest_Missingname =
		"""{ "requestDetails": {
	      "modelInfo": {
			"modelType": "networkTyp",
  			"modelId": "modelId",
  			"modelNameVersionId": "modelNameVersionId",
  			"modelName": "CONTRAIL_EXTERNAL",
  			"modelVersion": "1"
		  },
		  "cloudConfiguration": {
  			"lcpCloudRegionId": "RDM2WAGPLCP",
  			"tenantId": "7dd5365547234ee8937416c65507d266"
		  },
		  "requestInfo": {
  			"source": "VID",
  			"callbackUrl": "",
            "suppressRollback": true,
	        "productFamilyId": "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb"
		  },
		  "relatedInstanceList": [ 
		  	{  
    	  		"relatedInstance": {
       				"instanceId": "f70e927b-6087-4974-9ef8-c5e4d5847ca4",
       				"modelInfo": {   
          				"modelType": "serviceT",
          				"modelId": "modelI",
          				"modelNameVersionId": "modelNameVersionI",
          				"modelName": "modleNam",
          				"modelVersion": "1"
       	  			}
        		}
     		}
		  ],
		  "requestParameters": {
  			"userParams": []
		  }
  }}"""		
		  
		  String jsonIncomingRequest_MissingCloudRegion =
		"""{ "requestDetails": {
	      "modelInfo": {
			"modelType": "networkTyp",
  			"modelId": "modelId",
  			"modelNameVersionId": "modelNameVersionId",
  			"modelName": "CONTRAIL_EXTERNAL",
  			"modelVersion": "1"
		  },
		  "cloudConfiguration": {
  			"tenantId": "7dd5365547234ee8937416c65507d266"
		  },
		  "requestInfo": {
  			"instanceName": "MNS-25180-L-01-dmz_direct_net_1",
  			"source": "VID",
  			"callbackUrl": "",
            "suppressRollback": true,
	        "productFamilyId": "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb"
		  },
		  "relatedInstanceList": [ 
		  	{  
    	  		"relatedInstance": {
       				"instanceId": "f70e927b-6087-4974-9ef8-c5e4d5847ca4",
       				"modelInfo": {   
          				"modelType": "serviceT",
          				"modelId": "modelI",
          				"modelNameVersionId": "modelNameVersionI",
          				"modelName": "modleNam",
          				"modelVersion": "1"
       	  			}
        		}
     		}
		  ],
		  "requestParameters": {
  			"userParams": []
		  }
  }}"""		
		
		

   String expectedNetworkRequestMissingNetworkId =
   """<network-request xmlns="http://www.w3.org/2001/XMLSchema">  
   <request-info>             
      <request-id>88f65519-9a38-4c4b-8445-9eb4a5a5af56</request-id> 
      <action>UPDATE</action>  
      <source>VID</source>  
      <service-instance-id>f70e927b-6087-4974-9ef8-c5e4d5847ca4</service-instance-id> 
   </request-info>  
   <network-inputs>  
      <network-id/>  
      <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>  
      <network-type>CONTRAIL_EXTERNAL</network-type> 
      <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>  
      <tenant-id>7dd5365547234ee8937416c65507d266</tenant-id> 
      <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>  
      <backout-on-failure>true</backout-on-failure> 
   </network-inputs>  
   <network-params>
      <param name="some_user_param1">someValue1</param> 
   </network-params> 
</network-request>
"""

  
String expectedNetworkRequestMissingCloudRegion =
"""<network-request xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
   <request-info>
      <request-id>88f65519-9a38-4c4b-8445-9eb4a5a5af56</request-id>
      <action>UPDATE</action>
      <source>PORTAL</source>
   </request-info>
   <network-inputs>
      <network-name>HSL_direct_net_2</network-name>
      <network-type>CONTRAIL_EXTERNAL</network-type>
      <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
      <aic-cloud-region/>
      <tenant-id>e81d842d3e8b45c5a59f57cd76af3aaf</tenant-id>
   </network-inputs>
   <network-params>
      <param name="shared">1</param>
   </network-params>
</network-request>"""

		// vnfRESTRequest
		String networkRESTRequest =
"""<rest:RESTResponse xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"
					xmlns:vnfreq="http://org.openecomp/mso/infra/vnf-request/v1"
					statusCode="200">
	<rest:payload contentType="text/xml">
	   <vnfreq:network-request>
		  <vnfreq:request-info>
			 <vnfreq:request-id>1ef47428-cade-45bd-a103-0751e8b2deb0</vnfreq:request-id>
			 <vnfreq:action>UPDATE</vnfreq:action>
			 <vnfreq:source>PORTAL</vnfreq:source>
		  </vnfreq:request-info>
		  <vnfreq:network-inputs>
			 <vnfreq:network-name>MNS-25180-L-01-dmz_direct_net_1</vnfreq:network-name>
			 <vnfreq:network-type>CONTRAIL_EXTERNAL</vnfreq:network-type>
			 <vnfreq:service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</vnfreq:service-id>
			 <vnfreq:aic-cloud-region>RDM2WAGPLCP</vnfreq:aic-cloud-region>
			 <vnfreq:tenant-id>7dd5365547234ee8937416c65507d266</vnfreq:tenant-id>
			 <vnfreq:physicalNetworkName>dvs-slcp3-01</vnfreq:physicalNetworkName>
			 <vnfreq:vlans>3008</vnfreq:vlans>
		  </vnfreq:network-inputs>
		  <vnfreq:network-params>
			 <param name="shared">1</param>
			 <param name="external">0</param>
		  </vnfreq:network-params>
	   </vnfreq:network-request>
	</rest:payload>
 </rest:RESTResponse>"""

	String networkInputsMissingName =
 """<network-inputs xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
	<network-name/>
	<network-type>CONTRAIL_EXTERNAL</network-type>
	<service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
	<aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
	<tenant-id>e81d842d3e8b45c5a59f57cd76af3aaf</tenant-id>
 </network-inputs>"""

String networkInputsMissingCloudRegion =
"""<network-inputs xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
   <network-name>HSL_direct_net_2</network-name>
   <network-type>CONTRAIL_EXTERNAL</network-type>
   <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
   <aic-cloud-region/>
   <tenant-id>e81d842d3e8b45c5a59f57cd76af3aaf</tenant-id>
</network-inputs>"""
   
	String expectedUpdateNetworkInstanceInfraRequest =
	"""<rest:payload xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"
              xmlns:vnfreq="http://org.openecomp/mso/infra/vnf-request/v1"
              contentType="text/xml">
   <vnfreq:network-request>
      <vnfreq:request-info>
         <vnfreq:request-id>1ef47428-cade-45bd-a103-0751e8b2deb0</vnfreq:request-id>
         <vnfreq:action>UPDATE</vnfreq:action>
         <vnfreq:source>PORTAL</vnfreq:source>
      </vnfreq:request-info>
      <vnfreq:network-inputs>
         <vnfreq:network-name>MNS-25180-L-01-dmz_direct_net_1</vnfreq:network-name>
         <vnfreq:network-type>CONTRAIL_EXTERNAL</vnfreq:network-type>
         <vnfreq:service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</vnfreq:service-id>
         <vnfreq:aic-cloud-region>RDM2WAGPLCP</vnfreq:aic-cloud-region>
         <vnfreq:tenant-id>7dd5365547234ee8937416c65507d266</vnfreq:tenant-id>
         <vnfreq:physicalNetworkName>dvs-slcp3-01</vnfreq:physicalNetworkName>
         <vnfreq:vlans>3008</vnfreq:vlans>
      </vnfreq:network-inputs>
      <vnfreq:network-params>
         <param name="shared">1</param>
         <param name="external">0</param>
      </vnfreq:network-params>
   </vnfreq:network-request>
</rest:payload>"""
  
	  String expectedUpdateNetworkInstanceInfraRequest_Output =
  """<rest:payload xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"
              xmlns:vnfreq="http://org.openecomp/mso/infra/vnf-request/v1"
              contentType="text/xml">
   <vnfreq:network-request>
      <vnfreq:request-info>
         <vnfreq:request-id>1ef47428-cade-45bd-a103-0751e8b2deb0</vnfreq:request-id>
         <vnfreq:action>UPDATE</vnfreq:action>
         <vnfreq:source>PORTAL</vnfreq:source>
      </vnfreq:request-info>
      <vnfreq:network-inputs>
         <vnfreq:network-name>MNS-25180-L-01-dmz_direct_net_1</vnfreq:network-name>
         <vnfreq:network-type>CONTRAIL_EXTERNAL</vnfreq:network-type>
         <vnfreq:service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</vnfreq:service-id>
         <vnfreq:aic-cloud-region>RDM2WAGPLCP</vnfreq:aic-cloud-region>
         <vnfreq:tenant-id>7dd5365547234ee8937416c65507d266</vnfreq:tenant-id>
      </vnfreq:network-inputs>
      <vnfreq:network-outputs>
         <vnfreq:network-name>MNS-25180-L-01-dmz_direct_net_1</vnfreq:network-name>
         <vnfreq:network-id>bdc5efe8-404a-409b-85f6-0dcc9eebae30</vnfreq:network-id>
      </vnfreq:network-outputs>
      <vnfreq:network-params>
         <param name="shared">1</param>
         <param name="external">0</param>
      </vnfreq:network-params>
   </vnfreq:network-request>
</rest:payload>"""
	
	  // expectedNetworkRequest
	  String expectedNetworkRequest =
  """<network-request xmlns="http://www.w3.org/2001/XMLSchema">  
   <request-info>  
      <action>UPDATE</action>  
      <source>VID</source>  
      <service-instance-id>f70e927b-6087-4974-9ef8-c5e4d5847ca4</service-instance-id> 
   </request-info>  
   <network-inputs>  
      <network-id>49c86598-f766-46f8-84f8-8d1c1b10f9b4</network-id>  
      <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>  
      <network-type>CONTRAIL_EXTERNAL</network-type> 
      <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>  
      <tenant-id>7dd5365547234ee8937416c65507d266</tenant-id> 
      <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>  
      <backout-on-failure>true</backout-on-failure> 
   </network-inputs>
   <network-params>
      <param name="dhcp-enabled">true</param>
      <param name="serviceId">a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</param>
      <param name="cidr-mask">true</param>
  	  <param name="backoutOnFailure">true</param>
  	  <param name="gateway-address">10.10.125.1</param>
   </network-params>   
</network-request>"""
  
String expectedNetworkInputs =
"""<network-inputs xmlns="http://www.w3.org/2001/XMLSchema">
   <network-id/>
   <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
   <network-type>CONTRAIL_EXTERNAL</network-type>
   <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
   <tenant-id>7dd5365547234ee8937416c65507d266</tenant-id>
   <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
   <backout-on-failure>true</backout-on-failure>
</network-inputs>"""

 
  String NetworkRequest_noPhysicalName =
  """<vnfreq:network-request xmlns:vnfreq="http://org.openecomp/mso/infra/vnf-request/v1">
   <vnfreq:request-info>
      <vnfreq:request-id>1ef47428-cade-45bd-a103-0751e8b2deb0</vnfreq:request-id>
      <vnfreq:action>UPDATE</vnfreq:action>
      <vnfreq:source>PORTAL</vnfreq:source>
   </vnfreq:request-info>
   <vnfreq:network-inputs>
      <vnfreq:network-name>MNS-25180-L-01-dmz_direct_net_1</vnfreq:network-name>
      <vnfreq:network-type>CONTRAIL_EXTERNAL</vnfreq:network-type>
      <vnfreq:service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</vnfreq:service-id>
      <vnfreq:aic-cloud-region>RDM2WAGPLCP</vnfreq:aic-cloud-region>
      <vnfreq:tenant-id>7dd5365547234ee8937416c65507d266</vnfreq:tenant-id>
      <vnfreq:vlans>3008</vnfreq:vlans>
   </vnfreq:network-inputs>
   <vnfreq:network-params>
   <network-params>
      <param name="dhcp-enabled">true</param>
      <param name="serviceId">a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</param>
      <param name="cidr-mask">true</param>
  	  <param name="backoutOnFailure">true</param>
  	  <param name="gateway-address">10.10.125.1</param>
   </network-params> 
   </vnfreq:network-params>
</vnfreq:network-request>"""
  
  String vnfRequestFakeRegion =
  """<vnfreq:network-request xmlns:vnfreq="http://org.openecomp/mso/infra/vnf-request/v1">
   <vnfreq:request-info>
      <vnfreq:request-id>1ef47428-cade-45bd-a103-0751e8b2deb0</vnfreq:request-id>
      <vnfreq:action>UPDATE</vnfreq:action>
      <vnfreq:source>PORTAL</vnfreq:source>
   </vnfreq:request-info>
   <vnfreq:network-inputs>
      <vnfreq:network-name>MNS-25180-L-01-dmz_direct_net_1</vnfreq:network-name>
      <vnfreq:network-type>CONTRAIL_EXTERNAL</vnfreq:network-type>
      <vnfreq:service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</vnfreq:service-id>
      <vnfreq:aic-cloud-region>MDTWNJ21</vnfreq:aic-cloud-region>
      <vnfreq:tenant-id>7dd5365547234ee8937416c65507d266</vnfreq:tenant-id>
   </vnfreq:network-inputs>
   <vnfreq:network-params>
      <param name="shared">1</param>
      <param name="external">0</param>
   </vnfreq:network-params>
</vnfreq:network-request>"""
  
  // expectedNetworkRequest
	  String expectedNetworkRequest_Outputs =
  """<vnfreq:network-request xmlns:vnfreq="http://org.openecomp/mso/infra/vnf-request/v1">
   <vnfreq:request-info>
      <vnfreq:request-id>1ef47428-cade-45bd-a103-0751e8b2deb0</vnfreq:request-id>
      <vnfreq:action>UPDATE</vnfreq:action>
      <vnfreq:source>PORTAL</vnfreq:source>
   </vnfreq:request-info>
   <vnfreq:network-inputs>
      <vnfreq:network-name>MNS-25180-L-01-dmz_direct_net_1</vnfreq:network-name>
      <vnfreq:network-type>CONTRAIL_EXTERNAL</vnfreq:network-type>
      <vnfreq:service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</vnfreq:service-id>
      <vnfreq:aic-cloud-region>RDM2WAGPLCP</vnfreq:aic-cloud-region>
      <vnfreq:tenant-id>7dd5365547234ee8937416c65507d266</vnfreq:tenant-id>
   </vnfreq:network-inputs>
   <vnfreq:network-outputs>
      <vnfreq:network-name>MNS-25180-L-01-dmz_direct_net_1</vnfreq:network-name>
      <vnfreq:network-id>bdc5efe8-404a-409b-85f6-0dcc9eebae30</vnfreq:network-id>
   </vnfreq:network-outputs>
   <vnfreq:network-params>
      <param name="shared">1</param>
      <param name="external">0</param>
   </vnfreq:network-params>
</vnfreq:network-request>"""
  
		  
  // expectedNetworkRequest
		  String networkInputs_404 =
		  """<network-inputs  xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
		      <network-name>myOwn_Network</network-name>
		      <network-type>CONTRAIL_EXTERNAL</network-type>
		      <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
		      <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
		      <tenant-id>e81d842d3e8b45c5a59f57cd76af3aaf</tenant-id>
		   </network-inputs>"""
  
  String networkInputs =
  """<network-inputs xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
   <network-id>bdc5efe8-404a-409b-85f6-0dcc9eebae30</network-id>
   <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
   <network-type>CONTRAIL_EXTERNAL</network-type>
   <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
   <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
   <tenant-id>e81d842d3e8b45c5a59f57cd76af3aaf</tenant-id>
</network-inputs>"""
  
	  String networkOutputs =
	"""<network-outputs>
                   <network-id>49c86598-f766-46f8-84f8-8d1c1b10f9b4</network-id>			
                   <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
                 </network-outputs>"""

	  String queryAAIResponse =
		  """<rest:RESTResponse xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"
                   statusCode="200">
   <rest:headers>
      <rest:header name="Transfer-Encoding" value="chunked"/>
      <rest:header name="Date" value="Thu,10 Mar 2016 00:01:18 GMT"/>
      <rest:header name="Expires" value="Thu,01 Jan 1970 00:00:00 UTC"/>
      <rest:header name="X-AAI-TXID" value="mtcnjv9aaas03-20160310-00:01:18:502-132671"/>
      <rest:header name="Content-Type" value="application/xml"/>
      <rest:header name="Server" value="Apache-Coyote/1.1"/>
      <rest:header name="Cache-Control" value="private"/>
   </rest:headers>
   <rest:payload contentType="text/xml">
      <l3-network xmlns="http://org.openecomp.aai.inventory/v3">
         <network-id>bdc5efe8-404a-409b-85f6-0dcc9eebae30</network-id>
         <network-name>HSL_direct_net_2</network-name>
         <network-type>CONTRAIL_EXTERNAL</network-type>
         <network-role>dmz_direct</network-role>
         <network-technology>contrail</network-technology>
         <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
         <network-role-instance>0</network-role-instance>
         <orchestration-status>pending-delete</orchestration-status>
         <subnets>
            <subnet>
               <subnet-id>57e9a1ff-d14f-4071-a828-b19ae98eb2fc</subnet-id>
               <gateway-address>107.239.52.1</gateway-address>
               <network-start-address>107.239.52.0</network-start-address>
               <cidr-mask>24</cidr-mask>
               <ip-version>4</ip-version>
               <orchestration-status>pending-delete</orchestration-status>
               <dhcp-enabled>true</dhcp-enabled>
               <relationship-list/>
            </subnet>
         </subnets>
         <relationship-list>
            <relationship>
               <related-to>vpn-binding</related-to>
               <related-link>https://aai-app-e2e.test.com:8443/aai/v8/network/vpn-bindings/vpn-binding/85f015d0-2e32-4c30-96d2-87a1a27f8017/</related-link>
               <relationship-data>
                  <relationship-key>vpn-binding.vpn-id</relationship-key>
                  <relationship-value>85f015d0-2e32-4c30-96d2-87a1a27f8017</relationship-value>
               </relationship-data>
            </relationship>
            <relationship>
               <related-to>vpn-binding</related-to>
               <related-link>https://aai-app-e2e.test.com:8443/aai/v8/network/vpn-bindings/vpn-binding/c980a6ef-3b88-49f0-9751-dbad8608d0a6/</related-link>
               <relationship-data>
                  <relationship-key>vpn-binding.vpn-id</relationship-key>
                  <relationship-value>c980a6ef-3b88-49f0-9751-dbad8608d0a6</relationship-value>
               </relationship-data>
            </relationship>
            <relationship>
               <related-to>tenant</related-to>
               <related-link>https://aai-app-e2e.test.com:8443/aai/v8/cloud-infrastructure/tenants/tenant/7dd5365547234ee8937416c65507d266/</related-link>
               <relationship-data>
                  <relationship-key>tenant.tenant-id</relationship-key>
                  <relationship-value>7dd5365547234ee8937416c65507d266</relationship-value>
               </relationship-data>
            </relationship>
         </relationship-list>
      </l3-network>
   </rest:payload>
</rest:RESTResponse>"""
  
	  String queryIdAIIResponse =
	  """<rest:RESTResponse xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"
                   statusCode="200">
   <rest:headers>
      <rest:header name="Transfer-Encoding" value="chunked"/>
      <rest:header name="Date" value="Thu,10 Mar 2016 00:01:18 GMT"/>
      <rest:header name="Expires" value="Thu,01 Jan 1970 00:00:00 UTC"/>
      <rest:header name="X-AAI-TXID" value="mtcnjv9aaas03-20160310-00:01:18:502-132671"/>
      <rest:header name="Content-Type" value="application/xml"/>
      <rest:header name="Server" value="Apache-Coyote/1.1"/>
      <rest:header name="Cache-Control" value="private"/>
   </rest:headers>
   <rest:payload contentType="text/xml">
      <l3-network xmlns="http://org.openecomp.aai.inventory/v6">
         <network-id>49c86598-f766-46f8-84f8-8d1c1b10f9b4</network-id>
         <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
         <network-type>CONTRAIL_EXTERNAL</network-type>
         <network-role>dmz_direct</network-role>
         <network-technology>contrail</network-technology>
         <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
         <network-role-instance>0</network-role-instance>
		 <resource-version>l3-version</resource-version>
         <orchestration-status>pending-delete</orchestration-status>
	     <heat-stack-id>ST_2Bindings_6006/55288ef0-595c-47d3-819e-cf93aaac6326</heat-stack-id>
	  	 <physical-network-name>networkName</physical-network-name>
	     <is-provider-network>false</is-provider-network>
	  	 <is-shared-network>true</is-shared-network>
	  	 <is-external-network>false</is-external-network>
         <subnets>
            <subnet>
               <subnet-id>57e9a1ff-d14f-4071-a828-b19ae98eb2fc</subnet-id>
               <gateway-address>107.239.52.1</gateway-address>
               <network-start-address>107.239.52.0</network-start-address>
               <cidr-mask>24</cidr-mask>
               <ip-version>4</ip-version>
               <orchestration-status>pending-delete</orchestration-status>
               <dhcp-enabled>true</dhcp-enabled>
	  		   <subnet-name>subnetName</subnet-name>
               <relationship-list/>
            </subnet>
            <subnet>
               <subnet-id>57e9a1ff-d14f-4071-a828-b19ae98eb2fc</subnet-id>
               <gateway-address>107.239.52.1</gateway-address>
               <network-start-address>107.239.52.0</network-start-address>
               <cidr-mask>24</cidr-mask>
               <ip-version>4</ip-version>
               <orchestration-status>pending-delete</orchestration-status>
               <dhcp-enabled>true</dhcp-enabled>
	  		   <subnet-name>subnetName</subnet-name>
               <relationship-list/>
            </subnet>
         </subnets>
	  	 <segmentation-assignments>
	  		<segmentation-id>414</segmentation-id>
	  		<resource-version>4132176</resource-version>
	  	 </segmentation-assignments>	
	  	 <segmentation-assignments>
	  		<segmentation-id>415</segmentation-id>
	  		<resource-version>4132176</resource-version>
	  	 </segmentation-assignments>	
		 <ctag-assignments>	
			 <ctag-assignment>	
				 <vlan-id-inner>inner</vlan-id-inner>	
				 <resource-version>ctag-version</resource-version>	  
	             <relationship-list>
					<relationship>
					   <related-to>tenant</related-to>
					   <related-link>https://aai-ext1.test.com:8443/aai/v8/cloud-infrastructure/tenants/tenant/897deadc2b954a6bac6d3c197fb3525e/</related-link>
					   <relationship-data>
						  <relationship-key>tenant.tenant-id</relationship-key>
						  <relationship-value>897deadc2b954a6bac6d3c197fb3525e</relationship-value>
					   </relationship-data>
					   <related-to-property>
						  <property-key>tenant.tenant-name</property-key>
						  <property-value>MSOTest1</property-value>
					   </related-to-property>
					</relationship>
					<relationship>
					   <related-to>vpn-binding</related-to>
					   <related-link>https://aai-ext1.test.com:8443/aai/v8/network/vpn-bindings/vpn-binding/a290b841-f672-44dd-b9cd-6f8c20d7d8c8/</related-link>
					   <relationship-data>
						  <relationship-key>vpn-binding.vpn-id</relationship-key>
						  <relationship-value>a290b841-f672-44dd-b9cd-6f8c20d7d8c8</relationship-value>
					   </relationship-data>
					   <related-to-property>
						  <property-key>vpn-binding.vpn-name</property-key>
						  <property-value>oam_protected_net_6_MTN5_msotest2</property-value>
					   </related-to-property>
					</relationship>
					<relationship>
					   <related-to>vpn-binding</related-to>
					   <related-link>https://aai-ext1.test.com:8443/aai/v8/network/vpn-bindings/vpn-binding/24a4b507-853a-4a38-99aa-05fcc54be24d/</related-link>
					   <relationship-data>
						  <relationship-key>vpn-binding.vpn-id</relationship-key>
						  <relationship-value>24a4b507-853a-4a38-99aa-05fcc54be24d</relationship-value>
					   </relationship-data>
					   <related-to-property>
						  <property-key>vpn-binding.vpn-name</property-key>
						  <property-value>oam_protected_net_6_MTN5_msotest1</property-value>
					   </related-to-property>
					</relationship>
				 </relationship-list>  
				</ctag-assignment>	
		 </ctag-assignments>
         <relationship-list>
            <relationship>
               <related-to>vpn-binding</related-to>
               <related-link>https://aai-app-e2e.test.com:8443/aai/v8/network/vpn-bindings/vpn-binding/85f015d0-2e32-4c30-96d2-87a1a27f8017/</related-link>
               <relationship-data>
                  <relationship-key>vpn-binding.vpn-id</relationship-key>
                  <relationship-value>85f015d0-2e32-4c30-96d2-87a1a27f8017</relationship-value>
               </relationship-data>
            </relationship>
            <relationship>
               <related-to>vpn-binding</related-to>
               <related-link>https://aai-app-e2e.test.com:8443/aai/v8/network/vpn-bindings/vpn-binding/c980a6ef-3b88-49f0-9751-dbad8608d0a6/</related-link>
               <relationship-data>
                  <relationship-key>vpn-binding.vpn-id</relationship-key>
                  <relationship-value>c980a6ef-3b88-49f0-9751-dbad8608d0a6</relationship-value>
               </relationship-data>
            </relationship>
            <relationship>
               <related-to>tenant</related-to>
               <related-link>https://aai-app-e2e.test.com:8443/aai/v8/cloud-infrastructure/tenants/tenant/7dd5365547234ee8937416c65507d266/</related-link>
               <relationship-data>
                  <relationship-key>tenant.tenant-id</relationship-key>
                  <relationship-value>7dd5365547234ee8937416c65507d266</relationship-value>
               </relationship-data>
            </relationship>
			<relationship>
			  <related-to>network-policy</related-to>
			  <related-link>https://aai-app-e2e.test.com:8443/aai/v8/network/network-policies/network-policy/cee6d136-e378-4678-a024-2cd15f0ee0cg</related-link>
			  <relationship-data>
				  <relationship-key>network-policy.network-policy-id</relationship-key>
				  <relationship-value>cee6d136-e378-4678-a024-2cd15f0ee0cg</relationship-value>
			  </relationship-data>
			</relationship>
			<relationship>
			   <related-to>route-table-reference</related-to>
	  	       <related-link>https://aai-app-e2e.test.com:8443/aai/v8/network/route-table-references/route-table-reference/refFQDN1</related-link>
			   <relationship-data>
				  <relationship-key>route-table-reference.route-table-reference-id</relationship-key>
				  <relationship-value>cee6d136-e378-4678-a024-2cd15f0ee0hi</relationship-value>
			   </relationship-data>
			</relationship>
			<relationship>
			   <related-to>route-table-reference</related-to>
	  	       <related-link>https://aai-app-e2e.test.com:8443/aai/v8/network/route-table-references/route-table-reference/refFQDN2</related-link>
			   <relationship-data>
				  <relationship-key>route-table-reference.route-table-reference-id</relationship-key>
				  <relationship-value>cee6d136-e378-4678-a024-2cd15f0ee0hi</relationship-value>
			   </relationship-data>
			</relationship>
         </relationship-list>
      </l3-network>
   </rest:payload>
</rest:RESTResponse>"""
  
	  String queryIdAIIResponseTestScenario01 =
  """<?xml version="1.0" encoding="UTF-8"?>
<l3-network xmlns="http://org.openecomp.aai.inventory/v7">
	<network-id>4da55fe4-7a9e-478c-a434-8a98d62265ab</network-id>
	<network-name>GN_EVPN_direct_net_0_ST1</network-name>
	<network-type>CONTRAIL30_BASIC</network-type>
	<network-role>GN_EVPN_direct</network-role>
	<network-technology>contrail</network-technology>
	<is-bound-to-vpn>false</is-bound-to-vpn>
	<service-id>9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
	<network-role-instance>0</network-role-instance>
	<resource-version>1465398611</resource-version>
	<orchestration-status>pending-delete</orchestration-status>
  	<physical-network-name>networkName</physical-network-name>
	<is-provider-network>false</is-provider-network>
	<is-shared-network>true</is-shared-network>
	<is-external-network>false</is-external-network>
	<subnets>
		<subnet>
			<subnet-id>cb1a7b47-5428-44c9-89c2-8b17541c3228</subnet-id>
			<gateway-address>108.239.40.1</gateway-address>
			<network-start-address>108.239.40.0</network-start-address>
			<cidr-mask>28</cidr-mask>
			<ip-version>4</ip-version>
			<orchestration-status>pending-delete</orchestration-status>
			<dhcp-enabled>true</dhcp-enabled>
			<dhcp-start>108.239.40.0</dhcp-start>
			<dhcp-end>108.239.40.0</dhcp-end>
			<resource-version>1465398611</resource-version>
  		    <subnet-name>subnetName</subnet-name>
			<relationship-list />
		</subnet>
		<subnet>
			<subnet-id>e2cc7c14-90f0-4205-840d-b4e07f04e621</subnet-id>
			<gateway-address>2606:ae00:2e01:604::1</gateway-address>
			<network-start-address>2606:ae00:2e01:604::</network-start-address>
			<cidr-mask>64</cidr-mask>
			<ip-version>6</ip-version>
			<orchestration-status>pending-delete</orchestration-status>
			<dhcp-enabled>true</dhcp-enabled>
			<dhcp-start>2606:ae00:2e01:604::</dhcp-start>
			<dhcp-end>2606:ae00:2e01:604::</dhcp-end>
			<resource-version>1465398611</resource-version>
  			<subnet-name>subnetName</subnet-name>
			<relationship-list />
		</subnet>
	</subnets>
	<ctag-assignments />
	<segmentation-assignments>
	   	<segmentation-id>416</segmentation-id>
	  	<resource-version>4132176</resource-version>
	</segmentation-assignments>
	<relationship-list>
		<relationship>
			<related-to>cloud-region</related-to>
			<related-link>https://mtanjv9aaas03.aic.cip.com:8443/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/att-aic/AAIAIC25/
			</related-link>
			<relationship-data>
				<relationship-key>cloud-region.cloud-region-id</relationship-key>
				<relationship-value>AAIAIC25</relationship-value>
			</relationship-data>
			<relationship-data>
				<relationship-key>cloud-region.cloud-owner</relationship-key>
				<relationship-value>att-aic</relationship-value>
			</relationship-data>
			<related-to-property>
				<property-key>cloud-region.owner-defined-type</property-key>
				<property-value></property-value>
			</related-to-property>
		</relationship>
		<relationship>
			<related-to>tenant</related-to>
			<related-link>https://mtanjv9aaas03.aic.cip.com:8443/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/att-aic/AAIAIC25/tenants/tenant/4ae1d3446a4c48b2bec44b6cfba06d68/</related-link>
			<relationship-data>
				<relationship-key>tenant.tenant-id</relationship-key>
				<relationship-value>4ae1d3446a4c48b2bec44b6cfba06d68
				</relationship-value>
			</relationship-data>
			<relationship-data>
				<relationship-key>cloud-region.cloud-owner</relationship-key>
				<relationship-value>att-aic</relationship-value>
			</relationship-data>
			<relationship-data>
				<relationship-key>cloud-region.cloud-region-id</relationship-key>
				<relationship-value>AAIAIC25</relationship-value>
			</relationship-data>
			<related-to-property>
				<property-key>tenant.tenant-name</property-key>
				<property-value>Ruchira Contrail 3.0 test</property-value>
			</related-to-property>
		</relationship>
		<relationship>
			<related-to>vpn-binding</related-to>
			<related-link>https://mtanjv9aaas03.aic.cip.com:8443/aai/v8/network/vpn-bindings/vpn-binding/85f015d0-2e32-4c30-96d2-87a1a27f8017/</related-link>
			<relationship-data>
				<relationship-key>vpn-binding.vpn-id</relationship-key>
				<relationship-value>9a7b327d9-287aa00-82c4b0-100001</relationship-value>
			</relationship-data>
			<related-to-property>
				<property-key>vpn-binding.vpn-name</property-key>
				<property-value>GN_EVPN_direct_net_0_ST1</property-value>
			</related-to-property>
		</relationship>
		<relationship>
		   <related-to>route-table-reference</related-to>
		   <relationship-data>
			  <relationship-key>route-table-reference.route-table-reference-id</relationship-key>
			  <relationship-value>cee6d136-e378-4678-a024-2cd15f0ee0hi</relationship-value>
		   </relationship-data>
		</relationship>
	</relationship-list>
</l3-network>"""	
  
  String queryIdAIIResponseVpnNotPresent =
  """<rest:RESTResponse xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"
                   statusCode="200">
   <rest:headers>
      <rest:header name="Transfer-Encoding" value="chunked"/>
      <rest:header name="Date" value="Thu,10 Mar 2016 00:01:18 GMT"/>
      <rest:header name="Expires" value="Thu,01 Jan 1970 00:00:00 UTC"/>
      <rest:header name="X-AAI-TXID" value="mtcnjv9aaas03-20160310-00:01:18:502-132671"/>
      <rest:header name="Content-Type" value="application/xml"/>
      <rest:header name="Server" value="Apache-Coyote/1.1"/>
      <rest:header name="Cache-Control" value="private"/>
   </rest:headers>
   <rest:payload contentType="text/xml">
      <l3-network xmlns="http://org.openecomp.aai.inventory/v6">
         <network-id>49c86598-f766-46f8-84f8-8d1c1b10f9b4</network-id>
         <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
         <network-type>CONTRAIL_EXTERNAL</network-type>
         <network-role>dmz_direct</network-role>
         <network-technology>contrail</network-technology>
         <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
         <network-role-instance>0</network-role-instance>
         <orchestration-status>pending-delete</orchestration-status>
	  	 <physical-network-name>networkName</physical-network-name>
	     <is-provider-network>false</is-provider-network>
	  	 <is-shared-network>true</is-shared-network>
	  	 <is-external-network>false</is-external-network>
         <subnets>
            <subnet>
               <subnet-id>57e9a1ff-d14f-4071-a828-b19ae98eb2fc</subnet-id>
               <gateway-address>107.239.52.1</gateway-address>
               <network-start-address>107.239.52.0</network-start-address>
               <cidr-mask>24</cidr-mask>
               <ip-version>4</ip-version>
               <orchestration-status>pending-delete</orchestration-status>
               <dhcp-enabled>true</dhcp-enabled>
  			   <subnet-name>subnetName</subnet-name>
               <relationship-list/>
            </subnet>
         </subnets>
         <relationship-list/>
      </l3-network>
   </rest:payload>
</rest:RESTResponse>"""
  
	  String queryNameAIIResponse =
		  """<rest:RESTResponse xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"
		                   statusCode="200">
		   <rest:headers>
		      <rest:header name="Transfer-Encoding" value="chunked"/>
		      <rest:header name="Date" value="Thu,10 Mar 2016 00:01:18 GMT"/>
		      <rest:header name="Expires" value="Thu,01 Jan 1970 00:00:00 UTC"/>
		      <rest:header name="X-AAI-TXID" value="mtcnjv9aaas03-20160310-00:01:18:502-132671"/>
		      <rest:header name="Content-Type" value="application/xml"/>
		      <rest:header name="Server" value="Apache-Coyote/1.1"/>
		      <rest:header name="Cache-Control" value="private"/>
		   </rest:headers>
		   <rest:payload contentType="text/xml">
		      <l3-network xmlns="http://org.openecomp.aai.inventory/v6">
		         <network-id>49c86598-f766-46f8-84f8-8d1c1b10f9b4</network-id>
		         <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
		         <network-type>CONTRAIL_EXTERNAL</network-type>
		         <network-role>dmz_direct</network-role>
		         <network-technology>contrail</network-technology>
		         <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
		         <network-role-instance>0</network-role-instance>
		         <orchestration-status>pending-delete</orchestration-status>
		         <subnets>
		            <subnet>
		               <subnet-id>57e9a1ff-d14f-4071-a828-b19ae98eb2fc</subnet-id>
		               <gateway-address>107.239.52.1</gateway-address>
		               <network-start-address>107.239.52.0</network-start-address>
		               <cidr-mask>24</cidr-mask>
		               <ip-version>4</ip-version>
		               <orchestration-status>pending-delete</orchestration-status>
		               <dhcp-enabled>true</dhcp-enabled>
		               <relationship-list/>
		            </subnet>
		         </subnets>
		         <relationship-list>
		            <relationship>
		               <related-to>vpn-binding</related-to>
		               <related-link>https://aai-app-e2e.test.com:8443/aai/v8/network/vpn-bindings/vpn-binding/85f015d0-2e32-4c30-96d2-87a1a27f8017/</related-link>
		               <relationship-data>
		                  <relationship-key>vpn-binding.vpn-id</relationship-key>
		                  <relationship-value>85f015d0-2e32-4c30-96d2-87a1a27f8017</relationship-value>
		               </relationship-data>
		            </relationship>
		            <relationship>
		               <related-to>vpn-binding</related-to>
		               <related-link>https://aai-app-e2e.test.com:8443/aai/v8/network/vpn-bindings/vpn-binding/c980a6ef-3b88-49f0-9751-dbad8608d0a6/</related-link>
		               <relationship-data>
		                  <relationship-key>vpn-binding.vpn-id</relationship-key>
		                  <relationship-value>c980a6ef-3b88-49f0-9751-dbad8608d0a6</relationship-value>
		               </relationship-data>
		            </relationship>
		            <relationship>
		               <related-to>tenant</related-to>
		               <related-link>https://aai-app-e2e.test.com:8443/aai/v8/cloud-infrastructure/tenants/tenant/7dd5365547234ee8937416c65507d266/</related-link>
		               <relationship-data>
		                  <relationship-key>tenant.tenant-id</relationship-key>
		                  <relationship-value>7dd5365547234ee8937416c65507d266</relationship-value>
		               </relationship-data>
		            </relationship>
		         </relationship-list>
		      </l3-network>
		   </rest:payload>
		</rest:RESTResponse>"""
  
		  String queryNameAIIResponseVpnNotPresent =
		  """<rest:RESTResponse xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"
		                   statusCode="200">
		   <rest:headers>
		      <rest:header name="Transfer-Encoding" value="chunked"/>
		      <rest:header name="Date" value="Thu,10 Mar 2016 00:01:18 GMT"/>
		      <rest:header name="Expires" value="Thu,01 Jan 1970 00:00:00 UTC"/>
		      <rest:header name="X-AAI-TXID" value="mtcnjv9aaas03-20160310-00:01:18:502-132671"/>
		      <rest:header name="Content-Type" value="application/xml"/>
		      <rest:header name="Server" value="Apache-Coyote/1.1"/>
		      <rest:header name="Cache-Control" value="private"/>
		   </rest:headers>
		   <rest:payload contentType="text/xml">
		      <l3-network xmlns="http://org.openecomp.aai.inventory/v6>
		         <network-id>49c86598-f766-46f8-84f8-8d1c1b10f9b4</network-id>
		         <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
		         <network-type>CONTRAIL_EXTERNAL</network-type>
		         <network-role>dmz_direct</network-role>
		         <network-technology>contrail</network-technology>
		         <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
		         <network-role-instance>0</network-role-instance>
		         <orchestration-status>pending-delete</orchestration-status>
		         <subnets>
		            <subnet>
		               <subnet-id>57e9a1ff-d14f-4071-a828-b19ae98eb2fc</subnet-id>
		               <gateway-address>107.239.52.1</gateway-address>
		               <network-start-address>107.239.52.0</network-start-address>
		               <cidr-mask>24</cidr-mask>
		               <ip-version>4</ip-version>
		               <orchestration-status>pending-delete</orchestration-status>
		               <dhcp-enabled>true</dhcp-enabled>
		               <relationship-list/>
		            </subnet>
		         </subnets>
		      </l3-network>
		   </rest:payload>
		</rest:RESTResponse>"""
		  
	  String aaiVpnResponseStub =
  """<rest:payload xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"
              xmlns="http://org.openecomp.aai.inventory/v8"
              contentType="text/xml">
   <vpn-binding>
      <global-route-target/>
   </vpn-binding>
</rest:payload>"""	
		  
	  String queryVpnBindingAAIResponse =
	   """<rest:RESTResponse xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"
                   statusCode="200">
   <rest:headers>
      <rest:header name="Transfer-Encoding" value="chunked"/>
      <rest:header name="Date" value="Mon,14 Mar 2016 20:53:33 GMT"/>
      <rest:header name="Expires" value="Thu,01 Jan 1970 00:00:00 UTC"/>
      <rest:header name="X-AAI-TXID"
                   value="mtcnjv9aaas01.mtcnj.aic.cip.com-20160314-20:53:33:487-134392"/>
      <rest:header name="Content-Type" value="application/xml"/>
      <rest:header name="Server" value="Apache-Coyote/1.1"/>
      <rest:header name="Cache-Control" value="private"/>
   </rest:headers>
   <rest:payload contentType="text/xml">
      <vpn-binding xmlns="http://org.openecomp.aai.inventory/v6">
         <vpn-id>9a7b327d9-287aa00-82c4b0-105757</vpn-id>
         <vpn-name>GN_EVPN_Test</vpn-name>
         <global-route-target>13979:105757</global-route-target>
         <relationship-list>
            <relationship>
               <related-to>l3-network</related-to>
               <related-link>https://aai-app-e2e.test.com:8443/aai/v3/network/l3-networks/l3-network/689ec39e-c5fc-4462-8db2-4f760763ad28/</related-link>
               <relationship-data>
                  <relationship-key>l3-network.network-id</relationship-key>
                  <relationship-value>689ec39e-c5fc-4462-8db2-4f760763ad28</relationship-value>
               </relationship-data>
            </relationship>
            <relationship>
               <related-to>l3-network</related-to>
               <related-link>https://aai-app-e2e.test.com:8443/aai/v3/network/l3-networks/l3-network/1a49396b-19b3-40a4-8792-aa2fbd0f0704/</related-link>
               <relationship-data>
                  <relationship-key>l3-network.network-id</relationship-key>
                  <relationship-value>1a49396b-19b3-40a4-8792-aa2fbd0f0704</relationship-value>
               </relationship-data>
            </relationship>
            <relationship>
               <related-to>l3-network</related-to>
               <related-link>https://aai-app-e2e.test.com:8443/aai/v3/network/l3-networks/l3-network/774f3329-3c83-4771-86c7-9e6207cd50fd/</related-link>
               <relationship-data>
                  <relationship-key>l3-network.network-id</relationship-key>
                  <relationship-value>774f3329-3c83-4771-86c7-9e6207cd50fd</relationship-value>
               </relationship-data>
            </relationship>
         </relationship-list>
      </vpn-binding>
   </rest:payload>
</rest:RESTResponse>"""
  
	   String updateDBRequest_Active =
	   """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:ns="http://org.openecomp.mso/requestsdb">
   <soapenv:Header/>
   <soapenv:Body>
      <ns:updateInfraRequest>
         <requestId>88f65519-9a38-4c4b-8445-9eb4a5a5af56</requestId>
         <lastModifiedBy>BPMN</lastModifiedBy>
         <statusMessage>Network MNS-25180-L-01-dmz_direct_net_1 already exists. Silent success.</statusMessage>
         <responseBody/>
         <requestStatus>COMPLETED</requestStatus>
         <progress>100</progress>
         <vnfOutputs>&lt;network-id&gt;49c86598-f766-46f8-84f8-8d1c1b10f9b4&lt;/network-id&gt;&lt;network-name&gt;MNS-25180-L-01-dmz_direct_net_1&lt;/network-names&gt;</vnfOutputs>
         <networkId>49c86598-f766-46f8-84f8-8d1c1b10f9b4</networkId>
      </ns:updateInfraRequest>
   </soapenv:Body>
</soapenv:Envelope>"""
  
	  String updateDBRequest =
	   """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:ns="http://org.openecomp.mso/requestsdb">
   <soapenv:Header/>
   <soapenv:Body>
      <ns:updateInfraRequest>
         <requestId>88f65519-9a38-4c4b-8445-9eb4a5a5af56</requestId>
         <lastModifiedBy>BPMN</lastModifiedBy>
         <statusMessage>Network successfully updated.</statusMessage>
         <responseBody/>
         <requestStatus>COMPLETED</requestStatus>
         <progress>100</progress>
         <vnfOutputs>&lt;network-id&gt;&lt;/network-id&gt;&lt;network-name&gt;&lt;/network-names&gt;</vnfOutputs>
         <networkId/>
      </ns:updateInfraRequest>
   </soapenv:Body>
</soapenv:Envelope>"""	
  
	  String updateDBRequestError =
	  """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
						<soapenv:Header/>
						<soapenv:Body>
							<ns:updateInfraRequest xmlns:ns="http://org.openecomp.mso/requestsdb">
								<requestId>88f65519-9a38-4c4b-8445-9eb4a5a5af56</requestId>
								<lastModifiedBy>BPMN</lastModifiedBy>
								<statusMessage>Received error from SDN-C: No availability zone available</statusMessage>
								<responseBody></responseBody>
								<requestStatus>FAILED</requestStatus>
								<vnfOutputs>&lt;network-id&gt;&lt;/network-id&gt;&lt;network-name&gt;&lt;/network-names&gt;</vnfOutputs>
							</ns:updateInfraRequest>
					   	</soapenv:Body>
					   </soapenv:Envelope>"""
  
		 String updateDBRequestError01 =
	  """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
						<soapenv:Header/>
						<soapenv:Body>
							<ns:updateInfraRequest xmlns:ns="http://org.openecomp.mso/requestsdb">
								<requestId>88f65519-9a38-4c4b-8445-9eb4a5a5af56</requestId>
								<lastModifiedBy>BPMN</lastModifiedBy>
								<statusMessage>Received error unexpectedly from SDN-C.</statusMessage>
								<responseBody></responseBody>
								<requestStatus>FAILED</requestStatus>
								<vnfOutputs>&lt;network-id&gt;&lt;/network-id&gt;&lt;network-name&gt;&lt;/network-names&gt;</vnfOutputs>
							</ns:updateInfraRequest>
					   	</soapenv:Body>
					   </soapenv:Envelope>"""
						 
	  String updateDBRequest_Outputs =
  """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:ns="http://org.openecomp.mso/requestsdb">
   <soapenv:Header/>
   <soapenv:Body>
      <ns:updateInfraRequest>
         <requestId>88f65519-9a38-4c4b-8445-9eb4a5a5af56</requestId>
         <lastModifiedBy>BPMN</lastModifiedBy>
         <statusMessage>Network successfully updated.</statusMessage>
         <responseBody/>
         <requestStatus>COMPLETED</requestStatus>
         <progress>100</progress>
         <vnfOutputs>&lt;network-id&gt;49c86598-f766-46f8-84f8-8d1c1b10f9b4&lt;/network-id&gt;&lt;network-name&gt;MNS-25180-L-01-dmz_direct_net_1&lt;/network-names&gt;</vnfOutputs>
         <networkId>49c86598-f766-46f8-84f8-8d1c1b10f9b4</networkId>
      </ns:updateInfraRequest>
   </soapenv:Body>
</soapenv:Envelope>"""	
  
	  String updateNetworkRequest =
	  """<updateNetworkRequest>
   <cloudSiteId>RDM2WAGPLCP</cloudSiteId>
   <tenantId>7dd5365547234ee8937416c65507d266</tenantId>
   <networkId>49c86598-f766-46f8-84f8-8d1c1b10f9b4</networkId>
   <networkStackId>ST_2Bindings_6006/55288ef0-595c-47d3-819e-cf93aaac6326</networkStackId>
   <networkName>MNS-25180-L-01-dmz_direct_net_1</networkName>
   <networkType>CONTRAIL_EXTERNAL</networkType>
   <modelCustomizationUuid/>
   <networkTypeVersion/>
   <networkTechnology>CONTRAIL</networkTechnology>
   <providerVlanNetwork>
      <physicalNetworkName>networkName</physicalNetworkName>
      <vlans>414,415</vlans>
   </providerVlanNetwork>
   <contrailNetwork>
      <shared>true</shared>
      <external>false</external>
      <routeTargets>13979:105757</routeTargets>
      <routeTargets>13979:105757</routeTargets>
      <policyFqdns>GN_EVPN_Test</policyFqdns>
      <routeTableFqdns>refFQDN1</routeTableFqdns>
      <routeTableFqdns>refFQDN2</routeTableFqdns>
   </contrailNetwork>
   <skipAAI>true</skipAAI>
   <backout>true</backout>
   <failIfExists>false</failIfExists>
   <networkParams>
      <dhcp-enabled>true</dhcp-enabled>
      <serviceId>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</serviceId>
      <cidr-mask>true</cidr-mask>
      <backoutOnFailure>true</backoutOnFailure>
      <gateway-address>10.10.125.1</gateway-address>
   </networkParams>
   <msoRequest>
      <requestId>88f65519-9a38-4c4b-8445-9eb4a5a5af56</requestId>
      <serviceInstanceId>f70e927b-6087-4974-9ef8-c5e4d5847ca4</serviceInstanceId>
   </msoRequest>
   <messageId>messageId_generated</messageId>
   <notificationUrl/>
</updateNetworkRequest>"""
	  
   
  String updateNetworkRequest_noPhysicalName =
  """<updateNetworkRequest>
   <cloudSiteId>RDM2WAGPLCP</cloudSiteId>
   <tenantId>7dd5365547234ee8937416c65507d266</tenantId>
   <networkId>49c86598-f766-46f8-84f8-8d1c1b10f9b4</networkId>
   <networkStackId>ST_2Bindings_6006/55288ef0-595c-47d3-819e-cf93aaac6326</networkStackId>
   <networkName>MNS-25180-L-01-dmz_direct_net_1</networkName>
   <networkType>CONTRAIL_EXTERNAL</networkType>
   <modelCustomizationUuid/>
   <networkTypeVersion/>
   <networkTechnology>CONTRAIL</networkTechnology>
   <providerVlanNetwork>
      <physicalNetworkName>networkName</physicalNetworkName>
      <vlans>414,415</vlans>
   </providerVlanNetwork>
   <contrailNetwork>
      <shared>true</shared>
      <external>false</external>
      <routeTargets>13979:105757</routeTargets>
      <routeTargets>13979:105757</routeTargets>
      <policyFqdns>GN_EVPN_Test</policyFqdns>
   </contrailNetwork>
   <skipAAI>true</skipAAI>
   <backout>true</backout>
   <failIfExists>false</failIfExists>
   <networkParams>
      <dhcp-enabled>true</dhcp-enabled>
      <serviceId>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</serviceId>
      <cidr-mask>true</cidr-mask>
      <backoutOnFailure>true</backoutOnFailure>
      <gateway-address>10.10.125.1</gateway-address>
   </networkParams>
   <msoRequest>
      <requestId>88f65519-9a38-4c4b-8445-9eb4a5a5af56</requestId>
      <serviceInstanceId>null</serviceInstanceId>
   </msoRequest>
   <messageId>messageId_generated</messageId>
   <notificationUrl/>
</updateNetworkRequest>"""
  
	  String updateNetworkResponseREST =
  """<ns2:updateNetworkContrailResponse xmlns:ns2="http://org.openecomp.mso/network">
	<networkId>MNS-25180-L-01-dmz_direct_net_1/2c88a3a9-69b9-43a7-ada6-1aca577c3641</networkId>
	<neutronNetworkId>c4f4e878-cde0-4b15-ae9a-bda857759cea</neutronNetworkId>
    <networkFqdn>default-domain:MSOTest:GN_EVPN_direct_net_0_ST1</networkFqdn>
	<networkStackId></networkStackId>
	<subnetMap>
		<entry>
			<key>57e9a1ff-d14f-4071-a828-b19ae98eb2fc</key>
			<value>bd8e87c6-f4e2-41b8-b0bc-9596aa00cd73</value>
		</entry>
		<entry>
			<key>57e9a1ff-d14f-4071-a828-b19ae98eb2fc</key>
			<value>bd8e87c6-f4e2-41b8-b0bc-9596aa00cd73</value>
		</entry>
	</subnetMap>
	<rollback>
		<networkId>MNS-25180-L-01-dmz_direct_net_1/2c88a3a9-69b9-43a7-ada6-1aca577c3641</networkId>
		<neutronNetworkId>c4f4e878-cde0-4b15-ae9a-bda857759cea</neutronNetworkId>
		<networkStackId></networkStackId>
		<networkType>CONTRAIL_EXTERNAL</networkType>
		<networkUpdated>true</networkUpdated>
		<tenantId>7dd5365547234ee8937416c65507d266</tenantId>
		<cloudSiteId>RDM2WAGPLCP</cloudSiteId>
		<msoRequest>
			<requestId>1ef47428-cade-45bd-a103-0751e8b2deb0</requestId>
			<serviceInstanceId></serviceInstanceId>
		</msoRequest>
	</rollback>
	<messageId>messageId_generated</messageId>
</ns2:updateNetworkContrailResponse>"""
  
	  String updateRollbackNetworkRequest =
	  """<NetworkAdapter:rollbackNetwork xmlns:NetworkAdapter="http://org.openecomp.mso/network">
   <rollback>
      <networkId>MNS-25180-L-01-dmz_direct_net_1/2c88a3a9-69b9-43a7-ada6-1aca577c3641</networkId>
      <neutronNetworkId>c4f4e878-cde0-4b15-ae9a-bda857759cea</neutronNetworkId>
      <networkStackId/>
      <networkType>CONTRAIL_EXTERNAL</networkType>
      <networkUpdated>true</networkUpdated>
      <tenantId>7dd5365547234ee8937416c65507d266</tenantId>
      <cloudSiteId>RDM2WAGPLCP</cloudSiteId>
      <msoRequest>
         <requestId>1ef47428-cade-45bd-a103-0751e8b2deb0</requestId>
         <serviceInstanceId/>
      </msoRequest>
   </rollback>
</NetworkAdapter:rollbackNetwork>"""	
	  
	  String updateNetworkResponse =
	  """<ns2:updateNetworkContrailResponse xmlns:ns2="http://org.openecomp.mso/network"
                                    xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
   <networkId>MNS-25180-L-01-dmz_direct_net_1/2c88a3a9-69b9-43a7-ada6-1aca577c3641</networkId>
   <neutronNetworkId>c4f4e878-cde0-4b15-ae9a-bda857759cea</neutronNetworkId>
   <networkStackId>MNS-25180-L-01-dmz_direct_net_1/2c88a3a9-69b9-43a7-ada6-1aca577c3641</networkStackId>
	<networkFqdn>default-domain:MSOTest:GN_EVPN_direct_net_0_ST1</networkFqdn>
   <subnetIdMap>
      <entry>
         <key>57e9a1ff-d14f-4071-a828-b19ae98eb2fc</key>
         <value>bd8e87c6-f4e2-41b8-b0bc-9596aa00cd73</value>
      </entry>
   </subnetIdMap>
   <rollback>
      <cloudId>RDM2WAGPLCP</cloudId>
      <msoRequest>
         <requestId>1ef47428-cade-45bd-a103-0751e8b2deb0</requestId>
         <serviceInstanceId/>
      </msoRequest>
      <networkUpdated>true</networkUpdated>
      <networkId>MNS-25180-L-01-dmz_direct_net_1/2c88a3a9-69b9-43a7-ada6-1aca577c3641</networkId>
      <networkType>CONTRAIL_EXTERNAL</networkType>
      <networkUpdated>false</networkUpdated>
      <neutronNetworkId>c4f4e878-cde0-4b15-ae9a-bda857759cea</neutronNetworkId>
      <tenantId>7dd5365547234ee8937416c65507d266</tenantId>
   </rollback>
</ns2:updateNetworkContrailResponse>"""
  
	  String updateContrailAAIPayloadRequest =
  """<l3-network xmlns="http://org.openecomp.aai.inventory/v8">
   <network-id>49c86598-f766-46f8-84f8-8d1c1b10f9b4</network-id>
   <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
   <network-type>CONTRAIL_EXTERNAL</network-type>
   <network-role>dmz_direct</network-role>
   <network-technology>contrail</network-technology>
   <neutron-network-id>c4f4e878-cde0-4b15-ae9a-bda857759cea</neutron-network-id>
   <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
   <network-role-instance>0</network-role-instance>
   <resource-version>l3-version</resource-version>
   <orchestration-status>active</orchestration-status>
   <heat-stack-id>ST_2Bindings_6006/55288ef0-595c-47d3-819e-cf93aaac6326</heat-stack-id>
   <contrail-network-fqdn>default-domain:MSOTest:GN_EVPN_direct_net_0_ST1</contrail-network-fqdn>
   <physical-network-name>networkName</physical-network-name>
   <is-provider-network>false</is-provider-network>
   <is-shared-network>true</is-shared-network>
   <is-external-network>false</is-external-network>
   <subnets>
      <subnet>
         <subnet-id>57e9a1ff-d14f-4071-a828-b19ae98eb2fc</subnet-id>
         <neutron-subnet-id>bd8e87c6-f4e2-41b8-b0bc-9596aa00cd73</neutron-subnet-id>
         <gateway-address>107.239.52.1</gateway-address>
         <network-start-address>107.239.52.0</network-start-address>
         <cidr-mask>24</cidr-mask>
         <ip-version>4</ip-version>
         <orchestration-status>active</orchestration-status>
         <dhcp-enabled>true</dhcp-enabled>
         <subnet-name>subnetName</subnet-name>
      </subnet>
      <subnet>
         <subnet-id>57e9a1ff-d14f-4071-a828-b19ae98eb2fc</subnet-id>
         <neutron-subnet-id>bd8e87c6-f4e2-41b8-b0bc-9596aa00cd73</neutron-subnet-id>
         <gateway-address>107.239.52.1</gateway-address>
         <network-start-address>107.239.52.0</network-start-address>
         <cidr-mask>24</cidr-mask>
         <ip-version>4</ip-version>
         <orchestration-status>active</orchestration-status>
         <dhcp-enabled>true</dhcp-enabled>
         <subnet-name>subnetName</subnet-name>
      </subnet>
   </subnets>
   <segmentation-assignments>
      <segmentation-id>414</segmentation-id>
   </segmentation-assignments>
   <segmentation-assignments>
      <segmentation-id>415</segmentation-id>
   </segmentation-assignments>
   <ctag-assignments>
      <ctag-assignment>
         <vlan-id-inner>inner</vlan-id-inner>
         <resource-version>ctag-version</resource-version>
         <relationship-list>
            <relationship>
               <related-to>tenant</related-to>
               <related-link>https://aai-ext1.test.com:8443/aai/v8/cloud-infrastructure/tenants/tenant/897deadc2b954a6bac6d3c197fb3525e/</related-link>
               <relationship-data>
                  <relationship-key>tenant.tenant-id</relationship-key>
                  <relationship-value>897deadc2b954a6bac6d3c197fb3525e</relationship-value>
               </relationship-data>
               <related-to-property>
                  <property-key>tenant.tenant-name</property-key>
                  <property-value>MSOTest1</property-value>
               </related-to-property>
            </relationship>
            <relationship>
               <related-to>vpn-binding</related-to>
               <related-link>https://aai-ext1.test.com:8443/aai/v8/network/vpn-bindings/vpn-binding/a290b841-f672-44dd-b9cd-6f8c20d7d8c8/</related-link>
               <relationship-data>
                  <relationship-key>vpn-binding.vpn-id</relationship-key>
                  <relationship-value>a290b841-f672-44dd-b9cd-6f8c20d7d8c8</relationship-value>
               </relationship-data>
               <related-to-property>
                  <property-key>vpn-binding.vpn-name</property-key>
                  <property-value>oam_protected_net_6_MTN5_msotest2</property-value>
               </related-to-property>
            </relationship>
            <relationship>
               <related-to>vpn-binding</related-to>
               <related-link>https://aai-ext1.test.com:8443/aai/v8/network/vpn-bindings/vpn-binding/24a4b507-853a-4a38-99aa-05fcc54be24d/</related-link>
               <relationship-data>
                  <relationship-key>vpn-binding.vpn-id</relationship-key>
                  <relationship-value>24a4b507-853a-4a38-99aa-05fcc54be24d</relationship-value>
               </relationship-data>
               <related-to-property>
                  <property-key>vpn-binding.vpn-name</property-key>
                  <property-value>oam_protected_net_6_MTN5_msotest1</property-value>
               </related-to-property>
            </relationship>
         </relationship-list>
      </ctag-assignment>
   </ctag-assignments>
   <relationship-list>
      <relationship>
         <related-to>vpn-binding</related-to>
         <related-link>https://aai-app-e2e.test.com:8443/aai/v8/network/vpn-bindings/vpn-binding/85f015d0-2e32-4c30-96d2-87a1a27f8017/</related-link>
         <relationship-data>
            <relationship-key>vpn-binding.vpn-id</relationship-key>
            <relationship-value>85f015d0-2e32-4c30-96d2-87a1a27f8017</relationship-value>
         </relationship-data>
      </relationship>
      <relationship>
         <related-to>vpn-binding</related-to>
         <related-link>https://aai-app-e2e.test.com:8443/aai/v8/network/vpn-bindings/vpn-binding/c980a6ef-3b88-49f0-9751-dbad8608d0a6/</related-link>
         <relationship-data>
            <relationship-key>vpn-binding.vpn-id</relationship-key>
            <relationship-value>c980a6ef-3b88-49f0-9751-dbad8608d0a6</relationship-value>
         </relationship-data>
      </relationship>
      <relationship>
         <related-to>tenant</related-to>
         <related-link>https://aai-app-e2e.test.com:8443/aai/v8/cloud-infrastructure/tenants/tenant/7dd5365547234ee8937416c65507d266/</related-link>
         <relationship-data>
            <relationship-key>tenant.tenant-id</relationship-key>
            <relationship-value>7dd5365547234ee8937416c65507d266</relationship-value>
         </relationship-data>
      </relationship>
      <relationship>
         <related-to>network-policy</related-to>
         <related-link>https://aai-app-e2e.test.com:8443/aai/v8/network/network-policies/network-policy/cee6d136-e378-4678-a024-2cd15f0ee0cg</related-link>
         <relationship-data>
            <relationship-key>network-policy.network-policy-id</relationship-key>
            <relationship-value>cee6d136-e378-4678-a024-2cd15f0ee0cg</relationship-value>
         </relationship-data>
      </relationship>
      <relationship>
         <related-to>route-table-reference</related-to>
         <related-link>https://aai-app-e2e.test.com:8443/aai/v8/network/route-table-references/route-table-reference/refFQDN1</related-link>
         <relationship-data>
            <relationship-key>route-table-reference.route-table-reference-id</relationship-key>
            <relationship-value>cee6d136-e378-4678-a024-2cd15f0ee0hi</relationship-value>
         </relationship-data>
      </relationship>
      <relationship>
         <related-to>route-table-reference</related-to>
         <related-link>https://aai-app-e2e.test.com:8443/aai/v8/network/route-table-references/route-table-reference/refFQDN2</related-link>
         <relationship-data>
            <relationship-key>route-table-reference.route-table-reference-id</relationship-key>
            <relationship-value>cee6d136-e378-4678-a024-2cd15f0ee0hi</relationship-value>
         </relationship-data>
      </relationship>
   </relationship-list>
</l3-network>"""
  
	  String updateContrailAAIResponse =
  """<rest:RESTResponse xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"
                   statusCode="200">
   <rest:headers>
      <rest:header name="Date" value="Thu,10 Mar 2016 00:01:18 GMT"/>
      <rest:header name="Content-Length" value="0"/>
      <rest:header name="Expires" value="Thu,01 Jan 1970 00:00:00 UTC"/>
      <rest:header name="X-AAI-TXID" value="mtcnjv9aaas03-20160310-00:01:18:551-132672"/>
      <rest:header name="Server" value="Apache-Coyote/1.1"/>
      <rest:header name="Cache-Control" value="private"/>
   </rest:headers>
</rest:RESTResponse>"""
	  
	  String updateNetworkErrorResponse =
	  """<updateNetworkError>
		 <messageId>680bd458-5ec1-4a16-b77c-509022e53450</messageId><category>INTERNAL</category>
		 <message>400 Bad Request: The server could not comply with the request since it is either malformed or otherwise incorrect., error.type=StackValidationFailed, error.message=Property error: : resources.network.properties: : Unknown Property network_ipam_refs_data</message>
		 <rolledBack>true</rolledBack>
	   </updateNetworkError>"""
  
			
  String networkException500 =
  """<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"><soap:Body><soap:Fault><faultcode>soap:VersionMismatch</faultcode><faultstring>"http://org.openecomp.mso/network", the namespace on the "updateNetworkContrail" element, is not a valid SOAP version.</faultstring></soap:Fault></soap:Body></soap:Envelope>"""
				  
	String aaiResponse = 
   """<rest:RESTResponse xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"
					 statusCode="200">
	 <rest:headers>
		<rest:header name="Transfer-Encoding" value="chunked"/>
		<rest:header name="Date" value="Sat,30 Jan 2016 20:09:24 GMT"/>
		<rest:header name="Expires" value="Thu,01 Jan 1970 00:00:00 UTC"/>
		<rest:header name="X-AAI-TXID"
					 value="mtcnjv9aaas01.mtcnj.aic.cip.com-20160130-20:09:24:814-165843"/>
		<rest:header name="Content-Type" value="application/xml"/>
		<rest:header name="Server" value="Apache-Coyote/1.1"/>
		<rest:header name="Cache-Control" value="private"/>
	 </rest:headers>
	 <rest:payload contentType="text/xml">
		<l3-network xmlns="http://org.openecomp.aai.inventory/v3">
		   <network-id>bdc5efe8-404a-409b-85f6-0dcc9eebae30</network-id>
		   <network-name>HSL_direct_net_2</network-name>
		   <network-type>CONTRAIL_BASIC</network-type>
		   <network-role>HSL_direct</network-role>
		   <network-technology>contrail</network-technology>
		   <neutron-network-id>8bbd3edf-b835-4610-96a2-a5cafa029042</neutron-network-id>
		   <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
		   <orchestration-status>active</orchestration-status>
		   <heat-stack-id>HSL_direct_net_2/57594a56-1c92-4a38-9caa-641c1fa3d4b6</heat-stack-id>
		   <subnets>
			  <subnet>
				 <subnet-id>ea5f2a2c-604f-47ff-a9c5-253ee4f0ef0a</subnet-id>
				 <neutron-subnet-id>5a77fdc2-7789-4649-a1b9-6eaf1db1813a</neutron-subnet-id>
				 <gateway-address>172.16.34.1</gateway-address>
				 <network-start-address>172.16.34.0</network-start-address>
				 <cidr-mask>28</cidr-mask>
				 <ip-version>4</ip-version>
				 <orchestration-status>active</orchestration-status>
				 <dhcp-enabled>true</dhcp-enabled>
				 <relationship-list/>
			  </subnet>
		   </subnets>
		   <relationship-list>
			  <relationship>
				 <related-to>tenant</related-to>
				 <related-link>https://aai-app-e2e.test.com:8443/aai/v3/cloud-infrastructure/tenants/tenant/e81d842d3e8b45c5a59f57cd76af3aaf/</related-link>
				 <relationship-data>
					<relationship-key>tenant.tenant-id</relationship-key>
					<relationship-value>e81d842d3e8b45c5a59f57cd76af3aaf</relationship-value>
				 </relationship-data>
			  </relationship>
		   </relationship-list>
		</l3-network>
	 </rest:payload>
  </rest:RESTResponse>"""

	String changeAssignSDNCRequest = 
    """<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://openecomp.com/mso/workflow/schema/v1"
                                  xmlns:ns5="http://openecomp.com/mso/request/types/v1"
                                  xmlns:sdncadapter="http://domain2.openecomp.com/workflow/sdnc/adapter/schema/v1">
   <sdncadapter:RequestHeader>
      <sdncadapter:RequestId>88f65519-9a38-4c4b-8445-9eb4a5a5af56</sdncadapter:RequestId>
      <sdncadapter:SvcInstanceId>f70e927b-6087-4974-9ef8-c5e4d5847ca4</sdncadapter:SvcInstanceId>
      <sdncadapter:SvcAction>changeassign</sdncadapter:SvcAction>
      <sdncadapter:SvcOperation>network-topology-operation</sdncadapter:SvcOperation>
      <sdncadapter:CallbackUrl>sdncCallback</sdncadapter:CallbackUrl>
   </sdncadapter:RequestHeader>
   <aetgt:SDNCRequestData>
      <request-information>
         <request-id>88f65519-9a38-4c4b-8445-9eb4a5a5af56</request-id>
         <request-action>NetworkActivateRequest</request-action>
         <source>VID</source>
         <notification-url/>
         <order-number/>
         <order-version/>
      </request-information>
      <service-information>
         <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
         <service-type>MSO-dev-service-type</service-type>
         <service-instance-id>f70e927b-6087-4974-9ef8-c5e4d5847ca4</service-instance-id>
         <subscriber-name>MSO_1610_dev</subscriber-name>
      </service-information>
      <network-request-information>
         <network-id>49c86598-f766-46f8-84f8-8d1c1b10f9b4</network-id>
         <network-type>CONTRAIL_EXTERNAL</network-type>
         <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
         <tenant>7dd5365547234ee8937416c65507d266</tenant>
         <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
      </network-request-information>
   </aetgt:SDNCRequestData>
</aetgt:SDNCAdapterWorkflowRequest>"""

String assignResponse =
"""<sdncadapterworkflow:SDNCAdapterWorkflowResponse xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1" xmlns:tag0="http://org.openecomp/workflow/sdnc/adapter/schema/v1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"> <sdncadapterworkflow:response-data> <tag0:CallbackHeader> <tag0:RequestId>006927ca-f5a3-47fd-880c-dfcbcd81a093</tag0:RequestId> <tag0:ResponseCode>200</tag0:ResponseCode> <tag0:ResponseMessage>OK</tag0:ResponseMessage> </tag0:CallbackHeader> <tag0:RequestData xsi:type="xs:string"><output xmlns="org:openecomp:sdnctltl:vnf"><response-code>200</response-code><svc-request-id>006927ca-f5a3-47fd-880c-dfcbcd81a093</svc-request-id><ack-final-indicator>Y</ack-final-indicator><service-information><subscriber-name>notsurewecare</subscriber-name><service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id><service-instance-id>GN_EVPN_direct_net_0_ST_noGW</service-instance-id></service-information><network-information><network-id>8abc633a-810b-4ca5-8b3a-09511d13a2ce</network-id></network-information></output></tag0:RequestData> </sdncadapterworkflow:response-data> </sdncadapterworkflow:SDNCAdapterWorkflowResponse>"""
			  
  String sdncRollbackRequest =
			  """<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://openecomp.com/mso/workflow/schema/v1"
                                  xmlns:ns5="http://openecomp.com/mso/request/types/v1"
                                  xmlns:sdncadapter="http://domain2.openecomp.com/workflow/sdnc/adapter/schema/v1">
   <sdncadapter:RequestHeader>
      <sdncadapter:RequestId>88f65519-9a38-4c4b-8445-9eb4a5a5af56</sdncadapter:RequestId>
      <sdncadapter:SvcInstanceId>f70e927b-6087-4974-9ef8-c5e4d5847ca4</sdncadapter:SvcInstanceId>
      <sdncadapter:SvcAction>rollback</sdncadapter:SvcAction>
      <sdncadapter:SvcOperation>network-topology-operation</sdncadapter:SvcOperation>
      <sdncadapter:CallbackUrl>sdncCallback</sdncadapter:CallbackUrl>
   </sdncadapter:RequestHeader>
   <aetgt:SDNCRequestData>
      <request-information>
         <request-id>88f65519-9a38-4c4b-8445-9eb4a5a5af56</request-id>
         <request-action>NetworkActivateRequest</request-action>
         <source>VID</source>
         <notification-url/>
         <order-number/>
         <order-version/>
      </request-information>
      <service-information>
         <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
         <service-type>MSO-dev-service-type</service-type>
         <service-instance-id>f70e927b-6087-4974-9ef8-c5e4d5847ca4</service-instance-id>
         <subscriber-name>MSO_1610_dev</subscriber-name>
      </service-information>
      <network-request-information>
         <network-id>8abc633a-810b-4ca5-8b3a-09511d13a2ce</network-id>
         <network-type>CONTRAIL_EXTERNAL</network-type>
         <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
         <tenant>7dd5365547234ee8937416c65507d266</tenant>
         <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
      </network-request-information>
   </aetgt:SDNCRequestData>
</aetgt:SDNCAdapterWorkflowRequest>"""
						 
	   String sdncAdapterWorkflowResponse =
	  """<aetgt:SDNCAdapterWorkflowResponse xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
                                   xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1">
   <sdncadapterworkflow:response-data>
<tag0:CallbackHeader xmlns:tag0="http://org.openecomp/workflow/sdnc/adapter/schema/v1">
   <tag0:RequestId>745b1b50-e39e-4685-9cc8-c71f0bde8bf0</tag0:RequestId>
   <tag0:ResponseCode>200</tag0:ResponseCode>
   <tag0:ResponseMessage>OK</tag0:ResponseMessage>
</tag0:CallbackHeader>
   <tag0:RequestData xmlns:tag0="http://org.openecomp/workflow/sdnc/adapter/schema/v1"
                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xsi:type="xs:string">&lt;output xmlns="org:openecomp:sdnctl:vnf"&gt;&lt;svc-request-id&gt;00703dc8-71ff-442d-a4a8-3adc5beef6a9&lt;/svc-request-id&gt;&lt;response-code&gt;200&lt;/response-code&gt;&lt;ack-final-indicator&gt;Y&lt;/ack-final-indicator&gt;&lt;network-information&gt;&lt;network-id&gt;49c86598-f766-46f8-84f8-8d1c1b10f9b4&lt;/network-id&gt;&lt;/network-information&gt;&lt;service-information&gt;&lt;service-type&gt;a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb&lt;/service-type&gt;&lt;service-instance-id&gt;MNS-25180-L-01-dmz_direct_net_1&lt;/service-instance-id&gt;&lt;subscriber-name&gt;notsurewecare&lt;/subscriber-name&gt;&lt;/service-information&gt;&lt;/output&gt;</tag0:RequestData>
   </sdncadapterworkflow:response-data>
</aetgt:SDNCAdapterWorkflowResponse>"""
					  
  
	  String sdncAdapterWorkflowResponse_Error =
	  """<sdncadapterworkflow:SDNCAdapterWorkflowResponse xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
                                                 xmlns:tag0="http://org.openecomp/workflow/sdnc/adapter/schema/v1"
                                                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                                                 xmlns="org:openecomp:sdnctl:vnf">
   <sdncadapterworkflow:response-data>
      <tag0:RequestData xsi:type="xs:string">
         <output>
            <response-code>400</response-code>
            <response-message>Error writing to l3-netework</response-message>
            <ack-final-indicator>Y</ack-final-indicator>
            <svc-request-id>c79240d8-34b5-4853-af69-2021928dba00</svc-request-id>
         </output>
      </tag0:RequestData>
   </sdncadapterworkflow:response-data>
</sdncadapterworkflow:SDNCAdapterWorkflowResponse>"""
  
	  String expected_sdncAdapterWorkflowResponse_Error =
  """<sdncadapterworkflow:SDNCAdapterWorkflowResponse xmlns="org:openecomp:sdnctl:vnf"
                                                 xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
                                                 xmlns:tag0="http://org.openecomp/workflow/sdnc/adapter/schema/v1"
                                                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <sdncadapterworkflow:response-data>
      <tag0:RequestData xsi:type="xs:string">
         <output>
            <response-code>400</response-code>
            <response-message>Error writing to l3-netework</response-message>
            <ack-final-indicator>Y</ack-final-indicator>
            <svc-request-id>c79240d8-34b5-4853-af69-2021928dba00</svc-request-id>
         </output>
      </tag0:RequestData>
   </sdncadapterworkflow:response-data>
</sdncadapterworkflow:SDNCAdapterWorkflowResponse>"""
  
	  String sdncAdapterWorkflowFormattedResponse =
	  """<aetgt:SDNCAdapterWorkflowResponse xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
                                   xmlns="org:openecomp:sdnctl:vnf">
   <aetgt:response-data>
      <output>
         <svc-request-id>00703dc8-71ff-442d-a4a8-3adc5beef6a9</svc-request-id>
         <response-code>200</response-code>
         <ack-final-indicator>Y</ack-final-indicator>
         <network-information>
            <network-id>49c86598-f766-46f8-84f8-8d1c1b10f9b4</network-id>
         </network-information>
         <service-information>
            <service-type>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-type>
            <service-instance-id>f70e927b-6087-4974-9ef8-c5e4d5847ca4</service-instance-id>
            <subscriber-name>notsurewecare</subscriber-name>
         </service-information>
      </output>
   </aetgt:response-data>
</aetgt:SDNCAdapterWorkflowResponse>"""   
  
	  String falloutHandlerRequest =
	  """<aetgt:FalloutHandlerRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
					                             xmlns:ns="http://org.openecomp/mso/request/types/v1"
					                             xmlns:wfsch="http://org.openecomp/mso/workflow/schema/v1">
					   <request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
					      <request-id>b69c9054-da09-4a2c-adf5-51042b62bfac</request-id>
					      <action>UPDATE</action>
					      <source>PORTAL</source>
					   </request-info>
						<aetgt:WorkflowException xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1">
							<aetgt:ErrorMessage>Received error from SDN-C: No availability zone available.</aetgt:ErrorMessage>
							<aetgt:ErrorCode>5300</aetgt:ErrorCode>
						</aetgt:WorkflowException>
					</aetgt:FalloutHandlerRequest>"""
  
	  String falloutHandlerRequestObject =
	  """<aetgt:FalloutHandlerRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
					                             xmlns:ns="http://org.openecomp/mso/request/types/v1"
					                             xmlns:wfsch="http://org.openecomp/mso/workflow/schema/v1">
					   <request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
					      <request-id>b69c9054-da09-4a2c-adf5-51042b62bfac</request-id>
					      <action>UPDATE</action>
					      <source>VID</source>
					   </request-info>
						<aetgt:WorkflowException xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1">
							<aetgt:ErrorMessage>Received error from SDN-C: No availability zone available</aetgt:ErrorMessage>
							<aetgt:ErrorCode>7000</aetgt:ErrorCode>
						</aetgt:WorkflowException>
					</aetgt:FalloutHandlerRequest>"""
					  
					  
	  String falloutHandlerRequest_Scenario01 =
  """<aetgt:FalloutHandlerRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
					                             xmlns:ns="http://org.openecomp/mso/request/types/v1"
					                             xmlns:wfsch="http://org.openecomp/mso/workflow/schema/v1">
					   <request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
					      <request-id>b69c9054-da09-4a2c-adf5-51042b62bfac</request-id>
					      <action>UPDATE</action>
					      <source>VID</source>
					   </request-info>
						<aetgt:WorkflowException xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1">
							<aetgt:ErrorMessage>Unexpected Response from AAI - 400</aetgt:ErrorMessage>
							<aetgt:ErrorCode>7020</aetgt:ErrorCode>
						</aetgt:WorkflowException>
					</aetgt:FalloutHandlerRequest>"""
  
	  String completeMsoProcessRequest =
	  """<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
                            xmlns:ns="http://org.openecomp/mso/request/types/v1"
                            xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
   <request-info>
      <request-id>88f65519-9a38-4c4b-8445-9eb4a5a5af56</request-id>
      <action>UPDATE</action>
      <source>PORTAL</source>
   </request-info>
   <aetgt:mso-bpel-name>BPMN Network action: UPDATE</aetgt:mso-bpel-name>
</aetgt:MsoCompletionRequest>"""

// - - - - - - - -


	    @Before
		public void init()
		{
			MockitoAnnotations.initMocks(this)
			
		}
		
		public void initializeVariables (Execution mockExecution) {
			
			verify(mockExecution).setVariable("UPDNETI_messageId", "")
			verify(mockExecution).setVariable("BasicAuthHeaderValuePO", "")
			verify(mockExecution).setVariable("BasicAuthHeaderValueSDNC", "")
			verify(mockExecution).setVariable("UPDNETI_UpdateNetworkInstanceInfraJsonRequest", "")
			verify(mockExecution).setVariable("UPDNETI_networkRequest", "")
			verify(mockExecution).setVariable("UPDNETI_networkInputs", "")
			verify(mockExecution).setVariable("UPDNETI_networkOutputs", "")
			verify(mockExecution).setVariable("UPDNETI_requestId", "")
			verify(mockExecution).setVariable("UPDNETI_source", "")
			verify(mockExecution).setVariable("UPDNETI_networkId", "")
			
			verify(mockExecution).setVariable("UPDNETI_CompleteMsoProcessRequest", "")
			verify(mockExecution).setVariable("UPDNETI_FalloutHandlerRequest", "")
			verify(mockExecution).setVariable("UPDNETI_isSilentSuccess", false)
			verify(mockExecution).setVariable("UPDNETI_isPONR", false)
			
			// AAI query Cloud Region
			verify(mockExecution).setVariable("UPDNETI_queryCloudRegionRequest","")
			verify(mockExecution).setVariable("UPDNETI_queryCloudRegionReturnCode","")
			verify(mockExecution).setVariable("UPDNETI_queryCloudRegionResponse","")
			verify(mockExecution).setVariable("UPDNETI_cloudRegionPo","")
			verify(mockExecution).setVariable("UPDNETI_cloudRegionSdnc","")
			verify(mockExecution).setVariable("UPDNETI_isCloudRegionGood", false)
			
			// AAI query Id
			verify(mockExecution).setVariable("UPDNETI_queryIdAAIRequest","")
			verify(mockExecution).setVariable("UPDNETI_queryIdAAIResponse", "")
			verify(mockExecution).setVariable("UPDNETI_aaiIdReturnCode", "")

			// AAI query vpn binding
			verify(mockExecution).setVariable("UPDNETI_queryVpnBindingAAIRequest","")
			verify(mockExecution).setVariable("UPDNETI_queryVpnBindingAAIResponse", "")
			verify(mockExecution).setVariable("UPDNETI_aaiQqueryVpnBindingReturnCode", "")
			verify(mockExecution).setVariable("UPDNETI_vpnBindings", null)
			verify(mockExecution).setVariable("UPDNETI_vpnCount", 0)
			verify(mockExecution).setVariable("UPDNETI_routeCollection", "")
			
			// AAI query network policy
			verify(mockExecution).setVariable("UPDNETI_queryNetworkPolicyAAIRequest","")
			verify(mockExecution).setVariable("UPDNETI_queryNetworkPolicyAAIResponse", "")
			verify(mockExecution).setVariable("UPDNETI_aaiQqueryNetworkPolicyReturnCode", "")
			verify(mockExecution).setVariable("UPDNETI_networkPolicyUriList", null)
			verify(mockExecution).setVariable("UPDNETI_networkPolicyCount", 0)
			verify(mockExecution).setVariable("UPDNETI_networkCollection", "")
			
			// AAI query route table reference
			verify(mockExecution).setVariable("UPDNETI_queryNetworkTableRefAAIRequest","")
			verify(mockExecution).setVariable("UPDNETI_queryNetworkTableRefAAIResponse", "")
			verify(mockExecution).setVariable("UPDNETI_aaiQqueryNetworkTableRefReturnCode", "")
			verify(mockExecution).setVariable("UPDNETI_networkTableRefUriList", null)
			verify(mockExecution).setVariable("UPDNETI_networkTableRefCount", 0)
			verify(mockExecution).setVariable("UPDNETI_tableRefCollection", "")
			
			
			// AAI requery Id
			verify(mockExecution).setVariable("UPDNETI_requeryIdAAIRequest","")
			verify(mockExecution).setVariable("UPDNETI_requeryIdAAIResponse", "")
			verify(mockExecution).setVariable("UPDNETI_aaiRequeryIdReturnCode", "")

			// AAI update contrail
			verify(mockExecution).setVariable("UPDNETI_updateContrailAAIUrlRequest","")
			verify(mockExecution).setVariable("UPDNETI_updateContrailAAIPayloadRequest","")
			verify(mockExecution).setVariable("UPDNETI_updateContrailAAIResponse", "")
			verify(mockExecution).setVariable("UPDNETI_aaiUpdateContrailReturnCode", "")
				
			verify(mockExecution).setVariable("UPDNETI_updateNetworkRequest", "")
			verify(mockExecution).setVariable("UPDNETI_updateNetworkResponse", "")
			verify(mockExecution).setVariable("UPDNETI_rollbackNetworkRequest", "")
			verify(mockExecution).setVariable("UPDNETI_rollbackNetworkResponse", "")
			verify(mockExecution).setVariable("UPDNETI_networkReturnCode", "")
			verify(mockExecution).setVariable("UPDNETI_rollbackNetworkReturnCode", "")
			verify(mockExecution).setVariable("UPDNETI_isNetworkRollbackNeeded", false)
			
			verify(mockExecution).setVariable("UPDNETI_changeAssignSDNCRequest", "")
			verify(mockExecution).setVariable("UPDNETI_changeAssignSDNCResponse", "")
			verify(mockExecution).setVariable("UPDNETI_rollbackSDNCRequest", "")
			verify(mockExecution).setVariable("UPDNETI_rollbackSDNCResponse", "")
			verify(mockExecution).setVariable("UPDNETI_sdncReturnCode", "")
			verify(mockExecution).setVariable("UPDNETI_rollbackSDNCReturnCode", "")
			verify(mockExecution).setVariable("UPDNETI_isSdncRollbackNeeded", false)
			verify(mockExecution).setVariable("UPDNETI_sdncResponseSuccess", false)

			verify(mockExecution).setVariable("UPDNETI_updateDBRequest", "")
			verify(mockExecution).setVariable("UPDNETI_updateDBResponse", "")
			verify(mockExecution).setVariable("UPDNETI_dbReturnCode", "")
			
			verify(mockExecution).setVariable("UPDNETI_isVnfBindingPresent", false)
			verify(mockExecution).setVariable("UPDNETI_Success", false)
			verify(mockExecution).setVariable("UPDNETI_serviceInstanceId", "")
			verify(mockExecution).setVariable("GENGS_type", "service-instance") // Setting for Generic Sub Flow use
			
			
		}
		
		@Test
		//@Ignore  
		public void preProcessRequest_NetworkRequest() {
			
			println "************ preProcessRequest_Payload ************* " 
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("requestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("isBaseVfModule")).thenReturn(true)
			when(mockExecution.getVariable("recipeTimeout")).thenReturn(0)
			when(mockExecution.getVariable("requestAction")).thenReturn("UPDATE")
			when(mockExecution.getVariable("serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
			when(mockExecution.getVariable("vnfId")).thenReturn("")
			when(mockExecution.getVariable("volumeGroupId")).thenReturn("")
			when(mockExecution.getVariable("networkId")).thenReturn("49c86598-f766-46f8-84f8-8d1c1b10f9b4")
			when(mockExecution.getVariable("serviceType")).thenReturn("MOG")
			when(mockExecution.getVariable("networkType")).thenReturn("modelName")
			when(mockExecution.getVariable("bpmnRequest")).thenReturn(jsonIncomingRequest)
			
			when(mockExecution.getVariable("UPDNETI_messageId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("URN_mso_adapters_po_auth")).thenReturn("3141634BF7E070AA289CF2892C986C0B")
			when(mockExecution.getVariable("URN_mso_adapters_db_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_mso_rollback")).thenReturn("true")
									
			// preProcessRequest(Execution execution)						
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.preProcessRequest(mockExecution)
			
			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution).getVariable("isDebugLogEnabled")
			verify(mockExecution).setVariable("prefix", "UPDNETI_")
			
			//verify variable initialization
			initializeVariables(mockExecution)

			// Authentications
            verify(mockExecution).setVariable("BasicAuthHeaderValuePO", "Basic cGFzc3dvcmQ=")
			verify(mockExecution).setVariable("BasicAuthHeaderValueSDNC", "Basic cGFzc3dvcmQ=")

			verify(mockExecution).setVariable("UPDNETI_UpdateNetworkInstanceInfraJsonRequest", jsonIncomingRequest)
			//verify(mockExecution).setVariable("UPDNETI_networkRequest", expectedNetworkRequest)
			//verify(mockExecution).setVariable("UPDNETI_networkInputs", expectedNetworkInputs)
			//verify(mockExecution, atLeast(3)).setVariable("UPDNETI_networkOutputs", "")
			
			//verify(mockExecution).setVariable("UPDNETI_requestId", "88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			//verify(mockExecution).setVariable("UPDNETI_source", "VID")
			//verify(mockExecution).setVariable("UPDNETI_messageId", "88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			//verify(mockExecution, atLeast(1)).setVariable("GENGS_type", "service-instance")
							
		}

		
		@Test
		//@Ignore
		public void preProcessRequest_MissingNetworkId() {

			println "************ preProcessRequest_MissingName() ************* "
			
			WorkflowException missingNameWorkflowException = new WorkflowException("UpdateNetworkInstanceInfra", 2500, "Variable 'network-id' value/element is missing.")
			
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("requestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("isBaseVfModule")).thenReturn(true)
			when(mockExecution.getVariable("recipeTimeout")).thenReturn(0)
			when(mockExecution.getVariable("requestAction")).thenReturn("UPDATE")
			when(mockExecution.getVariable("serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
			when(mockExecution.getVariable("vnfId")).thenReturn("")
			when(mockExecution.getVariable("volumeGroupId")).thenReturn("")
			//when(mockExecution.getVariable("networkId")).thenReturn("49c86598-f766-46f8-84f8-8d1c1b10f9b4")
			when(mockExecution.getVariable("serviceType")).thenReturn("MOG")
			when(mockExecution.getVariable("networkType")).thenReturn("modelName")
			when(mockExecution.getVariable("bpmnRequest")).thenReturn(jsonIncomingRequest)
			
			when(mockExecution.getVariable("UPDNETI_messageId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("URN_mso_adapters_po_auth")).thenReturn("3141634BF7E070AA289CF2892C986C0B")
			when(mockExecution.getVariable("URN_mso_adapters_db_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_mso_rollback")).thenReturn("true")
			
			// preProcessRequest(Execution execution)
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			try {
				UpdateNetworkInstanceInfra.preProcessRequest(mockExecution)
			} catch (Exception ex) {
				println " Test End - Handle catch-throw BpmnError()! "
			}
			
			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution, atLeast(1)).getVariable("isDebugLogEnabled")
			verify(mockExecution, atLeast(1)).setVariable("prefix", "UPDNETI_")
			
			//verify variable initialization
			initializeVariables(mockExecution)

			// Authentications
			verify(mockExecution).setVariable("BasicAuthHeaderValuePO", "Basic cGFzc3dvcmQ=")
			verify(mockExecution).setVariable("BasicAuthHeaderValueSDNC", "Basic cGFzc3dvcmQ=")

			verify(mockExecution).setVariable("UPDNETI_UpdateNetworkInstanceInfraJsonRequest", jsonIncomingRequest)
			//verify(mockExecution, atLeast(1)).setVariable("UPDNETI_networkOutputs", networkOutputs)
			//verify(mockExecution).setVariable("UPDNETI_networkRequest", expectedNetworkRequestMissingNetworkId)
			//verify(mockExecution).setVariable("UPDNETI_networkInputs", expectedNetworkInputs)
			
			//verify(mockExecution).setVariable("UPDNETI_requestId", "88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			//verify(mockExecution).setVariable("UPDNETI_source", "VID")
			//verify(mockExecution).setVariable("UPDNETI_messageId", "88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			
			verify(mockExecution).setVariable(eq("WorkflowException"), refEq(missingNameWorkflowException))
						
		}
		
		@Test
		//@Ignore
		public void preProcessRequest_MissingCloudRegion() {
			
			println "************ preProcessRequest_MissingCloudRegion() ************* "
			
			WorkflowException missingCloudRegionWorkflowException = new WorkflowException("UpdateNetworkInstanceInfra", 2500, "requestDetails has missing 'aic-cloud-region' value/element.")
			
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("requestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("isBaseVfModule")).thenReturn(true)
			when(mockExecution.getVariable("recipeTimeout")).thenReturn(0)
			when(mockExecution.getVariable("requestAction")).thenReturn("UPDATE")
			when(mockExecution.getVariable("serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
			when(mockExecution.getVariable("vnfId")).thenReturn("")
			when(mockExecution.getVariable("volumeGroupId")).thenReturn("")
			when(mockExecution.getVariable("networkId")).thenReturn("networkId")
			when(mockExecution.getVariable("serviceType")).thenReturn("MOG")
			when(mockExecution.getVariable("networkType")).thenReturn("modelName")
			
			when(mockExecution.getVariable("bpmnRequest")).thenReturn(jsonIncomingRequest_MissingCloudRegion)
			when(mockExecution.getVariable("UPDNETI_messageId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("URN_mso_adapters_po_auth")).thenReturn("3141634BF7E070AA289CF2892C986C0B")
			when(mockExecution.getVariable("URN_mso_adapters_db_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_mso_rollback")).thenReturn("true")

									
			// preProcessRequest(Execution execution)
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			try {
				UpdateNetworkInstanceInfra.preProcessRequest(mockExecution)
			} catch (Exception ex) {
				println " Test End - Handle catch-throw BpmnError()! "
			}
			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution, atLeast(1)).getVariable("isDebugLogEnabled")
			verify(mockExecution, atLeast(1)).setVariable("prefix", "UPDNETI_")
			
			//verify variable initialization
			initializeVariables(mockExecution)

			verify(mockExecution).setVariable("UPDNETI_messageId", "88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			// Authentications
			verify(mockExecution).setVariable("BasicAuthHeaderValuePO", "Basic cGFzc3dvcmQ=")
			verify(mockExecution).setVariable("BasicAuthHeaderValueSDNC", "Basic cGFzc3dvcmQ=")

			verify(mockExecution).setVariable("UPDNETI_UpdateNetworkInstanceInfraJsonRequest", jsonIncomingRequest_MissingCloudRegion)
			//verify(mockExecution).setVariable("UPDNETI_networkRequest", "")
			//verify(mockExecution).setVariable("UPDNETI_networkInputs", "")
			//verify(mockExecution, atLeast(1)).setVariable("UPDNETI_networkOutputs", "")
			
			//verify(mockExecution).setVariable("UPDNETI_requestId", "88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			//verify(mockExecution).setVariable("UPDNETI_source", "VID")
			
			verify(mockExecution).setVariable(eq("WorkflowException"), refEq(missingCloudRegionWorkflowException))
							
		}
				
		@Test
		//@Ignore
		public void sendSyncResponse() {
			
			println "************ sendSyncResponse ************* "
			
			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("isAsyncProcess")).thenReturn(true)
			when(mockExecution.getVariable("mso-request-id")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			//when(mockExecution.getVariable("serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
			
			// preProcessRequest(Execution execution)
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.sendSyncResponse(mockExecution)

			verify(mockExecution).setVariable("prefix", "UPDNETI_")
			verify(mockExecution).setVariable("UpdateNetworkInstanceInfraResponseCode", "202")

			
		}
		
		@Test
		//@Ignore
		public void sendSyncError() {
			
			println "************ sendSyncError ************* "
			
			ExecutionEntity mockExecution = setupMock()									
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("isAsyncProcess")).thenReturn(true)
			when(mockExecution.getVariable("mso-request-id")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			//when(mockExecution.getVariable("serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")

			
			// preProcessRequest(Execution execution)
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.sendSyncError(mockExecution)

			verify(mockExecution).setVariable("prefix", "UPDNETI_")
			verify(mockExecution).setVariable("UpdateNetworkInstanceInfraResponseCode", "500")
			
		}
		
		
		@Test
		//@Ignore
		public void prepareDBRequest() {
			
			println "************ prepareDBRequest ************* "
			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			when(mockExecution.getVariable("UPDNETI_networkOutputs")).thenReturn("")
			when(mockExecution.getVariable("UPDNETI_requestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("UPDNETI_orchestrationStatus")).thenReturn("")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_mso_adapters_db_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			
			// preProcessRequest(Execution execution)
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.prepareDBRequest(mockExecution)
			
			verify(mockExecution).setVariable("prefix", "UPDNETI_")
			verify(mockExecution).setVariable("UPDNETI_updateDBRequest", updateDBRequest)
		
		}
		
		@Test
		//@Ignore
		public void prepareDBRequestErro_ExceptionObject() {
			
			println "************ prepareDBRequest ************* "
			
			WorkflowException sndcWorkflowException = new WorkflowException("UpdateNetworkInstanceInfra", 500, "Received error from SDN-C: No availability zone available")
			
			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			when(mockExecution.getVariable("UPDNETI_networkOutputs")).thenReturn("")
			when(mockExecution.getVariable("UPDNETI_requestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("UPDNETI_networkOutputs")).thenReturn("")
			when(mockExecution.getVariable("UPDNETI_orchestrationStatus")).thenReturn("")
			when(mockExecution.getVariable("WorkflowException")).thenReturn(sndcWorkflowException)
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_mso_adapters_db_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")

			// preProcessRequest(Execution execution)
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.prepareDBRequestError(mockExecution)
			
			verify(mockExecution).setVariable("prefix", "UPDNETI_")
			verify(mockExecution).setVariable("UPDNETI_updateDBRequest", updateDBRequestError)
		
		}
		
		@Test
		//@Ignore
		public void prepareDBRequest_Outputs() {
			
			println "************ prepareDBRequest ************* "
			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			when(mockExecution.getVariable("UPDNETI_networkOutputs")).thenReturn(networkOutputs)
			when(mockExecution.getVariable("UPDNETI_requestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("UPDNETI_orchestrationStatus")).thenReturn("")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_mso_adapters_db_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")

			// preProcessRequest(Execution execution)
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.prepareDBRequest(mockExecution)
			
			verify(mockExecution).setVariable("prefix", "UPDNETI_")
			verify(mockExecution).setVariable("UPDNETI_updateDBRequest", updateDBRequest_Outputs)
		
		}
			
		@Test
		//@Ignore
		public void prepareUpdateNetworkRequest() {
			
			println "************ prepareNetworkRequest ************* "
						ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("UPDNETI_networkRequest")).thenReturn(expectedNetworkRequest)
			when(mockExecution.getVariable("UPDNETI_requeryIdAAIResponse")).thenReturn(queryIdAIIResponse)
			when(mockExecution.getVariable("UPDNETI_cloudRegionPo")).thenReturn("RDM2WAGPLCP")
			when(mockExecution.getVariable("UPDNETI_messageId")).thenReturn("messageId_generated")
			when(mockExecution.getVariable("UPDNETI_source")).thenReturn("VID")
			//when(mockExecution.getVariable("UPDNETI_queryVpnBindingAAIResponse")).thenReturn(queryVpnBindingAAIResponse)
			when(mockExecution.getVariable("UPDNETI_routeCollection")).thenReturn("<routeTargets>13979:105757</routeTargets><routeTargets>13979:105757</routeTargets>")
			when(mockExecution.getVariable("UPDNETI_networkCollection")).thenReturn("<policyFqdns>GN_EVPN_Test</policyFqdns>")
			when(mockExecution.getVariable("UPDNETI_tableRefCollection")).thenReturn("<routeTableFqdns>refFQDN1</routeTableFqdns><routeTableFqdns>refFQDN2</routeTableFqdns>")
			when(mockExecution.getVariable("UPDNETI_requestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			//when(mockExecution.getVariable("URN_?????")).thenReturn("")   // notificationUrl, //TODO - is this coming from URN? What variable/value to use?
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("UPDNETI_rollbackEnabled")).thenReturn("true")
			
			// preProcessRequest(Execution execution)
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.prepareUpdateNetworkRequest(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			// verify set prefix = "UPDNETI_"
			verify(mockExecution).setVariable("prefix", "UPDNETI_")
			
			verify(mockExecution).setVariable("UPDNETI_updateNetworkRequest", updateNetworkRequest)
			
		}
		

		@Test
		//@Ignore
		public void prepareUpdateNetworkRequest_NoPhysicalname() {
			
			println "************ prepareNetworkRequest ************* "
			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			when(mockExecution.getVariable("UPDNETI_networkRequest")).thenReturn(NetworkRequest_noPhysicalName)
			when(mockExecution.getVariable("UPDNETI_requeryIdAAIResponse")).thenReturn(queryIdAIIResponse)
			when(mockExecution.getVariable("UPDNETI_cloudRegionPo")).thenReturn("RDM2WAGPLCP")
			when(mockExecution.getVariable("UPDNETI_messageId")).thenReturn("messageId_generated")
			when(mockExecution.getVariable("UPDNETI_source")).thenReturn("VID")
			//when(mockExecution.getVariable("UPDNETI_queryVpnBindingAAIResponse")).thenReturn(queryVpnBindingAAIResponse)
			when(mockExecution.getVariable("UPDNETI_routeCollection")).thenReturn("<routeTargets>13979:105757</routeTargets><routeTargets>13979:105757</routeTargets>")
			when(mockExecution.getVariable("UPDNETI_networkCollection")).thenReturn("<policyFqdns>GN_EVPN_Test</policyFqdns>")
			when(mockExecution.getVariable("UPDNETI_tableRefCollection")).thenReturn("")
			when(mockExecution.getVariable("UPDNETI_requestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			//when(mockExecution.getVariable("URN_?????")).thenReturn("")   // notificationUrl, //TODO - is this coming from URN? What variable/value to use?
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("UPDNETI_rollbackEnabled")).thenReturn("true")
			
			// preProcessRequest(Execution execution)
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.prepareUpdateNetworkRequest(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			// verify set prefix = "UPDNETI_"
			verify(mockExecution).setVariable("prefix", "UPDNETI_")
			
			verify(mockExecution).setVariable("UPDNETI_updateNetworkRequest", updateNetworkRequest_noPhysicalName)
			
		}
		
		@Test
		//@Ignore
		public void prepareSDNCRequest() {
			
			println "************ prepareSDNCRequest ************* "
			
			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			when(mockExecution.getVariable("UPDNETI_networkRequest")).thenReturn(expectedNetworkRequest)
			when(mockExecution.getVariable("UPDNETI_cloudRegionSdnc")).thenReturn("RDM2WAGPLCP")
			when(mockExecution.getVariable("UPDNETI_serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
			when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("URN_mso_workflow_sdncadapter_callback")).thenReturn("http://localhost:28090/SDNCAdapterCallback")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("GENGSI_siResourceLink")).thenReturn("https://aai-int1.test.com:8443/aai/v8/business/customers/customer/MSO_1610_dev/service-subscriptions/service-subscription/MSO-dev-service-type/service-instances/service-instance/6d4eb22a-82f1-4257-9f80-4176262cfe69/")
			
			
			// preProcessRequest(Execution execution)
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.prepareSDNCRequest(mockExecution)
			
			// verify set prefix = "UPDNETI_"
			verify(mockExecution).setVariable("prefix", "UPDNETI_")
			verify(mockExecution).setVariable("UPDNETI_changeAssignSDNCRequest", changeAssignSDNCRequest)
			
		}
		
		@Test
		//@Ignore
		public void prepareSDNCRollbackRequest() {
			
			println "************ prepareSDNCRollbackRequest ************* "
			

			
			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			when(mockExecution.getVariable("UPDNETI_networkRequest")).thenReturn(expectedNetworkRequest)
			when(mockExecution.getVariable("UPDNETI_cloudRegionSdnc")).thenReturn("RDM2WAGPLCP")
			when(mockExecution.getVariable("UPDNETI_serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
			when(mockExecution.getVariable("UPDNETI_changeAssignSDNCResponse")).thenReturn(assignResponse)
			when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("URN_mso_workflow_sdncadapter_callback")).thenReturn("http://localhost:28090/SDNCAdapterCallback")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("GENGSI_siResourceLink")).thenReturn("https://aai-int1.test.com:8443/aai/v8/business/customers/customer/MSO_1610_dev/service-subscriptions/service-subscription/MSO-dev-service-type/service-instances/service-instance/6d4eb22a-82f1-4257-9f80-4176262cfe69/")
			
			// preProcessRequest(Execution execution)
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.prepareSDNCRollbackRequest(mockExecution)
			
			// verify set prefix = "UPDNETI_"
			verify(mockExecution).setVariable("prefix", "UPDNETI_")
			verify(mockExecution).setVariable("UPDNETI_rollbackSDNCRequest", sdncRollbackRequest)
			
		}
		
		@Test
		//@Ignore
		public void callRESTQueryAAINetworkId_200() {

			println "************ callRESTQueryAAINetworkId ************* "
			
			WireMock.reset();
			
			MockGetNetwork("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "UpdateNetworkV2/updateNetwork_queryNetworkId_AAIResponse_Success.xml", 200);
			
			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable("UPDNETI_networkRequest")).thenReturn(expectedNetworkRequest)
			when(mockExecution.getVariable("UPDNETI_messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("URN_aai_endpoint")).thenReturn("http://localhost:28090")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn("8")
			when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_l3_network_uri")).thenReturn("/aai/v8/network/l3-networks/l3-network")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_aai_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			
			// preProcessRequest(Execution execution)
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.callRESTQueryAAINetworkId(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", "UPDNETI_")
			verify(mockExecution).setVariable("UPDNETI_queryIdAAIRequest", "http://localhost:28090/aai/v8/network/l3-networks/l3-network/49c86598-f766-46f8-84f8-8d1c1b10f9b4")
			verify(mockExecution).setVariable("UPDNETI_aaiIdReturnCode", "200")
			
		}
		
		@Test
		//@Ignore
		public void callRESTQueryAAICloudRegion30_200() {

			println "************ callRESTQueryAAICloudRegion30_200 ************* "
			
			WireMock.reset();
			MockGetCloudRegion("RDM2WAGPLCP", 200, "CreateNetworkV2/cloudRegion30_AAIResponse_Success.xml")
			
			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable("prefix")).thenReturn("UPDNETI_")
			when(mockExecution.getVariable("UPDNETI_networkInputs")).thenReturn(expectedNetworkRequest)
			when(mockExecution.getVariable("URN_aai_endpoint")).thenReturn("http://localhost:28090")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn("8")
			when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_cloud_region_uri")).thenReturn("/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/att-aic")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_aai_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			
			// preProcessRequest(Execution execution)
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.callRESTQueryAAICloudRegion(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution, atLeast(1)).setVariable("prefix", "UPDNETI_")
			verify(mockExecution).setVariable("UPDNETI_queryCloudRegionRequest", "http://localhost:28090/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/att-aic/RDM2WAGPLCP")
			verify(mockExecution, atLeast(2)).setVariable("UPDNETI_queryCloudRegionReturnCode", "200")
			verify(mockExecution).setVariable("UPDNETI_isCloudRegionGood", true)
			
		}
		
		@Test
		//@Ignore
		public void callRESTQueryAAICloudRegion25_200() {

			println "************ callRESTQueryAAICloudRegion25_200 ************* "
			
			WireMock.reset();
			MockGetCloudRegion("RDM2WAGPLCP", 200, "CreateNetworkV2/cloudRegion25_AAIResponse_Success.xml")
			
			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable("prefix")).thenReturn("UPDNETI_")
			when(mockExecution.getVariable("UPDNETI_networkInputs")).thenReturn(expectedNetworkRequest)
			when(mockExecution.getVariable("URN_aai_endpoint")).thenReturn("http://localhost:28090")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn("8")
			when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_cloud_region_uri")).thenReturn("/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/att-aic")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_aai_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			
			// preProcessRequest(Execution execution)
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.callRESTQueryAAICloudRegion(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution, atLeast(1)).setVariable("prefix", "UPDNETI_")
			verify(mockExecution).setVariable("UPDNETI_queryCloudRegionRequest", "http://localhost:28090/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/att-aic/RDM2WAGPLCP")
			verify(mockExecution, atLeast(2)).setVariable("UPDNETI_queryCloudRegionReturnCode", "200")
			verify(mockExecution).setVariable("UPDNETI_isCloudRegionGood", true)
			
		}
		
		@Test
		//@Ignore
		public void callRESTQueryAAICloudRegion_NotFound() {

			println "************ callRESTQueryAAICloudRegionFake ************* "
			
			WireMock.reset();
			MockGetCloudRegion("MDTWNJ21", 404, "")
			
			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable("prefix")).thenReturn("UPDNETI_")
			when(mockExecution.getVariable("UPDNETI_networkInputs")).thenReturn(vnfRequestFakeRegion)
			when(mockExecution.getVariable("URN_aai_endpoint")).thenReturn("http://localhost:28090")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn("8")
			when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_cloud_region_uri")).thenReturn("/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/att-aic")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_aai_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			
			// preProcessRequest(Execution execution)
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.callRESTQueryAAICloudRegion(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution, atLeast(1)).setVariable("prefix", "UPDNETI_")
			verify(mockExecution).setVariable("UPDNETI_queryCloudRegionRequest", "http://localhost:28090/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/att-aic/MDTWNJ21")
			verify(mockExecution, atLeast(1)).setVariable("UPDNETI_queryCloudRegionReturnCode", "404")
			verify(mockExecution).setVariable("UPDNETI_cloudRegionPo", "MDTWNJ21")
			verify(mockExecution).setVariable("UPDNETI_cloudRegionSdnc", "AAIAIC25")
			verify(mockExecution).setVariable("UPDNETI_isCloudRegionGood", true)
			
		}
		
		@Test
		//@Ignore
		public void callRESTQueryAAINetworkVpnBinding_200() {

			println "************ callRESTQueryAAINetworkVpnBinding_200 ************* "
			
			WireMock.reset();
			MockGetNetworkVpnBinding("85f015d0-2e32-4c30-96d2-87a1a27f8017", "UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", 200)
			MockGetNetworkVpnBinding("c980a6ef-3b88-49f0-9751-dbad8608d0a6", "UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", 200);
			
			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable("UPDNETI_requeryIdAAIResponse")).thenReturn(queryIdAIIResponse) // v6
			when(mockExecution.getVariable("UPDNETI_messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("URN_aai_endpoint")).thenReturn("http://localhost:28090")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn("8")
			when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_vpn_binding_uri")).thenReturn("/aai/v8/network/vpn-bindings/vpn-binding")
//			when(mockExecution.getVariable("URN_mso_workflow_UpdateNetworkInstanceInfra_aai_network_vpn-binding_uri")).thenReturn("")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true") 
			when(mockExecution.getVariable("URN_aai_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			
			// preProcessRequest(Execution execution)
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.callRESTQueryAAINetworkVpnBinding(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", "UPDNETI_")
			verify(mockExecution).setVariable("UPDNETI_vpnCount", 2)
			verify(mockExecution).setVariable("UPDNETI_vpnBindings", ['/aai/v8/network/vpn-bindings/vpn-binding/85f015d0-2e32-4c30-96d2-87a1a27f8017/', '/aai/v8/network/vpn-bindings/vpn-binding/c980a6ef-3b88-49f0-9751-dbad8608d0a6/'])
			// the last vpnBinding value is saved.
			verify(mockExecution).setVariable("UPDNETI_queryVpnBindingAAIRequest", "http://localhost:28090/aai/v8/network/vpn-bindings/vpn-binding/85f015d0-2e32-4c30-96d2-87a1a27f8017")
			verify(mockExecution, atLeast(2)).setVariable("UPDNETI_aaiQqueryVpnBindingReturnCode", "200")
			
		}

		@Test
		//@Ignore
		public void callRESTQueryAAINetworkVpnBinding_TestScenario01_200() {

			println "************ callRESTQueryAAINetworkVpnBinding_200 ************* "
			
			WireMock.reset();
			MockGetNetworkVpnBinding("85f015d0-2e32-4c30-96d2-87a1a27f8017", "UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", 200)
			
			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable("UPDNETI_requeryIdAAIResponse")).thenReturn(queryIdAIIResponseTestScenario01) 
			when(mockExecution.getVariable("UPDNETI_messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("URN_aai_endpoint")).thenReturn("http://localhost:28090")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn("8")
			when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_vpn_binding_uri")).thenReturn("/aai/v8/network/vpn-bindings/vpn-binding")
//			when(mockExecution.getVariable("URN_mso_workflow_UpdateNetworkInstanceInfra_aai_network_vpn-binding_uri")).thenReturn("")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_aai_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			
			// preProcessRequest(Execution execution)
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.callRESTQueryAAINetworkVpnBinding(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", "UPDNETI_")
			verify(mockExecution).setVariable("UPDNETI_vpnCount", 1)
			verify(mockExecution).setVariable("UPDNETI_vpnBindings", ['/aai/v8/network/vpn-bindings/vpn-binding/85f015d0-2e32-4c30-96d2-87a1a27f8017/'])
			// the last vpnBinding value is saved.
			verify(mockExecution).setVariable("UPDNETI_queryVpnBindingAAIRequest", "http://localhost:28090/aai/v8/network/vpn-bindings/vpn-binding/85f015d0-2e32-4c30-96d2-87a1a27f8017")
			verify(mockExecution).setVariable("UPDNETI_aaiQqueryVpnBindingReturnCode", "200")
			
		}
		
		@Test
		//@Ignore
		public void callRESTQueryAAINetworkVpnBinding_200_URN_Uri() {

			println "************ callRESTQueryAAINetworkVpnBinding_200 ************* "
			
			WireMock.reset();
			MockGetNetworkVpnBinding("85f015d0-2e32-4c30-96d2-87a1a27f8017", "UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", 200)
			MockGetNetworkVpnBinding("c980a6ef-3b88-49f0-9751-dbad8608d0a6", "UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", 200);
			
			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable("UPDNETI_requeryIdAAIResponse")).thenReturn(queryIdAIIResponse) 
			when(mockExecution.getVariable("UPDNETI_messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("URN_aai_endpoint")).thenReturn("http://localhost:28090")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn("8")
			when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_vpn_binding_uri")).thenReturn("/aai/v8/network/vpn-bindings/vpn-binding")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_aai_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			
			// preProcessRequest(Execution execution)
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.callRESTQueryAAINetworkVpnBinding(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", "UPDNETI_")
			verify(mockExecution).setVariable("UPDNETI_vpnCount", 2)
			verify(mockExecution).setVariable("UPDNETI_vpnBindings", ['/aai/v8/network/vpn-bindings/vpn-binding/85f015d0-2e32-4c30-96d2-87a1a27f8017/', '/aai/v8/network/vpn-bindings/vpn-binding/c980a6ef-3b88-49f0-9751-dbad8608d0a6/'])
			// the last vpnBinding value is saved.
			verify(mockExecution).setVariable("UPDNETI_queryVpnBindingAAIRequest", "http://localhost:28090/aai/v8/network/vpn-bindings/vpn-binding/85f015d0-2e32-4c30-96d2-87a1a27f8017")
			verify(mockExecution, atLeast(2)).setVariable("UPDNETI_aaiQqueryVpnBindingReturnCode", "200")
			
		}
		
		@Test
		//@Ignore
		public void callRESTQueryAAINetworkVpnBinding_NotPresent() {

			println "************ callRESTQueryAAINetworkVpnBinding_NotPresent ************* "
			
			WireMock.reset();
			MockGetNetworkVpnBinding("85f015d0-2e32-4c30-96d2-87a1a27f8017", "UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", 200)
			
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			
			when(mockExecution.getVariable("UPDNETI_requeryIdAAIResponse")).thenReturn(queryIdAIIResponseVpnNotPresent)  
			when(mockExecution.getVariable("UPDNETI_messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("URN_aai_endpoint")).thenReturn("http://localhost:28090")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn("8")
			when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_l3_network_uri")).thenReturn("/aai/v8/network/l3-networks/l3-network")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			
			// preProcessRequest(Execution execution)
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.callRESTQueryAAINetworkVpnBinding(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", "UPDNETI_")
			verify(mockExecution).setVariable("UPDNETI_aaiQqueryVpnBindingReturnCode", "200")
			verify(mockExecution).setVariable("UPDNETI_vpnCount", 0)
			verify(mockExecution).setVariable("UPDNETI_queryVpnBindingAAIResponse", aaiVpnResponseStub)
						
		}
		
		@Test
		//@Ignore
		public void callRESTQueryAAINetworkTableRef_200() {

			println "************ callRESTQueryAAINetworkTableRef_200 ************* "
			
			WireMock.reset();
			MockGetNetworkRouteTable("refFQDN1", "CreateNetworkV2/createNetwork_queryNetworkTableRef1_AAIResponse_Success.xml", 200)
			MockGetNetworkRouteTable("refFQDN2", "CreateNetworkV2/createNetwork_queryNetworkTableRef2_AAIResponse_Success.xml", 200)
			
			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable("UPDNETI_requeryIdAAIResponse")).thenReturn(queryIdAIIResponse)
			when(mockExecution.getVariable("UPDNETI_messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("URN_aai_endpoint")).thenReturn("http://localhost:28090")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn("8")
			when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_route_table_reference_uri")).thenReturn("/aai/v8/network/route-table-references/route-table-reference")
//			when(mockExecution.getVariable("URN_mso_workflow_CreateNetworkInstanceInfra_aai_network_table_reference_uri")).thenReturn("")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_aai_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			
			// preProcessRequest(Execution execution)
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.callRESTQueryAAINetworkTableRef(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", "UPDNETI_")
			verify(mockExecution).setVariable("UPDNETI_networkTableRefCount", 2)
			verify(mockExecution).setVariable("UPDNETI_networkTableRefUriList", ['/aai/v8/network/route-table-references/route-table-reference/refFQDN1','/aai/v8/network/route-table-references/route-table-reference/refFQDN2'])
			// the last vpnBinding value is saved.
			verify(mockExecution).setVariable("UPDNETI_queryNetworkTableRefAAIRequest", "http://localhost:28090/aai/v8/network/route-table-references/route-table-reference/refFQDN1")
			verify(mockExecution, atLeast(2)).setVariable("UPDNETI_aaiQqueryNetworkTableRefReturnCode", "200")
			
		}
		
		@Test
		//@Ignore
		public void callRESTQueryAAINetworkPolicy_200() {

			println "************ callRESTQueryAAINetworkPolicy_200 ************* "
			
			WireMock.reset();
			MockGetNetworkPolicy("cee6d136-e378-4678-a024-2cd15f0ee0cg", "UpdateNetworkV2/updateNetwork_queryNetworkPolicy_AAIResponse_Success.xml", 200)
			
			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable("UPDNETI_requeryIdAAIResponse")).thenReturn(queryIdAIIResponse) 
			when(mockExecution.getVariable("UPDNETI_messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("URN_aai_endpoint")).thenReturn("http://localhost:28090")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn("8")
			when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_network_policy_uri")).thenReturn("/aai/v8/network/network-policies/network-policy")
//			when(mockExecution.getVariable("URN_mso_workflow_UpdateNetworkInstanceInfra_aai_network_policy_uri")).thenReturn("")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_aai_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			
			// preProcessRequest(Execution execution)
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.callRESTQueryAAINetworkPolicy(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", "UPDNETI_")
			verify(mockExecution).setVariable("UPDNETI_networkPolicyCount", 1)
			verify(mockExecution).setVariable("UPDNETI_networkPolicyUriList", ['/aai/v8/network/network-policies/network-policy/cee6d136-e378-4678-a024-2cd15f0ee0cg'])
			// the last vpnBinding value is saved.
			verify(mockExecution).setVariable("UPDNETI_queryNetworkPolicyAAIRequest", "http://localhost:28090/aai/v8/network/network-policies/network-policy/cee6d136-e378-4678-a024-2cd15f0ee0cg")
			verify(mockExecution).setVariable("UPDNETI_aaiQqueryNetworkPolicyReturnCode", "200")
			
		}
		
		
		@Test
		//@Ignore
		public void callRESTReQueryAAINetworkId_200() {

			println "************ callRESTReQueryAAINetworkId ************* "
			
			WireMock.reset();
			MockGetNetwork("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "UpdateNetworkV2/updateNetwork_queryNetworkId_AAIResponse_Success.xml", 200)
			
			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable("UPDNETI_changeAssignSDNCResponse")).thenReturn(sdncAdapterWorkflowFormattedResponse)
			when(mockExecution.getVariable("UPDNETI_messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("URN_aai_endpoint")).thenReturn("http://localhost:28090")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn("8")
			when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_l3_network_uri")).thenReturn("/aai/v8/network/l3-networks/l3-network")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_aai_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			
			// preProcessRequest(Execution execution)
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.callRESTReQueryAAINetworkId(mockExecution)
			
			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", "UPDNETI_")
			verify(mockExecution).setVariable("UPDNETI_requeryIdAAIRequest", "http://localhost:28090/aai/v8/network/l3-networks/l3-network/49c86598-f766-46f8-84f8-8d1c1b10f9b4")
			verify(mockExecution).setVariable("UPDNETI_aaiRequeryIdReturnCode", "200")
			
		}
		

		@Test
		@Ignore
		public void callRESTUpdateContrailAAINetworkREST_200() {

			println "************ callRESTUpdateContrailAAINetwork ************* "
			
			WireMock.reset();
			MockPutNetwork("49c86598-f766-46f8-84f8-8d1c1b10f9b4", 200, "UpdateNetworkV2/updateNetwork_updateContrail_AAIResponse_Success.xml")
			
			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable("UPDNETI_changeAssignSDNCResponse")).thenReturn(sdncAdapterWorkflowFormattedResponse)
			when(mockExecution.getVariable("UPDNETI_requeryIdAAIResponse")).thenReturn(queryIdAIIResponse)
			when(mockExecution.getVariable("UPDNETI_updateNetworkResponse")).thenReturn(updateNetworkResponseREST)
			when(mockExecution.getVariable("UPDNETI_messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("URN_aai_endpoint")).thenReturn("http://localhost:28090")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn("8")
			when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_l3_network_uri")).thenReturn("/aai/v8/network/l3-networks/l3-network")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_aai_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			
			// preProcessRequest(Execution execution)
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.callRESTUpdateContrailAAINetwork(mockExecution)

			// Capture the arguments to setVariable
			ArgumentCaptor<String> captor1 = ArgumentCaptor.forClass(String.class);
			ArgumentCaptor<String> captor2 = ArgumentCaptor.forClass(String.class);
			
			verify(mockExecution, times(6)).setVariable(captor1.capture(), captor2.capture())
			List<String> arg2List = captor2.getAllValues()
			String payloadResponseActual = arg2List.get(4)
			
			assertEquals(updateContrailAAIResponse.replaceAll("\\s+", ""), payloadResponseActual.replaceAll("\\s+", ""))
			
			verify(mockExecution).setVariable("prefix", "UPDNETI_")
			verify(mockExecution).setVariable("UPDNETI_updateContrailAAIUrlRequest", "http://localhost:28090/aai/v8/network/l3-networks/l3-network/49c86598-f766-46f8-84f8-8d1c1b10f9b4")
			verify(mockExecution).setVariable("UPDNETI_updateContrailAAIPayloadRequest", updateContrailAAIPayloadRequest)
			verify(mockExecution).setVariable("UPDNETI_aaiUpdateContrailReturnCode", "200")
			verify(mockExecution).setVariable("UPDNETI_isPONR", true)
				
		}
		

	
		@Test
		//@Ignore
		public void validateUpdateNetworkResponseREST() {
			
			println "************ validateNetworkResponse ************* "
			
			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("UPDNETI_updateNetworkResponse")).thenReturn(updateNetworkResponseREST)
			when(mockExecution.getVariable("UPDNETI_networkReturnCode")).thenReturn('200')
			
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.validateUpdateNetworkResponse(mockExecution)

			//MockitoDebuggerImpl debugger = new MockitoDebuggerImpl()
			//debugger.printInvocations(mockExecution)
						
			verify(mockExecution).setVariable("prefix", "UPDNETI_")
			verify(mockExecution).setVariable("UPDNETI_updateNetworkResponse", updateNetworkResponseREST)
			verify(mockExecution).setVariable("UPDNETI_isNetworkRollbackNeeded", true)
			verify(mockExecution).setVariable("UPDNETI_rollbackNetworkRequest", updateRollbackNetworkRequest)

		}
		
		@Test
		//@Ignore
		public void validateUpdateNetworkResponseREST_Error() {
			
			println "************ validateNetworkResponse ************* "
			
			WorkflowException workflowException = new WorkflowException("UpdateNetworkInstanceInfra", 2500, "Received error from Network Adapter: JBWEB000065: HTTP Status 500.")
			
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("UPDNETI_updateNetworkResponse")).thenReturn(networkException500)
			when(mockExecution.getVariable("UPDNETI_networkReturnCode")).thenReturn('500')
			
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			try {
				UpdateNetworkInstanceInfra.validateUpdateNetworkResponse(mockExecution)
			} catch (Exception ex) {
				println " Test End - Handle catch-throw BpmnError()! "
			}
						
			verify(mockExecution).setVariable("prefix", "UPDNETI_")
			verify(mockExecution, atLeast(1)).setVariable("WorkflowException", refEq(workflowException, any(WorkflowException.class)))
			
		}
		
		@Test
		//@Ignore 
		public void validateSDNCResponse() {
			
			println "************ validateSDNCResponse ************* "
			
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("UPDNETI_changeAssignSDNCResponse")).thenReturn(sdncAdapterWorkflowResponse)
			when(mockExecution.getVariable("prefix")).thenReturn("UPDNETI_")
			when(mockExecution.getVariable("SDNCA_SuccessIndicator")).thenReturn(true)
			when(mockExecution.getVariable("UPDNETI_sdncReturnCode")).thenReturn("200")
			when(mockExecution.getVariable("UPDNETI_isResponseGood")).thenReturn(true)
			
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			try {
			  UpdateNetworkInstanceInfra.validateSDNCResponse(mockExecution)
			  verify(mockExecution).setVariable("UPDNETI_isSdncRollbackNeeded", true)
			  verify(mockExecution).setVariable("UPDNETI_rollbackSDNCRequest", "")
			  
			} catch (Exception ex) {
				println " Graceful Exit - " + ex.getMessage()
			}
			//MockitoDebuggerImpl debugger = new MockitoDebuggerImpl()
			//debugger.printInvocations(mockExecution)
			
			//verify(mockExecution).setVariable("UPDNETI_isSdncRollbackNeeded", true)
				
		}

		@Test
		//@Ignore
		public void validateSDNCResponse_Error() {
			
			println "************ validateSDNCResponse ************* "
			//ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("UPDNETI_changeAssignSDNCResponse")).thenReturn(sdncAdapterWorkflowResponse_Error)
			when(mockExecution.getVariable("prefix")).thenReturn("UPDNETI_")
			when(mockExecution.getVariable("SDNCA_SuccessIndicator")).thenReturn(false)
			when(mockExecution.getVariable("UPDNETI_sdncReturnCode")).thenReturn("200")
			when(mockExecution.getVariable("UPDNETI_isResponseGood")).thenReturn(true)
			
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			try {
				UpdateNetworkInstanceInfra.validateSDNCResponse(mockExecution)
			} catch (Exception ex) {
				println " Graceful Exit! - " + ex.getMessage() 
			}
			//MockitoDebuggerImpl debugger = new MockitoDebuggerImpl()
			//debugger.printInvocations(mockExecution)
			
			// verify set prefix = "UPDNETI_"
			//verify(mockExecution).setVariable("UPDNETI_sdncResponseSuccess", false)
				
		}

		
		
		@Test
		//@Ignore
		public void postProcessResponse() {
			
			println "************ postProcessResponse ************* "
			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("UPDNETI_source")).thenReturn("PORTAL")
			when(mockExecution.getVariable("UPDNETI_requestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("UPDNETI_dbReturnCode")).thenReturn("200")
			
			// postProcessResponse(Execution execution)						
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.postProcessResponse(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", "UPDNETI_")
			verify(mockExecution).setVariable("UPDNETI_Success", true)
			verify(mockExecution).setVariable("UPDNETI_CompleteMsoProcessRequest", completeMsoProcessRequest)
		
		}

		@Test
		//@Ignore
		public void validateRollbackResponses_Good() {
			
			WorkflowException workflowException = new WorkflowException("UpdateNetworkInstanceInfra", 2500, "AAI Update Contrail Failed.  Error 404.")
			WorkflowException expectedWorkflowException = new WorkflowException("UpdateNetworkInstanceInfra", 2500, "AAI Update Contrail Failed.  Error 404. + PO Network rollback is not supported for Update. Submit another Update to restore/rollback. + SNDC rollback completed.")
				  
			println "************ validateRollbackResponses_Good() ************* "
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("prefix")).thenReturn("UPDNETI_")

			when(mockExecution.getVariable("UPDNETI_isNetworkRollbackNeeded")).thenReturn(true)
			when(mockExecution.getVariable("UPDNETI_rollbackNetworkReturnCode")).thenReturn("200")
			when(mockExecution.getVariable("UPDNETI_rollbackNetworkResponse")).thenReturn("GoodResponse")
			when(mockExecution.getVariable("UPDNETI_isSdncRollbackNeeded")).thenReturn(true)
			when(mockExecution.getVariable("UPDNETI_rollbackSDNCReturnCode")).thenReturn("200")
			when(mockExecution.getVariable("UPDNETI_rollbackSDNCResponse")).thenReturn("GoodResponse")
			when(mockExecution.getVariable("WorkflowException")).thenReturn(workflowException)
									
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.validateRollbackResponses(mockExecution)
			
			// verify set prefix = "UPDNETI_"
			verify(mockExecution, atLeast(1)).setVariable("prefix", "UPDNETI_")
			verify(mockExecution).setVariable("WorkflowException", refEq(expectedWorkflowException, any(WorkflowException.class)))
			
		}
		
		@Test
		//@Ignore
		public void validateRollbackResponses_Failed() {
			
			WorkflowException workflowException = new WorkflowException("UpdateNetworkInstanceInfra", 7020, "AAI Update Contrail Failed.  Error 404.")
			WorkflowException expectedWorkflowException = new WorkflowException("UpdateNetworkInstanceInfra", 7020, "AAI Update Contrail Failed.  Error 404. + PO Network rollback is not supported for Update. Submit another Update to restore/rollback. + SDNC rollback failed. ")
				  
			println "************ validateRollbackResponses_Failed() ************* "
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("prefix")).thenReturn("UPDNETI_")

			when(mockExecution.getVariable("UPDNETI_isNetworkRollbackNeeded")).thenReturn(true)
			when(mockExecution.getVariable("UPDNETI_rollbackNetworkReturnCode")).thenReturn("404")
			when(mockExecution.getVariable("UPDNETI_rollbackNetworkResponse")).thenReturn("BadResponse")
			when(mockExecution.getVariable("UPDNETI_isSdncRollbackNeeded")).thenReturn(true)
			when(mockExecution.getVariable("UPDNETI_rollbackSDNCReturnCode")).thenReturn("500")
			when(mockExecution.getVariable("UPDNETI_rollbackSDNCResponse")).thenReturn("BadResponse")
			when(mockExecution.getVariable("WorkflowException")).thenReturn(workflowException)
									
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.validateRollbackResponses(mockExecution)
			
			// verify set prefix = "UPDNETI_"
			verify(mockExecution, atLeast(1)).setVariable("prefix", "UPDNETI_")
			verify(mockExecution, atLeast(1)).setVariable("WorkflowException", refEq(expectedWorkflowException, any(WorkflowException.class)))

		}
		
		@Test
		//@Ignore
		public void validateRollbackResponses_NetworkFailed() {
			
			WorkflowException workflowException = new WorkflowException("UpdateNetworkInstanceInfra", 7020, "AAI Update Contrail Failed.  Error 404.")
			WorkflowException expectedWorkflowExceptionFailed = new WorkflowException("UpdateNetworkInstanceInfra", 7020, "AAI Update Contrail Failed.  Error 404. + PO Network rollback is not supported for Update. Submit another Update to restore/rollback. + SNDC rollback completed.")
				  
			println "************ validateRollbackResponses_NetworkFailed() ************* "
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("prefix")).thenReturn("UPDNETI_")

			when(mockExecution.getVariable("UPDNETI_isNetworkRollbackNeeded")).thenReturn(true)
			when(mockExecution.getVariable("UPDNETI_rollbackNetworkReturnCode")).thenReturn("404")
			when(mockExecution.getVariable("UPDNETI_rollbackNetworkResponse")).thenReturn("BadResponse")
			when(mockExecution.getVariable("UPDNETI_isSdncRollbackNeeded")).thenReturn(true)
			when(mockExecution.getVariable("UPDNETI_rollbackSDNCReturnCode")).thenReturn("200")
			when(mockExecution.getVariable("UPDNETI_rollbackSDNCResponse")).thenReturn("GoodResponse")
			when(mockExecution.getVariable("WorkflowException")).thenReturn(workflowException)
									
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.validateRollbackResponses(mockExecution)
			
			// verify set prefix = "UPDNETI_"
			verify(mockExecution, atLeast(1)).setVariable("prefix", "UPDNETI_")
			verify(mockExecution, atLeast(1)).setVariable("WorkflowException", refEq(expectedWorkflowExceptionFailed , any(WorkflowException.class)))

		}
		
		@Test
		//@Ignore
		public void validateRollbackResponses_SdncFailed() {
			
			WorkflowException workflowException = new WorkflowException("UpdateNetworkInstanceInfra", 7020, "AAI Update Contrail Failed.  Error 404.")
			WorkflowException expectedWorkflowException = new WorkflowException("UpdateNetworkInstanceInfra", 7020, "AAI Update Contrail Failed.  Error 404. + PO Network rollback is not supported for Update. Submit another Update to restore/rollback. + SDNC rollback failed. ")
				  
			println "************ validateRollbackResponses_SdncFailed() ************* "
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("prefix")).thenReturn("UPDNETI_")

			when(mockExecution.getVariable("UPDNETI_isNetworkRollbackNeeded")).thenReturn(true)
			when(mockExecution.getVariable("UPDNETI_rollbackNetworkReturnCode")).thenReturn("200")
			when(mockExecution.getVariable("UPDNETI_rollbackNetworkResponse")).thenReturn("GoodResponse")
			when(mockExecution.getVariable("UPDNETI_isSdncRollbackNeeded")).thenReturn(true)
			when(mockExecution.getVariable("UPDNETI_rollbackSDNCReturnCode")).thenReturn("200")
			when(mockExecution.getVariable("UPDNETI_rollbackSDNCResponse")).thenReturn("<response-code>400</response-code>")
			when(mockExecution.getVariable("WorkflowException")).thenReturn(workflowException)
									
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.validateRollbackResponses(mockExecution)
			
			// verify set prefix = "UPDNETI_"
			verify(mockExecution, atLeast(1)).setVariable("prefix", "UPDNETI_")
			verify(mockExecution).setVariable("WorkflowException", refEq(expectedWorkflowException , any(WorkflowException.class)))

		}
		
		@Test
		//@Ignore
		public void validateRollbackResponses_NoRollbacks() {
			
			WorkflowException workflowException = new WorkflowException("UpdateNetworkInstanceInfra", 2500, " AAI Update Contrail Failed.  Error 404")
			WorkflowException expectedWorkflowException = new WorkflowException("UpdateNetworkInstanceInfra", 2500, " AAI Update Contrail Failed.  Error 404")
				  
			println "************ validateRollbackResponses_NoRollbacks() ************* "
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("prefix")).thenReturn("UPDNETI_")

			when(mockExecution.getVariable("UPDNETI_isNetworkRollbackNeeded")).thenReturn(false)
			when(mockExecution.getVariable("UPDNETI_rollbackNetworkReturnCode")).thenReturn("200")
			when(mockExecution.getVariable("UPDNETI_rollbackNetworkResponse")).thenReturn("GoodResponse")
			when(mockExecution.getVariable("UPDNETI_isSdncRollbackNeeded")).thenReturn(false)
			when(mockExecution.getVariable("UPDNETI_rollbackSDNCReturnCode")).thenReturn("200")
			when(mockExecution.getVariable("UPDNETI_rollbackSDNCResponse")).thenReturn("GoodResponse")
			when(mockExecution.getVariable("WorkflowException")).thenReturn(workflowException)
									
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.validateRollbackResponses(mockExecution)
			
			// verify set prefix = "UPDNETI_"
			verify(mockExecution, atLeast(1)).setVariable("prefix", "UPDNETI_")
			//verify(mockExecution, atLeast(1)).setVariable("WorkflowException", any(expectedWorkflowException))
			verify(mockExecution, atLeast(1)).setVariable("WorkflowException", refEq(expectedWorkflowException, any(WorkflowException.class)))

		}
		
	
		@Test
		//@Ignore 
		public void buildErrorResponse() {
			
			println "************ buildErrorResponse ************* "
			
			
			WorkflowException sndcWorkflowException = new WorkflowException("UpdateNetworkInstanceInfra", 5300, "Received error from SDN-C: No availability zone available.")
			
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("UPDNETI_requestId")).thenReturn("b69c9054-da09-4a2c-adf5-51042b62bfac")
			when(mockExecution.getVariable("UPDNETI_source")).thenReturn("PORTAL")
			when(mockExecution.getVariable("WorkflowException")).thenReturn(sndcWorkflowException)
			
			// buildErrorResponse(Execution execution)
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.buildErrorResponse(mockExecution)
			
			// verify set prefix = "UPDNETI_"
			verify(mockExecution, atLeast(1)).setVariable("prefix", "UPDNETI_")
			verify(mockExecution).setVariable("UPDNETI_Success", false)
			verify(mockExecution).setVariable("UPDNETI_FalloutHandlerRequest", falloutHandlerRequest)
			
			//MockitoDebuggerImpl debugger = new MockitoDebuggerImpl()
			//debugger.printInvocations(mockExecution)

		}
		
		@Test
		//@Ignore
		public void buildErrorResponse_WorkflowExceptionObject() {
			
			println "************ buildErrorResponse ************* "
			
			WorkflowException sndcWorkflowException = new WorkflowException("UpdateNetworkInstanceInfra", 7000, "Received error from SDN-C: No availability zone available") 
			
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("UPDNETI_requestId")).thenReturn("b69c9054-da09-4a2c-adf5-51042b62bfac")
			when(mockExecution.getVariable("UPDNETI_source")).thenReturn("VID")
			when(mockExecution.getVariable("WorkflowException")).thenReturn(sndcWorkflowException)
			
			// buildErrorResponse(Execution execution)
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.buildErrorResponse(mockExecution)
			
			// verify set prefix = "UPDNETI_"
			verify(mockExecution).setVariable("prefix", "UPDNETI_")
			verify(mockExecution).setVariable("UPDNETI_Success", false)
			verify(mockExecution).setVariable("UPDNETI_FalloutHandlerRequest", falloutHandlerRequestObject)
			
			//MockitoDebuggerImpl debugger = new MockitoDebuggerImpl()
			//debugger.printInvocations(mockExecution)

		}
		
		@Test
		//@Ignore
		public void buildErrorResponse_Scenario01() {
			
			WorkflowException aaiWorkflowException_Secnario01 = new WorkflowException("UpdateNetworkInstanceInfra", 7020, "Unexpected Response from AAI - 400")
			
			println "************ buildErrorResponse ************* "
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("UPDNETI_requestId")).thenReturn("b69c9054-da09-4a2c-adf5-51042b62bfac")
			when(mockExecution.getVariable("UPDNETI_source")).thenReturn("VID")
			when(mockExecution.getVariable("WorkflowException")).thenReturn(aaiWorkflowException_Secnario01)
			when(mockExecution.getVariable("UPDNETI_dbReturnCode")).thenReturn("200")
			when(mockExecution.getVariable("UPDNETI_updateDBResponse")).thenReturn("<GoodResponse>")
			
			when(mockExecution.getVariable("UPDNETI_aaiRequeryIdReturnCode")).thenReturn("400")
			
			// buildErrorResponse(Execution execution)
			UpdateNetworkInstanceInfra UpdateNetworkInstanceInfra = new UpdateNetworkInstanceInfra()
			UpdateNetworkInstanceInfra.buildErrorResponse(mockExecution)
			
			// verify set prefix = "UPDNETI_"
			verify(mockExecution, atLeast(1)).setVariable("prefix", "UPDNETI_")
			verify(mockExecution).setVariable("UPDNETI_Success", false)
			verify(mockExecution).setVariable("UPDNETI_FalloutHandlerRequest", falloutHandlerRequest_Scenario01)
			
			//MockitoDebuggerImpl debugger = new MockitoDebuggerImpl()
			//debugger.printInvocations(mockExecution)

		}
	

		private ExecutionEntity setupMock() {
			
			ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
			when(mockProcessDefinition.getKey()).thenReturn("UpdateNetworkInstanceInfra")
			RepositoryService mockRepositoryService = mock(RepositoryService.class)
			when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
			when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("UpdateNetworkInstanceInfra")
			when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
			ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
			when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)
			
			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			
			when(mockExecution.getId()).thenReturn("100")
			when(mockExecution.getProcessDefinitionId()).thenReturn("UpdateNetworkInstanceInfra")
			when(mockExecution.getProcessInstanceId()).thenReturn("UpdateNetworkInstanceInfra")
			when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
			when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)
			
			return mockExecution
		}
		
		
}
