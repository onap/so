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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
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
import org.json.JSONArray;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.beans.nsmf.SliceTaskParamsAdapter;
import org.onap.so.beans.nsmf.oof.TemplateInfo;
import org.onap.so.bpmn.core.WorkflowException;

public class NsiAllocationIdentityContractTest {
    private final DelegateExecution execution = mock(DelegateExecution.class);
    private final Map<String, Object> variables = new HashMap<>();
    private final List<InventoryWrite> writes = new ArrayList<>();
    private final SliceTaskParamsAdapter parameters = new SliceTaskParamsAdapter();
    private MockedConstruction<AAIResourcesClient> clients;
    private DoAllocateNSIandNSSI script;
    private RuntimeException inventoryFailure;

    @Before
    public void configureContracts() {
        clients = mockConstruction(AAIResourcesClient.class, (client, context) -> doAnswer(call -> {
            writes.add(new InventoryWrite(call.getArgument(0), call.getArgument(1)));
            if (inventoryFailure != null) {
                throw inventoryFailure;
            }
            return null;
        }).when(client).create(any(AAIResourceUri.class), any(ServiceInstance.class)));
        script = new DoAllocateNSIandNSSI();
        variables.put("testProcessKey", "DoAllocateNSIandNSSI");
        variables.put("sliceTaskParams", parameters);
        variables.put("globalSubscriberId", "customer-1");
        variables.put("subscriptionServiceType", "5G");
        variables.put("sliceServiceInstanceName", "München & 東京");
        variables.put("uuiRequest",
                "{\"service\":{\"parameters\":{\"requestInputs\":{\"plmnIdList\":[\"00101\",\"00202\"]}}}}");
        parameters.getServiceProfile().put("sST", "001");
        parameters.getServiceProfile().put("resourceSharingLevel", "shared");
        TemplateInfo model = new TemplateInfo();
        model.setUUID("model-2");
        model.setInvariantUUID("invariant-1");
        parameters.setNSTInfo(model);
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
    public void onlySixNsstsEnableFronthaulAndMidhaulWithBooleanFlags() {
        for (Object count : new Object[] {Integer.valueOf(6), "6", Integer.valueOf(3), "3", Integer.valueOf(0)}) {
            variables.put("nsstCount", count);
            script.preProcessRequest(execution);
            assertSame("6".equals(String.valueOf(count)) ? Boolean.TRUE : Boolean.FALSE,
                    variables.get("processFHandMH"));
            assertSame(Boolean.TRUE, variables.get("isMoreNSSTtoProcess"));
        }
        assertTrue(writes.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void initializationCreatesIndependentMutableStateWithCnFirst() {
        variables.put("nsstCount", Integer.valueOf(3));
        script.preProcessRequest(execution);
        Map<String, Object> firstMap = (Map<String, Object>) variables.get("nssiMap");
        List<String> firstSequence = (List<String>) variables.get("nsstSequence");
        assertTrue(firstMap.isEmpty());
        assertEquals(Arrays.asList("cn"), firstSequence);
        firstMap.put("cn", "allocated-nssi");
        firstSequence.add("an");
        script.preProcessRequest(execution);
        assertNotSame(firstMap, variables.get("nssiMap"));
        assertNotSame(firstSequence, variables.get("nsstSequence"));
        assertTrue(((Map<?, ?>) variables.get("nssiMap")).isEmpty());
        assertEquals(Arrays.asList("cn"), variables.get("nsstSequence"));
        assertEquals("allocated-nssi", firstMap.get("cn"));
        assertEquals(Arrays.asList("cn", "an"), firstSequence);
    }

    @Test
    public void aSuggestionNameCannotSelectAnNsiWithoutANonblankIdentifier() {
        for (String identifier : new String[] {null, "", " \t"}) {
            parameters.setSuggestNsiId(identifier);
            parameters.setSuggestNsiName("unused name");
            script.retriveSliceOption(execution);
            assertSame(Boolean.FALSE, variables.get("isNSIOptionAvailable"));
            assertFalse(variables.containsKey("nsiServiceInstanceId"));
            assertFalse(variables.containsKey("nsiServiceInstanceName"));
        }
        assertTrue(writes.isEmpty());
    }

    @Test
    public void selectedIdentityAndOptionalNamesRemainExactWithoutInventoryWrites() {
        for (String name : new String[] {"München & 東京", null, ""}) {
            parameters.setSuggestNsiId("nsi-001");
            parameters.setSuggestNsiName(name);
            script.retriveSliceOption(execution);
            assertSame(Boolean.TRUE, variables.get("isNSIOptionAvailable"));
            assertEquals("nsi-001", variables.get("nsiServiceInstanceId"));
            assertTrue(variables.containsKey("nsiServiceInstanceName"));
            assertEquals(name, variables.get("nsiServiceInstanceName"));
        }
        assertTrue(writes.isEmpty());
    }

    @Test
    public void inventoryCreationPublishesOneConsistentIdentifierAndTypedServiceFields() {
        script.createNSIinAAI(execution);
        assertEquals(1, writes.size());
        InventoryWrite write = writes.get(0);
        String identifier = write.service.getServiceInstanceId();
        assertEquals(identifier, UUID.fromString(identifier).toString());
        assertEquals(identifier, variables.get("sliceInstanceId"));
        assertEquals(identifier, parameters.getSuggestNsiId());
        assertEquals("nsi_München & 東京", parameters.getSuggestNsiName());
        assertEquals("nsi_München & 東京", write.service.getServiceInstanceName());
        assertEquals("001", write.service.getServiceType());
        assertEquals("deactivated", write.service.getOrchestrationStatus());
        assertEquals("invariant-1", write.service.getModelInvariantId());
        assertEquals("model-2", write.service.getModelVersionId());
        assertEquals("nsi", write.service.getServiceRole());
        assertEquals("shared", write.service.getServiceFunction());
        JSONArray plmns = new JSONArray(write.service.getServiceInstanceLocationId());
        assertEquals(2, plmns.length());
        assertEquals("00101", plmns.getString(0));
        assertEquals("00202", plmns.getString(1));
        String path = "/business/customers/customer/customer-1/service-subscriptions/service-subscription/5G/"
                + "service-instances/service-instance/" + identifier;
        assertEquals(path, write.uri.build().getPath());
        assertTrue(variables.get("nsiServiceUri") instanceof AAIResourceUri);
        assertEquals(path, ((AAIResourceUri) variables.get("nsiServiceUri")).build().getPath());
        assertSame(parameters, variables.get("sliceTaskParams"));
        assertFalse(variables.containsKey("WorkflowException"));
    }

    @Test
    public void failedInventoryCreationDoesNotPublishAnAaiUriAndUsesTheWorkflowErrorContract() {
        inventoryFailure = new IllegalStateException("AAI unavailable");
        try {
            script.createNSIinAAI(execution);
            fail("Expected inventory creation failure");
        } catch (BpmnError error) {
            assertEquals("7000", error.getErrorCode());
            assertTrue(variables.get("WorkflowException") instanceof WorkflowException);
            WorkflowException detail = (WorkflowException) variables.get("WorkflowException");
            assertEquals(7000, detail.getErrorCode());
            assertTrue(detail.getErrorMessage().contains("AAI unavailable"));
        }
        assertEquals(1, writes.size());
        assertFalse(variables.containsKey("nsiServiceUri"));
    }

    @Test
    public void anInventoryBpmnErrorIsRethrownWithoutReplacingTheExistingWorkflowError() {
        BpmnError original = new BpmnError("AAI_BOUNDARY");
        inventoryFailure = original;
        WorkflowException detail = new WorkflowException("Parent", 5310, "original business failure");
        variables.put("WorkflowException", detail);
        try {
            script.createNSIinAAI(execution);
            fail("Expected inventory BPMN error");
        } catch (BpmnError error) {
            assertSame(original, error);
        }
        assertSame(detail, variables.get("WorkflowException"));
        assertEquals(1, writes.size());
        assertFalse(variables.containsKey("nsiServiceUri"));
    }

    private static class InventoryWrite {
        private final AAIResourceUri uri;
        private final ServiceInstance service;

        private InventoryWrite(AAIResourceUri uri, ServiceInstance service) {
            this.uri = uri;
            this.service = service;
        }
    }
}
