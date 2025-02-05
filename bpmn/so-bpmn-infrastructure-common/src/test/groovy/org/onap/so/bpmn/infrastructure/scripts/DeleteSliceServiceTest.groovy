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

import static org.junit.Assert.assertNotNull
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.doNothing
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.times
import static org.mockito.Mockito.when
import jakarta.ws.rs.NotFoundException
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.so.bpmn.core.WorkflowException

class DeleteSliceServiceTest extends MsoGroovyTest {
    @Before
    void init() throws IOException {
        super.init("DeleteSliceServiceTest")
    }

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Test
    void testPreProcessRequest(){
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("eb0863e9-a69b-4b17-8a56-f05ad110bef7")
        when(mockExecution.getVariable("operationId")).thenReturn("998c2081-5a71-4a39-9ae6-d6b7c5bb50c0")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("5GCustomer")
        when(mockExecution.getVariable("serviceInstanceName")).thenReturn("5G-test")
        when(mockExecution.getVariable("serviceType")).thenReturn("5G")
        when(mockExecution.getVariable("result")).thenReturn("processing")
        when(mockExecution.getVariable("progress")).thenReturn("0")
        when(mockExecution.getVariable("operationContent")).thenReturn("Delete Slice service operation start")
        when(mockExecution.getVariable("bpmnRequest")).thenReturn("""
        {
            "globalSubscriberId ":"5GCustomer",
            "serviceType ":"5G"
        }""".replaceAll("\\\\s+", ""))
        when(mockExecution.getVariable("mso-request-id")).thenReturn("4c614769-f58a-4556-8ad9-dcd903077c82")

        DeleteSliceService delSS = new DeleteSliceService()
        delSS.preProcessRequest(mockExecution)
        Mockito.verify(mockExecution,times(1)).setVariable(eq("updateOperationStatus"), captor.capture())
        String updateOperationStatus = captor.getValue()
        assertNotNull(updateOperationStatus)
    }

    @Test
    void testDeleteSliceServiceInstance(){
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("5ad89cf9-0569-4a93-9306-d8324321e2be")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("5GCustomer")
        when(mockExecution.getVariable("serviceType")).thenReturn("5G")

        when(mockExecution.getVariable("result")).thenReturn("finished")
        when(mockExecution.getVariable("progress")).thenReturn("100")
        when(mockExecution.getVariable("operationContent")).thenReturn("NSMF completes slicing service termination.")

        AAIResourceUri serviceInstanceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer("5GCustomer").serviceSubscription("5G").serviceInstance("5ad89cf9-0569-4a93-9306-d8324321e2be"))
        DeleteSliceService obj = spy(DeleteSliceService.class)
        when(obj.getAAIClient()).thenReturn(client)
        doNothing().when(client).delete(serviceInstanceUri)

        obj.deleteSliceServiceInstance(mockExecution)
        Mockito.verify(mockExecution,times(1)).setVariable(eq("updateOperationStatus"), captor.capture())
        String updateOperationStatus= captor.getValue()
        assertNotNull(updateOperationStatus)
    }

    @Test
    void testDelServiceProfileFromAAI(){
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("5ad89cf9-0569-4a93-9306-d8324321e2be")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("5GCustomer")
        when(mockExecution.getVariable("serviceType")).thenReturn("5G")

        AAIResultWrapper wrapper = new AAIResultWrapper(mockQuerySliceServiceProfile())
        AAIPluralResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer("5GCustomer").serviceSubscription("5G").serviceInstance("5ad89cf9-0569-4a93-9306-d8324321e2be").serviceProfiles())
        AAIResourceUri profileUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer("5GCustomer").serviceSubscription("5G").serviceInstance("5ad89cf9-0569-4a93-9306-d8324321e2be").serviceProfile("5G-2222222"))

        DeleteSliceService obj = spy(DeleteSliceService.class)
        when(obj.getAAIClient()).thenReturn(client)
        when(client.exists(resourceUri)).thenReturn(true)
        when(client.exists(profileUri)).thenReturn(true)
        when(client.get(resourceUri, NotFoundException.class)).thenReturn(wrapper)
        doNothing().when(client).delete(profileUri)
        obj.delServiceProfileFromAAI(mockExecution)
        Mockito.verify(client,times(1)).delete(profileUri)
    }

    @Test
    void testPrepareEndOperationStatus(){
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("5ad89cf9-0569-4a93-9306-d8324321e2be")
        when(mockExecution.getVariable("operationId")).thenReturn("998c2081-5a71-4a39-9ae6-d6b7c5bb50c0")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("5GCustomer")
        when(mockExecution.getVariable("serviceInstanceName")).thenReturn("5G-test")
        when(mockExecution.getVariable("result")).thenReturn("error")
        when(mockExecution.getVariable("progress")).thenReturn("100")
        when(mockExecution.getVariable("operationContent")).thenReturn("terminate service failure")

        DeleteSliceService deleteSliceService = new DeleteSliceService()
        WorkflowException exception = new WorkflowException("11113",7000,"terminate service failure")
        when(mockExecution.getVariable("WorkflowException")).thenReturn(exception)
        deleteSliceService.prepareEndOperationStatus(mockExecution)

        Mockito.verify(mockExecution,times(1)).setVariable(eq("updateOperationStatus"), captor.capture())
        String updateOperationStatus= captor.getValue()
        assertNotNull(updateOperationStatus)

    }

    private String mockQuerySliceServiceProfile(){
        String expect =
            """{
                "service-profile": [
                    {
                        "profile-id": "5G-2222222",
                        "latency": 50,
                        "max-number-of-UEs": 500,
                        "coverage-area-TA-list": "longgang,futian",
                        "ue-mobility-level": "stationary",
                        "resource-sharing-level": "Non-Shared",
                        "exp-data-rate-UL": 10,
                        "exp-data-rate-DL": 30,
                        "area-traffic-cap-UL": 100,
                        "area-traffic-cap-DL": 100,
                        "activity-factor": 80,
                        "jitter": 10,
                        "survival-time": 30,
                        "cs-availability": 95.5,
                        "reliability": 99.9,
                        "exp-data-rate": 80,
                        "traffic-density": 100,
                        "conn-density": 80,
                        "resource-version": "1577454958647"
                    }
            ]
            }"""
        return expect
    }

}
