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
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.onap.aai.domain.yang.*
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.core.UrnPropertiesReader
import org.onap.so.bpmn.core.WorkflowException
import org.springframework.mock.env.MockEnvironment
import sun.dc.pr.PRError

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.post
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.ArgumentMatchers.refEq
import static org.mockito.Mockito.*
import static org.onap.so.bpmn.mock.StubResponseAAI.*

@RunWith(MockitoJUnitRunner.class)
class DoCreateNetworkInstanceTest  {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8090);

    @Captor
    public ArgumentCaptor<L3Network> captor

    def utils = new MsoUtils()
    String Prefix = "CRENWKI_"

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

    String networkXMLOutputs = """"""

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


    String networkInputs =
            """<network-inputs xmlns="http://org.onap/so/infra/vnf-request/v1">
   <network-id>bdc5efe8-404a-409b-85f6-0dcc9eebae30</network-id>
   <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
   <network-type>CONTRAIL_EXTERNAL</network-type>
   <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
   <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
   <tenant-id>e81d842d3e8b45c5a59f57cd76af3aaf</tenant-id>
</network-inputs>"""

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


    String networkException500 =
            """<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"><soap:Body><soap:Fault><faultcode>soap:VersionMismatch</faultcode><faultstring>"http://org.onap.so/network", the namespace on the "createNetworkContrail" element, is not a valid SOAP version.</faultstring></soap:Fault></soap:Body></soap:Envelope>"""

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
    public void init() {
        MockitoAnnotations.initMocks(this)
    }

    public void initializeVariables(DelegateExecution mockExecution) {

        verify(mockExecution).setVariable(Prefix + "networkRequest", "")
        verify(mockExecution).setVariable(Prefix + "rollbackEnabled", null)
        verify(mockExecution).setVariable(Prefix + "networkInputs", "")
        //verify(mockExecution).setVariable(Prefix + "requestId", "")
        verify(mockExecution).setVariable(Prefix + "messageId", "")
        verify(mockExecution).setVariable(Prefix + "source", "")
        verify(mockExecution).setVariable("BasicAuthHeaderValuePO", "")
        verify(mockExecution).setVariable("BasicAuthHeaderValueSDNC", "")
        verify(mockExecution).setVariable(Prefix + "serviceInstanceId", "")
        verify(mockExecution).setVariable("GENGS_type", "")
        verify(mockExecution).setVariable(Prefix + "rsrc_endpoint", null)
        verify(mockExecution).setVariable(Prefix + "networkOutputs", "")
        verify(mockExecution).setVariable(Prefix + "networkId", "")
        verify(mockExecution).setVariable(Prefix + "networkName", "")

        // AAI query Name
        verify(mockExecution).setVariable(Prefix + "queryNameAAIRequest", "")
        verify(mockExecution).setVariable(Prefix + "queryNameAAIResponse", "")
        verify(mockExecution).setVariable(Prefix + "aaiNameReturnCode", "")
        verify(mockExecution).setVariable(Prefix + "isAAIqueryNameGood", false)

        // AAI query Cloud Region
        verify(mockExecution).setVariable(Prefix + "queryCloudRegionRequest", "")
        verify(mockExecution).setVariable(Prefix + "queryCloudRegionReturnCode", "")
        verify(mockExecution).setVariable(Prefix + "queryCloudRegionResponse", "")
        verify(mockExecution).setVariable(Prefix + "cloudRegionPo", "")
        verify(mockExecution).setVariable(Prefix + "cloudRegionSdnc", "")
        verify(mockExecution).setVariable(Prefix + "isCloudRegionGood", false)

        // AAI query Id
        verify(mockExecution).setVariable(Prefix + "queryIdAAIRequest", "")
        verify(mockExecution).setVariable(Prefix + "queryIdAAIResponse", "")
        verify(mockExecution).setVariable(Prefix + "aaiIdReturnCode", "")

        // AAI query vpn binding
        verify(mockExecution).setVariable(Prefix + "queryVpnBindingAAIRequest", "")
        verify(mockExecution).setVariable(Prefix + "queryVpnBindingAAIResponse", "")
        verify(mockExecution).setVariable(Prefix + "aaiQqueryVpnBindingReturnCode", "")
        verify(mockExecution).setVariable(Prefix + "vpnBindings", null)
        verify(mockExecution).setVariable(Prefix + "vpnCount", 0)
        verify(mockExecution).setVariable(Prefix + "routeCollection", "")

        // AAI query network policy
        verify(mockExecution).setVariable(Prefix + "queryNetworkPolicyAAIRequest", "")
        verify(mockExecution).setVariable(Prefix + "queryNetworkPolicyAAIResponse", "")
        verify(mockExecution).setVariable(Prefix + "aaiQqueryNetworkPolicyReturnCode", "")
        verify(mockExecution).setVariable(Prefix + "networkPolicyUriList", null)
        verify(mockExecution).setVariable(Prefix + "networkPolicyCount", 0)
        verify(mockExecution).setVariable(Prefix + "networkCollection", "")

        // AAI query route table reference
        verify(mockExecution).setVariable(Prefix + "queryNetworkTableRefAAIRequest", "")
        verify(mockExecution).setVariable(Prefix + "queryNetworkTableRefAAIResponse", "")
        verify(mockExecution).setVariable(Prefix + "aaiQqueryNetworkTableRefReturnCode", "")
        verify(mockExecution).setVariable(Prefix + "networkTableRefUriList", null)
        verify(mockExecution).setVariable(Prefix + "networkTableRefCount", 0)
        verify(mockExecution).setVariable(Prefix + "tableRefCollection", "")

        // AAI requery Id
        verify(mockExecution).setVariable(Prefix + "requeryIdAAIRequest", "")
        verify(mockExecution).setVariable(Prefix + "requeryIdAAIResponse", "")
        verify(mockExecution).setVariable(Prefix + "aaiRequeryIdReturnCode", "")

        // AAI update contrail
        verify(mockExecution).setVariable(Prefix + "updateContrailAAIUrlRequest", "")
        verify(mockExecution).setVariable(Prefix + "updateContrailAAIPayloadRequest", "")
        verify(mockExecution).setVariable(Prefix + "updateContrailAAIResponse", "")
        verify(mockExecution).setVariable(Prefix + "aaiUpdateContrailReturnCode", "")

        verify(mockExecution).setVariable(Prefix + "createNetworkRequest", "")
        verify(mockExecution).setVariable(Prefix + "createNetworkResponse", "")
        verify(mockExecution).setVariable(Prefix + "rollbackNetworkRequest", "")
        verify(mockExecution).setVariable(Prefix + "networkReturnCode", "")
        verify(mockExecution).setVariable(Prefix + "isNetworkRollbackNeeded", false)

        verify(mockExecution).setVariable(Prefix + "assignSDNCRequest", "")
        verify(mockExecution).setVariable(Prefix + "assignSDNCResponse", "")
        verify(mockExecution).setVariable(Prefix + "rollbackSDNCRequest", "")
        verify(mockExecution).setVariable(Prefix + "sdncReturnCode", "")
        verify(mockExecution).setVariable(Prefix + "isSdncRollbackNeeded", false)
        verify(mockExecution).setVariable(Prefix + "sdncResponseSuccess", false)

        verify(mockExecution).setVariable(Prefix + "activateSDNCRequest", "")
        verify(mockExecution).setVariable(Prefix + "activateSDNCResponse", "")
        verify(mockExecution).setVariable(Prefix + "rollbackActivateSDNCRequest", "")
        verify(mockExecution).setVariable(Prefix + "sdncActivateReturnCode", "")
        verify(mockExecution).setVariable(Prefix + "isSdncActivateRollbackNeeded", false)
        verify(mockExecution).setVariable(Prefix + "sdncActivateResponseSuccess", false)

        verify(mockExecution).setVariable(Prefix + "orchestrationStatus", "")
        verify(mockExecution).setVariable(Prefix + "isVnfBindingPresent", false)
        verify(mockExecution).setVariable(Prefix + "Success", false)

        verify(mockExecution).setVariable(Prefix + "isException", false)
    }

    @Test
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

        when(mockExecution.getVariable("mso.adapters.po.auth")).
                thenReturn("5E12ACACBD552A415E081E29F2C4772F9835792A51C766CCFDD7433DB5220B59969CB2798C")
        when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")

        when(mockExecution.getVariable("mso.rollback")).thenReturn("true")
        when(mockExecution.getVariable("mso.adapters.sdnc.endpoint")).thenReturn("http://localhost:8090/SDNCAdapter")
        when(mockExecution.getVariable("mso.adapters.network.rest.endpoint")).thenReturn("http://localhost:8090/networks/NetworkAdapter")
        when(mockExecution.getVariable("mso.adapters.sdnc.resource.endpoint")).thenReturn("http://localhost:8090/SDNCAdapterRpc")


        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.preProcessRequest(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix)

        //verify variable initialization
        initializeVariables(mockExecution)

        verify(mockExecution).setVariable("action", "CREATE")
        verify(mockExecution).setVariable(Prefix + "networkId", "")
        verify(mockExecution).setVariable(Prefix + "networkRequest", expectedvIPRNetworkRequest)
        verify(mockExecution, atLeast(1)).setVariable(Prefix + "rollbackEnabled", false)
        verify(mockExecution).setVariable(Prefix + "networkInputs", expectedvIPRNetworkInputs)
        verify(mockExecution).setVariable(Prefix + "messageId", "88f65519-9a38-4c4b-8445-9eb4a5a5af56")
        verify(mockExecution).setVariable(Prefix + "source", "VID")
        verify(mockExecution).setVariable("BasicAuthHeaderValuePO", "Basic dGVzdDp0ZXN0")
        verify(mockExecution).setVariable("BasicAuthHeaderValueSDNC", "Basic dGVzdDp0ZXN0")
        verify(mockExecution).setVariable(Prefix + "serviceInstanceId", "f70e927b-6087-4974-9ef8-c5e4d5847ca4")
        verify(mockExecution, atLeast(1)).setVariable("GENGS_type", "service-instance")
        verify(mockExecution).setVariable(Prefix + "networkId", "")
        verify(mockExecution).setVariable(Prefix + "networkOutputs", networkvIPROutputs)
        verify(mockExecution).setVariable(Prefix + "networkName", "")
    }

    @Test
    public void preProcessRequest_JSON_NetworkRequest() {

        println "************ preProcessRequest_Payload ************* "
        ExecutionEntity mockExecution = setupMock()
        // Initialize prerequisite variables

        // Pre-defined value, testing Only
        when(mockExecution.getVariable("testMessageId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
        // Inputs:
        when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
        when(mockExecution.getVariable("requestAction")).thenReturn("CREATE")
        when(mockExecution.getVariable("networkId")).thenReturn("networkId")         // optional
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("bpmnRequest")).thenReturn(jsonIncomingRequest)
        // JSON format
        when(mockExecution.getVariable("sdncVersion")).thenReturn("1610")            // 1610 default
        when(mockExecution.getVariable("disableRollback")).thenReturn(true)

        when(mockExecution.getVariable("mso.adapters.po.auth")).
                thenReturn("5E12ACACBD552A415E081E29F2C4772F9835792A51C766CCFDD7433DB5220B59969CB2798C")
        when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")

        when(mockExecution.getVariable("mso.rollback")).thenReturn("true")
        when(mockExecution.getVariable("mso.adapters.sdnc.endpoint")).thenReturn("http://localhost:8090/SDNCAdapter")
        when(mockExecution.getVariable("mso.adapters.network.rest.endpoint")).thenReturn("http://localhost:8090/networks/NetworkAdapter")
        when(mockExecution.getVariable("mso.adapters.sdnc.resource.endpoint")).thenReturn("http://localhost:8090/SDNCAdapterRpc")

        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.preProcessRequest(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix)

        //verify variable initialization
        initializeVariables(mockExecution)

        verify(mockExecution).setVariable("action", "CREATE")
        verify(mockExecution).setVariable(Prefix + "networkRequest", expectedJSONNetworkRequest)
        verify(mockExecution).setVariable(Prefix + "rollbackEnabled", false)
        verify(mockExecution).setVariable(Prefix + "networkInputs", expectedJSONNetworkInputs)
        verify(mockExecution).setVariable(Prefix + "source", "VID")
        verify(mockExecution).setVariable("BasicAuthHeaderValuePO", "Basic dGVzdDp0ZXN0")
        verify(mockExecution).setVariable("BasicAuthHeaderValueSDNC", "Basic dGVzdDp0ZXN0")
        verify(mockExecution).setVariable(Prefix + "serviceInstanceId", "f70e927b-6087-4974-9ef8-c5e4d5847ca4")
        verify(mockExecution, atLeast(1)).setVariable("GENGS_type", "service-instance")
        verify(mockExecution).setVariable(Prefix + "networkId", "")
        verify(mockExecution).setVariable(Prefix + "networkOutputs", networkJSONOutputs)
        verify(mockExecution).setVariable(Prefix + "networkName", "")
    }

    @Test
    public void preProcessRequest_XML_NetworkRequest() {

        println "************ preProcessRequest_Payload ************* "
        ExecutionEntity mockExecution = setupMock()
        // Initialize prerequisite variables

        // Pre-defined value, testing Only
        when(mockExecution.getVariable("testMessageId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
        // Inputs:
        when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("bpmnRequest")).thenReturn(xmlIncomingRequest)                      // XML format

        when(mockExecution.getVariable("mso.adapters.po.auth")).
                thenReturn("5E12ACACBD552A415E081E29F2C4772F9835792A51C766CCFDD7433DB5220B59969CB2798C")
        when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")

        when(mockExecution.getVariable("mso.rollback")).thenReturn("true")
        when(mockExecution.getVariable("mso.adapters.sdnc.endpoint")).thenReturn("http://localhost:8090/SDNCAdapter")
        when(mockExecution.getVariable("mso.adapters.network.rest.endpoint")).thenReturn("http://localhost:8090/networks/NetworkAdapter")
        when(mockExecution.getVariable("mso.adapters.sdnc.resource.endpoint")).thenReturn("http://localhost:8090/SDNCAdapterRpc")

        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.preProcessRequest(mockExecution)

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
        verify(mockExecution).setVariable("BasicAuthHeaderValuePO", "Basic dGVzdDp0ZXN0")
        verify(mockExecution).setVariable("BasicAuthHeaderValueSDNC", "Basic dGVzdDp0ZXN0")
        verify(mockExecution).setVariable(Prefix + "serviceInstanceId", "MNS-25180-L-01-dmz_direct_net_1")
        verify(mockExecution, atLeast(1)).setVariable("GENGS_type", "service-instance")
        verify(mockExecution, atLeast(1)).setVariable(Prefix + "networkId", "")
        verify(mockExecution).setVariable(Prefix + "networkOutputs", networkXMLOutputs)
        verify(mockExecution).setVariable(Prefix + "networkName", "")
    }


    @Test
    public void prepareCreateNetworkRequest() {

        println "************ prepareNetworkRequest ************* "
        ExecutionEntity mockExecution = setupMock()
        // Initialize prerequisite variables
        when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(expectedvIPRNetworkRequest)
        when(mockExecution.getVariable(Prefix + "queryIdAAIResponse")).thenReturn(getContrailL3Network())
        when(mockExecution.getVariable(Prefix + "cloudRegionPo")).thenReturn("RDM2WAGPLCP")
        when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("messageId_generated")
        when(mockExecution.getVariable(Prefix + "source")).thenReturn("VID")
        when(mockExecution.getVariable(Prefix + "routeCollection")).
                thenReturn("<routeTargets>13979:105757</routeTargets><routeTargets>13979:105757</routeTargets>")
        when(mockExecution.getVariable(Prefix + "networkCollection")).
                thenReturn("<policyFqdns>GN_EVPN_Test</policyFqdns>")
        when(mockExecution.getVariable(Prefix + "tableRefCollection")).
                thenReturn("<routeTableFqdns>refFQDN1</routeTableFqdns><routeTableFqdns>refFQDN2</routeTableFqdns>")
        when(mockExecution.getVariable(Prefix + "rollbackEnabled")).thenReturn("true")
        when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")

        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.prepareCreateNetworkRequest(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix)
        verify(mockExecution).setVariable(Prefix + "createNetworkRequest", createNetworkRequest)
    }


    @Test
    public void prepareCreateNetworkRequest_Ipv4() {

        println "************ prepareNetworkRequest ************* "
        ExecutionEntity mockExecution = setupMock()
        // Initialize prerequisite variables
        when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(expectedvIPRNetworkRequest)
        when(mockExecution.getVariable(Prefix + "queryIdAAIResponse")).thenReturn(getAlaCarteL3Network())
        when(mockExecution.getVariable(Prefix + "cloudRegionPo")).thenReturn("RDM2WAGPLCP")
        when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("messageId_generated")
        when(mockExecution.getVariable(Prefix + "source")).thenReturn("VID")
        when(mockExecution.getVariable(Prefix + "routeCollection")).
                thenReturn("<routeTargets>13979:105757</routeTargets><routeTargets>13979:105757</routeTargets>")
        when(mockExecution.getVariable(Prefix + "networkCollection")).
                thenReturn("<policyFqdns>GN_EVPN_Test</policyFqdns>")
        when(mockExecution.getVariable(Prefix + "tableRefCollection")).
                thenReturn("<routeTableFqdns>refFQDN1</routeTableFqdns><routeTableFqdns>refFQDN2</routeTableFqdns>")
        when(mockExecution.getVariable(Prefix + "rollbackEnabled")).thenReturn("true")
        when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")

        //prepareUrnPropertiesReader()

        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.prepareCreateNetworkRequest(mockExecution)

        // verify set prefix = Prefix + ""
        verify(mockExecution).setVariable("prefix", Prefix)

        verify(mockExecution).setVariable(Prefix + "createNetworkRequest", createNetworkRequest_Ipv4)
    }

    @Test
    public void prepareCreateNetworkRequest_AlaCarte() {

        println "************ prepareNetworkRequest ************* "
        ExecutionEntity mockExecution = setupMock()
        // Initialize prerequisite variables
        when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(expectedJSONNetworkRequest)
        when(mockExecution.getVariable(Prefix + "queryIdAAIResponse")).thenReturn(getAlaCarteL3Network())
        when(mockExecution.getVariable(Prefix + "cloudRegionPo")).thenReturn("RDM2WAGPLCP")
        when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("messageId_generated")
        when(mockExecution.getVariable(Prefix + "source")).thenReturn("VID")
        when(mockExecution.getVariable(Prefix + "routeCollection")).
                thenReturn("<routeTargets>13979:105757</routeTargets><routeTargets>13979:105757</routeTargets>")
        when(mockExecution.getVariable(Prefix + "networkCollection")).
                thenReturn("<policyFqdns>GN_EVPN_Test</policyFqdns>")
        when(mockExecution.getVariable(Prefix + "tableRefCollection")).
                thenReturn("<routeTableFqdns>refFQDN1</routeTableFqdns><routeTableFqdns>refFQDN2</routeTableFqdns>")
        when(mockExecution.getVariable(Prefix + "rollbackEnabled")).thenReturn("true")
        when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")

        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.prepareCreateNetworkRequest(mockExecution)

        // verify set prefix = Prefix + ""
        verify(mockExecution).setVariable("prefix", Prefix)

        verify(mockExecution).setVariable(Prefix + "createNetworkRequest", createNetworkRequestAlaCarte)
    }

    @Test
    public void prepareCreateNetworkRequest_SRIOV() {

        println "************ prepareNetworkRequest ************* "
        ExecutionEntity mockExecution = setupMock()
        // Initialize prerequisite variables
        when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(expectedvIPRNetworkRequest)
        when(mockExecution.getVariable(Prefix + "queryIdAAIResponse")).thenReturn(getSRIOVL3Network())
        when(mockExecution.getVariable(Prefix + "cloudRegionPo")).thenReturn("RDM2WAGPLCP")
        when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("messageId_generated")
        when(mockExecution.getVariable(Prefix + "source")).thenReturn("VID")
        when(mockExecution.getVariable(Prefix + "routeCollection")).thenReturn("<routeTargets>13979:105757</routeTargets><routeTargets>13979:105757</routeTargets>")
        when(mockExecution.getVariable(Prefix + "networkCollection")).thenReturn("<policyFqdns>GN_EVPN_Test</policyFqdns>")
        when(mockExecution.getVariable(Prefix + "tableRefCollection")).thenReturn("<routeTableFqdns>refFQDN1</routeTableFqdns><routeTableFqdns>refFQDN2</routeTableFqdns>")
        when(mockExecution.getVariable(Prefix + "rollbackEnabled")).thenReturn("true")
        when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")

        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.prepareCreateNetworkRequest(mockExecution)

        // verify set prefix = Prefix + ""
        verify(mockExecution).setVariable("prefix", Prefix)

        verify(mockExecution).setVariable(Prefix + "createNetworkRequest", createNetworkRequest_SRIOV)
    }


    @Test
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

        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.prepareSDNCRequest(mockExecution)

        // verify set prefix = Prefix + ""
        verify(mockExecution).setVariable("prefix", Prefix)
        verify(mockExecution).setVariable(Prefix + "assignSDNCRequest", assignSDNCRequest)
    }

    @Test
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

        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.prepareSDNCRequest(mockExecution)

        // verify set prefix = Prefix + ""
        verify(mockExecution).setVariable("prefix", Prefix)
        verify(mockExecution).setVariable(Prefix + "assignSDNCRequest", assignSDNCRequest_decodeUrlLink)
    }

    @Test
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

        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.prepareRpcSDNCRequest(mockExecution)

        // verify set prefix = Prefix + ""
        verify(mockExecution).setVariable("prefix", Prefix)
        verify(mockExecution).setVariable(Prefix + "assignSDNCRequest", assignRpcSDNCRequest)
    }

    @Test
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

        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.prepareSDNCRollbackRequest(mockExecution)

        // verify set prefix = Prefix + ""
        verify(mockExecution).setVariable("prefix", Prefix)
        verify(mockExecution).setVariable(Prefix + "rollbackSDNCRequest", sdncRollbackRequest)
    }

    @Test
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

        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.prepareRpcSDNCActivateRequest(mockExecution)

        // verify set prefix = Prefix + ""
        verify(mockExecution).setVariable("prefix", Prefix)
        verify(mockExecution).setVariable(Prefix + "activateSDNCRequest", activateSDNCRequest)
    }


    @Test
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

        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.prepareRpcSDNCRollbackRequest(mockExecution)

        // verify set prefix = Prefix + ""
        verify(mockExecution).setVariable("prefix", Prefix)
        verify(mockExecution).setVariable(Prefix + "rollbackSDNCRequest", sdncRpcRollbackRequest)
    }

    @Test
    public void prepareRpcSDNCActivateRollback() {

        println "************ prepareRpcSDNCActivateRollback ************* "

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        // Initialize prerequisite variables
        when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(expectedvIPRNetworkRequest)
        when(mockExecution.getVariable("testMessageId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
        when(mockExecution.getVariable(Prefix + "serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
        when(mockExecution.getVariable(Prefix + "networkId")).thenReturn("8abc633a-810b-4ca5-8b3a-09511d13a2ce")
        when(mockExecution.getVariable(Prefix + "cloudRegionSdnc")).thenReturn("RDM2WAGPLCP")
        when(mockExecution.getVariable("GENGSI_siResourceLink")).thenReturn(
                "https://aai-int1.test.com:8443/aai/v8/business/customers/customer/MSO_1610_dev/service-subscriptions/service-subscription/MSO-dev-service-type/service-instances/service-instance/6d4eb22a-82f1-4257-9f80-4176262cfe69/")

        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.prepareRpcSDNCActivateRollback(mockExecution)

        // verify set prefix = Prefix + ""
        verify(mockExecution).setVariable("prefix", Prefix)
        verify(mockExecution).setVariable(Prefix + "rollbackActivateSDNCRequest", sdncActivateRollbackRequest)
    }

    @Test
    public void callRESTQueryAAINetworkName_200() {

        println "************ callRESTQueryAAINetworkName ************* "

        WireMock.reset();
        MockGetNetworkByName("MNS-25180-L-01-dmz_direct_net_1",
                "BuildingBlocks/Network/queryAAINetworkListResponse.json")
        prepareUrnPropertiesReader()

        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable(Prefix + "networkInputs")).thenReturn(networkInputs)

        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.callRESTQueryAAINetworkName(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix)
        verify(mockExecution).setVariable(Prefix + "isAAIqueryNameGood", true)

        verify(mockExecution).setVariable(Prefix + "orchestrationStatus", "PENDING-CREATE")
        verify(mockExecution).setVariable("orchestrationStatus", "pending-create")
    }

    @Test
    public void callRESTQueryAAINetworkName_404() {

        println "************ callRESTQueryAAINetworkName ************* "

        WireMock.reset();
        MockGetNetworkByName_404("", "MNS-25180-L-01-dmz_direct_net_1")
        prepareUrnPropertiesReader()

        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable(Prefix + "networkInputs")).thenReturn(networkInputs)

        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.callRESTQueryAAINetworkName(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix)
        verify(mockExecution, never()).setVariable(Prefix + "isAAIqueryNameGood", true)
    }

    @Test
    public void callRESTQueryAAINetworkId_200() {

        println "************ callRESTQueryAAINetworkId ************* "

        WireMock.reset();
        MockGetNetworkByIdWithDepth("49c86598-f766-46f8-84f8-8d1c1b10f9b4",
                "BuildingBlocks/Network/queryAAINetworkTestResponse.json", "1")
        prepareUrnPropertiesReader()

        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable(Prefix + "assignSDNCResponse")).thenReturn(sdncAdapterWorkflowAssignResponse)
        when(mockExecution.getVariable("sdncVersion")).thenReturn("1702")

        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.callRESTQueryAAINetworkId(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix)
        verify(mockExecution).setVariable(Prefix + "networkId", "49c86598-f766-46f8-84f8-8d1c1b10f9b4")
        verify(mockExecution).setVariable(Prefix + "networkName", null)
        verify(mockExecution).setVariable(eq(Prefix + "queryIdAAIResponse"), captor.capture())
        Assert.assertEquals(captor.getValue().getNetworkName(), "vprobes_pktinternal_net_4_1806")
        verify(mockExecution).setVariable(Prefix + "networkId", "467e3349-bec1-4922-bcb1-d0bb041bce30")
        verify(mockExecution).setVariable(Prefix + "networkName", "vprobes_pktinternal_net_4_1806")
    }

    @Test
    public void callRESTQueryAAICloudRegion30_200() {

        println "************ callRESTQueryAAICloudRegion30_200 ************* "

        WireMock.reset();
        MockGetNetworkCloudRegion("CreateNetworkV2/cloudRegion25_AAIResponse_Success.xml", "RDM2WAGPLCP")
        prepareUrnPropertiesReader()

        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(Prefix)
        when(mockExecution.getVariable(Prefix + "networkInputs")).thenReturn(expectedvIPRNetworkRequest)
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:8090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn("8")
        when(mockExecution.getVariable("mso.workflow.default.aai.v8.cloud-region.uri")).thenReturn("/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
        when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.callRESTQueryAAICloudRegion(mockExecution)

        verify(mockExecution, atLeast(1)).setVariable("prefix", Prefix)
        verify(mockExecution, atLeast(2)).setVariable(Prefix + "queryCloudRegionReturnCode", "200")
        verify(mockExecution).setVariable(Prefix + "isCloudRegionGood", true)
    }

    @Test
    public void callRESTQueryAAICloudRegion25_200() {

        println "************ callRESTQueryAAICloudRegion25_200 ************* "

        WireMock.reset();
        MockGetNetworkCloudRegion("CreateNetworkV2/cloudRegion25_AAIResponse_Success.xml", "RDM2WAGPLCP")
        prepareUrnPropertiesReader()

        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable("prefix")).thenReturn(Prefix)
        when(mockExecution.getVariable(Prefix + "networkInputs")).thenReturn(expectedvIPRNetworkRequest)
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:8090")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn("8")
        when(mockExecution.getVariable("mso.workflow.default.aai.v8.cloud-region.uri")).thenReturn("/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner")
        //
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
        when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.callRESTQueryAAICloudRegion(mockExecution)

        verify(mockExecution, atLeast(1)).setVariable("prefix", Prefix)
        verify(mockExecution, atLeast(2)).setVariable(Prefix + "queryCloudRegionReturnCode", "200")
        verify(mockExecution).setVariable(Prefix + "isCloudRegionGood", true)
    }

    @Test
    public void callRESTQueryAAICloudRegion_NotFound() {

        println "************ callRESTQueryAAICloudRegionFake ************* "

        WireMock.reset();
        MockGetNetworkCloudRegion_404("MDTWNJ21")
        prepareUrnPropertiesReader()

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

        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.callRESTQueryAAICloudRegion(mockExecution)

        verify(mockExecution, atLeast(1)).setVariable("prefix", Prefix)
        verify(mockExecution, atLeast(1)).setVariable(Prefix + "queryCloudRegionReturnCode", "404")
        verify(mockExecution).setVariable(Prefix + "cloudRegionPo", "MDTWNJ21")
        verify(mockExecution).setVariable(Prefix + "cloudRegionSdnc", "AAIAIC25")
        verify(mockExecution).setVariable(Prefix + "isCloudRegionGood", true)
    }

    @Test
    public void callRESTQueryAAINetworkVpnBinding_200() {

        println "************ callRESTQueryAAINetworkVpnBinding_200 ************* "

        WireMock.reset()
        MockGetNetwork("467e3349-bec1-4922-bcb1-d0bb041bce30",
                "BuildingBlocks/Network/queryAAIResponseEmptyUri.json", 200)
        MockGetNetworkByIdWithDepth("467e3349-bec1-4922-bcb1-d0bb041bce30",
                "BuildingBlocks/Network/queryAAIResponseEmptyUri.json", "2")
        prepareUrnPropertiesReader()

        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable(Prefix + "networkId")).thenReturn("467e3349-bec1-4922-bcb1-d0bb041bce30")

        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.callRESTQueryAAINetworkVpnBinding(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix)
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
        MockGetNetwork("467e3349-bec1-4922-bcb1-d0bb041bce30",
                "BuildingBlocks/Network/queryAAINetworkTestResponse.json", 200);
        MockGetNetworkByIdWithDepth("467e3349-bec1-4922-bcb1-d0bb041bce30",
                "BuildingBlocks/Network/queryAAINetworkTestResponse.json", "2");
        MockGetNetworkVpnBindingWithDepth("BuildingBlocks/Network/queryAAIVpnBindingTestResponseWithRoutes.json",
                "13e94b71-3ce1-4988-ab0e-61208fc91f1c", "2");

        prepareUrnPropertiesReader()

        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable(Prefix + "networkId")).thenReturn("467e3349-bec1-4922-bcb1-d0bb041bce30")

        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.callRESTQueryAAINetworkVpnBinding(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix)
        verify(mockExecution).setVariable("prefix", Prefix)
        verify(mockExecution).setVariable(eq(Prefix + "routeCollection"), eq("""<routeTargets>
 <routeTarget>2001:051111</routeTarget>\n <routeTargetRole>EXPORT</routeTargetRole>\n</routeTargets>
<routeTargets>\n <routeTarget>1000:051113</routeTarget>\n <routeTargetRole>IMPORT</routeTargetRole>\n</routeTargets>\n"""))
    }


    @Test
    public void callRESTQueryAAINetworkVpnBinding_200_URN_Uri() {

        println "************ callRESTQueryAAINetworkVpnBinding_200 ************* "

        WireMock.reset();
        MockGetNetwork("467e3349-bec1-4922-bcb1-d0bb041bce30",
                "BuildingBlocks/Network/queryAAINetworkTestResponse.json", 200)
        MockGetNetworkVpnBindingWithDepth("BuildingBlocks/Network/queryAAIVpnBindingTestResponseWithRoutes.json",
                "13e94b71-3ce1-4988-ab0e-61208fc91f1c", "2")

        prepareUrnPropertiesReader()

        ExecutionEntity mockExecution = setupMock()
        // Initialize prerequisite variables
        when(mockExecution.getVariable(Prefix + "networkId")).thenReturn("467e3349-bec1-4922-bcb1-d0bb041bce30")

        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.callRESTQueryAAINetworkVpnBinding(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix)
        verify(mockExecution).setVariable(Prefix + "routeCollection",
                """<routeTargets>\n <routeTarget>2001:051111</routeTarget>\n <routeTargetRole>EXPORT</routeTargetRole>
</routeTargets>\n<routeTargets>\n <routeTarget>1000:051113</routeTarget>\n <routeTargetRole>IMPORT</routeTargetRole>
</routeTargets>\n""")
    }

    @Test
    public void callRESTQueryAAINetworkVpnBinding_NotPresent() {

        println "************ callRESTQueryAAINetworkVpnBinding_NotPresent ************* "

        WireMock.reset();
        MockGetNetwork("467e3349-bec1-4922-bcb1-d0bb041bce30",
                "BuildingBlocks/Network/queryAAINetworkTestResponse.json", 200);
        MockGetNetworkByIdWithDepth("467e3349-bec1-4922-bcb1-d0bb041bce30",
                "BuildingBlocks/Network/queryAAINetworkTestResponse.json", "2");
        MockGetNetworkVpnBindingWithDepth("BuildingBlocks/Network/queryAAIVpnBindingTestResponse.json",
                "13e94b71-3ce1-4988-ab0e-61208fc91f1c", "2");

        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable(Prefix + "networkId")).thenReturn("467e3349-bec1-4922-bcb1-d0bb041bce30")

        prepareUrnPropertiesReader()

        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.callRESTQueryAAINetworkVpnBinding(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix)
        verify(mockExecution).setVariable(eq(Prefix + "routeCollection"), eq(""))
    }

    @Test
    public void callRESTQueryAAINetworkPolicy_200() {

        println "************ callRESTQueryAAINetworkPolicy_200 ************* "

        WireMock.reset();
        MockGetNetwork("467e3349-bec1-4922-bcb1-d0bb041bce30",
                "BuildingBlocks/Network/queryAAIResponseEmptyUri.json", 200)
        prepareUrnPropertiesReader()

        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable(Prefix + "networkId")).thenReturn("467e3349-bec1-4922-bcb1-d0bb041bce30")

        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.callRESTQueryAAINetworkPolicy(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix)
        verify(mockExecution).setVariable(Prefix + "networkPolicyCount", 0)
        verify(mockExecution).setVariable(Prefix + "aaiQqueryNetworkPolicyReturnCode", "200")
        verify(mockExecution).setVariable(Prefix + "queryNetworkPolicyAAIResponse",
                """<rest:payload xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"
              xmlns="defaultTestNamespacev14"\n              contentType="text/xml">
   <network-policy>\n      <network-policy-fqdn/>\n   </network-policy>\n</rest:payload>""")
        verify(mockExecution).setVariable(Prefix + "networkCollection", "<policyFqdns/>")
    }

    @Test
    public void callRESTQueryAAINetworkTableRef_200() {

        println "************ callRESTQueryAAINetworkTableRef_200 ************* "

        WireMock.reset();

        MockGetNetwork("467e3349-bec1-4922-bcb1-d0bb041bce30",
                "BuildingBlocks/Network/queryAAIResponseEmptyUri.json", 200)
        prepareUrnPropertiesReader()

        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable(Prefix + "networkId")).thenReturn("467e3349-bec1-4922-bcb1-d0bb041bce30")

        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.callRESTQueryAAINetworkTableRef(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix)
        verify(mockExecution).setVariable(Prefix + "networkTableRefCount", 0)
        verify(mockExecution).setVariable(Prefix + "aaiQqueryNetworkTableRefReturnCode", "200")
        verify(mockExecution).setVariable(Prefix + "queryNetworkTableRefAAIResponse",
                """<rest:payload xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"
              xmlns="defaultTestNamespacev14"\n              contentType="text/xml">\n   <route-table-references>
      <route-table-reference-fqdn/>\n   </route-table-references>\n</rest:payload>""")
        verify(mockExecution).setVariable(Prefix + "tableRefCollection", "<routeTableFqdns/>")
    }

    @Test
    public void callRESTReQueryAAINetworkId_200() {

        println "************ callRESTReQueryAAINetworkId ************* "

        WireMock.reset();
        MockGetNetworkByIdWithDepth("467e3349-bec1-4922-bcb1-d0bb041bce30",
                "BuildingBlocks/Network/queryAAIResponseEmptyUri.json", "1");

        prepareUrnPropertiesReader()

        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable(Prefix + "networkId")).thenReturn("467e3349-bec1-4922-bcb1-d0bb041bce30")
        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.callRESTReQueryAAINetworkId(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix)
        verify(mockExecution).setVariable(Prefix + "aaiRequeryIdReturnCode", "200")
        verify(mockExecution).setVariable(eq(Prefix + "requeryIdAAIResponse"), captor.capture())
        Assert.assertEquals(captor.getValue().getNetworkName(), "Dev_Bindings_1802_020118")
        verify(mockExecution).setVariable(Prefix + "networkOutputs", """<network-outputs>
                   <network-id>467e3349-bec1-4922-bcb1-d0bb041bce30</network-id>
                   <network-name>Dev_Bindings_1802_020118</network-name>
                 </network-outputs>""")
    }

    @Test
    public void callRESTUpdateContrailAAINetworkREST_200() {
        WireMock.reset()

        stubFor(post(urlMatching("/aai/v[0-9]+/network/l3-networks/l3-network/467e3349-bec1-4922-bcb1-d0bb041bce30"))
                .willReturn(aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "text/xml")))
        prepareUrnPropertiesReader()

        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable(Prefix + "networkId")).thenReturn("467e3349-bec1-4922-bcb1-d0bb041bce30")
        when(mockExecution.getVariable(Prefix + "requeryIdAAIResponse")).thenReturn(new L3Network())
        when(mockExecution.getVariable(Prefix + "createNetworkResponse")).thenReturn(createNetworkResponseREST)

        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.callRESTUpdateContrailAAINetwork(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix)
        verify(mockExecution).setVariable(Prefix + "isPONR", true)
    }

    @Test
    public void callRESTUpdateContrailAAINetworkREST_200_segmentation() {

        println "************ callRESTUpdateContrailAAINetwork ************* "

        WireMock.reset()

        stubFor(post(urlMatching(
                "/aai/v[0-9]+/network/l3-networks/l3-network/467e3349-bec1-4922-bcb1-d0bb041bce30"))
                .willReturn(aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "text/xml")))
        stubFor(post(urlMatching(
                "/aai/v[0-9]+/network/l3-networks/l3-network/467e3349-bec1-4922-bcb1-d0bb041bce30/subnets/subnet/57e9a1ff-d14f-4071-a828-b19ae98eb2fc"))
                .willReturn(aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "text/xml")))
        prepareUrnPropertiesReader()

        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable(Prefix + "networkId")).thenReturn("467e3349-bec1-4922-bcb1-d0bb041bce30")
        when(mockExecution.getVariable(Prefix + "requeryIdAAIResponse")).thenReturn(getContrailL3Network())
        when(mockExecution.getVariable(Prefix + "createNetworkResponse")).thenReturn(createNetworkResponseREST)

        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.callRESTUpdateContrailAAINetwork(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix)
        verify(mockExecution).setVariable(Prefix + "isPONR", true)
    }


    @Test
    public void validateCreateNetworkResponseREST() {

        println "************ validateNetworkResponse ************* "

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        // Initialize prerequisite variables
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable(Prefix + "createNetworkResponse")).thenReturn(createNetworkResponseREST)
        when(mockExecution.getVariable(Prefix + "networkReturnCode")).thenReturn('200')

        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.validateCreateNetworkResponse(mockExecution)

        verify(mockExecution).setVariable("prefix", Prefix)
        verify(mockExecution).setVariable(Prefix + "createNetworkResponse", createNetworkResponseREST)
        verify(mockExecution).setVariable(Prefix + "isNetworkRollbackNeeded", true)
        verify(mockExecution).setVariable(Prefix + "rollbackNetworkRequest", createRollbackNetworkRequest)
    }

    @Test
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
        verify(mockExecution, atLeast(1)).setVariable(eq("WorkflowException"), refEq(workflowException))
    }

    @Test
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
    }

    @Test
    public void validateSDNCResponse_Error() {

        println "************ validateSDNCResponse ************* "

        WorkflowException workflowException = new WorkflowException("DoCreateNetworkInstance", 2500, "Received error from SNDC Adapter: HTTP Status 500.")

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
    }

    @Test
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
    }

    @Test
    public void prepareRollbackData() {

        println "************ prepareRollbackData() ************* "

        WorkflowException workflowException = new WorkflowException(
                "DoCreateNetworkInstance", 2500, "Received error from Network Adapter: JBWEB000065: HTTP Status 500.")

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        // Initialize prerequisite variables
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable(Prefix + "rollbackSDNCRequest")).thenReturn(rollbackSDNCRequest)
        when(mockExecution.getVariable(Prefix + "rollbackActivateSDNCRequest")).thenReturn(rollbackActivateSDNCRequest)
        when(mockExecution.getVariable(Prefix + "rollbackNetworkRequest")).thenReturn(rollbackNetworkRequest)
        when(mockExecution.getVariable("WorkflowException")).thenReturn(workflowException)

        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.prepareRollbackData(mockExecution)

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

        DoCreateNetworkInstance DoCreateNetworkInstance = new DoCreateNetworkInstance()
        DoCreateNetworkInstance.postProcessResponse(mockExecution)

        verify(mockExecution, atLeastOnce()).setVariable("prefix", Prefix)
        verify(mockExecution, atLeastOnce()).setVariable(Prefix + "Success", true)
    }


    private static L3Network getAlaCarteL3Network() {

        Subnet subnet = new Subnet(networkStartAddress: "107.239.52.0", cidrMask: "24", dhcpEnabled: true,
                gatewayAddress: "107.239.52.1", ipVersion: "4", subnetId: "57e9a1ff-d14f-4071-a828-b19ae98eb2fc",
                subnetName: "subnetName")
        Subnets subnets = new Subnets()
        subnets.getSubnet().add(subnet)
        subnets.getSubnet().add(subnet)

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
                isSharedNetwork: true,
                subnets: subnets,
                segmentationAssignments: segments
        )
    }

    private static L3Network getContrailL3Network() {

        HostRoutes routes1 = new HostRoutes()
        routes1.getHostRoute().add(new HostRoute(routePrefix: "192.10.16.0/24", nextHop: "192.10.16.100/24"))
        routes1.getHostRoute().add(new HostRoute(routePrefix: "192.110.17.0/24", nextHop: "192.110.17.110/24"))

        Subnet subnet1 = new Subnet(networkStartAddress: "107.239.52.0", cidrMask: "24", dhcpEnabled: true,
                gatewayAddress: "107.239.52.1", ipVersion: "4", subnetId: "57e9a1ff-d14f-4071-a828-b19ae98eb2fc",
                subnetName: "subnetName", ipAssignmentDirection: "true", hostRoutes: routes1)
        Subnets subnets = new Subnets()
        subnets.getSubnet().add(subnet1)

        HostRoutes routes2 = new HostRoutes()
        routes2.getHostRoute().add(new HostRoute(routePrefix: "192.10.16.0/24", nextHop: "192.10.16.100/24"))

        Subnet subnet2 = new Subnet(networkStartAddress: "107.239.52.0", cidrMask: "24", dhcpEnabled: true,
                gatewayAddress: "107.239.52.1", ipVersion: "4", subnetId: "57e9a1ff-d14f-4071-a828-b19ae98eb2fc",
                subnetName: "subnetName", ipAssignmentDirection: "true", hostRoutes: routes2)
        subnets.getSubnet().add(subnet2)

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
                isSharedNetwork: true,
                subnets: subnets,
                segmentationAssignments: segments
        )
    }

    private static L3Network getSRIOVL3Network() {

        Subnet subnet = new Subnet(dhcpStart: "192.168.6.3", dhcpEnd: "192.168.6.62", networkStartAddress: "192.168.6.0",
                cidrMask: "26", dhcpEnabled: true, gatewayAddress: "192.168.6.1", ipVersion: "4", subnetId: "10437",
                subnetName: "MSO_TEST_1702_A_HnportalProviderNetwork.HNPortalPROVIDERNETWORK.SR_IOV_Provider2_1_net_17_S0")
        Subnets subnets = new Subnets()
        subnets.getSubnet().add(subnet)

        return new L3Network(
                networkName: "MSO_TEST_1702_A_HnportalProviderNetwork.HNPortalPROVIDERNETWORK.SR_IOV_Provider2_1_net_17",
                networkType: "CONTRAIL_EXTERNAL",
                networkTechnology: "AIC_SR_IOV",
                networkId: "6cb1ae5a-d2db-4eb6-97bf-d52a506a53d8",
                physicalNetworkName: "Physnet21",
                isSharedNetwork: true,
                subnets: subnets
        )
    }

    private static void prepareUrnPropertiesReader() {
        MockEnvironment mockEnvironment = mock(MockEnvironment.class)
        when(mockEnvironment.getProperty("mso.workflow.global.default.aai.version")).thenReturn("14")
        when(mockEnvironment.getProperty("mso.workflow.global.default.aai.namespace")).thenReturn("defaultTestNamespace")
        when(mockEnvironment.getProperty("aai.endpoint")).thenReturn("http://localhost:8090")
        UrnPropertiesReader urnPropertiesReader = new UrnPropertiesReader()
        urnPropertiesReader.setEnvironment(mockEnvironment)
    }

    private ExecutionEntity setupMock() {

        ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
        when(mockProcessDefinition.getKey()).thenReturn("DoCreateNetworkInstance")
        RepositoryService mockRepositoryService = mock(RepositoryService.class)
        when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
        when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("DoCreateNetworkInstance")
        ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
        when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)

        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        // Initialize prerequisite variables

        when(mockExecution.getProcessDefinitionId()).thenReturn("DoCreateNetworkInstance")
        when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
        when(mockExecution.getProcessEngineServices().getRepositoryService().
                getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)

        return mockExecution
    }
}
