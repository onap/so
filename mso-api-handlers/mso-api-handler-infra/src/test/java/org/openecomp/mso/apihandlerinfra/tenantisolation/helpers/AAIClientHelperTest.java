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

package org.openecomp.mso.apihandlerinfra.tenantisolation.helpers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.doThrow;


import org.openecomp.mso.apihandlerinfra.tenantisolation.exceptions.AAIClientCallFailed;
import org.openecomp.mso.apihandlerinfra.tenantisolation.mock.MockTest;
import org.openecomp.mso.client.aai.AAIResourcesClient;
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri;
import org.openecomp.mso.client.aai.objects.AAIOperationalEnvironment;


public class AAIClientHelperTest extends MockTest {
    
	@Mock private AAIResourcesClient aaiResourceClientMock;
	private AAIClientHelper clientHelper;
	
	@Before
	public void testSetUp() {
		MockitoAnnotations.initMocks(this);
		AAIClientHelper aaiHelper  = new AAIClientHelper();
		clientHelper = spy(aaiHelper);
		when(clientHelper.getClient()).thenReturn(aaiResourceClientMock);
	}
	
	@Test
	public void testGetAaiOperationalEnvironmentSuccess() throws Exception { 
		clientHelper.getAaiOperationalEnvironment("123");
		verify(aaiResourceClientMock, times(1)).get(any(AAIResourceUri.class));
	}
	
	@Test(expected = AAIClientCallFailed.class) 
	public void testGetAaiOperationalEnvironmentRainyDay() throws Exception {
		when(aaiResourceClientMock.get(any(AAIResourceUri.class))).thenThrow(new RuntimeException());
		clientHelper.getAaiOperationalEnvironment("123");
	}
	
	@Test
	public void testCreateOperationalEnvironmentSuccess() throws Exception { 
		AAIOperationalEnvironment env = AAIClientObjectBuilder.createAAIOperationalEnvironment("123", "Test Env", "ECOMP", "ACTIVE", "Test", "PVT");
		clientHelper.createOperationalEnvironment(env);
		verify(aaiResourceClientMock, times(1)).create(any(AAIResourceUri.class), eq(env));
	}
	
	@Test(expected = AAIClientCallFailed.class) 
	public void testCreateOperationalEnvironmentRainyDay() throws Exception { 
		AAIOperationalEnvironment env = AAIClientObjectBuilder.createAAIOperationalEnvironment("123", "Test Env", "ECOMP", "ACTIVE", "Test", "PVT");
		doThrow(RuntimeException.class).when(aaiResourceClientMock).create(any(AAIResourceUri.class), eq(env));
		clientHelper.createOperationalEnvironment(env);
	}
	
	@Test
	public void testCreateRelationshipSuccess() throws Exception { 
		clientHelper.createRelationship("VOE-001", "MEOE-002");
		verify(aaiResourceClientMock, times(1)).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));
	}
	
	@Test(expected = AAIClientCallFailed.class) 
	public void testCreateRelationshipRainyDay() throws Exception { 
		doThrow(RuntimeException.class).when(aaiResourceClientMock).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));
		clientHelper.createRelationship("VOE-001", "MEOE-002");
	}
}
