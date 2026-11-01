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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ServiceUnavailableException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.After;
import org.junit.Before;
import org.junit.Assume;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.so.beans.nsmf.NssiResponse;
import org.onap.so.beans.nsmf.SliceProfileAdapter;
import org.onap.so.beans.nsmf.SliceTaskInfo;
import org.onap.so.beans.nsmf.SliceTaskParamsAdapter;
import org.onap.so.beans.nsmf.oof.SubnetType;
import org.onap.so.bpmn.core.WorkflowException;

public class NsiInventoryRelationshipContractTest {
    private static final String SERVICE = "/business/customers/customer/customer-1/service-subscriptions/"
            + "service-subscription/5G/service-instances/service-instance/";
    private static final String RELATIONSHIP = "/relationship-list/relationship";
    private final Map<String, Object> variables = new HashMap<>();
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final SliceTaskParamsAdapter parameters = new SliceTaskParamsAdapter();
    private final List<String> writes = new ArrayList<>();
    private final List<String> connections = new ArrayList<>();
    private final List<String> queries = new ArrayList<>();
    private MockedConstruction<AAIResourcesClient> clients;
    private DoAllocateNSIandNSSI script;
    private AAIResultWrapper queryResult = new AAIResultWrapper("{\"service-instance-id\":\"nsi-1\"}");
    private RuntimeException queryFailure;
    private RuntimeException connectFailure;
    private RuntimeException writeFailure;
    private int failAtWrite;
    private SubnetType activeDomain;

