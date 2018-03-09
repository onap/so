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

package org.openecomp.mso.bpmn.core;

import java.io.IOException;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class PropertyConfigurationTest {
	@Before
	public void beforeTest() throws IOException {
		Map<String, String> defaultProperties = PropertyConfigurationSetup.createBpmnProperties();
		defaultProperties.put("testValue", "testKey");
		PropertyConfigurationSetup.init(defaultProperties);
	}
	
	@Test
	public void testPropertyFileWatcher() throws InterruptedException, IOException {
		Assert.assertEquals(true, PropertyConfiguration.getInstance().isFileWatcherRunning());
	}
	
	@Test
	public void testPropertyLoading() throws IOException, InterruptedException {
		PropertyConfiguration propertyConfiguration = PropertyConfiguration.getInstance();
		Map<String,String> props = propertyConfiguration.getProperties(PropertyConfiguration.MSO_BPMN_PROPERTIES);
		Assert.assertNotNull(props);
		Assert.assertEquals("testValue", props.get("testKey"));
	}
	
	@Test
	public void testPropertyReload() throws IOException, InterruptedException {
		PropertyConfiguration propertyConfiguration = PropertyConfiguration.getInstance();
		Map<String,String> properties = propertyConfiguration.getProperties(PropertyConfiguration.MSO_BPMN_PROPERTIES);
		Assert.assertNotNull(properties);
		Assert.assertEquals("testValue", properties.get("testKey"));

		Map<String, String> newProperties = PropertyConfigurationSetup.createBpmnProperties();
		newProperties.put("newKey", "newValue");
		PropertyConfigurationSetup.addProperties(newProperties, 10000);

		// Reload and check for the new value
		properties = propertyConfiguration.getProperties(PropertyConfiguration.MSO_BPMN_PROPERTIES);
		Assert.assertNotNull(properties);
		Assert.assertEquals("newValue", properties.get("newKey"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testPropertyFileDoesNotExists_NotIntheList() throws IOException {
		PropertyConfiguration propertyConfiguration = PropertyConfiguration.getInstance();
		propertyConfiguration.getProperties("badfile.properties");
		Assert.fail("Expected IllegalArgumentException");
	}
	
	@Test(expected=java.lang.UnsupportedOperationException.class)
	public void testPropertyModificationException() throws IOException {
		PropertyConfiguration propertyConfiguration = PropertyConfiguration.getInstance();
		Map<String,String> props = propertyConfiguration.getProperties(PropertyConfiguration.MSO_BPMN_PROPERTIES);
		Assert.assertNotNull(props);
		Assert.assertEquals("testValue", props.get("testKey"));
		props.put("newKey", "newvalue");
	}
}
