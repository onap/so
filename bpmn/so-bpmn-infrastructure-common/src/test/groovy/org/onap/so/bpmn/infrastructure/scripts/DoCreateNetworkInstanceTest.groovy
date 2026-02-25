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

package org.onap.so.bpmn.infrastructure.scripts


import static org.mockito.Mockito.*
import static org.onap.so.bpmn.mock.StubResponseAAI.MockGetNetworkByName;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockGetNetworkByName_404;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockGetNetworkByIdWithDepth;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockGetNetworkCloudRegion;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockGetNetworkCloudRegion_404;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockPutNetworkIdWithDepth;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockGetNetworkPolicy;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockGetNetworkTableReference;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockGetNetworkVpnBinding;
import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.onap.aai.domain.yang.L3Network
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.core.WorkflowException
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.apache.commons.lang3.*

@RunWith(MockitoJUnitRunner.class)
class DoCreateNetworkInstanceTest  {

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(8090);

		def utils = new MsoUtils()
		String Prefix="CRENWKI_"

// ---- Start XML Zone ----
		String xmlIncomingRequest =
		"""<rest:payload contentType="text/xml" xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd">
      <network-request xmlns:vnfreq="http://org.onap/so/infra/vnf-request/v1">
         <request-info>
            <request-id>1ef47428-cade-45bd-a103-0751e8b2deb0</request-id>
            <action>CREATE</action>
            <source>PORTAL</source>
         </request-info>
         <network-inputs>
            <network-id>bdc5efe8-404a-409b-85f6-0dcc9eebae30</network-id>
            <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
            <network-type>CONTRAIL_EXTERNAL</network-type>
            <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
            <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
            <tenant-id>7dd5365547234ee8937416c65507d266</tenant-id>
			<physicalNetworkName>dvs-slcp3-01</physicalNetworkName>
			<vlans>3008</vlans>
            <service-instance-id>MNS-25180-L-01-dmz_direct_net_1</service-instance-id>
	        <backout-on-failure>true</backout-on-failure>
         </network-inputs>
         <network-params>
            <param xmlns="" name="shared">1</param>
            <param xmlns="" name="external">0</param>
         </network-params>
      </network-request>
   </rest:payload>"""

   		String expectedXMLNetworkRequest =
"""<rest:payload xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"
              xmlns:vnfreq="http://org.onap/so/infra/vnf-request/v1"
              contentType="text/xml">
   <network-request>
      <request-info>
         <request-id>1ef47428-cade-45bd-a103-0751e8b2deb0</request-id>
         <action>CREATE</action>
         <source>PORTAL</source>
      </request-info>
      <network-inputs>
         <network-id>bdc5efe8-404a-409b-85f6-0dcc9eebae30</network-id>
         <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
         <network-type>CONTRAIL_EXTERNAL</network-type>
         <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
         <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
         <tenant-id>7dd5365547234ee8937416c65507d266</tenant-id>
         <physicalNetworkName>dvs-slcp3-01</physicalNetworkName>
         <vlans>3008</vlans>
         <service-instance-id>MNS-25180-L-01-dmz_direct_net_1</service-instance-id>
         <backout-on-failure>true</backout-on-failure>
      </network-inputs>
      <network-params>
         <param name="shared">1</param>
         <param name="external">0</param>
      </network-params>
   </network-request>
</rest:payload>"""

		String expectedXMLNetworkInputs =
"""<network-inputs>
   <network-id>bdc5efe8-404a-409b-85f6-0dcc9eebae30</network-id>
   <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
   <network-type>CONTRAIL_EXTERNAL</network-type>
   <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
   <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
   <tenant-id>7dd5365547234ee8937416c65507d266</tenant-id>
   <physicalNetworkName>dvs-slcp3-01</physicalNetworkName>
   <vlans>3008</vlans>
   <service-instance-id>MNS-25180-L-01-dmz_direct_net_1</service-instance-id>
   <backout-on-failure>true</backout-on-failure>
</network-inputs>"""

