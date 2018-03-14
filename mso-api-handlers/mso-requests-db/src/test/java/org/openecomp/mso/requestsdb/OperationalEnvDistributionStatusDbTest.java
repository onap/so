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


import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.Mockito;


public class OperationalEnvDistributionStatusDbTest {

	
	private static final String distributionId = "ff3514e3-5a33-55df-13ab-12abad84e7ff";
	private static final String operationalEnvId = "12abad84e7ff";
	private static final String serviceModelVersionId = "ff305d54-75b4-431b-adb2-eb6b9e5ff001";
	private static final String requestId = "431b-adb2-eb6b9e5ff001";
	private static final String status = "SENT";
	private OperationalEnvDistributionStatus operEnvDistStatus;
	
	
	@Test
	public void testGetOperationalEnvDistributionStatus() {
			
		OperationalEnvDistributionStatusDb oeds = Mockito.mock(OperationalEnvDistributionStatusDb.class);
		Mockito.when(oeds.getOperationalEnvDistributionStatus("ff3514e3-5a33-55df-13ab-12abad84e7ff")).thenReturn(operEnvDistStatus);
		OperationalEnvDistributionStatus actual = oeds.getOperationalEnvDistributionStatus(distributionId);
		assertEquals(actual, operEnvDistStatus);
		verify(oeds, times(1)).getOperationalEnvDistributionStatus(any(String.class));
	}

	@Test
	public void testGetOperationalEnvDistributionStatusPerReqId() {
			
		OperationalEnvDistributionStatusDb oeds = Mockito.mock(OperationalEnvDistributionStatusDb.class);
		Mockito.when(oeds.getOperationalEnvDistributionStatusPerReqId("ff3514e3-5a33-55df-13ab-12abad84e7ff", "431b-adb2-eb6b9e5ff001")).thenReturn(operEnvDistStatus);
		OperationalEnvDistributionStatus actual = oeds.getOperationalEnvDistributionStatusPerReqId(distributionId, requestId);
		assertEquals(actual, operEnvDistStatus);
		verify(oeds, times(1)).getOperationalEnvDistributionStatusPerReqId(any(String.class), any(String.class));
	}
	
	@Test
	public void testUpdateOperationalEnvDistributionStatus() {
	
		int val = 1;
		OperationalEnvDistributionStatusDb oeds = Mockito.mock(OperationalEnvDistributionStatusDb.class);
		Mockito.when(oeds.updateOperationalEnvDistributionStatus("OK", "ff3514e3-5a33", "ff3514e3-5a33", "ff3514e3-5a33-55df-13ab-12abad84e7ff")).thenReturn(val);
		int actual = oeds.updateOperationalEnvDistributionStatus("OK", "ff3514e3-5a33", "ff3514e3-5a33", "ff3514e3-5a33-55df-13ab-12abad84e7ff");
		assertEquals(actual, val);
		verify(oeds, times(1)).updateOperationalEnvDistributionStatus(any(String.class), any(String.class), any(String.class), any(String.class));
	}
	
	@Test
	public void testInsertOperationalEnvDistributionStatus() {
	
		OperationalEnvDistributionStatusDb oeds = mock(OperationalEnvDistributionStatusDb.class);
		
		oeds.insertOperationalEnvDistributionStatus(distributionId, operationalEnvId, serviceModelVersionId, status, requestId);	
		doNothing().when(oeds).insertOperationalEnvDistributionStatus(any(String.class), any(String.class), any(String.class), any(String.class), any(String.class));  
		verify(oeds, times(1)).insertOperationalEnvDistributionStatus(any(String.class), any(String.class), any(String.class), any(String.class), any(String.class));
	
	}
	
}