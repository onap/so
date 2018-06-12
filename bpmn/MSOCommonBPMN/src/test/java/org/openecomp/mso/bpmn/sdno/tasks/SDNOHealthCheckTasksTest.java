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

package org.openecomp.mso.bpmn.sdno.tasks;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.mso.BaseTest;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;

public class SDNOHealthCheckTasksTest extends BaseTest {
	@Autowired
	protected SDNOHealthCheckTasks sdnoHealthCheckTasks;
	
	private RequestContext requestContext;
	private GenericVnf genericVnf;
	
	@Before
	public void before() {
		genericVnf = setGenericVnf();
		requestContext = setRequestContext();
	}

	@Test
	public void sdnoHealthCheckTest() throws Exception {
		doReturn(true).when(MOCK_sdnoHealthCheckResources).healthCheck(genericVnf, requestContext);
		sdnoHealthCheckTasks.sdnoHealthCheck(execution);
		verify(MOCK_sdnoHealthCheckResources, times(1)).healthCheck(genericVnf, requestContext);
	}
	
	@Test
	public void sdnoHealthCheckNoResponseTest() throws Exception {
		expectedException.expect(BpmnError.class);
		
		doReturn(false).when(MOCK_sdnoHealthCheckResources).healthCheck(genericVnf, requestContext);
		sdnoHealthCheckTasks.sdnoHealthCheck(execution);
	}
	
	@Test
	public void sdnoHealthCheckExceptionTest() throws Exception {
		expectedException.expect(BpmnError.class);
		
		doThrow(Exception.class).when(MOCK_sdnoHealthCheckResources).healthCheck(genericVnf, requestContext);
		sdnoHealthCheckTasks.sdnoHealthCheck(execution);
	}
}
