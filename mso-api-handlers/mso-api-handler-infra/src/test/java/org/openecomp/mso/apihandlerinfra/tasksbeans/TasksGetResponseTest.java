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

package org.openecomp.mso.apihandlerinfra.tasksbeans;

import org.junit.After;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

public class TasksGetResponseTest {

	TasksGetResponse _tasksGetResponse;
	private List<TaskList> _taskList;

	public TasksGetResponseTest() {
	}

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		_tasksGetResponse = mock(TasksGetResponse.class);
		_taskList = mock(List.class);
		when(_tasksGetResponse.getTaskList()).thenReturn(_taskList);
	}

	@After
	public void tearDown() {
		_tasksGetResponse = null;
	}

	@Test
	public void testGetTaskList() {
		List<TaskList> result = _tasksGetResponse.getTaskList();
		assertEquals(_taskList, result);

	}

	@Test
	public void testSetTaskList() {
		_tasksGetResponse.setTaskList(_taskList);
		verify(_tasksGetResponse).setTaskList(_taskList);

	}
}
