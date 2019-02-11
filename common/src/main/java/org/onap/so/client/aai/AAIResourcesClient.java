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

package org.onap.so.client.aai;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.onap.aai.domain.yang.Relationship;
import org.onap.so.client.RestClient;
import org.onap.so.client.RestProperties;
import org.onap.so.client.aai.entities.AAIEdgeLabel;
import org.onap.so.client.aai.entities.AAIResultWrapper;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUri;
import org.onap.so.client.graphinventory.GraphInventoryResourcesClient;
import org.onap.so.client.graphinventory.entities.uri.Depth;

public class AAIResourcesClient extends AAIClient implements GraphInventoryResourcesClient<AAIResourcesClient, AAIResourceUri, AAIEdgeLabel, AAIResultWrapper, AAITransactionalClient, AAISingleTransactionClient> {
			
	public AAIResourcesClient() {
		super();
	}
	
	public AAIResourcesClient(AAIVersion version) {
		super();
		this.version = version;
	}
	
	/* (non-Javadoc)
	 * @see org.onap.so.client.aai.GraphInventoryResourcesClient#create(org.onap.so.client.aai.entities.uri.AAIResourceUri, java.lang.Object)
	 */
	@Override
	public void create(AAIResourceUri uri, Object obj) {
		RestClient aaiRC = this.createClient(uri);
		aaiRC.put(obj);
		return;
	}
	
	/* (non-Javadoc)
	 * @see org.onap.so.client.aai.GraphInventoryResourcesClient#createEmpty(org.onap.so.client.aai.entities.uri.AAIResourceUri)
	 */
	@Override
	public void createEmpty(AAIResourceUri uri) {
		RestClient aaiRC = this.createClient(uri);
		aaiRC.put("");
		return;
	}
	
