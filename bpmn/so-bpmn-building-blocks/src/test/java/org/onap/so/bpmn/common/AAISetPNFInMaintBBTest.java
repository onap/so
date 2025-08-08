package org.onap.so.bpmn.common;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.onap.so.bpmn.BaseBPMNTest;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

public class AAISetPNFInMaintBBTest extends BaseBPMNTest {
    @Test
    public void sunnyDayAAISetPnfInMaintBBTest() {
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("AAISetPnfInMaintBB", variables);
        assertThat(pi).isNotNull().isStarted().hasPassedInOrder("Start_AAISetPnfInMaintBB", "Task_SetInMaint",
                "End_AAISetPnfInMaintBB");
    }

    @Test
    public void rainyDayAAISetPnfInMaintBBTest() {
        doThrow(new BpmnError("7000", "TESTING ERRORS")).when(aaiFlagTasks)
                .modifyPnfInMaintFlag(any(BuildingBlockExecution.class), any(boolean.class));

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("AAISetPnfInMaintBB", variables);
        assertThat(processInstance).isNotNull();
        assertThat(processInstance).isStarted().hasPassedInOrder("Start_AAISetPnfInMaintBB", "Task_SetInMaint")
                .hasNotPassed("End_AAISetPnfInMaintBB");
        assertThat(processInstance).isEnded();
    }

}
