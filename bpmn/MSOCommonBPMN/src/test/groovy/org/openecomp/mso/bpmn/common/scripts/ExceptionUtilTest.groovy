/*- 
 * ============LICENSE_START======================================================= 
 * OPENECOMP - MSO 
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

package org.openecomp.mso.bpmn.common.scripts

import org.junit.Assert
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;


import org.junit.Ignore;

import static org.mockito.Mockito.*

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.internal.debugging.MockitoDebuggerImpl
import org.mockito.runners.MockitoJUnitRunner
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil;
@RunWith(MockitoJUnitRunner.class)
import org.junit.Test



class ExceptionUtilTest {


	@Before
	public void init()
	{
		MockitoAnnotations.initMocks(this)
	}

	@Test
	public void testErrorMessageDetails() {



		String restFault = """
				<Fault>
<requestError>
<serviceException>
<messageId>SVC3002</messageId>
<text>Error writing output performing %1 on %2 (msg=%3) (ec=%4)</text>
<variables>
<variable>PUTcustomer</variable>
<variable>SubName01</variable>
<variable>Unexpected error reading/updating database:Adding this property for key [service-instance-id] and value [USSTU2CFCNC0101UJZZ01] violates a uniqueness constraint [service-instance-id]</variable>
<variable>ERR.5.4.5105</variable>
</variables>
</serviceException>
</requestError>
</Fault>
			"""
		def errorString = """<aetgt:WorkflowException xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1">
				<aetgt:ErrorMessage>Received error from A&amp;AI (Error writing output performing PUTcustomer on SubName01 (msg=Unexpected error reading/updating database:Adding this property for key [service-instance-id] and value [USSTU2CFCNC0101UJZZ01] violates a uniqueness constraint [service-instance-id]) (ec=ERR.5.4.5105))</aetgt:ErrorMessage>
				<aetgt:ErrorCode>5000</aetgt:ErrorCode>
			</aetgt:WorkflowException>""" as String

		ExecutionEntity mockExecution = mock(ExecutionEntity.class)

		ExceptionUtil util = new ExceptionUtil()
		Assert.assertEquals(errorString, util.MapAAIExceptionToWorkflowExceptionOld(restFault, mockExecution))
	}


	@Test
	public void testErrorMessage() {
		String restFault = """
				<rest:RESTFault xmlns:rest="http://schemas.activebpel.org/REST/2007/12/01/aeREST.xsd"
				errorType="transport">
				<rest:errorCode>500</rest:errorCode>
				<rest:message>HTTP method: GET Internal Server Error </rest:message>
				</rest:RESTFault>
			"""
		def errorString = """<aetgt:WorkflowException xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1">
					<aetgt:ErrorMessage>Received error from A&amp;AI (HTTP method: GET Internal Server Error)</aetgt:ErrorMessage>
					<aetgt:ErrorCode>5000</aetgt:ErrorCode>
				</aetgt:WorkflowException>""" as String

		ExecutionEntity mockExecution = mock(ExecutionEntity.class)
		when(mockExecution.getVariable("prefix")).thenReturn("test_")
		when(mockExecution.getVariable("test_ErrorResponse")).thenReturn("HTTP method: GET Internal Server Error")

		ExceptionUtil util = new ExceptionUtil()
		String msg = "HTTP method: GET Internal Server Error"
		Assert.assertEquals(errorString, util.MapAAIExceptionToWorkflowExceptionOld(restFault, mockExecution))
	}

	@Test
	public void testBadResponseErrorMessage() {
		String restFault = "asfafasfasfd"
		ExecutionEntity mockExecution = mock(ExecutionEntity.class)
		when(mockExecution.getVariable("prefix")).thenReturn("test_")
		when(mockExecution.getVariable("test_ErrorResponse")).thenReturn(null)
		ExceptionUtil util = new ExceptionUtil()
		String expected = "<aetgt:WorkflowException xmlns:aetgt=\"http://org.openecomp/mso/workflow/schema/v1\">"+
		"\n					<aetgt:ErrorMessage>Received error from A&amp;AI ()</aetgt:ErrorMessage>"+
		"\n					<aetgt:ErrorCode>5000</aetgt:ErrorCode>"+
	"\n				</aetgt:WorkflowException>";
		Assert.assertEquals(expected,util.MapAAIExceptionToWorkflowExceptionOld(restFault, mockExecution))
	}

	@Test
	public void testMapErrorCode5010(){
		String msg = "Connect to njcdtl20ew2988:8070  failed: Connection refused: connect"
		ExceptionUtil util = new ExceptionUtil()
		Assert.assertEquals("5010",util.MapErrorCode(msg))
	}

	@Test
	public void testMapErrorCode5020(){
		String msg = "Connection timed out"
		ExceptionUtil util = new ExceptionUtil()
		Assert.assertEquals("5020",util.MapErrorCode(msg))
	}

	@Test
	public void testMapCategoryToErrorCode5100 () {
		String errorCategory = "OPENSTACK"
		ExceptionUtil util = new ExceptionUtil()
		Assert.assertEquals('5100', util.MapCategoryToErrorCode(errorCategory))
	}

	@Test
	public void testMapCategoryToErrorCode5110 () {
		String errorCategory = "IO"
		ExceptionUtil util = new ExceptionUtil()
		Assert.assertEquals('5110', util.MapCategoryToErrorCode(errorCategory))
	}

	@Test
	public void testMapCategoryToErrorCode7020 () {
		String errorCategory = "USERDATA"
		ExceptionUtil util = new ExceptionUtil()
		Assert.assertEquals('7020', util.MapCategoryToErrorCode(errorCategory))
	}

	@Test
	@Ignore
	public void testMapAdapterExecptionToWorkflowException_UserDataErrorMessage () {
		String response = """<ns2:TenantException xmlns:ns2=\"http://org.openecomp.mso/tenant\"><category>USERDATA</category><message>Cloud Site [CHCGILNE001] not found</message></ns2:TenantException>""" as String
		String expected = """WorkflowException[processKey=ExceptionUtil,errorCode=7020,errorMessage=Received error from Tenant Adapter: Cloud Site [CHCGILNE001] not found]""" as String

		ExecutionEntity mockExecution = mock(ExecutionEntity.class)
		when(mockExecution.getVariable("prefix")).thenReturn("test_")
		when(mockExecution.getVariable("testProcessKey")).thenReturn("ExceptionUtil")

		ExceptionUtil util = new ExceptionUtil()
		String actual = util.MapAdapterExecptionToWorkflowException(response, mockExecution, "Tenant Adapter")

		verify(mockExecution).setVariable("WorkflowException",expected)
	}

	@Test
	@Ignore
	public void testMapAdapterExecptionToWorkflowException_IOErrorMessage () {
		String response = """<ns2:TenantException xmlns:ns2=\"http://org.openecomp.mso/tenant\"><category>IO</category><message>[TokenAuth] Connect to test.aic.cip.att.com:5000 [test.aic.cip.att.com/300.200.100.100] failed: Connection refused</message></ns2:TenantException>""" as String
		String expected = """WorkflowException[processKey=ExceptionUtil,errorCode=5110,errorMessage=Could not communicate with Platform Orchestrator]""" as String

		ExecutionEntity mockExecution = mock(ExecutionEntity.class)
		when(mockExecution.getVariable("prefix")).thenReturn("test_")
		when(mockExecution.getVariable("testProcessKey")).thenReturn("ExceptionUtil")

		ExceptionUtil util = new ExceptionUtil()
		String actual = util.MapAdapterExecptionToWorkflowException(response, mockExecution, "Tenant Adapter")

		verify(mockExecution).setVariable("WorkflowException",expected)
	}

}
