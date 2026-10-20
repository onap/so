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
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.ws.rs.NotFoundException;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.beans.nsmf.NetworkType;
import org.onap.so.bpmn.core.WorkflowException;

public class AccessNssiModificationInputContractTest {
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();
    private final List<AAIResourceUri> existenceQueries = new ArrayList<>();
    private final List<AAIResourceUri> modelQueries = new ArrayList<>();
    private MockedConstruction<AAIResourcesClient> clients;
    private DoModifyAccessNSSI script;
    private boolean resourceExists = true;
    private RuntimeException readFailure;

    @Before
    public void configureContracts() {
        clients = mockConstruction(AAIResourcesClient.class, (client, context) -> {
            when(client.exists(any(AAIResourceUri.class))).thenAnswer(call -> {
                existenceQueries.add(call.getArgument(0));
                return resourceExists;
            });
            when(client.get(any(AAIResourceUri.class), eq(NotFoundException.class))).thenAnswer(call -> {
                modelQueries.add(call.getArgument(0));
                if (readFailure != null) {
                    throw readFailure;
                }
                return new AAIResultWrapper(
                        "{\"service-instance-id\":\"nssi-1\",\"model-version-id\":\"resolved-model\"}");
            });
        });
        script = new DoModifyAccessNSSI();
        variables.put("testProcessKey", "DoModifyAccessNSSI");
        variables.put("msoRequestId", "request-1");
        variables.put("globalSubscriberId", "customer-1");
        variables.put("serviceInstanceID", "nssi-1");
        variables.put("nsiId", "nsi-1");
        variables.put("networkType", NetworkType.ACCESS);
        variables.put("subscriptionServiceType", "5G");
        variables.put("modelUuid", "supplied-model");
        variables.put("jobId", "job-1");
        variables.put("unrelatedFalse", Boolean.FALSE);
        variables.put("unrelatedZero", Integer.valueOf(0));
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
    }

    @After
    public void closeInventoryBoundary() {
        if (clients != null) {
            clients.close();
        }
    }

    @Test
    public void allocationKeepsOrderedSnssaiValuesAndTypedOperationFlags() {
        preprocess(request("allocate"));
        assertEquals(Boolean.TRUE, variables.get("isModifyallocate"));
        assertFalse(variables.containsKey("isModifydeallocate"));
        assertFalse(variables.containsKey("isModifyreconfigure"));
        assertCommonState("allocate");
        assertTrue(clients.constructed().isEmpty());
    }

    @Test
    public void deallocationSelectsOnlyItsOwnBooleanRoute() {
        preprocess(request("deallocate"));
        assertEquals(Boolean.TRUE, variables.get("isModifydeallocate"));
        assertFalse(variables.containsKey("isModifyallocate"));
        assertFalse(variables.containsKey("isModifyreconfigure"));
        assertCommonState("deallocate");
        assertTrue(clients.constructed().isEmpty());
    }

    @Test
    public void reconfigurationRetainsTheSerializedResourceConfigurationAndItsTypes() {
        JSONObject configuration = new JSONObject().put("enabled", false).put("count", 0)
                .put("items", new JSONArray().put(1).put(2)).put("empty", "").put("name", "Größe & 東京");
        preprocess(request("reconfigure").put("resourceConfig", configuration));
        assertEquals(Boolean.TRUE, variables.get("isModifyreconfigure"));
        assertFalse(variables.containsKey("isModifyallocate"));
        assertFalse(variables.containsKey("isModifydeallocate"));
        assertTrue(variables.get("additionalProperties") instanceof String);
        JSONObject actual = new JSONObject((String) variables.get("additionalProperties"));
        assertTrue(configuration.similar(actual));
        assertEquals(Boolean.FALSE, actual.get("enabled"));
        assertEquals(Integer.valueOf(0), actual.get("count"));
        assertCommonState("reconfigure");
        assertTrue(clients.constructed().isEmpty());
    }

    @Test
    public void absentBlankOrUnsupportedActionFailsBeforeInventoryLookup() {
        variables.remove("modelUuid");
        for (String action : new String[] {null, "", " \t", "unsupported", "ALLOCATE"}) {
            JSONObject input = request("allocate");
            if (action == null) {
                input.remove("modifyAction");
            } else {
                input.put("modifyAction", action);
            }
            assertWorkflowFailure(() -> preprocess(input), 500);
            assertFalse(variables.containsKey("modelUuid"));
        }
        assertTrue(clients.constructed().isEmpty());
    }

    @Test
    public void malformedOrAbsentSliceParametersCannotTriggerInventoryLookup() {
        variables.remove("modelUuid");
        for (String input : new String[] {null, "", "not JSON", "[]", "null"}) {
            variables.put("sliceParams", input);
            assertWorkflowFailure(() -> script.preProcessRequest(execution), 500);
        }
        assertTrue(clients.constructed().isEmpty());
    }

    @Test
    public void absentOrBlankSliceProfileUsesTheBusinessValidationError() {
        for (String profile : new String[] {null, "", " \t"}) {
            JSONObject input = request("allocate");
            if (profile == null) {
                input.remove("sliceProfileId");
            } else {
                input.put("sliceProfileId", profile);
            }
            assertWorkflowFailure(() -> preprocess(input), 500);
            assertFalse(variables.containsKey("sliceProfileId"));
            assertFalse(variables.containsKey("snssaiList"));
        }
        assertTrue(clients.constructed().isEmpty());
    }

    @Test
    public void anEmptySnssaiListUsesTheBusinessValidationError() {
        assertWorkflowFailure(() -> preprocess(request("allocate").put("snssaiList", new JSONArray())), 500);
        assertFalse(variables.containsKey("snssaiList"));
        assertTrue(clients.constructed().isEmpty());
    }

