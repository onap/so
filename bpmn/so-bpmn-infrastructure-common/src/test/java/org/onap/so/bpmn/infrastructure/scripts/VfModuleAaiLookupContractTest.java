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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.bpmn.core.WorkflowException;

public class VfModuleAaiLookupContractTest {
    private static final String VNF = "/aai/v24/network/generic-vnfs/generic-vnf/vnf-contract";
    private static final String MODULES = VNF + "/vf-modules";
    private static final String NAME = "module & \u96ea";
    @Rule
    public WireMockRule aai = new WireMockRule(0);
    private final DoCreateVfModule script = new DoCreateVfModule();
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();
    private MockedStatic<UrnPropertiesReader> properties;

    @Before
    public void configureExecutionAndLocalInventory() {
        variables.put("testProcessKey", "DoCreateVfModule");
        variables.put("DCVFM_vnfId", "vnf-contract");
        variables.put("DCVFM_vfModuleName", NAME);
        variables.put("DCVFM_orchestrationStatus", "stale-status");
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
        properties = mockStatic(UrnPropertiesReader.class);
        properties.when(() -> UrnPropertiesReader.getVariable("aai.endpoint"))
                .thenReturn("http://localhost:" + aai.port());
        properties.when(() -> UrnPropertiesReader.getVariable("mso.workflow.global.default.aai.version"))
                .thenReturn("24");
    }

    @After
    public void restoreProperties() {
        if (properties != null) {
            properties.close();
        }
    }

    @Test
    public void matchingInventoryRetainsTypedStatusIdentityAndRawResponse() {
        String body = "<vf-modules xmlns='urn:aai'><vf-module><vf-module-id>module &amp; \u96ea</vf-module-id>"
                + "<orchestration-status>Created</orchestration-status></vf-module></vf-modules>";
        moduleResponse(200, body);
        script.queryAAIVfModuleForStatus(execution);
        assertEquals("Created", variables.get("DCVFM_orchestrationStatus"));
        assertEquals("module & \u96ea", variables.get("DCVFM_vfModuleId"));
        assertEquals(Integer.valueOf(200), variables.get("DCVFM_queryAAIVfModuleForStatusResponseCode"));
        assertEquals(body, variables.get("DCVFM_queryAAIVfModuleForStatusResponse"));
        assertFalse(variables.containsKey("WorkflowException"));
        assertRequest(MODULES, "vf-module-name", NAME);
    }

    @Test
    public void missingModuleClearsStatusAndKeepsTheNewlyAllocatedIdentity() {
        variables.put("DCVFM_vfModuleId", "newly-allocated-module");
        moduleResponse(404, "<requestError/>");
        script.queryAAIVfModuleForStatus(execution);
        assertEquals("", variables.get("DCVFM_orchestrationStatus"));
        assertEquals("newly-allocated-module", variables.get("DCVFM_vfModuleId"));
        assertEquals(Integer.valueOf(404), variables.get("DCVFM_queryAAIVfModuleForStatusResponseCode"));
        assertFalse(variables.containsKey("WorkflowException"));
        assertRequest(MODULES, "vf-module-name", NAME);
    }

    @Test
    public void unauthorizedInventoryIsNotTreatedAsAnAbsentModule() {
        readiness("SO-4457: VF-module status lookup ignores non-200 statuses including authorization failure");
        moduleResponse(403, "<requestError/>");
        error(() -> script.queryAAIVfModuleForStatus(execution));
        assertRequest(MODULES, "vf-module-name", NAME);
    }

    @Test
    public void serverFailureIsNotTreatedAsAnAbsentModule() {
        readiness("SO-4457: VF-module status lookup ignores server failure rather than failing the request");
        moduleResponse(503, "<requestError/>");
        error(() -> script.queryAAIVfModuleForStatus(execution));
        assertRequest(MODULES, "vf-module-name", NAME);
    }

    @Test
    public void malformedSuccessfulInventoryFailsWithTheLookupError() {
        moduleResponse(200, "<vf-modules><vf-module>");
        error(() -> script.queryAAIVfModuleForStatus(execution));
        assertEquals("", variables.get("DCVFM_orchestrationStatus"));
        assertRequest(MODULES, "vf-module-name", NAME);
    }

