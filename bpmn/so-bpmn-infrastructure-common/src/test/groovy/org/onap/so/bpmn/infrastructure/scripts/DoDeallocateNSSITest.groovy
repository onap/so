package org.onap.so.bpmn.infrastructure.scripts

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.checkerframework.checker.units.qual.A
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.so.bpmn.core.domain.ServiceArtifact
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.ServiceInfo
import org.onap.so.client.aai.AAIObjectType
import org.onap.so.client.aai.entities.AAIResultWrapper
import org.onap.so.client.aai.entities.uri.AAIResourceUri
import org.onap.so.client.aai.entities.uri.AAIUriFactory

import static org.junit.Assert.assertNotNull
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.doNothing
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.times
import static org.mockito.Mockito.when

class DoDeallocateNSSITest extends MsoGroovyTest {

    @Before
    void init() throws IOException {
        super.init("DoDeallocateNSSITest")
    }

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)


    @Test
    void testPreProcessRequest(){
        def currentNSSI = [:]
        currentNSSI.put("nssiServiceInstanceId","5G-999")
        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        DoDeallocateNSSI ddnssi = new DoDeallocateNSSI()
        ddnssi.preProcessRequest(mockExecution)
        Mockito.verify(mockExecution,times(1)).getVariable("currentNSSI")
    }

    @Test
    void testPrepareDecomposeService(){
        def currentNSSI = [:]
        currentNSSI.put("modelInvariantId", "21d57d4b-52ad-4d3c-a798-248b5bb9124b")
        currentNSSI.put("modelVersionId", "bfba363e-e39c-4bd9-a9d5-1371c28f4d22")
        currentNSSI.put("nssiServiceInstanceId","5G-999")
        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        DoDeallocateNSSI ddnssi = new DoDeallocateNSSI()
        ddnssi.prepareDecomposeService(mockExecution)
        Mockito.verify(mockExecution,times(1)).setVariable(eq("serviceModelInfo"), captor.capture())
        String serviceModelInfo = captor.getValue()
        assertNotNull(serviceModelInfo)
    }

    @Test
    void testProcessDecomposition(){
        def currentNSSI = [:]
        ServiceArtifact artifact = new ServiceArtifact()
        artifact.setContent(getArtifactContent())
        ServiceInfo serviceInfo = new ServiceInfo()
        List<ServiceArtifact> artifactList = new ArrayList<>()
        artifactList.add(artifact)
        serviceInfo.setServiceArtifact(artifactList)
        ServiceDecomposition decomposition = new ServiceDecomposition()
        decomposition.setServiceInfo(serviceInfo)
        when(mockExecution.getVariable("serviceDecomposition")).thenReturn(decomposition)
        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        DoDeallocateNSSI ddnssi = new DoDeallocateNSSI()
        ddnssi.processDecomposition(mockExecution)
        String vendor = currentNSSI.get("vendor")
        assertNotNull(vendor)
    }

    @Test
    void testHandleJobStatus(){
        def currentNSSI = [:]
        currentNSSI.put("jobProgress", 10)
        currentNSSI.put("proportion", 90)
        currentNSSI.put("statusDescription","")
        currentNSSI.put("e2eServiceInstanceId","21d57d4b-52ad-4d3c-a798-248b5bb9124b")
        currentNSSI.put("operationId","4c614769-f58a-4556-8ad9-dcd903077c82")
        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        DoDeallocateNSSI ddnssi = new DoDeallocateNSSI()
        ddnssi.handleJobStatus(mockExecution)
        Mockito.verify(mockExecution,times(1)).setVariable(eq("updateOperationStatus"), captor.capture())
        String updateOperationStatus= captor.getValue()
        assertNotNull(updateOperationStatus)
    }

    @Test
    void testDelSliceProfileFromAAI(){
        def currentNSSI = [:]
        currentNSSI.put("nssiServiceInstanceId", "5G-999")
        currentNSSI.put("profileId", "ddf57704-fe8d-417b-882d-2f2a12ddb225")
        currentNSSI.put("globalSubscriberId","5GCustomer")
        currentNSSI.put("serviceType","5G")
        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        AAIResourceUri profileUri = AAIUriFactory.createResourceUri(AAIObjectType.SLICE_PROFILE, "5GCustomer", "5G", "5G-999", "ddf57704-fe8d-417b-882d-2f2a12ddb225")
        DoDeallocateNSSI obj = spy(DoDeallocateNSSI.class)
        when(obj.getAAIClient()).thenReturn(client)
        when(client.exists(profileUri)).thenReturn(true)
        doNothing().when(client).delete(profileUri)

        obj.delSliceProfileFromAAI(mockExecution)
        Mockito.verify(client,times(1)).delete(profileUri)
    }


    private String getArtifactContent(){
        String content =
                """
                    {
                        "metadata":{
                            "id":"NSST-C-001-HDBNJ-NSSMF-01-A-HW",
                            "vendor":"HW",
                            "version":"1.0",
                            "name":"eMBB_demo",
                            "description":"eMBB for demo",
                            "type":"embb",
                            "domainType":"cn"
                        },
                        "capabilities":{
                            "latency":{
                                "type":"integer",
                                "constrainstsl":"less_or_equal",
                                "value":"20"
                            },
                            "areaTrafficCapDL":{
                                "type":"integer",
                                "constrainstsl":"less_or_equal",
                                "value":"300"
                            },
                            "areaTrafficCapUL":{
                                "type":"integer",
                                "constrainstsl":"less_or_equal",
                                "value":"300"
                            },
                            "maxNumberofUEs":{
                                "type":"integer",
                                "constrainstsl":"less_or_equal",
                                "value":"300"
                            }
                        }
                    }
                """
    }
}
