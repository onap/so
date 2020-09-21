/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020  TIM
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

import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionImpl
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.junit.Rule
import org.junit.rules.ExpectedException
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.onap.aai.domain.yang.CloudRegion
import org.onap.aai.domain.yang.Customer
import org.onap.aai.domain.yang.GenericVnf
import org.onap.aai.domain.yang.ModelVer
import org.onap.aai.domain.yang.OwningEntity
import org.onap.aai.domain.yang.Project
import org.onap.aai.domain.yang.Relationship
import org.onap.aai.domain.yang.RelationshipList
import org.onap.aai.domain.yang.ServiceSubscription
import org.onap.aai.domain.yang.SliceProfile
import org.onap.aai.domain.yang.SliceProfiles
import org.onap.aai.domain.yang.Tenant
import org.onap.aai.domain.yang.Tenants
import org.onap.aai.domain.yang.VfModule
import org.onap.aai.domain.yang.VfModules
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.Relationships
import org.onap.aaiclient.client.aai.entities.uri.AAISimpleUri
import org.onap.aaiclient.client.aai.entities.uri.ServiceInstanceUri
import org.onap.logging.filter.base.ONAPComponents
import org.onap.so.beans.nsmf.NssiResponse
import org.onap.so.bpmn.common.scripts.ExceptionUtil
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.mock.FileUtil
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory
import org.onap.so.serviceinstancebeans.RequestDetails
import org.onap.so.utils.CryptoUtils

import javax.ws.rs.core.Response

import static org.junit.Assert.*;
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.onap.aai.domain.yang.ServiceInstance
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.bpmn.common.scripts.MsoGroovyTest

import static org.mockito.ArgumentMatchers.anyDouble
import static org.mockito.ArgumentMatchers.anyInt
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.doNothing
import static org.mockito.Mockito.doReturn
import static org.mockito.Mockito.doThrow
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

class DoDeallocateCoreNSSITest extends MsoGroovyTest {

    @Before
    void init() throws IOException {
        super.init("DoDeallocateNSSITest")
    }


    @Test
    void testPreProcessRequest() {
        def currentNSSI = [:]
        currentNSSI.put("nssiId","5G-999")
        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        DoDeallocateCoreNSSI dcnssi = new DoDeallocateCoreNSSI()
        dcnssi.preProcessRequest(mockExecution)
        Mockito.verify(mockExecution,times(1)).getVariable("currentNSSI")
    }


    @Test
    void testExecuteTerminateNSSIQuery() {
        HttpClientFactory httpClientFactoryMock
        HttpClient httpClientMock

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
    void testGetNetworkServiceInstance() {
        def currentNSSI = [:]
        currentNSSI.put("nssiId","5G-999")

        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment("5G-999"))
        AAIResourceUri networkServiceInstanceUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment("NS-777"))

        ServiceInstance nssi = new ServiceInstance()
        nssi.setServiceInstanceId("5G-999")

        ServiceInstance networkServiceInstance = new ServiceInstance()
        networkServiceInstance.setServiceInstanceId("NS-777")
        networkServiceInstance.setServiceRole("Network Service")

        Optional<ServiceInstance> nssiOpt = Optional.of(nssi)
        Optional<ServiceInstance> networkServiceInstaneOpt = Optional.of(networkServiceInstance)

        DoDeallocateCoreNSSI spy = spy(DoDeallocateCoreNSSI.class)
        when(spy.getAAIClient()).thenReturn(client)

        when(client.get(ServiceInstance.class, nssiUri)).thenReturn(nssiOpt)

        //String json = FileUtil.readResourceFile("__files/AAI/ServiceInstanceWithAR.json")
        AAIResultWrapper wrapperMock = mock(AAIResultWrapper.class) //new AAIResultWrapper(json)
        Relationships rsMock = mock(Relationships.class)
        Optional<Relationships> orsMock = Optional.of(rsMock)
        List<AAIResourceUri> arus = new ArrayList<>()
        AAIResourceUri aru = new ServiceInstanceUri(networkServiceInstanceUri)
        arus.add(aru)

