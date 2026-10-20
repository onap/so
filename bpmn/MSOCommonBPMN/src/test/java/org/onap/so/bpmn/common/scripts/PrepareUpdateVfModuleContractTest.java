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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import jakarta.ws.rs.NotFoundException;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.VfModule;
import org.onap.aai.domain.yang.VfModules;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.bpmn.core.WorkflowException;

public class PrepareUpdateVfModuleContractTest {
    private static final String PREFIX = "PUAAIVfMod_";
    private final PrepareUpdateAAIVfModule script = new PrepareUpdateAAIVfModule();
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();

    @Before
    public void configureExecution() {
        variables.put("testProcessKey", "PrepareUpdateAAIVfModule");
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
        script.initProcessVariables(execution);
        variables.put(PREFIX + "vnfId", "vnf-1");
        variables.put(PREFIX + "vfModuleId", "module-1");
        variables.put(PREFIX + "orchestrationStatus", "pending-delete");
    }

    @Test
    public void initializationDistinguishesExplicitNullEmptyTextAndBooleanFalse() {
        script.initProcessVariables(execution);

        assertEquals(PREFIX, variables.get("prefix"));
        assertSame(Boolean.FALSE, variables.get(PREFIX + "vfModuleOK"));
        for (String name : new String[] {"vnfId", "vfModuleId", "vnfName", "orchestrationStatus", "vfModule",
                "vfModuleValidationError", "getVnfResponseCode", "updateVfModuleResponseCode", "outVfModule"}) {
            assertTrue(name, variables.containsKey(PREFIX + name));
            assertNull(name, variables.get(PREFIX + name));
        }
        assertEquals("", variables.get(PREFIX + "getVnfResponse"));
        assertEquals("", variables.get(PREFIX + "updateVfModuleResponse"));
    }

    @Test
    public void preprocessingDecodesXmlAndKeepsRequestFieldsAsStrings() {
        variables.put("PrepareUpdateAAIVfModuleRequest",
                "<request xmlns='urn:onap:so'>"
                        + "<vnf-id>vnf &amp; Gr\u00fc\u00dfe</vnf-id><vf-module-id>module &lt;1&gt;</vf-module-id>"
                        + "<orchestration-status>pending-delete</orchestration-status></request>");

        script.preProcessRequest(execution);

        assertEquals("vnf & Gr\u00fc\u00dfe", variables.get(PREFIX + "vnfId"));
        assertEquals("module <1>", variables.get(PREFIX + "vfModuleId"));
        assertEquals("pending-delete", variables.get(PREFIX + "orchestrationStatus"));
        assertSame(Boolean.FALSE, variables.get(PREFIX + "vfModuleOK"));
        assertFalse(variables.containsKey("WorkflowException"));
    }

    @Test
    public void genericVnfQueryUsesDepthOneAndStoresTheTypedAaiBean() {
        try (MockedConstruction<AAIResourcesClient> clients = mockConstruction(AAIResourcesClient.class,
                (client, context) -> when(client.get(any(AAIResourceUri.class), eq(NotFoundException.class)))
                        .thenReturn(
                                new AAIResultWrapper("{\"vnf-id\":\"vnf-1\",\"vnf-name\":\"Gr\\u00fc\\u00dfe\"}")))) {
            script.getGenericVnf(execution);

            assertEquals(Integer.valueOf(200), variables.get(PREFIX + "getVnfResponseCode"));
            GenericVnf response = (GenericVnf) variables.get(PREFIX + "getVnfResponse");
            assertEquals("vnf-1", response.getVnfId());
            assertEquals("Gr\u00fc\u00dfe", response.getVnfName());
            assertEquals(1, clients.constructed().size());
            ArgumentCaptor<AAIResourceUri> uri = ArgumentCaptor.forClass(AAIResourceUri.class);
            verify(clients.constructed().get(0)).get(uri.capture(), eq(NotFoundException.class));
            assertEquals("/network/generic-vnfs/generic-vnf/vnf-1", uri.getValue().build().getPath());
            assertEquals("depth=1", uri.getValue().build().getQuery());
            verifyNoMoreInteractions(clients.constructed().get(0));
        }
    }

    @Test
    public void absentModuleFailsValidationWithoutQueryingOrUpdatingIt() {
        variables.put(PREFIX + "getVnfResponse", genericVnf(1));
        try (MockedConstruction<AAIResourcesClient> clients = mockConstruction(AAIResourcesClient.class)) {
            script.validateVfModule(execution);

            assertSame(Boolean.FALSE, variables.get(PREFIX + "vfModuleOK"));
            assertEquals("VF Module 'module-1' does not exist in Generic VNF 'vnf-1'",
                    variables.get(PREFIX + "vfModuleValidationError"));
            assertEquals("VNF Gr\u00fc\u00dfe", variables.get(PREFIX + "vnfName"));
            assertNull(variables.get(PREFIX + "vfModule"));
            verify(clients.constructed().get(0)).exists(any(AAIResourceUri.class));
            verifyNoMoreInteractions(clients.constructed().get(0));
        }
    }

