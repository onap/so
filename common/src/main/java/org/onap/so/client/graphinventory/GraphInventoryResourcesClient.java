/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.graphinventory;

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
import org.onap.so.client.graphinventory.entities.GraphInventoryEdgeLabel;
import org.onap.so.client.graphinventory.entities.GraphInventoryResultWrapper;
import org.onap.so.client.graphinventory.entities.uri.Depth;
import org.onap.so.client.graphinventory.entities.uri.GraphInventoryResourceUri;
import org.onap.so.client.graphinventory.entities.uri.GraphInventoryUri;

public abstract class GraphInventoryResourcesClient<Self, Uri extends GraphInventoryResourceUri, EdgeLabel extends GraphInventoryEdgeLabel, Wrapper extends GraphInventoryResultWrapper, TransactionalClient, SingleTransactionClient> {

	protected GraphInventoryClient client;
	
	protected GraphInventoryResourcesClient(GraphInventoryClient client) {
		this.client = client;
	}
	/**
	 * creates a new object in GraphInventory
	 * 
	 * @param obj - can be any object which will marshal into a valid GraphInventory payload
	 * @param uri
	 * @return
	 */
	public void create(Uri uri, Object obj) {
		RestClient giRC = client.createClient(uri);
		giRC.put(obj);
	}

	/**
	 * creates a new object in GraphInventory with no payload body
	 * 
	 * @param uri
	 * @return
	 */
	public void createEmpty(Uri uri) {
		RestClient giRC = client.createClient(uri);
		giRC.put("");
	}

	/**
	 * returns false if the object does not exist in GraphInventory
	 * 
	 * @param uri
	 * @return
	 */
	public boolean exists(Uri uri) {
		GraphInventoryUri forceMinimal = this.addParams(Optional.of(Depth.ZERO), true, uri);
		try {
			RestClient giRC = client.createClient(forceMinimal);
			
			return giRC.get().getStatus() == Status.OK.getStatusCode();
		} catch (NotFoundException e) {
			return false;
		}
	}

	/**
	 * Adds a relationship between two objects in GraphInventory 
	 * @param uriA
	 * @param uriB
	 * @return
	 */
	public void connect(Uri uriA, Uri uriB) {
		GraphInventoryResourceUri uriAClone = uriA.clone();
		RestClient giRC = client.createClient(uriAClone.relationshipAPI());
		giRC.put(this.buildRelationship(uriB));
	}

	/**
	 * Adds a relationship between two objects in GraphInventory 
	 * with a given edge label
	 * @param uriA
	 * @param uriB
	 * @param edge label
	 * @return
	 */
	public void connect(Uri uriA, Uri uriB, EdgeLabel label) {
		GraphInventoryResourceUri uriAClone = uriA.clone();
		RestClient giRC = client.createClient(uriAClone.relationshipAPI());
		giRC.put(this.buildRelationship(uriB, label));
	}

	/**
	 * Removes relationship from two objects in GraphInventory
	 * 
	 * @param uriA
	 * @param uriB
	 * @return
	 */
	public void disconnect(Uri uriA, Uri uriB) {
		GraphInventoryResourceUri uriAClone = uriA.clone();
		RestClient giRC = client.createClient(uriAClone.relationshipAPI());
		giRC.delete(this.buildRelationship(uriB));
	}

	/**
	 * Deletes object from GraphInventory. Automatically handles resource-version.
	 * 
	 * @param uri
	 * @return
	 */
	public void delete(Uri uri) {
		GraphInventoryResourceUri clone = uri.clone();
		RestClient giRC = client.createClient(clone);
		Map<String, Object> result = giRC.get(new GenericType<Map<String, Object>>(){})
				.orElseThrow(() -> new NotFoundException(clone.build() + " does not exist in " + client.getGraphDBName()));
		String resourceVersion = (String) result.get("resource-version");
		giRC = client.createClient(clone.resourceVersion(resourceVersion));
		giRC.delete();
	}

	/**
	 * @param obj - can be any object which will marshal into a valid GraphInventory payload
	 * @param uri
	 * @return
	 */
	public void update(Uri uri, Object obj) {
		RestClient giRC = client.createClient(uri);
		giRC.patch(obj);
	}

