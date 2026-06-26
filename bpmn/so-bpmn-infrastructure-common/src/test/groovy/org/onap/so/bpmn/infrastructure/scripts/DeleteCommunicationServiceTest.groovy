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

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.mockito.Mockito.*
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.core.Response
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.logging.filter.base.ONAPComponents
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory

class DeleteCommunicationServiceTest extends MsoGroovyTest {

    private HttpClientFactory httpClientFactoryMock
    private HttpClient httpClientMock

    @Before
    void init() throws IOException {
        super.init("DeleteCommunicationServiceTest")
    }

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Test
    void testPreProcessRequest(){
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("12345")
        when(mockExecution.getVariable("bpmnRequest")).thenReturn("""
        {
            "globalSubscriberId ":"5GCustomer",
            "serviceType ":"5G"
        }""".replaceAll("\\\\s+", ""))
        when(mockExecution.getVariable("mso-request-id")).thenReturn("4c614769-f58a-4556-8ad9-dcd903077c82")

        DeleteCommunicationService delCS = new DeleteCommunicationService()
        delCS.preProcessRequest(mockExecution)
        Mockito.verify(mockExecution,times(3)).setVariable(captor.capture() as String, captor.capture())
        List<ExecutionEntity> values = captor.getAllValues()
        assertNotNull(values)
    }

    @Test
    void testPreInitUpdateOperationStatus(){
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("5ad89cf9-0569-4a93-9306-d8324321e2be")
        when(mockExecution.getVariable("operationId")).thenReturn("998c2081-5a71-4a39-9ae6-d6b7c5bb50c0")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("5GCustomer")
        when(mockExecution.getVariable("serviceInstanceName")).thenReturn("5G-test")
        when(mockExecution.getVariable("result")).thenReturn("processing")
        when(mockExecution.getVariable("progress")).thenReturn("0")
        when(mockExecution.getVariable("operationContent")).thenReturn("delete communication service operation start")

        DeleteCommunicationService delCS = new DeleteCommunicationService()
        delCS.preInitUpdateOperationStatus(mockExecution)
        Mockito.verify(mockExecution,times(1)).setVariable(eq("updateOperationStatus"), captor.capture())
        String updateOperationStatus= captor.getValue()
        assertNotNull(updateOperationStatus)
    }

    @Test
    void testQueryCommunicationSeriveFromAAI(){
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("5ad89cf9-0569-4a93-9306-d8324321e2be")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("5GCustomer")
        when(mockExecution.getVariable("serviceType")).thenReturn("5G")

        AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer("5GCustomer").serviceSubscription("5G").serviceInstance("5ad89cf9-0569-4a93-9306-d8324321e2be"))
        DeleteCommunicationService obj = spy(DeleteCommunicationService.class)

