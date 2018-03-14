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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

public class OperationalEnvServiceModelStatusDbTest {

		
		private static final String operationalEnvId = "12abad84e7ff";
		private static final String serviceModelVersionId = "ff305d54-75b4-431b-adb2-eb6b9e5ff001";
		private static final String requestId = "431b-adb2-eb6b9e5ff001";
		private static final String status = "SENT";
		private static final int retryCount = 1;
		private static final String recoveryAction = "Retry";
		private static final String workloadContext = "VNF_D2D";
		
		private OperationalEnvServiceModelStatus operEnvDistStatus;

		
		@Test
		public void testGetOperationalEnvDistributionStatus() {
				
			OperationalEnvServiceModelStatusDb oesms = Mockito.mock(OperationalEnvServiceModelStatusDb.class);
			Mockito.when(oesms.getOperationalEnvServiceModelStatus("12abad84e7ff", "ff305d54-75b4-431b-adb2-eb6b9e5ff001")).thenReturn(operEnvDistStatus);
			OperationalEnvServiceModelStatus actual = oesms.getOperationalEnvServiceModelStatus(operationalEnvId, serviceModelVersionId);
			assertEquals(actual, operEnvDistStatus);
			verify(oesms, times(1)).getOperationalEnvServiceModelStatus(any(String.class), any(String.class));
		}
		
		@Test
		public void testGetOperationalEnvIdStatus() {
			
			List<OperationalEnvServiceModelStatus> operEnvSvcModelStatus = new ArrayList<>();		
			OperationalEnvServiceModelStatusDb oesms = Mockito.mock(OperationalEnvServiceModelStatusDb.class);			
			Mockito.when(oesms.getOperationalEnvIdStatus("12abad84e7ff", "ff305d54-75b4-431b-adb2-eb6b9e5ff001")).thenReturn(operEnvSvcModelStatus);
			List<OperationalEnvServiceModelStatus> actual = oesms.getOperationalEnvIdStatus(operationalEnvId, requestId);
			assertEquals(actual, operEnvSvcModelStatus);
			verify(oesms, times(1)).getOperationalEnvIdStatus(any(String.class), any(String.class));
			
		}
		
		@Test
		public void testUpdateOperationalEnvRetryCountStatus() {
		
			int val = 1;
			OperationalEnvServiceModelStatusDb oesms = Mockito.mock(OperationalEnvServiceModelStatusDb.class);
			Mockito.when(oesms.updateOperationalEnvRetryCountStatus("12abad84e7ff", "ff305d54-75b4-431b-adb2-eb6b9e5ff001", "SENT", 1)).thenReturn(val);
			int actual = oesms.updateOperationalEnvRetryCountStatus(operationalEnvId, serviceModelVersionId, status, retryCount);
			assertEquals(actual, val);
			verify(oesms, times(1)).updateOperationalEnvRetryCountStatus(any(String.class), any(String.class), any(String.class), any(int.class));
		}

		@Test
		public void testUpdateOperationalEnvRetryCountStatusPerReqId() {
		
			int val = 1;
			OperationalEnvServiceModelStatusDb oesms = Mockito.mock(OperationalEnvServiceModelStatusDb.class);
			Mockito.when(oesms.updateOperationalEnvRetryCountStatusPerReqId("12abad84e7ff", "ff305d54-75b4-431b-adb2-eb6b9e5ff001", "SENT", 1, "431b-adb2-eb6b9e5ff001")).thenReturn(val);
			int actual = oesms.updateOperationalEnvRetryCountStatusPerReqId(operationalEnvId, serviceModelVersionId, status, retryCount, requestId);
			assertEquals(actual, val);
			verify(oesms, times(1)).updateOperationalEnvRetryCountStatusPerReqId(any(String.class), any(String.class), any(String.class), 
					any(int.class), any(String.class));
		}
	

		@Test
		public void testInsertOperationalEnvServiceModelStatus() {
		
			OperationalEnvServiceModelStatusDb oesms = mock(OperationalEnvServiceModelStatusDb.class);
			
			oesms.insertOperationalEnvServiceModelStatus(requestId, operationalEnvId, serviceModelVersionId, status, recoveryAction,  retryCount, workloadContext);		
			doNothing().when(oesms).insertOperationalEnvServiceModelStatus(any(String.class), any(String.class), any(String.class), 
					any(String.class), any(String.class), any(int.class), any(String.class));       
			verify(oesms, times(1)).insertOperationalEnvServiceModelStatus(any(String.class), any(String.class), any(String.class), 
					any(String.class), any(String.class), any(int.class), any(String.class));
		
		}
		
}
