/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.aai;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;

public class AAIObjectTypeTest {


    @Test
    public void fromTypeNameTest() throws IllegalArgumentException, IllegalAccessException, InstantiationException {
        AAIObjectType type = AAIObjectType.fromTypeName("allotted-resource");
        assertEquals("allotted-resource", type.typeName());

    }

    @Test
    public void customTypeTest() throws IllegalArgumentException, IllegalAccessException, InstantiationException {
        AAIObjectType type = AAIObjectType.fromTypeName("my-custom-name");
        assertEquals("my-custom-name", type.typeName());

    }

    @Test
    public void verifyDefaultCase() {
        assertEquals("default removed for tenant", "tenant", AAIObjectType.DEFAULT_TENANT.typeName());
        assertEquals("default removed for cloud-region", "cloud-region", AAIObjectType.DEFAULT_CLOUD_REGION.typeName());
    }

    @Test
    public void verifyRegularCase() {
        assertEquals("default removed for tenant", "allotted-resource", AAIObjectType.ALLOTTED_RESOURCE.typeName());
    }

    @Test
    public void instanceGroupObjectTypeTest() {
        final String id = "test1";
        AAIResourceUri aaiUri = AAIUriFactory.createResourceUri(AAIObjectType.INSTANCE_GROUP, id);
        assertEquals("/network/instance-groups/instance-group/test1", aaiUri.build().toString());
    }

    @Test
    public void collectionObjectTypeTest() {
        final String id = "test1";
        AAIResourceUri aaiUri = AAIUriFactory.createResourceUri(AAIObjectType.COLLECTION, id);
        assertEquals("/network/collections/collection/test1", aaiUri.build().toString());
    }

    @Test
    public void genericVnfTest() {
        AAIObjectType type = AAIObjectType.GENERIC_VNF;
        assertEquals("/network/generic-vnfs/generic-vnf/{vnf-id}", type.uriTemplate());
        assertEquals("/generic-vnfs/generic-vnf/{vnf-id}", type.partialUri());
    }

    @Test
    public void pInterfaceTest() {
        AAIObjectType type = AAIObjectType.P_INTERFACE;
        assertEquals("/cloud-infrastructure/pservers/pserver/{hostname}/p-interfaces/p-interface/{interface-name}",
                type.uriTemplate());
        assertEquals("/p-interfaces/p-interface/{interface-name}", type.partialUri());
    }

    @Test
    public void networkPolicyObjectTypeTest() {
        final String id = "test1";
        AAIResourceUri aaiUri = AAIUriFactory.createResourceUri(AAIObjectType.NETWORK_POLICY, id);
        assertEquals("/network/network-policies/network-policy/test1", aaiUri.build().toString());
    }
}
