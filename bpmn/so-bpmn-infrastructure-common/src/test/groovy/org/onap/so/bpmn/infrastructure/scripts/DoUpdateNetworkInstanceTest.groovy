/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.onap.aai.domain.yang.*
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.mock.FileUtil
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.springframework.mock.env.MockEnvironment

import static org.mockito.ArgumentMatchers.eq
import static org.mockito.ArgumentMatchers.isA
import static org.mockito.ArgumentMatchers.refEq
import static org.mockito.Mockito.atLeast
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when
import static org.onap.so.bpmn.mock.StubResponseAAI.*

@RunWith(MockitoJUnitRunner.class)
class DoUpdateNetworkInstanceTest  {

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(8090);

		def utils = new MsoUtils()
		String Prefix="UPDNETI_"

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


		  String jsonIncomingRequest_MissingCloudRegion =
		"""{ "requestDetails": {
	      "modelInfo": {
			"modelType": "network",
  			"modelCustomizationId": "f21df226-8093-48c3-be7e-0408fcda0422",
  			"modelName": "CONTRAIL_EXTERNAL",
  			"modelVersion": "1.0"
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


String expectedNetworkRequestMissingCloudRegion =
"""<network-request xmlns="http://www.w3.org/2001/XMLSchema">
   <request-info>
      <request-id>88f65519-9a38-4c4b-8445-9eb4a5a5af56</request-id>
      <action>UPDATE</action>
      <source>VID</source>
      <service-instance-id>f70e927b-6087-4974-9ef8-c5e4d5847ca4</service-instance-id>
   </request-info>
   <network-inputs>
      <network-id>networkId</network-id>
      <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
      <network-type>CONTRAIL_EXTERNAL</network-type>
      <modelCustomizationId>f21df226-8093-48c3-be7e-0408fcda0422</modelCustomizationId>
      <aic-cloud-region>null</aic-cloud-region>
      <tenant-id>7dd5365547234ee8937416c65507d266</tenant-id>
      <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
      <backout-on-failure>false</backout-on-failure>
      <sdncVersion>null</sdncVersion>
   </network-inputs>
   <network-params/>
</network-request>"""

String expectedNetworkInputMissingCloudRegion =
"""<network-inputs xmlns="http://www.w3.org/2001/XMLSchema">
   <network-id>networkId</network-id>
   <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
   <network-type>CONTRAIL_EXTERNAL</network-type>
   <modelCustomizationId>f21df226-8093-48c3-be7e-0408fcda0422</modelCustomizationId>
   <aic-cloud-region>null</aic-cloud-region>
   <tenant-id>7dd5365547234ee8937416c65507d266</tenant-id>
   <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
   <backout-on-failure>false</backout-on-failure>
   <sdncVersion>null</sdncVersion>
</network-inputs>"""

	  // expectedNetworkRequest
	  String expectedNetworkRequest =
  """<network-request xmlns="http://www.w3.org/2001/XMLSchema">
   <request-info>
      <request-id>88f65519-9a38-4c4b-8445-9eb4a5a5af56</request-id>
      <action>UPDATE</action>
      <source>VID</source>
      <service-instance-id>f70e927b-6087-4974-9ef8-c5e4d5847ca4</service-instance-id>
   </request-info>
   <network-inputs>
      <network-id>49c86598-f766-46f8-84f8-8d1c1b10f9b4</network-id>
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

String expectedNetworkInputs =
"""<network-inputs xmlns="http://www.w3.org/2001/XMLSchema">
   <network-id>49c86598-f766-46f8-84f8-8d1c1b10f9b4</network-id>
   <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
   <network-type>CONTRAIL_EXTERNAL</network-type>
   <modelCustomizationId>f21df226-8093-48c3-be7e-0408fcda0422</modelCustomizationId>
   <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
   <tenant-id>7dd5365547234ee8937416c65507d266</tenant-id>
   <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
   <backout-on-failure>false</backout-on-failure>
   <sdncVersion>1610</sdncVersion>
</network-inputs>"""

String expectedVperNetworkRequest =
"""<network-request xmlns="http://www.w3.org/2001/XMLSchema">
   <request-info>
      <request-id>88f65519-9a38-4c4b-8445-9eb4a5a5af56</request-id>
      <action>UPDATE</action>
      <source>VID</source>
      <service-instance-id>f70e927b-6087-4974-9ef8-c5e4d5847ca4</service-instance-id>
   </request-info>
   <network-inputs>
      <network-id>49c86598-f766-46f8-84f8-8d1c1b10f9b4</network-id>
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
         <modelName/>
         <modelUuid/>
         <modelInvariantUuid/>
         <modelVersion/>
         <modelCustomizationUuid/>
      </serviceModelInfo>
      <sdncVersion>1702</sdncVersion>
   </network-inputs>
   <network-params/>
</network-request>"""

String expectedVperNetworkInputs = 
"""<network-inputs xmlns="http://www.w3.org/2001/XMLSchema">
   <network-id>49c86598-f766-46f8-84f8-8d1c1b10f9b4</network-id>
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
      <modelName/>
      <modelUuid/>
      <modelInvariantUuid/>
      <modelVersion/>
      <modelCustomizationUuid/>
   </serviceModelInfo>
   <sdncVersion>1702</sdncVersion>
</network-inputs>"""

String expectedNetworkInputsMissingNetworkId =
"""<network-inputs xmlns="http://www.w3.org/2001/XMLSchema">
   <network-id/>
   <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
   <network-type>CONTRAIL_EXTERNAL</network-type>
   <modelCustomizationId>f21df226-8093-48c3-be7e-0408fcda0422</modelCustomizationId>
   <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
   <tenant-id>7dd5365547234ee8937416c65507d266</tenant-id>
   <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
   <backout-on-failure>false</backout-on-failure>
   <sdncVersion>1610</sdncVersion>
</network-inputs>"""


  String NetworkRequest_noPhysicalName =
  """<vnfreq:network-request xmlns:vnfreq="http://org.onap/so/infra/vnf-request/v1">
   <vnfreq:request-info>
      <vnfreq:request-id>1ef47428-cade-45bd-a103-0751e8b2deb0</vnfreq:request-id>
      <vnfreq:action>UPDATE</vnfreq:action>
      <vnfreq:source>PORTAL</vnfreq:source>
   </vnfreq:request-info>
   <vnfreq:network-inputs>
      <vnfreq:network-name>MNS-25180-L-01-dmz_direct_net_1</vnfreq:network-name>
      <vnfreq:network-type>CONTRAIL_EXTERNAL</vnfreq:network-type>
	  <vnfreq:modelCustomizationId></vnfreq:modelCustomizationId>
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
  """<vnfreq:network-request xmlns:vnfreq="http://org.onap/so/infra/vnf-request/v1">
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