		String networkXMLOutputs =
""""""

// ---- End XML Zone ----

// ---- Start JSON Zone ----
		// JSON format Input
		String jsonIncomingRequest =
		"""{ "requestDetails": {
	      "modelInfo": {
			"modelType": "network",
  			"modelCustomizationId": "f21df226-8093-48c3-be7e-0408fcda0422",
  			"modelName": "CONTRAIL_EXTERNAL",
  			"modelVersion": "1.0"
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

  String expectedJSONNetworkRequest =
  """<network-request xmlns="http://www.w3.org/2001/XMLSchema">
   <request-info>
      <request-id>null</request-id>
      <action>CREATE</action>
      <source>VID</source>
      <service-instance-id>f70e927b-6087-4974-9ef8-c5e4d5847ca4</service-instance-id>
   </request-info>
   <network-inputs>
      <network-id>networkId</network-id>
      <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
      <network-type>CONTRAIL_EXTERNAL</network-type>
      <modelCustomizationId>f21df226-8093-48c3-be7e-0408fcda0422</modelCustomizationId>
      <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
      <tenant-id>7dd5365547234ee8937416c65507d266</tenant-id>
      <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
      <backout-on-failure>false</backout-on-failure>
      <sdncVersion>1610</sdncVersion>
   </network-inputs>
   <network-params>
      <param name="some_user_param1">someValue1</param>
   </network-params>
</network-request>"""

		String expectedJSONNetworkInputs =
"""<network-inputs xmlns="http://www.w3.org/2001/XMLSchema">
   <network-id>networkId</network-id>
   <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
   <network-type>CONTRAIL_EXTERNAL</network-type>
   <modelCustomizationId>f21df226-8093-48c3-be7e-0408fcda0422</modelCustomizationId>
   <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
   <tenant-id>7dd5365547234ee8937416c65507d266</tenant-id>
   <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
   <backout-on-failure>false</backout-on-failure>
   <sdncVersion>1610</sdncVersion>
</network-inputs>"""

		String networkJSONOutputs =
"""<network-outputs>
	                   <network-id>networkId</network-id>
	                   <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
	                 </network-outputs>"""

// ---- End JSON Zone ----

// ---- Start vPIR Zone ----
	  // expectedNetworkRequest
		String expectedvIPRNetworkRequest =
  """<network-request xmlns="http://www.w3.org/2001/XMLSchema">
   <request-info>
      <request-id>88f65519-9a38-4c4b-8445-9eb4a5a5af56</request-id>
      <action>CREATE</action>
      <source>VID</source>
      <service-instance-id>f70e927b-6087-4974-9ef8-c5e4d5847ca4</service-instance-id>
   </request-info>
   <network-inputs>
      <network-id>networkId</network-id>
      <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
      <network-type>CONTRAIL_EXTERNAL</network-type>
      <subscription-service-type>MSO-dev-service-type</subscription-service-type>
      <global-customer-id>globalId_45678905678</global-customer-id>
      <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
      <tenant-id>7dd5365547234ee8937416c65507d266</tenant-id>
      <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
      <backout-on-failure>false</backout-on-failure>
      <failIfExist>false</failIfExist>
      <networkModelInfo>
         <modelName>CONTRAIL_EXTERNAL</modelName>
         <modelUuid>sn5256d1-5a33-55df-13ab-12abad84e111</modelUuid>
         <modelInvariantUuid>sn5256d1-5a33-55df-13ab-12abad84e764</modelInvariantUuid>
         <modelVersion>1</modelVersion>
         <modelCustomizationUuid>sn5256d1-5a33-55df-13ab-12abad84e222</modelCustomizationUuid>
      </networkModelInfo>
      <serviceModelInfo>
         <modelName>HNGW Protected OAM</modelName>
         <modelUuid>36a3a8ea-49a6-4ac8-b06c-89a54544b9b6</modelUuid>
         <modelInvariantUuid>fcc85cb0-ad74-45d7-a5a1-17c8744fdb71</modelInvariantUuid>
         <modelVersion>1.0</modelVersion>
         <modelCustomizationUuid/>
      </serviceModelInfo>
      <sdncVersion>1702</sdncVersion>
   </network-inputs>
   <network-params/>
</network-request>"""

		String expectedvIPRNetworkInputs =
"""<network-inputs xmlns="http://www.w3.org/2001/XMLSchema">
   <network-id>networkId</network-id>
   <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
   <network-type>CONTRAIL_EXTERNAL</network-type>
   <subscription-service-type>MSO-dev-service-type</subscription-service-type>
   <global-customer-id>globalId_45678905678</global-customer-id>
   <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
   <tenant-id>7dd5365547234ee8937416c65507d266</tenant-id>
   <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
   <backout-on-failure>false</backout-on-failure>
   <failIfExist>false</failIfExist>
   <networkModelInfo>
      <modelName>CONTRAIL_EXTERNAL</modelName>
      <modelUuid>sn5256d1-5a33-55df-13ab-12abad84e111</modelUuid>
      <modelInvariantUuid>sn5256d1-5a33-55df-13ab-12abad84e764</modelInvariantUuid>
      <modelVersion>1</modelVersion>
      <modelCustomizationUuid>sn5256d1-5a33-55df-13ab-12abad84e222</modelCustomizationUuid>
   </networkModelInfo>
   <serviceModelInfo>
      <modelName>HNGW Protected OAM</modelName>
      <modelUuid>36a3a8ea-49a6-4ac8-b06c-89a54544b9b6</modelUuid>
      <modelInvariantUuid>fcc85cb0-ad74-45d7-a5a1-17c8744fdb71</modelInvariantUuid>
      <modelVersion>1.0</modelVersion>
      <modelCustomizationUuid/>
   </serviceModelInfo>
   <sdncVersion>1702</sdncVersion>
</network-inputs>"""

		String networkvIPROutputs =
"""<network-outputs>
	                   <network-id>networkId</network-id>
	                   <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
	                 </network-outputs>"""

// ---- End vPIR Zone ----

  String vnfRequestFakeRegion =
  """<vnfreq:network-request xmlns:vnfreq="http://org.onap/so/infra/vnf-request/v1">
   <vnfreq:request-info>
      <vnfreq:request-id>1ef47428-cade-45bd-a103-0751e8b2deb0</vnfreq:request-id>
      <vnfreq:action>CREATE</vnfreq:action>
      <vnfreq:source>PORTAL</vnfreq:source>
   </vnfreq:request-info>
   <vnfreq:network-inputs>
      <vnfreq:network-name>MNS-25180-L-01-dmz_direct_net_1</vnfreq:network-name>
      <vnfreq:network-type>CONTRAIL_EXTERNAL</vnfreq:network-type>
      <subscriptionServiceType>MSO-dev-service-type</subscriptionServiceType>
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
  """<vnfreq:network-request xmlns:vnfreq="http://org.onap/so/infra/vnf-request/v1">
   <vnfreq:request-info>
      <vnfreq:request-id>1ef47428-cade-45bd-a103-0751e8b2deb0</vnfreq:request-id>
      <vnfreq:action>CREATE</vnfreq:action>
      <vnfreq:source>PORTAL</vnfreq:source>
   </vnfreq:request-info>
   <vnfreq:network-inputs>
      <vnfreq:network-name>MNS-25180-L-01-dmz_direct_net_1</vnfreq:network-name>
      <vnfreq:network-type>CONTRAIL_EXTERNAL</vnfreq:network-type>
      <subscriptionServiceType>MSO-dev-service-type</subscriptionServiceType>
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
		  """<network-inputs  xmlns="http://org.onap/so/infra/vnf-request/v1">
		      <network-name>myOwn_Network</network-name>
		      <network-type>CONTRAIL_EXTERNAL</network-type>
		      <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
		      <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
		      <tenant-id>e81d842d3e8b45c5a59f57cd76af3aaf</tenant-id>
		   </network-inputs>"""

  String networkInputs =
  """<network-inputs xmlns="http://org.onap/so/infra/vnf-request/v1">
   <network-id>bdc5efe8-404a-409b-85f6-0dcc9eebae30</network-id>
   <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
   <network-type>CONTRAIL_EXTERNAL</network-type>
   <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
   <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
   <tenant-id>e81d842d3e8b45c5a59f57cd76af3aaf</tenant-id>
</network-inputs>"""



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
         <orchestration-status>pending-create</orchestration-status>
         <subnets>
            <subnet>
               <subnet-id>57e9a1ff-d14f-4071-a828-b19ae98eb2fc</subnet-id>
               <gateway-address>107.239.52.1</gateway-address>
               <network-start-address>107.239.52.0</network-start-address>
               <cidr-mask>24</cidr-mask>
               <ip-version>4</ip-version>
               <orchestration-status>pending-create</orchestration-status>
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
         <network-technology>Contrail</network-technology>
         <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
         <network-role-instance>0</network-role-instance>
		 <resource-version>l3-version</resource-version>
         <orchestration-status>pending-create</orchestration-status>
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
               <orchestration-status>pending-create</orchestration-status>
               <dhcp-enabled>true</dhcp-enabled>
	  		   <subnet-name>subnetName</subnet-name>
	  		   <subnet-role>ECOMP</subnet-role>
	  		   <ip-assignment-direction>true</ip-assignment-direction>
	  		   <host-routes>
                 <host-route>
                   <host-route-id>string</host-route-id>
                   <route-prefix>192.10.16.0/24</route-prefix>
                   <next-hop>192.10.16.100/24</next-hop>
                   <next-hop-type>ip-address</next-hop-type>
	  			   <resource-version>1505857301954</resource-version>
                 </host-route>
                 <host-route>
                  <host-route-id>string</host-route-id>
                  <route-prefix>192.110.17.0/24</route-prefix>
                  <next-hop>192.110.17.110/24</next-hop>
                  <next-hop-type>ip-address</next-hop-type>
	  			  <resource-version>1505857301954</resource-version>
                 </host-route>
               </host-routes>
               <relationship-list/>
            </subnet>
            <subnet>
               <subnet-id>57e9a1ff-d14f-4071-a828-b19ae98eb2fc</subnet-id>
               <gateway-address>107.239.52.1</gateway-address>
               <network-start-address>107.239.52.0</network-start-address>
               <cidr-mask>24</cidr-mask>
               <ip-version>4</ip-version>
               <orchestration-status>pending-create</orchestration-status>
               <dhcp-enabled>true</dhcp-enabled>
	  		   <subnet-name>subnetName</subnet-name>
	  		   <ip-assignment-direction>true</ip-assignment-direction>
	  		   <host-routes>
                 <host-route>
                   <host-route-id>string</host-route-id>
                   <route-prefix>192.10.16.0/24</route-prefix>
                   <next-hop>192.10.16.100/24</next-hop>
                   <next-hop-type>ip-address</next-hop-type>
	  			  <resource-version>1505857301954</resource-version>
                 </host-route>
               </host-routes>
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

String queryIdAIIResponse_AlaCarte =
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
         <network-technology>Contrail</network-technology>
         <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
         <network-role-instance>0</network-role-instance>
		 <resource-version>l3-version</resource-version>
         <orchestration-status>pending-create</orchestration-status>
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
               <orchestration-status>pending-create</orchestration-status>
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
               <orchestration-status>pending-create</orchestration-status>
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

String queryIdAIIResponse_segmentation =
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
         <network-technology>Contrail</network-technology>
         <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
         <network-role-instance>0</network-role-instance>
		 <resource-version>l3-version</resource-version>
         <orchestration-status>pending-create</orchestration-status>
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
               <orchestration-status>pending-create</orchestration-status>
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
               <orchestration-status>pending-create</orchestration-status>
               <dhcp-enabled>true</dhcp-enabled>
	  		   <subnet-name>subnetName</subnet-name>
               <relationship-list/>
            </subnet>
         </subnets>
		 <segmentation-assignments>
			<segmentation-assignment>
				<segmentation-id>1</segmentation-id>
				<resource-version>1498507569188</resource-version>
			</segmentation-assignment>
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

String queryIdAIIResponse_Ipv4 =
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
         <network-technology>Contrail</network-technology>
         <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
         <network-role-instance>0</network-role-instance>
		 <resource-version>l3-version</resource-version>
         <orchestration-status>pending-create</orchestration-status>
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
               <ip-version>ipv4</ip-version>
               <orchestration-status>pending-create</orchestration-status>
               <dhcp-enabled>true</dhcp-enabled>
	  		   <subnet-name>subnetName</subnet-name>
               <relationship-list/>
            </subnet>
            <subnet>
               <subnet-id>57e9a1ff-d14f-4071-a828-b19ae98eb2fc</subnet-id>
               <gateway-address>107.239.52.1</gateway-address>
               <network-start-address>107.239.52.0</network-start-address>
               <cidr-mask>24</cidr-mask>
               <ip-version>ipv4</ip-version>
               <orchestration-status>pending-create</orchestration-status>
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

String queryIdAIIResponse_SRIOV =
"""<?xml version="1.0" encoding="UTF-8"?>
<l3-network xmlns="http://org.openecomp.aai.inventory/v8">
	<network-id>6cb1ae5a-d2db-4eb6-97bf-d52a506a53d8</network-id>
	<network-name>MSO_TEST_1702_A_HnportalProviderNetwork.HNPortalPROVIDERNETWORK.SR_IOV_Provider2_1_net_17</network-name>
	<network-type>SR_IOV_Provider2_1</network-type>
	<network-role>HnportalProviderNetwork.HNPortalPROVIDERNETWORK.SR_IOV_Provider2_1</network-role>
	<network-technology>AIC_SR_IOV</network-technology>
	<is-bound-to-vpn>false</is-bound-to-vpn>
	<service-id/>
	<resource-version>1487336177672</resource-version>
	<orchestration-status>PendingCreate</orchestration-status>
	<persona-model-id>f70d7a32-0ac8-4bd5-a0fb-3c9336540d78</persona-model-id>
	<persona-model-version>1.0</persona-model-version>
	<physical-network-name>Physnet21</physical-network-name>
	<is-provider-network>true</is-provider-network>
	<is-shared-network>false</is-shared-network>
	<is-external-network>false</is-external-network>
	<subnets>
		<subnet>
			<subnet-id>10437</subnet-id>
			<subnet-name>MSO_TEST_1702_A_HnportalProviderNetwork.HNPortalPROVIDERNETWORK.SR_IOV_Provider2_1_net_17_S0</subnet-name>
			<gateway-address>192.168.6.1</gateway-address>
			<network-start-address>192.168.6.0</network-start-address>
			<cidr-mask>26</cidr-mask>
			<ip-version>4</ip-version>
			<orchestration-status>PendingCreate</orchestration-status>
			<dhcp-enabled>true</dhcp-enabled>
			<dhcp-start>192.168.6.3</dhcp-start>
			<dhcp-end>192.168.6.62</dhcp-end>
			<resource-version>1487336177359</resource-version>
		</subnet>
	</subnets>
	<relationship-list>
		<relationship>
			<related-to>tenant</related-to>
			<related-link>https://aai-ext1.test.com:8443/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner/mtn16/tenants/tenant/6accefef3cb442ff9e644d589fb04107</related-link>
			<relationship-data>
				<relationship-key>cloud-region.cloud-owner</relationship-key>
				<relationship-value>CloudOwner</relationship-value>
			</relationship-data>
			<relationship-data>
				<relationship-key>cloud-region.cloud-region-id</relationship-key>
				<relationship-value>mtn16</relationship-value>
			</relationship-data>
			<relationship-data>
				<relationship-key>tenant.tenant-id</relationship-key>
				<relationship-value>6accefef3cb442ff9e644d589fb04107</relationship-value>
			</relationship-data>
			<related-to-property>
				<property-key>tenant.tenant-name</property-key>
				<property-value>MSO_TEST_1702_A</property-value>
			</related-to-property>
		</relationship>
		<relationship>
			<related-to>cloud-region</related-to>
			<related-link>https://aai-ext1.test.com:8443/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner/mtn16</related-link>
			<relationship-data>
				<relationship-key>cloud-region.cloud-owner</relationship-key>
				<relationship-value>CloudOwner</relationship-value>
			</relationship-data>
			<relationship-data>
				<relationship-key>cloud-region.cloud-region-id</relationship-key>
				<relationship-value>mtn16</relationship-value>
			</relationship-data>
			<related-to-property>
				<property-key>cloud-region.owner-defined-type</property-key>
				<property-value>lcp</property-value>
			</related-to-property>
		</relationship>
		<relationship>
			<related-to>service-instance</related-to>
			<related-link>https://aai-ext1.test.com:8443/aai/v8/business/customers/customer/MSO_1610_ST/service-subscriptions/service-subscription/MSO-dev-service-type/service-instances/service-instance/51d8336b-a993-4afe-a5fc-10b3afbd6560</related-link>
			<relationship-data>
				<relationship-key>customer.global-customer-id</relationship-key>
				<relationship-value>MSO_1610_ST</relationship-value>
			</relationship-data>
			<relationship-data>
				<relationship-key>service-subscription.service-type</relationship-key>
				<relationship-value>MSO-dev-service-type</relationship-value>
			</relationship-data>
			<relationship-data>
				<relationship-key>service-instance.service-instance-id</relationship-key>
				<relationship-value>51d8336b-a993-4afe-a5fc-10b3afbd6560</relationship-value>
			</relationship-data>
			<related-to-property>
				<property-key>service-instance.service-instance-name</property-key>
				<property-value>HnportalProviderNetwork_17</property-value>
			</related-to-property>
		</relationship>
	</relationship-list>
</l3-network>"""

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
	<orchestration-status>pending-create</orchestration-status>
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
			<orchestration-status>pending-create</orchestration-status>
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
			<orchestration-status>pending-create</orchestration-status>
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
			<related-link>https://localhost:8443/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner/AAIAIC25/
			</related-link>
			<relationship-data>
				<relationship-key>cloud-region.cloud-region-id</relationship-key>
				<relationship-value>AAIAIC25</relationship-value>
			</relationship-data>
			<relationship-data>
				<relationship-key>cloud-region.cloud-owner</relationship-key>
				<relationship-value>CloudOwner</relationship-value>
			</relationship-data>
			<related-to-property>
				<property-key>cloud-region.owner-defined-type</property-key>
				<property-value></property-value>
			</related-to-property>
		</relationship>
		<relationship>
			<related-to>tenant</related-to>
			<related-link>https://localhost:8443/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner/AAIAIC25/tenants/tenant/4ae1d3446a4c48b2bec44b6cfba06d68/</related-link>
			<relationship-data>
				<relationship-key>tenant.tenant-id</relationship-key>
				<relationship-value>4ae1d3446a4c48b2bec44b6cfba06d68
				</relationship-value>
			</relationship-data>
			<relationship-data>
				<relationship-key>cloud-region.cloud-owner</relationship-key>
				<relationship-value>CloudOwner</relationship-value>
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
			<related-link>https://localhost:8443/aai/v8/network/vpn-bindings/vpn-binding/85f015d0-2e32-4c30-96d2-87a1a27f8017/</related-link>
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
      <l3-network xmlns="http://org.openecomp.aai.inventory/v8">
         <network-id>49c86598-f766-46f8-84f8-8d1c1b10f9b4</network-id>
         <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
         <network-type>CONTRAIL_EXTERNAL</network-type>
         <network-role>dmz_direct</network-role>
         <network-technology>contrail</network-technology>
         <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
         <network-role-instance>0</network-role-instance>
         <orchestration-status>pending-create</orchestration-status>
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
               <orchestration-status>pending-create</orchestration-status>
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
		         <orchestration-status>pending-create</orchestration-status>
		         <subnets>
		            <subnet>
		               <subnet-id>57e9a1ff-d14f-4071-a828-b19ae98eb2fc</subnet-id>
		               <gateway-address>107.239.52.1</gateway-address>
		               <network-start-address>107.239.52.0</network-start-address>
		               <cidr-mask>24</cidr-mask>
		               <ip-version>4</ip-version>
		               <orchestration-status>pending-create</orchestration-status>
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
		         <orchestration-status>pending-create</orchestration-status>
		         <subnets>
		            <subnet>
		               <subnet-id>57e9a1ff-d14f-4071-a828-b19ae98eb2fc</subnet-id>
		               <gateway-address>107.239.52.1</gateway-address>
		               <network-start-address>107.239.52.0</network-start-address>
		               <cidr-mask>24</cidr-mask>
		               <ip-version>4</ip-version>
		               <orchestration-status>pending-create</orchestration-status>
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
                   value="localhost-20160314-20:53:33:487-134392"/>
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

		 String createDBRequestError01 =
	  """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
						<soapenv:Header/>
						<soapenv:Body>
							<ns:updateInfraRequest xmlns:ns="http://org.onap.so/requestsdb">
								<requestId>88f65519-9a38-4c4b-8445-9eb4a5a5af56</requestId>
								<lastModifiedBy>BPMN</lastModifiedBy>
								<statusMessage>Received error unexpectedly from SDN-C.</statusMessage>
								<responseBody></responseBody>
								<requestStatus>FAILED</requestStatus>
								<vnfOutputs>&lt;network-id&gt;&lt;/network-id&gt;&lt;network-name&gt;&lt;/network-names&gt;</vnfOutputs>
							</ns:updateInfraRequest>
					   	</soapenv:Body>
					   </soapenv:Envelope>"""

	  String createDBRequest_Outputs =
  """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:ns="http://org.onap.so/requestsdb">
   <soapenv:Header/>
   <soapenv:Body>
      <ns:updateInfraRequest>
         <requestId>88f65519-9a38-4c4b-8445-9eb4a5a5af56</requestId>
         <lastModifiedBy>BPMN</lastModifiedBy>
         <statusMessage>Network successfully created.</statusMessage>
         <responseBody/>
         <requestStatus>COMPLETED</requestStatus>
         <progress>100</progress>
         <vnfOutputs>&lt;network-id&gt;networkId&lt;/network-id&gt;&lt;network-name&gt;MNS-25180-L-01-dmz_direct_net_1&lt;/network-names&gt;</vnfOutputs>
         <networkId>networkId</networkId>
      </ns:updateInfraRequest>
   </soapenv:Body>
</soapenv:Envelope>"""

	  String createNetworkRequest =
	  """<createNetworkRequest>
   <cloudSiteId>RDM2WAGPLCP</cloudSiteId>
   <tenantId>7dd5365547234ee8937416c65507d266</tenantId>
   <networkId>49c86598-f766-46f8-84f8-8d1c1b10f9b4</networkId>
   <networkName>MNS-25180-L-01-dmz_direct_net_1</networkName>
   <networkType>CONTRAIL_EXTERNAL</networkType>
   <modelCustomizationUuid>sn5256d1-5a33-55df-13ab-12abad84e222</modelCustomizationUuid>
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
   <subnets>
      <allocationPools>
         <start/>
         <end/>
      </allocationPools>
      <cidr>107.239.52.0/24</cidr>
      <enableDHCP>true</enableDHCP>
      <gatewayIp>107.239.52.1</gatewayIp>
      <ipVersion>4</ipVersion>
      <subnetId>57e9a1ff-d14f-4071-a828-b19ae98eb2fc</subnetId>
      <subnetName>subnetName</subnetName>
      <addrFromStart>true</addrFromStart>
      <hostRoutes>
         <prefix>192.10.16.0/24</prefix>
         <nextHop>192.10.16.100/24</nextHop>
      </hostRoutes>
      <hostRoutes>
         <prefix>192.110.17.0/24</prefix>
         <nextHop>192.110.17.110/24</nextHop>
      </hostRoutes>
   </subnets>
   <subnets>
      <allocationPools>
         <start/>
         <end/>
      </allocationPools>
      <cidr>107.239.52.0/24</cidr>
      <enableDHCP>true</enableDHCP>
      <gatewayIp>107.239.52.1</gatewayIp>
      <ipVersion>4</ipVersion>
      <subnetId>57e9a1ff-d14f-4071-a828-b19ae98eb2fc</subnetId>
      <subnetName>subnetName</subnetName>
      <addrFromStart>true</addrFromStart>
      <hostRoutes>
         <prefix>192.10.16.0/24</prefix>
         <nextHop>192.10.16.100/24</nextHop>
      </hostRoutes>
   </subnets>
   <skipAAI>true</skipAAI>
   <backout>true</backout>
   <failIfExists>false</failIfExists>
   <msoRequest>
      <requestId>88f65519-9a38-4c4b-8445-9eb4a5a5af56</requestId>
      <serviceInstanceId>f70e927b-6087-4974-9ef8-c5e4d5847ca4</serviceInstanceId>
   </msoRequest>
   <messageId>messageId_generated</messageId>
   <notificationUrl/>
</createNetworkRequest>"""

String createNetworkRequest_Ipv4 =
"""<createNetworkRequest>
   <cloudSiteId>RDM2WAGPLCP</cloudSiteId>
   <tenantId>7dd5365547234ee8937416c65507d266</tenantId>
   <networkId>49c86598-f766-46f8-84f8-8d1c1b10f9b4</networkId>
   <networkName>MNS-25180-L-01-dmz_direct_net_1</networkName>
   <networkType>CONTRAIL_EXTERNAL</networkType>
   <modelCustomizationUuid>sn5256d1-5a33-55df-13ab-12abad84e222</modelCustomizationUuid>
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
   <subnets>
      <allocationPools>
         <start/>
         <end/>
      </allocationPools>
      <cidr>107.239.52.0/24</cidr>
      <enableDHCP>true</enableDHCP>
      <gatewayIp>107.239.52.1</gatewayIp>
      <ipVersion>4</ipVersion>
      <subnetId>57e9a1ff-d14f-4071-a828-b19ae98eb2fc</subnetId>
      <subnetName>subnetName</subnetName>
   </subnets>
   <subnets>
      <allocationPools>
         <start/>
         <end/>
      </allocationPools>
      <cidr>107.239.52.0/24</cidr>
      <enableDHCP>true</enableDHCP>
      <gatewayIp>107.239.52.1</gatewayIp>
      <ipVersion>4</ipVersion>
      <subnetId>57e9a1ff-d14f-4071-a828-b19ae98eb2fc</subnetId>
      <subnetName>subnetName</subnetName>
   </subnets>
   <skipAAI>true</skipAAI>
   <backout>true</backout>
   <failIfExists>false</failIfExists>
   <msoRequest>
      <requestId>88f65519-9a38-4c4b-8445-9eb4a5a5af56</requestId>
      <serviceInstanceId>f70e927b-6087-4974-9ef8-c5e4d5847ca4</serviceInstanceId>
   </msoRequest>
   <messageId>messageId_generated</messageId>
   <notificationUrl/>
</createNetworkRequest>"""

String createNetworkRequestAlaCarte =
"""<createNetworkRequest>
   <cloudSiteId>RDM2WAGPLCP</cloudSiteId>
   <tenantId>7dd5365547234ee8937416c65507d266</tenantId>
   <networkId>49c86598-f766-46f8-84f8-8d1c1b10f9b4</networkId>
   <networkName>MNS-25180-L-01-dmz_direct_net_1</networkName>
   <networkType>CONTRAIL_EXTERNAL</networkType>
   <modelCustomizationUuid>f21df226-8093-48c3-be7e-0408fcda0422</modelCustomizationUuid>
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
   <subnets>
      <allocationPools>
         <start/>
         <end/>
      </allocationPools>
      <cidr>107.239.52.0/24</cidr>
      <enableDHCP>true</enableDHCP>
      <gatewayIp>107.239.52.1</gatewayIp>
      <ipVersion>4</ipVersion>
      <subnetId>57e9a1ff-d14f-4071-a828-b19ae98eb2fc</subnetId>
      <subnetName>subnetName</subnetName>
   </subnets>
   <subnets>
      <allocationPools>
         <start/>
         <end/>
      </allocationPools>
      <cidr>107.239.52.0/24</cidr>
      <enableDHCP>true</enableDHCP>
      <gatewayIp>107.239.52.1</gatewayIp>
      <ipVersion>4</ipVersion>
      <subnetId>57e9a1ff-d14f-4071-a828-b19ae98eb2fc</subnetId>
      <subnetName>subnetName</subnetName>
   </subnets>
   <skipAAI>true</skipAAI>
   <backout>true</backout>
   <failIfExists>false</failIfExists>
   <networkParams>
      <some_user_param1>someValue1</some_user_param1>
   </networkParams>
   <msoRequest>
      <requestId>88f65519-9a38-4c4b-8445-9eb4a5a5af56</requestId>
      <serviceInstanceId>f70e927b-6087-4974-9ef8-c5e4d5847ca4</serviceInstanceId>
   </msoRequest>
   <messageId>messageId_generated</messageId>
   <notificationUrl/>
</createNetworkRequest>"""

String createNetworkRequest_SRIOV =
"""<createNetworkRequest>
   <cloudSiteId>RDM2WAGPLCP</cloudSiteId>
   <tenantId>7dd5365547234ee8937416c65507d266</tenantId>
   <networkId>6cb1ae5a-d2db-4eb6-97bf-d52a506a53d8</networkId>
   <networkName>MSO_TEST_1702_A_HnportalProviderNetwork.HNPortalPROVIDERNETWORK.SR_IOV_Provider2_1_net_17</networkName>
   <networkType>CONTRAIL_EXTERNAL</networkType>
   <modelCustomizationUuid>sn5256d1-5a33-55df-13ab-12abad84e222</modelCustomizationUuid>
   <networkTechnology>AIC_SR_IOV</networkTechnology>
   <providerVlanNetwork>
      <physicalNetworkName>Physnet21</physicalNetworkName>
      <vlans/>
   </providerVlanNetwork>
   <subnets>
      <allocationPools>
         <start>192.168.6.3</start>
         <end>192.168.6.62</end>
      </allocationPools>
      <cidr>192.168.6.0/26</cidr>
      <enableDHCP>true</enableDHCP>
      <gatewayIp>192.168.6.1</gatewayIp>
      <ipVersion>4</ipVersion>
      <subnetId>10437</subnetId>
      <subnetName>MSO_TEST_1702_A_HnportalProviderNetwork.HNPortalPROVIDERNETWORK.SR_IOV_Provider2_1_net_17_S0</subnetName>
   </subnets>
   <skipAAI>true</skipAAI>
   <backout>true</backout>
   <failIfExists>false</failIfExists>
   <msoRequest>
      <requestId>88f65519-9a38-4c4b-8445-9eb4a5a5af56</requestId>
      <serviceInstanceId>f70e927b-6087-4974-9ef8-c5e4d5847ca4</serviceInstanceId>
   </msoRequest>
   <messageId>messageId_generated</messageId>
   <notificationUrl/>
</createNetworkRequest>"""

  String createNetworkRequest_noPhysicalName =
  """<createNetworkRequest>
   <cloudSiteId>RDM2WAGPLCP</cloudSiteId>
   <tenantId>7dd5365547234ee8937416c65507d266</tenantId>
   <networkId>49c86598-f766-46f8-84f8-8d1c1b10f9b4</networkId>
   <networkName>MNS-25180-L-01-dmz_direct_net_1</networkName>
   <networkType>CONTRAIL_EXTERNAL</networkType>
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
   <subnets>
      <allocationPools>
         <start/>
         <end/>
      </allocationPools>
      <cidr>107.239.52.0/24</cidr>
      <enableDHCP>true</enableDHCP>
      <gatewayIp>107.239.52.1</gatewayIp>
      <ipVersion>4</ipVersion>
      <subnetId>57e9a1ff-d14f-4071-a828-b19ae98eb2fc</subnetId>
      <subnetName>subnetName</subnetName>
   </subnets>
   <subnets>
      <allocationPools>
         <start/>
         <end/>
      </allocationPools>
      <cidr>107.239.52.0/24</cidr>
      <enableDHCP>true</enableDHCP>
      <gatewayIp>107.239.52.1</gatewayIp>
      <ipVersion>4</ipVersion>
      <subnetId>57e9a1ff-d14f-4071-a828-b19ae98eb2fc</subnetId>
      <subnetName>subnetName</subnetName>
   </subnets>
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
</createNetworkRequest>"""

	  String createNetworkResponseREST =
  """<ns2:createNetworkResponse xmlns:ns2="http://org.onap.so/network">
	<networkId>MNS-25180-L-01-dmz_direct_net_1/2c88a3a9-69b9-43a7-ada6-1aca577c3641</networkId>
	<neutronNetworkId>c4f4e878-cde0-4b15-ae9a-bda857759cea</neutronNetworkId>
    <networkFqdn>default-domain:MSOTest:GN_EVPN_direct_net_0_ST1</networkFqdn>
	<networkStackId></networkStackId>
	<networkCreated>true</networkCreated>
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
		<networkCreated>true</networkCreated>
		<tenantId>7dd5365547234ee8937416c65507d266</tenantId>
		<cloudSiteId>RDM2WAGPLCP</cloudSiteId>
		<msoRequest>
			<requestId>1ef47428-cade-45bd-a103-0751e8b2deb0</requestId>
			<serviceInstanceId></serviceInstanceId>
		</msoRequest>
	</rollback>
	<messageId>messageId_generated</messageId>
</ns2:createNetworkResponse>"""

	  String createRollbackNetworkRequest =
	  """<rollbackNetworkRequest>
   <networkRollback>
      <networkId>MNS-25180-L-01-dmz_direct_net_1/2c88a3a9-69b9-43a7-ada6-1aca577c3641</networkId>
      <neutronNetworkId>c4f4e878-cde0-4b15-ae9a-bda857759cea</neutronNetworkId>
      <networkStackId/>
      <networkType>CONTRAIL_EXTERNAL</networkType>
      <networkCreated>true</networkCreated>
      <tenantId>7dd5365547234ee8937416c65507d266</tenantId>
      <cloudSiteId>RDM2WAGPLCP</cloudSiteId>
      <msoRequest>
         <requestId>1ef47428-cade-45bd-a103-0751e8b2deb0</requestId>
         <serviceInstanceId/>
      </msoRequest>
   </networkRollback>
</rollbackNetworkRequest>"""

	  String createNetworkResponse =
	  """<ns2:createNetworkResponse xmlns:ns2="http://org.onap.so/network"
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
      <networkCreated>true</networkCreated>
      <networkId>MNS-25180-L-01-dmz_direct_net_1/2c88a3a9-69b9-43a7-ada6-1aca577c3641</networkId>
      <networkType>CONTRAIL_EXTERNAL</networkType>
      <networkUpdated>false</networkUpdated>
      <neutronNetworkId>c4f4e878-cde0-4b15-ae9a-bda857759cea</neutronNetworkId>
      <tenantId>7dd5365547234ee8937416c65507d266</tenantId>
   </rollback>
</ns2:createNetworkResponse>"""

	  String updateContrailAAIPayloadRequest =
  """<l3-network xmlns="http://org.openecomp.aai.inventory/v9">
   <network-id>49c86598-f766-46f8-84f8-8d1c1b10f9b4</network-id>
   <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
   <network-type>CONTRAIL_EXTERNAL</network-type>
   <network-role>dmz_direct</network-role>
   <network-technology>Contrail</network-technology>
   <neutron-network-id>c4f4e878-cde0-4b15-ae9a-bda857759cea</neutron-network-id>
   <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
   <network-role-instance>0</network-role-instance>
   <resource-version>l3-version</resource-version>
   <orchestration-status>Created</orchestration-status>
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
         <orchestration-status>Created</orchestration-status>
         <dhcp-enabled>true</dhcp-enabled>
         <subnet-role>ECOMP</subnet-role>
         <resource-version>1505857301954</resource-version>
         <subnet-name>subnetName</subnet-name>
         <ip-assignment-direction>true</ip-assignment-direction>
         <host-routes>
            <host-route>
               <host-route-id>string</host-route-id>
               <route-prefix>192.10.16.0/24</route-prefix>
               <next-hop>192.10.16.100/24</next-hop>
               <next-hop-type>ip-address</next-hop-type>
               <resource-version>1505857301954</resource-version>
            </host-route>
            <host-route>
               <host-route-id>string</host-route-id>
               <route-prefix>192.110.17.0/24</route-prefix>
               <next-hop>192.110.17.110/24</next-hop>
               <next-hop-type>ip-address</next-hop-type>
               <resource-version>1505857301954</resource-version>
            </host-route>
         </host-routes>
      </subnet>
      <subnet>
         <subnet-id>57e9a1ff-d14f-4071-a828-b19ae98eb2fc</subnet-id>
         <neutron-subnet-id>bd8e87c6-f4e2-41b8-b0bc-9596aa00cd73</neutron-subnet-id>
         <gateway-address>107.239.52.1</gateway-address>
         <network-start-address>107.239.52.0</network-start-address>
         <cidr-mask>24</cidr-mask>
         <ip-version>4</ip-version>
         <orchestration-status>Created</orchestration-status>
         <dhcp-enabled>true</dhcp-enabled>
         <resource-version>1505857301954</resource-version>
         <subnet-name>subnetName</subnet-name>
         <ip-assignment-direction>true</ip-assignment-direction>
         <host-routes>
            <host-route>
               <host-route-id>string</host-route-id>
               <route-prefix>192.10.16.0/24</route-prefix>
               <next-hop>192.10.16.100/24</next-hop>
               <next-hop-type>ip-address</next-hop-type>
               <resource-version>1505857301954</resource-version>
            </host-route>
         </host-routes>
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
</l3-network>"""

String updateContrailAAIPayloadRequest_segmentation =
"""<l3-network xmlns="http://org.openecomp.aai.inventory/v9">
   <network-id>49c86598-f766-46f8-84f8-8d1c1b10f9b4</network-id>
   <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
   <network-type>CONTRAIL_EXTERNAL</network-type>
   <network-role>dmz_direct</network-role>
   <network-technology>Contrail</network-technology>
   <neutron-network-id>c4f4e878-cde0-4b15-ae9a-bda857759cea</neutron-network-id>
   <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
   <network-role-instance>0</network-role-instance>
   <resource-version>l3-version</resource-version>
   <orchestration-status>Created</orchestration-status>
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
         <orchestration-status>Created</orchestration-status>
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
         <orchestration-status>Created</orchestration-status>
         <dhcp-enabled>true</dhcp-enabled>
         <subnet-name>subnetName</subnet-name>
      </subnet>
   </subnets>
   <segmentation-assignments>
      <segmentation-assignment>
         <segmentation-id>1</segmentation-id>
         <resource-version>1498507569188</resource-version>
      </segmentation-assignment>
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

