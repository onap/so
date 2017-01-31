package org.openecomp.mso.global_tests.asdc.notif_emulator;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

public class JsonArtifactInfoDeserializer extends JsonDeserializer<List<JsonArtifactInfo>>{

	@Override
	public List<JsonArtifactInfo> deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		List<JsonArtifactInfo> jsonArtifactInfoList =  new ObjectMapper().readValue(jp, new TypeReference<List<JsonArtifactInfo>>(){}); 

		// For each artifact add the list of artifact retrieved 
		// This could be used later to index by UUID
		for (JsonArtifactInfo artifactInfo:jsonArtifactInfoList) {
			artifactInfo.addArtifactToUUIDMap(jsonArtifactInfoList);
		}
		return jsonArtifactInfoList;
	}

}
