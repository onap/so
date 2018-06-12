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

package org.openecomp.mso.asdc.tenantIsolation;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.mso.asdc.BaseTest;
import org.openecomp.mso.client.aai.AAIVersion;

public class AaiClientPropertiesImplTest extends BaseTest {
	
	private static final String SYSTEM_NAME = "MSO";
	private static final String AAI_ENDPOINT = "http://localhost:8080";
	
	@BeforeClass
	public static void setup() throws Exception {
		System.setProperty("mso.config.path", "src/test/resources");
	}
	
	@Test
	public void getEndpointTest() throws Exception {
		AaiClientPropertiesImpl aaiClientPropertiesImpl = new AaiClientPropertiesImpl();
		String aaiEndpoint = aaiClientPropertiesImpl.getEndpoint().toString();
		assertEquals(AAI_ENDPOINT, aaiEndpoint);
	}
	
	@Test
	public void getSystemNameTest() {
		AaiClientPropertiesImpl aaiClientPropertiesImpl = new AaiClientPropertiesImpl();
		assertEquals(SYSTEM_NAME, aaiClientPropertiesImpl.getSystemName());
	}
	
	@Test
	public void getDefaultVersionTest() {
		AaiClientPropertiesImpl aaiClientPropertiesImpl = new AaiClientPropertiesImpl();
		assertEquals(AAIVersion.LATEST, aaiClientPropertiesImpl.getDefaultVersion());
	}
}
