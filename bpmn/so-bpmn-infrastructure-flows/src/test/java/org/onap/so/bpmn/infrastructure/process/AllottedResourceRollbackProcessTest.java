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
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.onap.so.BaseBPMNTest;
import org.onap.so.bpmn.core.RollbackData;

public class AllottedResourceRollbackProcessTest extends BaseBPMNTest {
    private static final String[] FAMILIES = {"BRG", "TXC"};
    private static final String RESOURCE_PATH = "/business/customers/customer/customer-1/service-subscriptions/"
            + "service-subscription/service-type/service-instances/service-instance/parent-1/"
            + "allotted-resources/allotted-resource/resource-1";
    private static final String HTTP_PATH = "/aai/v[0-9]+" + RESOURCE_PATH;

    @Test
    public void createdResourceIsDeletedExactlyOnce() throws Exception {
        for (String family : FAMILIES) {
            prepare(204);
            ProcessInstance process = start(family, "false", true);
            assertEquals(family, Boolean.TRUE, historic(process, "rolledBack"));
            assertEquals(family, "true", historic(process, "wasDeleted"));
            assertNull(historic(process, "rollbackError"));
            assertNull(historic(process, "rollbackData"));
            assertActivity(process, "DeleteAAIAR", 1);
            assertActivity(process, "ProcessError", 0);
            assertDeleteContract();
        }
    }

    @Test
    public void alreadyDeletedResourceIsAnIdempotentSuccess() throws Exception {
        for (String family : FAMILIES) {
            prepare(404);
            ProcessInstance process = start(family, "false", true);
            assertEquals(family, Boolean.TRUE, historic(process, "rolledBack"));
            assertEquals(family, "true", historic(process, "wasDeleted"));
            assertNull(historic(process, "rollbackError"));
            assertActivity(process, "ProcessError", 0);
            assertDeleteContract();
        }
    }

    @Test
    public void resourceVersionConflictRecordsRollbackFailure() throws Exception {
        assertDeleteFailure(412);
    }

    @Test
    public void unavailableInventoryRecordsRollbackFailure() throws Exception {
        assertDeleteFailure(503);
    }

    @Test
    public void disabledRollbackDoesNotDeleteTheResource() throws Exception {
        for (String family : FAMILIES) {
            prepare(204);
            ProcessInstance process = start(family, "true", true);
            assertEquals(family, Boolean.TRUE, historic(process, "skipRollback"));
            assertNull(historic(process, "rolledBack"));
            assertNull(historic(process, "rollbackData"));
            assertActivity(process, "DeleteAAIAR", 0);
            assertTrue(family, wireMockServer.getAllServeEvents().isEmpty());
        }
    }

    @Test
    public void noCreatedResourceSkipsCompensation() throws Exception {
        for (String family : FAMILIES) {
            prepare(204);
            ProcessInstance process = start(family, "false", false);
            assertEquals(family, Boolean.TRUE, historic(process, "skipRollback"));
            assertNull(historic(process, "rolledBack"));
            assertNull(historic(process, "rollbackData"));
            assertActivity(process, "DeleteAAIAR", 0);
            assertTrue(family, wireMockServer.getAllServeEvents().isEmpty());
        }
    }

    private void assertDeleteFailure(int status) throws Exception {
        for (String family : FAMILIES) {
            prepare(status);
            ProcessInstance process = start(family, "false", true);
            assertEquals(family, Boolean.FALSE, historic(process, "rolledBack"));
            assertEquals(family, "Caught exception in AllottedResource Create Rollback",
                    historic(process, "rollbackError"));
            assertNull(historic(process, "rollbackData"));
            assertNull(historic(process, "wasDeleted"));
            assertNull(historic(process, "WorkflowException"));
            assertActivity(process, "DeleteAAIAR", 1);
            assertActivity(process, "ProcessError", 1);
            assertActivity(process, "postProcessRequest", 0);
            assertDeleteContract();
        }
    }

    @Test
    public void resourceAbsentBeforeDeletionNeedsNoDeleteRequest() throws Exception {
        for (String family : FAMILIES) {
            prepare(204);
            wireMockServer.stubFor(get(urlPathMatching(HTTP_PATH)).willReturn(aResponse().withStatus(404)));
            ProcessInstance process = start(family, "false", true);
            assertEquals(family, Boolean.TRUE, historic(process, "rolledBack"));
            assertEquals(family, "true", historic(process, "wasDeleted"));
            assertNull(historic(process, "rollbackError"));
            assertActivity(process, "ProcessError", 0);
            wireMockServer.verify(1, getRequestedFor(urlPathMatching(HTTP_PATH)));
            wireMockServer.verify(0, deleteRequestedFor(urlPathMatching(HTTP_PATH)));
            wireMockServer.verify(0, postRequestedFor(urlPathMatching(HTTP_PATH)));
        }
    }

    @Test
    public void absentInventoryStopsBeforeSdncCompensation() throws Exception {
        assertPendingDeleteFailure(404, 0);
    }