        when(client.get(nssiUri)).thenReturn(wrapperMock)
        when(wrapperMock.getRelationships()).thenReturn(orsMock)

        when(rsMock.getRelatedUris(Types.SERVICE_INSTANCE)).thenReturn(arus)
        when(client.get(ServiceInstance.class, aru)).thenReturn(networkServiceInstaneOpt)

        spy.getNetworkServiceInstance(mockExecution)

        assertTrue("Either NSSI doesn't exist or unexpected NSSI Service Instance ID",
                    currentNSSI.get("nssi") != null && ((ServiceInstance)currentNSSI.get("nssi")).getServiceInstanceId().equals(nssi.getServiceInstanceId()))

        assertTrue("Either Network Service Instance doesn't exist or unexpected Network Service Instance ID",
                currentNSSI.get("networkServiceInstance") != null && ((ServiceInstance)currentNSSI.get("networkServiceInstance")).getServiceInstanceId().equals(networkServiceInstance.getServiceInstanceId()))

        assertNotNull("networkServiceInstanceUri doesn't exist", currentNSSI.get("networkServiceInstanceUri"))
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
    void getConstituteVNFFromNetworkServiceInst() {
        def currentNSSI = [:]

        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        DoDeallocateCoreNSSI spy = spy(DoDeallocateCoreNSSI.class)
        when(spy.getAAIClient()).thenReturn(client)

        ServiceInstance networkServiceInstance = new ServiceInstance()
        networkServiceInstance.setServiceInstanceId("NS-777")
        networkServiceInstance.setServiceRole("Network Service")

        GenericVnf genericVNF = new GenericVnf()
        genericVNF.setVnfId("VNF-1")

        AAIResourceUri networkServiceInstanceUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(networkServiceInstance.getServiceInstanceId()))

