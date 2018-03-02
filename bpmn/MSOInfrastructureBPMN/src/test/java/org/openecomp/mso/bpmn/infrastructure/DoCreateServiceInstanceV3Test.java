package org.openecomp.mso.bpmn.infrastructure;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.WorkflowTest;
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition;
import org.openecomp.mso.bpmn.core.json.JsonDecomposingException;

/**
 * Unit test cases for DoCreateServiceInstanceV3.bpmn
 */
public class DoCreateServiceInstanceV3Test extends WorkflowTest {

	public DoCreateServiceInstanceV3Test() throws IOException {

	}

	@Test
	@Deployment(resources = { "subprocess/DoCreateServiceInstanceV3.bpmn",
							  "subprocess/DoCreateServiceInstanceV3Rollback.bpmn"
							})
	public void sunnyDay() throws Exception {
		logStart();
		String businessKey = UUID.randomUUID().toString();
		Map<String, Object> variables = new HashMap<String, Object>();
		setupVariables(variables);
		invokeSubProcess("DoCreateServiceInstanceV3", businessKey, variables);
		waitForProcessEnd(businessKey, 10000);
		Assert.assertTrue(isProcessEnded(businessKey));
		logEnd();
	}

	// Success Scenario
	private void setupVariables(Map<String, Object> variables) throws JsonDecomposingException {
		variables.put("abc", "thevalueisabc");
		variables.put("mso-request-id", "213");
		ServiceDecomposition serviceDecomp = new ServiceDecomposition("{\"serviceResources\":{}}", "123");
		serviceDecomp.setServiceType("PORT-MIRRO");
		serviceDecomp.setSdncVersion("1610");
		variables.put("serviceDecomposition", serviceDecomp);
	}
}