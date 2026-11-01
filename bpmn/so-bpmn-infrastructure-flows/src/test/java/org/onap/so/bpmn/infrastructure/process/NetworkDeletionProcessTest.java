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
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.BaseBPMNTest;
import org.onap.so.bpmn.core.WorkflowException;

public class NetworkDeletionProcessTest extends BaseBPMNTest {
    private static final String PROCESS = "DoDeleteNetworkInstance";
    private static final String PREFIX = "DELNWKI_";
    private static final String AAI_PATH = "/aai/v[0-9]+/network/l3-networks/l3-network/network-1";
    private final List<String> owned = new ArrayList<>();

    @Before
    public void configureProductionProcessAndInventory() throws Exception {
        String path = "subprocess/" + PROCESS + ".bpmn";
        URL resource = getClass().getClassLoader().getResource(path);
        assertNotNull(resource);
        String location = resource.toExternalForm();
        assertTrue(location,
                location.contains("/so-bpmn-infrastructure-flows/target/classes/")
                        || (location.matches("jar:.*/so-bpmn-infrastructure-flows-[^/]+\\.jar!/" + path)
                                && !location.contains("-tests.jar!/")));
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS)
                .latestVersion().singleResult();
        assertNotNull(definition);
        try (InputStream original = resource.openStream();
                InputStream deployed = repositoryService.getResourceAsStream(definition.getDeploymentId(),
                        definition.getResourceName())) {
            assertArrayEquals(original.readAllBytes(), deployed.readAllBytes());
        }
        wireMockServer.resetAll();
        inventory(404, "{}");
        variables.clear();
        variables.put("msoRequestId", UUID.randomUUID().toString());
        variables.put("serviceInstanceId", "service-1");
        variables.put("sdncVersion", "1610");
        variables.put("mso.rollback", "false");
        variables.put("bpmnRequest", request("network-1"));
    }

    @After
    public void removeOwnedInstances() {
        for (String id : owned) {
            if (runtimeService.createProcessInstanceQuery().processInstanceId(id).count() != 0) {
                runtimeService.deleteProcessInstance(id, "network deletion test cleanup");
            }
        }
    }

    @Test
    public void emptyNetworkIdentityStopsBeforeInventoryOrAdapterCalls() {
        variables.put("bpmnRequest", request(""));
        ProcessInstance process = start();
        error(process, 2500, "missing 'network-id'");
        activity(process, "callAAIQuery_scriptTask", 0);
        noAdapterWork(process);
        assertTrue(wireMockServer.getAllServeEvents().isEmpty());
    }

    @Test
    public void absentInventoryIsAnIdempotentSuccessWithoutCompensationOrExternalDeletes() {
        Assume.assumeTrue("SO-4457: absent networks still build SDNC rollback data from an empty inventory response",
                Boolean.getBoolean("groovy.migration.verification"));
        ProcessInstance process = start();
        assertEquals(Integer.valueOf(404), value(process, PREFIX + "aaiReturnCode"));
        assertEquals(Boolean.FALSE, value(process, PREFIX + "isAAIGood"));
        assertEquals(Boolean.TRUE, value(process, PREFIX + "isSilentSuccess"));
        assertEquals(Boolean.TRUE, value(process, PREFIX + "Success"));
        assertEquals(Boolean.FALSE, value(process, "rolledBack"));
        noAdapterWork(process);
        onlyInventoryReads(2);
        assertNull(value(process, "WorkflowException"));
        assertTrue(((java.util.Map<?, ?>) value(process, "rollbackData")).isEmpty());
        activity(process, "ScriptTask_0dtzzht", 1);
    }

    @Test
    public void aVfModuleRelationshipPreventsDeletionAndRetainsItsValidationError() {
        Assume.assumeTrue("SO-4457: network deletion replaces the relationship validation code 2500 with 7000",
                Boolean.getBoolean("groovy.migration.verification"));
        JSONObject relationship = new JSONObject().put("related-to", "vf-module")
                .put("related-link", "/aai/v14/network/generic-vnfs/generic-vnf/vnf-1/vf-modules/vf-module/module-1")
                .put("relationship-data",
                        new JSONArray()
                                .put(new JSONObject().put("relationship-key", "generic-vnf.vnf-id")
                                        .put("relationship-value", "vnf-1"))
                                .put(new JSONObject().put("relationship-key", "vf-module.vf-module-id")
                                        .put("relationship-value", "module-1")));
        inventory(200, new JSONObject().put("network-id", "network-1").put("resource-version", "version-1")
                .put("relationship-list", new JSONObject().put("relationship", new JSONArray().put(relationship)))
                .toString());
        ProcessInstance process = start();
        assertEquals(Boolean.TRUE, value(process, PREFIX + "isVfRelationshipExist"));
        activity(process, "relationshipError_EndEvent", 1);
        noAdapterWork(process);
        onlyInventoryReads(2);
        assertTrue(value(process, PREFIX + "WorkflowException") instanceof WorkflowException);
        assertEquals(2500, ((WorkflowException) value(process, PREFIX + "WorkflowException")).getErrorCode());
        error(process, 2500, "not allowed to delete");
    }

    @Test
    public void unavailableInventoryCannotBecomeASilentSuccessOrStartDeletion() {
        inventory(503, "{\"error\":\"unavailable\"}");
        ProcessInstance process = start();
        error(process, 7000, "callRESTQueryAAI()");
        assertEquals(Boolean.FALSE, value(process, PREFIX + "isSilentSuccess"));
        assertEquals(Boolean.FALSE, value(process, PREFIX + "Success"));
        noAdapterWork(process);
        onlyInventoryReads(1);
    }

    @Test
    public void malformedSuccessfulInventoryResponsesCannotStartDeletion() {
        inventory(200, "not-json");
        ProcessInstance process = start();
        error(process, 7000, "callRESTQueryAAI()");
        assertEquals(Boolean.FALSE, value(process, PREFIX + "isSilentSuccess"));
        assertEquals(Boolean.FALSE, value(process, PREFIX + "Success"));
        noAdapterWork(process);
        onlyInventoryReads(1);
    }

    private ProcessInstance start() {
        ProcessInstance process =
                runtimeService.startProcessInstanceByKey(PROCESS, UUID.randomUUID().toString(), variables);
        owned.add(process.getId());
        assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(process.getId()).count());
        return process;
    }

    private String request(String network) {
        return "<network-request xmlns=\"http://www.w3.org/2001/XMLSchema\"><request-info>"
                + "<request-id>request-1</request-id><action>DELETE</action><source>VID</source>"
                + "<service-instance-id>service-1</service-instance-id></request-info><network-inputs>" + "<network-id>"
                + network + "</network-id><network-type>private</network-type>"
                + "<modelCustomizationId>model-1</modelCustomizationId><aic-cloud-region>region-1</aic-cloud-region>"
                + "<tenant-id>tenant-1</tenant-id><backout-on-failure>false</backout-on-failure>"
                + "</network-inputs><network-params/></network-request>";
    }

    private void inventory(int status, String body) {
        wireMockServer.stubFor(get(urlPathMatching(AAI_PATH)).willReturn(aResponse().withStatus(status)
                .withHeader("Content-Type", "application/json").withHeader("Connection", "close").withBody(body)));
    }

    private void error(ProcessInstance process, int code, String detail) {
        assertTrue(value(process, "WorkflowException") instanceof WorkflowException);
        WorkflowException error = (WorkflowException) value(process, "WorkflowException");
        assertEquals(code, error.getErrorCode());
        assertTrue(error.getErrorMessage(), error.getErrorMessage().contains(detail));
    }

    private void noAdapterWork(ProcessInstance process) {
        activity(process, "callRESTQueryCloudRegion_ScriptTask", 0);
        activity(process, "prepareNetworkRequest_ScriptTask", 0);
        activity(process, "Activity_1bwwwba", 0);
        activity(process, "CallActivity_0wyu1mx", 0);
        activity(process, "CallActivity_03wzdas", 0);
        activity(process, "callDeleteSDNCAdapter_CallActivity", 0);
        assertEquals(0,
                historyService.createHistoricProcessInstanceQuery().superProcessInstanceId(process.getId()).count());
    }

    private void onlyInventoryReads(int count) {
        wireMockServer.verify(count, getRequestedFor(urlPathMatching(AAI_PATH)));
        assertEquals(count, wireMockServer.getAllServeEvents().size());
    }

    private void activity(ProcessInstance process, String id, long count) {
        assertEquals(id, count, historyService.createHistoricActivityInstanceQuery().processInstanceId(process.getId())
                .activityId(id).finished().count());
    }

    private Object value(ProcessInstance process, String name) {
        HistoricVariableInstance value = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(process.getId()).variableName(name).singleResult();
        return value == null ? null : value.getValue();
    }
}
