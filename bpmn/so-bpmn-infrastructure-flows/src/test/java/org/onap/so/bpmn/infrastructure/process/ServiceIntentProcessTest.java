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
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Assume;
import org.junit.Test;
import org.onap.so.BaseBPMNTest;
import org.onap.aaiclient.client.aai.AAIVersion;
import org.onap.so.bpmn.common.workflow.context.WorkflowContext;
import org.onap.so.bpmn.common.workflow.context.WorkflowContextHolder;
import org.onap.so.bpmn.core.WorkflowException;
import org.w3c.dom.Document;

public class ServiceIntentProcessTest extends BaseBPMNTest {
    private static final String STATUS_PATH = "/intent-status";
    private static final String[] PROCESSES =
            {"CreateServiceIntentInstance", "ModifyServiceIntentInstance", "DeleteServiceIntentInstance"};
    private final List<String> instances = new ArrayList<>();
    private String requestId;
    private String serviceId;
    private String resourcePath;
    private String nodesPath;

    @Before
    public void prepareContracts() throws Exception {
        for (String process : PROCESSES) {
            assertProductionResource(process);
        }
        assertProductionResource("DoCloudLeasedLineDelete");
        wireMockServer.resetAll();
        variables.clear();
        Map<String, Object> properties = new HashMap<>();
        properties.put("mso.adapters.openecomp.db.endpoint", "http://localhost:" + wireMockPort + STATUS_PATH);
        properties.put("mso.workflow.sdncadapter.callback", "http://localhost:" + wireMockPort + "/intent-callback");
        setWorkflowProperties(properties);
        requestId = "request-" + UUID.randomUUID();
        serviceId = "service-" + UUID.randomUUID();
        resourcePath = "/aai/v[0-9]+/business/customers/customer/customer-1/service-subscriptions/"
                + "service-subscription/5G/service-instances/service-instance/" + serviceId;
        nodesPath = "/aai/v[0-9]+/nodes/service-instances/service-instance/" + serviceId;
        variables.put("mso-request-id", requestId);
        variables.put("isAsyncProcess", Boolean.TRUE);
        variables.put("networkType", "OTHER");
        variables.put("mso.adapters.openecomp.db.endpoint", "http://localhost:" + wireMockPort + STATUS_PATH);
        wireMockServer.stubFor(
                post(urlEqualTo(STATUS_PATH)).willReturn(aResponse().withStatus(200).withHeader("Connection", "close")
                        .withHeader("Content-Type", "application/soap+xml").withBody("<accepted/>")));
        JSONObject node = new JSONObject().put("resource-type", "service-instance").put("resource-link",
                "http://localhost:" + wireMockPort + "/aai/" + AAIVersion.LATEST
                        + "/business/customers/customer/customer-1/"
                        + "service-subscriptions/service-subscription/5G/service-instances/service-instance/"
                        + serviceId);
        wireMockServer.stubFor(get(urlPathMatching(nodesPath))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(new JSONObject().put("results", new JSONArray().put(node)).toString())));
        wireMockServer.stubFor(get(urlPathMatching(resourcePath))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(new JSONObject().put("service-instance-id", serviceId)
                                .put("model-version-id", "model-2").put("resource-version", "version-1").toString())));
    }

    @After
    public void removeOwnedState() {
        WorkflowContextHolder holder = WorkflowContextHolder.getInstance();
        WorkflowContext context;
        while ((context = holder.getWorkflowContext(requestId)) != null) {
            holder.remove(context);
        }
        for (String instance : instances) {
            if (runtimeService.createProcessInstanceQuery().processInstanceId(instance).count() != 0) {
                runtimeService.deleteProcessInstance(instance, "Test cleanup");
            }
        }
    }

    @Test
    public void creationInitializesStatusAndReturnsTheJobWithoutCallingTheCllSubprocess() throws Exception {
        ProcessInstance process = start(PROCESSES[0], request());
        assertAccepted(process, "Create");
        assertActivity(process, "Activity_1ydx2rx", 1);
        assertActivity(process, "Activity_0qlstj2", 1);
        assertActivity(process, "Activity_1usi32q", 1);
        assertActivity(process, "Event_18u424w", 1);
        assertActivity(process, "CallDoCloudLeasedLineCreate", 0);
        assertEquals(1, wireMockServer.getAllServeEvents().size());
    }

    @Test
    public void modificationUsesTheTnDispatchRouteAfterReadingTheServiceModel() throws Exception {
        variables.put("networkType", "TN");
        ProcessInstance process = start(PROCESSES[1], request());
        assertAccepted(process, "Modify");
        assertActivity(process, "Activity_1s8ojcr", 1);
        assertActivity(process, "Activity_0ptby3d", 1);
        assertActivity(process, "Activity_1dzkfh2", 1);
        assertActivity(process, "Event_103vvkf", 1);
        assertActivity(process, "Activity_0t1g4mp", 0);
        assertInventoryRead();
    }

    @Test
    public void deletionOfAnotherServiceTypeReturnsTheJobWithoutCallingTheCllSubprocess() throws Exception {
        ProcessInstance process = start(PROCESSES[2], request());
        assertAccepted(process, "Delete");
        assertActivity(process, "Activity_1fj0cge", 1);
        assertActivity(process, "Activity_1dykbz9", 1);
        assertActivity(process, "Activity_0c6o2o7", 1);
        assertActivity(process, "Event_1ecylff", 1);
        assertActivity(process, "Activity_0li16m1", 0);
        assertInventoryRead();
    }

    @Test
    public void missingSubscriberStopsEachWorkflowBeforeInventoryStatusOrAcceptedResponse() {
        for (String workflow : PROCESSES) {
            JSONObject input = request();
            input.remove("globalSubscriberId");
            ProcessInstance process = start(workflow, input);
            assertValidationFailure(process, "globalSubscriberId");
        }
        assertTrue(wireMockServer.getAllServeEvents().isEmpty());
    }

    @Test
    public void missingCreationModelStopsBeforeStatusInitializationAndAcceptedResponse() {
        JSONObject input = request();
        input.remove("modelUuid");
        ProcessInstance process = start(PROCESSES[0], input);
        assertValidationFailure(process, "modelUuid");
        assertActivity(process, "Activity_1ydx2rx", 0);
        assertTrue(wireMockServer.getAllServeEvents().isEmpty());
    }

    @Test
    public void missingServiceStopsModifyAndDeleteBeforeAnyExternalAction() {
        for (String workflow : new String[] {PROCESSES[1], PROCESSES[2]}) {
            JSONObject input = request();
            input.remove("serviceInstanceID");
            ProcessInstance process = start(workflow, input);
            assertValidationFailure(process, "serviceInstanceID");
        }
        assertTrue(wireMockServer.getAllServeEvents().isEmpty());
    }

    @Test
    public void cllDeletionFailureReturnsFromTheRealChildWithoutACompletionStatusWrite() {
        wireMockServer.stubFor(delete(urlPathMatching(resourcePath)).willReturn(aResponse().withStatus(503)));
        ProcessInstance process = start(PROCESSES[2], cllRequest());
        assertChildFailure(process);
        wireMockServer.verify(1, deleteRequestedFor(urlPathMatching(resourcePath)));
    }

    @Test
    public void missingCllInventoryFailsBeforeDeletionAndDoesNotLoseTheAcceptedJobIdentifier() {
        wireMockServer.stubFor(get(urlPathMatching(resourcePath)).willReturn(aResponse().withStatus(404)));
        ProcessInstance process = start(PROCESSES[2], cllRequest());
        assertChildFailure(process);
        wireMockServer.verify(0, deleteRequestedFor(urlPathMatching(resourcePath)));
    }

    @Test
    public void cllDeletionRetainsTheServiceAndModelForTheFinalStatusUpdate() throws Exception {
        Assume.assumeTrue("SO-4446: the service-intent CLL call must retain final status identity",
                Boolean.getBoolean("groovy.migration.verification"));
        wireMockServer.stubFor(delete(urlPathMatching(resourcePath)).willReturn(aResponse().withStatus(204)));
        ProcessInstance process = start(PROCESSES[2], cllRequest());
        wireMockServer.verify(1, deleteRequestedFor(urlPathMatching(resourcePath)));
        assertNull("CLL deletion must complete its status handoff without a workflow error",
                historic(process, "WorkflowException"));
        List<com.github.tomakehurst.wiremock.verification.LoggedRequest> writes =
                wireMockServer.findAll(postRequestedFor(urlEqualTo(STATUS_PATH)));
        assertEquals("Initialize and complete the same job", 2, writes.size());
        String completion = writes.stream().map(value -> value.getBodyAsString())
                .filter(value -> value.contains("updateResourceOperationStatus")).findFirst().orElse(null);
        assertNotNull("A successful deletion must write its final status", completion);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        Document document = factory.newDocumentBuilder()
                .parse(new ByteArrayInputStream(completion.getBytes(StandardCharsets.UTF_8)));
        assertEquals("Final status must retain service and model identity", Arrays.asList(serviceId, "model-2"),
                Arrays.asList(text(document, "serviceId"), text(document, "resourceTemplateUUID")));
        assertEquals(historic(process, "jobId"), text(document, "operationId"));
        assertEquals(serviceId, text(document, "resourceInstanceID"));
        assertEquals("FINISHED", text(document, "status"));
        assertEquals("100", text(document, "progress"));
    }

    private JSONObject cllRequest() {
        return request().put("serviceType", "CLL").put("additionalProperties",
                new JSONObject().put("serviceInstanceID", serviceId).put("enableSdnc", false));
    }

    private void assertChildFailure(ProcessInstance process) {
        assertTrue(historic(process, "WorkflowException") instanceof WorkflowException);
        assertActivity(process, "Activity_0li16m1", 1);
        assertEquals(1, historyService.createHistoricProcessInstanceQuery().superProcessInstanceId(process.getId())
                .processDefinitionKey("DoCloudLeasedLineDelete").finished().count());
        String job = (String) historic(process, "jobId");
        assertNotNull(UUID.fromString(job));
        WorkflowContext context = WorkflowContextHolder.getInstance().getWorkflowContext(requestId);
        assertNotNull(context);
        assertEquals(202, context.getWorkflowResponse().getMessageCode());
        assertEquals(job, new JSONObject(context.getWorkflowResponse().getResponse()).getString("jobId"));
        wireMockServer.verify(1, postRequestedFor(urlEqualTo(STATUS_PATH)));
        assertTrue(wireMockServer.findAll(postRequestedFor(urlEqualTo(STATUS_PATH))).get(0).getBodyAsString()
                .contains("initResourceOperationStatus"));
    }

    private JSONObject request() {
        return new JSONObject().put("globalSubscriberId", "customer-1").put("serviceType", "intent")
                .put("subscriptionServiceType", "5G").put("modelInvariantUuid", "invariant-1")
                .put("modelUuid", "model-2").put("serviceInstanceID", serviceId)
                .put("additionalProperties", new JSONObject().put("serviceInstanceID", serviceId));
    }

    private ProcessInstance start(String workflow, JSONObject input) {
        variables.put("bpmnRequest", input.toString());
        ProcessInstance process =
                runtimeService.startProcessInstanceByKey(workflow, UUID.randomUUID().toString(), variables);
        instances.add(process.getId());
        assertEquals(0L, runtimeService.createProcessInstanceQuery().processInstanceId(process.getId()).count());
        return process;
    }

    private void assertAccepted(ProcessInstance process, String operation) throws Exception {
        String workflow = repositoryService.getProcessDefinition(process.getProcessDefinitionId()).getKey();
        assertNull(historic(process, "WorkflowException"));
        assertEquals(Boolean.TRUE, historic(process, "sentSyncResponse"));
        assertEquals("true", historic(process, workflow + "WorkflowResponseSent"));
        assertEquals("202", historic(process, workflow + "ResponseCode"));
        String job = (String) historic(process, "jobId");
        assertNotNull(UUID.fromString(job));
        WorkflowContext context = WorkflowContextHolder.getInstance().getWorkflowContext(requestId);
        assertNotNull(context);
        assertEquals(workflow, context.getProcessKey());
        assertEquals(process.getId(), context.getWorkflowResponse().getProcessInstanceID());
        assertEquals(202, context.getWorkflowResponse().getMessageCode());
        JSONObject response = new JSONObject(context.getWorkflowResponse().getResponse());
        assertEquals(job, response.getString("jobId"));
        assertEquals("processing", response.getString("status"));
        assertEquals(2, response.length());
        wireMockServer.verify(1, postRequestedFor(urlEqualTo(STATUS_PATH)).withHeader("Content-Type",
                containing("application/soap+xml")));
        String body = wireMockServer.findAll(postRequestedFor(urlEqualTo(STATUS_PATH))).get(0).getBodyAsString();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        Document document =
                factory.newDocumentBuilder().parse(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
        assertEquals(1, document.getElementsByTagNameNS("http://org.onap.so/requestsdb", "initResourceOperationStatus")
                .getLength());
        assertEquals(serviceId, text(document, "serviceId"));
        assertEquals(job, text(document, "operationId"));
        assertEquals(operation, text(document, "operType"));
        assertEquals("model-2", text(document, "resourceTemplateUUIDs"));
    }

    private void assertInventoryRead() {
        wireMockServer.verify(1, getRequestedFor(urlPathMatching(nodesPath)));
        wireMockServer.verify(1, getRequestedFor(urlPathMatching(resourcePath)));
        assertEquals(3, wireMockServer.getAllServeEvents().size());
    }

    private void assertValidationFailure(ProcessInstance process, String field) {
        Object value = historic(process, "WorkflowException");
        assertTrue(value instanceof WorkflowException);
        WorkflowException error = (WorkflowException) value;
        assertEquals(500, error.getErrorCode());
        assertTrue(error.getErrorMessage(), error.getErrorMessage().contains(field));
        assertNull(historic(process, "sentSyncResponse"));
        assertNull(WorkflowContextHolder.getInstance().getWorkflowContext(requestId));
    }

    private Object historic(ProcessInstance process, String variable) {
        HistoricVariableInstance value = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(process.getId()).variableName(variable).singleResult();
        return value == null ? null : value.getValue();
    }

    private void assertActivity(ProcessInstance process, String activity, long count) {
        assertEquals(activity, count, historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(process.getId()).activityId(activity).finished().count());
    }

    private String text(Document document, String name) {
        assertEquals(name, 1, document.getElementsByTagNameNS("", name).getLength());
        return document.getElementsByTagNameNS("", name).item(0).getTextContent();
    }

    private void assertProductionResource(String process) throws Exception {
        String directory = process.startsWith("Do") ? "subprocess/" : "process/";
        URL resource = getClass().getClassLoader().getResource(directory + process + ".bpmn");
        assertNotNull(resource);
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(process)
                .latestVersion().singleResult();
        assertNotNull(definition);
        try (InputStream original = resource.openStream();
                InputStream deployed = repositoryService.getResourceAsStream(definition.getDeploymentId(),
                        definition.getResourceName())) {
            assertArrayEquals(original.readAllBytes(), deployed.readAllBytes());
        }
    }
}
