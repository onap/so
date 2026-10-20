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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.bpmn.core.WorkflowException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AccessSliceProfileContractTest {
    private final AnNssmfUtils script = new AnNssmfUtils();
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();

    @Before
    public void configureExecution() {
        variables.put("testProcessKey", "AccessSliceProfile");
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
    }

    @Test
    public void transportCapabilityQuerySelectsBothTransportDomains() {
        JsonObject request =
                JsonParser.parseString(script.buildCreateTNNSSMFSubnetCapabilityRequest()).getAsJsonObject();

        assertCapability(request, "tn", "[\"TN_FH\",\"TN_MH\"]");
    }

    @Test
    public void accessCapabilityQuerySelectsOnlyTheAccessNetworkFunctionDomain() {
        JsonObject request =
                JsonParser.parseString(script.buildCreateANNFNSSMFSubnetCapabilityRequest()).getAsJsonObject();

        assertCapability(request, "an", "[\"AN_NF\"]");
    }

    @Test
    public void dispatchPreservesEachDomainsOriginalSerializedProfile() {
        String access = profile("AN_NF");
        String fronthaul = profile("TN_FH");
        String midhaul = profile("TN_MH");

        script.createDomainWiseSliceProfiles(Arrays.asList(midhaul, access, fronthaul), execution);

        assertEquals(access, variables.get("ranNfSliceProfile"));
        assertEquals(fronthaul, variables.get("tnFhSliceProfile"));
        assertEquals(midhaul, variables.get("tnMhSliceProfile"));
        assertFalse(variables.containsKey("WorkflowException"));
    }

    @Test
    public void unknownProfileDomainProducesTheDefinedBusinessError() {
        try {
            script.createDomainWiseSliceProfiles(Collections.singletonList(profile("UNKNOWN")), execution);
            fail("Expected an unsupported domain to fail");
        } catch (BpmnError error) {
            assertEquals("1000", error.getErrorCode());
        }
        WorkflowException error = (WorkflowException) variables.get("WorkflowException");
        assertEquals("AccessSliceProfile", error.getProcessKey());
        assertEquals(1000, error.getErrorCode());
        assertEquals("No expected match found for current domainType UNKNOWN", error.getErrorMessage());
        assertFalse(variables.containsKey("ranNfSliceProfile"));
        assertFalse(variables.containsKey("tnFhSliceProfile"));
        assertFalse(variables.containsKey("tnMhSliceProfile"));
    }

    @Test
    public void emptyAndNullProfileListsRetainTheGroovyNoWriteContract() {
        script.createDomainWiseSliceProfiles(Collections.emptyList(), execution);
        script.createDomainWiseSliceProfiles(null, execution);

        verify(execution, never()).setVariable(anyString(), any());
    }

    @Test
    public void selectionRequestPreservesCorrelationAndTypedProfilesAndCapabilities() {
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("label", "Gr\u00fc\u00dfe & <SO> \"quoted\"");
        profile.put("shared", false);
        profile.put("latency", 0);
        profile.put("snssaiList", Arrays.asList("001-000002", "001-000001"));
        JsonObject capability = new JsonObject();
        capability.addProperty("subnetType", "AN_NF");
        capability.addProperty("enabled", false);
        JsonArray capabilities = new JsonArray();
        capabilities.add(capability);

        JsonObject request = selection(profile, capabilities, Boolean.FALSE);

        JsonObject info = request.getAsJsonObject("requestInfo");
        assertEquals("request-1", info.get("requestId").getAsString());
        assertEquals("request-1", info.get("transactionId").getAsString());
        assertEquals("http://callback.invalid/oof/NSSISelectionResponse/request-1",
                info.get("callbackUrl").getAsString());
        assertEquals("SO", info.get("sourceId").getAsString());
        assertTrue(info.get("timeout").getAsJsonPrimitive().isNumber());
        assertEquals(600, info.get("timeout").getAsInt());
        assertTrue(info.get("numSolutions").getAsJsonPrimitive().isNumber());
        assertEquals(1, info.get("numSolutions").getAsInt());
        JsonObject template = request.getAsJsonObject("NSTInfo");
        assertEquals("model-1", template.get("UUID").getAsString());
        assertEquals("invariant-1", template.get("invariantUUID").getAsString());
        assertEquals("RAN Gr\u00fc\u00dfe & SO", template.get("name").getAsString());
        assertEquals(new Gson().toJsonTree(profile), request.get("serviceProfile"));
        assertEquals(capabilities, request.get("subnetCapabilities"));
        assertTrue(request.get("preferReuse").getAsJsonPrimitive().isBoolean());
        assertFalse(request.get("preferReuse").getAsBoolean());
    }

    @Test
    public void reusePreferenceTrueRemainsABooleanInTheSelectionPayload() {
        JsonObject preferred = selection(Collections.emptyMap(), new JsonArray(), Boolean.TRUE);
        assertTrue(preferred.get("preferReuse").getAsJsonPrimitive().isBoolean());
        assertTrue(preferred.get("preferReuse").getAsBoolean());

    }

    private JsonObject selection(Map<String, Object> profile, JsonArray capabilities, Boolean reuse) {
        try (MockedStatic<UrnPropertiesReader> urns = mockStatic(UrnPropertiesReader.class)) {
            urns.when(() -> UrnPropertiesReader.getVariable("mso.adapters.oof.callback.endpoint"))
                    .thenReturn("http://callback.invalid/oof");
            String result = script.buildSelectRANNSSIRequest("request-1", "NSSISelectionResponse", "model-1",
                    "invariant-1", "RAN Gr\u00fc\u00dfe & SO", profile, Collections.emptyList(), capabilities, reuse);
            urns.verify(() -> UrnPropertiesReader.getVariable("mso.adapters.oof.callback.endpoint"));
            return JsonParser.parseString(result).getAsJsonObject();
        }
    }

    private void assertCapability(JsonObject request, String network, String subnets) {
        assertEquals(network, request.getAsJsonObject("esrInfo").get("networkType").getAsString());
        assertEquals("ONAP_internal", request.getAsJsonObject("esrInfo").get("vendor").getAsString());
        assertEquals(JsonParser.parseString(subnets),
                request.getAsJsonObject("subnetCapabilityQuery").get("subnetTypes"));
    }

    private String profile(String domain) {
        JsonObject profile = new JsonObject();
        profile.addProperty("domainType", domain);
        profile.addProperty("label", "profile Gr\u00fc\u00dfe & SO");
        profile.addProperty("shared", false);
        profile.addProperty("latency", 0);
        return profile.toString();
    }
}
