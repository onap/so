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
import org.onap.aai.domain.yang.VfModule;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.bpmn.common.scripts.UpdateAAIVfModule;
import org.onap.so.bpmn.core.WorkflowException;

public class VfModuleInventoryUpdateProcessTest extends WorkflowPersistenceSupport {
    private static final String PROCESS = "UpdateAAIVfModule";
    private static final String PREFIX = "UAAIVfMod_";
    private static final String VNF_PATH = "/network/generic-vnfs/generic-vnf/vnf-1/vf-modules/vf-module/module-1";
    private final List<String> effects = new ArrayList<>();
    private MockedConstruction<AAIResourcesClient> clients;
    private VfModule patch;
    private boolean missing;
    private boolean queryRejected;
    private boolean updateRejected;
    private boolean updateMissing;
    private boolean legacyInvariant;

    @Before
    public void deployProductionWorkflowAndConfigureExternalInventory() throws Exception {
        engine.getRepositoryService().createDeployment()
                .addInputStream(PROCESS + ".bpmn", new ByteArrayInputStream(productionResource())).deploy();
        clients = mockConstruction(AAIResourcesClient.class, (client, context) -> {
            when(client.get(eq(VfModule.class), any(AAIResourceUri.class))).thenAnswer(call -> {
                AAIResourceUri uri = call.getArgument(1);
                assertEquals(VNF_PATH, uri.build().getPath());
                effects.add("GET");
                if (queryRejected) {
                    throw new IllegalStateException("inventory read rejected");
                }
                VfModule current = new VfModule();
                current.setVfModuleId("module-1");
                if (legacyInvariant) {
                    current.setModelVersionId("model-invariant");
                } else {
                    current.setModelInvariantId("model-invariant");
                }
                return missing ? Optional.empty() : Optional.of(current);
            });
            doAnswer(call -> {
                AAIResourceUri uri = call.getArgument(0);
                assertEquals(VNF_PATH, uri.build().getPath());
                effects.add("PATCH");
                patch = call.getArgument(1);
                if (updateMissing) {
                    throw new jakarta.ws.rs.NotFoundException("inventory module disappeared");
                }
                if (updateRejected) {
                    throw new IllegalStateException("inventory update rejected");
                }
                return null;
            }).when(client).update(any(AAIResourceUri.class), any(VfModule.class));
        });
    }

    @After
    public void restoreExternalInventory() {
        if (clients != null) {
            clients.close();
        }
    }

    @Test
    public void updatedStackAndEscapedMetadataReachOneTypedPatch() {
        ProcessInstance process = start("<heat-stack-id>stack-東京</heat-stack-id>"
                + "<contrail-service-instance-fqdn>domain:tenant:service &amp; é</contrail-service-instance-fqdn>"
                + "<orchestration-status>updated</orchestration-status>");
        successful(process);
        assertEquals("module-1", patch.getVfModuleId());
        assertEquals("stack-東京", patch.getHeatStackId());
        assertEquals("domain:tenant:service & é", patch.getContrailServiceInstanceFqdn());
        assertEquals("updated", patch.getOrchestrationStatus());
    }

    @Test
    public void matchingInvariantAllowsTheRequestedModelVersion() {
        successful(start(model("model-invariant")));
        assertEquals("requested-version", patch.getPersonaModelVersion());
    }

    @Test
    public void legacyInventoryModelVersionSupportsTheExistingInvariantFallback() {
        legacyInvariant = true;
        successful(start(model("model-invariant")));
        assertEquals("requested-version", patch.getPersonaModelVersion());
    }

    @Test
    public void deleteMarkersClearOnlyTheRequestedFields() {
        successful(start("<heat-stack-id>DELETE</heat-stack-id>"
                + "<contrail-service-instance-fqdn>DELETE</contrail-service-instance-fqdn>"
                + "<orchestration-status>DELETE</orchestration-status>"));
        assertEquals("", patch.getHeatStackId());
        assertEquals("", patch.getContrailServiceInstanceFqdn());
        assertEquals("", patch.getOrchestrationStatus());
    }

    @Test
    public void absentOptionalFieldsRemainUnsetInThePatch() {
        successful(start(""));
        assertNull(patch.getHeatStackId());
        assertNull(patch.getContrailServiceInstanceFqdn());
        assertNull(patch.getOrchestrationStatus());
    }

