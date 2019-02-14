package org.onap.so.client.graphinventory;

public interface TransactionBuilder {

	
	void put(String uri, Object body);
	void delete(String uri, Object body);
	void patch(String uri,Object body);
	
}
