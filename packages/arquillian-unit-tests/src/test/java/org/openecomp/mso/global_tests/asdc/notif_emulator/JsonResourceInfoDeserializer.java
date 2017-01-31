package org.openecomp.mso.global_tests.asdc.notif_emulator;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

public class JsonResourceInfoDeserializer extends JsonDeserializer<List<JsonResourceInfo>>{

	@Override
	public List<JsonResourceInfo> deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		List<JsonResourceInfo> jsonResourceInfoList =  new ObjectMapper().readValue(jp, new TypeReference<List<JsonResourceInfo>>(){}); 
		
		return jsonResourceInfoList;
	}

}