    @Test
    public void addOnModuleCanBeSelectedWhileOtherModulesExist() {
        validateModule(false, "active", 2);

        assertSame(Boolean.TRUE, variables.get(PREFIX + "vfModuleOK"));
        assertEquals("module-1", ((VfModule) variables.get(PREFIX + "vfModule")).getVfModuleId());
        assertNull(variables.get(PREFIX + "vfModuleValidationError"));
    }

    @Test
    public void soleBaseModuleCanBeSelectedForDeletion() {
        validateModule(true, "pending-delete", 1);

        assertSame(Boolean.TRUE, variables.get(PREFIX + "vfModuleOK"));
        assertTrue(variables.get(PREFIX + "vfModule") instanceof VfModule);
    }

    @Test
    public void baseModuleAlreadyPendingDeletionIsRejectedWhileAddOnsRemain() {
        validateModule(true, "pending-delete", 2);

        assertSame(Boolean.FALSE, variables.get(PREFIX + "vfModuleOK"));
        assertNull(variables.get(PREFIX + "vfModule"));
        assertEquals(
                "Orchestration status for VF Module 'module-1' cannot be set to 'pending-delete' since it is "
                        + "the base VF Module and it's not the only VF Module in Generic VNF 'vnf-1'",
                variables.get(PREFIX + "vfModuleValidationError"));
    }

    @Test
    public void updateRetainsResourceVersionAndHeatStackAndSendsExactlyOneAaiUpdate() {
        VfModule module = module(false, "active");
        variables.put(PREFIX + "vfModule", module);
        try (MockedConstruction<AAIResourcesClient> clients = mockConstruction(AAIResourcesClient.class)) {
            script.updateVfModule(execution);

            assertEquals(Integer.valueOf(200), variables.get(PREFIX + "updateVfModuleResponseCode"));
            assertSame(module, variables.get(PREFIX + "outVfModule"));
            assertSame(module, variables.get("WorkflowResponse"));
            assertEquals("stack-1", variables.get(PREFIX + "heatStackId"));
            assertEquals("pending-delete", module.getOrchestrationStatus());
            assertEquals("version-1", module.getResourceVersion());
            assertEquals("module-1", module.getVfModuleId());
            assertEquals(1, clients.constructed().size());
            ArgumentCaptor<AAIResourceUri> uri = ArgumentCaptor.forClass(AAIResourceUri.class);
            verify(clients.constructed().get(0)).update(uri.capture(), eq(module));
            assertEquals("/network/generic-vnfs/generic-vnf/vnf-1/vf-modules/vf-module/module-1",
                    uri.getValue().build().getPath());
            verifyNoMoreInteractions(clients.constructed().get(0));
        }
    }

    @Test
    public void aaiBpmnErrorSetsFailureCodeAndPreservesTheOriginalError() {
        VfModule module = module(false, "active");
        variables.put(PREFIX + "vfModule", module);
        BpmnError original = new BpmnError("AAI_TIMEOUT", "request timed out");
        try (MockedConstruction<AAIResourcesClient> clients =
                mockConstruction(AAIResourcesClient.class, (client, context) -> org.mockito.Mockito.doThrow(original)
                        .when(client).update(any(AAIResourceUri.class), any(VfModule.class)))) {
            try {
                script.updateVfModule(execution);
                fail("Expected the AAI BPMN error");
            } catch (BpmnError actual) {
                assertSame(original, actual);
            }
            assertEquals(Integer.valueOf(500), variables.get(PREFIX + "updateVfModuleResponseCode"));
            assertNull(variables.get(PREFIX + "outVfModule"));
            assertFalse(variables.containsKey("WorkflowResponse"));
            verify(clients.constructed().get(0)).update(any(AAIResourceUri.class), eq(module));
            verifyNoMoreInteractions(clients.constructed().get(0));
        }
    }

    @Test
    public void explicitFailureHandlersRetainTheProcessAndDetailedBusinessError() {
        variables.put(PREFIX + "getVnfResponseCode", 404);
        variables.put(PREFIX + "getVnfResponse", "VNF missing & Gr\u00fc\u00dfe");
        script.handleVnfNotFound(execution);
        assertWorkflowError("VNF missing & Gr\u00fc\u00dfe");

        variables.put(PREFIX + "vfModuleValidationError", "base module still has add-ons");
        script.handleVfModuleValidationError(execution);
        assertWorkflowError("VF Module validation error: base module still has add-ons");

        variables.put(PREFIX + "updateVfModuleResponseCode", 503);
        variables.put(PREFIX + "updateVfModuleResponse", "AAI unavailable");
        script.handleUpdateVfModuleFailure(execution);
        assertWorkflowError("AAI unavailable");
    }

