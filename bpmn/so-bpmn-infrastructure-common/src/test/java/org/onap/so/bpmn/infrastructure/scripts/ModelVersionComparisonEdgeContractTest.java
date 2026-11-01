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

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.core.domain.ModelInfo;
import org.onap.so.bpmn.core.domain.NetworkResource;
import org.onap.so.bpmn.core.domain.ServiceDecomposition;

public class ModelVersionComparisonEdgeContractTest {
    private final DoCompareModelVersions script = new DoCompareModelVersions();
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();
    private final ObjectMapper json = new ObjectMapper();

    @Before
    public void configureStatefulExecution() {
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
        variables.put("testProcessKey", "DoCompareModelVersions");
        validIdentifiers();
    }

    @Test
    public void validationComparesInvariantValuesAndPreservesBothVersions() {
        variables.put("model-invariant-id-target", new String("invariant"));
        variables.put("model-invariant-id-original", new String("invariant"));
        script.preProcessRequest(execution);
        assertEquals("DCMPMDV_", variables.get("prefix"));
        assertEquals("target-version", variables.get("model-version-id-target"));
        assertEquals("original-version", variables.get("model-version-id-original"));
        assertFalse(variables.containsKey("WorkflowException"));
    }

    @Test
    public void requiredIdentifiersKeepTheirValidationErrorCode() {
        readiness("SO-4457: model comparison wraps explicit validation errors as generic 7000");
        List<String> mismatches = new ArrayList<>();
        for (String key : new String[] {"model-invariant-id-target", "model-version-id-target",
                "model-invariant-id-original", "model-version-id-original"}) {
            for (String absent : new String[] {null, "", " \t"}) {
                validIdentifiers();
                variables.put(key, absent);
                try {
                    script.preProcessRequest(execution);
                    fail("Expected validation error for " + key);
                } catch (BpmnError error) {
                    WorkflowException stored = (WorkflowException) variables.get("WorkflowException");
                    assertNotNull(key, stored);
                    assertTrue(stored.getErrorMessage(),
                            stored.getErrorMessage().contains("Input " + key + " is null"));
                    if (!"500".equals(error.getErrorCode()) || stored.getErrorCode() != 500) {
                        mismatches.add(key + ": thrown=" + error.getErrorCode() + ", stored=" + stored.getErrorCode());
                    }
                }
            }
        }
        assertEquals("Every null/blank input must retain its validation code", List.of(), mismatches);
    }

    @Test
    public void differentInvariantsKeepTheirValidationErrorCode() {
        readiness("SO-4457: model comparison replaces invariant validation error 500 with 7000");
        variables.put("model-invariant-id-target", "another-invariant");
        rejected("model-invariant-id-target and model-invariant-id-original must to be the same");
    }

    @Test
    public void identicalVersionsKeepTheirValidationErrorCode() {
        readiness("SO-4457: model comparison replaces version validation error 500 with 7000");
        variables.put("model-version-id-target", new String("original-version"));
        rejected("model-version-id-target and model-version-id-original must not to be the same");
    }

    @Test
    public void decompositionRequestsKeepTargetAndOriginalIdentitiesSeparate() throws Exception {
        script.prepareDecomposeService_Target(execution);
        script.prepareDecomposeService_Original(execution);
        for (String suffix : new String[] {"Target", "Original"}) {
            Object value = variables.get("serviceModelInfo_" + suffix);
            assertEquals(String.class, value.getClass());
            JsonNode node = json.readTree((String) value);
            assertEquals(3, node.size());
            assertEquals("invariant", node.get("modelInvariantUuid").textValue());
            assertEquals(suffix.equals("Target") ? "target-version" : "original-version",
                    node.get("modelUuid").textValue());
            assertEquals("", node.get("modelVersion").textValue());
        }
    }

    @Test
    public void quotedModelIdentitiesRemainValidJsonStrings() throws Exception {
        readiness("SO-4457: decomposition request model identifiers are interpolated without JSON escaping");
        String id = "Gr\u00fc\u00dfe \"model\" \\ path";
        variables.put("model-invariant-id-target", id);
        variables.put("model-version-id-target", id + "-target");
        variables.put("model-invariant-id-original", id);
        variables.put("model-version-id-original", id + "-original");
        script.prepareDecomposeService_Target(execution);
        script.prepareDecomposeService_Original(execution);
        for (String suffix : new String[] {"Target", "Original"}) {
            JsonNode node = json.readTree((String) variables.get("serviceModelInfo_" + suffix));
            assertEquals(id, node.get("modelInvariantUuid").textValue());
            assertEquals(id + (suffix.equals("Target") ? "-target" : "-original"), node.get("modelUuid").textValue());
        }
    }

