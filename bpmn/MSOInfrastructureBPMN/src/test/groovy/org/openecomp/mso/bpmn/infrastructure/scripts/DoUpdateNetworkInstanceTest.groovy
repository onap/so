package org.openecomp.mso.bpmn.infrastructure.scripts

import static org.mockito.Mockito.*
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetNetworkByIdWithDepth;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetNetworkCloudRegion;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetNetworkCloudRegion_404;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetNetworkVpnBinding;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetNetworkTableReference;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetNetworkPolicy;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPutNetworkIdWithDepth;
import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner
import org.openecomp.mso.bpmn.common.scripts.MsoUtils
import org.openecomp.mso.bpmn.core.WorkflowException

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.apache.commons.lang3.*


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
  """<vnfreq:network-request xmlns:vnfreq="http://org.openecomp/mso/infra/vnf-request/v1">
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
               <orchestration-status>pending-update</orchestration-status>
               <dhcp-enabled>true</dhcp-enabled>
	  		   <subnet-name>subnetName</subnet-name>
	  		   <ip-assignment-direction>true</ip-assignment-direction>
			   <host-routes>
					<host-route>
						<host-route-id>400d286b-7e44-4514-b9b3-f70f7360ff32</host-route-id>
						<route-prefix>172.20.1.0/24</route-prefix>
						<next-hop>10.102.200.1</next-hop>
	  					<next-hop-type>ip-address</next-hop-type>
						<resource-version>1505857300987</resource-version>
					</host-route>
						<host-route>
						<host-route-id>6f038013-8b15-4eb8-914b-507489fbc8ee</host-route-id>
						<route-prefix>10.102.0.0/16</route-prefix>
						<next-hop>10.102.200.1</next-hop>
	  					<next-hop-type>ip-address</next-hop-type>
						<resource-version>1505857301151</resource-version>
					</host-route>
					<host-route>
						<host-route-id>8811c5f8-f1ed-4fa0-a505-e1be60396e28</host-route-id>
						<route-prefix>192.168.2.0/25</route-prefix>
						<next-hop>10.102.200.1</next-hop>
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
               <orchestration-status>pending-delete</orchestration-status>
               <dhcp-enabled>true</dhcp-enabled>
	  		   <subnet-name>subnetName</subnet-name>
	  		   <ip-assignment-direction>true</ip-assignment-direction>
			   <host-routes>
					<host-route>
						<host-route-id>400d286b-7e44-4514-b9b3-f70f7360ff32</host-route-id>
						<route-prefix>172.20.1.0/24</route-prefix>
						<next-hop>10.102.200.1</next-hop>
						<resource-version>1505857300987</resource-version>
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
  """<l3-network xmlns="http://org.openecomp.aai.inventory/v9">
   <network-id>49c86598-f766-46f8-84f8-8d1c1b10f9b4</network-id>
   <network-name>MNS-25180-L-01-dmz_direct_net_1</network-name>
   <network-type>CONTRAIL_EXTERNAL</network-type>
   <network-role>dmz_direct</network-role>
   <network-technology>contrail</network-technology>
   <neutron-network-id>c4f4e878-cde0-4b15-ae9a-bda857759cea</neutron-network-id>
   <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
   <network-role-instance>0</network-role-instance>
   <resource-version>l3-version</resource-version>
   <orchestration-status>Active</orchestration-status>
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
         <orchestration-status>Active</orchestration-status>
         <dhcp-enabled>true</dhcp-enabled>
         <resource-version>1505857300987</resource-version>
         <subnet-name>subnetName</subnet-name>
         <ip-assignment-direction>true</ip-assignment-direction>
         <host-routes>
            <host-route>
               <host-route-id>400d286b-7e44-4514-b9b3-f70f7360ff32</host-route-id>
               <route-prefix>172.20.1.0/24</route-prefix>
               <next-hop>10.102.200.1</next-hop>
               <next-hop-type>ip-address</next-hop-type>
               <resource-version>1505857300987</resource-version>
            </host-route>
            <host-route>
               <host-route-id>6f038013-8b15-4eb8-914b-507489fbc8ee</host-route-id>
               <route-prefix>10.102.0.0/16</route-prefix>
               <next-hop>10.102.200.1</next-hop>
               <next-hop-type>ip-address</next-hop-type>
               <resource-version>1505857301151</resource-version>
            </host-route>
            <host-route>
               <host-route-id>8811c5f8-f1ed-4fa0-a505-e1be60396e28</host-route-id>
               <route-prefix>192.168.2.0/25</route-prefix>
               <next-hop>10.102.200.1</next-hop>
               <next-hop-type/>
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
    """<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
                                  xmlns:ns5="http://org.openecomp/mso/request/types/v1"
                                  xmlns:sdncadapter="http://org.openecomp.mso/workflow/sdnc/adapter/schema/v1">
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
"""<sdncadapterworkflow:SDNCAdapterWorkflowResponse xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1" xmlns:tag0="http://org.openecomp.mso/workflow/sdnc/adapter/schema/v1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"> <sdncadapterworkflow:response-data> <tag0:CallbackHeader> <tag0:RequestId>006927ca-f5a3-47fd-880c-dfcbcd81a093</tag0:RequestId> <tag0:ResponseCode>200</tag0:ResponseCode> <tag0:ResponseMessage>OK</tag0:ResponseMessage> </tag0:CallbackHeader> <tag0:RequestData xsi:type="xs:string"><output xmlns="com:att:sdnctl:vnf"><response-code>200</response-code><svc-request-id>006927ca-f5a3-47fd-880c-dfcbcd81a093</svc-request-id><ack-final-indicator>Y</ack-final-indicator><service-information><subscriber-name>notsurewecare</subscriber-name><service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id><service-instance-id>GN_EVPN_direct_net_0_ST_noGW</service-instance-id></service-information><network-information><network-id>8abc633a-810b-4ca5-8b3a-09511d13a2ce</network-id></network-information></output></tag0:RequestData> </sdncadapterworkflow:response-data> </sdncadapterworkflow:SDNCAdapterWorkflowResponse>"""

  String sdncRollbackRequest =
			  """<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
                                  xmlns:ns5="http://org.openecomp/mso/request/types/v1"
                                  xmlns:sdncadapter="http://org.openecomp.mso/workflow/sdnc/adapter/schema/v1">
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
<tag0:CallbackHeader xmlns:tag0="http://org.openecomp.mso/workflow/sdnc/adapter/schema/v1">
   <tag0:RequestId>745b1b50-e39e-4685-9cc8-c71f0bde8bf0</tag0:RequestId>
   <tag0:ResponseCode>200</tag0:ResponseCode>
   <tag0:ResponseMessage>OK</tag0:ResponseMessage>
</tag0:CallbackHeader>
   <tag0:RequestData xmlns:tag0="http://org.openecomp.mso/workflow/sdnc/adapter/schema/v1"
                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xsi:type="xs:string">&lt;output xmlns="com:att:sdnctl:vnf"&gt;&lt;svc-request-id&gt;00703dc8-71ff-442d-a4a8-3adc5beef6a9&lt;/svc-request-id&gt;&lt;response-code&gt;200&lt;/response-code&gt;&lt;ack-final-indicator&gt;Y&lt;/ack-final-indicator&gt;&lt;network-information&gt;&lt;network-id&gt;49c86598-f766-46f8-84f8-8d1c1b10f9b4&lt;/network-id&gt;&lt;/network-information&gt;&lt;service-information&gt;&lt;service-type&gt;a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb&lt;/service-type&gt;&lt;service-instance-id&gt;MNS-25180-L-01-dmz_direct_net_1&lt;/service-instance-id&gt;&lt;subscriber-name&gt;notsurewecare&lt;/subscriber-name&gt;&lt;/service-information&gt;&lt;/output&gt;</tag0:RequestData>
   </sdncadapterworkflow:response-data>
</aetgt:SDNCAdapterWorkflowResponse>"""


	  String sdncAdapterWorkflowResponse_Error =
	  """<sdncadapterworkflow:SDNCAdapterWorkflowResponse xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
                                                 xmlns:tag0="http://org.openecomp.mso/workflow/sdnc/adapter/schema/v1"
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
                                                 xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
                                                 xmlns:tag0="http://org.openecomp.mso/workflow/sdnc/adapter/schema/v1"
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

String rollbackSDNCRequest =
"""<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
                                  xmlns:ns5="http://org.openecomp/mso/request/types/v1"
                                  xmlns:sdncadapter="http://org.openecomp.mso/workflow/sdnc/adapter/schema/v1">
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
         <ecomp-model-information>
            <model-invariant-uuid>invariant-uuid</model-invariant-uuid>
            <model-customization-uuid>customization-uuid</model-customization-uuid>
            <model-uuid>uuid</model-uuid>
            <model-version>version</model-version>
            <model-name>CONTRAIL_EXTERNAL</model-name>
         </ecomp-model-information>
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
"""<NetworkAdapter:rollbackNetwork xmlns:NetworkAdapter="http://org.openecomp.mso/network">
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
			when(mockExecution.getVariable("sdncVersion")).thenReturn("1610")
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("URN_mso_adapters_po_auth")).thenReturn("3141634BF7E070AA289CF2892C986C0B")
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_mso_rollback")).thenReturn("true")
			when(mockExecution.getVariable("disableRollback")).thenReturn("true")

			// preProcessRequest(DelegateExecution execution)
			DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
			DoUpdateNetworkInstance.preProcessRequest(mockExecution)

			verify(mockExecution).getVariable("isDebugLogEnabled")
			verify(mockExecution).setVariable("prefix", Prefix + "")

			//verify variable initialization
			initializeVariables(mockExecution)

			// Authentications
			verify(mockExecution).setVariable("action", "UPDATE")
			verify(mockExecution).setVariable(Prefix + "networkRequest", expectedNetworkRequest)
			verify(mockExecution).setVariable(Prefix + "rollbackEnabled", false)
			verify(mockExecution).setVariable(Prefix + "networkInputs", expectedNetworkInputs)
			verify(mockExecution).setVariable("BasicAuthHeaderValuePO", "Basic cGFzc3dvcmQ=")
			verify(mockExecution).setVariable("BasicAuthHeaderValueSDNC", "Basic cGFzc3dvcmQ=")
			verify(mockExecution).setVariable(Prefix + "networkOutputs", "")

		}

		@Test
		//@Ignore
		public void preProcessRequest_vPERNetworkRequest() {

			def networkModelInfo = """{"modelUuid": "sn5256d1-5a33-55df-13ab-12abad84e111",
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
			when(mockExecution.getVariable("action")).thenReturn("UPDATE")
			when(mockExecution.getVariable("subscriptionServiceType")).thenReturn("MSO-dev-service-type")
			when(mockExecution.getVariable("globalSubscriberId")).thenReturn("globalId_45678905678")
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			
			when(mockExecution.getVariable("URN_mso_adapters_po_auth")).thenReturn("3141634BF7E070AA289CF2892C986C0B")
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")

			when(mockExecution.getVariable("URN_mso_rollback")).thenReturn("true")
			when(mockExecution.getVariable("URN_mso_adapters_sdnc_endpoint")).thenReturn("http://localhost:8090/SDNCAdapter")
			when(mockExecution.getVariable("URN_mso_adapters_network_rest_endpoint")).thenReturn("http://localhost:8090/networks/NetworkAdapter")
			when(mockExecution.getVariable("URN_mso_adapters_sdnc_resource_endpoint")).thenReturn("http://localhost:8090/SDNCAdapterRpc")
			
			// preProcessRequest(DelegateExecution execution)
			DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
			DoUpdateNetworkInstance.preProcessRequest(mockExecution)

			verify(mockExecution).getVariable("isDebugLogEnabled")
			verify(mockExecution).setVariable("prefix", Prefix + "")

			//verify variable initialization
			initializeVariables(mockExecution)

			// Authentications
			verify(mockExecution).setVariable("action", "UPDATE")
			verify(mockExecution).setVariable(Prefix + "networkRequest", expectedVperNetworkRequest)
			verify(mockExecution).setVariable(Prefix + "rollbackEnabled", false)
			verify(mockExecution).setVariable(Prefix + "networkInputs", expectedVperNetworkInputs)
			verify(mockExecution).setVariable("BasicAuthHeaderValuePO", "Basic cGFzc3dvcmQ=")
			verify(mockExecution).setVariable("BasicAuthHeaderValueSDNC", "Basic cGFzc3dvcmQ=")
			verify(mockExecution).setVariable(Prefix + "networkOutputs", "")

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
			when(mockExecution.getVariable("URN_mso_adapters_po_auth")).thenReturn("3141634BF7E070AA289CF2892C986C0B")
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_mso_rollback")).thenReturn("true")
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
			when(mockExecution.getVariable("URN_mso_adapters_po_auth")).thenReturn("3141634BF7E070AA289CF2892C986C0B")
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_mso_rollback")).thenReturn("true")
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
		//@Ignore
		public void prepareUpdateNetworkRequest() {

			println "************ prepareNetworkRequest ************* "
						ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(expectedVperNetworkRequest)
			when(mockExecution.getVariable(Prefix + "requeryIdAAIResponse")).thenReturn(queryIdAIIResponse)
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

			// preProcessRequest(DelegateExecution execution)
			DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
			DoUpdateNetworkInstance.prepareUpdateNetworkRequest(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			// verify set prefix = Prefix + ""
			verify(mockExecution).setVariable("prefix", Prefix + "")

			verify(mockExecution).setVariable(Prefix + "updateNetworkRequest", updateNetworkRequest)

		}


		@Test
		//@Ignore
		public void prepareUpdateNetworkRequest_NoPhysicalname() {

			println "************ prepareNetworkRequest ************* "
			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(NetworkRequest_noPhysicalName)
			when(mockExecution.getVariable(Prefix + "requeryIdAAIResponse")).thenReturn(queryIdAIIResponse)
			when(mockExecution.getVariable(Prefix + "cloudRegionPo")).thenReturn("RDM2WAGPLCP")
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("messageId_generated")
			when(mockExecution.getVariable(Prefix + "source")).thenReturn("VID")
			//when(mockExecution.getVariable(Prefix + "queryVpnBindingAAIResponse")).thenReturn(queryVpnBindingAAIResponse)
			when(mockExecution.getVariable(Prefix + "routeCollection")).thenReturn("<routeTargets>13979:105757</routeTargets><routeTargets>13979:105757</routeTargets>")
			when(mockExecution.getVariable(Prefix + "networkCollection")).thenReturn("<policyFqdns>GN_EVPN_Test</policyFqdns>")
			when(mockExecution.getVariable(Prefix + "tableRefCollection")).thenReturn("")
			when(mockExecution.getVariable(Prefix + "requestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			//when(mockExecution.getVariable("URN_?????")).thenReturn("")   // notificationUrl, //TODO - is this coming from URN? What variable/value to use?
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable(Prefix + "rollbackEnabled")).thenReturn("true")

			// preProcessRequest(DelegateExecution execution)
			DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
			DoUpdateNetworkInstance.prepareUpdateNetworkRequest(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			// verify set prefix = Prefix + ""
			verify(mockExecution).setVariable("prefix", Prefix + "")

			verify(mockExecution).setVariable(Prefix + "updateNetworkRequest", updateNetworkRequest_noPhysicalName)

		}

		@Test
		//@Ignore
		public void prepareSDNCRequest() {

			println "************ prepareSDNCRequest ************* "

			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(expectedNetworkRequest)
			when(mockExecution.getVariable(Prefix + "cloudRegionSdnc")).thenReturn("RDM2WAGPLCP")
			when(mockExecution.getVariable(Prefix + "serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
			when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("testMessageId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("URN_mso_workflow_sdncadapter_callback")).thenReturn("http://localhost:8090/SDNCAdapterCallback")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("GENGSI_siResourceLink")).thenReturn("https://aai-int1.test.com:8443/aai/v8/business/customers/customer/MSO_1610_dev/service-subscriptions/service-subscription/MSO-dev-service-type/service-instances/service-instance/6d4eb22a-82f1-4257-9f80-4176262cfe69/")


			// preProcessRequest(DelegateExecution execution)
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
			when(mockExecution.getVariable("URN_mso_workflow_sdncadapter_callback")).thenReturn("http://localhost:8090/SDNCAdapterCallback")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("GENGSI_siResourceLink")).thenReturn("https://aai-int1.test.com:8443/aai/v8/business/customers/customer/MSO_1610_dev/service-subscriptions/service-subscription/MSO-dev-service-type/service-instances/service-instance/6d4eb22a-82f1-4257-9f80-4176262cfe69/")

			// preProcessRequest(DelegateExecution execution)
			DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
			DoUpdateNetworkInstance.prepareSDNCRollbackRequest(mockExecution)

			// verify set prefix = Prefix + ""
			verify(mockExecution).setVariable("prefix", Prefix + "")
			verify(mockExecution).setVariable(Prefix + "rollbackSDNCRequest", sdncRollbackRequest)

		}

		@Test
		//@Ignore
		public void callRESTQueryAAINetworkId_200() {

			println "************ callRESTQueryAAINetworkId ************* "

			WireMock.reset();
			MockGetNetworkByIdWithDepth("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "UpdateNetworkV2/updateNetwork_queryNetworkId_AAIResponse_Success.xml", "all");

			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(expectedNetworkRequest)
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("URN_aai_endpoint")).thenReturn("http://localhost:8090")
			when(mockExecution.getVariable("URN_mso_workflow_DoUpdateNetworkInstance_aai_l3_network_uri")).thenReturn("/aai/v9/network/l3-networks/l3-network")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("URN_aai_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")
			
			// preProcessRequest(DelegateExecution execution)
			DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
			DoUpdateNetworkInstance.callRESTQueryAAINetworkId(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix + "")
			verify(mockExecution).setVariable(Prefix + "queryIdAAIRequest", "http://localhost:8090/aai/v9/network/l3-networks/l3-network/49c86598-f766-46f8-84f8-8d1c1b10f9b4"+"?depth=all")
			verify(mockExecution).setVariable(Prefix + "aaiIdReturnCode", "200")

		}

		@Test
		//@Ignore
		public void callRESTQueryAAICloudRegion30_200() {

			println "************ callRESTQueryAAICloudRegion30_200 ************* "

			WireMock.reset();
			MockGetNetworkCloudRegion("CreateNetworkV2/cloudRegion30_AAIResponse_Success.xml", "RDM2WAGPLCP");

			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable("prefix")).thenReturn(Prefix + "")
			when(mockExecution.getVariable(Prefix + "networkInputs")).thenReturn(expectedNetworkRequest)
			when(mockExecution.getVariable("URN_aai_endpoint")).thenReturn("http://localhost:8090")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn("8")
			when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_cloud_region_uri")).thenReturn("/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/att-aic")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("URN_aai_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")
			
			// preProcessRequest(DelegateExecution execution)
			DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
			DoUpdateNetworkInstance.callRESTQueryAAICloudRegion(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution, atLeast(1)).setVariable("prefix", Prefix + "")
			verify(mockExecution).setVariable(Prefix + "queryCloudRegionRequest", "http://localhost:8090/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/att-aic/RDM2WAGPLCP")
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
			when(mockExecution.getVariable("prefix")).thenReturn(Prefix + "")
			when(mockExecution.getVariable(Prefix + "networkInputs")).thenReturn(expectedNetworkRequest)
			when(mockExecution.getVariable("URN_aai_endpoint")).thenReturn("http://localhost:8090")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn("8")
			when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_cloud_region_uri")).thenReturn("/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/att-aic")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("URN_aai_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

			// preProcessRequest(DelegateExecution execution)
			DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
			DoUpdateNetworkInstance.callRESTQueryAAICloudRegion(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution, atLeast(1)).setVariable("prefix", Prefix + "")
			verify(mockExecution).setVariable(Prefix + "queryCloudRegionRequest", "http://localhost:8090/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/att-aic/RDM2WAGPLCP")
			verify(mockExecution, atLeast(2)).setVariable(Prefix + "queryCloudRegionReturnCode", "200")
			verify(mockExecution).setVariable(Prefix + "isCloudRegionGood", true)

		}

		@Test
		//@Ignore
		public void callRESTQueryAAICloudRegion_NotFound() {

			println "************ callRESTQueryAAICloudRegionFake ************* "

			WireMock.reset();
			MockGetNetworkCloudRegion_404("MDTWNJ21");

			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable("prefix")).thenReturn(Prefix + "")
			when(mockExecution.getVariable(Prefix + "networkInputs")).thenReturn(vnfRequestFakeRegion)
			when(mockExecution.getVariable("URN_aai_endpoint")).thenReturn("http://localhost:8090")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn("8")
			when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_cloud_region_uri")).thenReturn("/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/att-aic")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("URN_aai_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

			// preProcessRequest(DelegateExecution execution)
			DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
			DoUpdateNetworkInstance.callRESTQueryAAICloudRegion(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution, atLeast(1)).setVariable("prefix", Prefix + "")
			verify(mockExecution).setVariable(Prefix + "queryCloudRegionRequest", "http://localhost:8090/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/att-aic/MDTWNJ21")
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
			MockGetNetworkVpnBinding("UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", "85f015d0-2e32-4c30-96d2-87a1a27f8017");
			MockGetNetworkVpnBinding("UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", "c980a6ef-3b88-49f0-9751-dbad8608d0a6");

			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable(Prefix + "requeryIdAAIResponse")).thenReturn(queryIdAIIResponse) // v6
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("URN_aai_endpoint")).thenReturn("http://localhost:8090")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn("8")
			when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_vpn_binding_uri")).thenReturn("/aai/v8/network/vpn-bindings/vpn-binding")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("URN_aai_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

			// preProcessRequest(DelegateExecution execution)
			DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
			DoUpdateNetworkInstance.callRESTQueryAAINetworkVpnBinding(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix + "")
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
			MockGetNetworkVpnBinding("UpdateNetworkV2/updateNetwork_queryVpnBindingList_AAIResponse_Success.xml", "85f015d0-2e32-4c30-96d2-87a1a27f8017");
			MockGetNetworkVpnBinding("UpdateNetworkV2/updateNetwork_queryVpnBindingList_AAIResponse_Success.xml", "c980a6ef-3b88-49f0-9751-dbad8608d0a6");

			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable(Prefix + "requeryIdAAIResponse")).thenReturn(queryIdAIIResponse) // v6
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("URN_aai_endpoint")).thenReturn("http://localhost:8090")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn("8")
			when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_vpn_binding_uri")).thenReturn("/aai/v8/network/vpn-bindings/vpn-binding")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("URN_aai_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

			// preProcessRequest(DelegateExecution execution)
			DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
			DoUpdateNetworkInstance.callRESTQueryAAINetworkVpnBinding(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix + "")
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
			MockGetNetworkVpnBinding("UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", "85f015d0-2e32-4c30-96d2-87a1a27f8017");

			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable(Prefix + "requeryIdAAIResponse")).thenReturn(queryIdAIIResponseTestScenario01)
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("URN_aai_endpoint")).thenReturn("http://localhost:8090")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn("8")
			when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_vpn_binding_uri")).thenReturn("/aai/v8/network/vpn-bindings/vpn-binding")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("URN_aai_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

			// preProcessRequest(DelegateExecution execution)
			DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
			DoUpdateNetworkInstance.callRESTQueryAAINetworkVpnBinding(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix + "")
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
			MockGetNetworkVpnBinding("UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", "85f015d0-2e32-4c30-96d2-87a1a27f8017");
			MockGetNetworkVpnBinding("UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", "c980a6ef-3b88-49f0-9751-dbad8608d0a6");

			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable(Prefix + "requeryIdAAIResponse")).thenReturn(queryIdAIIResponse)
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("URN_aai_endpoint")).thenReturn("http://localhost:8090")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn("8")
			when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_vpn_binding_uri")).thenReturn("/aai/v8/network/vpn-bindings/vpn-binding")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("URN_aai_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

			// preProcessRequest(DelegateExecution execution)
			DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
			DoUpdateNetworkInstance.callRESTQueryAAINetworkVpnBinding(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix + "")
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
			MockGetNetworkVpnBinding("UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", "85f015d0-2e32-4c30-96d2-87a1a27f8017");

			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables

			when(mockExecution.getVariable(Prefix + "requeryIdAAIResponse")).thenReturn(queryIdAIIResponseVpnNotPresent)
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("URN_aai_endpoint")).thenReturn("http://localhost:8090")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn("8")
			when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_l3_network_uri")).thenReturn("/aai/v8/network/l3-networks/l3-network")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("URN_aai_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

			// preProcessRequest(DelegateExecution execution)
			DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
			DoUpdateNetworkInstance.callRESTQueryAAINetworkVpnBinding(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix + "")
			verify(mockExecution).setVariable(Prefix + "aaiQqueryVpnBindingReturnCode", "200")
			verify(mockExecution).setVariable(Prefix + "vpnCount", 0)
			verify(mockExecution).setVariable(Prefix + "queryVpnBindingAAIResponse", aaiVpnResponseStub)

		}

		@Test
		//@Ignore
		public void callRESTQueryAAINetworkTableRef_200() {

			println "************ callRESTQueryAAINetworkTableRef_200 ************* "

			WireMock.reset();
			MockGetNetworkTableReference("UpdateNetworkV2/updateNetwork_queryNetworkTableRef1_AAIResponse_Success.xml", "refFQDN1");
			MockGetNetworkTableReference("UpdateNetworkV2/updateNetwork_queryNetworkTableRef2_AAIResponse_Success.xml", "refFQDN2");

			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable(Prefix + "requeryIdAAIResponse")).thenReturn(queryIdAIIResponse)
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("URN_aai_endpoint")).thenReturn("http://localhost:8090")
			when(mockExecution.getVariable("URN_mso_workflow_default_aai_network_table_reference_uri")).thenReturn("")
			when(mockExecution.getVariable("URN_mso_workflow_DoUpdateNetworkInstance_aai_route_table_reference_uri")).thenReturn("/aai/v8/network/route-table-references/route-table-reference")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("URN_aai_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

			// preProcessRequest(DelegateExecution execution)
			DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
			DoUpdateNetworkInstance.callRESTQueryAAINetworkTableRef(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix + "")
			verify(mockExecution).setVariable(Prefix + "networkTableRefCount", 2)
			verify(mockExecution).setVariable(Prefix + "networkTableRefUriList", ['/aai/v8/network/route-table-references/route-table-reference/refFQDN1','/aai/v8/network/route-table-references/route-table-reference/refFQDN2'])
			// the last vpnBinding value is saved.
			verify(mockExecution).setVariable(Prefix + "queryNetworkTableRefAAIRequest", "http://localhost:8090/aai/v8/network/route-table-references/route-table-reference/refFQDN1?depth=all")
			verify(mockExecution, atLeast(2)).setVariable(Prefix + "aaiQqueryNetworkTableRefReturnCode", "200")

		}

		@Test
		//@Ignore
		public void callRESTQueryAAINetworkPolicy_200() {

			println "************ callRESTQueryAAINetworkPolicy_200 ************* "

			WireMock.reset();
			MockGetNetworkPolicy("UpdateNetworkV2/updateNetwork_queryNetworkPolicy_AAIResponse_Success.xml", "cee6d136-e378-4678-a024-2cd15f0ee0cg");

			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable(Prefix + "requeryIdAAIResponse")).thenReturn(queryIdAIIResponse)
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("URN_aai_endpoint")).thenReturn("http://localhost:8090")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn("8")
			when(mockExecution.getVariable("URN_mso_workflow_default_aai_v8_network_policy_uri")).thenReturn("/aai/v8/network/network-policies/network-policy")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("URN_aai_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

			// preProcessRequest(DelegateExecution execution)
			DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
			DoUpdateNetworkInstance.callRESTQueryAAINetworkPolicy(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix + "")
			verify(mockExecution).setVariable(Prefix + "networkPolicyCount", 1)
			verify(mockExecution).setVariable(Prefix + "networkPolicyUriList", ['/aai/v8/network/network-policies/network-policy/cee6d136-e378-4678-a024-2cd15f0ee0cg'])
			// the last vpnBinding value is saved.
			verify(mockExecution).setVariable(Prefix + "queryNetworkPolicyAAIRequest", "http://localhost:8090/aai/v8/network/network-policies/network-policy/cee6d136-e378-4678-a024-2cd15f0ee0cg?depth=all")
			verify(mockExecution).setVariable(Prefix + "aaiQqueryNetworkPolicyReturnCode", "200")

		}


		@Test
		//@Ignore
		public void callRESTReQueryAAINetworkId_200() {

			println "************ callRESTReQueryAAINetworkId ************* "

			WireMock.reset();
			MockGetNetworkByIdWithDepth("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "UpdateNetworkV2/updateNetwork_queryNetworkId_AAIResponse_Success.xml", "all");

			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(expectedNetworkRequest)
			when(mockExecution.getVariable(Prefix + "changeAssignSDNCResponse")).thenReturn(sdncAdapterWorkflowFormattedResponse)
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("URN_aai_endpoint")).thenReturn("http://localhost:8090")
			when(mockExecution.getVariable("URN_mso_workflow_DoUpdateNetworkInstance_aai_l3_network_uri")).thenReturn("/aai/v9/network/l3-networks/l3-network")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("URN_aai_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

			// preProcessRequest(DelegateExecution execution)
			DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
			DoUpdateNetworkInstance.callRESTReQueryAAINetworkId(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix + "")
			verify(mockExecution).setVariable(Prefix + "requeryIdAAIRequest", "http://localhost:8090/aai/v9/network/l3-networks/l3-network/49c86598-f766-46f8-84f8-8d1c1b10f9b4"+"?depth=all")
			verify(mockExecution).setVariable(Prefix + "aaiRequeryIdReturnCode", "200")

		}


		@Test
		//@Ignore
		public void callRESTUpdateContrailAAINetworkREST_200() {

			println "************ callRESTUpdateContrailAAINetwork ************* "

			WireMock.reset();
			MockPutNetworkIdWithDepth("UpdateNetworkV2/updateNetwork_updateContrail_AAIResponse_Success.xml", "49c86598-f766-46f8-84f8-8d1c1b10f9b4", "all");
			
			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(expectedNetworkRequest)
			when(mockExecution.getVariable(Prefix + "changeAssignSDNCResponse")).thenReturn(sdncAdapterWorkflowFormattedResponse)
			when(mockExecution.getVariable(Prefix + "requeryIdAAIResponse")).thenReturn(queryIdAIIResponse)
			when(mockExecution.getVariable(Prefix + "updateNetworkResponse")).thenReturn(updateNetworkResponseREST)
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("URN_aai_endpoint")).thenReturn("http://localhost:8090")
			when(mockExecution.getVariable("URN_mso_workflow_DoUpdateNetworkInstance_aai_l3_network_uri")).thenReturn("/aai/v9/network/l3-networks/l3-network")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("URN_aai_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

			// preProcessRequest(DelegateExecution execution)
			DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
			DoUpdateNetworkInstance.callRESTUpdateContrailAAINetwork(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix + "")
			verify(mockExecution).setVariable(Prefix + "updateContrailAAIUrlRequest", "http://localhost:8090/aai/v9/network/l3-networks/l3-network/49c86598-f766-46f8-84f8-8d1c1b10f9b4"+"?depth=all")
			verify(mockExecution).setVariable(Prefix + "updateContrailAAIPayloadRequest", updateContrailAAIPayloadRequest)
			verify(mockExecution).setVariable(Prefix + "aaiUpdateContrailReturnCode", "200")
			//verify(mockExecution).setVariable(Prefix + "updateContrailAAIResponse", updateContrailAAIResponse)
			verify(mockExecution).setVariable(Prefix + "isPONR", true)

		}



		@Test
		//@Ignore
		public void validateUpdateNetworkResponseREST() {

			println "************ validateNetworkResponse ************* "

			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable(Prefix + "updateNetworkResponse")).thenReturn(updateNetworkResponseREST)
			when(mockExecution.getVariable(Prefix + "networkReturnCode")).thenReturn('200')

			DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
			DoUpdateNetworkInstance.validateUpdateNetworkResponse(mockExecution)

			//MockitoDebuggerImpl debugger = new MockitoDebuggerImpl()
			//debugger.printInvocations(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix + "")
			verify(mockExecution).setVariable(Prefix + "updateNetworkResponse", updateNetworkResponseREST)
			verify(mockExecution).setVariable(Prefix + "isNetworkRollbackNeeded", true)
			verify(mockExecution).setVariable(Prefix + "rollbackNetworkRequest", updateRollbackNetworkRequest)

		}

		@Test
		//@Ignore
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
			verify(mockExecution, atLeast(1)).setVariable("WorkflowException", refEq(workflowException, any(WorkflowException.class)))

		}

		@Test
		//@Ignore
		public void validateSDNCResponse() {

			println "************ validateSDNCResponse ************* "

			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable(Prefix + "changeAssignSDNCResponse")).thenReturn(sdncAdapterWorkflowResponse)
			when(mockExecution.getVariable("prefix")).thenReturn(Prefix + "")
			when(mockExecution.getVariable("SDNCA_SuccessIndicator")).thenReturn(true)
			when(mockExecution.getVariable(Prefix + "sdncReturnCode")).thenReturn("200")
			when(mockExecution.getVariable(Prefix + "isResponseGood")).thenReturn(true)

			DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
			try {
			  DoUpdateNetworkInstance.validateSDNCResponse(mockExecution)
			  verify(mockExecution).setVariable(Prefix + "isSdncRollbackNeeded", true)
			  verify(mockExecution).setVariable(Prefix + "rollbackSDNCRequest", "")

			} catch (Exception ex) {
				println " Graceful Exit - " + ex.getMessage()
			}
			//MockitoDebuggerImpl debugger = new MockitoDebuggerImpl()
			//debugger.printInvocations(mockExecution)

			//verify(mockExecution).setVariable(Prefix + "isSdncRollbackNeeded", true)

		}

		@Test
		//@Ignore
		public void validateSDNCResponse_Error() {

			println "************ validateSDNCResponse ************* "
			//ExecutionEntity mockExecution = mock(ExecutionEntity.class)
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
			} catch (Exception ex) {
				println " Graceful Exit! - " + ex.getMessage()
			}
			//MockitoDebuggerImpl debugger = new MockitoDebuggerImpl()
			//debugger.printInvocations(mockExecution)

			// verify set prefix = Prefix + ""
			//verify(mockExecution).setVariable(Prefix + "sdncResponseSuccess", false)

		}

		@Test
		//@Ignore
		public void prepareRollbackData() {

			println "************ prepareRollbackData() ************* "



			WorkflowException workflowException = new WorkflowException("DoUpdateNetworkInstance", 2500, "Received error from Network Adapter: JBWEB000065: HTTP Status 500.")

			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable(Prefix + "rollbackSDNCRequest")).thenReturn(rollbackSDNCRequest)
			when(mockExecution.getVariable(Prefix + "rollbackNetworkRequest")).thenReturn(rollbackNetworkRequest)
			when(mockExecution.getVariable("WorkflowException")).thenReturn(workflowException)

			// preProcessRequest(DelegateExecution execution)
			DoUpdateNetworkInstance DoUpdateNetworkInstance = new DoUpdateNetworkInstance()
			DoUpdateNetworkInstance.prepareRollbackData(mockExecution)

			verify(mockExecution).getVariable("isDebugLogEnabled")
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

			verify(mockExecution, atLeast(3)).getVariable("isDebugLogEnabled")
			verify(mockExecution, atLeast(3)).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable(Prefix + "Success", true)

		}

		private ExecutionEntity setupMock() {

			ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
			when(mockProcessDefinition.getKey()).thenReturn("DoUpdateNetworkInstance")
			RepositoryService mockRepositoryService = mock(RepositoryService.class)
			when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
			when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("DoUpdateNetworkInstance")
			when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
			ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
			when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)

			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables

			when(mockExecution.getId()).thenReturn("100")
			when(mockExecution.getProcessDefinitionId()).thenReturn("DoUpdateNetworkInstance")
			when(mockExecution.getProcessInstanceId()).thenReturn("DoUpdateNetworkInstance")
			when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
			when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)

			return mockExecution
		}


}
