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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.onap.aai.domain.yang.GraphNode;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.RelationshipList;
import org.onap.aaiclient.client.AAIResourcesClient;
import org.onap.aaiclient.client.aai.AAIObjectType;
import org.onap.aaiclient.client.aai.caseformat.ClassNameMapper;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.graphinventory.GraphInventoryObjectName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class Relationships {

    @Nullable
    private final RelationshipList relationshipList;
    String json;

    public boolean hasRelationshipsTo(GraphInventoryObjectName type) {
        if (relationshipList == null || relationshipList.getRelationship() == null) {
            return false;
        }
        return relationshipList.getRelationship().stream().map(Relationship::getRelatedTo).filter(Objects::nonNull)
                .anyMatch(relatedTo -> relatedTo.equals(type.typeName()));
    }

    public List<Relationship> getByType(GraphInventoryObjectName type) {
        List<String> uris = this.relationshipList.getRelationship().stream()
                .filter(relationship -> type.typeName().equals(relationship.getRelatedTo()))
                .map(Relationship::getRelatedLink).collect(Collectors.toList());
        AAIResourcesClient client = new AAIResourcesClient(null);
        // client
        // client.
        if (relationshipList == null || relationshipList.getRelationship() == null) {
            return Collections.emptyList();
        }
        return this.relationshipList.getRelationship().stream()
                .filter(relationship -> type.typeName().equals(relationship.getRelatedTo()))
                .collect(Collectors.toList());
    }

    public <T extends GraphNode> List<T> getByType(Class<T> type) {
        List<URI> uris = this.relationshipList.getRelationship().stream()
                .filter(relationship -> isRelationshipOfType(relationship, type)).map(Relationship::getRelatedLink)
                .map(uri -> {
                    try {
                        return new URI(uri);
                    } catch (URISyntaxException e) {
                        log.warn("Related link '{}' in relationship is not a valid uri. Skipping.", uri);
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());
        List<AAIResourceUri> resourceUris = uris.stream()
                .map(uri -> AAIUriFactory.createResourceUri(AAIObjectType.UNKNOWN, uri)).collect(Collectors.toList());
        AAIResourcesClient client = new AAIResourcesClient(null);

        if (relationshipList == null || relationshipList.getRelationship() == null) {
            return Collections.emptyList();
        }
        // client
        client.get(resourceUris.get(0), type);

        return Collections.emptyList(); // TODO: Use the Bulk API here to fetch the resources
                                        // and return them as list
    }

    private <T> boolean isRelationshipOfType(Relationship relationship, Class<T> type) {
        String lowerHyphen = ClassNameMapper.getInstance().toLowerHyphen(type);
        if (lowerHyphen != null) {
            return lowerHyphen.equals(relationship.getRelatedTo());
        } else {
            log.warn("Unknown entity '{}' in relationship.", relationship.getRelatedTo());
            return false;
        }
    }

}
