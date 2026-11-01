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
package org.onap.so.bpmn.common.verification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarFile;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Assume;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.bpmn.common.scripts.UpdateAAIGenericVnf;
import org.onap.so.bpmn.core.WorkflowException;

public class GenericVnfInventoryUpdateProcessTest extends WorkflowPersistenceSupport {
    private static final String PROCESS = "UpdateAAIGenericVnf";
    private static final String PREFIX = "UAAIGenVnf_";
    private static final String VNF_PATH = "/network/generic-vnfs/generic-vnf/vnf-1";
    private final List<String> effects = new ArrayList<>();
    private MockedConstruction<AAIResourcesClient> clients;
    private GenericVnf patch;
    private boolean missing;
    private boolean queryRejected;
    private boolean updateRejected;

    @Before
    public void deployProductionWorkflowAndConfigureExternalInventory() throws Exception {
        engine.getRepositoryService().createDeployment()
                .addInputStream(PROCESS + ".bpmn", new ByteArrayInputStream(productionResource())).deploy();
        clients = mockConstruction(AAIResourcesClient.class, (client, context) -> {
            when(client.get(eq(GenericVnf.class), any(AAIResourceUri.class))).thenAnswer(call -> {
                AAIResourceUri uri = call.getArgument(1);
                assertEquals(VNF_PATH, uri.build().getPath());
                assertTrue(uri.build().getRawQuery().contains("depth=1"));
                effects.add("GET");
                if (queryRejected) {
                    throw new IllegalStateException("inventory read rejected");
                }
                GenericVnf current = new GenericVnf();
                current.setVnfId("vnf-1");
                current.setModelInvariantId("model-invariant");
                current.setIpv4OamAddress("192.0.2.1");
                return missing ? Optional.empty() : Optional.of(current);
            });
            doAnswer(call -> {
                AAIResourceUri uri = call.getArgument(0);
                assertEquals(VNF_PATH, uri.build().getPath());
                effects.add("PATCH");
                patch = call.getArgument(1);
                if (updateRejected) {
                    throw new IllegalStateException("inventory update rejected");
                }
                return null;
            }).when(client).update(any(AAIResourceUri.class), any(GenericVnf.class));
        });
    }

    @After
    public void restoreExternalInventory() {
        if (clients != null) {
            clients.close();
        }
    }

    @Test
    public void updatedAddressesAndEscapedStatusReachOneTypedPatch() {
        ProcessInstance process = start("<ipv4-oam-address>192.0.2.9</ipv4-oam-address>"
                + "<management-v6-address>2001:db8::9</management-v6-address>"
                + "<orchestration-status>updated &amp; 東京</orchestration-status>");
        successful(process);
        assertEquals("vnf-1", patch.getVnfId());
        assertEquals("192.0.2.9", patch.getIpv4OamAddress());
        assertEquals("2001:db8::9", patch.getManagementV6Address());
        assertEquals("updated & 東京", patch.getOrchestrationStatus());
        assertNull(patch.getPersonaModelVersion());
    }

    @Test
    public void matchingInvariantAllowsTheRequestedModelVersion() {
        ProcessInstance process = start("<persona-model-id>model-invariant</persona-model-id>"
                + "<persona-model-version>legacy-version</persona-model-version>"
                + "<model-version-id>requested-version</model-version-id>");
        successful(process);
        assertEquals("requested-version", patch.getPersonaModelVersion());
    }

    @Test
    public void deleteMarkersClearOnlyTheRequestedFields() {
        ProcessInstance process = start(
                "<ipv4-oam-address>DELETE</ipv4-oam-address>" + "<management-v6-address>DELETE</management-v6-address>"
                        + "<orchestration-status>DELETE</orchestration-status>");
        successful(process);
        assertEquals("", patch.getIpv4OamAddress());
        assertEquals("", patch.getManagementV6Address());
        assertEquals("", patch.getOrchestrationStatus());
    }

    @Test
    public void absentOptionalFieldsRemainUnsetInThePatch() {
        successful(start(""));
        assertNull(patch.getIpv4OamAddress());
        assertNull(patch.getManagementV6Address());
        assertNull(patch.getOrchestrationStatus());
        assertNull(patch.getPersonaModelVersion());
    }

    @Test
    public void emptyAddressElementsRemainUnsetRatherThanRequestDeletion() {
        successful(start("<ipv4-oam-address/><management-v6-address/><orchestration-status/>"));
        assertNull(patch.getIpv4OamAddress());
        assertNull(patch.getManagementV6Address());
        assertNull(patch.getOrchestrationStatus());
    }

    @Test
    public void missingInventorySelectsTheQueryErrorRouteWithoutPatching() {
        missing = true;
        ProcessInstance process = start("");
        failure(process, 5000, "Generic VNF not found for VNF ID: vnf-1", "AAIQueryFailure", "EndEvent_7");
        assertEquals(Integer.valueOf(404), historic(process, PREFIX + "getGenericVnfResponseCode"));
        assertEquals(Collections.singletonList("GET"), effects);
        activity(process, "UpdateGenericVNF", 0);
    }