    @Before
    public void configureInventoryBoundaryAndTypedState() {
        clients = mockConstruction(AAIResourcesClient.class, (client, context) -> {
            doAnswer(call -> {
                AAIResourceUri source = call.getArgument(0);
                Relationship relationship = call.getArgument(1);
                writes.add(source.build().getPath() + " -> " + new URI(relationship.getRelatedLink()).getPath());
                if (activeDomain != null) {
                    assertEquals("before-" + activeDomain, task(activeDomain).getSuggestNssiId());
                }
                if (writeFailure != null && writes.size() == failAtWrite) {
                    throw writeFailure;
                }
                return null;
            }).when(client).create(any(AAIResourceUri.class), any(Relationship.class));
            doAnswer(call -> {
                AAIResourceUri source = call.getArgument(0);
                AAIResourceUri target = call.getArgument(1);
                connections.add(source.build().getPath() + " -> " + target.build().getPath());
                if (connectFailure != null) {
                    throw connectFailure;
                }
                return null;
            }).when(client).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));
            when(client.get(any(AAIResourceUri.class), eq(NotFoundException.class))).thenAnswer(call -> {
                AAIResourceUri uri = call.getArgument(0);
                queries.add(uri.build().getPath());
                if (queryFailure != null) {
                    throw queryFailure;
                }
                return queryResult;
            });
        });
        script = new DoAllocateNSIandNSSI();
        parameters.setSuggestNsiId("nsi-1");
        parameters.setServiceId("service-profile");
        for (SubnetType domain : Arrays.asList(SubnetType.AN, SubnetType.CN, SubnetType.TN_BH, SubnetType.TN_FH,
                SubnetType.TN_MH)) {
            task(domain).setSliceInstanceId("profile-" + domain);
            task(domain).setEndPointId("endpoint-" + domain);
            task(domain).setSuggestNssiId("before-" + domain);
            NssiResponse response = new NssiResponse();
            response.setNssiId("allocated-" + domain);
            variables.put(resultKey(domain), response);
        }
        variables.put("testProcessKey", "DoAllocateNSIandNSSI");
        variables.put("sliceTaskParams", parameters);
        variables.put("globalSubscriberId", "customer-1");
        variables.put("subscriptionServiceType", "5G");
        variables.put("allottedResourceUri",
                AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer("customer-1")
                        .serviceSubscription("5G").serviceInstance("parent-service").allottedResource("allotted-1")));
        variables.put("nsiServiceUri", service("nsi-1"));
        when(execution.getVariable(anyString())).thenAnswer(call -> variables.get(call.getArgument(0)));
        doAnswer(call -> {
            variables.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
    }

    @After
    public void closeInventoryBoundary() {
        if (clients != null) {
            clients.close();
        }
    }

    @Test
    public void accessLinksTheNsiProfilesAndEndpointBeforePublishingItsAllocatedIdentity() {
        successfulUpdate(SubnetType.AN, true);
    }

    @Test
    public void coreLinksOnlyItsOwnProfilesAndEndpointBeforePublishingItsAllocatedIdentity() {
        successfulUpdate(SubnetType.CN, true);
    }

    @Test
    public void backhaulLinksTheNsiAndProfilesWithoutInventingEndpointRelationships() {
        successfulUpdate(SubnetType.TN_BH, false);
    }

    @Test
    public void everyAccessWriteFailureStopsFurtherWritesAndKeepsThePreviousIdentity() {
        failedUpdates(SubnetType.AN, 5);
    }

    @Test
    public void everyCoreWriteFailureStopsFurtherWritesAndKeepsThePreviousIdentity() {
        failedUpdates(SubnetType.CN, 5);
    }

    @Test
    public void everyBackhaulWriteFailureStopsFurtherWritesAndKeepsThePreviousIdentity() {
        failedUpdates(SubnetType.TN_BH, 3);
    }

    @Test
    public void fronthaulLinksOnlyItsOwnNsiAndProfilesBeforePublishingItsIdentity() {
        successfulUpdate(SubnetType.TN_FH, false);
        assertEquals("before-TN_MH", task(SubnetType.TN_MH).getSuggestNssiId());
        assertEquals("before-TN_BH", task(SubnetType.TN_BH).getSuggestNssiId());
    }

    @Test
    public void midhaulLinksOnlyItsOwnNsiAndProfilesBeforePublishingItsIdentity() {
        successfulUpdate(SubnetType.TN_MH, false);
        assertEquals("before-TN_FH", task(SubnetType.TN_FH).getSuggestNssiId());
        assertEquals("before-TN_BH", task(SubnetType.TN_BH).getSuggestNssiId());
    }

    @Test
    public void everyFronthaulWriteFailureStopsFurtherWritesAndKeepsThePreviousIdentity() {
        failedUpdates(SubnetType.TN_FH, 3);
    }

    @Test
    public void everyMidhaulWriteFailureStopsFurtherWritesAndKeepsThePreviousIdentity() {
        failedUpdates(SubnetType.TN_MH, 3);
    }

    @Test
    public void aRelationshipBpmnErrorRetainsTheOriginalBusinessFailure() {
        BpmnError boundary = new BpmnError("AAI_BOUNDARY");
        WorkflowException original = new WorkflowException("Parent", 5310, "original allocation failure");
        variables.put("WorkflowException", original);
        writeFailure = boundary;
        failAtWrite = 3;
        assertSame(boundary, assertThrows(BpmnError.class, () -> update(SubnetType.AN)));
        assertEquals(3, writes.size());
        assertSame(original, variables.get("WorkflowException"));
        assertEquals("before-AN", task(SubnetType.AN).getSuggestNssiId());
    }

    @Test
    public void newNsiRelationshipConnectsTheProvidedResourcesWithoutReversingThem() {
        script.createRelationship(execution);
        assertEquals(Arrays.asList(
                SERVICE + "parent-service/allotted-resources/allotted-resource/allotted-1 -> " + SERVICE + "nsi-1"),
                connections);
        assertTrue(writes.isEmpty());
        assertTrue(queries.isEmpty());
        assertFalse(variables.containsKey("WorkflowException"));
    }

    @Test
    public void failedNewNsiConnectionUsesTheWorkflowErrorAndRetainsTheCauseDetail() {
        connectFailure = new IllegalStateException("inventory unavailable");
        BpmnError error = assertThrows(BpmnError.class, () -> script.createRelationship(execution));
        assertEquals("7000", error.getErrorCode());
        WorkflowException detail = (WorkflowException) variables.get("WorkflowException");
        assertEquals(7000, detail.getErrorCode());
        assertTrue(detail.getErrorMessage().contains("createRelationship"));
        assertTrue(detail.getErrorMessage().contains("inventory unavailable"));
        assertEquals(1, connections.size());
        assertTrue(writes.isEmpty());
    }

    @Test
    public void aNewNsiConnectionBpmnErrorKeepsTheOriginalWorkflowFailure() {
        BpmnError boundary = new BpmnError("AAI_BOUNDARY");
        WorkflowException original = new WorkflowException("Parent", 5310, "original failure");
        variables.put("WorkflowException", original);
        connectFailure = boundary;
        assertSame(boundary, assertThrows(BpmnError.class, () -> script.createRelationship(execution)));
        assertSame(original, variables.get("WorkflowException"));
        assertEquals(1, connections.size());
    }

    @Test
    public void aSelectedNsiIsQueriedBeforeConnectingTheAllottedResource() {
        script.updateRelationship(execution);
        assertEquals(Arrays.asList(SERVICE + "nsi-1"), queries);
        assertEquals(Arrays.asList(
                SERVICE + "parent-service/allotted-resources/allotted-resource/allotted-1 -> " + SERVICE + "nsi-1"),
                connections);
        assertSame(parameters, variables.get("sliceTaskParams"));
        assertTrue(writes.isEmpty());
        assertFalse(variables.containsKey("WorkflowException"));
    }

    @Test
    public void anAbsentSelectedNsiCannotCreateAnyRelationship() {
        queryResult = new AAIResultWrapper("");
        BpmnError error = assertThrows(BpmnError.class, () -> script.updateRelationship(execution));
        assertEquals("7000", error.getErrorCode());
        WorkflowException detail = (WorkflowException) variables.get("WorkflowException");
        assertEquals(7000, detail.getErrorCode());
        assertTrue(detail.getErrorMessage().contains("doesn't exist"));
        assertTrue(detail.getErrorMessage().contains("nsi-1"));
        assertEquals(Arrays.asList(SERVICE + "nsi-1"), queries);
        assertTrue(connections.isEmpty());
        assertTrue(writes.isEmpty());
    }

    @Test
    public void anUnavailableSelectedNsiLookupRetainsTheTransportFailureInsteadOfClaimingAbsence() {
        Assume.assumeTrue("SO-4457: a failed selected-NSI lookup is reported as resource absence",
                Boolean.getBoolean("groovy.migration.verification"));
        queryFailure = new ServiceUnavailableException("inventory unavailable");
        BpmnError error = assertThrows(BpmnError.class, () -> script.updateRelationship(execution));
        assertEquals("7000", error.getErrorCode());
        assertEquals(Arrays.asList(SERVICE + "nsi-1"), queries);
        assertTrue(connections.isEmpty());
        assertTrue(writes.isEmpty());
        WorkflowException detail = (WorkflowException) variables.get("WorkflowException");
        assertTrue(detail.getErrorMessage(), detail.getErrorMessage().contains("inventory unavailable"));
        assertFalse(detail.getErrorMessage().contains("doesn't exist"));
    }

    @Test
    public void aSelectedNsiLookupBpmnErrorIsRethrownWithItsOriginalWorkflowDetail() {
        BpmnError boundary = new BpmnError("AAI_BOUNDARY");
        WorkflowException original = new WorkflowException("Parent", 5310, "original failure");
        variables.put("WorkflowException", original);
        queryFailure = boundary;
        assertSame(boundary, assertThrows(BpmnError.class, () -> script.updateRelationship(execution)));
        assertSame(original, variables.get("WorkflowException"));
        assertTrue(connections.isEmpty());
        assertTrue(writes.isEmpty());
    }

    private void successfulUpdate(SubnetType domain, boolean endpoints) {
        update(domain);
        List<String> expected = new ArrayList<>();
        expected.add(link("nsi-1", "allocated-" + domain));
        expected.add(link("service-profile", "profile-" + domain));
        expected.add(link("profile-" + domain, "allocated-" + domain));
        if (endpoints) {
            expected.add(SERVICE + "allocated-" + domain + RELATIONSHIP
                    + " -> /network/network-routes/network-route/endpoint-" + domain);
            expected.add(SERVICE + "profile-" + domain + RELATIONSHIP
                    + " -> /network/network-routes/network-route/endpoint-" + domain);
        }
        assertEquals(expected, writes);
        assertEquals("allocated-" + domain, task(domain).getSuggestNssiId());
        assertSame(parameters, variables.get("sliceTaskParams"));
        assertTrue(connections.isEmpty());
        assertTrue(queries.isEmpty());
    }

    private void failedUpdates(SubnetType domain, int stages) {
        for (int stage = 1; stage <= stages; stage++) {
            writes.clear();
            task(domain).setSuggestNssiId("before-" + domain);
            failAtWrite = stage;
            writeFailure = new IllegalStateException("AAI stage " + stage);
            assertSame(writeFailure, assertThrows(IllegalStateException.class, () -> update(domain)));
            assertEquals("failed stage " + stage, stage, writes.size());
            assertEquals("before-" + domain, task(domain).getSuggestNssiId());
            assertSame(parameters, variables.get("sliceTaskParams"));
        }
    }

    private String link(String source, String target) {
        return SERVICE + source + RELATIONSHIP + " -> " + SERVICE + target;
    }

    private void update(SubnetType domain) {
        activeDomain = domain;
        if (domain == SubnetType.AN) {
            script.updateAnRelationship(execution);
        } else if (domain == SubnetType.CN) {
            script.updateCnRelationship(execution);
        } else if (domain == SubnetType.TN_FH) {
            script.updateTnFHRelationship(execution);
        } else if (domain == SubnetType.TN_MH) {
            script.updateTnMHRelationship(execution);
        } else {
            script.updateTnBHRelationship(execution);
        }
    }

    private SliceTaskInfo<SliceProfileAdapter> task(SubnetType domain) {
        if (domain == SubnetType.AN) {
            return parameters.getAnSliceTaskInfo();
        }
        if (domain == SubnetType.CN) {
            return parameters.getCnSliceTaskInfo();
        }
        if (domain == SubnetType.TN_FH) {
            return parameters.getTnFHSliceTaskInfo();
        }
        return domain == SubnetType.TN_MH ? parameters.getTnMHSliceTaskInfo() : parameters.getTnBHSliceTaskInfo();
    }

    private String resultKey(SubnetType domain) {
        switch (domain) {
            case AN:
                return "anNssiAllocateResult";
            case CN:
                return "cnNssiAllocateResult";
            case TN_FH:
                return "tnFHNssiAllocateResult";
            case TN_MH:
                return "tnMHNssiAllocateResult";
            default:
                return "tnBHNssiAllocateResult";
        }
    }

    private AAIResourceUri service(String id) {
        return AAIUriFactory.createResourceUri(
                AAIFluentTypeBuilder.business().customer("customer-1").serviceSubscription("5G").serviceInstance(id));
    }
}
