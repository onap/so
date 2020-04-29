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
package org.onap.so.bpmn.common.aai.tasks;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.bpmn.infrastructure.aai.tasks.AAIFlagTasks;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.mapper.AAIObjectMapper;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.AAIVnfResources;
import org.springframework.beans.factory.annotation.Autowired;


public class AAIFlagTasksTest extends BaseTaskTest {

    @InjectMocks
    private AAIFlagTasks aaiFlagTasks = new AAIFlagTasks();

    private GenericVnf genericVnf;

    @Before
    public void before() throws BBObjectNotFoundException {
        genericVnf = setGenericVnf();
        doReturn(MOCK_aaiResourcesClient).when(MOCK_injectionHelper).getAaiClient();
        when(extractPojosForBB.extractByKey(any(), any())).thenReturn(genericVnf);
    }

    @Test
    public void checkVnfInMaintTestTrue() throws Exception {
        doThrow(new BpmnError("VNF is in maintenance in A&AI")).when(exceptionUtil)
                .buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(String.class));
        doReturn(false).when(aaiVnfResources).checkInMaintFlag(isA(String.class));
        doReturn(true).when(aaiVnfResources).checkInMaintFlag(isA(String.class));
        try {
            aaiFlagTasks.checkVnfInMaintFlag(execution);
        } catch (Exception e) {
            verify(aaiVnfResources, times(1)).checkInMaintFlag(any(String.class));
            verify(exceptionUtil, times(1)).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000),
                    eq("VNF is in maintenance in A&AI"));
        }
    }

    @Test
    public void checkVnfInMaintTestFalse() throws Exception {
        doReturn(false).when(aaiVnfResources).checkInMaintFlag(isA(String.class));
        aaiFlagTasks.checkVnfInMaintFlag(execution);
        verify(exceptionUtil, times(0)).buildAndThrowWorkflowException(any(BuildingBlockExecution.class),
                any(int.class), any(String.class));
    }

    @Test
    public void checkVnfInMaintFlagExceptionTest() {

        doThrow(new BpmnError("Unknown Error")).when(exceptionUtil)
                .buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(String.class));
        doThrow(RuntimeException.class).when(aaiVnfResources).checkInMaintFlag(isA(String.class));
        try {
            aaiFlagTasks.checkVnfInMaintFlag(execution);
        } catch (Exception e) {
            verify(aaiVnfResources, times(1)).checkInMaintFlag(any(String.class));
            verify(exceptionUtil, times(1)).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000),
                    any(String.class));
        }

    }

    @Test
    public void modifyVnfInMaintFlagTest() throws Exception {
        doNothing().when(aaiVnfResources).updateObjectVnf(ArgumentMatchers.any(GenericVnf.class));
        aaiFlagTasks.modifyVnfInMaintFlag(execution, true);
        verify(aaiVnfResources, times(1)).updateObjectVnf(ArgumentMatchers.any(GenericVnf.class));
    }

    @Test
    public void modifyVnfInMaintFlagExceptionTest() {

        doThrow(new BpmnError("Unknown Error")).when(exceptionUtil)
                .buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(String.class));
        doThrow(RuntimeException.class).when(aaiVnfResources).updateObjectVnf(isA(GenericVnf.class));
        try {
            aaiFlagTasks.modifyVnfInMaintFlag(execution, true);
        } catch (Exception e) {
            verify(aaiVnfResources, times(1)).checkInMaintFlag(any(String.class));
            verify(exceptionUtil, times(1)).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000),
                    any(String.class));
        }
    }

    @Test
    public void checkVnfClosedLoopDisabledTestTrue() throws Exception {
        doThrow(new BpmnError("VNF Closed Loop Disabled in A&AI")).when(exceptionUtil)
                .buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(String.class));
        doReturn(false).when(aaiVnfResources).checkVnfClosedLoopDisabledFlag(isA(String.class));
        try {
            aaiFlagTasks.checkVnfClosedLoopDisabledFlag(execution);
        } catch (Exception e) {
            verify(aaiVnfResources, times(1)).checkVnfClosedLoopDisabledFlag(any(String.class));
            verify(exceptionUtil, times(1)).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000),
                    eq("VNF Closed Loop Disabled in A&AI"));
        }
    }

    @Test
    public void checkVnfClosedLoopDisabledTestFalse() throws Exception {
        doReturn(false).when(aaiVnfResources).checkVnfClosedLoopDisabledFlag(isA(String.class));
        aaiFlagTasks.checkVnfClosedLoopDisabledFlag(execution);
        verify(exceptionUtil, times(0)).buildAndThrowWorkflowException(any(BuildingBlockExecution.class),
                any(int.class), any(String.class));
    }

    @Test
    public void checkVnfClosedLoopDisabledFlagExceptionTest() {

        doThrow(new BpmnError("Unknown Error")).when(exceptionUtil)
                .buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(String.class));
        doThrow(RuntimeException.class).when(aaiVnfResources).checkVnfClosedLoopDisabledFlag(isA(String.class));
        try {
            aaiFlagTasks.checkVnfClosedLoopDisabledFlag(execution);
        } catch (Exception e) {
            verify(aaiVnfResources, times(1)).checkVnfClosedLoopDisabledFlag(any(String.class));
            verify(exceptionUtil, times(1)).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000),
                    any(String.class));
        }
    }

    @Test
    public void modifyVnfClosedLoopDisabledFlagTest() throws Exception {
        doNothing().when(aaiVnfResources).updateObjectVnf(ArgumentMatchers.any(GenericVnf.class));
        aaiFlagTasks.modifyVnfClosedLoopDisabledFlag(execution, true);
        verify(aaiVnfResources, times(1)).updateObjectVnf(ArgumentMatchers.any(GenericVnf.class));
    }

    @Test
    public void modifyVnfClosedLoopDisabledFlagExceptionTest() {

        doThrow(new BpmnError("Unknown Error")).when(exceptionUtil)
                .buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(String.class));
        doThrow(RuntimeException.class).when(aaiVnfResources).updateObjectVnf(isA(GenericVnf.class));
        try {
            aaiFlagTasks.modifyVnfClosedLoopDisabledFlag(execution, true);
        } catch (Exception e) {
            verify(aaiVnfResources, times(1)).checkVnfClosedLoopDisabledFlag(any(String.class));
            verify(exceptionUtil, times(1)).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000),
                    any(String.class));
        }
    }


    @Test
    public void checkVnfPserversLockedFlagTestTrue() throws Exception {
        doThrow(new BpmnError("VNF PServers in Locked in A&AI")).when(exceptionUtil)
                .buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(String.class));
        doReturn(true).when(aaiVnfResources).checkVnfPserversLockedFlag(isA(String.class));
        try {
            aaiFlagTasks.checkVnfClosedLoopDisabledFlag(execution);
        } catch (Exception e) {
            verify(aaiVnfResources, times(1)).checkVnfPserversLockedFlag(any(String.class));
            verify(exceptionUtil, times(1)).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000),
                    eq("VNF PServers in Locked in A&AI"));
        }
    }

    @Test
    public void checkVnfPserversLockedFlagTestFalse() throws Exception {
        doReturn(false).when(aaiVnfResources).checkVnfPserversLockedFlag(isA(String.class));
        aaiFlagTasks.checkVnfPserversLockedFlag(execution);
        verify(exceptionUtil, times(0)).buildAndThrowWorkflowException(any(BuildingBlockExecution.class),
                any(int.class), any(String.class));
    }

    @Test
    public void checkVnfPserversLockedFlagExceptionTest() throws IOException {

        doThrow(new BpmnError("Unknown Error")).when(exceptionUtil)
                .buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(String.class));
        doThrow(RuntimeException.class).when(aaiVnfResources).checkVnfClosedLoopDisabledFlag(isA(String.class));
        try {
            aaiFlagTasks.checkVnfPserversLockedFlag(execution);
        } catch (Exception e) {
            verify(aaiVnfResources, times(1)).checkVnfPserversLockedFlag(any(String.class));
            verify(exceptionUtil, times(1)).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000),
                    any(String.class));
        }
    }
}
