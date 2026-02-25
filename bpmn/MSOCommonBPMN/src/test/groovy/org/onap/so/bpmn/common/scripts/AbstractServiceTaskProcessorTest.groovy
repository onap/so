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

package org.onap.so.bpmn.common.scripts

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.junit.Assert
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.onap.so.bpmn.core.WorkflowException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*

import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner;
import org.junit.Before
import org.onap.so.bpmn.common.scripts.AaiUtil;
import org.junit.Rule;
import org.junit.Test
import org.junit.Ignore
import org.junit.runner.RunWith
import org.junit.Before;
import org.junit.Test;
import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl
import org.camunda.bpm.engine.repository.ProcessDefinition



class AbstractServiceTaskProcessorImpl extends  AbstractServiceTaskProcessor{

	@Override
	void preProcessRequest(DelegateExecution execution) {

	}
}


@RunWith(MockitoJUnitRunner.Silent.class)
public class AbstractServiceTaskProcessorTest extends MsoGroovyTest {

	@Captor
	ArgumentCaptor<ExecutionEntity> captor=  ArgumentCaptor.forClass(ExecutionEntity.class);

	@Test
	public void testCreateCallbackURL_Success() {
		ExecutionEntity mockExecution = setupMock()
		when(mockExecution.getVariable("mso.workflow.message.endpoint")).thenReturn('http://localhost:18080/mso/WorkflowMessage/')
		AbstractServiceTaskProcessorImpl  serviceTaskProcessor = new AbstractServiceTaskProcessorImpl();
		def endpoint = serviceTaskProcessor.createCallbackURL(mockExecution, 'testMessageType', 'testCorrelator')
		assertEquals('http://localhost:18080/mso/WorkflowMessage/testMessageType/testCorrelator', endpoint)
	}

	@Test
	public void testCreateCallbackURL_NullEndpoint() {

		ExecutionEntity mockExecution = setupMock()
		try {

			when(mockExecution.getVariable("mso.workflow.message.endpoint")).thenReturn("")
			AbstractServiceTaskProcessorImpl serviceTaskProcessor = new AbstractServiceTaskProcessorImpl();
			def endpoint = serviceTaskProcessor.createCallbackURL(mockExecution, 'testMessageType', 'testCorrelator')
		}
		catch(Exception ex){
		}
		Mockito.verify(mockExecution,times(1)).setVariable(captor.capture(),captor.capture())
		WorkflowException workflowException = captor.getValue()
		Assert.assertEquals("mso:workflow:message:endpoint URN mapping is not set",workflowException.getErrorMessage())
		Assert.assertEquals(2000,workflowException.getErrorCode())
	}

	@Test
	public void testCreateWorkflowMessageAdapterCallbackURL_Success() {
		ExecutionEntity mockExecution = setupMock()
		when(mockExecution.getVariable("mso.adapters.workflow.message.endpoint")).thenReturn('http://localhost:18080/workflows/messages/message/')

		AbstractServiceTaskProcessorImpl  serviceTaskProcessor = new AbstractServiceTaskProcessorImpl();
		def endpoint = serviceTaskProcessor.createWorkflowMessageAdapterCallbackURL(mockExecution, 'testMessageType', 'testCorrelator')
		assertEquals('http://localhost:18080/workflows/messages/message/testMessageType/testCorrelator', endpoint)
	}

	@Test
	public void testCreateWorkflowMessageAdapterCallbackURL_NullEndpoint() {

		ExecutionEntity mockExecution = setupMock()
		try {

			when(mockExecution.getVariable("mso.adapters.workflow.message.endpoint")).thenReturn("")

			AbstractServiceTaskProcessorImpl serviceTaskProcessor = new AbstractServiceTaskProcessorImpl();
			def endpoint = serviceTaskProcessor.createWorkflowMessageAdapterCallbackURL(mockExecution, 'testMessageType', 'testCorrelator')
		}
		catch(Exception ex){
		}
		Mockito.verify(mockExecution,times(1)).setVariable(captor.capture(),captor.capture())
		WorkflowException workflowException = captor.getValue()
		Assert.assertEquals("mso:adapters:workflow:message:endpoint URN mapping is not set",workflowException.getErrorMessage())
		Assert.assertEquals(2000,workflowException.getErrorCode())
	}

