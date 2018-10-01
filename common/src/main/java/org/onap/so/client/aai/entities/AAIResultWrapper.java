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

package org.onap.so.client.aai.entities;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


import org.onap.so.client.aai.AAICommonObjectMapperProvider;
import org.onap.so.jsonpath.JsonPathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AAIResultWrapper implements Serializable {

	private static final long serialVersionUID = 5895841925807816737L;
	private final String jsonBody;
	private final ObjectMapper mapper;
	private final transient Logger logger = LoggerFactory.getLogger(AAIResultWrapper.class);
	
	public AAIResultWrapper(String json) {
		this.jsonBody = json;
		this.mapper = new AAICommonObjectMapperProvider().getMapper();
	}
	
	public AAIResultWrapper(Object aaiObject) {
		this.mapper = new AAICommonObjectMapperProvider().getMapper();
		this.jsonBody = mapObjectToString(aaiObject);
	}
	
	protected String mapObjectToString(Object aaiObject) {
		try {
			return mapper.writeValueAsString(aaiObject);
		} catch (JsonProcessingException e) {
			logger.warn("could not parse object into json - defaulting to {}");
			return "{}";
		}
	}
	public Optional<Relationships> getRelationships() {
		final String path = "$.relationship-list";
		if (isEmpty()) {
			return Optional.empty();
		}
		Optional<String> result = JsonPathUtil.getInstance().locateResult(jsonBody, path);
		if (result.isPresent()) {
			return Optional.of(new Relationships(result.get()));
		} else {
			return Optional.empty();
		}
	}
	
	public String getJson() {
		if(jsonBody == null) {
			return "{}";
		} else {
			return jsonBody;
		}
	}
	
	public Map<String, Object> asMap() {
		if (isEmpty()) {
			return new HashMap<>();
		}
		try {
			return mapper.readValue(this.jsonBody, new TypeReference<Map<String, Object>>(){});
		} catch (IOException e) {
			return new HashMap<>();
		}
	}
	
	public <T> Optional<T> asBean(Class<T> clazz) {
		if (isEmpty()) {
			return Optional.empty();
		}
		try {
			return Optional.of(mapper.readValue(this.jsonBody, clazz));
		} catch (IOException e) {
			return Optional.empty();
		}
	}
	
	public boolean isEmpty() {
		return jsonBody == null;
	}
	@Override
	public String toString() {
		return this.getJson();
	}

}
