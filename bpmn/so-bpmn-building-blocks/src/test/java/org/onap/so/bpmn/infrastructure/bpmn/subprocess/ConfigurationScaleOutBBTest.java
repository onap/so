package org.onap.so.bpmn.infrastructure.bpmn.subprocess;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;

import java.io.IOException;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.runtime.ProcessInstance;

import org.junit.Test;
import org.onap.so.bpmn.BaseBPMNTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;



public class ConfigurationScaleOutBBTest extends BaseBPMNTest {

	@Test
	public void sunnyDayConfigurationScaleOutBBTest() throws InterruptedException, IOException {
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("ConfigurationScaleOutBB", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted().hasPassedInOrder("Start_ConfigScaleOutBB", "QueryVfModule", "GetConfigScaleOutParams",
					"Call-AppC-ConfigScaleOut", "End_ConfigScaleOutBB");
		assertThat(pi).isEnded();
	}

	@Test
	public void configurationScaleOutBBExceptionTest() throws Exception {
		doThrow(new BpmnError("7000", "TESTING ERRORS")).when(sdncQueryTasks).queryVfModule(any(BuildingBlockExecution.class));
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("ConfigurationScaleOutBB", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted()
				.hasPassedInOrder("Start_ConfigScaleOutBB", "QueryVfModule")
				.hasNotPassed("GetConfigScaleOutParams", "Call-AppC-ConfigScaleOut", "End_ConfigScaleOutBB");
		assertThat(pi).isEnded();
	}
}
