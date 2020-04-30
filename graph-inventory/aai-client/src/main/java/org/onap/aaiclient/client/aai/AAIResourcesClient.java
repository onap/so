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

package org.onap.aaiclient.client.aai;

import java.util.Optional;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aaiclient.client.aai.entities.AAIEdgeLabel;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.uri.AAIBaseResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.graphinventory.GraphInventoryResourcesClient;
import org.onap.aaiclient.client.graphinventory.entities.GraphInventoryEdgeLabel;

public class AAIResourcesClient extends
        GraphInventoryResourcesClient<AAIResourcesClient, AAIBaseResourceUri<?, ?>, AAIResourceUri, AAIPluralResourceUri, AAIEdgeLabel, AAIResultWrapper, AAITransactionalClient, AAISingleTransactionClient> {

    private AAIClient aaiClient;

    public AAIResourcesClient() {
        super(new AAIClient());
        aaiClient = (AAIClient) super.client;
    }

    public AAIResourcesClient(AAIVersion version) {
        super(new AAIClient(version));
        aaiClient = (AAIClient) super.client;
    }

    public AAIResourcesClient(AAIClient client) {
        super(client);
        aaiClient = (AAIClient) super.client;
    }

    @Override
    public AAIResultWrapper createWrapper(String json) {
        return new AAIResultWrapper(json);
    }

    @Override
    public AAIResultWrapper createWrapper(Object obj) {
        return new AAIResultWrapper(obj);
    }

    @Override
    public AAITransactionalClient beginTransaction() {
        return new AAITransactionalClient(this, aaiClient);
    }

    @Override
    public AAISingleTransactionClient beginSingleTransaction() {
        return new AAISingleTransactionClient(this, aaiClient);
    }

    @Override
    protected Relationship buildRelationship(AAIResourceUri uri) {
        return super.buildRelationship(uri, Optional.empty());
    }

    @Override
    protected Relationship buildRelationship(AAIResourceUri uri, GraphInventoryEdgeLabel label) {
        return super.buildRelationship(uri, Optional.of(label));
    }

    @Override
    protected Relationship buildRelationship(AAIResourceUri uri, Optional<GraphInventoryEdgeLabel> label) {
        return super.buildRelationship(uri, label);
    }

}
