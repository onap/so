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

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.GenericType;

import org.onap.aai.domain.yang.Relationship;
import org.openecomp.mso.client.aai.entities.AAIResultWrapper;
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri;
import org.openecomp.mso.client.aai.entities.uri.AAIUri;
import org.openecomp.mso.client.aai.entities.uri.Depth;
import org.openecomp.mso.client.policy.RestClient;

public class AAIResourcesClient extends AAIClient {
	
	private final AAIVersion version;
		
	public AAIResourcesClient() {
		super(UUID.randomUUID());
		this.version = super.getVersion();
	}
	
	public AAIResourcesClient(AAIVersion version) {
		super(UUID.randomUUID());
		this.version = version;
	}
	
	public AAIResourcesClient(AAIVersion version, UUID requestId) {
		super(requestId);
		this.version = version;
	}
	
	/**
	 * creates a new object in A&AI
	 * 
	 * @param obj - can be any object which will marshal into a valid A&AI payload
	 * @param uri
	 * @return
	 */
	public void create(AAIResourceUri uri, Object obj) {
		RestClient aaiRC = this.createClient(uri);
		aaiRC.put(obj);
		return;
	}
	
	/**
	 * creates a new object in A&AI with no payload body
	 * 
	 * @param uri
	 * @return
	 */
	public void createEmpty(AAIResourceUri uri) {
		RestClient aaiRC = this.createClient(uri);
		aaiRC.put("");
		return;
	}
	
	/**
	 * returns false if the object does not exist in A&AI
	 * 
	 * @param uri
	 * @return
	 */
	public boolean exists(AAIResourceUri uri) {
		AAIUri forceMinimal = this.addParams(Optional.of(Depth.ZERO), true, uri);
		RestClient aaiRC = this.createClient(forceMinimal);
		try{
			aaiRC.get();	
		} catch(ResponseProcessingException e) {
			if (e.getCause() instanceof NotFoundException) {
				return false;
			} else {
				throw e;
			}
		}
		return true;
	}
	
	/**
	 * Adds a relationship between two objects in A&AI 
	 * @param uriA
	 * @param uriB
	 * @return
	 */
	public void connect(AAIResourceUri uriA, AAIResourceUri uriB) {
		AAIResourceUri uriAClone = uriA.clone();
		RestClient aaiRC = this.createClient(uriAClone.relationshipAPI());
		aaiRC.put(this.buildRelationship(uriB));
		return;
	}
	
	/**
	 * Removes relationship from two objects in A&AI
	 * 
	 * @param uriA
	 * @param uriB
	 * @return
	 */
	public void disconnect(AAIResourceUri uriA, AAIResourceUri uriB) {
		AAIResourceUri uriAClone = uriA.clone();
		RestClient aaiRC = this.createClient(uriAClone.relationshipAPI());
		aaiRC.delete(this.buildRelationship(uriB));
		return;
	}
	
	/**
	 * Deletes object from A&AI. Automatically handles resource-version.
	 * 
	 * @param uri
	 * @return
	 */
	public void delete(AAIResourceUri uri) {
		AAIResourceUri clone = uri.clone();
		RestClient aaiRC = this.createClient(clone);
		Map<String, Object> result = aaiRC.get(new GenericType<Map<String, Object>>(){});
		String resourceVersion = (String) result.get("resource-version");
		aaiRC = this.createClient(clone.resourceVersion(resourceVersion));
		aaiRC.delete();
		return;
	}
	
	/**
	 * @param obj - can be any object which will marshal into a valid A&AI payload
	 * @param uri
	 * @return
	 */
	public void update(AAIResourceUri uri, Object obj) {
		RestClient aaiRC = this.createClient(uri);
		aaiRC.patch(obj);
		return;
	}
	
	/**
	 * Retrieves an object from A&AI and unmarshalls it into the Class specified
	 * @param clazz
	 * @param uri
	 * @return
	 */
	public <T> T get(Class<T> clazz, AAIResourceUri uri) {
		return this.createClient(uri).get(clazz);
	}
	
	/**
	 * Retrieves an object from A&AI and automatically unmarshalls it into a Map or List 
	 * @param resultClass
	 * @param uri
	 * @return
	 */
	public <T> T get(GenericType<T> resultClass, AAIResourceUri uri) {
		return this.createClient(uri).get(resultClass);
	}
	
	/**
	 * Retrieves an object from A&AI wrapped in a helper class which offer additional features
	 * 
	 * @param uri
	 * @return
	 */
	public AAIResultWrapper get(AAIResourceUri uri) {
		String json = this.createClient(uri).get(String.class);
		
		return new AAIResultWrapper(json);

	}
	private Relationship buildRelationship(AAIResourceUri uri) {
		final Relationship result = new Relationship();
		result.setRelatedLink(uri.build().toString());
		return result;
	}
	
	/**
	 * Will automatically create the object if it does not exist
	 * 
	 * @param obj - Optional object which serializes to a valid A&AI payload
	 * @param uri
	 * @return
	 */
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

	/**
	 * Starts a transaction which encloses multiple A&AI mutations
	 * 
	 * @return
	 */
	public AAITransactionalClient beginTransaction() {
		return new AAITransactionalClient(this.version, this.requestId);
	}

	@Override
	protected AAIVersion getVersion() {
		return this.version;
	}
	
	@Override
	protected RestClient createClient(AAIUri uri) {
		return super.createClient(uri);
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
}
