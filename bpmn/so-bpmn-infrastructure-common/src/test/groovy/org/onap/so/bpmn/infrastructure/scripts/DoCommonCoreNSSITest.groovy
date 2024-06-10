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

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.onap.aai.domain.yang.v19.*
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.Relationships
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAISimpleUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.aai.entities.uri.ServiceInstanceUri
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.logging.filter.base.ONAPComponents
import org.onap.so.bpmn.common.scripts.CatalogDbUtils
import org.onap.so.bpmn.common.scripts.CatalogDbUtilsFactory
import org.onap.so.bpmn.common.scripts.ExternalAPIUtil
import org.onap.so.bpmn.common.scripts.ExternalAPIUtilFactory
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.so.bpmn.common.scripts.MsoUtils
import org.onap.so.bpmn.core.json.JsonUtils
import org.onap.so.client.HttpClient
import org.onap.so.client.HttpClientFactory
import org.onap.so.serviceinstancebeans.RequestDetails

import jakarta.ws.rs.core.Response
import java.time.Instant

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertTrue
import static org.mockito.Mockito.*

class DoCommonCoreNSSITest extends MsoGroovyTest {
    @Before
    void init() throws IOException {
        super.init("DoCommonCoreNSSITest")
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

        DoCommonCoreNSSI spy = spy(DoCommonCoreNSSI.class)
        when(spy.getAAIClient()).thenReturn(client)

        when(client.get(ServiceInstance.class, nssiUri)).thenReturn(nssiOpt)

        //String json = FileUtil.readResourceFile("__files/AAI/ServiceInstanceWithAR.json")
        AAIResultWrapper wrapperMock = mock(AAIResultWrapper.class) //new AAIResultWrapper(json)
        Relationships rsMock = mock(Relationships.class)
        Optional<Relationships> orsMock = Optional.of(rsMock)
        List<AAIResourceUri> arus = new ArrayList<>()
        AAIResourceUri aru = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(networkServiceInstance.getServiceInstanceId()))
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
    void getConstituteVNFFromNetworkServiceInst() {
        def currentNSSI = [:]

        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        DoCommonCoreNSSI spy = spy(DoCommonCoreNSSI.class)
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
    void testInvokePUTServiceInstance() {
        def currentNSSI = [:]

        ServiceInstance networkServiceInstance = new ServiceInstance()
        networkServiceInstance.setServiceInstanceId("NS-777")
        networkServiceInstance.setServiceRole("Network Service")

        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        currentNSSI.put("networkServiceInstance", networkServiceInstance)

        when(mockExecution.getVariable("mso.infra.endpoint.url")).thenReturn("http://mso.onap:8088")
        when(mockExecution.getVariable("mso.msoKey")).thenReturn("mso.msoKey")
        when(mockExecution.getVariable("mso.infra.endpoint.auth")).thenReturn("mso.infra.endpoint.auth")

        DoCommonCoreNSSI spy = spy(DoCommonCoreNSSI.class)
        when(spy.getAAIClient()).thenReturn(client)

        GenericVnf genericVnf = new GenericVnf()
        genericVnf.setServiceId("service-id")
        genericVnf.setVnfName("vnf-name")
        genericVnf.setModelInvariantId("model-invariant-id")
        genericVnf.setModelCustomizationId("model-customization-id")
        genericVnf.setVnfName("vnf-name")
        genericVnf.setVnfId("vnf-id")

        currentNSSI.put("constituteVnf", genericVnf)


        String urlString = String.format("http://mso.onap:8088/serviceInstantiation/v7/serviceInstances/%s/vnfs/%s", networkServiceInstance.getServiceInstanceId(), genericVnf.getVnfId())

        String callPUTServiceInstanceResponse = "put"

        RequestDetails requestDetails = new RequestDetails()
        ObjectMapper mapper = new ObjectMapper()
        String requestDetailsStr = mapper.writeValueAsString(requestDetails)

        when(spy.prepareRequestDetails(mockExecution)).thenReturn(requestDetailsStr)

        MsoUtils msoUtilsMock = mock(MsoUtils.class)
        String basicAuth = "basicAuth"
        when(msoUtilsMock.getBasicAuth(anyString(), anyString())).thenReturn(basicAuth)

        HttpClientFactory httpClientFactoryMock = mock(HttpClientFactory.class)
        when(spy.getHttpClientFactory()).thenReturn(httpClientFactoryMock)
        Response responseMock = mock(Response.class)

        HttpClient httpClientMock = mock(HttpClient.class)

        when(httpClientFactoryMock.newJsonClient(any(), any())).thenReturn(httpClientMock)

        when(httpClientMock.put(requestDetailsStr)).thenReturn(responseMock)

        when(responseMock.getStatus()).thenReturn(200)
        when(responseMock.hasEntity()).thenReturn(true)

        String macroOperationId = "request-id"
        String requestSelfLink = "request-self-link"
        String entity = "{\"requestReferences\":{\"requestId\": \"${macroOperationId}\",\"requestSelfLink\":\"${requestSelfLink}\"}}"
        when(responseMock.readEntity(String.class)).thenReturn(entity)

        spy.invokePUTServiceInstance(mockExecution)

        Mockito.verify(mockExecution,times(1)).setVariable("macroOperationId", macroOperationId)
        Mockito.verify(mockExecution,times(1)).setVariable("requestSelfLink", requestSelfLink)
    }


    @Test(expected = Test.None.class)
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

        AAIResourceUri sliceProfileInstanceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer("global-subscriber-id").serviceSubscription("subscription-service-type").
                serviceInstance("slice-profile-instance-id"))

