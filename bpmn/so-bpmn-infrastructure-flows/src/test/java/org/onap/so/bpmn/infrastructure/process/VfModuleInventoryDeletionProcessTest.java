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

import static org.junit.Assert.assertArrayEquals;
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
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarFile;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.VfModule;
import org.onap.aai.domain.yang.VfModules;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.bpmn.common.scripts.DeleteAAIVfModule;
import org.onap.so.bpmn.common.verification.WorkflowPersistenceSupport;
import org.onap.so.bpmn.core.WorkflowException;

public class VfModuleInventoryDeletionProcessTest extends WorkflowPersistenceSupport {
    private static final String PARENT = "DoDeleteVfModule";
    private static final String CHILD = "DeleteAAIVfModule";
    private static final String VNF = "/network/generic-vnfs/generic-vnf/vnf-1";
    private static final String MODULE = VNF + "/vf-modules/vf-module/module-1";
    private final List<String> effects = new ArrayList<>();
    private MockedConstruction<AAIResourcesClient> clients;
    private boolean base = true;
    private boolean anotherModule;
    private boolean moduleMissing;
    private boolean vnfMissing;
    private boolean moduleDeleted;
    private boolean queryFailure;
    private String failedDelete;

    @Before
    public void deployRealParentAndChildAndConfigureExternalInventory() throws Exception {
        deploy(PARENT, productionFlow());
        deploy(CHILD, productionCommon());
        clients = mockConstruction(AAIResourcesClient.class, (client, context) -> {
            when(client.get(eq(GenericVnf.class), any(AAIResourceUri.class))).thenAnswer(call -> {
                AAIResourceUri uri = call.getArgument(1);
                assertTrue(uri.build().getPath(), uri.build().getPath().endsWith(VNF));
                assertTrue(uri.build().getRawQuery(), uri.build().getRawQuery().contains("depth=1"));
                effects.add("GET VNF");
                if (queryFailure) {
                    throw new IllegalStateException("inventory read rejected");
                }
                return vnfMissing ? Optional.empty() : Optional.of(inventory());
            });
            doAnswer(call -> {
                AAIResourceUri uri = call.getArgument(0);
                String path = uri.build().getPath();
                boolean isModule = path.endsWith(MODULE);
                assertTrue(path, isModule || path.endsWith(VNF));
                String operation = isModule ? "MODULE" : "VNF";
                effects.add("DELETE " + operation);
                if (operation.equals(failedDelete)) {
                    throw new IllegalStateException("inventory delete rejected");
                }
                if (isModule) {
                    moduleDeleted = true;
                }
                return null;
            }).when(client).delete(any(AAIResourceUri.class));
        });
    }

    @After
    public void restoreTheExternalInventoryBoundary() {
        if (clients != null) {
            clients.close();
        }
    }

    @Test
    public void anAddonDeletionPreservesTheVnfAndOtherModule() {
        base = false;
        anotherModule = true;
        ProcessInstance parent = start("false");
        completed(parent, Arrays.asList("GET VNF", "DELETE MODULE"));
        childActivity(parent, "DeleteVfModule", 1);
        childActivity(parent, "DeleteGenericVnf", 0);
    }

    @Test
    public void aVidRequestDeletesItsLastModuleAndPreservesTheVnf() {
        ProcessInstance parent = start("true");
        completed(parent, Arrays.asList("GET VNF", "DELETE MODULE"));
        childActivity(parent, "DeleteVfModule", 1);
        childActivity(parent, "QueryAAIForGenericVnf1", 0);
        childActivity(parent, "DeleteGenericVnf", 0);
    }

    @Test
    public void aNonVidLastModuleDeletionRefreshesInventoryBeforeDeletingTheVnf() {
        ProcessInstance parent = start("false");
        completed(parent, Arrays.asList("GET VNF", "DELETE MODULE", "GET VNF", "DELETE VNF"));
        childActivity(parent, "DeleteVfModule", 1);
        childActivity(parent, "QueryAAIForGenericVnf1", 1);
        childActivity(parent, "DeleteGenericVnf", 1);
    }

