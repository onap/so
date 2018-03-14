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

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.UriBuilder;

import org.openecomp.mso.client.aai.AAIObjectType;
import org.openecomp.mso.client.aai.AAIQueryClient;
import org.openecomp.mso.client.aai.Format;
import org.openecomp.mso.client.aai.entities.CustomQuery;
import org.openecomp.mso.client.aai.entities.Results;
import org.openecomp.mso.client.aai.exceptions.AAIPayloadException;
import org.openecomp.mso.client.aai.exceptions.AAIUriComputationException;
import org.openecomp.mso.client.aai.exceptions.AAIUriNotFoundException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ServiceInstanceUri extends SimpleUri {

	private Optional<String> cachedValue = Optional.empty();

	protected ServiceInstanceUri(Object... values) {
		super(AAIObjectType.SERVICE_INSTANCE, values);
	}
	protected ServiceInstanceUri(UriBuilder builder, Optional<String> cachedValue, Object... values) {
		super(AAIObjectType.SERVICE_INSTANCE, builder, values);
		this.cachedValue = cachedValue;
	}
	protected String getSerivceInstance(Object id) throws AAIUriNotFoundException, AAIPayloadException {
		if (!this.getCachedValue().isPresent()) {
			AAIResourceUri serviceInstanceUri = AAIUriFactory.createNodesUri(AAIObjectType.SERVICE_INSTANCE, id);
			CustomQuery query = new CustomQuery(Collections.singletonList(serviceInstanceUri));
			String resultJson;
			try {
				resultJson = this.getQueryClient().query(Format.PATHED, query);
			} catch (ResponseProcessingException e) {
				if (e.getCause() instanceof BadRequestException) {
					throw new AAIUriNotFoundException("Service instance " + id + " not found at: " + serviceInstanceUri.build());
				} else {
					throw e;
				}
			}
			try {
				cachedValue = extractRelatedLink(resultJson);
				if (!cachedValue.isPresent()) {
					throw new AAIUriNotFoundException("Service instance " + id + " not found at: " + serviceInstanceUri.build());
				}
			} catch (IOException e) {
				throw new AAIPayloadException("could not map payload: " + resultJson, e);
			}
			
		}
		
		return this.getCachedValue().get();
	}
	
	protected Optional<String> extractRelatedLink(String jsonString) throws IOException {
		Optional<String> result;
		ObjectMapper mapper = new ObjectMapper();
		
			Results<Map<String, String>> results = mapper.readValue(jsonString, new TypeReference<Results<Map<String, String>>>(){});
			if (results.getResult().size() == 1) {
				String uriString = results.getResult().get(0).get("resource-link");
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
			String uri = getSerivceInstance(this.values[0]);
			Map<String, String> map = getURIKeys(uri);
			return super.build(map.values().toArray(values));
		}
		} catch (AAIUriNotFoundException | AAIPayloadException e) {
			throw new AAIUriComputationException(e);
		}
		return super.build();
	}
	
	@Override
	public ServiceInstanceUri clone() {
		return new ServiceInstanceUri(this.internalURI.clone(), this.getCachedValue(), values);
	}
	
	protected AAIQueryClient getQueryClient() {
		return new AAIQueryClient();
	}
}