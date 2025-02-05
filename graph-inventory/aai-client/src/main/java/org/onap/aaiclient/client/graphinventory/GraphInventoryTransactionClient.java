/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.aaiclient.client.graphinventory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.GenericType;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aaiclient.client.graphinventory.entities.GraphInventoryEdgeLabel;
import org.onap.aaiclient.client.graphinventory.entities.uri.GraphInventoryResourceUri;
import org.onap.aaiclient.client.graphinventory.entities.uri.GraphInventorySingleResourceUri;
import org.onap.aaiclient.client.graphinventory.exceptions.BulkProcessFailed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GraphInventoryTransactionClient<Self, Uri extends GraphInventoryResourceUri<?, ?>, SingleUri extends GraphInventorySingleResourceUri<?, ?, ?, ?, ?, ?>, EdgeLabel extends GraphInventoryEdgeLabel> {

    protected static Logger logger = LoggerFactory.getLogger(GraphInventoryTransactionClient.class);

    protected int actionCount = 0;

    protected final GraphInventoryPatchConverter patchConverter = new GraphInventoryPatchConverter();

    protected GraphInventoryTransactionClient() {}

    /**
     * creates a new object in A&AI
     * 
     * @param obj - can be any object which will marshal into a valid A&AI payload
     * @param uri
     * @return
     */
    public Self create(SingleUri uri, Object obj) {
        this.put(uri.build().toString(), obj);
        incrementActionAmount();
        return (Self) this;
    }

    /**
     * creates a new object in A&AI with no payload body
     * 
     * @param uri
     * @return
     */
    public Self createEmpty(SingleUri uri) {
        this.put(uri.build().toString(), new HashMap<String, String>());
        incrementActionAmount();
        return (Self) this;
    }

    /**
     * Will automatically create the object if it does not exist
     * 
     * @param obj - Optional object which serializes to a valid GraphInventory payload
     * @param uri
     * @return
     */
    public Self createIfNotExists(SingleUri uri, Optional<Object> obj) {
        if (!this.exists((Uri) uri)) {
            if (obj.isPresent()) {
                this.create(uri, obj.get());
            } else {
                this.createEmpty(uri);
            }

        }
        return (Self) this;
    }

    /**
     * Adds a relationship between two objects in A&AI
     * 
     * @param uriA
     * @param uriB
     * @return
     */
    public Self connect(SingleUri uriA, SingleUri uriB) {
        GraphInventorySingleResourceUri uriAClone = uriA.clone();
        this.put(uriAClone.relationshipAPI().build().toString(), this.buildRelationship(uriB));
        incrementActionAmount();
        return (Self) this;
    }

    /**
     * relationship between multiple objects in A&AI - connects A to all objects specified in list
     * 
     * @param uriA
     * @param uris
     * @return
     */
    public Self connect(SingleUri uriA, List<SingleUri> uris) {
        for (SingleUri uri : uris) {
            this.connect(uriA, uri);
        }
        return (Self) this;
    }

    /**
     * relationship between multiple objects in A&AI - connects A to all objects specified in list
     * 
     * @param uriA
     * @param uris
     * @return
     */
    public Self connect(SingleUri uriA, SingleUri uriB, EdgeLabel label) {
        GraphInventorySingleResourceUri uriAClone = uriA.clone();
        this.put(uriAClone.relationshipAPI().build().toString(), this.buildRelationship(uriB, label));
        return (Self) this;
    }

    /**
     * relationship between multiple objects in A&AI - connects A to all objects specified in list
     * 
     * @param uriA
     * @param uris
     * @return
     */
    public Self connect(SingleUri uriA, List<SingleUri> uris, EdgeLabel label) {
        for (SingleUri uri : uris) {
            this.connect(uriA, uri, label);
        }
        return (Self) this;
    }

    /**
     * Removes relationship from two objects in A&AI
     * 
     * @param uriA
     * @param uriB
     * @return
     */
    public Self disconnect(SingleUri uriA, SingleUri uriB) {
        GraphInventorySingleResourceUri uriAClone = uriA.clone();
        this.delete(uriAClone.relationshipAPI().build().toString(), this.buildRelationship(uriB));
        incrementActionAmount();
        return (Self) this;
    }

    /**
     * Removes relationship from multiple objects - disconnects A from all objects specified in list
     * 
     * @param uriA
     * @param uris
     * @return
     */
    public Self disconnect(SingleUri uriA, List<SingleUri> uris) {
        for (SingleUri uri : uris) {
            this.disconnect(uriA, uri);
        }
        return (Self) this;
    }

    /**
     * Deletes object from A&AI. Automatically handles resource-version.
     * 
     * @param uri
     * @return
     */
    public Self delete(SingleUri uri) {
        Map<String, Object> result = this.get(new GenericType<Map<String, Object>>() {}, (Uri) uri)
                .orElseThrow(() -> new NotFoundException(uri.build() + " does not exist in " + this.getGraphDBName()));
        String resourceVersion = (String) result.get("resource-version");
        this.delete(uri.resourceVersion(resourceVersion).build().toString());
        incrementActionAmount();
        return (Self) this;
    }

    protected abstract <T> Optional<T> get(GenericType<T> genericType, Uri clone);

    protected abstract boolean exists(Uri uri);

    protected abstract String getGraphDBName();

    protected abstract void put(String uri, Object body);

    protected abstract void delete(String uri);

    protected abstract void delete(String uri, Object obj);

    protected abstract void patch(String uri, Object body);

    /**
     * @param obj - can be any object which will marshal into a valid A&AI payload
     * @param uri
     * @return
     */
    public Self update(Uri uri, Object obj) {

        final String payload = getPatchConverter().convertPatchFormat(obj);
        this.patch(uri.build().toString(), payload);
        incrementActionAmount();
        return (Self) this;
    }

    private void incrementActionAmount() {
        actionCount++;
    }

    /**
     * Executes all created transactions in A&AI
     * 
     * @throws BulkProcessFailed
     */
    public abstract void execute() throws BulkProcessFailed;


    /**
     * Executes all created transactions in A&AI, with optional dry run flag
     * 
     * @throws BulkProcessFailed
     */
    public abstract void execute(boolean dryrun) throws BulkProcessFailed;

    private Relationship buildRelationship(SingleUri uri) {
        return buildRelationship(uri, Optional.empty());
    }

    private Relationship buildRelationship(SingleUri uri, EdgeLabel label) {
        return buildRelationship(uri, Optional.of(label));
    }

    private Relationship buildRelationship(SingleUri uri, Optional<EdgeLabel> label) {
        final Relationship result = new Relationship();
        result.setRelatedLink(uri.build().toString());
        if (label.isPresent()) {
            result.setRelationshipLabel(label.toString());
        }
        return result;
    }

    protected GraphInventoryPatchConverter getPatchConverter() {
        return this.patchConverter;
    }

}
