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
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.core.RollbackData;
import org.onap.so.bpmn.core.WorkflowException;
import org.w3c.dom.Document;

public class DoCreateVnfAndModulesRollbackTest {
    private final DoCreateVnfAndModulesRollback script = new DoCreateVnfAndModulesRollback();
    private final Map<String, Object> variables = new HashMap<>();
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final RollbackData rollback = new RollbackData();

    @Before
    public void configureExecution() {
        variables.put("testProcessKey", "DoCreateVnfAndModulesRollback");
        variables.put("rollbackData", rollback);
        variables.put("disableRollback", "false");
        variables.put("msoRequestId", "request-1");
        rollback.put("VNF", "vnfId", "vnf-1");
        rollback.put("VNF", "serviceInstanceId", "service-1");
        rollback.put("VNF", "tenantId", "tenant-1");
        rollback.put("VNF", "cloudSiteId", "region-1");
        rollback.put("VNF", "source", "VID");
        rollback.put("VNF", "sdncCallbackUrl", "http://localhost/callback");
        rollback.put("VNF", "rollbackSDNCAssign", "false");
        rollback.put("VNF", "rollbackSDNCActivate", "false");
        rollback.put("VNF", "rollbackVnfCreate", "true");
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
    }

    @Test
    public void preprocessingCopiesResourceIdentityAndKeepsEligibilityTypes() {
        script.preProcessRequest(execution);
        assertEquals("DCVAMR_", variables.get("prefix"));
        assertEquals("request-1", variables.get("mso-request-id"));
        for (String key : new String[] {"vnfId", "serviceInstanceId", "tenantId", "cloudSiteId", "source",
                "sdncCallbackUrl", "rollbackSDNCAssign", "rollbackSDNCActivate", "rollbackVnfCreate"}) {
            assertEquals(rollback.get("VNF", key), variables.get("DCVAMR_" + key));
        }
        assertEquals("true", variables.get("DCVAMR_rollbackVnfCreate"));
        assertEquals(Integer.valueOf(0), variables.get("DCVAMR_numOfAddOnModules"));
        assertEquals(Integer.valueOf(0), variables.get("DCVAMR_numOfModulesToDelete"));
        assertSame(Boolean.FALSE, variables.get("DCVAMR_aLaCarte"));
        assertFalse("Eligible compensation must not be skipped", Boolean.TRUE.equals(variables.get("skipRollback")));
        assertTrue(variables.containsKey("rolledBack"));
        assertNull(variables.get("rolledBack"));
        assertTrue(variables.containsKey("rollbackError"));
        assertNull(variables.get("rollbackError"));
    }

    @Test
    public void explicitlyDisabledRollbackSetsABooleanSkipFlag() {
        variables.put("disableRollback", "true");
        script.preProcessRequest(execution);
        assertSame(Boolean.TRUE, variables.get("skipRollback"));
    }

    @Test
    public void addonModulesAreSelectedInReverseOrderBeforeTheBaseModule() {
        rollback.put("VNFANDMODULES", "numOfCreatedAddOnModules", "2");
        rollback.put("VFMODULE_BASE", "vfModuleId", "base");
        rollback.put("VFMODULE_ADDON_1", "vfModuleId", "addon-1");
        rollback.put("VFMODULE_ADDON_2", "vfModuleId", "addon-2");
        script.preProcessRequest(execution);
        assertEquals(Integer.valueOf(3), variables.get("DCVAMR_numOfModulesToDelete"));
        for (String id : new String[] {"addon-2", "addon-1", "base"}) {
            script.preProcessCreateVfModuleRollback(execution);
            Object copied = variables.get("DCVAMR_RollbackData");
            assertTrue(copied instanceof RollbackData);
            assertEquals(id, ((RollbackData) copied).get("VFMODULE", "vfModuleId"));
            variables.put("DCVM_rolledBack", true);
            script.postProcessCreateVfModuleRollback(execution);
        }
        assertEquals(Integer.valueOf(0), variables.get("DCVAMR_numOfModulesToDelete"));
        assertEquals("base", rollback.get("VFMODULE_BASE", "vfModuleId"));
        assertEquals("addon-1", rollback.get("VFMODULE_ADDON_1", "vfModuleId"));
        assertEquals("addon-2", rollback.get("VFMODULE_ADDON_2", "vfModuleId"));
    }

    @Test
    public void invalidCreatedModuleCountUsesThePreprocessingErrorContract() {
        rollback.put("VNFANDMODULES", "numOfCreatedAddOnModules", "not-a-number");
        try {
            script.preProcessRequest(execution);
            fail("Expected invalid module count to fail");
        } catch (BpmnError error) {
            assertEquals("2500", error.getErrorCode());
            assertWorkflowError(2500, "Internal Error - Occured in DoCreateVnfAndModulesRollback PreProcessRequest");
        }
    }

