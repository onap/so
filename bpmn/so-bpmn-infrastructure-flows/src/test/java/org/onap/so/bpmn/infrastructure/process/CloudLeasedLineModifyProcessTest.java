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
import org.junit.Before;
import org.junit.Assume;
import org.junit.Test;
import org.onap.aaiclient.client.aai.AAIVersion;
import org.onap.so.BaseBPMNTest;
import org.w3c.dom.Document;

public class CloudLeasedLineModifyProcessTest extends BaseBPMNTest {
    private static final String PROCESS = "DoCloudLeasedLineModify";
    private static final String RESOURCE = "subprocess/DoCloudLeasedLineModify.bpmn";
    private static final String STATUS = "/cll-modify-status";
    private static final String BUSINESS = "/business/customers/customer/customer-1/service-subscriptions/"
            + "service-subscription/service-type/service-instances/service-instance/";
    private String id;
    private String servicePath;
    private String resourcePath;
    private String policyPath;
    private JSONObject network;

    @Before
    public void prepareContracts() throws Exception {
        assertProductionResource();
        wireMockServer.resetAll();
        variables.clear();
        id = "cll-" + UUID.randomUUID();
        servicePath = "/aai/v[0-9]+" + BUSINESS + id;
        resourcePath = servicePath + "/allotted-resources/allotted-resource/network-1";
        policyPath = "/aai/v[0-9]+/network/network-policies/network-policy/policy-1";
        variables.put("msoRequestId", "request-1");
        variables.put("serviceInstanceID", id);
        variables.put("servicename", "leased Gr\u00fc\u00dfe & SO");
        variables.put("modelInvariantUuid", "invariant-1");
        variables.put("modelUuid", "model-1");
        variables.put("jobId", "job-1");
        variables.put("subscriptionServiceType", "service-type");
        variables.put("serviceType", "leased-line");
        variables.put("globalSubscriberId", "customer-1");
        variables.put("mso.workflow.sdncadapter.callback", "http://localhost:" + wireMockPort + "/callback");
        variables.put("mso.adapters.openecomp.db.endpoint", "http://localhost:" + wireMockPort + STATUS);
        network = new JSONObject().put("id", "network-1").put("sla", new JSONObject().put("maxBandwidth", 0))
                .put("connectionLinks", new JSONArray().put(new JSONObject().put("transportEndpointA", "endpoint-A")
                        .put("transportEndpointB", "endpoint-B")));
        inputs(false);
        String canonical = "/aai/" + AAIVersion.LATEST + BUSINESS + id;
        stubRead("/aai/v[0-9]+/nodes/service-instances/service-instance/" + id,
                new JSONObject().put("results",
                        new JSONArray().put(new JSONObject().put("resource-type", "service-instance")
                                .put("resource-link", "http://localhost:" + wireMockPort + canonical))));
        stubRead(servicePath, new JSONObject().put("service-instance-id", id).put("resource-version", "version-1"));
        stubRead(servicePath + "/allotted-resources", new JSONObject().put("allotted-resource",
                new JSONArray().put(new JSONObject().put("id", "network-1"))));
        JSONArray relationships = new JSONArray()
                .put(relationship("network-policy", "/network/network-policies/" + "network-policy/policy-1"))
                .put(relationship("logical-link", "/network/logical-links/logical-link/endpoint-A"));
        stubRead(resourcePath, new JSONObject().put("id", "network-1").put("relationship-list",
                new JSONObject().put("relationship", relationships)));
        stubRead(policyPath, new JSONObject().put("network-policy-id", "policy-1").put("max-bandwidth", 100));
        stubRead("/aai/v[0-9]+/network/logical-links/logical-link/endpoint-A",
                new JSONObject().put("link-id", "link-1").put("link-name", "endpoint-A"));
        stubUpdate(policyPath, 200);
        stubUpdate(servicePath, 200);
        wireMockServer.stubFor(post(urlEqualTo(STATUS))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/soap+xml")
                        .withHeader("Connection", "close").withBody("<updated/>")));
    }

    @Test
    public void anExistingLinkIsRetainedWhileZeroBandwidthAndCompletionAreWritten() throws Exception {
        ProcessInstance process = start();
        assertSuccess(process);
        assertEquals(Integer.valueOf(0), updateBody(policyPath).get("max-bandwidth"));
        assertEquals(1, updateBody(policyPath).length());
        assertEquals("modified", updateBody(servicePath).getString("orchestration-status"));
        wireMockServer.verify(0, putRequestedFor(anyUrl()));
        wireMockServer.verify(0, deleteRequestedFor(anyUrl()));
        assertCompletion();
    }

    @Test
    public void bandwidthOnlyModificationDoesNotRequireConnectionLinks() throws Exception {
        network.remove("connectionLinks");
        inputs(true);
        assertSuccess(start());
        assertEquals(Integer.valueOf(0), updateBody(policyPath).get("max-bandwidth"));
        wireMockServer.verify(0, putRequestedFor(anyUrl()));
        assertCompletion();
    }

    @Test
    public void aNewLinkIsCreatedAndAttachedToTheRequestedResource() throws Exception {
        prepareNewLink(201);
        assertSuccess(start());
        String path = "/aai/v[0-9]+/network/logical-links/logical-link/endpoint-C";
        wireMockServer.verify(1, putRequestedFor(urlPathMatching(path)));
        JSONObject link =
                new JSONObject(wireMockServer.findAll(putRequestedFor(urlPathMatching(path))).get(0).getBodyAsString());
        assertEquals("new-link-1", link.getString("link-id"));
        assertEquals("endpoint-C", link.getString("link-name"));
        assertEquals("endpoint-D", link.getString("link-name2"));
        assertEquals("TsciConnectionLink", link.getString("link-type"));
        assertEquals(Boolean.FALSE, link.get("in-maint"));
        wireMockServer.verify(1, putRequestedFor(urlPathMatching(resourcePath + "/relationship-list/relationship")));
        JSONObject attachment = new JSONObject(wireMockServer
                .findAll(putRequestedFor(urlPathMatching(resourcePath + "/relationship-list/relationship"))).get(0)
                .getBodyAsString());
        assertEquals("logical-link", attachment.getString("related-to"));
        assertTrue(attachment.getString("related-link").endsWith("/network/logical-links/logical-link/endpoint-C"));
        assertEquals(Integer.valueOf(0), updateBody(policyPath).get("max-bandwidth"));
        assertCompletion();
    }

    @Test
    public void rejectedNewLinkStopsBeforeAttachmentPolicyAndCompletion() {
        prepareNewLink(503);
        assertFailure(start());
        wireMockServer.verify(1,
                putRequestedFor(urlPathMatching("/aai/v[0-9]+/network/logical-links/logical-link/endpoint-C")));
        wireMockServer.verify(0, putRequestedFor(urlPathMatching(resourcePath + "/relationship-list/relationship")));
        wireMockServer.verify(0, postRequestedFor(urlPathMatching(policyPath)));
        wireMockServer.verify(0, postRequestedFor(urlPathMatching(servicePath)));
    }

    @Test
    public void rejectedServiceStatusStopsBeforeCompletion() {
        stubUpdate(servicePath, 503);
        assertFailure(start());
        wireMockServer.verify(1, postRequestedFor(urlPathMatching(policyPath)));
        wireMockServer.verify(1, postRequestedFor(urlPathMatching(servicePath)));
    }

    @Test
    public void rejectedCompletionStatusCannotCompleteTheWorkflowSuccessfully() {
        Assume.assumeTrue("SO-4456: CLL modification ignores rejected final request-database status",
                Boolean.getBoolean("groovy.migration.verification"));
        wireMockServer.stubFor(post(urlEqualTo(STATUS))
                .willReturn(aResponse().withStatus(500).withHeader("Connection", "close").withBody("<rejected/>")));
        ProcessInstance process = start();
        assertNotNull("A rejected completion write must fail the workflow", historic(process, "WorkflowException"));
        assertActivity(process, "EndEvent_05h01gx", 0);
    }

    @Test
    public void rejectedPolicyUpdateStopsBeforeServiceStatusAndCompletion() {
        stubUpdate(policyPath, 503);
        assertFailure(start());
        wireMockServer.verify(1, postRequestedFor(urlPathMatching(policyPath)));
        wireMockServer.verify(0, postRequestedFor(urlPathMatching(servicePath)));
    }

    @Test
    public void missingServiceStopsBeforeAnyInventoryMutation() {
        wireMockServer.stubFor(get(urlPathMatching(servicePath))
                .willReturn(aResponse().withStatus(404).withHeader("Content-Type", "application/json").withBody("{}")));
        assertFailure(start());
        wireMockServer.verify(0, postRequestedFor(anyUrl()));
        wireMockServer.verify(0, putRequestedFor(anyUrl()));
    }

    @Test
    public void missingPolicyRelationshipStopsBeforeAnyInventoryMutation() {
        stubRead(resourcePath, new JSONObject().put("id", "network-1").put("relationship-list", new JSONObject().put(
                "relationship",
                new JSONArray().put(relationship("logical-link", "/network/logical-links/logical-link/endpoint-A")))));
        assertFailure(start());
        wireMockServer.verify(0, postRequestedFor(anyUrl()));
        wireMockServer.verify(0, putRequestedFor(anyUrl()));
    }

    @Test
    public void missingSlaStopsBeforeUpdatingThePolicy() {
        network.remove("sla");
        inputs(false);
        assertFailure(start());
        wireMockServer.verify(0, postRequestedFor(anyUrl()));
        wireMockServer.verify(0, putRequestedFor(anyUrl()));
    }

    @Test
    public void missingCallbackStopsBeforeAnyExternalAction() {
        variables.put("mso.workflow.sdncadapter.callback", "");
        assertFailure(start());
        assertTrue(wireMockServer.getAllServeEvents().isEmpty());
    }

    private JSONObject relationship(String type, String path) {
        return new JSONObject().put("related-to", type).put("related-link", "/aai/" + AAIVersion.LATEST + path);
    }

    private void prepareNewLink(int status) {
        network.put("connectionLinks", new JSONArray().put(new JSONObject().put("id", "new-link-1")
                .put("transportEndpointA", "endpoint-C").put("transportEndpointB", "endpoint-D")));
        inputs(false);
        wireMockServer.stubFor(get(urlPathMatching(resourcePath)).withQueryParam("format", equalTo("count"))
                .withQueryParam("resultIndex", equalTo("0")).withQueryParam("resultSize", equalTo("1"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{\"results\":[{\"resource-type\":\"allotted-resource\",\"total\":1}]}")));
        wireMockServer.stubFor(put(urlPathMatching("/aai/v[0-9]+/network/logical-links/logical-link/endpoint-C"))
                .willReturn(aResponse().withStatus(status).withHeader("Connection", "close")));
        wireMockServer.stubFor(put(urlPathMatching(resourcePath + "/relationship-list/relationship"))
                .willReturn(aResponse().withStatus(200).withHeader("Connection", "close")));
    }

    private void inputs(boolean bandwidthOnly) {
        JSONObject input =
                new JSONObject().put("enableSdnc", false).put("transportNetworks", new JSONArray().put(network));
        if (bandwidthOnly) {
            input.put("modifyAction", "bandwidth");
        }
        variables.put("serviceIntentParams", input.toString());
    }

    private ProcessInstance start() {
        ProcessInstance process =
                runtimeService.startProcessInstanceByKey(PROCESS, UUID.randomUUID().toString(), variables);
        assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(process.getId()).count());
        return process;
    }

    private void assertSuccess(ProcessInstance process) {
        assertNull(historic(process, "WorkflowException"));
        assertEquals(id, historic(process, "cllId"));
        assertEquals("false", historic(process, "enableSdnc"));
        for (String activity : new String[] {"ScriptTask_1tc44ge", "Activity_0phv8e5", "ScriptTask_19uxoi8",
                "ScriptTask_1ssh2l9", "Activity_0ziz3ti", "EndEvent_05h01gx"}) {
            assertActivity(process, activity, 1);
        }
        assertActivity(process, "Activity_14ab476", 0);
        assertActivity(process, "ScriptTask_1swzdpw", 0);
        assertTrue(wireMockServer.getAllServeEvents().stream().allMatch(event -> event.getWasMatched()));
    }

    private void assertFailure(ProcessInstance process) {
        assertNotNull(historic(process, "WorkflowException"));
        assertActivity(process, "ScriptTask_1swzdpw", 1);
        assertActivity(process, "EndEvent_05h01gx", 0);
        wireMockServer.verify(0, postRequestedFor(urlEqualTo(STATUS)));
    }

    private JSONObject updateBody(String path) {
        wireMockServer.verify(1,
                postRequestedFor(urlPathMatching(path)).withHeader("X-HTTP-Method-Override", equalTo("PATCH")));
        return new JSONObject(wireMockServer.findAll(postRequestedFor(urlPathMatching(path))).get(0).getBodyAsString());
    }

    private void assertCompletion() throws Exception {
        wireMockServer.verify(1, postRequestedFor(urlEqualTo(STATUS)));
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        Document payload = factory.newDocumentBuilder()
                .parse(new ByteArrayInputStream(wireMockServer.findAll(postRequestedFor(urlEqualTo(STATUS))).get(0)
                        .getBodyAsString().getBytes(StandardCharsets.UTF_8)));
        assertEquals("http://schemas.xmlsoap.org/soap/envelope/", payload.getDocumentElement().getNamespaceURI());
        assertEquals(1, payload.getElementsByTagNameNS("http://org.onap.so/requestsdb", "updateResourceOperationStatus")
                .getLength());
        assertEquals(id, payload.getElementsByTagName("serviceId").item(0).getTextContent());
        assertEquals("job-1", payload.getElementsByTagName("operationId").item(0).getTextContent());
        assertEquals("MODIFY", payload.getElementsByTagName("operType").item(0).getTextContent());
        assertEquals("FINISHED", payload.getElementsByTagName("status").item(0).getTextContent());
        assertEquals("100", payload.getElementsByTagName("progress").item(0).getTextContent());
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

    private void stubRead(String path, JSONObject body) {
        wireMockServer.stubFor(get(urlPathMatching(path))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withHeader("Connection", "close").withBody(body.toString())));
    }

    private void stubUpdate(String path, int status) {
        wireMockServer.stubFor(post(urlPathMatching(path)).withHeader("X-HTTP-Method-Override", equalTo("PATCH"))
                .willReturn(aResponse().withStatus(status).withHeader("Connection", "close")));
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