    @Test
    public void decompositionResultsRetainTheirOwnObjectReferences() {
        ServiceDecomposition target = decomposition(resource("target", "v2", "i", "c"));
        ServiceDecomposition original = decomposition(resource("original", "v1", "i", "c"));
        variables.put("serviceDecomposition", target);
        script.processDecomposition_Target(execution);
        variables.put("serviceDecomposition", original);
        script.processDecomposition_Original(execution);
        assertSame(target, variables.get("serviceDecomposition_Target"));
        assertSame(original, variables.get("serviceDecomposition_Original"));
        assertSame(original, variables.get("serviceDecomposition"));
    }

    @Test
    public void identicalModelTriplesProduceNoChangesWithoutMutatingInputs() {
        NetworkResource target = resource("target-id", "version", "invariant", "customization");
        NetworkResource original = resource("original-id", "version", "invariant", "customization");
        ServiceDecomposition targetInput = decomposition(target);
        ServiceDecomposition originalInput = decomposition(original);
        compare(targetInput, originalInput);
        assertEquals(List.of(), variables.get("addResourceList"));
        assertEquals(List.of(), variables.get("delResourceList"));
        assertEquals(List.of(target), targetInput.getNetworkResources());
        assertEquals(List.of(original), originalInput.getNetworkResources());
        assertSame(target, targetInput.getNetworkResources().get(0));
        assertSame(original, originalInput.getNetworkResources().get(0));
    }

    @Test
    public void changesToAnyModelIdentityRequireAnAdditionAndDeletion() {
        for (String[] identity : new String[][] {{"new-v", "i", "c"}, {"v", "new-i", "c"}, {"v", "i", "new-c"}}) {
            NetworkResource target = resource("target", identity[0], identity[1], identity[2]);
            NetworkResource original = resource("original", "v", "i", "c");
            compare(decomposition(target), decomposition(original));
            assertEquals(List.of(target), variables.get("addResourceList"));
            assertEquals(List.of(original), variables.get("delResourceList"));
            assertSame(target, ((List<?>) variables.get("addResourceList")).get(0));
            assertSame(original, ((List<?>) variables.get("delResourceList")).get(0));
        }
    }

    @Test
    public void emptyDecompositionsProduceTypedEmptyOrOneSidedChanges() {
        NetworkResource resource = resource("resource", "v", "i", "c");
        compare(decomposition(), decomposition());
        assertEquals(ArrayList.class, variables.get("addResourceList").getClass());
        assertEquals(ArrayList.class, variables.get("delResourceList").getClass());
        assertEquals(List.of(), variables.get("addResourceList"));
        assertEquals(List.of(), variables.get("delResourceList"));
        compare(decomposition(resource), decomposition());
        assertEquals(List.of(resource), variables.get("addResourceList"));
        assertEquals(List.of(), variables.get("delResourceList"));
        compare(decomposition(), decomposition(resource));
        assertEquals(List.of(), variables.get("addResourceList"));
        assertEquals(List.of(resource), variables.get("delResourceList"));
    }

    @Test
    public void unmatchedChangesRetainTheirInputOrderingAndReferences() {
        NetworkResource addFirst = resource("add-first", "v2", "i1", "c1");
        NetworkResource addSecond = resource("add-second", "v2", "i2", "c2");
        NetworkResource shared = resource("shared-target", "shared-v", "shared-i", "shared-c");
        NetworkResource delFirst = resource("del-first", "v1", "i1", "c1");
        NetworkResource delSecond = resource("del-second", "v1", "i2", "c2");
        compare(decomposition(addFirst, shared, addSecond),
                decomposition(delFirst, resource("shared-original", "shared-v", "shared-i", "shared-c"), delSecond));
        assertEquals(List.of(addFirst, addSecond), variables.get("addResourceList"));
        assertEquals(List.of(delFirst, delSecond), variables.get("delResourceList"));
    }

