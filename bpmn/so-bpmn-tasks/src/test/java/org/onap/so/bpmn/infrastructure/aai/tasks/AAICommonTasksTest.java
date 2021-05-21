/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Modifications Copyright (c) 2021 Nokia
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

package org.onap.so.bpmn.infrastructure.aai.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Optional;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.ExceptionBuilder;

@RunWith(MockitoJUnitRunner.class)
public class AAICommonTasksTest {

    private static final String SERVICE_TYPE = "testService";

    @Mock
    private ExtractPojosForBB extractPojosForBBMock;
    @Mock
    private ExceptionBuilder exceptionBuilder;
    @InjectMocks
    private AAICommonTasks testedObject;

    private ServiceInstance serviceInstance;
    private BuildingBlockExecution buildingBlockExecution;

    @Before
    public void setup() {
        serviceInstance = new ServiceInstance();
        buildingBlockExecution = new DelegateExecutionImpl(new DelegateExecutionFake());
    }

    @Test
    public void getServiceType_success() throws Exception {
        // given
        ModelInfoServiceInstance modelInfoServiceInstance = new ModelInfoServiceInstance();
        modelInfoServiceInstance.setServiceType(SERVICE_TYPE);
        serviceInstance.setModelInfoServiceInstance(modelInfoServiceInstance);
        when(extractPojosForBBMock.extractByKey(buildingBlockExecution, ResourceKey.SERVICE_INSTANCE_ID))
                .thenReturn(serviceInstance);
        // when
        Optional<String> resultOpt = testedObject.getServiceType(buildingBlockExecution);
        // then
        assertThat(resultOpt).isNotEmpty();
        String result = resultOpt.get();
        assertThat(result).isEqualTo(SERVICE_TYPE);
    }

    @Test
    public void getServiceType_emptyWhenServiceInstanceModelIsNull() throws Exception {
        // given
        when(extractPojosForBBMock.extractByKey(buildingBlockExecution, ResourceKey.SERVICE_INSTANCE_ID))
                .thenReturn(serviceInstance);
        // when
        Optional<String> result = testedObject.getServiceType(buildingBlockExecution);
        // then
        assertThat(result).isEmpty();
    }

    @Test
    public void getServiceType_exceptionHandling() throws Exception {
        // given
        BBObjectNotFoundException exception = new BBObjectNotFoundException();
        when(extractPojosForBBMock.extractByKey(buildingBlockExecution, ResourceKey.SERVICE_INSTANCE_ID))
                .thenThrow(exception);
        // when
        testedObject.getServiceType(buildingBlockExecution);
        // then
        verify(exceptionBuilder).buildAndThrowWorkflowException(buildingBlockExecution, 7000, exception);
    }
}
