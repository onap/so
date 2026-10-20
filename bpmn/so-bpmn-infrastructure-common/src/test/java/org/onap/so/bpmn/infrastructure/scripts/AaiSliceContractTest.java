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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.ws.rs.NotFoundException;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.bpmn.core.WorkflowException;

public class AaiSliceContractTest {
    private final AAISliceUtil script = spy(new AAISliceUtil());
    private final AAIResourcesClient client = mock(AAIResourcesClient.class);
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();
    private final Map<String, AAIResultWrapper> responses = new HashMap<>();

    @Before
    public void configureExecution() {
        doReturn(client).when(script).getAAIClient();
        variables.put("testProcessKey", "AaiSliceContract");
        variables.put("globalSubscriberId", "customer-1");
        variables.put("serviceType", "service-type");
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
        when(client.exists(any(AAIResourceUri.class)))
                .thenAnswer(call -> responses.containsKey(instanceId(call.getArgument(0))));
        when(client.get(any(AAIResourceUri.class), eq(NotFoundException.class)))
                .thenAnswer(call -> responses.get(instanceId(call.getArgument(0))));
    }

    @Test
    public void relationshipsSelectOnlyNssiServicesAndPreserveTheirOrder() {
        parent(relationship("service-instance", "/services/nssi-2"), relationship("generic-vnf", "/vnfs/ignored"),
                relationship("service-instance", "https://aai.invalid/services/nsi-child"),
                relationship("service-instance", "/services/nssi-1"));
        response("nssi-2", service("nssi-2", "NsSi"));
        response("nsi-child", service("nsi-child", "nsi"));
        response("nssi-1", service("nssi-1", "nssi"));

        assertEquals(Arrays.asList("nssi-2", "nssi-1"), script.getNSSIIdList(execution, "nsi-1"));

        ArgumentCaptor<AAIResourceUri> uris = ArgumentCaptor.forClass(AAIResourceUri.class);
        verify(client, times(4)).get(uris.capture(), eq(NotFoundException.class));
        assertEquals(Arrays.asList("nsi-1", "nssi-2", "nsi-child", "nssi-1"),
                Arrays.asList(instanceId(uris.getAllValues().get(0)), instanceId(uris.getAllValues().get(1)),
                        instanceId(uris.getAllValues().get(2)), instanceId(uris.getAllValues().get(3))));
        for (AAIResourceUri uri : uris.getAllValues()) {
            assertEquals(
                    "/business/customers/customer/customer-1/service-subscriptions/"
                            + "service-subscription/service-type/service-instances/service-instance/" + instanceId(uri),
                    uri.build().getPath());
        }
        assertFalse(variables.containsKey("WorkflowException"));
    }

    @Test
    public void missingRelationshipListReturnsAnEmptyListWithoutChildQueries() {
        response("nsi-1", service("nsi-1", "nsi"));

        assertEquals(Collections.emptyList(), script.getNSSIIdList(execution, "nsi-1"));

        verify(client, times(1)).exists(any(AAIResourceUri.class));
        verify(client, times(1)).get(any(AAIResourceUri.class), eq(NotFoundException.class));
    }

    @Test
    public void anEmptyParentBeanDoesNotInventNssiIdentities() {
        responses.put("nsi-1", new AAIResultWrapper(""));

        assertEquals(Collections.emptyList(), script.getNSSIIdList(execution, "nsi-1"));

        assertFalse(variables.containsKey("WorkflowException"));
    }

    @Test
    public void absentRolesAndEmptyChildBeansAreExcludedFromTheSelectedIds() {
        parent(relationship("service-instance", "/services/no-role"),
                relationship("service-instance", "/services/empty"),
                relationship("service-instance", "/services/nssi-1"));
        response("no-role", new JSONObject().put("service-instance-id", "no-role"));
        responses.put("empty", new AAIResultWrapper(""));
        response("nssi-1", service("nssi-1", "NSSI"));

        assertEquals(Collections.singletonList("nssi-1"), script.getNSSIIdList(execution, "nsi-1"));
    }

    @Test
    public void missingParentProducesTheDefinedBusinessErrorWithoutAResourceRead() {
        assertError(2500, "query nssi from aai failed.", () -> script.getNSSIIdList(execution, "missing"));

        verify(client, times(1)).exists(any(AAIResourceUri.class));
        verify(client, never()).get(any(AAIResourceUri.class), eq(NotFoundException.class));
    }

    @Test
    public void missingRelatedServiceFailsInsteadOfReturningAPartialSelection() {
        parent(relationship("service-instance", "/services/nssi-1"),
                relationship("service-instance", "/services/missing"));
        response("nssi-1", service("nssi-1", "nssi"));

        assertError(2500, "query nssi from aai failed.", () -> script.getNSSIIdList(execution, "nsi-1"));

        verify(client, times(3)).exists(any(AAIResourceUri.class));
        verify(client, times(2)).get(any(AAIResourceUri.class), eq(NotFoundException.class));
    }

