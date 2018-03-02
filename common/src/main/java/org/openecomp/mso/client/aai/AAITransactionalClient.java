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

package org.openecomp.mso.client.aai;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.onap.aai.domain.yang.Relationship;
import org.openecomp.mso.client.aai.entities.AAIError;
import org.openecomp.mso.client.aai.entities.bulkprocess.OperationBody;
import org.openecomp.mso.client.aai.entities.bulkprocess.Transaction;
import org.openecomp.mso.client.aai.entities.bulkprocess.Transactions;
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri;
import org.openecomp.mso.client.aai.entities.uri.AAIUri;
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory;
import org.openecomp.mso.client.aai.exceptions.BulkProcessFailed;
import org.openecomp.mso.client.policy.RestClient;
import org.openecomp.mso.jsonpath.JsonPathUtil;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;

public class AAITransactionalClient extends AAIClient {

	private final Transactions transactions;
	private Transaction currentTransaction;
	private final AAIVersion version;
	private int actionCount = 0;
	protected AAITransactionalClient(AAIVersion version, UUID requestId) {
		super(requestId);
		this.version = version;
		this.transactions = new Transactions();
		startTransaction();
	}
	
	private void startTransaction() {
		Transaction transaction = new Transaction();
		transactions.getTransactions().add(transaction);
		currentTransaction = transaction;
	}
	
	/**
	 * adds an additional transaction and closes the previous transaction
	 * 
	 * @return AAITransactionalClient
	 */
	public AAITransactionalClient beginNewTransaction() {
		startTransaction();
		return this;
	}
	
	/**
	 * creates a new object in A&AI
	 * 
	 * @param obj - can be any object which will marshal into a valid A&AI payload
	 * @param uri
	 * @return
	 */
	public AAITransactionalClient create(AAIResourceUri uri, Object obj) {
		currentTransaction.getPut().add(new OperationBody().withUri(uri.build().toString()).withBody(obj));
		incrementActionAmount();
		return this;
	}
	
	/**
	 * creates a new object in A&AI with no payload body
	 * 
	 * @param uri
	 * @return
	 */
	public AAITransactionalClient createEmpty(AAIResourceUri uri) {
		currentTransaction.getPut().add(new OperationBody().withUri(uri.build().toString()).withBody(new HashMap<String, String>()));
		incrementActionAmount();
		return this;
	}
	
	/**
	 * Adds a relationship between two objects in A&AI 
	 * @param uriA
	 * @param uriB
	 * @return
	 */
	public AAITransactionalClient connect(AAIResourceUri uriA, AAIResourceUri uriB) {
		AAIResourceUri uriAClone = uriA.clone();
		currentTransaction.getPut().add(new OperationBody().withUri(uriAClone.relationshipAPI().build().toString()).withBody(this.buildRelationship(uriB)));
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
	public AAITransactionalClient connect(AAIResourceUri uriA, List<AAIResourceUri> uris) {
		for (AAIResourceUri uri : uris) {
			this.connect(uriA, uri);
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
	public AAITransactionalClient disconnect(AAIResourceUri uriA, AAIResourceUri uriB) {
		AAIResourceUri uriAClone = uriA.clone();
		currentTransaction.getDelete().add(new OperationBody().withUri(uriAClone.relationshipAPI().build().toString()).withBody(this.buildRelationship(uriB)));
		incrementActionAmount();
		return this;
	}
	
	/**
	 * Removes relationship from multiple objects - disconnects A from all objects specified in list
	 * @param uriA
	 * @param uris
	 * @return
	 */
	public AAITransactionalClient disconnect(AAIResourceUri uriA, List<AAIResourceUri> uris) {
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
	public AAITransactionalClient delete(AAIResourceUri uri) {
		AAIResourcesClient client = new AAIResourcesClient();
		AAIResourceUri clone = uri.clone();
		Map<String, Object> result = client.get(new GenericType<Map<String, Object>>(){}, clone);
		String resourceVersion = (String) result.get("resource-version");
		currentTransaction.getDelete().add(new OperationBody().withUri(clone.resourceVersion(resourceVersion).build().toString()).withBody(""));
		incrementActionAmount();
		return this;
	}
	
	/**
	 * @param obj - can be any object which will marshal into a valid A&AI payload
	 * @param uri
	 * @return
	 */
	public AAITransactionalClient update(AAIResourceUri uri, Object obj) {
		currentTransaction.getPatch().add(new OperationBody().withUri(uri.build().toString()).withBody(obj));
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
		RestClient client = this.createClient(AAIUriFactory.createResourceUri(AAIObjectType.BULK_PROCESS));
		try {
			Response response = client.put(this.transactions);
			if (response.hasEntity()) {
				final Optional<String> errorMessage = this.locateErrorMessages(response.readEntity(String.class));
				if (errorMessage.isPresent()) {
					throw new BulkProcessFailed("One or more transactions failed in A&AI. Request-id=" + this.getRequestId() + ". Check logs for payloads.\nMessages:\n" + errorMessage.get());
				}
			} else {
				throw new BulkProcessFailed("Transactions acccepted by A&AI, but there was no response. Unsure of result.");
			}
		} finally {
			this.transactions.getTransactions().clear();
			this.currentTransaction = null;
			this.actionCount = 0;
		}
	}
	
	protected Optional<String> locateErrorMessages(String response) {
		final List<String> errorMessages = new ArrayList<>();
		final List<String> results = JsonPathUtil.getInstance().locateResultList(response, "$..body");
		final ObjectMapper mapper = new ObjectMapper();
		if (!results.isEmpty()) {
			List<Map<String, Object>> parsed = new ArrayList<>();
			try {
				for (String result : results) {
					parsed.add(mapper.readValue(result, new TypeReference<Map<String, Object>>(){}));
				}
			} catch (IOException e) {
				metricsLogger.error("could not map json", e);
			}
			for (Map<String, Object> map : parsed) {
				for (Entry<String, Object> entry : map.entrySet()) {
					if (!entry.getKey().matches("2\\d\\d")) {
						AAIError error;
						try {
							error = mapper.readValue(entry.getValue().toString(), AAIError.class);
						} catch (IOException e) {
							metricsLogger.error("could not parse error object from A&AI", e);
							error = new AAIError();
						}
						AAIErrorFormatter formatter = new AAIErrorFormatter(error);
						String outputMessage = formatter.getMessage();
						metricsLogger.error("part of a bulk action failed in A&AI: " + entry.getValue());
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
	private Relationship buildRelationship(AAIUri uri) {
		final Relationship result = new Relationship();
		result.setRelatedLink(uri.build().toString());
		return result;
	}

	@Override
	protected AAIVersion getVersion() {
		return this.version;
	}
	
	protected Transactions getTransactions() {
		return this.transactions;
	}
}
