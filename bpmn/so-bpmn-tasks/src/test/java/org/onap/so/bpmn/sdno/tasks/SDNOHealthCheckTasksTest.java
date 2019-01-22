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

package org.onap.so.bpmn.sdno.tasks;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.SDNOHealthCheckResources;


@RunWith(MockitoJUnitRunner.Silent.class)
public class SDNOHealthCheckTasksTest extends TestDataSetup {
	
	@InjectMocks
	protected SDNOHealthCheckTasks sdnoHealthCheckTasks = new SDNOHealthCheckTasks();
	
	@Mock
	SDNOHealthCheckResources MOCK_sdnoHealthCheckResources;
	
	
	@Mock
	private ExceptionBuilder exceptionUtil;
	
	@Mock
	private ExtractPojosForBB extractPojosForBB;
	
	
	private RequestContext requestContext;
	private GenericVnf genericVnf;
	
	@Before
	public void before() throws BBObjectNotFoundException {
		genericVnf = setGenericVnf();
		requestContext = setRequestContext();
		when(extractPojosForBB.extractByKey(any(),any(), any())).thenReturn(genericVnf);
		
	}

	@Test
	public void sdnoHealthCheckTest() throws Exception {
		doReturn(true).when(MOCK_sdnoHealthCheckResources).healthCheck(genericVnf,  requestContext);
		sdnoHealthCheckTasks.sdnoHealthCheck(execution);
		verify(MOCK_sdnoHealthCheckResources, times(1)).healthCheck(genericVnf, requestContext);
	}
	
	@Test
	public void sdnoHealthCheckNoResponseTest() throws Exception {
	
		doReturn(false).when(MOCK_sdnoHealthCheckResources).healthCheck(genericVnf, requestContext);
		try {
			sdnoHealthCheckTasks.sdnoHealthCheck(execution);
		} catch (Exception e) {
			verify(MOCK_sdnoHealthCheckResources, times(1)).healthCheck(genericVnf, requestContext);
			verify(exceptionUtil, times(1)).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(String.class));
		}
		
	}
	
	@Test
	public void sdnoHealthCheckExceptionTest() throws Exception {
		doThrow(new Exception("Unknown Error")).when(MOCK_sdnoHealthCheckResources).healthCheck(genericVnf, requestContext);
		doThrow(new BpmnError("Unknown Error")).when(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(String.class));
		try {
			sdnoHealthCheckTasks.sdnoHealthCheck(execution);
		} catch (Exception e) {
			verify(MOCK_sdnoHealthCheckResources, times(1)).healthCheck(genericVnf, requestContext);
			verify(exceptionUtil, times(1)).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(String.class));
		}

	}
}
