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
package org.openecomp.mso.adapters.json;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom JSON Deserializer for Map<String, String>.
 * In MSO with Jackson 1.9.12 and RestEasy 3.0.8, maps in JSON are serialized as
 * follows:
 * <pre>
 * "params": {
 *   "entry": [
 *     {"key": "P1", "value": "V1"},
 *     {"key": "P2", "value": "V2"},
 *     ...
 *     {"key": "PN", "value": "VN"}
 *   ]
 * }
 * The implementation uses a LinkedHashMap to preserve the ordering of entries.
 * </pre>
 */
public class MapDeserializer extends JsonDeserializer<Map<String, String>> {

	@Override
	public Map<String, String> deserialize(JsonParser parser,
			DeserializationContext context) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode tree = mapper.readTree(parser);
		Map<String, String> map = new LinkedHashMap<>();
		if (tree == null ) return map;
		Iterator<JsonNode> iterator = tree.iterator();
		while (iterator.hasNext()) {
			JsonNode element = iterator.next();
			Iterator<JsonNode> arrayIterator = element.iterator();
			while (arrayIterator.hasNext()) {
				JsonNode arrayElement = arrayIterator.next();
				String key = arrayElement.get("key").getTextValue();
				String value = arrayElement.get("value").getTextValue();
				map.put(key, value);
			}
		}
		return map;
	}
}
