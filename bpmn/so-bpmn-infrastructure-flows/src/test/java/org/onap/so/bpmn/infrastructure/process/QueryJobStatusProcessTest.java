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
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.connect.plugin.impl.ConnectProcessEnginePlugin;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

public class QueryJobStatusProcessTest {
    private static final String JOB_PATH = "/nssmf/api/rest/provMns/v1/NSS/jobs/job-1";
    private static ProcessEngine engine;
    private static RuntimeService runtimeService;
    private static HistoryService historyService;
    private static ManagementService managementService;
    private final List<String> instances = new ArrayList<>();

    @Rule
    public WireMockRule wireMockServer = new WireMockRule(0);

    @BeforeClass
    public static void deployProductionProcess() throws Exception {
        String name = "query-job-" + UUID.randomUUID();
        ProcessEngineConfigurationImpl configuration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
                .createStandaloneInMemProcessEngineConfiguration();
        configuration.setProcessEngineName(name);
        configuration.setJdbcUrl("jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP);
        configuration.setHistory(ProcessEngineConfiguration.HISTORY_FULL);
        configuration.setEnforceHistoryTimeToLive(false);
        configuration.setJobExecutorActivate(false);
        configuration.setProcessEnginePlugins(Collections.singletonList(new ConnectProcessEnginePlugin()));
        engine = configuration.buildProcessEngine();
        runtimeService = engine.getRuntimeService();
        historyService = engine.getHistoryService();
        managementService = engine.getManagementService();
        try (InputStream production = Files.newInputStream(
                Paths.get(System.getProperty("basedir"), "src/main/resources/subprocess/QueryJobStatus.bpmn"))) {
            engine.getRepositoryService().createDeployment().addInputStream("QueryJobStatus.bpmn", production).deploy();
        }
    }

    @AfterClass
    public static void closeEngine() {
        if (engine != null) {
            engine.close();
        }
    }

    @After
    public void removeOwnProcessInstances() {
        for (String id : instances) {
            if (runtimeService.createProcessInstanceQuery().processInstanceId(id).count() != 0) {
                runtimeService.deleteProcessInstance(id, "Test cleanup");
            }
            historyService.deleteHistoricProcessInstance(id);
        }
    }

    @Test
    public void finishedJobExecutesAllProductionScriptsAndCompletesWithoutATimer() {
        respondWith("finished");

        ProcessInstance process = start();

        assertCompleted(process, "finished");
        assertActivityCount(process, "Activity_0cd23fy", 1);
        assertActivityCount(process, "Activity_1aivult", 1);
        assertActivityCount(process, "Activity_16hyg49", 1);
        assertActivityCount(process, "Activity_0rtwx97", 1);
        assertActivityCount(process, "Event_0dp6trf", 0);
        assertRequests(1);
    }

    @Test
    public void failedJobIsATerminalDescriptorRatherThanAnotherPoll() {
        respondWith("failed");

        ProcessInstance process = start();

        assertCompleted(process, "failed");
        assertActivityCount(process, "Activity_0rtwx97", 1);
        assertActivityCount(process, "Event_0dp6trf", 0);
        assertRequests(1);
    }

    @Test
    public void processingJobWaitsForItsTimerBeforeQueryingAgain() {
        respondWith("processing");
        ProcessInstance process = start();
        Job timer = waitingTimer(process);
        assertEquals("FALSE", runtimeService.getVariable(process.getId(), "JobStatusCompleted"));
        assertActivityCount(process, "Activity_0rtwx97", 0);
        assertRequests(1);
        respondWith("finished");

        managementService.executeJob(timer.getId());

        assertCompleted(process, "finished");
        assertActivityCount(process, "Activity_0cd23fy", 1);
        assertActivityCount(process, "Activity_1aivult", 2);
        assertActivityCount(process, "Activity_16hyg49", 2);
        assertActivityCount(process, "Event_0dp6trf", 1);
        assertRequests(2);
    }

    @Test
    public void timedOutProcessingJobUsesTheProductionTimeoutDescriptorAndCompletionRoute() {
        respondWith("processing");
        ProcessInstance process = start();
        Job timer = waitingTimer(process);
        runtimeService.setVariable(process.getId(), "startTime", 0L);
        runtimeService.setVariable(process.getId(), "timeout", 1L);

        managementService.executeJob(timer.getId());

        JSONObject descriptor = assertCompleted(process, "failed");
        assertEquals("timeout", descriptor.getString("statusDescription"));
        assertActivityCount(process, "Activity_0rtwx97", 1);
        assertRequests(2);
    }

    @Test
    public void unknownNonterminalStatusTakesTheTimerRouteAndCanLaterFail() {
        respondWith("queued");
        ProcessInstance process = start();
        Job timer = waitingTimer(process);
        assertEquals("FALSE", runtimeService.getVariable(process.getId(), "JobStatusCompleted"));
        respondWith("failed");

        managementService.executeJob(timer.getId());

        assertCompleted(process, "failed");
        assertActivityCount(process, "Event_0dp6trf", 1);
        assertRequests(2);
    }

    private ProcessInstance start() {
        Map<String, Object> input = new HashMap<>();
        input.put("msoRequestId", "request-1");
        input.put("responseId", "response-1");
        input.put("jobId", "job-1");
        input.put("esrInfo", "{\"name\":\"München\",\"enabled\":false}");
        input.put("serviceInfo", "{\"count\":0,\"items\":[1,2]}");
        input.put("mso.adapters.nssmf.endpoint", "http://localhost:" + wireMockServer.port() + "/nssmf");
        ProcessInstance process = runtimeService.startProcessInstanceByKey("QueryJobStatus", input);
        instances.add(process.getId());
        return process;
    }

    private void respondWith(String status) {
        wireMockServer.stubFor(post(urlEqualTo(JOB_PATH))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json; charset=UTF-8")
                        .withBody("{\"responseDescriptor\":{\"status\":\"" + status
                                + "\",\"statusDescription\":\"job-result\",\"progress\":0,\"enabled\":false}}")));
    }

