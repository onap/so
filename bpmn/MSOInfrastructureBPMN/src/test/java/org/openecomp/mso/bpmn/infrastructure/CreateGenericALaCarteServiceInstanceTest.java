package org.openecomp.mso.bpmn.infrastructure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.openecomp.mso.bpmn.common.BPMNUtil.executeWorkFlow;
import static org.openecomp.mso.bpmn.common.BPMNUtil.waitForWorkflowToFinish;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetCustomer;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetServiceInstance;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockNodeQueryServiceInstanceById;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockNodeQueryServiceInstanceByName;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPutServiceInstance;
import static org.openecomp.mso.bpmn.mock.StubResponseDatabase.mockUpdateRequestDB;
import static org.openecomp.mso.bpmn.mock.StubResponseSDNCAdapter.mockSDNCAdapter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.BPMNUtil;
import org.openecomp.mso.bpmn.common.WorkflowTest;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowResponse;
import org.openecomp.mso.bpmn.mock.FileUtil;

/**
 * Unit test cases for CreateGenericALaCarteServiceInstance.bpmn
 */
public class CreateGenericALaCarteServiceInstanceTest extends WorkflowTest {

	private final CallbackSet callbacks = new CallbackSet();

	public CreateGenericALaCarteServiceInstanceTest() throws IOException {
		callbacks.put("assign", FileUtil.readResourceFile("__files/VfModularity/SDNCSITopologyAssignCallback.xml"));
	}

	/**
	 * Sunny day VID scenario.
	 *
	 * @throws Exception
	 */
	//@Ignore // File not found - unable to run the test.  Also, Stubs need updating..
	@Test
	@Deployment(resources = {
			"process/CreateGenericALaCarteServiceInstance.bpmn",
			"subprocess/DoCreateServiceInstance.bpmn",
			"subprocess/DoCreateServiceInstanceRollback.bpmn",
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/GenericPutService.bpmn",
			"subprocess/GenericGetService.bpmn",
			"subprocess/CompleteMsoProcess.bpmn",
			"subprocess/FalloutHandler.bpmn" })
	public void sunnyDayAlaCarte() throws Exception {

		logStart();

		//AAI
		MockGetCustomer("MCBH-1610", "CreateServiceInstance/createServiceInstance_queryGlobalCustomerId_AAIResponse_Success.xml");
		MockPutServiceInstance("MCBH-1610", "viprsvc", "RaaTest-1-id", "");
		MockGetServiceInstance("MCBH-1610", "viprsvc", "RaaTest-1-id", "GenericFlows/getServiceInstance.xml");
		MockNodeQueryServiceInstanceByName("RAATest-1", "");
		MockNodeQueryServiceInstanceById("RaaTest-1-id", "");
		//SDNC
		mockSDNCAdapter(200);
		//DB
		mockUpdateRequestDB(200, "DBUpdateResponse.xml");


		String businessKey = UUID.randomUUID().toString();

		//String createVfModuleRequest = FileUtil.readResourceFile("__files/SIRequest.json");

		Map<String, String> variables = setupVariables();
		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "CreateGenericALaCarteServiceInstance", variables);
		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());

		String workflowResp = BPMNUtil.getVariable(processEngineRule, "CreateGenericALaCarteServiceInstance", "WorkflowResponse");
		//assertNotNull(workflowResp);
		System.out.println("Workflow (Synch) Response:\n" + workflowResp);
		String workflowException = BPMNUtil.getVariable(processEngineRule, "CreateGenericALaCarteServiceInstance", "WorkflowException");
		String completionReq = BPMNUtil.getVariable(processEngineRule, "CreateGenericALaCarteServiceInstance", "completionRequest");
		System.out.println("completionReq:\n" + completionReq);
		System.out.println("workflowException:\n" + workflowException);
		assertNotNull(completionReq);
		assertEquals(null, workflowException);


		//injectSDNCCallbacks(callbacks, "assign");

		logEnd();
	}

	// Success Scenario
	private Map<String, String> setupVariables() {
		Map<String, String> variables = new HashMap<String, String>();
		variables.put("isDebugLogEnabled", "true");
		variables.put("bpmnRequest", getRequest());
		variables.put("mso-request-id", "RaaCSIRequestId-1");
		variables.put("serviceInstanceId","RaaTest-1-id");
		return variables;
	}

	public String getRequest() {
		String request = "{\"requestDetails\":{\"modelInfo\":{\"modelType\":\"service\",\"modelInvariantUuid\":\"uuid-miu-svc-011-abcdef\",\"modelVersionUuid\":\"ASDC_TOSCA_UUID\",\"modelName\":\"SIModelName1\",\"modelVersion\":\"2\"},\"subscriberInfo\":{\"globalSubscriberId\":\"MCBH-1610\",\"subscriberName\":\"Kaneohe\"},\"requestInfo\":{\"instanceName\":\"RAATest-1\",\"source\":\"VID\",\"suppressRollback\":\"true\",\"productFamilyId\":\"a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb\"},\"cloudConfiguration\":{\"lcpCloudRegionId\":\"mdt1\",\"tenantId\":\"8b1df54faa3b49078e3416e21370a3ba\"},\"requestParameters\":{\"subscriptionServiceType\":\"viprsvc\",\"aLaCarte\":\"false\",\"userParams\":[]}}}";
		return request;
	}

}
