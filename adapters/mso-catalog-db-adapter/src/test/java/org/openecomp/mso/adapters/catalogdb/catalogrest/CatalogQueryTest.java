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

package org.openecomp.mso.adapters.catalogdb.catalogrest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class CatalogQueryTest {
	private static final String MAP_KEY = "keyTest";
	private static final String VALUE_MAP = "valueTest";
	private CatalogQuery testedObject;

	@Before
	public void init() {
		testedObject = new CatalogQueryForTesting();
	}

	@Test
	public void putStringValueToMap() {
		Map<String, String> valueMap = new HashMap<>();
		testedObject.put(valueMap, MAP_KEY, VALUE_MAP);
		assertThat(valueMap).hasSize(1).containsEntry(MAP_KEY, "\"valueTest\"");
	}

	@Test
	public void putNullStringValueToMap() {
		Map<String, String> valueMap = new HashMap<>();
		String value = null;
		testedObject.put(valueMap, MAP_KEY, value);
		assertThat(valueMap).hasSize(1).containsEntry(MAP_KEY, "null");
	}

	@Test
	public void putIntegerValueToMap() {
		Map<String, String> valueMap = new HashMap<>();
		testedObject.put(valueMap, MAP_KEY, 1);
		assertThat(valueMap).hasSize(1).containsEntry(MAP_KEY, "1");
	}

	@Test
	public void putNullIntegerValueToMap() {
		Map<String, String> valueMap = new HashMap<>();
		Integer value = null;
		testedObject.put(valueMap, MAP_KEY, value);
		assertThat(valueMap).hasSize(1).containsEntry(MAP_KEY, "null");
	}

	@Test
	public void putTrueBooleanValueToMap() {
		Map<String, String> valueMap = new HashMap<>();
		testedObject.put(valueMap, MAP_KEY, true);
		assertThat(valueMap).hasSize(1).containsEntry(MAP_KEY, "true");
	}

	@Test
	public void putFalseBooleanValueToMap() {
		Map<String, String> valueMap = new HashMap<>();
		testedObject.put(valueMap, MAP_KEY, false);
		assertThat(valueMap).hasSize(1).containsEntry(MAP_KEY, "false");
	}

	@Test
	public void putNullBooleanValueToMap() {
		Map<String, String> valueMap = new HashMap<>();
		Boolean value = null;
		testedObject.put(valueMap, MAP_KEY, value);
		assertThat(valueMap).hasSize(1).containsEntry(MAP_KEY, "null");
	}

	@Test
	public void setTemplate_keyFindInMap() {
		Map<String, String> valueMap = new HashMap<>();
		valueMap.put(MAP_KEY, VALUE_MAP);
		String template = "<keyTest>";
		String result = testedObject.setTemplate(template, valueMap);
		assertThat(result).isEqualTo(VALUE_MAP);
	}

	@Test
	public void setTemplate_keyNotFindInMap() {
		Map<String, String> valueMap = new HashMap<>();
		String template = "<keyTest>";
		String result = testedObject.setTemplate(template, valueMap);
		assertThat(result).isEqualTo("\"TBD\"");
	}

	@Test
	public void setTemplate_templateDoesNotMatch() {
		Map<String, String> valueMap = new HashMap<>();
		String template = "key";
		String result = testedObject.setTemplate(template, valueMap);
		assertThat(result).isEqualTo("key");
	}

	@Test
	public void smartToJson(){
		String expectedResult = "{\"s\":\"s1\"}";
		assertThat(testedObject.smartToJSON()).isEqualTo(expectedResult);
	}

	@Test
	public void toJsonString_withVersion1() {
		String expectedResult = "{\"s\":\"s1\"}";
		assertThat(testedObject.toJsonString("v1",true)).isEqualTo(expectedResult);
	}

	@Test
	public void toJsonString_withVersion2() {
		assertThat(testedObject.toJsonString("v2",true)).isEqualTo("json2");
	}

	@Test
	public void toJsonString_withInvalidVersion() {
		assertThat(testedObject.toJsonString("ver77",true)).isEqualTo("invalid version: ver77");
	}

	private class CatalogQueryForTesting extends CatalogQuery {

		private String s = "s1";

		public String getS() {
			return s;
		}

		@Override
		public String JSON2(boolean isArray, boolean isEmbed) {
			return "json2";
		}
	}

}


