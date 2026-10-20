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
 * Unit tests for UpdateAAIGenericVnf bpmn.
 */

public class UpdateAAIGenericVnfIT extends BaseIntegrationTest {

    Logger logger = LoggerFactory.getLogger(UpdateAAIGenericVnfIT.class);


    /**
     * Test the happy path through the flow.
     */
    @Test

    public void happyPath() {
        logStart();

        String updateAAIGenericVnfRequest =
                FileUtil.readResourceFile("__files/VfModularity/UpdateAAIGenericVnfRequest.xml");
        stubQuery(200, inventoryJson());
        stubUpdate(200);

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        variables.put("mso-request-id", UUID.randomUUID().toString());
        variables.put("isDebugLogEnabled", "true");
        variables.put("UpdateAAIGenericVnfRequest", updateAAIGenericVnfRequest);
        invokeSubProcess("UpdateAAIGenericVnf", businessKey, variables);

        Assert.assertTrue(isProcessEnded(businessKey));
        String response = (String) getVariableFromHistory(businessKey, "UAAIGenVnf_updateGenericVnfResponse");
        Integer responseCode = (Integer) getVariableFromHistory(businessKey, "UAAIGenVnf_updateGenericVnfResponseCode");
        logger.debug("Subflow response code: {}", responseCode);
        logger.debug("Subflow response: {}", response);
        Assert.assertEquals(200, responseCode.intValue());
        Assert.assertNull(getVariableFromHistory(businessKey, "WorkflowException"));
        wireMockServer.verify(1,
                getRequestedFor(urlPathMatching(resourcePath())).withHeader("Accept", equalTo("application/json")));
        wireMockServer.verify(1,
                postRequestedFor(urlPathMatching(resourcePath())).withHeader("X-HTTP-Method-Override", equalTo("PATCH"))
                        .withHeader("Content-Type", equalTo("application/merge-patch+json"))
                        .withRequestBody(matchingJsonPath("$['vnf-id']", equalTo("skask"))));
        assertActivity(businessKey, "UpdateGenericVNF", 1);
        assertActivity(businessKey, "EndEvent_4", 1);
        assertActivity(businessKey, "AAIQueryFailure", 0);


        logEnd();
    }

    /**
     * Test the happy path through the flow.
     */
    @Test

