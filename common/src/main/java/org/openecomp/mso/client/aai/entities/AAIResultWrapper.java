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
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.openecomp.mso.client.aai.AAICommonObjectMapperProvider;
import org.openecomp.mso.jsonpath.JsonPathUtil;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AAIResultWrapper implements Serializable {

	private static final long serialVersionUID = 5895841925807816737L;
	private final Optional<String> jsonBody;
	private final ObjectMapper mapper;
	public AAIResultWrapper(String json) {
		this.jsonBody = Optional.ofNullable(json);
		this.mapper = new AAICommonObjectMapperProvider().getMapper();
	}
	
	public Optional<Relationships> getRelationships() {
		final String path = "$.relationship-list";
		if (!jsonBody.isPresent()) {
			return Optional.empty();
		}
		Optional<String> result = JsonPathUtil.getInstance().locateResult(jsonBody.get(), path);
		if (result.isPresent()) {
			return Optional.of(new Relationships(result.get()));
		} else {
			return Optional.empty();
		}
	}
	
	public String getJson() {
		return jsonBody.orElse("{}");
	}
	
	public Map<String, Object> asMap() {
		if (!this.jsonBody.isPresent()) {
			return new HashMap<>();
		}
		try {
			return mapper.readValue(this.jsonBody.get(), new TypeReference<Map<String, Object>>(){});
		} catch (IOException e) {
			return new HashMap<>();
		}
	}
	
	public <T> Optional<T> asBean(Class<T> clazz) {
		if (!this.jsonBody.isPresent()) {
			return Optional.empty();
		}
		try {
			return Optional.of(mapper.readValue(this.jsonBody.get(), clazz));
		} catch (IOException e) {
			return Optional.empty();
		}
	}
	
	public boolean isEmpty() {
		return !this.jsonBody.isPresent();
	}
	@Override
	public String toString() {
		return this.getJson();
	}

}
