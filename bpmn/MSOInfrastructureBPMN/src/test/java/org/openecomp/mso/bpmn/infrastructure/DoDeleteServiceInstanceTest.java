package org.openecomp.mso.bpmn.infrastructure;

import static org.junit.Assert.assertEquals;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockDeleteServiceInstance;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetServiceInstance;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockNodeQueryServiceInstanceById;
import static org.openecomp.mso.bpmn.mock.StubResponseDatabase.mockUpdateRequestDB;
import static org.openecomp.mso.bpmn.mock.StubResponseSDNCAdapter.mockSDNCAdapter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.BPMNUtil;
import org.openecomp.mso.bpmn.common.WorkflowTest;

/**
 * Unit test cases for DoDeleteServiceInstance.bpmn
 */
public class DoDeleteServiceInstanceTest extends WorkflowTest {

	private final CallbackSet callbacks = new CallbackSet();
	private static final String EOL = "\n";
	private final String sdncAdapterCallback =
			"<output xmlns=\"com:att:sdnctl:l3api\">" + EOL +
			"  <svc-request-id>((REQUEST-ID))</svc-request-id>" + EOL +
			"  <ack-final-indicator>Y</ack-final-indicator>" + EOL +
			"</output>" + EOL;
		
	public DoDeleteServiceInstanceTest() throws IOException {
		callbacks.put("deactivate", sdncAdapterCallback);
		callbacks.put("delete", sdncAdapterCallback);
	}
		
	/**
	 * Sunny day VID scenario.
	 *
	 * @throws Exception
	 */
	//@Ignore // File not found - unable to run the test.  Also, Stubs need updating..
	@Test
	@Deployment(resources = {
			"subprocess/DoDeleteServiceInstance.bpmn",
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/GenericDeleteService.bpmn",
			"subprocess/GenericGetService.bpmn",
			"subprocess/CompleteMsoProcess.bpmn",
			"subprocess/FalloutHandler.bpmn" })
	public void sunnyDay() throws Exception {

		logStart();

		//AAI
		MockDeleteServiceInstance("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET", "", 204);
		MockGetServiceInstance("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET", "GenericFlows/getSINoRelations.xml");
		MockNodeQueryServiceInstanceById("MIS%252F1604%252F0026%252FSW_INTERNET", "GenericFlows/getSIUrlById.xml");
		//SDNC
		mockSDNCAdapter(200);
		//DB
		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");
		String businessKey = UUID.randomUUID().toString();

		Map<String, Object> variables =  new HashMap<String, Object>();
		setupVariables(variables);
		invokeSubProcess("DoDeleteServiceInstance", businessKey, variables);
		injectSDNCCallbacks(callbacks, "deactivate");
		injectSDNCCallbacks(callbacks, "delete");
		waitForProcessEnd(businessKey, 10000);
		Assert.assertTrue(isProcessEnded(businessKey));
		String workflowException = BPMNUtil.getVariable(processEngineRule, "DoDeleteServiceInstance", "WorkflowException");
		System.out.println("workflowException:\n" + workflowException);
		assertEquals(null, workflowException);

		logEnd();
	}

	// Success Scenario
	private void setupVariables(Map<String, Object> variables) {
		variables.put("isDebugLogEnabled", "true");
		variables.put("msoRequestId", "RaaDDSIRequestId-1");
		variables.put("mso-request-id", "RaaDDSIRequestId-1");
		variables.put("serviceInstanceId","MIS%252F1604%252F0026%252FSW_INTERNET");
	}
}
