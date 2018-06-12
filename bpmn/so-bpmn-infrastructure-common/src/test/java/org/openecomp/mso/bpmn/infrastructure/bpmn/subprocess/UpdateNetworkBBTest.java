package org.openecomp.mso.bpmn.infrastructure.bpmn.subprocess;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.openecomp.mso.bpmn.BaseBPMNTest;
import org.openecomp.mso.bpmn.common.BuildingBlockExecution;


public class UpdateNetworkBBTest extends BaseBPMNTest {
    @Test
    public void updateNetworkBBTest() throws InterruptedException {
    	ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("UpdateNetworkBB", variables);
    	assertThat(processInstance).isNotNull();
    	assertThat(processInstance).isStarted().hasPassedInOrder(
    			"UpdateNetworkBB_Start", "SDNCChangeAssignNetwork", 
    			"QueryNetworkAAI", "QueryVpnBindingAAI", "QueryNetworkPolicyAAI", "QueryNetworkTableRefAAI", 
    			"UpdateNetworkAdapter", "UpdateNetworkAAI", "UpdateNetworkBB_End");
    	assertThat(processInstance).isEnded();
    }

	@Test
	public void updateNetworkBBExceptionTest() throws Exception {
		doThrow(new BpmnError("7000", "TESTING ERRORS")).when(aaiQueryTasks).queryNetworkVpnBinding(any(BuildingBlockExecution.class));
		
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("UpdateNetworkBB", variables);
		assertThat(processInstance).isStarted().hasPassedInOrder(
				"UpdateNetworkBB_Start", "SDNCChangeAssignNetwork", 
    			"QueryNetworkAAI", "QueryVpnBindingAAI")
			.hasNotPassed("QueryNetworkPolicyAAI", "QueryNetworkTableRefAAI", 
    			"UpdateNetworkAdapter", "UpdateNetworkAAI", "UpdateNetworkBB_End");
		assertThat(processInstance).isEnded();
	}
}