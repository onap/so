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

import static org.openecomp.mso.bpmn.common.BPMNUtil.executeAsyncWorkflow;
import static org.openecomp.mso.bpmn.common.BPMNUtil.getVariable;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetCloudRegion;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetNetworkByIdWithDepth;
import static org.openecomp.mso.bpmn.mock.StubResponseDatabase.mockUpdateRequestDB;
import static org.openecomp.mso.bpmn.mock.StubResponseNetworkAdapter.MockNetworkAdapter;
import static org.openecomp.mso.bpmn.mock.StubResponseNetworkAdapter.MockNetworkAdapterContainingRequest;
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
import org.openecomp.mso.bpmn.mock.SDNCAdapterNetworkTopologyMockTransformer;

import com.github.tomakehurst.wiremock.extension.ResponseTransformer;


/**
 * Unit test cases for DeleteNetworkInstance.bpmn
 *
 */
//@Ignore
public class DeleteNetworkInstanceTest extends WorkflowTest {
	@WorkflowTestTransformer
	public static final ResponseTransformer sdncAdapterMockTransformer =
		new SDNCAdapterNetworkTopologyMockTransformer();

	@Rule
	public final SDNCAdapterCallbackRule sdncAdapterCallbackRule =
		new SDNCAdapterCallbackRule(processEngineRule);

	/**
	 * End-to-End flow - Unit test for DeleteNetworkInstance.bpmn
	 *  - String input & String response
	 */

	@Test
	//@Ignore
	@Deployment(resources = {"process/DeleteNetworkInstance.bpmn",
							 "subprocess/DoDeleteNetworkInstance.bpmn",
							 "subprocess/DoDeleteNetworkInstanceRollback.bpmn",
			                 "subprocess/FalloutHandler.bpmn",
	                         "subprocess/CompleteMsoProcess.bpmn",
	                         "subprocess/SDNCAdapterV1.bpmn"})

	public void shouldInvokeServiceDeleteNetworkInstance_VID_Success() throws Exception {

		System.out.println("----------------------------------------------------------");
		System.out.println("      Success VID - DeleteNetworkInstance flow Started!   ");
		System.out.println("----------------------------------------------------------");

		// setup simulators
		mockSDNCAdapterTopology("DeleteNetworkV2mock/sdncDeleteNetworkTopologySimResponse.xml", "SvcAction>delete");
		MockNetworkAdapter("bdc5efe8-404a-409b-85f6-0dcc9eebae30", 200, "deleteNetworkResponse_Success.xml");
		MockGetNetworkByIdWithDepth("bdc5efe8-404a-409b-85f6-0dcc9eebae30", "DeleteNetworkV2/deleteNetworkAAIResponse_Success.xml", "all");
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		MockGetCloudRegion("RDM2WAGPLCP", 200, "DeleteNetworkV2/cloudRegion30_AAIResponse_Success.xml");

		Map<String, String> variables = new HashMap<String, String>();
		variables.put("mso-request-id", "testRequestId");
		variables.put("requestId", "testRequestId");
		variables.put("isBaseVfModule", "true");
		variables.put("recipeTimeout", "0");
		variables.put("requestAction", "DELETE");
		variables.put("serviceInstanceId", "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb");
		variables.put("vnfId", "");
		variables.put("vfModuleId", "");
		variables.put("volumeGroupId", "");
		variables.put("networkId", "bdc5efe8-404a-409b-85f6-0dcc9eebae30");
		variables.put("serviceType", "MOG");
		variables.put("vfModuleType", "");
		variables.put("networkType", "modelName");
		variables.put("bpmnRequest", getDeleteNetworkInstanceInfraRequest());

		executeAsyncWorkflow(processEngineRule, "DeleteNetworkInstance", variables);

	    Assert.assertNotNull("DELNI_CompleteMsoProcessRequest - ", getVariable(processEngineRule, "DeleteNetworkInstance", "DELNI_CompleteMsoProcessRequest"));
	    Assert.assertEquals("true", getVariable(processEngineRule, "DeleteNetworkInstance", "DELNI_Success"));

		String workflowResp = BPMNUtil.getVariable(processEngineRule, "DeleteNetworkInstance", "WorkflowResponse");
		Assert.assertNotNull(workflowResp);
		System.out.println("DeleteNetworkInstanceTest.shouldInvokeServiceDeleteNetworkInstance_Success() WorkflowResponse:\n" + workflowResp);

	    String completeMsoProcessRequest =
	    		"<aetgt:MsoCompletionRequest xmlns:aetgt=\"http://org.openecomp/mso/workflow/schema/v1\""  + '\n'
	    	  + "                            xmlns:ns=\"http://org.openecomp/mso/request/types/v1\""  + '\n'
	    	  + "                            xmlns=\"http://org.openecomp/mso/infra/vnf-request/v1\">"  + '\n'
	    	  + "   <request-info>"  + '\n'
	    	  + "      <request-id>testRequestId</request-id>"  + '\n'
	    	  + "      <action>DELETE</action>"  + '\n'
	    	  + "      <source>VID</source>"  + '\n'
	    	  + "   </request-info>"  + '\n'
	    	  + "   <aetgt:status-message>Network has been deleted successfully.</aetgt:status-message>"  + '\n'
	    	  + "   <aetgt:mso-bpel-name>BPMN Network action: DELETE</aetgt:mso-bpel-name>" + '\n'
	    	  + "</aetgt:MsoCompletionRequest>";

	    Assert.assertEquals(completeMsoProcessRequest, getVariable(processEngineRule, "DeleteNetworkInstance", "DELNI_CompleteMsoProcessRequest"));

		System.out.println("----------------------------------------------------------");
		System.out.println("     Success VID - DeleteNetworkInstance flow Completed   ");
		System.out.println("----------------------------------------------------------");


	}

