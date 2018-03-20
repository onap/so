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
package org.openecomp.mso.client.sdnc.beans;

import static org.junit.Assert.*;



import org.junit.Test;
public class SDNCRequestTest {
	private SDNCRequest sdncrequest = new SDNCRequest();
	SDNCSvcAction svcaction = SDNCSvcAction.ACTIVATE;
	SDNCSvcOperation svcoperation = SDNCSvcOperation.VNF_TOPOLOGY_OPERATION; 
	@Test
	public void testSDNCRequestTest() {
		sdncrequest.setRequestId("requestId");
		sdncrequest.setSvcInstanceId("svcInstanceId");
		sdncrequest.setSvcAction(svcaction);
		sdncrequest.setSvcOperation(svcoperation);
		sdncrequest.setCallbackUrl("callbackUrl");
		sdncrequest.setMsoAction("msoAction");
		sdncrequest.setRequestData("requestData");
		assertEquals(sdncrequest.getRequestId(), "requestId");
		assertEquals(sdncrequest.getSvcInstanceId(), "svcInstanceId");
		assertEquals(sdncrequest.getSvcAction(), svcaction);
		assertEquals(sdncrequest.getSvcOperation(), svcoperation);
		assertEquals(sdncrequest.getCallbackUrl(), "callbackUrl");
		assertEquals(sdncrequest.getMsoAction(), "msoAction");
		assertEquals(sdncrequest.getRequestData(), "requestData");
		}
	@Test
	public void testToString(){
		assert(sdncrequest.toString()!=null);
	}

}
