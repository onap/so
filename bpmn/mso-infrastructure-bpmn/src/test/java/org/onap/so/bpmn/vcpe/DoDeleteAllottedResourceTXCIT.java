/*
 * ============LICENSE_START======================================================= ONAP - SO
 * ================================================================================ Copyright (C) 2017 AT&T Intellectual
 * Property. All rights reserved. ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.vcpe;

import org.junit.Assert;
import org.junit.Test;
import org.onap.so.bpmn.common.BPMNUtil;
import org.onap.so.bpmn.mock.FileUtil;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.onap.so.bpmn.mock.StubResponseAAI.*;
import static org.onap.so.bpmn.mock.StubResponseDatabase.mockUpdateRequestDB;
import static org.onap.so.bpmn.mock.StubResponseSDNCAdapter.mockSDNCAdapter;


public class DoDeleteAllottedResourceTXCIT extends AbstractTestBase {

    private static final String PROCNAME = "DoDeleteAllottedResourceTXC";
    private final CallbackSet callbacks = new CallbackSet();

    public DoDeleteAllottedResourceTXCIT() {
        callbacks.put("deactivate",
                FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyDeactivateCallback.xml"));
        callbacks.put("deactivateNF",
                FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyDeactivateCallbackNotFound.xml"));
        callbacks.put("delete", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyDeleteCallback.xml"));
        callbacks.put("unassign", FileUtil.readResourceFile("__files/VfModularity/SDNCTopologyUnassignCallback.xml"));
    }

    @Test
    public void testDoDeleteAllottedResourceTXC_Success() throws Exception {
        logStart();
        MockQueryAllottedResourceById(wireMockServer, ARID, "GenericFlows/getARUrlById.xml");
        MockGetAllottedResource(wireMockServer, CUST, SVC, INST, ARID,
                "VCPE/DoDeleteAllottedResourceTXC/arGetById.xml");
        MockPatchAllottedResource(wireMockServer, CUST, SVC, INST, ARID);
        MockDeleteAllottedResource(wireMockServer, CUST, SVC, INST, ARID, ARVERS);
        mockSDNCAdapter(wireMockServer, 200);
        mockUpdateRequestDB(wireMockServer, 200, "Database/DBUpdateResponse.xml");

        Map<String, Object> variables = new HashMap<>();
        setVariablesSuccess(variables);

        String processId = invokeSubProcess(PROCNAME, variables);

        injectSDNCCallbacks(callbacks, "deactivate");
        injectSDNCCallbacks(callbacks, "delete");
        injectSDNCCallbacks(callbacks, "unassign");

        BPMNUtil.waitForWorkflowToFinish(processEngine, processId);

        Assert.assertTrue(isProcessEndedByProcessInstanceId(processId));
        String workflowException = BPMNUtil.getVariable(processEngine, PROCNAME, "WorkflowException", processId);
        System.out.println("workflowException:\n" + workflowException);
        assertNull(workflowException);
        logEnd();
    }

    @Test
    public void testDoDeleteAllottedResourceTXC_ARNotInSDNC() throws Exception {
        logStart();
        MockQueryAllottedResourceById(wireMockServer, ARID, "GenericFlows/getARUrlById.xml");
        MockGetAllottedResource(wireMockServer, CUST, SVC, INST, ARID,
                "VCPE/DoDeleteAllottedResourceTXC/arGetById.xml");
        MockPatchAllottedResource(wireMockServer, CUST, SVC, INST, ARID);
        MockDeleteAllottedResource(wireMockServer, CUST, SVC, INST, ARID, ARVERS);
        mockSDNCAdapter(wireMockServer, 200);
        mockUpdateRequestDB(wireMockServer, 200, "Database/DBUpdateResponse.xml");

        Map<String, Object> variables = new HashMap<>();
        setVariablesSuccess(variables);

        variables.put("failNotFound", "false");

        String processId = invokeSubProcess(PROCNAME, variables);

        injectSDNCCallbacks(callbacks, "deactivateNF");

        BPMNUtil.waitForWorkflowToFinish(processEngine, processId);

        Assert.assertTrue(isProcessEndedByProcessInstanceId(processId));
        logEnd();
    }


    @Test
    public void testDoDeleteAllottedResourceTXC_SubProcessError() throws Exception {
        logStart();
        MockQueryAllottedResourceById(wireMockServer, ARID, "GenericFlows/getARUrlById.xml");
        MockGetAllottedResource(wireMockServer, CUST, SVC, INST, ARID,
                "VCPE/DoDeleteAllottedResourceTXC/arGetById.xml");
        MockPatchAllottedResource(wireMockServer, CUST, SVC, INST, ARID);
        MockDeleteAllottedResource(wireMockServer, CUST, SVC, INST, ARID, ARVERS);
        mockUpdateRequestDB(wireMockServer, 200, "Database/DBUpdateResponse.xml");

        mockSDNCAdapter(wireMockServer, 500);

        Map<String, Object> variables = new HashMap<>();
        setVariablesSuccess(variables);

        String processId = invokeSubProcess(PROCNAME, variables);

        BPMNUtil.waitForWorkflowToFinish(processEngine, processId);

        Assert.assertTrue(isProcessEndedByProcessInstanceId(processId));
        String workflowException = BPMNUtil.getVariable(processEngine, PROCNAME, "WorkflowException", processId);
        System.out.println("workflowException:\n" + workflowException);
        assertNotNull(workflowException);
        logEnd();
    }

    private void setVariablesSuccess(Map<String, Object> variables) {
        variables.put("isDebugLogEnabled", "true");
        variables.put("failNotFound", "true");
        variables.put("msoRequestId", "testRequestId1");
        variables.put("mso-request-id", "requestId");
        variables.put("allottedResourceId", ARID);

        variables.put("serviceInstanceId", DEC_INST);
        variables.put("parentServiceInstanceId", DEC_PARENT_INST);
    }

}
