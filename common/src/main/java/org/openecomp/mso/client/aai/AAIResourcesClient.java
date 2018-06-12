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

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.onap.aai.domain.yang.Relationship;
import org.openecomp.mso.client.RestClient;
import org.openecomp.mso.client.aai.entities.AAIResultWrapper;
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri;
import org.openecomp.mso.client.aai.entities.uri.AAIUri;
import org.openecomp.mso.client.aai.entities.uri.Depth;

public class AAIResourcesClient extends AAIClient {
	
	private final AAIVersion version;
		
	public AAIResourcesClient() {
		super();
		this.version = super.getVersion();
	}
	
	public AAIResourcesClient(AAIVersion version) {
		super();
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
		
		return aaiRC.get().getStatus() == Status.OK.getStatusCode();
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
		Map<String, Object> result = aaiRC.get(new GenericType<Map<String, Object>>(){})
				.orElseThrow(() -> new NotFoundException(clone.build() + " does not exist in A&AI"));
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
	public <T> Optional<T> get(Class<T> clazz, AAIResourceUri uri) {
		return this.createClient(uri).get(clazz);
	}
	
	/**
	 * Retrieves an object from A&AI and returns complete response
	 * @param uri
	 * @return
	 */
	public Response getFullResponse(AAIResourceUri uri) {
		return this.createClient(uri).get();
	}
	
	/**
	 * Retrieves an object from A&AI and automatically unmarshalls it into a Map or List 
	 * @param resultClass
	 * @param uri
	 * @return
	 */
	public <T> Optional<T> get(GenericType<T> resultClass, AAIResourceUri uri) {
		return this.createClient(uri).get(resultClass);
	}
	
	/**
	 * Retrieves an object from A&AI wrapped in a helper class which offer additional features
	 * 
	 * @param uri
	 * @return
	 */
	public AAIResultWrapper get(AAIResourceUri uri) {
		String json = this.createClient(uri).get(String.class).orElse(null);
		return new AAIResultWrapper(json);
	}
	
	/**
	 * Retrieves an object from A&AI wrapped in a helper class which offer additional features
	 * If the object cannot be found in A&AI the method will throw the runtime exception
	 * included as an argument
	 * @param uri
	 * @return
	 */
	public AAIResultWrapper get(AAIResourceUri uri, Class<? extends RuntimeException> c) {
		
		RuntimeException e;
		try {
			e = c.getConstructor(String.class).newInstance(uri.build() + " not found in A&AI");
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e1) {
			throw new IllegalArgumentException("could not create instance for " + c.getName());
		}
		String json = this.createClient(uri).get(String.class)
				.orElseThrow(() -> e);
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
		return new AAITransactionalClient(this.version);
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
