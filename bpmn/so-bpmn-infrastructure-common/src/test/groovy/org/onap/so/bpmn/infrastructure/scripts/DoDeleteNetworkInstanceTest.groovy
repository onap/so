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
import static org.onap.so.bpmn.mock.StubResponseAAI.MockGetNetworkCloudRegion;
import static org.onap.so.bpmn.mock.StubResponseAAI.MockGetNetworkCloudRegion_404;
import static org.onap.so.bpmn.mock.StubResponseNetworkAdapter.MockNetworkAdapter;

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.onap.aai.domain.yang.L3Network
import org.onap.aai.domain.yang.Relationship
import org.onap.aai.domain.yang.RelationshipList
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.core.WorkflowException
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.aaiclient.client.graphinventory.entities.uri.Depth
import org.onap.so.constants.Defaults

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit.WireMockRule


class DoDeleteNetworkInstanceTest  extends  MsoGroovyTest{

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(8090);

		def utils = new MsoUtils()
		String Prefix="DELNWKI_"

		String incomingJsonRequest =
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
  			"instanceName": "HSL_direct_net_2",
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
  }"""

  String expectedDoDeleteNetworkInstanceRequest =
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
  			"instanceName": "HSL_direct_net_2",
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
  }"""

    // expectedVnfRequest
	String expectedNetworkRequest =
"""<network-request xmlns="http://www.w3.org/2001/XMLSchema">
   <request-info>
      <request-id>88f65519-9a38-4c4b-8445-9eb4a5a5af56</request-id>
      <action>DELETE</action>
      <source>VID</source>
      <service-instance-id>f70e927b-6087-4974-9ef8-c5e4d5847ca4</service-instance-id>
   </request-info>
   <network-inputs>
      <network-id>bdc5efe8-404a-409b-85f6-0dcc9eebae30</network-id>
      <network-name>HSL_direct_net_2</network-name>
      <network-type>CONTRAIL_EXTERNAL</network-type>
      <modelCustomizationId>f21df226-8093-48c3-be7e-0408fcda0422</modelCustomizationId>
      <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
      <tenant-id>7dd5365547234ee8937416c65507d266</tenant-id>
      <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
      <backout-on-failure>false</backout-on-failure>
      <sdncVersion>1610</sdncVersion>
   </network-inputs>
   <network-params/>
</network-request>"""

	String expectedVperNetworkRequest =
"""<network-request xmlns="http://www.w3.org/2001/XMLSchema">
   <request-info>
      <request-id>88f65519-9a38-4c4b-8445-9eb4a5a5af56</request-id>
      <action>DELETE</action>
      <source>VID</source>
      <service-instance-id>f70e927b-6087-4974-9ef8-c5e4d5847ca4</service-instance-id>
   </request-info>
   <network-inputs>
      <network-id>bdc5efe8-404a-409b-85f6-0dcc9eebae30</network-id>
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

	String expected_networkInput =
	"""<network-inputs xmlns="http://www.w3.org/2001/XMLSchema">
   <network-id>bdc5efe8-404a-409b-85f6-0dcc9eebae30</network-id>
   <network-name>HSL_direct_net_2</network-name>
   <network-type>CONTRAIL_EXTERNAL</network-type>
   <modelCustomizationId>f21df226-8093-48c3-be7e-0408fcda0422</modelCustomizationId>
   <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
   <tenant-id>7dd5365547234ee8937416c65507d266</tenant-id>
   <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
   <backout-on-failure>false</backout-on-failure>
   <sdncVersion>1610</sdncVersion>
</network-inputs>"""

	String expectedVper_networkInput = 
"""<network-inputs xmlns="http://www.w3.org/2001/XMLSchema">
   <network-id>bdc5efe8-404a-409b-85f6-0dcc9eebae30</network-id>
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

// emptyRegionVnfRequest
String emptyRegionVnfRequest =
"""<network-request xmlns="http://org.onap/so/infra/vnf-request/v1">
   <request-info>
      <request-id>88f65519-9a38-4c4b-8445-9eb4a5a5af56</request-id>
      <action>DELETE</action>
      <source>PORTAL</source>
   </request-info>
   <network-inputs>
      <network-id>bdc5efe8-404a-409b-85f6-0dcc9eebae30</network-id>
      <network-name>HSL_direct_net_2</network-name>
      <network-type>CONTRAIL_BASIC</network-type>
      <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
      <aic-cloud-region/>
      <tenant-id>e81d842d3e8b45c5a59f57cd76af3aaf</tenant-id>
   </network-inputs>
   <network-params>
      <param name="shared">0</param>
   </network-params>
</network-request>"""

String vnfRequestCloudRegionNotFound =
"""<network-request xmlns="http://org.onap/so/infra/vnf-request/v1">
   <request-info>
      <request-id>88f65519-9a38-4c4b-8445-9eb4a5a5af56</request-id>
      <action>DELETE</action>
      <source>PORTAL</source>
   </request-info>
   <network-inputs>
      <network-id>bdc5efe8-404a-409b-85f6-0dcc9eebae30</network-id>
      <network-name>HSL_direct_net_2</network-name>
      <network-type>CONTRAIL_BASIC</network-type>
      <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
      <aic-cloud-region>MDTWNJ21</aic-cloud-region>
      <tenant-id>e81d842d3e8b45c5a59f57cd76af3aaf</tenant-id>
   </network-inputs>
   <network-params>
      <param name="shared">0</param>
   </network-params>
</network-request>"""

		String vnfPayload =
	"""<rest:payload xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"
              contentType="text/xml">
   <network-request xmlns="http://org.onap/so/infra/vnf-request/v1">
      <request-info>
         <request-id>88f65519-9a38-4c4b-8445-9eb4a5a5af56</request-id>
         <action>DELETE</action>
         <source>PORTAL</source>
      </request-info>
      <network-inputs>
         <network-id>bdc5efe8-404a-409b-85f6-0dcc9eebae30</network-id>
         <network-name>HSL_direct_net_2</network-name>
         <network-type>CONTRAIL_BASIC</network-type>
         <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
         <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
         <tenant-id>e81d842d3e8b45c5a59f57cd76af3aaf</tenant-id>
      </network-inputs>
      <network-params>
         <param name="shared">0</param>
      </network-params>
   </network-request>
</rest:payload>"""

	String vnfPayload_MissingId =
"""<rest:payload xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"
              contentType="text/xml">
   <network-request xmlns="http://org.onap/so/infra/vnf-request/v1">
      <request-info>
         <request-id>88f65519-9a38-4c4b-8445-9eb4a5a5af56</request-id>
         <action>DELETE</action>
         <source>PORTAL</source>
      </request-info>
      <network-inputs>
         <network-id>bdc5efe8-404a-409b-85f6-0dcc9eebae30</network-id>
         <network-name/>
         <network-type>CONTRAIL_BASIC</network-type>
         <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
         <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
         <tenant-id>e81d842d3e8b45c5a59f57cd76af3aaf</tenant-id>
      </network-inputs>
      <network-params>
         <param name="shared">0</param>
      </network-params>
   </network-request>
</rest:payload>"""

		String vnfRequestRESTPayload =
"""<network-request xmlns="http://org.onap/so/infra/vnf-request/v1">
      <request-info>
         <request-id>88f65519-9a38-4c4b-8445-9eb4a5a5af56</request-id>
         <action>DELETE</action>
         <source>PORTAL</source>
      </request-info>
      <network-inputs>
         <network-id>bdc5efe8-404a-409b-85f6-0dcc9eebae30</network-id>
         <network-name>HSL_direct_net_2</network-name>
         <network-type>CONTRAIL_BASIC</network-type>
         <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
         <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
         <tenant-id>e81d842d3e8b45c5a59f57cd76af3aaf</tenant-id>
      </network-inputs>
      <network-outputs>
         <network-id>id</network-id>
         <network-name>name</network-name>
      </network-outputs>
      <network-params>
         <param name="shared">0</param>
      </network-params>
  </network-request>"""


String incomingRequestMissingCloudRegion =
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
  			"instanceName": "HSL_direct_net_2",
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
  }"""

	String expectedNetworkRequestMissingId =
  """<network-request xmlns="http://www.w3.org/2001/XMLSchema">
   <request-info>
      <request-id>88f65519-9a38-4c4b-8445-9eb4a5a5af56</request-id>
      <action>DELETE</action>
      <source>VID</source>
      <service-instance-id>f70e927b-6087-4974-9ef8-c5e4d5847ca4</service-instance-id>
   </request-info>
   <network-inputs>
      <network-id/>
      <network-name>HSL_direct_net_2</network-name>
      <network-type>CONTRAIL_EXTERNAL</network-type>
      <modelCustomizationId>f21df226-8093-48c3-be7e-0408fcda0422</modelCustomizationId>
      <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
      <tenant-id>7dd5365547234ee8937416c65507d266</tenant-id>
      <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
      <backout-on-failure>false</backout-on-failure>
      <sdncVersion>1610</sdncVersion>
   </network-inputs>
   <network-params/>
