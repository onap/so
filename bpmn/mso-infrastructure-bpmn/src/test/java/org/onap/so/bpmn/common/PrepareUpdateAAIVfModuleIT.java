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
import static org.onap.so.bpmn.mock.StubResponseAAI.MockGetGenericVnfById_404;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import org.onap.so.BaseIntegrationTest;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.mock.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for PrepareUpdateAAIVfModule.bpmn.
 */

public class PrepareUpdateAAIVfModuleIT extends BaseIntegrationTest {

    Logger logger = LoggerFactory.getLogger(PrepareUpdateAAIVfModuleIT.class);

    /**
     * Test the happy path through the flow.
     */
    @Test

    public void happyPath() {

        logStart();

        String prepareUpdateAAIVfModuleRequest =
                FileUtil.readResourceFile("__files/VfModularity/PrepareUpdateAAIVfModuleRequest.xml");

        stubGenericVnfAndModule("supercool");
        stubModuleUpdate("supercool", 200);

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        variables.put("mso-request-id", UUID.randomUUID().toString());
        variables.put("isDebugLogEnabled", "true");
        variables.put("PrepareUpdateAAIVfModuleRequest", prepareUpdateAAIVfModuleRequest);
        invokeSubProcess("PrepareUpdateAAIVfModule", businessKey, variables);

        Assert.assertTrue(isProcessEnded(businessKey));
        String response = (String) getVariableFromHistory(businessKey, "PUAAIVfMod_updateVfModuleResponse");
        Integer responseCode = (Integer) getVariableFromHistory(businessKey, "PUAAIVfMod_updateVfModuleResponseCode");
        logger.debug("Subflow response code: {}", responseCode);
        logger.debug("Subflow response: {}", response);
        Assert.assertEquals(200, responseCode.intValue());
        String heatStackId = (String) getVariableFromHistory(businessKey, "PUAAIVfMod_heatStackId");
        logger.debug("Ouput heat-stack-id:{}", heatStackId);
        Assert.assertEquals("slowburn", heatStackId);
        Assert.assertEquals(Boolean.TRUE, getVariableFromHistory(businessKey, "PUAAIVfMod_vfModuleOK"));
        Assert.assertNull(getVariableFromHistory(businessKey, "WorkflowException"));
        wireMockServer.verify(1, getRequestedFor(urlPathMatching(genericVnfPath()))
                .withQueryParam("depth", equalTo("1")).withHeader("Accept", equalTo("application/json")));
        wireMockServer.verify(1,
                getRequestedFor(urlPathMatching(modulePath("supercool"))).withQueryParam("format", equalTo("count"))
                        .withQueryParam("resultIndex", equalTo("0")).withQueryParam("resultSize", equalTo("1")));
        wireMockServer.verify(1,
                postRequestedFor(urlPathMatching(modulePath("supercool")))
                        .withHeader("X-HTTP-Method-Override", equalTo("PATCH"))
                        .withHeader("Content-Type", equalTo("application/merge-patch+json"))
                        .withRequestBody(matchingJsonPath("$['vf-module-id']", equalTo("supercool")))
                        .withRequestBody(matchingJsonPath("$['orchestration-status']", equalTo("pending-delete")))
                        .withRequestBody(matchingJsonPath("$['heat-stack-id']", equalTo("slowburn")))
                        .withRequestBody(matchingJsonPath("$['resource-version']", equalTo("0000075"))));
        assertActivity(businessKey, "UpdateVfModule", 1);
        assertActivity(businessKey, "EndEvent_4", 1);
        assertActivity(businessKey, "ScriptTask_2", 0);

        logEnd();
    }

    /**
     * Test the case where the GET to AAI returns a 404.
     */
    @Test

