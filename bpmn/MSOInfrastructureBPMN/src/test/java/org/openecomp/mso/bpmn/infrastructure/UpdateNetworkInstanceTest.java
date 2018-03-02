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

package org.openecomp.mso.bpmn.infrastructure;

import static org.openecomp.mso.bpmn.mock.StubResponseAAI.*;
import static org.openecomp.mso.bpmn.mock.StubResponseDatabase.MockUpdateRequestDB;
import static org.openecomp.mso.bpmn.mock.StubResponseNetworkAdapter.MockNetworkAdapterRestPut;
import static org.openecomp.mso.bpmn.mock.StubResponseSDNCAdapter.mockSDNCAdapterTopology;
import static org.openecomp.mso.bpmn.common.BPMNUtil.executeAsyncWorkflow;
import static org.openecomp.mso.bpmn.common.BPMNUtil.getVariable;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import org.openecomp.mso.bpmn.common.BPMNUtil;
import org.openecomp.mso.bpmn.common.WorkflowTest;
import org.openecomp.mso.bpmn.common.WorkflowTestTransformer;
import org.openecomp.mso.bpmn.common.SDNCAdapterCallbackRule;
import org.openecomp.mso.bpmn.mock.SDNCAdapterNetworkTopologyMockTransformer;

import com.github.tomakehurst.wiremock.extension.ResponseTransformer;

/**
 * Unit test cases for DoUpdateNetworkInstance.bpmn
 *
 */
public class UpdateNetworkInstanceTest extends WorkflowTest {
	@WorkflowTestTransformer
	public static final ResponseTransformer sdncAdapterMockTransformer =
		new SDNCAdapterNetworkTopologyMockTransformer();

	@Rule
	public final SDNCAdapterCallbackRule sdncAdapterCallbackRule =
		new SDNCAdapterCallbackRule(processEngineRule);

	/**
	 * End-to-End flow - Unit test for DoUpdateNetworkInstance.bpmn
	 *  - String input & String response
	 */

	@Test
	//@Ignore
	@Deployment(resources = {"process/UpdateNetworkInstance.bpmn",
							 "subprocess/DoUpdateNetworkInstance.bpmn",
			                 "subprocess/FalloutHandler.bpmn",
	                         "subprocess/CompleteMsoProcess.bpmn",
	                         "subprocess/GenericGetService.bpmn",
	                         "subprocess/SDNCAdapterV1.bpmn"})

