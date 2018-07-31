/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.aai;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class EmptyStringToNullSerializer extends StdSerializer<String> {

	private static final long serialVersionUID = 5367385969270400106L;

	public EmptyStringToNullSerializer() {
		this(null);
	}
	public EmptyStringToNullSerializer(Class<String> t) {
		super(t);
	}

	@Override
	public void serialize(String value, JsonGenerator gen, SerializerProvider serializers)
			throws IOException, JsonProcessingException {

		if("".equals(value)) {
			gen.writeNull();
		} else {
			gen.writeString(value);
		}
	}

}