    public void personaMismatch() {

        logStart();

        String updateAAIGenericVnfRequest =
                FileUtil.readResourceFile("__files/VfModularity/UpdateAAIGenericVnfRequest.xml");
        updateAAIGenericVnfRequest = updateAAIGenericVnfRequest.replaceFirst("introvert", "extrovert");

        stubQuery(200, inventoryJson());

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        variables.put("mso-request-id", UUID.randomUUID().toString());
        variables.put("isDebugLogEnabled", "true");
        variables.put("UpdateAAIGenericVnfRequest", updateAAIGenericVnfRequest);
        invokeSubProcess("UpdateAAIGenericVnf", businessKey, variables);

        Assert.assertTrue(isProcessEnded(businessKey));
        WorkflowException workflowException =
                (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");
        logger.debug("Workflow Exception: {}", workflowException);
        Assert.assertNotNull(workflowException);
        assertError(businessKey, 1002);
        wireMockServer.verify(1, getRequestedFor(urlPathMatching(resourcePath())));
        wireMockServer.verify(0, postRequestedFor(urlPathMatching(resourcePath())));
        assertActivity(businessKey, "EndEvent_4", 0);

        logEnd();
    }

    /**
     * Test the case where the GET to AAI returns a 404.
     */
    @Test

    public void badGet() {

        logStart();

        String updateAAIGenericVnfRequest =
                FileUtil.readResourceFile("__files/VfModularity/UpdateAAIGenericVnfRequest.xml");

        stubQuery(404, "");

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        variables.put("mso-request-id", UUID.randomUUID().toString());
        variables.put("isDebugLogEnabled", "true");
        variables.put("UpdateAAIGenericVnfRequest", updateAAIGenericVnfRequest);
        invokeSubProcess("UpdateAAIGenericVnf", businessKey, variables);

        Assert.assertTrue(isProcessEnded(businessKey));
        String response = (String) getVariableFromHistory(businessKey, "UAAIGenVnf_getGenericVnfResponse");
        Integer responseCode = (Integer) getVariableFromHistory(businessKey, "UAAIGenVnf_getGenericVnfResponseCode");
        logger.debug("Subflow response code: {}", responseCode);
        logger.debug("Subflow response: {}", response);
        Assert.assertEquals(404, responseCode.intValue());
        wireMockServer.verify(0, postRequestedFor(urlPathMatching(resourcePath())));
        assertActivity(businessKey, "AAIQueryFailure", 1);
        assertActivity(businessKey, "UpdateGenericVNF", 0);
        assertError(businessKey, 5000);

        logEnd();
    }

    /**
     * Test the case where the GET to AAI is successful, but he subsequent PUT returns 404.
     */
    @Test

    public void badPatch() {

        logStart();

        String updateAAIGenericVnfRequest =
                FileUtil.readResourceFile("__files/VfModularity/UpdateAAIGenericVnfRequest.xml");

        stubQuery(200, inventoryJson());
        stubUpdate(404);

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        variables.put("mso-request-id", UUID.randomUUID().toString());
        variables.put("isDebugLogEnabled", "true");
        variables.put("UpdateAAIGenericVnfRequest", updateAAIGenericVnfRequest);
        invokeSubProcess("UpdateAAIGenericVnf", businessKey, variables);

        Assert.assertTrue(isProcessEnded(businessKey));
        String response = (String) getVariableFromHistory(businessKey, "UAAIGenVnf_updateGenericVnfResponse");
        Integer responseCode = (Integer) getVariableFromHistory(businessKey, "UAAIGenVnf_updateGenericVnfResponseCode");
        logger.debug("Subflow response code: {}", responseCode);
        logger.debug("Subflow response: {}", response);
        Assert.assertEquals(500, responseCode.intValue());
        wireMockServer.verify(1, postRequestedFor(urlPathMatching(resourcePath())).withHeader("X-HTTP-Method-Override",
                equalTo("PATCH")));
        assertActivity(businessKey, "EndEvent_4", 0);
        assertError(businessKey, 5000);

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
        variables.put("UpdateAAIGenericVnfRequest",
                FileUtil.readResourceFile("__files/VfModularity/UpdateAAIGenericVnfRequest.xml"));
        invokeSubProcess("UpdateAAIGenericVnf", key, variables);
        Assert.assertTrue(isProcessEnded(key));
        Assert.assertEquals(Integer.valueOf(classifiedStatus),
                getVariableFromHistory(key, "UAAIGenVnf_getGenericVnfResponseCode"));
        Assert.assertNull(getVariableFromHistory(key, "UAAIGenVnf_updateGenericVnfResponseCode"));
        wireMockServer.verify(1, getRequestedFor(urlPathMatching(resourcePath())));
        wireMockServer.verify(0, postRequestedFor(urlPathMatching(resourcePath())));
        assertActivity(key, "AAIQueryFailure", 1);
        assertActivity(key, "UpdateGenericVNF", 0);
        assertActivity(key, "EndEvent_4", 0);
        assertError(key, 5000);
    }

    private void assertError(String key, int code) {
        Object error = getVariableFromHistory(key, "WorkflowException");
        Assert.assertTrue(error instanceof WorkflowException);
        Assert.assertEquals(code, ((WorkflowException) error).getErrorCode());
    }

    private String resourcePath() {
        return "/aai/v[0-9]+/network/generic-vnfs/generic-vnf/skask";
    }

    private String inventoryJson() {
        return "{\"vnf-id\":\"skask\",\"model-invariant-id\":\"introvert\",\"resource-version\":\"0000020\"}";
    }

    private void stubQuery(int status, String body) {
        wireMockServer.stubFor(get(urlPathMatching(resourcePath())).withQueryParam("depth", equalTo("1")).willReturn(
                aResponse().withStatus(status).withHeader("Content-Type", "application/json").withBody(body)));
    }

    private void stubUpdate(int status) {
        wireMockServer.stubFor(post(urlPathMatching(resourcePath()))
                .withHeader("X-HTTP-Method-Override", equalTo("PATCH")).willReturn(aResponse().withStatus(status)));
    }

    private void assertActivity(String key, String activity, long expected) {
        String id = historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey(key)
                .processDefinitionKey("UpdateAAIGenericVnf").singleResult().getId();
        Assert.assertEquals(activity, expected, historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(id).activityId(activity).finished().count());
    }

    @Test
    public void requestedAddressesAndStatusRetainTheirWireValues() {
        stubQuery(200, inventoryJson());
        stubUpdate(200);
        String key = invokeRequest(
                "<UpdateAAIGenericVnfRequest><vnf-id>skask</vnf-id>" + "<ipv4-oam-address>192.0.2.17</ipv4-oam-address>"
                        + "<management-v6-address>2001:db8::17</management-v6-address>"
                        + "<orchestration-status>active</orchestration-status></UpdateAAIGenericVnfRequest>");
        wireMockServer.verify(1,
                postRequestedFor(urlPathMatching(resourcePath()))
                        .withRequestBody(matchingJsonPath("$['ipv4-oam-address']", equalTo("192.0.2.17")))
                        .withRequestBody(matchingJsonPath("$['management-v6-address']", equalTo("2001:db8::17")))
                        .withRequestBody(matchingJsonPath("$['orchestration-status']", equalTo("active")))
                        .withRequestBody(matchingJsonPath("$['persona-model-version']", absent())));
        Assert.assertNull(getVariableFromHistory(key, "WorkflowException"));
        assertActivity(key, "EndEvent_4", 1);
    }

    @Test
    public void deleteSentinelsBecomeExplicitNullMergePatchValues() {
        stubQuery(200, inventoryJson());
        stubUpdate(200);
        String key = invokeRequest(
                "<UpdateAAIGenericVnfRequest><vnf-id>skask</vnf-id>" + "<ipv4-oam-address>DELETE</ipv4-oam-address>"
                        + "<management-v6-address>DELETE</management-v6-address>"
                        + "<orchestration-status>DELETE</orchestration-status></UpdateAAIGenericVnfRequest>");
        wireMockServer.verify(1, postRequestedFor(urlPathMatching(resourcePath())).withHeader("X-HTTP-Method-Override",
                equalTo("PATCH")));
        JSONObject patch = new JSONObject(
                wireMockServer.findAll(postRequestedFor(urlPathMatching(resourcePath()))).get(0).getBodyAsString());
        for (String field : new String[] {"ipv4-oam-address", "management-v6-address", "orchestration-status"}) {
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
        variables.put("UpdateAAIGenericVnfRequest", request);
        invokeSubProcess("UpdateAAIGenericVnf", key, variables);
        Assert.assertTrue(isProcessEnded(key));
        return key;
    }
}
