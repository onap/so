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

package org.onap.so.db.catalog.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onap.so.db.catalog.beans.CloudifyManager;


public class CloudifyManagerTest {
	
	private CloudifyManager cloudifyManager = new CloudifyManager();
	private static final String ID = "testId";
	private static final String CLOUDIFY_URL = "testCloudifyUrl";
	private static final String USERNAME = "testUsername";
	private static final String PASSWORD = "testPassword";
	private static final String VERSION = "testVersion";
	
	@Test
	public final void testCloudifyManager () {
		CloudifyManager id = new CloudifyManager();
		id.setId("testId");
		id.setCloudifyUrl("testCloudifyUrl");
		id.setUsername("testUsername");
		id.setPassword("testPassword");
		id.setVersion("testVersion");
        

        assertTrue (id.getId().equals("testId"));
        assertTrue (id.getCloudifyUrl().equals("testCloudifyUrl"));
        assertTrue (id.getUsername().equals("testUsername"));
        assertTrue (id.getPassword().equals("testPassword"));
        assertTrue (id.getVersion().equals("testVersion"));
    }
	
	
	@Test
	public void cloneTest() {
		cloudifyManager.setId(ID);
		cloudifyManager.setCloudifyUrl(CLOUDIFY_URL);
		cloudifyManager.setUsername(USERNAME);
		cloudifyManager.setPassword(PASSWORD);
		cloudifyManager.setVersion(VERSION);
		
		CloudifyManager clone = cloudifyManager.clone();
		assertEquals(cloudifyManager, clone);
	}
}
