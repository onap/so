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

// new mock methods

import java.util.HashMap;
import java.util.Map;

/**
 * Unit test cases for CreateNetworkInstance.bpmn
 *
 */
public class CreateNetworkInstanceTest extends WorkflowTest {
	@WorkflowTestTransformer
	public static final ResponseTransformer sdncAdapterMockTransformer =
		new SDNCAdapterNetworkTopologyMockTransformer();

	@Rule
	public final SDNCAdapterCallbackRule sdncAdapterCallbackRule =
		new SDNCAdapterCallbackRule(processEngineRule);

	/**
	 * End-to-End flow - Unit test for CreateNetworkInstance.bpmn
	 *  - String input & String response
	 */


	@Test
	//@Ignore
	@Deployment(resources = {"process/CreateNetworkInstance.bpmn",
							 "subprocess/DoCreateNetworkInstance.bpmn",
							 "subprocess/DoCreateNetworkInstanceRollback.bpmn",
			                 "subprocess/FalloutHandler.bpmn",
	                         "subprocess/CompleteMsoProcess.bpmn",
	                         "subprocess/GenericGetService.bpmn",
	                         "subprocess/SDNCAdapterV1.bpmn"})

	public void shouldInvokeServiceCreateNetworkInstance_vIPER_Success1() throws Exception {

		System.out.println("-----------------------------------------------------------------");
		System.out.println("    Success vIPER 1 - CreateNetworkInstance flow Started!       ");
		System.out.println("-----------------------------------------------------------------");

		// setup simulators
		mockSDNCAdapterTopology("CreateNetworkV2mock/sdncCreateNetworkTopologyRsrcAssignResponse.xml", "SvcAction>assign");
		mockSDNCAdapterTopology("CreateNetworkV2mock/sdncCreateNetworkTopologySimResponse.xml", "SvcAction>activate");
		MockNetworkAdapterPost("CreateNetworkV2/createNetworkResponse_Success.xml", "createNetworkRequest");
		MockGetNetworkByName("MNS-25180-L-01-dmz_direct_net_1", "CreateNetworkV2/createNetwork_queryName_AAIResponse_Success.xml");
		MockGetNetworkCloudRegion("CreateNetworkV2/cloudRegion25_AAIResponse_Success.xml", "RDM2WAGPLCP");
		MockGetNetworkByIdWithDepth("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "CreateNetworkV2/createNetwork_queryNetworkId_AAIResponse_Success.xml", "1");
		MockGetNetworkVpnBinding("CreateNetworkV2/createNetwork_queryVpnBinding_AAIResponse_Success.xml", "85f015d0-2e32-4c30-96d2-87a1a27f8017");
		MockGetNetworkVpnBinding("CreateNetworkV2/createNetwork_queryVpnBinding_AAIResponse_Success.xml", "c980a6ef-3b88-49f0-9751-dbad8608d0a6");
		MockGetNetworkPolicy("CreateNetworkV2/createNetwork_queryNetworkPolicy_AAIResponse_Success.xml", "cee6d136-e378-4678-a024-2cd15f0ee0cg");
		MockGetNetworkTableReference("CreateNetworkV2/createNetwork_queryNetworkTableRef1_AAIResponse_Success.xml", "refFQDN1");
		MockGetNetworkTableReference("CreateNetworkV2/createNetwork_queryNetworkTableRef2_AAIResponse_Success.xml", "refFQDN2");
		MockPutNetworkIdWithDepth("CreateNetworkV2/createNetwork_updateContrail_AAIResponse_Success.xml", "49c86598-f766-46f8-84f8-8d1c1b10f9b4", "1");
		MockUpdateRequestDB("DBUpdateResponse.xml");
		//MockNodeQueryServiceInstanceById("f70e927b-6087-4974-9ef8-c5e4d5847ca4", "CreateNetworkV2/createNetwork_queryInstance_Success.xml", "v8");
		MockNodeQueryServiceInstanceById("f70e927b-6087-4974-9ef8-c5e4d5847ca4", "CreateNetworkV2/createNetwork_queryInstance_Success.xml");

		Map<String, String> variables = setupVariables1();
		//WorkflowResponse workflowResponse = executeAsyncWorkflow(processEngineRule, "CreateNetworkInstance", variables);
		executeAsyncWorkflow(processEngineRule, "CreateNetworkInstance", variables);
		System.out.println("----------------------------------------------------------");
		System.out.println("- got workflow response -");
		System.out.println("----------------------------------------------------------");
		//waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

	    assertEquals("true", getVariable(processEngineRule, "CreateNetworkInstance", "CRENI_Success"));
	    assertEquals("true", getVariable(processEngineRule, "DoCreateNetworkInstance", "CRENWKI_Success"));
	    Assert.assertNotNull("CRENI_CompleteMsoProcessRequest - ", getVariable(processEngineRule, "CreateNetworkInstance", "CRENI_CompleteMsoProcessRequest"));

		String workflowResp = BPMNUtil.getVariable(processEngineRule, "CreateNetworkInstance", "WorkflowResponse");
		Assert.assertNotNull(workflowResp);

		System.out.println("----------------------------------------------------------");
		System.out.println("   Success vIPER 1 - CreateNetworkInstance flow Completed      ");
		System.out.println("----------------------------------------------------------");

	}

	@Test
	//@Ignore
	@Deployment(resources = {"process/CreateNetworkInstance.bpmn",
			 				 "subprocess/DoCreateNetworkInstance.bpmn",
							 "subprocess/DoCreateNetworkInstanceRollback.bpmn",
			 				 "subprocess/GenericGetService.bpmn",
			                 "subprocess/FalloutHandler.bpmn",
	                         "subprocess/CompleteMsoProcess.bpmn",
	                         "subprocess/SDNCAdapterV1.bpmn"})

	public void shouldInvokeServiceCreateNetworkInstance_vIPER_Success2() throws Exception {

		System.out.println("----------------------------------------------------------------");
		System.out.println("  Success viPER 2 - CreateNetworkInstance flow Started!      ");
		System.out.println("----------------------------------------------------------------");

		// setup simulators
		mockSDNCAdapterTopology("CreateNetworkV2mock/sdncCreateNetworkTopologyRsrcAssignResponse.xml", "SvcAction>assign");
		mockSDNCAdapterTopology("CreateNetworkV2mock/sdncCreateNetworkTopologySimResponse.xml", "SvcAction>activate");
		MockNetworkAdapterPost("CreateNetworkV2/createNetworkResponse_Success.xml", "createNetworkRequest");
		MockGetNetworkByName_404("CreateNetworkV2/createNetwork_queryName_AAIResponse_Success.xml", "myOwn_Network");
		MockGetNetworkCloudRegion("CreateNetworkV2/cloudRegion25_AAIResponse_Success.xml", "RDM2WAGPLCP");
		MockGetNetworkByIdWithDepth("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "CreateNetworkV2/createNetwork_queryNetworkId_AAIResponse_Success.xml", "1");
		MockGetNetworkVpnBinding("CreateNetworkV2/createNetwork_queryVpnBinding_AAIResponse_Success.xml", "85f015d0-2e32-4c30-96d2-87a1a27f8017");
		MockGetNetworkVpnBinding("CreateNetworkV2/createNetwork_queryVpnBinding_AAIResponse_Success.xml", "c980a6ef-3b88-49f0-9751-dbad8608d0a6");
		MockGetNetworkPolicy("CreateNetworkV2/createNetwork_queryNetworkPolicy_AAIResponse_Success.xml", "cee6d136-e378-4678-a024-2cd15f0ee0cg");
		MockGetNetworkTableReference("CreateNetworkV2/createNetwork_queryNetworkTableRef1_AAIResponse_Success.xml", "refFQDN1");
		MockGetNetworkTableReference("CreateNetworkV2/createNetwork_queryNetworkTableRef2_AAIResponse_Success.xml", "refFQDN2");
		MockPutNetworkIdWithDepth("CreateNetworkV2/createNetwork_updateContrail_AAIResponse_Success.xml", "49c86598-f766-46f8-84f8-8d1c1b10f9b4", "1");
		MockUpdateRequestDB("DBUpdateResponse.xml");
		//MockNodeQueryServiceInstanceById("f70e927b-6087-4974-9ef8-c5e4d5847ca4", "CreateNetworkV2/createNetwork_queryInstance_Success.xml", "v8");
		MockNodeQueryServiceInstanceById("f70e927b-6087-4974-9ef8-c5e4d5847ca4", "CreateNetworkV2/createNetwork_queryInstance_Success.xml");

		Map<String, String> variables = setupVariables2();
		//WorkflowResponse workflowResponse = executeAsyncWorkflow(processEngineRule, "CreateNetworkInstance", variables);
		executeAsyncWorkflow(processEngineRule, "CreateNetworkInstance", variables);
		//waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

	    assertEquals("true", getVariable(processEngineRule, "CreateNetworkInstance", "CRENI_Success"));
	    assertEquals("true", getVariable(processEngineRule, "DoCreateNetworkInstance", "CRENWKI_Success"));
	    Assert.assertNotNull("CRENI_CompleteMsoProcessRequest - ", getVariable(processEngineRule, "CreateNetworkInstance", "CRENI_CompleteMsoProcessRequest"));

	    String completeMsoProcessRequest =
	    		"<aetgt:MsoCompletionRequest xmlns:aetgt=\"http://org.openecomp/mso/workflow/schema/v1\""  + '\n'
	    	  + "                            xmlns:ns=\"http://org.openecomp/mso/request/types/v1\""  + '\n'
	    	  + "                            xmlns=\"http://org.openecomp/mso/infra/vnf-request/v1\">"  + '\n'
	    	  + "   <request-info>"  + '\n'
	    	  + "      <request-id>testRequestId</request-id>"  + '\n'
	    	  + "      <action>CREATE</action>"  + '\n'
	    	  + "      <source>VID</source>"  + '\n'
	    	  + "   </request-info>"  + '\n'
	    	  + "   <aetgt:status-message>Network has been created successfully.</aetgt:status-message>"  + '\n'
	    	  + "   <aetgt:mso-bpel-name>BPMN Network action: CREATE</aetgt:mso-bpel-name>" + '\n'
	    	  + "</aetgt:MsoCompletionRequest>";

	    Assert.assertEquals(completeMsoProcessRequest, getVariable(processEngineRule, "CreateNetworkInstance", "CRENI_CompleteMsoProcessRequest"));

		System.out.println("---------------------------------------------------------");
		System.out.println("  Success viPER 2 - CreateNetworkInstance flow Completed     ");
		System.out.println("---------------------------------------------------------");

	}

	@Test
	//@Ignore
	@Deployment(resources = {"process/CreateNetworkInstance.bpmn",
                             "subprocess/DoCreateNetworkInstance.bpmn",
							 "subprocess/DoCreateNetworkInstanceRollback.bpmn",
			 			     "subprocess/GenericGetService.bpmn",
			                 "subprocess/FalloutHandler.bpmn",
	                         "subprocess/CompleteMsoProcess.bpmn",
	                         "subprocess/SDNCAdapterV1.bpmn"})

	public void shouldInvokeServiceCreateNetworkInstance_VID_1610_Network_SDNC_Rollback() throws Exception {

		System.out.println("--------------------------------------------------------------------------");
		System.out.println("    Network and SDNC Rollback - CreateNetworkInstance flow Started!       ");
		System.out.println("--------------------------------------------------------------------------");

		// setup simulators
		mockSDNCAdapterTopology("CreateNetworkV2mock/sdncCreateNetworkTopologySimResponse.xml", "SvcAction>assign");
		mockSDNCAdapterTopology("CreateNetworkV2mock/sdncCreateNetworkTopologySimResponse.xml", "SvcAction>rollback");
		MockNetworkAdapterPost("CreateNetworkV2/createNetworkResponse_Success.xml", "createNetworkRequest");
		MockGetNetworkByName("MNS-25180-L-01-dmz_direct_net_1", "CreateNetworkV2/createNetwork_queryName_AAIResponse_Success.xml");
		MockGetNetworkCloudRegion("CreateNetworkV2/cloudRegion25_AAIResponse_Success.xml", "RDM2WAGPLCP");
		MockGetNetworkByIdWithDepth("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "CreateNetworkV2/createNetwork_queryNetworkId_AAIResponse_Success.xml", "1");
		MockGetNetworkVpnBinding("CreateNetworkV2/createNetwork_queryVpnBinding_AAIResponse_Success.xml", "85f015d0-2e32-4c30-96d2-87a1a27f8017");
		MockGetNetworkVpnBinding("CreateNetworkV2/createNetwork_queryVpnBinding_AAIResponse_Success.xml", "c980a6ef-3b88-49f0-9751-dbad8608d0a6");
		MockGetNetworkPolicy("CreateNetworkV2/createNetwork_queryNetworkPolicy_AAIResponse_Success.xml", "cee6d136-e378-4678-a024-2cd15f0ee0cg");
		MockGetNetworkTableReference("CreateNetworkV2/createNetwork_queryNetworkTableRef1_AAIResponse_Success.xml", "refFQDN1");
		MockGetNetworkTableReference("CreateNetworkV2/createNetwork_queryNetworkTableRef2_AAIResponse_Success.xml", "refFQDN2");
		MockUpdateRequestDB("DBUpdateResponse.xml");
		//MockNodeQueryServiceInstanceById("f70e927b-6087-4974-9ef8-c5e4d5847ca4", "CreateNetworkV2/createNetwork_queryInstance_Success.xml", "v8");
		MockNodeQueryServiceInstanceById("f70e927b-6087-4974-9ef8-c5e4d5847ca4", "CreateNetworkV2/createNetwork_queryInstance_Success.xml");
		MockNetworkAdapterRestRollbackDelete("deleteNetworkResponse_Success.xml","49c86598-f766-46f8-84f8-8d1c1b10f9b4");

		Map<String, String> variables = setupVariablesVID1();
		executeAsyncWorkflow(processEngineRule, "CreateNetworkInstance", variables);
		//WorkflowResponse workflowResponse = executeAsyncWorkflow(processEngineRule, "CreateNetworkInstance", variables);
		//waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

	    assertEquals("false", getVariable(processEngineRule, "CreateNetworkInstance", "CRENI_Success"));
	    assertEquals("false", getVariable(processEngineRule, "DoCreateNetworkInstance", "CRENWKI_Success"));
	    Assert.assertNotNull("CRENI_FalloutHandlerRequest - ", getVariable(processEngineRule, "CreateNetworkInstance", "CRENI_FalloutHandlerRequest"));

		System.out.println("--------------------------------------------------------------------");
		System.out.println(" Network and SCNC Rollback - CreateNetworkInstance flow Completed   ");
		System.out.println("--------------------------------------------------------------------");

	}

	@Test
	//@Ignore
	@Deployment(resources = {"process/CreateNetworkInstance.bpmn",
                             "subprocess/DoCreateNetworkInstance.bpmn",
							 "subprocess/DoCreateNetworkInstanceRollback.bpmn",
			 			     "subprocess/GenericGetService.bpmn",
			                 "subprocess/FalloutHandler.bpmn",
	                         "subprocess/CompleteMsoProcess.bpmn",
	                         "subprocess/SDNCAdapterV1.bpmn"})

	public void shouldInvokeServiceCreateNetworkInstance_vIPER_1702_Network_SDNC_Rollback() throws Exception {

		System.out.println("--------------------------------------------------------------------------");
		System.out.println("    Network and SDNC Rollback - CreateNetworkInstance flow Started!       ");
		System.out.println("--------------------------------------------------------------------------");

		// setup simulators
		mockSDNCAdapterTopology("CreateNetworkV2mock/sdncCreateNetworkTopologyRsrcAssignResponse.xml", "SvcAction>assign");
		mockSDNCAdapterTopology("CreateNetworkV2mock/sdncCreateNetworkTopologySimResponse.xml", "SvcAction>unassign");
		mockSDNCAdapterTopology("CreateNetworkV2mock/sdncCreateNetworkTopologySimResponse.xml", "SvcAction>activate");
		mockSDNCAdapterTopology("CreateNetworkV2mock/sdncCreateNetworkTopologySimResponse.xml", "SvcAction>deactivate");
		MockNetworkAdapterPost("CreateNetworkV2/createNetworkResponse_Success.xml", "createNetworkRequest");
		MockGetNetworkByName("MNS-25180-L-01-dmz_direct_net_1", "CreateNetworkV2/createNetwork_queryName_AAIResponse_Success.xml");
		MockGetNetworkCloudRegion("CreateNetworkV2/cloudRegion25_AAIResponse_Success.xml", "RDM2WAGPLCP");
		MockGetNetworkByIdWithDepth("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "CreateNetworkV2/createNetwork_queryNetworkId_AAIResponse_Success.xml", "1");
		MockGetNetworkVpnBinding("CreateNetworkV2/createNetwork_queryVpnBinding_AAIResponse_Success.xml", "85f015d0-2e32-4c30-96d2-87a1a27f8017");
		MockGetNetworkVpnBinding("CreateNetworkV2/createNetwork_queryVpnBinding_AAIResponse_Success.xml", "c980a6ef-3b88-49f0-9751-dbad8608d0a6");
		MockGetNetworkPolicy("CreateNetworkV2/createNetwork_queryNetworkPolicy_AAIResponse_Success.xml", "cee6d136-e378-4678-a024-2cd15f0ee0cg");
		MockGetNetworkTableReference("CreateNetworkV2/createNetwork_queryNetworkTableRef1_AAIResponse_Success.xml", "refFQDN1");
		MockGetNetworkTableReference("CreateNetworkV2/createNetwork_queryNetworkTableRef2_AAIResponse_Success.xml", "refFQDN2");
		MockUpdateRequestDB("DBUpdateResponse.xml");
		//MockNodeQueryServiceInstanceById("f70e927b-6087-4974-9ef8-c5e4d5847ca4", "CreateNetworkV2/createNetwork_queryInstance_Success.xml", "v8");
		MockNodeQueryServiceInstanceById("f70e927b-6087-4974-9ef8-c5e4d5847ca4", "CreateNetworkV2/createNetwork_queryInstance_Success.xml");
		MockNetworkAdapterRestRollbackDelete("deleteNetworkResponse_Success.xml","49c86598-f766-46f8-84f8-8d1c1b10f9b4");		

		Map<String, String> variables = setupVariables1();
		executeAsyncWorkflow(processEngineRule, "CreateNetworkInstance", variables);
		//WorkflowResponse workflowResponse = executeAsyncWorkflow(processEngineRule, "CreateNetworkInstance", variables);
		//waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

	    assertEquals("false", getVariable(processEngineRule, "CreateNetworkInstance", "CRENI_Success"));
	    assertEquals("false", getVariable(processEngineRule, "DoCreateNetworkInstance", "CRENWKI_Success"));
	    Assert.assertNotNull("CRENI_FalloutHandlerRequest - ", getVariable(processEngineRule, "CreateNetworkInstance", "CRENI_FalloutHandlerRequest"));

		System.out.println("--------------------------------------------------------------------");
		System.out.println(" Network and SCNC Rollback - CreateNetworkInstance flow Completed   ");
		System.out.println("--------------------------------------------------------------------");

	}

	@Test
	//@Ignore
	@Deployment(resources = {"process/CreateNetworkInstance.bpmn",
                             "subprocess/DoCreateNetworkInstance.bpmn",
							 "subprocess/DoCreateNetworkInstanceRollback.bpmn",
			                 "subprocess/GenericGetService.bpmn",
			                 "subprocess/FalloutHandler.bpmn",
	                         "subprocess/CompleteMsoProcess.bpmn",
	                         "subprocess/SDNCAdapterV1.bpmn"})

	public void shouldInvokeServiceCreateNetworkInstance_sdncFailure() throws Exception {

		System.out.println("----------------------------------------------------------------");
		System.out.println("        SNDC Failure - CreateNetworkInstance flow Started!      ");
		System.out.println("----------------------------------------------------------------");

		// setup simulators
		mockSDNCAdapter_500("SvcAction>query");
		MockGetNetworkByName_404("CreateNetworkV2/createNetwork_queryName_AAIResponse_Success.xml", "myOwn_Network");
		MockGetNetworkCloudRegion("CreateNetworkV2/cloudRegion25_AAIResponse_Success.xml", "RDM2WAGPLCP");
		MockUpdateRequestDB("DBUpdateResponse.xml");
		//MockNodeQueryServiceInstanceById("f70e927b-6087-4974-9ef8-c5e4d5847ca4", "CreateNetworkV2/createNetwork_queryInstance_Success.xml", "v8");
		MockNodeQueryServiceInstanceById("f70e927b-6087-4974-9ef8-c5e4d5847ca4", "CreateNetworkV2/createNetwork_queryInstance_Success.xml");

		Map<String, String> variables = setupVariables2();
		//WorkflowResponse workflowResponse = executeAsyncWorkflow(processEngineRule, "CreateNetworkInstance", variables);
		executeAsyncWorkflow(processEngineRule, "CreateNetworkInstance", variables);
		//waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

	    assertEquals("false", getVariable(processEngineRule, "CreateNetworkInstance", "CRENI_Success"));
	    assertEquals("false", getVariable(processEngineRule, "DoCreateNetworkInstance", "CRENWKI_Success"));
	    Assert.assertNotNull("CRENI_FalloutHandlerRequest - ", getVariable(processEngineRule, "CreateNetworkInstance", "CRENI_FalloutHandlerRequest"));

		System.out.println("---------------------------------------------------------");
		System.out.println("     SNDC Failure - CreateNetworkInstance flow Completed ");
		System.out.println("---------------------------------------------------------");

	}

	@Test
	//@Ignore
	@Deployment(resources = {"process/CreateNetworkInstance.bpmn",
							 "subprocess/DoCreateNetworkInstance.bpmn",
							 "subprocess/DoCreateNetworkInstanceRollback.bpmn",
							 "subprocess/GenericGetService.bpmn",
							 "subprocess/FalloutHandler.bpmn",
							 "subprocess/CompleteMsoProcess.bpmn",
							 "subprocess/SDNCAdapterV1.bpmn"})

	public void shouldInvokeServiceCreateNetworkInstance_queryServiceInstance404() throws Exception {

		System.out.println("----------------------------------------------------------------------------------");
		System.out.println(" Query ServiceIntance Not found - CreateNetworkInstance flow Started! ");
		System.out.println("----------------------------------------------------------------------------------");
	
		//setup simulators
		mockSDNCAdapter_500("SvcAction>query");
		MockGetNetworkByName_404("CreateNetworkV2/createNetwork_queryName_AAIResponse_Success.xml", "myOwn_Network");
		MockUpdateRequestDB("DBUpdateResponse.xml");
		MockNodeQueryServiceInstanceById_404("f70e927b-6087-4974-9ef8-c5e4d5847ca4");
	
		Map<String, String> variables = setupVariables2();
		//WorkflowResponse workflowResponse = executeAsyncWorkflow(processEngineRule, "CreateNetworkInstance", variables);
		executeAsyncWorkflow(processEngineRule, "CreateNetworkInstance", variables);
		//waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());
	
		assertEquals("false", getVariable(processEngineRule, "CreateNetworkInstance", "CRENI_Success"));
		assertEquals("false", getVariable(processEngineRule, "DoCreateNetworkInstance", "CRENWKI_Success"));
		Assert.assertNotNull("CRENI_FalloutHandlerRequest - ", getVariable(processEngineRule, "CreateNetworkInstance", "CRENI_FalloutHandlerRequest"));
	
		System.out.println("---------------------------------------------------------------------------------");
		System.out.println(" Query ServiceIntance Not found - CreateNetworkInstance flow Completed ");
		System.out.println("---------------------------------------------------------------------------------");

	}	
	
	@Test
	//@Ignore
	@Deployment(resources = {"process/CreateNetworkInstance.bpmn",
							 "subprocess/DoCreateNetworkInstance.bpmn",
							 "subprocess/DoCreateNetworkInstanceRollback.bpmn",
							 "subprocess/GenericGetService.bpmn",
							 "subprocess/FalloutHandler.bpmn",
							 "subprocess/CompleteMsoProcess.bpmn",
            				 "subprocess/SDNCAdapterV1.bpmn"})

	public void shouldInvokeServiceCreateNetworkInstance_VID_Success1() throws Exception {

		System.out.println("----------------------------------------------------------");
		System.out.println("  Success VID1 - CreateNetworkInstance flow Started!      ");
		System.out.println("----------------------------------------------------------");

		// setup simulators
		mockSDNCAdapterTopology("CreateNetworkV2mock/sdncCreateNetworkTopologySimResponse.xml", "SvcAction>assign");
		MockNetworkAdapterPost("CreateNetworkV2/createNetworkResponse_Success.xml", "createNetworkRequest");
		MockGetNetworkByName("MNS-25180-L-01-dmz_direct_net_1", "CreateNetworkV2/createNetwork_queryName_AAIResponse_Success.xml");
		MockGetNetworkCloudRegion("CreateNetworkV2/cloudRegion25_AAIResponse_Success.xml", "RDM2WAGPLCP");
		MockGetNetworkByIdWithDepth("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "CreateNetworkV2/createNetwork_queryNetworkId_AAIResponse_Success.xml", "1");
		MockGetNetworkVpnBinding("CreateNetworkV2/createNetwork_queryVpnBinding_AAIResponse_Success.xml", "85f015d0-2e32-4c30-96d2-87a1a27f8017");
		MockGetNetworkVpnBinding("CreateNetworkV2/createNetwork_queryVpnBinding_AAIResponse_Success.xml", "c980a6ef-3b88-49f0-9751-dbad8608d0a6");
		MockGetNetworkPolicy("CreateNetworkV2/createNetwork_queryNetworkPolicy_AAIResponse_Success.xml", "cee6d136-e378-4678-a024-2cd15f0ee0cg");
		MockGetNetworkTableReference("CreateNetworkV2/createNetwork_queryNetworkTableRef1_AAIResponse_Success.xml", "refFQDN1");
		MockGetNetworkTableReference("CreateNetworkV2/createNetwork_queryNetworkTableRef2_AAIResponse_Success.xml", "refFQDN2");
		MockPutNetworkIdWithDepth("CreateNetworkV2/createNetwork_updateContrail_AAIResponse_Success.xml", "49c86598-f766-46f8-84f8-8d1c1b10f9b4", "1");
		MockUpdateRequestDB("DBUpdateResponse.xml");
		//MockNodeQueryServiceInstanceById("f70e927b-6087-4974-9ef8-c5e4d5847ca4", "CreateNetworkV2/createNetwork_queryInstance_Success.xml", "v8");
		MockNodeQueryServiceInstanceById("f70e927b-6087-4974-9ef8-c5e4d5847ca4", "CreateNetworkV2/createNetwork_queryInstance_Success.xml");

		Map<String, String> variables = setupVariablesVID1();
		//WorkflowResponse workflowResponse = executeAsyncWorkflow(processEngineRule, "CreateNetworkInstance", variables);
		executeAsyncWorkflow(processEngineRule, "CreateNetworkInstance", variables);
		System.out.println("----------------------------------------------------------");
		System.out.println("- got workflow response -");
		System.out.println("----------------------------------------------------------");
		//waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

	    assertEquals("true", getVariable(processEngineRule, "CreateNetworkInstance", "CRENI_Success"));
	    Assert.assertNotNull("CRENI_CompleteMsoProcessRequest - ", getVariable(processEngineRule, "CreateNetworkInstance", "CRENI_CompleteMsoProcessRequest"));

		String workflowResp = BPMNUtil.getVariable(processEngineRule, "CreateNetworkInstance", "WorkflowResponse");
		Assert.assertNotNull(workflowResp);

		System.out.println("-----------------------------------------------------------");
		System.out.println("  Success VID1 - CreateNetworkInstanceInfra flow Completed ");
		System.out.println("-----------------------------------------------------------");

	}

	// *****************
	// Utility Section
	// *****************

	String networkModelInfo =
		       "  {\"modelUuid\": \"mod-inst-uuid-123\", " + '\n' +
               "   \"modelName\": \"mod_inst_z_123\", " + '\n' +
		       "   \"modelVersion\": \"mod-inst-uuid-123\", " + '\n' +
		       "   \"modelCustomizationUuid\": \"z_network_123\", " + '\n' +
		       "   \"modelInvariantUuid\": \"mod-invar-uuid-123\" " + '\n' +
		       "  }";

	String serviceModelInfo =
		       "  {\"modelUuid\": \"36a3a8ea-49a6-4ac8-b06c-89a54544b9b6\", " + '\n' +
               "   \"modelName\": \"HNGW Protected OAM\", " + '\n' +
		       "   \"modelVersion\": \"1.0\", " + '\n' +
		       "   \"modelInvariantUuid\": \"fcc85cb0-ad74-45d7-a5a1-17c8744fdb71\" " + '\n' +
		       "  }";

	// Success Scenario
	private Map<String, String> setupVariables1() {
		Map<String, String> variables = new HashMap<>();
		variables.put("testMessageId", "88f65519-9a38-4c4b-8445-9eb4a5a5af56");
		variables.put("msoRequestId", "testRequestId");
		variables.put("requestId", "testRequestId");
		variables.put("serviceInstanceId", "f70e927b-6087-4974-9ef8-c5e4d5847ca4");
		variables.put("networkId", "networkId");
		variables.put("networkName", "MNS-25180-L-01-dmz_direct_net_1");
		variables.put("lcpCloudRegionId", "RDM2WAGPLCP");
		variables.put("tenantId", "7dd5365547234ee8937416c65507d266");
		variables.put("productFamilyId", "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb");
		variables.put("disableRollback", "false"); // macro
		variables.put("failIfExists", "false");
		variables.put("sdncVersion", "1702");
		variables.put("subscriptionServiceType", "MSO-dev-service-type");
		variables.put("globalSubscriberId", "globalId_45678905678");
		variables.put("networkModelInfo", networkModelInfo);
		variables.put("serviceModelInfo", serviceModelInfo);


		return variables;

	}

	// Success Scenario 2
	private Map<String, String> setupVariables2() {
		Map<String, String> variables = new HashMap<>();
		variables.put("testMessageId", "88f65519-9a38-4c4b-8445-9eb4a5a5af56");
		variables.put("msoRequestId", "testRequestId");
		variables.put("serviceInstanceId", "f70e927b-6087-4974-9ef8-c5e4d5847ca4");
		variables.put("networkId", "networkId");
		variables.put("networkName", "myOwn_Network");  // Name Not found in AA&I
		variables.put("lcpCloudRegionId", "RDM2WAGPLCP");
		variables.put("tenantId", "7dd5365547234ee8937416c65507d266");
		variables.put("productFamilyId", "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb");
		variables.put("disableRollback", "false");  // 1702
		variables.put("failIfExists", "false");
		//variables.put("sdncVersion", "1702");
		variables.put("sdncVersion", "1707");
		variables.put("subscriptionServiceType", "MSO-dev-service-type");
		variables.put("globalSubscriberId", "globalId_45678905678");
		variables.put("networkModelInfo", networkModelInfo);
		variables.put("serviceModelInfo", serviceModelInfo);

		return variables;

	}

	// Active Scenario
	private Map<String, String> setupVariablesActive() {
		Map<String, String> variables = new HashMap<>();
		variables.put("testMessageId", "88f65519-9a38-4c4b-8445-9eb4a5a5af56");
		variables.put("msoRequestId", "testRequestId");
		variables.put("serviceInstanceId", "f70e927b-6087-4974-9ef8-c5e4d5847ca4");
		variables.put("networkId", "networkId");
		variables.put("networkName", "MNS-25180-L-01-dmz_direct_net_2");   // Unique name for Active
		variables.put("lcpCloudRegionId", "RDM2WAGPLCP");
		variables.put("tenantId", "7dd5365547234ee8937416c65507d266");
		variables.put("productFamilyId", "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb");
		variables.put("suppressRollback", "false");
		variables.put("disableRollback", "false");
		variables.put("failIfExists", "false");
		variables.put("sdncVersion", "1702");
		variables.put("subscriptionServiceType", "MSO-dev-service-type");
		variables.put("globalSubscriberId", "globalId_45678905678");
		variables.put("networkModelInfo", networkModelInfo);
		variables.put("serviceModelInfo", serviceModelInfo);

		return variables;

	}

	// Missing Name Scenario
	private Map<String, String> setupVariablesMissingName() {
		Map<String, String> variables = new HashMap<>();
		//variables.put("bpmnRequest", getCreateNetworkRequestMissingName());
		variables.put("testMessageId", "88f65519-9a38-4c4b-8445-9eb4a5a5af56");
		variables.put("msoRequestId", "testRequestId");
		variables.put("serviceInstanceId", "f70e927b-6087-4974-9ef8-c5e4d5847ca4");
		variables.put("networkId", "networkId");
		// variables.put("networkName", "MNS-25180-L-01-dmz_direct_net_2");  // Missing 'name' variable
		// variables.put("networkName", "");                                 // Missing 'value' of name variable
		variables.put("modelName", "CONTRAIL_EXTERNAL");
		variables.put("cloudConfiguration", "RDM2WAGPLCP");
		variables.put("tenantId", "7dd5365547234ee8937416c65507d266");
		variables.put("productFamilyId", "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb");
		variables.put("suppressRollback", "true");
		variables.put("failIfExists", "false");

		return variables;

	}

	// SDNC Rollback Scenario
	private Map<String, String> setupVariablesSDNCRollback() {
		Map<String, String> variables = new HashMap<>();
		variables.put("testMessageId", "88f65519-9a38-4c4b-8445-9eb4a5a5af56");
		variables.put("msoRequestId", "testRequestId");
		variables.put("serviceInstanceId", "f70e927b-6087-4974-9ef8-c5e4d5847ca4");
		variables.put("networkId", "networkId");
		variables.put("networkName", "MNS-25180-L-01-dmz_direct_net_3");  // Unique name for Rollback
		variables.put("modelName", "CONTRAIL_EXTERNAL");
		variables.put("cloudConfiguration", "RDM2WAGPLCP");
		variables.put("tenantId", "7dd5365547234ee8937416c65507d266");
		variables.put("productFamilyId", "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb");
		variables.put("suppressRollback", "true");
		variables.put("disableRollback", "false");

		return variables;

	}

	// old
	public String getCreateNetworkRequestActive() {

		String request =
				"{ \"requestDetails\": { " + '\n' +
				"      \"modelInfo\": { " + '\n' +
				"         \"modelType\": \"modelType\", " + '\n' +
				"         \"modelId\": \"modelId\", " + '\n' +
				"         \"modelCustomizationUuid\": \"modelCustUuid\", " + '\n' +
				"         \"modelName\": \"CONTRAIL_EXTERNAL\", " + '\n' +
				"         \"modelVersion\": \"1\" " + '\n' +
				"      }, " + '\n' +
				"      \"cloudConfiguration\": { " + '\n' +
				"          \"lcpCloudRegionId\": \"RDM2WAGPLCP\", " + '\n' +
				"          \"tenantId\": \"88a6ca3ee0394ade9403f075db23167e\" " + '\n' +
				"      }, " + '\n' +
				"      \"requestInfo\": { " + '\n' +
				"          \"instanceName\": \"MNS-25180-L-01-dmz_direct_net_2\", " + '\n' +
				"          \"source\": \"VID\", " + '\n' +
				"          \"callbackUrl\": \"\", " + '\n' +
				"          \"suppressRollback\": \"false\" ," + '\n' +
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


	// VID json input
	private Map<String, String> setupVariablesVID1() {
		Map<String, String> variables = new HashMap<>();
		variables.put("bpmnRequest", getCreateNetworkRequestVID1());
		variables.put("mso-request-id", "testRequestId");
		//variables.put("msoRequestId", "testRequestId");
		variables.put("requestId", "testRequestId");
		variables.put("isBaseVfModule", "true");
		variables.put("recipeTimeout", "0");
		variables.put("requestAction", "CREATE");
		variables.put("serviceInstanceId", "f70e927b-6087-4974-9ef8-c5e4d5847ca4");
		variables.put("vnfId", "");
		variables.put("vfModuleId", "");
		variables.put("volumeGroupId", "");
		//variables.put("networkId", "networkId");
		variables.put("serviceType", "vMOG");
		variables.put("vfModuleType", "");
		variables.put("networkType", "modelName");

		return variables;

	}

	public String getCreateNetworkRequestVID1() {

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
				"          \"backoutOnFailure\": false, " + '\n' +
				"          \"serviceId\": \"a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb\", " + '\n' +
				"          \"userParams\": [] " + '\n' +
				"      }	" + '\n' +
			    " } " + '\n' +
			    "}";

		return request;
	}

}