	@Test
	//@Ignore
	@Deployment(resources = {"process/DeleteNetworkInstance.bpmn",
							 "subprocess/DoDeleteNetworkInstance.bpmn",
							 //"subprocess/DoDeleteNetworkInstanceRollback.bpmn",
			                 "subprocess/FalloutHandler.bpmn",
	                         "subprocess/CompleteMsoProcess.bpmn",
	                         "subprocess/SDNCAdapterV1.bpmn"})

	public void shouldInvokeServiceDeleteNetworkInstance_vIPER_Success() throws Exception {

		System.out.println("----------------------------------------------------------");
		System.out.println("      Success vIPER - DeleteNetworkInstance flow Started! ");
		System.out.println("----------------------------------------------------------");

		// setup simulators
		mockSDNCAdapterTopology("DeleteNetworkV2mock/sdncDeleteNetworkTopologySimResponse.xml", "SvcAction>unassign");
		mockSDNCAdapterTopology("DeleteNetworkV2mock/sdncDeleteNetworkTopologySimResponse.xml", "SvcAction>deactivate");
		MockNetworkAdapter("bdc5efe8-404a-409b-85f6-0dcc9eebae30", 200, "deleteNetworkResponse_Success.xml");
		MockGetNetworkByIdWithDepth("bdc5efe8-404a-409b-85f6-0dcc9eebae30", "DeleteNetworkV2/deleteNetworkAAIResponse_Success.xml", "all");
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		MockGetCloudRegion("RDM2WAGPLCP", 200, "DeleteNetworkV2/cloudRegion30_AAIResponse_Success.xml");

		String networkModelInfo = "  {\"modelName\": \"modelName\", " + '\n' +
		                          "   \"networkType\": \"modelName\" }";

		Map<String, String> variables = new HashMap<String, String>();
		variables.put("testMessageId", "88f65519-9a38-4c4b-8445-9eb4a5a5af56");
		variables.put("msoRequestId", "testRequestId");
		variables.put("requestId", "testRequestId");
		variables.put("serviceInstanceId", "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb");
		variables.put("networkId", "bdc5efe8-404a-409b-85f6-0dcc9eebae30");
		variables.put("networkName", "HSL_direct_net_2");
		variables.put("lcpCloudRegionId", "RDM2WAGPLCP");
		variables.put("tenantId", "88a6ca3ee0394ade9403f075db23167e");
		variables.put("productFamilyId", "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb");
		variables.put("disableRollback", "false");  // 1702
		variables.put("failIfExists", "false");
		//variables.put("sdncVersion", "1702");
		variables.put("sdncVersion", "1707");
		variables.put("subscriptionServiceType", "MSO-dev-service-type");
		variables.put("networkModelInfo", networkModelInfo);

		executeAsyncWorkflow(processEngineRule, "DeleteNetworkInstance", variables);

	    Assert.assertNotNull("DELNI_CompleteMsoProcessRequest - ", getVariable(processEngineRule, "DeleteNetworkInstance", "DELNI_CompleteMsoProcessRequest"));
	    Assert.assertEquals("true", getVariable(processEngineRule, "DeleteNetworkInstance", "DELNI_Success"));

		String workflowResp = BPMNUtil.getVariable(processEngineRule, "DeleteNetworkInstance", "WorkflowResponse");
		Assert.assertNotNull(workflowResp);
		System.out.println("DeleteNetworkInstanceTest.shouldInvokeServiceDeleteNetworkInstance_vIPER_Success() WorkflowResponse:\n" + workflowResp);

	    String completeMsoProcessRequest =
	    		"<aetgt:MsoCompletionRequest xmlns:aetgt=\"http://org.openecomp/mso/workflow/schema/v1\""  + '\n'
	    	  + "                            xmlns:ns=\"http://org.openecomp/mso/request/types/v1\""  + '\n'
	    	  + "                            xmlns=\"http://org.openecomp/mso/infra/vnf-request/v1\">"  + '\n'
	    	  + "   <request-info>"  + '\n'
	    	  + "      <request-id>testRequestId</request-id>"  + '\n'
	    	  + "      <action>DELETE</action>"  + '\n'
	    	  + "      <source>VID</source>"  + '\n'
	    	  + "   </request-info>"  + '\n'
	    	  + "   <aetgt:status-message>Network has been deleted successfully.</aetgt:status-message>"  + '\n'
	    	  + "   <aetgt:mso-bpel-name>BPMN Network action: DELETE</aetgt:mso-bpel-name>" + '\n'
	    	  + "</aetgt:MsoCompletionRequest>";

	    Assert.assertEquals(completeMsoProcessRequest, getVariable(processEngineRule, "DeleteNetworkInstance", "DELNI_CompleteMsoProcessRequest"));

		System.out.println("----------------------------------------------------------");
		System.out.println("     Success VID - DeleteNetworkInstance flow Completed   ");
		System.out.println("----------------------------------------------------------");


	}

