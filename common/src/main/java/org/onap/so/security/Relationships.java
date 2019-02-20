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

package com.att.ecomp.mso.client.narad.entities;

import javax.ws.rs.core.UriBuilder;

import org.onap.so.client.graphinventory.entities.GraphInventoryRelationships;

import com.att.ecomp.mso.client.narad.NARADObjectType;
import com.att.ecomp.mso.client.narad.NARADResourcesClient;
import com.att.ecomp.mso.client.narad.entities.uri.NARADResourceUri;
import com.att.ecomp.mso.client.narad.entities.uri.NARADUriFactory;

public class Relationships extends GraphInventoryRelationships<NARADResultWrapper, NARADResourceUri, NARADObjectType>{

	public Relationships(String json) {
		super(json);
	}
	
	@Override
	protected NARADResultWrapper get(NARADResourceUri uri) {
		return new NARADResourcesClient().get(uri);
		
	}

	@Override
	protected NARADResourceUri createUri(NARADObjectType type, String relatedLink) {
		
		return NARADUriFactory.createResourceFromExistingURI(type, UriBuilder.fromPath(relatedLink).build());
	}

	@Override
	protected NARADObjectType fromTypeName(String name) {
		return NARADObjectType.fromTypeName(name);
	}
}
