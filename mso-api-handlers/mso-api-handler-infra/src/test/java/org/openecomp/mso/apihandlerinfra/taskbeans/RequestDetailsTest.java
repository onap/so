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
import org.openecomp.mso.apihandlerinfra.tasksbeans.RequestInfo;



public class RequestDetailsTest {

	RequestDetails _requestDetails;
	RequestInfo _requestInfo;

	public RequestDetailsTest() {
	}

	@Before
	public void setUp() {
		_requestDetails = mock(RequestDetails.class);
		_requestInfo = new RequestInfo();
		when(_requestDetails.getRequestInfo()).thenReturn(_requestInfo);
	}

	@After
	public void tearDown() {
		_requestDetails = null;
		_requestInfo = null;
	}

	/**
	 * Test of getRequestInfo method
	 */
	@Test
	public void testGetRequestInfo() {
		_requestDetails.setRequestInfo(_requestInfo);
		assertTrue(_requestDetails.getRequestInfo().equals(_requestInfo));

	}

	/**
	 * Test setRequestInfo
	 */
	@Test
	public void testSetRequestInfo() {
		_requestDetails.setRequestInfo(_requestInfo);
		verify(_requestDetails).setRequestInfo(_requestInfo);
	}
}
