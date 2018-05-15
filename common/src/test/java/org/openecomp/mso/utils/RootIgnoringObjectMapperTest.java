/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *	  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.mso.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RootIgnoringObjectMapperTest {

	@Test
	public void someObjectWithoutRootTest() throws Exception {
		ObjectMapper mapper = new RootIgnoringObjectMapper<SomeObject>(SomeObject.class);

		String content = "{"
			+ "\"attribute\":\"charm\""
			+ "}";

		SomeObject response = mapper.readValue(content, SomeObject.class);
		assertEquals("SomeObject[attribute=charm]", response.toString());
	}
	
	@Test
	public void someObjectWithRootTest() throws Exception {
		ObjectMapper mapper = new RootIgnoringObjectMapper<SomeObject>(SomeObject.class);

		String content = "{\"SomeObject\":{"
			+ "\"attribute\":\"charm\""
			+ "}}";

		SomeObject response = mapper.readValue(content, SomeObject.class);
		assertEquals("SomeObject[attribute=charm]", response.toString());
	}
	
	@Test
	public void annotatedObjectWithoutRootTest() throws Exception {
		ObjectMapper mapper = new RootIgnoringObjectMapper<AnnotatedObject>(AnnotatedObject.class);

		String content = "{"
			+ "\"attribute\":\"charm\""
			+ "}";

		AnnotatedObject response = mapper.readValue(content, AnnotatedObject.class);
		assertEquals("AnnotatedObject[attribute=charm]", response.toString());
	}
	
	@Test
	public void annotatedObjectWithRootTest() throws Exception {
		ObjectMapper mapper = new RootIgnoringObjectMapper<AnnotatedObject>(AnnotatedObject.class);

		String content = "{\"annotated-object\":{"
			+ "\"attribute\":\"charm\""
			+ "}}";

		AnnotatedObject response = mapper.readValue(content, AnnotatedObject.class);
		assertEquals("AnnotatedObject[attribute=charm]", response.toString());
	}

	public static class SomeObject {

		@JsonProperty("attribute")
		private String attribute;

		public String toString() {
			return getClass().getSimpleName() + "[attribute=" + attribute + "]";
		}
	}

   	@JsonRootName(value = "annotated-object")
	public static class AnnotatedObject extends SomeObject {
	}
}