    private Job waitingTimer(ProcessInstance process) {
        assertNotNull(runtimeService.createProcessInstanceQuery().processInstanceId(process.getId()).singleResult());
        assertEquals(1, runtimeService.createExecutionQuery().processInstanceId(process.getId())
                .activityId("Event_0dp6trf").count());
        Job timer = managementService.createJobQuery().processInstanceId(process.getId()).timers().singleResult();
        assertNotNull(timer);
        assertNotNull(timer.getDuedate());
        assertEquals(0, managementService.createJobQuery().processInstanceId(process.getId()).withException().count());
        return timer;
    }

    private JSONObject assertCompleted(ProcessInstance process, String status) {
        assertNull(runtimeService.createProcessInstanceQuery().processInstanceId(process.getId()).singleResult());
        assertEquals(0, managementService.createJobQuery().processInstanceId(process.getId()).count());
        assertNotNull(historyService.createHistoricProcessInstanceQuery().processInstanceId(process.getId()).finished()
                .singleResult());
        assertEquals("TRUE", historicVariable(process, "JobStatusCompleted"));
        Object stored = historicVariable(process, "responseDescriptor");
        assertTrue(stored instanceof String);
        JSONObject descriptor = new JSONObject((String) stored);
        assertEquals(status, descriptor.getString("status"));
        if (!"timeout".equals(descriptor.getString("statusDescription"))) {
            assertEquals("job-result", descriptor.getString("statusDescription"));
            assertEquals(Integer.valueOf(0), descriptor.get("progress"));
            assertEquals(Boolean.FALSE, descriptor.get("enabled"));
        }
        assertActivityCount(process, "Event_0p00z2s", 1);
        return descriptor;
    }

    private Object historicVariable(ProcessInstance process, String name) {
        HistoricVariableInstance variable = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(process.getId()).variableName(name).singleResult();
        assertNotNull(name, variable);
        return variable.getValue();
    }

    private void assertActivityCount(ProcessInstance process, String activity, long expected) {
        assertEquals(activity, expected, historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(process.getId()).activityId(activity).finished().count());
    }

    private void assertRequests(int expected) {
        List<LoggedRequest> requests = wireMockServer.findAll(postRequestedFor(urlEqualTo(JOB_PATH)));
        assertEquals(expected, requests.size());
        for (LoggedRequest request : requests) {
            assertEquals("application/json", request.getHeader("content-type"));
            assertTrue(request.getHeader("Authorization").startsWith("Basic "));
            JSONObject body = new JSONObject(request.getBodyAsString());
            assertEquals(3, body.length());
            assertEquals("response-1", body.getString("responseId"));
            assertEquals("München", body.getJSONObject("esrInfo").getString("name"));
            assertEquals(Boolean.FALSE, body.getJSONObject("esrInfo").get("enabled"));
            assertEquals(Integer.valueOf(0), body.getJSONObject("serviceInfo").get("count"));
            assertEquals(2, body.getJSONObject("serviceInfo").getJSONArray("items").length());
            assertEquals(1, body.getJSONObject("serviceInfo").getJSONArray("items").getInt(0));
            assertEquals(2, body.getJSONObject("serviceInfo").getJSONArray("items").getInt(1));
        }
    }
}
