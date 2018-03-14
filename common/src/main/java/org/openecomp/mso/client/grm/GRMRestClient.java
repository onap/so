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

package org.openecomp.mso.client.grm;


import java.net.URI;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.client.ClientResponseFilter;

import org.openecomp.mso.client.ResponseExceptionMapperImpl;
import org.openecomp.mso.client.RestProperties;
import org.openecomp.mso.client.policy.RestClient;

public class GRMRestClient extends RestClient {

	private final String username;
	private final String password;
	
	public GRMRestClient(RestProperties props, URI path, String username, String password) {
		super(props, UUID.randomUUID(), Optional.of(path));
		this.username = username;
		this.password = password;
	}

	@Override
	protected void initializeHeaderMap(Map<String, String> headerMap) {
		headerMap.put("Authorization", "Basic " + Base64.getEncoder().encodeToString(new String(username + ":" + password).getBytes()));
	}

	@Override
	protected Optional<ClientResponseFilter> addResponseFilter() {
		return Optional.of(new ResponseExceptionMapperImpl());
	}

	@Override
	public RestClient addRequestId(UUID requestId) {
		return this;
	}
}