    @Test
    public void aBaseModuleWithAnAddonIsRejectedWithoutDeletingInventory() {
        anotherModule = true;
        ProcessInstance parent = start("false");
        rejected(parent, 1002, Arrays.asList("GET VNF"));
        childActivity(parent, "DeleteVfModule", 0);
    }

    @Test
    public void anAbsentModuleIsRejectedWithoutDeletingTheVnf() {
        moduleMissing = true;
        ProcessInstance parent = start("false");
        rejected(parent, 1002, Arrays.asList("GET VNF"));
        childActivity(parent, "DeleteVfModule", 0);
    }

    @Test
    public void anAbsentVnfIsRejectedBeforeAnyDeletion() {
        vnfMissing = true;
        ProcessInstance parent = start("false");
        rejected(parent, 1002, Arrays.asList("GET VNF"));
        childActivity(parent, "DeleteVfModule", 0);
    }

    @Test
    public void anInventoryReadFailureStopsBeforeDeletion() {
        queryFailure = true;
        ProcessInstance parent = start("false");
        rejected(parent, 5000, Arrays.asList("GET VNF"));
        childActivity(parent, "DeleteVfModule", 0);
    }

    @Test
    public void aModuleDeletionFailureStopsBeforeVnfDeletion() {
        failedDelete = "MODULE";
        ProcessInstance parent = start("false");
        rejected(parent, 5000, Arrays.asList("GET VNF", "DELETE MODULE"));
        childActivity(parent, "DeleteVfModule", 1);
        childActivity(parent, "DeleteGenericVnf", 0);
    }

    @Test
    public void aVnfDeletionFailureRetainsTheCompletedModuleDeletionAndItsError() {
        failedDelete = "VNF";
        ProcessInstance parent = start("false");
        rejected(parent, 5000, Arrays.asList("GET VNF", "DELETE MODULE", "GET VNF", "DELETE VNF"));
        assertTrue(moduleDeleted);
        childActivity(parent, "DeleteGenericVnf", 1);
    }

    private ProcessInstance start(String vid) {
        ProcessInstance parent = engine.getRuntimeService().createProcessInstanceByKey(PARENT)
                .setVariable("vnfId", "vnf-1").setVariable("vfModuleId", "module-1").setVariable("isVidRequest", vid)
                .startBeforeActivity("DeleteAAIVfModulePrep").execute();
        assertEquals(vid, variable(child(parent).getId(), "isVidRequest"));
        return parent;
    }

    private void completed(ProcessInstance parent, List<String> expected) {
        ended(parent, expected);
        assertEquals(1, engine.getHistoryService().createHistoricActivityInstanceQuery()
                .processInstanceId(parent.getId()).activityId("EndEvent_9").finished().count());
        assertNull(variable(child(parent).getId(), "WorkflowException"));
        assertEquals(Integer.valueOf(200), variable(child(parent).getId(), "DAAIVfMod_deleteVfModuleResponseCode"));
    }

    private void rejected(ProcessInstance parent, int code, List<String> expected) {
        ended(parent, expected);
        Object value = variable(child(parent).getId(), "WorkflowException");
        assertTrue(value instanceof WorkflowException);
        WorkflowException error = (WorkflowException) value;
        assertEquals(CHILD, error.getProcessKey());
        assertEquals(code, error.getErrorCode());
        Object returned = variable(parent.getId(), "WorkflowException");
        assertTrue(returned instanceof WorkflowException);
        WorkflowException parentError = (WorkflowException) returned;
        assertEquals(error.getProcessKey(), parentError.getProcessKey());
        assertEquals(error.getErrorCode(), parentError.getErrorCode());
        assertEquals(error.getErrorMessage(), parentError.getErrorMessage());
    }

