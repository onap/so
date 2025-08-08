/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2022 Samsung Electronics
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

package org.onap.aaiclient.client.aai;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;

public class AAIObjectTypeTest {

    @Test
    public void fromTypeNameTest() throws IllegalArgumentException {
        AAIObjectType type = AAIObjectType.fromTypeName("generic-query");
        assertEquals("generic-query", type.typeName());

    }

    @Test
    public void customTypeTest() throws IllegalArgumentException {
        AAIObjectType type = AAIObjectType.fromTypeName("my-custom-name");
        assertEquals("my-custom-name", type.typeName());

    }

    @Test
    public void equalityTest() {

        AAIObjectType genericVnf = AAIFluentTypeBuilder.network().genericVnf("test").build();
        AAIObjectType genericVnf2 = AAIFluentTypeBuilder.network().genericVnf("test2").build();

        assertEquals(genericVnf2, genericVnf);
    }

    @Test
    public void uriParamTest() {

        assertEquals("vnf-id", AAIFluentTypeBuilder.Types.GENERIC_VNF.getUriParams().vnfId);

        assertEquals("l-interface.interface-name", AAIFluentTypeBuilder.Types.L_INTERFACE.getUriParams().interfaceName);

        assertEquals("cloud-owner", AAIFluentTypeBuilder.Types.CLOUD_REGION.getUriParams().cloudOwner);

        assertEquals("cloud-region-id", AAIFluentTypeBuilder.Types.CLOUD_REGION.getUriParams().cloudRegionId);


    }
}
