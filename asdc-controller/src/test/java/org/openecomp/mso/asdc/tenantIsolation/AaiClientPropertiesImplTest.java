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
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.mso.properties.MsoPropertiesFactory;

public class AaiClientPropertiesImplTest {

	@BeforeClass
	public static void setup() throws Exception {
		System.setProperty("mso.config.path", "src/test/resources");
	}
	
	
	@Test
	@Ignore // 1802 merge
	public void testGetEndpoint() throws Exception {

		MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();
		msoPropertiesFactory.initializeMsoProperties(AsdcPropertiesUtils.MSO_ASDC_CLIENTS, "/mso.asdc.clients.properties");

		AaiClientPropertiesImpl aaiPropertiesImpl = new AaiClientPropertiesImpl();		
		String aaiEndpoint = aaiPropertiesImpl.getEndpoint().toString();
		
		assertEquals("AAI endpoint", "http://localhost:28090", aaiEndpoint);
		
	}
	
}
