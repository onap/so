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

package org.openecomp.mso.bpmn.infrastructure;

import static org.junit.Assert.assertEquals;
import static org.openecomp.mso.bpmn.common.BPMNUtil.executeAsyncWorkflow;
import static org.openecomp.mso.bpmn.common.BPMNUtil.getVariable;
import static org.openecomp.mso.bpmn.common.BPMNUtil.waitForWorkflowToFinish;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetCloudRegion;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetNetwork;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetNetworkPolicy;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetNetworkRouteTable;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetNetworkVpnBinding;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockNodeQueryServiceInstanceById;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPutNetwork;
import static org.openecomp.mso.bpmn.mock.StubResponseDatabase.mockUpdateRequestDB;
import static org.openecomp.mso.bpmn.mock.StubResponseNetworkAdapter.MockPutNetworkAdapter;
import static org.openecomp.mso.bpmn.mock.StubResponseSDNCAdapter.mockSDNCAdapter;
import static org.openecomp.mso.bpmn.mock.StubResponseSDNCAdapter.mockSDNCAdapterTopology;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.BPMNUtil;
import org.openecomp.mso.bpmn.common.SDNCAdapterCallbackRule;
import org.openecomp.mso.bpmn.common.WorkflowTest;
import org.openecomp.mso.bpmn.common.WorkflowTestTransformer;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowResponse;
import org.openecomp.mso.bpmn.mock.SDNCAdapterNetworkTopologyMockTransformer;

import com.github.tomakehurst.wiremock.extension.ResponseTransformer;

/**
 * Unit test cases for UpdateNetworkInstanceInfra.bpmn
 *
 */
public class UpdateNetworkInstanceInfraTest extends WorkflowTest {
	@WorkflowTestTransformer
	public static final ResponseTransformer sdncAdapterMockTransformer =
		new SDNCAdapterNetworkTopologyMockTransformer();

	@Rule
	public final SDNCAdapterCallbackRule sdncAdapterCallbackRule =
		new SDNCAdapterCallbackRule(processEngineRule);

	/**
	 * End-to-End flow - Unit test for UpdateNetworkInstanceInfra.bpmn
	 *  - String input & String response
	 */

	@Test
	//@Ignore 
	@Deployment(resources = {"process/UpdateNetworkInstanceInfra.bpmn",
			                 "subprocess/FalloutHandler.bpmn",
	                         "subprocess/CompleteMsoProcess.bpmn",
	                         "subprocess/GenericGetService.bpmn",
	                         "subprocess/SDNCAdapterV1.bpmn"})

