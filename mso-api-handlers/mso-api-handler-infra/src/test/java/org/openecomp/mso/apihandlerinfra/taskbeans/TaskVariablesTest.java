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

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.mso.apihandlerinfra.tasksbeans.TaskVariableValue;
import org.openecomp.mso.apihandlerinfra.tasksbeans.TaskVariables;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

public class TaskVariablesTest {

	TaskVariables _taskVariables;
	private List<TaskVariableValue> _taskVariableValueList;

	public TaskVariablesTest() {
	}

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		_taskVariables = mock(TaskVariables.class);
		_taskVariableValueList  = mock(List.class);
		when(_taskVariables.getTaskVariables()).thenReturn(_taskVariableValueList);
	}

	@After
	public void tearDown() {
		_taskVariables = null;
	}

	@Test
	public void testGetTaskVariables() {
		List<TaskVariableValue> result = _taskVariables.getTaskVariables();
		assertEquals(_taskVariableValueList, result);

	}

	@Test
	public void testSetTaskVariables() {
		_taskVariables.setTaskVariables(_taskVariableValueList);
		verify(_taskVariables).setTaskVariables(_taskVariableValueList);

	}
}
