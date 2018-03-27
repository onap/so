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

public class SDNCAdapterRequestTest {
	
	SDNCAdapterRequest adapter = new SDNCAdapterRequest();
	RequestHeader rh = new RequestHeader();
	Object o = new Object();

	@Test
	public void testSDNCAdapterRequest() {
		adapter.setRequestHeader(rh);
		adapter.setRequestData(o);
		assertEquals(adapter.getRequestHeader(), rh);
		assertEquals(adapter.getRequestData(), o);
		}
}