	  String updateNetworkRequest =
	  """<updateNetworkRequest>
   <cloudSiteId>RDM2WAGPLCP</cloudSiteId>
   <tenantId>7dd5365547234ee8937416c65507d266</tenantId>
   <networkId>49c86598-f766-46f8-84f8-8d1c1b10f9b4</networkId>
   <networkStackId>ST_2Bindings_6006/55288ef0-595c-47d3-819e-cf93aaac6326</networkStackId>
   <networkName>MNS-25180-L-01-dmz_direct_net_1</networkName>
   <networkType>CONTRAIL_EXTERNAL</networkType>
   <modelCustomizationUuid>sn5256d1-5a33-55df-13ab-12abad84e222</modelCustomizationUuid>
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
         <prefix>172.20.1.0/24</prefix>
         <nextHop>10.102.200.1</nextHop>
      </hostRoutes>
      <hostRoutes>
         <prefix>10.102.0.0/16</prefix>
         <nextHop>10.102.200.1</nextHop>
      </hostRoutes>
      <hostRoutes>
         <prefix>192.168.2.0/25</prefix>
         <nextHop>10.102.200.1</nextHop>
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
         <prefix>172.20.1.0/24</prefix>
         <nextHop>10.102.200.1</nextHop>
      </hostRoutes>
      <hostRoutes>
         <prefix>10.102.0.0/16</prefix>
         <nextHop>10.102.200.1</nextHop>
      </hostRoutes>
      <hostRoutes>
         <prefix>192.168.2.0/25</prefix>
         <nextHop>10.102.200.1</nextHop>
      </hostRoutes>
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
</updateNetworkRequest>"""

	  String updateNetworkResponseREST =
  """<ns2:updateNetworkContrailResponse xmlns:ns2="http://org.onap.so/network">
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
	  """<rollbackNetworkRequest>
   <networkRollback>
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
   </networkRollback>
</rollbackNetworkRequest>"""


  String networkException500 =
  """<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"><soap:Body><soap:Fault><faultcode>soap:VersionMismatch</faultcode><faultstring>"http://org.onap.so/network", the namespace on the "updateNetworkContrail" element, is not a valid SOAP version.</faultstring></soap:Fault></soap:Body></soap:Envelope>"""

	String changeAssignSDNCRequest =
    """<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                  xmlns:ns5="http://org.onap/so/request/types/v1"
                                  xmlns:sdncadapter="http://org.onap.so/workflow/sdnc/adapter/schema/v1">
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
         <network-type>CONTRAIL30_BASIC</network-type>
         <network-name>vprobes_pktinternal_net_4_1806</network-name>
         <tenant>7dd5365547234ee8937416c65507d266</tenant>
         <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
      </network-request-information>
   </aetgt:SDNCRequestData>
</aetgt:SDNCAdapterWorkflowRequest>"""

String assignResponse =
"""<sdncadapterworkflow:SDNCAdapterWorkflowResponse xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1" xmlns:tag0="http://org.onap.so/workflow/sdnc/adapter/schema/v1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"> <sdncadapterworkflow:response-data> <tag0:CallbackHeader> <tag0:RequestId>006927ca-f5a3-47fd-880c-dfcbcd81a093</tag0:RequestId> <tag0:ResponseCode>200</tag0:ResponseCode> <tag0:ResponseMessage>OK</tag0:ResponseMessage> </tag0:CallbackHeader> <tag0:RequestData xsi:type="xs:string"><output xmlns="com:att:sdnctl:vnf"><response-code>200</response-code><svc-request-id>006927ca-f5a3-47fd-880c-dfcbcd81a093</svc-request-id><ack-final-indicator>Y</ack-final-indicator><service-information><subscriber-name>notsurewecare</subscriber-name><service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id><service-instance-id>GN_EVPN_direct_net_0_ST_noGW</service-instance-id></service-information><network-information><network-id>8abc633a-810b-4ca5-8b3a-09511d13a2ce</network-id></network-information></output></tag0:RequestData> </sdncadapterworkflow:response-data> </sdncadapterworkflow:SDNCAdapterWorkflowResponse>"""

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

	   String sdncAdapterWorkflowResponse =
	  """<aetgt:SDNCAdapterWorkflowResponse xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                   xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1">
   <sdncadapterworkflow:response-data>
<tag0:CallbackHeader xmlns:tag0="http://org.onap.so/workflow/sdnc/adapter/schema/v1">
   <tag0:RequestId>745b1b50-e39e-4685-9cc8-c71f0bde8bf0</tag0:RequestId>
   <tag0:ResponseCode>200</tag0:ResponseCode>
   <tag0:ResponseMessage>OK</tag0:ResponseMessage>
</tag0:CallbackHeader>
   <tag0:RequestData xmlns:tag0="http://org.onap.so/workflow/sdnc/adapter/schema/v1"
                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xsi:type="xs:string">&lt;output xmlns="com:att:sdnctl:vnf"&gt;&lt;svc-request-id&gt;00703dc8-71ff-442d-a4a8-3adc5beef6a9&lt;/svc-request-id&gt;&lt;response-code&gt;200&lt;/response-code&gt;&lt;ack-final-indicator&gt;Y&lt;/ack-final-indicator&gt;&lt;network-information&gt;&lt;network-id&gt;49c86598-f766-46f8-84f8-8d1c1b10f9b4&lt;/network-id&gt;&lt;/network-information&gt;&lt;service-information&gt;&lt;service-type&gt;a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb&lt;/service-type&gt;&lt;service-instance-id&gt;MNS-25180-L-01-dmz_direct_net_1&lt;/service-instance-id&gt;&lt;subscriber-name&gt;notsurewecare&lt;/subscriber-name&gt;&lt;/service-information&gt;&lt;/output&gt;</tag0:RequestData>
   </sdncadapterworkflow:response-data>
</aetgt:SDNCAdapterWorkflowResponse>"""


