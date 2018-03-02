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
