/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.adapter.cnfm.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.net.URI;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.cnfm.lcm.model.AsLcmOpOcc;

/**
 * @author Raviteja Karumuri (raviteja.karumuri@est.tech)
 */
@RunWith(MockitoJUnitRunner.class)
public class MonitorCnfmCreateJobTaskTest {

    private final BuildingBlockExecution stubbedExecution = new StubbedBuildingBlockExecution();
    public static final String CREATE_CNF_STATUS_RESPONSE_PARAM_NAME = "createCnfStatusResponse";
    private final String CNFM_REQUEST_STATUS_CHECK_URL = "CnfmStatusCheckUrl";
    public static final String OPERATION_STATUS_PARAM_NAME = "operationStatus";
    private MonitorCnfmCreateJobTask monitorCnfmCreateJobTask;
    @Mock
    private CnfmHttpServiceProvider mockedCnfmHttpServiceProvider;
    @Mock
    private ExceptionBuilder exceptionUtil;

    @Before
    public void setup() {
        monitorCnfmCreateJobTask = new MonitorCnfmCreateJobTask(mockedCnfmHttpServiceProvider, exceptionUtil);
    }

    @Test
    public void getCurrentOperationStatus_completed() {
        stubbedExecution.setVariable(CNFM_REQUEST_STATUS_CHECK_URL, URI.create("sampleURL"));
        when(mockedCnfmHttpServiceProvider.getInstantiateOperationJobStatus(Mockito.anyString()))
                .thenReturn(getAsLcmOpOcc());
        monitorCnfmCreateJobTask.getCurrentOperationStatus(stubbedExecution);
        assertEquals(AsLcmOpOcc.OperationStateEnum.COMPLETED,
                stubbedExecution.getVariable(OPERATION_STATUS_PARAM_NAME));
    }

    @Test
    public void test_getCurrentOperationStatus_Exception() {
        stubbedExecution.setVariable(CNFM_REQUEST_STATUS_CHECK_URL, URI.create("sampleURL"));
        when(mockedCnfmHttpServiceProvider.getInstantiateOperationJobStatus(Mockito.anyString()))
                .thenThrow(new RuntimeException());
        monitorCnfmCreateJobTask.getCurrentOperationStatus(stubbedExecution);
        verify(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(1209), anyString(),
                any());
    }

    @Test
    public void test_checkIfOperationWasSuccessful_status_completed() {
        final MonitorCnfmCreateJobTask mockedMonitorCnfmCreateJobTask = Mockito.spy(monitorCnfmCreateJobTask);
        mockedMonitorCnfmCreateJobTask.checkIfOperationWasSuccessful(stubbedExecution);
        verify(mockedMonitorCnfmCreateJobTask, times(1)).checkIfOperationWasSuccessful(stubbedExecution);
    }

    @Test
    public void test_checkIfOperationWasSuccessful_status_Failed() {
        Optional<AsLcmOpOcc> mockedAsLcmOpOcc = getAsLcmOpOcc();
        mockedAsLcmOpOcc.orElseThrow().setOperationState(AsLcmOpOcc.OperationStateEnum.FAILED);
        stubbedExecution.setVariable(OPERATION_STATUS_PARAM_NAME, AsLcmOpOcc.OperationStateEnum.FAILED);
        stubbedExecution.setVariable(CREATE_CNF_STATUS_RESPONSE_PARAM_NAME, mockedAsLcmOpOcc.orElseThrow());
        monitorCnfmCreateJobTask.checkIfOperationWasSuccessful(stubbedExecution);
        verify(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(1207), anyString(),
                any());
    }

    @Test
    public void test_checkIfOperationWasSuccessful_status_Null() {
        stubbedExecution.setVariable(OPERATION_STATUS_PARAM_NAME, null);
        monitorCnfmCreateJobTask.checkIfOperationWasSuccessful(stubbedExecution);
        verify(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(1206), anyString(),
                any());
    }

    @Test
    public void test_hasOperationFinished_status_completed() {
        stubbedExecution.setVariable(OPERATION_STATUS_PARAM_NAME, AsLcmOpOcc.OperationStateEnum.COMPLETED);
        boolean returnedValue = monitorCnfmCreateJobTask.hasOperationFinished(stubbedExecution);
        assertTrue(returnedValue);
    }

    @Test
    public void test_hasOperationFinished_status_failed() {
        stubbedExecution.setVariable(OPERATION_STATUS_PARAM_NAME, AsLcmOpOcc.OperationStateEnum.FAILED);
        boolean returnedValue = monitorCnfmCreateJobTask.hasOperationFinished(stubbedExecution);
        assertTrue(returnedValue);
    }

    @Test
    public void test_hasOperationFinished_status_rollback() {
        stubbedExecution.setVariable(OPERATION_STATUS_PARAM_NAME, AsLcmOpOcc.OperationStateEnum.ROLLED_BACK);
        boolean returnedValue = monitorCnfmCreateJobTask.hasOperationFinished(stubbedExecution);
        assertTrue(returnedValue);
    }

    @Test
    public void test_hasOperationFinished_status_null() {
        stubbedExecution.setVariable(OPERATION_STATUS_PARAM_NAME, null);
        boolean returnedValue = monitorCnfmCreateJobTask.hasOperationFinished(stubbedExecution);
        assertFalse(returnedValue);
    }

    @Test
    public void test_timeOutLogFailure() {
        monitorCnfmCreateJobTask.timeOutLogFailue(stubbedExecution);
        verify(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(1205), anyString(),
                any());
    }

    private Optional<AsLcmOpOcc> getAsLcmOpOcc() {
        final AsLcmOpOcc asLcmOpOcc = new AsLcmOpOcc();
        asLcmOpOcc.setOperationState(AsLcmOpOcc.OperationStateEnum.COMPLETED);
        return Optional.of(asLcmOpOcc);
    }

}
