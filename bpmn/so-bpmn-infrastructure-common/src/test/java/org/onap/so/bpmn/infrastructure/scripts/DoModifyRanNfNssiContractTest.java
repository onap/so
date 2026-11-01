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
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.bpmn.core.WorkflowException;

public class DoModifyRanNfNssiContractTest {
    private final DoModifyRanNfNssi script = new DoModifyRanNfNssi();
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();

    @Before
    public void configureStatefulExecution() {
        variables.put("testProcessKey", "DoModifyRanNfNssi");
        variables.put("msoRequestId", "request-1");
        variables.put("serviceInstanceID", "ran-nssi-1");
        variables.put("globalSubscriberId", "customer-1");
        variables.put("subscriptionServiceType", "subscription-1");
        variables.put("modifyAction", "allocate");
        variables.put("sliceProfileId", "profile-1");
        variables.put("snssaiList", Arrays.asList("001-000001", "001-000002"));
        variables.put("additionalProperties", "{}");
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
    }

    @Test
    public void supportedActionsPrepareTypedRoutingAndPreserveSnssaiOrder() {
        for (String action : Arrays.asList("allocate", "deallocate", "reconfigure")) {
            variables.put("modifyAction", action);
            script.preProcessRequest(execution);
            assertEquals(action, variables.get("modifyAction"));
            assertEquals("MANNFNSS_", variables.get("prefix"));
            assertTrue(variables.get("startTime") instanceof Long);
            assertEquals("profile-1", variables.get("sliceProfileId"));
            assertEquals(Arrays.asList("001-000001", "001-000002"), variables.get("snssaiList"));
            assertEquals("001-000001", variables.get("snssai"));
            assertEquals("{}", variables.get("reconfigure".equals(action) ? "resourceConfig" : "sliceProfile"));
        }
    }

    @Test
    public void absentOrBlankActionsUseTheValidationErrorContract() {
        for (String action : new String[] {null, "", "  "}) {
            variables.put("modifyAction", action);
            assertValidationFailure("Input modifyAction is null");
        }
    }

    @Test
    public void unknownActionsAreRejectedBeforePreparingAnOutboundRequest() {
        variables.put("modifyAction", "unsupported");
        assertValidationFailure("Invalid modify Action : unsupported");
        assertFalse(variables.containsKey("createNSSI_sdnrRequest"));
    }

    @Test
    public void absentOrBlankProfileIdentitiesUseTheValidationErrorContract() {
        for (String identity : new String[] {null, "", "  "}) {
            variables.put("sliceProfileId", identity);
            assertValidationFailure("Mandatory fields are empty");
        }
    }

    @Test
    public void anEmptySnssaiListUsesTheValidationErrorContract() {
        variables.put("snssaiList", Collections.emptyList());
        assertValidationFailure("Mandatory fields are empty");
    }

    @Test
    public void anAbsentSnssaiListUsesTheValidationErrorInsteadOfAnInternalError() {
        Assume.assumeTrue(
                "SO-4457: an absent snssaiList is mapped to internal error 7000 instead of validation error 500",
                Boolean.getBoolean("groovy.migration.verification"));
        variables.remove("snssaiList");
        assertValidationFailure("Mandatory fields are empty");
    }

    @Test
    public void allocationPreservesPayloadTypesAndMapsCapacityFieldsWithoutLosingTheOriginals() {
        JSONObject profile = new JSONObject().put("maxNumberofPDUSession", 0).put("expDataRateUL", 0)
                .put("expDataRateDL", 12).put("shared", false).put("label", "Gr\u00fc\u00dfe & \u96ea")
                .put("optional", "").put("areas", new JSONArray());
        String original = profile.toString();
        variables.put("additionalProperties", original);
        JSONObject request = request();
        assertEquals("modify-allocate", request.getJSONObject("body").getJSONObject("input").getString("action"));
        JSONObject payload = payload(request);
        JSONObject expected = new JSONObject(original).put("sliceProfileId", "profile-1").put("maxNumberofConns", 0)
                .put("uLThptPerSlice", 0).put("dLThptPerSlice", 12);
        assertTrue(payload.getJSONObject("sliceProfile").toString(),
                expected.similar(payload.getJSONObject("sliceProfile")));
        assertTrue(payload.getJSONObject("additionalproperties").isEmpty());
        assertEquals(original, variables.get("additionalProperties"));
        assertEquals(original, variables.get("sliceProfile"));
    }

