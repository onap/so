package org.openecomp.mso.asdc.client.test.emulators;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonResourceInfoDeserializer extends JsonDeserializer<List<JsonResourceInfo>>{

	@Override
	public List<JsonResourceInfo> deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		List<JsonResourceInfo> jsonResourceInfoList =  new ObjectMapper().readValue(jp, new TypeReference<List<JsonResourceInfo>>(){}); 
		
		return jsonResourceInfoList;
	}

}
