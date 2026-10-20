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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RunWith(Parameterized.class)
public class NssmfTransportRequestContractTest {
    private final String domain;
    private final String prefix;
    private final String profileVariable;
    private final String endpointA;
    private final String endpointB;
    private final String namePrefix;
    private final AnNssmfUtils script = new AnNssmfUtils();
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> domains() {
        return Arrays.asList(new Object[][] {{"TN_FH", "TNFH", "tnFhSliceProfile", "ru-1", "du-in-1", "nssi_tn_fh_"},
                {"TN_MH", "TNMH", "tnMhSliceProfile", "du-eg-1", "cu-in-1", "nssi_tn_mh_"}});
    }

    public NssmfTransportRequestContractTest(String domain, String prefix, String profileVariable, String endpointA,
            String endpointB, String namePrefix) {
        this.domain = domain;
        this.prefix = prefix;
        this.profileVariable = profileVariable;
        this.endpointA = endpointA;
        this.endpointB = endpointB;
        this.namePrefix = namePrefix;
    }

    @Before
    public void configureExecution() {
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        variables.put(prefix + "_modelInvariantUuid", "invariant-" + domain);
        variables.put(prefix + "_modelUuid", "model-" + domain);
        variables.put(prefix + "_modelName", "model Gr\u00fc\u00dfe & <" + domain + ">");
        variables.put(prefix + "_sliceProfileInstanceId", "profile-" + domain);
        variables.put(prefix + "_NSSI", "nssi-" + domain);
        variables.put(prefix + "_nssiName", "existing Gr\u00fc\u00dfe " + domain);
        variables.put("sst", 1);
        variables.put("nsiId", "nsi-1");
        variables.put("globalSubscriberId", "customer-1");
        variables.put("subscriptionServiceType", "service Gr\u00fc\u00dfe & SO");
        variables.put("tranportEp_ID_RU", "ru-1");
        variables.put("tranportEp_ID_DUIN", "du-in-1");
        variables.put("tranportEp_ID_DUEG", "du-eg-1");
        variables.put("tranportEp_ID_CUIN", "cu-in-1");
        variables.put("snssaiList", Arrays.asList("001-000002", "001-000001"));
        variables.put(profileVariable, "{\"sliceProfileId\":\"incoming-profile\",\"maxBandwidth\":0,"
                + "\"shared\":false,\"snssaiList\":[\"001-000002\",\"001-000001\"],\"label\":\"Gr\\u00fc\\u00dfe & SO\"}");
        variables.put("sliceParams", "{\"nsiInfo\":{\"nsiId\":\"nsi-1\",\"active\":false,\"count\":0}}");
    }

    @Test
    public void allocationPreservesTypedProfilesAndSelectsTheDomainSpecificEndpoints() {
        String originalProfile = (String) variables.get(profileVariable);

        JsonObject request = allocation("allocate");

        assertEnvelope(request);
        JsonObject service = request.getAsJsonObject("serviceInfo");
        assertEquals("invariant-" + domain, service.get("serviceInvariantUuid").getAsString());
        assertEquals("model-" + domain, service.get("serviceUuid").getAsString());
        assertTrue(service.get("sst").getAsJsonPrimitive().isNumber());
        assertEquals(1, service.get("sst").getAsInt());
        String generatedName = service.get("nssiName").getAsString();
        assertTrue(generatedName, generatedName.startsWith(namePrefix));
        UUID.fromString(generatedName.substring(namePrefix.length()));
        JsonObject payload = request.getAsJsonObject("allocateTnNssi");
        assertEquals("model-" + domain, payload.get("nsstId").getAsString());
        assertEquals(variables.get(prefix + "_modelName"), payload.get("nssiName").getAsString());
        assertProfileAndEndpoints(payload);
        assertEquals(originalProfile, variables.get(profileVariable));
        verify(execution, never()).setVariable(anyString(), any());
    }

    @Test
    public void modificationUsesExistingIdentityAndOmitsAllocationOnlyFields() {
        JsonObject request = allocation("modify-allocate");

        assertEnvelope(request);
        JsonObject service = request.getAsJsonObject("serviceInfo");
        assertEquals("nssi-" + domain, service.get("nssiId").getAsString());
        assertEquals(variables.get(prefix + "_nssiName"), service.get("nssiName").getAsString());
        assertFalse(service.has("serviceUuid"));
        assertFalse(service.has("serviceInvariantUuid"));
        assertFalse(service.has("sst"));
        JsonObject payload = request.getAsJsonObject("allocateTnNssi");
        assertEquals("nssi-" + domain, payload.get("nssiId").getAsString());
        assertEquals("profile-" + domain, payload.get("sliceProfileId").getAsString());
        assertEquals(variables.get(prefix + "_nssiName"), payload.get("nssiName").getAsString());
        assertFalse(payload.has("nsstId"));
        assertProfileAndEndpoints(payload);
    }

