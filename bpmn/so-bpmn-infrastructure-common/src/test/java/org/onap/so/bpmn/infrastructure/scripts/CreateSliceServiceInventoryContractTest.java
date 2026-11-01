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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.onap.aai.domain.yang.AllottedResource;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.ServiceProfile;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.beans.nsmf.SliceTaskParamsAdapter;
import org.onap.so.bpmn.core.RollbackData;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.core.domain.ModelInfo;
import org.onap.so.bpmn.core.domain.ServiceDecomposition;

public class CreateSliceServiceInventoryContractTest {
    private static final String INSTANCE = "33333333-3333-4333-8333-333333333333";
    private static final String INVARIANT = "11111111-1111-4111-8111-111111111111";
    private static final String VERSION = "22222222-2222-4222-8222-222222222222";
    private static final String ROOT = "/aai/v[0-9]+/business/customers/customer/customer-1/service-subscriptions/"
            + "service-subscription/subscription-1/service-instances/service-instance/" + INSTANCE;
    private static final String PROFILES = ROOT + "/service-profiles/service-profile/[^/]+";
    private static final String ALLOTTED = ROOT + "/allotted-resources/allotted-resource/[^/]+";
    private static final String NAME = "profile Gr\u00fc\u00dfe & \u96ea <one> \\\"quoted\\\"";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final List<String> NUMBERS = Arrays.asList("latency", "maxNumberofUEs", "dLThptPerSlice",
            "dLThptPerUE", "uLThptPerSlice", "uLThptPerUE", "activityFactor", "maxNumberofConns", "jitter");
    @Rule
    public WireMockRule inventory = new WireMockRule(0);
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();
    private final SliceTaskParamsAdapter parameters = new SliceTaskParamsAdapter();
    private final ServiceDecomposition decomposition = new ServiceDecomposition();
    private MockedStatic<UrnPropertiesReader> properties;
    private DoCreateSliceServiceInstance script;
    private AAIResourcesClient client;

    @Before
    public void configureRealInventoryBoundary() {
        Map<String, String> configuration = new HashMap<>();
        configuration.put("aai.endpoint", "http://localhost:" + inventory.port());
        configuration.put("aai.username", "contract");
        configuration.put("aai.password", "contract");
        properties = mockStatic(UrnPropertiesReader.class);
        properties.when(() -> UrnPropertiesReader.getVariable(anyString()))
                .thenAnswer(call -> configuration.get(call.getArgument(0)));
        properties.when(() -> UrnPropertiesReader.getVariable(anyString(), anyString()))
                .thenAnswer(call -> configuration.getOrDefault(call.getArgument(0), call.getArgument(1)));
        ModelInfo model = model(INVARIANT, VERSION);
        decomposition.setModelInfo(model);
        decomposition.setAllottedResources(Collections.emptyList());
        Map<String, Object> profile = parameters.getServiceProfile();
        profile.put("sST", "1");
        profile.put("pLMNIdList", "[\"310-410\"]");
        profile.put("sNSSAI", "001-000001");
        for (String field : NUMBERS) {
            profile.put(field, "0");
        }
        profile.put("coverageAreaTAList", "000001,000002");
        profile.put("uEMobilityLevel", "stationary");
        profile.put("resourceSharingLevel", "shared");
        variables.put("testProcessKey", "DoCreateSliceServiceInstance");
        variables.put("msoRequestId", "request-1");
        variables.put("serviceInstanceId", INSTANCE);
        variables.put("serviceInstanceName", NAME);
        variables.put("globalSubscriberId", "customer-1");
        variables.put("subscriptionServiceType", "subscription-1");
        variables.put("sliceTaskParams", parameters);
        variables.put("serviceProfileDecomposition", decomposition);
        variables.put("uuiRequest",
                "{\"service\":{\"serviceInvariantUuid\":\"" + INVARIANT + "\",\"serviceUuid\":\"" + VERSION + "\"}}");
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
        script = new DoCreateSliceServiceInstance();
        client = spy(script.getClient());
        script.setClient(client);
        respond(ROOT, 201);
        respond(PROFILES, 201);
        respond(ALLOTTED, 201);
    }

    @After
    public void restoreConfiguration() {
        if (properties != null) {
            properties.close();
        }
    }

    @Test
    public void preprocessingEnablesTheTypedDebugFlagWithoutAnInventoryAction() {
        variables.put("isDebugLogEnabled", Boolean.FALSE);
        script.preProcessRequest(execution);
        assertEquals(Boolean.TRUE, variables.get("isDebugLogEnabled"));
        assertEquals(Boolean.class, variables.get("isDebugLogEnabled").getClass());
        assertTrue(inventory.getAllServeEvents().isEmpty());
    }

