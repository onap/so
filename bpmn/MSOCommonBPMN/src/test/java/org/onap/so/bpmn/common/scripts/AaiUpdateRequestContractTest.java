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
import static org.junit.Assert.assertNull;
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

public class AaiUpdateRequestContractTest {
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();

    @Before
    public void useStatefulPublicExecutionVariables() {
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
    }

    @Test
    public void genericRequestRetainsNamespacedIdentitiesAndOptionalValues() {
        generic("<w:UpdateAAIGenericVnfRequest xmlns:w='urn:onap:request'><w:vnf-id>vnf &amp; 雪</w:vnf-id>"
                + "<w:persona-model-id>model</w:persona-model-id><w:persona-model-version>0</w:persona-model-version>"
                + "<w:ipv4-oam-address>192.0.2.1</w:ipv4-oam-address><w:management-v6-address>2001:db8::1</w:management-v6-address>"
                + "<w:orchestration-status>false</w:orchestration-status></w:UpdateAAIGenericVnfRequest>");
        assertEquals("UAAIGenVnf_", variables.get("prefix"));
        assertEquals("vnf & 雪", variables.get("UAAIGenVnf_vnfId"));
        assertEquals("model", variables.get("UAAIGenVnf_personaModelId"));
        assertEquals("0", variables.get("UAAIGenVnf_personaModelVersion"));
        assertEquals("192.0.2.1", variables.get("UAAIGenVnf_ipv4OamAddress"));
        assertEquals("2001:db8::1", variables.get("UAAIGenVnf_managementV6Address"));
        assertEquals("false", variables.get("UAAIGenVnf_orchestrationStatus"));
    }

    @Test
    public void genericInitializationClearsEarlierOptionalInputsAndResponses() {
        variables.put("unrelated", false);
        variables.put("UAAIGenVnf_ipv4OamAddress", "stale");
        variables.put("UAAIGenVnf_personaModelId", "stale");
        variables.put("UAAIGenVnf_updateGenericVnfResponseCode", 500);
        variables.put("UAAIGenVnf_updateGenericVnfResponse", "stale");
        generic("<request><vnf-id>vnf-1</vnf-id><ipv4-oam-address/><persona-model-id/></request>");
        assertNull(variables.get("UAAIGenVnf_ipv4OamAddress"));
        assertNull(variables.get("UAAIGenVnf_personaModelId"));
        assertNull(variables.get("UAAIGenVnf_updateGenericVnfResponseCode"));
        assertEquals("", variables.get("UAAIGenVnf_updateGenericVnfResponse"));
        assertEquals(false, variables.get("unrelated"));
    }

    @Test
    public void genericMissingVnfIdRaisesTheRequiredElementError() {
        requiredError("UpdateAAIGenericVnf", "vnf-id", () -> generic("<request/>"));
    }

    @Test
    public void genericEmptyVnfIdRaisesTheRequiredElementError() {
        requiredError("UpdateAAIGenericVnf", "vnf-id", () -> generic("<request><vnf-id/></request>"));
    }

    @Test
    public void moduleRequestRetainsBothNamespacedIdentities() {
        module("<w:request xmlns:w='urn:onap:request'><w:vnf-id>vnf &amp; 雪</w:vnf-id>"
                + "<w:vf-module-id>module &lt;é&gt;</w:vf-module-id></w:request>");
        assertEquals("UAAIVfMod_", variables.get("prefix"));
        assertEquals("vnf & 雪", variables.get("UAAIVfMod_vnfId"));
        assertEquals("module <é>", variables.get("UAAIVfMod_vfModuleId"));
    }

    @Test
    public void moduleInitializationClearsEarlierQueryAndUpdateResults() {
        variables.put("UAAIVfMod_getVfModuleResponse", "stale");
        variables.put("UAAIVfMod_getVfModuleResponseCode", 404);
        variables.put("UAAIVfMod_updateVfModuleResponse", "stale");
        variables.put("UAAIVfMod_updateVfModuleResponseCode", 500);
        module("<request><vnf-id>vnf-1</vnf-id><vf-module-id>module-1</vf-module-id></request>");
        assertEquals("", variables.get("UAAIVfMod_getVfModuleResponse"));
        assertNull(variables.get("UAAIVfMod_getVfModuleResponseCode"));
        assertEquals("", variables.get("UAAIVfMod_updateVfModuleResponse"));
        assertNull(variables.get("UAAIVfMod_updateVfModuleResponseCode"));
    }

    @Test
    public void moduleMissingVnfIdRaisesTheRequiredElementError() {
        requiredError("UpdateAAIVfModule", "vnf-id",
                () -> module("<request><vf-module-id>module-1</vf-module-id></request>"));
    }

    @Test
    public void moduleEmptyVnfIdRaisesTheRequiredElementError() {
        requiredError("UpdateAAIVfModule", "vnf-id",
                () -> module("<request><vnf-id/><vf-module-id>module-1</vf-module-id></request>"));
    }

    @Test
    public void moduleMissingModuleIdRaisesTheRequiredElementError() {
        requiredError("UpdateAAIVfModule", "vf-module-id", () -> module("<request><vnf-id>vnf-1</vnf-id></request>"));
    }

    @Test
    public void moduleEmptyModuleIdRaisesTheRequiredElementError() {
        requiredError("UpdateAAIVfModule", "vf-module-id",
                () -> module("<request><vnf-id>vnf-1</vnf-id><vf-module-id/></request>"));
    }

    private void generic(String xml) {
        variables.put("testProcessKey", "UpdateAAIGenericVnf");
        variables.put("UpdateAAIGenericVnfRequest", xml);
        new UpdateAAIGenericVnf().preProcessRequest(execution);
    }

    private void module(String xml) {
        variables.put("testProcessKey", "UpdateAAIVfModule");
        variables.put("UpdateAAIVfModuleRequest", xml);
        new UpdateAAIVfModule().preProcessRequest(execution);
    }

    private void requiredError(String process, String element, org.junit.function.ThrowingRunnable action) {
        BpmnError error = assertThrows(BpmnError.class, action);
        assertEquals("2000", error.getErrorCode());
        Object value = variables.get("WorkflowException");
        assertEquals(WorkflowException.class, value.getClass());
        WorkflowException workflow = (WorkflowException) value;
        assertEquals(process, workflow.getProcessKey());
        assertEquals(2000, workflow.getErrorCode());
        assertEquals("Required element '" + element + "' is missing or empty", workflow.getErrorMessage());
    }
}
