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

package org.openecomp.mso.requestsdb;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;


public class WatchdogComponentDistributionStatusDbTest {

	private static final String distributionId = "ff3514e3-5a33-55df-13ab-12abad84e7ff";
	private static final String componentName = "MSO";
	private static final String componentDistributionStatus = "SENT";
	
	
	@Test
	public void testGetWatchdogComponentDistributionStatus() {
		List<WatchdogComponentDistributionStatus> watchDogCompDistStatus = new ArrayList<>();
		WatchdogComponentDistributionStatusDb wdcds = Mockito.mock(WatchdogComponentDistributionStatusDb.class);
		Mockito.when(wdcds.getWatchdogComponentDistributionStatus("ff3514e3-5a33-55df-13ab-12abad84e7ff")).thenReturn(watchDogCompDistStatus);
		List<WatchdogComponentDistributionStatus> actual = wdcds.getWatchdogComponentDistributionStatus(distributionId);
		
		assertEquals(actual, watchDogCompDistStatus);
		verify(wdcds, times(1)).getWatchdogComponentDistributionStatus(any(String.class));
	}
	
	
	@Test
	public void testInsertWatchdogComponentDistributionStatus() {
	
		WatchdogComponentDistributionStatusDb wdcds = mock(WatchdogComponentDistributionStatusDb.class);
		
		wdcds.insertWatchdogComponentDistributionStatus(distributionId, componentName, componentDistributionStatus);		
		doNothing().when(wdcds).insertWatchdogComponentDistributionStatus(any(String.class), any(String.class), any(String.class));       
		verify(wdcds, times(1)).insertWatchdogComponentDistributionStatus(any(String.class), any(String.class), any(String.class));
	
	}
	
}