    @Test
    public void decompositionPreservesBothModelIdentitiesAndTheEmptyVersionField() throws Exception {
        script.prepareDecomposeService(execution);
        JsonNode model = MAPPER.readTree((String) variables.get("serviceModelInfo"));
        assertEquals(INVARIANT, model.path("modelInvariantUuid").asText());
        assertEquals(VERSION, model.path("modelUuid").asText());
        assertTrue(model.path("modelVersion").isTextual());
        assertEquals("", model.path("modelVersion").asText());
        assertTrue(inventory.getAllServeEvents().isEmpty());
    }

    @Test
    public void instanceCreationRetainsModelLocationAndOwnershipDataInBothRollbackVariables() throws Exception {
        script.createServiceProfileInstance(execution);
        ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
        verify(client).create(any(AAIResourceUri.class), body.capture());
        ServiceInstance instance = (ServiceInstance) body.getValue();
        assertEquals(INSTANCE, instance.getServiceInstanceId());
        assertEquals(NAME, instance.getServiceInstanceName());
        assertEquals("1", instance.getServiceType());
        assertEquals("deactivated", instance.getOrchestrationStatus());
        assertEquals("service-profile", instance.getServiceRole());
        assertEquals(INVARIANT, instance.getModelInvariantId());
        assertEquals(VERSION, instance.getModelVersionId());
        assertEquals("[\"310-410\"]", instance.getServiceInstanceLocationId());
        assertEquals("001-000001", instance.getEnvironmentContext());
        assertPut(1, ROOT);
        JsonNode sent = payload(ROOT);
        assertEquals(INSTANCE, sent.path("service-instance-id").asText());
        assertEquals(NAME, sent.path("service-instance-name").asText());
        assertRollback();
        assertEquals(1, inventory.getAllServeEvents().size());
    }

    @Test
    public void instanceCreationPreservesExistingCompensationEntries() {
        RollbackData existing = new RollbackData();
        existing.put("OTHER", "marker", "keep");
        existing.put("SERVICEINSTANCE", "earlierStep", "keep-too");
        variables.put("RollbackData", existing);
        script.createServiceProfileInstance(execution);
        assertRollback();
        RollbackData result = (RollbackData) variables.get("RollbackData");
        assertEquals("keep", result.get("OTHER", "marker"));
        assertEquals("keep-too", result.get("SERVICEINSTANCE", "earlierStep"));
        assertEquals(MAPPER.valueToTree(result), MAPPER.valueToTree(variables.get("rollbackData")));
    }

    @Test
    public void rejectedInstanceCreationCannotClaimRollbackOwnership() {
        for (int status : new int[] {409, 500, 503}) {
            inventory.resetRequests();
            respond(ROOT, status);
            BpmnError error = assertThrows(BpmnError.class, () -> script.createServiceProfileInstance(execution));
            assertWorkflowError(error);
            assertFalse(variables.containsKey("RollbackData"));
            assertFalse(variables.containsKey("rollbackData"));
            assertPut(1, ROOT);
        }
    }

    @Test
    public void instanceCreationPropagatesAnAlreadyClassifiedBusinessError() {
        BpmnError original = new BpmnError("5601", "preclassified inventory failure");
        doThrow(original).when(client).create(any(AAIResourceUri.class), any(ServiceInstance.class));
        BpmnError error = assertThrows(BpmnError.class, () -> script.createServiceProfileInstance(execution));
        assertEquals("5601", error.getErrorCode());
        assertEquals(original.getMessage(), error.getMessage());
        assertFalse(variables.containsKey("RollbackData"));
        assertTrue(inventory.getAllServeEvents().isEmpty());
    }

