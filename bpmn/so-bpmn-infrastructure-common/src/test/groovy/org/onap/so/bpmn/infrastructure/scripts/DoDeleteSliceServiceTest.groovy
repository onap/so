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
import static org.junit.Assert.assertTrue
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when
import jakarta.ws.rs.NotFoundException
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.bpmn.common.scripts.MsoGroovyTest

class DoDeleteSliceServiceTest extends MsoGroovyTest {
    @Before
    void init() throws IOException {
        super.init("DoDeleteSliceServiceTest")
    }

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Test
    void testPreProcessRequest(){
        when(mockExecution.getVariable("serviceType")).thenReturn("5G")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("5GCustomer")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("e2a747a0-2ca6-476d-ac28-de999cf3fbfe")

        DoDeleteSliceService doDeleteSliceService = new DoDeleteSliceService()
        doDeleteSliceService.preProcessRequest(mockExecution)

        Mockito.verify(mockExecution,times(1)).setVariable(captor.capture() as String, captor.capture())
        List<ExecutionEntity> values = captor.getAllValues()
        assertNotNull(values)
    }

    @Test
    void testQueryE2ESliceSeriveFromAAI(){
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("5ad89cf9-0569-4a93-9306-d8324321e2be")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("5GCustomer")
        when(mockExecution.getVariable("serviceType")).thenReturn("5G")

        AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer("5GCustomer").serviceSubscription("5G").serviceInstance("5ad89cf9-0569-4a93-9306-d8324321e2be"))
        DoDeleteSliceService obj = spy(DoDeleteSliceService.class)

