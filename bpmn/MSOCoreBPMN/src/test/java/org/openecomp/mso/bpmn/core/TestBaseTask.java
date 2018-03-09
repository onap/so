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

package org.openecomp.mso.bpmn.core;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.openecomp.mso.bpmn.core.utils.CamundaDBSetup;
import org.openecomp.mso.logger.MsoLogger;

/**
 * Unit test for BaseTask class.
 */
public class TestBaseTask {

	@Rule
	public ProcessEngineRule processEngineRule = new ProcessEngineRule();
	
	@Before
	public void beforeTest() throws Exception {
		CamundaDBSetup.configure();
		PropertyConfigurationSetup.init();
	}
	
	@Test
	@Deployment(resources={"BaseTaskTest.bpmn"})
	public void shouldInvokeService() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("firstName", "Jane");
		variables.put("lastName", "Doe");
		variables.put("age", (Integer)25);
		variables.put("lastVisit", (Long)1438270117000L);

		RuntimeService runtimeService = processEngineRule.getRuntimeService();
		assertNotNull(runtimeService);
		processEngineRule.getTaskService();
		runtimeService.startProcessInstanceByKey("BaseTaskTest", variables);
	}
	
	/**
	 * Unit test code for BaseTask.
	 */
	public static class TestTask extends BaseTask {
		private static MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL);

		private Expression existingString;
		private Expression nonExistingString;
		private Expression existingStringFromVar;
		private Expression nonExistingStringFromVar;
		
		private Expression existingInteger;
		private Expression nonExistingInteger;
		private Expression existingIntegerFromVar;
		private Expression nonExistingIntegerFromVar;
		
		private Expression existingLong;
		private Expression nonExistingLong;
		private Expression existingLongFromVar;
		private Expression nonExistingLongFromVar;
		
		private Expression existingOutputVar;
		private Expression nonExistingOutputVar;
		private Expression existingBadOutputVar;
		
		public void execute(DelegateExecution execution) throws Exception {
			msoLogger.debug("Started executing " + getClass().getSimpleName());

			/*********************************************************************/
			msoLogger.debug("Running String Field Tests");
			/*********************************************************************/

			String s = getStringField(existingString, execution, "existingString");
			Assert.assertEquals("Hello World", s);

			try {
				s = getStringField(nonExistingString, execution, "nonExistingString");
				Assert.fail("Expected BadInjectedFieldException for nonExistingString");
			} catch (Exception e) {
				if (!(e instanceof BadInjectedFieldException)) {
					Assert.fail("Expected BadInjectedFieldException for nonExistingString");
				}
			}

			s = getOptionalStringField(existingString, execution, "existingString");
			Assert.assertEquals("Hello World", s);

			s = getOptionalStringField(nonExistingString, execution, "nonExistingString");
			Assert.assertEquals(null, s);

			/*********************************************************************/
			msoLogger.debug("Running String Expression Tests");
			/*********************************************************************/

			s = getStringField(existingStringFromVar, execution, "existingStringFromVar");
			Assert.assertEquals("Jane", s);

			try {
				s = getStringField(nonExistingStringFromVar, execution, "nonExistingStringFromVar");
				Assert.fail("Expected BadInjectedFieldException for nonExistingString");
			} catch (Exception e) {
				if (!(e instanceof BadInjectedFieldException)) {
					Assert.fail("Expected BadInjectedFieldException for nonExistingStringFromVar");
				}
			}

			s = getOptionalStringField(existingStringFromVar, execution, "existingStringFromVar");
			Assert.assertEquals("Jane", s);

			s = getOptionalStringField(nonExistingStringFromVar, execution, "nonExistingStringFromVar");
			Assert.assertEquals(null, s);

			/*********************************************************************/
			msoLogger.debug("Running Integer Field Tests");
			/*********************************************************************/

			Integer i = getIntegerField(existingInteger, execution, "existingInteger");
			Assert.assertEquals((Integer)42, i);

			try {
				i = getIntegerField(nonExistingInteger, execution, "nonExistingInteger");
				Assert.fail("Expected BadInjectedFieldException for nonExistingInteger");
			} catch (Exception e) {
				if (!(e instanceof BadInjectedFieldException)) {
					Assert.fail("Expected BadInjectedFieldException for nonExistingInteger");
				}
			}

			i = getOptionalIntegerField(existingInteger, execution, "existingInteger");
			Assert.assertEquals((Integer)42, i);

			i = getOptionalIntegerField(nonExistingInteger, execution, "nonExistingInteger");
			Assert.assertEquals(null, i);

			/*********************************************************************/
			msoLogger.debug("Running Integer Expression Tests");
			/*********************************************************************/

			i = getIntegerField(existingIntegerFromVar, execution, "existingIntegerFromVar");
			Assert.assertEquals((Integer)25, i);

			try {
				i = getIntegerField(nonExistingIntegerFromVar, execution, "nonExistingIntegerFromVar");
				Assert.fail("Expected BadInjectedFieldException for nonExistingInteger");
			} catch (Exception e) {
				if (!(e instanceof BadInjectedFieldException)) {
					Assert.fail("Expected BadInjectedFieldException for nonExistingIntegerFromVar");
				}
			}

			i = getOptionalIntegerField(existingIntegerFromVar, execution, "existingIntegerFromVar");
			Assert.assertEquals((Integer)25, i);

			i = getOptionalIntegerField(nonExistingIntegerFromVar, execution, "nonExistingIntegerFromVar");
			Assert.assertEquals(null, i);

			/*********************************************************************/
			msoLogger.debug("Running Long Field Tests");
			/*********************************************************************/

			Long l = getLongField(existingLong, execution, "existingLong");
			Assert.assertEquals((Long)123456789L, l);

			try {
				l = getLongField(nonExistingLong, execution, "nonExistingLong");
				Assert.fail("Expected BadInjectedFieldException for nonExistingLong");
			} catch (Exception e) {
				if (!(e instanceof BadInjectedFieldException)) {
					Assert.fail("Expected BadInjectedFieldException for nonExistingLong");
				}
			}

			l = getOptionalLongField(existingLong, execution, "existingLong");
			Assert.assertEquals((Long)123456789L, l);

			l = getOptionalLongField(nonExistingLong, execution, "nonExistingLong");
			Assert.assertEquals(null, l);

			/*********************************************************************/
			msoLogger.debug("Running Long Expression Tests");
			/*********************************************************************/

			l = getLongField(existingLongFromVar, execution, "existingLongFromVar");
			Assert.assertEquals((Long)1438270117000L, l);

			try {
				l = getLongField(nonExistingLongFromVar, execution, "nonExistingLongFromVar");
				Assert.fail("Expected BadInjectedFieldException for nonExistingLong");
			} catch (Exception e) {
				if (!(e instanceof BadInjectedFieldException)) {
					Assert.fail("Expected BadInjectedFieldException for nonExistingLongFromVar");
				}
			}

			l = getOptionalLongField(existingLongFromVar, execution, "existingLongFromVar");
			Assert.assertEquals((Long)1438270117000L, l);

			l = getOptionalLongField(nonExistingLongFromVar, execution, "nonExistingLongFromVar");
			Assert.assertEquals(null, i);

			/*********************************************************************/
			msoLogger.debug("Running Output Variable Field Tests");
			/*********************************************************************/

			String var = getOutputField(existingOutputVar, execution, "existingOutputVar");
			Assert.assertEquals("goodVariable", var);

			try {
				var = getOutputField(nonExistingOutputVar, execution, "nonExistingOutputVar");
				Assert.fail("Expected BadInjectedFieldException for nonExistingString");
			} catch (Exception e) {
				if (!(e instanceof BadInjectedFieldException)) {
					Assert.fail("Expected BadInjectedFieldException for nonExistingString");
				}
			}

			var = getOptionalOutputField(existingOutputVar, execution, "existingOutputVar");
			Assert.assertEquals("goodVariable", var);

			var = getOptionalOutputField(nonExistingOutputVar, execution, "nonExistingOutputVar");
			Assert.assertEquals(null, var);

			try {
				var = getOutputField(existingBadOutputVar, execution, "existingBadOutputVar");
				Assert.fail("Expected BadInjectedFieldException for nonExistingString");
			} catch (Exception e) {
				if (!(e instanceof BadInjectedFieldException)) {
					Assert.fail("Expected BadInjectedFieldException for nonExistingString");
				}
			}

			msoLogger.debug("Finished executing " + getClass().getSimpleName());
		}
	}
}
