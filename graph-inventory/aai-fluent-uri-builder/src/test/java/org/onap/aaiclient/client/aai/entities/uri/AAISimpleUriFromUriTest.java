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
import javax.ws.rs.core.UriBuilder;
import org.junit.jupiter.api.Test;
import org.onap.aaiclient.client.aai.AAIObjectType;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;

public class AAISimpleUriFromUriTest {



    @Test
    public void removeHost() {

        AAIResourceUri uri = new AAISimpleUri(AAIObjectType.UNKNOWN,
                UriBuilder
                        .fromUri("https://localhost:8443/aai/v9/network/vces/vce/a9fec18e-1ea3-40e4-a6c0-a89b3de07053")
                        .build());

        assertEquals("/network/vces/vce/a9fec18e-1ea3-40e4-a6c0-a89b3de07053", uri.build().toString(),
                "root and version removed");

    }

    @Test
    public void noChange() {

        AAIResourceUri uri = new AAISimpleUri(AAIObjectType.UNKNOWN,
                UriBuilder.fromUri("/network/vces/vce/a9fec18e-1ea3-40e4-a6c0-a89b3de07053").build());

        assertEquals("/network/vces/vce/a9fec18e-1ea3-40e4-a6c0-a89b3de07053", uri.build().toString(), "no change");

    }

    @Test
    public void encodingPreserved() {

        AAIResourceUri uri = new AAISimpleUri(AAIObjectType.UNKNOWN,
                UriBuilder.fromUri("/network/vces/vce/a9f%20%20ec18e-1ea3-40e4-a6c0-a89b3de07053").build());

        assertEquals("/network/vces/vce/a9f%20%20ec18e-1ea3-40e4-a6c0-a89b3de07053", uri.build().toString(),
                "encoding preserved");

    }

    @Test
    public void beforeBuildEquality() {

        AAIResourceUri uri = new AAISimpleUri(AAIFluentTypeBuilder.network().vce("").build(),
                UriBuilder.fromUri("/network/vces/vce/a9f%20%20ec18e-1ea3-40e4-a6c0-a89b3de07053").build());

        AAIResourceUri uri2 = new AAISimpleUri(AAIFluentTypeBuilder.network().vce("").build(),
                "a9f  ec18e-1ea3-40e4-a6c0-a89b3de07053");
        assertEquals(uri2, uri, "are equal");

    }
}