	  String createNetworkErrorResponse =
	  """<createNetworkError>
		 <messageId>680bd458-5ec1-4a16-b77c-509022e53450</messageId><category>INTERNAL</category>
		 <message>400 Bad Request: The server could not comply with the request since it is either malformed or otherwise incorrect., error.type=StackValidationFailed, error.message=Property error: : resources.network.properties: : Unknown Property network_ipam_refs_data</message>
		 <rolledBack>true</rolledBack>
	   </createNetworkError>"""


  String networkException500 =
  """<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"><soap:Body><soap:Fault><faultcode>soap:VersionMismatch</faultcode><faultstring>"http://org.onap.so/network", the namespace on the "createNetworkContrail" element, is not a valid SOAP version.</faultstring></soap:Fault></soap:Body></soap:Envelope>"""

	String aaiResponse =
   """<rest:RESTResponse xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"
					 statusCode="200">
	 <rest:headers>
		<rest:header name="Transfer-Encoding" value="chunked"/>
		<rest:header name="Date" value="Sat,30 Jan 2016 20:09:24 GMT"/>
		<rest:header name="Expires" value="Thu,01 Jan 1970 00:00:00 UTC"/>
		<rest:header name="X-AAI-TXID"
					 value="localhost-20160130-20:09:24:814-165843"/>
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

	String assignSDNCRequest =
    """<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                  xmlns:ns5="http://org.onap/so/request/types/v1"
                                  xmlns:sdncadapter="http://org.onap.so/workflow/sdnc/adapter/schema/v1">
   <sdncadapter:RequestHeader>
      <sdncadapter:RequestId>88f65519-9a38-4c4b-8445-9eb4a5a5af56</sdncadapter:RequestId>
      <sdncadapter:SvcInstanceId>f70e927b-6087-4974-9ef8-c5e4d5847ca4</sdncadapter:SvcInstanceId>
      <sdncadapter:SvcAction>assign</sdncadapter:SvcAction>
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
         <network-id>networkId</network-id>
         <network-type>CONTRAIL_EXTERNAL</network-type>
         <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
         <tenant>7dd5365547234ee8937416c65507d266</tenant>
         <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
      </network-request-information>
   </aetgt:SDNCRequestData>
</aetgt:SDNCAdapterWorkflowRequest>"""

String assignSDNCRequest_decodeUrlLink =
"""<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                  xmlns:ns5="http://org.onap/so/request/types/v1"
                                  xmlns:sdncadapter="http://org.onap.so/workflow/sdnc/adapter/schema/v1">
   <sdncadapter:RequestHeader>
      <sdncadapter:RequestId>88f65519-9a38-4c4b-8445-9eb4a5a5af56</sdncadapter:RequestId>
      <sdncadapter:SvcInstanceId>f70e927b-6087-4974-9ef8-c5e4d5847ca4</sdncadapter:SvcInstanceId>
      <sdncadapter:SvcAction>assign</sdncadapter:SvcAction>
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
         <service-type>VIRTUAL USP</service-type>
         <service-instance-id>f70e927b-6087-4974-9ef8-c5e4d5847ca4</service-instance-id>
         <subscriber-name>MSO_1610_dev</subscriber-name>
      </service-information>
      <network-request-information>
         <network-id>networkId</network-id>
         <network-type>CONTRAIL_EXTERNAL</network-type>
         <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
         <tenant>7dd5365547234ee8937416c65507d266</tenant>
         <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
      </network-request-information>
   </aetgt:SDNCRequestData>
</aetgt:SDNCAdapterWorkflowRequest>"""

String assignRpcSDNCRequest =
    """<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                  xmlns:sdncadapter="http://org.onap.so/workflow/sdnc/adapter/schema/v1">
   <sdncadapter:RequestHeader>
      <sdncadapter:RequestId>88f65519-9a38-4c4b-8445-9eb4a5a5af56</sdncadapter:RequestId>
      <sdncadapter:SvcInstanceId>f70e927b-6087-4974-9ef8-c5e4d5847ca4</sdncadapter:SvcInstanceId>
      <sdncadapter:SvcAction>assign</sdncadapter:SvcAction>
      <sdncadapter:SvcOperation>network-topology-operation</sdncadapter:SvcOperation>
      <sdncadapter:CallbackUrl>sdncCallback</sdncadapter:CallbackUrl>
      <sdncadapter:MsoAction>generic-resource</sdncadapter:MsoAction>
   </sdncadapter:RequestHeader>
   <aetgt:SDNCRequestData>
      <request-information>
         <request-id>88f65519-9a38-4c4b-8445-9eb4a5a5af56</request-id>
         <request-action>CreateNetworkInstance</request-action>
         <source>VID</source>
         <notification-url/>
         <order-number/>
         <order-version/>
      </request-information>
      <service-information>
         <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
         <subscription-service-type>MSO-dev-service-type</subscription-service-type>
         <onap-model-information>
            <model-invariant-uuid>fcc85cb0-ad74-45d7-a5a1-17c8744fdb71</model-invariant-uuid>
            <model-uuid>36a3a8ea-49a6-4ac8-b06c-89a54544b9b6</model-uuid>
            <model-version>1.0</model-version>
            <model-name>HNGW Protected OAM</model-name>
         </onap-model-information>
         <service-instance-id>f70e927b-6087-4974-9ef8-c5e4d5847ca4</service-instance-id>
         <global-customer-id>globalId_45678905678</global-customer-id>
         <subscriber-name>MSO_1610_dev</subscriber-name>
      </service-information>
      <network-information>
         <network-id>networkId</network-id>
         <network-type>CONTRAIL_EXTERNAL</network-type>
         <onap-model-information>
            <model-invariant-uuid>sn5256d1-5a33-55df-13ab-12abad84e764</model-invariant-uuid>
            <model-customization-uuid>sn5256d1-5a33-55df-13ab-12abad84e222</model-customization-uuid>
            <model-uuid>sn5256d1-5a33-55df-13ab-12abad84e111</model-uuid>
            <model-version>1</model-version>
            <model-name>CONTRAIL_EXTERNAL</model-name>
         </onap-model-information>
      </network-information>
      <network-request-input>
         <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
         <tenant>7dd5365547234ee8937416c65507d266</tenant>
         <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
         <aic-clli/>
         <network-input-parameters/>
      </network-request-input>
   </aetgt:SDNCRequestData>
</aetgt:SDNCAdapterWorkflowRequest>"""

String activateSDNCRequest =
"""<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                  xmlns:sdncadapter="http://org.onap.so/workflow/sdnc/adapter/schema/v1">
   <sdncadapter:RequestHeader>
      <sdncadapter:RequestId>88f65519-9a38-4c4b-8445-9eb4a5a5af56</sdncadapter:RequestId>
      <sdncadapter:SvcInstanceId>f70e927b-6087-4974-9ef8-c5e4d5847ca4</sdncadapter:SvcInstanceId>
      <sdncadapter:SvcAction>activate</sdncadapter:SvcAction>
      <sdncadapter:SvcOperation>network-topology-operation</sdncadapter:SvcOperation>
      <sdncadapter:CallbackUrl>sdncCallback</sdncadapter:CallbackUrl>
      <sdncadapter:MsoAction>generic-resource</sdncadapter:MsoAction>
   </sdncadapter:RequestHeader>
   <aetgt:SDNCRequestData>
      <request-information>
         <request-id>88f65519-9a38-4c4b-8445-9eb4a5a5af56</request-id>
         <request-action>CreateNetworkInstance</request-action>
         <source>VID</source>
         <notification-url/>
         <order-number/>
         <order-version/>
      </request-information>
      <service-information>
         <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
         <subscription-service-type>MSO-dev-service-type</subscription-service-type>
         <onap-model-information>
            <model-invariant-uuid>fcc85cb0-ad74-45d7-a5a1-17c8744fdb71</model-invariant-uuid>
            <model-uuid>36a3a8ea-49a6-4ac8-b06c-89a54544b9b6</model-uuid>
            <model-version>1.0</model-version>
            <model-name>HNGW Protected OAM</model-name>
         </onap-model-information>
         <service-instance-id>f70e927b-6087-4974-9ef8-c5e4d5847ca4</service-instance-id>
         <global-customer-id>globalId_45678905678</global-customer-id>
         <subscriber-name>MSO_1610_dev</subscriber-name>
      </service-information>
      <network-information>
         <network-id>networkId</network-id>
         <network-type>CONTRAIL_EXTERNAL</network-type>
         <onap-model-information>
            <model-invariant-uuid>sn5256d1-5a33-55df-13ab-12abad84e764</model-invariant-uuid>
            <model-customization-uuid>sn5256d1-5a33-55df-13ab-12abad84e222</model-customization-uuid>
            <model-uuid>sn5256d1-5a33-55df-13ab-12abad84e111</model-uuid>
            <model-version>1</model-version>
            <model-name>CONTRAIL_EXTERNAL</model-name>
         </onap-model-information>
      </network-information>
      <network-request-input>
         <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
         <tenant>7dd5365547234ee8937416c65507d266</tenant>
         <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
         <aic-clli/>
         <network-input-parameters/>
      </network-request-input>
   </aetgt:SDNCRequestData>
</aetgt:SDNCAdapterWorkflowRequest>"""

String assignResponse =
"""<sdncadapterworkflow:SDNCAdapterWorkflowResponse xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1" xmlns:tag0="http://org.onap/workflow/sdnc/adapter/schema/v1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"> <sdncadapterworkflow:response-data> <tag0:CallbackHeader> <tag0:RequestId>006927ca-f5a3-47fd-880c-dfcbcd81a093</tag0:RequestId> <tag0:ResponseCode>200</tag0:ResponseCode> <tag0:ResponseMessage>OK</tag0:ResponseMessage> </tag0:CallbackHeader> <tag0:RequestData xsi:type="xs:string"><output xmlns="com:att:sdnctl:vnf"><response-code>200</response-code><svc-request-id>006927ca-f5a3-47fd-880c-dfcbcd81a093</svc-request-id><ack-final-indicator>Y</ack-final-indicator><service-information><subscriber-name>notsurewecare</subscriber-name><service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id><service-instance-id>GN_EVPN_direct_net_0_ST_noGW</service-instance-id></service-information><network-information><network-id>8abc633a-810b-4ca5-8b3a-09511d13a2ce</network-id></network-information></output></tag0:RequestData> </sdncadapterworkflow:response-data> </sdncadapterworkflow:SDNCAdapterWorkflowResponse>"""