	  String sdncAdapterWorkflowResponse_Error =
	  """<sdncadapterworkflow:SDNCAdapterWorkflowResponse xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
                                                 xmlns:tag0="http://org.onap.so/workflow/sdnc/adapter/schema/v1"
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

// - - - - - - - -


    @BeforeClass
    static void initEnv() {
        MockEnvironment mockEnvironment = mock(MockEnvironment.class)
        when(mockEnvironment.getProperty("mso.workflow.global.default.aai.version")).thenReturn("14")
        when(mockEnvironment.getProperty("mso.workflow.global.default.aai.namespace")).thenReturn("defaultTestNamespace")
        when(mockEnvironment.getProperty("aai.endpoint")).thenReturn("http://localhost:8090")
        UrnPropertiesReader urnPropertiesReader = new UrnPropertiesReader()
        urnPropertiesReader.setEnvironment(mockEnvironment)
    }

    @AfterClass
    static void cleanupEnv() {
        UrnPropertiesReader urnPropertiesReader = new UrnPropertiesReader()
        urnPropertiesReader.setEnvironment(null)
    }

	    @Before
		public void init()
		{
			MockitoAnnotations.initMocks(this)
		}

		public void initializeVariables (DelegateExecution mockExecution) {

			verify(mockExecution).setVariable(Prefix + "messageId", "")
			verify(mockExecution).setVariable("BasicAuthHeaderValuePO", "")
			verify(mockExecution).setVariable("BasicAuthHeaderValueSDNC", "")
			verify(mockExecution).setVariable(Prefix + "networkRequest", "")
			verify(mockExecution).setVariable(Prefix + "networkInputs", "")
			verify(mockExecution).setVariable(Prefix + "networkOutputs", "")
			verify(mockExecution).setVariable(Prefix + "requestId", "")
			verify(mockExecution).setVariable(Prefix + "source", "")
			verify(mockExecution).setVariable(Prefix + "networkId", "")

			verify(mockExecution).setVariable(Prefix + "isPONR", false)

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

			verify(mockExecution).setVariable(Prefix + "updateNetworkRequest", "")
			verify(mockExecution).setVariable(Prefix + "updateNetworkResponse", "")
			verify(mockExecution).setVariable(Prefix + "rollbackNetworkRequest", "")
			//verify(mockExecution).setVariable(Prefix + "rollbackNetworkResponse", "")
			verify(mockExecution).setVariable(Prefix + "networkReturnCode", "")
			//verify(mockExecution).setVariable(Prefix + "rollbackNetworkReturnCode", "")
			verify(mockExecution).setVariable(Prefix + "isNetworkRollbackNeeded", false)

			verify(mockExecution).setVariable(Prefix + "changeAssignSDNCRequest", "")
			verify(mockExecution).setVariable(Prefix + "changeAssignSDNCResponse", "")
			verify(mockExecution).setVariable(Prefix + "rollbackSDNCRequest", "")
			//verify(mockExecution).setVariable(Prefix + "rollbackSDNCResponse", "")
			verify(mockExecution).setVariable(Prefix + "sdncReturnCode", "")
			//verify(mockExecution).setVariable(Prefix + "rollbackSDNCReturnCode", "")
			verify(mockExecution).setVariable(Prefix + "isSdncRollbackNeeded", false)
			verify(mockExecution).setVariable(Prefix + "sdncResponseSuccess", false)

			verify(mockExecution).setVariable(Prefix + "isVnfBindingPresent", false)
			verify(mockExecution).setVariable(Prefix + "Success", false)
			verify(mockExecution).setVariable(Prefix + "serviceInstanceId", "")

			verify(mockExecution).setVariable(Prefix + "isException", false)

		}

    @Test
    public void preProcessRequest_NetworkRequest() {

        println "************ preProcessRequest_Payload ************* "
        ExecutionEntity mockExecution = setupMock()
        // Initialize prerequisite variables
        when(mockExecution.getVariable("requestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
        when(mockExecution.getVariable("requestAction")).thenReturn("UPDATE")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
        when(mockExecution.getVariable("networkId")).thenReturn("49c86598-f766-46f8-84f8-8d1c1b10f9b4")
        when(mockExecution.getVariable("bpmnRequest")).thenReturn(jsonIncomingRequest)
        when(mockExecution.getVariable("sdncVersion")).thenReturn("1610")
        when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
        when(mockExecution.getVariable("mso.adapters.po.auth")).
                thenReturn("5E12ACACBD552A415E081E29F2C4772F9835792A51C766CCFDD7433DB5220B59969CB2798C")
        when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")


        DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
        DoUpdateNetworkInstance.preProcessRequest(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix + "")

        //verify variable initialization
        initializeVariables(mockExecution)

        // Authentications
        verify(mockExecution).setVariable("action", "UPDATE")
        verify(mockExecution).setVariable(Prefix + "networkRequest", expectedNetworkRequest)
        verify(mockExecution).setVariable(Prefix + "rollbackEnabled", false)
        verify(mockExecution).setVariable(Prefix + "networkInputs", expectedNetworkInputs)
        verify(mockExecution).setVariable(Prefix + "messageId", "88f65519-9a38-4c4b-8445-9eb4a5a5af56")
        verify(mockExecution).setVariable(Prefix + "source", "VID")
        verify(mockExecution).setVariable("BasicAuthHeaderValuePO", "Basic dGVzdDp0ZXN0")
        verify(mockExecution).setVariable("BasicAuthHeaderValueSDNC", "Basic dGVzdDp0ZXN0")
        verify(mockExecution).setVariable(Prefix + "serviceInstanceId", "f70e927b-6087-4974-9ef8-c5e4d5847ca4")
        verify(mockExecution).setVariable(Prefix + "networkOutputs", """<network-outputs>
\t                   <network-id>49c86598-f766-46f8-84f8-8d1c1b10f9b4</network-id>
\t                   <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
\t                 </network-outputs>""")
        verify(mockExecution).setVariable(Prefix + "networkId", "49c86598-f766-46f8-84f8-8d1c1b10f9b4")
        verify(mockExecution).setVariable(Prefix + "networkName", "MNS-25180-L-01-dmz_direct_net_1")
    }

    @Test
    public void preProcessRequest_vPERNetworkRequest() {

        String networkModelInfo = """{"modelUuid": "sn5256d1-5a33-55df-13ab-12abad84e111",
                                     "modelName": "CONTRAIL_EXTERNAL",
                                     "modelType": "CONTRAIL_EXTERNAL",
                                     "modelVersion": "1",
                                     "modelCustomizationUuid": "sn5256d1-5a33-55df-13ab-12abad84e222",
                                     "modelInvariantUuid": "sn5256d1-5a33-55df-13ab-12abad84e764"
                                    }""".trim()

        println "************ preProcessRequest_Payload ************* "

        ExecutionEntity mockExecution = setupMock()

        // Initialize prerequisite variables
        when(mockExecution.getVariable("msoRequestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
        when(mockExecution.getVariable("action")).thenReturn("UPDATE")

        when(mockExecution.getVariable("disableRollback")).thenReturn("true")
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
        when(mockExecution.getVariable("networkId")).thenReturn("49c86598-f766-46f8-84f8-8d1c1b10f9b4")     // optional
        when(mockExecution.getVariable("networkName")).thenReturn("MNS-25180-L-01-dmz_direct_net_1")        // optional
        when(mockExecution.getVariable("productFamilyId")).thenReturn("a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb")
        when(mockExecution.getVariable("networkModelInfo")).thenReturn("CONTRAIL_EXTERNAL")
        when(mockExecution.getVariable("lcpCloudRegionId")).thenReturn("RDM2WAGPLCP")
        when(mockExecution.getVariable("tenantId")).thenReturn("7dd5365547234ee8937416c65507d266")
        when(mockExecution.getVariable("failIfExists")).thenReturn("false")
        when(mockExecution.getVariable("networkModelInfo")).thenReturn(networkModelInfo)
        when(mockExecution.getVariable("sdncVersion")).thenReturn("1702")

        when(mockExecution.getVariable("subscriptionServiceType")).thenReturn("MSO-dev-service-type")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("globalId_45678905678")
        when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")

        when(mockExecution.getVariable("mso.adapters.po.auth")).
                thenReturn("5E12ACACBD552A415E081E29F2C4772F9835792A51C766CCFDD7433DB5220B59969CB2798C")
        when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")

        when(mockExecution.getVariable("mso.rollback")).thenReturn("true")
        when(mockExecution.getVariable("mso.adapters.sdnc.endpoint")).
                thenReturn("http://localhost:8090/SDNCAdapter")
        when(mockExecution.getVariable("mso.adapters.network.rest.endpoint")).
                thenReturn("http://localhost:8090/networks/NetworkAdapter")
        when(mockExecution.getVariable("mso.adapters.sdnc.resource.endpoint")).
                thenReturn("http://localhost:8090/SDNCAdapterRpc")

        DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
        DoUpdateNetworkInstance.preProcessRequest(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix + "")

        //verify variable initialization
        initializeVariables(mockExecution)

        // Authentications
        verify(mockExecution).setVariable("action", "UPDATE")
        verify(mockExecution).setVariable(Prefix + "networkRequest", expectedVperNetworkRequest)
        verify(mockExecution).setVariable(Prefix + "rollbackEnabled", false)
        verify(mockExecution).setVariable(Prefix + "networkInputs", expectedVperNetworkInputs)
        verify(mockExecution).setVariable(Prefix + "messageId", "88f65519-9a38-4c4b-8445-9eb4a5a5af56")
        verify(mockExecution).setVariable(Prefix + "source", "VID")
        verify(mockExecution).setVariable("BasicAuthHeaderValuePO", "Basic dGVzdDp0ZXN0")
        verify(mockExecution).setVariable("BasicAuthHeaderValueSDNC", "Basic dGVzdDp0ZXN0")
        verify(mockExecution).setVariable(Prefix + "serviceInstanceId", "f70e927b-6087-4974-9ef8-c5e4d5847ca4")
        verify(mockExecution).setVariable(eq(Prefix + "networkOutputs"), eq("""<network-outputs>
\t                   <network-id>49c86598-f766-46f8-84f8-8d1c1b10f9b4</network-id>
\t                   <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
\t                 </network-outputs>"""))

        verify(mockExecution).setVariable(Prefix + "networkId", "49c86598-f766-46f8-84f8-8d1c1b10f9b4")
        verify(mockExecution).setVariable(Prefix + "networkName", "MNS-25180-L-01-dmz_direct_net_1")
    }

    @Test
		//@Ignore
		public void preProcessRequest_MissingNetworkId() {

			println "************ preProcessRequest_MissingName() ************* "

			WorkflowException missingNameWorkflowException = new WorkflowException("DoUpdateNetworkInstance", 2500, "Variable 'network-id' value/element is missing.")

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
			when(mockExecution.getVariable("sdncVersion")).thenReturn("1610")

			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("mso.adapters.po.auth")).thenReturn("3141634BF7E070AA289CF2892C986C0B")
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("mso.rollback")).thenReturn("true")
			when(mockExecution.getVariable("disableRollback")).thenReturn("true")

			// preProcessRequest(DelegateExecution execution)
			DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
			try {
				DoUpdateNetworkInstance.preProcessRequest(mockExecution)
			} catch (Exception ex) {
				println " Test End - Handle catch-throw BpmnError()! "
			}

			verify(mockExecution, atLeast(1)).getVariable("isDebugLogEnabled")
			verify(mockExecution, atLeast(1)).setVariable("prefix", Prefix + "")

			//verify variable initialization
			initializeVariables(mockExecution)

			verify(mockExecution).setVariable("action", "UPDATE")
			verify(mockExecution).setVariable(Prefix + "networkRequest", expectedNetworkRequestMissingNetworkId)
			verify(mockExecution).setVariable(Prefix + "rollbackEnabled", false)
			verify(mockExecution).setVariable(Prefix + "networkInputs", expectedNetworkInputsMissingNetworkId)
			verify(mockExecution).setVariable(Prefix + "messageId", "88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			verify(mockExecution).setVariable(Prefix + "source", "VID")
			verify(mockExecution).setVariable(Prefix + "networkOutputs", "")

			verify(mockExecution).setVariable(eq("WorkflowException"), refEq(missingNameWorkflowException))

		}

		@Test
		//@Ignore
		public void preProcessRequest_MissingCloudRegion() {

			println "************ preProcessRequest_MissingCloudRegion() ************* "

			WorkflowException missingCloudRegionWorkflowException = new WorkflowException("DoUpdateNetworkInstance", 2500, "requestDetails has missing 'aic-cloud-region' value/element.")

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
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("mso.adapters.po.auth")).thenReturn("3141634BF7E070AA289CF2892C986C0B")
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("mso.rollback")).thenReturn("true")
			when(mockExecution.getVariable("disableRollback")).thenReturn("true")

			// preProcessRequest(DelegateExecution execution)
			DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
			try {
				DoUpdateNetworkInstance.preProcessRequest(mockExecution)
			} catch (Exception ex) {
				println " Test End - Handle catch-throw BpmnError()! "
			}
			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution, atLeast(1)).getVariable("isDebugLogEnabled")
			verify(mockExecution, atLeast(1)).setVariable("prefix", Prefix + "")

			//verify variable initialization
			initializeVariables(mockExecution)

			verify(mockExecution).setVariable("action", "UPDATE")
			verify(mockExecution).setVariable(Prefix + "networkRequest", expectedNetworkRequestMissingCloudRegion)
			verify(mockExecution).setVariable(Prefix + "rollbackEnabled", false)
			verify(mockExecution).setVariable(Prefix + "networkInputs", expectedNetworkInputMissingCloudRegion)
			verify(mockExecution).setVariable(Prefix + "messageId", "88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			verify(mockExecution).setVariable(Prefix + "source", "VID")

			verify(mockExecution).setVariable(eq("WorkflowException"), refEq(missingCloudRegionWorkflowException))

		}

    @Test
    public void prepareUpdateNetworkRequest() {

        println "************ prepareNetworkRequest ************* "
        ExecutionEntity mockExecution = setupMock()
        // Initialize prerequisite variables
        when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(expectedVperNetworkRequest)
        when(mockExecution.getVariable(Prefix + "requeryIdAAIResponse")).thenReturn(getContrailL3Network())
        when(mockExecution.getVariable(Prefix + "cloudRegionPo")).thenReturn("RDM2WAGPLCP")
        when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("messageId_generated")
        when(mockExecution.getVariable(Prefix + "source")).thenReturn("VID")
        when(mockExecution.getVariable(Prefix + "routeCollection")).
                thenReturn("<routeTargets>13979:105757</routeTargets><routeTargets>13979:105757</routeTargets>")
        when(mockExecution.getVariable(Prefix + "networkCollection")).
                thenReturn("<policyFqdns>GN_EVPN_Test</policyFqdns>")
        when(mockExecution.getVariable(Prefix + "tableRefCollection")).
                thenReturn("<routeTableFqdns>refFQDN1</routeTableFqdns><routeTableFqdns>refFQDN2</routeTableFqdns>")
        when(mockExecution.getVariable(Prefix + "requestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
        when(mockExecution.getVariable(Prefix + "rollbackEnabled")).thenReturn("true")

        DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
        DoUpdateNetworkInstance.prepareUpdateNetworkRequest(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix + "")
        verify(mockExecution).setVariable(Prefix + "updateNetworkRequest", updateNetworkRequest)
    }


    @Test
    public void prepareUpdateNetworkRequest_NoPhysicalname() {

        println "************ prepareNetworkRequest ************* "
        ExecutionEntity mockExecution = setupMock()
        // Initialize prerequisite variables
        when(mockExecution.getVariable(Prefix + "requestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
        when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("messageId_generated")
        when(mockExecution.getVariable(Prefix + "source")).thenReturn("VID")

        when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(NetworkRequest_noPhysicalName)
        when(mockExecution.getVariable(Prefix + "requeryIdAAIResponse")).
                thenReturn(getContrailL3Network())
        when(mockExecution.getVariable(Prefix + "cloudRegionPo")).thenReturn("RDM2WAGPLCP")
        when(mockExecution.getVariable(Prefix + "rollbackEnabled")).thenReturn("true")

        when(mockExecution.getVariable(Prefix + "routeCollection")).thenReturn("<routeTargets>13979:105757</routeTargets><routeTargets>13979:105757</routeTargets>")
        when(mockExecution.getVariable(Prefix + "networkCollection")).thenReturn("<policyFqdns>GN_EVPN_Test</policyFqdns>")
        when(mockExecution.getVariable(Prefix + "tableRefCollection")).thenReturn("")

        DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
        DoUpdateNetworkInstance.prepareUpdateNetworkRequest(mockExecution)

        // verify set prefix = Prefix + ""
        verify(mockExecution).setVariable("prefix", Prefix + "")
        verify(mockExecution).setVariable(Prefix + "updateNetworkRequest", updateNetworkRequest_noPhysicalName)
    }

    @Test
    public void prepareSDNCRequest() {

        println "************ prepareSDNCRequest ************* "

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        // Initialize prerequisite variables
        when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(expectedNetworkRequest)
        when(mockExecution.getVariable(Prefix + "cloudRegionSdnc")).thenReturn("RDM2WAGPLCP")
        when(mockExecution.getVariable(Prefix + "serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
        when(mockExecution.getVariable(Prefix + "queryIdAAIResponse")).thenReturn(
                new AAIResultWrapper(FileUtil.readResourceFile("__files/BuildingBlocks/Network/queryAAINetworkTestResponse.json")))
        when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
        when(mockExecution.getVariable("testMessageId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
        when(mockExecution.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("http://localhost:8090/SDNCAdapterCallback")
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("GENGSI_siResourceLink")).thenReturn(
                "https://aai-int1.test.com:8443/aai/v8/business/customers/customer/MSO_1610_dev/service-subscriptions/service-subscription/MSO-dev-service-type/service-instances/service-instance/6d4eb22a-82f1-4257-9f80-4176262cfe69/")

        DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
        DoUpdateNetworkInstance.prepareSDNCRequest(mockExecution)

        // verify set prefix = Prefix + ""
        verify(mockExecution).setVariable("prefix", Prefix + "")
        verify(mockExecution).setVariable(Prefix + "changeAssignSDNCRequest", changeAssignSDNCRequest)
    }

		@Test
		//@Ignore
		public void prepareSDNCRollbackRequest() {

			println "************ prepareSDNCRollbackRequest ************* "



			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(expectedNetworkRequest)
			when(mockExecution.getVariable(Prefix + "cloudRegionSdnc")).thenReturn("RDM2WAGPLCP")
			when(mockExecution.getVariable(Prefix + "serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
			when(mockExecution.getVariable(Prefix + "changeAssignSDNCResponse")).thenReturn(assignResponse)
			when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("testMessageId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("http://localhost:8090/SDNCAdapterCallback")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("GENGSI_siResourceLink")).thenReturn("https://aai-int1.test.com:8443/aai/v8/business/customers/customer/MSO_1610_dev/service-subscriptions/service-subscription/MSO-dev-service-type/service-instances/service-instance/6d4eb22a-82f1-4257-9f80-4176262cfe69/")

			// preProcessRequest(DelegateExecution execution)
			DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
			DoUpdateNetworkInstance.prepareSDNCRollbackRequest(mockExecution)

			// verify set prefix = Prefix + ""
			verify(mockExecution).setVariable("prefix", Prefix + "")
			verify(mockExecution).setVariable(Prefix + "WorkflowException", null)
			verify(mockExecution).setVariable(Prefix + "rollbackSDNCRequest", sdncRollbackRequest)

		}

    @Test
    public void callRESTQueryAAINetworkId_200() {

        println "************ callRESTQueryAAINetworkId ************* "

        WireMock.reset();
        MockGetNetworkByIdWithDepth(wireMockRule, "49c86598-f766-46f8-84f8-8d1c1b10f9b4",
                "UpdateNetworkV2/updateNetwork_queryNetworkId_AAIResponse_Success.xml", "1");

        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(expectedNetworkRequest)

        DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
        DoUpdateNetworkInstance.callRESTQueryAAINetworkId(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix)
        verify(mockExecution).setVariable(Prefix + "aaiIdReturnCode", "200")
        verify(mockExecution).setVariable(eq(Prefix + "queryIdAAIResponse"), isA(AAIResultWrapper.class))
    }

    @Test
    public void callRESTQueryAAICloudRegion30_200() {

        println "************ callRESTQueryAAICloudRegion30_200 ************* "

        WireMock.reset();
        MockGetNetworkCloudRegion(wireMockRule, "CreateNetworkV2/cloudRegion30_AAIResponse_Success.xml", "RDM2WAGPLCP");

        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(Prefix + "")
        when(mockExecution.getVariable(Prefix + "networkInputs")).thenReturn(expectedNetworkRequest)
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:8090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn("8")
        when(mockExecution.getVariable("mso.workflow.default.aai.v8.cloud-region.uri")).
                thenReturn("/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner")
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
        when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
        when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

        DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
        DoUpdateNetworkInstance.callRESTQueryAAICloudRegion(mockExecution)

        verify(mockExecution, atLeast(1)).setVariable("prefix", Prefix + "")
        verify(mockExecution, atLeast(2)).setVariable(Prefix + "queryCloudRegionReturnCode", "200")
        verify(mockExecution).setVariable(Prefix + "isCloudRegionGood", true)
    }

    @Test
    public void callRESTQueryAAICloudRegion25_200() {

        println "************ callRESTQueryAAICloudRegion25_200 ************* "

        WireMock.reset();
        MockGetNetworkCloudRegion(wireMockRule, "CreateNetworkV2/cloudRegion25_AAIResponse_Success.xml", "RDM2WAGPLCP");

        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(Prefix + "")
        when(mockExecution.getVariable(Prefix + "networkInputs")).thenReturn(expectedNetworkRequest)
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:8090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn("8")
        when(mockExecution.getVariable("mso.workflow.default.aai.v8.cloud-region.uri")).
                thenReturn("/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner")
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
        when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
        when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

        DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
        DoUpdateNetworkInstance.callRESTQueryAAICloudRegion(mockExecution)

        verify(mockExecution, atLeast(1)).setVariable("prefix", Prefix + "")
        verify(mockExecution).setVariable(Prefix + "isCloudRegionGood", true)
    }

    @Test
    public void callRESTQueryAAICloudRegion_NotFound() {

        println "************ callRESTQueryAAICloudRegionFake ************* "

        WireMock.reset();
        MockGetNetworkCloudRegion_404(wireMockRule, "MDTWNJ21");

        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(Prefix + "")
        when(mockExecution.getVariable(Prefix + "networkInputs")).thenReturn(vnfRequestFakeRegion)
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:8090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn("8")
        when(mockExecution.getVariable("mso.workflow.default.aai.v8.cloud-region.uri")).
                thenReturn("/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner")
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
        when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
        when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

        DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
        DoUpdateNetworkInstance.callRESTQueryAAICloudRegion(mockExecution)

        verify(mockExecution, atLeast(1)).setVariable("prefix", Prefix + "")
        verify(mockExecution, atLeast(1)).setVariable(Prefix + "queryCloudRegionReturnCode", "404")
        verify(mockExecution).setVariable(Prefix + "cloudRegionPo", "MDTWNJ21")
        verify(mockExecution).setVariable(Prefix + "cloudRegionSdnc", "AAIAIC25")
        verify(mockExecution).setVariable(Prefix + "isCloudRegionGood", true)
    }

    @Test
    public void callRESTQueryAAINetworkVpnBinding_200() {

        println "************ callRESTQueryAAINetworkVpnBinding_200 ************* "

        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable(Prefix + "requeryIdAAIResponse")).thenReturn(
                new AAIResultWrapper(FileUtil.readResourceFile("__files/BuildingBlocks/Network/queryAAIResponseEmptyUri.json")))

        DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
        DoUpdateNetworkInstance.callRESTQueryAAINetworkVpnBinding(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix + "")
        verify(mockExecution).setVariable(Prefix + "aaiQqueryVpnBindingReturnCode", "200")
        verify(mockExecution).setVariable(Prefix + "queryVpnBindingAAIResponse",
                """<rest:payload xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"
              xmlns="defaultTestNamespacev14"\n              contentType="text/xml">\n   <vpn-binding>
      <global-route-target/>\n   </vpn-binding>\n</rest:payload>""")
        verify(mockExecution).setVariable(Prefix + "routeCollection", "<routeTargets/>")
    }

    @Test
    public void callRESTQueryAAINetworkVpnBindingList_200() {

        println "************ callRESTQueryAAINetworkVpnBinding_200 ************* "

        WireMock.reset();

        MockGetNetworkVpnBindingWithDepth(wireMockRule,
                "BuildingBlocks/Network/queryAAIVpnBindingTestResponseWithRoutes.json",
                "13e94b71-3ce1-4988-ab0e-61208fc91f1c", "2")

        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable(Prefix + "requeryIdAAIResponse")).thenReturn(
                new AAIResultWrapper(FileUtil.readResourceFile("__files/BuildingBlocks/Network/queryAAINetworkTestResponse.json")))

        DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
        DoUpdateNetworkInstance.callRESTQueryAAINetworkVpnBinding(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix + "")
        verify(mockExecution).setVariable(Prefix + "routeCollection", """<routeTargets>
 <routeTarget>2001:051111</routeTarget>\n <routeTargetRole>EXPORT</routeTargetRole>\n</routeTargets>
<routeTargets>\n <routeTarget>1000:051113</routeTarget>\n <routeTargetRole>IMPORT</routeTargetRole>\n</routeTargets>\n""")
    }


    @Test
    public void callRESTQueryAAINetworkVpnBinding_200_URN_Uri() {

        println "************ callRESTQueryAAINetworkVpnBinding_200 ************* "

        WireMock.reset();
        MockGetNetworkVpnBindingWithDepth(wireMockRule,
                "BuildingBlocks/Network/queryAAIVpnBindingTestResponseWithRoutes.json",
                "13e94b71-3ce1-4988-ab0e-61208fc91f1c", "2");

        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable(Prefix + "requeryIdAAIResponse")).thenReturn(
                new AAIResultWrapper(FileUtil.readResourceFile("__files/BuildingBlocks/Network/queryAAINetworkTestResponse.json")))

        DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
        DoUpdateNetworkInstance.callRESTQueryAAINetworkVpnBinding(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix + "")
        verify(mockExecution).setVariable(Prefix + "routeCollection",
                """<routeTargets>\n <routeTarget>2001:051111</routeTarget>\n <routeTargetRole>EXPORT</routeTargetRole>
</routeTargets>\n<routeTargets>\n <routeTarget>1000:051113</routeTarget>\n <routeTargetRole>IMPORT</routeTargetRole>
</routeTargets>\n""")
    }

    @Test
    public void callRESTQueryAAINetworkVpnBinding_NotPresent() {

        println "************ callRESTQueryAAINetworkVpnBinding_NotPresent ************* "

        WireMock.reset();

        MockGetNetworkVpnBindingWithDepth(wireMockRule,
                "BuildingBlocks/Network/queryAAIVpnBindingTestResponse.json",
                "13e94b71-3ce1-4988-ab0e-61208fc91f1c", "2")

        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable(Prefix + "requeryIdAAIResponse")).thenReturn(
                new AAIResultWrapper(FileUtil.readResourceFile("__files/BuildingBlocks/Network/queryAAINetworkTestResponse.json")))

        DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
        DoUpdateNetworkInstance.callRESTQueryAAINetworkVpnBinding(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix + "")
        verify(mockExecution).setVariable(Prefix + "routeCollection", "")
    }

    @Test
    public void callRESTQueryAAINetworkTableRef_200() {

        println "************ callRESTQueryAAINetworkTableRef_200 ************* "
        WireMock.reset();

        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable(Prefix + "requeryIdAAIResponse")).thenReturn(
                new AAIResultWrapper(FileUtil.readResourceFile("__files/BuildingBlocks/Network/queryAAINetworkTestResponse.json")))

        DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
        DoUpdateNetworkInstance.callRESTQueryAAINetworkTableRef(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix + "")
        verify(mockExecution).setVariable(Prefix + "networkTableRefCount", 0)
        verify(mockExecution).setVariable(Prefix + "aaiQqueryNetworkTableRefReturnCode", "200")
        verify(mockExecution).setVariable(Prefix + "queryNetworkTableRefAAIResponse",
                """<rest:payload xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"
              xmlns="defaultTestNamespacev14"\n              contentType="text/xml">\n   <route-table-references>
      <route-table-reference-fqdn/>\n   </route-table-references>\n</rest:payload>""")
        verify(mockExecution).setVariable(Prefix + "tableRefCollection", "<routeTableFqdns/>")
    }

    @Test
    public void callRESTQueryAAINetworkPolicy_200() {

        println "************ callRESTQueryAAINetworkPolicy_200 ************* "

        WireMock.reset();

        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable(Prefix + "requeryIdAAIResponse")).thenReturn(
                new AAIResultWrapper(FileUtil.readResourceFile("__files/BuildingBlocks/Network/queryAAINetworkTestResponse.json")))

        DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
        DoUpdateNetworkInstance.callRESTQueryAAINetworkPolicy(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix + "")
        verify(mockExecution).setVariable(Prefix + "networkPolicyCount", 0)
        verify(mockExecution).setVariable(Prefix + "aaiQqueryNetworkPolicyReturnCode", "200")
        verify(mockExecution).setVariable(Prefix + "queryNetworkPolicyAAIResponse",
                """<rest:payload xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"
              xmlns="defaultTestNamespacev14"\n              contentType="text/xml">\n   <network-policy>
      <network-policy-fqdn/>\n   </network-policy>\n</rest:payload>""")
        verify(mockExecution).setVariable(Prefix + "networkCollection", "<policyFqdns/>")
    }


    @Test
    public void callRESTReQueryAAINetworkId_200() {

        println "************ callRESTReQueryAAINetworkId ************* "

        WireMock.reset();
        MockGetNetworkByIdWithDepth(wireMockRule, "49c86598-f766-46f8-84f8-8d1c1b10f9b4",
                "BuildingBlocks/Network/queryAAINetworkTestResponse.json", "1");

        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(expectedNetworkRequest)

        DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
        DoUpdateNetworkInstance.callRESTReQueryAAINetworkId(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix + "")
        verify(mockExecution).setVariable(Prefix + "aaiRequeryIdReturnCode", "200")
        verify(mockExecution).setVariable(eq(Prefix + "requeryIdAAIResponse"), isA(AAIResultWrapper.class))
        verify(mockExecution).setVariable(Prefix + "networkOutputs", """<network-outputs>
                   <network-id>467e3349-bec1-4922-bcb1-d0bb041bce30</network-id>
                   <network-name>vprobes_pktinternal_net_4_1806</network-name>
                 </network-outputs>""")
    }


    @Test
    public void callRESTUpdateContrailAAINetworkREST_200() {

        println "************ callRESTUpdateContrailAAINetwork ************* "

        WireMock.reset();
        MockPostNetwork(wireMockRule, "49c86598-f766-46f8-84f8-8d1c1b10f9b4")
        MockPostNetworkSubnet(wireMockRule, "49c86598-f766-46f8-84f8-8d1c1b10f9b4", "209f62cf-cf0c-42f8-b13c-f038b92ef108")
        MockPostNetworkSubnet(wireMockRule, "49c86598-f766-46f8-84f8-8d1c1b10f9b4", "971bc608-1aff-47c0-923d-92e43b699f01")

        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(expectedNetworkRequest)
        when(mockExecution.getVariable(Prefix + "requeryIdAAIResponse")).thenReturn(
                new AAIResultWrapper(FileUtil.readResourceFile("__files/BuildingBlocks/Network/queryAAIResponseEmptyUri.json")))
        when(mockExecution.getVariable(Prefix + "updateNetworkResponse")).thenReturn(updateNetworkResponseREST)

        DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
        DoUpdateNetworkInstance.callRESTUpdateContrailAAINetwork(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix + "")
        verify(mockExecution).setVariable(Prefix + "isPONR", true)
    }


    @Test
    public void validateUpdateNetworkResponseREST() {

        println "************ validateNetworkResponse ************* "

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        // Initialize prerequisite variables
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable(Prefix + "updateNetworkResponse")).thenReturn(updateNetworkResponseREST)
        when(mockExecution.getVariable(Prefix + "networkReturnCode")).thenReturn('200')

        DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
        DoUpdateNetworkInstance.validateUpdateNetworkResponse(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix + "")
        verify(mockExecution).setVariable(Prefix + "updateNetworkResponse", updateNetworkResponseREST)
        verify(mockExecution).setVariable(Prefix + "isNetworkRollbackNeeded", true)
        verify(mockExecution).setVariable(Prefix + "rollbackNetworkRequest", updateRollbackNetworkRequest)
    }

    @Test
    public void validateUpdateNetworkResponseREST_Error() {

        println "************ validateNetworkResponse ************* "

        WorkflowException workflowException = new WorkflowException("DoUpdateNetworkInstance", 2500, "Received error from Network Adapter: JBWEB000065: HTTP Status 500.")

        ExecutionEntity mockExecution = setupMock()
        // Initialize prerequisite variables
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable(Prefix + "updateNetworkResponse")).thenReturn(networkException500)
        when(mockExecution.getVariable(Prefix + "networkReturnCode")).thenReturn('500')

        DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
        try {
            DoUpdateNetworkInstance.validateUpdateNetworkResponse(mockExecution)
        } catch (Exception ex) {
            println " Test End - Handle catch-throw BpmnError()! "
        }

        verify(mockExecution).setVariable("prefix", Prefix + "")
        verify(mockExecution, atLeast(1)).setVariable(eq("WorkflowException"), refEq(workflowException))

    }

    @Test
    public void validateSDNCResponse() {

        println "************ validateSDNCResponse ************* "

        ExecutionEntity mockExecution = setupMock()
        // Initialize prerequisite variables
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable(Prefix + "changeAssignSDNCResponse")).thenReturn(sdncAdapterWorkflowResponse)
        when(mockExecution.getVariable("prefix")).thenReturn(Prefix + "")
        when(mockExecution.getVariable("SDNCA_SuccessIndicator")).thenReturn(true)
        when(mockExecution.getVariable(Prefix + "sdncReturnCode")).thenReturn("200")
        when(mockExecution.getVariable(Prefix + "sdncResponseSuccess")).thenReturn(true)

        DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
        DoUpdateNetworkInstance.validateSDNCResponse(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix)
        verify(mockExecution).setVariable(Prefix + "sdncResponseSuccess", false)
        verify(mockExecution).setVariable(Prefix + "sdncRequestDataResponseCode", "200")
        verify(mockExecution).setVariable(Prefix + "sdncResponseSuccess", true)
        verify(mockExecution).setVariable(Prefix + "changeAssignSDNCResponse", sdncAdapterWorkflowResponse)
        verify(mockExecution).setVariable(Prefix + "isSdncRollbackNeeded", true)
    }

    @Test(expected = BpmnError.class)
    public void validateSDNCResponse_Error() {

        println "************ validateSDNCResponse ************* "
        ExecutionEntity mockExecution = setupMock()
        // Initialize prerequisite variables
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable(Prefix + "changeAssignSDNCResponse")).thenReturn(sdncAdapterWorkflowResponse_Error)
        when(mockExecution.getVariable("prefix")).thenReturn(Prefix + "")
        when(mockExecution.getVariable("SDNCA_SuccessIndicator")).thenReturn(false)
        when(mockExecution.getVariable(Prefix + "sdncReturnCode")).thenReturn("200")
        when(mockExecution.getVariable(Prefix + "isResponseGood")).thenReturn(true)

        DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
        try {
            DoUpdateNetworkInstance.validateSDNCResponse(mockExecution)
        } catch (BpmnError ex) {
            verify(mockExecution).setVariable(Prefix + "sdncResponseSuccess", false)
            println " Graceful Exit! - " + ex.getMessage()
            throw ex
        }
    }

    @Test
    public void prepareRollbackData() {

        println "************ prepareRollbackData() ************* "

        WorkflowException workflowException = new WorkflowException("DoUpdateNetworkInstance", 2500, "Received error from Network Adapter: JBWEB000065: HTTP Status 500.")

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        // Initialize prerequisite variables
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable(Prefix + "rollbackSDNCRequest")).thenReturn(rollbackSDNCRequest)
        when(mockExecution.getVariable(Prefix + "rollbackNetworkRequest")).thenReturn(rollbackNetworkRequest)
        when(mockExecution.getVariable("WorkflowException")).thenReturn(workflowException)

        DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
        DoUpdateNetworkInstance.prepareRollbackData(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix)
    }

		@Test
		//@Ignore
		public void postProcessResponse() {

			println "************ postProcessResponse() ************* "
			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable(Prefix + "isException")).thenReturn(false)
			when(mockExecution.getVariable("sdncVersion")).thenReturn("1610")
			when(mockExecution.getVariable(Prefix + "rollbackSDNCRequest")).thenReturn(rollbackSDNCRequest)
			when(mockExecution.getVariable(Prefix + "rollbackNetworkRequest")).thenReturn(rollbackSDNCRequest)
			when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(expectedNetworkRequest)
			when(mockExecution.getVariable("mso-request-id")).thenReturn("requestId")

			// preProcessRequest(DelegateExecution execution)
			DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
			DoUpdateNetworkInstance.postProcessResponse(mockExecution)

//			verify(mockExecution, atLeast(3)).getVariable("isDebugLogEnabled")
			verify(mockExecution, atLeast(3)).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable(Prefix + "Success", true)

		}

    private static L3Network getContrailL3Network() {

        HostRoutes routes1 = new HostRoutes()
        routes1.getHostRoute().add(new HostRoute(routePrefix: "172.20.1.0/24", nextHop: "10.102.200.1"))
        routes1.getHostRoute().add(new HostRoute(routePrefix: "10.102.0.0/16", nextHop: "10.102.200.1"))
        routes1.getHostRoute().add(new HostRoute(routePrefix: "192.168.2.0/25", nextHop: "10.102.200.1"))

        Subnet subnet1 = new Subnet(networkStartAddress: "107.239.52.0", cidrMask: "24", dhcpEnabled: true,
                gatewayAddress: "107.239.52.1", ipVersion: "4", subnetId: "57e9a1ff-d14f-4071-a828-b19ae98eb2fc",
                subnetName: "subnetName", ipAssignmentDirection: "true", hostRoutes: routes1)
        Subnets subnets = new Subnets()
        subnets.getSubnet().add(subnet1)

        SegmentationAssignments segments = new SegmentationAssignments()
        segments.getSegmentationAssignment().add(new SegmentationAssignment(segmentationId: "414"))
        segments.getSegmentationAssignment().add(new SegmentationAssignment(segmentationId: "415"))

        return new L3Network(
                networkName: "MNS-25180-L-01-dmz_direct_net_1",
                networkType: "CONTRAIL_EXTERNAL",
                networkTechnology: "Contrail",
                networkId: "49c86598-f766-46f8-84f8-8d1c1b10f9b4",
                orchestrationStatus: "pending-create",
                physicalNetworkName: "networkName",
                heatStackId: "ST_2Bindings_6006/55288ef0-595c-47d3-819e-cf93aaac6326",
                isSharedNetwork: true,
                subnets: subnets,
                segmentationAssignments: segments
        )
    }

		private ExecutionEntity setupMock() {

			ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
			when(mockProcessDefinition.getKey()).thenReturn("DoUpdateNetworkInstance")
			RepositoryService mockRepositoryService = mock(RepositoryService.class)
			when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
			when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("DoUpdateNetworkInstance")
			ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
			when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)

			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables

			when(mockExecution.getProcessDefinitionId()).thenReturn("DoUpdateNetworkInstance")
			when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
			when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)

			return mockExecution
		}


}
