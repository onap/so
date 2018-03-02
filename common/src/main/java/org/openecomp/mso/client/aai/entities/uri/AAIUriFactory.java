package org.openecomp.mso.client.aai.entities.uri;

import java.net.URI;

import org.openecomp.mso.client.aai.AAIObjectPlurals;
import org.openecomp.mso.client.aai.AAIObjectType;


public class AAIUriFactory {
	
	/**
	 * values are filled into the URI template specified in {@link AAIObjectType} in order
	 * 
	 * @param type
	 * @param values
	 * @return
	 */
	public static AAIResourceUri createResourceUri(AAIObjectType type, Object... values) {
		if (AAIObjectType.SERVICE_INSTANCE.equals(type)) {
			return new ServiceInstanceUri(values);
		} else {
			return new SimpleUri(type, values);
		}
	}
	
	public static AAIResourceUri createNodesUri(AAIObjectType type, Object... values) {
		return new NodesUri(type, values);
		
	}
	
	/**
	 * This method should only be used to wrap a URI retrieved from A&AI contained within an object response
	 * 
	 * @param type
	 * @param uri
	 * @return
	 */
	public static AAIResourceUri createResourceFromExistingURI(AAIObjectType type, URI uri) {
		return new SimpleUri(type, uri);
	}
	
	/**
	 * Creates a uri for a plural type e.g. /cloud-infrastructure/pservers
	 * 
	 * @param type
	 * @return
	 */
	public static AAIResourceUri createResourceUri(AAIObjectPlurals type) {
		
		return new SimpleUri(type);
	
	}
}
