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

package org.openecomp.mso.client.aai.entities;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aai.domain.yang.GenericVnf;
import org.openecomp.mso.client.aai.AAICommonObjectMapperProvider;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class) 
public class AAIResultWrapperTest {
	String json;

	AAIResultWrapper aaiResultWrapper;
	AAIResultWrapper aaiResultWrapperEmpty;
	
	@Before
	public void init() throws IOException {
		final String RESOURCE_PATH = "src/test/resources/__files/aai/resources/";
		json = new String(Files.readAllBytes(Paths.get(RESOURCE_PATH + "e2e-complex.json")));
		
		aaiResultWrapper = new AAIResultWrapper(json);
		aaiResultWrapperEmpty = new AAIResultWrapper("{}");
	}
	
	@Test
	public void testGetRelationshipsEmpty() {
		Optional<Relationships> relationships = aaiResultWrapperEmpty.getRelationships();
		assertEquals("Compare relationships", Optional.empty(), relationships);
	}

	@Test
	public void testAsMap() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new AAICommonObjectMapperProvider().getMapper();
		Map<String, Object> expected = mapper.readValue(json, new TypeReference<Map<String, Object>>(){});
		
		Map<String, Object> actual = aaiResultWrapper.asMap();
		assertEquals(expected, actual);
	}
	
	@Test
	public void testAsMapEmpty() {
		Map<String, Object> actual = aaiResultWrapperEmpty.asMap();
		assertEquals(new HashMap<>(), actual);
	}
	
	@Test
	public void nullCases() {
		
		AAIResultWrapper wrapper = new AAIResultWrapper(null);
		
		assertEquals(Optional.empty(), wrapper.getRelationships());
		assertEquals("{}", wrapper.getJson());
		assertEquals(Optional.empty(), wrapper.asBean(GenericVnf.class));
		assertEquals(true, wrapper.asMap().isEmpty());
		assertEquals("{}", wrapper.toString());


		
	}
}
