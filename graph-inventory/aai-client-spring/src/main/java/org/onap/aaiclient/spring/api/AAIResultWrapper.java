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
package org.onap.aaiclient.spring.api;

import javax.annotation.Nonnull;
import org.onap.aai.domain.yang.GraphNode;
import org.onap.aaiclient.client.graphinventory.GraphInventoryObjectName;
import org.onap.aaiclient.spring.AAIResourcesClient;
import lombok.Value;

@Value
public class AAIResultWrapper<T extends GraphNode> {

    private final T result;
    @Nonnull
    private Relationships relationships;

    public AAIResultWrapper(T result, AAIResourcesClient client) {
        this.result = result;
        this.relationships = new Relationships(result.getRelationshipList(), client);
    }

    public AAIResultWrapper(T result, Relationships relationships, AAIResourcesClient client) {
        this.result = result;
        this.relationships = relationships;
    }


    public boolean hasRelationshipsTo(GraphInventoryObjectName type) {
        return relationships.hasRelationshipsTo(type);
    }

}
