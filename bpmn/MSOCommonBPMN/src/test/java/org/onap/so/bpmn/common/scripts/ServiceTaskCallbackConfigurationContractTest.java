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
package org.onap.so.bpmn.common.scripts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.core.WorkflowException;

public class ServiceTaskCallbackConfigurationContractTest {
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();
    private final AbstractServiceTaskProcessor script = new AbstractServiceTaskProcessor() {
        @Override
        public void preProcessRequest(DelegateExecution ignored) {}
    };

    @Before
    public void useIndependentStatefulWorkflowVariables() {
        variables.put("testProcessKey", "callback-consumer");
        variables.put("prefix", "Current_");
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
    }

    @Test
    public void internalCallbackEncodesEachPathSegmentWithoutChangingTheEndpoint() {
        variables.put("mso.workflow.message.endpoint", "https://internal.example/messages///");
        assertEquals("https://internal.example/messages/type%20%2F%3F/id%23%25%E6%9D%B1%E4%BA%AC",
                script.createCallbackURL(execution, "type /?", "id#%東京"));
    }

    @Test
    public void externalCallbacksUseTheirOwnEndpointAndTheSameSegmentEncoding() {
        variables.put("mso.workflow.message.endpoint", "https://internal.example/messages");
        variables.put("mso.adapters.workflow.message.endpoint", "https://external.example/callbacks///");
        assertEquals("https://external.example/callbacks/type%20%2F%3F/id%23%25%E6%9D%B1%E4%BA%AC",
                script.createWorkflowMessageAdapterCallbackURL(execution, "type /?", "id#%東京"));
    }

    @Test
    public void zeroOneOrSeveralTrailingSlashesProduceTheSameInternalCallback() {
        for (String suffix : new String[] {"", "/", "////"}) {
            variables.put("mso.workflow.message.endpoint", "http://internal.example/path" + suffix);
            assertEquals("http://internal.example/path/type/id", script.createCallbackURL(execution, "type", "id"));
        }
    }

    @Test
    public void zeroOneOrSeveralTrailingSlashesProduceTheSameExternalCallback() {
        for (String suffix : new String[] {"", "/", "////"}) {
            variables.put("mso.adapters.workflow.message.endpoint", "http://external.example/path" + suffix);
            assertEquals("http://external.example/path/type/id",
                    script.createWorkflowMessageAdapterCallbackURL(execution, "type", "id"));
        }
    }

    @Test
    public void missingOrEmptyInternalEndpointRetainsTheConfigurationError() {
        for (String endpoint : new String[] {null, ""}) {
            variables.put("mso.workflow.message.endpoint", endpoint);
            configurationError("mso:workflow:message:endpoint URN mapping is not set",
                    () -> script.createCallbackURL(execution, "type", "id"));
        }
    }

    @Test
    public void missingOrEmptyExternalEndpointRetainsTheConfigurationError() {
        for (String endpoint : new String[] {null, ""}) {
            variables.put("mso.adapters.workflow.message.endpoint", endpoint);
            configurationError("mso:adapters:workflow:message:endpoint URN mapping is not set",
                    () -> script.createWorkflowMessageAdapterCallbackURL(execution, "type", "id"));
        }
    }

    @Test
    public void booleanDisableOverridesAnEnabledGlobalRollbackDefault() {
        rollback("true", true, false);
    }

    @Test
    public void booleanEnableOverridesADisabledGlobalRollbackDefault() {
        rollback("false", false, true);
    }

    @Test
    public void absentAndEmptyOverridesUseTheTypedGlobalDefault() {
        for (Object override : new Object[] {null, ""}) {
            rollback("true", override, true);
            rollback("false", override, false);
        }
    }

    @Test
    public void nonBooleanOverridesKeepTheGlobalDefaultAndDoNotRewriteTheInput() {
        for (Object override : new Object[] {"true", "false", 0}) {
            rollback("true", override, true);
            rollback("false", override, false);
        }
    }

    private void rollback(String global, Object override, boolean expected) {
        variables.put("mso.rollback", global);
        variables.put("disableRollback", override);
        variables.put("Other_backoutOnFailure", "unrelated");
        script.setRollbackEnabled(execution, false);
        Object value = variables.get("Current_backoutOnFailure");
        assertEquals(Boolean.class, value.getClass());
        assertEquals(expected, value);
        assertEquals(override, variables.get("disableRollback"));
        assertEquals("unrelated", variables.get("Other_backoutOnFailure"));
    }

    private void configurationError(String message, org.junit.function.ThrowingRunnable call) {
        BpmnError thrown = assertThrows(BpmnError.class, call);
        assertEquals("2000", thrown.getErrorCode());
        WorkflowException error = (WorkflowException) variables.get("WorkflowException");
        assertEquals("callback-consumer", error.getProcessKey());
        assertEquals(2000, error.getErrorCode());
        assertEquals(message, error.getErrorMessage());
    }
}
