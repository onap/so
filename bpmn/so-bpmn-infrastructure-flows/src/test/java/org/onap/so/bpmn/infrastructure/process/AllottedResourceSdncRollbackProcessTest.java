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
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingXPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.BaseBPMNTest;
import org.onap.so.bpmn.core.RollbackData;
import org.springframework.beans.factory.annotation.Autowired;

public class AllottedResourceSdncRollbackProcessTest extends BaseBPMNTest {
    private static final String SDNC_PATH = "/SDNCAdapter";
    private static final String RESOURCE_PATH = "/business/customers/customer/customer-1/service-subscriptions/"
            + "service-subscription/service-type/service-instances/service-instance/parent-1/"
            + "allotted-resources/allotted-resource/resource-1";
    private static final String HTTP_PATH = "/aai/v[0-9]+" + RESOURCE_PATH;
    private final List<String> startedProcesses = new ArrayList<>();
    @Autowired
    private ProcessEngine processEngine;
    private JobExecutor executor;
    private boolean restartExecutor;

    @Before
    public void prepareDeterministicEngineAndContracts() throws Exception {
        executor = ((ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration()).getJobExecutor();
        restartExecutor = executor.isActive();
        // Background acquisition would race the explicit execution of SDNC continuation jobs.
        if (restartExecutor) {
            executor.shutdown();
        }
        ClockUtil.setCurrentTime(Date.from(Instant.parse("2020-01-01T00:00:00Z")));
        assertProductionResource("sdncAdapter", "subprocess/SDNCAdapterV1.bpmn", "MSOCommonBPMN");
        wireMockServer.resetAll();
        wireMockServer.stubFor(get(urlPathMatching(HTTP_PATH))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(
                        "{\"id\":\"resource-1\",\"resource-version\":\"version-7\",\"orchestration-status\":\"Created\"}")));
        wireMockServer.stubFor(post(urlPathMatching(HTTP_PATH)).withHeader("X-HTTP-Method-Override", equalTo("PATCH"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{}")));
        wireMockServer.stubFor(delete(urlPathMatching(HTTP_PATH)).willReturn(aResponse().withStatus(204)));
        wireMockServer.stubFor(post(urlEqualTo(SDNC_PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/soap+xml").withBody("<accepted/>")));
    }

    @After
    public void cleanProcessesAndRestoreEngineClock() {
        try {
            for (String id : startedProcesses) {
                if (runtimeService.createProcessInstanceQuery().processInstanceId(id).count() != 0) {
                    runtimeService.deleteProcessInstance(id, "test cleanup");
                }
            }
        } finally {
            ClockUtil.reset();
            if (restartExecutor) {
                executor.start();
            }
        }
    }

    @Test
    public void brgRollbackWaitsForEachControllerCallbackBeforeDeletingInventory() throws Exception {
        assertSuccessfulRollback("BRG");
    }

    @Test
    public void txcRollbackWaitsForEachControllerCallbackBeforeDeletingInventory() throws Exception {
        assertSuccessfulRollback("TXC");
    }

    @Test
    public void failedControllerCallbacksStopTheRemainingCompensationStages() throws Exception {
        List<String> actions = Arrays.asList("deactivate", "delete", "unassign");
        for (String family : Arrays.asList("BRG", "TXC")) {
            for (int failedStage = 0; failedStage < actions.size(); failedStage++) {
                wireMockServer.resetRequests();
                ProcessInstance process = start(family);
                for (int stage = 0; stage <= failedStage; stage++) {
                    wireMockServer.verify(1, postRequestedFor(urlEqualTo(SDNC_PATH)).withRequestBody(
                            matchingXPath("//*[local-name()='SvcAction' and text()='" + actions.get(stage) + "']")));
                    deliverCallback(controllerProcess(process), stage == failedStage ? 500 : 200);
                }
                assertFailedRollback(process);
                wireMockServer.verify(failedStage + 1, postRequestedFor(urlEqualTo(SDNC_PATH)));
                for (int stage = failedStage + 1; stage < actions.size(); stage++) {
                    wireMockServer.verify(0, postRequestedFor(urlEqualTo(SDNC_PATH)).withRequestBody(
                            matchingXPath("//*[local-name()='SvcAction' and text()='" + actions.get(stage) + "']")));
                }
            }
        }
    }

    @Test
    public void controllerTimeoutStopsInventoryDeletionAndRejectsLateCallbacks() throws Exception {
        for (String family : Arrays.asList("BRG", "TXC")) {
            wireMockServer.resetRequests();
            ProcessInstance process = start(family);
            ProcessInstance child = controllerProcess(process);
            String correlator = (String) runtimeService.getVariable(child.getId(), "SDNCA_requestId");
            List<Job> timers = processEngine.getManagementService().createJobQuery().processInstanceId(child.getId())
                    .timers().list();
            assertEquals(1, timers.size());
            assertNotNull(timers.get(0).getDuedate());
            ClockUtil.setCurrentTime(new Date(timers.get(0).getDuedate().getTime() + 1));
            processEngine.getManagementService().executeJob(timers.get(0).getId());
            assertFailedRollback(process);
            rejectCallback(correlator);
            wireMockServer.verify(1, postRequestedFor(urlEqualTo(SDNC_PATH)));
            wireMockServer.verify(0, deleteRequestedFor(urlPathMatching(HTTP_PATH)));
        }
    }

    @Test
    public void rejectedControllerRequestStopsBeforeCallbackWaitingOrInventoryDeletion() throws Exception {
        wireMockServer.stubFor(post(urlEqualTo(SDNC_PATH)).willReturn(aResponse().withStatus(503)));
        for (String family : Arrays.asList("BRG", "TXC")) {
            wireMockServer.resetRequests();
            ProcessInstance process = start(family);
            assertFailedRollback(process);
            assertEquals(0,
                    runtimeService.createProcessInstanceQuery().superProcessInstanceId(process.getId()).count());
            wireMockServer.verify(1, postRequestedFor(urlEqualTo(SDNC_PATH)));
        }
    }

    private ProcessInstance controllerProcess(ProcessInstance process) {
        ProcessInstance child = runtimeService.createProcessInstanceQuery().superProcessInstanceId(process.getId())
                .processDefinitionKey("sdncAdapter").singleResult();
        assertNotNull("A controller callback must have a waiting production subprocess", child);
        return child;
    }

    private void deliverCallback(ProcessInstance child, int code) {
        String correlator = (String) runtimeService.getVariable(child.getId(), "SDNCA_requestId");
        assertNotNull(correlator);
        runtimeService.createMessageCorrelation("sdncAdapterCallbackRequest")
                .processInstanceVariableEquals("SDNCA_requestId", correlator)
                .setVariable("sdncAdapterCallbackRequest", callback(correlator, code)).correlate();
        List<Job> continuations = processEngine.getManagementService().createJobQuery().processInstanceId(child.getId())
                .executable().list();
        assertEquals(1, continuations.size());
        processEngine.getManagementService().executeJob(continuations.get(0).getId());
    }

    private void assertFailedRollback(ProcessInstance process) {
        assertEquals(0L, runtimeService.createProcessInstanceQuery().processInstanceId(process.getId()).count());
        assertEquals(Boolean.FALSE, historic(process, "rolledBack"));
        assertNotNull(historic(process, "rollbackError"));
        assertNull(historic(process, "rollbackData"));
        wireMockServer.verify(0, deleteRequestedFor(urlPathMatching(HTTP_PATH)));
        assertEquals(1, historyService.createHistoricActivityInstanceQuery().processInstanceId(process.getId())
                .activityId("ProcessError").finished().count());
        assertEquals(0, historyService.createHistoricActivityInstanceQuery().processInstanceId(process.getId())
                .activityId("DeleteAAIAR").finished().count());
    }

    private void assertSuccessfulRollback(String family) throws Exception {
        ProcessInstance process = start(family);
        wireMockServer.verify(1, postRequestedFor(urlPathMatching(HTTP_PATH))
                .withRequestBody(matchingJsonPath("$.orchestration-status", equalTo("PendingDelete"))));
        String completedCorrelator = null;
        for (String action : Arrays.asList("deactivate", "delete", "unassign")) {
            wireMockServer.verify(1, postRequestedFor(urlEqualTo(SDNC_PATH))
                    .withRequestBody(matchingXPath("//*[local-name()='SvcAction' and text()='" + action + "']")));
            wireMockServer.verify(0, deleteRequestedFor(urlPathMatching(HTTP_PATH)));
            ProcessInstance child = runtimeService.createProcessInstanceQuery().superProcessInstanceId(process.getId())
                    .processDefinitionKey("sdncAdapter").singleResult();
            assertNotNull(action, child);
            String correlator = (String) runtimeService.getVariable(child.getId(), "SDNCA_requestId");
            assertNotNull(action, correlator);
            rejectCallback("unknown-" + correlator);
            if (completedCorrelator != null) {
                rejectCallback(completedCorrelator);
            }
            runtimeService.createMessageCorrelation("sdncAdapterCallbackRequest")
                    .processInstanceVariableEquals("SDNCA_requestId", correlator)
                    .setVariable("sdncAdapterCallbackRequest", callback(correlator, 200)).correlate();
            List<Job> continuations = processEngine.getManagementService().createJobQuery()
                    .processInstanceId(child.getId()).executable().list();
            assertEquals(action, 1, continuations.size());
            processEngine.getManagementService().executeJob(continuations.get(0).getId());
            completedCorrelator = correlator;
        }
        assertEquals(0L, runtimeService.createProcessInstanceQuery().processInstanceId(process.getId()).count());
        assertEquals(Boolean.TRUE, historic(process, "rolledBack"));
        assertEquals("true", historic(process, "wasDeleted"));
        assertNull(historic(process, "rollbackData"));
        assertNull(historic(process, "rollbackError"));
        wireMockServer.verify(1, deleteRequestedFor(urlPathMatching(HTTP_PATH)).withQueryParam("resource-version",
                equalTo("version-7")));
        wireMockServer.verify(3, postRequestedFor(urlEqualTo(SDNC_PATH)));
        assertEquals(1, historyService.createHistoricActivityInstanceQuery().processInstanceId(process.getId())
                .activityId("DeleteAAIAR").finished().count());
        assertEquals(0, historyService.createHistoricActivityInstanceQuery().processInstanceId(process.getId())
                .activityId("ProcessError").finished().count());
    }

    private ProcessInstance start(String family) throws Exception {
        String process = "DoCreateAllottedResource" + family + "Rollback";
        assertProductionResource(process, "subprocess/" + process + ".bpmn", "so-bpmn-infrastructure-flows");
        RollbackData rollback = new RollbackData();
        String type = "DCAR" + family + "_";
        rollback.put(type, "serviceInstanceId", "service-1");
        rollback.put(type, "parentServiceInstanceId", "parent-1");
        rollback.put(type, "aaiARPath", RESOURCE_PATH);
        rollback.put(type, "rollbackAAI", "true");
        rollback.put(type, "rollbackSDNCactivate", "true");
        rollback.put(type, "rollbackSDNCcreate", "true");
        rollback.put(type, "rollbackSDNCassign", "true");
        String id = UUID.randomUUID().toString();
        rollback.put(type, "sdncActivateRollbackReq", request(id + "-deactivate", "deactivate"));
        rollback.put(type, "sdncCreateRollbackReq", request(id + "-delete", "delete"));
        rollback.put(type, "sdncAssignRollbackReq", request(id + "-unassign", "unassign"));
        rollback.put("SERVICEINSTANCE", "allottedResourceId", "resource-1");
        variables.clear();
        variables.put("rollbackData", rollback);
        variables.put("disableRollback", "false");
        variables.put("msoRequestId", "request-1");
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(process, id, variables);
        startedProcesses.add(instance.getId());
        return instance;
    }

    private String request(String id, String action) {
        return "<w:SDNCAdapterWorkflowRequest xmlns:w=\"http://org.onap/so/workflow/schema/v1\" "
                + "xmlns:a=\"http://org.onap/workflow/sdnc/adapter/schema/v1\"><a:RequestHeader>" + "<a:RequestId>" + id
                + "</a:RequestId><a:SvcInstanceId>service-1</a:SvcInstanceId>" + "<a:SvcAction>" + action
                + "</a:SvcAction><a:SvcOperation>vnf-topology-operation</a:SvcOperation>"
                + "<a:CallbackUrl>http://localhost/callback</a:CallbackUrl><a:MsoAction>generic-resource</a:MsoAction>"
                + "</a:RequestHeader><w:SDNCRequestData><request-information><request-action>" + action
                + "</request-action></request-information></w:SDNCRequestData></w:SDNCAdapterWorkflowRequest>";
    }

    private String callback(String id, int code) {
        return "<SDNCAdapterCallbackRequest><CallbackHeader><RequestId>" + id + "</RequestId>" + "<ResponseCode>" + code
                + "</ResponseCode><ResponseMessage>callback result</ResponseMessage>"
                + "</CallbackHeader><RequestData><output><response-code>" + code + "</response-code>"
                + "<ack-final-indicator>Y</ack-final-indicator></output></RequestData></SDNCAdapterCallbackRequest>";
    }

    private void rejectCallback(String correlator) {
        try {
            runtimeService.createMessageCorrelation("sdncAdapterCallbackRequest")
                    .processInstanceVariableEquals("SDNCA_requestId", correlator)
                    .setVariable("sdncAdapterCallbackRequest", callback(correlator, 200)).correlate();
            fail("An unknown or completed SDNC correlator must not match");
        } catch (MismatchingMessageCorrelationException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    private Object historic(ProcessInstance process, String name) {
        HistoricVariableInstance value = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(process.getId()).variableName(name).singleResult();
        return value == null ? null : value.getValue();
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
