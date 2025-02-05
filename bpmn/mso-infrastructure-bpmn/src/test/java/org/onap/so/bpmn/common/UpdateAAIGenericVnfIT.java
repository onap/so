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
 * Unit tests for UpdateAAIGenericVnf bpmn.
 */

public class UpdateAAIGenericVnfIT extends BaseIntegrationTest {

    Logger logger = LoggerFactory.getLogger(CreateAAIVfModuleIT.class);


    /**
     * Test the happy path through the flow.
     */
    @Test

    public void happyPath() throws IOException {
        logStart();

        String updateAAIGenericVnfRequest =
                FileUtil.readResourceFile("__files/VfModularity/UpdateAAIGenericVnfRequest.xml");
        MockGetGenericVnfByIdWithDepth(wireMockServer, "skask", 1, "VfModularity/GenericVnf.xml");
        MockPutGenericVnf(wireMockServer, "/skask", 200);
        MockPatchGenericVnf(wireMockServer, "skask");

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

        logEnd();
    }

    /**
     * Test the happy path through the flow.
     */
    @Test

    public void personaMismatch() throws IOException {

        logStart();

        String updateAAIGenericVnfRequest =
                FileUtil.readResourceFile("__files/VfModularity/UpdateAAIGenericVnfRequest.xml");
        updateAAIGenericVnfRequest = updateAAIGenericVnfRequest.replaceFirst("introvert", "extrovert");

        MockGetGenericVnfByIdWithDepth(wireMockServer, "skask", 1, "VfModularity/GenericVnf.xml");

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

        logEnd();
    }

    /**
     * Test the case where the GET to AAI returns a 404.
     */
    @Test

    public void badGet() throws IOException {

        logStart();

        String updateAAIGenericVnfRequest =
                FileUtil.readResourceFile("__files/VfModularity/UpdateAAIGenericVnfRequest.xml");

        MockGetGenericVnfById_404(wireMockServer, "skask[?]depth=1");

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

        logEnd();
    }

    /**
     * Test the case where the GET to AAI is successful, but he subsequent PUT returns 404.
     */
    @Test

    public void badPatch() throws IOException {

        logStart();

        String updateAAIGenericVnfRequest =
                FileUtil.readResourceFile("__files/VfModularity/UpdateAAIGenericVnfRequest.xml");

        MockGetGenericVnfByIdWithDepth(wireMockServer, "skask", 1, "VfModularity/GenericVnf.xml");
        MockPutGenericVnf_Bad(wireMockServer, "skask", 404);
        MockAAIVfModuleBadPatch(wireMockServer, "/aai/v[0-9]+/network/generic-vnfs/generic-vnf/skask", 404);

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
        Assert.assertEquals(404, responseCode.intValue());

        logEnd();
    }
}