    @Test
    public void queryTransportFailureKeepsTheWorkflowErrorAndOriginalDetail() {
        doThrow(new IllegalStateException("AAI timed out")).when(client).exists(any(AAIResourceUri.class));

        assertError(7000, "Exception in getNSIFromAAI AAI timed out", () -> script.getNSSIIdList(execution, "nsi-1"));

        verify(client, never()).get(any(AAIResourceUri.class), eq(NotFoundException.class));
    }

    @Test
    public void missingRelationshipLinkProducesAWorkflowErrorBeforeAChildRead() {
        assertInvalidRelationship(new JSONObject().put("related-to", "service-instance"));
    }

    @Test
    public void emptyRelationshipLinkProducesAWorkflowErrorBeforeAChildRead() {
        assertInvalidRelationship(relationship("service-instance", ""));
    }

    @Test
    public void emptyRequestedIdListMakesNoAaiCalls() {
        assertTrue(script.getNSSIListFromAAI(execution, Collections.emptyList()).isEmpty());

        verifyNoInteractions(client);
    }

    @Test
    public void nullRequestedIdListRetainsTheGroovyEmptyIterationContract() {
        assertTrue(script.getNSSIListFromAAI(execution, null).isEmpty());

        verifyNoInteractions(client);
    }

    @Test
    public void requestedServiceBeansPreserveOrderingDuplicatesAndIdentityFields() {
        response("nssi-2", service("nssi-2", "nssi").put("service-instance-name", "slice Gr\u00fc\u00dfe"));
        response("nssi-1", service("nssi-1", "NSSI"));

        List<ServiceInstance> services =
                script.getNSSIListFromAAI(execution, Arrays.asList("nssi-2", "nssi-1", "nssi-2"));

        assertEquals(3, services.size());
        assertEquals("nssi-2", services.get(0).getServiceInstanceId());
        assertEquals("nssi-1", services.get(1).getServiceInstanceId());
        assertEquals("nssi-2", services.get(2).getServiceInstanceId());
        assertEquals("slice Gr\u00fc\u00dfe", services.get(0).getServiceInstanceName());
        verify(client, times(3)).exists(any(AAIResourceUri.class));
        verify(client, times(3)).get(any(AAIResourceUri.class), eq(NotFoundException.class));
    }

    @Test
    public void requestedServiceListOmitsEmptyBeansWithoutReorderingTheOthers() {
        responses.put("empty", new AAIResultWrapper(""));
        response("nssi-1", service("nssi-1", "nssi"));

        List<ServiceInstance> services = script.getNSSIListFromAAI(execution, Arrays.asList("empty", "nssi-1"));

        assertEquals(1, services.size());
        assertEquals("nssi-1", services.get(0).getServiceInstanceId());
    }

    @Test
    public void missingRequestedServiceUsesTheListSpecificBusinessError() {
        assertError(2500, "query nssi list from aai failed",
                () -> script.getNSSIListFromAAI(execution, Collections.singletonList("missing")));
    }

    private JSONObject service(String id, String role) {
        return new JSONObject().put("service-instance-id", id).put("service-role", role);
    }

    private JSONObject relationship(String relatedTo, String link) {
        return new JSONObject().put("related-to", relatedTo).put("related-link", link);
    }

    private void parent(JSONObject... relationships) {
        response("nsi-1", service("nsi-1", "nsi").put("relationship-list",
                new JSONObject().put("relationship", new JSONArray(Arrays.asList(relationships)))));
    }

    private void response(String id, JSONObject body) {
        responses.put(id, new AAIResultWrapper(body.toString()));
    }

    private String instanceId(AAIResourceUri uri) {
        String path = uri.build().getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private void assertInvalidRelationship(JSONObject relationship) {
        parent(relationship);
        assertError(2500, "query nssi from aai failed.", () -> script.getNSSIIdList(execution, "nsi-1"));
        verify(client, times(1)).get(any(AAIResourceUri.class), eq(NotFoundException.class));
    }

    private void assertError(int code, String message, Runnable action) {
        try {
            action.run();
            fail("Expected a BPMN error");
        } catch (BpmnError error) {
            assertEquals(String.valueOf(code), error.getErrorCode());
        }
        WorkflowException error = (WorkflowException) variables.get("WorkflowException");
        assertEquals("AaiSliceContract", error.getProcessKey());
        assertEquals(code, error.getErrorCode());
        assertEquals(message, error.getErrorMessage());
    }
}