    @Test
    public void rejectedQueryPreservesItsMessageAndNeverAttemptsAPatch() {
        queryRejected = true;
        ProcessInstance process = start("");
        failure(process, 5000, "AAI GET Failed:inventory read rejected", "AAIQueryFailure", "EndEvent_7");
        assertEquals(Integer.valueOf(500), historic(process, PREFIX + "getGenericVnfResponseCode"));
        assertEquals(Collections.singletonList("GET"), effects);
        activity(process, "UpdateGenericVNF", 0);
    }

    @Test
    public void rejectedPatchSelectsTheUpdateErrorRouteWithoutRetrying() {
        updateRejected = true;
        ProcessInstance process = start("<orchestration-status>updated</orchestration-status>");
        failure(process, 5000, "AAI PATCH Failed:inventory update rejected", "HandleUpdateGenericVNFFailure",
                "EndEvent_2");
        assertEquals(Integer.valueOf(500), historic(process, PREFIX + "updateGenericVnfResponseCode"));
        assertEquals(Arrays.asList("GET", "PATCH"), effects);
        assertEquals("updated", patch.getOrchestrationStatus());
    }

    @Test
    public void aDifferentModelInvariantStopsBeforePatching() {
        Assume.assumeTrue("SO-4457: model mismatch logging fails before recording the workflow error",
                Boolean.getBoolean("groovy.migration.verification"));
        ProcessInstance process = start("<persona-model-id>different-model</persona-model-id>"
                + "<persona-model-version>v2</persona-model-version>");
        restart();
        WorkflowException error = (WorkflowException) historic(process, "WorkflowException");
        assertNotNull(error);
        assertEquals(1002, error.getErrorCode());
        assertTrue(error.getErrorMessage().contains("'persona-model-id' mismatch"));
        assertEquals(Collections.singletonList("GET"), effects);
        assertNull(patch);
        activity(process, "EndEvent_4", 0);
    }

    private ProcessInstance start(String fields) {
        return engine.getRuntimeService().startProcessInstanceByKey(PROCESS,
                Collections.singletonMap(PROCESS + "Request",
                        "<" + PROCESS + "Request><vnf-id>vnf-1</vnf-id>" + fields + "</" + PROCESS + "Request>"));
    }

    private void successful(ProcessInstance process) {
        restart();
        assertEquals(Arrays.asList("GET", "PATCH"), effects);
        assertNotNull(patch);
        assertNull(historic(process, "WorkflowException"));
        assertEquals(Integer.valueOf(200), historic(process, PREFIX + "getGenericVnfResponseCode"));
        assertEquals(Integer.valueOf(200), historic(process, PREFIX + "updateGenericVnfResponseCode"));
        assertEquals("Success", historic(process, PREFIX + "updateGenericVnfResponse"));
        activity(process, "ProcessReceivedRequest", 1);
        activity(process, "QueryAAIForGenericVNF", 1);
        activity(process, "UpdateGenericVNF", 1);
        activity(process, "EndEvent_4", 1);
        activity(process, "AAIQueryFailure", 0);
        activity(process, "HandleUpdateGenericVNFFailure", 0);
        assertEquals(0,
                engine.getRuntimeService().createProcessInstanceQuery().processInstanceId(process.getId()).count());
    }

    private void failure(ProcessInstance process, int code, String message, String handler, String end) {
        restart();
        Object value = historic(process, "WorkflowException");
        assertNotNull(value);
        assertEquals(WorkflowException.class, value.getClass());
        WorkflowException error = (WorkflowException) value;
        assertEquals(code, error.getErrorCode());
        assertEquals(message, error.getErrorMessage());
        activity(process, handler, 1);
        activity(process, end, 1);
        activity(process, "EndEvent_4", 0);
    }

    private Object historic(ProcessInstance process, String variable) {
        var value = engine.getHistoryService().createHistoricVariableInstanceQuery().processInstanceId(process.getId())
                .variableName(variable).singleResult();
        return value == null ? null : value.getValue();
    }

    private void activity(ProcessInstance process, String activity, long count) {
        assertEquals(activity, count, engine.getHistoryService().createHistoricActivityInstanceQuery()
                .processInstanceId(process.getId()).activityId(activity).count());
    }

    private static byte[] productionResource() throws Exception {
        String name = "subprocess/" + PROCESS + ".bpmn";
        Path location =
                Paths.get(UpdateAAIGenericVnf.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        if (Files.isDirectory(location)) {
            return Files.readAllBytes(location.resolve(name));
        }
        try (JarFile jar = new JarFile(location.toFile())) {
            var entry = jar.getJarEntry(name);
            assertNotNull(name, entry);
            try (var input = jar.getInputStream(entry)) {
                return input.readAllBytes();
            }
        }
    }
}