    @Test
    public void anAbsentSnssaiListUsesTheSameValidationErrorAsAnEmptyList() {
        Assume.assumeTrue("SO-4446: absent mandatory SNSSAI list must retain validation error 500",
                Boolean.getBoolean("groovy.migration.verification"));
        JSONObject input = request("allocate");
        input.remove("snssaiList");
        assertWorkflowFailure(() -> preprocess(input), 500);
        assertTrue(clients.constructed().isEmpty());
    }

    @Test
    public void missingOptionalNamesBecomeExplicitNullVariables() {
        JSONObject input = request("allocate");
        input.remove("nsiInfo");
        input.remove("scriptName");
        preprocess(input);
        assertTrue(variables.containsKey("nsiName"));
        assertTrue(variables.containsKey("scriptName"));
        assertNull(variables.get("nsiName"));
        assertNull(variables.get("scriptName"));
        assertTrue(clients.constructed().isEmpty());
    }

    @Test
    public void aSuppliedModelNeverCausesAnInventoryReadForAnySupportedAction() {
        for (String action : new String[] {"allocate", "deallocate", "reconfigure"}) {
            preprocess(request(action));
            assertEquals("supplied-model", variables.get("modelUuid"));
        }
        assertTrue(clients.constructed().isEmpty());
    }

    @Test
    public void absentOrBlankModelsResolveTheModelFromTheRequestedNssi() {
        for (String missing : new String[] {null, "", " \t"}) {
            variables.put("modelUuid", missing);
            preprocess(request("allocate"));
            assertEquals("resolved-model", variables.get("modelUuid"));
        }
        assertEquals(3, clients.constructed().size());
        assertEquals(3, existenceQueries.size());
        assertEquals(3, modelQueries.size());
        for (AAIResourceUri uri : modelQueries) {
            assertTrue(uri.build().toString().endsWith("/business/customers/customer/customer-1/"
                    + "service-subscriptions/service-subscription/5G/service-instances/service-instance/nssi-1"));
        }
        assertNoInventoryMutations();
    }

    @Test
    public void missingInventoryRetainsItsNotFoundBusinessError() {
        resourceExists = false;
        variables.remove("modelUuid");
        assertWorkflowFailure(() -> preprocess(request("allocate")), 2500);
        assertTrue(((WorkflowException) variables.get("WorkflowException")).getErrorMessage().contains("nssi-1"));
        assertEquals(1, existenceQueries.size());
        assertTrue(modelQueries.isEmpty());
        assertFalse(variables.containsKey("sliceProfileId"));
        assertNoInventoryMutations();
    }

    @Test
    public void inventoryReadFailureUsesTheTransportErrorBeforePublishingProfileState() {
        readFailure = new IllegalStateException("inventory unavailable");
        variables.remove("modelUuid");
        assertWorkflowFailure(() -> preprocess(request("allocate")), 7000);
        assertTrue(((WorkflowException) variables.get("WorkflowException")).getErrorMessage()
                .contains("inventory unavailable"));
        assertEquals(1, existenceQueries.size());
        assertEquals(1, modelQueries.size());
        assertFalse(variables.containsKey("sliceProfileId"));
        assertNoInventoryMutations();
    }

    private JSONObject request(String action) {
        return new JSONObject().put("modifyAction", action).put("sliceProfileId", "profile-1")
                .put("snssaiList", new JSONArray().put("001-000001").put("002-000002"))
                .put("nsiInfo", new JSONObject().put("nsiName", "東京 & slice")).put("scriptName", "access-script")
                .put("resourceConfig", new JSONObject());
    }

    private void preprocess(JSONObject input) {
        variables.put("sliceParams", input.toString());
        script.preProcessRequest(execution);
    }

    private void assertCommonState(String action) {
        assertEquals(action, variables.get("modifyAction"));
        assertEquals("MASS_", variables.get("prefix"));
        assertTrue(variables.get("startTime") instanceof Long);
        assertTrue(((Long) variables.get("startTime")).longValue() > 0);
        assertEquals(Integer.valueOf(10), variables.get("job_timeout"));
        assertEquals(Boolean.FALSE, variables.get("ranNssiPreferReuse"));
        assertEquals("profile-1", variables.get("sliceProfileId"));
        assertTrue(variables.get("snssaiList") instanceof List);
        assertEquals(Arrays.asList("001-000001", "002-000002"), variables.get("snssaiList"));
        assertEquals("東京 & slice", variables.get("nsiName"));
        assertEquals("access-script", variables.get("scriptName"));
        assertEquals(NetworkType.ACCESS, variables.get("networkType"));
        assertEquals(Boolean.FALSE, variables.get("unrelatedFalse"));
        assertEquals(Integer.valueOf(0), variables.get("unrelatedZero"));
        assertFalse(variables.containsKey("WorkflowException"));
    }

    private void assertWorkflowFailure(Runnable action, int code) {
        try {
            action.run();
            fail("Expected workflow error " + code);
        } catch (BpmnError expected) {
            assertEquals(String.valueOf(code), expected.getErrorCode());
            assertTrue(variables.get("WorkflowException") instanceof WorkflowException);
            assertEquals(code, ((WorkflowException) variables.get("WorkflowException")).getErrorCode());
        }
    }

    private void assertNoInventoryMutations() {
        for (AAIResourcesClient client : clients.constructed()) {
            verify(client, never()).create(any(AAIResourceUri.class), any());
            verify(client, never()).update(any(AAIResourceUri.class), any());
            verify(client, never()).delete(any(AAIResourceUri.class));
        }
    }
}
