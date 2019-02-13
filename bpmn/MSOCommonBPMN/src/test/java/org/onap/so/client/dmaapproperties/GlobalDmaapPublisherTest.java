/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.dmaapproperties;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.so.BaseTest;
import org.mockito.Mockito;

public class GlobalDmaapPublisherTest extends BaseTest{

	@BeforeClass
	public static void setUp() throws Exception {
		System.setProperty("mso.global.dmaap.username", "testUser");
		System.setProperty("mso.global.dmaap.password", "testPassword");
		System.setProperty("mso.global.dmaap.publisher.topic", "com.att.mso.asyncStatusUpdate");
		System.setProperty("mso.global.dmaap.host", "http://test:1234");
	}
    
    
	@Test
	public void testGetters() {

		GlobalDmaapPublisher globalDmaapPublisher = Mockito.spy(GlobalDmaapPublisher.class);
	
		String user = globalDmaapPublisher.getUserName();
		String passWord = globalDmaapPublisher.getPassword();
		String topic= globalDmaapPublisher.getTopic();
		String host = globalDmaapPublisher.getHost().get();
		
		verify(globalDmaapPublisher, times(1)).getUserName();
		verify(globalDmaapPublisher, times(1)).getPassword();
		verify(globalDmaapPublisher, times(1)).getTopic();
		verify(globalDmaapPublisher, times(1)).getHost();
		
		assertEquals("testUser", user);
		assertEquals("testPassword", passWord);
		assertEquals("com.att.mso.asyncStatusUpdate", topic);
		assertEquals("http://test:1234", host);
	}
}
