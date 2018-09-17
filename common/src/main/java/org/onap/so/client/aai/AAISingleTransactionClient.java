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

package org.onap.so.client.aai;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.GenericType;

import org.onap.aai.domain.yang.Relationship;
import org.onap.so.client.RestClient;
import org.onap.so.client.aai.entities.AAIEdgeLabel;
import org.onap.so.client.aai.entities.AAIError;
import org.onap.so.client.aai.entities.bulkprocess.Transactions;
import org.onap.so.client.aai.entities.singletransaction.OperationBodyRequest;
import org.onap.so.client.aai.entities.singletransaction.OperationBodyResponse;
import org.onap.so.client.aai.entities.singletransaction.SingleTransactionRequest;
import org.onap.so.client.aai.entities.singletransaction.SingleTransactionResponse;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.graphinventory.exceptions.BulkProcessFailed;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;

public class AAISingleTransactionClient extends AAIClient {

	private final SingleTransactionRequest request;
	private final AAIVersion version;
	private int actionCount = 0;
	
	private final AAIPatchConverter patchConverter = new AAIPatchConverter();
	
	protected AAISingleTransactionClient(AAIVersion version) {
		super();
		this.version = version;
		this.request = new SingleTransactionRequest();
	}

	/**
	 * creates a new object in A&AI
	 * 
	 * @param obj - can be any object which will marshal into a valid A&AI payload
	 * @param uri
	 * @return
	 */
	public AAISingleTransactionClient create(AAIResourceUri uri, Object obj) {
		request.getOperations().add(new OperationBodyRequest().withAction("put").withUri(uri.build().toString()).withBody(obj));
		incrementActionAmount();
		return this;
	}

	/**
	 * creates a new object in A&AI with no payload body
	 * 
	 * @param uri
	 * @return
	 */
	public AAISingleTransactionClient createEmpty(AAIResourceUri uri) {
		request.getOperations().add(new OperationBodyRequest().withAction("put").withUri(uri.build().toString()).withBody(new HashMap<String, String>()));
		incrementActionAmount();
		return this;
	}

	/**
	 * Adds a relationship between two objects in A&AI 
	 * @param uriA
	 * @param uriB
	 * @return
	 */
	public AAISingleTransactionClient connect(AAIResourceUri uriA, AAIResourceUri uriB) {
		AAIResourceUri uriAClone = uriA.clone();
		request.getOperations().add(new OperationBodyRequest().withAction("put").withUri(uriAClone.relationshipAPI().build().toString()).withBody(this.buildRelationship(uriB)));
		incrementActionAmount();
		return this;
	}

	/**
	 * relationship between multiple objects in A&AI - connects A to all objects specified in list
	 * 
	 * @param uriA
	 * @param uris
	 * @return
	 */
	public AAISingleTransactionClient connect(AAIResourceUri uriA, List<AAIResourceUri> uris) {
		for (AAIResourceUri uri : uris) {
			this.connect(uriA, uri);
		}
		return this;
	}
	
	public AAISingleTransactionClient connect(AAIResourceUri uriA, AAIResourceUri uriB, AAIEdgeLabel label) {
		AAIResourceUri uriAClone = uriA.clone();
		RestClient aaiRC = this.createClient(uriAClone.relationshipAPI());
		aaiRC.put(this.buildRelationship(uriB, label));
		return this;
	}
	
	public AAISingleTransactionClient connect(AAIResourceUri uriA, List<AAIResourceUri> uris, AAIEdgeLabel label) {
		for (AAIResourceUri uri : uris) {
			this.connect(uriA, uri, label);
		}
		return this;
	}

	/**
	 * Removes relationship from two objects in A&AI
	 * 
	 * @param uriA
	 * @param uriB
	 * @return
	 */
	public AAISingleTransactionClient disconnect(AAIResourceUri uriA, AAIResourceUri uriB) {
		AAIResourceUri uriAClone = uriA.clone();
		request.getOperations().add(new OperationBodyRequest().withAction("delete").withUri(uriAClone.relationshipAPI().build().toString()).withBody(this.buildRelationship(uriB)));
		incrementActionAmount();
		return this;
	}

	/**
	 * Removes relationship from multiple objects - disconnects A from all objects specified in list
	 * @param uriA
	 * @param uris
	 * @return
	 */
	public AAISingleTransactionClient disconnect(AAIResourceUri uriA, List<AAIResourceUri> uris) {
		for (AAIResourceUri uri : uris) {
			this.disconnect(uriA, uri);
		}
		return this;
	}
	/**
	 * Deletes object from A&AI. Automatically handles resource-version.
	 * 
	 * @param uri
	 * @return
	 */
	public AAISingleTransactionClient delete(AAIResourceUri uri) {
		AAIResourcesClient client = new AAIResourcesClient();
		AAIResourceUri clone = uri.clone();
		Map<String, Object> result = client.get(new GenericType<Map<String, Object>>(){}, clone)
				.orElseThrow(() -> new NotFoundException(clone.build() + " does not exist in A&AI"));
		String resourceVersion = (String) result.get("resource-version");
		request.getOperations().add(new OperationBodyRequest().withAction("delete").withUri(clone.resourceVersion(resourceVersion).build().toString()).withBody(""));
		incrementActionAmount();
		return this;
	}

	/**
	 * @param obj - can be any object which will marshal into a valid A&AI payload
	 * @param uri
	 * @return
	 */
	public AAISingleTransactionClient update(AAIResourceUri uri, Object obj) {
		
		final String payload = getPatchConverter().convertPatchFormat(obj);
		request.getOperations().add(new OperationBodyRequest().withAction("patch").withUri(uri.build().toString()).withBody(payload));
		incrementActionAmount();
		return this;
	}

	private void incrementActionAmount() {
		actionCount++;
	}
	/**
	 * Executes all created transactions in A&AI
	 * @throws BulkProcessFailed 
	 */
	public void execute() throws BulkProcessFailed {
		RestClient client = this.createClient(AAIUriFactory.createResourceUri(AAIObjectType.SINGLE_TRANSACTION));
		try {
			SingleTransactionResponse response = client.post(this.request, SingleTransactionResponse.class);
			if (response != null) {
				final Optional<String> errorMessage = this.locateErrorMessages(response);
				if (errorMessage.isPresent()) {
					throw new BulkProcessFailed("One or more transactions failed in A&AI. Check logs for payloads.\nMessages:\n" + errorMessage.get());
				}
			} else {
				throw new BulkProcessFailed("Transactions acccepted by A&AI, but there was no response. Unsure of result.");
			}
		} finally {
			this.request.getOperations().clear();
			this.actionCount = 0;
		}
	}

	protected Optional<String> locateErrorMessages(SingleTransactionResponse response) {
		final List<String> errorMessages = new ArrayList<>();
		final ObjectMapper mapper = new ObjectMapper();
		
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

	@Override
	protected AAIVersion getVersion() {
		return this.version;
	}
	
	protected SingleTransactionRequest getRequest() {
		return this.request;
	}
	
	protected AAIPatchConverter getPatchConverter() {
		return this.patchConverter;
	}
}