	/**
	 * Retrieves an object from GraphInventory and unmarshalls it into the Class specified
	 * @param clazz
	 * @param uri
	 * @return
	 */
	public <T> Optional<T> get(Class<T> clazz, Uri uri) {
		try {
			return client.createClient(uri).get(clazz);
		} catch (NotFoundException e) {
			if (this.getRestProperties().mapNotFoundToEmpty()) {
				return Optional.empty();
			} else {
				throw e;
			}
		}
	}

	/**
	 * Retrieves an object from GraphInventory and returns complete response
	 * @param uri
	 * @return
	 */
	public Response getFullResponse(Uri uri) {
		try {
			return client.createClient(uri).get();
		} catch (NotFoundException e) {
			if (this.getRestProperties().mapNotFoundToEmpty()) {
				return e.getResponse();
			} else {
				throw e;
			}
		}
	}

	/**
	 * Retrieves an object from GraphInventory and automatically unmarshalls it into a Map or List 
	 * @param resultClass
	 * @param uri
	 * @return
	 */
	public <T> Optional<T> get(GenericType<T> resultClass, Uri uri) {
		try {
			return client.createClient(uri).get(resultClass);
		} catch (NotFoundException e) {
			if (this.getRestProperties().mapNotFoundToEmpty()) {
				return Optional.empty();
			} else {
				throw e;
			}
		}
	}
	/**
	 * Retrieves an object from GraphInventory wrapped in a helper class which offer additional features
	 * 
	 * @param uri
	 * @return
	 */
	public Wrapper get(Uri uri) {
		String json;
		try {
			json = client.createClient(uri).get(String.class).orElse(null);
		} catch (NotFoundException e) {
			if (this.getRestProperties().mapNotFoundToEmpty()) {
				json = null;
			} else {
				throw e;
			}
		}
		return this.createWrapper(json);
	}

	/**
	 * Retrieves an object from GraphInventory wrapped in a helper class which offer additional features
	 * If the object cannot be found in GraphInventory the method will throw the runtime exception
	 * included as an argument
	 * @param uri
	 * @return
	 */
	public Wrapper get(Uri uri, Class<? extends RuntimeException> c) {
		String json;
		try {
			json = client.createClient(uri).get(String.class)
					.orElseThrow(() -> createException(c, uri.build() + " not found in " + client.getGraphDBName(), Optional.empty()));
		} catch (NotFoundException e) {
			throw createException(c, "could not construct uri for use with " + client.getGraphDBName(), Optional.of(e));
		}

		return this.createWrapper(json);
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

	/**
	 * Will automatically create the object if it does not exist
	 * 
	 * @param obj - Optional object which serializes to a valid GraphInventory payload
	 * @param uri
	 * @return
	 */
	public Self createIfNotExists(Uri uri, Optional<Object> obj) {
		if(!this.exists(uri)){
			if (obj.isPresent()) {
				this.create(uri, obj.get());
			} else {
				this.createEmpty(uri);
			}
			
		}
		return (Self)this;
	}
	protected Relationship buildRelationship(GraphInventoryResourceUri uri) {
		return buildRelationship(uri, Optional.empty());
	}
	
	protected Relationship buildRelationship(GraphInventoryResourceUri uri, GraphInventoryEdgeLabel label) {
		return buildRelationship(uri, Optional.of(label));
	}
	protected Relationship buildRelationship(GraphInventoryResourceUri uri, Optional<GraphInventoryEdgeLabel> label) {
		final Relationship result = new Relationship();
		result.setRelatedLink(uri.build().toString());
		if (label.isPresent()) {
			result.setRelationshipLabel(label.get().toString());
		}
		return result;
	}
	
	public abstract Wrapper createWrapper(String json);
	
	/**
	 * Starts a transaction which encloses multiple GraphInventory mutations
	 * 
	 * @return
	 */
	public abstract TransactionalClient beginTransaction();

	/**
	 * Starts a transaction groups multiple GraphInventory mutations
	 * 
	 * @return
	 */
	public abstract SingleTransactionClient beginSingleTransaction();

	private GraphInventoryUri addParams(Optional<Depth> depth, boolean nodesOnly, GraphInventoryUri uri) {
		GraphInventoryUri clone = uri.clone();
		if (depth.isPresent()) {
			clone.depth(depth.get());
		}
		if (nodesOnly) {
			clone.nodesOnly(nodesOnly);
		}
		
		return clone;
	}
	
	public <T extends RestProperties> T getRestProperties() {
		return client.getRestProperties();
	}

}
