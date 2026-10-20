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
package org.onap.so.bpmn.vcpe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class VcpeWorkflowCleanupTest {
    private final AbstractTestBase fixture = new AbstractTestBase();
    private final RuntimeService runtime = mock(RuntimeService.class);
    private final ProcessInstance process = mock(ProcessInstance.class);
    private final ProcessInstanceQuery query = mock(ProcessInstanceQuery.class);
    private final Map<String, Object> inputs = new HashMap<>();

    @Before
    public void configureRuntime() {
        inputs.put("mso-request-id", "request-1");
        ReflectionTestUtils.setField(fixture, "runtimeService", runtime);
        when(runtime.startProcessInstanceByKey("workflow", "request-1", inputs)).thenReturn(process);
        when(process.getId()).thenReturn("owned-1");
        when(runtime.createProcessInstanceQuery()).thenReturn(query);
        when(query.processInstanceId("owned-1")).thenReturn(query);
    }

    @Test
    public void cleanupDeletesOnlyAnActiveOwnedInstanceAndIsIdempotent() {
        when(query.count()).thenReturn(1L);
        assertEquals("owned-1", fixture.invokeSubProcess("workflow", inputs));
        fixture.removeOwnedInstances();
        fixture.removeOwnedInstances();
        verify(runtime, times(1)).deleteProcessInstance("owned-1", "Test cleanup");
        verify(query, times(1)).processInstanceId("owned-1");
        verify(query, never()).list();
    }

    @Test
    public void cleanupDoesNotDeleteAnAlreadyFinishedOwnedInstance() {
        when(query.count()).thenReturn(0L);
        fixture.invokeSubProcess("workflow", inputs);
        fixture.removeOwnedInstances();
        verify(runtime, never()).deleteProcessInstance(anyString(), anyString());
    }

    @Test
    public void failedProcessStartDoesNotRegisterAnInstanceForCleanup() {
        when(runtime.startProcessInstanceByKey("workflow", "request-1", inputs))
                .thenThrow(new IllegalStateException("startup failed"));
        try {
            fixture.invokeSubProcess("workflow", inputs);
            fail("Expected the start failure");
        } catch (IllegalStateException expected) {
            assertEquals("startup failed", expected.getMessage());
        }
        fixture.removeOwnedInstances();
        verify(runtime, never()).createProcessInstanceQuery();
        verify(runtime, never()).deleteProcessInstance(anyString(), anyString());
    }
}
