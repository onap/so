/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
package org.onap.so.bpmn.infrastructure.scripts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Map;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.core.WorkflowException;

public class NetworkAndVfModuleRollbackContractTest {
    private static final String PREFIX = "CRENWKI_";
    private static final String[] REQUESTS =
            {"rollbackSDNCRequest", "rollbackNetworkRequest", "rollbackActivateSDNCRequest"};
    private final DoCreateNetworkInstance network = new DoCreateNetworkInstance();
    private final DoCreateVfModule module = new DoCreateVfModule();
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();

    @Before
    public void configureExecution() {
        variables.put("testProcessKey", "RollbackContract");
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
    }

    @Test
    public void networkRollbackCopiesEveryPreparedRequestAndRetainsTheSavedError() {
        WorkflowException original = error(5310, "network creation failed");
        variables.put(PREFIX + "WorkflowException", original);
        Map<String, String> expected = new HashMap<>();
        for (String request : REQUESTS) {
            String payload = "<request>" + request + " Grüße &amp; 東京</request>";
            expected.put(request, payload);
            variables.put(PREFIX + request, payload);
        }
        Map<String, String> previous = new HashMap<>();
        previous.put("unrelated", "old rollback state");
        variables.put("rollbackData", previous);
        network.prepareRollbackData(execution);
        assertEquals(PREFIX, variables.get("prefix"));
        assertTrue(variables.get("rollbackData") instanceof Map);
        assertEquals(expected, variables.get("rollbackData"));
        assertNotSame(previous, variables.get("rollbackData"));
        assertEquals("old rollback state", previous.get("unrelated"));
        assertSame(original, variables.get("WorkflowException"));
    }

    @Test
    public void absentAndEmptyCompensationRequestsAreOmittedIndependently() {
        for (String request : REQUESTS) {
            for (String absent : new String[] {null, ""}) {
                for (String name : REQUESTS) {
                    variables.put(PREFIX + name, "prepared-" + name);
                }
                variables.put(PREFIX + request, absent);
                network.prepareRollbackData(execution);
                Map<?, ?> rollback = (Map<?, ?>) variables.get("rollbackData");
                assertEquals(2, rollback.size());
                assertFalse(rollback.containsKey(request));
                for (String name : REQUESTS) {
                    if (!name.equals(request)) {
                        assertEquals("prepared-" + name, rollback.get(name));
                    }
                }
            }
        }
    }

    @Test
    public void thePreviouslySavedNetworkErrorTakesPrecedenceWhenMarkingFailure() {
        WorkflowException original = error(5310, "original failure");
        variables.put("SavedWorkflowException1", original);
        variables.put("WorkflowException", error(7000, "cleanup failed"));
        network.setExceptionFlag(execution);
        assertSame(Boolean.TRUE, variables.get(PREFIX + "isException"));
        assertSame(original, variables.get(PREFIX + "WorkflowException"));
    }

    @Test
    public void theCurrentNetworkErrorIsSavedWhenNoEarlierErrorExists() {
        WorkflowException original = error(5310, "current failure");
        variables.put("WorkflowException", original);
        network.setExceptionFlag(execution);
        assertSame(Boolean.TRUE, variables.get(PREFIX + "isException"));
        assertSame(original, variables.get(PREFIX + "WorkflowException"));
    }

    @Test
    public void markingNetworkFailureWithoutAnErrorRetainsAnExplicitNullSavedValue() {
        network.setExceptionFlag(execution);
        assertSame(Boolean.TRUE, variables.get(PREFIX + "isException"));
        assertTrue(variables.containsKey(PREFIX + "WorkflowException"));
        assertNull(variables.get(PREFIX + "WorkflowException"));
    }

    @Test
    public void networkFailureRetainsTheOriginalBusinessCodeAfterRollback() {
        Assume.assumeTrue("SO-4446: network rollback must preserve the original business error code",
                Boolean.getBoolean("groovy.migration.verification"));
        assertOriginalNetworkError(false);
    }

    @Test
    public void aCleanupFailureCannotReplaceTheOriginalNetworkFailure() {
        Assume.assumeTrue("SO-4446: network cleanup failure must not replace the original request failure",
                Boolean.getBoolean("groovy.migration.verification"));
        assertOriginalNetworkError(true);
    }

    @Test
    public void vfModuleRollbackSavesTheTypedOriginalErrorWithoutClearingIt() {
        WorkflowException original = error(5310, "module creation failed");
        variables.put("WorkflowException", original);
        module.preProcessRollback(execution);
        assertSame(original, variables.get("prevWorkflowException"));
        assertSame(original, variables.get("WorkflowException"));
    }

    @Test
    public void vfModuleRollbackRestoresTheOriginalErrorAndClearsTheHandoff() {
        WorkflowException original = error(5310, "module creation failed");
        variables.put("prevWorkflowException", original);
        variables.put("WorkflowException", error(7000, "cleanup failed"));
        Map<String, String> rollback = new HashMap<>();
        rollback.put("resource", "module-1");
        variables.put("rollbackData", rollback);
        module.postProcessRollback(execution);
        assertSame(original, variables.get("WorkflowException"));
        assertTrue(variables.containsKey("rollbackData"));
        assertNull(variables.get("rollbackData"));
        assertEquals("module-1", rollback.get("resource"));
    }

    @Test
    public void vfModuleRollbackDoesNotTreatAnUntypedPreviousValueAsAWorkflowError() {
        WorkflowException current = error(7000, "cleanup failed");
        variables.put("prevWorkflowException", "legacy diagnostic");
        variables.put("WorkflowException", current);
        module.postProcessRollback(execution);
        assertSame(current, variables.get("WorkflowException"));
        assertTrue(variables.containsKey("rollbackData"));
        assertNull(variables.get("rollbackData"));
    }

    private void assertOriginalNetworkError(boolean cleanupFailed) {
        WorkflowException original = error(5310, "original network failure");
        variables.put(PREFIX + "isException", Boolean.TRUE);
        variables.put(PREFIX + "WorkflowException", original);
        variables.put("rollbackData", new HashMap<>());
        if (cleanupFailed) {
            variables.put("workflowException", error(7000, "cleanup also failed"));
        }
        try {
            network.postProcessResponse(execution);
            fail("Expected the network failure BPMN route");
        } catch (BpmnError failure) {
            assertEquals("MSOWorkflowException", failure.getErrorCode());
            assertSame(Boolean.FALSE, variables.get(PREFIX + "Success"));
            assertTrue(variables.containsKey("rollbackData"));
            assertNull(variables.get("rollbackData"));
            assertTrue(variables.get("WorkflowException") instanceof WorkflowException);
            WorkflowException actual = (WorkflowException) variables.get("WorkflowException");
            assertEquals("Retain the original business code and failure detail",
                    Arrays.asList(original.getErrorCode(), original.getErrorMessage()),
                    Arrays.asList(actual.getErrorCode(), actual.getErrorMessage()));
        }
    }

    private WorkflowException error(int code, String message) {
        return new WorkflowException("RollbackContract", code, message, "create");
    }
}
