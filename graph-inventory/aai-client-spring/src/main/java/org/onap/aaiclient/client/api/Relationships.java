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

package org.onap.aaiclient.client.api;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.ws.rs.core.UriBuilder;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.RelationshipList;
import org.onap.aaiclient.client.aai.AAIObjectType;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.graphinventory.GraphInventoryObjectName;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Relationships {
    private final RelationshipList relationshipList;
    String json;

    public boolean hasRelationshipsTo(GraphInventoryObjectName type) {
        if (relationshipList.getRelationship() == null) {
            return false;
        }
        return relationshipList.getRelationship().stream().map(Relationship::getRelatedTo).filter(Objects::nonNull)
                .anyMatch(relatedTo -> relatedTo.equals(type.typeName()));
    }

    public List<Relationship> getByType(GraphInventoryObjectName type) {
        return this.relationshipList.getRelationship().stream()
                .filter(relationship -> relationship.getRelatedTo().equals(type.typeName()))
                .collect(Collectors.toList());
    }

    private AAIResourceUri createUri(AAIObjectType type, String relatedLink) {
        return AAIUriFactory.createResourceFromExistingURI(type, UriBuilder.fromPath(relatedLink).build(new Object[0]));
    }

}
