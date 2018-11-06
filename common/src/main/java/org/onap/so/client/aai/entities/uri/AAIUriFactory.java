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

package org.onap.so.client.aai.entities.uri;

import java.net.URI;

import org.onap.so.client.aai.AAIObjectPlurals;
import org.onap.so.client.aai.AAIObjectType;


public class AAIUriFactory {
	
	/**
	 * values are filled into the URI template specified in {@link AAIObjectType} in order
	 * <br>
	 * There are two special lookups performed on certain types when a single value is specified:
	 * <br>
	 * Service Instance and AllottedResources
	 * <br>
	 * These can be retrieved without all their required keys but an HTTP call is required to do so
	 * @param type
	 * @param values
	 * @return
	 */
	public static AAIResourceUri createResourceUri(AAIObjectType type, Object... values) {
		if (AAIObjectType.SERVICE_INSTANCE.equals(type)) {
			return new ServiceInstanceUri(values);
		} else if (AAIObjectType.ALLOTTED_RESOURCE.equals(type)) {
			return new AllottedResourceLookupUri(values);
		} else {
			return new AAISimpleUri(type, values);
		}
	}
	
	public static AAIResourceUri createNodesUri(AAIObjectType type, Object... values) {
		return new NodesUri(type, values);
		
	}
	
	public static AAIResourceUri createNodesUri(AAIObjectPlurals type) {
		return new NodesUri(type);
		
	}
	
	/**
	 * This method should only be used to wrap a URI retrieved from A&AI contained within an object response
	 * 
	 * @param type
	 * @param uri
	 * @return
	 */
	public static AAIResourceUri createResourceFromExistingURI(AAIObjectType type, URI uri) {
		return new AAISimpleUri(type, uri);
	}
	
	
	/**
	 * creates an AAIResourceUri from a parentUri
	 * 
	 * @param parentUri
	 * @param childType
	 * @param childValues
	 * @return
	 */
	public static AAIResourceUri createResourceFromParentURI(AAIResourceUri parentUri, AAIObjectType childType, Object... childValues) {
		
		return new AAISimpleUri(parentUri, childType, childValues);
	}
	
	/**
	 * Creates a uri for a plural type e.g. /cloud-infrastructure/pservers
	 * 
	 * @param type
	 * @return
	 */
	public static AAIResourceUri createResourceUri(AAIObjectPlurals type) {
		
		return new AAISimpleUri(type);
	
	}
	
	/**
	 * Creates a uri for a plural type with values e.g. /cloud-infrastructure/pservers
	 * 
	 * @param type
	 * @return
	 */
	public static AAIResourceUri createResourceUri(AAIObjectPlurals type, Object... values) {
		
		return new AAISimpleUri(type, values);
	
	}
}