  String sdncRollbackRequest =
			  """<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                  xmlns:ns5="http://org.onap/so/request/types/v1"
                                  xmlns:sdncadapter="http://org.onap.so/workflow/sdnc/adapter/schema/v1">
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

String sdncRpcRollbackRequest =
"""<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                  xmlns:sdncadapter="http://org.onap.so/workflow/sdnc/adapter/schema/v1">
   <sdncadapter:RequestHeader>
      <sdncadapter:RequestId>88f65519-9a38-4c4b-8445-9eb4a5a5af56</sdncadapter:RequestId>
      <sdncadapter:SvcInstanceId>f70e927b-6087-4974-9ef8-c5e4d5847ca4</sdncadapter:SvcInstanceId>
      <sdncadapter:SvcAction>unassign</sdncadapter:SvcAction>
      <sdncadapter:SvcOperation>network-topology-operation</sdncadapter:SvcOperation>
      <sdncadapter:CallbackUrl>sdncCallback</sdncadapter:CallbackUrl>
      <sdncadapter:MsoAction>generic-resource</sdncadapter:MsoAction>
   </sdncadapter:RequestHeader>
   <aetgt:SDNCRequestData>
      <request-information>
         <request-id>88f65519-9a38-4c4b-8445-9eb4a5a5af56</request-id>
         <request-action>DeleteNetworkInstance</request-action>
         <source>VID</source>
         <notification-url/>
         <order-number/>
         <order-version/>
      </request-information>
      <service-information>
         <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
         <subscription-service-type>MSO-dev-service-type</subscription-service-type>
         <onap-model-information>
            <model-invariant-uuid>fcc85cb0-ad74-45d7-a5a1-17c8744fdb71</model-invariant-uuid>
            <model-uuid>36a3a8ea-49a6-4ac8-b06c-89a54544b9b6</model-uuid>
            <model-version>1.0</model-version>
            <model-name>HNGW Protected OAM</model-name>
         </onap-model-information>
         <service-instance-id>f70e927b-6087-4974-9ef8-c5e4d5847ca4</service-instance-id>
         <global-customer-id>globalId_45678905678</global-customer-id>
         <subscriber-name>MSO_1610_dev</subscriber-name>
      </service-information>
      <network-information>
         <network-id>8abc633a-810b-4ca5-8b3a-09511d13a2ce</network-id>
         <network-type>CONTRAIL_EXTERNAL</network-type>
         <onap-model-information>
            <model-invariant-uuid>sn5256d1-5a33-55df-13ab-12abad84e764</model-invariant-uuid>
            <model-customization-uuid>sn5256d1-5a33-55df-13ab-12abad84e222</model-customization-uuid>
            <model-uuid>sn5256d1-5a33-55df-13ab-12abad84e111</model-uuid>
            <model-version>1</model-version>
            <model-name>CONTRAIL_EXTERNAL</model-name>
         </onap-model-information>
      </network-information>
      <network-request-input>
         <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
         <tenant>7dd5365547234ee8937416c65507d266</tenant>
         <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
         <aic-clli/>
         <network-input-parameters/>
      </network-request-input>
   </aetgt:SDNCRequestData>
</aetgt:SDNCAdapterWorkflowRequest>"""

String sdncActivateRollbackRequest =
"""<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                  xmlns:sdncadapter="http://org.onap.so/workflow/sdnc/adapter/schema/v1">
   <sdncadapter:RequestHeader>
      <sdncadapter:RequestId>88f65519-9a38-4c4b-8445-9eb4a5a5af56</sdncadapter:RequestId>
      <sdncadapter:SvcInstanceId>f70e927b-6087-4974-9ef8-c5e4d5847ca4</sdncadapter:SvcInstanceId>
      <sdncadapter:SvcAction>deactivate</sdncadapter:SvcAction>
      <sdncadapter:SvcOperation>network-topology-operation</sdncadapter:SvcOperation>
      <sdncadapter:CallbackUrl>sdncCallback</sdncadapter:CallbackUrl>
      <sdncadapter:MsoAction>generic-resource</sdncadapter:MsoAction>
   </sdncadapter:RequestHeader>
   <aetgt:SDNCRequestData>
      <request-information>
         <request-id>88f65519-9a38-4c4b-8445-9eb4a5a5af56</request-id>
         <request-action>DeleteNetworkInstance</request-action>
         <source>VID</source>
         <notification-url/>
         <order-number/>
         <order-version/>
      </request-information>
      <service-information>
         <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
         <subscription-service-type>MSO-dev-service-type</subscription-service-type>
         <onap-model-information>
            <model-invariant-uuid>fcc85cb0-ad74-45d7-a5a1-17c8744fdb71</model-invariant-uuid>
            <model-uuid>36a3a8ea-49a6-4ac8-b06c-89a54544b9b6</model-uuid>
            <model-version>1.0</model-version>
            <model-name>HNGW Protected OAM</model-name>
         </onap-model-information>
         <service-instance-id>f70e927b-6087-4974-9ef8-c5e4d5847ca4</service-instance-id>
         <global-customer-id>globalId_45678905678</global-customer-id>
         <subscriber-name>MSO_1610_dev</subscriber-name>
      </service-information>
      <network-information>
         <network-id>8abc633a-810b-4ca5-8b3a-09511d13a2ce</network-id>
         <network-type>CONTRAIL_EXTERNAL</network-type>
         <onap-model-information>
            <model-invariant-uuid>sn5256d1-5a33-55df-13ab-12abad84e764</model-invariant-uuid>
            <model-customization-uuid>sn5256d1-5a33-55df-13ab-12abad84e222</model-customization-uuid>
            <model-uuid>sn5256d1-5a33-55df-13ab-12abad84e111</model-uuid>
            <model-version>1</model-version>
            <model-name>CONTRAIL_EXTERNAL</model-name>
         </onap-model-information>
      </network-information>
      <network-request-input>
         <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
         <tenant>7dd5365547234ee8937416c65507d266</tenant>
         <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
         <aic-clli/>
         <network-input-parameters/>
      </network-request-input>
   </aetgt:SDNCRequestData>
</aetgt:SDNCAdapterWorkflowRequest>"""

	   String sdncAdapterWorkflowResponse =
	  """<aetgt:SDNCAdapterWorkflowResponse xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                   xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1">
   <sdncadapterworkflow:response-data>
<tag0:CallbackHeader xmlns:tag0="http://org.onap.so/workflow/sdnc/adapter/schema/v1">
   <tag0:RequestId>745b1b50-e39e-4685-9cc8-c71f0bde8bf0</tag0:RequestId>
   <tag0:ResponseCode>200</tag0:ResponseCode>
   <tag0:ResponseMessage>OK</tag0:ResponseMessage>
</tag0:CallbackHeader>
   <tag0:RequestData xmlns:tag0="http://org.onap/workflow/sdnc/adapter/schema/v1"
                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xsi:type="xs:string">&lt;output xmlns="com:att:sdnctl:vnf"&gt;&lt;svc-request-id&gt;00703dc8-71ff-442d-a4a8-3adc5beef6a9&lt;/svc-request-id&gt;&lt;response-code&gt;200&lt;/response-code&gt;&lt;ack-final-indicator&gt;Y&lt;/ack-final-indicator&gt;&lt;network-information&gt;&lt;network-id&gt;49c86598-f766-46f8-84f8-8d1c1b10f9b4&lt;/network-id&gt;&lt;/network-information&gt;&lt;service-information&gt;&lt;service-type&gt;a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb&lt;/service-type&gt;&lt;service-instance-id&gt;MNS-25180-L-01-dmz_direct_net_1&lt;/service-instance-id&gt;&lt;subscriber-name&gt;notsurewecare&lt;/subscriber-name&gt;&lt;/service-information&gt;&lt;/output&gt;</tag0:RequestData>
   </sdncadapterworkflow:response-data>
</aetgt:SDNCAdapterWorkflowResponse>"""


	  String sdncAdapterWorkflowResponse_Error =
	  """<sdncadapterworkflow:SDNCAdapterWorkflowResponse xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
                                                 xmlns:tag0="http://org.onap/workflow/sdnc/adapter/schema/v1"
                                                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                                                 xmlns="com:att:sdnctl:vnf">
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
  """<sdncadapterworkflow:SDNCAdapterWorkflowResponse xmlns="com:att:sdnctl:vnf"
                                                 xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
                                                 xmlns:tag0="http://org.onap/workflow/sdnc/adapter/schema/v1"
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
	  """<aetgt:SDNCAdapterWorkflowResponse xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                   xmlns="com:att:sdnctl:vnf">
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

String sdncAdapterWorkflowAssignResponse =
"""<sdncadapterworkflow:SDNCAdapterWorkflowResponse xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
                                                 xmlns:tag0="http://org.onap.so/workflow/sdnc/adapter/schema/v1"
                                                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<sdncadapterworkflow:response-data>
		<tag0:CallbackHeader>
			<tag0:RequestId>79ec9006-3695-4fcc-93a8-be6f9e248beb</tag0:RequestId>
			<tag0:ResponseCode>200</tag0:ResponseCode>
			<tag0:ResponseMessage>OK</tag0:ResponseMessage>
		</tag0:CallbackHeader>
		<tag0:RequestData xsi:type="xs:string">
			<output xmlns="org:onap:sdnc:northbound:generic-resource">
				<response-message/>
				<svc-request-id>79ec9006-3695-4fcc-93a8-be6f9e248beb</svc-request-id>
				<service-response-information>
					<instance-id>f805ec2b-b4d8-473e-8325-67f110139e5d</instance-id>
				</service-response-information>
				<response-code>200</response-code>
				<network-response-information>
					<instance-id>49c86598-f766-46f8-84f8-8d1c1b10f9b4</instance-id>
					<object-path>restconf/config/GENERIC-RESOURCE-API:services/service/f805ec2b-b4d8-473e-8325-67f110139e5d/service-data/networks/network/f7e4db56-aab5-4065-8e65-cec1cd1de24f</object-path>
				</network-response-information>
				<ack-final-indicator>Y</ack-final-indicator>
			</output>
		</tag0:RequestData>
	</sdncadapterworkflow:response-data>
</sdncadapterworkflow:SDNCAdapterWorkflowResponse>"""

		String rollbackNetworkRequest =
"""<NetworkAdapter:rollbackNetwork xmlns:NetworkAdapter="http://org.onap.so/network">
   <rollback>
      <networkId>MNS-25180-L-01-dmz_direct_net_1/2c88a3a9-69b9-43a7-ada6-1aca577c3641</networkId>
      <neutronNetworkId>c4f4e878-cde0-4b15-ae9a-bda857759cea</neutronNetworkId>
      <networkStackId/>
      <networkType>CONTRAIL_EXTERNAL</networkType>
      <networkCreated>true</networkCreated>
      <tenantId>7dd5365547234ee8937416c65507d266</tenantId>
      <cloudSiteId>RDM2WAGPLCP</cloudSiteId>
      <msoRequest>
         <requestId>1ef47428-cade-45bd-a103-0751e8b2deb0</requestId>
         <serviceInstanceId/>
      </msoRequest>
   </rollback>
</NetworkAdapter:rollbackNetwork>"""

			String rollbackActivateSDNCRequest =
"""<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                  xmlns:ns5="http://org.onap/so/request/types/v1"
                                  xmlns:sdncadapter="http://org.onap.so/workflow/sdnc/adapter/schema/v1">
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
         <request-action>CreateNetworkInstance</request-action>
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
      <network-information>
         <network-id>8abc633a-810b-4ca5-8b3a-09511d13a2ce</network-id>
         <network-type>CONTRAIL_EXTERNAL</network-type>
         <onap-model-information>
            <model-invariant-uuid>invariant-uuid</model-invariant-uuid>
            <model-customization-uuid>customization-uuid</model-customization-uuid>
            <model-uuid>uuid</model-uuid>
            <model-version>version</model-version>
            <model-name>CONTRAIL_EXTERNAL</model-name>
         </onap-model-information>
      </network-information>
      <network-request-information>
         <network-id>8abc633a-810b-4ca5-8b3a-09511d13a2ce</network-id>
         <network-type>CONTRAIL_EXTERNAL</network-type>
         <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
         <tenant>7dd5365547234ee8937416c65507d266</tenant>
         <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
      </network-request-information>
   </aetgt:SDNCRequestData>
</aetgt:SDNCAdapterWorkflowRequest>"""

			String rollbackSDNCRequest =
"""<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                  xmlns:ns5="http://org.onap/so/request/types/v1"
                                  xmlns:sdncadapter="http://org.onap.so/workflow/sdnc/adapter/schema/v1">
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
         <request-action>CreateNetworkInstance</request-action>
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
      <network-information>
         <network-id>8abc633a-810b-4ca5-8b3a-09511d13a2ce</network-id>
         <network-type>CONTRAIL_EXTERNAL</network-type>
         <onap-model-information>
            <model-invariant-uuid>invariant-uuid</model-invariant-uuid>
            <model-customization-uuid>customization-uuid</model-customization-uuid>
            <model-uuid>uuid</model-uuid>
            <model-version>version</model-version>
            <model-name>CONTRAIL_EXTERNAL</model-name>
         </onap-model-information>
      </network-information>
      <network-request-information>
         <network-id>8abc633a-810b-4ca5-8b3a-09511d13a2ce</network-id>
         <network-type>CONTRAIL_EXTERNAL</network-type>
         <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
         <tenant>7dd5365547234ee8937416c65507d266</tenant>
         <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
      </network-request-information>
   </aetgt:SDNCRequestData>
</aetgt:SDNCAdapterWorkflowRequest>"""

// - - - - - - - -

