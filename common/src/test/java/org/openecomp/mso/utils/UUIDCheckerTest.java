/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.mso.logger.MsoLogger;

public class UUIDCheckerTest {

	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.GENERAL);	
	
    @Test
    public void testUUIDChecker()  throws Exception {
    	boolean isValidUUID = UUIDChecker.isValidUUID("invalid-uuid");
    	assertEquals(false, isValidUUID);
    	String validUUID = UUIDChecker.verifyOldUUID("invalid-uuid", LOGGER);
    	assertEquals(true, UUIDChecker.isValidUUID(validUUID));
    	String generatedUUID = UUIDChecker.generateUUID(LOGGER);
    	assertEquals(true, UUIDChecker.isValidUUID(generatedUUID));
    	String generatedServiceInstanceId = UUIDChecker.generateServiceInstanceID(LOGGER);
    	assertEquals(true, UUIDChecker.isValidUUID(generatedServiceInstanceId));
    }

}
