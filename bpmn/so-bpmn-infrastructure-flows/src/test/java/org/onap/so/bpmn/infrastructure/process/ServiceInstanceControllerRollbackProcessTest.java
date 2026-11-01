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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Assume;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.bpmn.common.verification.WorkflowPersistenceSupport;
import org.onap.so.bpmn.core.RollbackData;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;
import org.w3c.dom.Document;

public class ServiceInstanceControllerRollbackProcessTest extends WorkflowPersistenceSupport {
    private static final String PROCESS = "DoCreateServiceInstanceRollback";
    private static final String SERVICE = "/business/customers/customer/customer-1/service-subscriptions/"
            + "service-subscription/subscription/service-instances/service-instance/service-1";
    private final Map<String, Object> variables = new HashMap<>();
    private final List<String> effects = Collections.synchronizedList(new ArrayList<>());
    private WireMockServer adapter;
    private MockedConstruction<AAIResourcesClient> inventory;
    private Environment previousEnvironment;

    @Before
    public void deployTheRealLegacyAdapterAndConfigureExternalBoundaries() throws Exception {
        previousEnvironment = (Environment) ReflectionTestUtils.getField(UrnPropertiesReader.class, "environment");
        deploy(PROCESS, "so-bpmn-infrastructure-flows");
        deploy("SDNCAdapterV1", "MSOCommonBPMN");
        adapter = new WireMockServer(0);
        adapter.addMockServiceRequestListener((request, response) -> {
            try {
                effects.add(text(request.getBodyAsString(), "SvcAction"));
            } catch (Exception error) {
                throw new IllegalStateException(error);
            }
        });
        adapter.start();
        adapter.stubFor(post(urlEqualTo("/sdnc"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/soap+xml").withBody(
                        "<s:Envelope xmlns:s='http://schemas.xmlsoap.org/soap/envelope/'><s:Body/></s:Envelope>")));
        new UrnPropertiesReader().setEnvironment(new MockEnvironment()
                .withProperty("mso.adapters.sdnc.endpoint", "http://localhost:" + adapter.port() + "/sdnc")
                .withProperty("mso.workflow.sdncadapter.callback", "http://callback.invalid/SDNCAdapterCallback")
                .withProperty("mso.adapters.sdnc.timeout", "PT1M").withProperty("so.auth.username", "contract")
                .withProperty("so.auth.password", "contract"));
        inventory = mockConstruction(AAIResourcesClient.class, (client, context) -> {
            when(client.get(any(AAIResourceUri.class), any())).thenReturn(
                    new AAIResultWrapper("{\"results\":[{\"resource-type\":\"service-instance\",\"resource-link\":"
                            + "\"http://inventory.example/aai/v24" + SERVICE + "\"}]}"));
            doAnswer(call -> {
                AAIResourceUri uri = call.getArgument(0);
                effects.add("AAI:" + uri.build().getPath());
                return null;
            }).when(client).delete(any(AAIResourceUri.class));
        });
        variables.put("disableRollback", "false");
        variables.put("msoRequestId", "request-1");
        cleanup(true);
    }

    @After
    public void restoreExternalBoundaries() {
        try {
            if (inventory != null) {
                inventory.close();
            }
        } finally {
            new UrnPropertiesReader().setEnvironment(previousEnvironment);
            if (adapter != null) {
                adapter.stop();
            }
        }
    }

    @Test
    public void controllerOnlyCompensationResumesEachWaitWithoutRepeatingRequests() throws Exception {
        cleanup(false);
        ProcessInstance root = start();
        ProcessInstance deactivate = waitingChild();
        assertPosts("deactivate");
        restart();
        assertNotNull(engine.getRuntimeService().createEventSubscriptionQuery().processInstanceId(deactivate.getId())
                .eventName("sdncAdapterCallbackRequest").singleResult());
        callback(deactivate, "200");
        ProcessInstance delete = waitingChild();
        assertNotEquals(deactivate.getId(), delete.getId());
        rejectDuplicate(deactivate);
        assertPosts("deactivate", "delete");
        restart();
        callback(delete, "200");
        completed(root);
        assertEquals(Arrays.asList("deactivate", "delete"), effects);
        assertEquals(Boolean.TRUE, historic(root, "rolledBack"));
        assertNull(historic(root, "rollbackData"));
        assertNull(historic(root, "rollbackError"));
        activity(root, "Task_06o1aio", 0);
    }

    @Test
    public void controllerCleanupPrecedesInventoryDeletionExactlyOnce() throws Exception {
        ProcessInstance root = start();
        callback(waitingChild(), "200");
        callback(waitingChild(), "200");
        completed(root);
        assertEquals(Arrays.asList("deactivate", "delete", "AAI:" + SERVICE), effects);
        assertEquals(Boolean.TRUE, historic(root, "rolledBack"));
        activity(root, "Task_06o1aio", 1);
        assertPosts("deactivate", "delete");
    }