</network-request>"""

String expectedNetworkRequestMissingCloudRegion =
"""<network-request xmlns="http://www.w3.org/2001/XMLSchema">
   <request-info>
      <request-id>88f65519-9a38-4c4b-8445-9eb4a5a5af56</request-id>
      <action>DELETE</action>
      <source>VID</source>
      <service-instance-id>f70e927b-6087-4974-9ef8-c5e4d5847ca4</service-instance-id>
   </request-info>
   <network-inputs>
      <network-id>bdc5efe8-404a-409b-85f6-0dcc9eebae30</network-id>
      <network-name>HSL_direct_net_2</network-name>
      <network-type>CONTRAIL_EXTERNAL</network-type>
      <modelCustomizationId>f21df226-8093-48c3-be7e-0408fcda0422</modelCustomizationId>
      <aic-cloud-region>null</aic-cloud-region>
      <tenant-id>7dd5365547234ee8937416c65507d266</tenant-id>
      <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
      <backout-on-failure>false</backout-on-failure>
      <sdncVersion>1610</sdncVersion>
   </network-inputs>
   <network-params/>
</network-request>"""

		// vnfRESTRequest
		String vnfRESTRequest =
"""<rest:RESTResponse xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"
                   xmlns="http://www.w3.org/2001/XMLSchema"
                   statusCode="200">
   <rest:payload contentType="text/xml">
      <network-request>
         <request-info>
            <action>DELETE</action>
            <source>VID</source>
            <service-instance-id>f70e927b-6087-4974-9ef8-c5e4d5847ca4</service-instance-id>
         </request-info>
         <network-inputs>
            <network-id>bdc5efe8-404a-409b-85f6-0dcc9eebae30</network-id>
            <network-name>HSL_direct_net_2</network-name>
            <network-type>CONTRAIL_EXTERNAL</network-type>
            <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
            <tenant-id>7dd5365547234ee8937416c65507d266</tenant-id>
            <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
            <backout-on-failure>true</backout-on-failure>
         </network-inputs>
         <network-params>
            <userParams/>
         </network-params>
      </network-request>
   </rest:payload>
</rest:RESTResponse>"""

	String networkInputs =
  """<network-inputs xmlns="http://org.onap/so/infra/vnf-request/v1">
   <network-id>bdc5efe8-404a-409b-85f6-0dcc9eebae30</network-id>
   <network-name>HSL_direct_net_2</network-name>
   <network-type>CONTRAIL_BASIC</network-type>
   <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
   <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
   <tenant-id>e81d842d3e8b45c5a59f57cd76af3aaf</tenant-id>
</network-inputs>"""

String networkInputsNoType =
"""<network-inputs xmlns="http://org.onap/so/infra/vnf-request/v1">
   <network-id></network-id>
   <network-name></network-name>
   <network-type></network-type>
   <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
   <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
   <tenant-id>e81d842d3e8b45c5a59f57cd76af3aaf</tenant-id>
</network-inputs>"""

	String networkInputsMissingId =
 """<network-inputs xmlns="http://www.w3.org/2001/XMLSchema">
   <network-id/>
   <network-name>HSL_direct_net_2</network-name>
   <network-type>CONTRAIL_EXTERNAL</network-type>
   <modelCustomizationId>f21df226-8093-48c3-be7e-0408fcda0422</modelCustomizationId>
   <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
   <tenant-id>7dd5365547234ee8937416c65507d266</tenant-id>
   <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
   <backout-on-failure>false</backout-on-failure>
   <sdncVersion>1610</sdncVersion>
</network-inputs>"""

String networkInputsMissingCloudRegion =
"""<network-inputs xmlns="http://www.w3.org/2001/XMLSchema">
   <network-id>bdc5efe8-404a-409b-85f6-0dcc9eebae30</network-id>
   <network-name>HSL_direct_net_2</network-name>
   <network-type>CONTRAIL_EXTERNAL</network-type>
   <modelCustomizationId>f21df226-8093-48c3-be7e-0408fcda0422</modelCustomizationId>
   <aic-cloud-region>null</aic-cloud-region>
   <tenant-id>7dd5365547234ee8937416c65507d266</tenant-id>
   <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
   <backout-on-failure>false</backout-on-failure>
   <sdncVersion>1610</sdncVersion>
</network-inputs>"""

	String MissingIdFault = "Invalid value or missing network-id element"
	String MissingRegionFault = "Invalid value or missing 'aic-cloud-region' element"

	String  invalidWorkflowException = """<aetgt:WorkflowException xmlns:aetgt="http://org.onap/so/workflow/schema/v1">
					<aetgt:ErrorMessage>Invalid value of network-id element</aetgt:ErrorMessage>
					<aetgt:ErrorCode>2500</aetgt:ErrorCode>
				  </aetgt:WorkflowException>"""


	String queryAAIResponse =
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

    String deleteNetworkRequest =
    """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
  <soapenv:Header/>
  <soapenv:Body>
      <NetworkAdapter:deleteNetwork xmlns:NetworkAdapter="http://org.onap.so/network">
         <cloudSiteId>RDM2WAGPLCP</cloudSiteId>
         <tenantId>e81d842d3e8b45c5a59f57cd76af3aaf</tenantId>
         <networkType>CONTRAIL_BASIC</networkType>
         <networkId>HSL_direct_net_2/57594a56-1c92-4a38-9caa-641c1fa3d4b6</networkId>
         <request>
            <requestId>88f65519-9a38-4c4b-8445-9eb4a5a5af56</requestId>
            <serviceInstanceId>0</serviceInstanceId>
         </request>
      </NetworkAdapter:deleteNetwork>
  </soapenv:Body>
</soapenv:Envelope>"""

String deleteNetworkRESTRequest =
"""<deleteNetworkRequest>
   <cloudSiteId>RDM2WAGPLCP</cloudSiteId>
   <tenantId>e81d842d3e8b45c5a59f57cd76af3aaf</tenantId>
   <networkId>bdc5efe8-404a-409b-85f6-0dcc9eebae30</networkId>
   <networkStackId>HSL_direct_net_2/57594a56-1c92-4a38-9caa-641c1fa3d4b6</networkStackId>
   <networkType>CONTRAIL_BASIC</networkType>
   <modelCustomizationUuid>sn5256d1-5a33-55df-13ab-12abad84e222</modelCustomizationUuid>
   <skipAAI>true</skipAAI>
   <msoRequest>
      <requestId>88f65519-9a38-4c4b-8445-9eb4a5a5af56</requestId>
      <serviceInstanceId>f70e927b-6087-4974-9ef8-c5e4d5847ca4</serviceInstanceId>
   </msoRequest>
   <messageId>messageId_value</messageId>
   <notificationUrl/>
</deleteNetworkRequest>"""

String deleteNetworkRESTRequestAlaCarte =
"""<deleteNetworkRequest>
   <cloudSiteId>RDM2WAGPLCP</cloudSiteId>
   <tenantId>e81d842d3e8b45c5a59f57cd76af3aaf</tenantId>
   <networkId>bdc5efe8-404a-409b-85f6-0dcc9eebae30</networkId>
   <networkStackId>HSL_direct_net_2/57594a56-1c92-4a38-9caa-641c1fa3d4b6</networkStackId>
   <networkType>CONTRAIL_BASIC</networkType>
   <modelCustomizationUuid>f21df226-8093-48c3-be7e-0408fcda0422</modelCustomizationUuid>
   <skipAAI>true</skipAAI>
   <msoRequest>
      <requestId>88f65519-9a38-4c4b-8445-9eb4a5a5af56</requestId>
      <serviceInstanceId>f70e927b-6087-4974-9ef8-c5e4d5847ca4</serviceInstanceId>
   </msoRequest>
   <messageId>messageId_value</messageId>
   <notificationUrl/>
</deleteNetworkRequest>"""

    String deleteNetworkResponse_noRollback =
"""<ns2:deleteNetworkResponse xmlns:ns2="http://org.onap.so/network">
	<networkDeleted>true</networkDeleted>
</ns2:deleteNetworkResponse>
"""

	String deleteNetworkResponse =
	"""<ns2:deleteNetworkResponse xmlns:ns2="http://org.onap.so/network">
	    <networkDeleted>true</networkDeleted>
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
       </ns2:deleteNetworkResponse>"""

	   String deleteRollbackNetworkRequest =
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

	String deleteNetworkResponseFalseCompletion =
		"""<ns2:deleteNetworkResponse xmlns:ns2="http://org.onap.so/network"
		     xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
		   <networkDeleted>false</networkDeleted>
		</ns2:deleteNetworkResponse>"""

	String deleteNetworkErrorResponse =
	"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<deleteNetworkError>
<messageId>680bd458-5ec1-4a16-b77c-509022e53450</messageId><category>INTERNAL</category>
<message>400 Bad Request: The server could not comply with the request since it is either malformed or otherwise incorrect., error.type=StackValidationFailed, error.message=Property error: : resources.network.properties: : Unknown Property network_ipam_refs_data</message>
<rolledBack>true</rolledBack>
</deleteNetworkError>
"""

	String deleteNetworkWorkflowException =
	"""<aetgt:WorkflowException xmlns:aetgt="http://org.onap/so/workflow/schema/v1">
					<aetgt:ErrorMessage>Received error from Network Adapter: 400 Bad Request: The server could not comply with the request since it is either malformed or otherwise incorrect., error.type=StackValidationFailed, error.message=Property error: : resources.network.properties: : Unknown Property network_ipam_refs_data</aetgt:ErrorMessage>
					<aetgt:ErrorCode>7020</aetgt:ErrorCode>
				  </aetgt:WorkflowException>"""

