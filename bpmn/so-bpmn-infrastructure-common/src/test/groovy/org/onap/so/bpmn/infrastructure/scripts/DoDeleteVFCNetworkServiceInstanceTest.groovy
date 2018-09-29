package org.onap.so.bpmn.infrastructure.scripts

import org.camunda.bpm.engine.delegate.BpmnError
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.onap.aai.domain.yang.VolumeGroup
import org.onap.aai.domain.yang.VolumeGroups
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.so.client.aai.AAIObjectPlurals
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory
import org.onap.so.constants.Defaults

import static org.mockito.Matchers.isA
import static org.mockito.Mockito.doThrow
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.when

public class DoDeleteVFCNetworkServiceInstanceTest extends MsoGroovyTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private  DoDeleteVFCNetworkServiceInstance doDeleteVFCNetworkServiceInstance;
    @Before
    public void init(){
        super.init("DoDeleteVFCNetworkServiceInstance");
        doDeleteVFCNetworkServiceInstance = spy(DoDeleteVFCNetworkServiceInstance.class);
        when(doDeleteVFCNetworkServiceInstance.getAAIClient()).thenReturn(client)
    }

    @Test
    void callRESTDeleteAAIVolumeGroupTest(){
        String resourceInstanceId = "resourceInstanceId"
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("resourceInstanceId")).thenReturn(resourceInstanceId)
        doDeleteVFCNetworkServiceInstance.deleteNSRelationship(mockExecution)
    }

    @Test
    void callRESTDeleteAAIVolumeGroupTestException(){
        String resourceInstanceId = "resourceInstanceId"
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("resourceInstanceId")).thenReturn(resourceInstanceId)
        doThrow(Exception.class).when(client).disconnect(isA(AAIResourceUri.class),isA(AAIResourceUri.class))
        thrown.expect(BpmnError.class)
        doDeleteVFCNetworkServiceInstance.deleteNSRelationship(mockExecution)
    }

}
