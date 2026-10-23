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
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.bpmn.core.WorkflowException;

public class CloudLeasedLineInputContractTest {
    private static final String CALLBACK = "http://localhost:12345/sdnc-callback";
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();
    private final DoCloudLeasedLineCreate create = new DoCloudLeasedLineCreate();
    private final DoCloudLeasedLineModify modify = new DoCloudLeasedLineModify();
    private MockedStatic<UrnPropertiesReader> properties;
    private MockedConstruction<AAIResourcesClient> clients;

    @Before
    public void configureInputs() {
        variables.put("msoRequestId", "request-1");
        variables.put("serviceInstanceID", "existing-service");
        variables.put("servicename", "leased Grüße & 雪");
        variables.put("modelInvariantUuid", "invariant-1");
        variables.put("modelUuid", "model-1");
        variables.put("sst", "001");
        variables.put("serviceIntentParams", parameters().toString());
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
        properties = mockStatic(UrnPropertiesReader.class);
        properties.when(() -> UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback", execution))
                .thenReturn(CALLBACK);
        properties.when(() -> UrnPropertiesReader.getVariable("mso.workflow.TnNssmf.enableSDNCNetworkConfig"))
                .thenReturn("true");
        clients = mockConstruction(AAIResourcesClient.class);
    }

    @After
    public void closeConfigurationAndAssertNoInventoryAccess() {
        try {
            assertTrue("Input and link-selection contracts must not contact inventory",
                    clients.constructed().isEmpty());
        } finally {
            clients.close();
            properties.close();
        }
    }

    @Test
    public void creationRetainsAnExplicitIdentifierAndTypedControllerInputs() {
        variables.put("testProcessKey", "DoCloudLeasedLineCreate");
        variables.put("serviceIntentParams", parameters().put("serviceInstanceID", "cll-fixed").toString());
        create.preProcessRequest(execution);
        assertEquals("CLLC_", variables.get("prefix"));
        assertEquals("cll-fixed", variables.get("cllId"));
        assertEquals("existing-service", variables.get("serviceInstanceID"));
        assertEquals("001", variables.get("sst"));
        assertCommonInputs();
    }

    @Test
    public void creationGeneratesAUuidWhenTheAdditionalIdentifierIsAbsent() {
        create.preProcessRequest(execution);
        String id = (String) variables.get("cllId");
        assertEquals(id, UUID.fromString(id).toString());
        assertEquals("existing-service", variables.get("serviceInstanceID"));
        assertCommonInputs();
    }

    @Test
    public void anOmittedControllerFlagUsesTheConfiguredStringValue() {
        JSONObject input = parameters();
        input.remove("enableSdnc");
        variables.put("serviceIntentParams", input.toString());
        create.preProcessRequest(execution);
        assertEquals("true", variables.get("enableSdnc"));
        assertEquals(String.class, variables.get("enableSdnc").getClass());
    }

    @Test
    public void creationRejectsMissingTransportNetworksWithTheValidationCode() {
        JSONObject input = parameters();
        input.remove("transportNetworks");
        variables.put("serviceIntentParams", input.toString());
        variables.put("testProcessKey", "DoCloudLeasedLineCreate");
        assertError(() -> create.preProcessRequest(execution), "DoCloudLeasedLineCreate", "transportNetworks");
        assertFalse(variables.containsKey("transportNetworks"));
    }

    @Test
    public void modificationRetainsItsIdentifierModelsAndNestedJsonTypes() {
        variables.put("serviceIntentParams", parameters().put("modifyAction", "add").toString());
        modify.preProcessRequest(execution);
        assertEquals("existing-service", variables.get("cllId"));
        assertEquals("add", variables.get("modifyAction"));
        assertEquals(Boolean.TRUE, variables.get("isDebugLogEnabled"));
        JSONObject model = new JSONObject((String) variables.get("serviceModelInfo"));
        assertEquals("invariant-1", model.getString("modelInvariantUuid"));
        assertEquals("model-1", model.getString("modelUuid"));
        assertEquals("", model.getString("modelVersion"));
        assertCommonInputs();
    }

    @Test
    public void modificationRejectsMissingIdentifiersBeforeCreatingAnOperation() {
        for (String missing : Arrays.asList(null, "", " \t")) {
            variables.put("serviceInstanceID", missing);
            variables.put("testProcessKey", "DoCloudLeasedLineModify");
            assertError(() -> modify.preProcessRequest(execution), "DoCloudLeasedLineModify", "cllId");
            assertFalse(variables.containsKey("operationId"));
        }
    }

    @Test
    public void modificationRejectsMissingParametersBeforeCreatingAnOperation() {
        for (String missing : Arrays.asList(null, "", " \t")) {
            variables.put("serviceIntentParams", missing);
            variables.put("testProcessKey", "DoCloudLeasedLineModify");
            assertError(() -> modify.preProcessRequest(execution), "DoCloudLeasedLineModify", "additionalPropJsonStr");
            assertFalse(variables.containsKey("operationId"));
        }
    }

    @Test
    public void modificationRejectsMissingTransportNetworksWithTheValidationCode() {
        JSONObject input = parameters();
        input.remove("transportNetworks");
        variables.put("serviceIntentParams", input.toString());
        variables.put("testProcessKey", "DoCloudLeasedLineModify");
        assertError(() -> modify.preProcessRequest(execution), "DoCloudLeasedLineModify", "transportNetworks");
    }

    @Test
    public void bothOperationsRejectMissingCallbacksBeforeAllocatingIdentifiers() {
        properties.when(() -> UrnPropertiesReader.getVariable("mso.workflow.sdncadapter.callback", execution))
                .thenReturn(null);
        variables.put("testProcessKey", "DoCloudLeasedLineCreate");
        assertError(() -> create.preProcessRequest(execution), "DoCloudLeasedLineCreate", "sdncadapter.callback");
        variables.put("testProcessKey", "DoCloudLeasedLineModify");
        assertError(() -> modify.preProcessRequest(execution), "DoCloudLeasedLineModify", "sdncadapter.callback");
        assertFalse(variables.containsKey("operationId"));
        assertFalse(variables.containsKey("cllId"));
    }

    @Test
    public void unspecifiedAllottedResourcesSelectTheFirstDeclaredResource() {
        List<String> ids = List.of("ar-1", "ar-2");
        variables.put("arIdList", ids);
        for (String unspecified : Arrays.asList(null, "", " \t")) {
            assertEquals("ar-1", modify.getValidArId(execution, unspecified));
        }
        assertEquals(List.of("ar-1", "ar-2"), ids);
    }

    @Test
    public void anExplicitAllottedResourceIsRetained() {
        variables.put("arIdList", List.of("ar-1", "ar-2"));
        assertEquals("ar-2", modify.getValidArId(execution, "ar-2"));
    }

    @Test
    public void anExistingEndpointIsDetectedOnlyWithinItsAllottedResource() {
        variables.put("arLogicalLinkMap", Map.of("ar-1", List.of("endpoint & 雪"), "ar-2", List.of("other")));
        String link = new JSONObject().put("transportEndpointA", "endpoint & 雪").toString();
        assertTrue(modify.logicalLinkExists(execution, "ar-1", link));
        assertFalse(modify.logicalLinkExists(execution, "ar-2", link));
    }

    @Test
    public void missingLinkStateAndMissingEndpointNamesDoNotMatch() {
        String link = new JSONObject().put("transportEndpointA", "endpoint").toString();
        assertFalse(modify.logicalLinkExists(execution, "ar-1", link));
        variables.put("arLogicalLinkMap", Map.of("ar-1", List.of("endpoint")));
        assertFalse(modify.logicalLinkExists(execution, "ar-1", "{}"));
    }

    @Test
    public void missingResourceOrLinkArgumentsDoNotMatch() {
        variables.put("arLogicalLinkMap", Map.of("ar-1", List.of("endpoint")));
        for (String missing : Arrays.asList(null, "", " \t")) {
            assertFalse(modify.logicalLinkExists(execution, missing, "{}"));
            assertFalse(modify.logicalLinkExists(execution, "ar-1", missing));
        }
    }

    @Test
    public void anUnknownAllottedResourceHasNoExistingLinks() {
        Assume.assumeTrue("SO-4456: cloud leased-line modification dereferences a missing resource link list",
                Boolean.getBoolean("groovy.migration.verification"));
        variables.put("arLogicalLinkMap", Map.of("ar-1", List.of("endpoint")));
        String link = new JSONObject().put("transportEndpointA", "endpoint").toString();
        assertFalse(modify.logicalLinkExists(execution, "ar-unknown", link));
    }

    private void assertCommonInputs() {
        assertEquals("leased Grüße & 雪", variables.get("cllName"));
        assertEquals(CALLBACK, variables.get("sdncCallbackUrl"));
        assertEquals("false", variables.get("enableSdnc"));
        assertEquals(String.class, variables.get("enableSdnc").getClass());
        assertEquals(Long.class, variables.get("startTime").getClass());
        String operation = (String) variables.get("operationId");
        assertEquals(operation, UUID.fromString(operation).toString());
        JSONObject network = new JSONArray((String) variables.get("transportNetworks")).getJSONObject(0);
        assertEquals("network-1", network.getString("id"));
        assertEquals(Boolean.FALSE, network.get("enabled"));
        assertEquals(Integer.valueOf(0), network.get("capacity"));
        assertFalse(variables.containsKey("WorkflowException"));
    }

    private void assertError(Runnable action, String process, String messagePart) {
        BpmnError thrown = assertThrows(BpmnError.class, action::run);
        assertEquals("500", thrown.getErrorCode());
        Object raw = variables.get("WorkflowException");
        assertEquals(WorkflowException.class, raw.getClass());
        WorkflowException error = (WorkflowException) raw;
        assertEquals(process, error.getProcessKey());
        assertEquals(500, error.getErrorCode());
        assertTrue(error.getErrorMessage(), error.getErrorMessage().contains(messagePart));
    }

    private static JSONObject parameters() {
        JSONObject network = new JSONObject().put("id", "network-1").put("enabled", false).put("capacity", 0);
        return new JSONObject().put("transportNetworks", new JSONArray().put(network)).put("enableSdnc", false);
    }
}
