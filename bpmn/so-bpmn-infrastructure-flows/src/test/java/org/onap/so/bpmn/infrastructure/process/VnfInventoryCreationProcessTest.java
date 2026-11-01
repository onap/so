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

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Assume;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.bpmn.common.verification.WorkflowPersistenceSupport;
import org.onap.so.bpmn.core.RollbackData;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.core.domain.VnfResource;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

public class VnfInventoryCreationProcessTest extends WorkflowPersistenceSupport {
    private static final String VNF = "/network/generic-vnfs/generic-vnf/vnf-1";
    private static final String SERVICE = "/business/customers/customer/customer-1/service-subscriptions/"
            + "service-subscription/subscription/service-instances/service-instance/service-1";
    private final Map<String, Object> variables = new HashMap<>();
    private final List<String> mutations = new ArrayList<>();
    private final List<String> completedCreates = new ArrayList<>();
    private final Map<String, Object> payload = new LinkedHashMap<>();
    private MockedConstruction<AAIResourcesClient> inventories;
    private MockedConstruction<CatalogDbClient> catalogs;
    private Environment previousEnvironment;
    private boolean serviceExists = true;
    private boolean rejectLookup;
    private boolean rejectCreate;
    private boolean rejectConnect;
    private int resolutions;

    @Before
    public void deployProductionCreationAndConfigureExternalClients() throws Exception {
        previousEnvironment = (Environment) ReflectionTestUtils.getField(UrnPropertiesReader.class, "environment");
        String path = "subprocess/DoCreateVnf.bpmn";
        URL url = getClass().getClassLoader().getResource(path);
        assertNotNull(path, url);
        String location = url.toExternalForm();
        assertTrue(location,
                location.contains("/so-bpmn-infrastructure-flows/target/classes/")
                        || (location.matches("jar:.*/so-bpmn-infrastructure-flows-[^/]+\\.jar!/" + path)
                                && !location.contains("-tests.jar!/")));
        try (InputStream original = url.openStream()) {
            var deployment = engine.getRepositoryService().createDeployment().addInputStream(path, original).deploy();
            try (InputStream input = url.openStream();
                    InputStream stored = engine.getRepositoryService().getResourceAsStream(deployment.getId(), path)) {
                assertArrayEquals(input.readAllBytes(), stored.readAllBytes());
            }
        }
        new UrnPropertiesReader().setEnvironment(
                new MockEnvironment().withProperty("mso.catalog.db.spring.endpoint", "http://catalog.invalid")
                        .withProperty("mso.workflow.sdncadapter.callback", "http://callback.invalid/sdnc"));
        catalogs = mockConstruction(CatalogDbClient.class);
        inventories = mockConstruction(AAIResourcesClient.class, (client, context) -> {
            when(client.exists(any(AAIResourceUri.class))).thenAnswer(call -> {
                if (rejectLookup) {
                    throw new IllegalStateException("inventory lookup rejected");
                }
                return serviceExists;
            });
            when(client.get(any(AAIResourceUri.class), any())).thenAnswer(call -> {
                resolutions++;
                return new AAIResultWrapper("{\"results\":[{\"resource-type\":\"service-instance\","
                        + "\"resource-link\":\"http://inventory.example/aai/v24" + SERVICE + "\"}]}");
            });
            doAnswer(call -> {
                AAIResourceUri uri = call.getArgument(0);
                String pathCreated = uri.build().getPath();
                mutations.add("create:" + pathCreated);
                Map<?, ?> sent = call.getArgument(1);
                sent.forEach((key, value) -> payload.put(String.valueOf(key), value));
                if (rejectCreate) {
                    throw new IllegalStateException("creation rejected before write");
                }
                completedCreates.add(pathCreated);
                return null;
            }).when(client).create(any(AAIResourceUri.class), any());
            doAnswer(call -> {
                AAIResourceUri from = call.getArgument(0);
                AAIResourceUri to = call.getArgument(1);
                mutations.add("connect:" + from.build().getPath() + "->" + to.build().getPath());
                if (rejectConnect) {
                    throw new IllegalStateException("relationship rejected after creation");
                }
                return null;
            }).when(client).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));
        });
        variables.put("msoRequestId", "request-1");
        variables.put("serviceInstanceId", "service-1");
        variables.put("vnfId", "vnf-1");
        variables.put("vnfName", "name & 東京");
        variables.put("vnfType", "type-1");
        variables.put("productFamilyId", "family-1");
        variables.put("disableRollback", "false");
        variables.put("lcpCloudRegionId", "region-1");
        variables.put("tenantId", "tenant-1");
        variables.put("vnfModelInfo", "{\"modelInvariantUuid\":\"invariant-1\",\"modelUuid\":\"version-1\","
                + "\"modelCustomizationUuid\":\"custom-1\"}");
        variables.put("serviceModelInfo", "{}");
        VnfResource resource = new VnfResource();
        resource.setNfType("nf-type-1");
        resource.setNfRole("role-1");
        resource.setNfFunction("function-1");
        resource.setNfNamingCode("naming-1");
        variables.put("vnfResourceDecomposition", resource);
    }

    @After
    public void restoreExternalClientsAndEnvironment() {
        try {
            if (inventories != null) {
                inventories.close();
            }
            if (catalogs != null) {
                catalogs.close();
            }
        } finally {
            new UrnPropertiesReader().setEnvironment(previousEnvironment);
        }
    }

    @Test
    public void creationConnectsTheOwningServiceAndPublishesTypedCleanupIdentity() {
        ProcessInstance process = startAndReopenCompleted();
        assertEquals(List.of("create:" + VNF, "connect:" + VNF + "->" + SERVICE), mutations);
        assertEquals(List.of(VNF), completedCreates);
        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put("vnf-id", "vnf-1");
        expected.put("vnf-name", "name & 東京");
        expected.put("service-id", "family-1");
        expected.put("vnf-type", "type-1");
        expected.put("prov-status", "PREPROV");
        expected.put("orchestration-status", "Created");
        expected.put("model-invariant-id", "invariant-1");
        expected.put("model-version-id", "version-1");
        expected.put("model-customization-id", "custom-1");
        expected.put("nf-type", "nf-type-1");
        expected.put("nf-role", "role-1");
        expected.put("nf-function", "function-1");
        expected.put("nf-naming-code", "naming-1");
        assertEquals(expected, payload);
        payload.values().forEach(value -> assertEquals(String.class, value.getClass()));
        assertEquals("vnf-1", rollback(process).get("VNF", "vnfId"));
        assertEquals("true", rollback(process).get("VNF", "rollbackVnfCreate"));
        assertNull(historic(process, "WorkflowException"));
        assertEquals(Boolean.FALSE, historic(process, "SDNCInteractionEnabled"));
        assertTrue(resolutions > 0);
        activity(process, "initialization", 1);
        activity(process, "callGetService", 1);
        activity(process, "prepareCreateGenericVnf", 1);
        activity(process, "postProcessCreateGenericVnf", 1);
        noController(process);
    }

    @Test
    public void missingModelIdsAndStringZeroOrFalseRemainTypedInventoryValues() {
        variables.put("vnfModelInfo", "{\"modelInvariantUuid\":\"invariant-1\"}");
        VnfResource resource = (VnfResource) variables.get("vnfResourceDecomposition");
        resource.setNfRole("0");
        resource.setNfFunction("false");
        ProcessInstance process = startAndReopenCompleted();
        assertEquals("", payload.get("model-version-id"));
        assertEquals("", payload.get("model-customization-id"));
        assertEquals("0", payload.get("nf-role"));
        assertEquals("false", payload.get("nf-function"));
        assertEquals("true", rollback(process).get("VNF", "rollbackVnfCreate"));
        assertEquals(2, mutations.size());
    }

    @Test
    public void missingServicePreventsAnyInventoryMutation() {
        serviceExists = false;
        ProcessInstance process = startAndReopenCompleted();
        error(process, 2500);
        assertTrue(mutations.isEmpty());
        assertTrue(completedCreates.isEmpty());
        assertEquals(0, resolutions);
        assertEquals("false", rollback(process).get("VNF", "rollbackVnfCreate"));
        activity(process, "prepareCreateGenericVnf", 0);
        noController(process);
    }

    @Test
    public void unavailableServiceLookupPreventsMutationAndRetainsItsBusinessError() {
        rejectLookup = true;
        ProcessInstance process = startAndReopenCompleted();
        error(process, 7000);
        assertTrue(mutations.isEmpty());
        assertTrue(completedCreates.isEmpty());
        activity(process, "prepareCreateGenericVnf", 0);
        noController(process);
    }

    @Test
    public void rejectedCreationDoesNotAuthorizeCleanupForAnUncreatedResource() {
        rejectCreate = true;
        ProcessInstance process = startAndReopenCompleted();
        error(process, 2500);
        assertEquals(List.of("create:" + VNF), mutations);
        assertTrue(completedCreates.isEmpty());
        assertEquals("false", rollback(process).get("VNF", "rollbackVnfCreate"));
        activity(process, "postProcessCreateGenericVnf", 0);
        noController(process);
    }

    @Test
    public void relationshipFailureStopsWithoutRetryingTheCreation() {
        rejectConnect = true;
        ProcessInstance process = startAndReopenCompleted();
        error(process, 2500);
        assertEquals(List.of(VNF), completedCreates);
        assertEquals(List.of("create:" + VNF, "connect:" + VNF + "->" + SERVICE), mutations);
        activity(process, "prepareCreateGenericVnf", 1);
        activity(process, "postProcessCreateGenericVnf", 0);
        noController(process);
    }

    @Test
    public void relationshipFailureRetainsCleanupEligibilityForTheCreatedVnf() {
        Assume.assumeTrue("SO-4457: a failed VNF relationship leaves the created resource unrecorded for rollback",
                Boolean.getBoolean("groovy.migration.verification"));
        rejectConnect = true;
        ProcessInstance process = startAndReopenCompleted();
        error(process, 2500);
        assertEquals(List.of(VNF), completedCreates);
        assertEquals(List.of("create:" + VNF, "connect:" + VNF + "->" + SERVICE), mutations);
        assertEquals("vnf-1", rollback(process).get("VNF", "vnfId"));
        assertEquals("true", rollback(process).get("VNF", "rollbackVnfCreate"));
    }

    private ProcessInstance startAndReopenCompleted() {
        ProcessInstance process = engine.getRuntimeService().startProcessInstanceByKey("DoCreateVnf", variables);
        restart();
        assertEquals(0,
                engine.getRuntimeService().createProcessInstanceQuery().processInstanceId(process.getId()).count());
        assertEquals(1, engine.getHistoryService().createHistoricProcessInstanceQuery()
                .processInstanceId(process.getId()).finished().count());
        for (var client : inventories.constructed()) {
            mockingDetails(client).getInvocations()
                    .forEach(call -> assertTrue("Unexpected inventory action: " + call.getMethod().getName(),
                            List.of("exists", "get", "create", "connect").contains(call.getMethod().getName())));
        }
        for (var client : catalogs.constructed()) {
            mockingDetails(client).getInvocations()
                    .forEach(call -> assertEquals("Only the external homing lookup is expected", "getHomingInstance",
                            call.getMethod().getName()));
        }
        return process;
    }

    private Object historic(ProcessInstance process, String name) {
        var value = engine.getHistoryService().createHistoricVariableInstanceQuery().processInstanceId(process.getId())
                .variableName(name).singleResult();
        return value == null ? null : value.getValue();
    }

    private RollbackData rollback(ProcessInstance process) {
        Object value = historic(process, "rollbackData");
        assertNotNull(value);
        assertEquals(RollbackData.class, value.getClass());
        return (RollbackData) value;
    }

    private void error(ProcessInstance process, int code) {
        Object value = historic(process, "WorkflowException");
        assertNotNull(value);
        assertEquals(WorkflowException.class, value.getClass());
        assertEquals(code, ((WorkflowException) value).getErrorCode());
        assertEquals("DoCreateVnf", ((WorkflowException) value).getProcessKey());
    }

    private void activity(ProcessInstance process, String activity, long count) {
        assertEquals(activity, count, engine.getHistoryService().createHistoricActivityInstanceQuery()
                .processInstanceId(process.getId()).activityId(activity).count());
    }

    private void noController(ProcessInstance process) {
        activity(process, "callSDNCAdapterVNFTopologyAssign", 0);
        activity(process, "callSDNCAdapterVNFTopologyActivate", 0);
    }
}
