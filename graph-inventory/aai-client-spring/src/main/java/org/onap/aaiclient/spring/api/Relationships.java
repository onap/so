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

package org.onap.aaiclient.spring.api;

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
import org.onap.aaiclient.client.aai.AAIObjectType;
import org.onap.aaiclient.client.aai.caseformat.ClassNameMapper;
import org.onap.aaiclient.client.aai.entities.uri.AAIFluentTypeReverseLookup;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.graphinventory.GraphInventoryObjectName;
import org.onap.aaiclient.spring.AAIResourcesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class Relationships {

    @Nullable
    private final RelationshipList relationshipList;
    private final AAIResourcesClient client;
    private final AAIFluentTypeReverseLookup lookup = new AAIFluentTypeReverseLookup();
    String json;

    public boolean hasRelationshipsTo(GraphInventoryObjectName type) {
        if (relationshipList == null || relationshipList.getRelationship() == null) {
            return false;
        }
        return relationshipList.getRelationship().stream().map(Relationship::getRelatedTo).filter(Objects::nonNull)
                .anyMatch(relatedTo -> relatedTo.equals(type.typeName()));
    }

    public <T extends GraphNode> List<T> getByType(Class<T> type) {
        if (relationshipList == null || relationshipList.getRelationship() == null) {
            return Collections.emptyList();
        }
        final String name = ClassNameMapper.getInstance().toLowerHyphen(type);

        List<URI> uris = this.relationshipList.getRelationship().stream()
                .filter(relationship -> isRelationshipOfType(relationship, type)).map(Relationship::getRelatedLink)
                .map(uri -> {
                    try {
                        return new URI(uri);
                    } catch (URISyntaxException e) {
                        log.warn("Related link '{}' in relationship is not a valid uri. Skipping.", uri, e);
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());
        List<AAIResourceUri> resourceUris = uris.stream().map(uri -> {
            AAIObjectType objectType = lookup.fromName(name, uri.toString());
            return AAIUriFactory.createResourceFromExistingURI(objectType, uri);
        }).collect(Collectors.toList());

        // TODO: Use the Bulk API here once it is available
        return resourceUris.stream().map(uri -> client.get(uri, type)).map(AAIResultWrapper::getResult)
                .collect(Collectors.toList());
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
