/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright © 2025 Deutsche Telekom AG Intellectual Property. All rights reserved.
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

package org.onap.aaiclient.client.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.aai.domain.yang.Complex;
import org.onap.aai.domain.yang.OamNetwork;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.RelationshipList;
import org.onap.aaiclient.client.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

@ExtendWith(MockitoExtension.class)
public class RelationshipsTest {

    private static final String AAI_JSON_FILE_LOCATION = "src/test/resources/__files/";
    private static final ObjectMapper mapper;

    @Mock
    AAIResourcesClient client;

    static {
        mapper = new ObjectMapper();
        mapper.registerModule(new JaxbAnnotationModule());
    }


    @Test
    public void thatRelationshipsCanBeNull() throws IOException {
        Relationships relationships = new Relationships(null, null);
        assertFalse(relationships.hasRelationshipsTo(Types.VCE));
    }

    @Test
    public void verifyHasRelationshipsToTrue() throws IOException {
        RelationshipList relationshipList = new RelationshipList();
        Relationship relationshipToServiceInstance = new Relationship();
        relationshipToServiceInstance.setRelatedTo("service-instance");
        relationshipList.getRelationship().add(relationshipToServiceInstance);
        Relationship relationshipToVce = new Relationship();
        relationshipToVce.setRelatedTo("vce");
        relationshipList.getRelationship().add(relationshipToVce);
        Relationships relationships = new Relationships(relationshipList, null);

        assertTrue(relationships.hasRelationshipsTo(Types.VCE));
    }

    @Test
    public void verifyHasRelationshipsToFalse() throws IOException {
        RelationshipList relationshipList = new RelationshipList();
        Relationship relationshipToServiceInstance = new Relationship();
        relationshipToServiceInstance.setRelatedTo("service-instance");
        relationshipList.getRelationship().add(relationshipToServiceInstance);
        Relationships relationships = new Relationships(relationshipList, null);

        assertFalse(relationships.hasRelationshipsTo(Types.VCE));
    }

    @Test
    public void getByTypeTest() throws IOException {
        final String content = new String(Files.readAllBytes(Paths.get(AAI_JSON_FILE_LOCATION + "e2e-complex.json")));

        Complex complex = mapper.readValue(content, Complex.class);
        OamNetwork oam1 = new OamNetwork();
        oam1.setNetworkUuid("oam1");
        var oam1Wrapper = new AAIResultWrapper<>(oam1, client);
        OamNetwork oam2 = new OamNetwork();
        oam2.setNetworkUuid("oam2");
        var oam2Wrapper = new AAIResultWrapper<>(oam2, client);
        OamNetwork oam3 = new OamNetwork();
        oam3.setNetworkUuid("oam3");
        var oam3Wrapper = new AAIResultWrapper<>(oam3, client);

        when(client.get(any(AAIResourceUri.class), ArgumentMatchers.<Class<OamNetwork>>any())).thenReturn(oam1Wrapper,
                oam2Wrapper, oam3Wrapper);

        Relationships relationships = new Relationships(complex.getRelationshipList(), client);

        List<OamNetwork> result = relationships.getByType(OamNetwork.class);
        assertNotNull(result);
        assertEquals(3, result.size());
    }
}
