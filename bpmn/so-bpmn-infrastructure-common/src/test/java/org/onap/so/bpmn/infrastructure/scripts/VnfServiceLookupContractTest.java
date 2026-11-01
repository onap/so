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

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.bpmn.core.WorkflowException;

public class VnfServiceLookupContractTest {
    private static final String SERVICE = "/business/customers/customer/customer-1/service-subscriptions/"
            + "service-subscription/subscription/service-instances/service-instance/service-1";
    private final DoCreateVnf script = new DoCreateVnf();
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();
    private final List<URI> nodeRequests = new ArrayList<>();
    private MockedConstruction<AAIResourcesClient> clients;
    private boolean exists = true;
    private boolean rejectAvailability;
    private boolean rejectResolution;
    private String nodeBody;

    @Before
    public void configureStatefulExecutionAndTheExternalInventoryBoundary() {
        variables.put("DoCVNF_serviceInstanceId", "service-1");
        nodeBody = response(SERVICE);
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
        ProcessDefinition definition = mock(ProcessDefinition.class);
        when(definition.getKey()).thenReturn("DoCreateVnf");
        RepositoryService repository = mock(RepositoryService.class);
        when(repository.getProcessDefinition("definition-1")).thenReturn(definition);
        ProcessEngineServices services = mock(ProcessEngineServices.class);
        when(services.getRepositoryService()).thenReturn(repository);
        when(execution.getProcessDefinitionId()).thenReturn("definition-1");
        when(execution.getProcessInstanceId()).thenReturn("process-1");
        when(execution.getProcessEngineServices()).thenReturn(services);
        clients = mockConstruction(AAIResourcesClient.class, (client, context) -> {
            when(client.exists(any(AAIResourceUri.class))).thenAnswer(call -> {
                if (rejectAvailability) {
                    throw new IllegalStateException("availability lookup unavailable");
                }
                return exists;
            });
            when(client.get(any(AAIResourceUri.class), any())).thenAnswer(call -> {
                AAIResourceUri request = call.getArgument(0);
                nodeRequests.add(request.build());
                if (rejectResolution) {
                    throw new IllegalStateException("node resolution unavailable");
                }
                return new AAIResultWrapper(nodeBody);
            });
        });
    }

    @After
    public void closeExternalInventory() {
        if (clients != null) {
            clients.close();
        }
    }

    @Test
    public void requestedServiceResolvesTypedCustomerSubscriptionAndResourceLink() {
        query();
        assertRequestedNode("service-1");
        assertEquals("customer-1", variables.get("globalCustomerId"));
        assertEquals("subscription", variables.get("serviceType"));
        assertEquals(String.class, variables.get("globalCustomerId").getClass());
        assertEquals(String.class, variables.get("serviceType").getClass());
        assertEquals(String.class, variables.get("GENGS_siResourceLink").getClass());
        assertEquals(SERVICE, URI.create((String) variables.get("GENGS_siResourceLink")).getPath());
        assertNull(variables.get("WorkflowException"));
    }

    @Test
    public void prefixedServiceIdentityTakesPrecedenceOverVnfAndLegacyAliases() {
        variables.put("DoCVNF_serviceInstanceId", "service-2");
        variables.put("DoCVNF_vnfId", "vnf-other");
        variables.put("mso-service-instance-id", "legacy-other");
        variables.put("serviceInstanceId", "caller-other");
        variables.put("mso-request-id", "request-other");
        String selected = SERVICE.replace("service-1", "service-2");
        nodeBody = response(selected);
        query();
        assertRequestedNode("service-2");
        assertEquals(selected, URI.create((String) variables.get("GENGS_siResourceLink")).getPath());
        assertEquals("vnf-other", variables.get("DoCVNF_vnfId"));
        assertEquals("caller-other", variables.get("serviceInstanceId"));
        assertEquals("request-other", variables.get("mso-request-id"));
    }

    @Test
    public void absentServiceProduces2500BeforeResolvingItsNodeOrPublishingContext() {
        exists = false;
        failure(2500);
        assertTrue(nodeRequests.isEmpty());
        assertUnpublishedContext();
    }

    @Test
    public void availabilityTransportFailureRetains7000AndDoesNotPublishContext() {
        rejectAvailability = true;
        failure(7000);
        assertTrue(nodeRequests.isEmpty());
        assertUnpublishedContext();
    }

    @Test
    public void nodeResolutionTransportFailureRetains7000AndDoesNotPublishContext() {
        rejectResolution = true;
        failure(7000);
        assertRequestedNode("service-1");
        assertUnpublishedContext();
    }

    @Test
    public void malformedNodeResponseRetains7000InsteadOfPublishingServiceContext() {
        nodeBody = "<invalid-json/>";
        failure(7000);
        assertRequestedNode("service-1");
        assertUnpublishedContext();
    }

    @Test
    public void emptyNodeResultsRetain7000InsteadOfPublishingServiceContext() {
        nodeBody = "{\"results\":[]}";
        failure(7000);
        assertRequestedNode("service-1");
        assertUnpublishedContext();
    }

    private void query() {
        script.getServiceInstance(execution);
        assertReadOnlyInventory();
    }

    private void failure(int code) {
        BpmnError error = assertThrows(BpmnError.class, () -> script.getServiceInstance(execution));
        assertEquals(String.valueOf(code), error.getErrorCode());
        Object value = variables.get("WorkflowException");
        assertNotNull(value);
        assertEquals(WorkflowException.class, value.getClass());
        assertEquals(code, ((WorkflowException) value).getErrorCode());
        assertEquals("DoCreateVnf", ((WorkflowException) value).getProcessKey());
        assertReadOnlyInventory();
    }

    private void assertReadOnlyInventory() {
        for (var client : clients.constructed()) {
            mockingDetails(client).getInvocations()
                    .forEach(call -> assertTrue("Unexpected inventory action: " + call.getMethod().getName(),
                            List.of("exists", "get").contains(call.getMethod().getName())));
        }
    }

    private void assertRequestedNode(String id) {
        assertFalse(nodeRequests.isEmpty());
        for (URI request : nodeRequests) {
            assertEquals("/nodes/service-instances/service-instance/" + id, request.getPath());
            assertEquals("format=pathed", request.getRawQuery());
        }
    }

    private void assertUnpublishedContext() {
        for (String name : List.of("globalCustomerId", "serviceType", "GENGS_siResourceLink")) {
            assertFalse(name, variables.containsKey(name));
        }
    }

    private String response(String path) {
        return "{\"results\":[{\"resource-type\":\"service-instance\",\"resource-link\":"
                + "\"http://inventory.example/aai/v24" + path + "\"}]}";
    }
}
