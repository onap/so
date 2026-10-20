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
package org.onap.so.bpmn.infrastructure.process;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.BaseBPMNTest;
import org.onap.so.bpmn.core.domain.Resource;
import org.onap.so.bpmn.core.domain.ServiceDecomposition;

public class ModelVersionComparisonProcessTest extends BaseBPMNTest {
    private static final String PROCESS = "DoCompareModelVersions";
    private static final String CATALOG_PATH = "/v2/serviceResources";

    @Before
    public void prepareContracts() throws Exception {
        assertProductionResource(PROCESS, "subprocess/DoCompareModelVersions.bpmn", "so-bpmn-infrastructure-flows");
        assertProductionResource("DecomposeService", "subprocess/BuildingBlock/DecomposeService.bpmn", "MSOCommonBPMN");
        wireMockServer.resetAll();
        variables.clear();
        variables.put("msoRequestId", "request-1");
        variables.put("serviceInstanceId", "service-1");
        variables.put("model-invariant-id-target", "service-invariant");
        variables.put("model-invariant-id-original", "service-invariant");
        variables.put("model-version-id-target", "version-2");
        variables.put("model-version-id-original", "version-1");
        variables.put("isDebugLogEnabled", Boolean.TRUE);
        stubCatalog("version-2", 200, decomposition("version-2", "shared"));
        stubCatalog("version-1", 200, decomposition("version-1", "shared"));
    }

    @Test
    public void equivalentModelsUseBothRealDecompositionCallsAndProduceNoChanges() {
        ProcessInstance process = start();
        assertNull(historic(process, "WorkflowException"));
        assertTrue(models(process, "addResourceList").isEmpty());
        assertTrue(models(process, "delResourceList").isEmpty());
        assertSuccessfulActivities(process);
        assertTypedDecomposition(process, "serviceDecomposition_Target", "version-2");
        assertTypedDecomposition(process, "serviceDecomposition_Original", "version-1");
        assertCatalogCalls(1, 1);
        assertEquals(2, historyService.createHistoricProcessInstanceQuery().superProcessInstanceId(process.getId())
                .processDefinitionKey("DecomposeService").finished().count());
    }

    @Test
    public void changedModelsPreserveAdditionAndDeletionOrderAcrossCallMappings() {
        stubCatalog("version-2", 200, decomposition("version-2", "new-2", "shared", "new-1"));
        stubCatalog("version-1", 200, decomposition("version-1", "old-2", "shared", "old-1"));
        ProcessInstance process = start();
        assertNull(historic(process, "WorkflowException"));
        assertEquals(Arrays.asList("new-2", "new-1"), models(process, "addResourceList"));
        assertEquals(Arrays.asList("old-2", "old-1"), models(process, "delResourceList"));
        assertSuccessfulActivities(process);
        assertCatalogCalls(1, 1);
    }

    @Test
    public void unavailableTargetCatalogStopsBeforeTheOriginalQueryOrComparison() {
        stubCatalog("version-2", 503, "{}");
        ProcessInstance process = start();
        assertFailed(process);
        assertActivity(process, "CallActivity_1fc56sd", 0);
        assertCatalogCalls(1, 0);
    }

    @Test
    public void missingOriginalCatalogStopsBeforeComparison() {
        stubCatalog("version-1", 404, "{}");
        ProcessInstance process = start();
        assertFailed(process);
        assertActivity(process, "ScriptTask_1odhh8p", 1);
        assertCatalogCalls(1, 1);
    }

    @Test
    public void malformedSuccessfulTargetDecompositionStopsBeforeTheOriginalQuery() {
        stubCatalog("version-2", 200, "{\"unexpectedRoot\":{}}");
        ProcessInstance process = start();
        assertFailed(process);
        assertActivity(process, "CallActivity_1fc56sd", 0);
        assertCatalogCalls(1, 0);
    }

    @Test
    public void missingModelIdentityStopsBeforeAnyCatalogRequest() {
        variables.remove("model-version-id-target");
        ProcessInstance process = start();
        assertFailed(process);
        assertActivity(process, "CallActivity_1va14ul", 0);
        assertTrue(wireMockServer.getAllServeEvents().isEmpty());
    }

    private void assertSuccessfulActivities(ProcessInstance process) {
        for (String activity : Arrays.asList("ScriptTask_0nie46r", "ScriptTask_1fau2rr", "CallActivity_1va14ul",
                "ScriptTask_1odhh8p", "ScriptTask_1i06996", "CallActivity_1fc56sd", "ScriptTask_1d9qb54",
                "ScriptTask_04rn9mp", "EndEvent_0x8im5g")) {
            assertActivity(process, activity, 1);
        }
        assertActivity(process, "ScriptTask_0xk9fk3", 0);
    }

