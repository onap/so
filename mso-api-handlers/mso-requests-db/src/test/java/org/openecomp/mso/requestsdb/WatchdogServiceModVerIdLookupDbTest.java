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

/**
 * 
 */
package org.openecomp.mso.requestsdb;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class WatchdogServiceModVerIdLookupDbTest {

	private static final String distributionId = "ff3514e3-5a33-55df-13ab-12abad84e7ff";
	private static final String serviceModelVersionId = "SENT";
	
	@Test
	public void testInsertWatchdogServiceModVerIdLookup() {
	
		WatchdogServiceModVerIdLookupDb wdsm = mock(WatchdogServiceModVerIdLookupDb.class);
		
		wdsm.insertWatchdogServiceModVerIdLookup(distributionId, serviceModelVersionId);		
		doNothing().when(wdsm).insertWatchdogServiceModVerIdLookup(any(String.class), any(String.class));       
		verify(wdsm, times(1)).insertWatchdogServiceModVerIdLookup(any(String.class), any(String.class));
	
	}
	
	@Test
	public void testGetWatchdogServiceModVerId() {
			
		WatchdogServiceModVerIdLookupDb wdsm = Mockito.mock(WatchdogServiceModVerIdLookupDb.class);
		Mockito.when(wdsm.getWatchdogServiceModVerId("ff305d54-75b4-431b-adb2-eb6b9e5ff001")).thenReturn("ff3514e3-5a33-55df-13ab-12abad84e7ff");
		String actual = wdsm.getWatchdogServiceModVerId("ff305d54-75b4-431b-adb2-eb6b9e5ff001");
		assertEquals(actual, "ff3514e3-5a33-55df-13ab-12abad84e7ff");
		verify(wdsm, times(1)).getWatchdogServiceModVerId(any(String.class));
	}
	
}
