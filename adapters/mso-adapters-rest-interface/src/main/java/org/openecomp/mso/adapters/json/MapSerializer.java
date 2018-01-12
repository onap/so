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

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;
import java.util.Map;

/**
 * Custom JSON Serializer for Map<String, String>.
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
 * </pre>
 * The implementation uses a TreeMap, so entries are always sorted according
 * to the natural ordering of the keys.
 */
public class MapSerializer extends JsonSerializer<Map<String, String>> {
	@Override
	public void serialize(Map<String, String> map, JsonGenerator jsonGenerator,
			SerializerProvider serializerProvider) throws IOException {
		jsonGenerator.writeStartObject();
		jsonGenerator.writeArrayFieldStart("entry");
		for (Map.Entry<String,String> entry : map.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringField("key", key);
			jsonGenerator.writeStringField("value", value);
			jsonGenerator.writeEndObject();
		}
		jsonGenerator.writeEndArray();
		jsonGenerator.writeEndObject();
	}
}
