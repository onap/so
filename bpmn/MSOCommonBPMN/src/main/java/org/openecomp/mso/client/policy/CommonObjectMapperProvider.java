package org.openecomp.mso.client.policy;

import javax.ws.rs.ext.ContextResolver;

import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Provider
public class CommonObjectMapperProvider implements ContextResolver<ObjectMapper> {

	final ObjectMapper mapper;

	public CommonObjectMapperProvider() {
		
		mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.enable(MapperFeature.USE_ANNOTATIONS);
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
	}

	@Override
	public ObjectMapper getContext(Class<?> type) {
		return mapper;
	}
}