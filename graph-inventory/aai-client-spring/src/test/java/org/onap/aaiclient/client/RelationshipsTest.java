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

package org.onap.aaiclient.client;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.RelationshipList;
import org.onap.aaiclient.client.api.Relationships;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;

public class RelationshipsTest {


    @Test
    public void thatRelationshipsCanBeNull() throws IOException {
        Relationships relationships = new Relationships(null);
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
        Relationships relationships = new Relationships(relationshipList);

        assertTrue(relationships.hasRelationshipsTo(Types.VCE));
    }

    @Test
    public void verifyHasRelationshipsToFalse() throws IOException {
        RelationshipList relationshipList = new RelationshipList();
        Relationship relationshipToServiceInstance = new Relationship();
        relationshipToServiceInstance.setRelatedTo("service-instance");
        relationshipList.getRelationship().add(relationshipToServiceInstance);
        Relationships relationships = new Relationships(relationshipList);

        assertFalse(relationships.hasRelationshipsTo(Types.VCE));
    }
}
