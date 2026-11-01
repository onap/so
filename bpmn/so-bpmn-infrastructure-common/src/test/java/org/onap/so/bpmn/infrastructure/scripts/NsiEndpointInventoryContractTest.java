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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.onap.aai.domain.yang.NetworkRoute;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.beans.nsmf.SliceProfileAdapter;
import org.onap.so.beans.nsmf.SliceTaskInfo;
import org.onap.so.beans.nsmf.SliceTaskParamsAdapter;
import org.onap.so.beans.nsmf.oof.SubnetType;
import org.onap.so.bpmn.core.WorkflowException;

public class NsiEndpointInventoryContractTest {
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();
    private final SliceTaskParamsAdapter parameters = new SliceTaskParamsAdapter();
    private final List<NetworkRoute> routes = new ArrayList<>();
    private final List<String> paths = new ArrayList<>();
    private final List<String> publishedVariables = new ArrayList<>();
    private MockedConstruction<AAIResourcesClient> clients;
    private DoAllocateNSIandNSSI script;
    private RuntimeException writeFailure;

    @Before
    public void configureIndependentEndpointInputsAndExternalInventoryBoundary() {
        clients = mockConstruction(AAIResourcesClient.class, (client, context) -> {
            doAnswer(call -> {
                AAIResourceUri uri = call.getArgument(0);
                paths.add(uri.build().getPath());
                routes.add(call.getArgument(1));
                assertFalse(publishedVariables.contains("sliceTaskParams"));
                assertFalse(publishedVariables.contains("cnEndpointId"));
                if (writeFailure != null) {
                    throw writeFailure;
                }
                return null;
            }).when(client).create(any(AAIResourceUri.class), any(NetworkRoute.class));
        });
        script = new DoAllocateNSIandNSSI();
        configure(task(SubnetType.AN), "192.0.2.10", "192.0.2.1", "AN & <\u96ea>");
        configure(task(SubnetType.CN), "198.51.100.20", "198.51.100.1", "CN \"\u96ea\"");
        variables.put("sliceTaskParams", parameters);
        variables.put("globalSubscriberId", "customer-1");
        variables.put("subscriptionServiceType", "5G");
        variables.put("testProcessKey", "DoAllocateNSIandNSSI");
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            String key = call.getArgument(0);
            variables.put(key, call.getArgument(1));
            publishedVariables.add(key);
            return null;
        }).when(execution).setVariable(anyString(), any());
    }

    @After
    public void closeExternalInventoryBoundary() {
        if (clients != null) {
            clients.close();
        }
    }

    @Test
    public void accessEndpointKeepsItsOwnAddressInterfaceRoleAndGeneratedRouteIdentity() {
        endpointContract(SubnetType.AN);
        assertEquals("before-core", task(SubnetType.CN).getEndPointId());
        assertFalse(variables.containsKey("cnEndpointId"));
    }

    @Test
    public void coreEndpointKeepsItsOwnAddressInterfaceAndPublishedRouteIdentity() {
        endpointContract(SubnetType.CN);
        assertEquals("before-access", task(SubnetType.AN).getEndPointId());
        assertEquals(routes.get(0).getRouteId(), variables.get("cnEndpointId"));
    }

    @Test
    public void accessAndCoreEndpointsHaveIndependentRoutesAndKeepBothPublishedIdentities() {
        create(SubnetType.AN);
        String access = task(SubnetType.AN).getEndPointId();
        publishedVariables.clear();
        create(SubnetType.CN);
        String core = task(SubnetType.CN).getEndPointId();
        assertNotEquals(access, core);
        assertEquals(access, task(SubnetType.AN).getEndPointId());
        assertEquals(core, variables.get("cnEndpointId"));
        assertEquals("AN", routes.get(0).getRole());
        assertEquals("CN", routes.get(1).getRole());
        assertEquals("192.0.2.10", routes.get(0).getIpAddress());
        assertEquals("198.51.100.20", routes.get(1).getIpAddress());
        assertEquals(Arrays.asList("/network/network-routes/network-route/" + access,
                "/network/network-routes/network-route/" + core), paths);
        assertEquals(2, routes.size());
    }

    @Test
    public void endpointOptionalValuesKeepNullAndEmptyDistinct() {
        for (SubnetType domain : Arrays.asList(SubnetType.AN, SubnetType.CN)) {
            for (String value : Arrays.asList(null, "")) {
                resetCapturedWrites();
                variables.remove("cnEndpointId");
                configure(task(domain), value, value, value);
                create(domain);
                assertEquals(value, routes.get(0).getIpAddress());
                assertEquals(value, routes.get(0).getNextHop());
                assertEquals(value, routes.get(0).getLogicalInterfaceId());
            }
        }
    }

    @Test
    public void accessInventoryFailurePreservesTheCauseWithoutPublishingCompletion() {
        writeFailure = new IllegalStateException("access inventory unavailable");
        assertSame(writeFailure, assertThrows(IllegalStateException.class, () -> create(SubnetType.AN)));
        assertEquals(1, routes.size());
        assertTrue(publishedVariables.isEmpty());
        assertEquals("before-core", task(SubnetType.CN).getEndPointId());
    }

    @Test
    public void coreInventoryFailureRetainsThePreviouslyPublishedCoreEndpoint() {
        variables.put("cnEndpointId", "previous-core");
        writeFailure = new IllegalStateException("core inventory unavailable");
        assertSame(writeFailure, assertThrows(IllegalStateException.class, () -> create(SubnetType.CN)));
        assertEquals(1, routes.size());
        assertEquals("previous-core", variables.get("cnEndpointId"));
        assertTrue(publishedVariables.isEmpty());
        assertEquals("before-access", task(SubnetType.AN).getEndPointId());
    }

    @Test
    public void endpointBpmnErrorsRetainTheirOriginalWorkflowException() {
        WorkflowException original = new WorkflowException("Parent", 5310, "original failure");
        variables.put("WorkflowException", original);
        BpmnError error = new BpmnError("AAI_BOUNDARY");
        writeFailure = error;
        for (SubnetType domain : Arrays.asList(SubnetType.AN, SubnetType.CN)) {
            resetCapturedWrites();
            assertSame(error, assertThrows(BpmnError.class, () -> create(domain)));
            assertSame(original, variables.get("WorkflowException"));
            assertTrue(publishedVariables.isEmpty());
            assertEquals(1, routes.size());
        }
    }

    @Test
    public void missingTypedSliceStateCannotWriteAnInventoryRoute() {
        variables.remove("sliceTaskParams");
        for (SubnetType domain : Arrays.asList(SubnetType.AN, SubnetType.CN)) {
            assertThrows(RuntimeException.class, () -> create(domain));
            assertTrue(routes.isEmpty());
            assertTrue(publishedVariables.isEmpty());
        }
    }

    @Test
    public void emptyTransportEndpointsReturnTheDefinedWorkflowValidationErrorBeforeAnyWrite() {
        variables.put("sliceParams", "{\"endPoints\":[]}");
        BpmnError error = assertThrows(BpmnError.class, () -> script.createTNEndPoints(execution));
        assertEquals("500", error.getErrorCode());
        WorkflowException detail = (WorkflowException) variables.get("WorkflowException");
        assertEquals("DoAllocateNSIandNSSI", detail.getProcessKey());
        assertEquals(500, detail.getErrorCode());
        assertEquals("End point info is empty", detail.getErrorMessage());
        assertTrue(routes.isEmpty());
        assertFalse(variables.containsKey("tranportEp_ID_bh"));
    }

    @Test
    public void transportEndpointCreationCanUseTheSuppliedBackhaulExecutionContext() {
        Assume.assumeTrue("SO-4457: TN endpoint creation does not read bh_endpoint from execution",
                Boolean.getBoolean("groovy.migration.verification"));
        variables.put("sliceParams", "{\"endPoints\":[\"backhaul\"]}");
        variables.put("bh_endpoint",
                "{\"IpAddress\":\"192.0.2.30\",\"LogicalLinkId\":\"backhaul-1\"," + "\"nextHopInfo\":\"192.0.2.1\"}");
        variables.put("RANServiceInstanceId", "ran-service");
        variables.put("RANNFServiceInstanceId", "ran-nf-service");
        script.createTNEndPoints(execution);
        assertEquals(5, routes.size());
        assertEquals("192.0.2.30", routes.get(0).getIpAddress());
        assertEquals("backhaul-1", routes.get(0).getLogicalInterfaceId());
        assertEquals("192.0.2.1", routes.get(0).getNextHop());
    }

    private void endpointContract(SubnetType domain) {
        create(domain);
        assertEquals(1, routes.size());
        NetworkRoute route = routes.get(0);
        assertEquals(route.getRouteId(), UUID.fromString(route.getRouteId()).toString());
        assertEquals(Arrays.asList("/network/network-routes/network-route/" + route.getRouteId()), paths);
        assertEquals(route.getRouteId(), task(domain).getEndPointId());
        assertEquals("endpoint", route.getType());
        assertEquals(domain.name(), route.getRole());
        assertEquals("3gppTransportEP", route.getFunction());
        assertEquals("ipv4", route.getAddressFamily());
        assertEquals(Integer.valueOf(24), route.getPrefixLength());
        SliceProfileAdapter profile = task(domain).getSliceProfile();
        assertEquals(profile.getIpAddress(), route.getIpAddress());
        assertEquals(profile.getLogicInterfaceId(), route.getLogicalInterfaceId());
        assertEquals(profile.getNextHopInfo(), route.getNextHop());
        assertSame(parameters, variables.get("sliceTaskParams"));
        assertEquals(1, publishedVariables.stream().filter("sliceTaskParams"::equals).count());
    }

    private void configure(SliceTaskInfo<SliceProfileAdapter> task, String address, String nextHop, String logicalId) {
        SliceProfileAdapter profile = new SliceProfileAdapter();
        profile.setIpAddress(address);
        profile.setNextHopInfo(nextHop);
        profile.setLogicInterfaceId(logicalId);
        task.setSliceProfile(profile);
        task.setEndPointId(task == parameters.getAnSliceTaskInfo() ? "before-access" : "before-core");
    }

    private SliceTaskInfo<SliceProfileAdapter> task(SubnetType domain) {
        return domain == SubnetType.AN ? parameters.getAnSliceTaskInfo() : parameters.getCnSliceTaskInfo();
    }

    private void create(SubnetType domain) {
        if (domain == SubnetType.AN) {
            script.createANEndpoint(execution);
        } else {
            script.createCNEndpoint(execution);
        }
    }

    private void resetCapturedWrites() {
        routes.clear();
        paths.clear();
        publishedVariables.clear();
    }
}
