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
package org.openecomp.mso.client.sdnc.sync;

import static org.junit.Assert.*;

import org.junit.Test;

public class SDNCResponseTest {
	
	SDNCResponse sdnc = new SDNCResponse("reqId");
	SDNCResponse sdnc1 = new SDNCResponse("reqId", 0, "respMsg");

	@Test
	public void testSDNCResponse() {
		sdnc.setReqId("reqId");
		sdnc.setRespCode(0);
		sdnc.setRespMsg("respMsg");
		sdnc.setSdncResp("sdncResp");
		assertEquals(sdnc.getReqId(), "reqId");
		assertEquals(sdnc.getRespCode(), 0);
		assertEquals(sdnc.getRespMsg(), "respMsg");
		assertEquals(sdnc.getSdncResp(), "sdncResp");
		assert(sdnc.toString()!= null);	
	}
}
