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
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openecomp.mso.apihandlerinfra.tasksbeans.RequestDetails;
import org.openecomp.mso.apihandlerinfra.tasksbeans.TasksRequest;


public class TasksRequestTest {
	TasksRequest _tasksRequest;
	private RequestDetails _requestDetails;

	public TasksRequestTest() {
	}

	@Before
	public void setUp() {
		_tasksRequest = mock(TasksRequest.class);
		_requestDetails = new RequestDetails();
		when(_tasksRequest.getRequestDetails()).thenReturn(_requestDetails);
	}

	@After
	public void tearDown() {
		_tasksRequest = null;
	}

	/**
	 * Test of getRequestDetails method
	 */
	@Test
	public void testGetRequestDetails() {
		_tasksRequest.setRequestDetails(_requestDetails);
		assertTrue(_tasksRequest.getRequestDetails().equals(_requestDetails));

	}

	/**
	 * Test setRequestDetails
	 */
	@Test
	public void testSetRequestDetails() {
		_tasksRequest.setRequestDetails(_requestDetails);
		verify(_tasksRequest).setRequestDetails(_requestDetails);
	}

}
