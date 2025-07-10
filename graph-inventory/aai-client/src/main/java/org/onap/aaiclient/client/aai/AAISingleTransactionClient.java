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

package org.onap.aaiclient.client.aai;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.GenericType;
import org.onap.so.client.RestClient;
import org.onap.aaiclient.client.aai.entities.AAIEdgeLabel;
import org.onap.aaiclient.client.aai.entities.AAIError;
import org.onap.aaiclient.client.aai.entities.singletransaction.OperationBodyRequest;
import org.onap.aaiclient.client.aai.entities.singletransaction.OperationBodyResponse;
import org.onap.aaiclient.client.aai.entities.singletransaction.SingleTransactionRequest;
import org.onap.aaiclient.client.aai.entities.singletransaction.SingleTransactionResponse;
import org.onap.aaiclient.client.aai.entities.uri.AAIBaseResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.graphinventory.GraphInventoryPatchConverter;
import org.onap.aaiclient.client.graphinventory.GraphInventoryTransactionClient;
import org.onap.aaiclient.client.graphinventory.exceptions.BulkProcessFailed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;

public class AAISingleTransactionClient extends
        GraphInventoryTransactionClient<AAISingleTransactionClient, AAIBaseResourceUri<?, ?>, AAIResourceUri, AAIEdgeLabel> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final SingleTransactionRequest request;
    private AAIResourcesClient resourcesClient;
    private AAIClient aaiClient;

    protected AAISingleTransactionClient(AAIResourcesClient resourcesClient, AAIClient aaiClient) {
        super();
        this.resourcesClient = resourcesClient;
        this.aaiClient = aaiClient;
        this.request = new SingleTransactionRequest();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.onap.aaiclient.client.aai.GraphInventoryTransactionClient#execute()
     */
    @Override
    public void execute() throws BulkProcessFailed {
        try {
            if (!this.request.getOperations().isEmpty()) {
                RestClient client =
                        aaiClient.createClient(AAIUriFactory.createResourceUri(AAIObjectType.SINGLE_TRANSACTION));
                SingleTransactionResponse response = client.post(this.request, SingleTransactionResponse.class);
                if (response != null) {
                    final Optional<String> errorMessage = this.locateErrorMessages(response);
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
            this.request.getOperations().clear();
            this.actionCount = 0;
        }
    }

    @Override
    public void execute(boolean dryRun) throws BulkProcessFailed {
        if (dryRun) {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Would execute: {}", mapper.writeValueAsString(this.request));
                }
            } catch (JsonProcessingException e) {
                logger.debug("Could not format request to JSON", e);
            }
        } else {
            this.execute();
        }
    }

    protected Optional<String> locateErrorMessages(SingleTransactionResponse response) {
        final List<String> errorMessages = new ArrayList<>();

        for (OperationBodyResponse body : response.getOperationResponses()) {
            if (Optional.ofNullable(body.getResponseStatusCode()).orElse(400) > 300) {
                AAIError error;
                try {
                    error = mapper.readValue(mapper.writeValueAsString(body.getResponseBody()), AAIError.class);
                } catch (IOException e) {
                    logger.error("could not parse error object from A&AI", e);
                    error = new AAIError();
                }
                AAIErrorFormatter formatter = new AAIErrorFormatter(error);
                String outputMessage = formatter.getMessage();
                errorMessages.add(outputMessage);
            }
        }

        if (!errorMessages.isEmpty()) {
            return Optional.of(Joiner.on("\n").join(errorMessages));
        } else {
            return Optional.empty();
        }
    }

    protected SingleTransactionRequest getRequest() {
        return this.request;
    }

    @Override
    protected void put(String uri, Object body) {
        request.getOperations().add(new OperationBodyRequest().withAction("put").withUri(uri).withBody(body));
    }

    @Override
    protected void delete(String uri) {
        request.getOperations()
                .add(new OperationBodyRequest().withAction("delete").withUri(uri).withBody(new Object()));
    }

    @Override
    protected void delete(String uri, Object obj) {
        request.getOperations().add(new OperationBodyRequest().withAction("delete").withUri(uri).withBody(obj));
    }

    @Override
    protected void patch(String uri, Object body) {
        request.getOperations().add(new OperationBodyRequest().withAction("patch").withUri(uri).withBody(body));
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
