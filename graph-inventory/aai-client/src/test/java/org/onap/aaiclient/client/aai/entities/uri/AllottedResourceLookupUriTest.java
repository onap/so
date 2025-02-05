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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import jakarta.ws.rs.core.UriBuilder;
import org.junit.Test;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.onap.aaiclient.client.graphinventory.exceptions.GraphInventoryPayloadException;
import org.onap.aaiclient.client.graphinventory.exceptions.GraphInventoryUriNotFoundException;

public class AllottedResourceLookupUriTest {

    @Test
    public void oneKey()
            throws IOException, URISyntaxException, GraphInventoryUriNotFoundException, GraphInventoryPayloadException {

        AllottedResourceLookupUri instance = new AllottedResourceLookupUri(Types.ALLOTTED_RESOURCE.getFragment("key1"));
        AllottedResourceLookupUri spy = spy(instance);
        doReturn(
                "/business/customers/customer/key1/service-subscriptions/service-subscription/key2/service-instances/service-instance/key3/allotted-resources/allotted-resource/key4")
                        .when(spy).getObjectById(any(Object.class));

        final URI result = spy.locateAndBuild();
        final URI expected = UriBuilder.fromPath(
                "/business/customers/customer/key1/service-subscriptions/service-subscription/key2/service-instances/service-instance/key3/allotted-resources/allotted-resource/key4")
                .build();
        assertEquals("result is equal", expected, result);

    }
}
