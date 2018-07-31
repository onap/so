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
package org.onap.so.bpmn.common.aai.tasks;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.bpmn.infrastructure.aai.tasks.AAIFlagTasks;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.mapper.AAIObjectMapper;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.AAIVnfResources;
import org.springframework.beans.factory.annotation.Autowired;

@RunWith(MockitoJUnitRunner.class)
public class AAIFlagTasksTest extends TestDataSetup {

	@InjectMocks
	private AAIFlagTasks aaiFlagTasks = new AAIFlagTasks();

	@Mock
	private AAIVnfResources aaiVnfResources;

	@Mock
	protected AAIObjectMapper MOCK_aaiObjectMapper;

	@Mock
	protected InjectionHelper MOCK_injectionHelper;
	
	@Mock
	protected AAIResourcesClient MOCK_aaiResourcesClient;
	

	@Mock
	private ExtractPojosForBB extractPojosForBB;

	@Mock
	private ExceptionBuilder exceptionUtil;

	private GenericVnf genericVnf;

	@Before
	public void before() throws BBObjectNotFoundException {
		genericVnf = setGenericVnf();
		doReturn(MOCK_aaiResourcesClient).when(MOCK_injectionHelper).getAaiClient();
		when(extractPojosForBB.extractByKey(any(),any(), any())).thenReturn(genericVnf);
	}

	@Test
	public void checkVnfInMaintTestTrue() throws Exception {
		doThrow(new BpmnError("VNF is in maintenance in A&AI")).when(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(String.class));
		doReturn(false).when(aaiVnfResources).checkInMaintFlag(isA(String.class));
		doReturn(true).when(aaiVnfResources).checkInMaintFlag(isA(String.class));
		try {
			aaiFlagTasks.checkVnfInMaintFlag(execution);
		} catch (Exception e) {
			verify(aaiVnfResources, times(1)).checkInMaintFlag(any(String.class));
			verify(exceptionUtil, times(1)).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), eq("VNF is in maintenance in A&AI"));
		}
	}

	@Test
	public void checkVnfInMaintTestFalse() throws Exception {
		doReturn(false).when(aaiVnfResources).checkInMaintFlag(isA(String.class));
		aaiFlagTasks.checkVnfInMaintFlag(execution);
		verify(exceptionUtil, times(0)).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), any(int.class), any(String.class));    
	}

	@Test
	public void checkVnfInMaintFlagExceptionTest() {

		doThrow(new BpmnError("Unknown Error")).when(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(String.class));
		doThrow(Exception.class).when(aaiVnfResources).checkInMaintFlag(isA(String.class));
		try {
			aaiFlagTasks.checkVnfInMaintFlag(execution);
		} catch (Exception e) {
			verify(aaiVnfResources, times(1)).checkInMaintFlag(any(String.class));
			verify(exceptionUtil, times(1)).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(String.class));
		}
		
	}

	@Test
	public void modifyVnfInMaintFlagTest() throws Exception {
		doNothing().when(aaiVnfResources).updateObjectVnf(isA(GenericVnf.class));
		aaiFlagTasks.modifyVnfInMaintFlag(execution, true);
		verify(aaiVnfResources, times(1)).updateObjectVnf(genericVnf);
	}

	@Test
	public void modifyVnfInMaintFlagExceptionTest() {
		
		doThrow(new BpmnError("Unknown Error")).when(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(String.class));
		doThrow(Exception.class).when(aaiVnfResources).updateObjectVnf(isA(GenericVnf.class));
		try {
			aaiFlagTasks.modifyVnfInMaintFlag(execution, true);
		} catch (Exception e) {
			verify(aaiVnfResources, times(1)).checkInMaintFlag(any(String.class));
			verify(exceptionUtil, times(1)).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(String.class));
		}
	}
}
