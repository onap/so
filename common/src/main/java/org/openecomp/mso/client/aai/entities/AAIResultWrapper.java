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

package org.openecomp.mso.client.aai.entities;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.openecomp.mso.client.aai.AAICommonObjectMapperProvider;
import org.openecomp.mso.jsonpath.JsonPathUtil;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AAIResultWrapper {

	private final String jsonBody;
	private final ObjectMapper mapper;
	public AAIResultWrapper(String json) {
		this.jsonBody = json;
		this.mapper = new AAICommonObjectMapperProvider().getMapper();
	}
	
	public Optional<Relationships> getRelationships() {
		final String path = "$.relationship-list";
		Optional<String> result = JsonPathUtil.getInstance().locateResult(jsonBody, path);
		if (result.isPresent()) {
			return Optional.of(new Relationships(result.get()));
		} else {
			return Optional.empty();
		}
	}
	
	public String getJson() {
		return jsonBody;
	}
	
	public Map<String, Object> asMap() {
		try {
			return mapper.readValue(this.jsonBody, new TypeReference<Map<String, Object>>(){});
		} catch (IOException e) {
			return new HashMap<>();
		}
	}
	
	public <T> Optional<T> asBean(Class<T> clazz) {
		try {
			return Optional.of(mapper.readValue(this.jsonBody, clazz));
		} catch (IOException e) {
			return Optional.empty();
		}
	}
	
	@Override
	public String toString() {
		return this.getJson();
	}

}
