/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 # Copyright (c) 2019, CMCC Technologies Co., Ltd.
 #
 # Licensed under the Apache License, Version 2.0 (the "License")
 # you may not use this file except in compliance with the License.
 # You may obtain a copy of the License at
 #
 #       http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing, software
 # distributed under the License is distributed on an "AS IS" BASIS,
 # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 # See the License for the specific language governing permissions and
 # limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.so.bpmn.infrastructure.scripts

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.onap.logging.filter.base.ONAPComponents
import org.onap.so.beans.nsmf.JobStatusResponse
import org.onap.so.beans.nsmf.NssiResponse
import org.onap.so.beans.nsmf.ResponseDescriptor
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.so.bpmn.core.domain.ServiceArtifact
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.ServiceInfo
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types

import jakarta.ws.rs.core.Response

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.doNothing
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.times
import static org.mockito.Mockito.when

class DoDeallocateNSSITest extends MsoGroovyTest {

    private HttpClientFactory httpClientFactoryMock
    private HttpClient httpClientMock

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

        AAIResourceUri profileUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer("5GCustomer").serviceSubscription("5G").serviceInstance("5G-999").sliceProfile("ddf57704-fe8d-417b-882d-2f2a12ddb225"))
        DoDeallocateNSSI obj = spy(DoDeallocateNSSI.class)
        when(obj.getAAIClient()).thenReturn(client)
        when(client.exists(profileUri)).thenReturn(true)
        doNothing().when(client).delete(profileUri)

        obj.delSliceProfileFromAAI(mockExecution)
        Mockito.verify(client,times(1)).delete(profileUri)
    }

    @Test
    void testSendRequestToNSSMF(){
        httpClientFactoryMock = mock(HttpClientFactory.class)
        httpClientMock = mock(HttpClient.class)

        def currentNSSI = [:]
        currentNSSI.put("snssai", "01-010101")
        currentNSSI.put("profileId", "ddf57704-fe8d-417b-882d-2f2a12ddb225")
        currentNSSI.put("nssiServiceInstanceId","5G-999")
        currentNSSI.put("nsiServiceInstanceId","5G-888")

        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)
        when(mockExecution.getVariable("mso.adapters.nssmf.endpoint")).thenReturn("http://so-nssmf-adapter.onap:8088")
        String nssmfRequest = "http://so-nssmf-adapter.onap:8088/api/rest/provMns/v1/NSS/SliceProfiles/ddf57704-fe8d-417b-882d-2f2a12ddb225"

        when(httpClientFactoryMock.newJsonClient(new URL(nssmfRequest), ONAPComponents.EXTERNAL)).thenReturn(httpClientMock)
        DoDeallocateNSSI obj = spy(DoDeallocateNSSI.class)
        when(obj.getHttpClientFactory()).thenReturn(httpClientFactoryMock)
        Response responseMock = mock(Response.class)
        NssiResponse response = new NssiResponse()
        response.setNssiId("NSSI-C-004-HDBHZ-NSSMF-01-A-HW")
        response.setJobId("a5c5913d-448a-bcb1-9b800a944d84")
        when(httpClientMock.post(anyString())).thenReturn(responseMock)
        when(responseMock.getStatus()).thenReturn(202)
        when(responseMock.readEntity(NssiResponse.class)) thenReturn(response)
        when(responseMock.hasEntity()).thenReturn(true)

        obj.sendRequestToNSSMF(mockExecution)
        String jobId = currentNSSI['jobId']
        assertNotNull(jobId)
    }

    @Test
    void testGetJobStatus(){
        httpClientFactoryMock = mock(HttpClientFactory.class)
        httpClientMock = mock(HttpClient.class)

        def currentNSSI = [:]
        currentNSSI.put("jobId", "a5c5913d-448a-bcb1-9b800a944d84")
        currentNSSI.put("nssiServiceInstanceId","5G-999")
        currentNSSI.put("nsiServiceInstanceId","5G-888")
        currentNSSI.put("jobProgress",60)

        when(mockExecution.getVariable("isNSSIDeAllocated")).thenReturn(false)
        when(mockExecution.getVariable("isNSSIDeAllocated")).thenReturn(false)
        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)
        when(mockExecution.getVariable("mso.adapters.nssmf.endpoint")).thenReturn("http://so-nssmf-adapter.onap:8088")
        String nssmfRequest = "http://so-nssmf-adapter.onap:8088/api/rest/provMns/v1/NSS/jobs/a5c5913d-448a-bcb1-9b800a944d84"

        when(httpClientFactoryMock.newJsonClient(new URL(nssmfRequest), ONAPComponents.EXTERNAL)).thenReturn(httpClientMock)
        DoDeallocateNSSI obj = spy(DoDeallocateNSSI.class)
        when(obj.getHttpClientFactory()).thenReturn(httpClientFactoryMock)
        Response responseMock = mock(Response.class)
        ResponseDescriptor descriptor = new ResponseDescriptor()
        descriptor.setProgress(100)
        descriptor.setStatusDescription("finished deallocate nssi")
        JobStatusResponse jobStatusResponse = new JobStatusResponse()
        jobStatusResponse.setResponseDescriptor(descriptor)
        when(httpClientMock.post(anyString())).thenReturn(responseMock)
        when(responseMock.getStatus()).thenReturn(202)
        when(responseMock.readEntity(JobStatusResponse.class)) thenReturn(jobStatusResponse)
        when(responseMock.hasEntity()).thenReturn(true)

        obj.getJobStatus(mockExecution)
        Mockito.verify(mockExecution,times(1)).setVariable(eq("isNSSIDeAllocated"), captor.capture())
        boolean value = captor.getValue()
        assertTrue(value)
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