    @Test
    public void controllerHttpRejectionStopsBeforeDeleteAndInventoryCleanup() throws Exception {
        adapter.stubFor(post(urlEqualTo("/sdnc")).atPriority(1).willReturn(aResponse().withStatus(503)));
        ProcessInstance root = start();
        failed(root, "deactivate");
        activity(root, "ScriptTask_1uxr0cx", 1);
        activity(root, "CallActivity_0ppz9nd", 0);
        assertPosts("deactivate");
    }

    @Test
    public void failedDeactivateCallbackStopsBeforeDeleteAndInventoryCleanup() throws Exception {
        ProcessInstance root = start();
        callback(waitingChild(), "500");
        failed(root, "deactivate");
        activity(root, "CallActivity_0ppz9nd", 0);
        assertPosts("deactivate");
    }

    @Test
    public void failedDeleteCallbackCannotPublishSuccessfulCompensation() throws Exception {
        Assume.assumeTrue("SO-4457: service rollback ignores the failed SDNC delete callback",
                Boolean.getBoolean("groovy.migration.verification"));
        ProcessInstance root = start();
        callback(waitingChild(), "200");
        callback(waitingChild(), "500");
        completed(root);
        assertEquals(Boolean.FALSE, historic(root, "rolledBack"));
        assertEquals(Arrays.asList("deactivate", "delete"), effects);
        activity(root, "Task_06o1aio", 0);
        activity(root, "ScriptTask_1", 1);
        activity(root, "ScriptTask_1awt1ds", 1);
        assertPosts("deactivate", "delete");
    }

    @Test
    public void disabledRollbackSuppressesBothControllerAndInventoryActions() throws Exception {
        variables.put("disableRollback", "true");
        ProcessInstance root = start();
        completed(root);
        assertTrue(effects.isEmpty());
        assertEquals(0, adapter.getAllServeEvents().size());
        assertEquals(Boolean.FALSE, historic(root, "rolledBack"));
        activity(root, "CallActivity_0ak0ezb", 0);
        activity(root, "CallActivity_0ppz9nd", 0);
        activity(root, "Task_06o1aio", 0);
    }

    private void deploy(String resource, String module) throws Exception {
        String path = "subprocess/" + resource + ".bpmn";
        URL url = getClass().getClassLoader().getResource(path);
        assertNotNull(path, url);
        String location = url.toExternalForm();
        assertTrue(location,
                location.contains("/" + module + "/target/classes/")
                        || (location.matches("jar:.*/" + module + "-[^/]+\\.jar!/" + path)
                                && !location.contains("-tests.jar!/")));
        try (InputStream input = url.openStream()) {
            var deployment = engine.getRepositoryService().createDeployment().addInputStream(path, input).deploy();
            try (InputStream original = url.openStream();
                    InputStream stored = engine.getRepositoryService().getResourceAsStream(deployment.getId(), path)) {
                assertArrayEquals(original.readAllBytes(), stored.readAllBytes());
            }
        }
    }

    private void cleanup(boolean aai) {
        RollbackData rollback = new RollbackData();
        rollback.put("SERVICEINSTANCE", "serviceInstanceId", "service-1");
        rollback.put("SERVICEINSTANCE", "subscriptionServiceType", "subscription");
        rollback.put("SERVICEINSTANCE", "globalSubscriberId", "customer-1");
        rollback.put("SERVICEINSTANCE", "rollbackAAI", String.valueOf(aai));
        rollback.put("SERVICEINSTANCE", "rollbackSDNC", "true");
        rollback.put("SERVICEINSTANCE", "sdncDeactivate", request("deactivate"));
        rollback.put("SERVICEINSTANCE", "sdncDelete", request("delete"));
        variables.put("rollbackData", rollback);
    }

    private String request(String action) {
        return "<w:SDNCAdapterWorkflowRequest xmlns:w='http://org.onap/so/workflow/schema/v1'"
                + " xmlns:a='http://org.onap/workflow/sdnc/adapter/schema/v1'><a:RequestHeader>"
                + "<a:RequestId>rollback-" + action + "</a:RequestId><a:SvcInstanceId>service-1</a:SvcInstanceId>"
                + "<a:SvcAction>" + action + "</a:SvcAction><a:SvcOperation>service-topology-operation</a:SvcOperation>"
                + "<a:CallbackUrl>http://original.invalid/callback</a:CallbackUrl></a:RequestHeader>"
                + "<w:SDNCRequestData><service-information><service-instance-id>service-1</service-instance-id>"
                + "</service-information></w:SDNCRequestData></w:SDNCAdapterWorkflowRequest>";
    }

