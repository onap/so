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
import org.onap.aai.domain.yang.v19.AllottedResource
import org.onap.aai.domain.yang.v19.ServiceInstance
import org.onap.aai.domain.yang.v19.SliceProfile
import org.onap.aai.domain.yang.v19.SliceProfiles
import org.onap.aaiclient.client.aai.entities.AAIEdgeLabel
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.Relationships
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.springframework.data.domain.Slice

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue
import static org.mockito.Mockito.*

class DoModifyCoreNSSITest extends MsoGroovyTest  {

    @Before
    void init() throws IOException {
        super.init("DoModifyCoreNSSITest")
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
                ",\"modifyAction\":\"${modifyAction}\"\n" +
                "}"
        when(mockExecution.getVariable("sliceParams")).thenReturn(sliceParams)

        DoModifyCoreNSSI obj = new DoModifyCoreNSSI()
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
    void testCalculateSNSSAISliceProfileInstanceHasToBeDeleted() {
        def currentNSSI = [:]
        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        when(mockExecution.getVariable("isCreateSliceProfileInstance")).thenReturn("false")

        String theSNSSAI = "theS-NSSAI"

        currentNSSI.put("S-NSSAI", theSNSSAI)

        String sliceProfileId = "the-slice-profile-id"
        currentNSSI['sliceProfileId'] = sliceProfileId

        List<SliceProfile> associatedProfiles = new ArrayList<>()
        SliceProfile sliceProfile1 = new SliceProfile()
        sliceProfile1.setProfileId(sliceProfileId)
        sliceProfile1.setSNssai(theSNSSAI)

        SliceProfile sliceProfile2 = new SliceProfile()
        sliceProfile2.setProfileId("sp-2")
        sliceProfile2.setSNssai(theSNSSAI)

        SliceProfile sliceProfile3 = new SliceProfile()
        sliceProfile3.setProfileId("sp-3")
        sliceProfile3.setSNssai("snssai2")

        associatedProfiles.add(sliceProfile1)
        associatedProfiles.add(sliceProfile2)
        associatedProfiles.add(sliceProfile3)

        currentNSSI.put("associatedProfiles", associatedProfiles)

        when(mockExecution.getVariable("isCreateSliceProfileInstance" )).thenReturn(false)

        DoModifyCoreNSSI obj = new DoModifyCoreNSSI()
        obj.calculateSNSSAI(mockExecution)

        List<SliceProfile> snssais = (List<SliceProfile>)currentNSSI.get("S-NSSAIs")
        SliceProfile sliceProfileContainsSNSSAI = (SliceProfile)currentNSSI.get("sliceProfileS-NSSAI")

        assertTrue("Either snssais doesn't exist or size is incorrect", (snssais != null && snssais.size() == (associatedProfiles.size() - 1)))
        assertNotNull("Slice Profile which contains given S-NSSAI not found", sliceProfileContainsSNSSAI)
        assertTrue("Wrong Slice Profile", sliceProfileContainsSNSSAI.getSNssai().equals(theSNSSAI))
    }


    @Test
    void testCalculateSNSSAISliceProfileInstanceHasToBeCreated() {
        def currentNSSI = [:]
        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        when(mockExecution.getVariable("isCreateSliceProfileInstance")).thenReturn("true")

        String theSNSSAI = "theS-NSSAI"

        currentNSSI.put("S-NSSAI", theSNSSAI)

        List<SliceProfile> associatedProfiles = new ArrayList<>()
        SliceProfile sliceProfile1 = new SliceProfile()
        sliceProfile1.setSNssai("snssai1")

        SliceProfile sliceProfile2 = new SliceProfile()
        sliceProfile2.setSNssai("snssai2")

        associatedProfiles.add(sliceProfile1)
        associatedProfiles.add(sliceProfile2)

        int sizeBefore = associatedProfiles.size()

        currentNSSI.put("associatedProfiles", associatedProfiles)

        DoModifyCoreNSSI obj = new DoModifyCoreNSSI()
        obj.calculateSNSSAI(mockExecution)

        List<SliceProfile> snssais = (List<SliceProfile>)currentNSSI.get("S-NSSAIs")

        assertTrue("Either snssais doesn't exist or size is incorrect", (snssais != null && snssais.size() == (sizeBefore + 1)))

    }


    @Test
    void testCreateSliceProfileInstance() {
        def currentNSSI = [:]

        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        String sliceProfileId = "sliceProfileId"

        currentNSSI.put("sliceProfile", "{\"sliceProfileId\":\"slice-profile-id\",\"snssaiList\":[\"S-NSSAI\"],\"expDataRateUL\":\"12\",\"expDataRateDL\":\"5\"," +
                        "\"activityFactor\":\"2\",\"resourceSharingLevel\":\"resource-sharing-level\",\"uEMobilityLevel\":\"ue-mobility-level\",\"coverageAreaTAList\":\"coverage-area-ta-list\"," +
                        "\"maxNumberofUEs\":\"10000\",\"latency\":\"7\"}")
        currentNSSI.put("sliceProfileId", sliceProfileId)

        List<String> snssais = new ArrayList<>()
        snssais.add("s-nssai")
        currentNSSI.put("S-NSSAIs", snssais)

        DoModifyCoreNSSI spy = spy(DoModifyCoreNSSI.class)
        when(spy.getAAIClient()).thenReturn(client)

        String globalSubscriberId = "globalSubscriberId"
        String subscriptionServiceType = "subscription-service-type"
        String nssiId = "nssiId"

        when(mockExecution.getVariable("globalSubscriberId")).thenReturn(globalSubscriberId)
        when(mockExecution.getVariable("subscriptionServiceType")).thenReturn(subscriptionServiceType)

        currentNSSI.put("nssiId", nssiId)

        ServiceInstance sliceProfileInstance = new ServiceInstance()
        sliceProfileInstance.setServiceInstanceId(UUID.randomUUID().toString())

        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(subscriptionServiceType).
                                                            serviceInstance(sliceProfileInstance.getServiceInstanceId()))

        SliceProfile sliceProfile = new SliceProfile()
        sliceProfile.setProfileId(sliceProfileId)

        doNothing().when(client).create(uri, sliceProfileInstance)

        spy.createSliceProfileInstance(mockExecution)

        assertTrue("Slice Profile Instance Id doesn't exist", (currentNSSI.get("createdSliceProfileInstanceId")) != null)

    }


