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

package org.onap.so.client.aai;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.onap.so.client.ResponseExceptionMapper;
import org.onap.so.client.RestClientSSL;
import org.onap.so.client.graphinventory.GraphInventoryPatchConverter;
import org.onap.so.client.policy.CommonObjectMapperProvider;
import org.onap.so.utils.TargetEntity;

public class AAIRestClient extends RestClientSSL {

	private final AAIProperties aaiProperties;
	private static final AAICommonObjectMapperProvider standardProvider = new AAICommonObjectMapperProvider();

	private final GraphInventoryPatchConverter patchConverter = new GraphInventoryPatchConverter();
	
	protected AAIRestClient(AAIProperties props, URI uri) {
		super(props, Optional.of(uri));
		this.aaiProperties = props;
	}

	@Override
    public TargetEntity getTargetEntity(){
	    return TargetEntity.AAI;
    }

	@Override
	protected void initializeHeaderMap(Map<String, String> headerMap) {
		headerMap.put("X-FromAppId", aaiProperties.getSystemName());
		headerMap.put("X-TransactionId", requestId);
		String auth = aaiProperties.getAuth();
		String key = aaiProperties.getKey();

		if (auth != null && !auth.isEmpty() && key != null && !key.isEmpty()) {
			addBasicAuthHeader(auth, key);
		}
	}

	@Override
	protected Optional<ResponseExceptionMapper> addResponseExceptionMapper() {

		return Optional.of(new AAIClientResponseExceptionMapper());
	}
	
	@Override
	protected CommonObjectMapperProvider getCommonObjectMapperProvider() {
		return standardProvider;
	}

	@Override
	public Response patch(Object obj) {
		return super.patch(convertToPatchFormat(obj));
	}

	@Override
	public <T> T patch(Object obj, Class<T> resultClass) {
		return super.patch(convertToPatchFormat(obj), resultClass);
	}
	
	protected GraphInventoryPatchConverter getPatchConverter() {
		return this.patchConverter;
	}
	
	protected String convertToPatchFormat(Object obj) {
		return getPatchConverter().convertPatchFormat(obj);
	}

}
