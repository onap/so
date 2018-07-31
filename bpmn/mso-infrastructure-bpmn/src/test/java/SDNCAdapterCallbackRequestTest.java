/*
* ============LICENSE_START=======================================================
 * ONAP : SO
 * ================================================================================
 * Copyright (C) 2018 TechMahindra
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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.onap.so.BaseTest;
import org.onap.so.bpmn.common.adapter.sdnc.CallbackHeader;
import org.onap.so.bpmn.common.adapter.sdnc.SDNCAdapterCallbackRequest;

public class SDNCAdapterCallbackRequestTest{
	
	SDNCAdapterCallbackRequest sdnccall = new SDNCAdapterCallbackRequest();
	CallbackHeader cbh = new CallbackHeader();
	String o = "test";

	@Test
	public void testSDNCAdapterCallbackRequest() {
		sdnccall.setCallbackHeader(cbh);
		sdnccall.setRequestData(o);
		assertEquals(sdnccall.getCallbackHeader(), cbh);
		assertEquals(sdnccall.getRequestData(), o);
		assertNotNull(sdnccall.toString());	
	}

}
