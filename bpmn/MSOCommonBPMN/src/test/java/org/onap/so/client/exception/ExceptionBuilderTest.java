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

package org.onap.so.client.exception;

import static org.junit.Assert.assertEquals;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Test;
import org.onap.so.bpmn.mock.FileUtil;
import org.onap.so.BaseTest;

public class ExceptionBuilderTest extends BaseTest {

	private static final String RESOURCE_PATH = "__files/";
	private static final String VALID_ERROR_MESSAGE = "{test error message}";

	/*@Test
	public void buildAndThrowWorkflowExceptionTest() {
		try {
			ExceptionBuilder exceptionBuilder = new ExceptionBuilder();
			exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, new NullPointerException(VALID_ERROR_MESSAGE));
		} catch (BpmnError bpmnException){
			assertEquals("MSOWorkflowException", bpmnException.getErrorCode());
		}
	}

	@Test
	public void buildAndThrowWorkflowExceptionInvalidMessageTest() {
		try{
			ExceptionBuilder exceptionBuilder = new ExceptionBuilder();
			String invalidErrorMessage = FileUtil.readResourceFile(RESOURCE_PATH + "invalidErrorMessage.txt");
			exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, new NullPointerException(invalidErrorMessage));
		} catch (BpmnError bpmnException){
			assertEquals("MSOWorkflowException", bpmnException.getErrorCode());
		}
	}
	
	@Test
	public void buildAndThrowWorkflowExceptionInvalidMessageFlagTest() {
		try{
			ExceptionBuilder exceptionBuilder = new ExceptionBuilder();
			String invalidErrorMessage = FileUtil.readResourceFile(RESOURCE_PATH + "invalidErrorMessageFlag.txt");
			exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, new NullPointerException(invalidErrorMessage));
		} catch (BpmnError bpmnException){
			assertEquals("MSOWorkflowException", bpmnException.getErrorCode());
		}
	}
	
	@Test
	public void buildAndThrowWorkflowExceptionNullMessageTest() {
		try{
			ExceptionBuilder exceptionBuilder = new ExceptionBuilder();
			exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, new NullPointerException());
		} catch (BpmnError bpmnException){
			assertEquals("MSOWorkflowException", bpmnException.getErrorCode());
		}
	}*/
}
