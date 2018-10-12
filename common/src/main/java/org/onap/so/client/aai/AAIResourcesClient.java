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
import org.onap.so.client.graphinventory.entities.uri.Depth;

public class AAIResourcesClient extends AAIClient {
			
	public AAIResourcesClient() {
		super();
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
		try {
			RestClient aaiRC = this.createClient(forceMinimal);
			
			return aaiRC.get().getStatus() == Status.OK.getStatusCode();
		} catch (NotFoundException e) {
			return false;
		}
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
	 * Adds a relationship between two objects in A&AI 
	 * with a given edge label
	 * @param uriA
	 * @param uriB
	 * @param edge label
	 * @return
	 */
	public void connect(AAIResourceUri uriA, AAIResourceUri uriB, AAIEdgeLabel label) {
		AAIResourceUri uriAClone = uriA.clone();
		RestClient aaiRC = this.createClient(uriAClone.relationshipAPI());
		aaiRC.put(this.buildRelationship(uriB, label));
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
	
	/**
	 * Retrieves an object from A&AI and returns complete response
	 * @param uri
	 * @return
	 */
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
	
	/**
	 * Retrieves an object from A&AI and automatically unmarshalls it into a Map or List 
	 * @param resultClass
	 * @param uri
	 * @return
	 */
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
	
	/**
	 * Retrieves an object from A&AI wrapped in a helper class which offer additional features
	 * 
	 * @param uri
	 * @return
	 */
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
	
	/**
	 * Retrieves an object from A&AI wrapped in a helper class which offer additional features
	 * If the object cannot be found in A&AI the method will throw the runtime exception
	 * included as an argument
	 * @param uri
	 * @return
	 */
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
		return new AAITransactionalClient(this.getVersion());
	}
	
	/**
	 * Starts a transaction groups multiple A&AI mutations
	 * 
	 * @return
	 */
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
	@Override
	public <T extends RestProperties> T getRestProperties() {
		return super.getRestProperties();
	}
}
