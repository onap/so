/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.mockito.junit.MockitoJUnitRunner
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.AAIResourcesClient
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory

import javax.ws.rs.NotFoundException

import static org.junit.Assert.assertEquals
import static org.mockito.ArgumentMatchers.isA
import static org.mockito.Mockito.doNothing
import static org.mockito.Mockito.when
import static org.mockito.Mockito.doThrow

@RunWith(MockitoJUnitRunner.class)
class CreateVFCNSResourceTest {

    @Spy
    CreateVFCNSResource createVFCNSResource

    protected ExecutionEntity mockExecution
    protected AAIResourcesClient client

    @Before
    void init() throws IOException {
        MockitoAnnotations.initMocks(this)
        client = Mockito.mock(AAIResourcesClient.class)
        mockExecution = Mockito.mock(ExecutionEntity.class)
        when(createVFCNSResource.getAAIClient()).thenReturn(client)
    }

    @Test
    void testaddNSRelationship(){
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("globalSubscriberId1")
        when(mockExecution.getVariable("serviceType")).thenReturn("serviceType")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("serviceInstanceId")
        when(mockExecution.getVariable("nsInstanceId")).thenReturn("nsInstanceId")
        doNothing().when(client).connect((AAIResourceUri) isA(AAIResourceUri.class),(AAIResourceUri) isA(AAIResourceUri.class))
        createVFCNSResource.addNSRelationship(mockExecution)
        AAIResourceUri nsUri = AAIUriFactory.createResourceUri(
                AAIObjectType.SERVICE_INSTANCE,"globalSubscriberId1","serviceType","nsInstanceId")
        AAIResourceUri relatedServiceUri = AAIUriFactory.createResourceUri(
                AAIObjectType.SERVICE_INSTANCE,"globalSubscriberId1","serviceType","serviceInstanceId")
        Mockito.verify(client).connect(nsUri,relatedServiceUri)
    }

    @Test(expected = BpmnError.class)
    void testaddNSRelationshipError(){
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("globalSubscriberId1")
        when(mockExecution.getVariable("serviceType")).thenReturn("serviceType")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("serviceInstanceId")
        when(mockExecution.getVariable("nsInstanceId")).thenReturn("nsInstanceId")
        doThrow(new NotFoundException("Error creating relationship")).when(client).
                connect((AAIResourceUri) isA(AAIResourceUri.class),(AAIResourceUri) isA(AAIResourceUri.class))
        try {
            createVFCNSResource.addNSRelationship(mockExecution)
        } catch (BpmnError ex) {
            assertEquals(ex.getErrorCode(),"MSOWorkflowException")
            throw ex
        }
    }

}
