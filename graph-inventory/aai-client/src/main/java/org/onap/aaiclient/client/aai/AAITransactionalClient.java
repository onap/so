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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import org.onap.aai.domain.yang.Relationship;
import org.onap.so.client.RestClient;
import org.onap.aaiclient.client.aai.entities.AAIEdgeLabel;
import org.onap.aaiclient.client.aai.entities.AAIError;
import org.onap.aaiclient.client.aai.entities.bulkprocess.OperationBody;
import org.onap.aaiclient.client.aai.entities.bulkprocess.Transaction;
import org.onap.aaiclient.client.aai.entities.bulkprocess.Transactions;
import org.onap.aaiclient.client.aai.entities.uri.AAIBaseResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIClientUriFactory;
import org.onap.aaiclient.client.graphinventory.GraphInventoryPatchConverter;
import org.onap.aaiclient.client.graphinventory.GraphInventoryTransactionClient;
import org.onap.aaiclient.client.graphinventory.exceptions.BulkProcessFailed;
import org.onap.so.jsonpath.JsonPathUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;

public class AAITransactionalClient extends
        GraphInventoryTransactionClient<AAITransactionalClient, AAIBaseResourceUri<?, ?>, AAIResourceUri, AAIEdgeLabel> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final Transactions transactions;
    private Transaction currentTransaction;

    private AAIResourcesClient resourcesClient;
    private AAIClient aaiClient;

    protected AAITransactionalClient(AAIResourcesClient resourcesClient, AAIClient aaiClient) {
        super();
        this.resourcesClient = resourcesClient;
        this.aaiClient = aaiClient;
        this.transactions = new Transactions();
        startTransaction();
    }

    private void startTransaction() {
        Transaction transaction = new Transaction();
        transactions.getTransactions().add(transaction);
        currentTransaction = transaction;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.onap.aaiclient.client.aai.GraphInventoryTransactionalClient#beginNewTransaction()
     */
    public AAITransactionalClient beginNewTransaction() {
        startTransaction();
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.onap.aaiclient.client.aai.GraphInventoryTransactionalClient#execute()
     */
    @Override
    public void execute() throws BulkProcessFailed {
        try {
            if (!this.transactions.getTransactions().isEmpty()) {
                RestClient client =
                        aaiClient.createClient(AAIClientUriFactory.createResourceUri(AAIObjectType.BULK_PROCESS));
                Response response = client.put(this.transactions);
                if (response.hasEntity()) {
                    final Optional<String> errorMessage = this.locateErrorMessages(response.readEntity(String.class));
                    if (errorMessage.isPresent()) {
                        throw new BulkProcessFailed(
                                "One or more transactions failed in A&AI. Check logs for payloads.\nMessages:\n"
                                        + errorMessage.get());
                    }
                } else {
                    throw new BulkProcessFailed(
                            "Transactions acccepted by A&AI, but there was no response. Unsure of result.");
                }
            }
        } finally {
            this.transactions.getTransactions().clear();
            this.currentTransaction = null;
            this.actionCount = 0;
        }
    }

    @Override
    public void execute(boolean dryRun) throws BulkProcessFailed {
        if (dryRun) {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Would execute: {}", mapper.writeValueAsString(this.transactions));
                }
            } catch (JsonProcessingException e) {
                logger.debug("Could not format request to JSON", e);
            }
        } else {
            this.execute();
        }
    }

    protected Optional<String> locateErrorMessages(String response) {
        final List<String> errorMessages = new ArrayList<>();
        final List<String> results = JsonPathUtil.getInstance().locateResultList(response, "$..body");
        if (!results.isEmpty()) {
            List<Map<String, Object>> parsed = new ArrayList<>();
            try {
                for (String result : results) {
                    parsed.add(mapper.readValue(result, new TypeReference<Map<String, Object>>() {}));
                }
            } catch (IOException e) {
                logger.error("could not map json", e);
            }
            for (Map<String, Object> map : parsed) {
                for (Entry<String, Object> entry : map.entrySet()) {
                    if (!entry.getKey().matches("2\\d\\d")) {
                        AAIError error;
                        try {
                            error = mapper.readValue(entry.getValue().toString(), AAIError.class);
                        } catch (IOException e) {
                            logger.error("could not parse error object from A&AI", e);
                            error = new AAIError();
                        }
                        AAIErrorFormatter formatter = new AAIErrorFormatter(error);
                        String outputMessage = formatter.getMessage();
                        logger.error("part of a bulk action failed in A&AI: " + entry.getValue());
                        errorMessages.add(outputMessage);
                    }
                }
            }
        }

        if (!errorMessages.isEmpty()) {
            return Optional.of(Joiner.on("\n").join(errorMessages));
        } else {
            return Optional.empty();
        }
    }

    private Relationship buildRelationship(AAIResourceUri uri) {
        return buildRelationship(uri, Optional.empty());
    }

    private Relationship buildRelationship(AAIResourceUri uri, AAIEdgeLabel label) {
        return buildRelationship(uri, Optional.of(label));
    }

    private Relationship buildRelationship(AAIResourceUri uri, Optional<AAIEdgeLabel> label) {
        final Relationship result = new Relationship();
        result.setRelatedLink(uri.build().toString());
        if (label.isPresent()) {
            result.setRelationshipLabel(label.toString());
        }
        return result;
    }

    protected Transactions getTransactions() {
        return this.transactions;
    }

    @Override
    protected void put(String uri, Object body) {
        currentTransaction.getPut().add(new OperationBody().withUri(uri).withBody(body));
    }

    @Override
    protected void delete(String uri) {
        currentTransaction.getDelete().add(new OperationBody().withUri(uri).withBody(null));
    }

    @Override
    protected void delete(String uri, Object obj) {
        currentTransaction.getDelete().add(new OperationBody().withUri(uri).withBody(obj));
    }

    @Override
    protected void patch(String uri, Object body) {
        currentTransaction.getPatch().add(new OperationBody().withUri(uri).withBody(body));
    }

    @Override
    protected <T> Optional<T> get(GenericType<T> genericType, AAIBaseResourceUri<?, ?> clone) {
        return resourcesClient.get(genericType, clone);
    }

    @Override
    protected boolean exists(AAIBaseResourceUri<?, ?> uri) {
        return resourcesClient.exists(uri);
    }

    @Override
    protected String getGraphDBName() {
        return aaiClient.getGraphDBName();
    }

    @Override
    protected GraphInventoryPatchConverter getPatchConverter() {
        return this.patchConverter;
    }
}
