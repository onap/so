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
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.onap.so.BaseIntegrationTest;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.mock.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for UpdateAAIVfModuleTest.bpmn.
 */

public class UpdateAAIVfModuleIT extends BaseIntegrationTest {

    Logger logger = LoggerFactory.getLogger(UpdateAAIVfModuleIT.class);


    /**
     * Test the happy path through the flow.
     */
    @Test

    public void happyPath() {
        logStart();

        String updateAAIVfModuleRequest =
                FileUtil.readResourceFile("__files/VfModularity/UpdateAAIVfModuleRequest.xml");
        stubQuery(200, inventoryJson());
        stubUpdate(200);

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        variables.put("mso-request-id", UUID.randomUUID().toString());
        variables.put("isDebugLogEnabled", "true");
        variables.put("UpdateAAIVfModuleRequest", updateAAIVfModuleRequest);
        invokeSubProcess("UpdateAAIVfModule", businessKey, variables);

        Assert.assertTrue(isProcessEnded(businessKey));
        String response = (String) getVariableFromHistory(businessKey, "UAAIVfMod_updateVfModuleResponse");
        Integer responseCode = (Integer) getVariableFromHistory(businessKey, "UAAIVfMod_updateVfModuleResponseCode");
        logger.debug("Subflow response code: {}", responseCode);
        logger.debug("Subflow response: {}", response);
        Assert.assertEquals(200, responseCode.intValue());
        Assert.assertNull(getVariableFromHistory(businessKey, "WorkflowException"));
        wireMockServer.verify(1,
                getRequestedFor(urlPathMatching(resourcePath())).withHeader("Accept", equalTo("application/json")));
        wireMockServer.verify(1,
                postRequestedFor(urlPathMatching(resourcePath())).withHeader("X-HTTP-Method-Override", equalTo("PATCH"))
                        .withHeader("Content-Type", equalTo("application/merge-patch+json"))
                        .withRequestBody(matchingJsonPath("$['vf-module-id']", equalTo("supercool"))));
        assertActivity(businessKey, "UpdateVfModule", 1);
        assertActivity(businessKey, "EndEvent_4", 1);
        assertActivity(businessKey, "AAIQueryFailure", 0);


        logEnd();
    }

    /**
     * Test the case where the GET to AAI returns a 404.
     */
    @Test

    public void badGet() {

        logStart();

        String updateAAIVfModuleRequest =
                FileUtil.readResourceFile("__files/VfModularity/UpdateAAIVfModuleRequest.xml");
        stubQuery(404, "");

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        variables.put("mso-request-id", UUID.randomUUID().toString());
        variables.put("isDebugLogEnabled", "true");
        variables.put("UpdateAAIVfModuleRequest", updateAAIVfModuleRequest);
        invokeSubProcess("UpdateAAIVfModule", businessKey, variables);

        Assert.assertTrue(isProcessEnded(businessKey));
        String response = (String) getVariableFromHistory(businessKey, "UAAIVfMod_getVfModuleResponse");
        Integer responseCode = (Integer) getVariableFromHistory(businessKey, "UAAIVfMod_getVfModuleResponseCode");
        logger.debug("Subflow response code: {}", responseCode);
        logger.debug("Subflow response: {}", response);
        Assert.assertEquals(404, responseCode.intValue());
        wireMockServer.verify(0, postRequestedFor(urlPathMatching(resourcePath())));
        assertActivity(businessKey, "AAIQueryFailure", 1);
        assertActivity(businessKey, "UpdateVfModule", 0);
        assertError(businessKey, 5000);

        logEnd();
    }

    /**
     * Test the case where the GET to AAI is successful, but he subsequent PUT returns 404.
     */
    @Test

    public void badPatch() {

        logStart();

        String updateAAIVfModuleRequest =
                FileUtil.readResourceFile("__files/VfModularity/UpdateAAIVfModuleRequest.xml");
        stubQuery(200, inventoryJson());
        stubUpdate(404);

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        variables.put("mso-request-id", UUID.randomUUID().toString());
        variables.put("isDebugLogEnabled", "true");
        variables.put("UpdateAAIVfModuleRequest", updateAAIVfModuleRequest);
        invokeSubProcess("UpdateAAIVfModule", businessKey, variables);

        Assert.assertTrue(isProcessEnded(businessKey));
        String response = (String) getVariableFromHistory(businessKey, "UAAIVfMod_updateVfModuleResponse");
        Integer responseCode = (Integer) getVariableFromHistory(businessKey, "UAAIVfMod_updateVfModuleResponseCode");
        logger.debug("Subflow response code: {}", responseCode);
        logger.debug("Subflow response: {}", response);
        Assert.assertEquals(404, responseCode.intValue());
        wireMockServer.verify(1, postRequestedFor(urlPathMatching(resourcePath())).withHeader("X-HTTP-Method-Override",
                equalTo("PATCH")));
        assertActivity(businessKey, "EndEvent_4", 0);
        assertError(businessKey, 2500);

        logEnd();
    }

    @Test
    public void rejectedQueryPreventsTheUpdate() {
        assertQueryFailure(503, "", 500);
    }

