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
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue
import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.Silent.class)
class OofUtilsTest extends MsoGroovyTest {

    OofUtils oofUtils

    @Before
    void init() throws IOException {
        super.init("OofUtils")
        MockitoAnnotations.initMocks(this)
        oofUtils = new OofUtils(new OofHoming())
    }

    @Test
    void testCreateCandidateJson_nullInputs() {
        String result = oofUtils.createCandidateJson(null, null, null)
        assertEquals("", result)
    }

    @Test
    void testValidateCallbackResponse_validSolution() {
        ExecutionEntity mockExecution = setupMock("OofUtils")
        String response = """{
            "solutions": {
                "placementSolutions": [[{"resourceModuleName":"vGMuxInfra"}]],
                "licenseSolutions": []
            }
        }"""

        oofUtils.validateCallbackResponse(mockExecution, response)
        // Should return without exception
    }

    @Test(expected = BpmnError.class)
    void testValidateCallbackResponse_emptyResponse() {
        ExecutionEntity mockExecution = setupMock("OofUtils")

        oofUtils.validateCallbackResponse(mockExecution, "")
    }

    @Test(expected = BpmnError.class)
    void testValidateCallbackResponse_nullResponse() {
        ExecutionEntity mockExecution = setupMock("OofUtils")

        oofUtils.validateCallbackResponse(mockExecution, null)
    }

    @Test(expected = BpmnError.class)
    void testValidateCallbackResponse_policyException() {
        ExecutionEntity mockExecution = setupMock("OofUtils")
        String response = """{
            "requestError": {
                "policyException": {
                    "text": "Placement not feasible"
                }
            }
        }"""

        oofUtils.validateCallbackResponse(mockExecution, response)
    }

    @Test(expected = BpmnError.class)
    void testValidateCallbackResponse_serviceException() {
        ExecutionEntity mockExecution = setupMock("OofUtils")
        String response = """{
            "requestError": {
                "serviceException": {
                    "text": "Internal OOF error"
                }
            }
        }"""

        oofUtils.validateCallbackResponse(mockExecution, response)
    }

    @Test(expected = BpmnError.class)
    void testValidateCallbackResponse_unknownAsyncResponse() {
        ExecutionEntity mockExecution = setupMock("OofUtils")
        String response = """{"unexpectedField": "value"}"""

        oofUtils.validateCallbackResponse(mockExecution, response)
    }
}
