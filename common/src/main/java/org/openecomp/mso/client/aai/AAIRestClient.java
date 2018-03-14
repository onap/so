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

package org.openecomp.mso.client.aai;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.ext.ContextResolver;

import org.openecomp.mso.client.RestProperties;
import org.openecomp.mso.client.policy.RestClient;
import org.openecomp.mso.client.policy.RestClientSSL;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AAIRestClient extends RestClientSSL {

	protected AAIRestClient(RestProperties props, UUID requestId, URI uri) {
		super(props, requestId, Optional.of(uri));
		headerMap.put("X-TransactionId", requestId.toString());
	}

	@Override
	protected void initializeHeaderMap(Map<String, String> headerMap) {
		headerMap.put("X-FromAppId", "MSO");
	}

	@Override
	protected Optional<ClientResponseFilter> addResponseFilter() {

		return Optional.of(new AAIClientResponseExceptionMapper(this.getRequestId()));
	}

	@Override
	public RestClient addRequestId(UUID requestId) {
		headerMap.put("X-TransactionId", requestId.toString());
		this.requestId = requestId;
		return this;
	}
	
	@Override
	protected ContextResolver<ObjectMapper> getMapper() {
		return new AAICommonObjectMapperProvider();
	}

}
