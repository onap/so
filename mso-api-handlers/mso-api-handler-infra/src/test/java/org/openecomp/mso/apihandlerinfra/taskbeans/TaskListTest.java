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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.json.JSONArray;
import org.openecomp.mso.apihandlerinfra.tasksbeans.TaskList;

public class TaskListTest {

	TaskList _taskList;
	protected String _taskId;
	protected String _type;
	protected String _nfRole;
	protected String _subscriptionServiceType;
	protected String _originalRequestId;
	protected String _originalRequestorId;
	protected String _errorSource;
	protected String _errorCode;
	protected String _errorMessage;
	protected String _buildingBlockName;
	protected String _buildingBlockStep;
	protected JSONArray _validResponses;

	public TaskListTest() {
	}

	@Before
	public void setUp() {
		_taskList = mock(TaskList.class);
		_taskId = "_taskid";
		_type = "type";
		_nfRole = "nfrole";
		_subscriptionServiceType = "subscriptionservicetype";
		_originalRequestId = "originalrequestid";
		_originalRequestorId = "originalrequestorid";
		_errorSource = "errorsource";
		_errorCode = "errorcode";
		_errorMessage = "errormessage";
		_buildingBlockName = "buildingblockname";
		_buildingBlockStep = "buildingblockstep";
		_validResponses = mock(JSONArray.class);

		when(_taskList.getTaskId()).thenReturn(_taskId);
		when(_taskList.getType()).thenReturn(_type);
		when(_taskList.getNfRole()).thenReturn(_nfRole);
		when(_taskList.getSubscriptionServiceType()).thenReturn(_subscriptionServiceType);
		when(_taskList.getOriginalRequestId()).thenReturn(_originalRequestId);
		when(_taskList.getOriginalRequestorId()).thenReturn(_originalRequestorId);
		when(_taskList.getErrorSource()).thenReturn(_errorSource);
		when(_taskList.getErrorCode()).thenReturn(_errorCode);
		when(_taskList.getErrorMessage()).thenReturn(_errorMessage);
		when(_taskList.getBuildingBlockName()).thenReturn(_buildingBlockName);
		when(_taskList.getBuildingBlockStep()).thenReturn(_buildingBlockStep);
		when(_taskList.getValidResponses()).thenReturn(_validResponses);
	}

	@After
	public void tearDown() {
		_taskList = null;
		_validResponses = null;
	}

	@Test
	public void testGetTaskId() {
		String result = _taskList.getTaskId();
		assertEquals(_taskId, result);

	}

	@Test
	public void testSetTaskId() {
		_taskList.setTaskId("_taskid");
		verify(_taskList).setTaskId(_taskId);
	}

	@Test
	public void testGetType() {
		String result = _taskList.getType();
		assertEquals(_type, result);

	}

	@Test
	public void testSetType() {
		_taskList.setType(_type);
		verify(_taskList).setType(_type);
	}

	@Test
	public void testGetNfRole() {
		String result = _taskList.getNfRole();
		assertEquals(_nfRole, result);

	}

	@Test
	public void testSetNfRole() {
		_taskList.setType(_nfRole);
		verify(_taskList).setType(_nfRole);
	}

	@Test
	public void testGetSubscriptionServiceType() {
		String result = _taskList.getSubscriptionServiceType();
		assertEquals(_subscriptionServiceType, result);

	}

	@Test
	public void testSetSubscriptionServiceType() {
		_taskList.setSubscriptionServiceType(_subscriptionServiceType);
		verify(_taskList).setSubscriptionServiceType(_subscriptionServiceType);
	}

	@Test
	public void testGetOriginalRequestId() {
		String result = _taskList.getOriginalRequestId();
		assertEquals(_originalRequestId, result);

	}

	@Test
	public void testSetOriginalRequestId() {
		_taskList.setOriginalRequestId(_originalRequestId);
		verify(_taskList).setOriginalRequestId(_originalRequestId);
	}

	@Test
	public void testGetOriginalRequestorId() {
		String result = _taskList.getOriginalRequestorId();
		assertEquals(_originalRequestorId, result);

	}

	@Test
	public void testSetOriginalRequestorId() {
		_taskList.setOriginalRequestorId(_originalRequestorId);
		verify(_taskList).setOriginalRequestorId(_originalRequestorId);
	}

	@Test
	public void testGetErrorSource() {
		String result = _taskList.getErrorSource();
		assertEquals(_errorSource, result);

	}

	@Test
	public void testSetErrorSource() {
		_taskList.setErrorSource(_errorSource);
		verify(_taskList).setErrorSource(_errorSource);
	}

	@Test
	public void testGetErrorCode() {
		String result = _taskList.getErrorCode();
		assertEquals(_errorCode, result);

	}

	@Test
	public void testSetErrorCode() {
		_taskList.setErrorCode(_errorCode);
		verify(_taskList).setErrorCode(_errorCode);
	}

	@Test
	public void testGetErrorMessage() {
		String result = _taskList.getErrorMessage();
		assertEquals(_errorMessage, result);

	}

	@Test
	public void testSetErrorMessage() {
		_taskList.setErrorMessage(_errorMessage);
		verify(_taskList).setErrorMessage(_errorMessage);
	}

	@Test
	public void testGetBuildingBlockName() {
		String result = _taskList.getBuildingBlockName();
		assertEquals(_buildingBlockName, result);

	}

	@Test
	public void testSetBuildingBlockName() {
		_taskList.setBuildingBlockName(_buildingBlockName);
		verify(_taskList).setBuildingBlockName(_buildingBlockName);
	}

	@Test
	public void testGetBuildingBlockStep() {
		String result = _taskList.getBuildingBlockStep();
		assertEquals(_buildingBlockStep, result);

	}

	@Test
	public void testSetBuildingBlockStep() {
		_taskList.setBuildingBlockStep(_buildingBlockStep);
		verify(_taskList).setBuildingBlockStep(_buildingBlockStep);
	}

	@Test
	public void testGetValidResponses() {

		JSONArray result = _taskList.getValidResponses();
		assertEquals(_validResponses, result);

	}
	
	@Test
	public void testSetValidResponses() {
		_taskList.setValidResponses(_validResponses);
		verify(_taskList).setValidResponses(_validResponses);
	}


}
