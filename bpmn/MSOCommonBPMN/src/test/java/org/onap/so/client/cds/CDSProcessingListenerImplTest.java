/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 TechMahindra
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

package org.onap.so.client.cds;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.onap.ccsdk.apps.controllerblueprints.common.api.EventType;
import org.onap.ccsdk.apps.controllerblueprints.common.api.Status;
import org.onap.ccsdk.apps.controllerblueprints.processing.api.ExecutionServiceOutput;

@RunWith(JUnit4.class)
public class CDSProcessingListenerImplTest {
	@InjectMocks
	private CDSProcessingListenerImpl cdsProcessingListenerImpl = new CDSProcessingListenerImpl();
	
	@Test
	public void onMessageSuccessTest() throws Exception {
		EventType eventType = EventType.EVENT_COMPONENT_EXECUTED;
		Status status = Status.newBuilder().setEventType(eventType).build();
		ExecutionServiceOutput message = ExecutionServiceOutput.newBuilder().setStatus(status).build();
		
		cdsProcessingListenerImpl.onMessage(message);
	}
	
	@Test
	public void onMessageFailedTest() throws Exception {
		EventType eventType = EventType.EVENT_COMPONENT_FAILURE;
		Status status = Status.newBuilder().setEventType(eventType).build();
		ExecutionServiceOutput message = ExecutionServiceOutput.newBuilder().setStatus(status).build();
		
		cdsProcessingListenerImpl.onMessage(message);
	}
	
	@Test
	public void onMessageProcessingTest() throws Exception {
		EventType eventType = EventType.EVENT_COMPONENT_PROCESSING;
		Status status = Status.newBuilder().setEventType(eventType).build();
		ExecutionServiceOutput message = ExecutionServiceOutput.newBuilder().setStatus(status).build();
		
		cdsProcessingListenerImpl.onMessage(message);
	}
	
	@Test
	public void onMessageNotificationTest() throws Exception {
		EventType eventType = EventType.EVENT_COMPONENT_NOTIFICATION;
		Status status = Status.newBuilder().setEventType(eventType).build();
		ExecutionServiceOutput message = ExecutionServiceOutput.newBuilder().setStatus(status).build();
		
		cdsProcessingListenerImpl.onMessage(message);
	}
}
