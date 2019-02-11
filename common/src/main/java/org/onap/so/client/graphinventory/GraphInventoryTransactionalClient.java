package org.onap.so.client.graphinventory;

import java.util.List;

import org.onap.so.client.graphinventory.entities.GraphInventoryEdgeLabel;
import org.onap.so.client.graphinventory.entities.uri.GraphInventoryUri;
import org.onap.so.client.graphinventory.exceptions.BulkProcessFailed;

public interface GraphInventoryTransactionalClient<Self, Uri extends GraphInventoryUri, EdgeLabel extends GraphInventoryEdgeLabel> {

	/**
	 * adds an additional transaction and closes the previous transaction
	 * 
	 * @return Self
	 */
	Self beginNewTransaction();

	/**
	 * creates a new object in A&AI
	 * 
	 * @param obj - can be any object which will marshal into a valid A&AI payload
	 * @param uri
	 * @return
	 */
	Self create(Uri uri, Object obj);

	/**
	 * creates a new object in A&AI with no payload body
	 * 
	 * @param uri
	 * @return
	 */
	Self createEmpty(Uri uri);

	/**
	 * Adds a relationship between two objects in A&AI 
	 * @param uriA
	 * @param uriB
	 * @return
	 */
	Self connect(Uri uriA, Uri uriB);

	/**
	 * relationship between multiple objects in A&AI - connects A to all objects specified in list
	 * 
	 * @param uriA
	 * @param uris
	 * @return
	 */
	Self connect(Uri uriA, List<Uri> uris);

	Self connect(Uri uriA, Uri uriB, EdgeLabel label);

	Self connect(Uri uriA, List<Uri> uris, EdgeLabel label);

	/**
	 * Removes relationship from two objects in A&AI
	 * 
	 * @param uriA
	 * @param uriB
	 * @return
	 */
	Self disconnect(Uri uriA, Uri uriB);

	/**
	 * Removes relationship from multiple objects - disconnects A from all objects specified in list
	 * @param uriA
	 * @param uris
	 * @return
	 */
	Self disconnect(Uri uriA, List<Uri> uris);

	/**
	 * Deletes object from A&AI. Automatically handles resource-version.
	 * 
	 * @param uri
	 * @return
	 */
	Self delete(Uri uri);

	/**
	 * @param obj - can be any object which will marshal into a valid A&AI payload
	 * @param uri
	 * @return
	 */
	Self update(Uri uri, Object obj);

	/**
	 * Executes all created transactions in A&AI
	 * @throws BulkProcessFailed 
	 */
	void execute() throws BulkProcessFailed;

}