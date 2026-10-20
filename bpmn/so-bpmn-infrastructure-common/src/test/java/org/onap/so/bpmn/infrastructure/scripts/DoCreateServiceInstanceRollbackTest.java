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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.core.RollbackData;
import org.onap.so.bpmn.core.WorkflowException;

public class DoCreateServiceInstanceRollbackTest {
    private final DoCreateServiceInstanceRollback script = new DoCreateServiceInstanceRollback();
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();
    private final RollbackData rollback = new RollbackData();

    @Before
    public void configureExecution() {
        variables.put("disableRollback", "false");
        variables.put("rollbackData", rollback);
        rollback.put("SERVICEINSTANCE", "serviceInstanceId", "service-1");
        rollback.put("SERVICEINSTANCE", "subscriptionServiceType", "München & SO");
        rollback.put("SERVICEINSTANCE", "globalSubscriberId", "customer-1");
        rollback.put("SERVICEINSTANCE", "rollbackAAI", "true");
        rollback.put("SERVICEINSTANCE", "rollbackSDNC", "true");
        rollback.put("SERVICEINSTANCE", "sdncDelete", "<delete><id>service-1</id></delete>");
        rollback.put("SERVICEINSTANCE", "sdncDeactivate", "<deactivate><id>service-1</id></deactivate>");
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
    }

    @Test
    public void preprocessingKeepsResourceIdentityAndConvertsEligibilityToBooleans() {
        script.preProcessRequest(execution);

        assertEquals("DCRESIRB_", variables.get("prefix"));
        for (String key : new String[] {"serviceInstanceId", "subscriptionServiceType", "globalSubscriberId",
                "sdncDelete", "sdncDeactivate"}) {
            assertEquals(rollback.get("SERVICEINSTANCE", key), variables.get(key));
        }
        assertSame(Boolean.TRUE, variables.get("rollbackAAI"));
        assertSame(Boolean.TRUE, variables.get("rollbackSDNC"));
        assertFalse(variables.containsKey("skipRollback"));
        assertSame(rollback, variables.get("rollbackData"));
    }

    @Test
    public void missingRollbackDataSkipsBothExternalCompensations() {
        variables.remove("rollbackData");

        script.preProcessRequest(execution);

        assertSkipped();
        assertFalse(variables.containsKey("serviceInstanceId"));
    }

    @Test
    public void unrelatedRollbackDataDoesNotEnableServiceInstanceDeletion() {
        variables.put("rollbackData", new RollbackData());

        script.preProcessRequest(execution);

        assertSkipped();
        assertFalse(variables.containsKey("serviceInstanceId"));
    }

    @Test
    public void noCreatedResourcesSkipsRollbackAndDoesNotEnableEitherAction() {
        rollback.put("SERVICEINSTANCE", "rollbackAAI", "false");
        rollback.put("SERVICEINSTANCE", "rollbackSDNC", "false");

        script.preProcessRequest(execution);

        assertSkipped();
    }

    @Test
    public void onlyEligibleCompensationIsEnabled() {
        rollback.put("SERVICEINSTANCE", "rollbackSDNC", "false");

        script.preProcessRequest(execution);

        assertSame(Boolean.TRUE, variables.get("rollbackAAI"));
        assertSame(Boolean.FALSE, variables.get("rollbackSDNC"));
        assertFalse(variables.containsKey("skipRollback"));
    }

    @Test
    public void explicitDisableSkipsRollbackEvenWhenResourcesAreEligible() {
        variables.put("disableRollback", "true");

        script.preProcessRequest(execution);

        assertSame(Boolean.TRUE, variables.get("skipRollback"));
    }

    @Test
    public void successfulCompensationClearsRollbackDataAndRetainsTheOriginalError() {
        WorkflowException original = new WorkflowException("CreateService", 7000, "Creation failed");
        variables.put("WorkflowException", original);
        script.preProcessRequest(execution);
        variables.put("GENDS_SuccessIndicator", true);

        script.postProcessRequest(execution);

        assertSame(Boolean.TRUE, variables.get("rolledBack"));
        assertTrue(variables.containsKey("rollbackData"));
        assertNull(variables.get("rollbackData"));
        assertSame(original, variables.get("WorkflowException"));
        verify(execution, never()).removeVariable("rollbackData");
    }

    @Test
    public void failedAaiCompensationCannotBeReportedAsSuccessfulRollback() {
        WorkflowException original = new WorkflowException("CreateService", 7000, "Creation failed");
        variables.put("WorkflowException", original);
        script.preProcessRequest(execution);
        variables.put("GENDS_SuccessIndicator", false);

        script.postProcessRequest(execution);

        assertSame(Boolean.FALSE, variables.get("rolledBack"));
        assertEquals("Error deleting service-instance in AAI for rollback", variables.get("rollbackError"));
        assertNull(variables.get("rollbackData"));
        assertSame(original, variables.get("WorkflowException"));
    }

    @Test
    public void sdncOnlyCompensationDoesNotRequireAnAaiSuccessFlag() {
        variables.put("rollbackAAI", false);
        variables.put("rollbackSDNC", true);

        script.postProcessRequest(execution);

        assertSame(Boolean.TRUE, variables.get("rolledBack"));
        verify(execution, never()).getVariable("GENDS_SuccessIndicator");
    }

    @Test
    public void javaCleanupFailureRetainsTheOriginalWorkflowError() {
        WorkflowException original = new WorkflowException("CreateService", 7000, "Creation failed");
        variables.put("WorkflowException", original);

        script.processRollbackJavaException(execution);

        assertTrue(variables.containsKey("rollbackData"));
        assertNull(variables.get("rollbackData"));
        assertEquals("Caught Java exception in ServiceInstance Create Rollback", variables.get("rollbackError"));
        assertSame(original, variables.get("WorkflowException"));
    }

    private void assertSkipped() {
        assertSame(Boolean.TRUE, variables.get("skipRollback"));
        assertSame(Boolean.FALSE, variables.get("rollbackAAI"));
        assertSame(Boolean.FALSE, variables.get("rollbackSDNC"));
    }
}