	@Test
	//@Ignore
	@Deployment(resources = {"process/DeleteNetworkInstance.bpmn",
						 	 "subprocess/DoDeleteNetworkInstance.bpmn",
						 	 "subprocess/DoDeleteNetworkInstanceRollback.bpmn",
			                 "subprocess/FalloutHandler.bpmn",
	                         "subprocess/CompleteMsoProcess.bpmn",
	                         "subprocess/SDNCAdapterV1.bpmn"})

	public void shouldInvokeServiceDeleteNetworkInstanceInfra_vIPER_Rollback() throws Exception {
        // Rollback is not Applicable for DeleteNetwork (no requirements). Rollback should not be invoked.
		System.out.println("----------------------------------------------------------");
		System.out.println("      Rollback - DeleteNetworkInstance flow Started!      ");
		System.out.println("----------------------------------------------------------");

		// setup simulatores
		mockSDNCAdapter("/SDNCAdapter", "SvcAction>unassign", 500, "");
		mockSDNCAdapterTopology("DeleteNetworkV2mock/sdncDeleteNetworkTopologySimResponse.xml", "SvcAction>deactivate");
		mockSDNCAdapterTopology("CreateNetworkV2mock/sdncCreateNetworkTopologySimResponse.xml", "SvcAction>activate");
		MockNetworkAdapter("bdc5efe8-404a-409b-85f6-0dcc9eebae30", 200, "deleteNetworkResponse_Success.xml");
		MockNetworkAdapterContainingRequest("createNetworkRequest", 200, "CreateNetworkV2/createNetworkResponse_Success.xml");
		MockGetNetworkByIdWithDepth	("bdc5efe8-404a-409b-85f6-0dcc9eebae30", "DeleteNetworkV2/deleteNetworkAAIResponse_Success.xml", "all");
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		MockGetCloudRegion("RDM2WAGPLCP", 200, "DeleteNetworkV2/cloudRegion30_AAIResponse_Success.xml");

		String networkModelInfo = "  {\"modelCustomizationId\": \"uuid-nrc-001-1234\", " + '\n' +
                "   \"modelInvariantId\": \"was-ist-das-001-1234\" }";

		Map<String, String> variables = new HashMap<String, String>();
		variables.put("testMessageId", "88f65519-9a38-4c4b-8445-9eb4a5a5af56");
		variables.put("msoRequestId", "testRequestId");
		variables.put("requestId", "testRequestId");
		variables.put("serviceInstanceId", "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb");
		variables.put("networkId", "bdc5efe8-404a-409b-85f6-0dcc9eebae30");
		variables.put("networkName", "HSL_direct_net_2");
		variables.put("lcpCloudRegionId", "RDM2WAGPLCP");
		variables.put("tenantId", "88a6ca3ee0394ade9403f075db23167e");
		variables.put("productFamilyId", "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb");
		variables.put("disableRollback", "false");  // 1702
		variables.put("failIfExists", "false");
		variables.put("sdncVersion", "1702");
		variables.put("subscriptionServiceType", "MSO-dev-service-type");
		variables.put("networkModelInfo", networkModelInfo);

		executeAsyncWorkflow(processEngineRule, "DeleteNetworkInstance", variables);
		//WorkflowResponse workflowResponse = executeAsyncWorkflow(processEngineRule, "DeleteNetworkInstance", variables);
		//waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		String workflowResp = BPMNUtil.getVariable(processEngineRule, "DeleteNetworkInstance", "WorkflowResponse");
		Assert.assertNotNull(workflowResp);

		Assert.assertNotNull("DELNI_FalloutHandlerRequest - ", getVariable(processEngineRule, "DeleteNetworkInstance", "DELNI_FalloutHandlerRequest"));
	    Assert.assertEquals("false", getVariable(processEngineRule, "DeleteNetworkInstance", "DELNI_Success"));
	    Assert.assertEquals("false", BPMNUtil.getVariable(processEngineRule, "DoDeleteNetworkInstance", "DELNWKI_Success"));

		System.out.println("----------------------------------------------------------");
		System.out.println("     Rollback - DeleteNetworkInstanceModular flow Completed     ");
		System.out.println("----------------------------------------------------------");


	}


	// *****************
	// Utility Section
	// *****************

	public String getDeleteNetworkInstanceInfraRequest() {

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
				"          \"instanceName\": \"HSL_direct_net_2\", " + '\n' +
				"          \"source\": \"VID\", " + '\n' +
				"          \"suppressRollback\": \"false\", " + '\n' +
				"          \"callbackUrl\": \"\" " + '\n' +
				"      }, " + '\n' +
				"      \"requestParameters\": { " + '\n' +
				"          \"backoutOnFailure\": true, " + '\n' +
				"          \"serviceId\": \"a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb\", " + '\n' +
				"          \"userParams\": {} " + '\n' +
				"      }	" + '\n' +
			    " } " + '\n' +
			    "}";
		return request;

	}


	public String getDeleteNetworkInstanceInfraRequest_MissingId() {

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
				"          \"instanceName\": \"HSL_direct_net_2\", " + '\n' +
				"          \"source\": \"VID\", " + '\n' +
				"          \"callbackUrl\": \"\" " + '\n' +
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

}