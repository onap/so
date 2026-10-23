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
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.onap.aaiclient.client.aai.AAIVersion;
import org.onap.so.BaseBPMNTest;
import org.onap.so.bpmn.core.WorkflowException;
import org.w3c.dom.Document;

public class CloudLeasedLineCreateProcessTest extends BaseBPMNTest {
    private static final String PROCESS = "DoCloudLeasedLineCreate";
    private static final String RESOURCE = "subprocess/DoCloudLeasedLineCreate.bpmn";
    private static final String STATUS_PATH = "/cll-create-status";
    private static final String BUSINESS = "/business/customers/customer/customer-1/service-subscriptions/"
            + "service-subscription/service-type/service-instances/service-instance/";
    private static final String POLICY = "/aai/v[0-9]+/network/network-policies/network-policy/[0-9a-f-]+";
    private static final String LINK = "/aai/v[0-9]+/network/logical-links/logical-link/endpoint-A";
    private String id;
    private String servicePath;
    private String resourcePath;
    private JSONObject network;

    @Before
    public void prepareContracts() throws Exception {
        assertProductionResource();
        wireMockServer.resetAll();
        variables.clear();
        id = "cll-" + UUID.randomUUID();
        servicePath = "/aai/v[0-9]+" + BUSINESS + id;
        resourcePath = servicePath + "/allotted-resources/allotted-resource/network-1";
        variables.put("msoRequestId", "request-1");
        variables.put("servicename", "leased Grüße & 雪");
        variables.put("modelInvariantUuid", "invariant-1");
        variables.put("modelUuid", "model-1");
        variables.put("jobId", "job-1");
        variables.put("subscriptionServiceType", "service-type");
        variables.put("serviceType", "leased-line");
        variables.put("globalSubscriberId", "customer-1");
        variables.put("mso.workflow.sdncadapter.callback", "http://localhost:" + wireMockPort + "/callback");
        variables.put("mso.adapters.openecomp.db.endpoint", "http://localhost:" + wireMockPort + STATUS_PATH);
        JSONObject link = new JSONObject().put("name", "endpoint-A").put("transportEndpointA", "endpoint-A")
                .put("transportEndpointB", "endpoint-B").put("transportEndpointBProtection", "backup-B");
        network = new JSONObject().put("id", "network-1")
                .put("sla", new JSONObject().put("maxBandwidth", 0).put("latency", 0))
                .put("connectionLinks", new JSONArray().put(link));
        inputs();
        stubStatus(200);
        stubPut(servicePath, 201);
        stubPut(resourcePath, 201);
        stubPut(resourcePath + "/relationship-list/relationship", 200);
        stubPut(POLICY, 201);
        stubPut(LINK, 201);
        wireMockServer.stubFor(get(urlPathMatching(resourcePath)).withQueryParam("format", equalTo("count"))
                .withQueryParam("resultIndex", equalTo("0")).withQueryParam("resultSize", equalTo("1"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{\"results\":[{\"resource-type\":\"allotted-resource\",\"total\":1}]}")));
        String lookup = "/aai/v[0-9]+/nodes/service-instances/service-instance/" + id;
        JSONObject target = new JSONObject().put("resource-type", "service-instance").put("resource-link",
                "http://localhost:" + wireMockPort + "/aai/" + AAIVersion.LATEST + BUSINESS + id);
        wireMockServer.stubFor(get(urlPathMatching(lookup)).withQueryParam("format", equalTo("pathed"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(new JSONObject().put("results", new JSONArray().put(target)).toString())));
        wireMockServer.stubFor(post(urlPathMatching(servicePath)).withHeader("X-HTTP-Method-Override", equalTo("PATCH"))
                .willReturn(aResponse().withStatus(200).withHeader("Connection", "close")));
    }

    @Test
    public void disabledSdncCreatesTypedInventoryAndRecordsBothOperationStates() throws Exception {
        ProcessInstance process = start();
        assertSuccess(process);
        assertInventoryWrites();
        JSONObject link = bodyFor(LINK);
        assertEquals("backup-B", link.getString("segment-id"));
        assertStatusPayloads();
    }

    @Test
    public void anUnprotectedLogicalLinkOmitsItsOptionalProtectionIdentifier() throws Exception {
        network.getJSONArray("connectionLinks").getJSONObject(0).remove("transportEndpointBProtection");
        inputs();
        assertSuccess(start());
        assertInventoryWrites();
        assertFalse(bodyFor(LINK).has("segment-id"));
    }

    @Test
    public void rejectedServiceCreationStopsBeforeAllocatingNetworkResources() {
        stubPut(servicePath, 503);
        assertFailure(start());
        wireMockServer.verify(1, putRequestedFor(urlPathMatching(servicePath)));
        wireMockServer.verify(0, putRequestedFor(urlPathMatching(resourcePath)));
        wireMockServer.verify(0, putRequestedFor(urlPathMatching(POLICY)));
        assertOnlyInitialStatus();
    }

    @Test
    public void rejectedNetworkResourceStopsBeforeCreatingPolicyOrLinks() {
        stubPut(resourcePath, 503);
        assertFailure(start());
        wireMockServer.verify(1, putRequestedFor(urlPathMatching(servicePath)));
        wireMockServer.verify(1, putRequestedFor(urlPathMatching(resourcePath)));
        wireMockServer.verify(0, putRequestedFor(urlPathMatching(POLICY)));
        wireMockServer.verify(0, putRequestedFor(urlPathMatching(LINK)));
        assertOnlyInitialStatus();
    }

    @Test
    public void missingSlaStopsBeforeTheAllottedResourceMutation() {
        network.remove("sla");
        inputs();
        assertFailure(start());
        wireMockServer.verify(1, putRequestedFor(urlPathMatching(servicePath)));
        wireMockServer.verify(0, putRequestedFor(urlPathMatching(resourcePath)));
        assertOnlyInitialStatus();
    }

    @Test
    public void missingCallbackStopsBeforeAnyOutboundAction() {
        variables.put("mso.workflow.sdncadapter.callback", "");
        assertFailure(start());
        assertTrue(wireMockServer.getAllServeEvents().isEmpty());
    }

    @Test
    public void initialStatusRejectionStopsBeforeInventoryMutation() {
        Assume.assumeTrue("SO-4456: CLL creation ignores rejected initial request-database status",
                Boolean.getBoolean("groovy.migration.verification"));
        stubStatus(500);
        assertFailure(start());
        wireMockServer.verify(0, putRequestedFor(urlPathMatching(servicePath)));
    }

    @Test
    public void finalStatusRejectionCannotCompleteTheWorkflowSuccessfully() {
        Assume.assumeTrue("SO-4456: CLL creation ignores rejected final request-database status",
                Boolean.getBoolean("groovy.migration.verification"));
        wireMockServer.stubFor(post(urlEqualTo(STATUS_PATH)).atPriority(1).withRequestBody(containing("FINISHED"))
                .willReturn(aResponse().withStatus(500).withHeader("Connection", "close").withBody("<rejected/>")));
        assertFailure(start());
    }

    @Test
    public void theErrorRoutePreservesTheOriginalValidationCode() {
        Assume.assumeTrue("SO-4456: CLL creation replaces the validation error while handling its BPMN error",
                Boolean.getBoolean("groovy.migration.verification"));
        variables.put("mso.workflow.sdncadapter.callback", "");
        ProcessInstance process = start();
        assertFailure(process);
        assertEquals(500, ((WorkflowException) historic(process, "WorkflowException")).getErrorCode());
    }

    private void inputs() {
        variables.put("serviceIntentParams", new JSONObject().put("serviceInstanceID", id).put("enableSdnc", false)
                .put("transportNetworks", new JSONArray().put(network)).toString());
    }

    private ProcessInstance start() {
        ProcessInstance process =
                runtimeService.startProcessInstanceByKey(PROCESS, UUID.randomUUID().toString(), variables);
        assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(process.getId()).count());
        return process;
    }

    private void assertSuccess(ProcessInstance process) {
        assertNull(historic(process, "WorkflowException"));
        assertEquals("false", historic(process, "enableSdnc"));
        assertEquals(id, historic(process, "cllId"));
        for (String activity : new String[] {"ScriptTask_1tc44ge", "ScriptTask_1xxag1o", "Activity_14an583",
                "Activity_1tiyq9t", "Activity_09pfclp", "ScriptTask_19uxoi8", "ScriptTask_1ssh2l9", "Activity_1xko5pk",
                "EndEvent_05h01gx"}) {
            assertActivity(process, activity, 1);
        }
        assertActivity(process, "Activity_01m0xuh", 0);
        assertActivity(process, "ScriptTask_1swzdpw", 0);
        wireMockServer.verify(0, deleteRequestedFor(anyUrl()));
        assertTrue(wireMockServer.getAllServeEvents().stream().allMatch(event -> event.getWasMatched()));
    }

    private void assertInventoryWrites() {
        JSONObject service = bodyFor(servicePath);
        assertEquals(id, service.getString("service-instance-id"));
        assertEquals("leased Grüße & 雪", service.getString("service-instance-name"));
        assertEquals("model-1", service.getString("model-version-id"));
        assertEquals("invariant-1", service.getString("model-invariant-id"));
        assertEquals("cll", service.getString("service-role"));
        assertEquals("created", service.getString("orchestration-status"));
        assertEquals("network-1", bodyFor(resourcePath).getString("id"));
        JSONObject policy = bodyFor(POLICY);
        assertEquals(Integer.valueOf(0), policy.get("latency"));
        assertEquals(Integer.valueOf(0), policy.get("max-bandwidth"));
        JSONObject link = bodyFor(LINK);
        assertEquals("endpoint-A", link.getString("link-id"));
        assertEquals("endpoint-A", link.getString("link-name"));
        assertEquals("endpoint-B", link.getString("link-name2"));
        assertEquals(Boolean.FALSE, link.get("in-maint"));
        wireMockServer.verify(2, putRequestedFor(urlPathMatching(resourcePath + "/relationship-list/relationship")));
        wireMockServer.verify(1,
                postRequestedFor(urlPathMatching(servicePath)).withHeader("X-HTTP-Method-Override", equalTo("PATCH")));
    }

    private JSONObject bodyFor(String path) {
        wireMockServer.verify(1, putRequestedFor(urlPathMatching(path)));
        return new JSONObject(wireMockServer.findAll(putRequestedFor(urlPathMatching(path))).get(0).getBodyAsString());
    }

    private void assertStatusPayloads() throws Exception {
        wireMockServer.verify(2, postRequestedFor(urlEqualTo(STATUS_PATH)));
        java.util.Set<String> states = new java.util.HashSet<>();
        for (var request : wireMockServer.findAll(postRequestedFor(urlEqualTo(STATUS_PATH)))) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            Document payload = factory.newDocumentBuilder()
                    .parse(new ByteArrayInputStream(request.getBodyAsString().getBytes(StandardCharsets.UTF_8)));
            assertEquals(id, payload.getElementsByTagName("serviceId").item(0).getTextContent());
            assertEquals("job-1", payload.getElementsByTagName("operationId").item(0).getTextContent());
            assertEquals("CREATE", payload.getElementsByTagName("operType").item(0).getTextContent());
            states.add(payload.getElementsByTagName("status").item(0).getTextContent());
        }
        assertEquals(java.util.Set.of("INPROGRESS", "FINISHED"), states);
    }