    @Test
    public void rejectedPendingDeleteUpdateStopsBeforeSdncCompensation() throws Exception {
        assertPendingDeleteFailure(200, 1);
    }

    private void assertPendingDeleteFailure(int lookupStatus, int updateCount) throws Exception {
        for (String family : FAMILIES) {
            prepare(204);
            if (lookupStatus != 200) {
                wireMockServer
                        .stubFor(get(urlPathMatching(HTTP_PATH)).willReturn(aResponse().withStatus(lookupStatus)));
            }
            wireMockServer.stubFor(post(urlPathMatching(HTTP_PATH))
                    .withHeader("X-HTTP-Method-Override", equalTo("PATCH")).willReturn(aResponse().withStatus(503)));
            ProcessInstance process = start(family, "false", true, true);
            assertEquals(family, Boolean.FALSE, historic(process, "rolledBack"));
            assertEquals(family, "Caught exception in AllottedResource Create Rollback",
                    historic(process, "rollbackError"));
            assertNull(historic(process, "rollbackData"));
            assertNull(historic(process, "wasDeleted"));
            assertActivity(process, "UpdateAAIARPendingDelete", 1);
            assertActivity(process, "ProcessError", 1);
            assertActivity(process, "DeleteAAIAR", 0);
            assertActivity(process, "SDNCDeactivate", 0);
            assertActivity(process, "SDNCDelete", 0);
            assertActivity(process, "SDNCUnassign", 0);
            wireMockServer.verify(1, getRequestedFor(urlPathMatching(HTTP_PATH)));
            wireMockServer.verify(updateCount,
                    postRequestedFor(urlPathMatching(HTTP_PATH)).withHeader("X-HTTP-Method-Override", equalTo("PATCH"))
                            .withHeader("Content-Type", equalTo("application/merge-patch+json"))
                            .withRequestBody(matchingJsonPath("$.orchestration-status", equalTo("PendingDelete"))));
            wireMockServer.verify(0, deleteRequestedFor(urlPathMatching(HTTP_PATH)));
            assertEquals(1 + updateCount, wireMockServer.getAllServeEvents().size());
        }
    }

    private void assertDeleteContract() {
        wireMockServer.verify(1, getRequestedFor(urlPathMatching(HTTP_PATH)));
        wireMockServer.verify(1, deleteRequestedFor(urlPathMatching(HTTP_PATH))
                .withQueryParam("resource-version", equalTo("version-7")).withHeader("X-FromAppId", equalTo("MSO")));
        wireMockServer.verify(0, postRequestedFor(urlPathMatching(HTTP_PATH)));
        assertEquals(2, wireMockServer.getAllServeEvents().size());
    }

    private void prepare(int deleteStatus) {
        wireMockServer.resetAll();
        variables.clear();
        wireMockServer.stubFor(get(urlPathMatching(HTTP_PATH))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(
                        "{\"id\":\"resource-1\",\"resource-version\":\"version-7\",\"orchestration-status\":\"Created\"}")));
        wireMockServer.stubFor(delete(urlPathMatching(HTTP_PATH)).willReturn(aResponse().withStatus(deleteStatus)));
    }

    private ProcessInstance start(String family, String disableRollback, boolean created) throws Exception {
        return start(family, disableRollback, created, false);
    }

    private ProcessInstance start(String family, String disableRollback, boolean created, boolean sdnc)
            throws Exception {
        String key = "DoCreateAllottedResource" + family + "Rollback";
        assertProductionResource(key);
        RollbackData rollback = new RollbackData();
        if (created) {
            String type = "DCAR" + family + "_";
            rollback.put(type, "rollbackAAI", "true");
            if (sdnc) {
                rollback.put(type, "rollbackSDNCassign", "true");
            }
            rollback.put(type, "serviceInstanceId", "service-1");
            rollback.put(type, "parentServiceInstanceId", "parent-1");
            rollback.put(type, "aaiARPath", RESOURCE_PATH);
            rollback.put("SERVICEINSTANCE", "allottedResourceId", "resource-1");
        }
        variables.put("rollbackData", rollback);
        variables.put("disableRollback", disableRollback);
        variables.put("msoRequestId", "request-1");
        ProcessInstance process =
                runtimeService.startProcessInstanceByKey(key, UUID.randomUUID().toString(), variables);
        assertEquals(family, 0, runtimeService.createProcessInstanceQuery().processInstanceId(process.getId()).count());
        return process;
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

    private void assertProductionResource(String key) throws Exception {
        String resourceName = "subprocess/" + key + ".bpmn";
        URL resource = getClass().getClassLoader().getResource(resourceName);
        assertNotNull(resource);
        String location = resource.toExternalForm();
        assertTrue(location,
                location.contains("/so-bpmn-infrastructure-flows/target/classes/")
                        || (location.matches("jar:.*/so-bpmn-infrastructure-flows-[^/]+\\.jar!/" + resourceName)
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
