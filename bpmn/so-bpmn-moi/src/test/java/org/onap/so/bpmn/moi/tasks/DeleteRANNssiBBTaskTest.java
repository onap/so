/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (c) 2022 Deutsche telekom
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

package org.onap.so.bpmn.moi.tasks;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.ServiceSubscription;
import org.onap.aaiclient.client.aai.AAIRestClientImpl;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.ExceptionBuilder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.Silent.class)

public class DeleteRANNssiBBTaskTest {

    @Mock
    BuildingBlockExecution execution;
    @Mock
    ModelInfoServiceInstance modelInfoServiceInstance;
    @Mock
    protected ExtractPojosForBB extractPojosForBBMock;
    @Mock
    protected ExceptionBuilder exceptionUtil;
    @Mock
    ServiceInstance serviceInstance;
    private String operationalState;
    private String administrativeState;
    private String serviceInstanceId = "123";

    private String sliceProfileServiceInstanceId = "123";

    private AAIRestClientImpl aaiRestClient = new AAIRestClientImpl();

    private GeneralBuildingBlock gBB = new GeneralBuildingBlock();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Mock
    DeleteRANNssiBBTask deleteRANNssiBBTask;

    @Before
    public void before() throws BBObjectNotFoundException {

        serviceInstance.setServiceInstanceId("123");
        modelInfoServiceInstance.setModelUuid("231");
        ServiceInstance serviceInstance = new ServiceInstance();
        when(extractPojosForBBMock.extractByKey(any(), eq(ResourceKey.SERVICE_INSTANCE_ID)))
                .thenReturn(serviceInstanceId);
        when(extractPojosForBBMock.extractByKey(any(), eq(ResourceKey.ServiceInstance))).thenReturn(serviceInstance);
        when(extractPojosForBBMock.extractByKey(any(), eq(ResourceKey.ModelInfoServiceInstance)))
                .thenReturn(modelInfoServiceInstance);
        when(extractPojosForBBMock.extractByKey(any(), eq(ResourceKey.operationalState))).thenReturn(operationalState);
        when(extractPojosForBBMock.extractByKey(any(), eq(ResourceKey.administrativeState)))
                .thenReturn(administrativeState);
        doThrow(new BpmnError("BPMN Error")).when(exceptionUtil)
                .buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(Exception.class));
    }

    @Test
    public void deleteNssiTest() throws JsonProcessingException {
        doNothing().when(deleteRANNssiBBTask).deleteNssi(execution);
        deleteRANNssiBBTask.deleteNssi(execution);
        verify(deleteRANNssiBBTask, times(1)).deleteNssi(execution);

    }
}