    private ProcessInstance start() {
        return engine.getRuntimeService().startProcessInstanceByKey(PROCESS, variables);
    }

    private ProcessInstance waitingChild() {
        ProcessInstance child = engine.getRuntimeService().createProcessInstanceQuery()
                .processDefinitionKey("sdncAdapter").singleResult();
        assertNotNull("A real SDNC child must wait for its callback", child);
        assertEquals(1, engine.getRuntimeService().createEventSubscriptionQuery().processInstanceId(child.getId())
                .eventName("sdncAdapterCallbackRequest").count());
        return child;
    }

    private void callback(ProcessInstance child, String code) {
        String id = String.valueOf(engine.getRuntimeService().getVariable(child.getId(), "SDNCA_requestId"));
        String body = "<a:SDNCAdapterCallbackRequest xmlns:a='http://org.onap/workflow/sdnc/adapter/schema/v1'>"
                + "<a:CallbackHeader><a:RequestId>" + id + "</a:RequestId><a:ResponseCode>" + code
                + "</a:ResponseCode><a:ResponseMessage>controller result</a:ResponseMessage></a:CallbackHeader>"
                + "<a:RequestData><ack-final-indicator>Y</ack-final-indicator><response-code>" + code
                + "</response-code></a:RequestData></a:SDNCAdapterCallbackRequest>";
        engine.getRuntimeService().createMessageCorrelation("sdncAdapterCallbackRequest")
                .processInstanceId(child.getId()).setVariable("sdncAdapterCallbackRequest", body).correlate();
        var job = engine.getManagementService().createJobQuery().processInstanceId(child.getId()).executable()
                .singleResult();
        assertNotNull("Callback must reach the shipped async continuation", job);
        engine.getManagementService().executeJob(job.getId());
    }

    private void rejectDuplicate(ProcessInstance child) {
        assertThrows(MismatchingMessageCorrelationException.class,
                () -> engine.getRuntimeService().createMessageCorrelation("sdncAdapterCallbackRequest")
                        .processInstanceId(child.getId()).setVariable("sdncAdapterCallbackRequest", "duplicate")
                        .correlate());
    }

    private void failed(ProcessInstance root, String... attempted) {
        completed(root);
        assertEquals(Arrays.asList(attempted), effects);
        assertFalse(Boolean.TRUE.equals(historic(root, "rolledBack")));
        assertNotNull(historic(root, "rollbackError"));
        assertNull(historic(root, "rollbackData"));
        activity(root, "Task_06o1aio", 0);
        activity(root, "PostProcess_ScriptTask", 0);
        activity(root, "ScriptTask_1", 1);
    }

    private void assertPosts(String... actions) throws Exception {
        assertEquals(actions.length, adapter.getAllServeEvents().size());
        for (String action : actions) {
            adapter.verify(1,
                    postRequestedFor(urlEqualTo("/sdnc")).withHeader("Content-Type", containing("application/soap+xml"))
                            .withHeader("Authorization", equalTo("Basic Y29udHJhY3Q6Y29udHJhY3Q="))
                            .withHeader("X-ONAP-RequestID", equalTo("request-1")).withRequestBody(
                                    matchingXPath("//*[local-name()='SvcAction' and text()='" + action + "']")));
        }
        for (var event : adapter.getAllServeEvents()) {
            String body = event.getRequest().getBodyAsString();
            assertEquals("rollback-" + text(body, "SvcAction"), text(body, "RequestId"));
            assertEquals("service-1", text(body, "SvcInstanceId"));
            assertEquals("service-topology-operation", text(body, "SvcOperation"));
            assertEquals("http://callback.invalid/SDNCAdapterCallback", text(body, "CallbackUrl"));
        }
    }

    private static String text(String xml, String name) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        Document document =
                factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        return document.getElementsByTagNameNS("*", name).item(0).getTextContent();
    }

    private void completed(ProcessInstance process) {
        restart();
        assertEquals(0,
                engine.getRuntimeService().createProcessInstanceQuery().processInstanceId(process.getId()).count());
        assertEquals(1, engine.getHistoryService().createHistoricProcessInstanceQuery()
                .processInstanceId(process.getId()).finished().count());
    }

    private Object historic(ProcessInstance process, String name) {
        var value = engine.getHistoryService().createHistoricVariableInstanceQuery().processInstanceId(process.getId())
                .variableName(name).singleResult();
        return value == null ? null : value.getValue();
    }

    private void activity(ProcessInstance process, String activity, long count) {
        assertEquals(activity, count, engine.getHistoryService().createHistoricActivityInstanceQuery()
                .processInstanceId(process.getId()).activityId(activity).count());
    }
}
