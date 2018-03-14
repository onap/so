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

package org.openecomp.mso.client.aai.entities.uri;

import java.net.URI;
import java.util.Map;

import org.openecomp.mso.client.aai.AAIObjectPlurals;
import org.openecomp.mso.client.aai.AAIObjectType;

public interface AAIUri {

	public URI build();
	/**
	 * By default A&AI enforces a depth of 1. Some objects can be told to retrieve objects
	 * nested beneath them by increasing this number.
	 * 
	 * You can use 0 to restrict the returned information to only the object you requested
	 * You can use all to retrieve all nested objects (this should only be used if you really need a massive amount of information and are caching the retrieval)
	 * @param depth
	 * @return
	 */
	public AAIUri depth(Depth depth);
	/**
	 * Makes client only return object fields, no relationships
	 * 
	 * @return
	 */
	public AAIUri nodesOnly(boolean nodesOnly);
	public AAIUri queryParam(String name, String... values);
	public AAIUri clone();
	
	/**
	 * returns all key values of the URI as a map. Key names can be found in {@link AAIObjectType}
	 * @return
	 */
	public Map<String, String> getURIKeys();
	public AAIObjectType getObjectType();
	public boolean equals(Object o);
}