        String sliceProfileInstanceId = "slice-profile-instance-id"
        ServiceInstance sliceProfileInstance = new ServiceInstance()
        sliceProfileInstance.setServiceInstanceId(sliceProfileInstanceId)
        sliceProfileInstance.setServiceRole("slice-profile-instance")

        Optional<ServiceInstance> sliceProfileInstanceOpt = Optional.of(sliceProfileInstance)

        when(client.get(ServiceInstance.class, sliceProfileInstanceUri)).thenReturn(sliceProfileInstanceOpt)

        currentNSSI.put("sliceProfileInstanceUri", sliceProfileInstanceUri)

        DoCommonCoreNSSI spy = spy(DoCommonCoreNSSI.class)

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

        SliceProfiles sps = new SliceProfiles()
        sps.getSliceProfile().addAll(associatedProfiles)
        sliceProfileInstance.setSliceProfiles(sps)

        int sizeBefore = associatedProfiles.size()

        doNothing().when(client).update(sliceProfileInstanceUri, sliceProfileInstance)

        doNothing().when(client). disconnect(nssiUri, sliceProfileInstanceUri)

        spy.removeSPAssociationWithNSSI(mockExecution)

     //   assertTrue("Association between slice profile and NSSI wasn't removed", ((ServiceInstance)currentNSSI.get("sliceProfileInstance")).getSliceProfiles().getSliceProfile().size() == (sizeBefore - 1))
    }


    @Test
    void testDeleteSliceProfileInstance() {
        def currentNSSI = [:]

        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer("global-subscriber-id").serviceSubscription("subscription-service-type").
                serviceInstance("slice-profile-instance-id"))

        currentNSSI.put("sliceProfileInstanceUri", uri)

        DoCommonCoreNSSI spy = spy(DoCommonCoreNSSI.class)

        when(spy.getAAIClient()).thenReturn(client)

        doNothing().when(client).delete(uri)

        spy.deleteSliceProfileInstance(mockExecution)

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

        AAIResourceUri networkServiceInstanceUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(networkServiceInstance.getServiceInstanceId()))

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

        DoCommonCoreNSSI spy = spy(DoCommonCoreNSSI.class)

        when(spy.getAAIClient()).thenReturn(client)

        prepareModelVer(networkServiceInstance)

        //prepareSubscriberInfo(networkServiceInstanceUri)

        prepareCloudConfiguration(constituteVNFURI, cloudRegionAAIUri)

        prepareModelVer(genericVnf)

        prepareModelVer(vfModule)

        prepareOwningEntity(networkServiceInstanceUri)

        prepareProject(cloudRegionAAIUri)

        CatalogDbUtilsFactory catalogDbUtilsFactoryMock = mock(CatalogDbUtilsFactory.class)
        when(spy.getCatalogDbUtilsFactory()).thenReturn(catalogDbUtilsFactoryMock)

        CatalogDbUtils catalogDbUtilsMock = mock(CatalogDbUtils.class)
        when(catalogDbUtilsFactoryMock.create()).thenReturn(catalogDbUtilsMock)

        String json = "{\"serviceResources\":{\"serviceVnfs\": [{\"modelInfo\": {\"modelCustomizationUuid\":\"model-customization-uuid\",\"modelId\":\"model-id\"},\"vfModules\":[{\"modelInfo\": {\"modelCustomizationUuid\":\"model-customization-uuid\",\"modelId\":\"model-id\"}}]}]}}"
        when(catalogDbUtilsMock.getServiceResourcesByServiceModelInvariantUuidString(mockExecution, networkServiceInstance.getModelInvariantId())).thenReturn(json)

        String prepareRequestDetailsResponse = spy.prepareRequestDetails(mockExecution)

        JsonUtils jsonUtil = new JsonUtils()
        String errorCode = jsonUtil.getJsonValue(prepareRequestDetailsResponse, "errorCode")
        String errMsg = jsonUtil.getJsonValue(prepareRequestDetailsResponse, "errorMessage")

        assertTrue(errMsg, errorCode == null || errorCode.isEmpty())
    }


    @Test
    void testPrepareFailedOperationStatusUpdate() {
        def currentNSSI = [:]

        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)
        when(mockExecution.getVariable("jobId")).thenReturn("job-id")
        when(mockExecution.getVariable("operationType")).thenReturn("operation-type")

        String nssiId = "5G-999"
        String nsiId = "5G-777"

        ServiceInstance nssi = new ServiceInstance()
        nssi.setServiceInstanceId(nssiId)
        nssi.setModelVersionId(UUID.randomUUID().toString())

        currentNSSI.put("nssiId", nssiId)
        currentNSSI.put("nsiId", nsiId)
        currentNSSI.put("e2eServiceInstanceId", "e2eServiceInstanceId")
        currentNSSI.put("operationId", "operationId")
        currentNSSI.put("operationType", "operationType")
        currentNSSI.put("nssi", nssi)

        DoCommonCoreNSSI spy = spy(DoCommonCoreNSSI.class)

        spy.prepareFailedOperationStatusUpdate(mockExecution)
    }


    @Test
    void testPrepareUpdateResourceOperationStatus() {
        def currentNSSI = [:]

        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)
        when(mockExecution.getVariable("jobId")).thenReturn("job-id")
        when(mockExecution.getVariable("operationType")).thenReturn("operation-type")

        String nssiId = "5G-999"
        String nsiId = "5G-777"

        ServiceInstance nssi = new ServiceInstance()
        nssi.setServiceInstanceId(nssiId)
        nssi.setModelVersionId(UUID.randomUUID().toString())

        currentNSSI.put("nssiId", nssiId)
        currentNSSI.put("nsiId", nsiId)
        currentNSSI.put("e2eServiceInstanceId", "e2eServiceInstanceId")
        currentNSSI.put("operationId", "operationId")
        currentNSSI.put("operationType", "operationType")
        currentNSSI.put("nssi", nssi)

        DoCommonCoreNSSI spy = spy(DoCommonCoreNSSI.class)

        spy.prepareUpdateResourceOperationStatus(mockExecution)

    }


    @Test
    void testGetPUTServiceInstanceProgressAcknowledged() {

        executePUTServiceInstanceProgress("ACKNOWLEDGED")
        Mockito.verify(mockExecution,times(1)).setVariable("putStatus", "processing")
    }


    @Test
    void testGetPUTServiceInstanceProgressInProgress() {

        executePUTServiceInstanceProgress("IN_PROGRESS")
        Mockito.verify(mockExecution,times(1)).setVariable("putStatus", "processing")
    }


    @Test
    void testGetPUTServiceInstanceProgressCompleted() {

        executePUTServiceInstanceProgress("COMPLETE")
        Mockito.verify(mockExecution,times(1)).setVariable("putStatus", "completed")
    }


    @Test
    void testTimeDelay() {
        DoCommonCoreNSSI obj = spy(DoCommonCoreNSSI.class)

        long before = Instant.now().toEpochMilli()
        obj.timeDelay(mockExecution)

        long after = Instant.now().toEpochMilli()

        long delay = 5L

        assertTrue(String.format("Didn't wait %d sec", delay), ((after - before) >= delay))
    }


    @Test
    void testPostProcessRequest() {

        def currentNSSI = [:]
        mockExecution.setVariable("currentNSSI", currentNSSI)

        DoCommonCoreNSSI dcnssi = new DoCommonCoreNSSI()
        dcnssi.postProcessRequest(mockExecution)

        currentNSSI = mockExecution.getVariable("currentNSSI")
        assertNull("currentNSSI is not null", currentNSSI)

    }


    void executePUTServiceInstanceProgress(String state) {

        def currentNSSI = [:]

        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        String url = "http://mso.onap:8088/serviceInstantiation/v7/serviceInstances/5G-777"

        currentNSSI['requestSelfLink'] =  url

        DoCommonCoreNSSI spy = spy(DoCommonCoreNSSI.class)

        ExternalAPIUtilFactory externalAPIUtilFactoryMock = mock(ExternalAPIUtilFactory.class)
        when(spy.getExternalAPIUtilFactory()).thenReturn(externalAPIUtilFactoryMock)

       // ExternalAPIUtil externalAPIUtilMock = mock(ExternalAPIUtil.class)

       // when(externalAPIUtilFactoryMock.create()).thenReturn(externalAPIUtilMock)

        MsoUtils msoUtilsMock = mock(MsoUtils.class)
        String basicAuth = "basicAuth"
        when(msoUtilsMock.getBasicAuth(anyString(), anyString())).thenReturn(basicAuth)

        HttpClientFactory httpClientFactoryMock = mock(HttpClientFactory.class)
        when(spy.getHttpClientFactory()).thenReturn(httpClientFactoryMock)
        Response responseMock = mock(Response.class)

        HttpClient httpClientMock = mock(HttpClient.class)


        when(httpClientFactoryMock.newJsonClient(any(), any())).thenReturn(httpClientMock)

        when(httpClientMock.get()).thenReturn(responseMock)
//        when(externalAPIUtilMock.executeExternalAPIGetCall(mockExecution, url)).thenReturn(responseMock)

        when(responseMock.getStatus()).thenReturn(200)
        when(responseMock.hasEntity()).thenReturn(true)

        String entity = "{\"request\":{\"requestStatus\":{\"requestState\":\"${state}\"}},\"state\":\"ACCEPTED\"}"
        when(responseMock.readEntity(String.class)).thenReturn(entity)

        spy.getPUTServiceInstanceProgress(mockExecution)

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