    @Test
    public void failedModuleCompensationRaisesTheBusinessError() {
        variables.put("DCVAMR_numOfModulesToDelete", 2);
        variables.put("DCVM_rolledBack", false);
        try {
            script.postProcessCreateVfModuleRollback(execution);
            fail("Expected failed compensation to fail");
        } catch (BpmnError error) {
            assertEquals("1002", error.getErrorCode());
            assertWorkflowError(1002, "Error Occurred during rollback of DoCreateVfModule");
        }
    }

    @Test
    public void successfulCompensationSetsTypedStatusWithoutReplacingTheOriginalError() {
        WorkflowException original = new WorkflowException("DoCreateVnf", 7000, "Original failure");
        variables.put("WorkflowException", original);
        variables.put("rollbackError", "old cleanup error");
        script.setSuccessfulRollbackStatus(execution);
        assertSame(Boolean.TRUE, variables.get("rolledBack"));
        assertTrue(variables.containsKey("rollbackError"));
        assertNull(variables.get("rollbackError"));
        assertSame(original, variables.get("WorkflowException"));
        assertSame(rollback, variables.get("rollbackData"));
    }

    @Test
    public void failedCompensationKeepsItsSpecificErrorAndClearsRollbackData() {
        WorkflowException original = new WorkflowException("DoCreateVnf", 7000, "Original failure");
        variables.put("WorkflowException", original);
        variables.put("rollbackError", "Controller cleanup failed");
        script.setFailedRollbackStatus(execution);
        assertSame(Boolean.FALSE, variables.get("rolledBack"));
        assertEquals("Controller cleanup failed", variables.get("rollbackError"));
        assertSame(original, variables.get("WorkflowException"));
        assertTrue(variables.containsKey("rollbackData"));
        assertNull(variables.get("rollbackData"));
    }

    @Test
    public void failedCompensationHasAFallbackErrorWhenNoneWasProvided() {
        script.setFailedRollbackStatus(execution);
        assertSame(Boolean.FALSE, variables.get("rolledBack"));
        assertEquals("Caught exception in DoCreateVnfAndModulesRollback", variables.get("rollbackError"));
    }

    @Test
    public void controllerCleanupPayloadsKeepActionNamespacesAndEscapedIdentifiers() throws Exception {
        variables.put("msoRequestId", "Grüße & <request>");
        variables.put("vnfId", "vnf&1");
        variables.put("serviceInstanceId", "service&1");
        rollback.put("VNF", "vnfId", "vnf&1");
        rollback.put("VNF", "serviceInstanceId", "service&1");
        rollback.put("VNF", "tenantId", "tenant&1");
        script.preProcessRequest(execution);
        script.preProcessSDNCDeactivateRequest(execution);
        script.preProcessSDNCUnassignRequest(execution);
        for (String action : new String[] {"deactivate", "unassign"}) {
            String payload = (String) variables.get("DCVAMR_" + action + "SDNCRequest");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            Document document = factory.newDocumentBuilder()
                    .parse(new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8)));
            String adapter = "http://org.onap.so/workflow/sdnc/adapter/schema/v1";
            assertEquals("http://org.onap/so/workflow/schema/v1", document.getDocumentElement().getNamespaceURI());
            assertEquals(action, document.getElementsByTagNameNS(adapter, "SvcAction").item(0).getTextContent());
            assertEquals("service&1",
                    document.getElementsByTagNameNS(adapter, "SvcInstanceId").item(0).getTextContent());
            assertEquals("http://localhost/callback",
                    document.getElementsByTagNameNS(adapter, "CallbackUrl").item(0).getTextContent());
            assertEquals("Grüße & <request>", document.getElementsByTagName("request-id").item(0).getTextContent());
            assertEquals("vnf&1", document.getElementsByTagName("vnf-id").item(0).getTextContent());
            assertEquals("tenant&1", document.getElementsByTagName("tenant").item(0).getTextContent());
            assertTrue(document.getElementsByTagNameNS(adapter, "RequestId").item(0).getTextContent()
                    .startsWith("Grüße & <request>-"));
        }
        assertFalse(variables.containsKey("WorkflowException"));
    }

    private void assertWorkflowError(int code, String message) {
        Object stored = variables.get("WorkflowException");
        assertTrue(stored instanceof WorkflowException);
        WorkflowException error = (WorkflowException) stored;
        assertEquals("DoCreateVnfAndModulesRollback", error.getProcessKey());
        assertEquals(code, error.getErrorCode());
        assertEquals(message, error.getErrorMessage());
    }
}