    @Test
    void testAssociateSliceProfileInstanceWithNSSI() {
        def currentNSSI = [:]

        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        DoModifyCoreNSSI spy = spy(DoModifyCoreNSSI.class)
        when(spy.getAAIClient()).thenReturn(client)

        String sliceProfileId = "sliceProfileId"

        when(mockExecution.getVariable("sliceProfileID")).thenReturn(sliceProfileId)

        String nssiId = "5G-999"
        currentNSSI.put("nssiId", nssiId)

        String globalSubscriberId = "globalSubscriberId"
        String subscriptionServiceType = "subscriptionServiceType"

        String sliceProfileInstanceId = "slice-rpofile-instance-id"
        currentNSSI.put("createdSliceProfileInstanceId", sliceProfileInstanceId)

        AllottedResource allottedResource = new AllottedResource()

        String allottedResourceId = UUID.randomUUID().toString()

        allottedResource.setId(allottedResourceId)

        AAIResourceUri allottedResourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(subscriptionServiceType).serviceInstance(sliceProfileInstanceId).allottedResource(allottedResourceId))
        doNothing().when(client).create(allottedResourceUri, allottedResource)

        currentNSSI.put("allottedResourceUri", allottedResourceUri)

        when(mockExecution.getVariable("globalSubscriberId")).thenReturn(globalSubscriberId)
        when(mockExecution.getVariable("subscriptionServiceType")).thenReturn(subscriptionServiceType)

        currentNSSI.put("sliceProfileId", sliceProfileId)

        SliceProfile sliceProfile = new SliceProfile()
        currentNSSI.put("createdSliceProfile", sliceProfile)