String aaiWorkflowException =
"""<aetgt:WorkflowException xmlns:aetgt="http://org.onap/so/workflow/schema/v1">
					<aetgt:ErrorMessage>Bpmn error encountered in DoDeleteNetworkInstance flow. Unexpected Response from AAI Adapter - org.apache.http.conn.HttpHostConnectException: Connect to localhost:8090 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: connect</aetgt:ErrorMessage>
					<aetgt:ErrorCode>2500</aetgt:ErrorCode>
				  </aetgt:WorkflowException>"""

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
      <l3-network xmlns="http://org.openecomp.aai.inventory/v8">
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
               <related-link>https://aai-app-e2e.test.com:8443/aai/v8/cloud-infrastructure/tenants/tenant/e81d842d3e8b45c5a59f57cd76af3aaf/</related-link>
               <relationship-data>
                  <relationship-key>tenant.tenant-id</relationship-key>
                  <relationship-value>e81d842d3e8b45c5a59f57cd76af3aaf</relationship-value>
               </relationship-data>
            </relationship>
			<relationship>
               <related-to>cloud-region</related-to>
               <related-link>cloud-infrastructure/cloud-regions/cloud-region/CloudOwner/RDM2WAGPLCP/</related-link>
               <relationship-data>
                  <relationship-key>cloud-region.cloud-owner</relationship-key>
                  <relationship-value>CloudOwner</relationship-value>
               </relationship-data>
               <relationship-data>
                  <relationship-key>cloud-region.cloud-region-id</relationship-key>
                  <relationship-value>RDM2WAGPLCP</relationship-value>
               </relationship-data>						   
            </relationship>			            
         </relationship-list>
      </l3-network>
   </rest:payload>
</rest:RESTResponse>"""

String aaiResponseWithRelationship =
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
      <l3-network xmlns="http://org.openecomp.aai.inventory/v8">
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
               <related-link>https://aai-app-e2e.test.com:8443/aai/v8/cloud-infrastructure/tenants/tenant/e81d842d3e8b45c5a59f57cd76af3aaf/</related-link>
               <relationship-data>
                  <relationship-key>tenant.tenant-id</relationship-key>
                  <relationship-value>e81d842d3e8b45c5a59f57cd76af3aaf</relationship-value>
               </relationship-data>
            </relationship>
			<relationship>
				<related-to>vf-module</related-to>
				<related-link>https://aai-app-e2e.ecomp.cci.com:8443/aai/v8/network/generic-vnfs/generic-vnf/105df7e5-0b3b-49f7-a837-4864b62827c4/vf-modules/vf-module/d9217058-95a0-49ee-b9a9-949259e89349/</related-link>
				<relationship-data>
				   <relationship-key>generic-vnf.vnf-id</relationship-key>
				   <relationship-value>105df7e5-0b3b-49f7-a837-4864b62827c4</relationship-value>
			    </relationship-data>
			    <relationship-data>
				   <relationship-key>vf-module.vf-module-id</relationship-key>
				   <relationship-value>d9217058-95a0-49ee-b9a9-949259e89349</relationship-value>
			    </relationship-data>
		    </relationship>            
			<relationship>
       		   <related-to>generic-vnf</related-to>
               <related-link>https://aai-app-e2e.test.com:8443/aai/v8/network/generic-vnfs/generic-vnf/45f822d9-73ca-4255-9844-7cef401bbf47/</related-link>
               <relationship-data>
                 <relationship-key>generic-vnf.vnf-id</relationship-key>
                 <relationship-value>45f822d9-73ca-4255-9844-7cef401bbf47</relationship-value>
               </relationship-data>
               <related-to-property>
                 <property-key>generic-vnf.vnf-name</property-key>
                 <property-value>zrdm1scpx05</property-value>
               </related-to-property>
            </relationship>
         </relationship-list>
      </l3-network>
   </rest:payload>
</rest:RESTResponse>"""

	String deleteSDNCRequest =
    """<aetgt:SDNCAdapterWorkflowRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                  xmlns:ns5="http://org.onap/so/request/types/v1"
                                  xmlns:sdncadapter="http://org.onap.so/workflow/sdnc/adapter/schema/v1">
   <sdncadapter:RequestHeader>
      <sdncadapter:RequestId>88f65519-9a38-4c4b-8445-9eb4a5a5af56</sdncadapter:RequestId>
      <sdncadapter:SvcInstanceId>f70e927b-6087-4974-9ef8-c5e4d5847ca4</sdncadapter:SvcInstanceId>
      <sdncadapter:SvcAction>delete</sdncadapter:SvcAction>
      <sdncadapter:SvcOperation>network-topology-operation</sdncadapter:SvcOperation>
      <sdncadapter:CallbackUrl>sdncCallback</sdncadapter:CallbackUrl>
   </sdncadapter:RequestHeader>
   <aetgt:SDNCRequestData>
      <request-information>
         <request-id>88f65519-9a38-4c4b-8445-9eb4a5a5af56</request-id>
         <request-action>DisconnectNetworkRequest</request-action>
         <source>VID</source>
         <notification-url/>
         <order-number/>
         <order-version/>
      </request-information>
      <service-information>
         <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
         <service-type/>
         <service-instance-id>f70e927b-6087-4974-9ef8-c5e4d5847ca4</service-instance-id>
         <subscriber-name/>
      </service-information>
      <network-request-information>
         <network-id>bdc5efe8-404a-409b-85f6-0dcc9eebae30</network-id>
         <network-type>CONTRAIL_BASIC</network-type>
         <network-name>HSL_direct_net_2</network-name>
         <tenant>7dd5365547234ee8937416c65507d266</tenant>
         <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>
      </network-request-information>
   </aetgt:SDNCRequestData>
</aetgt:SDNCAdapterWorkflowRequest>"""

	String deleteRpcSDNCRequest =
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
         <subscriber-name/>
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

	String sdncAdapaterDeactivateRollback =
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
         <subscriber-name/>
      </service-information>
      <network-information>
         <network-id>bdc5efe8-404a-409b-85f6-0dcc9eebae30</network-id>
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
      <tag0:RequestData xmlns:tag0="http://org.onap.so/workflow/sdnc/adapter/schema/v1">&lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;output xmlns="com:att:sdnctl:vnf"&gt;&lt;svc-request-id&gt;19174929-3809-49ca-89eb-17f84a035389&lt;/svc-request-id&gt;&lt;response-code&gt;200&lt;/response-code&gt;&lt;ack-final-indicator&gt;Y&lt;/ack-final-indicator&gt;&lt;network-information&gt;&lt;network-id&gt;bdc5efe8-404a-409b-85f6-0dcc9eebae30&lt;/network-id&gt;&lt;/network-information&gt;&lt;service-information&gt;&lt;service-type&gt;a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb&lt;/service-type&gt;&lt;service-instance-id&gt;HSL_direct_net_2&lt;/service-instance-id&gt;&lt;subscriber-name&gt;notsurewecare&lt;/subscriber-name&gt;&lt;/service-information&gt;&lt;/output&gt;</tag0:RequestData>
     </sdncadapterworkflow:response-data>
   </aetgt:SDNCAdapterWorkflowResponse>"""

   String sdncAdapterWorkflowResponse_404 =
   """<aetgt:SDNCAdapterWorkflowResponse xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                   xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1">
   <sdncadapterworkflow:response-data>&lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;output xmlns="com:att:sdnctl:vnf"&gt;&lt;svc-request-id&gt;00703dc8-71ff-442d-a4a8-3adc5beef6a9&lt;/svc-request-id&gt;&lt;response-code&gt;404&lt;/response-code&gt;&lt;response-message&gt;Service instance not found in config tree&lt;/response-message&gt;&lt;ack-final-indicator&gt;Y&lt;/ack-final-indicator&gt;&lt;network-information&gt;&lt;network-id&gt;49c86598-f766-46f8-84f8-8d1c1b10f9b4&lt;/network-id&gt;&lt;/network-information&gt;&lt;service-information&gt;&lt;service-type&gt;a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb&lt;/service-type&gt;&lt;service-instance-id&gt;MNS-25180-L-01-dmz_direct_net_1&lt;/service-instance-id&gt;&lt;subscriber-name&gt;notsurewecare&lt;/subscriber-name&gt;&lt;/service-information&gt;&lt;/output&gt;</sdncadapterworkflow:response-data>
