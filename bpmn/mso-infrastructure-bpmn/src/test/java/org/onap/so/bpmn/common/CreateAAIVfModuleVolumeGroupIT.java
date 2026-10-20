/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.bpmn.common;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;
import org.onap.aai.domain.yang.VfModule;
import org.onap.so.BaseIntegrationTest;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.mock.FileUtil;

public class CreateAAIVfModuleVolumeGroupIT extends BaseIntegrationTest {
    private static final String PROCESS = "CreateAAIVfModuleVolumeGroup";
    private static final String PREFIX = "CAAIVfModVG_";
    private static final String MODULE =
            "/aai/v[0-9]+/network/generic-vnfs/generic-vnf/skask" + "/vf-modules/vf-module/lukewarm";
    private static final String RELATIONSHIP = MODULE + "/relationship-list/relationship";
    private static final String MODULE_JSON = "{\"vf-module-id\":\"lukewarm\",\"resource-version\":\"0000075\"}";

    @Test
    public void happyPath() {
        stubModule(200, MODULE_JSON);
        stubRelationship(200);

        String key = invoke();

        assertEquals(Integer.valueOf(200), value(key, "getVfModuleResponseCode"));
        assertTrue(value(key, "getVfModuleResponse") instanceof VfModule);
        VfModule module = (VfModule) value(key, "getVfModuleResponse");
        assertEquals("lukewarm", module.getVfModuleId());
        assertEquals("0000075", module.getResourceVersion());
        assertEquals(Integer.valueOf(200), value(key, "updateVfModuleResponseCode"));
        assertEquals("Success", value(key, "updateVfModuleResponse"));
        assertNull(getVariableFromHistory(key, "WorkflowException"));
        wireMockServer.verify(1,
                getRequestedFor(urlPathMatching(MODULE)).withHeader("Accept", equalTo("application/json")));
        wireMockServer.verify(1, putRequestedFor(urlPathMatching(RELATIONSHIP))

                .withRequestBody(matchingJsonPath("$['related-link']", equalTo("/cloud-infrastructure/"
                        + "cloud-regions/cloud-region/test-owner/pdk1/volume-groups/volume-group/78987"))));
        assertActivity(key, "UpdateVfModule", 1);
        assertActivity(key, "EndEvent_4", 1);
        assertActivity(key, "AAIQueryFailure", 0);
        assertActivity(key, "HandleUpdateVfModuleFailure", 0);
    }

    @Test
    public void badGet() {
        assertQueryFailure(404, "", 404);
    }

    @Test
    public void rejectedQueryDoesNotCreateARelationship() {
        assertQueryFailure(500, "", 500);
    }

    @Test
    public void malformedSuccessfulQueryDoesNotCreateARelationship() {
        assertQueryFailure(200, "not JSON", 500);
    }

    @Test
    public void rejectedRelationshipUsesTheUpdateErrorRoute() {
        stubModule(200, MODULE_JSON);
        stubRelationship(503);

        String key = invoke();

        assertEquals(Integer.valueOf(200), value(key, "getVfModuleResponseCode"));
        assertEquals(Integer.valueOf(500), value(key, "updateVfModuleResponseCode"));
        assertTrue(((String) value(key, "updateVfModuleResponse")).startsWith("AAI PUT Failed:"));
        assertWorkflowError(key);
        wireMockServer.verify(1, getRequestedFor(urlPathMatching(MODULE)));
        wireMockServer.verify(1, putRequestedFor(urlPathMatching(RELATIONSHIP)));
        assertActivity(key, "UpdateVfModule", 1);
        assertActivity(key, "HandleUpdateVfModuleFailure", 1);
        assertActivity(key, "EndEvent_2", 1);
        assertActivity(key, "EndEvent_4", 0);
    }

    private void assertQueryFailure(int status, String body, int classifiedStatus) {
        stubModule(status, body);

        String key = invoke();

        assertEquals(Integer.valueOf(classifiedStatus), value(key, "getVfModuleResponseCode"));
        assertNull(value(key, "updateVfModuleResponseCode"));
        assertEquals("", value(key, "updateVfModuleResponse"));
        assertWorkflowError(key);
        wireMockServer.verify(1, getRequestedFor(urlPathMatching(MODULE)));
        wireMockServer.verify(0, putRequestedFor(urlPathMatching(RELATIONSHIP)));
        assertActivity(key, "AAIQueryFailure", 1);
        assertActivity(key, "EndEvent_7", 1);
        assertActivity(key, "UpdateVfModule", 0);
        assertActivity(key, "EndEvent_4", 0);
    }

    private String invoke() {
        String key = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        variables.put("mso-request-id", key);
        variables.put("CreateAAIVfModuleVolumeGroupRequest",
                FileUtil.readResourceFile("__files/VfModularity/CreateAAIVfModuleVolumeGroupRequest.xml"));
        invokeSubProcess(PROCESS, key, variables);
        assertTrue(isProcessEnded(key));
        assertEquals("skask", value(key, "vnfId"));
        assertEquals("lukewarm", value(key, "vfModuleId"));
        assertEquals("pdk1", value(key, "aicCloudRegion"));
        assertEquals("test-owner", value(key, "cloudOwner"));
        assertEquals("78987", value(key, "volumeGroupId"));
        return key;
    }

    private Object value(String key, String name) {
        return getVariableFromHistory(key, PREFIX + name);
    }

    private void assertWorkflowError(String key) {
        Object error = getVariableFromHistory(key, "WorkflowException");
        assertTrue(error instanceof WorkflowException);
        assertEquals(5000, ((WorkflowException) error).getErrorCode());
    }

    private void stubModule(int status, String body) {
        wireMockServer.stubFor(get(urlPathMatching(MODULE)).willReturn(
                aResponse().withStatus(status).withHeader("Content-Type", "application/json").withBody(body)));
    }

    private void stubRelationship(int status) {
        wireMockServer.stubFor(put(urlPathMatching(RELATIONSHIP)).willReturn(aResponse().withStatus(status)));
    }

    private void assertActivity(String key, String activity, long expected) {
        String processId = historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey(key)
                .processDefinitionKey(PROCESS).singleResult().getId();
        assertEquals(activity, expected, historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processId).activityId(activity).finished().count());
    }
}
