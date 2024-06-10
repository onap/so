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

package org.onap.so.bpmn.infrastructure.scripts

import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import jakarta.ws.rs.NotFoundException
import static org.junit.Assert.assertEquals
import static org.mockito.ArgumentMatchers.isA
import static org.mockito.Mockito.doNothing
import static org.mockito.Mockito.doThrow
import static org.mockito.Mockito.when

class DoCreateVFCNetworkServiceInstanceTest extends MsoGroovyTest {

    @Spy
    DoCreateVFCNetworkServiceInstance doCreateVFCNetworkServiceInstance

    @Before
    public void init() throws IOException {
        super.init("CreateVFCNSResource")
        MockitoAnnotations.initMocks(this)
        when(doCreateVFCNetworkServiceInstance.getAAIClient()).thenReturn(client)
    }

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Test
    public void testAddNSRelationship(){
        when(mockExecution.getVariable("nsInstanceId")).thenReturn("NS12345")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("MSO_dev")
        when(mockExecution.getVariable("serviceType")).thenReturn("MSO-dev-service-type")
        when(mockExecution.getVariable("serviceId")).thenReturn("SER12345")
        doNothing().when(client).connect(isA(AAIResourceUri.class),isA(AAIResourceUri.class))
        doCreateVFCNetworkServiceInstance.addNSRelationship(mockExecution);
        AAIResourceUri nsUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer("MSO_dev").serviceSubscription("MSO-dev-service-type").serviceInstance("NS12345"))
        AAIResourceUri relatedServiceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer("MSO_dev").serviceSubscription("MSO-dev-service-type").serviceInstance("SER12345"))
        Mockito.verify(client).connect(nsUri,relatedServiceUri)
    }

    @Test
    void testaddNSRelationshipError(){
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("globalSubscriberId1")
        when(mockExecution.getVariable("serviceType")).thenReturn("serviceType")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("serviceInstanceId")
        when(mockExecution.getVariable("nsInstanceId")).thenReturn("nsInstanceId")
        doThrow(new NotFoundException("Error creating relationship")).when(client).connect(isA(AAIResourceUri.class),isA(AAIResourceUri.class))
        try {
            doCreateVFCNetworkServiceInstance.addNSRelationship(mockExecution)
        } catch (BpmnError ex) {
            assertEquals(ex.getErrorCode(),"MSOWorkflowException")
        }
    }

}
