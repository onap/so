package org.openecomp.mso.bpmn.buildingblock;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat;

import java.io.IOException;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.mso.BaseTest;


public class SniroHomingBBTest extends BaseTest{

	@Test
	@Ignore
	public void testSniroHoming_success() throws InterruptedException, IOException {
		mockSubprocess("ReceiveWorkflowMessage", "Mock ReceiveWorkflowMessage", "GenericStub");
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("HomingV2", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted().hasPassedInOrder("start", "callSniro", "callReceiveAsync", "processSolution", "end");
		assertThat(pi).isEnded();
	}

}