    @Test
    public void profileCreationAcceptsNumericStringsAndNumbersAndPublishesItsGeneratedIdentity() throws Exception {
        for (boolean numeric : new boolean[] {false, true}) {
            inventory.resetRequests();
            clearInvocations(client);
            for (String field : NUMBERS) {
                parameters.getServiceProfile().put(field, numeric ? Integer.valueOf(0) : "0");
            }
            script.createServiceProfile(execution);
            ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
            verify(client).create(any(AAIResourceUri.class), body.capture());
            ServiceProfile profile = (ServiceProfile) body.getValue();
            UUID.fromString(profile.getProfileId());
            assertEquals(Integer.valueOf(0), profile.getLatency());
            assertEquals(Integer.valueOf(0), profile.getMaxNumberOfUEs());
            assertEquals(Integer.valueOf(0), profile.getDlThptPerSlice());
            assertEquals(Integer.valueOf(0), profile.getDlThptPerUE());
            assertEquals(Integer.valueOf(0), profile.getUlThptPerSlice());
            assertEquals(Integer.valueOf(0), profile.getUlThptPerUE());
            assertEquals(Integer.valueOf(0), profile.getActivityFactor());
            assertEquals(Integer.valueOf(0), profile.getMaxNumberOfConns());
            assertEquals(Integer.valueOf(0), profile.getJitter());
            assertEquals("000001,000002", profile.getCoverageAreaTAList());
            assertEquals("stationary", profile.getUeMobilityLevel());
            assertEquals("shared", profile.getResourceSharingLevel());
            assertEquals("0", profile.getSurvivalTime());
            assertEquals("", profile.getReliability());
            assertTrue(variables.get("sliceTaskParams") instanceof SliceTaskParamsAdapter);
            SliceTaskParamsAdapter stored = (SliceTaskParamsAdapter) variables.get("sliceTaskParams");
            assertEquals(profile.getProfileId(), stored.getServiceProfile().get("profileId"));
            assertPut(1, ROOT + "/service-profiles/service-profile/" + profile.getProfileId());
            JsonNode sent = payload(PROFILES);
            assertEquals(profile.getProfileId(), sent.path("profile-id").asText());
            assertTrue(sent.path("latency").isIntegralNumber());
            assertEquals(0, sent.path("latency").asInt());
            assertEquals(1, inventory.getAllServeEvents().size());
        }
    }

    @Test
    public void rejectedProfileCreationProducesTheDefinedWorkflowError() {
        respond(PROFILES, 503);
        BpmnError error = assertThrows(BpmnError.class, () -> script.createServiceProfile(execution));
        assertWorkflowError(error);
        assertPut(1, PROFILES);
    }

    @Test
    public void profileCreationPropagatesAnAlreadyClassifiedBusinessError() {
        BpmnError original = new BpmnError("5602", "preclassified profile failure");
        doThrow(original).when(client).create(any(AAIResourceUri.class), any(ServiceProfile.class));
        BpmnError error = assertThrows(BpmnError.class, () -> script.createServiceProfile(execution));
        assertEquals("5602", error.getErrorCode());
        assertEquals(original.getMessage(), error.getMessage());
        assertTrue(inventory.getAllServeEvents().isEmpty());
    }

    @Test
    public void anEmptyAllottedResourceDecompositionDoesNotCreateInventoryOrIdentities() {
        script.createAllottedResource(execution);
        assertTrue(inventory.getAllServeEvents().isEmpty());
        assertFalse(variables.containsKey("allottedResourceUri"));
        assertFalse(variables.containsKey("allottedResourceId"));
    }

    @Test
    public void allottedResourcesRetainTheirTypesRolesAndModelsInDecompositionOrder() throws Exception {
        decomposition.setAllottedResources(
                Arrays.asList(resource("type-a", "role-a", INVARIANT), resource("type-b", "role-b", VERSION)));
        script.createAllottedResource(execution);
        ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
        verify(client, times(2)).create(any(AAIResourceUri.class), body.capture());
        AllottedResource first = (AllottedResource) body.getAllValues().get(0);
        AllottedResource second = (AllottedResource) body.getAllValues().get(1);
        assertResource(first, "type-a", "role-a", INVARIANT);
        assertResource(second, "type-b", "role-b", VERSION);
        assertNotEquals(first.getId(), second.getId());
        assertEquals(second.getId(), variables.get("allottedResourceId"));
        assertTrue(variables.get("allottedResourceUri") instanceof AAIResourceUri);
        assertTrue(((AAIResourceUri) variables.get("allottedResourceUri")).build().getPath()
                .endsWith("/allotted-resources/allotted-resource/" + second.getId()));
        assertPut(1, ROOT + "/allotted-resources/allotted-resource/" + first.getId());
        assertPut(1, ROOT + "/allotted-resources/allotted-resource/" + second.getId());
        assertPut(2, ALLOTTED);
        assertEquals(2, inventory.getAllServeEvents().size());
    }

