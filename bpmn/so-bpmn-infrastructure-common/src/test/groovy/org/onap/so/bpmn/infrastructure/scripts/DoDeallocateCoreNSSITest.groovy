/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020  Telecom Italia
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

import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.onap.aai.domain.yang.v19.AllottedResource
import org.onap.aai.domain.yang.v19.AllottedResources
import org.onap.aai.domain.yang.v19.ServiceInstance
import org.onap.aai.domain.yang.v19.SliceProfile
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.Relationships
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.bpmn.common.scripts.ExternalAPIUtil
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.so.bpmn.common.scripts.OofUtils
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory

import jakarta.ws.rs.core.Response

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue
import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.*

class DoDeallocateCoreNSSITest extends MsoGroovyTest {

    @Before
    void init() throws IOException {
        super.init("DoDeallocateNSSITest")
    }


    @Test
    void testPreProcessRequest() {

        String nssiId = "5G-999"
        when(mockExecution.getVariable("serviceInstanceID")).thenReturn(nssiId)

        String nsiId = "5G-777"
        when(mockExecution.getVariable("nsiId")).thenReturn(nsiId)

        String snssai = "S-NSSAI"
        String snssaiList = "[ \"${snssai}\" ]"
        String sliceProfileId = "slice-profile-id"
        String modifyAction = "allocate"
        String sliceParams =  "{\n" +
                "\"sliceProfileId\":\"${sliceProfileId}\",\"snssaiList\":${snssaiList}\n" +
                "}"
        when(mockExecution.getVariable("sliceParams")).thenReturn(sliceParams)

        DoDeallocateCoreNSSI obj = new DoDeallocateCoreNSSI()
        obj.preProcessRequest(mockExecution)

        def currentNSSI = [:]
        currentNSSI.put("nssiId", nssiId)
        currentNSSI.put("nsiId", nsiId)
        currentNSSI.put("sliceProfile", sliceParams)
        currentNSSI.put("S-NSSAI", snssai)
        currentNSSI.put("sliceProfileId", sliceProfileId)
        Mockito.verify(mockExecution,times(1)).setVariable("currentNSSI", currentNSSI)

    }


    @Test
    void testExecuteTerminateNSSIQuery() {

        def currentNSSI = [:]

        String nssiId = "5G-999"
        currentNSSI.put("nssiId", nssiId)

        String nsiId = "ns-777"
        currentNSSI.put("nsiId", nsiId)

        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        when(mockExecution.getVariable("mso.oof.endpoint")).thenReturn("http://oof.onap:8088")
        when(mockExecution.getVariable("mso.oof.auth")).thenReturn("mso.oof.auth")
        when(mockExecution.getVariable("mso.msoKey")).thenReturn("mso.msoKey")
        when(mockExecution.getVariable("mso-request-id")).thenReturn("mso-request-id")

        DoDeallocateCoreNSSI spy = spy(DoDeallocateCoreNSSI.class)
        when(spy.getAAIClient()).thenReturn(client)

        OofUtils oofUtilsMock = mock(OofUtils.class)
        when(spy.getOofUtils()).thenReturn(oofUtilsMock)

        when(spy.encryptBasicAuth("mso.oof.auth", "mso.msoKey")).thenReturn("auth-value")

        String authHeaderResponse =  "auth-header"

        when(spy.getAuthHeader(mockExecution, "auth-value", "mso.msoKey")).thenReturn(authHeaderResponse)

        AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(nssiId))

        ServiceInstance nssi = new ServiceInstance()
        nssi.setServiceInstanceId("5G-999")
        Optional<ServiceInstance> nssiOpt = Optional.of(nssi)

        when(client.get(ServiceInstance.class, nssiUri)).thenReturn(nssiOpt)

