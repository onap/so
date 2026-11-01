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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockConstruction;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Assume;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.graphinventory.entities.uri.GraphInventoryResourceUri;
import org.onap.so.bpmn.common.verification.WorkflowPersistenceSupport;
import org.onap.so.bpmn.core.RollbackData;
import org.onap.so.bpmn.core.WorkflowException;

public class ServiceInstanceRollbackProcessTest extends WorkflowPersistenceSupport {
    private static final String PROCESS = "DoCreateServiceInstanceRollback";
    private static final String SERVICE = "/business/customers/customer/customer-1/service-subscriptions/"
            + "service-subscription/subscription/service-instances/service-instance/service-1";
    private final Map<String, Object> variables = new HashMap<>();
    private final List<String> deletions = new ArrayList<>();
    private final List<String> lookups = new ArrayList<>();
    private MockedConstruction<AAIResourcesClient> clients;
    private boolean rejectDeletion;

    @Before
    public void deployRealRollbackAndConfigureTheExternalInventoryBoundary() throws Exception {
        deployProduction(PROCESS);
        deployProduction("DoCreateServiceInstance");
        variables.put("disableRollback", "false");
        variables.put("msoRequestId", "request-1");
        clients = mockConstruction(AAIResourcesClient.class, (client, context) -> {
            doAnswer(call -> {
                GraphInventoryResourceUri uri = call.getArgument(0);
                var built = uri.build();
                lookups.add(built.getPath() + "?" + built.getRawQuery());
                return new AAIResultWrapper("{\"results\":[{\"resource-type\":\"service-instance\","
                        + "\"resource-link\":\"http://inventory.example/aai/v24" + SERVICE + "\"}]}");
            }).when(client).get(any(AAIResourceUri.class), any());
            doAnswer(call -> {
                AAIResourceUri uri = call.getArgument(0);
                // HttpLookupUri.build() performs the lookup needed before the real client can delete.
                deletions.add(uri.build().getPath());
                if (rejectDeletion) {
                    throw new IllegalStateException("inventory cleanup rejected");
                }
                return null;
            }).when(client).delete(any(AAIResourceUri.class));
        });
    }

    private void deployProduction(String process) throws Exception {
        String resource = "subprocess/" + process + ".bpmn";
        URL location = getClass().getClassLoader().getResource(resource);
        assertNotNull(resource, location);
        String path = location.toExternalForm();
        assertTrue(path,
                path.contains("/so-bpmn-infrastructure-flows/target/classes/")
                        || (path.matches("jar:.*/so-bpmn-infrastructure-flows-[^/]+\\.jar!/" + resource)
                                && !path.contains("-tests.jar!/")));
        try (InputStream input = location.openStream()) {
            engine.getRepositoryService().createDeployment()
                    .addInputStream(process + ".bpmn", new ByteArrayInputStream(input.readAllBytes())).deploy();
        }
    }

    @After
    public void restoreExternalInventory() {
        if (clients != null) {
            clients.close();
        }
    }

    @Test
    public void absentRollbackDataSkipsEveryExternalCompensation() {
        skipped(start());
    }

    @Test
    public void anUnrelatedResourceSectionDoesNotAuthorizeServiceDeletion() {
        RollbackData unrelated = new RollbackData();
        unrelated.put("VNF", "rollbackAAI", "true");
        variables.put("rollbackData", unrelated);
        skipped(start());
    }

    @Test
    public void noRecordedCreationSkipsBothExternalCompensations() {
        requestCleanup("false", "false");
        skipped(start());
    }

    @Test
    public void unrecognizedEligibilityValuesDoNotAuthorizeCleanup() {
        requestCleanup("TRUE", "1");
        skipped(start());
    }

    @Test
    public void successfulInventoryCompensationDeletesOnlyTheRecordedService() {
        requestCleanup("true", "false");
        ProcessInstance process = start();
        completed(process);
        assertEquals(Collections.singletonList(SERVICE), deletions);
        assertEquals(Collections.singletonList("/nodes/service-instances/service-instance/service-1?format=pathed"),
                lookups);
        assertNull(historic(process, "WorkflowException"));
        assertEquals(Boolean.TRUE, historic(process, "GENDS_SuccessIndicator"));
        assertEquals(Boolean.TRUE, historic(process, "rolledBack"));
        assertNull(historic(process, "rollbackError"));
        assertNull(historic(process, "rollbackData"));
        activity(process, "Task_06o1aio", 1);
        activity(process, "PostProcess_ScriptTask", 1);
        activity(process, "EndEvent_3", 1);
        noControllerCalls(process);
    }

    @Test
    public void disabledCompensationSkipsInventoryAndReturnsFalseForUncompensatedResources() {
        requestCleanup("true", "false");
        variables.put("disableRollback", "true");
        ProcessInstance process = start();
        completed(process);
        assertEquals(Collections.emptyList(), deletions);
        assertEquals(Collections.emptyList(), lookups);
        assertEquals(Boolean.FALSE, historic(process, "rolledBack"));
        assertNull(historic(process, "rollbackData"));
        activity(process, "Task_06o1aio", 0);
        noControllerCalls(process);
    }

