/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.workflow.tasks;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.db.request.beans.InfraActiveRequests;

public class WorkflowActionBBTasksUpdateReqDbTest extends BaseTaskTest {

    protected WorkflowAction workflowAction = new WorkflowAction();

    @Spy
    @InjectMocks
    protected WorkflowActionBBTasks workflowActionBBTasks;

    private DelegateExecution execution;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void before() throws Exception {
        execution = new DelegateExecutionFake();
        org.onap.aai.domain.yang.ServiceInstance servInstance = new org.onap.aai.domain.yang.ServiceInstance();
        servInstance.setServiceInstanceId("TEST");
        when(bbSetupUtils.getAAIServiceInstanceByName(anyString(), any())).thenReturn(servInstance);
        workflowAction.setBbInputSetupUtils(bbSetupUtils);
        workflowAction.setBbInputSetup(bbInputSetup);
    }

    @Test
    public void getUpdatedRequestTest() {
        List<ExecuteBuildingBlock> flowsToExecute = new ArrayList();
        BuildingBlock bb1 = new BuildingBlock().setBpmnFlowName("CreateNetworkBB");
        ExecuteBuildingBlock ebb1 = new ExecuteBuildingBlock().setBuildingBlock(bb1);
        flowsToExecute.add(ebb1);
        BuildingBlock bb2 = new BuildingBlock().setBpmnFlowName("ActivateNetworkBB");
        ExecuteBuildingBlock ebb2 = new ExecuteBuildingBlock().setBuildingBlock(bb2);
        flowsToExecute.add(ebb2);
        String requestId = "requestId";
        execution.setVariable("mso-request-id", requestId);
        execution.setVariable("flowsToExecute", flowsToExecute);
        int currentSequence = 2;
        String expectedStatusMessage =
                "Execution of CreateNetworkBB has completed successfully, next invoking ActivateNetworkBB (Execution Path progress: BBs completed = 1; BBs remaining = 1).";
        Long expectedLong = new Long(52);
        InfraActiveRequests mockedRequest = new InfraActiveRequests();
        doReturn(mockedRequest).when(requestsDbClient).getInfraActiveRequestbyRequestId(isA(String.class));
        InfraActiveRequests actual = workflowActionBBTasks.getUpdatedRequest(execution, currentSequence);
        assertEquals(expectedStatusMessage, actual.getFlowStatus());
        assertEquals(expectedLong, actual.getProgress());
    }
}