    @Test
    public void requestedBaseModuleDeletionIsRejectedWhileAddOnsRemain() {
        Assume.assumeTrue("SO-4446: requested base-module deletion must be validated against the requested status",
                Boolean.getBoolean("groovy.migration.verification"));

        validateModule(true, "active", 2);

        assertSame("The requested pending-delete status must not bypass base-module validation", Boolean.FALSE,
                variables.get(PREFIX + "vfModuleOK"));
        assertNull(variables.get(PREFIX + "vfModule"));
    }

    @Test
    public void absentAaiBeanProducesTheDefinedQueryFailure() {
        Assume.assumeTrue("SO-4446: empty AAI results must not escape through the Groovy logging error handler",
                Boolean.getBoolean("groovy.migration.verification"));
        try (MockedConstruction<AAIResourcesClient> clients = mockConstruction(AAIResourcesClient.class,
                (client, context) -> when(client.get(any(AAIResourceUri.class), eq(NotFoundException.class)))
                        .thenReturn(new AAIResultWrapper("")))) {
            script.getGenericVnf(execution);

            assertEquals(Integer.valueOf(500), variables.get(PREFIX + "getVnfResponseCode"));
            assertEquals("AAI GET Failed:No value present", variables.get(PREFIX + "getVnfResponse"));
            verify(clients.constructed().get(0)).get(any(AAIResourceUri.class), eq(NotFoundException.class));
            verifyNoMoreInteractions(clients.constructed().get(0));
        }
    }

    @Test
    public void updateTransportFailureProducesTheDefinedWorkflowError() {
        Assume.assumeTrue("SO-4446: AAI update failures must not escape through the Groovy logging error handler",
                Boolean.getBoolean("groovy.migration.verification"));
        variables.put(PREFIX + "vfModule", module(false, "active"));
        try (MockedConstruction<AAIResourcesClient> clients = mockConstruction(AAIResourcesClient.class,
                (client, context) -> org.mockito.Mockito.doThrow(new IllegalStateException("AAI unavailable"))
                        .when(client).update(any(AAIResourceUri.class), any(VfModule.class)))) {
            try {
                script.updateVfModule(execution);
                fail("Expected the defined workflow error");
            } catch (BpmnError error) {
                assertEquals("1002", error.getErrorCode());
            }
            assertEquals(Integer.valueOf(500), variables.get(PREFIX + "updateVfModuleResponseCode"));
            WorkflowException error = (WorkflowException) variables.get("WorkflowException");
            assertEquals(1002, error.getErrorCode());
            assertEquals("Error in updateVfModule(): AAI unavailable", error.getErrorMessage());
            assertNull(variables.get(PREFIX + "outVfModule"));
            assertFalse(variables.containsKey("WorkflowResponse"));
            verify(clients.constructed().get(0)).update(any(AAIResourceUri.class), any(VfModule.class));
            verifyNoMoreInteractions(clients.constructed().get(0));
        }
    }

    private void validateModule(boolean base, String currentStatus, int moduleCount) {
        variables.put(PREFIX + "getVnfResponse", genericVnf(moduleCount));
        try (MockedConstruction<AAIResourcesClient> clients =
                mockConstruction(AAIResourcesClient.class, (client, context) -> {
                    when(client.exists(any(AAIResourceUri.class))).thenReturn(true);
                    when(client.get(any(AAIResourceUri.class), eq(NotFoundException.class)))
                            .thenReturn(new AAIResultWrapper(moduleJson(base, currentStatus)));
                })) {
            script.validateVfModule(execution);
            assertEquals(1, clients.constructed().size());
            ArgumentCaptor<AAIResourceUri> uri = ArgumentCaptor.forClass(AAIResourceUri.class);
            verify(clients.constructed().get(0)).exists(uri.capture());
            verify(clients.constructed().get(0)).get(eq(uri.getValue()), eq(NotFoundException.class));
            verify(clients.constructed().get(0), never()).update(any(AAIResourceUri.class), any(VfModule.class));
            verifyNoMoreInteractions(clients.constructed().get(0));
        }
    }

    private GenericVnf genericVnf(int moduleCount) {
        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId("vnf-1");
        vnf.setVnfName("VNF Gr\u00fc\u00dfe");
        vnf.setVfModules(new VfModules());
        for (int i = 0; i < moduleCount; i++) {
            vnf.getVfModules().getVfModule().add(new VfModule());
        }
        return vnf;
    }

    private VfModule module(boolean base, String currentStatus) {
        return new AAIResultWrapper(moduleJson(base, currentStatus)).asBean(VfModule.class).get();
    }

    private String moduleJson(boolean base, String currentStatus) {
        return "{\"vf-module-id\":\"module-1\",\"is-base-vf-module\":" + base + ",\"orchestration-status\":\""
                + currentStatus + "\",\"resource-version\":\"version-1\",\"heat-stack-id\":\"stack-1\"}";
    }

    private void assertWorkflowError(String message) {
        WorkflowException error = (WorkflowException) variables.get("WorkflowException");
        assertEquals("PrepareUpdateAAIVfModule", error.getProcessKey());
        assertEquals(5000, error.getErrorCode());
        assertEquals(message, error.getErrorMessage());
    }
}
