/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.openecomp.mso.apihandlerinfra.taskbeans;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openecomp.mso.apihandlerinfra.tasksbeans.TaskVariableValue;

public class TaskVariableValueTest {
	TaskVariableValue _taskVariableValue;
	protected String _name;
	protected String _value;
	protected String _operator;

	public TaskVariableValueTest() {
	}

	@Before
	public void setUp() {
		_taskVariableValue = mock(TaskVariableValue.class);
		_name = "name";
		_value = "value";
		_operator = "operator";
		when(_taskVariableValue.getName()).thenReturn(_name);
		when(_taskVariableValue.getValue()).thenReturn(_value);
		when(_taskVariableValue.getOperator()).thenReturn(_operator);
	}

	@After
	public void tearDown() {
		_taskVariableValue = null;
	}

	/**
	 * Test of getName method
	 */
	@Test
	public void testGetName() {
		_taskVariableValue.setName(_name);
		assertEquals(_taskVariableValue.getName(),_name);

	}

	/**
	 * Test setName
	 */
	@Test
	public void testSetName() {
		_taskVariableValue.setName(_name);
		verify(_taskVariableValue).setName(_name);
	}
	
	/**
	 * Test of getName method
	 */
	@Test
	public void testGetValue() {
		_taskVariableValue.setValue(_value);
		assertEquals(_taskVariableValue.getValue(),_value);

	}

	/**
	 * Test setName
	 */
	@Test
	public void testSetValue() {
		_taskVariableValue.setValue(_value);
		verify(_taskVariableValue).setValue(_value);
	}
	
	/**
	 * Test of getName method
	 */
	@Test
	public void testGetOperator() {
		_taskVariableValue.setOperator(_operator);
		assertEquals(_taskVariableValue.getOperator(),_operator);

	}

	/**
	 * Test setName
	 */
	@Test
	public void testSetRequestDetails() {
		_taskVariableValue.setOperator(_operator);
		verify(_taskVariableValue).setOperator(_operator);
	}

}
