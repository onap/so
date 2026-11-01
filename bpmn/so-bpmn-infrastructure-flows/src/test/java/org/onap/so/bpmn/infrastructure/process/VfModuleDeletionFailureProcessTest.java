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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.common.verification.WorkflowPersistenceSupport;
import org.onap.so.bpmn.core.WorkflowException;

public class VfModuleDeletionFailureProcessTest extends WorkflowPersistenceSupport {
    private static final String PROCESS = "DoDeleteVfModule";
    private static final String RESPONSE = "DoDVfMod_doDeleteVfModuleResponse";
    private final Map<String, Object> variables = new HashMap<>();

    @Before
    public void deployTheShippedDeletionProcess() throws Exception {
        String path = "subprocess/" + PROCESS + ".bpmn";
        URL resource = getClass().getClassLoader().getResource(path);
        assertNotNull(resource);
        String location = resource.toExternalForm();
        assertTrue(location,
                location.contains("/so-bpmn-infrastructure-flows/target/classes/")
                        || (location.matches("jar:.*/so-bpmn-infrastructure-flows-[^/]+\\.jar!/" + path)
                                && !location.contains("-tests.jar!/")));
        try (InputStream source = resource.openStream()) {
            engine.getRepositoryService().createDeployment().addInputStream(path, source).deploy();
        }
        ProcessDefinition definition = engine.getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionKey(PROCESS).singleResult();
        try (InputStream original = resource.openStream();
                InputStream deployed = engine.getRepositoryService().getResourceAsStream(definition.getDeploymentId(),
                        definition.getResourceName())) {
            assertArrayEquals(original.readAllBytes(), deployed.readAllBytes());
        }
    }

    @Test
    public void absentAdapterResponseEndsBeforeInventoryCleanup() {
        rejected("Empty response from VNF Adapter");
    }

    @Test
    public void explicitNullAdapterResponseEndsBeforeInventoryCleanup() {
        variables.put(RESPONSE, null);
        rejected("Empty response from VNF Adapter");
    }

    @Test
    public void emptyAdapterResponseEndsBeforeInventoryCleanup() {
        variables.put(RESPONSE, "");
        rejected("VNF Adapter Error");
    }

    @Test
    public void aDifferentAdapterOperationEndsBeforeInventoryCleanup() {
        variables.put(RESPONSE, "<createVfModuleResponse/>");
        rejected("VNF Adapter Error");
    }

    @Test
    public void malformedOutputsEndBeforePolicyAddressOrModuleCleanup() {
        variables.put(RESPONSE,
                "<deleteVfModuleResponse><vfModuleOutputs><entry></vfModuleOutputs></deleteVfModuleResponse>");
        rejected("Internal Error Occured in PostProcess Method");
    }

    private void rejected(String message) {
        ProcessInstance process = engine.getRuntimeService().createProcessInstanceByKey(PROCESS).setVariables(variables)
                .startBeforeActivity("PostProcessVNFAdapterRequest").execute();
        assertEquals(0,
                engine.getRuntimeService().createProcessInstanceQuery().processInstanceId(process.getId()).count());
        assertEquals(1, engine.getHistoryService().createHistoricProcessInstanceQuery()
                .processInstanceId(process.getId()).finished().count());
        Object restored = engine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(process.getId()).variableName("WorkflowException").singleResult().getValue();
        assertTrue(restored instanceof WorkflowException);
        WorkflowException failure = (WorkflowException) restored;
        assertEquals(PROCESS, failure.getProcessKey());
        assertEquals(1002, failure.getErrorCode());
        assertEquals(message, failure.getErrorMessage());
        assertEquals(1, engine.getHistoryService().createHistoricActivityInstanceQuery()
                .processInstanceId(process.getId()).activityId("PostProcessVNFAdapterRequest").finished().count());
        for (String activity : new String[] {"DeleteNetworkPoliciesFromAAI", "PreProcessUpdateAAIGenericVnf",
                "UpdateAAIGenericVnfTask", "UpdateAAIVfModulePrep", "InvokeUpdateAAIVfModule", "SDNCAdapterPrep2",
                "InvokeSDNCAdapterV1_2", "DeleteAAIVfModulePrep", "InvokeDeleteAAIVfModule", "EndEvent_9"}) {
            assertEquals(activity, 0, engine.getHistoryService().createHistoricActivityInstanceQuery()
                    .processInstanceId(process.getId()).activityId(activity).count());
        }
        assertEquals(1, engine.getHistoryService().createHistoricProcessInstanceQuery().count());
    }
}
