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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class GenericNotificationProcessTest {
    private static final String PROCESS = "GenericNotificationService";
    private static final String RESOURCE = "subprocess/GenericNotificationService.bpmn";
    private static ProcessEngine engine;

    @BeforeClass
    public static void deployProductionProcess() throws Exception {
        ProcessEngineConfigurationImpl configuration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
                .createStandaloneInMemProcessEngineConfiguration();
        String name = "notification-" + UUID.randomUUID();
        configuration.setProcessEngineName(name);
        configuration.setJdbcUrl("jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP);
        configuration.setHistory(ProcessEngineConfiguration.HISTORY_FULL);
        configuration.setEnforceHistoryTimeToLive(false);
        configuration.setJavaSerializationFormatEnabled(true);
        configuration.setJobExecutorActivate(false);
        engine = configuration.buildProcessEngine();
        URL resource = GenericNotificationProcessTest.class.getClassLoader().getResource(RESOURCE);
        assertNotNull(resource);
        String location = resource.toExternalForm();
        assertTrue(location,
                location.contains("/MSOCommonBPMN/target/classes/")
                        || (location.matches("jar:.*/MSOCommonBPMN-[^/]+\\.jar!/" + RESOURCE)
                                && !location.contains("-tests.jar!/")));
        try (InputStream input = resource.openStream()) {
            engine.getRepositoryService().createDeployment().addInputStream(RESOURCE, input).deploy();
        }
        ProcessDefinition definition = engine.getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionKey(PROCESS).singleResult();
        assertNotNull(definition);
        try (InputStream original = resource.openStream();
                InputStream deployed = engine.getRepositoryService().getResourceAsStream(definition.getDeploymentId(),
                        definition.getResourceName())) {
            assertArrayEquals(original.readAllBytes(), deployed.readAllBytes());
        }
    }

    @AfterClass
    public static void closeEngine() {
        if (engine != null) {
            engine.close();
        }
    }

    @Test
    public void theShippedNotificationScriptCompletesWithoutRequiringInputs() {
        ProcessInstance process = start(new HashMap<>());
        assertEquals(0, engine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(process.getId()).count());
    }

    @Test
    public void theNotificationPreservesTypedAndExplicitlyEmptyCallerState() {
        Map<String, Object> input = new HashMap<>();
        input.put("zero", 0);
        input.put("disabled", false);
        input.put("empty", "");
        input.put("unset", null);
        input.put("items", new ArrayList<>());
        input.put("message", "Gr\u00fc\u00dfe & \u96ea");
        ProcessInstance process = start(input);
        assertEquals(Integer.class, variable(process, "zero").getValue().getClass());
        assertEquals(0, variable(process, "zero").getValue());
        assertEquals(Boolean.FALSE, variable(process, "disabled").getValue());
        assertEquals("", variable(process, "empty").getValue());
        assertNull(variable(process, "unset").getValue());
        assertEquals(new ArrayList<>(), variable(process, "items").getValue());
        assertEquals(input.get("message"), variable(process, "message").getValue());
        assertEquals(input.size(), engine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(process.getId()).count());
    }

    private ProcessInstance start(Map<String, Object> input) {
        ProcessInstance process =
                engine.getRuntimeService().startProcessInstanceByKey(PROCESS, UUID.randomUUID().toString(), input);
        assertEquals(0,
                engine.getRuntimeService().createProcessInstanceQuery().processInstanceId(process.getId()).count());
        for (String activity : new String[] {"StartEvent_1", "Task_1tj48xt", "EndEvent_0u9cxx7"}) {
            assertEquals(activity, 1, engine.getHistoryService().createHistoricActivityInstanceQuery()
                    .processInstanceId(process.getId()).activityId(activity).finished().count());
        }
        assertEquals(0, engine.getManagementService().createJobQuery().processInstanceId(process.getId()).count());
        return process;
    }

    private HistoricVariableInstance variable(ProcessInstance process, String name) {
        HistoricVariableInstance value = engine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(process.getId()).variableName(name).singleResult();
        assertNotNull(name, value);
        return value;
    }
}