    @Test
    public void missingInventorySelectsTheQueryErrorRouteWithoutPatching() {
        missing = true;
        ProcessInstance process = start("");
        failure(process, 5000, "VF Module not found in AAI", "AAIQueryFailure", "EndEvent_7");
        assertEquals(Integer.valueOf(404), historic(process, PREFIX + "getVfModuleResponseCode"));
        assertEquals(Collections.singletonList("GET"), effects);
        activity(process, "UpdateVfModule", 0);
    }

    @Test
    public void rejectedQueryPreservesItsMessageAndNeverAttemptsAPatch() {
        queryRejected = true;
        ProcessInstance process = start("");
        failure(process, 5000, "AAI GET Failed:inventory read rejected", "AAIQueryFailure", "EndEvent_7");
        assertEquals(Integer.valueOf(500), historic(process, PREFIX + "getVfModuleResponseCode"));
        assertEquals(Collections.singletonList("GET"), effects);
        activity(process, "UpdateVfModule", 0);
    }

    @Test
    public void rejectedPatchPreservesTheWorkflowErrorWithoutRetrying() {
        updateRejected = true;
        ProcessInstance process = start("<orchestration-status>updated</orchestration-status>");
        patchFailure(process, "Exception occurred while executing AAI PATCH:inventory update rejected", 500);
        assertEquals("updated", patch.getOrchestrationStatus());
    }

    @Test
    public void disappearingInventoryDuringPatchRemainsDistinctFromTransportFailure() {
        updateMissing = true;
        ProcessInstance process = start("");
        patchFailure(process, "vf-module module-1 not found for under vnf vnf-1 in A&AI!", 404);
    }

    @Test
    public void aDifferentModelInvariantStopsBeforePatching() {
        Assume.assumeTrue("SO-4457: model mismatch logging fails before recording the workflow error",
                Boolean.getBoolean("groovy.migration.verification"));
        ProcessInstance process = start(model("different-model"));
        restart();
        WorkflowException error = (WorkflowException) historic(process, "WorkflowException");
        assertNotNull(error);
        assertEquals(1002, error.getErrorCode());
        assertTrue(error.getErrorMessage().contains("'persona-model-id' mismatch"));
        assertEquals(Collections.singletonList("GET"), effects);
        assertNull(patch);
        activity(process, "EndEvent_4", 0);
    }

    private String model(String invariant) {
        return "<persona-model-id>" + invariant + "</persona-model-id>"
                + "<persona-model-version>requested-version</persona-model-version>";
    }

    private void patchFailure(ProcessInstance process, String message, int status) {
        restart();
        WorkflowException error = (WorkflowException) historic(process, "WorkflowException");
        assertNotNull(error);
        assertEquals(2500, error.getErrorCode());
        assertEquals(message, error.getErrorMessage());
        assertEquals(Integer.valueOf(status), historic(process, PREFIX + "updateVfModuleResponseCode"));
        assertEquals(Arrays.asList("GET", "PATCH"), effects);
        assertNotNull(patch);
        activity(process, "EndEvent_4", 0);
        activity(process, "AAIQueryFailure", 0);
    }

    private ProcessInstance start(String fields) {
        return engine.getRuntimeService().startProcessInstanceByKey(PROCESS,
                Collections.singletonMap(PROCESS + "Request",
                        "<" + PROCESS + "Request><vnf-id>vnf-1</vnf-id><vf-module-id>module-1</vf-module-id>" + fields
                                + "</" + PROCESS + "Request>"));
    }

    private void successful(ProcessInstance process) {
        restart();
        assertEquals(Arrays.asList("GET", "PATCH"), effects);
        assertNotNull(patch);
        assertNull(historic(process, "WorkflowException"));
        assertEquals(Integer.valueOf(200), historic(process, PREFIX + "getVfModuleResponseCode"));
        assertEquals(Integer.valueOf(200), historic(process, PREFIX + "updateVfModuleResponseCode"));
        assertEquals("Success", historic(process, PREFIX + "updateVfModuleResponse"));
        activity(process, "ProcessReceivedRequest", 1);
        activity(process, "QueryAAIForVfModule", 1);
        activity(process, "UpdateVfModule", 1);
        activity(process, "EndEvent_4", 1);
        activity(process, "AAIQueryFailure", 0);
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
        Path location = Paths.get(UpdateAAIVfModule.class.getProtectionDomain().getCodeSource().getLocation().toURI());
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
