package org.openecomp.mso.adapters.providers;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Provider
@Produces("application/json")
public class JettisonStyleMapperProvider implements ContextResolver<ObjectMapper> {
	
	final ObjectMapper mapper;

	public JettisonStyleMapperProvider() {
		
		mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.enable(MapperFeature.USE_ANNOTATIONS);
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
		mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	@Override
	public ObjectMapper getContext(Class<?> type) {
		return mapper;
	}
}