        Optional<GenericVnf> genericVnfOpt = Optional.of(genericVNF)
        AAIResourceUri genericVNFUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(genericVNF.getVnfId()))

        currentNSSI.put("networkServiceInstanceUri", networkServiceInstanceUri)
        currentNSSI.put("networkServiceInstance", networkServiceInstance)

        AAIResultWrapper wrapperMock = mock(AAIResultWrapper.class) //new AAIResultWrapper(json)
        Relationships rsMock = mock(Relationships.class)
        Optional<Relationships> orsMock = Optional.of(rsMock)
        List<AAIResourceUri> arus = new ArrayList<>()
        AAIResourceUri aru = new AAISimpleUri(genericVNFUri)
        arus.add(aru)

        when(client.get(networkServiceInstanceUri)).thenReturn(wrapperMock)
        when(wrapperMock.getRelationships()).thenReturn(orsMock)

        when(rsMock.getRelatedUris(Types.GENERIC_VNF)).thenReturn(arus)
        when(client.get(GenericVnf.class, genericVNFUri)).thenReturn(genericVnfOpt)

        spy.getConstituteVNFFromNetworkServiceInst(mockExecution)

        assertNotNull("constituteVnfUri doesn't exist", currentNSSI.get("constituteVnfUri"))

        assertTrue("Either Constitute VNF doesn't exist or unexpected VNF ID",
                currentNSSI.get("constituteVnf") != null && ((GenericVnf)currentNSSI.get("constituteVnf")).getVnfId().equals(genericVNF.getVnfId()))
    }


    @Test
    void testGetNSSIAssociatedProfiles() {
        def currentNSSI = [:]
        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        ServiceInstance nssi = new ServiceInstance()
        nssi.setServiceInstanceId("5G-999")

        SliceProfiles sliceProfiles = new SliceProfiles()

        List<SliceProfile> slProfiles = sliceProfiles.getSliceProfile()
        slProfiles.add(new SliceProfile())
        slProfiles.add(new SliceProfile())

        nssi.setSliceProfiles(sliceProfiles)
        currentNSSI.put("nssi", nssi)

        DoDeallocateCoreNSSI obj = new DoDeallocateCoreNSSI()
        obj.getNSSIAssociatedProfiles(mockExecution)

        List<SliceProfile> associatedProfiles = (List<SliceProfile>)currentNSSI.get("associatedProfiles")
        assertTrue("Either associatedProfiles doesn't exist or size is incorrect", (associatedProfiles != null && associatedProfiles.size() == 2))
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
    void testInvokePUTServiceInstance() {
        def currentNSSI = [:]

        ServiceInstance networkServiceInstance = new ServiceInstance()
        networkServiceInstance.setServiceInstanceId("NS-777")
        networkServiceInstance.setServiceRole("Network Service")

        GenericVnf constituteVnf = new GenericVnf()
        constituteVnf.setVnfId("VNF-1")

        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        currentNSSI.put("networkServiceInstance", networkServiceInstance)
        currentNSSI.put("constituteVnf", constituteVnf)

        when(mockExecution.getVariable("mso.infra.endpoint.url")).thenReturn("http://mso.onap:8088")
        when(mockExecution.getVariable("mso.msoKey")).thenReturn("mso.msoKey")
        when(mockExecution.getVariable("mso.infra.endpoint.auth")).thenReturn("mso.infra.endpoint.auth")

        DoDeallocateCoreNSSI spy = spy(DoDeallocateCoreNSSI.class)
        when(spy.getAAIClient()).thenReturn(client)

        when(spy.encryptBasicAuth("mso.infra.endpoint.auth", "mso.msoKey")).thenReturn("auth-value")

        String authHeaderResponse =  "auth-header"

        when(spy.getAuthHeader(mockExecution, "auth-value", "mso.msoKey")).thenReturn(authHeaderResponse)

        String urlString = String.format("http://mso.onap:8088/serviceInstantiation/v7/serviceInstances/%s/vnfs/%s", networkServiceInstance.getServiceInstanceId(), constituteVnf.getVnfId())

        String callPUTServiceInstanceResponse = "put"

        RequestDetails requestDetails = new RequestDetails()
        ObjectMapper mapper = new ObjectMapper()
        String requestDetailsStr = mapper.writeValueAsString(requestDetails)

        when(spy.prepareRequestDetails(mockExecution)).thenReturn(requestDetailsStr)

        when(spy.callPUTServiceInstance(urlString, "auth-header", requestDetailsStr)).thenReturn(callPUTServiceInstanceResponse)

        spy.invokePUTServiceInstance(mockExecution)
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
        AAIResourceUri nsiUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(nsiId))

        doNothing().when(client).disconnect(nssiUri, nsiUri)

        spy.removeNSSIAssociationWithNSI(mockExecution)

    }


    @Test
    void testRemoveSPAssociationWithNSSI() {
        def currentNSSI = [:]

        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        String nssiId = "5G-999"
        currentNSSI.put("nssiId", nssiId)
        ServiceInstance nssi = new ServiceInstance()
        nssi.setServiceInstanceId(nssiId)
        nssi.setSliceProfiles(new SliceProfiles())

        currentNSSI.put("nssi", nssi)

        AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(nssiId))

        DoDeallocateCoreNSSI spy = spy(DoDeallocateCoreNSSI.class)

        when(spy.getAAIClient()).thenReturn(client)

        String theSNSSAI = "theS-NSSAI"
        currentNSSI.put("S-NSSAI", theSNSSAI)

        List<SliceProfile> associatedProfiles = nssi.getSliceProfiles().getSliceProfile()

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

        doNothing().when(client).update(nssiUri, nssi)

        spy.removeSPAssociationWithNSSI(mockExecution)

        assertTrue("Association between slice profile and NSSI wasn't removed", ((ServiceInstance)currentNSSI.get("nssi")).getSliceProfiles().getSliceProfile().size() == (sizeBefore - 1))
    }


    @Test
    void testDeleteSliceProfileInstance() {
        def currentNSSI = [:]

        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        String globalSubscriberId = "global-id"
        String serviceType = "service"
        String nssiId = "5G-999"

        currentNSSI.put("globalSubscriberId", nssiId)
        currentNSSI.put("serviceType", nssiId)
        currentNSSI.put("nssiId", nssiId)

        String theSNSSAI = "theS-NSSAI"

        SliceProfile sliceProfile = new SliceProfile()
        sliceProfile.setSNssai(theSNSSAI)
        sliceProfile.setProfileId("prof-id")

        AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(nssiId))

        currentNSSI.put("sliceProfileS-NSSAI", sliceProfile)

        DoDeallocateCoreNSSI spy = spy(DoDeallocateCoreNSSI.class)

        when(spy.getAAIClient()).thenReturn(client)

        doNothing().when(client).delete(nssiUri)

        spy.deleteSliceProfileInstance(mockExecution)

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
    void testUpdateServiceOperationStatus() {
        def currentNSSI = [:]

        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        String nssiId = "5G-999"

        currentNSSI.put("nssiId", nssiId)
        currentNSSI.put("e2eServiceInstanceId", "e2eServiceInstanceId")
        currentNSSI.put("operationId", "operationId")
        currentNSSI.put("operationType", "operationType")

        DoDeallocateCoreNSSI spy = spy(DoDeallocateCoreNSSI.class)

        spy.updateServiceOperationStatus(mockExecution)

    }


    @Test
    void testPrepareRequestDetails() {
        def currentNSSI = [:]

        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        ServiceInstance networkServiceInstance = new ServiceInstance()
        networkServiceInstance.setServiceInstanceId("NS-777")
        networkServiceInstance.setServiceRole("Network Service")
        networkServiceInstance.setModelInvariantId("model-invariant-id")
        networkServiceInstance.setServiceInstanceName("service-instance-name")

        ServiceInstance nssi = new ServiceInstance()
        nssi.setServiceInstanceId("5G-999")
        nssi.setOrchestrationStatus("orchestration-status")

        AAIResourceUri networkServiceInstanceUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment("networkServiceInstance.getServiceInstanceId()"))

        AAIResourceUri cloudRegionAAIUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion("cloud-owner", "cloud-region-id"))

        currentNSSI.put("networkServiceInstanceUri", networkServiceInstanceUri)

        currentNSSI.put("networkServiceInstance", networkServiceInstance)

        currentNSSI.put("globalSubscriberId", "globalSubscriberId")

        currentNSSI.put("subscriberName", "subscriber-name")

        currentNSSI.put("serviceId", "service-id")

        currentNSSI.put("nssi", nssi)

        List<SliceProfile> associatedProfiles = new ArrayList<>()
        SliceProfile sliceProfile1 = new SliceProfile()
        sliceProfile1.setSNssai("snssai1")

        SliceProfile sliceProfile2 = new SliceProfile()
        sliceProfile2.setSNssai("snssai2")

        associatedProfiles.add(sliceProfile1)
        associatedProfiles.add(sliceProfile2)

        List<String> snssais = new ArrayList<>()
        snssais.add(sliceProfile1.getSNssai())
        snssais.add(sliceProfile2.getSNssai())

        currentNSSI.put("S-NSSAIs", snssais)


        ServiceSubscription serviceSubscription = new ServiceSubscription()
        serviceSubscription.setServiceType("service-type")

        currentNSSI.put("serviceSubscription", serviceSubscription)

        GenericVnf genericVnf = new GenericVnf()
        genericVnf.setServiceId("service-id")
        genericVnf.setVnfName("vnf-name")
        genericVnf.setModelInvariantId("model-invariant-id")
        genericVnf.setModelCustomizationId("model-customization-id")
        genericVnf.setVnfName("vnf-name")
        genericVnf.setVnfId("vnf-id")

        VfModule vfModule = new VfModule()
        vfModule.setModelInvariantId("model-invariant-id")
        vfModule.setModelCustomizationId("model-customization-id")
        vfModule.setModelVersionId("model-version-id")
        vfModule.setVfModuleName("vf-module-name")

        VfModules vfModules = new VfModules()
        vfModules.getVfModule().add(vfModule)
        genericVnf.setVfModules(vfModules)

        currentNSSI.put("constituteVnf", genericVnf)

        AAIResourceUri constituteVNFURI = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(genericVnf.getVnfId()))

        currentNSSI.put("constituteVnfUri", constituteVNFURI)

        DoDeallocateCoreNSSI spy = spy(DoDeallocateCoreNSSI.class)

        when(spy.getAAIClient()).thenReturn(client)

        prepareModelVer(networkServiceInstance)

        //prepareSubscriberInfo(networkServiceInstanceUri)

        prepareCloudConfiguration(constituteVNFURI, cloudRegionAAIUri)

        prepareModelVer(genericVnf)

        prepareModelVer(vfModule)

        prepareOwningEntity(networkServiceInstanceUri)

        prepareProject(cloudRegionAAIUri)

        String requestDetails = spy.prepareRequestDetails(mockExecution)

    }


    void prepareProject(AAIResourceUri cloudRegionAAIUri) {
        Project project = new Project()
        project.setProjectName("project-name")

        AAIResultWrapper wrapperMock = mock(AAIResultWrapper.class)
        Relationships rsMock = mock(Relationships.class)
        Optional<Relationships> orsMock = Optional.of(rsMock)

        when(client.get(cloudRegionAAIUri)).thenReturn(wrapperMock)
        when(wrapperMock.getRelationships()).thenReturn(orsMock)

        List<AAIResourceUri> arus = new ArrayList<>()
        AAIResourceUri aru = new AAISimpleUri(cloudRegionAAIUri)
        arus.add(aru)

        when(rsMock.getRelatedUris(Types.PROJECT)).thenReturn(arus)

        Optional<Project> projectOpt = Optional.of(project)

        when(client.get(Project.class, aru)).thenReturn(projectOpt)
    }


    void prepareOwningEntity(AAIResourceUri networkServiceInstanceUri) {
        OwningEntity owningEntity = new OwningEntity()

        owningEntity.setOwningEntityId("owning-entity-id")
        owningEntity.setOwningEntityName("owning-entity-name")

        AAIResultWrapper wrapperMock = mock(AAIResultWrapper.class)

        Relationships rsMock = mock(Relationships.class)
        Optional<Relationships> orsMock = Optional.of(rsMock)

        when(client.get(networkServiceInstanceUri)).thenReturn(wrapperMock)
        when(wrapperMock.getRelationships()).thenReturn(orsMock)

        List<AAIResourceUri> arus = new ArrayList<>()
        AAIResourceUri aru = new AAISimpleUri(networkServiceInstanceUri)
        arus.add(aru)

        when(rsMock.getRelatedUris(Types.OWNING_ENTITY)).thenReturn(arus)

        Optional<OwningEntity> owningEntityOpt = Optional.of(owningEntity)

        when(client.get(OwningEntity.class, aru)).thenReturn(owningEntityOpt)
    }



    void prepareCloudConfiguration(AAIResourceUri constituteVNFURI, cloudRegionAAIUri) {
        AAIResultWrapper wrapperMock = mock(AAIResultWrapper.class)

        Relationships rsMock = mock(Relationships.class)
        Optional<Relationships> orsMock = Optional.of(rsMock)

        when(client.get(constituteVNFURI)).thenReturn(wrapperMock)
        when(wrapperMock.getRelationships()).thenReturn(orsMock)

        List<AAIResourceUri> arus = new ArrayList<>()
        AAIResourceUri aru = new AAISimpleUri(cloudRegionAAIUri)
        arus.add(aru)

        when(rsMock.getRelatedUris(Types.CLOUD_REGION)).thenReturn(arus)

        CloudRegion cloudRegion = new CloudRegion()
        cloudRegion.setCloudRegionId("cloud-region-id")
        cloudRegion.setCloudOwner("cloud-owner")
        Tenant tenant = new Tenant()
        tenant.setTenantId("tenant-id")

        Tenants tenants = new Tenants()
        tenants.getTenant().add(tenant)
        cloudRegion.setTenants(tenants)
        Optional<CloudRegion> cloudRegionOpt = Optional.of(cloudRegion)

        when(client.get(CloudRegion.class, aru)).thenReturn(cloudRegionOpt)
    }


    void prepareSubscriberInfo( AAIResourceUri networkServiceInstanceUri) {
        AAIResultWrapper wrapperMock = mock(AAIResultWrapper.class)

        Relationships rsMock = mock(Relationships.class)
        Optional<Relationships> orsMock = Optional.of(rsMock)

        when(client.get(networkServiceInstanceUri)).thenReturn(wrapperMock)
        when(wrapperMock.getRelationships()).thenReturn(orsMock)

        AAIResourceUri serviceSubscriptionUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer("global-customer-id").serviceSubscription("service-type"))

        AAIResourceUri customerUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer("global-customer-id"))
        List<AAIResourceUri> arus = new ArrayList<>()

        arus.add(serviceSubscriptionUri)

        when(rsMock.getRelatedUris(Types.SERVICE_SUBSCRIPTION)).thenReturn(arus)

        ServiceSubscription serviceSubscription = new ServiceSubscription()
        serviceSubscription.setServiceType("service-type")
        Optional<ServiceSubscription> serviceSubscriptionOpt = Optional.of(serviceSubscription)

        when(client.get(ServiceSubscription.class, serviceSubscriptionUri)).thenReturn(serviceSubscriptionOpt)

        when(client.get(networkServiceInstanceUri)).thenReturn(wrapperMock)

        when(rsMock.getRelatedUris(Types.CUSTOMER)).thenReturn(arus)

        Customer customer = new Customer()
        customer.setSubscriberName("subscriber-name")
        Optional<Customer> customerOpt = Optional.of(customer)

        when(client.get(Customer.class, customerUri)).thenReturn(customerOpt)
    }


    void prepareModelVer(ServiceInstance networkServiceInstance) {
        ModelVer modelVer = new ModelVer()
        modelVer.setModelVersionId("model-version-id")
        modelVer.setModelName("model-name")
        modelVer.setModelVersion("model-version")

        Optional<ModelVer> modelVerOpt = Optional.of(modelVer)

        AAIResourceUri modelVerUrl = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.serviceDesignAndCreation().model(networkServiceInstance.getModelInvariantId()).modelVer(networkServiceInstance.getModelVersionId()))
        when(client.get(ModelVer.class, modelVerUrl)).thenReturn(modelVerOpt)
    }

    void prepareModelVer(GenericVnf genericVnf) {
        ModelVer modelVer = new ModelVer()
        modelVer.setModelVersionId("model-version-id")
        modelVer.setModelName("model-name")
        modelVer.setModelVersion("model-version")

        Optional<ModelVer> modelVerOpt = Optional.of(modelVer)

        AAIResourceUri modelVerUrl = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.serviceDesignAndCreation().model(genericVnf.getModelInvariantId()).modelVer(genericVnf.getModelVersionId()))
        when(client.get(ModelVer.class, modelVerUrl)).thenReturn(modelVerOpt)
    }

    void prepareModelVer(VfModule vfModule) {
        ModelVer modelVer = new ModelVer()
        modelVer.setModelVersionId("model-version-id")
        modelVer.setModelName("model-name")
        modelVer.setModelVersion("model-version")

        Optional<ModelVer> modelVerOpt = Optional.of(modelVer)

        AAIResourceUri modelVerUrl = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.serviceDesignAndCreation().model(vfModule.getModelInvariantId()).modelVer(vfModule.getModelVersionId()))
        when(client.get(ModelVer.class, modelVerUrl)).thenReturn(modelVerOpt)
    }

}