        AAIResourceUri nsiUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(nsiId))

        ServiceInstance nsi = new ServiceInstance()
        nsi.setServiceInstanceId("5G-777")
        Optional<ServiceInstance> nsiOpt = Optional.of(nsi)

        when(client.get(ServiceInstance.class, nsiUri)).thenReturn(nsiOpt)

        String urlString = "http://oof.onap:8088"

        String httpRequest =    "{\n" +
                "  \"type\": \"NSSI\",\n" +
                "  \"NxIId\": \"5G-999\",\n" +
                "  \"requestInfo\": {\n" +
                "    \"transactionId\": \"mso-request-id\",\n" +
                "    \"requestId\": \"mso-request-id\",\n" +
                "    \"sourceId\": \"so\",\n" +
                "    }\n" +
                "}"

        String requestId = "request-id"
        String nxlId = nssi.getServiceInstanceId()
        String nxlType = "NSSI"
        String messageType = "cn"
        String serviceInstanceId = nsi.getServiceInstanceId()

        when(mockExecution.getVariable("msoRequestId")).thenReturn(requestId)
        when(oofUtilsMock.buildTerminateNxiRequest(requestId, nxlId, nxlType, messageType, serviceInstanceId)).thenReturn(httpRequest)

        String terminateResponse = "false"

        String oofResponse =   "{\n" +
                " \"requestId\": \"mso-request-id\",\n" +
                " \"transactionId\": \"mso-request-id\",\n" +
                " \"statusMessage\": \"\",\n" +
                " \"requestStatus\": \"accepted\",\n" +
                " \"terminateResponse\": \"${terminateResponse}\",\n" +
                " \"reason\": \"\"\n" +
                " }\n"

        String apiPath =  "/api/oof/terminate/nxi/v1"

        urlString = urlString + apiPath

        HttpClientFactory httpClientFactoryMock = mock(HttpClientFactory.class)
        when(spy.getHttpClientFactory()).thenReturn(httpClientFactoryMock)
        Response responseMock = mock(Response.class)

        HttpClient httpClientMock = mock(HttpClient.class)

        when(httpClientFactoryMock.newJsonClient(any(), any())).thenReturn(httpClientMock)

        when(httpClientMock.post(httpRequest)).thenReturn(responseMock)

        when(responseMock.getStatus()).thenReturn(200)
        when(responseMock.hasEntity()).thenReturn(true)

        when(responseMock.readEntity(String.class)).thenReturn(oofResponse)

        spy.executeTerminateNSSIQuery(mockExecution)

        Mockito.verify(mockExecution,times(1)).setVariable("isTerminateNSSI", terminateResponse)

    }


    @Test
    void testDeleteServiceOrder() {
        def currentNSSI = [:]
        currentNSSI.put("nssiId","5G-999")

        ServiceInstance networkServiceInstance = new ServiceInstance()
        networkServiceInstance.setServiceInstanceId("NS-777")
        networkServiceInstance.setServiceRole("Network Service")

        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        currentNSSI.put("networkServiceInstance", networkServiceInstance)

        String externalAPIURL = "https://nbi.onap:8443/nbi/api/v4/serviceOrder"
        when(mockExecution.getVariable("ExternalAPIURL")).thenReturn(externalAPIURL)

        DoDeallocateCoreNSSI spy = spy(DoDeallocateCoreNSSI.class)
        when(spy.getAAIClient()).thenReturn(client)

        String serviceOrderRequest = ""

        when(mockExecution.getVariable("serviceOrderRequest")).thenReturn(serviceOrderRequest)

        Response responseMock = mock(Response.class)

        ExternalAPIUtil externalAPIUtil = mock(ExternalAPIUtil.class)
        when(spy.getExternalAPIUtil()).thenReturn(externalAPIUtil)
        when(externalAPIUtil.executeExternalAPIPostCall(mockExecution, externalAPIURL, serviceOrderRequest)).thenReturn(responseMock)

        when(responseMock.getStatus()).thenReturn(200)

        String id = UUID.randomUUID().toString()
        String entity = "{\"id\":\"${id}\"}"
        when(responseMock.readEntity(String.class)).thenReturn(entity)

        spy.deleteServiceOrder(mockExecution)

        verify(mockExecution,times(1)).setVariable("ServiceOrderId", id)

    }


    @Test
    void testCalculateSNSSAITerminateNSSI() {
        invokeCalculateSNSSAI("true")
    }

    @Test
    void testCalculateSNSSAINotTerminateNSSI() {
        invokeCalculateSNSSAI("false")
    }

    void invokeCalculateSNSSAI(String isTerminateNSSI) {
        def currentNSSI = [:]
        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        when(mockExecution.getVariable("isTerminateNSSI")).thenReturn(isTerminateNSSI)

        String theSNSSAI = "theS-NSSAI"

        currentNSSI.put("S-NSSAI", theSNSSAI)

        String theSliceProfileId = "the-slice-profile-id"
        currentNSSI['sliceProfileId'] = theSliceProfileId

        List<SliceProfile> associatedProfiles = new ArrayList<>()
        SliceProfile sliceProfile1 = new SliceProfile()
        sliceProfile1.setProfileId(theSliceProfileId)
        sliceProfile1.setSNssai(theSNSSAI)

        SliceProfile sliceProfile2 = new SliceProfile()
        sliceProfile2.setSNssai("snssai2")

        SliceProfile sliceProfile3 = new SliceProfile()
        sliceProfile3.setSNssai("snssai3")

        if(isTerminateNSSI.equals("false")) {
            associatedProfiles.add(sliceProfile1)
            associatedProfiles.add(sliceProfile2)
            associatedProfiles.add(sliceProfile3)
        }

        int sizeBefore = associatedProfiles.size()

        currentNSSI.put("associatedProfiles", associatedProfiles)

        DoDeallocateCoreNSSI obj = new DoDeallocateCoreNSSI()
        obj.calculateSNSSAI(mockExecution)

        List<SliceProfile> snssais = (List<SliceProfile>)currentNSSI.get("S-NSSAIs")
        SliceProfile sliceProfileContainsSNSSAI = (SliceProfile)currentNSSI.get("sliceProfileS-NSSAI")

        if(isTerminateNSSI.equals("false")) {
            assertTrue("Either snssais doesn't exist or size is incorrect", (snssais != null && snssais.size() == (sizeBefore - 1)))
            assertNotNull("Slice Profile which contains given S-NSSAI not found", sliceProfileContainsSNSSAI)
            assertTrue("Wrong Slice Profile", sliceProfileContainsSNSSAI.getSNssai().equals(theSNSSAI))
        }
        else {
            assertTrue("Either snssais doesn't exist or size is incorrect", (snssais != null && snssais.size() == 0))
        }
    }


    @Test
    void testRemoveNSSIAssociationWithNSI() {
        def currentNSSI = [:]

        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        DoDeallocateCoreNSSI spy = spy(DoDeallocateCoreNSSI.class)

        when(spy.getAAIClient()).thenReturn(client)

        String nssiId = "5G-999"
        String nsiId = "5G-99"
        currentNSSI.put("nssiId", nssiId)
        currentNSSI.put("nsiId", nsiId)

        AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(nssiId))

        ServiceInstance nssi = new ServiceInstance()
        nssi.setServiceInstanceId(nssiId)

        ServiceInstance nsi = new ServiceInstance()
        nsi.setServiceInstanceId(nsiId)
        nsi.setServiceRole("nsi")

        AllottedResources allottedResources = new AllottedResources()
        AllottedResource allottedResource = new AllottedResource()
        allottedResource.setId(UUID.randomUUID().toString())
        allottedResources.getAllottedResource().add(allottedResource)
        nssi.setAllottedResources(allottedResources)

        currentNSSI.put("nssi", nssi)

        AAIResourceUri nsiUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(nsiId))

        AAIResultWrapper wrapperMock = mock(AAIResultWrapper.class)
        when(client.get(nssiUri)).thenReturn(wrapperMock)
        Relationships rsMock = mock(Relationships.class)
        Optional<Relationships> orsMock = Optional.of(rsMock)
        when(wrapperMock.getRelationships()).thenReturn(orsMock)

        List<AAIResourceUri> allottedUris = new ArrayList<>()
        AAIResourceUri allottedUri = AAIUriFactory.createResourceUri(Types.ALLOTTED_RESOURCE.getFragment("allotted-id"))
        allottedUris.add(allottedUri)

        when(rsMock.getRelatedUris(Types.ALLOTTED_RESOURCE)).thenReturn(allottedUris)

        List<AAIResourceUri> nsiUris = new ArrayList<>()
        nsiUris.add(nsiUri)

        Optional<ServiceInstance> nsiOpt = Optional.of(nsi)

        when(client.get(allottedUri)).thenReturn(wrapperMock)
        when(rsMock.getRelatedUris(Types.SERVICE_INSTANCE)).thenReturn(nsiUris)
        when(client.get(ServiceInstance.class, nsiUri)).thenReturn(nsiOpt)

        String globalSubscriberId = "globalSubscriberId"
        String subscriptionServiceType = "subscription-service-type"
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn(globalSubscriberId)
        when(mockExecution.getVariable("subscriptionServiceType")).thenReturn(subscriptionServiceType)

        AAIResourceUri allottedResourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(subscriptionServiceType).serviceInstance(nssiId).allottedResource(allottedResource.getId()))

        doNothing().when(client).disconnect(nssiUri, nsiUri)

        spy.removeNSSIAssociationWithNSI(mockExecution)

    }


    @Test
    void testDeleteNSSIServiceInstance() {
        def currentNSSI = [:]

        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        String nssiId = "5G-999"

        currentNSSI.put("nssiId", nssiId)

        AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(nssiId))

        DoDeallocateCoreNSSI spy = spy(DoDeallocateCoreNSSI.class)

        when(spy.getAAIClient()).thenReturn(client)

        doNothing().when(client).delete(nssiUri)

        spy.deleteNSSIServiceInstance(mockExecution)
    }


    @Test
    void testDeleteServiceOrderProgressAcknowledged() {

        executeDeleteServiceOrderProgress("ACKNOWLEDGED")
        Mockito.verify(mockExecution,times(1)).setVariable("deleteStatus", "processing")
    }

    @Test
    void testDeleteServiceOrderProgressInProgress() {

        executeDeleteServiceOrderProgress("IN_PROGRESS")
        Mockito.verify(mockExecution,times(1)).setVariable("deleteStatus", "processing")
    }


    @Test
    void testDeleteServiceOrderProgressCompleted() {

        executeDeleteServiceOrderProgress("COMPLETED")
        Mockito.verify(mockExecution,times(1)).setVariable("deleteStatus", "completed")
    }


    void executeDeleteServiceOrderProgress(String state) {
        def currentNSSI = [:]
        currentNSSI.put("nssiId","5G-999")

        ServiceInstance networkServiceInstance = new ServiceInstance()
        networkServiceInstance.setServiceInstanceId("NS-777")
        networkServiceInstance.setServiceRole("Network Service")

        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        currentNSSI.put("networkServiceInstance", networkServiceInstance)

        String serviceOrder = UUID.randomUUID().toString()

        when(mockExecution.getVariable("ServiceOrderId")).thenReturn(serviceOrder)

        String externalAPIURL = "https://nbi.onap:8443/nbi/api/v4/serviceOrder"
        when(mockExecution.getVariable("ExternalAPIURL")).thenReturn(externalAPIURL)

        DoDeallocateCoreNSSI spy = spy(DoDeallocateCoreNSSI.class)

        Response responseMock = mock(Response.class)

        ExternalAPIUtil externalAPIUtil = mock(ExternalAPIUtil.class)
        when(spy.getExternalAPIUtil()).thenReturn(externalAPIUtil)

        externalAPIURL += "/" + serviceOrder
        when(externalAPIUtil.executeExternalAPIGetCall(mockExecution, externalAPIURL)).thenReturn(responseMock)

        when(responseMock.getStatus()).thenReturn(200)

        String id = UUID.randomUUID().toString()
        String entity = "{\"state\":\"ACCEPTED\",\"orderItem\":[{\"state\":\"${state}\",\"service\":{\"id\":\"${id}\"}}]}"
        when(responseMock.readEntity(String.class)).thenReturn(entity)

        spy.getDeleteServiceOrderProgress(mockExecution)
    }

}
