/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.manualhandling.tasks;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.ticket.ExternalTicket;
import org.onap.so.db.request.beans.InfraActiveRequests;

public class ManualHandlingTasksTest extends BaseTaskTest {
    @InjectMocks
    protected ManualHandlingTasks manualHandlingTasks = new ManualHandlingTasks();

    @Mock
    TaskService taskService;

    @Mock
    private DelegateExecution mockExecution;

    @Mock
    ProcessEngineServices processEngineServices;

    @Mock
    private DelegateTask task;

    @Mock
    private BuildingBlockExecution buildingBlockExecution;

    @Mock
    private GeneralBuildingBlock generalBuildingBlock;

    @Mock
    private RequestContext requestContext;

    @Mock
    private ExternalTicket MOCK_externalTicket;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        delegateExecution = new DelegateExecutionFake();
        buildingBlockExecution = new DelegateExecutionImpl(delegateExecution);
        generalBuildingBlock = new GeneralBuildingBlock();
        requestContext = new RequestContext();
        requestContext.setRequestorId("someRequestorId");
        generalBuildingBlock.setRequestContext(requestContext);
        buildingBlockExecution.setVariable("mso-request-id", ("testMsoRequestId"));
        buildingBlockExecution.setVariable("vnfType", "testVnfType");
        buildingBlockExecution.setVariable("gBBInput", generalBuildingBlock);
        buildingBlockExecution.setVariable("rainyDayVnfName", "someVnfName");
        buildingBlockExecution.setVariable("workStep", "someWorkstep");
        buildingBlockExecution.setVariable("taskTimeout", "PT5M");
    }

    @Test
    public void setFalloutTaskVariables_Test() {
        when(mockExecution.getVariable("gBuildingBlockExecution")).thenReturn(buildingBlockExecution);
        buildingBlockExecution.setVariable("gBBInput", generalBuildingBlock);
        when(task.getId()).thenReturn("taskId");
        when(task.getExecution()).thenReturn(mockExecution);
        when(mockExecution.getProcessEngineServices()).thenReturn(processEngineServices);
        when(processEngineServices.getTaskService()).thenReturn(taskService);
        manualHandlingTasks.setFalloutTaskVariables(task);
        verify(taskService, times(1)).setVariablesLocal(any(String.class), any(Map.class));
    }

    @Test
    public void setPauseTaskVariables_Test() {
        when(task.getId()).thenReturn("taskId");
        when(task.getExecution()).thenReturn(mockExecution);
        when(mockExecution.getProcessEngineServices()).thenReturn(processEngineServices);
        when(processEngineServices.getTaskService()).thenReturn(taskService);
        manualHandlingTasks.setPauseTaskVariables(task);
        verify(taskService, times(1)).setVariablesLocal(any(String.class), any(Map.class));
    }

    @Test
    public void completeTask_Test() {
        when(task.getId()).thenReturn("taskId");
        when(task.getExecution()).thenReturn(mockExecution);
        Map<String, Object> taskVariables = new HashMap<String, Object>();
        taskVariables.put("responseValue", "resume");
        when(mockExecution.getProcessEngineServices()).thenReturn(processEngineServices);
        when(processEngineServices.getTaskService()).thenReturn(taskService);
        when(taskService.getVariables(any(String.class))).thenReturn(taskVariables);
        manualHandlingTasks.completeTask(task);
        verify(mockExecution, times(1)).setVariable("responseValueTask", "Resume");
    }

    @Test
    public void updateRequestDbStatus_Test() {
        InfraActiveRequests mockedRequest = new InfraActiveRequests();
        when(requestsDbClient.getInfraActiveRequestbyRequestId(any(String.class))).thenReturn(mockedRequest);
        doNothing().when(requestsDbClient).updateInfraActiveRequests(any(InfraActiveRequests.class));
        manualHandlingTasks.updateRequestDbStatus(buildingBlockExecution, "IN_PROGRESS");
        verify(requestsDbClient, times(1)).updateInfraActiveRequests(any(InfraActiveRequests.class));
        assertEquals(mockedRequest.getRequestStatus(), "IN_PROGRESS");
    }

}
