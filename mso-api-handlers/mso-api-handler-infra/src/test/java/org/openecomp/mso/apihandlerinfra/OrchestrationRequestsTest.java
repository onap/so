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

package org.openecomp.mso.apihandlerinfra;

import static org.junit.Assert.assertTrue;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.mockito.Mockito;

public class OrchestrationRequestsTest {

	OrchestrationRequests req = new OrchestrationRequests();
	
	@Test
	public void getOrchestrationRequestTest(){
		Response resp = req.getOrchestrationRequest("1001", "v3");
		assertTrue(resp.getEntity().toString() != null);
	}
	
	@Test
	public void getE2EServiceInstancesTest(){
		Response resp = req.getE2EServiceInstances("1001", "v3","599934");
		assertTrue(resp.getEntity().toString() != null);
	}
	
	@Test
	public void getOrchestrationRequest2Test(){
		UriInfo uriInfo = Mockito.mock(UriInfo.class);
		Response resp = req.getOrchestrationRequest(uriInfo, "v3");
		assertTrue(resp.getEntity().toString() != null);
	}
	
	@Test
	public void unlockOrchestrationRequestTest(){
		Response resp = req.unlockOrchestrationRequest("{\"result\":\"success\"}","1001", "v3");
		assertTrue(resp.getEntity().toString() != null);
	}
}
