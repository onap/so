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

package org.onap.aaiclient.client.aai.entities.uri;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAISimpleUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;

public class AAIUriFactoryTest {

    @Test
    public void testCreateResourceUri() {

        AAIResourceUri uri =
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf("VIP(VelocitytoIP)"));

        String expected = "/network/generic-vnfs/generic-vnf/VIP%28VelocitytoIP%29";
        assertEquals(expected, uri.build().toString());
    }

    @Test
    public void testCreateNodesUri() {

        AAIResourceUri uri = AAIUriFactory.createNodesUri(Types.GENERIC_VNF.getFragment("VIP(VelocitytoIP)"));

        String expected = "/nodes/generic-vnfs/generic-vnf/VIP%28VelocitytoIP%29";
        assertEquals(expected, uri.build().toString());
    }

    @Test
    public void testCreateResourceFromExistingURI() {

        AAIResourceUri uri =
                new AAISimpleUri(AAIFluentTypeBuilder.network().genericVnf("").build(), "VIP(VelocitytoIP)");
        AAIResourceUri uri2 = AAIUriFactory.createResourceFromExistingURI(Types.GENERIC_VNF, uri.build());

        String expected = "/network/generic-vnfs/generic-vnf/VIP%28VelocitytoIP%29";
        assertEquals(expected, uri2.build().toString());
    }

    @Test
    public void testCreateResourceURIForPluralsWithValues() {

        AAIPluralResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                .customer("customerId").serviceSubscription("serviceType").serviceInstances());

        String expected =
                "/business/customers/customer/customerId/service-subscriptions/service-subscription/serviceType/service-instances";
        assertEquals(expected, uri.build().toString());
    }

    @Test
    public void testCreateResourceURIForPluralsWithNoValues() {

        AAIPluralResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customers());

        String expected = "/business/customers";
        assertEquals(expected, uri.build().toString());
    }
}
