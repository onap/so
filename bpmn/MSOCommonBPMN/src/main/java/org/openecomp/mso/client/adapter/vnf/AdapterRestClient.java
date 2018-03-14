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

package org.openecomp.mso.client.adapter.vnf;

import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.ext.ContextResolver;

import org.apache.commons.codec.binary.Base64;
import org.openecomp.mso.bpmn.common.util.CryptoUtils;
import org.openecomp.mso.client.ResponseExceptionMapperImpl;
import org.openecomp.mso.client.policy.JettisonStyleMapperProvider;
import org.openecomp.mso.client.policy.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AdapterRestClient extends RestClient {

	private final AdapterRestProperties props;
	public AdapterRestClient(AdapterRestProperties props, URI uri) {
		super(props, UUID.randomUUID(), Optional.of(uri));
		this.props = props;
	}

	public AdapterRestClient(AdapterRestProperties props, URI uri, String accept, String contentType) {
		super(props, UUID.randomUUID(), Optional.of(uri), accept, contentType);
		this.props = props;
	}

	@Override
	protected void initializeHeaderMap(Map<String, String> headerMap) {
		headerMap.put("Authorization",
				this.getBasicAuth(props.getAuth(), props.getKey()));
	}

	@Override
	protected Optional<ClientResponseFilter> addResponseFilter() {
		return Optional.of(new ResponseExceptionMapperImpl());
	}

	@Override
	public RestClient addRequestId(UUID requestId) {
		return null;
	}

	@Override
	protected ContextResolver<ObjectMapper> getMapper() {
		return new JettisonStyleMapperProvider();
	}
	
	private String getBasicAuth(String encryptedAuth, String msoKey) {
		if ((encryptedAuth == null || encryptedAuth.isEmpty()) || (msoKey == null || msoKey.isEmpty())) {
			return null;
		}
		try {
			String auth = CryptoUtils.decrypt(encryptedAuth, msoKey);
			byte[] encoded = Base64.encodeBase64(auth.getBytes());
			String encodedString = new String(encoded);
			encodedString = "Basic " + encodedString;
			return encodedString;
		} catch (GeneralSecurityException e) {
			this.logger.warn(e.getMessage(), e);
			return null;
		}
	}
}
