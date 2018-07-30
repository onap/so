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

package org.onap.so.utils;

import static org.junit.Assert.*;

import org.junit.Test;
import org.onap.so.logger.MsoLogger;

public class UUIDCheckerTest {
	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.GENERAL, UUIDCheckerTest.class);
	
	@Test
	public void isValidUUIDTest(){
		String nullID = null;
		String badID = "This is not a UUID";
		String id = UUIDChecker.getUUID();
		assertFalse(UUIDChecker.isValidUUID(nullID));
		assertFalse(UUIDChecker.isValidUUID(badID));
		assertTrue(UUIDChecker.isValidUUID(id));
	}

	@Test
	public void verifyOldUUIDTest(){
		String oldID = UUIDChecker.getUUID();
		String invalidID = "This is not a UUID";
		assertEquals(UUIDChecker.verifyOldUUID(oldID,LOGGER),oldID);
		assertNotEquals(UUIDChecker.verifyOldUUID(invalidID,LOGGER),invalidID);
	}
	
	@Test
	public void generateTest(){
		String id = UUIDChecker.generateUUID(LOGGER);
		assertNotNull(id);
		assertTrue(UUIDChecker.isValidUUID(id));
		
		id = UUIDChecker.generateServiceInstanceID(LOGGER);
		assertNotNull(id);
		assertTrue(UUIDChecker.isValidUUID(id));
		
	}
}
