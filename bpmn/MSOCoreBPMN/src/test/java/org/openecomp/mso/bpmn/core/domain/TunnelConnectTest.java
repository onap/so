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
package org.openecomp.mso.bpmn.core.domain;

import static org.junit.Assert.*;

import org.junit.Test;

public class TunnelConnectTest {
	private TunnelConnect tc= new TunnelConnect();

	@Test
	public void testTunnelConnect() {
		tc.setId("id");
		tc.setUpBandwidth("upBandwidth");
		tc.setDownBandwidth("downBandwidth");
		tc.setUpBandwidth2("upBandwidth2");
		tc.setDownBandwidth2("downBandwidth2");
		assertEquals(tc.getId(), "id");
		assertEquals(tc.getUpBandwidth(), "upBandwidth");
		assertEquals(tc.getDownBandwidth(), "downBandwidth");
		assertEquals(tc.getUpBandwidth2(), "upBandwidth2");
		assertEquals(tc.getDownBandwidth2(), "downBandwidth2");
		
	}

}
