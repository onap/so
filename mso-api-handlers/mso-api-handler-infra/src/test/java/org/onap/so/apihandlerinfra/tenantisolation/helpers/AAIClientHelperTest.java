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

package org.onap.so.apihandlerinfra.tenantisolation.helpers;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.onap.so.apihandlerinfra.BaseTest;
import org.onap.so.client.aai.AAIVersion;
import org.onap.so.client.aai.entities.AAIResultWrapper;
import org.onap.so.client.aai.objects.AAIOperationalEnvironment;
import org.springframework.beans.factory.annotation.Autowired;



public class AAIClientHelperTest extends BaseTest{
    
	@Autowired
	private AAIClientHelper clientHelper;
	
	@Test
	public void testGetAaiOperationalEnvironmentSuccess() throws Exception { 
		stubFor(get(urlPathMatching("/aai/" + AAIVersion.LATEST + "/cloud-infrastructure/operational-environments/.*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("vnfoperenv/ecompOperationalEnvironment.json").withStatus(HttpStatus.SC_ACCEPTED)));
		
		AAIResultWrapper wrapper = clientHelper.getAaiOperationalEnvironment("EMOE-001");
		Optional<AAIOperationalEnvironment> aaiOpEnv = wrapper.asBean(AAIOperationalEnvironment.class);
		assertEquals("EMOE-001", aaiOpEnv.get().getOperationalEnvironmentId());
	}
	
	@Test
	public void testUpdateSuccess() { 
		stubFor(post(urlPathMatching("/aai/" + AAIVersion.LATEST + "/cloud-infrastructure/operational-environments/.*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_ACCEPTED)));
		
		AAIOperationalEnvironment ecompEnv = new AAIOperationalEnvironment();
		ecompEnv.setTenantContext("Test");
		ecompEnv.setWorkloadContext("ECOMPL_PSL");
		
		try {
			AAIClientHelper clientHelper = mock(AAIClientHelper.class);
			doNothing().when(clientHelper).updateAaiOperationalEnvironment(any(String.class), any(AAIOperationalEnvironment.class));
			clientHelper.updateAaiOperationalEnvironment("EMOE-001", ecompEnv);
			
			verify(clientHelper, times(1)).updateAaiOperationalEnvironment("EMOE-001", ecompEnv);
		} catch(Exception e) {
			fail("shouldn't reach here");
		}
	}
	
	@Test
	public void testUpdateMapSuccess() { 
		stubFor(post(urlPathMatching("/aai/" + AAIVersion.LATEST + "/cloud-infrastructure/operational-environments/.*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_ACCEPTED)));
		
		Map<String, String> payload = new HashMap<String, String>();
		payload.put("tenant-context", "Test");
		payload.put("workload-context", "ECOMPL_PSL");
		
		try {
			AAIClientHelper clientHelper = mock(AAIClientHelper.class);
			doNothing().when(clientHelper).updateAaiOperationalEnvironment("EMOE-001", payload);
			clientHelper.updateAaiOperationalEnvironment("EMOE-001", payload);
			
			verify(clientHelper, times(1)).updateAaiOperationalEnvironment("EMOE-001", payload);
		} catch(Exception e) {
			fail("shouldn't reach here");
		}
	}	
	
	@Test
	public void testCreateSuccess() { 
		stubFor(put(urlPathMatching("/aai/" + AAIVersion.LATEST + "/cloud-infrastructure/operational-environments/.*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_ACCEPTED)));
		
		AAIOperationalEnvironment ecompEnv = new AAIOperationalEnvironment();
		ecompEnv.setOperationalEnvironmentId("opeEvnId");
		ecompEnv.setTenantContext("Test");
		ecompEnv.setWorkloadContext("ECOMPL_PSL");
		
		try {
			AAIClientHelper clientHelper = mock(AAIClientHelper.class);
			doNothing().when(clientHelper).createOperationalEnvironment(any(AAIOperationalEnvironment.class));
			clientHelper.createOperationalEnvironment(ecompEnv);
			
			verify(clientHelper, times(1)).createOperationalEnvironment(ecompEnv);
		} catch(Exception e) {
			fail("shouldn't reach here");
		}
	}
	
	@Test
	public void testcreateRelationshipSuccess() { 
		stubFor(put(urlPathMatching("/aai/" + AAIVersion.LATEST + "/cloud-infrastructure/operational-environments/.*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_ACCEPTED)));
		
		AAIOperationalEnvironment ecompEnv = new AAIOperationalEnvironment();
		ecompEnv.setTenantContext("Test");
		ecompEnv.setWorkloadContext("ECOMPL_PSL");
		
		try {
			AAIClientHelper clientHelper = mock(AAIClientHelper.class);
			doNothing().when(clientHelper).createRelationship(anyString(), anyString());
			clientHelper.createRelationship("managingEcomp", "vnfOp");
			
			verify(clientHelper, times(1)).createRelationship("managingEcomp", "vnfOp");
		} catch(Exception e) {
			fail("shouldn't reach here");
		}
	}
}