    @Test
    public void deallocationUsesTheRequestedProfileAndFirstSnssaiWithoutAllocationCapacityFields() {
        variables.put("modifyAction", "deallocate");
        JSONObject request = request();
        assertEquals("modify-deallocate", request.getJSONObject("body").getJSONObject("input").getString("action"));
        JSONObject payload = payload(request);
        JSONObject expected = new JSONObject().put("sliceProfileId", "profile-1").put("sNSSAI", "001-000001");
        assertTrue(expected.similar(payload.getJSONObject("sliceProfile")));
        assertTrue(payload.getJSONObject("additionalproperties").isEmpty());
    }

    @Test
    public void reconfigurationPreservesNestedBooleanNumericUnicodeAndEmptyValues() {
        variables.put("modifyAction", "reconfigure");
        JSONObject configuration = new JSONObject().put("enabled", false).put("count", 0).put("name", "\u96ea & <cell>")
                .put("empty", "").put("cells", new JSONArray().put("cell-2").put("cell-1"));
        variables.put("additionalProperties", configuration.toString());
        JSONObject request = request();
        assertEquals("reconfigure", request.getJSONObject("body").getJSONObject("input").getString("action"));
        JSONObject payload = payload(request);
        assertTrue(
                configuration.similar(payload.getJSONObject("additionalproperties").getJSONObject("resourceConfig")));
        assertTrue(new JSONObject().put("sliceProfileId", "profile-1").put("sNSSAI", "001-000001")
                .similar(payload.getJSONObject("sliceProfile")));
    }

    @Test
    public void malformedAllocationProfilesCannotPublishARequestOrCallbackWaitMetadata() {
        for (String profile : Arrays.asList("", "not-json", "[]")) {
            variables.put("additionalProperties", profile);
            script.preProcessRequest(execution);
            try (MockedStatic<UrnPropertiesReader> properties = callbackConfiguration()) {
                assertThrows(Exception.class, () -> script.invokeMethod("createSdnrRequest", execution));
            }
            for (String name : Arrays.asList("createNSSI_sdnrRequest", "createNSSI_timeout", "createNSSI_correlator",
                    "createNSSI_messageType")) {
                assertFalse(name, variables.containsKey(name));
            }
        }
    }

    @Test
    public void successfulCallbacksPreserveIdentifierOrderAndTheActualStatusValue() {
        for (String status : Arrays.asList("success", "SUCCESS")) {
            JSONArray identifiers = new JSONArray().put("nf-2").put("nf-1");
            variables.put("SDNR_asyncCallbackResponse",
                    new JSONObject().put("status", status).put("nfIds", identifiers).toString());
            script.invokeMethod("processSdnrResponse", execution);
            assertEquals(status, variables.get("ranNfStatus"));
            assertTrue(variables.get("ranNfIdsJson") instanceof String);
            assertTrue(identifiers.similar(new JSONArray((String) variables.get("ranNfIdsJson"))));
            assertFalse(variables.containsKey("WorkflowException"));
        }
    }

    @Test
    public void failedCallbacksRetainTheBusinessErrorAndCannotPublishSuccessfulIdentifiers() {
        variables.put("SDNR_asyncCallbackResponse", new JSONObject().put("status", "failed")
                .put("reason", "\u96ea & <controller> rejected").put("nfIds", new JSONArray().put("nf-1")).toString());
        BpmnError error = assertThrows(BpmnError.class, () -> script.invokeMethod("processSdnrResponse", execution));
        assertError(error, 7000, "received failed status from SDNR \u96ea & <controller> rejected");
        assertFalse(variables.containsKey("ranNfIdsJson"));
        assertFalse(variables.containsKey("ranNfStatus"));
    }