    @Test
    public void blankSuccessfulInventoryFailsWithTheLookupError() {
        moduleResponse(200, "");
        error(() -> script.queryAAIVfModuleForStatus(execution));
        assertEquals("", variables.get("DCVFM_orchestrationStatus"));
        assertRequest(MODULES, "vf-module-name", NAME);
    }

    @Test
    public void baseModuleLookupSelectsTheBaseRecordAndRetainsItsStackIdentity() {
        String body = "<generic-vnf xmlns='urn:aai'><vf-modules>"
                + "<vf-module><vf-module-id>ordinary</vf-module-id><is-base-vf-module>false</is-base-vf-module>"
                + "<heat-stack-id>ordinary-stack</heat-stack-id></vf-module>"
                + "<vf-module><vf-module-id>base &amp; \u96ea</vf-module-id><is-base-vf-module>true</is-base-vf-module>"
                + "<heat-stack-id>base-stack</heat-stack-id></vf-module></vf-modules></generic-vnf>";
        aai.stubFor(get(urlPathEqualTo(VNF)).withQueryParam("depth", equalTo("1"))
                .willReturn(aResponse().withStatus(200).withBody(body)));
        script.queryAAIVfModule(execution);
        assertEquals("base & \u96ea", variables.get("DCVFM_baseVfModuleId"));
        assertEquals("base-stack", variables.get("DCVFM_baseVfModuleHeatStackId"));
        assertEquals(Integer.valueOf(200), variables.get("DCVFM_queryAAIVfModuleResponseCode"));
        assertEquals(body, variables.get("DCVFM_queryAAIVfModuleResponse"));
        assertFalse(variables.containsKey("WorkflowException"));
        assertRequest(VNF, "depth", "1");
    }

    @Test
    public void baseInventoryServerFailureDoesNotSilentlyContinueCreation() {
        readiness("SO-4457: VF-module base lookup ignores an AAI server failure");
        aai.stubFor(get(urlPathEqualTo(VNF)).withQueryParam("depth", equalTo("1"))
                .willReturn(aResponse().withStatus(500).withBody("<requestError/>")));
        error(() -> script.queryAAIVfModule(execution));
        assertRequest(VNF, "depth", "1");
    }

    @Test
    public void unavailableInventoryTransportFailsWithoutClaimingAStatusResponse() {
        aai.stop();
        error(() -> script.queryAAIVfModuleForStatus(execution));
        assertEquals("", variables.get("DCVFM_orchestrationStatus"));
        assertFalse(variables.containsKey("DCVFM_queryAAIVfModuleForStatusResponseCode"));
    }

    private void moduleResponse(int status, String body) {
        aai.stubFor(get(urlPathEqualTo(MODULES)).withQueryParam("vf-module-name", equalTo(NAME)).willReturn(
                aResponse().withStatus(status).withHeader("Content-Type", "application/xml").withBody(body)));
    }

    private void error(Runnable action) {
        try {
            action.run();
            fail("Expected inventory lookup error");
        } catch (BpmnError error) {
            assertEquals("1002", error.getErrorCode());
        }
        WorkflowException error = (WorkflowException) variables.get("WorkflowException");
        assertNotNull(error);
        assertEquals(1002, error.getErrorCode());
        assertTrue(error.getErrorMessage(), error.getErrorMessage().contains("AAI GET Failed:"));
    }

    private void assertRequest(String path, String query, String value) {
        aai.verify(1,
                getRequestedFor(urlPathEqualTo(path)).withQueryParam(query, equalTo(value))
                        .withHeader("X-FromAppId", equalTo("MSO")).withHeader("Accept", containing("application/xml"))
                        .withHeader("Content-Type", containing("application/xml")));
        assertEquals(1, aai.getAllServeEvents().size());
        String id = aai.getAllServeEvents().get(0).getRequest().getHeader("X-TransactionId");
        assertNotNull(id);
        for (String transaction : id.split(",")) {
            String transactionId = transaction.trim();
            assertEquals(transactionId, UUID.fromString(transactionId).toString());
        }
    }

    private void readiness(String reason) {
        Assume.assumeTrue(reason, Boolean.getBoolean("groovy.migration.verification"));
    }
}
