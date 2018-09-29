package org.onap.so.bpmn.infrastructure.scripts

import org.camunda.bpm.engine.delegate.BpmnError
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory

import javax.ws.rs.NotFoundException

import static org.junit.Assert.assertEquals
import static org.mockito.Matchers.isA
import static org.mockito.Mockito.doNothing
import static org.mockito.Mockito.doThrow
import static org.mockito.Mockito.when

class CreateVFCNSResourceTest extends MsoGroovyTest{

    @Spy
    CreateVFCNSResource createVFCNSResource

    @Before
    void init() throws IOException {
        super.init("CreateVFCNSResource")
        MockitoAnnotations.initMocks(this);
        when(createVFCNSResource.getAAIClient()).thenReturn(client)
    }

    @Test
    void testaddNSRelationship(){
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("globalSubscriberId1")
        when(mockExecution.getVariable("serviceType")).thenReturn("serviceType")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("serviceInstanceId")
        when(mockExecution.getVariable("nsInstanceId")).thenReturn("nsInstanceId")
        doNothing().when(client).connect(isA(AAIResourceUri.class),isA(AAIResourceUri.class))
        createVFCNSResource.addNSRelationship(mockExecution)
        AAIResourceUri nsUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,"globalSubscriberId1","serviceType","nsInstanceId")
        AAIResourceUri relatedServiceUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE,"globalSubscriberId1","serviceType","serviceInstanceId")
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
            createVFCNSResource.addNSRelationship(mockExecution)
        } catch (BpmnError ex) {
            assertEquals(ex.getErrorCode(),"MSOWorkflowException")
        }
    }

}