</aetgt:SDNCAdapterWorkflowResponse>"""

	String expected_sdncAdapterWorkflowFormattedResponse_404 =
"""<aetgt:SDNCAdapterWorkflowResponse xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                   xmlns="com:att:sdnctl:vnf">
   <aetgt:response-data>
      <output>
         <svc-request-id>00703dc8-71ff-442d-a4a8-3adc5beef6a9</svc-request-id>
         <response-code>404</response-code>
         <response-message>Service instance not found in config tree</response-message>
         <ack-final-indicator>Y</ack-final-indicator>
         <network-information>
            <network-id>49c86598-f766-46f8-84f8-8d1c1b10f9b4</network-id>
         </network-information>
         <service-information>
            <service-type>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-type>
            <service-instance-id>MNS-25180-L-01-dmz_direct_net_1</service-instance-id>
            <subscriber-name>notsurewecare</subscriber-name>
         </service-information>
      </output>
   </aetgt:response-data>
</aetgt:SDNCAdapterWorkflowResponse>"""

	String sdncAdapterWorkflowFormattedResponse =
	"""<aetgt:SDNCAdapterWorkflowResponse xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                   xmlns="com:att:sdnctl:vnf">
   <aetgt:response-data>
      <output>
         <svc-request-id>19174929-3809-49ca-89eb-17f84a035389</svc-request-id>
         <response-code>200</response-code>
         <ack-final-indicator>Y</ack-final-indicator>
         <network-information>
            <network-id>bdc5efe8-404a-409b-85f6-0dcc9eebae30</network-id>
         </network-information>
         <service-information>
            <service-type>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-type>
            <service-instance-id>HSL_direct_net_2</service-instance-id>
            <subscriber-name>notsurewecare</subscriber-name>
         </service-information>
      </output>
   </aetgt:response-data>
</aetgt:SDNCAdapterWorkflowResponse>"""

String sdncAdapterWorkflowFormattedResponse_404 =
"""<aetgt:SDNCAdapterWorkflowResponse xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                   xmlns="com:att:sdnctl:vnf">
   <aetgt:response-data>
      <output>
         <svc-request-id>00703dc8-71ff-442d-a4a8-3adc5beef6a9</svc-request-id>
         <response-code>404</response-code>
         <response-message>Service instance not found in config tree</response-message>
         <ack-final-indicator>Y</ack-final-indicator>
         <network-information>
            <network-id>49c86598-f766-46f8-84f8-8d1c1b10f9b4</network-id>
         </network-information>
         <service-information>
            <service-type>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-type>
            <service-instance-id>MNS-25180-L-01-dmz_direct_net_1</service-instance-id>
            <subscriber-name>notsurewecare</subscriber-name>
         </service-information>
      </output>
   </aetgt:response-data>
</aetgt:SDNCAdapterWorkflowResponse>"""

	String invalidRequest = "Invalid value of network-id element"



	String sndcWorkflowException =
	"""<aetgt:WorkflowException xmlns:aetgt="http://org.onap/so/workflow/schema/v1">
   <aetgt:ErrorMessage>Received error from SDN-C: No availability zone available</aetgt:ErrorMessage>
   <aetgt:ErrorCode>5300</aetgt:ErrorCode>
   <aetgt:SourceSystemErrorCode>200</aetgt:SourceSystemErrorCode>
</aetgt:WorkflowException>"""

	String sndcWorkflowErrorResponse =
	"""<aetgt:WorkflowException xmlns:aetgt="http://org.onap/so/workflow/schema/v1">
					<aetgt:ErrorMessage>Received error from SDN-C: <aetgt:SDNCAdapterWorkflowResponse xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                                   xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1">
   <sdncadapterworkflow:response-data>&lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;output xmlns="com:att:sdnctl:vnf"&gt;&lt;svc-request-id&gt;00703dc8-71ff-442d-a4a8-3adc5beef6a9&lt;/svc-request-id&gt;&lt;response-code&gt;404&lt;/response-code&gt;&lt;response-message&gt;Service instance not found in config tree&lt;/response-message&gt;&lt;ack-final-indicator&gt;Y&lt;/ack-final-indicator&gt;&lt;network-information&gt;&lt;network-id&gt;49c86598-f766-46f8-84f8-8d1c1b10f9b4&lt;/network-id&gt;&lt;/network-information&gt;&lt;service-information&gt;&lt;service-type&gt;a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb&lt;/service-type&gt;&lt;service-instance-id&gt;MNS-25180-L-01-dmz_direct_net_1&lt;/service-instance-id&gt;&lt;subscriber-name&gt;notsurewecare&lt;/subscriber-name&gt;&lt;/service-information&gt;&lt;/output&gt;</sdncadapterworkflow:response-data>
</aetgt:SDNCAdapterWorkflowResponse></aetgt:ErrorMessage>
					<aetgt:ErrorCode>5300</aetgt:ErrorCode>
				  </aetgt:WorkflowException>"""

	String unexpectedErrorEncountered =
	"""<aetgt:WorkflowException xmlns:aetgt="http://org.onap/so/workflow/schema/v1">
					<aetgt:ErrorMessage>bpel error deleting network</aetgt:ErrorMessage>
					<aetgt:ErrorCode>5300</aetgt:ErrorCode>
				  </aetgt:WorkflowException>"""


				  // expectedVnfRequest
  String inputViprSDC_NetworkRequest =
			  """<network-request xmlns="http://www.w3.org/2001/XMLSchema">
   <request-info>
      <request-id>88f65519-9a38-4c4b-8445-9eb4a5a5af56</request-id>
      <action>DELETE</action>
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
         <modelType>CONTRAIL_EXTERNAL</modelType>
      </networkModelInfo>
      <serviceModelInfo>
         <modelName>HNGW Protected OAM</modelName>
         <modelUuid>36a3a8ea-49a6-4ac8-b06c-89a54544b9b6</modelUuid>
         <modelInvariantUuid>fcc85cb0-ad74-45d7-a5a1-17c8744fdb71</modelInvariantUuid>
         <modelVersion>1.0</modelVersion>
         <modelCustomizationUuid/>
         <modelType>service</modelType>
      </serviceModelInfo>
      <sdncVersion>1702</sdncVersion>
   </network-inputs>
   <network-params/>
