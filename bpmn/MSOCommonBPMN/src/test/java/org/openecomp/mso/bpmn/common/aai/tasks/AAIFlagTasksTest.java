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
package org.openecomp.mso.bpmn.common.aai.tasks;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.mso.BaseTest;
import org.openecomp.mso.bpmn.common.BuildingBlockExecution;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.springframework.beans.factory.annotation.Autowired;

public class AAIFlagTasksTest extends BaseTest {
	@Autowired
	private AAIFlagTasks aaiFlagTasks;
	
	private GenericVnf genericVnf;
	
	@Before
	public void before() {
		genericVnf = setGenericVnf();
	}
	
	@Test
	public void checkVnfInMaintTestTrue() throws Exception {
		expectedException.expect(BpmnError.class);
		 
		doReturn(true).when(aaiVnfResources).checkInMaintFlag(isA(String.class));
		aaiFlagTasks.checkVnfInMaintFlag(execution);
	}
	
	@Test
	public void checkVnfInMaintTestFalse() throws Exception {
		doReturn(false).when(aaiVnfResources).checkInMaintFlag(isA(String.class));
	    aaiFlagTasks.checkVnfInMaintFlag(execution);
	    verify(exceptionUtil, times(0)).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), any(int.class), any(String.class));    
	}
	
	@Test
	public void checkVnfInMaintFlagExceptionTest() {
		expectedException.expect(BpmnError.class);
		
		doThrow(Exception.class).when(aaiVnfResources).checkInMaintFlag(isA(String.class));
		aaiFlagTasks.checkVnfInMaintFlag(execution);
	}
	
	@Test
	public void modifyVnfInMaintFlagTest() throws Exception {
		doNothing().when(aaiVnfResources).updateObjectVnf(isA(GenericVnf.class));
		aaiFlagTasks.modifyVnfInMaintFlag(execution, true);
		verify(aaiVnfResources, times(1)).updateObjectVnf(genericVnf);
	}
	
	@Test
	public void modifyVnfInMaintFlagExceptionTest() {
		expectedException.expect(BpmnError.class);
		
		doThrow(Exception.class).when(aaiVnfResources).updateObjectVnf(isA(GenericVnf.class));
		aaiFlagTasks.modifyVnfInMaintFlag(execution, true);
	}
}
