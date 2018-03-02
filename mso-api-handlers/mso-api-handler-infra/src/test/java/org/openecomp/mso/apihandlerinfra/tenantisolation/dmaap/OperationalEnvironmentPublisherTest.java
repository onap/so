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

package org.openecomp.mso.apihandlerinfra.tenantisolation.dmaap;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.mso.apihandlerinfra.tenantisolation.dmaap.OperationalEnvironmentPublisher;
import org.openecomp.mso.properties.MsoPropertiesException;
import org.openecomp.mso.properties.MsoPropertiesFactory;

public class OperationalEnvironmentPublisherTest {

	
	@BeforeClass
	public static void setUp() throws MsoPropertiesException {
		System.setProperty("mso.config.path", "src/test/resources/");
		MsoPropertiesFactory propertiesFactory = new MsoPropertiesFactory();
		propertiesFactory.initializeMsoProperties("MSO_PROP_APIHANDLER_INFRA", "mso.apihandler-infra.properties");
	}
	
	@Test
	public void getProperties() throws FileNotFoundException, IOException {
		OperationalEnvironmentPublisher publisher = new OperationalEnvironmentPublisher();
		
		assertEquals("m97898@mso.ecomp.att.com", publisher.getUserName());
		assertEquals("VjR5NDcxSzA=", publisher.getPassword());
		assertEquals("com.att.ecomp.mso.operationalEnvironmentEvent", publisher.getTopic());
		assertEquals("https://dcae-mrtr-ftl3.ecomp.cci.att.com:3905", publisher.getHost().get());
	}
}
