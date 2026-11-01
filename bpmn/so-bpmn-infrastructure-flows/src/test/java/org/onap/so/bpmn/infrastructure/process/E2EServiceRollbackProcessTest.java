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
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Assume;
import org.junit.Test;
import org.onap.aaiclient.client.aai.AAIVersion;
import org.onap.so.BaseBPMNTest;
import org.onap.so.bpmn.core.RollbackData;
import org.onap.so.bpmn.core.WorkflowException;

public class E2EServiceRollbackProcessTest extends BaseBPMNTest {
    private static final String CREATE = "DoCreateE2EServiceInstanceRollback";
    private static final String UPDATE = "DoUpdateE2EServiceInstanceRollback";
    private static final String BUSINESS = "/business/customers/customer/customer-1/service-subscriptions/"
            + "service-subscription/subscription-1/service-instances/service-instance/service-1";
    private static final String INVENTORY = "/aai/v[0-9]+" + BUSINESS;

    @Before
    public void prepareContracts() throws Exception {
        assertProductionResource(CREATE);
        assertProductionResource(UPDATE);
        wireMockServer.resetAll();
        variables.clear();
        variables.put("msoRequestId", "request-1");
        variables.put("disableRollback", "false");
        variables.put("WorkflowException", new WorkflowException("original-request", 5001, "creation failed"));
        variables.put("serviceInstanceName", "service Gr\u00fc\u00dfe & SO");
        variables.put("modelInvariantUuid", "invariant-1");
        variables.put("model-version-id-original", "original-model");
        variables.put("model-version-id-target", "target-model");
        wireMockServer.stubFor(get(urlPathMatching(INVENTORY)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withHeader("Connection", "close")
                .withBody("{\"service-instance-id\":\"service-1\",\"resource-version\":\"version-7\"}")));
        stubDelete(204);
        JSONObject location = new JSONObject().put("resource-type", "service-instance").put("resource-link",
                "http://localhost:" + wireMockPort + "/aai/" + AAIVersion.LATEST + BUSINESS);
        wireMockServer.stubFor(get(urlPathMatching("/aai/v[0-9]+/nodes/service-instances/service-instance/service-1"))
                .withQueryParam("format", equalTo("pathed"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(new JSONObject().put("results", new JSONArray().put(location)).toString())));
        wireMockServer.stubFor(post(urlPathMatching(INVENTORY)).withHeader("X-HTTP-Method-Override", equalTo("PATCH"))
                .willReturn(aResponse().withStatus(200).withHeader("Connection", "close")));
    }

    @Test
    public void creationWithoutRecordedResourcesSkipsEveryExternalAction() {
        assertSkipped(start(CREATE));
    }

    @Test
    public void updateWithoutRecordedResourcesSkipsEveryExternalAction() {
        assertSkipped(start(UPDATE));
    }

    @Test
    public void disabledCreationRollbackDoesNotDeleteResources() {
        requestInventoryRollback();
        variables.put("disableRollback", "true");
        assertSkipped(start(CREATE));
    }

    @Test
    public void disabledUpdateRollbackDoesNotMutateInventory() {
        requestInventoryRollback();
        variables.put("disableRollback", "true");
        assertSkipped(start(UPDATE));
    }

    @Test
    public void creationCleanupDeletesOnlyTheRecordedServiceAndKeepsTheOriginalRequestError() {
        requestInventoryRollback();
        ProcessInstance process = start(CREATE);
        assertDeletion();
        assertOriginalError(process);
        assertNull(historic(process, "rollbackData"));
        assertActivity(process, "deleteSI", 1);
        assertActivity(process, "PostProcess_ScriptTask", 1);
        assertActivity(process, "EndEvent_3", 1);
        assertActivity(process, "CallActivity_0ak0ezb", 0);
        assertActivity(process, "CallActivity_0ppz9nd", 0);
    }

    @Test
    public void successfulCreationCleanupIsReportedAsRolledBack() {
        Assume.assumeTrue("SO-4457: E2E create rollback reports failure after successful inventory deletion",
                Boolean.getBoolean("groovy.migration.verification"));
        requestInventoryRollback();
        ProcessInstance process = start(CREATE);
        assertDeletion();
        assertEquals(Boolean.TRUE, historic(process, "rolledBack"));
        assertNull(historic(process, "rollbackError"));
        assertOriginalError(process);
    }

    @Test
    public void rejectedCreationCleanupCannotBeReportedAsSuccessfulRollback() {
        requestInventoryRollback();
        stubDelete(503);
        ProcessInstance process = start(CREATE);
        assertDeletion();
        assertEquals(Boolean.FALSE, historic(process, "rolledBack"));
        assertNotNull(historic(process, "rollbackError"));
        assertOriginalError(process);
    }

    @Test
    public void disabledCreationCleanupDoesNotClaimThatItRolledBack() {
        requestInventoryRollback();
        variables.put("disableRollback", "true");
        ProcessInstance process = start(CREATE);
        assertSkipped(process);
        assertFalse(Boolean.TRUE.equals(historic(process, "rolledBack")));
    }

    @Test
    public void disabledUpdateCleanupDoesNotClaimThatItRolledBack() {
        requestInventoryRollback();
        variables.put("disableRollback", "true");
        ProcessInstance process = start(UPDATE);
        assertSkipped(process);
        assertFalse(Boolean.TRUE.equals(historic(process, "rolledBack")));
    }

    @Test
    public void inventoryOnlyUpdateRestoresTheOriginalModelBeforeDeclaringRollbackComplete() {
        Assume.assumeTrue("SO-4457: E2E update rollback skips the requested inventory restoration",
                Boolean.getBoolean("groovy.migration.verification"));
        requestInventoryRollback();
        ProcessInstance process = start(UPDATE);
        wireMockServer.verify(1,
                postRequestedFor(urlPathMatching(INVENTORY)).withHeader("X-HTTP-Method-Override", equalTo("PATCH")));
        JSONObject payload = new JSONObject(
                wireMockServer.findAll(postRequestedFor(urlPathMatching(INVENTORY))).get(0).getBodyAsString());
        assertEquals("service-1", payload.getString("service-instance-id"));
        assertEquals("original-model", payload.getString("model-version-id"));
        assertEquals("invariant-1", payload.getString("model-invariant-id"));
        assertEquals("service Gr\u00fc\u00dfe & SO", payload.getString("service-instance-name"));
        assertEquals(Boolean.TRUE, historic(process, "rolledBack"));
        assertOriginalError(process);
    }

    private void requestInventoryRollback() {
        RollbackData data = new RollbackData();
        data.put("SERVICEINSTANCE", "serviceInstanceId", "service-1");
        data.put("SERVICEINSTANCE", "subscriptionServiceType", "subscription-1");
        data.put("SERVICEINSTANCE", "globalSubscriberId", "customer-1");
        data.put("SERVICEINSTANCE", "rollbackAAI", "true");
        data.put("SERVICEINSTANCE", "rollbackSDNC", "false");
        data.put("SERVICEINSTANCE", "rollbackAdded", "false");
        data.put("SERVICEINSTANCE", "rollbackDeleted", "false");
        variables.put("rollbackData", data);
    }

    private ProcessInstance start(String key) {
        ProcessInstance process =
                runtimeService.startProcessInstanceByKey(key, UUID.randomUUID().toString(), variables);
        assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(process.getId()).count());
        return process;
    }

