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

package org.onap.so.bpmn.infrastructure.scripts

import org.camunda.bpm.engine.delegate.BpmnError
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.aaiclient.client.aai.AAIResourcesClient
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types

import javax.ws.rs.NotFoundException

import static org.junit.Assert.assertEquals
import static org.mockito.Mockito.doThrow
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class CreateVFCNSResourceTest extends MsoGroovyTest{

    @Spy
    CreateVFCNSResource createVFCNSResource

    @Before
    void init() throws IOException {
        mockExecution = setupMock("CreateVFCNSResource")
        client = mock(AAIResourcesClient.class)
        MockitoAnnotations.openMocks(this);
        when(createVFCNSResource.getAAIClient()).thenReturn(client)
    }

    @Test
    void testaddNSRelationship(){
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("globalSubscriberId1")
        when(mockExecution.getVariable("serviceType")).thenReturn("serviceType")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("serviceInstanceId")
        when(mockExecution.getVariable("nsInstanceId")).thenReturn("nsInstanceId")
        createVFCNSResource.addNSRelationship(mockExecution)
        AAIResourceUri nsUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer("globalSubscriberId1").serviceSubscription("serviceType").serviceInstance("nsInstanceId"))
        AAIResourceUri relatedServiceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer("globalSubscriberId1").serviceSubscription("serviceType").serviceInstance("serviceInstanceId"))
        Mockito.verify(client).connect(nsUri,relatedServiceUri)
    }

    @Test
    void testaddNSRelationshipError(){
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("globalSubscriberId1")
        when(mockExecution.getVariable("serviceType")).thenReturn("serviceType")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("serviceInstanceId")
        when(mockExecution.getVariable("nsInstanceId")).thenReturn("nsInstanceId")
        AAIResourceUri nsUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer("globalSubscriberId1").serviceSubscription("serviceType").serviceInstance("nsInstanceId"))
        AAIResourceUri relatedServiceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer("globalSubscriberId1").serviceSubscription("serviceType").serviceInstance("serviceInstanceId"))
        doThrow(new NotFoundException("Error creating relationship")).when(client).connect(nsUri, relatedServiceUri)
        try {
            createVFCNSResource.addNSRelationship(mockExecution)
        } catch (BpmnError ex) {
            assertEquals(ex.getErrorCode(),"MSOWorkflowException")
        }
    }

}