    private void ended(ProcessInstance parent, List<String> expected) {
        assertEquals(expected, effects);
        restart();
        assertEquals(0, engine.getRuntimeService().createProcessInstanceQuery().count());
        assertEquals(1, engine.getHistoryService().createHistoricProcessInstanceQuery()
                .processInstanceId(parent.getId()).finished().count());
        assertEquals(2, engine.getHistoryService().createHistoricProcessInstanceQuery().count());
        assertEquals(1, engine.getHistoryService().createHistoricActivityInstanceQuery()
                .processInstanceId(parent.getId()).activityId("DeleteAAIVfModulePrep").finished().count());
        assertEquals(1, engine.getHistoryService().createHistoricActivityInstanceQuery()
                .processInstanceId(parent.getId()).activityId("InvokeDeleteAAIVfModule").finished().count());
    }

    private HistoricProcessInstance child(ProcessInstance parent) {
        HistoricProcessInstance child = engine.getHistoryService().createHistoricProcessInstanceQuery()
                .superProcessInstanceId(parent.getId()).processDefinitionKey(CHILD).singleResult();
        assertNotNull(child);
        assertNotNull(child.getEndTime());
        return child;
    }

    private void childActivity(ProcessInstance parent, String activity, int expected) {
        assertEquals(activity, expected, engine.getHistoryService().createHistoricActivityInstanceQuery()
                .processInstanceId(child(parent).getId()).activityId(activity).count());
    }

    private Object variable(String process, String name) {
        var value = engine.getHistoryService().createHistoricVariableInstanceQuery().processInstanceId(process)
                .variableName(name).singleResult();
        return value == null ? null : value.getValue();
    }

    private GenericVnf inventory() {
        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId("vnf-1");
        vnf.setResourceVersion(moduleDeleted ? "refreshed-version" : "initial-version");
        VfModules modules = new VfModules();
        vnf.setVfModules(modules);
        if (!moduleMissing && !moduleDeleted) {
            modules.getVfModule().add(module("module-1", base));
        }
        if (anotherModule) {
            modules.getVfModule().add(module("other-module", !base));
        }
        return vnf;
    }

    private static VfModule module(String id, boolean base) {
        VfModule module = new VfModule();
        module.setVfModuleId(id);
        module.setIsBaseVfModule(base);
        module.setResourceVersion("module-version");
        return module;
    }

    private byte[] productionFlow() throws Exception {
        String name = "subprocess/" + PARENT + ".bpmn";
        URL resource = getClass().getClassLoader().getResource(name);
        assertNotNull(resource);
        String location = resource.toExternalForm();
        assertTrue(location,
                location.contains("/so-bpmn-infrastructure-flows/target/classes/")
                        || (location.matches("jar:.*/so-bpmn-infrastructure-flows-[^/]+\\.jar!/" + name)
                                && !location.contains("-tests.jar!/")));
        try (InputStream input = resource.openStream()) {
            return input.readAllBytes();
        }
    }

    private byte[] productionCommon() throws Exception {
        String name = "subprocess/" + CHILD + ".bpmn";
        Path location = Paths.get(DeleteAAIVfModule.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        if (Files.isDirectory(location)) {
            return Files.readAllBytes(location.resolve(name));
        }
        try (JarFile jar = new JarFile(location.toFile())) {
            assertNotNull(jar.getJarEntry(name));
            try (InputStream input = jar.getInputStream(jar.getJarEntry(name))) {
                return input.readAllBytes();
            }
        }
    }

    private void deploy(String process, byte[] bytes) throws Exception {
        String name = "subprocess/" + process + ".bpmn";
        engine.getRepositoryService().createDeployment().addInputStream(name, new ByteArrayInputStream(bytes)).deploy();
        ProcessDefinition definition = engine.getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionKey(process).singleResult();
        try (InputStream deployed = engine.getRepositoryService().getResourceAsStream(definition.getDeploymentId(),
                definition.getResourceName())) {
            assertArrayEquals(bytes, deployed.readAllBytes());
        }
    }
}
