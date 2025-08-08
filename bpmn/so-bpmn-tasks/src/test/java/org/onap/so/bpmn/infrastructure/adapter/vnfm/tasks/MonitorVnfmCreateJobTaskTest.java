/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.UUID;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.CreateVnfResponse;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.OperationStateEnum;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.OperationStatusRetrievalStatusEnum;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.QueryJobResponse;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import com.google.common.base.Optional;

/**
 * 
 * @author Lathishbabu Ganesan (lathishbabu.ganesan@est.tech)
 *
 */
public class MonitorVnfmCreateJobTaskTest extends BaseTaskTest {

    private static final String JOB_ID = UUID.randomUUID().toString();

    @Mock
    private VnfmAdapterServiceProvider mockedVnfmAdapterServiceProvider;

    private final BuildingBlockExecution stubbedxecution = new StubbedBuildingBlockExecution();

    @Test
    public void testGetCurrentOperationStatus() {
        final MonitorVnfmCreateJobTask objUnderTest = getEtsiVnfMonitorJobTask();
        stubbedxecution.setVariable(Constants.CREATE_VNF_RESPONSE_PARAM_NAME, getCreateVnfResponse());
        Optional<QueryJobResponse> queryJobResponse = getQueryJobResponse();
        queryJobResponse.get().setOperationStatusRetrievalStatus(OperationStatusRetrievalStatusEnum.STATUS_FOUND);
        queryJobResponse.get().setOperationState(OperationStateEnum.COMPLETED);
        when(mockedVnfmAdapterServiceProvider.getInstantiateOperationJobStatus(JOB_ID)).thenReturn(queryJobResponse);
        objUnderTest.getCurrentOperationStatus(stubbedxecution);
        final Optional<OperationStateEnum> operationState =
                stubbedxecution.getVariable(Constants.OPERATION_STATUS_PARAM_NAME);
        assertNotNull(operationState);
        assertEquals(OperationStateEnum.COMPLETED, operationState.get());
    }

    @Test
    public void testGetCurrentOperationStatusFailed() {
        final MonitorVnfmCreateJobTask objUnderTest = getEtsiVnfMonitorJobTask();
        stubbedxecution.setVariable(Constants.CREATE_VNF_RESPONSE_PARAM_NAME, getCreateVnfResponse());
        Optional<QueryJobResponse> queryJobResponse = getQueryJobResponse();
        queryJobResponse.get()
                .setOperationStatusRetrievalStatus(OperationStatusRetrievalStatusEnum.CANNOT_RETRIEVE_STATUS);
        queryJobResponse.get().setOperationState(OperationStateEnum.FAILED);
        when(mockedVnfmAdapterServiceProvider.getInstantiateOperationJobStatus(JOB_ID)).thenReturn(queryJobResponse);
        objUnderTest.getCurrentOperationStatus(stubbedxecution);
        final Optional<OperationStateEnum> operationState =
                stubbedxecution.getVariable(Constants.OPERATION_STATUS_PARAM_NAME);
        assertNotNull(operationState);
        assertEquals(OperationStateEnum.FAILED, operationState.get());
    }

    @Test
    public void testGetCurrentOperationStatusEmpty() {
        final MonitorVnfmCreateJobTask objUnderTest = getEtsiVnfMonitorJobTask();
        stubbedxecution.setVariable(Constants.CREATE_VNF_RESPONSE_PARAM_NAME, getCreateVnfResponse());
        Optional<QueryJobResponse> queryJobResponse = getQueryJobResponse();
        queryJobResponse.get().setOperationStatusRetrievalStatus(OperationStatusRetrievalStatusEnum.STATUS_FOUND);
        when(mockedVnfmAdapterServiceProvider.getInstantiateOperationJobStatus(JOB_ID)).thenReturn(queryJobResponse);
        objUnderTest.getCurrentOperationStatus(stubbedxecution);
        final Optional<OperationStateEnum> operationState =
                stubbedxecution.getVariable(Constants.OPERATION_STATUS_PARAM_NAME);
        assertFalse(operationState.isPresent());
    }

