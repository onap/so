package org.openecomp.mso.asdc.client.test.emulators;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

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
