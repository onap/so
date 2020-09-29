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
import org.onap.aai.domain.yang.v19.ServiceInstance
import org.onap.aai.domain.yang.v19.SliceProfile
import org.onap.aai.domain.yang.v19.SliceProfiles
import org.onap.aaiclient.client.aai.entities.AAIEdgeLabel
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.bpmn.common.scripts.MsoGroovyTest

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue
import static org.mockito.Mockito.*

class DoModifyCoreNSSITest extends MsoGroovyTest  {

    @Before
    void init() throws IOException {
        super.init("DoModifyCoreNSSITest")
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

        DoModifyCoreNSSI obj = new DoModifyCoreNSSI()
        obj.getNSSIAssociatedProfiles(mockExecution)

        List<SliceProfile> associatedProfiles = (List<SliceProfile>)currentNSSI.get("associatedProfiles")
        assertTrue("Either associatedProfiles doesn't exist or size is incorrect", (associatedProfiles != null && associatedProfiles.size() == 2))
    }


    @Test
    void testCalculateSNSSAISliceProfileInstanceHasToBeDeleted() {
        def currentNSSI = [:]
        when(mockExecution.getVariable("currentNSSI")).thenReturn(currentNSSI)

        when(mockExecution.getVariable("isCreateSliceProfileInstance")).thenReturn("false")

        String theSNSSAI = "theS-NSSAI"

        currentNSSI.put("S-NSSAI", theSNSSAI)

        List<SliceProfile> associatedProfiles = new ArrayList<>()
        SliceProfile sliceProfile1 = new SliceProfile()
        sliceProfile1.setSNssai("snssai1")

        SliceProfile sliceProfile2 = new SliceProfile()
        sliceProfile2.setSNssai(theSNSSAI)

        SliceProfile sliceProfile3 = new SliceProfile()
        sliceProfile3.setSNssai("snssai2")

        associatedProfiles.add(sliceProfile1)
        associatedProfiles.add(sliceProfile2)
        associatedProfiles.add(sliceProfile3)

        int sizeBefore = associatedProfiles.size()

        currentNSSI.put("associatedProfiles", associatedProfiles)

        DoModifyCoreNSSI obj = new DoModifyCoreNSSI()
        obj.calculateSNSSAI(mockExecution)

        List<SliceProfile> snssais = (List<SliceProfile>)currentNSSI.get("S-NSSAIs")
        SliceProfile sliceProfileContainsSNSSAI = (SliceProfile)currentNSSI.get("sliceProfileS-NSSAI")

        assertTrue("Either snssais doesn't exist or size is incorrect", (snssais != null && snssais.size() == (sizeBefore - 1)))
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

        currentNSSI.put("sliceProfile", "{\"sliceProfileId\":\"slice-profile-id\",\"snssaiList\":[\"S-NSSAI\"],\"expDataRateUL\":\"12\"}")
        currentNSSI.put("sliceProfileId", sliceProfileId)

        DoModifyCoreNSSI spy = spy(DoModifyCoreNSSI.class)
        when(spy.getAAIClient()).thenReturn(client)

        String globalSubscriberId = "globalSubscriberId"
        String subscriptionServiceType = "subscription-service-type"
        String nssiId = "nssiId"

        when(mockExecution.getVariable("globalSubscriberId")).thenReturn(globalSubscriberId)
        when(mockExecution.getVariable("subscriptionServiceType")).thenReturn(subscriptionServiceType)

        currentNSSI.put("nssiId", nssiId)

        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(subscriptionServiceType).serviceInstance(nssiId).sliceProfile(sliceProfileId))

        SliceProfile sliceProfile = new SliceProfile()
        sliceProfile.setProfileId(sliceProfileId)

        doNothing().when(client).create(uri, sliceProfile)

        spy.createSliceProfileInstance(mockExecution)

        assertNotNull("Slice Profile doesn't exist", currentNSSI.get("createdSliceProfile"))
        assertTrue("Unexpected Slice Profile Id", ((SliceProfile)currentNSSI.get("createdSliceProfile")).getProfileId().equals(sliceProfile.getProfileId()))
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

        AAIResourceUri nssiUri = AAIUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment(nssiId))
        AAIResourceUri sliceProfileUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer(globalSubscriberId).serviceSubscription(subscriptionServiceType).serviceInstance(nssiId).sliceProfile(sliceProfileId))

        when(mockExecution.getVariable("globalSubscriberId")).thenReturn(globalSubscriberId)
        when(mockExecution.getVariable("subscriptionServiceType")).thenReturn(subscriptionServiceType)

        currentNSSI.put("sliceProfileId", sliceProfileId)

        SliceProfile sliceProfile = new SliceProfile()
        currentNSSI.put("createdSliceProfile", sliceProfile)

        ServiceInstance nssi = new ServiceInstance()
        nssi.setServiceInstanceId(nssiId)
        nssi.setSliceProfiles(new SliceProfiles())
        currentNSSI.put("nssi", nssi)

        int sizeBelore = nssi.getSliceProfiles().getSliceProfile().size()

        doNothing().when(client).update(nssiUri, nssi)
        doNothing().when(client).connect(sliceProfileUri, nssiUri, AAIEdgeLabel.BELONGS_TO)

        spy.associateSliceProfileInstanceWithNSSI(mockExecution)

        assertTrue("Wrong number of associated slice profiles", ((ServiceInstance)currentNSSI.get("nssi")).getSliceProfiles().getSliceProfile().size() == (sizeBelore + 1))
    }

}
