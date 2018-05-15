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

package org.openecomp.mso.utils;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A JSON deserializer that ignores the root element if it is present.
 */
public class RootIgnoringJsonDeserializer<T> extends JsonDeserializer<T> {

	private final ObjectMapper mapper = new ObjectMapper();
	private final Class<T> clazz;
	private final String jsonRootName;

	public RootIgnoringJsonDeserializer(Class<T> clazz) {
		this.clazz = clazz;

		JsonRootName annotation = clazz.getAnnotation(JsonRootName.class);
		
		if (annotation == null || annotation.value() == null || annotation.value().equals("")) {
			jsonRootName = clazz.getSimpleName();
		} else {
			jsonRootName = annotation.value();
		}
	}

	@Override
	public T deserialize(JsonParser jp, DeserializationContext dc) 
			throws IOException, JsonProcessingException {
	   JsonNode rootNode = jp.getCodec().readTree(jp);
	   Map.Entry<String,JsonNode> field = rootNode.fields().next();

	   if (jsonRootName.equals(field.getKey())) {
		   rootNode = field.getValue();
	   }

	   return mapper.convertValue(rootNode, clazz);
	}
}