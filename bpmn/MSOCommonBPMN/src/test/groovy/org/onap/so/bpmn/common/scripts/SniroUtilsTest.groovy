/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG
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

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.onap.so.bpmn.core.domain.*

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue
import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.Silent.class)
class SniroUtilsTest extends MsoGroovyTest {

    SniroUtils sniroUtils

    @Before
    void init() throws IOException {
        super.init("SniroUtils")
        MockitoAnnotations.initMocks(this)
        sniroUtils = new SniroUtils(new OofHoming())
    }

    @Test
    void testValidateCallbackResponse_validSolutionWithPlacements() {
        ExecutionEntity mockExecution = setupMock("SniroUtils")
        String response = """{
            "solutionInfo": {
                "placementInfo": [{"resourceModuleName": "vGMuxInfra", "solution": {"cloudOwner": "att-aic"}}],
                "licenseInfo": []
            }
        }"""

        sniroUtils.validateCallbackResponse(mockExecution, response)
        // Should return without exception
    }

    @Test
    void testValidateCallbackResponse_validSolutionWithLicenses() {
        ExecutionEntity mockExecution = setupMock("SniroUtils")
        String response = """{
            "solutionInfo": {
                "placementInfo": [],
                "licenseInfo": [{"resourceModuleName": "vGMuxInfra", "license": {"entitlementPoolList": ["pool1"]}}]
            }
        }"""

        sniroUtils.validateCallbackResponse(mockExecution, response)
        // Should return without exception
    }

    @Test(expected = BpmnError.class)
    void testValidateCallbackResponse_emptyResponse() {
        ExecutionEntity mockExecution = setupMock("SniroUtils")

        sniroUtils.validateCallbackResponse(mockExecution, "")
    }

    @Test(expected = BpmnError.class)
    void testValidateCallbackResponse_nullResponse() {
        ExecutionEntity mockExecution = setupMock("SniroUtils")

        sniroUtils.validateCallbackResponse(mockExecution, null)
    }

    @Test(expected = BpmnError.class)
    void testValidateCallbackResponse_policyException() {
        ExecutionEntity mockExecution = setupMock("SniroUtils")
        String response = """{
            "requestError": {
                "policyException": {
                    "text": "No viable placement found"
                }
            }
        }"""

        sniroUtils.validateCallbackResponse(mockExecution, response)
    }

    @Test(expected = BpmnError.class)
    void testValidateCallbackResponse_serviceException() {
        ExecutionEntity mockExecution = setupMock("SniroUtils")
        String response = """{
            "requestError": {
                "serviceException": {
                    "text": "Service not found"
                }
            }
        }"""

        sniroUtils.validateCallbackResponse(mockExecution, response)
    }

    @Test(expected = BpmnError.class)
    void testValidateCallbackResponse_unknownError() {
        ExecutionEntity mockExecution = setupMock("SniroUtils")
        String response = """{"unknownField": "value"}"""

        sniroUtils.validateCallbackResponse(mockExecution, response)
    }
}
