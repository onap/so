/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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


package org.openecomp.mso.adapters.sdnc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.mso.adapters.sdnc.SDNCAdapterRequest;
import org.openecomp.mso.adapters.sdnc.RequestHeader;


public class SDNCAdapterRequestTest {

	static Object sd= new SDNCAdapterRequest();
	static RequestHeader rh=new RequestHeader();
	
	@BeforeClass
	public static final void RHeader()
	{
		rh.setCallbackUrl("callback");
		rh.setMsoAction ("action");
		rh.setRequestId ("reqid");
		rh.setSvcAction ("svcAction");
		rh.setSvcInstanceId ("svcId");
		rh.setSvcOperation ("op");
	}
	@Test
	public final void testtoString(){
		((SDNCAdapterRequest) sd).setRequestData("data");
		((SDNCAdapterRequest) sd).setRequestHeader(rh);
        assertNotNull(((SDNCAdapterRequest) sd).getRequestData()) ;
        assertEquals("data", ((SDNCAdapterRequest) sd).getRequestData());
        assertEquals(rh, ((SDNCAdapterRequest) sd).getRequestHeader());
	}

}
