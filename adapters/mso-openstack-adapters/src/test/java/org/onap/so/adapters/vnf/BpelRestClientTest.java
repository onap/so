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

package org.onap.so.adapters.vnf;

import static org.junit.Assert.assertEquals;

import javax.inject.Provider;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.server.LocalServerPort;

public class BpelRestClientTest extends BaseRestTestUtils{

	
	@LocalServerPort
	private int port;
	@Autowired
	private Provider<BpelRestClient> clientProvider;

	@Test
	public void verifyPropertiesRead() {
		BpelRestClient client = clientProvider.get();
		
		assertEquals(5, client.getRetryCount());
		assertEquals(30, client.getConnectTimeout());
		assertEquals("test:test", client.getCredentials());
		assertEquals(30, client.getSocketTimeout());
		assertEquals("408, 429, 500, 502, 503, 504, 900", client.getRetryList());
		assertEquals(-15, client.getRetryInterval());
		
	}

}
