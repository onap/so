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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * An ObjectMapper for a specific class that ignores the root element
 * if it is present.
 */
public class RootIgnoringObjectMapper<T> extends ObjectMapper {

	private static final long serialVersionUID = 6812584067195377395L;

	public RootIgnoringObjectMapper(Class<T> clazz) {
		SimpleModule module = new SimpleModule();
		module.addDeserializer(clazz, new RootIgnoringJsonDeserializer<T>(clazz));
		registerModule(module);
	}
}