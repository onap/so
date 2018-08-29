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



public class GenericVnfHealthCheckBBTest extends BaseBPMNTest {
	
	@Test
	public void sunnyDayGenericVnfHealthCheckBBTest() throws InterruptedException, IOException {

		ProcessInstance pi = runtimeService.startProcessInstanceByKey("GenericVnfHealthCheckBB", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted().hasPassedInOrder("Start_GenericVnfHealthChkBB", "SetParamsHealthCheck", 
					"Call-AppC-HealthCheck", "End_GenericVnfHealthChkBB");
		assertThat(pi).isEnded();
	}

	@Test
	public void genericVnfHealthCheckBBExceptionTest() throws Exception {
		doThrow(new BpmnError("7000", "TESTING ERRORS")).when(genericVnfHealthCheck).setParamsForGenericVnfHealthCheck(any(BuildingBlockExecution.class));
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("GenericVnfHealthCheckBB", variables);
		assertThat(pi).isNotNull();
		assertThat(pi).isStarted()
				.hasPassedInOrder("Start_GenericVnfHealthChkBB", "SetParamsHealthCheck")
				.hasNotPassed("Call-AppC-HealthCheck", "End_GenericVnfHealthChkBB");
		assertThat(pi).isEnded();
	}
}


