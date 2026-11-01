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
package org.onap.so.bpmn.infrastructure.scripts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.so.bpmn.core.RollbackData;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.core.domain.NetworkResource;

public class E2EServiceRollbackContractTest {
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();
    private final WorkflowException original = new WorkflowException("original-request", 5001, "creation failed");
    private final DoCreateE2EServiceInstanceRollback create = new DoCreateE2EServiceInstanceRollback();
    private final DoUpdateE2EServiceInstanceRollback update = new DoUpdateE2EServiceInstanceRollback();

    @Before
    public void prepareExecution() {
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
        resetVariables();
    }

    @Test
    public void absentRollbackDataSkipsBothFamiliesWithoutClaimingSuccess() {
        for (boolean updating : new boolean[] {false, true}) {
            resetVariables();
            preprocess(updating);
            assertSame(Boolean.TRUE, variables.get("skipRollback"));
            assertSame(Boolean.FALSE, variables.get("rollbackAAI"));
            assertSame(original, variables.get("WorkflowException"));
            assertFalse(variables.containsKey("rolledBack"));
        }
    }

    @Test
    public void anUnrelatedRollbackSectionDoesNotAuthorizeServiceCleanup() {
        for (boolean updating : new boolean[] {false, true}) {
            resetVariables();
            RollbackData data = new RollbackData();
            data.put("VNF", "rollbackAAI", "true");
            variables.put("rollbackData", data);
            preprocess(updating);
            assertSame(Boolean.TRUE, variables.get("skipRollback"));
            assertSame(Boolean.FALSE, variables.get("rollbackAAI"));
        }
    }

    @Test
    public void creationUsesTypedFlagsAndRetainsTheControllerPayloads() {
        RollbackData data = data("true", "false", "false");
        data.put("SERVICEINSTANCE", "rollbackSDNC", "true");
        data.put("SERVICEINSTANCE", "sdncDelete", "<delete>service &amp; SO</delete>");
        data.put("SERVICEINSTANCE", "sdncDeactivate", "<deactivate/>");
        variables.put("rollbackData", data);
        create.preProcessRequest(execution);
        assertSame(Boolean.TRUE, variables.get("rollbackAAI"));
        assertSame(Boolean.TRUE, variables.get("rollbackSDNC"));
        assertFalse(Boolean.TRUE.equals(variables.get("skipRollback")));
        assertEquals("service-1", variables.get("serviceInstanceId"));
        assertEquals("customer-1", variables.get("globalSubscriberId"));
        assertEquals("subscription-1", variables.get("subscriptionServiceType"));
        assertEquals("<delete>service &amp; SO</delete>", variables.get("sdncDelete"));
        assertEquals("<deactivate/>", variables.get("sdncDeactivate"));
        assertSame(original, variables.get("WorkflowException"));
    }

    @Test
    public void explicitDisableOverridesTheRequestedCleanupFlags() {
        for (boolean updating : new boolean[] {false, true}) {
            resetVariables();
            variables.put("rollbackData", data("true", "true", "true"));
            variables.put("disableRollback", "true");
            preprocess(updating);
            assertSame(Boolean.TRUE, variables.get("skipRollback"));
            assertSame(original, variables.get("WorkflowException"));
        }
    }

    @Test
    public void updateReversesTheResourceListsAndRestoresTheirOriginalReferences() {
        List<NetworkResource> added = new ArrayList<>(List.of(network("added-1"), network("added-2")));
        List<NetworkResource> deleted = new ArrayList<>(List.of(network("deleted-1")));
        variables.put("addResourceList", added);
        variables.put("delResourceList", deleted);
        variables.put("rollbackData", data("true", "true", "true"));
        update.preProcessRequest(execution);
        assertSame(deleted, variables.get("addResourceList"));
        assertSame(added, variables.get("delResourceList"));
        assertSame(added, variables.get("addResourceList_o"));
        assertSame(deleted, variables.get("delResourceList_o"));
        assertSame(Boolean.TRUE, variables.get("rollbackAdded"));
        assertSame(Boolean.TRUE, variables.get("rollbackDeleted"));
        variables.put("GENDS_SuccessIndicator", true);
        update.postProcessRequest(execution);
        assertSame(added, variables.get("addResourceList"));
        assertSame(deleted, variables.get("delResourceList"));
        assertEquals(2, added.size());
        assertEquals(1, deleted.size());
        assertSame(Boolean.TRUE, variables.get("rolledBack"));
        assertNull(variables.get("rollbackData"));
        assertSame(original, variables.get("WorkflowException"));
    }

