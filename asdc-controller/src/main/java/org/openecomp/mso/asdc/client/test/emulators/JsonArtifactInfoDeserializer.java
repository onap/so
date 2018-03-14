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
