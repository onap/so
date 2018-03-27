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
import org.openecomp.mso.properties.MsoPropertiesFactory;

public class RequestTunablesTest {
	MsoPropertiesFactory mpf = new MsoPropertiesFactory();
	
	RequestTunables rt = new RequestTunables("reqId", "msoAction", "operation", "action", mpf);

	@Test
	public void testRequestTunables() {
		rt.setReqId("reqId");
		rt.setReqMethod("reqMethod");
		rt.setMsoAction("msoAction");
		rt.setAction("action");
		rt.setOperation("operation");
		rt.setSdncUrl("sdncUrl");
		rt.setTimeout("timeout");
		rt.setAsyncInd("asyncInd");
		rt.setHeaderName("headerName");
		rt.setSdncaNotificationUrl("sdncaNotificationUrl");
		rt.setNamespace("namespace");
		assertEquals(rt.getReqId(), "reqId");
		assertEquals(rt.getReqMethod(), "reqMethod");
		assertEquals(rt.getMsoAction(), "msoAction");
		assertEquals(rt.getAction(), "action");
		assertEquals(rt.getOperation(), "operation");
		assertEquals(rt.getSdncUrl(), "sdncUrl");
		assertEquals(rt.getTimeout(), "timeout");
		assertEquals(rt.getAsyncInd(), "asyncInd");
		assertEquals(rt.getHeaderName(), "headerName");
		assertEquals(rt.getSdncaNotificationUrl(), "sdncaNotificationUrl");
		assertEquals(rt.getNamespace(), "namespace");
		assert(rt.toString()!=null);	
	}
}