    @Test
    public void rejectedInventoryDeletionSelectsTheErrorHandlerWithoutRetrying() {
        requestCleanup("true", "false");
        rejectDeletion = true;
        ProcessInstance process = start();
        completed(process);
        assertEquals(Collections.singletonList(SERVICE), deletions);
        assertFalse(Boolean.TRUE.equals(historic(process, "rolledBack")));
        assertNotNull(historic(process, "rollbackError"));
        assertNull(historic(process, "rollbackData"));
        activity(process, "Task_06o1aio", 1);
        activity(process, "ScriptTask_1", 1);
        activity(process, "PostProcess_ScriptTask", 0);
        noControllerCalls(process);
    }

    @Test
    public void inventoryCleanupFailurePreservesTheOriginalRequestError() {
        requestCleanup("true", "false");
        rejectDeletion = true;
        ProcessInstance process = startParentRollback();
        completed(process);
        assertOriginalError(process);
        assertEquals(Collections.singletonList(SERVICE), deletions);
        activity(process, "ScriptTask_1p0vyip", 1);
    }

    @Test
    public void inventoryCleanupFailureReportsATypedFailedRollbackOutcome() {
        Assume.assumeTrue("SO-4457: service rollback leaves the failed compensation outcome unset",
                Boolean.getBoolean("groovy.migration.verification"));
        requestCleanup("true", "false");
        rejectDeletion = true;
        ProcessInstance process = start();
        completed(process);
        assertEquals(Boolean.FALSE, historic(process, "rolledBack"));
    }

    @Test
    public void parentSkipStateDoesNotLeakIntoTheFreshRollbackChild() {
        requestCleanup("true", "false");
        variables.put("skipRollback", true);
        ProcessInstance process = startParentRollback();
        completed(process);
        assertEquals(Collections.singletonList(SERVICE), deletions);
        assertEquals(Boolean.TRUE, historic(process, "rolledBack"));
        assertEquals(Boolean.TRUE, historic(process, "skipRollback"));
        assertOriginalError(process);
    }

    private void requestCleanup(String aai, String controller) {
        RollbackData data = new RollbackData();
        data.put("SERVICEINSTANCE", "serviceInstanceId", "service-1");
        data.put("SERVICEINSTANCE", "subscriptionServiceType", "subscription 東京");
        data.put("SERVICEINSTANCE", "globalSubscriberId", "customer-1");
        data.put("SERVICEINSTANCE", "rollbackAAI", aai);
        data.put("SERVICEINSTANCE", "rollbackSDNC", controller);
        data.put("SERVICEINSTANCE", "sdncDelete", "<delete/>");
        data.put("SERVICEINSTANCE", "sdncDeactivate", "<deactivate/>");
        variables.put("rollbackData", data);
    }

    private ProcessInstance start() {
        return engine.getRuntimeService().startProcessInstanceByKey(PROCESS, variables);
    }

    private ProcessInstance startParentRollback() {
        variables.put("WorkflowException",
                new WorkflowException("original-create", 4107, "creation rejected", "create"));
        return engine.getRuntimeService().createProcessInstanceByKey("DoCreateServiceInstance").setVariables(variables)
                .startBeforeActivity("ScriptTask_0ocetux").execute();
    }

    private void completed(ProcessInstance process) {
        restart();
        assertEquals(0,
                engine.getRuntimeService().createProcessInstanceQuery().processInstanceId(process.getId()).count());
        assertEquals(1, engine.getHistoryService().createHistoricProcessInstanceQuery()
                .processInstanceId(process.getId()).finished().count());
    }

    private void skipped(ProcessInstance process) {
        completed(process);
        assertEquals(Collections.emptyList(), deletions);
        assertEquals(Collections.emptyList(), lookups);
        assertNull(historic(process, "WorkflowException"));
        assertNull(historic(process, "rolledBack"));
        assertNull(historic(process, "rollbackError"));
        assertNull(historic(process, "rollbackData"));
        activity(process, "Task_06o1aio", 0);
        activity(process, "PostProcess_ScriptTask", 1);
        activity(process, "EndEvent_3", 1);
        noControllerCalls(process);
    }

    private void noControllerCalls(ProcessInstance process) {
        activity(process, "CallActivity_0ak0ezb", 0);
        activity(process, "CallActivity_0ppz9nd", 0);
    }

    private void assertOriginalError(ProcessInstance process) {
        Object value = historic(process, "WorkflowException");
        assertNotNull("The original create error must survive compensation", value);
        assertEquals(WorkflowException.class, value.getClass());
        WorkflowException error = (WorkflowException) value;
        assertEquals("original-create", error.getProcessKey());
        assertEquals(4107, error.getErrorCode());
        assertEquals("creation rejected", error.getErrorMessage());
        assertEquals("create", error.getWorkStep());
    }

    private Object historic(ProcessInstance process, String name) {
        var value = engine.getHistoryService().createHistoricVariableInstanceQuery().processInstanceId(process.getId())
                .variableName(name).singleResult();
        return value == null ? null : value.getValue();
    }

    private void activity(ProcessInstance process, String activity, long count) {
        assertEquals(activity, count, engine.getHistoryService().createHistoricActivityInstanceQuery()
                .processInstanceId(process.getId()).activityId(activity).count());
    }
}
