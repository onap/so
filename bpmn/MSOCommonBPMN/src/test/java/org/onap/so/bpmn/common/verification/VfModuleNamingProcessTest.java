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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mockStatic;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.jar.JarFile;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.onap.so.bpmn.common.scripts.GenerateVfModuleName;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.onap.so.bpmn.core.WorkflowException;

public class VfModuleNamingProcessTest {
    private static final String PATH = "/aai/v24/network/generic-vnfs/generic-vnf/vnf-1?depth=1";
    private static ProcessEngine engine;

    @Rule
    public WireMockRule aai = new WireMockRule(0);

    @BeforeClass
    public static void deployProductionProcess() throws Exception {
        String name = "vf-module-name-" + UUID.randomUUID();
        ProcessEngineConfigurationImpl configuration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
                .createStandaloneInMemProcessEngineConfiguration();
        configuration.setProcessEngineName(name);
        configuration.setJdbcUrl("jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP);
        configuration.setHistory(ProcessEngineConfiguration.HISTORY_FULL);
        configuration.setEnforceHistoryTimeToLive(false);
        configuration.setJavaSerializationFormatEnabled(true);
        configuration.setJobExecutorActivate(false);
        engine = configuration.buildProcessEngine();
        engine.getRepositoryService().createDeployment()
                .addInputStream("GenerateVfModuleName.bpmn", new ByteArrayInputStream(productionResource())).deploy();
    }

    @AfterClass
    public static void closeEngine() {
        if (engine != null) {
            engine.close();
        }
    }

    @Test
    public void noModuleCollectionStartsAtZeroAndRecordsTheTypedAaiResponse() {
        ProcessInstance process = generate("<generic-vnf><vnf-id>vnf-1</vnf-id></generic-vnf>", "vnf", "base", "0");
        assertEquals(null, historic(process, "GVFMN_vfModuleXml"));
    }

    @Test
    public void unrelatedModelIndicesDoNotReserveTheRequestedModelName() {
        ProcessInstance process = generate(inventory(module("other", "model-invariant-id", 0)), "vnf", "base", "0");
        assertEquals("<vfModules></vfModules>", historic(process, "GVFMN_vfModuleXml"));
    }

    @Test
    public void currentAndLegacyModelIdentifiersShareTheSameIndexSequence() {
        generate(inventory(module("model-1", "model-invariant-id", 0) + module("model-1", "persona-model-id", 2)
                + module("other", "model-invariant-id", 1)), "vnf", "base", "1");
    }

    @Test
    public void unsortedIndicesAreComparedNumericallyBeforeChoosingTheFirstGap() {
        generate(
                inventory(module("model-1", "model-invariant-id", 10) + module("model-1", "model-invariant-id", 2)
                        + module("model-1", "model-invariant-id", 0) + module("model-1", "model-invariant-id", 1)),
                "vnf", "base", "3");
    }

    @Test
    public void aContiguousSequenceUsesTheNextFreeIndex() {
        generate(inventory(module("model-1", "model-invariant-id", 1) + module("model-1", "model-invariant-id", 0)),
                "vnf", "base", "2");
    }

    @Test
    public void aPresentModernModelIdentifierTakesPrecedenceOverTheLegacyField() {
        generate(
                inventory("<vf-module><model-invariant-id>other</model-invariant-id>"
                        + "<persona-model-id>model-1</persona-model-id><module-index>0</module-index></vf-module>"),
                "vnf", "base", "0");
    }

    @Test
    public void unicodeNamesAndExistingUnderscoresRemainInTheStringResult() {
        generate("<generic-vnf/>", "vnf_東京", "module_é", "0");
    }

    @Test
    public void malformedSuccessfulInventoryStopsBeforeGeneratingAName() {
        assertInventoryError(start(200, "<generic-vnf>", "vnf", "base"));
    }

    @Test
    public void emptySuccessfulInventoryStopsBeforeGeneratingAName() {
        assertInventoryError(start(200, "", "vnf", "base"));
    }

    @Test
    public void anEmptyModuleCollectionStartsAtZero() {
        Assume.assumeTrue("SO-4446: empty VF-module collections fail before name generation",
                Boolean.getBoolean("groovy.migration.verification"));
        generate(inventory(""), "vnf", "base", "0");
    }

    @Test
    public void rejectedInventoryCannotProduceASuccessfulName() {
        Assume.assumeTrue("SO-4446: rejected AAI inventory is treated as an unused module index",
                Boolean.getBoolean("groovy.migration.verification"));
        ProcessInstance process = start(500, "<error>inventory unavailable</error>", "vnf", "base");
        HistoricVariableInstance error = engine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(process.getId()).variableName("WorkflowException").singleResult();
        assertNotNull("Rejected inventory must produce a workflow failure", error);
        assertEquals(WorkflowException.class, error.getValue().getClass());
        assertEquals(0, engine.getHistoryService().createHistoricActivityInstanceQuery()
                .processInstanceId(process.getId()).activityId("PostProcessAAIResponse").count());
        assertAaiRequest();
    }

    private ProcessInstance generate(String body, String vnfName, String label, String index) {
        ProcessInstance process = start(200, body, vnfName, label);
        assertEquals(0,
                engine.getRuntimeService().createProcessInstanceQuery().processInstanceId(process.getId()).count());
        assertEquals(1, engine.getHistoryService().createHistoricProcessInstanceQuery()
                .processInstanceId(process.getId()).finished().count());
        for (String activity : new String[] {"InitializeVariables", "QueryAAIForIndex", "PostProcessAAIResponse",
                "EndEvent_0mj5vyw"}) {
            assertEquals(activity, 1, engine.getHistoryService().createHistoricActivityInstanceQuery()
                    .processInstanceId(process.getId()).activityId(activity).finished().count());
        }
        Object result = historic(process, "vfModuleName");
        assertEquals(String.class, result.getClass());
        assertEquals(vnfName + "_" + label + "_" + index, result);
        assertEquals(Integer.valueOf(200), historic(process, "GVFMN_queryAAIVfModuleResponseCode"));
        assertEquals(Integer.class, historic(process, "GVFMN_queryAAIVfModuleResponseCode").getClass());
        assertEquals(body, historic(process, "GVFMN_queryAAIVfModuleResponse"));
        assertEquals("vnf-1", historic(process, "vnfId"));
        assertEquals("model-1", historic(process, "personaModelId"));
        assertAaiRequest();
        return process;
    }

    private ProcessInstance start(int status, String body, String vnfName, String label) {
        aai.stubFor(get(urlEqualTo(PATH)).willReturn(aResponse().withStatus(status)
                .withHeader("Content-Type", "application/xml").withHeader("Connection", "close").withBody(body)));
        Map<String, Object> input = new HashMap<>();
        input.put("vnfId", "vnf-1");
        input.put("vnfName", vnfName);
        input.put("vfModuleLabel", label);
        input.put("personaModelId", "model-1");
        input.put("GVFMN_vfModuleXml", "stale data");
        ProcessInstance process;
        try (MockedStatic<UrnPropertiesReader> properties = mockStatic(UrnPropertiesReader.class)) {
            properties.when(() -> UrnPropertiesReader.getVariable("aai.endpoint"))
                    .thenReturn("http://localhost:" + aai.port());
            properties.when(() -> UrnPropertiesReader.getVariable("mso.workflow.global.default.aai.version"))
                    .thenReturn("24");
            process = engine.getRuntimeService().startProcessInstanceByKey("GenerateVfModuleName", input);
        }
        return process;
    }

    private void assertInventoryError(ProcessInstance process) {
        Object raw = historic(process, "WorkflowException");
        assertEquals(WorkflowException.class, raw.getClass());
        WorkflowException error = (WorkflowException) raw;
        assertEquals("GenerateVfModuleName", error.getProcessKey());
        assertEquals(1002, error.getErrorCode());
        org.junit.Assert.assertTrue(error.getErrorMessage().startsWith("AAI GET Failed:"));
        assertEquals(0, engine.getHistoryService().createHistoricActivityInstanceQuery()
                .processInstanceId(process.getId()).activityId("PostProcessAAIResponse").count());
        assertEquals(0, engine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(process.getId()).variableName("vfModuleName").count());
        assertAaiRequest();
    }

    private void assertAaiRequest() {
        aai.verify(1, getRequestedFor(urlEqualTo(PATH)).withHeader("Accept", containing("application/xml"))
                .withHeader("Content-Type", equalTo("application/xml")).withHeader("X-FromAppId", equalTo("MSO")));
        assertEquals(1, aai.getAllServeEvents().size());
        String transactions = aai.getAllServeEvents().get(0).getRequest().getHeader("X-TransactionId");
        assertNotNull(transactions);
        for (String transaction : transactions.split(",")) {
            assertEquals(transaction, UUID.fromString(transaction).toString());
        }
    }

    private static Object historic(ProcessInstance process, String name) {
        HistoricVariableInstance variable = engine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(process.getId()).variableName(name).singleResult();
        assertNotNull(name, variable);
        return variable.getValue();
    }

    private static String inventory(String modules) {
        return "<generic-vnf><vf-modules>" + modules + "</vf-modules></generic-vnf>";
    }

    private static String module(String model, String field, int index) {
        return "<vf-module><" + field + ">" + model + "</" + field + "><module-index>" + index
                + "</module-index></vf-module>";
    }

    private static byte[] productionResource() throws Exception {
        String name = "subprocess/GenerateVfModuleName.bpmn";
        Path location =
                Paths.get(GenerateVfModuleName.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        if (Files.isDirectory(location)) {
            return Files.readAllBytes(location.resolve(name));
        }
        try (JarFile jar = new JarFile(location.toFile())) {
            assertNotNull(name, jar.getJarEntry(name));
            try (var input = jar.getInputStream(jar.getJarEntry(name))) {
                return input.readAllBytes();
            }
        }
    }
}