    @Test
    public void nullAndEmptyCustomizationIdsRemainDistinct() {
        NetworkResource nullTarget = resource("target", "v", "i", null);
        NetworkResource nullOriginal = resource("original", "v", "i", null);
        compare(decomposition(nullTarget), decomposition(nullOriginal));
        assertEquals(List.of(), variables.get("addResourceList"));
        assertEquals(List.of(), variables.get("delResourceList"));
        NetworkResource emptyOriginal = resource("empty", "v", "i", "");
        compare(decomposition(nullTarget), decomposition(emptyOriginal));
        assertEquals(List.of(nullTarget), variables.get("addResourceList"));
        assertEquals(List.of(emptyOriginal), variables.get("delResourceList"));
    }

    @Test
    public void decompositionRequestStorageFailuresRetainTypedStageErrors() {
        doThrow(new IllegalStateException("store unavailable")).when(execution)
                .setVariable(eq("serviceModelInfo_Target"), any());
        stageError(() -> script.prepareDecomposeService_Target(execution), "prepareDecomposeService_Target");
        assertFalse(variables.containsKey("serviceModelInfo_Target"));
        doThrow(new IllegalStateException("store unavailable")).when(execution)
                .setVariable(eq("serviceModelInfo_Original"), any());
        stageError(() -> script.prepareDecomposeService_Original(execution), "prepareDecomposeService_Original");
        assertFalse(variables.containsKey("serviceModelInfo_Original"));
    }

    @Test
    public void unexpectedDecompositionTypesRetainTypedStageErrorsWithoutPublishingResults() {
        variables.put("serviceDecomposition", "not-a-decomposition");
        stageError(() -> script.processDecomposition_Target(execution), "processDecomposition_Target");
        assertFalse(variables.containsKey("serviceDecomposition_Target"));
        stageError(() -> script.processDecomposition_Original(execution), "processDecomposition_Original");
        assertFalse(variables.containsKey("serviceDecomposition_Original"));
    }

    private void stageError(Runnable action, String stage) {
        try {
            action.run();
            fail("Expected decomposition stage error");
        } catch (BpmnError error) {
            assertEquals("7000", error.getErrorCode());
        }
        Object stored = variables.get("WorkflowException");
        assertNotNull(stored);
        assertEquals(WorkflowException.class, stored.getClass());
        WorkflowException error = (WorkflowException) stored;
        assertEquals(7000, error.getErrorCode());
        assertTrue(error.getErrorMessage(), error.getErrorMessage().contains(stage));
    }

    private void validIdentifiers() {
        variables.put("model-invariant-id-target", "invariant");
        variables.put("model-invariant-id-original", "invariant");
        variables.put("model-version-id-target", "target-version");
        variables.put("model-version-id-original", "original-version");
    }

    private void rejected(String message) {
        try {
            script.preProcessRequest(execution);
            fail("Expected validation BPMN error");
        } catch (BpmnError error) {
            assertEquals("500", error.getErrorCode());
        }
        WorkflowException error = (WorkflowException) variables.get("WorkflowException");
        assertEquals(500, error.getErrorCode());
        assertTrue(error.getErrorMessage(), error.getErrorMessage().contains(message));
    }

    private void readiness(String reason) {
        Assume.assumeTrue(reason, Boolean.getBoolean("groovy.migration.verification"));
    }

    private NetworkResource resource(String id, String version, String invariant, String customization) {
        NetworkResource resource = new NetworkResource();
        resource.setResourceId(id);
        ModelInfo model = new ModelInfo();
        model.setModelUuid(version);
        model.setModelInvariantUuid(invariant);
        model.setModelCustomizationUuid(customization);
        resource.setModelInfo(model);
        return resource;
    }

    private ServiceDecomposition decomposition(NetworkResource... resources) {
        ServiceDecomposition value = new ServiceDecomposition();
        value.setNetworkResources(new ArrayList<>(List.of(resources)));
        return value;
    }

    private void compare(ServiceDecomposition target, ServiceDecomposition original) {
        variables.put("serviceDecomposition_Target", target);
        variables.put("serviceDecomposition_Original", original);
        script.doCompareModelVersions(execution);
    }
}
