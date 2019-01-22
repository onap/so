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

package org.onap.so.bpmn.common;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.so.bpmn.common.exceptions.MalformedBuildingBlockInputException;
import org.onap.so.bpmn.common.exceptions.MissingBuildingBlockInputException;
import org.onap.so.bpmn.common.exceptions.RequiredExecutionVariableExeception;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;

public class DelegateExecutionImplTest {

	@Rule
	public ExpectedException thrown= ExpectedException.none();

	
	@Test
	public void getVariable() throws RequiredExecutionVariableExeception {
		Map<String, Serializable> map = new HashMap<>();
		map.put("var1", "value1");
		map.put("var2", "value2");
		map.put("list1", (Serializable)Arrays.asList("value1", "value2"));
		DelegateExecutionImpl impl = create(map);
		
		assertEquals("value1", impl.getVariable("var1"));
		assertEquals("value2", impl.getRequiredVariable("var2"));
		assertThat(impl.getVariable("list1"), IsIterableContainingInOrder.contains("value1", "value2"));

	}
	
	
	@Test
	public void getRequiredVariableNotFound() throws RequiredExecutionVariableExeception {
		DelegateExecutionImpl impl = create();
		
		thrown.expect(RequiredExecutionVariableExeception.class);
		impl.getRequiredVariable("var1");
	}
	
	
	@Test
	public void setVariable() {
		DelegateExecutionImpl impl = create();
		impl.setVariable("var1", "value1");
		
		assertEquals("value1", impl.get("var1"));
	}
	
	@Test
	public void getGeneralBuildingBlock() {
		GeneralBuildingBlock gBB = mock(GeneralBuildingBlock.class);
		Map<String, Serializable> map = new HashMap<>();
		map.put("gBBInput", gBB);
		DelegateExecutionImpl impl = create(map);
		
		assertEquals(gBB, impl.getGeneralBuildingBlock());
	}
	
	@Test
	public void getGeneralBuildingBlockNotFound() {

		DelegateExecutionImpl impl = create();
		
		thrown.expect(MissingBuildingBlockInputException.class);
		impl.getGeneralBuildingBlock();
	}
	
	@Test
	public void getGeneralBuildingBlockCastException() {
		Map<String, Serializable> map = new HashMap<>();
		map.put("gBBInput", new DelegateExecutionFake());
		DelegateExecutionImpl impl = create(map);
		
		thrown.expect(MalformedBuildingBlockInputException.class);
		impl.getGeneralBuildingBlock();
	}
	
	@Test
	public void getDelegateExecution() {
		DelegateExecutionImpl impl = create();

		assertNotNull(impl.getDelegateExecution());
	}
	
	@Test
	public void getLookupMap() {
		Map<String, Serializable> lookup = new HashMap<>();
		Map<String, Serializable> map = new HashMap<>();
		map.put("lookupKeyMap", (Serializable) lookup);
		DelegateExecutionImpl impl = create(map);
		
		assertEquals(lookup, impl.getLookupMap());
	}
	
	private DelegateExecutionImpl create() {
		return create(new HashMap<String, Serializable>());
	}
	
	private DelegateExecutionImpl create(Map<String, Serializable> map) {
		DelegateExecutionFake fake = new DelegateExecutionFake();
		
		for (Entry<String, Serializable> entry : map.entrySet()) {
			fake.setVariable(entry.getKey(), entry.getValue());
		}
		return new DelegateExecutionImpl(fake);
	}

}
