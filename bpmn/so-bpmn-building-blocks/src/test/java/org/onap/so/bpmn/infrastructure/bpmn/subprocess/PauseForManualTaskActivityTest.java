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

package org.onap.so.bpmn.infrastructure.bpmn.subprocess;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.onap.so.bpmn.BaseBPMNTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;


public class PauseForManualTaskActivityTest extends BaseBPMNTest {
    private static final String TIMEOUT_10_S = "PT10S";

    @Autowired
    protected ManagementService managementService;

    @Autowired
    protected TaskService taskService;

    @Test
    public void sunnyDayPauseForManualTaskTimeout_Test() throws InterruptedException {
        variables.put("taskTimeout", TIMEOUT_10_S);
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("PauseForManualTaskActivity", variables);
        assertThat(pi).isNotNull();
        assertThat(pi).isWaitingAt("ManualUserTask");
        Task task = taskService.createTaskQuery().active().list().get(0);
        assertThat(pi).task().isNotNull();
        assertNotNull(task);

        Job job = managementService.createJobQuery().activityId("ManualTaskTimer").singleResult();
        assertNotNull(job);
        managementService.executeJob(job.getId());

        assertThat(pi).isStarted().hasPassedInOrder("PauseForManualTaskActivity_Start",
                "UpdateDbStatusToPendingManualTask", "CreateExternalTicket", "ManualTaskTimer",
                "UpdateDBStatusToTimeout", "ThrowTimeoutError");
    }

    @Test
    public void sunnyDayPauseForManualTaskCompleted_Test() throws InterruptedException {
        variables.put("taskTimeout", TIMEOUT_10_S);
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("PauseForManualTaskActivity", variables);
        assertThat(pi).isNotNull();
        assertThat(pi).isWaitingAt("ManualUserTask");
        assertThat(pi).task().isNotNull();
        Task task = taskService.createTaskQuery().active().list().get(0);
        assertNotNull(task);
        taskService.complete(task.getId());

        assertThat(pi).isStarted().hasPassedInOrder("PauseForManualTaskActivity_Start",
                "UpdateDbStatusToPendingManualTask", "CreateExternalTicket", "ManualUserTask",
                "UpdateDbStatusToInProgress", "PauseForManualTaskActivity_End");
        assertThat(pi).isEnded();
    }

    @Test
    public void rainyDayPauseForManualTask_Test() throws Exception {
        doThrow(new BpmnError("7000", "TESTING ERRORS")).when(manualHandlingTasks)
                .createExternalTicket((any(BuildingBlockExecution.class)));
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("PauseForManualTaskActivity", variables);
        assertThat(pi).isNotNull().isStarted()
                .hasPassedInOrder("PauseForManualTaskActivity_Start", "UpdateDbStatusToPendingManualTask",
                        "CreateExternalTicket")
                .hasNotPassed("ManualUserTask", "UpdateDbStatusToInProgress", "PauseForManualTaskActivity_End");
    }

}
