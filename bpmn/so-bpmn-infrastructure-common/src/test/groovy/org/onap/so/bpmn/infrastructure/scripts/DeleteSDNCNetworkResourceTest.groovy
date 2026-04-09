/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG
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

package org.onap.so.bpmn.infrastructure.scripts

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.junit.MockitoJUnitRunner
import org.onap.so.bpmn.common.scripts.MsoGroovyTest

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

@RunWith(MockitoJUnitRunner.Silent.class)
class DeleteSDNCNetworkResourceTest extends MsoGroovyTest {

    @Before
    void init() throws IOException {
        super.init("DeleteSDNCNetworkResource")
        when(mockExecution.getVariable("testProcessKey")).thenReturn("DeleteSDNCNetworkResource")
    }

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Test
    void testPreProcessRequest_setsPrefix() {
        String resourceInput = '{"resourceInstancenUuid":"ri-123","globalSubscriberId":"cust-1","serviceType":"5G",' +
                '"serviceInstanceId":"si-123","resourceModelInfo":{"modelName":"testModel","modelType":"Network",' +
                '"modelInvariantUuid":"inv-1","modelCustomizationUuid":"cust-1","modelUuid":"uuid-1","modelVersion":"1.0"},' +
                '"serviceModelInfo":{"modelInvariantUuid":"sinv-1","modelUuid":"suuid-1","modelVersion":"1.0","modelName":"svc"}}'

        when(mockExecution.getVariable("mso-request-id")).thenReturn("req-123")
        when(mockExecution.getVariable("requestAction")).thenReturn("DeleteNetworkInstance")
        when(mockExecution.getVariable("recipeParams")).thenReturn(null)
        when(mockExecution.getVariable("resourceInput")).thenReturn(resourceInput)
        when(mockExecution.getVariable("recipeParamXsd")).thenReturn(null)

        DeleteSDNCNetworkResource instance = new DeleteSDNCNetworkResource()
        instance.preProcessRequest(mockExecution)

        verify(mockExecution).setVariable(eq("DELSDNCRES_svcAction"), eq("delete"))
    }

    @Test
    void testPreProcessRequest_setsRequestAction() {
        String resourceInput = '{"resourceInstancenUuid":"ri-123","globalSubscriberId":"cust-1","serviceType":"5G",' +
                '"serviceInstanceId":"si-123","resourceModelInfo":{"modelName":"testModel","modelType":"Network",' +
                '"modelInvariantUuid":"inv-1","modelCustomizationUuid":"cust-1","modelUuid":"uuid-1","modelVersion":"1.0"},' +
                '"serviceModelInfo":{"modelInvariantUuid":"sinv-1","modelUuid":"suuid-1","modelVersion":"1.0","modelName":"svc"}}'

        when(mockExecution.getVariable("mso-request-id")).thenReturn("req-123")
        when(mockExecution.getVariable("requestAction")).thenReturn("DeleteNetworkInstance")
        when(mockExecution.getVariable("recipeParams")).thenReturn(null)
        when(mockExecution.getVariable("resourceInput")).thenReturn(resourceInput)
        when(mockExecution.getVariable("recipeParamXsd")).thenReturn(null)

        DeleteSDNCNetworkResource instance = new DeleteSDNCNetworkResource()
        instance.preProcessRequest(mockExecution)

        verify(mockExecution).setVariable(eq("DELSDNCRES_requestAction"), eq("DeleteNetworkInstance"))
    }

    @Test
    void testPreProcessRequest_setsServiceInstanceId() {
        String resourceInput = '{"resourceInstancenUuid":"ri-123","globalSubscriberId":"cust-1","serviceType":"5G",' +
                '"serviceInstanceId":"si-456","resourceModelInfo":{"modelName":"testModel","modelType":"Network",' +
                '"modelInvariantUuid":"inv-1","modelCustomizationUuid":"cust-1","modelUuid":"uuid-1","modelVersion":"1.0"},' +
                '"serviceModelInfo":{"modelInvariantUuid":"sinv-1","modelUuid":"suuid-1","modelVersion":"1.0","modelName":"svc"}}'

        when(mockExecution.getVariable("mso-request-id")).thenReturn("req-123")
        when(mockExecution.getVariable("requestAction")).thenReturn("DeleteNetworkInstance")
        when(mockExecution.getVariable("recipeParams")).thenReturn(null)
        when(mockExecution.getVariable("resourceInput")).thenReturn(resourceInput)
        when(mockExecution.getVariable("recipeParamXsd")).thenReturn(null)

        DeleteSDNCNetworkResource instance = new DeleteSDNCNetworkResource()
        instance.preProcessRequest(mockExecution)

        verify(mockExecution).setVariable(eq("DELSDNCRES_serviceInstanceId"), eq("si-456"))
    }
}