	@Test
	public void testSetRollbackEnabledNullOrEmptyDisableRollback() {
		ExecutionEntity mockExecution = setupMock()
		when(mockExecution.getVariable("prefix")).thenReturn('TEST_PREFIX')
		when(mockExecution.getVariable("disableRollback")).thenReturn(null)
		when(mockExecution.getVariable("mso.rollback")).thenReturn("")

		AbstractServiceTaskProcessorImpl  serviceTaskProcessor = new AbstractServiceTaskProcessorImpl();
		serviceTaskProcessor.setRollbackEnabled(mockExecution, "true")
		assertEquals(null, mockExecution.getVariable('TEST_PREFIXbackoutOnFailure'))
	}

	@Test
	public void testSetRollbackEnabledDisableRollback() {
		ExecutionEntity mockExecution = setupMock()
		when(mockExecution.getVariable("prefix")).thenReturn('TEST_PREFIX')
		when(mockExecution.getVariable("disableRollback")).thenReturn(true)
		when(mockExecution.getVariable("mso.rollback")).thenReturn("true")

		AbstractServiceTaskProcessorImpl  serviceTaskProcessor = new AbstractServiceTaskProcessorImpl();
		serviceTaskProcessor.setRollbackEnabled(mockExecution, "true")
		verify(mockExecution).setVariable("TEST_PREFIXbackoutOnFailure",false)
	}

	@Test
	public void testSetRollbackEnabledRollback() {
		ExecutionEntity mockExecution = setupMock()
		when(mockExecution.getVariable("prefix")).thenReturn('TEST_PREFIX')
		when(mockExecution.getVariable("disableRollback")).thenReturn(false)
		when(mockExecution.getVariable("mso.rollback")).thenReturn("true")

		AbstractServiceTaskProcessorImpl  serviceTaskProcessor = new AbstractServiceTaskProcessorImpl();
		serviceTaskProcessor.setRollbackEnabled(mockExecution, "true")
		verify(mockExecution).setVariable("TEST_PREFIXbackoutOnFailure",true)
	}

	@Test
	public void testSetRollbackEnabledDefaultRollback() {
		ExecutionEntity mockExecution = setupMock()
		when(mockExecution.getVariable("prefix")).thenReturn('TEST_PREFIX')
		when(mockExecution.getVariable("disableRollback")).thenReturn("test")
		when(mockExecution.getVariable("mso.rollback")).thenReturn("true")

		AbstractServiceTaskProcessorImpl  serviceTaskProcessor = new AbstractServiceTaskProcessorImpl();
		serviceTaskProcessor.setRollbackEnabled(mockExecution, "true")
		verify(mockExecution).setVariable("TEST_PREFIXbackoutOnFailure",true)
	}

	@Test
	public void testSetBasicDBAuthHeader_Success() {
		ExecutionEntity mockExecution = setupMock()
		when(mockExecution.getVariable("mso.adapters.db.auth")).thenReturn('5E12ACACBD552A415E081E29F2C4772F9835792A51C766CCFDD7433DB5220B59969CB2798C')
		when(mockExecution.getVariable("mso.msoKey")).thenReturn('07a7159d3bf51a0e53be7a8f89699be7')

		AbstractServiceTaskProcessorImpl  serviceTaskProcessor = new AbstractServiceTaskProcessorImpl();
		serviceTaskProcessor.setBasicDBAuthHeader(mockExecution, "true")
		verify(mockExecution).setVariable("BasicAuthHeaderValueDB",'Basic dGVzdDp0ZXN0')
	}

}