	public void shouldInvokeServiceUpdateNetworkInstanceInfra_Success1() throws Exception {

		System.out.println("----------------------------------------------------------");
		System.out.println("            Success1 - UpdateNetworkInstanceInfra flow Started!       ");
		System.out.println("----------------------------------------------------------");

		// setup simulators

		mockSDNCAdapterTopology("UpdateNetworkV2mock/sdncUpdateNetworkTopologySimResponse.xml", "SvcAction>changeassign");  //
		MockPutNetworkAdapter("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "updateNetworkRequest", 200, "UpdateNetworkV2/updateNetworkResponse_Success.xml");
		MockGetCloudRegion("RDM2WAGPLCP", 200, "CreateNetworkV2/cloudRegion25_AAIResponse_Success.xml");
		MockGetNetwork("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "UpdateNetworkV2/updateNetwork_queryNetworkId_AAIResponse_Success.xml", 200);
		MockGetNetworkVpnBinding("85f015d0-2e32-4c30-96d2-87a1a27f8017", "UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", 200);
		MockGetNetworkVpnBinding("c980a6ef-3b88-49f0-9751-dbad8608d0a6", "UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", 200);
		MockGetNetworkPolicy("cee6d136-e378-4678-a024-2cd15f0ee0cg", "UpdateNetworkV2/updateNetwork_queryNetworkPolicy_AAIResponse_Success.xml", 200);
		MockGetNetworkRouteTable("refFQDN1", "UpdateNetworkV2/updateNetwork_queryNetworkTableRef1_AAIResponse_Success.xml", 200);
		MockGetNetworkRouteTable("refFQDN2", "UpdateNetworkV2/updateNetwork_queryNetworkTableRef2_AAIResponse_Success.xml", 200);
		MockPutNetwork("49c86598-f766-46f8-84f8-8d1c1b10f9b4", 200, "UpdateNetworkV2/updateNetwork_updateContrail_AAIResponse_Success.xml");
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		MockNodeQueryServiceInstanceById("f70e927b-6087-4974-9ef8-c5e4d5847ca4", "UpdateNetworkV2/updateNetwork_queryInstance_Success.xml");

		Map<String, String> variables = setupVariables1();
		WorkflowResponse workflowResponse = executeAsyncWorkflow(processEngineRule, "UpdateNetworkInstanceInfra", variables);
		System.out.println("----------------------------------------------------------");
		System.out.println("- got workflow response -");
		System.out.println("----------------------------------------------------------");
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

	    assertEquals("true", getVariable(processEngineRule, "UpdateNetworkInstanceInfra", "UPDNETI_Success"));
	    Assert.assertNotNull("UPDNETI_CompleteMsoProcessRequest - ", getVariable(processEngineRule, "UpdateNetworkInstanceInfra", "UPDNETI_CompleteMsoProcessRequest"));

		String workflowResp = BPMNUtil.getVariable(processEngineRule, "UpdateNetworkInstanceInfra", "WorkflowResponse");
		Assert.assertNotNull(workflowResp);

		System.out.println("----------------------------------------------------------");
		System.out.println("     Success1 - UpdateNetworkInstanceInfra flow Completed      ");
		System.out.println("----------------------------------------------------------");

	}

	@Test
	//@Ignore 
	@Deployment(resources = {"process/UpdateNetworkInstanceInfra.bpmn",
			 				 "subprocess/GenericGetService.bpmn",
			                 "subprocess/FalloutHandler.bpmn",
	                         "subprocess/CompleteMsoProcess.bpmn",
	                         "subprocess/SDNCAdapterV1.bpmn"})

	public void shouldInvokeServiceUpdateNetworkInstanceInfra_Success2() throws Exception {

		System.out.println("----------------------------------------------------------");
		System.out.println("            Success2 - UpdateNetworkInstanceInfra flow Started!      ");
		System.out.println("----------------------------------------------------------");

		// setup simulators

		mockSDNCAdapterTopology("UpdateNetworkV2mock/sdncUpdateNetworkTopologySimResponse.xml", "SvcAction>changeassign");
		MockPutNetworkAdapter("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "updateNetworkRequest", 200, "UpdateNetworkV2/updateNetworkResponse_Success.xml");
		MockGetCloudRegion("RDM2WAGPLCP", 200, "CreateNetworkV2/cloudRegion25_AAIResponse_Success.xml");
		MockGetNetwork("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "UpdateNetworkV2/updateNetwork_queryNetworkId_AAIResponse_Success.xml", 200);
		MockGetNetworkVpnBinding("85f015d0-2e32-4c30-96d2-87a1a27f8017", "UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", 200);
		MockGetNetworkVpnBinding("c980a6ef-3b88-49f0-9751-dbad8608d0a6", "UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", 200);
		MockGetNetworkPolicy("cee6d136-e378-4678-a024-2cd15f0ee0cg", "UpdateNetworkV2/updateNetwork_queryNetworkPolicy_AAIResponse_Success.xml", 200);
		MockGetNetworkRouteTable("refFQDN1", "UpdateNetworkV2/updateNetwork_queryNetworkTableRef1_AAIResponse_Success.xml", 200);
		MockGetNetworkRouteTable("refFQDN2", "UpdateNetworkV2/updateNetwork_queryNetworkTableRef2_AAIResponse_Success.xml", 200);
		MockPutNetwork("49c86598-f766-46f8-84f8-8d1c1b10f9b4", 200, "UpdateNetworkV2/updateNetwork_updateContrail_AAIResponse_Success.xml");
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		MockNodeQueryServiceInstanceById("f70e927b-6087-4974-9ef8-c5e4d5847ca4", "UpdateNetworkV2/updateNetwork_queryInstance_Success.xml");

		Map<String, String> variables = setupVariables2();
		WorkflowResponse workflowResponse = executeAsyncWorkflow(processEngineRule, "UpdateNetworkInstanceInfra", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

	    assertEquals("true", getVariable(processEngineRule, "UpdateNetworkInstanceInfra", "UPDNETI_Success"));
	    Assert.assertNotNull("UPDNETI_CompleteMsoProcessRequest - ", getVariable(processEngineRule, "UpdateNetworkInstanceInfra", "UPDNETI_CompleteMsoProcessRequest"));

	    String completeMsoProcessRequest =
	    		"<aetgt:MsoCompletionRequest xmlns:aetgt=\"http://org.openecomp/mso/workflow/schema/v1\""  + '\n'
	    	  + "                            xmlns:ns=\"http://org.openecomp/mso/request/types/v1\""  + '\n'
	    	  + "                            xmlns=\"http://org.openecomp/mso/infra/vnf-request/v1\">"  + '\n'
	    	  + "   <request-info>"  + '\n'
	    	  + "      <request-id>testRequestId</request-id>"  + '\n'
	    	  + "      <action>UPDATE</action>"  + '\n'
	    	  + "      <source>VID</source>"  + '\n'
	    	  + "   </request-info>"  + '\n'
	    	  + "   <aetgt:mso-bpel-name>BPMN Network action: UPDATE</aetgt:mso-bpel-name>" + '\n'
	    	  + "</aetgt:MsoCompletionRequest>";

	    Assert.assertEquals(completeMsoProcessRequest, getVariable(processEngineRule, "UpdateNetworkInstanceInfra", "UPDNETI_CompleteMsoProcessRequest"));

		System.out.println("----------------------------------------------------------");
		System.out.println("     Success2 - UpdateNetworkInstanceInfra flow Completed     ");
		System.out.println("----------------------------------------------------------");

	}


	@Test
	@Deployment(resources = {"process/UpdateNetworkInstanceInfra.bpmn",
			 				 "subprocess/GenericGetService.bpmn",
			                 "subprocess/FalloutHandler.bpmn",
	                         "subprocess/CompleteMsoProcess.bpmn",
	                         "subprocess/SDNCAdapterV1.bpmn"})

	public void shouldInvokeServiceUpdateNetworkInstanceInfra_MissingNetworkId() throws Exception {

		System.out.println("--------------------------------------------------------------------");
		System.out.println("     Missing networkId - UpdateNetworkInstanceInfra flow Started!   ");
		System.out.println("--------------------------------------------------------------------");

		// setup simulators

		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		MockNodeQueryServiceInstanceById("f70e927b-6087-4974-9ef8-c5e4d5847ca4", "UpdateNetworkV2/updateNetwork_queryInstance_Success.xml");

		Map<String, String> variables = setupVariablesMissingNetworkId();
		WorkflowResponse workflowResponse = executeAsyncWorkflow(processEngineRule, "UpdateNetworkInstanceInfra", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

	    assertEquals("false", getVariable(processEngineRule, "UpdateNetworkInstanceInfra", "UPDNETI_Success"));
	    Assert.assertNotNull("UPDNETI_FalloutHandlerRequest - ", getVariable(processEngineRule, "UpdateNetworkInstanceInfra", "UPDNETI_FalloutHandlerRequest"));
	    
	    String falloutHandlerActual = getVariable(processEngineRule, "UpdateNetworkInstanceInfra", "UPDNETI_FalloutHandlerRequest");	
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
"							<aetgt:ErrorCode>2500</aetgt:ErrorCode>" + "\n" +
"						</aetgt:WorkflowException>" + "\n" +
"					</aetgt:FalloutHandlerRequest>"; 
	    		
		assertEquals("Response", falloutHandlerExpected, falloutHandlerActual);

		System.out.println("------------------------------------------------------------------");
		System.out.println("    Missing networkId - UpdateNetworkInstanceInfra flow Completed ");
		System.out.println("------------------------------------------------------------------");

	}

	/* NOT NEEDED
	@Test
	@Deployment(resources = {"process/UpdateNetworkInstanceInfra.bpmn",
			 				 "subprocess/GenericGetService.bpmn",
			                 "subprocess/FalloutHandler.bpmn",
	                         "subprocess/CompleteMsoProcess.bpmn",
	                         "subprocess/SDNCAdapterV1.bpmn"})

	public void shouldInvokeServiceUpdateNetworkInstanceInfra_SDNCRollback1() throws Exception {

		System.out.println("----------------------------------------------------------");
		System.out.println("       SDNCRollback1 - UpdateNetworkInstanceInfra flow Started!       ");
		System.out.println("----------------------------------------------------------");

		// setup simulators

		mockSDNCAdapterTopology("UpdateNetworkV2mock/sdncUpdateNetworkTopologySimResponse.xml", "SvcAction>changeassign");
		mockSDNCAdapterTopology("UpdateNetworkV2mock/sdncUpdateNetworkTopologySimResponse.xml", "SvcAction>rollback");
		MockPutNetworkAdapter("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "updateNetworkRequest", 200, "UpdateNetworkV2/updateNetworkResponse_Success.xml");
		MockGetCloudRegion("RDM2WAGPLCP", 200, "CreateNetworkV2/cloudRegion25_AAIResponse_Success.xml");
		MockAAIResponse_queryId_UpdateNetwork_404V2();            // failure in queryId in AAI
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		MockNodeQueryServiceInstanceById("f70e927b-6087-4974-9ef8-c5e4d5847ca4", "UpdateNetworkV2/updateNetwork_queryInstance_Success.xml");

		Map<String, String> variables = setupVariablesSDNCRollback();
		WorkflowResponse workflowResponse = executeAsyncWorkflow(processEngineRule, "UpdateNetworkInstanceInfra", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

	    assertEquals("false", getVariable(processEngineRule, "UpdateNetworkInstanceInfra", "UPDNETI_Success"));
	    Assert.assertNotNull("UPDNETI_FalloutHandlerRequest - ", getVariable(processEngineRule, "UpdateNetworkInstanceInfra", "UPDNETI_FalloutHandlerRequest"));

		System.out.println("----------------------------------------------------------");
		System.out.println("   SDNCRollback1 - UpdateNetworkInstanceInfra flow Completed   ");
		System.out.println("----------------------------------------------------------");

	} */

	@Test
	@Deployment(resources = {"process/UpdateNetworkInstanceInfra.bpmn",
			 				 "subprocess/GenericGetService.bpmn",
			                 "subprocess/FalloutHandler.bpmn",
	                         "subprocess/CompleteMsoProcess.bpmn",
	                         "subprocess/SDNCAdapterV1.bpmn"})

	public void shouldInvokeServiceUpdateNetworkInstanceInfra_SDNCRollback_Scenario01() throws Exception {

		System.out.println("----------------------------------------------------------");
		System.out.println("       SDNCRollback - UpdateNetworkInstanceInfra flow Started!       ");
		System.out.println("----------------------------------------------------------");

		// setup simulators

		mockSDNCAdapterTopology("UpdateNetworkV2mock/sdncUpdateNetworkTopologySimResponse.xml", "SvcAction>changeassign");          //
		mockSDNCAdapterTopology("UpdateNetworkV2mock/sdncUpdateNetworkTopologySimResponse.xml", "SvcAction>rollback");
		MockPutNetworkAdapter("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "updateNetworkRequest", 200, "UpdateNetworkV2/updateNetworkResponse_Success.xml");
		MockGetCloudRegion("RDM2WAGPLCP", 200, "CreateNetworkV2/cloudRegion25_AAIResponse_Success.xml");
		MockGetNetwork("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "UpdateNetworkV2/updateNetwork_queryNetworkId_AAIResponse_NoPayload_Success.xml", 200);    // no 'payload' response from NetworkAdapter, version 2
		//MockGetNetworkVpnBinding("85f015d0-2e32-4c30-96d2-87a1a27f8017", "UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", 200);     // let it fail (404) to see SDNC compensation
		//MockGetNetworkVpnBinding("c980a6ef-3b88-49f0-9751-dbad8608d0a6", "UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", 200);	 // let it fail (404) to see SDNC compensation
		//MockGetNetworkRouteTable("refFQDN2", "UpdateNetworkV2/updateNetwork_queryNetworkTableRef2_AAIResponse_Success.xml", 200);
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		MockNodeQueryServiceInstanceById("f70e927b-6087-4974-9ef8-c5e4d5847ca4", "UpdateNetworkV2/updateNetwork_queryInstance_Success.xml");

		Map<String, String> variables = setupVariablesSDNCRollback();
		WorkflowResponse workflowResponse = executeAsyncWorkflow(processEngineRule, "UpdateNetworkInstanceInfra", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

	    assertEquals("false", getVariable(processEngineRule, "UpdateNetworkInstanceInfra", "UPDNETI_Success"));
	    Assert.assertNotNull("UPDNETI_FalloutHandlerRequest - ", getVariable(processEngineRule, "UpdateNetworkInstanceInfra", "UPDNETI_FalloutHandlerRequest"));

		System.out.println("----------------------------------------------------------");
		System.out.println("   SDNCRollback - UpdateNetworkInstanceInfra flow Completed   ");
		System.out.println("----------------------------------------------------------");

	}

	@Test
	@Deployment(resources = {"process/UpdateNetworkInstanceInfra.bpmn",
			 			     "subprocess/GenericGetService.bpmn",
			                 "subprocess/FalloutHandler.bpmn",
	                         "subprocess/CompleteMsoProcess.bpmn",
	                         "subprocess/SDNCAdapterV1.bpmn"})

	public void shouldInvokeServiceUpdateNetworkInstanceInfra_Network_SDNC_Rollback() throws Exception {

		System.out.println("---------------------------------------------------------------");
		System.out.println("    Network and SDNC Rollback - UpdateNetworkInstanceInfra flow Started!       ");
		System.out.println("---------------------------------------------------------------");

		// setup simulators

		mockSDNCAdapterTopology("UpdateNetworkV2mock/sdncUpdateNetworkTopologySimResponse.xml", "SvcAction>changeassign");          //
		mockSDNCAdapterTopology("UpdateNetworkV2mock/sdncUpdateNetworkTopologySimResponse.xml", "SvcAction>rollback");
		MockPutNetworkAdapter("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "updateNetworkRequest", 200, "UpdateNetworkV2/updateNetworkResponse_Success.xml");
		MockGetCloudRegion("RDM2WAGPLCP", 200, "CreateNetworkV2/cloudRegion25_AAIResponse_Success.xml");
		MockGetNetwork("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "UpdateNetworkV2/updateNetwork_queryNetworkId_AAIResponse_Success.xml", 200);
		MockGetNetworkVpnBinding("85f015d0-2e32-4c30-96d2-87a1a27f8017", "UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", 200);
		MockGetNetworkVpnBinding("c980a6ef-3b88-49f0-9751-dbad8608d0a6", "UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", 200);
		MockGetNetworkPolicy("cee6d136-e378-4678-a024-2cd15f0ee0cg", "UpdateNetworkV2/updateNetwork_queryNetworkPolicy_AAIResponse_Success.xml", 200);
		MockGetNetworkRouteTable("refFQDN1", "UpdateNetworkV2/updateNetwork_queryNetworkTableRef1_AAIResponse_Success.xml", 200);
		MockGetNetworkRouteTable("refFQDN2", "UpdateNetworkV2/updateNetwork_queryNetworkTableRef2_AAIResponse_Success.xml", 200);
		//MockAAIResponse_updateContrail_CreateNetwork_SuccessV2();  // designed to fail in AAI Update
		//mockUpdateRequestDB(500, "Database/DBUpdateResponse.xml");
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		MockNodeQueryServiceInstanceById("f70e927b-6087-4974-9ef8-c5e4d5847ca4", "UpdateNetworkV2/updateNetwork_queryInstance_Success.xml");

		Map<String, String> variables = setupVariables1();
		WorkflowResponse workflowResponse = executeAsyncWorkflow(processEngineRule, "UpdateNetworkInstanceInfra", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

	    assertEquals("false", getVariable(processEngineRule, "UpdateNetworkInstanceInfra", "UPDNETI_Success"));
	    Assert.assertNotNull("UPDNETI_FalloutHandlerRequest - ", getVariable(processEngineRule, "UpdateNetworkInstanceInfra", "UPDNETI_FalloutHandlerRequest"));

		System.out.println("---------------------------------------------------------------------");
		System.out.println(" Network and SCNC Rollback - UpdateNetworkInstanceInfra flow Completed   ");
		System.out.println("---------------------------------------------------------------------");

	}

	@Test
	@Deployment(resources = {"process/UpdateNetworkInstanceInfra.bpmn",
			 				 "subprocess/GenericGetService.bpmn",
			                 "subprocess/FalloutHandler.bpmn",
	                         "subprocess/CompleteMsoProcess.bpmn",
	                         "subprocess/SDNCAdapterV1.bpmn"})

	public void shouldInvokeServiceUpdateNetworkInstanceInfra_Network_SDNC_Rollback_Failed() throws Exception {

		System.out.println("---------------------------------------------------------------");
		System.out.println("    Network and SDNC Rollback - UpdateNetworkInstanceInfra flow Started!       ");
		System.out.println("---------------------------------------------------------------");

		// setup simulators

		mockSDNCAdapterTopology("UpdateNetworkV2mock/sdncUpdateNetworkTopologySimResponse.xml", "SvcAction>changeassign");          //
		mockSDNCAdapterTopology("UpdateNetworkV2mock/sdncUpdateNetworkTopologySim500Response.xml", "SvcAction>rollback");
		MockPutNetworkAdapter("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "updateNetworkRequest", 200, "UpdateNetworkV2/updateNetworkResponse_Success.xml");
		MockGetCloudRegion("RDM2WAGPLCP", 200, "CreateNetworkV2/cloudRegion25_AAIResponse_Success.xml");
		MockGetNetwork("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "UpdateNetworkV2/updateNetwork_queryNetworkId_AAIResponse_Success.xml", 200);
		MockGetNetworkVpnBinding("85f015d0-2e32-4c30-96d2-87a1a27f8017", "UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", 200);
		MockGetNetworkVpnBinding("c980a6ef-3b88-49f0-9751-dbad8608d0a6", "UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", 200);
		MockGetNetworkPolicy("cee6d136-e378-4678-a024-2cd15f0ee0cg", "UpdateNetworkV2/updateNetwork_queryNetworkPolicy_AAIResponse_Success.xml", 200);
		MockGetNetworkRouteTable("refFQDN1", "UpdateNetworkV2/updateNetwork_queryNetworkTableRef1_AAIResponse_Success.xml", 200);
		MockGetNetworkRouteTable("refFQDN2", "UpdateNetworkV2/updateNetwork_queryNetworkTableRef2_AAIResponse_Success.xml", 200);
		//MockAAIResponse_updateContrail_CreateNetwork_SuccessV2();  // designed to fail in AAI Update
		//mockUpdateRequestDB(500, "Database/DBUpdateResponse.xml");
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		MockNodeQueryServiceInstanceById("f70e927b-6087-4974-9ef8-c5e4d5847ca4", "UpdateNetworkV2/updateNetwork_queryInstance_Success.xml");

		Map<String, String> variables = setupVariables1();
		WorkflowResponse workflowResponse = executeAsyncWorkflow(processEngineRule, "UpdateNetworkInstanceInfra", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

	    assertEquals("false", getVariable(processEngineRule, "UpdateNetworkInstanceInfra", "UPDNETI_Success"));
	    Assert.assertNotNull("UPDNETI_FalloutHandlerRequest - ", getVariable(processEngineRule, "UpdateNetworkInstanceInfra", "UPDNETI_FalloutHandlerRequest"));

		System.out.println("---------------------------------------------------------------------");
		System.out.println(" Network and SCNC Rollback - UpdateNetworkInstanceInfra flow Completed   ");
		System.out.println("---------------------------------------------------------------------");

	}

	@Test
	@Deployment(resources = {"process/UpdateNetworkInstanceInfra.bpmn",
			                 "subprocess/GenericGetService.bpmn",
			                 "subprocess/FalloutHandler.bpmn",
	                         "subprocess/CompleteMsoProcess.bpmn",
	                         "subprocess/SDNCAdapterV1.bpmn"})

	public void shouldInvokeServiceUpdateNetworkInstanceInfra_SDNCRollback2() throws Exception {

		System.out.println("----------------------------------------------------");
		System.out.println("    SDNCRollback2 - UpdateNetworkInstanceInfra flow Started!    ");
		System.out.println("----------------------------------------------------");

		// setup simulators

		mockSDNCAdapterTopology("UpdateNetworkV2mock/sdncUpdateNetworkTopologySimResponse.xml", "SvcAction>changeassign");
		mockSDNCAdapterTopology("UpdateNetworkV2mock/sdncUpdateNetworkTopologySimResponse.xml", "SvcAction>rollback");
		MockPutNetworkAdapter("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "updateNetworkRequest", 500, "UpdateNetworkV2/updateNetworkResponse_500.xml");            // failure 500 in NetworkAdapter
		MockGetCloudRegion("RDM2WAGPLCP", 200, "CreateNetworkV2/cloudRegion25_AAIResponse_Success.xml");
		MockGetNetwork("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "UpdateNetworkV2/updateNetwork_queryNetworkId_AAIResponse_Success.xml", 200);
		MockGetNetworkVpnBinding("85f015d0-2e32-4c30-96d2-87a1a27f8017", "UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", 200);
		MockGetNetworkVpnBinding("c980a6ef-3b88-49f0-9751-dbad8608d0a6", "UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", 200);
		MockGetNetworkPolicy("cee6d136-e378-4678-a024-2cd15f0ee0cg", "UpdateNetworkV2/updateNetwork_queryNetworkPolicy_AAIResponse_Success.xml", 200);
		MockGetNetworkRouteTable("refFQDN1", "UpdateNetworkV2/updateNetwork_queryNetworkTableRef1_AAIResponse_Success.xml", 200);
		MockGetNetworkRouteTable("refFQDN2", "UpdateNetworkV2/updateNetwork_queryNetworkTableRef2_AAIResponse_Success.xml", 200);
		//MockPutNetwork("49c86598-f766-46f8-84f8-8d1c1b10f9b4", 200, "UpdateNetworkV2/updateNetwork_updateContrail_AAIResponse_Success.xml");
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		MockNodeQueryServiceInstanceById("f70e927b-6087-4974-9ef8-c5e4d5847ca4", "UpdateNetworkV2/updateNetwork_queryInstance_Success.xml");

		Map<String, String> variables = setupVariables1();
		WorkflowResponse workflowResponse = executeAsyncWorkflow(processEngineRule, "UpdateNetworkInstanceInfra", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

	    assertEquals("false", getVariable(processEngineRule, "UpdateNetworkInstanceInfra", "UPDNETI_Success"));
	    Assert.assertNotNull("UPDNETI_FalloutHandlerRequest - ", getVariable(processEngineRule, "UpdateNetworkInstanceInfra", "UPDNETI_FalloutHandlerRequest"));

		System.out.println("------------------------------------------------------------");
		System.out.println(" SDNCRollback2 - UpdateNetworkInstanceInfra flow Completed      ");
		System.out.println("------------------------------------------------------------");

	}

	@Test
	@Deployment(resources = {"process/UpdateNetworkInstanceInfra.bpmn",
			 				 "subprocess/GenericGetService.bpmn",
			                 "subprocess/FalloutHandler.bpmn",
	                         "subprocess/CompleteMsoProcess.bpmn",
	                         "subprocess/SDNCAdapterV1.bpmn"})

	public void shouldInvokeServiceUpdateNetworkInstanceInfra_SDNCRollback3() throws Exception {

		System.out.println("----------------------------------------------------");
		System.out.println("    SDNCRollback3 - UpdateNetworkInstanceInfra flow Started!    ");
		System.out.println("----------------------------------------------------");

		// setup simulators

		mockSDNCAdapterTopology("UpdateNetworkV2mock/sdncUpdateNetworkTopologySimResponse.xml", "SvcAction>changeassign");
		mockSDNCAdapterTopology("UpdateNetworkV2mock/sdncUpdateNetworkTopologySimResponse.xml", "SvcAction>rollback");
		MockPutNetworkAdapter("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "updateNetworkRequest", 400, "UpdateNetworkV2/updateNetworkResponse_400.xml");            // failure 400 in NetworkAdapter
		MockGetCloudRegion("RDM2WAGPLCP", 200, "CreateNetworkV2/cloudRegion25_AAIResponse_Success.xml");
		MockGetNetwork("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "UpdateNetworkV2/updateNetwork_queryNetworkId_AAIResponse_Success.xml", 200);
		MockGetNetworkVpnBinding("85f015d0-2e32-4c30-96d2-87a1a27f8017", "UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", 200);
		MockGetNetworkVpnBinding("c980a6ef-3b88-49f0-9751-dbad8608d0a6", "UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", 200);
		MockGetNetworkPolicy("cee6d136-e378-4678-a024-2cd15f0ee0cg", "UpdateNetworkV2/updateNetwork_queryNetworkPolicy_AAIResponse_Success.xml", 200);
		MockGetNetworkRouteTable("refFQDN1", "UpdateNetworkV2/updateNetwork_queryNetworkTableRef1_AAIResponse_Success.xml", 200);
		MockGetNetworkRouteTable("refFQDN2", "UpdateNetworkV2/updateNetwork_queryNetworkTableRef2_AAIResponse_Success.xml", 200);
		//MockPutNetwork("49c86598-f766-46f8-84f8-8d1c1b10f9b4", 200, "UpdateNetworkV2/updateNetwork_updateContrail_AAIResponse_Success.xml");
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		MockNodeQueryServiceInstanceById("f70e927b-6087-4974-9ef8-c5e4d5847ca4", "UpdateNetworkV2/updateNetwork_queryInstance_Success.xml");

		Map<String, String> variables = setupVariables1();
		WorkflowResponse workflowResponse = executeAsyncWorkflow(processEngineRule, "UpdateNetworkInstanceInfra", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

	    assertEquals("false", getVariable(processEngineRule, "UpdateNetworkInstanceInfra", "UPDNETI_Success"));
	    Assert.assertNotNull("UPDNETI_FalloutHandlerRequest - ", getVariable(processEngineRule, "UpdateNetworkInstanceInfra", "UPDNETI_FalloutHandlerRequest"));

		System.out.println("------------------------------------------------------------");
		System.out.println(" SDNCRollback3 - UpdateNetworkInstanceInfra flow Completed      ");
		System.out.println("------------------------------------------------------------");

	}

	@Test
	@Deployment(resources = {"process/UpdateNetworkInstanceInfra.bpmn",
			 				 "subprocess/GenericGetService.bpmn",
			                 "subprocess/FalloutHandler.bpmn",
	                         "subprocess/CompleteMsoProcess.bpmn",
	                         "subprocess/SDNCAdapterV1.bpmn"})

	public void shouldInvokeServiceUpdateNetworkInstanceInfra_PONR() throws Exception {

		System.out.println("--------------------------------------------------------------------");
		System.out.println("    PONR (Point-of-no-Return) - UpdateNetworkInstanceInfra flow Started!       ");
		System.out.println("--------------------------------------------------------------------");

		// setup simulators

		mockSDNCAdapterTopology("UpdateNetworkV2mock/sdncUpdateNetworkTopologySimResponse.xml", "SvcAction>changeassign");          //
		mockSDNCAdapterTopology("UpdateNetworkV2mock/sdncUpdateNetworkTopologySimResponse.xml", "SvcAction>rollback");
		MockPutNetworkAdapter("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "updateNetworkRequest", 200, "UpdateNetworkV2/updateNetworkResponse_Success.xml");
		MockGetCloudRegion("RDM2WAGPLCP", 200, "CreateNetworkV2/cloudRegion25_AAIResponse_Success.xml");
		MockGetNetwork("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "UpdateNetworkV2/updateNetwork_queryNetworkId_AAIResponse_Success.xml", 200);
		MockGetNetworkVpnBinding("85f015d0-2e32-4c30-96d2-87a1a27f8017", "UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", 200);
		MockGetNetworkVpnBinding("c980a6ef-3b88-49f0-9751-dbad8608d0a6", "UpdateNetworkV2/updateNetwork_queryVpnBinding_AAIResponse_Success.xml", 200);
		MockGetNetworkPolicy("cee6d136-e378-4678-a024-2cd15f0ee0cg", "UpdateNetworkV2/updateNetwork_queryNetworkPolicy_AAIResponse_Success.xml", 200);
		MockGetNetworkRouteTable("refFQDN1", "UpdateNetworkV2/updateNetwork_queryNetworkTableRef1_AAIResponse_Success.xml", 200);
		MockGetNetworkRouteTable("refFQDN2", "UpdateNetworkV2/updateNetwork_queryNetworkTableRef2_AAIResponse_Success.xml", 200);
		MockPutNetwork("49c86598-f766-46f8-84f8-8d1c1b10f9b4", 200, "UpdateNetworkV2/updateNetwork_updateContrail_AAIResponse_Success.xml");
		mockUpdateRequestDB(500, "Database/DBUpdateResponse.xml");
		MockNodeQueryServiceInstanceById("f70e927b-6087-4974-9ef8-c5e4d5847ca4", "UpdateNetworkV2/updateNetwork_queryInstance_Success.xml");

		Map<String, String> variables = setupVariables1();
		WorkflowResponse workflowResponse = executeAsyncWorkflow(processEngineRule, "UpdateNetworkInstanceInfra", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

	    assertEquals("false", getVariable(processEngineRule, "UpdateNetworkInstanceInfra", "UPDNETI_Success"));
	    Assert.assertNotNull("UPDNETI_FalloutHandlerRequest - ", getVariable(processEngineRule, "UpdateNetworkInstanceInfra", "UPDNETI_FalloutHandlerRequest"));

	    String falloutHandlerRequest =
	    		  "<aetgt:FalloutHandlerRequest xmlns:aetgt=\"http://org.openecomp/mso/workflow/schema/v1\"" + '\n'
				+ "					                             xmlns:ns=\"http://org.openecomp/mso/request/types/v1\"" + '\n'
				+ "					                             xmlns:wfsch=\"http://org.openecomp/mso/workflow/schema/v1\">" + '\n'
				+ "					   <request-info xmlns=\"http://org.openecomp/mso/infra/vnf-request/v1\">" + '\n'
				+ "					      <request-id>testRequestId</request-id>" + '\n'
				+ "					      <action>CREATE</action>" + '\n'
				+ "					      <source>VID</source>" + '\n'
				+ "					   </request-info>" + '\n'
				+ "						<aetgt:WorkflowException xmlns:aetgt=\"http://org.openecomp/mso/workflow/schema/v1\">" + '\n'
				+ "							<aetgt:ErrorMessage> DB Update failed, code: 500</aetgt:ErrorMessage>" + '\n'
				+ "							<aetgt:ErrorCode>2500</aetgt:ErrorCode>" + '\n'
				+ "						</aetgt:WorkflowException>" + '\n'
				+ "					</aetgt:FalloutHandlerRequest>";

	    System.out.println("Display UPDNETI_FalloutHandlerRequest - " + getVariable(processEngineRule, "UpdateNetworkInstanceInfra", "UPDNETI_FalloutHandlerRequest"));
	    //Assert.assertEquals(falloutHandlerRequest, getVariable(processEngineRule, "UpdateNetworkInstanceInfra", "UPDNETI_FalloutHandlerRequest"));

		System.out.println("--------------------------------------------------------------------");
		System.out.println("    PONR (Point-of-no-Return) - UpdateNetworkInstanceInfra flow Completed!     ");
		System.out.println("--------------------------------------------------------------------");


	}

	@Test
	@Deployment(resources = {"process/UpdateNetworkInstanceInfra.bpmn",
			                 "subprocess/GenericGetService.bpmn",
			                 "subprocess/FalloutHandler.bpmn",
	                         "subprocess/CompleteMsoProcess.bpmn",
	                         "subprocess/SDNCAdapterV1.bpmn"})

	public void shouldInvokeServiceUpdateNetworkInstanceInfra_sdncFailure() throws Exception {

		System.out.println("----------------------------------------------------------");
		System.out.println("        SNDC Failure - UpdateNetworkInstanceInfra flow Started!      ");
		System.out.println("----------------------------------------------------------");

		// setup simulators

		//MockSDNCAdapterBadAsynchronousResponse();                // 404
		mockSDNCAdapter("/SDNCAdapter", "SvcAction>query", 500, "");             // 500
		MockGetCloudRegion("RDM2WAGPLCP", 200, "CreateNetworkV2/cloudRegion25_AAIResponse_Success.xml");
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		MockNodeQueryServiceInstanceById("f70e927b-6087-4974-9ef8-c5e4d5847ca4", "UpdateNetworkV2/updateNetwork_queryInstance_Success.xml");

		Map<String, String> variables = setupVariables2();
		WorkflowResponse workflowResponse = executeAsyncWorkflow(processEngineRule, "UpdateNetworkInstanceInfra", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

	    assertEquals("false", getVariable(processEngineRule, "UpdateNetworkInstanceInfra", "UPDNETI_Success"));
	    Assert.assertNotNull("UPDNETI_CompleteMsoProcessRequest - ", getVariable(processEngineRule, "UpdateNetworkInstanceInfra", "UPDNETI_CompleteMsoProcessRequest"));

		System.out.println("----------------------------------------------------------");
		System.out.println("     SNDC Failure - UpdateNetworkInstanceInfra flow Completed ");
		System.out.println("----------------------------------------------------------");

	}

	// *****************
	// Utility Section
	// *****************

	// Success Scenario
	private Map<String, String> setupVariables1() {
		Map<String, String> variables = new HashMap<String, String>();
		variables.put("bpmnRequest", getCreateNetworkRequest1());
		variables.put("mso-request-id", "testRequestId");
		variables.put("requestId", "testRequestId");
		variables.put("isBaseVfModule", "true");
		variables.put("recipeTimeout", "0");
		variables.put("requestAction", "CREATE");
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
				"          \"instanceName\": \"MNS-25180-L-01-dmz_direct_net_1\", " + '\n' +
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

	// Success Scenario 2
	private Map<String, String> setupVariables2() {
		Map<String, String> variables = new HashMap<String, String>();
		variables.put("bpmnRequest", getCreateNetworkRequest2());
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

	/* Active Scenario
	private Map<String, String> setupVariablesActive() {
		Map<String, String> variables = new HashMap<String, String>();
		variables.put("mso-request-id", "testRequestId");
		variables.put("bpmnRequest", getCreateNetworkRequestActive());
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

	public String getCreateNetworkRequestActive() {

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
				"          \"instanceName\": \"MNS-25180-L-01-dmz_direct_net_2\", " + '\n' +
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

	} */

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

	// SDNC Rollback Scenario
	private Map<String, String> setupVariablesSDNCRollback() {
		Map<String, String> variables = new HashMap<String, String>();
		variables.put("mso-request-id", "testRequestId");
		variables.put("bpmnRequest", getCreateNetworkRequestSDNCRollback());
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