        AAIResultWrapper wrapper = new AAIResultWrapper(mockQuerySliceServiceReturn())
        when(obj.getAAIClient()).thenReturn(client)
        when(client.exists(resourceUri)).thenReturn(true)
        when(client.get(resourceUri, NotFoundException.class)).thenReturn(wrapper)
        obj.queryE2ESliceSeriveFromAAI(mockExecution)
        Mockito.verify(mockExecution,times(1)).setVariable(eq("snssai"), captor.capture())
        String snssai = captor.getValue()
        assertNotNull(snssai)
    }

    @Test
    void testGetAllottedResFromAAI(){
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("5ad89cf9-0569-4a93-9306-d8324321e2be")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("5GCustomer")
        when(mockExecution.getVariable("serviceType")).thenReturn("5G")

        AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(
            AAIFluentTypeBuilder.business().customer("5GCustomer").serviceSubscription("5G").serviceInstance("5ad89cf9-0569-4a93-9306-d8324321e2be").allottedResources())
            DoDeleteSliceService obj = spy(DoDeleteSliceService.class)

        AAIResultWrapper wrapper = new AAIResultWrapper(mockQueryAllottedResource())
        when(obj.getAAIClient()).thenReturn(client)
        when(client.exists(resourceUri)).thenReturn(true)
        when(client.get(resourceUri, NotFoundException.class)).thenReturn(wrapper)
        obj.getAllottedResFromAAI(mockExecution)
        Mockito.verify(mockExecution,times(1)).setVariable(eq("nsiId"), captor.capture())
        String nsiId = captor.getValue()
        assertNotNull(nsiId)
    }

    @Test
    void testGetNSIFromAAI(){
        when(mockExecution.getVariable("nsiId")).thenReturn("5G-888")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("5GCustomer")
        when(mockExecution.getVariable("serviceType")).thenReturn("5G")

        AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer("5GCustomer").serviceSubscription("5G").serviceInstance("5G-888"))
        DoDeleteSliceService obj = spy(DoDeleteSliceService.class)

        AAIResultWrapper wrapper = new AAIResultWrapper(mockNSIReturn())
        when(obj.getAAIClient()).thenReturn(client)
        when(client.exists(resourceUri)).thenReturn(true)
        when(client.get(resourceUri, NotFoundException.class)).thenReturn(wrapper)
        obj.getNSIFromAAI(mockExecution)
        Mockito.verify(mockExecution,times(1)).setVariable(eq("nssiIdList"), captor.capture())
        List<String> nssiIdList = captor.getValue()
        assertNotNull(nssiIdList)
    }

    @Test
    void testGetNSSIListFromAAI(){
        List<String> nssiIdList = []
        nssiIdList.add("5G-999")

        when(mockExecution.getVariable("nssiIdList")).thenReturn(nssiIdList)
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("5GCustomer")
        when(mockExecution.getVariable("serviceType")).thenReturn("5G")

        AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer("5GCustomer").serviceSubscription("5G").serviceInstance("5G-999"))
        DoDeleteSliceService obj = spy(DoDeleteSliceService.class)

        AAIResultWrapper wrapper = new AAIResultWrapper(mockNSSIReturn())
        when(obj.getAAIClient()).thenReturn(client)
        when(client.exists(resourceUri)).thenReturn(true)
        when(client.get(resourceUri, NotFoundException.class)).thenReturn(wrapper)
        obj.getNSSIListFromAAI(mockExecution)
        Mockito.verify(mockExecution,times(1)).setVariable(eq("nssiInstanceList"), captor.capture())
        List<ServiceInstance> nssiInstanceList = captor.getValue()
        assertNotNull(nssiInstanceList)
    }

    @Test
    void testGetCurrentNSSI(){
        ServiceInstance nssi = new ServiceInstance()
        nssi.setServiceInstanceId("5G-999")
        nssi.setModelInvariantId("21d57d4b-52ad-4d3c-a798-248b5bb9124a")
        nssi.setModelVersionId("bfba363e-e39c-4bd9-a9d5-1371c28f4d22")
        List<ServiceInstance> nssiInstanceList = []
        nssiInstanceList.add(nssi)
        when(mockExecution.getVariable("currentNSSIIndex")).thenReturn(0)
        when(mockExecution.getVariable("nssiInstanceList")).thenReturn(nssiInstanceList)
        when(mockExecution.getVariable("snssai")).thenReturn("01-010101")
        when(mockExecution.getVariable("nsiId")).thenReturn("5G-888")
        when(mockExecution.getVariable("operationId")).thenReturn("998c2081-5a71-4a39-9ae6-d6b7c5bb50c0")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("5G-777")
        when(mockExecution.getVariable("msoRequestId")).thenReturn("4c614769-f58a-4556-8ad9-dcd903077c82")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("5GCustomer")
        when(mockExecution.getVariable("serviceType")).thenReturn("5G")
        when(mockExecution.getVariable("proportion")).thenReturn("90")

        DoDeleteSliceService ddss = new DoDeleteSliceService()
        ddss.getCurrentNSSI(mockExecution)
        verify(mockExecution,times(1)).setVariable(eq("currentNSSI"), captor.capture())
        Map currentNSSI = captor.getValue()
        assertTrue(currentNSSI.size()>0)
    }

    @Test
    void testQuerySliceProfileFromAAI(){
        def currentNSSI = [:]
        currentNSSI.put("nssiServiceInstanceId","5G-999")
        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("5GCustomer")
        when(mockExecution.getVariable("serviceType")).thenReturn("5G")

        AAIResultWrapper wrapper = new AAIResultWrapper(mockSliceProfile())
        AAIPluralResourceUri profileUri = AAIUriFactory.createResourceUri(
            AAIFluentTypeBuilder.business().customer("5GCustomer").serviceSubscription("5G").serviceInstance("5G-999").sliceProfiles())

        DoDeleteSliceService obj = spy(DoDeleteSliceService.class)
        when(obj.getAAIClient()).thenReturn(client)
        when(client.exists(profileUri)).thenReturn(true)
        when(client.get(profileUri, NotFoundException.class)).thenReturn(wrapper)
        obj.querySliceProfileFromAAI(mockExecution)
        verify(mockExecution,times(1)).setVariable(eq("currentNSSI"), captor.capture())
        Map value = captor.getValue()
        assertNotNull(currentNSSI.get('profileId'))
    }

    @Test
    void parseNextNSSI(){
        ServiceInstance nssi = new ServiceInstance()
        nssi.setServiceInstanceId("5G-999")
        nssi.setModelInvariantId("21d57d4b-52ad-4d3c-a798-248b5bb9124b")
        nssi.setModelVersionId("bfba363e-e39c-4bd9-a9d5-1371c28f4d22")
        List<ServiceInstance> nssiInstanceList = []
        nssiInstanceList.add(nssi)
        when(mockExecution.getVariable("currentNSSIIndex")).thenReturn(0)
        when(mockExecution.getVariable("nssiInstanceList")).thenReturn(nssiInstanceList)

        DoDeleteSliceService ddss = new DoDeleteSliceService()
        ddss.parseNextNSSI(mockExecution)
        verify(mockExecution,times(1)).setVariable(eq("isAllNSSIFinished"), captor.capture())
        boolean isAllNSSIFinished = captor.getValue()
        assertTrue(isAllNSSIFinished)
    }

    private String mockSliceProfile(){
        String expect =
                """{
                        "slice-profile": [
                            {
                                "profile-id": "ddf57704-fe8d-417b-882d-2f2a12ddb225",
                                "latency": 20,
                                "max-number-of-UEs": 0,
                                "coverage-area-TA-list": "[{\\"province\\":\\"??\\",\\"city\\":\\"???\\",\\"county\\":\\"???\\",\\"street\\":\\"?????\\"}]",
                                "ue-mobility-level": "stationary",
                                "resource-sharing-level": "0",
                                "exp-data-rate-UL": 100,
                                "exp-data-rate-DL": 100,
                                "activity-factor": 0,
                                "e2e-latency": 0,
                                "jitter": 0,
                                "survival-time": 0,
                                "exp-data-rate": 0,
                                "payload-size": 0,
                                "traffic-density": 0,
                                "conn-density": 0,
                                "s-nssai": "01003",
                                "resource-version": "1580800791373"
                            }
                        ]
                    }
                """
        return expect
    }

    private String mockNSSIReturn(){
        String expect =
                """
                    {
                        "service-instance-id": "5G-999",
                        "service-instance-name": "eMBB_Slice_NSSI_5GCustomer",
                        "service-type": "eMBB",
                        "service-role": "nssi",
                        "environment-context": "cn",
                        "model-invariant-id": "21d57d4b-52ad-4d3c-a798-248b5bb9124a",
                        "model-version-id": "bfba363e-e39c-4bd9-a9d5-1371c28f4d22",
                        "service-instance-location-id": "300-01|300-02",
                        "resource-version": "1578449638032",
                        "orchestration-status": "activated",
                        "relationship-list": {
                            "relationship": [
                                {
                                    "related-to": "service-instance",
                                    "relationship-label": "org.onap.relationships.inventory.ComposedOf",
                                    "related-link": "/aai/v16/business/customers/customer/5GCustomer/service-subscriptions/service-subscription/5G/service-instances/service-instance/5G-888",
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
                                            "relationship-value": "5G-888"
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
                    }
                """
        return expect
    }

    private String mockNSIReturn(){
        String expect =
                """
                    {
                        "service-instance-id": "5G-888",
                        "service-instance-name": "eMBB_e2e_Slice_Service_5GCustomer",
                        "service-type": "embb",
                        "service-role": "nsi",
                        "model-invariant-id": "0e9bcb9a-c832-433b-a0c1-74866768f608",
                        "model-version-id": "2c5fd79d-0f84-4057-9222-952cb6f27036",
                        "service-instance-location-id": "300-01|300-02",
                        "resource-version": "1579691104911",
                        "orchestration-status": "activated",
                        "relationship-list": {
                            "relationship": [
                                {
                                    "related-to": "service-instance",
                                    "relationship-label": "org.onap.relationships.inventory.ComposedOf",
                                    "related-link": "/aai/v16/business/customers/customer/5GCustomer/service-subscriptions/service-subscription/5G/service-instances/service-instance/5G-999",
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
                                            "relationship-value": "5G-999"
                                        }
                                    ],
                                    "related-to-property": [
                                        {
                                            "property-key": "service-instance.service-instance-name",
                                            "property-value": "eMBB_Slice_NSSI_5GCustomer"
                                        }
                                    ]
                                },
                                {
                                    "related-to": "allotted-resource",
                                    "relationship-label": "org.onap.relationships.inventory.Uses",
                                    "related-link": "/aai/v16/business/customers/customer/5GCustomer/service-subscriptions/service-subscription/5G/service-instances/service-instance/5G-777/allotted-resources/allotted-resource/5G-1234",
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
                                        },
                                        {
                                            "relationship-key": "allotted-resource.id",
                                            "relationship-value": "5G-1234"
                                        }
                                    ],
                                    "related-to-property": [
                                        {
                                            "property-key": "allotted-resource.description"
                                        },
                                        {
                                            "property-key": "allotted-resource.allotted-resource-name"
                                        }
                                    ]
                                }
                            ]
                        }
                    }
                """
        return expect
    }

    private String mockQueryAllottedResource(){
        String expect =
                """{
                        "allotted-resource": [
                            {
                                "id": "5G-1234",
                                "resource-version": "1577454983471",
                                "relationship-list": {
                                    "relationship": [
                                        {
                                            "related-to": "service-instance",
                                            "relationship-label": "org.onap.relationships.inventory.Uses",
                                            "related-link": "/aai/v16/business/customers/customer/5GCustomer/service-subscriptions/service-subscription/5G/service-instances/service-instance/5G-888",
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
                                                    "relationship-value": "5G-888"
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
                            }
                        ]
                    }
                """
        return expect
    }

    String mockQuerySliceServiceReturn(){
        String expect =
                """{
                        "service-instance-id": "5G-777",
                        "service-instance-name": "eMBB_e2e_Slice_Service_5GCustomer",
                        "service-type": "embb",
                        "service-role": "e2eslice-service",
                        "environment-context": "01-010101",
                        "model-invariant-id": "e65d737a-41e0-4ad1-958f-56defdf2e907",
                        "model-version-id": "f2f5967e-72d3-4c5c-b880-e214e71dba4e",
                        "service-instance-location-id": "300-01|300-02",
                        "resource-version": "1578449638436",
                        "orchestration-status": "activated",
                        "relationship-list": {
                            "relationship": [
                                {
                                    "related-to": "service-instance",
                                    "relationship-label": "org.onap.relationships.inventory.ComposedOf",
                                    "related-link": "/aai/v16/business/customers/customer/5GCustomer/service-subscriptions/service-subscription/5G/service-instances/service-instance/5G-666",
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
                                            "relationship-value": "5G-666"
                                        }
                                    ],
                                    "related-to-property": [
                                        {
                                            "property-key": "service-instance.service-instance-name",
                                            "property-value": "eMBB_Slice_Communication_Service_5GCustomer"
                                        }
                                    ]
                                }
                            ]
                        }
                    }
                """
        return expect
    }

}
