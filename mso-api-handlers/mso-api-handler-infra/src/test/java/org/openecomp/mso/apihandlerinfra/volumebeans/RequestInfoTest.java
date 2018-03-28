/*
* ============LICENSE_START=======================================================
* ONAP : SO
* ================================================================================
* Copyright 2018 TechMahindra
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/
package org.openecomp.mso.apihandlerinfra.volumebeans;

import static org.junit.Assert.*;

import org.junit.Test;

public class RequestInfoTest {
	RequestInfo ri = new RequestInfo();
	ActionType actiontype = ActionType.CREATE;
	RequestStatusType requestStatustype = RequestStatusType.COMPLETE;
	int abc;
	@Test
	public void testRequestInfo() {
		ri.setRequestId("requestId");
		ri.setAction(actiontype);
		ri.setRequestStatus(requestStatustype);
		ri.setStatusMessage("statusMessage");
		ri.setProgress(abc);
		ri.setStartTime("startTime");
		ri.setEndTime("endTime");
		ri.setSource("source");
		assertEquals(ri.getRequestId(), "requestId");
		assertEquals(ri.getAction(), actiontype);
		assertEquals(ri.getRequestStatus(), requestStatustype);
		assertEquals(ri.getStatusMessage(), "statusMessage");
		assert(ri.getProgress().equals(abc));
		assertEquals(ri.getStartTime(), "startTime");
		assertEquals(ri.getEndTime(), "endTime");
		assertEquals(ri.getSource(), "source");	
	}
}