    @Test
    public void deallocationPreservesZeroTerminationOptionAndBooleanModifyFlag() {
        JsonObject request = deallocation();

        assertDeallocationEnvelope(request);
        JsonObject payload = request.getAsJsonObject("deAllocateNssi");
        assertTrue(payload.get("terminateNssiOption").getAsJsonPrimitive().isNumber());
        assertEquals(0, payload.get("terminateNssiOption").getAsInt());
        assertEquals("nssi-" + domain, payload.get("nssiId").getAsString());
        assertEquals("nsi-1", payload.get("nsiId").getAsString());
        assertEquals("profile-" + domain, payload.get("sliceProfileId").getAsString());
        assertEquals("TN1", payload.get("scriptName").getAsString());
        assertEquals(JsonParser.parseString("[\"001-000002\",\"001-000001\"]"), payload.get("snssaiList"));
    }

    @Test
    public void emptySliceSelectionRemainsAnEmptyArrayInTheDeallocationPayload() {
        variables.put("snssaiList", Collections.emptyList());

        JsonObject request = deallocation();

        assertDeallocationEnvelope(request);
        assertEquals(new JsonArray(), request.getAsJsonObject("deAllocateNssi").get("snssaiList"));
    }

    @Test
    public void absentSliceSelectionIsOmittedFromTheDeallocationPayload() {
        variables.remove("snssaiList");

        JsonObject request = deallocation();

        assertDeallocationEnvelope(request);
        assertFalse(request.getAsJsonObject("deAllocateNssi").has("snssaiList"));
    }

    @Test
    public void malformedProfileFailsBeforeWritingAnyWorkflowVariables() {
        variables.put(profileVariable, "{broken");

        assertThrows(JsonProcessingException.class,
                () -> script.buildCreateNSSMFRequest(execution, domain, "allocate"));

        verify(execution, never()).setVariable(anyString(), any());
    }

    @Test
    public void stringSstRetainsItsCallerSuppliedJsonType() {
        variables.put("sst", "1");

        JsonObject request = allocation("allocate");

        assertTrue(request.getAsJsonObject("serviceInfo").get("sst").getAsJsonPrimitive().isString());
        assertEquals("1", request.getAsJsonObject("serviceInfo").get("sst").getAsString());
    }

    private JsonObject allocation(String action) {
        return JsonParser.parseString(script.buildCreateNSSMFRequest(execution, domain, action)).getAsJsonObject();
    }

    private JsonObject deallocation() {
        return JsonParser.parseString(script.buildDeallocateNssiRequest(execution, domain)).getAsJsonObject();
    }

    private void assertProfileAndEndpoints(JsonObject payload) {
        assertEquals("TN1", payload.get("scriptName").getAsString());
        JsonObject profile = payload.getAsJsonObject("sliceProfile");
        assertEquals("profile-" + domain, profile.get("sliceProfileId").getAsString());
        assertTrue(profile.get("maxBandwidth").getAsJsonPrimitive().isNumber());
        assertEquals(0, profile.get("maxBandwidth").getAsInt());
        assertTrue(profile.get("shared").getAsJsonPrimitive().isBoolean());
        assertFalse(profile.get("shared").getAsBoolean());
        assertEquals("Gr\u00fc\u00dfe & SO", profile.get("label").getAsString());
        assertEquals(JsonParser.parseString("[\"001-000002\",\"001-000001\"]"), profile.get("snssaiList"));
        JsonArray networks = payload.getAsJsonArray("transportSliceNetworks");
        assertEquals(1, networks.size());
        JsonArray links = networks.get(0).getAsJsonObject().getAsJsonArray("connectionLinks");
        assertEquals(1, links.size());
        assertEquals(endpointA, links.get(0).getAsJsonObject().get("transportEndpointA").getAsString());
        assertEquals(endpointB, links.get(0).getAsJsonObject().get("transportEndpointB").getAsString());
        JsonObject nsiInfo = payload.getAsJsonObject("nsiInfo");
        assertEquals("nsi-1", nsiInfo.get("nsiId").getAsString());
        assertTrue(nsiInfo.get("active").getAsJsonPrimitive().isBoolean());
        assertFalse(nsiInfo.get("active").getAsBoolean());
        assertEquals(0, nsiInfo.get("count").getAsInt());
    }

    private void assertEnvelope(JsonObject request) {
        assertEquals("nsi-1", request.getAsJsonObject("serviceInfo").get("nsiId").getAsString());
        assertCommonEnvelope(request);
    }

    private void assertDeallocationEnvelope(JsonObject request) {
        assertCommonEnvelope(request);
        JsonObject service = request.getAsJsonObject("serviceInfo");
        assertEquals("nssi-" + domain, service.get("nssiId").getAsString());
        assertTrue(service.get("modifyAction").getAsJsonPrimitive().isBoolean());
        assertTrue(service.get("modifyAction").getAsBoolean());
    }

    private void assertCommonEnvelope(JsonObject request) {
        assertEquals("tn", request.getAsJsonObject("esrInfo").get("networkType").getAsString());
        assertEquals("ONAP_internal", request.getAsJsonObject("esrInfo").get("vendor").getAsString());
        assertEquals("customer-1", request.getAsJsonObject("serviceInfo").get("globalSubscriberId").getAsString());
        assertEquals("service Gr\u00fc\u00dfe & SO",
                request.getAsJsonObject("serviceInfo").get("subscriptionServiceType").getAsString());
    }
}
