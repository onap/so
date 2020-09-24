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
import org.onap.aai.domain.yang.*
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.Relationships
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAISimpleUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.aai.entities.uri.ServiceInstanceUri
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.so.serviceinstancebeans.RequestDetails

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue
import static org.mockito.Mockito.*

class DoCommonCoreNSSITest extends MsoGroovyTest {
    @Before
    void init() throws IOException {
        super.init("DoCommonCoreNSSITest")
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
        String sliceParams =  "{\n" +
                "\"sliceProfile\":{\"sliceProfileId\":\"${sliceProfileId}\",\"snssaiList\":${snssaiList}}\n" +
                "}"
        when(mockExecution.getVariable("sliceParams")).thenReturn(sliceParams)

        DoCommonCoreNSSI dcnssi = new DoCommonCoreNSSI()
        dcnssi.preProcessRequest(mockExecution)

        def currentNSSI = [:]
        currentNSSI.put("nssiId", nssiId)
        currentNSSI.put("nsiId", nsiId)
        currentNSSI.put("sliceProfile", "{\"sliceProfileId\":\"slice-profile-id\",\"snssaiList\":[\"S-NSSAI\"]}")
        currentNSSI.put("S-NSSAI", snssai)
        currentNSSI.put("sliceProfileId", sliceProfileId)
        Mockito.verify(mockExecution,times(1)).setVariable("currentNSSI", currentNSSI)

    }


    @Test
    void testGetNetworkServiceInstance() {
        def currentNSSI = [:]
        currentNSSI.put("nssiId","5G-999")

        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, "5G-999")
        AAIResourceUri networkServiceInstanceUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, "NS-777")

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
        AAIResourceUri aru = new ServiceInstanceUri(networkServiceInstanceUri)
        arus.add(aru)

        when(client.get(nssiUri)).thenReturn(wrapperMock)
        when(wrapperMock.getRelationships()).thenReturn(orsMock)

        when(rsMock.getRelatedAAIUris(AAIObjectType.SERVICE_INSTANCE)).thenReturn(arus)
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

        AAIResourceUri networkServiceInstanceUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, networkServiceInstance.getServiceInstanceId())

        Optional<GenericVnf> genericVnfOpt = Optional.of(genericVNF)
        AAIResourceUri genericVNFUri = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, genericVNF.getVnfId())

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

        when(rsMock.getRelatedAAIUris(AAIObjectType.GENERIC_VNF)).thenReturn(arus)
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

        DoCommonCoreNSSI obj = new DoCommonCoreNSSI()
        obj.getNSSIAssociatedProfiles(mockExecution)

        List<SliceProfile> associatedProfiles = (List<SliceProfile>)currentNSSI.get("associatedProfiles")
        assertTrue("Either associatedProfiles doesn't exist or size is incorrect", (associatedProfiles != null && associatedProfiles.size() == 2))
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

        DoCommonCoreNSSI spy = spy(DoCommonCoreNSSI.class)
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
    void testRemoveSPAssociationWithNSSI() {
        def currentNSSI = [:]

        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        String nssiId = "5G-999"
        currentNSSI.put("nssiId", nssiId)
        ServiceInstance nssi = new ServiceInstance()
        nssi.setServiceInstanceId(nssiId)
        nssi.setSliceProfiles(new SliceProfiles())

        currentNSSI.put("nssi", nssi)

        AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, nssiId)

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

        currentNSSI.put("globalSubscriberId", globalSubscriberId)
        currentNSSI.put("serviceType", serviceType)
        currentNSSI.put("nssiId", nssiId)

        String theSNSSAI = "theS-NSSAI"

        SliceProfile sliceProfile = new SliceProfile()
        sliceProfile.setSNssai(theSNSSAI)
        sliceProfile.setProfileId("prof-id")

        AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, nssiId)

        currentNSSI.put("sliceProfileS-NSSAI", sliceProfile)

        DoCommonCoreNSSI spy = spy(DoCommonCoreNSSI.class)

        when(spy.getAAIClient()).thenReturn(client)

        doNothing().when(client).delete(nssiUri)

        spy.deleteSliceProfileInstance(mockExecution)

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

        DoCommonCoreNSSI spy = spy(DoCommonCoreNSSI.class)

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

        AAIResourceUri networkServiceInstanceUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_INSTANCE, "networkServiceInstance.getServiceInstanceId()")

        AAIResourceUri cloudRegionAAIUri = AAIUriFactory.createResourceUri(AAIObjectType.CLOUD_REGION, "cloud-owner", "cloud-region-id")

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

        AAIResourceUri constituteVNFURI = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, genericVnf.getVnfId())

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

        when(rsMock.getRelatedAAIUris(AAIObjectType.PROJECT)).thenReturn(arus)

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

        when(rsMock.getRelatedAAIUris(AAIObjectType.OWNING_ENTITY)).thenReturn(arus)

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

        when(rsMock.getRelatedAAIUris(AAIObjectType.CLOUD_REGION)).thenReturn(arus)

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

        AAIResourceUri serviceSubscriptionUri = AAIUriFactory.createResourceUri(AAIObjectType.SERVICE_SUBSCRIPTION, "global-customer-id", "service-type")

        AAIResourceUri customerUri = AAIUriFactory.createResourceUri(AAIObjectType.CUSTOMER, "global-customer-id")
        List<AAIResourceUri> arus = new ArrayList<>()

        arus.add(serviceSubscriptionUri)

        when(rsMock.getRelatedAAIUris(AAIObjectType.SERVICE_SUBSCRIPTION)).thenReturn(arus)

        ServiceSubscription serviceSubscription = new ServiceSubscription()
        serviceSubscription.setServiceType("service-type")
        Optional<ServiceSubscription> serviceSubscriptionOpt = Optional.of(serviceSubscription)

        when(client.get(ServiceSubscription.class, serviceSubscriptionUri)).thenReturn(serviceSubscriptionOpt)

        when(client.get(networkServiceInstanceUri)).thenReturn(wrapperMock)

        when(rsMock.getRelatedAAIUris(AAIObjectType.CUSTOMER)).thenReturn(arus)

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

        AAIResourceUri modelVerUrl = AAIUriFactory.createResourceUri(AAIObjectType.MODEL_VER, networkServiceInstance.getModelInvariantId(), networkServiceInstance.getModelVersionId())
        when(client.get(ModelVer.class, modelVerUrl)).thenReturn(modelVerOpt)
    }

    void prepareModelVer(GenericVnf genericVnf) {
        ModelVer modelVer = new ModelVer()
        modelVer.setModelVersionId("model-version-id")
        modelVer.setModelName("model-name")
        modelVer.setModelVersion("model-version")

        Optional<ModelVer> modelVerOpt = Optional.of(modelVer)

        AAIResourceUri modelVerUrl = AAIUriFactory.createResourceUri(AAIObjectType.MODEL_VER, genericVnf.getModelInvariantId(), genericVnf.getModelVersionId())
        when(client.get(ModelVer.class, modelVerUrl)).thenReturn(modelVerOpt)
    }

    void prepareModelVer(VfModule vfModule) {
        ModelVer modelVer = new ModelVer()
        modelVer.setModelVersionId("model-version-id")
        modelVer.setModelName("model-name")
        modelVer.setModelVersion("model-version")

        Optional<ModelVer> modelVerOpt = Optional.of(modelVer)

        AAIResourceUri modelVerUrl = AAIUriFactory.createResourceUri(AAIObjectType.MODEL_VER, vfModule.getModelInvariantId(), vfModule.getModelVersionId())
        when(client.get(ModelVer.class, modelVerUrl)).thenReturn(modelVerOpt)
    }
}