    @Test
    public void testGetCurrentOperationStatusException() {
        final MonitorVnfmCreateJobTask objUnderTest = getEtsiVnfMonitorJobTask();
        stubbedxecution.setVariable(Constants.CREATE_VNF_RESPONSE_PARAM_NAME, getCreateVnfResponse());
        Optional<QueryJobResponse> queryJobResponse = getQueryJobResponse();
        queryJobResponse.get().setOperationStatusRetrievalStatus(OperationStatusRetrievalStatusEnum.STATUS_FOUND);
        when(mockedVnfmAdapterServiceProvider.getInstantiateOperationJobStatus(JOB_ID)).thenReturn(queryJobResponse);
        objUnderTest.getCurrentOperationStatus(stubbedxecution);
        final Optional<OperationStateEnum> operationState =
                stubbedxecution.getVariable(Constants.OPERATION_STATUS_PARAM_NAME);
        assertFalse(operationState.isPresent());
    }

    @Test
    public void testHasOperationFinished() {
        final MonitorVnfmCreateJobTask objUnderTest = getEtsiVnfMonitorJobTask();
        stubbedxecution.setVariable(Constants.OPERATION_STATUS_PARAM_NAME, Optional.of(OperationStateEnum.COMPLETED));
        assertTrue(objUnderTest.hasOperationFinished(stubbedxecution));
    }

    @Test
    public void testHasOperationPending() {
        final MonitorVnfmCreateJobTask objUnderTest = getEtsiVnfMonitorJobTask();
        stubbedxecution.setVariable(Constants.OPERATION_STATUS_PARAM_NAME, Optional.absent());
        assertFalse(objUnderTest.hasOperationFinished(stubbedxecution));
    }

    @Test
    public void testTimeOutLogFailue() {
        final MonitorVnfmCreateJobTask objUnderTest = getEtsiVnfMonitorJobTask();
        objUnderTest.timeOutLogFailue(stubbedxecution);
        verify(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(1205),
                eq("Instantiation operation time out"));
    }

    @Test
    public void testCheckIfOperationWasSuccessful() {
        stubbedxecution.setVariable(Constants.OPERATION_STATUS_PARAM_NAME, Optional.of(OperationStateEnum.COMPLETED));
        MonitorVnfmCreateJobTask objUnderTest = Mockito.spy(getEtsiVnfMonitorJobTask());
        objUnderTest.checkIfOperationWasSuccessful(stubbedxecution);
        verify(objUnderTest, times(1)).checkIfOperationWasSuccessful(stubbedxecution);
    }

    @Test
    public void testCheckIfOperationWasSuccessfulWithPending() {
        final MonitorVnfmCreateJobTask objUnderTest = getEtsiVnfMonitorJobTask();
        stubbedxecution.setVariable(Constants.OPERATION_STATUS_PARAM_NAME, Optional.of(OperationStateEnum.PROCESSING));
        objUnderTest.checkIfOperationWasSuccessful(stubbedxecution);
        verify(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(1207), anyString());
    }

    @Test
    public void testCheckIfOperationWasSuccessfulEmpty() {
        MonitorVnfmCreateJobTask objUnderTest = getEtsiVnfMonitorJobTask();
        stubbedxecution.setVariable(Constants.CREATE_VNF_RESPONSE_PARAM_NAME, getCreateVnfResponse());
        stubbedxecution.setVariable(Constants.OPERATION_STATUS_PARAM_NAME, Optional.absent());
        objUnderTest.checkIfOperationWasSuccessful(stubbedxecution);
        verify(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(1206), anyString());
    }

    private CreateVnfResponse getCreateVnfResponse() {
        final CreateVnfResponse response = new CreateVnfResponse();
        response.setJobId(JOB_ID);
        return response;
    }

    private Optional<QueryJobResponse> getQueryJobResponse() {
        final QueryJobResponse queryJobResponse = new QueryJobResponse();
        queryJobResponse.setId(JOB_ID);
        return Optional.of(queryJobResponse);
    }

    private MonitorVnfmCreateJobTask getEtsiVnfMonitorJobTask() {
        return new MonitorVnfmCreateJobTask(mockedVnfmAdapterServiceProvider, exceptionUtil);
    }

}
