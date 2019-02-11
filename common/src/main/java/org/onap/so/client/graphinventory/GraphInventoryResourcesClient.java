package org.onap.so.client.graphinventory;

import java.util.Optional;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.onap.so.client.RestProperties;
import org.onap.so.client.graphinventory.entities.GraphInventoryEdgeLabel;
import org.onap.so.client.graphinventory.entities.GraphInventoryResultWrapper;
import org.onap.so.client.graphinventory.entities.uri.GraphInventoryUri;

public interface GraphInventoryResourcesClient<Self, Uri extends GraphInventoryUri, EdgeLabel extends GraphInventoryEdgeLabel, Wrapper extends GraphInventoryResultWrapper, TransactionalClient, SingleTransactionClient> {

	/**
	 * creates a new object in GraphInventory
	 * 
	 * @param obj - can be any object which will marshal into a valid GraphInventory payload
	 * @param uri
	 * @return
	 */
	void create(Uri uri, Object obj);

	/**
	 * creates a new object in GraphInventory with no payload body
	 * 
	 * @param uri
	 * @return
	 */
	void createEmpty(Uri uri);

	/**
	 * returns false if the object does not exist in GraphInventory
	 * 
	 * @param uri
	 * @return
	 */
	boolean exists(Uri uri);

	/**
	 * Adds a relationship between two objects in GraphInventory 
	 * @param uriA
	 * @param uriB
	 * @return
	 */
	void connect(Uri uriA, Uri uriB);

	/**
	 * Adds a relationship between two objects in GraphInventory 
	 * with a given edge label
	 * @param uriA
	 * @param uriB
	 * @param edge label
	 * @return
	 */
	void connect(Uri uriA, Uri uriB, EdgeLabel label);

	/**
	 * Removes relationship from two objects in GraphInventory
	 * 
	 * @param uriA
	 * @param uriB
	 * @return
	 */
	void disconnect(Uri uriA, Uri uriB);

	/**
	 * Deletes object from GraphInventory. Automatically handles resource-version.
	 * 
	 * @param uri
	 * @return
	 */
	void delete(Uri uri);

	/**
	 * @param obj - can be any object which will marshal into a valid GraphInventory payload
	 * @param uri
	 * @return
	 */
	void update(Uri uri, Object obj);

	/**
	 * Retrieves an object from GraphInventory and unmarshalls it into the Class specified
	 * @param clazz
	 * @param uri
	 * @return
	 */
	<T> Optional<T> get(Class<T> clazz, Uri uri);

	/**
	 * Retrieves an object from GraphInventory and returns complete response
	 * @param uri
	 * @return
	 */
	Response getFullResponse(Uri uri);

	/**
	 * Retrieves an object from GraphInventory and automatically unmarshalls it into a Map or List 
	 * @param resultClass
	 * @param uri
	 * @return
	 */
	<T> Optional<T> get(GenericType<T> resultClass, Uri uri);

	/**
	 * Retrieves an object from GraphInventory wrapped in a helper class which offer additional features
	 * 
	 * @param uri
	 * @return
	 */
	Wrapper get(Uri uri);

	/**
	 * Retrieves an object from GraphInventory wrapped in a helper class which offer additional features
	 * If the object cannot be found in GraphInventory the method will throw the runtime exception
	 * included as an argument
	 * @param uri
	 * @return
	 */
	Wrapper get(Uri uri, Class<? extends RuntimeException> c);

	/**
	 * Will automatically create the object if it does not exist
	 * 
	 * @param obj - Optional object which serializes to a valid GraphInventory payload
	 * @param uri
	 * @return
	 */
	Self createIfNotExists(Uri uri, Optional<Object> obj);

	/**
	 * Starts a transaction which encloses multiple GraphInventory mutations
	 * 
	 * @return
	 */
	TransactionalClient beginTransaction();

	/**
	 * Starts a transaction groups multiple GraphInventory mutations
	 * 
	 * @return
	 */
	SingleTransactionClient beginSingleTransaction();

	<T extends RestProperties> T getRestProperties();

}