    @Test
    public void malformedSuccessfulQueryPreventsTheUpdate() {
        assertQueryFailure(200, "not JSON", 500);
    }

    private void assertQueryFailure(int status, String body, int classifiedStatus) {
        stubQuery(status, body);
        String key = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        variables.put("mso-request-id", key);
        variables.put("UpdateAAIVfModuleRequest",
                FileUtil.readResourceFile("__files/VfModularity/UpdateAAIVfModuleRequest.xml"));
        invokeSubProcess("UpdateAAIVfModule", key, variables);
        Assert.assertTrue(isProcessEnded(key));
        Assert.assertEquals(Integer.valueOf(classifiedStatus),
                getVariableFromHistory(key, "UAAIVfMod_getVfModuleResponseCode"));
        Assert.assertNull(getVariableFromHistory(key, "UAAIVfMod_updateVfModuleResponseCode"));
        wireMockServer.verify(1, getRequestedFor(urlPathMatching(resourcePath())));
        wireMockServer.verify(0, postRequestedFor(urlPathMatching(resourcePath())));
        assertActivity(key, "AAIQueryFailure", 1);
        assertActivity(key, "UpdateVfModule", 0);
        assertActivity(key, "EndEvent_4", 0);
        assertError(key, 5000);
    }

    private void assertError(String key, int code) {
        Object error = getVariableFromHistory(key, "WorkflowException");
        Assert.assertTrue(error instanceof WorkflowException);
        Assert.assertEquals(code, ((WorkflowException) error).getErrorCode());
    }

    private String resourcePath() {
        return "/aai/v[0-9]+/network/generic-vnfs/generic-vnf/skask/vf-modules/vf-module/supercool";
    }

    private String inventoryJson() {
        return "{\"vf-module-id\":\"supercool\",\"model-invariant-id\":\"introvert\",\"resource-version\":\"0000075\"}";
    }

    private void stubQuery(int status, String body) {
        wireMockServer.stubFor(get(urlPathMatching(resourcePath())).willReturn(
                aResponse().withStatus(status).withHeader("Content-Type", "application/json").withBody(body)));
    }

    private void stubUpdate(int status) {
        wireMockServer.stubFor(post(urlPathMatching(resourcePath()))
                .withHeader("X-HTTP-Method-Override", equalTo("PATCH")).willReturn(aResponse().withStatus(status)));
    }

    private void assertActivity(String key, String activity, long expected) {
        String id = historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey(key)
                .processDefinitionKey("UpdateAAIVfModule").singleResult().getId();
        Assert.assertEquals(activity, expected, historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(id).activityId(activity).finished().count());
    }

    @Test
    public void requestedModuleFieldsAreSentInOnePatch() {
        stubQuery(200, inventoryJson());
        stubUpdate(200);
        String key = invokeRequest(FileUtil.readResourceFile("__files/VfModularity/UpdateAAIVfModuleRequest.xml"));
        wireMockServer.verify(1,
                postRequestedFor(urlPathMatching(resourcePath()))
                        .withRequestBody(matchingJsonPath("$['vf-module-id']", equalTo("supercool")))
                        .withRequestBody(matchingJsonPath("$['heat-stack-id']", equalTo("slowburn")))
                        .withRequestBody(matchingJsonPath("$['orchestration-status']", equalTo("complete")))
                        .withRequestBody(matchingJsonPath("$['persona-model-version']", equalTo("3.14")))
                        .withRequestBody(
                                matchingJsonPath("$['contrail-service-instance-fqdn']", equalTo("myhost.appl.com")))
                        .withRequestBody(matchingJsonPath("$['resource-version']", absent())));
        Assert.assertNull(getVariableFromHistory(key, "WorkflowException"));
        assertActivity(key, "EndEvent_4", 1);
    }

    @Test
    public void deleteSentinelsBecomeExplicitNullMergePatchValues() {
        stubQuery(200, inventoryJson());
        stubUpdate(200);
        String request = FileUtil.readResourceFile("__files/VfModularity/UpdateAAIVfModuleRequest.xml")
                .replace("slowburn", "DELETE").replace("complete", "DELETE").replace("myhost.appl.com", "DELETE");
        String key = invokeRequest(request);
        wireMockServer.verify(1, postRequestedFor(urlPathMatching(resourcePath())).withHeader("X-HTTP-Method-Override",
                equalTo("PATCH")));
        JSONObject patch = new JSONObject(
                wireMockServer.findAll(postRequestedFor(urlPathMatching(resourcePath()))).get(0).getBodyAsString());
        for (String field : new String[] {"heat-stack-id", "orchestration-status", "contrail-service-instance-fqdn"}) {
            Assert.assertTrue(field + " must be present", patch.has(field));
            Assert.assertTrue(field + " must be JSON null", patch.isNull(field));
        }
        Assert.assertNull(getVariableFromHistory(key, "WorkflowException"));
        assertActivity(key, "EndEvent_4", 1);
    }

    private String invokeRequest(String request) {
        String key = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        variables.put("mso-request-id", key);
        variables.put("UpdateAAIVfModuleRequest", request);
        invokeSubProcess("UpdateAAIVfModule", key, variables);
        Assert.assertTrue(isProcessEnded(key));
        return key;
    }
}
