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

package org.onap.so.bpmn.infrastructure.scripts

import static org.mockito.Mockito.times
import static org.mockito.Mockito.when
import jakarta.ws.rs.core.UriBuilder
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Spy
import org.onap.aai.domain.yang.AllottedResource
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.so.bpmn.mock.FileUtil

class DoCustomDeleteE2EServiceInstanceV2Test extends MsoGroovyTest {

    @Spy
    DoCustomDeleteE2EServiceInstanceV2 doCustomDeleteE2EServiceInstanceV2

    @Before
    void init(){
        super.init("DoCustomDeleteE2EServiceInstanceV2")
        Mockito.when(doCustomDeleteE2EServiceInstanceV2.getAAIClient()).thenReturn(client)
    }

    @Test
    void testPrepareServiceDeleteResource(){
        when(mockExecution.getVariable("serviceInstance")).thenReturn(FileUtil.readResourceFile("__files/AAI/ServiceInstanceWithAR.json"))
        def relink = "/aai/v11/business/customers/customer/testCustIdInfra/service-subscriptions/service-subscription/HNPORTAL/service-instances/service-instance/testServiceInstanceIdPortalPri/allotted-resources/allotted-resource/testAllottedResourceIdPortalPri"
        AAIResourceUri uri = AAIUriFactory.createResourceFromExistingURI(Types.ALLOTTED_RESOURCE, UriBuilder.fromPath(relink).build())
        AllottedResource ar = new AllottedResource();
        ar.setId("ar1")
        ar.setType("ar")
        ar.setRole("ar")
        ar.setResourceVersion("1234")
        when(client.get(AllottedResource.class,uri)).thenReturn(Optional.of(ar))
        doCustomDeleteE2EServiceInstanceV2.prepareServiceDeleteResource(mockExecution)
        Mockito.verify(client, times(1)).get(AllottedResource.class,uri)
    }
}