        AAIResourceUri sliceProfileInstanceUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(sliceProfileInstanceId))

        AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(nssiId))

        ServiceInstance sliceProfileInstance = new ServiceInstance()
        sliceProfileInstance.setServiceInstanceId(sliceProfileInstanceId)
        Optional<ServiceInstance> sliceProfileInstanceOpt = Optional.of(sliceProfileInstance)

        when(client.get(ServiceInstance.class, sliceProfileInstanceUri)).thenReturn(sliceProfileInstanceOpt)
        doNothing().when(client).update(sliceProfileInstanceUri, sliceProfileInstance)

        ServiceInstance nssi = new ServiceInstance()
        nssi.setServiceInstanceId(nssiId)
        nssi.setSliceProfiles(new SliceProfiles())
        currentNSSI.put("nssi", nssi)

        doNothing().when(client).connect(nssiUri, sliceProfileInstanceUri, AAIEdgeLabel.USES)

        spy.associateSliceProfileInstanceWithNSSI(mockExecution)

    }


    @Test
    void testGetNSSIAssociatedProfilesCreateSLiceProfileEmptySLiceProfiles() {
        invokeGetNSSIAssociatedProfiles("true", true)
    }

    @Test
    void testGetNSSIAssociatedProfilesCreateSLiceProfileNonEmptySliceProfiles() {
        invokeGetNSSIAssociatedProfiles("true", false)
    }

    @Test
    void testGetNSSIAssociatedProfilesDeleteSLiceProfile() {
        invokeGetNSSIAssociatedProfiles("false", false)
    }

    void invokeGetNSSIAssociatedProfiles(String isCreateSliceProfileInstance, boolean isEmptySliceProfiles) {
        def currentNSSI = [:]
        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        String globalSubscriberId = "5GCustomer"
        String subscriptionServiceType = "5G"
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn(globalSubscriberId)
        when(mockExecution.getVariable("subscriptionServiceType")).thenReturn(subscriptionServiceType)

        when(mockExecution.getVariable("isCreateSliceProfileInstance")).thenReturn(isCreateSliceProfileInstance)

        String nssiId = "5G-999"
        ServiceInstance nssi = new ServiceInstance()
        nssi.setServiceInstanceId(nssiId)
        currentNSSI.put("nssiId", nssiId)

        String theSLiceProfileId = "slice-profile-id"
        currentNSSI.put("sliceProfileId", theSLiceProfileId)

        SliceProfiles sliceProfiles = new SliceProfiles()

        List<SliceProfile> slProfiles = sliceProfiles.getSliceProfile()

        if(!isEmptySliceProfiles) {
            SliceProfile sp1 = new SliceProfile()
            sp1.setProfileId(theSLiceProfileId)
            slProfiles.add(sp1)
            slProfiles.add(new SliceProfile())
        }

        //nssi.setSliceProfiles(sliceProfiles)
        currentNSSI.put("nssi", nssi)

        DoModifyCoreNSSI spy = spy(DoModifyCoreNSSI.class)
        when(spy.getAAIClient()).thenReturn(client)

        AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(nssiId))

        AAIResultWrapper wrapperMock = mock(AAIResultWrapper.class) //new AAIResultWrapper(json)
        Relationships rsMock = mock(Relationships.class)
        Optional<Relationships> orsMock = Optional.of(rsMock)
        List<AAIResourceUri> allottedUris = new ArrayList<>()
        AAIResourceUri allottedUri = AAIUriFactory.createResourceUri(Types.ALLOTTED_RESOURCE.getFragment("allotted-id"))
        allottedUris.add(allottedUri)

        when(client.get(nssiUri)).thenReturn(wrapperMock)
        when(wrapperMock.getRelationships()).thenReturn(orsMock)
        when(rsMock.getRelatedUris(Types.ALLOTTED_RESOURCE)).thenReturn(allottedUris)

        String sliceProfileInstanceId = "slice-profile-instance-id"
        ServiceInstance sliceProfileInstance = new ServiceInstance()
        sliceProfileInstance.setServiceInstanceId(sliceProfileInstanceId)
        sliceProfileInstance.setServiceRole("slice-profile-instance")

        List<AAIResourceUri> sliceProfileInstanceUris = new ArrayList<>()
        AAIResourceUri sliceProfileInstanceUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(sliceProfileInstance.getServiceInstanceId()))
        sliceProfileInstanceUris.add(sliceProfileInstanceUri)

        Optional<ServiceInstance> sliceProfileInstanceOpt = Optional.of(sliceProfileInstance)

        when(client.get(allottedUri)).thenReturn(wrapperMock)
        when(rsMock.getRelatedUris(Types.SERVICE_INSTANCE)).thenReturn(sliceProfileInstanceUris)
        when(client.get(ServiceInstance.class, sliceProfileInstanceUri)).thenReturn(sliceProfileInstanceOpt)


        SliceProfiles sps = new SliceProfiles()
        sps.getSliceProfile().addAll(slProfiles)
        sliceProfileInstance.setSliceProfiles(sps)

        Optional<SliceProfiles> sliceProfilesOpt = Optional.of(sps)

        org.onap.aaiclient.client.generated.fluentbuilders.SliceProfiles sliceProfilesType =
                AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(subscriptionServiceType).serviceInstance(sliceProfileInstance.getServiceInstanceId()).sliceProfiles()

        def sliceProfilesUri = AAIUriFactory.createResourceUri(sliceProfilesType)
        when(client.exists(sliceProfilesUri)).thenReturn(true)

        when(client.get(sliceProfilesUri)).thenReturn(wrapperMock)

        when(wrapperMock.asBean(SliceProfiles.class)).thenReturn(sliceProfilesOpt)

        spy.getNSSIAssociatedProfiles(mockExecution)

        List<SliceProfile> associatedProfiles = (List<SliceProfile>)currentNSSI.get("associatedProfiles")
        int expectedSize = sps.getSliceProfile().size()

        if(!isEmptySliceProfiles) {
            assertTrue("sliceProfileInstanceUri not found in contect Map", currentNSSI.get("sliceProfileInstanceUri") != null)
        }

        assertTrue("Either associatedProfiles doesn't exist or size is incorrect", (associatedProfiles != null && associatedProfiles.size() == expectedSize))
    }


}
