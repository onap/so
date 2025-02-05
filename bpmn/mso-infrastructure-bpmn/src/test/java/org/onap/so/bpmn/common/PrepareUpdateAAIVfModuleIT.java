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

import org.junit.Assert;
import org.junit.Test;
import org.onap.so.BaseIntegrationTest;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.mock.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static org.onap.so.bpmn.mock.StubResponseAAI.*;


/**
 * Unit tests for PrepareUpdateAAIVfModule.bpmn.
 */

public class PrepareUpdateAAIVfModuleIT extends BaseIntegrationTest {

    Logger logger = LoggerFactory.getLogger(PrepareUpdateAAIVfModuleIT.class);

    /**
     * Test the happy path through the flow.
     */
    @Test

    public void happyPath() throws IOException {

        logStart();

        String prepareUpdateAAIVfModuleRequest =
                FileUtil.readResourceFile("__files/VfModularity/PrepareUpdateAAIVfModuleRequest.xml");

        MockGetGenericVnfByIdWithDepth(wireMockServer, "skask", 1, "VfModularity/GenericVnf.xml");
        MockPutGenericVnf(wireMockServer, "/skask/vf-modules/vf-module/supercool", "PCRF", 200);
        MockPatchVfModuleId(wireMockServer, "skask", "supercool");

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

        logEnd();
    }

    /**
     * Test the case where the GET to AAI returns a 404.
     */
    @Test

    public void badGet() throws IOException {

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

    public void failValidation1() throws IOException {

        logStart();

        String prepareUpdateAAIVfModuleRequest =
                FileUtil.readResourceFile("__files/VfModularity/PrepareUpdateAAIVfModuleRequest.xml")
                        .replaceFirst("supercool", "lukewarm");

        MockGetGenericVnfByIdWithDepth(wireMockServer, "skask", 1, "VfModularity/GenericVnf.xml");

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

    public void failValidation2() throws IOException {

        logStart();

        String prepareUpdateAAIVfModuleRequest =
                FileUtil.readResourceFile("__files/VfModularity/PrepareUpdateAAIVfModuleRequest.xml")
                        .replaceFirst("supercool", "notsocool");

        MockGetGenericVnfByIdWithDepth(wireMockServer, "skask", 1, "VfModularity/GenericVnf.xml");

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

        logEnd();
    }

    /**
     * Test the case where the GET to AAI is successful, but the subsequent PUT returns 404.
     */
    @Test

    public void badPatch() throws IOException {

        logStart();

        String prepareUpdateAAIVfModuleRequest =
                FileUtil.readResourceFile("__files/VfModularity/PrepareUpdateAAIVfModuleRequest.xml");

        MockGetGenericVnfByIdWithDepth(wireMockServer, "skask", 1, "VfModularity/GenericVnf.xml");
        MockAAIVfModuleBadPatch(wireMockServer,
                "/aai/v[0-9]+/network/generic-vnfs/generic-vnf/skask/vf-modules/vf-module/supercool", 404);

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
}

