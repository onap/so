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

package org.onap.so.client.adapter.vnf.mapper;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class VnfAdapterVfModuleObjectMapperTest{

	private VnfAdapterVfModuleObjectMapper mapper = new VnfAdapterVfModuleObjectMapper();
	
	@Test
	public void createVnfcSubInterfaceKeyTest() {
		
		assertEquals("type_0_subint_role_port_0", mapper.createVnfcSubInterfaceKey("type", 0, "role", 0));
	}
	
	@Test
	public void createGlobalVnfcSubInterfaceKeyTest() {
		
		assertEquals("type_subint_role_port_0", mapper.createGlobalVnfcSubInterfaceKey("type", "role", 0));
	}
	
	@Test
	public void addPairToMapTest() {
		Map<String, String> map = new HashMap<>();
		
		mapper.addPairToMap(map, "test", "_key", Arrays.asList("a", "b"));
		
		assertEquals("a,b", map.get("test_key"));
		
		mapper.addPairToMap(map, "test", "_key2", Arrays.asList());
		
		assertThat(map.containsKey("test_key2"), equalTo(false));
		
		mapper.addPairToMap(map, "test", "_key3", "myVal");
		
		assertEquals("myVal", map.get("test_key3"));
		
	}
	
}
