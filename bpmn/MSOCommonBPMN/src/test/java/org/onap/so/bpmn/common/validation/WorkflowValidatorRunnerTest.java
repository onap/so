/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.common.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.bpmn.core.WorkflowException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ValidationConfig.class})
public class WorkflowValidatorRunnerTest {

	@Rule
    public ExpectedException thrown= ExpectedException.none();
	
	@Autowired
	private WorkflowValidatorRunner runner;
	
	@Test
	public void filterValidatorTest() {
		
		WorkflowPreValidatorOne one = new WorkflowPreValidatorOne();
		WorkflowPreValidatorTwo two = new WorkflowPreValidatorTwo();		
		List<WorkflowValidator> validators = Arrays.asList(one, two);

		List<WorkflowValidator> result = runner.filterValidators(validators, "test");

		List<WorkflowValidator> expected = Arrays.asList(two, one);
		
		assertEquals(expected, result);
	}
	
	@Test
	public void testValidate() {
		
		DelegateExecution execution = new DelegateExecutionFake();
		execution.setVariable("testProcessKey", "1234");
		try {
			runner.preValidate("test", execution);
			fail("exception not thrown");
		} catch (BpmnError e) {
			WorkflowException workflowException = (WorkflowException) execution.getVariable("WorkflowException");
			assertEquals("Failed Validations:\norg.onap.so.bpmn.common.validation.WorkflowPreValidatorTwo\norg.onap.so.bpmn.common.validation.WorkflowPreValidatorOne", workflowException.getErrorMessage());
		}
		runner.preValidate("test2", mock(DelegateExecution.class));
	}
	
	@Test
	public void testEmptyList() {
		boolean result = runner.preValidate("test3", mock(DelegateExecution.class));
		
		assertTrue(result);
	}
}