	/* (non-Javadoc)
	 * @see org.onap.so.client.aai.GraphInventoryResourcesClient#exists(org.onap.so.client.aai.entities.uri.AAIResourceUri)
	 */
	@Override
	public boolean exists(AAIResourceUri uri) {
		AAIUri forceMinimal = this.addParams(Optional.of(Depth.ZERO), true, uri);
		try {
			RestClient aaiRC = this.createClient(forceMinimal);
			
			return aaiRC.get().getStatus() == Status.OK.getStatusCode();
		} catch (NotFoundException e) {
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.onap.so.client.aai.GraphInventoryResourcesClient#connect(org.onap.so.client.aai.entities.uri.AAIResourceUri, org.onap.so.client.aai.entities.uri.AAIResourceUri)
	 */
	@Override
	public void connect(AAIResourceUri uriA, AAIResourceUri uriB) {
		AAIResourceUri uriAClone = uriA.clone();
		RestClient aaiRC = this.createClient(uriAClone.relationshipAPI());
		aaiRC.put(this.buildRelationship(uriB));
		return;
	}
	
	/* (non-Javadoc)
	 * @see org.onap.so.client.aai.GraphInventoryResourcesClient#connect(org.onap.so.client.aai.entities.uri.AAIResourceUri, org.onap.so.client.aai.entities.uri.AAIResourceUri, org.onap.so.client.aai.entities.AAIEdgeLabel)
	 */
	@Override
	public void connect(AAIResourceUri uriA, AAIResourceUri uriB, AAIEdgeLabel label) {
		AAIResourceUri uriAClone = uriA.clone();
		RestClient aaiRC = this.createClient(uriAClone.relationshipAPI());
		aaiRC.put(this.buildRelationship(uriB, label));
		return;
	}
	
	/* (non-Javadoc)
	 * @see org.onap.so.client.aai.GraphInventoryResourcesClient#disconnect(org.onap.so.client.aai.entities.uri.AAIResourceUri, org.onap.so.client.aai.entities.uri.AAIResourceUri)
	 */
	@Override
	public void disconnect(AAIResourceUri uriA, AAIResourceUri uriB) {
		AAIResourceUri uriAClone = uriA.clone();
		RestClient aaiRC = this.createClient(uriAClone.relationshipAPI());
		aaiRC.delete(this.buildRelationship(uriB));
		return;
	}
	
	/* (non-Javadoc)
	 * @see org.onap.so.client.aai.GraphInventoryResourcesClient#delete(org.onap.so.client.aai.entities.uri.AAIResourceUri)
	 */
	@Override
	public void delete(AAIResourceUri uri) {
		AAIResourceUri clone = uri.clone();
		RestClient aaiRC = this.createClient(clone);
		Map<String, Object> result = aaiRC.get(new GenericType<Map<String, Object>>(){})
				.orElseThrow(() -> new NotFoundException(clone.build() + " does not exist in A&AI"));
		String resourceVersion = (String) result.get("resource-version");
		aaiRC = this.createClient(clone.resourceVersion(resourceVersion));
		aaiRC.delete();
		return;
	}
	
	/* (non-Javadoc)
	 * @see org.onap.so.client.aai.GraphInventoryResourcesClient#update(org.onap.so.client.aai.entities.uri.AAIResourceUri, java.lang.Object)
	 */
	@Override
	public void update(AAIResourceUri uri, Object obj) {
		RestClient aaiRC = this.createClient(uri);
		aaiRC.patch(obj);
		return;
	}
	
	/* (non-Javadoc)
	 * @see org.onap.so.client.aai.GraphInventoryResourcesClient#get(java.lang.Class, org.onap.so.client.aai.entities.uri.AAIResourceUri)
	 */
	@Override
	public <T> Optional<T> get(Class<T> clazz, AAIResourceUri uri) {
		try {
			return this.createClient(uri).get(clazz);
		} catch (NotFoundException e) {
			if (this.getRestProperties().mapNotFoundToEmpty()) {
				return Optional.empty();
			} else {
				throw e;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.onap.so.client.aai.GraphInventoryResourcesClient#getFullResponse(org.onap.so.client.aai.entities.uri.AAIResourceUri)
	 */
	@Override
	public Response getFullResponse(AAIResourceUri uri) {
		try {
			return this.createClient(uri).get();
		} catch (NotFoundException e) {
			if (this.getRestProperties().mapNotFoundToEmpty()) {
				return e.getResponse();
			} else {
				throw e;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.onap.so.client.aai.GraphInventoryResourcesClient#get(javax.ws.rs.core.GenericType, org.onap.so.client.aai.entities.uri.AAIResourceUri)
	 */
	@Override
	public <T> Optional<T> get(GenericType<T> resultClass, AAIResourceUri uri) {
		try {
			return this.createClient(uri).get(resultClass);
		} catch (NotFoundException e) {
			if (this.getRestProperties().mapNotFoundToEmpty()) {
				return Optional.empty();
			} else {
				throw e;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.onap.so.client.aai.GraphInventoryResourcesClient#get(org.onap.so.client.aai.entities.uri.AAIResourceUri)
	 */
	@Override
	public AAIResultWrapper get(AAIResourceUri uri) {
		String json;
		try {
			json = this.createClient(uri).get(String.class).orElse(null);
		} catch (NotFoundException e) {
			if (this.getRestProperties().mapNotFoundToEmpty()) {
				json = null;
			} else {
				throw e;
			}
		}
		return new AAIResultWrapper(json);
	}
	
	/* (non-Javadoc)
	 * @see org.onap.so.client.aai.GraphInventoryResourcesClient#get(org.onap.so.client.aai.entities.uri.AAIResourceUri, java.lang.Class)
	 */
	@Override
	public AAIResultWrapper get(AAIResourceUri uri, Class<? extends RuntimeException> c) {
		String json;
		try {
			json = this.createClient(uri).get(String.class)
					.orElseThrow(() -> createException(c, uri.build() + " not found in A&AI", Optional.empty()));
		} catch (NotFoundException e) {
			throw createException(c, "could not construct uri for use with A&AI", Optional.of(e));
		}

		return new AAIResultWrapper(json);
	}
	
	private RuntimeException createException(Class<? extends RuntimeException> c, String message, Optional<Throwable> t) {
		RuntimeException e;
		try {
			if (t.isPresent()) {
				e = c.getConstructor(String.class, Throwable.class).newInstance(message, t.get());
			} else {
				e = c.getConstructor(String.class).newInstance(message);
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e1) {
			throw new IllegalArgumentException("could not create instance for " + c.getName());
		}
		
		return e;
	}
	
	protected Relationship buildRelationship(AAIResourceUri uri) {
		return buildRelationship(uri, Optional.empty());
	}
	
	protected Relationship buildRelationship(AAIResourceUri uri, AAIEdgeLabel label) {
		return buildRelationship(uri, Optional.of(label));
	}
	protected Relationship buildRelationship(AAIResourceUri uri, Optional<AAIEdgeLabel> label) {
		final Relationship result = new Relationship();
		result.setRelatedLink(uri.build().toString());
		if (label.isPresent()) {
			result.setRelationshipLabel(label.get().toString());
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see org.onap.so.client.aai.GraphInventoryResourcesClient#createIfNotExists(org.onap.so.client.aai.entities.uri.AAIResourceUri, java.util.Optional)
	 */
	@Override
	public AAIResourcesClient createIfNotExists(AAIResourceUri uri, Optional<Object> obj) {
		if(!this.exists(uri)){
			if (obj.isPresent()) {
				this.create(uri, obj.get());
			} else {
				this.createEmpty(uri);
			}
			
		}
		return this;
	}

	/* (non-Javadoc)
	 * @see org.onap.so.client.aai.GraphInventoryResourcesClient#beginTransaction()
	 */
	@Override
	public AAITransactionalClient beginTransaction() {
		return new AAITransactionalClient(this.getVersion());
	}
	
	/* (non-Javadoc)
	 * @see org.onap.so.client.aai.GraphInventoryResourcesClient#beginSingleTransaction()
	 */
	@Override
	public AAISingleTransactionClient beginSingleTransaction() {
		return new AAISingleTransactionClient(this.getVersion());
	}
	
	private AAIUri addParams(Optional<Depth> depth, boolean nodesOnly, AAIUri uri) {
		AAIUri clone = uri.clone();
		if (depth.isPresent()) {
			clone.depth(depth.get());
		}
		if (nodesOnly) {
			clone.nodesOnly(nodesOnly);
		}
		
		return clone;
	}
	/* (non-Javadoc)
	 * @see org.onap.so.client.aai.GraphInventoryResourcesClient#getRestProperties()
	 */
	@Override
	public <T extends RestProperties> T getRestProperties() {
		return super.getRestProperties();
	}
}