</network-request>"""
// - - - - - - - -


	    @Before
		public void init()
		{
			super.init("DoDeleteNetworkInstance")
			MockitoAnnotations.initMocks(this)
		}

		@Test
		public void preProcessRequest_Json() {
			
			println "************ preProcessRequest_Payload ************* "
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("requestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("isBaseVfModule")).thenReturn(true)
			when(mockExecution.getVariable("recipeTimeout")).thenReturn(0)
			when(mockExecution.getVariable("requestAction")).thenReturn("DELETE")
			when(mockExecution.getVariable("networkId")).thenReturn("bdc5efe8-404a-409b-85f6-0dcc9eebae30")
			when(mockExecution.getVariable("serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
			when(mockExecution.getVariable("vnfId")).thenReturn("")
			when(mockExecution.getVariable("volumeGroupId")).thenReturn("")
			//when(mockExecution.getVariable("networkId")).thenReturn("")
			when(mockExecution.getVariable("serviceType")).thenReturn("MOG")
			when(mockExecution.getVariable("networkType")).thenReturn("modelName")
			when(mockExecution.getVariable("bpmnRequest")).thenReturn(incomingJsonRequest)
			when(mockExecution.getVariable("disableRollback")).thenReturn(true)
			when(mockExecution.getVariable("testMessageId")).thenReturn("7df689f9-7b93-430b-9b9e-28140d70cc7ad")
			when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("mso-service-instance-id")).thenReturn("FH/VLXM/003717//SW_INTERNET")
			when(mockExecution.getVariable("mso.adapters.po.auth")).thenReturn("3141634BF7E070AA289CF2892C986C0B")
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("mso.rollback")).thenReturn("true")
			when(mockExecution.getVariable("sdncVersion")).thenReturn("1610")

			DoDeleteNetworkInstance DoDeleteNetworkInstance = new DoDeleteNetworkInstance()
			DoDeleteNetworkInstance.preProcessRequest(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

//			verify(mockExecution).getVariable("isDebugLogEnabled")
			verify(mockExecution).setVariable("prefix", Prefix)

			//verify variable initialization
			initializeVariables(mockExecution)

			verify(mockExecution).setVariable("action", "DELETE")
			verify(mockExecution).setVariable(Prefix + "networkRequest", expectedNetworkRequest)

			verify(mockExecution).setVariable(Prefix + "rollbackEnabled", false)

			verify(mockExecution).setVariable(Prefix + "networkInputs", expected_networkInput)
			verify(mockExecution).setVariable(Prefix + "messageId", "7df689f9-7b93-430b-9b9e-28140d70cc7ad")
			verify(mockExecution).setVariable(Prefix + "source", "VID")

			// Authentications
			verify(mockExecution).setVariable("BasicAuthHeaderValuePO", "Basic cGFzc3dvcmQ=")
			verify(mockExecution).setVariable("BasicAuthHeaderValueSDNC", "Basic cGFzc3dvcmQ=")


		}

		@Test
		//@Ignore
		public void preProcessRequest_vPER() {

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
			when(mockExecution.getVariable("testMessageId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			// Inputs:
			when(mockExecution.getVariable("msoRequestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("disableRollback")).thenReturn("true")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
			when(mockExecution.getVariable("networkId")).thenReturn("bdc5efe8-404a-409b-85f6-0dcc9eebae30")                                // optional
			when(mockExecution.getVariable("networkName")).thenReturn("MNS-25180-L-01-dmz_direct_net_1")        // optional
			when(mockExecution.getVariable("productFamilyId")).thenReturn("a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb")
			when(mockExecution.getVariable("networkModelInfo")).thenReturn("CONTRAIL_EXTERNAL")
			when(mockExecution.getVariable("lcpCloudRegionId")).thenReturn("RDM2WAGPLCP")
			when(mockExecution.getVariable("tenantId")).thenReturn("7dd5365547234ee8937416c65507d266")
			when(mockExecution.getVariable("failIfExists")).thenReturn("false")
			when(mockExecution.getVariable("networkModelInfo")).thenReturn(networkModelInfo)
			when(mockExecution.getVariable("sdncVersion")).thenReturn("1702")
			when(mockExecution.getVariable("action")).thenReturn("DELETE")
			when(mockExecution.getVariable("subscriptionServiceType")).thenReturn("MSO-dev-service-type")
			when(mockExecution.getVariable("globalSubscriberId")).thenReturn("globalId_45678905678")
			when(mockExecution.getVariable("testMessageId")).thenReturn("7df689f9-7b93-430b-9b9e-28140d70cc7ad")

			when(mockExecution.getVariable("mso.adapters.po.auth")).thenReturn("3141634BF7E070AA289CF2892C986C0B")
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")

			DoDeleteNetworkInstance DoDeleteNetworkInstance = new DoDeleteNetworkInstance()
			DoDeleteNetworkInstance.preProcessRequest(mockExecution)

			// check the sequence of variable invocation
			//MockitoDebuggerImpl preDebugger = new MockitoDebuggerImpl()
			//preDebugger.printInvocations(mockExecution)

//			verify(mockExecution).getVariable("isDebugLogEnabled")
			verify(mockExecution).setVariable("prefix", Prefix)

			//verify variable initialization
			initializeVariables(mockExecution)

			verify(mockExecution).setVariable("action", "DELETE")
			verify(mockExecution).setVariable(Prefix + "networkRequest", expectedVperNetworkRequest)

			verify(mockExecution).setVariable(Prefix + "rollbackEnabled", false)

			verify(mockExecution).setVariable(Prefix + "networkInputs", expectedVper_networkInput)
			verify(mockExecution).setVariable(Prefix + "messageId", "7df689f9-7b93-430b-9b9e-28140d70cc7ad")
			verify(mockExecution).setVariable(Prefix + "source", "VID")

			// Authentications
			verify(mockExecution).setVariable("BasicAuthHeaderValuePO", "Basic cGFzc3dvcmQ=")
			verify(mockExecution).setVariable("BasicAuthHeaderValueSDNC", "Basic cGFzc3dvcmQ=")


		}
		
		public void initializeVariables (DelegateExecution mockExecution) {

			verify(mockExecution).setVariable(Prefix + "networkRequest", "")
			verify(mockExecution).setVariable(Prefix + "isSilentSuccess", false)
			verify(mockExecution).setVariable(Prefix + "Success", false)

			verify(mockExecution).setVariable(Prefix + "requestId", "")
			verify(mockExecution).setVariable(Prefix + "source", "")
			verify(mockExecution).setVariable(Prefix + "lcpCloudRegion", "")
			verify(mockExecution).setVariable(Prefix + "networkInputs", "")
			verify(mockExecution).setVariable(Prefix + "tenantId", "")

			verify(mockExecution).setVariable(Prefix + "queryAAIResponse", "")
			verify(mockExecution).setVariable(Prefix + "aaiReturnCode", "")
			verify(mockExecution).setVariable(Prefix + "isAAIGood", false)
			verify(mockExecution).setVariable(Prefix + "isVfRelationshipExist", false)

			// AAI query Cloud Region
			verify(mockExecution).setVariable(Prefix + "queryCloudRegionRequest","")
			verify(mockExecution).setVariable(Prefix + "queryCloudRegionReturnCode","")
			verify(mockExecution).setVariable(Prefix + "queryCloudRegionResponse","")
			verify(mockExecution).setVariable(Prefix + "cloudRegionPo","")
			verify(mockExecution).setVariable(Prefix + "cloudRegionSdnc","")

			verify(mockExecution).setVariable(Prefix + "deleteNetworkRequest", "")
			verify(mockExecution).setVariable(Prefix + "deleteNetworkResponse", "")
			verify(mockExecution).setVariable(Prefix + "networkReturnCode", "")
			verify(mockExecution).setVariable(Prefix + "rollbackNetworkRequest", "")

			verify(mockExecution).setVariable(Prefix + "deleteSDNCRequest", "")
			verify(mockExecution).setVariable(Prefix + "deleteSDNCResponse", "")
			verify(mockExecution).setVariable(Prefix + "sdncReturnCode", "")
			verify(mockExecution).setVariable(Prefix + "sdncResponseSuccess", false)

			verify(mockExecution).setVariable(Prefix + "deactivateSDNCRequest", "")
			verify(mockExecution).setVariable(Prefix + "deactivateSDNCResponse", "")
			verify(mockExecution).setVariable(Prefix + "deactivateSdncReturnCode", "")
			verify(mockExecution).setVariable(Prefix + "isSdncDeactivateRollbackNeeded", "")

			verify(mockExecution).setVariable(Prefix + "rollbackDeactivateSDNCRequest", "")
			verify(mockExecution).setVariable(Prefix + "isException", false)

		}

		@Test
		//@Ignore
		public void preProcessRequest_Json_MissingId() {

			println "************ preProcessRequest_MissingId() ************* "
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("requestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("isBaseVfModule")).thenReturn(true)
			when(mockExecution.getVariable("recipeTimeout")).thenReturn(0)
			when(mockExecution.getVariable("requestAction")).thenReturn("DELETE")
			//when(mockExecution.getVariable("networkId")).thenReturn("") // missing Id
			when(mockExecution.getVariable("serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
			when(mockExecution.getVariable("vnfId")).thenReturn("")
			when(mockExecution.getVariable("volumeGroupId")).thenReturn("")
			//when(mockExecution.getVariable("networkId")).thenReturn("")
			when(mockExecution.getVariable("serviceType")).thenReturn("MOG")
			when(mockExecution.getVariable("networkType")).thenReturn("modelName")
			when(mockExecution.getVariable("bpmnRequest")).thenReturn(incomingJsonRequest)
			when(mockExecution.getVariable("disableRollback")).thenReturn(true)
			
			when(mockExecution.getVariable("testMessageId")).thenReturn("7df689f9-7b93-430b-9b9e-28140d70cc7ad")
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("mso-service-instance-id")).thenReturn("FH/VLXM/003717//SW_INTERNET")
			when(mockExecution.getVariable("mso.adapters.po.auth")).thenReturn("3141634BF7E070AA289CF2892C986C0B")
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("mso.rollback")).thenReturn("true")
			when(mockExecution.getVariable("sdncVersion")).thenReturn("1610")

			// preProcessRequest(DelegateExecution execution)
			DoDeleteNetworkInstance DoDeleteNetworkInstance = new DoDeleteNetworkInstance()
			try {
				DoDeleteNetworkInstance.preProcessRequest(mockExecution)
			} catch (Exception ex) {
				println " Test End - Handle catch-throw BpmnError()! "
			}

			//verify(mockExecution).getVariable("isDebugLogEnabled")
			verify(mockExecution).setVariable("prefix", Prefix)

			//verify variable initialization
			initializeVariables(mockExecution)

			verify(mockExecution).setVariable("action", "DELETE")
			verify(mockExecution).setVariable(Prefix + "networkRequest", expectedNetworkRequestMissingId)
			verify(mockExecution).setVariable(Prefix + "rollbackEnabled", false)

			verify(mockExecution).setVariable(Prefix + "networkInputs", networkInputsMissingId)
			verify(mockExecution).setVariable(Prefix + "messageId", "7df689f9-7b93-430b-9b9e-28140d70cc7ad")
			verify(mockExecution).setVariable(Prefix + "source", "VID")

		}

		@Test
		//@Ignore
		public void preProcessRequest_Json_MissingCloudRegion() {

			String networkModelInfo = """{"modelVersionId": "sn5256d1-5a33-55df-13ab-12abad84e111",
                                     "modelName": "CONTRAIL_EXTERNAL",
									 "modelType": "CONTRAIL_EXTERNAL",
									 "modelVersion": "1",
									 "modelCustomizationId": "sn5256d1-5a33-55df-13ab-12abad84e222",
									 "modelInvariantId": "sn5256d1-5a33-55df-13ab-12abad84e764"
									}""".trim()
			
			println "************ preProcessRequest_MissingCloudRegion() ************* "
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("networkModelInfo")).thenReturn(networkModelInfo)
			when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("requestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("isBaseVfModule")).thenReturn(true)
			when(mockExecution.getVariable("recipeTimeout")).thenReturn(0)
			when(mockExecution.getVariable("requestAction")).thenReturn("DELETE")
			when(mockExecution.getVariable("networkId")).thenReturn("bdc5efe8-404a-409b-85f6-0dcc9eebae30")
			when(mockExecution.getVariable("serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
			when(mockExecution.getVariable("vnfId")).thenReturn("")
			when(mockExecution.getVariable("volumeGroupId")).thenReturn("")
			//when(mockExecution.getVariable("networkId")).thenReturn("")
			when(mockExecution.getVariable("serviceType")).thenReturn("MOG")
			when(mockExecution.getVariable("networkType")).thenReturn("modelName")
			when(mockExecution.getVariable("bpmnRequest")).thenReturn(incomingRequestMissingCloudRegion)
			when(mockExecution.getVariable("disableRollback")).thenReturn(true)
			
			when(mockExecution.getVariable("testMessageId")).thenReturn("7df689f9-7b93-430b-9b9e-28140d70cc7ad")
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("mso-service-instance-id")).thenReturn("FH/VLXM/003717//SW_INTERNET")
			when(mockExecution.getVariable("mso.adapters.po.auth")).thenReturn("3141634BF7E070AA289CF2892C986C0B")
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("mso.rollback")).thenReturn("true")
			when(mockExecution.getVariable("sdncVersion")).thenReturn("1610")

			// preProcessRequest(DelegateExecution execution)
			DoDeleteNetworkInstance DoDeleteNetworkInstance = new DoDeleteNetworkInstance()
			DoDeleteNetworkInstance.preProcessRequest(mockExecution)

			//verify(mockExecution).getVariable("isDebugLogEnabled")
			verify(mockExecution).setVariable("prefix", Prefix)

			//verify variable initialization
			initializeVariables(mockExecution)

			verify(mockExecution).setVariable("action", "DELETE")
			verify(mockExecution).setVariable(Prefix + "networkRequest", expectedNetworkRequestMissingCloudRegion)
			verify(mockExecution).setVariable(Prefix + "rollbackEnabled", false)

			verify(mockExecution).setVariable(Prefix + "networkInputs", networkInputsMissingCloudRegion)
			verify(mockExecution).setVariable(Prefix + "messageId", "7df689f9-7b93-430b-9b9e-28140d70cc7ad")
			verify(mockExecution).setVariable(Prefix + "lcpCloudRegion", null)

			verify(mockExecution).setVariable("BasicAuthHeaderValuePO","Basic cGFzc3dvcmQ=")
			verify(mockExecution).setVariable("BasicAuthHeaderValueSDNC", "Basic cGFzc3dvcmQ=")

		}



		@Test
		//@Ignore
		public void prepareNetworkRequest() {

			println "************ prepareNetworkRequest ************* "
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(expectedVperNetworkRequest)
			when(mockExecution.getVariable(Prefix + "queryAAIResponse")).thenReturn(queryAAIResponse)
			when(mockExecution.getVariable(Prefix + "cloudRegionPo")).thenReturn("RDM2WAGPLCP")
			when(mockExecution.getVariable(Prefix + "tenantId")).thenReturn("e81d842d3e8b45c5a59f57cd76af3aaf")

			when(mockExecution.getVariable("serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
			when(mockExecution.getVariable("mso.adapters.network.rest.endpoint")).thenReturn("http://localhost:8090/networks/NetworkAdapter")
			when(mockExecution.getVariable("msoRequestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("messageId_value")
			//when(mockExecution.getVariable("URN_?????")).thenReturn("")   // notificationUrl, //TODO - is this coming from URN? What variable/value to use? 
			when(mockExecution.getVariable(Prefix + "rollbackEnabled")).thenReturn("true")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")

			DoDeleteNetworkInstance DoDeleteNetworkInstance = new DoDeleteNetworkInstance()
			DoDeleteNetworkInstance.prepareNetworkRequest(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable("mso.adapters.network.rest.endpoint", "http://localhost:8090/networks/NetworkAdapter/bdc5efe8-404a-409b-85f6-0dcc9eebae30")
			//verify(mockExecution).setVariable(Prefix + "deleteNetworkRequest", deleteNetworkRequest)
			verify(mockExecution).setVariable(Prefix + "deleteNetworkRequest", deleteNetworkRESTRequest)

		}
		
		@Test
		//@Ignore
		public void prepareNetworkRequest_AlaCarte() {

			println "************ prepareNetworkRequest ************* "
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(expectedNetworkRequest)
			when(mockExecution.getVariable(Prefix + "queryAAIResponse")).thenReturn(queryAAIResponse)
			when(mockExecution.getVariable(Prefix + "cloudRegionPo")).thenReturn("RDM2WAGPLCP")
			when(mockExecution.getVariable(Prefix + "tenantId")).thenReturn("e81d842d3e8b45c5a59f57cd76af3aaf")

			when(mockExecution.getVariable("serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
			when(mockExecution.getVariable("mso.adapters.network.rest.endpoint")).thenReturn("http://localhost:8090/networks/NetworkAdapter")
			when(mockExecution.getVariable("msoRequestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("messageId_value")
			//when(mockExecution.getVariable("URN_?????")).thenReturn("")   // notificationUrl, //TODO - is this coming from URN? What variable/value to use?
			when(mockExecution.getVariable(Prefix + "rollbackEnabled")).thenReturn("true")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")

			DoDeleteNetworkInstance DoDeleteNetworkInstance = new DoDeleteNetworkInstance()
			DoDeleteNetworkInstance.prepareNetworkRequest(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable("mso.adapters.network.rest.endpoint", "http://localhost:8090/networks/NetworkAdapter/bdc5efe8-404a-409b-85f6-0dcc9eebae30")
			//verify(mockExecution).setVariable(Prefix + "deleteNetworkRequest", deleteNetworkRequest)
			verify(mockExecution).setVariable("mso-request-id", "88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			verify(mockExecution).setVariable(Prefix + "deleteNetworkRequest", deleteNetworkRESTRequestAlaCarte)

		}

		@Test
		//@Ignore
		public void sendRequestToVnfAdapter() {

			println "************ sendRequestToVnfAdapter ************* "

			WireMock.reset();
			MockNetworkAdapter("bdc5efe8-404a-409b-85f6-0dcc9eebae30", 200, "deleteNetworkResponse_Success.xml");

			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable(Prefix + "deleteNetworkRequest")).thenReturn(deleteNetworkRESTRequest)
			when(mockExecution.getVariable("mso.adapters.network.rest.endpoint")).thenReturn("http://localhost:8090/networks/NetworkAdapter/bdc5efe8-404a-409b-85f6-0dcc9eebae30")
			when(mockExecution.getVariable("BasicAuthHeaderValuePO")).thenReturn("3141634BF7E070AA289CF2892C986C0B")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")

			DoDeleteNetworkInstance DoDeleteNetworkInstance = new DoDeleteNetworkInstance()
			DoDeleteNetworkInstance.sendRequestToVnfAdapter(mockExecution)

			verify(mockExecution).setVariable(Prefix + "networkReturnCode", 200)
			//verify(mockExecution).setVariable(Prefix + "deleteNetworkResponse", deleteNetworkResponse_noRollback)
						
		}


		@Test
		//@Ignore
		public void prepareSDNCRequest() {

			println "************ prepareSDNCRequest ************* "

			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(expectedNetworkRequest)
			when(mockExecution.getVariable(Prefix + "cloudRegionSdnc")).thenReturn("RDM2WAGPLCP")
			when(mockExecution.getVariable(Prefix + "queryAAIResponse")).thenReturn(aaiResponse)
			when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("testMessageId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
			when(mockExecution.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("http://localhost:8090/SDNCAdapterCallback")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")

			DoDeleteNetworkInstance DoDeleteNetworkInstance = new DoDeleteNetworkInstance()
			DoDeleteNetworkInstance.prepareSDNCRequest(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable(Prefix + "deleteSDNCRequest", deleteSDNCRequest)

		}

		@Test
		//@Ignore
		public void prepareRpcSDNCRequest() {

			println "************ prepareRpcSDNCRequest ************* "

			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(inputViprSDC_NetworkRequest)
			when(mockExecution.getVariable(Prefix + "cloudRegionSdnc")).thenReturn("RDM2WAGPLCP")
			when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("msoRequestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("testMessageId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56") // test ONLY
			when(mockExecution.getVariable("serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
			when(mockExecution.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("http://localhost:8090/SDNCAdapterCallback")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")

			DoDeleteNetworkInstance DoDeleteNetworkInstance = new DoDeleteNetworkInstance()
			DoDeleteNetworkInstance.prepareRpcSDNCRequest(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable(Prefix + "deleteSDNCRequest", deleteRpcSDNCRequest)

		}

		@Test
		//@Ignore
		public void prepareRpcSDNCActivateRollback() {

			println "************ prepareRpcSDNCActivateRollback ************* "

			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			when(mockExecution.getVariable(Prefix + "networkRequest")).thenReturn(inputViprSDC_NetworkRequest)
			when(mockExecution.getVariable(Prefix + "cloudRegionSdnc")).thenReturn("RDM2WAGPLCP")
			when(mockExecution.getVariable(Prefix + "serviceInstanceId")).thenReturn("f70e927b-6087-4974-9ef8-c5e4d5847ca4")
			when(mockExecution.getVariable(Prefix + "networkId")).thenReturn("8abc633a-810b-4ca5-8b3a-09511d13a2ce")
			when(mockExecution.getVariable("mso-request-id")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("msoRequestId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			when(mockExecution.getVariable("testMessageId")).thenReturn("88f65519-9a38-4c4b-8445-9eb4a5a5af56") // test ONLY
			when(mockExecution.getVariable("mso.workflow.sdncadapter.callback")).thenReturn("http://localhost:8090/SDNCAdapterCallback")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("GENGSI_siResourceLink")).thenReturn(null)
			when(mockExecution.getVariable(Prefix + "deactivateSDNCResponse")).thenReturn(sdncAdapterWorkflowFormattedResponse)

			// preProcessRequest(DelegateExecution execution)
			DoDeleteNetworkInstance DoDeleteNetworkInstance = new DoDeleteNetworkInstance()
			DoDeleteNetworkInstance.prepareRpcSDNCDeactivateRollback(mockExecution)

			// verify set prefix = Prefix + ""
			verify(mockExecution).setVariable("prefix", Prefix)
			//verify(mockExecution).setVariable("mso-request-id", "88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			//verify(mockExecution).setVariable(Prefix + "requestId", "88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			//verify(mockExecution).setVariable("mso-service-instance-id", "88f65519-9a38-4c4b-8445-9eb4a5a5af56")
			verify(mockExecution).setVariable(Prefix + "rollbackDeactivateSDNCRequest", sdncAdapaterDeactivateRollback)

		}


		@Test

		public void callRESTQueryAAI_VfRelationshipExist() {

			println "************ callRESTQueryAAI ************* "

			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable(Prefix + "networkInputs")).thenReturn(expectedNetworkRequest)
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:8090")
			when(mockExecution.getVariable("mso.workflow.default.aai.network.l3-network.uri")).thenReturn("")
			// old: when(mockExecution.getVariable("mso.workflow.DoDeleteNetworkInstance.aai.network.l3-network.uri")).thenReturn("/aai/v8/network/l3-networks/l3-network")
			when(mockExecution.getVariable("mso.workflow.DoDeleteNetworkInstance.aai.l3-network.uri")).thenReturn("/aai/v9/network/l3-networks/l3-network")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")
            String networkId = "bdc5efe8-404a-409b-85f6-0dcc9eebae30"
            DoDeleteNetworkInstance doDeleteNetworkInstance = spy(DoDeleteNetworkInstance.class)
            when(doDeleteNetworkInstance.getAAIClient()).thenReturn(client)
            L3Network l3Network = getL3Network()
            Relationship relationship = new Relationship();
            relationship.setRelatedTo("vf-module")
            l3Network.getRelationshipList().getRelationship().add(relationship)
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().l3Network(networkId)).depth(Depth.ALL)
            when(client.get(L3Network.class,uri)).thenReturn(Optional.of(l3Network))

			doDeleteNetworkInstance.callRESTQueryAAI(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix)

			verify(mockExecution).setVariable(Prefix + "aaiReturnCode", 200)
			//verify(mockExecution).setVariable(Prefix + "queryAAIResponse", aaiResponse)
			verify(mockExecution).setVariable(Prefix + "isAAIGood", true)
			verify(mockExecution).setVariable(Prefix + "isVfRelationshipExist", true)

		}

		@Test
		//@Ignore
		public void callRESTQueryAAI_200() {

			println "************ callRESTQueryAAI ************* "
			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable(Prefix + "networkInputs")).thenReturn(expectedNetworkRequest)
			when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
			when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:8090")
			when(mockExecution.getVariable("mso.workflow.default.aai.network.l3-network.uri")).thenReturn("")
			when(mockExecution.getVariable("mso.workflow.doDeleteNetworkInstance.aai.l3-network.uri")).thenReturn("/aai/v9/network/l3-networks/l3-network")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")
            when(mockExecution.getVariable(Prefix + "lcpCloudRegion")).thenReturn(null)
			String networkId = "bdc5efe8-404a-409b-85f6-0dcc9eebae30"
			DoDeleteNetworkInstance doDeleteNetworkInstance = spy(DoDeleteNetworkInstance.class)
			when(doDeleteNetworkInstance.getAAIClient()).thenReturn(client)
			L3Network l3Network = getL3Network()
            AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().l3Network(networkId)).depth(Depth.ALL)
			when(client.get(L3Network.class,uri)).thenReturn(Optional.of(l3Network))
			doDeleteNetworkInstance.callRESTQueryAAI(mockExecution)

			verify(mockExecution, atLeast(1)).setVariable("prefix", Prefix)

			verify(mockExecution).setVariable(Prefix + "aaiReturnCode", 200)
			verify(mockExecution).setVariable(Prefix + "isAAIGood", true)
            verify(mockExecution).setVariable(Prefix + "queryAAIResponse", l3Network)
		}


    @Test
    //@Ignore
    public void callRESTQueryAAI_CloudRegionRelation() {

        println "************ callRESTQueryAAI ************* "
        ExecutionEntity mockExecution = setupMock()
        when(mockExecution.getVariable(Prefix + "networkInputs")).thenReturn(expectedNetworkRequest)
        when(mockExecution.getVariable(Prefix + "messageId")).thenReturn("e8ebf6a0-f8ea-4dc0-8b99-fe98a87722d6")
        when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:8090")
        when(mockExecution.getVariable("mso.workflow.default.aai.network.l3-network.uri")).thenReturn("")
        when(mockExecution.getVariable("mso.workflow.doDeleteNetworkInstance.aai.l3-network.uri")).thenReturn("/aai/v9/network/l3-networks/l3-network")
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
        when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
        when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")
        when(mockExecution.getVariable(Prefix + "lcpCloudRegion")).thenReturn(null)
        String networkId = "bdc5efe8-404a-409b-85f6-0dcc9eebae30"
        DoDeleteNetworkInstance doDeleteNetworkInstance = spy(DoDeleteNetworkInstance.class)
        when(doDeleteNetworkInstance.getAAIClient()).thenReturn(client)
        L3Network l3Network = new L3Network();
        RelationshipList relationshipList = new RelationshipList()
        Relationship relationship = new Relationship();
        relationship.setRelatedTo("cloud-region")
        relationship.setRelatedLink("http://localhost:18080/cloud-regions/cloud-region/" + Defaults.CLOUD_OWNER.toString() + "/lcpCloudRegion/")
        relationshipList.getRelationship().add(relationship)
        l3Network.setRelationshipList(relationshipList)
        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().l3Network(networkId)).depth(Depth.ALL)
        when(client.get(L3Network.class,uri)).thenReturn(Optional.of(l3Network))
        doDeleteNetworkInstance.callRESTQueryAAI(mockExecution)

        verify(mockExecution, atLeast(1)).setVariable("prefix", Prefix)

        verify(mockExecution).setVariable(Prefix + "aaiReturnCode", 200)
        verify(mockExecution).setVariable(Prefix + "isAAIGood", true)
        verify(mockExecution).setVariable(Prefix + "queryAAIResponse", l3Network)
    }

    private L3Network getL3Network() {
		L3Network l3Network = new L3Network();
		RelationshipList relationshipList = new RelationshipList()
		Relationship relationship = new Relationship();
		relationship.setRelatedTo("cloud-region")
		relationship.setRelatedLink("http://localhost:18080/cloud-regions/cloud-region/" + Defaults.CLOUD_OWNER.toString() + "/lcpCloudRegion/")
		relationshipList.getRelationship().add(relationship)
		relationship.setRelatedTo("tenant")
		relationship.setRelatedLink("http://localhost:18080/cloud-regions/cloud-region/" + Defaults.CLOUD_OWNER.toString() + "/lcpCloudRegion/tenants/tenant/tenantId/")
		relationshipList.getRelationship().add(relationship)

		l3Network.setRelationshipList(relationshipList)
		l3Network
	}


		@Test
		public void callRESTQueryAAICloudRegion30_200() {

			println "************ callRESTQueryAAICloudRegion30_200 ************* "

			WireMock.reset();
			MockGetNetworkCloudRegion("DeleteNetworkV2/cloudRegion30_AAIResponse_Success.xml", "RDM2WAGPLCP");

			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable("prefix")).thenReturn(Prefix)
			when(mockExecution.getVariable(Prefix + "networkInputs")).thenReturn(expectedNetworkRequest)
			when(mockExecution.getVariable(Prefix + "lcpCloudRegion")).thenReturn("RDM2WAGPLCP")
			when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:8090")
			// old: when(mockExecution.getVariable("mso.workflow.default.aai.cloud-infrastructure.cloud-region.uri")).thenReturn("/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner")
			when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn("8")
			when(mockExecution.getVariable("mso.workflow.default.aai.v8.cloud-region.uri")).thenReturn("/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

			DoDeleteNetworkInstance DoDeleteNetworkInstance = new DoDeleteNetworkInstance()
			DoDeleteNetworkInstance.callRESTQueryAAICloudRegion(mockExecution)

			verify(mockExecution, atLeast(1)).setVariable("prefix", Prefix)
			verify(mockExecution, atLeast(2)).setVariable(Prefix + "queryCloudRegionReturnCode", "200")

		}

		@Test
		//@Ignore
		public void callRESTQueryAAICloudRegion_NotFound() {

			println "************ callRESTQueryAAICloudRegion_NotFound ************* "

			WireMock.reset();
			MockGetNetworkCloudRegion_404("MDTWNJ21");

			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable("prefix")).thenReturn(Prefix)
			when(mockExecution.getVariable(Prefix + "networkInputs")).thenReturn(vnfRequestCloudRegionNotFound)
			when(mockExecution.getVariable(Prefix + "lcpCloudRegion")).thenReturn("MDTWNJ21")
			when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:8090")
			// old: when(mockExecution.getVariable("mso.workflow.default.aai.cloud-infrastructure.cloud-region.uri")).thenReturn("/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner")
			when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn("8")
			when(mockExecution.getVariable("mso.workflow.default.aai.v8.cloud-region.uri")).thenReturn("/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner")
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

			// preProcessRequest(DelegateExecution execution)
			DoDeleteNetworkInstance DoDeleteNetworkInstance = new DoDeleteNetworkInstance()
			DoDeleteNetworkInstance.callRESTQueryAAICloudRegion(mockExecution)

			verify(mockExecution, atLeast(1)).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable(Prefix + "cloudRegionPo", "MDTWNJ21")
			verify(mockExecution).setVariable(Prefix + "cloudRegionSdnc", "AAIAIC25")

		}

		@Test
		//@Ignore
		public void callRESTQueryAAICloudRegion25_200() {

			println "************ callRESTQueryAAICloudRegion25_200 ************* "

			WireMock.reset();
			MockGetNetworkCloudRegion("DeleteNetworkV2/cloudRegion25_AAIResponse_Success.xml", "RDM2WAGPLCP");

			ExecutionEntity mockExecution = setupMock()
			when(mockExecution.getVariable("prefix")).thenReturn(Prefix)
			when(mockExecution.getVariable(Prefix + "networkInputs")).thenReturn(expectedNetworkRequest)
			when(mockExecution.getVariable(Prefix + "lcpCloudRegion")).thenReturn("RDM2WAGPLCP")
			when(mockExecution.getVariable("aai.endpoint")).thenReturn("http://localhost:8090")
			// old: when(mockExecution.getVariable("mso.workflow.default.aai.cloud-infrastructure.cloud-region.uri")).thenReturn("/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner")
			when(mockExecution.getVariable("mso.workflow.global.default.aai.version")).thenReturn("8")
			when(mockExecution.getVariable("mso.workflow.default.aai.v8.cloud-region.uri")).thenReturn("/aai/v8/cloud-infrastructure/cloud-regions/cloud-region/CloudOwner")
			//
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("mso.workflow.global.default.aai.namespace")).thenReturn('http://org.openecomp.aai.inventory/')
			when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
			when(mockExecution.getVariable("aai.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")

			// preProcessRequest(DelegateExecution execution)
			DoDeleteNetworkInstance DoDeleteNetworkInstance = new DoDeleteNetworkInstance()
			DoDeleteNetworkInstance.callRESTQueryAAICloudRegion(mockExecution)

			verify(mockExecution, atLeast(1)).setVariable("prefix", Prefix)		
			verify(mockExecution, atLeast(2)).setVariable(Prefix + "queryCloudRegionReturnCode", "200")

		}


		@Test
		//@Ignore
		public void validateNetworkResponse() {

			println "************ validateNetworkResponse ************* "

			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable(Prefix + "deleteNetworkResponse")).thenReturn(deleteNetworkResponse)
			when(mockExecution.getVariable(Prefix + "networkReturnCode")).thenReturn('200')

			DoDeleteNetworkInstance DoDeleteNetworkInstance = new DoDeleteNetworkInstance()
			DoDeleteNetworkInstance.validateNetworkResponse(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix)
			//verify(mockExecution).setVariable(Prefix + "rollbackNetworkRequest", null)

		}


		@Test
		//@Ignore
		public void validateSDNCResponse_200() {

			println "************ validateSDNCResponse ************* "
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable(Prefix + "sdncReturnCode")).thenReturn("200")
			when(mockExecution.getVariable("SDNCA_SuccessIndicator")).thenReturn(true)

			DoDeleteNetworkInstance DoDeleteNetworkInstance = new DoDeleteNetworkInstance()
			try {
				DoDeleteNetworkInstance.validateSDNCResponse(mockExecution, sdncAdapterWorkflowResponse)
			} catch (Exception ex) {
			    println " Graceful Exit - " + ex.getMessage()
			}

		}

		@Test
		//@Ignore
		public void validateSDNCResponse_404() {

			println "************ validateSDNCResponse ************* "

			WorkflowException workflowException = new WorkflowException("DoCreateNetworkInstance", 2500, "Received error from SNDC Adapter: HTTP Status 404.")

			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable(Prefix + "sdncReturnCode")).thenReturn("404")
			when(mockExecution.getVariable("SDNCA_SuccessIndicator")).thenReturn(false)
			when(mockExecution.getVariable("WorkflowException")).thenReturn(workflowException)

			DoDeleteNetworkInstance DoDeleteNetworkInstance = new DoDeleteNetworkInstance()
			try {
				DoDeleteNetworkInstance.validateSDNCResponse(mockExecution)
			} catch (Exception ex) {
				println " Graceful Exit - " + ex.getMessage()
			}

		}

		@Test
		//@Ignore
		public void validateRpcSDNCDeactivateResponse() {

			println "************ validateRpcSDNCDeactivateResponse ************* "

			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable(Prefix + "deactivateSDNCResponse")).thenReturn(sdncAdapterWorkflowResponse)
			when(mockExecution.getVariable("prefix")).thenReturn(Prefix + "")
			when(mockExecution.getVariable("SDNCA_SuccessIndicator")).thenReturn(true)
			when(mockExecution.getVariable(Prefix + "deactivateSDNCReturnCode")).thenReturn("200")

			DoDeleteNetworkInstance DoDeleteNetworkInstance = new DoDeleteNetworkInstance()
			try {
			  DoDeleteNetworkInstance.validateRpcSDNCDeactivateResponse(mockExecution)
			  verify(mockExecution).setVariable(Prefix + "isSdncDeactivateRollbackNeeded", true)

			} catch (Exception ex) {
				println " Graceful Exit - " + ex.getMessage()
			}
			//debugger.printInvocations(mockExecution)

			//verify(mockExecution).setVariable(Prefix + "isSdncRollbackNeeded", true)

		}

		@Test
		@Ignore
		public void postProcessResponse() {

			println "************ postProcessResponse ************* "
			ExecutionEntity mockExecution = setupMock()
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable(Prefix + "source")).thenReturn("PORTAL")
			when(mockExecution.getVariable(Prefix + "isException")).thenReturn(false)

			// postProcessResponse(DelegateExecution execution)
			DoDeleteNetworkInstance DoDeleteNetworkInstance = new DoDeleteNetworkInstance()
			DoDeleteNetworkInstance.postProcessResponse(mockExecution)

			verify(mockExecution).setVariable("prefix", Prefix)
			verify(mockExecution).setVariable(Prefix + "Success", true)

		}

		@Test
		//@Ignore
		public void prepareRollbackData() {

			println "************ prepareRollbackData() ************* "



			WorkflowException workflowException = new WorkflowException("DoCreateNetworkInstance", 2500, "Received error from Network Adapter: JBWEB000065: HTTP Status 500.")

			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable(Prefix + "rollbackNetworkRequest")).thenReturn(deleteRollbackNetworkRequest)
			//when(mockExecution.getVariable(Prefix + "rollbackNetworkRequest")).thenReturn(null)
			//when(mockExecution.getVariable(Prefix + "rollbackNetworkRequest")).thenReturn("")
			when(mockExecution.getVariable(Prefix + "rollbackDeactivateSDNCRequest")).thenReturn(sdncAdapaterDeactivateRollback)
			when(mockExecution.getVariable("WorkflowException")).thenReturn(workflowException)

			// preProcessRequest(DelegateExecution execution)
			DoDeleteNetworkInstance DoDeleteNetworkInstance = new DoDeleteNetworkInstance()
			DoDeleteNetworkInstance.prepareRollbackData(mockExecution)

//			verify(mockExecution).getVariable("isDebugLogEnabled")
			verify(mockExecution).setVariable("prefix", Prefix)

		}


		private ExecutionEntity setupMock() {

			ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
			when(mockProcessDefinition.getKey()).thenReturn("DoDeleteNetworkInstance")
			RepositoryService mockRepositoryService = mock(RepositoryService.class)
			when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
			when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("DoDeleteNetworkInstance")
			when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
			ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
			when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)

			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables

			when(mockExecution.getId()).thenReturn("100")
			when(mockExecution.getProcessDefinitionId()).thenReturn("DoDeleteNetworkInstance")
			when(mockExecution.getProcessInstanceId()).thenReturn("DoDeleteNetworkInstance")
			when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
			when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)

			return mockExecution

		}

}