	public void shouldInvokeServiceUpdateNetworkInstance_SuccessVID1() throws Exception {

		System.out.println("----------------------------------------------------------");
		System.out.println("            Success1 - UpdateNetworkInstance flow Started!       ");
		System.out.println("----------------------------------------------------------");

		// setup simulators
		mockSDNCAdapterTopology("UpdateNetworkV2mock/sdncUpdateNetworkTopologySimResponse.xml", "SvcAction>changeassign");
		MockNetworkAdapterRestPut("UpdateNetworkV2/updateNetworkResponse_Success.xml", "49c86598-f766-46f8-84f8-8d1c1b10f9b4");
		MockGetNetworkCloudRegion("CreateNetworkV2/cloudRegion25_AAIResponse_Success.xml", "RDM2WAGPLCP");
		MockGetNetworkByIdWithDepth("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "UpdateNetworkV2/updateNetwork_queryNetworkId_AAIResponse_Success.xml", "all");
		MockPutNetworkIdWithDepth("UpdateNetworkV2/updateNetwork_updateContrail_AAIResponse_Success.xml", "49c86598-f766-46f8-84f8-8d1c1b10f9b4", "all");
		MockGetNetworkVpnBinding("UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", "85f015d0-2e32-4c30-96d2-87a1a27f8017");
		MockGetNetworkVpnBinding("UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", "c980a6ef-3b88-49f0-9751-dbad8608d0a6");
		MockGetNetworkPolicy("UpdateNetworkV2/updateNetwork_queryNetworkPolicy_AAIResponse_Success.xml", "cee6d136-e378-4678-a024-2cd15f0ee0cg");
		MockGetNetworkTableReference("UpdateNetworkV2/updateNetwork_queryNetworkTableRef1_AAIResponse_Success.xml", "refFQDN1");
		MockGetNetworkTableReference("UpdateNetworkV2/updateNetwork_queryNetworkTableRef2_AAIResponse_Success.xml", "refFQDN2");
		MockUpdateRequestDB("DBUpdateResponse.xml");
		//MockNodeQueryServiceInstanceById("f70e927b-6087-4974-9ef8-c5e4d5847ca4", "UpdateNetworkV2/updateNetwork_queryInstance_Success.xml", "v8");
		MockNodeQueryServiceInstanceById("f70e927b-6087-4974-9ef8-c5e4d5847ca4", "UpdateNetworkV2/updateNetwork_queryInstance_Success.xml");

		Map<String, String> variables = setupVariablesVID1();
		//WorkflowResponse workflowResponse = executeAsyncWorkflow(processEngineRule, "UpdateNetworkInstance", variables);
		executeAsyncWorkflow(processEngineRule, "UpdateNetworkInstance", variables);
		System.out.println("----------------------------------------------------------");
		System.out.println("- got workflow response -");
		System.out.println("----------------------------------------------------------");
		//waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

	    assertEquals("true", getVariable(processEngineRule, "UpdateNetworkInstance", "UPDNI_Success"));
	    Assert.assertNotNull("UPDNI_CompleteMsoProcessRequest - ", getVariable(processEngineRule, "UpdateNetworkInstance", "UPDNI_CompleteMsoProcessRequest"));

		String workflowResp = BPMNUtil.getVariable(processEngineRule, "UpdateNetworkInstance", "WorkflowResponse");
		Assert.assertNotNull(workflowResp);

		System.out.println("----------------------------------------------------------");
		System.out.println("     Success1 - UpdateNetworkInstance flow Completed      ");
		System.out.println("----------------------------------------------------------");

	}

	@Test
	//@Ignore
	@Deployment(resources = {"process/UpdateNetworkInstance.bpmn",
			                 "subprocess/DoUpdateNetworkInstance.bpmn",
			                 "subprocess/FalloutHandler.bpmn",
	                         "subprocess/CompleteMsoProcess.bpmn",
	                         "subprocess/GenericGetService.bpmn",
	                         "subprocess/SDNCAdapterV1.bpmn"})

	public void shouldInvokeServiceUpdateNetworkInstance_SuccessVIPER1() throws Exception {

		System.out.println("----------------------------------------------------------");
		System.out.println("            Success2 - UpdateNetworkInstance flow Started!      ");
		System.out.println("----------------------------------------------------------");

		// setup simulators
		mockSDNCAdapterTopology("UpdateNetworkV2mock/sdncUpdateNetworkTopologySimResponse.xml", "SvcAction>changeassign");
		MockNetworkAdapterRestPut("UpdateNetworkV2/updateNetworkResponse_Success.xml", "49c86598-f766-46f8-84f8-8d1c1b10f9b4");
		MockGetNetworkCloudRegion("CreateNetworkV2/cloudRegion25_AAIResponse_Success.xml", "RDM2WAGPLCP");
		MockGetNetworkByIdWithDepth("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "UpdateNetworkV2/updateNetwork_queryNetworkId_AAIResponse_Success.xml", "all");
		MockPutNetworkIdWithDepth("UpdateNetworkV2/updateNetwork_updateContrail_AAIResponse_Success.xml", "49c86598-f766-46f8-84f8-8d1c1b10f9b4", "all");
		MockGetNetworkVpnBinding("UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", "85f015d0-2e32-4c30-96d2-87a1a27f8017");
		MockGetNetworkVpnBinding("UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", "c980a6ef-3b88-49f0-9751-dbad8608d0a6");
		MockGetNetworkPolicy("UpdateNetworkV2/updateNetwork_queryNetworkPolicy_AAIResponse_Success.xml", "cee6d136-e378-4678-a024-2cd15f0ee0cg");
		MockGetNetworkTableReference("UpdateNetworkV2/updateNetwork_queryNetworkTableRef1_AAIResponse_Success.xml", "refFQDN1");
		MockGetNetworkTableReference("UpdateNetworkV2/updateNetwork_queryNetworkTableRef2_AAIResponse_Success.xml", "refFQDN2");
		MockUpdateRequestDB("DBUpdateResponse.xml");
		//MockNodeQueryServiceInstanceById("f70e927b-6087-4974-9ef8-c5e4d5847ca4", "UpdateNetworkV2/updateNetwork_queryInstance_Success.xml", "v8");
		MockNodeQueryServiceInstanceById("f70e927b-6087-4974-9ef8-c5e4d5847ca4", "UpdateNetworkV2/updateNetwork_queryInstance_Success.xml");

		Map<String, String> variables = setupVariablesVIPER1();
		//WorkflowResponse workflowResponse = executeAsyncWorkflow(processEngineRule, "UpdateNetworkInstance", variables);
		executeAsyncWorkflow(processEngineRule, "UpdateNetworkInstance", variables);
		//waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

	    assertEquals("true", getVariable(processEngineRule, "UpdateNetworkInstance", "UPDNI_Success"));
	    Assert.assertNotNull("UPDNI_CompleteMsoProcessRequest - ", getVariable(processEngineRule, "UpdateNetworkInstance", "UPDNI_CompleteMsoProcessRequest"));

	    String completeMsoProcessRequest =
	    		"<aetgt:MsoCompletionRequest xmlns:aetgt=\"http://org.openecomp/mso/workflow/schema/v1\""  + '\n'
	    	  + "                            xmlns:ns=\"http://org.openecomp/mso/request/types/v1\""  + '\n'
	    	  + "                            xmlns=\"http://org.openecomp/mso/infra/vnf-request/v1\">"  + '\n'
	    	  + "   <request-info>"  + '\n'
	    	  + "      <request-id>testRequestId</request-id>"  + '\n'
	    	  + "      <action>UPDATE</action>"  + '\n'
	    	  + "      <source>VID</source>"  + '\n'
	    	  + "   </request-info>"  + '\n'
	    	  + "   <aetgt:status-message>Network has been updated successfully.</aetgt:status-message>" + '\n'
	    	  + "   <aetgt:mso-bpel-name>BPMN Network action: UPDATE</aetgt:mso-bpel-name>" + '\n'
	    	  + "</aetgt:MsoCompletionRequest>";

	    Assert.assertEquals(completeMsoProcessRequest, getVariable(processEngineRule, "UpdateNetworkInstance", "UPDNI_CompleteMsoProcessRequest"));

		System.out.println("----------------------------------------------------------");
		System.out.println("     Success2 - UpdateNetworkInstance flow Completed     ");
		System.out.println("----------------------------------------------------------");

	}


	@Test
	//@Ignore
	@Deployment(resources = {"process/UpdateNetworkInstance.bpmn",
            				 "subprocess/DoUpdateNetworkInstance.bpmn",
			                 "subprocess/FalloutHandler.bpmn",
	                         "subprocess/CompleteMsoProcess.bpmn",
	                         "subprocess/GenericGetService.bpmn",
	                         "subprocess/SDNCAdapterV1.bpmn"})

	public void shouldInvokeServiceUpdateNetworkInstance_MissingNetworkId() throws Exception {

		System.out.println("--------------------------------------------------------------------");
		System.out.println("     Missing networkId - UpdateNetworkInstance flow Started!   ");
		System.out.println("--------------------------------------------------------------------");

		// setup simulators
		MockUpdateRequestDB("DBUpdateResponse.xml");
		//MockNodeQueryServiceInstanceById("f70e927b-6087-4974-9ef8-c5e4d5847ca4", "UpdateNetworkV2/updateNetwork_queryInstance_Success.xml", "v8");
		MockNodeQueryServiceInstanceById("f70e927b-6087-4974-9ef8-c5e4d5847ca4", "UpdateNetworkV2/updateNetwork_queryInstance_Success.xml");

		Map<String, String> variables = setupVariablesMissingNetworkId();
		//WorkflowResponse workflowResponse = executeAsyncWorkflow(processEngineRule, "UpdateNetworkInstance", variables);
		executeAsyncWorkflow(processEngineRule, "UpdateNetworkInstance", variables);
		//waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

	    assertEquals("false", getVariable(processEngineRule, "UpdateNetworkInstance", "UPDNI_Success"));
	    Assert.assertNotNull("UPDNI_FalloutHandlerRequest - ", getVariable(processEngineRule, "UpdateNetworkInstance", "UPDNI_FalloutHandlerRequest"));

	    String falloutHandlerActual = getVariable(processEngineRule, "UpdateNetworkInstance", "UPDNI_FalloutHandlerRequest");
	    String falloutHandlerExpected =
"<aetgt:FalloutHandlerRequest xmlns:aetgt=\"http://org.openecomp/mso/workflow/schema/v1\"" + "\n" +
"					                             xmlns:ns=\"http://org.openecomp/mso/request/types/v1\"" + "\n" +
"					                             xmlns:wfsch=\"http://org.openecomp/mso/workflow/schema/v1\">" + "\n" +
"					   <request-info xmlns=\"http://org.openecomp/mso/infra/vnf-request/v1\">" + "\n" +
"					      <request-id>88f65519-9a38-4c4b-8445-9eb4a5a5af56</request-id>" + "\n" +
"					      <action>UPDATE</action>" + "\n" +
"					      <source>VID</source>" + "\n" +
"					   </request-info>" + "\n" +
"						<aetgt:WorkflowException xmlns:aetgt=\"http://org.openecomp/mso/workflow/schema/v1\">" + "\n" +
"							<aetgt:ErrorMessage>Variable 'network-id' value/element is missing.</aetgt:ErrorMessage>" + "\n" +
"							<aetgt:ErrorCode>7000</aetgt:ErrorCode>" + "\n" +
"						</aetgt:WorkflowException>" + "\n" +
"					</aetgt:FalloutHandlerRequest>";

		assertEquals("Response", falloutHandlerExpected, falloutHandlerActual);

		System.out.println("------------------------------------------------------------------");
		System.out.println("    Missing networkId - UpdateNetworkInstance flow Completed ");
		System.out.println("------------------------------------------------------------------");

	}


	@Test
	//@Ignore
	@Deployment(resources = {"process/UpdateNetworkInstance.bpmn",
		     				 "subprocess/DoUpdateNetworkInstance.bpmn",
		     				 "subprocess/DoUpdateNetworkInstanceRollback.bpmn",
			                 "subprocess/FalloutHandler.bpmn",
	                         "subprocess/CompleteMsoProcess.bpmn",
	                         "subprocess/GenericGetService.bpmn",
	                         "subprocess/SDNCAdapterV1.bpmn"})

	public void shouldInvokeServiceUpdateNetworkInstance_Network_SDNC_Rollback() throws Exception {

		System.out.println("---------------------------------------------------------------");
		System.out.println("    Network and SDNC Rollback - UpdateNetworkInstance flow Started!       ");
		System.out.println("---------------------------------------------------------------");

		// setup simulators
		mockSDNCAdapterTopology("UpdateNetworkV2mock/sdncUpdateNetworkTopologySimResponse.xml", "SvcAction>changeassign");
		mockSDNCAdapterTopology("UpdateNetworkV2mock/sdncUpdateNetworkTopologySimResponse.xml", "SvcAction>rollback");
		MockNetworkAdapterRestPut("UpdateNetworkV2/updateNetworkResponse_Success.xml", "49c86598-f766-46f8-84f8-8d1c1b10f9b4");
		MockGetNetworkCloudRegion("CreateNetworkV2/cloudRegion25_AAIResponse_Success.xml", "RDM2WAGPLCP");
		MockGetNetworkByIdWithDepth("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "UpdateNetworkV2/updateNetwork_queryNetworkId_AAIResponse_Success.xml", "1");
		MockGetNetworkVpnBinding("UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", "85f015d0-2e32-4c30-96d2-87a1a27f8017");
		MockGetNetworkVpnBinding("UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", "c980a6ef-3b88-49f0-9751-dbad8608d0a6");
		MockGetNetworkPolicy("UpdateNetworkV2/updateNetwork_queryNetworkPolicy_AAIResponse_Success.xml", "cee6d136-e378-4678-a024-2cd15f0ee0cg");
		MockGetNetworkTableReference("UpdateNetworkV2/updateNetwork_queryNetworkTableRef1_AAIResponse_Success.xml", "refFQDN1");
		MockGetNetworkTableReference("UpdateNetworkV2/updateNetwork_queryNetworkTableRef2_AAIResponse_Success.xml", "refFQDN2");
		MockUpdateRequestDB("DBUpdateResponse.xml");
		//MockNodeQueryServiceInstanceById("f70e927b-6087-4974-9ef8-c5e4d5847ca4", "UpdateNetworkV2/updateNetwork_queryInstance_Success.xml", "v8");
		MockNodeQueryServiceInstanceById("f70e927b-6087-4974-9ef8-c5e4d5847ca4", "UpdateNetworkV2/updateNetwork_queryInstance_Success.xml");

		Map<String, String> variables = setupVariablesVID1();
		//WorkflowResponse workflowResponse = executeAsyncWorkflow(processEngineRule, "UpdateNetworkInstance", variables);
		executeAsyncWorkflow(processEngineRule, "UpdateNetworkInstance", variables);
		//waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

	    assertEquals("false", getVariable(processEngineRule, "UpdateNetworkInstance", "UPDNI_Success"));
	    Assert.assertNotNull("UPDNI_FalloutHandlerRequest - ", getVariable(processEngineRule, "UpdateNetworkInstance", "UPDNI_FalloutHandlerRequest"));

		System.out.println("---------------------------------------------------------------------");
		System.out.println(" Network and SCNC Rollback - UpdateNetworkInstance flow Completed   ");
		System.out.println("---------------------------------------------------------------------");

	}

	// *****************
	// Utility Section
	// *****************

	String networkModelInfo =
		       "  {\"modelUuid\": \"mod-inst-uuid-123\", " + '\n' +
            "   \"modelName\": \"mod_inst_z_123\", " + '\n' +
		       "   \"modelVersion\": \"1.0\", " + '\n' +
		       "   \"modelCustomizationUuid\": \"mod-inst-uuid-123\", " + '\n' +
		       "   \"modelInvariantUuid\": \"mod-invar-uuid-123\" " + '\n' +
		       "  }";

	String serviceModelInfo =
		       "  {\"modelUuid\": \"36a3a8ea-49a6-4ac8-b06c-89a54544b9b6\", " + '\n' +
            "   \"modelName\": \"HNGW Protected OAM\", " + '\n' +
		       "   \"modelVersion\": \"1.0\", " + '\n' +
		       "   \"modelInvariantUuid\": \"fcc85cb0-ad74-45d7-a5a1-17c8744fdb71\" " + '\n' +
		       "  }";

	// Success Scenario
	private Map<String, String> setupVariablesVID1() {
		Map<String, String> variables = new HashMap<String, String>();
		variables.put("bpmnRequest", getCreateNetworkRequest1());
		variables.put("mso-request-id", "testRequestId");
		variables.put("requestId", "testRequestId");
		variables.put("isBaseVfModule", "true");
		variables.put("recipeTimeout", "0");
		variables.put("requestAction", "UPDATE");
		variables.put("serviceInstanceId", "f70e927b-6087-4974-9ef8-c5e4d5847ca4");
		variables.put("vnfId", "");
		variables.put("vfModuleId", "");
		variables.put("volumeGroupId", "");
		variables.put("networkId", "49c86598-f766-46f8-84f8-8d1c1b10f9b4");
		variables.put("serviceType", "vMOG");
		variables.put("vfModuleType", "");
		variables.put("networkType", "modelName");
		return variables;

	}

	public String getCreateNetworkRequest1() {

		String request =
				"{ \"requestDetails\": { " + '\n' +
				"      \"modelInfo\": { " + '\n' +
				"         \"modelType\": \"modelType\", " + '\n' +
				"         \"modelCustomizationId\": \"f21df226-8093-48c3-be7e-0408fcda0422\", " + '\n' +
				"         \"modelName\": \"CONTRAIL_EXTERNAL\", " + '\n' +
				"         \"modelVersion\": \"1.0\" " + '\n' +
				"      }, " + '\n' +
				"      \"cloudConfiguration\": { " + '\n' +
				"          \"lcpCloudRegionId\": \"RDM2WAGPLCP\", " + '\n' +
				"          \"tenantId\": \"88a6ca3ee0394ade9403f075db23167e\" " + '\n' +
				"      }, " + '\n' +
				"      \"requestInfo\": { " + '\n' +
				"          \"instanceName\": \"MNS-25180-L-01-dmz_direct_net_1\", " + '\n' +
				"          \"source\": \"VID\", " + '\n' +
				"          \"callbackUrl\": \"\", " + '\n' +
				"          \"suppressRollback\": \"false\" ," + '\n' +
				"          \"productFamilyId\": \"a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb\" " + '\n' +
				"      }, " + '\n' +
				"      \"requestParameters\": { " + '\n' +
				"          \"backoutOnFailure\": true, " + '\n' +
				"          \"serviceId\": \"a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb\", " + '\n' +
				"          \"userParams\": [] " + '\n' +
				"      }	" + '\n' +
			    " } " + '\n' +
			    "}";

		return request;
	}

	public String getCreateNetworkRequest2() {

		String request =
				"{ \"requestDetails\": { " + '\n' +
				"      \"modelInfo\": { " + '\n' +
				"         \"modelType\": \"modelType\", " + '\n' +
				"         \"modelId\": \"modelId\", " + '\n' +
				"         \"modelNameVersionId\": \"modelNameVersionId\", " + '\n' +
				"         \"modelName\": \"CONTRAIL_EXTERNAL\", " + '\n' +
				"         \"modelVersion\": \"1\" " + '\n' +
				"      }, " + '\n' +
				"      \"cloudConfiguration\": { " + '\n' +
				"          \"lcpCloudRegionId\": \"RDM2WAGPLCP\", " + '\n' +
				"          \"tenantId\": \"88a6ca3ee0394ade9403f075db23167e\" " + '\n' +
				"      }, " + '\n' +
				"      \"requestInfo\": { " + '\n' +
				"          \"instanceName\": \"myOwn_Network\", " + '\n' +
				"          \"source\": \"VID\", " + '\n' +
				"          \"callbackUrl\": \"\", " + '\n' +
				"          \"suppressRollback\": \"true\" ," + '\n' +
				"          \"productFamilyId\": \"a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb\" " + '\n' +
				"      }, " + '\n' +
				"      \"requestParameters\": { " + '\n' +
				"          \"backoutOnFailure\": true, " + '\n' +
				"          \"serviceId\": \"a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb\", " + '\n' +
				"          \"userParams\": [] " + '\n' +
				"      }	" + '\n' +
			    " } " + '\n' +
			    "}";

		return request;

	}

	// Success Scenario
	private Map<String, String> setupVariablesVIPER1() {
		Map<String, String> variables = new HashMap<String, String>();
		variables.put("testMessageId", "88f65519-9a38-4c4b-8445-9eb4a5a5af56");
		variables.put("msoRequestId", "testRequestId");
		variables.put("requestId", "testRequestId");
		variables.put("serviceInstanceId", "f70e927b-6087-4974-9ef8-c5e4d5847ca4");
		variables.put("networkId", "49c86598-f766-46f8-84f8-8d1c1b10f9b4");
		variables.put("networkName", "MNS-25180-L-01-dmz_direct_net_1");
		variables.put("lcpCloudRegionId", "RDM2WAGPLCP");
		variables.put("tenantId", "88a6ca3ee0394ade9403f075db23167e");
		variables.put("productFamilyId", "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb");
		variables.put("disableRollback", "false"); // macro
		variables.put("failIfExists", "false");
		//variables.put("sdncVersion", "1702");
		variables.put("sdncVersion", "1707");
		variables.put("subscriptionServiceType", "MSO-dev-service-type");
		variables.put("globalSubscriberId", "globalId_45678905678");
		variables.put("networkModelInfo", networkModelInfo);
		variables.put("serviceModelInfo", serviceModelInfo);
		return variables;

	}

	// Missing Name Scenario
	private Map<String, String> setupVariablesMissingNetworkId() {
		Map<String, String> variables = new HashMap<String, String>();
		variables.put("mso-request-id", "88f65519-9a38-4c4b-8445-9eb4a5a5af56");
		variables.put("bpmnRequest", getCreateNetworkRequestNetworkId());
		variables.put("requestId", "testRequestId");
		variables.put("isBaseVfModule", "true");
		variables.put("recipeTimeout", "0");
		variables.put("requestAction", "UPDATE");
		variables.put("serviceInstanceId", "f70e927b-6087-4974-9ef8-c5e4d5847ca4");
		variables.put("vnfId", "");
		variables.put("vfModuleId", "");
		variables.put("volumeGroupId", "");
		//variables.put("networkId", "49c86598-f766-46f8-84f8-8d1c1b10f9b4"); // missing, ok
		variables.put("serviceType", "vMOG");
		variables.put("vfModuleType", "");
		variables.put("networkType", "modelName");

		return variables;

	}

	public String getCreateNetworkRequestNetworkId() {

		String request =
				"{ \"requestDetails\": { " + '\n' +
				"      \"modelInfo\": { " + '\n' +
				"         \"modelType\": \"modelType\", " + '\n' +
				"         \"modelId\": \"modelId\", " + '\n' +
				"         \"modelNameVersionId\": \"modelNameVersionId\", " + '\n' +
				"         \"modelName\": \"CONTRAIL_EXTERNAL\", " + '\n' +
				"         \"modelVersion\": \"1\" " + '\n' +
				"      }, " + '\n' +
				"      \"cloudConfiguration\": { " + '\n' +
				"          \"lcpCloudRegionId\": \"RDM2WAGPLCP\", " + '\n' +
				"          \"tenantId\": \"88a6ca3ee0394ade9403f075db23167e\" " + '\n' +
				"      }, " + '\n' +
				"      \"requestInfo\": { " + '\n' +
				"          \"source\": \"VID\", " + '\n' +
				"          \"callbackUrl\": \"\", " + '\n' +
				"          \"suppressRollback\": \"true\" ," + '\n' +
				"          \"productFamilyId\": \"a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb\" " + '\n' +
				"      }, " + '\n' +
				"      \"requestParameters\": { " + '\n' +
				"          \"userParams\": [] " + '\n' +
				"      }	" + '\n' +
			    " } " + '\n' +
			    "}";

			return request;

	}

	public String getCreateNetworkRequestSDNCRollback() {

		String request =
				"{ \"requestDetails\": { " + '\n' +
				"      \"modelInfo\": { " + '\n' +
				"         \"modelType\": \"modelType\", " + '\n' +
				"         \"modelId\": \"modelId\", " + '\n' +
				"         \"modelNameVersionId\": \"modelNameVersionId\", " + '\n' +
				"         \"modelName\": \"CONTRAIL_EXTERNAL\", " + '\n' +
				"         \"modelVersion\": \"1\" " + '\n' +
				"      }, " + '\n' +
				"      \"cloudConfiguration\": { " + '\n' +
				"          \"lcpCloudRegionId\": \"RDM2WAGPLCP\", " + '\n' +
				"          \"tenantId\": \"88a6ca3ee0394ade9403f075db23167e\" " + '\n' +
				"      }, " + '\n' +
				"      \"requestInfo\": { " + '\n' +
				"          \"instanceName\": \"MNS-25180-L-01-dmz_direct_net_3\", " + '\n' +
				"          \"source\": \"VID\", " + '\n' +
				"          \"callbackUrl\": \"\", " + '\n' +
				"          \"suppressRollback\": \"true\" ," + '\n' +
				"          \"productFamilyId\": \"a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb\" " + '\n' +
				"      }, " + '\n' +
				"      \"requestParameters\": { " + '\n' +
				"          \"userParams\": [] " + '\n' +
				"      }	" + '\n' +
			    " } " + '\n' +
			    "}";


		return request;
	}

}