    private void assertFailure(ProcessInstance process) {
        Object error = historic(process, "WorkflowException");
        assertNotNull("A failed boundary must retain a workflow failure", error);
        assertEquals(WorkflowException.class, error.getClass());
        assertActivity(process, "EndEvent_05h01gx", 0);
        assertActivity(process, "ScriptTask_1swzdpw", 1);
    }

    private void assertOnlyInitialStatus() {
        wireMockServer.verify(1, postRequestedFor(urlEqualTo(STATUS_PATH)).withRequestBody(containing("INPROGRESS")));
        wireMockServer.verify(0, postRequestedFor(urlEqualTo(STATUS_PATH)).withRequestBody(containing("FINISHED")));
    }

    private Object historic(ProcessInstance process, String name) {
        HistoricVariableInstance variable = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(process.getId()).variableName(name).singleResult();
        return variable == null ? null : variable.getValue();
    }

    private void assertActivity(ProcessInstance process, String activity, long count) {
        assertEquals(activity, count, historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(process.getId()).activityId(activity).finished().count());
    }

    private void stubPut(String path, int status) {
        wireMockServer.stubFor(put(urlPathMatching(path))
                .willReturn(aResponse().withStatus(status).withHeader("Connection", "close")));
    }

    private void stubStatus(int status) {
        wireMockServer.stubFor(post(urlEqualTo(STATUS_PATH))
                .willReturn(aResponse().withStatus(status).withHeader("Content-Type", "application/soap+xml")
                        .withHeader("Connection", "close").withBody("<updated/>")));
    }

    private void assertProductionResource() throws Exception {
        URL resource = getClass().getClassLoader().getResource(RESOURCE);
        assertNotNull(resource);
        String location = resource.toExternalForm();
        assertTrue(location,
                location.contains("/so-bpmn-infrastructure-flows/target/classes/")
                        || (location.matches("jar:.*/so-bpmn-infrastructure-flows-[^/]+\\.jar!/" + RESOURCE)
                                && !location.contains("-tests.jar!/")));
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS)
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
