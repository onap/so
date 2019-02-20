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

package org.onap.so.client.aai.entities;

import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.graphinventory.GraphInventoryObjectName;
import org.onap.so.client.graphinventory.entities.GraphInventoryRelationships;

public class Relationships extends GraphInventoryRelationships<AAIResultWrapper, AAIResourceUri, AAIObjectType>{

	public Relationships(String json) {
		super(json);
	}
	
	@Deprecated
	/**
	 * Use getRelatedUris instead
	 * @return
	 */
	public List<AAIResourceUri> getRelatedAAIUris() {
		return this.getRelatedUris();
	}
	
	@Deprecated
	/**
	 * Use getRelatedUris instead
	 * @return
	 */
	public List<AAIResourceUri> getRelatedAAIUris(GraphInventoryObjectName type) {
		return this.getRelatedUris(type);
	}
	
	
	protected AAIResultWrapper get(AAIResourceUri uri) {
		return new AAIResourcesClient().get(uri);
		
	}

	@Override
	protected AAIResourceUri createUri(AAIObjectType type, String relatedLink) {
		
		return AAIUriFactory.createResourceFromExistingURI(type, UriBuilder.fromPath(relatedLink).build());
	}

	@Override
	protected AAIObjectType fromTypeName(String name) {
		return AAIObjectType.fromTypeName(name);
	}
}
