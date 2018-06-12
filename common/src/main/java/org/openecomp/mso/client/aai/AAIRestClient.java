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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;

import org.openecomp.mso.client.ResponseExceptionMapper;
import org.openecomp.mso.client.RestClientSSL;
import org.openecomp.mso.client.aai.exceptions.AAIPatchDepthExceededException;
import org.openecomp.mso.client.policy.CommonObjectMapperProvider;
import org.openecomp.mso.jsonpath.JsonPathUtil;
import org.openecomp.mso.utils.TargetEntity;

import com.fasterxml.jackson.core.JsonProcessingException;

public class AAIRestClient extends RestClientSSL {

	private final AAIProperties props;
	private static final AAICommonObjectMapperProvider standardProvider = new AAICommonObjectMapperProvider();
	private static final AAICommonObjectMapperPatchProvider patchProvider = new AAICommonObjectMapperPatchProvider();
	private static final Pattern LOCATE_COMPLEX_OBJECT = Pattern.compile("^((?!relationship-list).)+?\\['[^\\[\\]]+?'\\]$");

	protected AAIRestClient(AAIProperties props, URI uri) {
		super(props, Optional.of(uri));
		this.props = props;
	}

	@Override
    public TargetEntity getTargetEntity(){
	    return TargetEntity.AAI;
    }

	@Override
	protected void initializeHeaderMap(Map<String, String> headerMap) {
		headerMap.put("X-FromAppId", "MSO");
		headerMap.put("X-TransactionId", requestId);
		String auth = props.getAuth();
		String key = props.getKey();

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
		String value = convertObjectToPatchFormat(obj);
		validatePatchObject(value);
		return super.patch(value);
	}

	@Override
	public <T> T patch(Object obj, Class<T> resultClass) {
		String value = convertObjectToPatchFormat(obj);
		validatePatchObject(value);
		return super.patch(value, resultClass);
	}
	
	protected String convertObjectToPatchFormat(Object obj) {
		Object value = obj;
		try {
			if (!(obj instanceof Map || obj instanceof String)) {
				value = patchProvider.getMapper().writeValueAsString(obj);
			} else if (obj instanceof Map) {
				value = standardProvider.getMapper().writeValueAsString(obj);
			}
		} catch (JsonProcessingException e) {
			value = "{}";
		}
		
		return (String)value;
	}
	
	
	protected void validatePatchObject(String payload) {
		if (hasComplexObject(payload)) {
			throw new AAIPatchDepthExceededException(payload);
		}
	}
	
	/** validates client side that json does not include any complex objects
	 * relationship-list is omitted from this validation
	 */
	protected boolean hasComplexObject(String json) {
		if (json.isEmpty()) {
			return false;
		}
		String complex = "$.*.*";
		String array = "$.*.*.*";
		List<String> result = JsonPathUtil.getInstance().getPathList(json, complex);
		List<String> result2 = JsonPathUtil.getInstance().getPathList(json, array);
		
		result.addAll(result2);
		return result.stream().anyMatch(item -> LOCATE_COMPLEX_OBJECT.matcher(item).find());
	}
}
