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
public class BuildingBlockValidatorRunnerTest {

	@Rule
    public ExpectedException thrown= ExpectedException.none();
	
	@Autowired
	private BuildingBlockValidatorRunner runner;
	
	@Test
	public void filterValidatorTest() {
		
		MyPreValidatorOne one = new MyPreValidatorOne();
		MyPreValidatorTwo two = new MyPreValidatorTwo();
		MyPreValidatorThree three = new MyPreValidatorThree();
		List<FlowValidator> validators = Arrays.asList(one, two, three);

		List<FlowValidator> result = runner.filterValidators(validators, "test");

		List<FlowValidator> expected = Arrays.asList(two, one);
		
		assertEquals(expected, result);
	}
	
	@Test
	public void testValidate() {
		
		BuildingBlockExecution execution = new DelegateExecutionImpl(new DelegateExecutionFake());
		execution.setVariable("testProcessKey", "1234");
		try {
			runner.preValidate("test", execution);
			fail("exception not thrown");
		} catch (BpmnError e) {
			WorkflowException workflowException = (WorkflowException)execution.getVariable("WorkflowException");
			assertEquals("Failed Validations:\norg.onap.so.bpmn.common.validation.MyPreValidatorTwo\norg.onap.so.bpmn.common.validation.MyPreValidatorOne", workflowException.getErrorMessage());
		}
		runner.preValidate("test2", mock(BuildingBlockExecution.class));
	}
	
	@Test
	public void testEmptyList() {
		boolean result = runner.preValidate("test3", mock(BuildingBlockExecution.class));
		
		assertTrue(result);
	}
}
