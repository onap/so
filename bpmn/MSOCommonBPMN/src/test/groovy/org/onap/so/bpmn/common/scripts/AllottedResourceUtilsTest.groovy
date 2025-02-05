/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.common.scripts

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue
import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.atLeastOnce
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when
import jakarta.ws.rs.core.UriBuilder
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.mockito.internal.stubbing.answers.DoesNothing
import org.onap.aai.domain.yang.AllottedResource
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types


class AllottedResourceUtilsTest extends MsoGroovyTest{

    private static final String ALLOTTED_RESOURSE_URI = "/aai/v11/business/customers/customer/MSO-MUX-User/service-subscriptions/service-subscription/MSO-vCB/service-instances/service-instance/a1f53c6a-81a3-4e44-a900-d64f3b131d35/allotted-resources/allotted-resource/ID"

    AllottedResourceUtils allottedResourceUtils

    @Before
    void init(){
        super.init("AllottedResourceUtils")
        allottedResourceUtils = spy(new AllottedResourceUtils(mock(AbstractServiceTaskProcessor.class)))
        when(allottedResourceUtils.getAAIClient()).thenReturn(client)
    }

    @Test
    @Ignore
    void getARbyId() {
        String allottedResourceId = "allottedResourceId"
        AllottedResource expectedAllottedResource = new AllottedResource()
        expectedAllottedResource.setId("ID")
        expectedAllottedResource.setResourceVersion("1.2")
        when(client.get(any(AAIResourceUri.class))).thenReturn(new AAIResultWrapper(expectedAllottedResource))
        AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(Types.ALLOTTED_RESOURCE.getFragment(allottedResourceId))
        when(allottedResourceUtils.setExecutionVariables(mockExecution,expectedAllottedResource,resourceUri)).thenAnswer(new DoesNothing())
        boolean allottedResource = allottedResourceUtils.ifExistsAR(mockExecution,allottedResourceId)
        assertTrue(allottedResource)
    }

    @Test
    void getARbyLink() {
        println "************ testGetARbyLink ************* "

        AllottedResource expectedAllottedResource = new AllottedResource()
        expectedAllottedResource.setId("ID")
        AAIResourceUri uri = AAIUriFactory.createResourceFromExistingURI(Types.ALLOTTED_RESOURCE, UriBuilder.fromPath(ALLOTTED_RESOURSE_URI).build())
        when(client.get(AllottedResource.class, uri)).thenReturn(Optional.of(expectedAllottedResource))
        Optional<AllottedResource> allottedResource = allottedResourceUtils.getARbyLink(mockExecution, ALLOTTED_RESOURSE_URI,"")
        assertEquals(expectedAllottedResource.getId(),allottedResource.get().getId())
    }

    @Test
    void updateAROrchStatus() {
        allottedResourceUtils.updateAROrchStatus(mockExecution,"PENDING",ALLOTTED_RESOURSE_URI)
    }

    @Test
    void deleteAR() {
        allottedResourceUtils.deleteAR(mockExecution,ALLOTTED_RESOURSE_URI)
        verify(mockExecution,atLeastOnce()).setVariable("wasDeleted","true")
    }

}
