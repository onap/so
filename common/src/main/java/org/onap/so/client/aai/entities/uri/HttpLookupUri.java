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

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriBuilder;

import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.entities.Results;
import org.onap.so.client.graphinventory.Format;
import org.onap.so.client.graphinventory.entities.Pathed;
import org.onap.so.client.graphinventory.entities.uri.HttpAwareUri;
import org.onap.so.client.graphinventory.exceptions.GraphInventoryPayloadException;
import org.onap.so.client.graphinventory.exceptions.GraphInventoryUriComputationException;
import org.onap.so.client.graphinventory.exceptions.GraphInventoryUriNotFoundException;
import org.onap.so.client.graphinventory.exceptions.IncorrectNumberOfUriKeys;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class HttpLookupUri extends AAISimpleUri implements HttpAwareUri {

	private Optional<String> cachedValue = Optional.empty();
	private final AAIObjectType aaiType;
	protected HttpLookupUri(AAIObjectType type, Object... values) {
		super(type, values);
		this.aaiType = type;
	}
	protected HttpLookupUri(AAIObjectType type, UriBuilder builder, Optional<String> cachedValue, Object... values) {
		super(type, builder, values);
		this.cachedValue = cachedValue;
		this.aaiType = type;
	}
	protected String getObjectById(Object id) throws GraphInventoryUriNotFoundException, GraphInventoryPayloadException {
		if (!this.getCachedValue().isPresent()) {
			AAIResourceUri serviceInstanceUri = AAIUriFactory.createNodesUri(aaiType, id).format(Format.PATHED);
			String resultJson;
			try {
				resultJson = this.getResourcesClient().get(serviceInstanceUri, NotFoundException.class).getJson();
			} catch (BadRequestException e) {
				throw new GraphInventoryUriNotFoundException(aaiType.typeName() + " " + id + " not found at: " + serviceInstanceUri.build());
				
			}
			try {
				cachedValue = extractRelatedLink(resultJson);
				if (!cachedValue.isPresent()) {
					throw new GraphInventoryUriNotFoundException(aaiType.typeName() + " " + id + " not found at: " + serviceInstanceUri.build());
				}
			} catch (IOException e) {
				throw new GraphInventoryPayloadException("could not map payload: " + resultJson, e);
			}
		}
		Optional<String> cachedValueOpt = this.getCachedValue();
		return cachedValueOpt.isPresent() ? cachedValueOpt.get() : "";
	}
	
	protected Optional<String> extractRelatedLink(String jsonString) throws IOException {
		Optional<String> result;
		ObjectMapper mapper = new ObjectMapper();
		
		Results<Pathed> results = mapper.readValue(jsonString, new TypeReference<Results<Pathed>>(){});
		if (results.getResult().size() == 1) {
			String uriString = results.getResult().get(0).getResourceLink();
			URI uri = UriBuilder.fromUri(uriString).build();
			String rawPath = uri.getRawPath();
			result = Optional.of(rawPath.replaceAll("/aai/v\\d+", ""));
		} else if (results.getResult().isEmpty()) {
			result = Optional.empty();
		} else {
			throw new IllegalStateException("more than one result returned");
		}
	
		return result;
	}
	
	protected Optional<String> getCachedValue() {
		return this.cachedValue;
	}
	
	@Override
	public URI build() {
		try {
			if (this.values.length == 1) {
				String uri = getObjectById(this.values[0]);
				Map<String, String> map = getURIKeys(uri);
				return super.build(map.values().toArray(values));
			}
		} catch (GraphInventoryUriNotFoundException | GraphInventoryPayloadException e) {
			throw new GraphInventoryUriComputationException(e);
		}
		return super.build();
	}
	
	@Override
	public abstract HttpLookupUri clone();
	
	@Override
	public void validateValuesSize(String template, Object... values) {
		try {
			super.validateValuesSize(template, values);
		} catch (IncorrectNumberOfUriKeys e) {
			if (values.length == 1) {
				//Special case where we perform an http look up
			} else {
				throw e;
			}
		}
	}
	public AAIResourcesClient getResourcesClient() {
		return new AAIResourcesClient();
	}
	@Override
	public abstract URI buildNoNetwork();
}