	    @Before
		public void init()
		{
			MockitoAnnotations.initMocks(this)
		}

		public void initializeVariables (DelegateExecution mockExecution) {

			verify(mockExecution).setVariable(Prefix + "networkRequest", "")
			verify(mockExecution).setVariable(Prefix + "rollbackEnabled", null)
			verify(mockExecution).setVariable(Prefix + "networkInputs", "")
			//verify(mockExecution).setVariable(Prefix + "requestId", "")
			verify(mockExecution).setVariable(Prefix + "messageId", "")
			verify(mockExecution).setVariable(Prefix + "source", "")
			verify(mockExecution).setVariable("BasicAuthHeaderValuePO", "")
			verify(mockExecution).setVariable("BasicAuthHeaderValueSDNC", "")
			verify(mockExecution).setVariable(Prefix + "serviceInstanceId","")
			verify(mockExecution).setVariable("GENGS_type","")
			verify(mockExecution).setVariable(Prefix + "rsrc_endpoint", null)
			verify(mockExecution).setVariable(Prefix + "networkOutputs", "")
			verify(mockExecution).setVariable(Prefix + "networkId","")
			verify(mockExecution).setVariable(Prefix + "networkName","")

			// AAI query Name
			verify(mockExecution).setVariable(Prefix + "queryNameAAIRequest","")
			verify(mockExecution).setVariable(Prefix + "queryNameAAIResponse", "")
			verify(mockExecution).setVariable(Prefix + "aaiNameReturnCode", "")
			verify(mockExecution).setVariable(Prefix + "isAAIqueryNameGood", false)

			// AAI query Cloud Region
			verify(mockExecution).setVariable(Prefix + "queryCloudRegionRequest","")
			verify(mockExecution).setVariable(Prefix + "queryCloudRegionReturnCode","")
			verify(mockExecution).setVariable(Prefix + "queryCloudRegionResponse","")
			verify(mockExecution).setVariable(Prefix + "cloudRegionPo","")
			verify(mockExecution).setVariable(Prefix + "cloudRegionSdnc","")
			verify(mockExecution).setVariable(Prefix + "isCloudRegionGood", false)

			// AAI query Id
			verify(mockExecution).setVariable(Prefix + "queryIdAAIRequest","")
			verify(mockExecution).setVariable(Prefix + "queryIdAAIResponse", "")
			verify(mockExecution).setVariable(Prefix + "aaiIdReturnCode", "")

			// AAI query vpn binding
			verify(mockExecution).setVariable(Prefix + "queryVpnBindingAAIRequest","")
			verify(mockExecution).setVariable(Prefix + "queryVpnBindingAAIResponse", "")
			verify(mockExecution).setVariable(Prefix + "aaiQqueryVpnBindingReturnCode", "")
			verify(mockExecution).setVariable(Prefix + "vpnBindings", null)
			verify(mockExecution).setVariable(Prefix + "vpnCount", 0)
			verify(mockExecution).setVariable(Prefix + "routeCollection", "")

			// AAI query network policy
			verify(mockExecution).setVariable(Prefix + "queryNetworkPolicyAAIRequest","")
			verify(mockExecution).setVariable(Prefix + "queryNetworkPolicyAAIResponse", "")
			verify(mockExecution).setVariable(Prefix + "aaiQqueryNetworkPolicyReturnCode", "")
			verify(mockExecution).setVariable(Prefix + "networkPolicyUriList", null)
			verify(mockExecution).setVariable(Prefix + "networkPolicyCount", 0)
			verify(mockExecution).setVariable(Prefix + "networkCollection", "")

			// AAI query route table reference
			verify(mockExecution).setVariable(Prefix + "queryNetworkTableRefAAIRequest","")
			verify(mockExecution).setVariable(Prefix + "queryNetworkTableRefAAIResponse", "")
			verify(mockExecution).setVariable(Prefix + "aaiQqueryNetworkTableRefReturnCode", "")
			verify(mockExecution).setVariable(Prefix + "networkTableRefUriList", null)
			verify(mockExecution).setVariable(Prefix + "networkTableRefCount", 0)
			verify(mockExecution).setVariable(Prefix + "tableRefCollection", "")

			// AAI requery Id
			verify(mockExecution).setVariable(Prefix + "requeryIdAAIRequest","")
			verify(mockExecution).setVariable(Prefix + "requeryIdAAIResponse", "")
			verify(mockExecution).setVariable(Prefix + "aaiRequeryIdReturnCode", "")

			// AAI update contrail
			verify(mockExecution).setVariable(Prefix + "updateContrailAAIUrlRequest","")
			verify(mockExecution).setVariable(Prefix + "updateContrailAAIPayloadRequest","")
			verify(mockExecution).setVariable(Prefix + "updateContrailAAIResponse", "")
			verify(mockExecution).setVariable(Prefix + "aaiUpdateContrailReturnCode", "")

			verify(mockExecution).setVariable(Prefix + "createNetworkRequest", "")
			verify(mockExecution).setVariable(Prefix + "createNetworkResponse", "")
			verify(mockExecution).setVariable(Prefix + "rollbackNetworkRequest", "")
			//verify(mockExecution).setVariable(Prefix + "rollbackNetworkResponse", "")
			verify(mockExecution).setVariable(Prefix + "networkReturnCode", "")
			//verify(mockExecution).setVariable(Prefix + "rollbackNetworkReturnCode", "")
			verify(mockExecution).setVariable(Prefix + "isNetworkRollbackNeeded", false)

			verify(mockExecution).setVariable(Prefix + "assignSDNCRequest", "")
			verify(mockExecution).setVariable(Prefix + "assignSDNCResponse", "")
			verify(mockExecution).setVariable(Prefix + "rollbackSDNCRequest", "")
			//verify(mockExecution).setVariable(Prefix + "rollbackSDNCResponse", "")
			verify(mockExecution).setVariable(Prefix + "sdncReturnCode", "")
			//verify(mockExecution).setVariable(Prefix + "rollbackSDNCReturnCode", "")
			verify(mockExecution).setVariable(Prefix + "isSdncRollbackNeeded", false)
			verify(mockExecution).setVariable(Prefix + "sdncResponseSuccess", false)

			verify(mockExecution).setVariable(Prefix + "activateSDNCRequest", "")
			verify(mockExecution).setVariable(Prefix + "activateSDNCResponse", "")
			verify(mockExecution).setVariable(Prefix + "rollbackActivateSDNCRequest", "")
			//verify(mockExecution).setVariable(Prefix + "rollbackActivateSDNCResponse", "")
			verify(mockExecution).setVariable(Prefix + "sdncActivateReturnCode", "")
			//verify(mockExecution).setVariable(Prefix + "rollbackActivateSDNCReturnCode", "")
			verify(mockExecution).setVariable(Prefix + "isSdncActivateRollbackNeeded", false)
			verify(mockExecution).setVariable(Prefix + "sdncActivateResponseSuccess", false)

			verify(mockExecution).setVariable(Prefix + "orchestrationStatus", "")
			verify(mockExecution).setVariable(Prefix + "isVnfBindingPresent", false)
			verify(mockExecution).setVariable(Prefix + "Success", false)

			verify(mockExecution).setVariable(Prefix + "isException", false)

		}

