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

package org.onap.so.client.graphinventory;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.onap.so.client.ResponseExceptionMapper;
import org.onap.so.client.RestClientSSL;
import org.onap.so.client.RestProperties;
import org.onap.so.client.policy.CommonObjectMapperProvider;
import org.onap.so.utils.TargetEntity;

public abstract class GraphInventoryRestClient extends RestClientSSL {

	protected static final GraphInventoryCommonObjectMapperProvider standardProvider = new GraphInventoryCommonObjectMapperProvider();

	protected final GraphInventoryPatchConverter patchConverter = new GraphInventoryPatchConverter();
	
	protected GraphInventoryRestClient(RestProperties props, URI uri) {
		super(props, Optional.of(uri));
	}

	@Override
    public abstract TargetEntity getTargetEntity();

	@Override
	protected abstract void initializeHeaderMap(Map<String, String> headerMap);

	@Override
	protected abstract Optional<ResponseExceptionMapper> addResponseExceptionMapper();
	
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