    private void assertDeletion() {
        wireMockServer.verify(1, getRequestedFor(urlPathMatching(INVENTORY)));
        wireMockServer.verify(1, deleteRequestedFor(urlPathMatching(INVENTORY)).withQueryParam("resource-version",
                equalTo("version-7")));
        assertEquals(2, wireMockServer.getAllServeEvents().size());
    }

    private void assertSkipped(ProcessInstance process) {
        assertEquals(Boolean.TRUE, historic(process, "skipRollback"));
        assertOriginalError(process);
        assertNull(historic(process, "rollbackData"));
        assertActivity(process, "preProcessRequest_ScriptTask", 1);
        assertActivity(process, "PostProcess_ScriptTask", 1);
        assertActivity(process, "EndEvent_3", 1);
        assertTrue(wireMockServer.getAllServeEvents().isEmpty());
    }

    private void assertOriginalError(ProcessInstance process) {
        Object value = historic(process, "WorkflowException");
        assertNotNull(value);
        assertEquals(WorkflowException.class, value.getClass());
        WorkflowException error = (WorkflowException) value;
        assertEquals("original-request", error.getProcessKey());
        assertEquals(5001, error.getErrorCode());
        assertEquals("creation failed", error.getErrorMessage());
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

    private void stubDelete(int status) {
        wireMockServer.stubFor(delete(urlPathMatching(INVENTORY))
                .willReturn(aResponse().withStatus(status).withHeader("Connection", "close")));
    }

    private void assertProductionResource(String key) throws Exception {
        String path = "subprocess/" + key + ".bpmn";
        URL resource = getClass().getClassLoader().getResource(path);
        assertNotNull(resource);
        String location = resource.toExternalForm();
        assertTrue(location,
                location.contains("/so-bpmn-infrastructure-flows/target/classes/")
                        || (location.matches("jar:.*/so-bpmn-infrastructure-flows-[^/]+\\.jar!/" + path)
                                && !location.contains("-tests.jar!/")));
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(key)
                .latestVersion().singleResult();
        assertNotNull(definition);
        try (InputStream original = resource.openStream();
                InputStream deployed = repositoryService.getResourceAsStream(definition.getDeploymentId(),
                        definition.getResourceName())) {
            assertArrayEquals("The engine must execute the packaged production BPMN", original.readAllBytes(),
                    deployed.readAllBytes());
        }
    }
}