		@Test
		//@Ignore
		public void preProcessRequest_vIPR_NetworkRequest() {

			println "************ preProcessRequest_Payload ************* "

		  String networkModelInfo = """{"modelUuid": "sn5256d1-5a33-55df-13ab-12abad84e111",
                                     "modelName": "CONTRAIL_EXTERNAL",
									 "modelType": "CONTRAIL_EXTERNAL",
									 "modelVersion": "1",
									 "modelCustomizationUuid": "sn5256d1-5a33-55df-13ab-12abad84e222",
									 "modelInvariantUuid": "sn5256d1-5a33-55df-13ab-12abad84e764"
									}""".trim()

		 String serviceModelInfo = """{"modelUuid": "36a3a8ea-49a6-4ac8-b06c-89a54544b9b6",
                                     "modelName": "HNGW Protected OAM",
									 "modelType": "service",
									 "modelVersion": "1.0",
									 "modelInvariantUuid": "fcc85cb0-ad74-45d7-a5a1-17c8744fdb71"
									}""".trim()


			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables

			// Pre-defined value, testing Only
			when(mockExecution.getVariable("testMessageId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			// Inputs:
			when(mockExecution.getVariable("msoRequestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("disableRollback")).thenReturn("true")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
			when(mockExecution.getVariable("networkId")).thenReturn("networkId")                                // optional
			when(mockExecution.getVariable("networkName")).thenReturn("MNS-25180-L-01-dmz_direct_net_1")        // optional
			when(mockExecution.getVariable("productFamilyId")).thenReturn("a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb")
			when(mockExecution.getVariable("networkModelInfo")).thenReturn("CONTRAIL_EXTERNAL")
			when(mockExecution.getVariable("lcpCloudRegionId")).thenReturn("RDM2WAGPLCP")
			when(mockExecution.getVariable("tenantId")).thenReturn("7dd5365547234ee8937416c65507d266")
			when(mockExecution.getVariable("failIfExists")).thenReturn("false")
			when(mockExecution.getVariable("networkModelInfo")).thenReturn(networkModelInfo)
			when(mockExecution.getVariable("serviceModelInfo")).thenReturn(serviceModelInfo)
			when(mockExecution.getVariable("sdncVersion")).thenReturn("1702")
			when(mockExecution.getVariable("action")).thenReturn("CREATE")
			when(mockExecution.getVariable("subscriptionServiceType")).thenReturn("MSO-dev-service-type")
			when(mockExecution.getVariable("globalSubscriberId")).thenReturn("globalId_45678905678")

			when(mockExecution.getVariable("mso.adapters.po.auth")).thenReturn("3141634BF7E070AA289CF2892C986C0B")
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")

			when(mockExecution.getVariable("mso.rollback")).thenReturn("true")
			when(mockExecution.getVariable("mso.adapters.sdnc.endpoint")).thenReturn("http://localhost:8090/SDNCAdapter")
			when(mockExecution.getVariable("mso.adapters.network.rest.endpoint")).thenReturn("http://localhost:8090/networks/NetworkAdapter")
			when(mockExecution.getVariable("mso.adapters.sdnc.resource.endpoint")).thenReturn("http://localhost:8090/SDNCAdapterRpc")


			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.preProcessRequest(mockExecution)

//			verify(mockExecution).getVariable("isDebugLogEnabled")
			verify(mockExecution).setVariable("prefix", Prefix)

			//verify variable initialization
			initializeVariables(mockExecution)

			verify(mockExecution).setVariable("action", "CREATE")
			verify(mockExecution).setVariable(Prefix + "networkId","")
			verify(mockExecution).setVariable(Prefix + "networkRequest", expectedvIPRNetworkRequest)
			verify(mockExecution, atLeast(1)).setVariable(Prefix + "rollbackEnabled", false)
			verify(mockExecution).setVariable(Prefix + "networkInputs", expectedvIPRNetworkInputs)
			//verify(mockExecution).setVariable(Prefix + "requestId", "88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			//verify(mockExecution).setVariable("mso-service-instance-id", "88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			verify(mockExecution).setVariable(Prefix + "messageId", "88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			verify(mockExecution).setVariable(Prefix + "source", "VID")
			verify(mockExecution).setVariable("BasicAuthHeaderValuePO", "Basic cGFzc3dvcmQ=")
			verify(mockExecution).setVariable("BasicAuthHeaderValueSDNC", "Basic cGFzc3dvcmQ=")
			verify(mockExecution).setVariable(Prefix + "serviceInstanceId","f70e927b-6087-4974-9ef8-c5e4d5847ca4")
			verify(mockExecution, atLeast(1)).setVariable("GENGS_type", "service-instance")
			//verify(mockExecution, atLeast(1)).setVariable("mso-request-id", "88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			//verify(mockExecution, atLeast(1)).setVariable("msoRequestId", "88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			//verify(mockExecution).setVariable("mso-service-instance-id","88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			verify(mockExecution).setVariable(Prefix + "networkId","")
			verify(mockExecution).setVariable(Prefix + "networkOutputs", networkvIPROutputs)
			verify(mockExecution).setVariable(Prefix + "networkName","")

		}

		@Test
		//@Ignore
		public void preProcessRequest_JSON_NetworkRequest() {

			println "************ preProcessRequest_Payload ************* "
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables

			// Pre-defined value, testing Only
			when(mockExecution.getVariable("testMessageId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			// Inputs:
			// when(mockExecution.getVariable("requestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
			when(mockExecution.getVariable("requestAction")).thenReturn("CREATE")
			when(mockExecution.getVariable("networkId")).thenReturn("networkId")                                // optional
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("bpmnRequest")).thenReturn(jsonIncomingRequest)                      // JSON format
			when(mockExecution.getVariable("sdncVersion")).thenReturn("1610")                      // 1610 default
			when(mockExecution.getVariable("disableRollback")).thenReturn(true)

			when(mockExecution.getVariable("mso.adapters.po.auth")).thenReturn("3141634BF7E070AA289CF2892C986C0B")
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")

			when(mockExecution.getVariable("mso.rollback")).thenReturn("true")
			when(mockExecution.getVariable("mso.adapters.sdnc.endpoint")).thenReturn("http://localhost:8090/SDNCAdapter")
			when(mockExecution.getVariable("mso.adapters.network.rest.endpoint")).thenReturn("http://localhost:8090/networks/NetworkAdapter")
			when(mockExecution.getVariable("mso.adapters.sdnc.resource.endpoint")).thenReturn("http://localhost:8090/SDNCAdapterRpc")

			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.preProcessRequest(mockExecution)

//			verify(mockExecution).getVariable("isDebugLogEnabled")
			verify(mockExecution).setVariable("prefix", Prefix)

			//verify variable initialization
			initializeVariables(mockExecution)

			verify(mockExecution).setVariable("action", "CREATE")
			verify(mockExecution).setVariable(Prefix + "networkRequest", expectedJSONNetworkRequest)
			verify(mockExecution).setVariable(Prefix + "rollbackEnabled", false)
			verify(mockExecution).setVariable(Prefix + "networkInputs", expectedJSONNetworkInputs)
			//verify(mockExecution).setVariable(Prefix + "requestId", "88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			//verify(mockExecution).setVariable(Prefix + "messageId", "88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			verify(mockExecution).setVariable(Prefix + "source", "VID")
			verify(mockExecution).setVariable("BasicAuthHeaderValuePO", "Basic cGFzc3dvcmQ=")
			verify(mockExecution).setVariable("BasicAuthHeaderValueSDNC", "Basic cGFzc3dvcmQ=")
			verify(mockExecution).setVariable(Prefix + "serviceInstanceId","f70e927b-6087-4974-9ef8-c5e4d5847ca4")
			verify(mockExecution, atLeast(1)).setVariable("GENGS_type", "service-instance")
			//verify(mockExecution, atLeast(1)).setVariable("msoRequestId", "88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			//verify(mockExecution).setVariable("mso-service-instance-id","88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			verify(mockExecution).setVariable(Prefix + "networkId","")
			verify(mockExecution).setVariable(Prefix + "networkOutputs", networkJSONOutputs)
			verify(mockExecution).setVariable(Prefix + "networkName","")


		}

		@Test
		//@Ignore
		public void preProcessRequest_XML_NetworkRequest() {

			println "************ preProcessRequest_Payload ************* "
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables

			// Pre-defined value, testing Only
			when(mockExecution.getVariable("testMessageId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			// Inputs:
			// when(mockExecution.getVariable("msoRequestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("bpmnRequest")).thenReturn(xmlIncomingRequest)                      // XML format

			when(mockExecution.getVariable("mso.adapters.po.auth")).thenReturn("3141634BF7E070AA289CF2892C986C0B")
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")

			when(mockExecution.getVariable("mso.rollback")).thenReturn("true")
			when(mockExecution.getVariable("mso.adapters.sdnc.endpoint")).thenReturn("http://localhost:8090/SDNCAdapter")
			when(mockExecution.getVariable("mso.adapters.network.rest.endpoint")).thenReturn("http://localhost:8090/networks/NetworkAdapter")
			when(mockExecution.getVariable("mso.adapters.sdnc.resource.endpoint")).thenReturn("http://localhost:8090/SDNCAdapterRpc")

			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.preProcessRequest(mockExecution)

//			verify(mockExecution).getVariable("isDebugLogEnabled")
			verify(mockExecution).setVariable("prefix", Prefix)

			//verify variable initialization
			initializeVariables(mockExecution)

			verify(mockExecution).setVariable("action", "CREATE")
			verify(mockExecution).setVariable(Prefix + "networkRequest", expectedXMLNetworkRequest)
			verify(mockExecution).setVariable(Prefix + "rollbackEnabled", true)
			verify(mockExecution).setVariable(Prefix + "networkInputs", expectedXMLNetworkInputs)
			//verify(mockExecution).setVariable(Prefix + "requestId", "88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			verify(mockExecution).setVariable(Prefix + "messageId", "88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			verify(mockExecution).setVariable(Prefix + "source", "PORTAL")
			verify(mockExecution).setVariable("BasicAuthHeaderValuePO", "Basic cGFzc3dvcmQ=")
			verify(mockExecution).setVariable("BasicAuthHeaderValueSDNC", "Basic cGFzc3dvcmQ=")
			verify(mockExecution).setVariable(Prefix + "serviceInstanceId","MNS-25180-L-01-dmz_direct_net_1")
			verify(mockExecution, atLeast(1)).setVariable("GENGS_type", "service-instance")
			//verify(mockExecution).setVariable("mso-service-instance-id","88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			verify(mockExecution, atLeast(1)).setVariable(Prefix + "networkId","")
			verify(mockExecution).setVariable(Prefix + "networkOutputs", networkXMLOutputs)
			verify(mockExecution).setVariable(Prefix + "networkName","")

		}



		@Test
		//@Ignore
		public void prepareCreateNetworkRequest() {

			println "************ prepareNetworkRequest ************* "
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(expectedvIPRNetworkRequest)
			when(mockExecution.getVariable(Prefix + "queryIdAAIResponse")).thenReturn(queryIdAIIResponse)
			when(mockExecution.getVariable(Prefix + "cloudRegionPo")).thenReturn("RDM2WAGPLCP")
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("messageId_generated")
			when(mockExecution.getVariable(Prefix + "source")).thenReturn("VID")
			//when(mockExecution.getVariable(Prefix + "queryVpnBindingAAIResponse")).thenReturn(queryVpnBindingAAIResponse)
			when(mockExecution.getVariable(Prefix + "routeCollection")).thenReturn("<routeTargets>13979:105757</routeTargets><routeTargets>13979:105757</routeTargets>")
			when(mockExecution.getVariable(Prefix + "networkCollection")).thenReturn("<policyFqdns>GN_EVPN_Test</policyFqdns>")
			when(mockExecution.getVariable(Prefix + "tableRefCollection")).thenReturn("<routeTableFqdns>refFQDN1</routeTableFqdns><routeTableFqdns>refFQDN2</routeTableFqdns>")
			when(mockExecution.getVariable(Prefix + "requestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			//when(mockExecution.getVariable("URN_?????")).thenReturn("")   // notificationUrl, //TODO - is this coming from URN? What variable/value to use?
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable(Prefix + "rollbackEnabled")).thenReturn("true")
			when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")

			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.prepareCreateNetworkRequest(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			// verify set prefix = Prefix + ""
			verify(mockExecution).setVariable("prefix", Prefix)

			verify(mockExecution).setVariable(Prefix + "createNetworkRequest", createNetworkRequest)

		}


		@Test
		//@Ignore
		public void prepareCreateNetworkRequest_Ipv4() {

			println "************ prepareNetworkRequest ************* "
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(expectedvIPRNetworkRequest)
			when(mockExecution.getVariable(Prefix + "queryIdAAIResponse")).thenReturn(queryIdAIIResponse_Ipv4)
			when(mockExecution.getVariable(Prefix + "cloudRegionPo")).thenReturn("RDM2WAGPLCP")
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("messageId_generated")
			when(mockExecution.getVariable(Prefix + "source")).thenReturn("VID")
			//when(mockExecution.getVariable(Prefix + "queryVpnBindingAAIResponse")).thenReturn(queryVpnBindingAAIResponse)
			when(mockExecution.getVariable(Prefix + "routeCollection")).thenReturn("<routeTargets>13979:105757</routeTargets><routeTargets>13979:105757</routeTargets>")
			when(mockExecution.getVariable(Prefix + "networkCollection")).thenReturn("<policyFqdns>GN_EVPN_Test</policyFqdns>")
			when(mockExecution.getVariable(Prefix + "tableRefCollection")).thenReturn("<routeTableFqdns>refFQDN1</routeTableFqdns><routeTableFqdns>refFQDN2</routeTableFqdns>")
			when(mockExecution.getVariable(Prefix + "requestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			//when(mockExecution.getVariable("URN_?????")).thenReturn("")   // notificationUrl, //TODO - is this coming from URN? What variable/value to use?
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable(Prefix + "rollbackEnabled")).thenReturn("true")
			when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")

			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.prepareCreateNetworkRequest(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			// verify set prefix = Prefix + ""
			verify(mockExecution).setVariable("prefix", Prefix)

			verify(mockExecution).setVariable(Prefix + "createNetworkRequest", createNetworkRequest_Ipv4)

		}

		@Test
		//@Ignore
		public void prepareCreateNetworkRequest_AlaCarte() {

			println "************ prepareNetworkRequest ************* "
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(expectedJSONNetworkRequest)
			when(mockExecution.getVariable(Prefix + "queryIdAAIResponse")).thenReturn(queryIdAIIResponse_AlaCarte)
			when(mockExecution.getVariable(Prefix + "cloudRegionPo")).thenReturn("RDM2WAGPLCP")
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("messageId_generated")
			when(mockExecution.getVariable(Prefix + "source")).thenReturn("VID")
			//when(mockExecution.getVariable(Prefix + "queryVpnBindingAAIResponse")).thenReturn(queryVpnBindingAAIResponse)
			when(mockExecution.getVariable(Prefix + "routeCollection")).thenReturn("<routeTargets>13979:105757</routeTargets><routeTargets>13979:105757</routeTargets>")
			when(mockExecution.getVariable(Prefix + "networkCollection")).thenReturn("<policyFqdns>GN_EVPN_Test</policyFqdns>")
			when(mockExecution.getVariable(Prefix + "tableRefCollection")).thenReturn("<routeTableFqdns>refFQDN1</routeTableFqdns><routeTableFqdns>refFQDN2</routeTableFqdns>")
			when(mockExecution.getVariable(Prefix + "requestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			//when(mockExecution.getVariable("URN_?????")).thenReturn("")   // notificationUrl, //TODO - is this coming from URN? What variable/value to use?
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable(Prefix + "rollbackEnabled")).thenReturn("true")
			when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")

			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.prepareCreateNetworkRequest(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			// verify set prefix = Prefix + ""
			verify(mockExecution).setVariable("prefix", Prefix)

			verify(mockExecution).setVariable(Prefix + "createNetworkRequest", createNetworkRequestAlaCarte)

		}

		@Test
		//@Ignore
		public void prepareCreateNetworkRequest_SRIOV() {

			println "************ prepareNetworkRequest ************* "
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(expectedvIPRNetworkRequest)
			when(mockExecution.getVariable(Prefix + "queryIdAAIResponse")).thenReturn(queryIdAIIResponse_SRIOV)
			when(mockExecution.getVariable(Prefix + "cloudRegionPo")).thenReturn("RDM2WAGPLCP")
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("messageId_generated")
			when(mockExecution.getVariable(Prefix + "source")).thenReturn("VID")
			//when(mockExecution.getVariable(Prefix + "queryVpnBindingAAIResponse")).thenReturn(queryVpnBindingAAIResponse)
			when(mockExecution.getVariable(Prefix + "routeCollection")).thenReturn("<routeTargets>13979:105757</routeTargets><routeTargets>13979:105757</routeTargets>")
			when(mockExecution.getVariable(Prefix + "networkCollection")).thenReturn("<policyFqdns>GN_EVPN_Test</policyFqdns>")
			when(mockExecution.getVariable(Prefix + "tableRefCollection")).thenReturn("<routeTableFqdns>refFQDN1</routeTableFqdns><routeTableFqdns>refFQDN2</routeTableFqdns>")
			when(mockExecution.getVariable(Prefix + "requestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			//when(mockExecution.getVariable("URN_?????")).thenReturn("")   // notificationUrl, //TODO - is this coming from URN? What variable/value to use?
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable(Prefix + "rollbackEnabled")).thenReturn("true")
			when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")

			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.prepareCreateNetworkRequest(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			// verify set prefix = Prefix + ""
			verify(mockExecution).setVariable("prefix", Prefix)

			verify(mockExecution).setVariable(Prefix + "createNetworkRequest", createNetworkRequest_SRIOV)

		}


		@Test
		//@Ignore
		public void prepareSDNCRequest() {

			println "************ prepareSDNCRequest ************* "

			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(expectedvIPRNetworkRequest)
			when(mockExecution.getVariable(Prefix + "cloudRegionSdnc")).thenReturn("RDM2WAGPLCP")
			when(mockExecution.getVariable(Prefix + "serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
			when(mockExecution.getVariable(Prefix + "networkId")).thenReturn("networkId")
			when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("testMessageId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("http://localhost:8090/SDNCAdapterCallback")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("GENGSI_siResourceLink")).thenReturn("https://aai-int1.test.com:8443/aai/v8/business/customers/customer/MSO_1610_dev/service-subscriptions/service-subscription/MSO-dev-service-type/service-instances/service-instance/6d4eb22a-82f1-4257-9f80-4176262cfe69/")


			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.prepareSDNCRequest(mockExecution)

			// verify set prefix = Prefix + ""
			verify(mockExecution).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable(Prefix + "assignSDNCRequest", assignSDNCRequest)

		}

		@Test
		//@Ignore
		public void prepareSDNCRequest_decodeUrlLink() {

			println "************ prepareSDNCRequest ************* "

			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(expectedvIPRNetworkRequest)
			when(mockExecution.getVariable(Prefix + "cloudRegionSdnc")).thenReturn("RDM2WAGPLCP")
			when(mockExecution.getVariable(Prefix + "serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
			when(mockExecution.getVariable(Prefix + "networkId")).thenReturn("networkId")
			when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("testMessageId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("http://localhost:8090/SDNCAdapterCallback")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("GENGSI_siResourceLink")).thenReturn("https://aai-int1.test.com:8443/aai/v8/business/customers/customer/MSO_1610_dev/service-subscriptions/service-subscription/VIRTUAL%20USP/service-instances/service-instance/6d4eb22a-82f1-4257-9f80-4176262cfe69/")


			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.prepareSDNCRequest(mockExecution)

			// verify set prefix = Prefix + ""
			verify(mockExecution).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable(Prefix + "assignSDNCRequest", assignSDNCRequest_decodeUrlLink)

		}

		@Test
		//@Ignore
		public void prepareRpcSDNCRequest() {

			println "************ prepareRpcSDNCRequest ************* "

			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(expectedvIPRNetworkRequest)
			when(mockExecution.getVariable(Prefix + "cloudRegionSdnc")).thenReturn("RDM2WAGPLCP")
			when(mockExecution.getVariable(Prefix + "serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
			when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("msoRequestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("testMessageId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56") // test ONLY
			when(mockExecution.getVariable(Prefix + "networkId")).thenReturn("networkId")
			when(mockExecution.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("http://localhost:8090/SDNCAdapterCallback")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("GENGSI_siResourceLink")).thenReturn("https://aai-int1.test.com:8443/aai/v8/business/customers/customer/MSO_1610_dev/service-subscriptions/service-subscription/MSO-dev-service-type/service-instances/service-instance/6d4eb22a-82f1-4257-9f80-4176262cfe69/")

			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.prepareRpcSDNCRequest(mockExecution)

			// verify set prefix = Prefix + ""
			verify(mockExecution).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable(Prefix + "assignSDNCRequest", assignRpcSDNCRequest)

		}

		@Test
		//@Ignore
		public void prepareSDNCRollbackRequest() {

			println "************ prepareSDNCRollbackRequest ************* "

			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(expectedvIPRNetworkRequest)
			when(mockExecution.getVariable(Prefix + "cloudRegionSdnc")).thenReturn("RDM2WAGPLCP")
			when(mockExecution.getVariable(Prefix + "serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
			when(mockExecution.getVariable(Prefix + "assignSDNCResponse")).thenReturn(assignResponse)
			when(mockExecution.getVariable(Prefix + "networkId")).thenReturn("8abc633a-810b-4ca5-8b3a-09511d13a2ce")
			when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("testMessageId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("http://localhost:8090/SDNCAdapterCallback")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("GENGSI_siResourceLink")).thenReturn("https://aai-int1.test.com:8443/aai/v8/business/customers/customer/MSO_1610_dev/service-subscriptions/service-subscription/MSO-dev-service-type/service-instances/service-instance/6d4eb22a-82f1-4257-9f80-4176262cfe69/")

			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.prepareSDNCRollbackRequest(mockExecution)

			// verify set prefix = Prefix + ""
			verify(mockExecution).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable(Prefix + "rollbackSDNCRequest", sdncRollbackRequest)

		}

		@Test
		//@Ignore
		public void prepareRpcSDNCActivateRequest() {

			println "************ prepareRpcSDNCActivateRequest ************* "

			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(expectedvIPRNetworkRequest)
			when(mockExecution.getVariable(Prefix + "cloudRegionSdnc")).thenReturn("RDM2WAGPLCP")
			when(mockExecution.getVariable(Prefix + "serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
			when(mockExecution.getVariable(Prefix + "networkId")).thenReturn("networkId")
			when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("msoRequestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("testMessageId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56") // test ONLY
			when(mockExecution.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("http://localhost:8090/SDNCAdapterCallback")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("GENGSI_siResourceLink")).thenReturn("https://aai-int1.test.com:8443/aai/v8/business/customers/customer/MSO_1610_dev/service-subscriptions/service-subscription/MSO-dev-service-type/service-instances/service-instance/6d4eb22a-82f1-4257-9f80-4176262cfe69/")

			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.prepareRpcSDNCActivateRequest(mockExecution)

			// verify set prefix = Prefix + ""
			verify(mockExecution).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable(Prefix + "activateSDNCRequest", activateSDNCRequest)

		}


		@Test
		//@Ignore
		public void prepareRpcSDNCRollbackRequest() {

			println "************ prepareRpcSDNCRollbackRequest ************* "

			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(expectedvIPRNetworkRequest)
			when(mockExecution.getVariable(Prefix + "cloudRegionSdnc")).thenReturn("RDM2WAGPLCP")
			when(mockExecution.getVariable(Prefix + "serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
			when(mockExecution.getVariable(Prefix + "assignSDNCResponse")).thenReturn(assignResponse)
			when(mockExecution.getVariable(Prefix + "networkId")).thenReturn("8abc633a-810b-4ca5-8b3a-09511d13a2ce")
			when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("testMessageId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56") // test ONLY
			when(mockExecution.getVariable("msoRequestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("http://localhost:8090/SDNCAdapterCallback")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("GENGSI_siResourceLink")).thenReturn("https://aai-int1.test.com:8443/aai/v8/business/customers/customer/MSO_1610_dev/service-subscriptions/service-subscription/MSO-dev-service-type/service-instances/service-instance/6d4eb22a-82f1-4257-9f80-4176262cfe69/")

			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.prepareRpcSDNCRollbackRequest(mockExecution)

			// verify set prefix = Prefix + ""
			verify(mockExecution).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable(Prefix + "rollbackSDNCRequest", sdncRpcRollbackRequest)

		}

		@Test
		//@Ignore
		public void prepareRpcSDNCActivateRollback() {

			println "************ prepareRpcSDNCActivateRollback ************* "

			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(expectedvIPRNetworkRequest)
			when(mockExecution.getVariable(Prefix + "cloudRegionSdnc")).thenReturn("RDM2WAGPLCP")
			when(mockExecution.getVariable(Prefix + "serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
			when(mockExecution.getVariable(Prefix + "activateSDNCResponse")).thenReturn(assignResponse)
			when(mockExecution.getVariable(Prefix + "networkId")).thenReturn("8abc633a-810b-4ca5-8b3a-09511d13a2ce")
			when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("msoRequestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("testMessageId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56") // test ONLY
			when(mockExecution.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("http://localhost:8090/SDNCAdapterCallback")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("GENGSI_siResourceLink")).thenReturn("https://aai-int1.test.com:8443/aai/v8/business/customers/customer/MSO_1610_dev/service-subscriptions/service-subscription/MSO-dev-service-type/service-instances/service-instance/6d4eb22a-82f1-4257-9f80-4176262cfe69/")

			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.prepareRpcSDNCActivateRollback(mockExecution)

			// verify set prefix = Prefix + ""
			verify(mockExecution).setVariable("prefix", Prefix)
			//verify(mockExecution).setVariable("mso-request-id", "88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			//verify(mockExecution).setVariable(Prefix + "requestId", "88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			//verify(mockExecution).setVariable("mso-service-instance-id", "88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			verify(mockExecution).setVariable(Prefix + "rollbackActivateSDNCRequest", sdncActivateRollbackRequest)

		}

		@Test
		//@Ignore
		public void callRESTQueryAAINetworkName_200() {

			println "************ callRESTQueryAAINetworkName ************* "

			WireMock.reset();
			MockGetNetworkByName("MNS-25180-L-01-dmz_direct_net_1", "CreateNetworkV2/createNetwork_queryName_AAIResponse_Success.xml");

			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable(Prefix + "networkInputs")).thenReturn(networkInputs)
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:8090")
			// old: when(mockExecution.getVariable("mso.workflow.DoCreateNetworkInstance.aai.network.l3-network.uri")).thenReturn("/aai/v8/network/l3-networks/l3-network")
			when(mockExecution.getVariable("mso.workflow.DoCreateNetworkInstance.aai.l3-network.uri")).thenReturn("/aai/v8/network/l3-networks/l3-network")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.callRESTQueryAAINetworkName(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable(Prefix + "queryNameAAIRequest", "http://localhost:8090/aai/v8/network/l3-networks/l3-network?network-name=MNS-25180-L-01-dmz_direct_net_1")

			verify(mockExecution).setVariable(Prefix + "aaiNameReturnCode", "200")
			verify(mockExecution).setVariable(Prefix + "orchestrationStatus", "PENDING-CREATE")

		}

		@Test
		//@Ignore
		public void callRESTQueryAAINetworkName_404() {

			println "************ callRESTQueryAAINetworkName ************* "

			WireMock.reset();
			MockGetNetworkByName_404("CreateNetworkV2/createNetwork_queryName_AAIResponse_Success.xml", "myOwn_Network");

			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable(Prefix + "networkInputs")).thenReturn(networkInputs_404)
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:8090")
			// old: when(mockExecution.getVariable("mso.workflow.DoCreateNetworkInstance.aai.network.l3-network.uri")).thenReturn("/aai/v8/network/l3-networks/l3-network")
			when(mockExecution.getVariable("mso.workflow.DoCreateNetworkInstance.aai.l3-network.uri")).thenReturn("/aai/v8/network/l3-networks/l3-network")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.callRESTQueryAAINetworkName(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution, atLeast(1)).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable(Prefix + "queryNameAAIRequest", "http://localhost:8090/aai/v8/network/l3-networks/l3-network?network-name=myOwn_Network")
			verify(mockExecution).setVariable(Prefix + "aaiNameReturnCode", "404")

		}

		@Test
		//@Ignore
		public void callRESTQueryAAINetworkId_200() {

			println "************ callRESTQueryAAINetworkId ************* "

			WireMock.reset();
			MockGetNetworkByIdWithDepth("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "CreateNetworkV2/createNetwork_queryNetworkId_AAIResponse_Success.xml", "all");

			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable(Prefix + "assignSDNCResponse")).thenReturn(sdncAdapterWorkflowAssignResponse)
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:8090")
			// old: when(mockExecution.getVariable("mso.workflow.DoCreateNetworkInstance.aai.network.l3-network.uri")).thenReturn("/aai/v8/network/l3-networks/l3-network")
			when(mockExecution.getVariable("mso.workflow.DoCreateNetworkInstance.aai.l3-network.uri")).thenReturn("/aai/v9/network/l3-networks/l3-network")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("sdncVersion")).thenReturn("1702")
			when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.callRESTQueryAAINetworkId(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable(Prefix + "queryIdAAIRequest", "http://localhost:8090/aai/v9/network/l3-networks/l3-network/49c86598-f766-46f8-84f8-8d1c1b10f9b4"+"?depth=all")
			verify(mockExecution).setVariable(Prefix + "aaiIdReturnCode", "200")

		}

		@Test
		//@Ignore
		public void callRESTQueryAAICloudRegion30_200() {

			println "************ callRESTQueryAAICloudRegion30_200 ************* "

			WireMock.reset();
			MockGetNetworkCloudRegion("CreateNetworkV2/cloudRegion25_AAIResponse_Success.xml", "RDM2WAGPLCP");

			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable("prefix")).thenReturn(Prefix)
			when(mockExecution.getVariable(Prefix + "networkInputs")).thenReturn(expectedvIPRNetworkRequest)
			when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:8090")
			// old: when(mockExecution.getVariable("mso.workflow.default.aai.cloud-infrastructure.cloud-region.uri")).thenReturn("/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner")
			when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn("8")
			when(mockExecution.getVariable("mso.workflow.default.aai.v8.cloud-region.uri")).thenReturn("/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner")
			//
			when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.callRESTQueryAAICloudRegion(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution, atLeast(1)).setVariable("prefix", Prefix)
			verify(mockExecution, atLeast(2)).setVariable(Prefix + "queryCloudRegionReturnCode", "200")
			verify(mockExecution).setVariable(Prefix + "isCloudRegionGood", true)

		}

		@Test
		//@Ignore
		public void callRESTQueryAAICloudRegion25_200() {

			println "************ callRESTQueryAAICloudRegion25_200 ************* "

			WireMock.reset();
			MockGetNetworkCloudRegion("CreateNetworkV2/cloudRegion25_AAIResponse_Success.xml", "RDM2WAGPLCP");

			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable("prefix")).thenReturn(Prefix)
			when(mockExecution.getVariable(Prefix + "networkInputs")).thenReturn(expectedvIPRNetworkRequest)
			when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:8090")
			// old: when(mockExecution.getVariable("mso.workflow.default.aai.cloud-infrastructure.cloud-region.uri")).thenReturn("/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner")
			when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn("8")
			when(mockExecution.getVariable("mso.workflow.default.aai.v8.cloud-region.uri")).thenReturn("/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner")
			//
			when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.callRESTQueryAAICloudRegion(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution, atLeast(1)).setVariable("prefix", Prefix)
			verify(mockExecution, atLeast(2)).setVariable(Prefix + "queryCloudRegionReturnCode", "200")
			verify(mockExecution).setVariable(Prefix + "isCloudRegionGood", true)

		}

		@Test
		//@Ignore
		public void callRESTQueryAAICloudRegion_NotFound() {

			println "************ callRESTQueryAAICloudRegionFake ************* "

			WireMock.reset();
			MockGetNetworkCloudRegion_404("MDTWNJ21")

			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable("prefix")).thenReturn(Prefix)
			when(mockExecution.getVariable(Prefix + "networkInputs")).thenReturn(vnfRequestFakeRegion)
			when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:8090")
			//old: when(mockExecution.getVariable("mso.workflow.default.aai.cloud-infrastructure.cloud-region.uri")).thenReturn("/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner")
			when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn("8")
			when(mockExecution.getVariable("mso.workflow.default.aai.v8.cloud-region.uri")).thenReturn("/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner")
			//
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.callRESTQueryAAICloudRegion(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution, atLeast(1)).setVariable("prefix", Prefix)
			verify(mockExecution, atLeast(1)).setVariable(Prefix + "queryCloudRegionReturnCode", "404")
			verify(mockExecution).setVariable(Prefix + "cloudRegionPo", "MDTWNJ21")
			verify(mockExecution).setVariable(Prefix + "cloudRegionSdnc", "AAIAIC25")
			verify(mockExecution).setVariable(Prefix + "isCloudRegionGood", true)

		}

		@Test
		//@Ignore
		public void callRESTQueryAAINetworkVpnBinding_200() {

			println "************ callRESTQueryAAINetworkVpnBinding_200 ************* "

			WireMock.reset();
			MockGetNetworkVpnBinding("CreateNetworkV2/createNetwork_queryVpnBinding_AAIResponse_Success.xml", "85f015d0-2e32-4c30-96d2-87a1a27f8017");
			MockGetNetworkVpnBinding("CreateNetworkV2/createNetwork_queryVpnBinding_AAIResponse_Success.xml", "c980a6ef-3b88-49f0-9751-dbad8608d0a6");

			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable(Prefix + "queryIdAAIResponse")).thenReturn(queryIdAIIResponse) // v6
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:8090")
			// old: when(mockExecution.getVariable("mso.workflow.default.aai.network.vpn-binding.uri")).thenReturn("")
			// old: when(mockExecution.getVariable("mso.workflow.DoCreateNetworkInstance.aai.network.vpn-binding.uri")).thenReturn("")
			when(mockExecution.getVariable("mso.workflow.DoCreateNetworkInstance.aai.vpn-binding.uri")).thenReturn("/aai/v8/network/vpn-bindings/vpn-binding")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.callRESTQueryAAINetworkVpnBinding(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable(Prefix + "vpnCount", 2)
			verify(mockExecution).setVariable(Prefix + "vpnBindings", ['/aai/v8/network/vpn-bindings/vpn-binding/85f015d0-2e32-4c30-96d2-87a1a27f8017/', '/aai/v8/network/vpn-bindings/vpn-binding/c980a6ef-3b88-49f0-9751-dbad8608d0a6/'])
			// the last vpnBinding value is saved.
			verify(mockExecution).setVariable(Prefix + "queryVpnBindingAAIRequest", "http://localhost:8090/aai/v8/network/vpn-bindings/vpn-binding/85f015d0-2e32-4c30-96d2-87a1a27f8017?depth=all")
			verify(mockExecution, atLeast(2)).setVariable(Prefix + "aaiQqueryVpnBindingReturnCode", "200")

		}

		@Test
		//@Ignore
		public void callRESTQueryAAINetworkVpnBindingList_200() {

			println "************ callRESTQueryAAINetworkVpnBinding_200 ************* "

			WireMock.reset();
			MockGetNetworkVpnBinding("CreateNetworkV2/createNetwork_queryVpnBindingList_AAIResponse_Success.xml", "85f015d0-2e32-4c30-96d2-87a1a27f8017");
			MockGetNetworkVpnBinding("CreateNetworkV2/createNetwork_queryVpnBindingList_AAIResponse_Success.xml", "c980a6ef-3b88-49f0-9751-dbad8608d0a6");

			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable(Prefix + "queryIdAAIResponse")).thenReturn(queryIdAIIResponse) // v6
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:8090")
			when(mockExecution.getVariable("mso.workflow.DoCreateNetworkInstance.aai.vpn-binding.uri")).thenReturn("/aai/v8/network/vpn-bindings/vpn-binding")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.callRESTQueryAAINetworkVpnBinding(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable(Prefix + "vpnCount", 2)
			verify(mockExecution).setVariable(Prefix + "vpnBindings", ['/aai/v8/network/vpn-bindings/vpn-binding/85f015d0-2e32-4c30-96d2-87a1a27f8017/', '/aai/v8/network/vpn-bindings/vpn-binding/c980a6ef-3b88-49f0-9751-dbad8608d0a6/'])
			// the last vpnBinding value is saved.
			verify(mockExecution).setVariable(Prefix + "queryVpnBindingAAIRequest", "http://localhost:8090/aai/v8/network/vpn-bindings/vpn-binding/85f015d0-2e32-4c30-96d2-87a1a27f8017?depth=all")
			verify(mockExecution, atLeast(2)).setVariable(Prefix + "aaiQqueryVpnBindingReturnCode", "200")

		}

		@Test
		//@Ignore
		public void callRESTQueryAAINetworkVpnBinding_TestScenario01_200() {

			println "************ callRESTQueryAAINetworkVpnBinding_200 ************* "

			WireMock.reset();
			MockGetNetworkVpnBinding("CreateNetworkV2/createNetwork_queryVpnBinding_AAIResponse_Success.xml", "85f015d0-2e32-4c30-96d2-87a1a27f8017");

			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable(Prefix + "queryIdAAIResponse")).thenReturn(queryIdAIIResponseTestScenario01)
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:8090")
			// old: when(mockExecution.getVariable("mso.workflow.default.aai.network.vpn-binding.uri")).thenReturn("")
			// old: when(mockExecution.getVariable("mso.workflow.DoCreateNetworkInstance.aai.network.vpn-binding.uri")).thenReturn("")
			when(mockExecution.getVariable("mso.workflow.DoCreateNetworkInstance.aai.vpn-binding.uri")).thenReturn("/aai/v8/network/vpn-bindings/vpn-binding")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.callRESTQueryAAINetworkVpnBinding(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable(Prefix + "vpnCount", 1)
			verify(mockExecution).setVariable(Prefix + "vpnBindings", ['/aai/v8/network/vpn-bindings/vpn-binding/85f015d0-2e32-4c30-96d2-87a1a27f8017/'])
			// the last vpnBinding value is saved.
			verify(mockExecution).setVariable(Prefix + "queryVpnBindingAAIRequest", "http://localhost:8090/aai/v8/network/vpn-bindings/vpn-binding/85f015d0-2e32-4c30-96d2-87a1a27f8017?depth=all")
			verify(mockExecution).setVariable(Prefix + "aaiQqueryVpnBindingReturnCode", "200")

		}

		@Test
		//@Ignore
		public void callRESTQueryAAINetworkVpnBinding_200_URN_Uri() {

			println "************ callRESTQueryAAINetworkVpnBinding_200 ************* "

			WireMock.reset();
			MockGetNetworkVpnBinding("CreateNetworkV2/createNetwork_queryVpnBinding_AAIResponse_Success.xml", "85f015d0-2e32-4c30-96d2-87a1a27f8017");
			MockGetNetworkVpnBinding("CreateNetworkV2/createNetwork_queryVpnBinding_AAIResponse_Success.xml", "c980a6ef-3b88-49f0-9751-dbad8608d0a6");

			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable(Prefix + "queryIdAAIResponse")).thenReturn(queryIdAIIResponse)
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:8090")
			//when(mockExecution.getVariable("mso.workflow.DoCreateNetworkInstance.aai.network.vpn-binding.uri")).thenReturn("/aai/v8/network/vpn-bindings/vpn-binding")
			//when(mockExecution.getVariable("mso.workflow.default.aai.network.vpn-binding.uri")).thenReturn("/aai/v8/network/vpn-bindings/vpn-binding")
			when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn("8")
			when(mockExecution.getVariable("mso.workflow.default.aai.v8.vpn-binding.uri")).thenReturn("/aai/v8/network/vpn-bindings/vpn-binding")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.callRESTQueryAAINetworkVpnBinding(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable(Prefix + "vpnCount", 2)
			verify(mockExecution).setVariable(Prefix + "vpnBindings", ['/aai/v8/network/vpn-bindings/vpn-binding/85f015d0-2e32-4c30-96d2-87a1a27f8017/', '/aai/v8/network/vpn-bindings/vpn-binding/c980a6ef-3b88-49f0-9751-dbad8608d0a6/'])
			// the last vpnBinding value is saved.
			verify(mockExecution).setVariable(Prefix + "queryVpnBindingAAIRequest", "http://localhost:8090/aai/v8/network/vpn-bindings/vpn-binding/85f015d0-2e32-4c30-96d2-87a1a27f8017?depth=all")
			verify(mockExecution, atLeast(2)).setVariable(Prefix + "aaiQqueryVpnBindingReturnCode", "200")

		}

		@Test
		//@Ignore
		public void callRESTQueryAAINetworkVpnBinding_NotPresent() {

			println "************ callRESTQueryAAINetworkVpnBinding_NotPresent ************* "

			WireMock.reset();
			MockGetNetworkVpnBinding("CreateNetworkV2/createNetwork_queryVpnBinding_AAIResponse_Success.xml", "85f015d0-2e32-4c30-96d2-87a1a27f8017");

			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables

			when(mockExecution.getVariable(Prefix + "queryIdAAIResponse")).thenReturn(queryIdAIIResponseVpnNotPresent)
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:8090")
			//when(mockExecution.getVariable("mso.workflow.default.aai.network.l3-network.uri")).thenReturn("/aai/v8/network/l3-networks/l3-network")
			when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn("8")
			when(mockExecution.getVariable("mso.workflow.default.aai.v8.l3-network.uri")).thenReturn("/aai/v8/network/l3-networks/l3-network")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.callRESTQueryAAINetworkVpnBinding(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable(Prefix + "aaiQqueryVpnBindingReturnCode", "200")
			verify(mockExecution).setVariable(Prefix + "vpnCount", 0)
			verify(mockExecution).setVariable(Prefix + "queryVpnBindingAAIResponse", aaiVpnResponseStub)

		}

		@Test
		//@Ignore
		public void callRESTQueryAAINetworkPolicy_200() {

			println "************ callRESTQueryAAINetworkPolicy_200 ************* "

			WireMock.reset();
			MockGetNetworkPolicy("CreateNetworkV2/createNetwork_queryNetworkPolicy_AAIResponse_Success.xml", "cee6d136-e378-4678-a024-2cd15f0ee0cg");

			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable(Prefix + "queryIdAAIResponse")).thenReturn(queryIdAIIResponse)
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:8090")
			//when(mockExecution.getVariable("mso.workflow.DoCreateNetworkInstance.aai.network-policy.uri")).thenReturn("")
			// old: when(mockExecution.getVariable("mso.workflow.default.aai.network-policy.uri")).thenReturn("/aai/v8/network/network-policies/network-policy")
			when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn("8")
			when(mockExecution.getVariable("mso.workflow.default.aai.v8.network-policy.uri")).thenReturn("/aai/v8/network/network-policies/network-policy")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.callRESTQueryAAINetworkPolicy(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable(Prefix + "networkPolicyCount", 1)
			verify(mockExecution).setVariable(Prefix + "networkPolicyUriList", ['/aai/v8/network/network-policies/network-policy/cee6d136-e378-4678-a024-2cd15f0ee0cg'])
			// the last vpnBinding value is saved.
			verify(mockExecution).setVariable(Prefix + "queryNetworkPolicyAAIRequest", "http://localhost:8090/aai/v8/network/network-policies/network-policy/cee6d136-e378-4678-a024-2cd15f0ee0cg?depth=all")
			verify(mockExecution).setVariable(Prefix + "aaiQqueryNetworkPolicyReturnCode", "200")

		}

		@Test
		//@Ignore
		public void callRESTQueryAAINetworkTableRef_200() {

			println "************ callRESTQueryAAINetworkTableRef_200 ************* "

			WireMock.reset();
			MockGetNetworkTableReference("CreateNetworkV2/createNetwork_queryNetworkTableRef1_AAIResponse_Success.xml", "refFQDN1");
			MockGetNetworkTableReference("CreateNetworkV2/createNetwork_queryNetworkTableRef2_AAIResponse_Success.xml", "refFQDN2");

			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable(Prefix + "queryIdAAIResponse")).thenReturn(queryIdAIIResponse)
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:8090")
			when(mockExecution.getVariable("mso.workflow.default.aai.network-table-reference.uri")).thenReturn("")
			// old: when(mockExecution.getVariable("mso.workflow.DoCreateNetworkInstance.aai.network-table-reference.uri")).thenReturn("")
			when(mockExecution.getVariable("mso.workflow.DoCreateNetworkInstance.aai.route-table-reference.uri")).thenReturn("/aai/v8/network/route-table-references/route-table-reference")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.callRESTQueryAAINetworkTableRef(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable(Prefix + "networkTableRefCount", 2)
			verify(mockExecution).setVariable(Prefix + "networkTableRefUriList", ['/aai/v8/network/route-table-references/route-table-reference/refFQDN1','/aai/v8/network/route-table-references/route-table-reference/refFQDN2'])
			// the last vpnBinding value is saved.
			verify(mockExecution).setVariable(Prefix + "queryNetworkTableRefAAIRequest", "http://localhost:8090/aai/v8/network/route-table-references/route-table-reference/refFQDN1?depth=all")
			verify(mockExecution, atLeast(2)).setVariable(Prefix + "aaiQqueryNetworkTableRefReturnCode", "200")

		}

		@Test
		//@Ignore
		public void callRESTReQueryAAINetworkId_200() {

			println "************ callRESTReQueryAAINetworkId ************* "

			WireMock.reset();
			MockGetNetworkByIdWithDepth("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "CreateNetworkV2/createNetwork_queryNetworkId_AAIResponse_Success.xml", "all");

			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable(Prefix + "networkId")).thenReturn("49c86598-f766-46f8-84f8-8d1c1b10f9b4")
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:8090")
			//when(mockExecution.getVariable("mso.workflow.default.aai.l3-network.version")).thenReturn("8")
			when(mockExecution.getVariable("mso.workflow.DoCreateNetworkInstance.aai.l3-network.uri")).thenReturn("/aai/v9/network/l3-networks/l3-network")
			//old: when(mockExecution.getVariable("mso.workflow.DoCreateNetworkInstance.aai.network.l3-network.uri")).thenReturn("/aai/v8/network/l3-networks/l3-network")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("sdncVersion")).thenReturn("1702")
			when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.callRESTReQueryAAINetworkId(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable(Prefix + "requeryIdAAIRequest", "http://localhost:8090/aai/v9/network/l3-networks/l3-network/49c86598-f766-46f8-84f8-8d1c1b10f9b4"+"?depth=all")
			verify(mockExecution).setVariable(Prefix + "aaiRequeryIdReturnCode", "200")

		}

		@Test
		//@Ignore
		public void callRESTUpdateContrailAAINetworkREST_200() {
			AAIResourcesClient mockClient = mock(AAIResourcesClient.class)
			WireMock.reset();
			L3Network network = new L3Network()

			//TODO need to inject mock
			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable(Prefix + "networkId")).thenReturn("49c86598-f766-46f8-84f8-8d1c1b10f9b4")
			when(mockExecution.getVariable(Prefix + "requeryIdAAIResponse")).thenReturn(network)
			when(mockExecution.getVariable(Prefix + "createNetworkResponse")).thenReturn(createNetworkResponseREST)
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")

			// old: when(mockExecution.getVariable("mso.workflow.DoCreateNetworkInstance.aai.network.l3-network.uri")).thenReturn("/aai/v8/network/l3-networks/l3-network")
			when(mockExecution.getVariable("mso.workflow.DoCreateNetworkInstance.aai.l3-network.uri")).thenReturn("/aai/v9/network/l3-networks/l3-network")
			when(mockExecution.getVariable(Prefix + "rollbackEnabled")).thenReturn("false")
			when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

			doNothing().when(mockClient).update(isA(AAIResourceUri.class), isA(L3Network.class))
			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.callRESTUpdateContrailAAINetwork(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable(Prefix + "updateContrailAAIUrlRequest", "http://localhost:8090/aai/v9/network/l3-networks/l3-network/49c86598-f766-46f8-84f8-8d1c1b10f9b4"+"?depth=all")
			verify(mockExecution).setVariable(Prefix + "updateContrailAAIPayloadRequest", updateContrailAAIPayloadRequest)
			verify(mockExecution).setVariable(Prefix + "aaiUpdateContrailReturnCode", "200")
			//verify(mockExecution).setVariable(Prefix + "updateContrailAAIResponse", updateContrailAAIResponse)
			verify(mockExecution).setVariable(Prefix + "isPONR", true)

		}

		@Test
		//@Ignore
		public void callRESTUpdateContrailAAINetworkREST_200_segmentation() {

			println "************ callRESTUpdateContrailAAINetwork ************* "

			WireMock.reset();
			MockPutNetworkIdWithDepth("CreateNetworkV2/createNetwork_updateContrail_AAIResponse_Success.xml", "49c86598-f766-46f8-84f8-8d1c1b10f9b4", "all");

			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable(Prefix + "networkId")).thenReturn("49c86598-f766-46f8-84f8-8d1c1b10f9b4")
			when(mockExecution.getVariable(Prefix + "requeryIdAAIResponse")).thenReturn(queryIdAIIResponse_segmentation)
			when(mockExecution.getVariable(Prefix + "createNetworkResponse")).thenReturn(createNetworkResponseREST)
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:8090")
			// old: when(mockExecution.getVariable("mso.workflow.DoCreateNetworkInstance.aai.network.l3-network.uri")).thenReturn("/aai/v8/network/l3-networks/l3-network")
			when(mockExecution.getVariable("mso.workflow.DoCreateNetworkInstance.aai.l3-network.uri")).thenReturn("/aai/v9/network/l3-networks/l3-network")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable(Prefix + "rollbackEnabled")).thenReturn("false")
			when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.callRESTUpdateContrailAAINetwork(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable(Prefix + "updateContrailAAIUrlRequest", "http://localhost:8090/aai/v9/network/l3-networks/l3-network/49c86598-f766-46f8-84f8-8d1c1b10f9b4"+"?depth=all")
			verify(mockExecution).setVariable(Prefix + "updateContrailAAIPayloadRequest", updateContrailAAIPayloadRequest_segmentation)
			verify(mockExecution).setVariable(Prefix + "aaiUpdateContrailReturnCode", "200")
			//verify(mockExecution).setVariable(Prefix + "updateContrailAAIResponse", updateContrailAAIResponse)
			verify(mockExecution).setVariable(Prefix + "isPONR", true)

		}



		@Test
		//@Ignore
		public void validateCreateNetworkResponseREST() {

			println "************ validateNetworkResponse ************* "

			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable(Prefix + "createNetworkResponse")).thenReturn(createNetworkResponseREST)
			when(mockExecution.getVariable(Prefix + "networkReturnCode")).thenReturn('200')

			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.validateCreateNetworkResponse(mockExecution)

			//debugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable(Prefix + "createNetworkResponse", createNetworkResponseREST)
			verify(mockExecution).setVariable(Prefix + "isNetworkRollbackNeeded", true)
			verify(mockExecution).setVariable(Prefix + "rollbackNetworkRequest", createRollbackNetworkRequest)

		}

		@Test
		//@Ignore
		public void validateCreateNetworkResponseREST_Error() {

			println "************ validateNetworkResponse ************* "

			WorkflowException workflowException = new WorkflowException("DoCreateNetworkInstance", 2500, "Received error from Network Adapter: JBWEB000065: HTTP Status 500.")

			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable(Prefix + "createNetworkResponse")).thenReturn(networkException500)
			when(mockExecution.getVariable(Prefix + "networkReturnCode")).thenReturn('500')

			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			try {
				DoCreateNetworkInstance.validateCreateNetworkResponse(mockExecution)
			} catch (Exception ex) {
				println " Test End - Handle catch-throw BpmnError()! "
			}

			verify(mockExecution).setVariable("prefix", Prefix)
			verify(mockExecution, atLeast(1)).setVariable("WorkflowException", refEq(workflowException, any(WorkflowException.class)))

		}

		@Test
		//@Ignore
		public void validateSDNCResponse() {

			println "************ validateSDNCResponse ************* "

			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable(Prefix + "assignSDNCResponse")).thenReturn(sdncAdapterWorkflowResponse)
			when(mockExecution.getVariable("prefix")).thenReturn(Prefix + "")
			when(mockExecution.getVariable("SDNCA_SuccessIndicator")).thenReturn(true)
			when(mockExecution.getVariable(Prefix + "sdncReturnCode")).thenReturn("200")
			when(mockExecution.getVariable(Prefix + "isResponseGood")).thenReturn(true)

			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			try {
			  DoCreateNetworkInstance.validateSDNCResponse(mockExecution)
			  verify(mockExecution).setVariable(Prefix + "isSdncRollbackNeeded", true)

			} catch (Exception ex) {
				println " Graceful Exit - " + ex.getMessage()
			}
			//debugger.printInvocations(mockExecution)

			//verify(mockExecution).setVariable(Prefix + "isSdncRollbackNeeded", true)

		}

		@Test
		//@Ignore
		public void validateSDNCResponse_Error() {

			println "************ validateSDNCResponse ************* "

			WorkflowException workflowException = new WorkflowException("DoCreateNetworkInstance", 2500, "Received error from SNDC Adapter: HTTP Status 500.")

			//ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable(Prefix + "assignSDNCResponse")).thenReturn(sdncAdapterWorkflowResponse_Error)
			when(mockExecution.getVariable("prefix")).thenReturn(Prefix + "")
			when(mockExecution.getVariable("SDNCA_SuccessIndicator")).thenReturn(false)
			when(mockExecution.getVariable(Prefix + "sdncReturnCode")).thenReturn("200")
			when(mockExecution.getVariable("WorkflowException")).thenReturn(workflowException)


			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			try {
				DoCreateNetworkInstance.validateSDNCResponse(mockExecution)
			} catch (Exception ex) {
				println " Graceful Exit! - " + ex.getMessage()
			}
			//debugger.printInvocations(mockExecution)

			// verify set prefix = Prefix + ""
			//verify(mockExecution).setVariable(Prefix + "sdncResponseSuccess", false)

		}

		@Test
		//@Ignore
		public void validateRpcSDNCActivateResponse() {

			println "************ validateRpcSDNCActivateResponse ************* "

			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable(Prefix + "activateSDNCResponse")).thenReturn(sdncAdapterWorkflowResponse)
			when(mockExecution.getVariable("prefix")).thenReturn(Prefix + "")
			when(mockExecution.getVariable("SDNCA_SuccessIndicator")).thenReturn(true)
			when(mockExecution.getVariable(Prefix + "sdncActivateReturnCode")).thenReturn("200")

			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			try {
			  DoCreateNetworkInstance.validateRpcSDNCActivateResponse(mockExecution)
			  verify(mockExecution).setVariable(Prefix + "isSdncActivateRollbackNeeded", true)

			} catch (Exception ex) {
				println " Graceful Exit - " + ex.getMessage()
			}
			//debugger.printInvocations(mockExecution)

			//verify(mockExecution).setVariable(Prefix + "isSdncRollbackNeeded", true)

		}

		@Test
		//@Ignore
		public void prepareRollbackData() {

			println "************ prepareRollbackData() ************* "



			WorkflowException workflowException = new WorkflowException("DoCreateNetworkInstance", 2500, "Received error from Network Adapter: JBWEB000065: HTTP Status 500.")

			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable(Prefix + "rollbackSDNCRequest")).thenReturn(rollbackSDNCRequest)
			when(mockExecution.getVariable(Prefix + "rollbackActivateSDNCRequest")).thenReturn(rollbackActivateSDNCRequest)
			when(mockExecution.getVariable(Prefix + "rollbackNetworkRequest")).thenReturn(rollbackNetworkRequest)
			when(mockExecution.getVariable("WorkflowException")).thenReturn(workflowException)

			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.prepareRollbackData(mockExecution)

//			verify(mockExecution).getVariable("isDebugLogEnabled")
			verify(mockExecution).setVariable("prefix", Prefix)

		}

		@Test
		public void postProcessResponse() {

			println "************ postProcessResponse() ************* "
			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("testProcessKey")).thenReturn("DoCreateNetworkInstanceTest")
			when(mockExecution.getVariable(Prefix + "isException")).thenReturn(false)
			when(mockExecution.getVariable("sdncVersion")).thenReturn("1702")
			when(mockExecution.getVariable(Prefix + "rollbackSDNCRequest")).thenReturn(sdncRpcRollbackRequest)
			when(mockExecution.getVariable(Prefix + "rollbackNetworkRequest")).thenReturn(rollbackSDNCRequest)
			when(mockExecution.getVariable(Prefix + "rollbackActivateSDNCRequest")).thenReturn(sdncActivateRollbackRequest)


			// preProcessRequest(DelegateExecution execution)
			DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
			DoCreateNetworkInstance.postProcessResponse(mockExecution)

//			verify(mockExecution,atLeastOnce()).getVariable("isDebugLogEnabled")
			verify(mockExecution,atLeastOnce()).setVariable("prefix", Prefix)
			verify(mockExecution,atLeastOnce()).setVariable(Prefix + "Success", true)

		}

		private ExecutionEntity setupMock() {

			ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
			when(mockProcessDefinition.getKey()).thenReturn("DoCreateNetworkInstance")
			RepositoryService mockRepositoryService = mock(RepositoryService.class)
			when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
			when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("DoCreateNetworkInstance")
			when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
			ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
			when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)

			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables

			when(mockExecution.getId()).thenReturn("100")
			when(mockExecution.getProcessDefinitionId()).thenReturn("DoCreateNetworkInstance")
			when(mockExecution.getProcessInstanceId()).thenReturn("DoCreateNetworkInstance")
			when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
			when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)

			return mockExecution
		}
}