    @Test
    public void partialAllottedResourceFailureRetainsTheCreatedIdentityAndStopsLaterActions() {
        decomposition.setAllottedResources(Arrays.asList(resource("type-a", "role-a", INVARIANT),
                resource("type-b", "role-b", VERSION), resource("type-c", "role-c", INVARIANT)));
        inventory.stubFor(put(urlPathMatching(ALLOTTED)).inScenario("partial creation").whenScenarioStateIs("Started")
                .willSetStateTo("first created")
                .willReturn(aResponse().withStatus(201).withHeader("Connection", "close").withBody("{}")));
        inventory.stubFor(put(urlPathMatching(ALLOTTED)).inScenario("partial creation")
                .whenScenarioStateIs("first created")
                .willReturn(aResponse().withStatus(503).withHeader("Connection", "close").withBody("rejected")));
        BpmnError error = assertThrows(BpmnError.class, () -> script.createAllottedResource(execution));
        assertWorkflowError(error);
        ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
        verify(client, times(2)).create(any(AAIResourceUri.class), body.capture());
        AllottedResource first = (AllottedResource) body.getAllValues().get(0);
        assertEquals(first.getId(), variables.get("allottedResourceId"));
        assertPut(2, ALLOTTED);
        assertEquals(2, inventory.getAllServeEvents().size());
    }

    @Test
    public void allottedResourceCreationPreservesAnAlreadyClassifiedBusinessError() {
        Assume.assumeTrue("SO-4457: allotted-resource creation replaces a classified business error with 7000",
                Boolean.getBoolean("groovy.migration.verification"));
        decomposition.setAllottedResources(Collections.singletonList(resource("type-a", "role-a", INVARIANT)));
        BpmnError original = new BpmnError("5603", "preclassified allotted-resource failure");
        doThrow(original).when(client).create(any(AAIResourceUri.class), any(AllottedResource.class));
        BpmnError error = assertThrows(BpmnError.class, () -> script.createAllottedResource(execution));
        assertEquals("5603", error.getErrorCode());
        assertEquals(original.getMessage(), error.getMessage());
        assertTrue(inventory.getAllServeEvents().isEmpty());
    }

    private void assertRollback() {
        for (String name : new String[] {"RollbackData", "rollbackData"}) {
            assertTrue(variables.get(name) instanceof RollbackData);
            RollbackData data = (RollbackData) variables.get(name);
            assertEquals("true", data.get("SERVICEINSTANCE", "rollbackAAI"));
            assertEquals(INSTANCE, data.get("SERVICEINSTANCE", "serviceInstanceId"));
            assertEquals("subscription-1", data.get("SERVICEINSTANCE", "subscriptionServiceType"));
            assertEquals("customer-1", data.get("SERVICEINSTANCE", "globalSubscriberId"));
        }
    }

    private void assertWorkflowError(BpmnError error) {
        assertEquals("7000", error.getErrorCode());
        assertTrue(variables.get("WorkflowException") instanceof WorkflowException);
        WorkflowException workflow = (WorkflowException) variables.get("WorkflowException");
        assertEquals(7000, workflow.getErrorCode());
        assertEquals("DoCreateSliceServiceInstance", workflow.getProcessKey());
    }

    private void assertResource(AllottedResource resource, String type, String role, String invariant) {
        UUID.fromString(resource.getId());
        assertEquals(type, resource.getType());
        assertEquals(role, resource.getRole());
        assertEquals("Allotted_" + NAME, resource.getAllottedResourceName());
        assertEquals(invariant, resource.getModelInvariantId());
        assertEquals(VERSION, resource.getModelVersionId());
    }

    private org.onap.so.bpmn.core.domain.AllottedResource resource(String type, String role, String invariant) {
        org.onap.so.bpmn.core.domain.AllottedResource resource = new org.onap.so.bpmn.core.domain.AllottedResource();
        resource.setAllottedResourceType(type);
        resource.setAllottedResourceRole(role);
        resource.setModelInfo(model(invariant, VERSION));
        return resource;
    }

    private ModelInfo model(String invariant, String version) {
        ModelInfo model = new ModelInfo();
        model.setModelInvariantUuid(invariant);
        model.setModelUuid(version);
        return model;
    }

    private void assertPut(int count, String path) {
        inventory.verify(count,
                putRequestedFor(urlPathMatching(path))
                        .withHeader("Authorization", equalTo("Basic Y29udHJhY3Q6Y29udHJhY3Q="))
                        .withHeader("Content-Type", containing("application/json")));
    }

    private void respond(String path, int status) {
        inventory.stubFor(put(urlPathMatching(path)).willReturn(aResponse().withStatus(status)
                .withHeader("Content-Type", "application/json").withHeader("Connection", "close").withBody("{}")));
    }

    private JsonNode payload(String path) throws Exception {
        return MAPPER.readTree(inventory.findAll(putRequestedFor(urlPathMatching(path))).get(0).getBodyAsString());
    }
}