    public void badGet() {

        logStart();

        String prepareUpdateAAIVfModuleRequest =
                FileUtil.readResourceFile("__files/VfModularity/PrepareUpdateAAIVfModuleRequest.xml");
        MockGetGenericVnfById_404(wireMockServer, "skask[?]depth=1");

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        variables.put("mso-request-id", UUID.randomUUID().toString());
        variables.put("isDebugLogEnabled", "true");
        variables.put("PrepareUpdateAAIVfModuleRequest", prepareUpdateAAIVfModuleRequest);
        invokeSubProcess("PrepareUpdateAAIVfModule", businessKey, variables);

        Assert.assertTrue(isProcessEnded(businessKey));
        String response = (String) getVariableFromHistory(businessKey, "PUAAIVfMod_getVnfResponse");
        Integer responseCode = (Integer) getVariableFromHistory(businessKey, "PUAAIVfMod_getVnfResponseCode");
        WorkflowException workflowException =
                (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");
        logger.debug("Subflow response code: {}", responseCode);
        logger.debug("Subflow response: {}", response);
        Assert.assertEquals(404, responseCode.intValue());
        Assert.assertNotNull(workflowException);
        logger.debug("Subflow WorkflowException error message: {}", workflowException.getErrorMessage());

        logEnd();
    }

    /**
     * Test the case where the validation of the VF Module fails.
     */
    @Test

    public void failValidation1() {

        logStart();

        String prepareUpdateAAIVfModuleRequest =
                FileUtil.readResourceFile("__files/VfModularity/PrepareUpdateAAIVfModuleRequest.xml")
                        .replaceFirst("supercool", "lukewarm");

        stubGenericVnfAndModule("lukewarm");
        stubModuleUpdate("lukewarm", 200);

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        variables.put("mso-request-id", UUID.randomUUID().toString());
        variables.put("isDebugLogEnabled", "true");
        variables.put("PrepareUpdateAAIVfModuleRequest", prepareUpdateAAIVfModuleRequest);
        invokeSubProcess("PrepareUpdateAAIVfModule", businessKey, variables);

        WorkflowException workflowException =
                (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");
        Assert.assertNotNull(workflowException);
        logger.debug("Subflow WorkflowException error message: {}", workflowException.getErrorMessage());
        Assert.assertTrue(workflowException.getErrorMessage().startsWith("VF Module validation error: Orchestration"));

        logEnd();
    }

    /**
     * Test the case where the validation of the VF Module fails.
     */
    @Test

    public void failValidation2() {

        logStart();

        String prepareUpdateAAIVfModuleRequest =
                FileUtil.readResourceFile("__files/VfModularity/PrepareUpdateAAIVfModuleRequest.xml")
                        .replaceFirst("supercool", "notsocool");

        stubGenericVnfAndModule("notsocool");

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        variables.put("mso-request-id", UUID.randomUUID().toString());
        variables.put("isDebugLogEnabled", "true");
        variables.put("PrepareUpdateAAIVfModuleRequest", prepareUpdateAAIVfModuleRequest);
        invokeSubProcess("PrepareUpdateAAIVfModule", businessKey, variables);

        WorkflowException workflowException =
                (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");
        Assert.assertNotNull(workflowException);
        logger.debug("Subflow WorkflowException error message: {}", workflowException.getErrorMessage());
        Assert.assertTrue(workflowException.getErrorMessage().startsWith("VF Module validation error: VF Module"));
        Assert.assertEquals(5000, workflowException.getErrorCode());
        Assert.assertEquals(Boolean.FALSE, getVariableFromHistory(businessKey, "PUAAIVfMod_vfModuleOK"));
        wireMockServer.verify(0, postRequestedFor(urlPathMatching(modulePath("notsocool"))));
        assertActivity(businessKey, "ScriptTask_2", 1);
        assertActivity(businessKey, "EndEvent_1", 1);
        assertActivity(businessKey, "UpdateVfModule", 0);

        logEnd();
    }

    /**
     * Test the case where the GET to AAI is successful, but the subsequent update returns 404.
     */
    @Test

    public void badPatch() {

        logStart();

        String prepareUpdateAAIVfModuleRequest =
                FileUtil.readResourceFile("__files/VfModularity/PrepareUpdateAAIVfModuleRequest.xml");

        stubGenericVnfAndModule("supercool");
        stubModuleUpdate("supercool", 404);

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<>();
        variables.put("mso-request-id", UUID.randomUUID().toString());
        variables.put("isDebugLogEnabled", "true");
        variables.put("PrepareUpdateAAIVfModuleRequest", prepareUpdateAAIVfModuleRequest);
        invokeSubProcess("PrepareUpdateAAIVfModule", businessKey, variables);

        Assert.assertTrue(isProcessEnded(businessKey));
        String response = (String) getVariableFromHistory(businessKey, "PUAAIVfMod_updateVfModuleResponse");
        Integer responseCode = (Integer) getVariableFromHistory(businessKey, "PUAAIVfMod_updateVfModuleResponseCode");
        WorkflowException workflowException =
                (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");
        logger.debug("Subflow response code: {}", responseCode);
        logger.debug("Subflow response: {}", response);
        Assert.assertEquals(404, responseCode.intValue());
        Assert.assertNotNull(workflowException);
        logger.debug("Subflow WorkflowException error message: {}", workflowException.getErrorMessage());

        logEnd();
    }

    private void stubGenericVnfAndModule(String moduleId) {
        wireMockServer.stubFor(get(urlPathMatching(genericVnfPath())).withQueryParam("depth", equalTo("1"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBodyFile("vf-module-update/generic-vnf.json")));
        boolean missing = "notsocool".equals(moduleId);
        wireMockServer.stubFor(get(urlPathMatching(modulePath(moduleId))).withQueryParam("format", equalTo("count"))
                .withQueryParam("resultIndex", equalTo("0")).withQueryParam("resultSize", equalTo("1"))
                .willReturn(aResponse().withStatus(missing ? 404 : 200)));
        if (!missing) {
            wireMockServer.stubFor(get(urlPathMatching(modulePath(moduleId))).withQueryParam("format", absent())
                    .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                            .withBodyFile("vf-module-update/" + moduleId + ".json")));
        }
    }

    private String genericVnfPath() {
        return "/aai/v[0-9]+/network/generic-vnfs/generic-vnf/skask";
    }

    private String modulePath(String moduleId) {
        return genericVnfPath() + "/vf-modules/vf-module/" + moduleId;
    }

    private void stubModuleUpdate(String moduleId, int status) {
        wireMockServer.stubFor(
                post(urlPathMatching(modulePath(moduleId))).withHeader("X-HTTP-Method-Override", equalTo("PATCH"))
                        .withHeader("Content-Type", equalTo("application/merge-patch+json"))
                        .willReturn(aResponse().withStatus(status)));
    }

    private void assertActivity(String businessKey, String activityId, long expected) {
        String processId = historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey(businessKey)
                .processDefinitionKey("PrepareUpdateAAIVfModule").singleResult().getId();
        Assert.assertEquals(activityId, expected, historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processId).activityId(activityId).finished().count());
    }
}