        AAIResultWrapper wrapper = new AAIResultWrapper(mockQueryCommunicationServiceReturn())
        when(obj.getAAIClient()).thenReturn(client)
        when(client.exists(resourceUri)).thenReturn(true)
        when(client.get(resourceUri, NotFoundException.class)).thenReturn(wrapper)
        obj.queryCommunicationSeriveFromAAI(mockExecution)
        Mockito.verify(mockExecution,times(1)).setVariable(eq("e2eSliceServiceInstanceId"), captor.capture())
        String e2eSliceServiceInstanceId = captor.getValue()
        assertNotNull(e2eSliceServiceInstanceId)
    }

    @Test
    void testPrepareCallCheckProcessStatus(){
        DeleteCommunicationService dcs = new DeleteCommunicationService()
        dcs.prepareCallCheckProcessStatus(mockExecution)
        Mockito.verify(mockExecution,times(1)).setVariable(eq("endProgress"), captor.capture())
        int endProgress = captor.getValue()
        assertEquals(90,endProgress)
    }

    @Test
    void testDelCSProfileFromAAI()
    {
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("5ad89cf9-0569-4a93-9306-d8324321e2be")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("5GCustomer")
        when(mockExecution.getVariable("serviceType")).thenReturn("5G")

        AAIResultWrapper wrapper = new AAIResultWrapper(mockQueryCommunicationServiceProfile())
        AAIPluralResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer("5GCustomer").serviceSubscription("5G").serviceInstance("5ad89cf9-0569-4a93-9306-d8324321e2be").communicationServiceProfiles())
        AAIResourceUri profileUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer("5GCustomer").serviceSubscription("5G").serviceInstance("5ad89cf9-0569-4a93-9306-d8324321e2be").communicationServiceProfile("5G-111111"))

        DeleteCommunicationService obj = spy(DeleteCommunicationService.class)
        when(obj.getAAIClient()).thenReturn(client)
        when(client.exists(resourceUri)).thenReturn(true)
        when(client.exists(profileUri)).thenReturn(true)
        when(client.get(resourceUri, NotFoundException.class)).thenReturn(wrapper)
        doNothing().when(client).delete(profileUri)
        obj.delCSProfileFromAAI(mockExecution)
        Mockito.verify(client,times(1)).delete(profileUri)
    }

    @Test
    void testPrepareFailureStatus(){
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("5ad89cf9-0569-4a93-9306-d8324321e2be")
        when(mockExecution.getVariable("operationId")).thenReturn("998c2081-5a71-4a39-9ae6-d6b7c5bb50c0")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("5GCustomer")
        when(mockExecution.getVariable("serviceInstanceName")).thenReturn("5G-test")
        when(mockExecution.getVariable("result")).thenReturn("finished")
        when(mockExecution.getVariable("progress")).thenReturn("100")
        when(mockExecution.getVariable("operationContent")).thenReturn("terminate service failure.")

        DeleteCommunicationService dcs = new DeleteCommunicationService()
        dcs.prepareFailureStatus(mockExecution)
        Mockito.verify(mockExecution,times(1)).setVariable(eq("updateOperationStatus"), captor.capture())
        String updateOperationStatus= captor.getValue()
        assertNotNull(updateOperationStatus)
    }

    @Test
    void testDelCSFromAAI(){
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("5ad89cf9-0569-4a93-9306-d8324321e2be")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("5GCustomer")
        when(mockExecution.getVariable("serviceType")).thenReturn("5G")

        AAIResourceUri serviceInstanceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer("5GCustomer").serviceSubscription("5G").serviceInstance("5ad89cf9-0569-4a93-9306-d8324321e2be"))
        DeleteCommunicationService obj = spy(DeleteCommunicationService.class)
        when(obj.getAAIClient()).thenReturn(client)
        doNothing().when(client).delete(serviceInstanceUri)

        obj.delCSFromAAI(mockExecution)
        Mockito.verify(mockExecution,times(1)).setVariable(eq("updateOperationStatus"), captor.capture())
        String updateOperationStatus= captor.getValue()
        assertNotNull(updateOperationStatus)
    }

    @Test
    void testPreFailedOperationStatus(){
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("5ad89cf9-0569-4a93-9306-d8324321e2be")
        when(mockExecution.getVariable("operationId")).thenReturn("998c2081-5a71-4a39-9ae6-d6b7c5bb50c0")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("5GCustomer")
        when(mockExecution.getVariable("serviceInstanceName")).thenReturn("5G-test")
        when(mockExecution.getVariable("result")).thenReturn("error")
        when(mockExecution.getVariable("progress")).thenReturn("100")
        when(mockExecution.getVariable("operationContent")).thenReturn("terminate service failure")

        DeleteCommunicationService deleteCommunicationService = new DeleteCommunicationService()
        WorkflowException exception = new WorkflowException("11113",7000,"terminate service failure")
        when(mockExecution.getVariable("WorkflowException")).thenReturn(exception)
        deleteCommunicationService.preFailedOperationStatus(mockExecution)

        Mockito.verify(mockExecution,times(1)).setVariable(eq("updateOperationStatus"), captor.capture())
        String updateOperationStatus= captor.getValue()
        assertNotNull(updateOperationStatus)
    }

    @Test
    void testSendRequest2NSMFWF(){
        httpClientMock = mock(HttpClient.class)
        httpClientFactoryMock = mock(HttpClientFactory.class)
        String url ="http://so.onap:8080/onap/so/infra/e2eServiceInstances/v3/5G-777"
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("5ad89cf9-0569-4a93-9306-d8324321e2be")
        when(mockExecution.getVariable("operationId")).thenReturn("998c2081-5a71-4a39-9ae6-d6b7c5bb50c0")
        when(mockExecution.getVariable("progress")).thenReturn("20")
        when(mockExecution.getVariable("operationContent")).thenReturn("waiting nsmf service delete finished")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("5GCustomer")
        when(mockExecution.getVariable("mso.infra.endpoint.url")).thenReturn("http://so.onap:8080/onap/so/infra")
        when(mockExecution.getVariable("e2eSliceServiceInstanceId")).thenReturn("5G-777")
        when(mockExecution.getVariable("e2eOperationId")).thenReturn("e151059a-d924-4629-845f-264db19e50b3")
        when(httpClientFactoryMock.newJsonClient(new URL(url), ONAPComponents.SO)).thenReturn(httpClientMock)
        DeleteCommunicationService obj = spy(DeleteCommunicationService.class)

        Response responseMock = mock(Response.class)
        when(responseMock.getStatus()).thenReturn(200)
        when(responseMock.hasEntity()).thenReturn(true)
        when(responseMock.getEntity()).thenReturn(getNSSMFResponse())
        when(obj.getHttpClientFactory()).thenReturn(httpClientFactoryMock)
        when(httpClientMock.delete(anyString())).thenReturn(responseMock)

        obj.sendRequest2NSMFWF(mockExecution)
        Mockito.verify(mockExecution,times(1)).setVariable(eq("updateOperationStatus"), captor.capture())
        String updateOperationStatus= captor.getValue()
        assertNotNull(updateOperationStatus)
    }

    private String getNSSMFResponse(){
        String response = """{
            "service":{        
                "serviceId":"e151059a-d924-4629-845f-264db19e50b4",        
                "operationId":"e151059a-d924-4629-845f-264db19e50b3"        
            }
        }"""
        return response
    }




    private String mockQueryCommunicationServiceReturn()
    {
        String expect =
                """{
                        "service-instance-id": "5G-666",
                        "service-instance-name": "eMBB_Slice_Communication_Service_5GCustomer",
                        "service-type": "eMBB",
                        "service-role": "communication-service",
                        "environment-context": "01-010101",
                        "workload-context": "12",
                        "created-at": "2019-12-11 19:56:00",
                        "description": "",
                        "model-invariant-id": "e75698d9-925a-4cdd-a6c0-edacbe6a0b51",
                        "model-version-id": "8ee5926d-720b-4bb2-86f9-d20e921c143b",
                        "service-instance-location-id": "300-01|300-02",
                        "resource-version": "1582623470778",
                        "orchestration-status": "created",
                        "relationship-list": {
                            "relationship": [
                                {
                                    "related-to": "service-instance",
                                    "relationship-label": "org.onap.relationships.inventory.ComposedOf",
                                    "related-link": "/aai/v16/business/customers/customer/5GCustomer/service-subscriptions/service-subscription/5G/service-instances/service-instance/5G-777",
                                    "relationship-data": [
                                        {
                                            "relationship-key": "customer.global-customer-id",
                                            "relationship-value": "5GCustomer"
                                        },
                                        {
                                            "relationship-key": "service-subscription.service-type",
                                            "relationship-value": "5G"
                                        },
                                        {
                                            "relationship-key": "service-instance.service-instance-id",
                                            "relationship-value": "5G-777"
                                        }
                                    ],
                                    "related-to-property": [
                                        {
                                            "property-key": "service-instance.service-instance-name",
                                            "property-value": "eMBB_e2e_Slice_Service_5GCustomer"
                                        }
                                    ]
                                }
                            ]
                        }
                    }"""
        return expect
    }

    private String mockQueryCommunicationServiceProfile()
    {
        String expect =
        """{
            "communication-service-profile": [
                {
                    "profile-id": "5G-111111",
                    "max-number-of-UEs": 50,
                    "coverage-area-list": "longgang,futian",
                    "latency": 20,
                    "exp-data-rate-UL": 300,
                    "exp-data-rate-DL": 500,
                    "ue-mobility-level": "stationary",
                    "resource-sharing-level": "Non-Shared",
                    "resource-version": "1577454950460"
                }
        ]
        }"""
        return expect
    }
}
