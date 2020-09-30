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
import org.onap.aai.domain.yang.v19.*
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.bpmn.common.scripts.ExternalAPIUtil
import org.onap.so.bpmn.common.scripts.ExternalAPIUtilFactory
import org.onap.so.bpmn.common.scripts.MsoGroovyTest

import javax.ws.rs.core.Response

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue
import static org.mockito.Mockito.*

class DoDeallocateCoreNSSITest extends MsoGroovyTest {

    @Before
    void init() throws IOException {
        super.init("DoDeallocateNSSITest")
    }


    @Test
    void testExecuteTerminateNSSIQuery() {

        def currentNSSI = [:]
        currentNSSI.put("nssiId","5G-999")

        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        when(mockExecution.getVariable("mso.oof.endpoint")).thenReturn("http://oof.onap:8088")
        when(mockExecution.getVariable("mso.oof.auth")).thenReturn("mso.oof.auth")
        when(mockExecution.getVariable("mso.msoKey")).thenReturn("mso.msoKey")
        when(mockExecution.getVariable("mso-request-id")).thenReturn("mso-request-id")

        DoDeallocateCoreNSSI spy = spy(DoDeallocateCoreNSSI.class)
        when(spy.getAAIClient()).thenReturn(client)

        when(spy.encryptBasicAuth("mso.oof.auth", "mso.msoKey")).thenReturn("auth-value")

        String authHeaderResponse =  "auth-header"

      /*  String authHeaderResponse =  "{\n" +
                " \"errorCode\": \"401\",\n" +
                " \"errorMessage\": \"Bad request\"\n" +
                "}" */

        when(spy.getAuthHeader(mockExecution, "auth-value", "mso.msoKey")).thenReturn(authHeaderResponse)

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

        boolean terminateResponse = true

        String oofResponse =   "{\n" +
                " \"requestId\": \"mso-request-id\",\n" +
                " \"transactionId\": \"mso-request-id\",\n" +
                " \"statusMessage\": \"\",\n" +
                " \"requestStatus\": \"accepted\",\n" +
                " \"terminateResponse\": \"${terminateResponse}\",\n" +
                " \"reason\": \"\"\n" +
                " }\n"

        String oofCallResponse = oofResponse

      /*  String oofCallResponse =  "{\n" +
                " \"errorCode\": \"401\",\n" +
                " \"errorMessage\": \"Exception during the call\"\n" +
                "}" */

        when(spy.callOOF(urlString, "auth-header", httpRequest)).thenReturn(oofCallResponse)

        spy.executeTerminateNSSIQuery(mockExecution)

        verify(mockExecution).setVariable("isTerminateNSSI", terminateResponse)

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

        when(mockExecution.getVariable("nbi.endpoint.url")).thenReturn("http://nbi.onap:8088")
        when(mockExecution.getVariable("mso.msoKey")).thenReturn("mso.msoKey")
        when(mockExecution.getVariable("mso.infra.endpoint.auth")).thenReturn("mso.infra.endpoint.auth")

        DoDeallocateCoreNSSI spy = spy(DoDeallocateCoreNSSI.class)
        when(spy.getAAIClient()).thenReturn(client)

        when(spy.encryptBasicAuth("mso.infra.endpoint.auth", "mso.msoKey")).thenReturn("auth-value")

        String authHeaderResponse =  "auth-header"

        /*  String authHeaderResponse =  "{\n" +
                  " \"errorCode\": \"401\",\n" +
                  " \"errorMessage\": \"Bad request\"\n" +
                  "}" */

        when(spy.getAuthHeader(mockExecution, "auth-value", "mso.msoKey")).thenReturn(authHeaderResponse)

        String urlString = String.format("http://nbi.onap:8088/api/v4/serviceOrder/%s", networkServiceInstance.getServiceInstanceId())

        String callDeleteServiceOrderResponse = "deleted"

        when(spy.callDeleteServiceOrder(mockExecution, urlString, "auth-header")).thenReturn(callDeleteServiceOrderResponse)

        spy.deleteServiceOrder(mockExecution)
    }


