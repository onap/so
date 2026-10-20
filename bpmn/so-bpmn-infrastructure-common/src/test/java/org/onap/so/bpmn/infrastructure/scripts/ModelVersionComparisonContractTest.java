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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.core.domain.ModelInfo;
import org.onap.so.bpmn.core.domain.Resource;
import org.onap.so.bpmn.core.domain.ServiceDecomposition;
import org.onap.so.bpmn.core.domain.VnfResource;

public class ModelVersionComparisonContractTest {
    private final DoCompareModelVersions script = new DoCompareModelVersions();
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();

    @Before
    public void configureExecution() {
        variables.put("testProcessKey", "DoCompareModelVersions");
        variables.put("model-invariant-id-target", "service-invariant");
        variables.put("model-invariant-id-original", new String("service-invariant"));
        variables.put("model-version-id-target", "service-version-2");
        variables.put("model-version-id-original", "service-version-1");
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
    }

    @Test
    public void validDistinctVersionsOfTheSameServiceEstablishTheProcessPrefix() {
        script.preProcessRequest(execution);
        assertEquals("DCMPMDV_", variables.get("prefix"));
        assertFalse(variables.containsKey("WorkflowException"));
    }

    @Test
    public void decompositionRequestsKeepTargetAndOriginalModelIdentitySeparate() {
        script.prepareDecomposeService_Target(execution);
        script.prepareDecomposeService_Original(execution);
        assertModelInfo("serviceModelInfo_Target", "service-version-2");
        assertModelInfo("serviceModelInfo_Original", "service-version-1");
    }

    @Test
    public void decompositionHandoffRetainsTheTypedTargetAndOriginalObjects() {
        ServiceDecomposition target = decomposition(resource("target", "version-2", "invariant", "custom"));
        ServiceDecomposition original = decomposition(resource("original", "version-1", "invariant", "custom"));
        variables.put("serviceDecomposition", target);
        script.processDecomposition_Target(execution);
        variables.put("serviceDecomposition", original);
        script.processDecomposition_Original(execution);
        assertSame(target, variables.get("serviceDecomposition_Target"));
        assertSame(original, variables.get("serviceDecomposition_Original"));
    }

    @Test
    public void emptyDecompositionsProduceTypedEmptyAdditionAndDeletionLists() {
        compare(decomposition(), decomposition());
        assertTrue(resources("addResourceList").isEmpty());
        assertTrue(resources("delResourceList").isEmpty());
    }

    @Test
    public void matchingModelTriplesCancelDespiteDifferentResourceIdsAndStringObjects() {
        VnfResource target = resource("new-instance", "version", "invariant", "custom");
        VnfResource original =
                resource("old-instance", new String("version"), new String("invariant"), new String("custom"));
        compare(decomposition(target), decomposition(original));
        assertTrue(resources("addResourceList").isEmpty());
        assertTrue(resources("delResourceList").isEmpty());
    }

    @Test
    public void aDifferenceInAnyModelIdentityFieldRequiresReplacement() {
        String[][] changes = {{"new-version", "invariant", "custom"}, {"version", "new-invariant", "custom"},
                {"version", "invariant", "new-custom"}};
        for (String[] change : changes) {
            VnfResource target = resource("new-instance", change[0], change[1], change[2]);
            VnfResource original = resource("old-instance", "version", "invariant", "custom");
            compare(decomposition(target), decomposition(original));
            assertEquals(Collections.singletonList(target), resources("addResourceList"));
            assertEquals(Collections.singletonList(original), resources("delResourceList"));
        }
    }

    @Test
    public void additionsAndDeletionsKeepTheirRespectiveDecompositionOrder() {
        VnfResource sharedTarget = resource("shared-target", "shared", "invariant", "custom");
        VnfResource sharedOriginal = resource("shared-original", "shared", "invariant", "custom");
        VnfResource addFirst = resource("add-first", "add-1", "invariant", "custom");
        VnfResource addSecond = resource("add-second", "add-2", "invariant", "custom");
        VnfResource deleteFirst = resource("delete-first", "delete-1", "invariant", "custom");
        VnfResource deleteSecond = resource("delete-second", "delete-2", "invariant", "custom");
        compare(decomposition(addFirst, sharedTarget, addSecond),
                decomposition(deleteFirst, sharedOriginal, deleteSecond));
        assertEquals(Arrays.asList(addFirst, addSecond), resources("addResourceList"));
        assertEquals(Arrays.asList(deleteFirst, deleteSecond), resources("delResourceList"));
        assertSame(addFirst, resources("addResourceList").get(0));
        assertSame(deleteFirst, resources("delResourceList").get(0));
    }

    @Test
    public void absentOptionalCustomizationMatchesAnotherAbsentCustomization() {
        compare(decomposition(resource("target", "version", "invariant", null)),
                decomposition(resource("original", "version", "invariant", null)));
        assertTrue(resources("addResourceList").isEmpty());
        assertTrue(resources("delResourceList").isEmpty());
    }

    @Test
    public void modelIdentifiersRemainCaseSensitive() {
        VnfResource target = resource("target", "Version", "invariant", "custom");
        VnfResource original = resource("original", "version", "invariant", "custom");
        compare(decomposition(target), decomposition(original));
        assertEquals(Collections.singletonList(target), resources("addResourceList"));
        assertEquals(Collections.singletonList(original), resources("delResourceList"));
    }

    private void compare(ServiceDecomposition target, ServiceDecomposition original) {
        List<Resource> beforeTarget = target.getServiceResources();
        List<Resource> beforeOriginal = original.getServiceResources();
        variables.put("serviceDecomposition_Target", target);
        variables.put("serviceDecomposition_Original", original);
        script.doCompareModelVersions(execution);
        assertEquals(beforeTarget, target.getServiceResources());
        assertEquals(beforeOriginal, original.getServiceResources());
        assertTrue(variables.get("addResourceList") instanceof List);
        assertTrue(variables.get("delResourceList") instanceof List);
        assertNotSame(target.getVnfResources(), variables.get("addResourceList"));
        assertNotSame(original.getVnfResources(), variables.get("delResourceList"));
    }

    private List<?> resources(String key) {
        return (List<?>) variables.get(key);
    }

    private void assertModelInfo(String variable, String version) {
        JSONObject model = new JSONObject((String) variables.get(variable));
        assertEquals(3, model.length());
        assertEquals("service-invariant", model.getString("modelInvariantUuid"));
        assertEquals(version, model.getString("modelUuid"));
        assertEquals("", model.getString("modelVersion"));
    }

    private static ServiceDecomposition decomposition(VnfResource... resources) {
        ServiceDecomposition result = new ServiceDecomposition();
        result.setVnfResources(Collections.unmodifiableList(Arrays.asList(resources)));
        return result;
    }

    private static VnfResource resource(String id, String version, String invariant, String customization) {
        ModelInfo model = new ModelInfo();
        model.setModelUuid(version);
        model.setModelInvariantUuid(invariant);
        model.setModelCustomizationUuid(customization);
        VnfResource result = new VnfResource();
        result.setResourceId(id);
        result.setModelInfo(model);
        return result;
    }
}