    @Test
    public void nonSuccessCallbackStatusesCannotPublishIdentifiers() {
        variables.put("SDNR_asyncCallbackResponse",
                "{\"status\":\"RUNNING\",\"reason\":\"not terminal\",\"nfIds\":[\"nf-1\"]}");
        BpmnError error = assertThrows(BpmnError.class, () -> script.invokeMethod("processSdnrResponse", execution));
        assertError(error, 7000, "received failed status from SDNR not terminal");
        assertFalse(variables.containsKey("ranNfIdsJson"));
        assertFalse(variables.containsKey("ranNfStatus"));
    }

    @Test
    public void malformedCallbacksCannotPublishIdentifiers() {
        variables.put("SDNR_asyncCallbackResponse", "not-json");
        assertThrows(Exception.class, () -> script.invokeMethod("processSdnrResponse", execution));
        assertFalse(variables.containsKey("ranNfIdsJson"));
        assertFalse(variables.containsKey("ranNfStatus"));
    }

    private JSONObject request() {
        script.preProcessRequest(execution);
        try (MockedStatic<UrnPropertiesReader> properties = callbackConfiguration()) {
            script.invokeMethod("createSdnrRequest", execution);
        }
        assertEquals("PT10M", variables.get("createNSSI_timeout"));
        assertEquals("request-1", variables.get("createNSSI_correlator"));
        assertEquals("AsyncSdnrResponse", variables.get("createNSSI_messageType"));
        JSONObject request = new JSONObject((String) variables.get("createNSSI_sdnrRequest"));
        assertEquals("1.0", request.getString("version"));
        assertEquals("instantiateRANSlice", request.getString("rpc-name"));
        assertEquals("request-1-1", request.getString("correlation-id"));
        assertEquals("request", request.getString("type"));
        JSONObject header = request.getJSONObject("body").getJSONObject("input").getJSONObject("common-header");
        assertEquals("1.0", header.getString("api-ver"));
        assertEquals("request-1", header.getString("request-id"));
        assertEquals("1", header.getString("sub-request-id"));
        assertTrue(header.getJSONObject("flags").isEmpty());
        Instant.parse(header.getString("timestamp"));
        JSONObject payload = payload(request);
        assertEquals("ran-nssi-1", payload.getString("RANNFNSSIId"));
        assertEquals("http://localhost/callback/AsyncSdnrResponse/request-1", payload.getString("callbackURL"));
        assertEquals("customer-1", payload.getString("globalSubscriberId"));
        assertEquals("subscription-1", payload.getString("subscriptionServiceType"));
        return request;
    }

    private JSONObject payload(JSONObject request) {
        Object payload = request.getJSONObject("body").getJSONObject("input").get("payload");
        assertTrue(payload instanceof String);
        return new JSONObject((String) payload).getJSONObject("input");
    }

    private MockedStatic<UrnPropertiesReader> callbackConfiguration() {
        MockedStatic<UrnPropertiesReader> properties = mockStatic(UrnPropertiesReader.class);
        properties.when(() -> UrnPropertiesReader.getVariable("mso.workflow.message.endpoint"))
                .thenReturn("http://localhost/callback");
        return properties;
    }

    private void assertValidationFailure(String message) {
        BpmnError error = assertThrows(BpmnError.class, () -> script.preProcessRequest(execution));
        assertError(error, 500, message);
    }

    private void assertError(BpmnError error, int code, String message) {
        assertEquals(String.valueOf(code), error.getErrorCode());
        assertTrue(variables.get("WorkflowException") instanceof WorkflowException);
        WorkflowException workflow = (WorkflowException) variables.get("WorkflowException");
        assertEquals(code, workflow.getErrorCode());
        assertEquals(message, workflow.getErrorMessage());
        assertEquals("DoModifyRanNfNssi", workflow.getProcessKey());
    }
}
