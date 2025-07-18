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

package org.onap.aaiclient.client.aai.entities;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIClientUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;

public class RelationshipsTest {

    private final static String AAI_JSON_FILE_LOCATION = "src/test/resources/__files/aai/resources/";

    @Test
    public void run() throws IOException {
        final String content = new String(Files.readAllBytes(Paths.get(AAI_JSON_FILE_LOCATION + "e2e-complex.json")));

        AAIResultWrapper wrapper = new AAIResultWrapper(content);
        Relationships relationships = wrapper.getRelationships().get();

        List<AAIResourceUri> test = relationships.getRelatedUris(Types.VCE);
        List<AAIResourceUri> uris = Arrays.asList(
                AAIClientUriFactory
                        .createResourceUri(AAIFluentTypeBuilder.network().vce("a9fec18e-1ea3-40e4-a6c0-a89b3de07053")),
                AAIClientUriFactory
                        .createResourceUri(AAIFluentTypeBuilder.network().vce("8ae1e5f8-61f1-4c71-913a-b40cc4593cb9")),
                AAIClientUriFactory
                        .createResourceUri(AAIFluentTypeBuilder.network().vce("a2935fa9-b743-49f4-9813-a127f13c4e93")),
                AAIClientUriFactory
                        .createResourceUri(AAIFluentTypeBuilder.network().vce("c7fe7698-8063-4e26-8bd3-ca3edde0b0d4")));

        assertTrue(uris.containsAll(test) && test.containsAll(uris));

    }

    @Test
    public void getByTypeTest() throws IOException {
        final String content = new String(Files.readAllBytes(Paths.get(AAI_JSON_FILE_LOCATION + "e2e-complex.json")));

        AAIResultWrapper wrapper = new AAIResultWrapper(content);
        Relationships relationships = wrapper.getRelationships().get();

        Relationships spy = Mockito.spy(relationships);
        ArgumentCaptor<AAIResourceUri> argument = ArgumentCaptor.forClass(AAIResourceUri.class);
        doReturn(new AAIResultWrapper("{}")).when(spy).get(argument.capture());

        spy.getByType(Types.VCE, uri -> uri.nodesOnly(true));

        assertTrue(argument.getAllValues().stream().allMatch(item -> item.build().toString().contains("nodes-only")));

        argument = ArgumentCaptor.forClass(AAIResourceUri.class);

        doReturn(new AAIResultWrapper("{}")).when(spy).get(argument.capture());
        spy.getByType(Types.VCE);

        assertTrue(argument.getAllValues().stream().allMatch(item -> !item.build().toString().contains("?")));

    }
}