    private void assertFailed(ProcessInstance process) {
        assertNotNull(historic(process, "WorkflowException"));
        assertActivity(process, "ScriptTask_0xk9fk3", 1);
        assertActivity(process, "ScriptTask_04rn9mp", 0);
        assertActivity(process, "EndEvent_0x8im5g", 0);
    }

    private void assertTypedDecomposition(ProcessInstance process, String name, String model) {
        Object value = historic(process, name);
        assertTrue(name, value instanceof ServiceDecomposition);
        ServiceDecomposition decomposition = (ServiceDecomposition) value;
        assertEquals(model, decomposition.getModelInfo().getModelUuid());
        assertEquals("service-1", decomposition.getServiceInstance().getInstanceId());
        assertEquals(1, decomposition.getServiceResources().size());
    }

    private List<String> models(ProcessInstance process, String variable) {
        Object value = historic(process, variable);
        assertTrue(variable, value instanceof List);
        List<String> models = new ArrayList<>();
        for (Object resource : (List<?>) value) {
            assertTrue(variable, resource instanceof Resource);
            models.add(((Resource) resource).getModelInfo().getModelUuid());
        }
        return models;
    }

    private void assertCatalogCalls(int target, int original) {
        wireMockServer.verify(target,
                getRequestedFor(urlPathEqualTo(CATALOG_PATH)).withQueryParam("serviceModelUuid", equalTo("version-2")));
        wireMockServer.verify(original,
                getRequestedFor(urlPathEqualTo(CATALOG_PATH)).withQueryParam("serviceModelUuid", equalTo("version-1")));
        wireMockServer.verify(target + original, getRequestedFor(urlPathEqualTo(CATALOG_PATH))
                .withHeader("X-FromAppId", equalTo("BPMN")).withHeader("Accept", containing("application/json")));
        assertEquals(target + original, wireMockServer.getAllServeEvents().size());
    }

    private ProcessInstance start() {
        ProcessInstance process =
                runtimeService.startProcessInstanceByKey(PROCESS, UUID.randomUUID().toString(), variables);
        assertEquals(0L, runtimeService.createProcessInstanceQuery().processInstanceId(process.getId()).count());
        return process;
    }

    private void stubCatalog(String version, int status, String body) {
        wireMockServer.stubFor(
                get(urlPathEqualTo(CATALOG_PATH)).withQueryParam("serviceModelUuid", equalTo(version)).willReturn(
                        aResponse().withStatus(status).withHeader("Content-Type", "application/json").withBody(body)));
    }

    private String decomposition(String modelVersion, String... versions) {
        JSONArray resources = new JSONArray();
        for (String version : versions) {
            JSONObject model = new JSONObject().put("modelUuid", version).put("modelInvariantUuid", "vnf-invariant")
                    .put("modelCustomizationUuid", "custom-1");
            resources.put(new JSONObject().put("modelInfo", model));
        }
        JSONObject service = new JSONObject().put("modelInfo", new JSONObject().put("modelUuid", modelVersion))
                .put("serviceVnfs", resources).put("serviceNetworks", new JSONArray())
                .put("serviceAllottedResources", new JSONArray());
        return new JSONObject().put("serviceResources", service).toString();
    }

    private Object historic(ProcessInstance process, String name) {
        HistoricVariableInstance value = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(process.getId()).variableName(name).singleResult();
        return value == null ? null : value.getValue();
    }

    private void assertActivity(ProcessInstance process, String activity, long expected) {
        assertEquals(activity, expected, historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(process.getId()).activityId(activity).finished().count());
    }

    private void assertProductionResource(String key, String resourceName, String module) throws Exception {
        URL resource = getClass().getClassLoader().getResource(resourceName);
        assertNotNull(resourceName, resource);
        String location = resource.toExternalForm();
        assertTrue(location,
                location.contains("/" + module + "/target/classes/")
                        || (location.matches("jar:.*/" + module + "-[^/]+\\.jar!/" + resourceName)
                                && !location.contains("-tests.jar!/")));
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(key)
                .latestVersion().singleResult();
        assertNotNull(key, definition);
        try (InputStream original = resource.openStream();
                InputStream deployed = repositoryService.getResourceAsStream(definition.getDeploymentId(),
                        definition.getResourceName())) {
            assertArrayEquals("The engine must execute packaged production BPMN", original.readAllBytes(),
                    deployed.readAllBytes());
        }
    }
}