    @Test
    void testCalculateSNSSAI() {
        def currentNSSI = [:]
        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        String theSNSSAI = "theS-NSSAI"

        currentNSSI.put("S-NSSAI", theSNSSAI)

        List<SliceProfile> associatedProfiles = new ArrayList<>()
        SliceProfile sliceProfile1 = new SliceProfile()
        sliceProfile1.setSNssai("snssai1")

        SliceProfile sliceProfile2 = new SliceProfile()
        sliceProfile2.setSNssai(theSNSSAI)

        SliceProfile sliceProfile3 = new SliceProfile()
        sliceProfile3.setSNssai("snssai3")

        associatedProfiles.add(sliceProfile1)
        associatedProfiles.add(sliceProfile2)
        associatedProfiles.add(sliceProfile3)

        int sizeBefore = associatedProfiles.size()

        currentNSSI.put("associatedProfiles", associatedProfiles)

        DoDeallocateCoreNSSI obj = new DoDeallocateCoreNSSI()
        obj.calculateSNSSAI(mockExecution)

        List<SliceProfile> snssais = (List<SliceProfile>)currentNSSI.get("S-NSSAIs")
        SliceProfile sliceProfileContainsSNSSAI = (SliceProfile)currentNSSI.get("sliceProfileS-NSSAI")

        assertTrue("Either snssais doesn't exist or size is incorrect", (snssais != null && snssais.size() == (sizeBefore - 1)))
        assertNotNull("Slice Profile which contains given S-NSSAI not found", sliceProfileContainsSNSSAI)
        assertTrue("Wrong Slice Profile", sliceProfileContainsSNSSAI.getSNssai().equals(theSNSSAI))
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

        AllottedResources allottedResources = new AllottedResources()
        AllottedResource allottedResource = new AllottedResource()
        allottedResource.setId(UUID.randomUUID().toString())
        allottedResources.getAllottedResource().add(allottedResource)
        nssi.setAllottedResources(allottedResources)

        currentNSSI.put("nssi", nssi)

        AAIResourceUri nsiUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(nsiId))

        doNothing().when(client).update(nssiUri, nssi)

        String globalSubscriberId = "globalSubscriberId"
        String subscriptionServiceType = "subscription-service-type"
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn(globalSubscriberId)
        when(mockExecution.getVariable("subscriptionServiceType")).thenReturn(subscriptionServiceType)

        AAIResourceUri allottedResourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(subscriptionServiceType).serviceInstance(nssiId).allottedResource(allottedResource.getId()))

        doNothing().when(client).disconnect(nsiUri, allottedResourceUri)

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

        executeDeleteServiceOrderProgress("INPROGRESS")
        Mockito.verify(mockExecution,times(1)).setVariable("deleteStatus", "processing")
    }


    @Test
    void testDeleteServiceOrderProgressCompleted() {

        executeDeleteServiceOrderProgress("COMPLETED")
        Mockito.verify(mockExecution,times(1)).setVariable("deleteStatus", "completed")
    }


    void executeDeleteServiceOrderProgress(String state) {
        def currentNSSI = [:]

        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        String url = "http://nbi.onap:8088/api/v4/serviceOrder/NS-777"

        currentNSSI.put("deleteServiceOrderURL", url)

        DoDeallocateCoreNSSI spy = spy(DoDeallocateCoreNSSI.class)

        ExternalAPIUtilFactory externalAPIUtilFactoryMock = mock(ExternalAPIUtilFactory.class)
        when(spy.getExternalAPIUtilFactory()).thenReturn(externalAPIUtilFactoryMock)

        ExternalAPIUtil externalAPIUtilMock = mock(ExternalAPIUtil.class)

        when(externalAPIUtilFactoryMock.create()).thenReturn(externalAPIUtilMock)

        Response responseMock = mock(Response.class)
        when(externalAPIUtilMock.executeExternalAPIGetCall(mockExecution, url)).thenReturn(responseMock)

        when(responseMock.getStatus()).thenReturn(200)

        String entity = "{\"state\":\"ACCEPTED\",\"orderItem\":[{\"service\":{\"id\":\"5G-999\"},\"state\":\"${state}\"}]}"
        when(responseMock.readEntity(String.class)).thenReturn(entity)

        spy.getDeleteServiceOrderProgress(mockExecution)
    }

}