    @Test
    public void failedInventoryCleanupCannotBeReportedAsSuccessfulRollback() {
        for (boolean updating : new boolean[] {false, true}) {
            resetVariables();
            variables.put("rollbackData", data("true", "false", "false"));
            preprocess(updating);
            variables.put("GENDS_SuccessIndicator", false);
            postprocess(updating);
            assertSame(Boolean.FALSE, variables.get("rolledBack"));
            assertEquals("Error deleting service-instance in AAI for rollback", variables.get("rollbackError"));
            assertSame(original, variables.get("WorkflowException"));
            assertNull(variables.get("rollbackData"));
        }
    }

    @Test
    public void noRequestedCleanupLeavesTheResultAbsent() {
        for (boolean updating : new boolean[] {false, true}) {
            resetVariables();
            variables.put("rollbackData", data("false", "false", "false"));
            preprocess(updating);
            postprocess(updating);
            assertSame(Boolean.TRUE, variables.get("skipRollback"));
            assertFalse(variables.containsKey("rolledBack"));
            assertNull(variables.get("rollbackData"));
        }
    }

    @Test
    public void updateInventoryPayloadRestoresTheOriginalModelRatherThanTheTargetModel() {
        variables.put("serviceInstanceId", "service-1");
        variables.put("serviceInstanceName", "service Gr\u00fc\u00dfe & SO");
        variables.put("modelInvariantUuid", "invariant-1");
        variables.put("model-version-id-original", "original-model");
        variables.put("model-version-id-target", "target-model");
        update.preProcessAAIPUT(execution);
        ServiceInstance instance = (ServiceInstance) variables.get("serviceInstanceData");
        assertEquals("service-1", instance.getServiceInstanceId());
        assertEquals("service Gr\u00fc\u00dfe & SO", instance.getServiceInstanceName());
        assertEquals("original-model", instance.getModelVersionId());
        assertEquals("invariant-1", instance.getModelInvariantId());
        assertEquals("E2E Service", instance.getServiceType());
        assertEquals("E2E Service", instance.getServiceRole());
    }

    @Test
    public void creationJavaFailureClearsRollbackDataButKeepsTheOriginalRequestError() {
        variables.put("rollbackData", data("true", "false", "false"));
        create.processRollbackJavaException(execution);
        assertNull(variables.get("rollbackData"));
        assertNotNull(variables.get("rollbackError"));
        assertSame(original, variables.get("WorkflowException"));
    }

    @Test
    public void updateJavaFailureClearsRollbackDataButKeepsTheOriginalRequestError() {
        variables.put("rollbackData", data("true", "false", "false"));
        update.processRollbackJavaException(execution);
        assertNull(variables.get("rollbackData"));
        assertNotNull(variables.get("rollbackError"));
        assertSame(original, variables.get("WorkflowException"));
    }

    @Test
    public void creationBpmnFailureKeepsTheOriginalRequestError() {
        Assume.assumeTrue("SO-4457: E2E create rollback clears the original workflow exception",
                Boolean.getBoolean("groovy.migration.verification"));
        create.processRollbackException(execution);
        assertSame(original, variables.get("WorkflowException"));
    }

    @Test
    public void updateBpmnFailureKeepsTheOriginalRequestError() {
        Assume.assumeTrue("SO-4457: E2E update rollback clears the original workflow exception",
                Boolean.getBoolean("groovy.migration.verification"));
        update.processRollbackException(execution);
        assertSame(original, variables.get("WorkflowException"));
    }

    private void resetVariables() {
        variables.clear();
        variables.put("disableRollback", "false");
        variables.put("WorkflowException", original);
    }

    private RollbackData data(String inventory, String added, String deleted) {
        RollbackData data = new RollbackData();
        data.put("SERVICEINSTANCE", "serviceInstanceId", "service-1");
        data.put("SERVICEINSTANCE", "subscriptionServiceType", "subscription-1");
        data.put("SERVICEINSTANCE", "globalSubscriberId", "customer-1");
        data.put("SERVICEINSTANCE", "rollbackAAI", inventory);
        data.put("SERVICEINSTANCE", "rollbackAdded", added);
        data.put("SERVICEINSTANCE", "rollbackDeleted", deleted);
        data.put("SERVICEINSTANCE", "rollbackSDNC", "false");
        return data;
    }

    private NetworkResource network(String id) {
        NetworkResource resource = new NetworkResource();
        resource.setResourceId(id);
        return resource;
    }

    private void preprocess(boolean updating) {
        if (updating) {
            update.preProcessRequest(execution);
        } else {
            create.preProcessRequest(execution);
        }
    }

    private void postprocess(boolean updating) {
        if (updating) {
            update.postProcessRequest(execution);
        } else {
            create.postProcessRequest(execution);
        }
    }
}
