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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.so.bpmn.core.RollbackData;
import org.onap.so.bpmn.core.WorkflowException;

public class VnfRollbackControlProcessTest {
    private static ProcessEngine engine;

    @BeforeClass
    public static void deployProductionProcess() throws Exception {
        String name = "rollback-control-" + UUID.randomUUID();
        ProcessEngineConfigurationImpl configuration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
                .createStandaloneInMemProcessEngineConfiguration();
        configuration.setProcessEngineName(name);
        configuration.setJdbcUrl("jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP);
        configuration.setHistory(ProcessEngineConfiguration.HISTORY_FULL);
        configuration.setEnforceHistoryTimeToLive(false);
        configuration.setJobExecutorActivate(false);
        engine = configuration.buildProcessEngine();
        try (InputStream production = Files.newInputStream(Paths.get(System.getProperty("basedir"),
                "src/main/resources/subprocess/DoCreateVnfAndModulesRollback.bpmn"))) {
            engine.getRepositoryService().createDeployment()
                    .addInputStream("DoCreateVnfAndModulesRollback.bpmn", production).deploy();
        }
    }

    @AfterClass
    public static void closeEngine() {
        if (engine != null) {
            engine.close();
        }
    }

    @Test
    public void enabledRollbackWithNoCreatedResourcesReachesTheSuccessActivity() {
        String id = start(inputs(new RollbackData(), "false"));

        assertActivity(id, "PreProcessRequest", 1);
        assertActivity(id, "SetSuccessfulRollbackStatus", 1);
        assertActivity(id, "SetFailedRollbackStatus", 0);
        assertEquals(Boolean.TRUE, value(id, "rolledBack"));
        assertNull(value(id, "rollbackError"));
        assertNoExternalCompensation(id);
    }

    @Test
    public void explicitlyDisabledRollbackExitsBeforeSuccessOrExternalCompensation() {
        RollbackData rollback = new RollbackData();
        rollback.put("VNF", "rollbackVnfCreate", "true");
        rollback.put("VNF", "rollbackSDNCActivate", "true");
        rollback.put("VFMODULE_BASE", "vfModuleId", "module-1");
        String id = start(inputs(rollback, "true"));

        assertActivity(id, "PreProcessRequest", 1);
        assertActivity(id, "SetSuccessfulRollbackStatus", 0);
        assertActivity(id, "SetFailedRollbackStatus", 0);
        assertEquals(Boolean.TRUE, value(id, "skipRollback"));
        assertNull(value(id, "rolledBack"));
        assertNoExternalCompensation(id);
    }

    @Test
    public void invalidCreatedModuleCountUsesTheProductionErrorSubprocess() {
        RollbackData rollback = new RollbackData();
        rollback.put("VNFANDMODULES", "numOfCreatedAddOnModules", "invalid");
        assertPreprocessingFailure(inputs(rollback, "false"));
    }

    @Test
    public void absentRollbackDataUsesTheProductionErrorSubprocess() {
        Map<String, Object> variables = inputs(new RollbackData(), "false");
        variables.remove("rollbackData");
        assertPreprocessingFailure(variables);
    }

    @Test
    public void successfulNoopRollbackRetainsTheOriginalRequestError() {
        Map<String, Object> variables = inputs(new RollbackData(), "false");
        variables.put("WorkflowException", new WorkflowException("original-process", 5310, "original request failure"));
        String id = start(variables);

        WorkflowException original = (WorkflowException) value(id, "WorkflowException");
        assertEquals("original-process", original.getProcessKey());
        assertEquals(5310, original.getErrorCode());
        assertEquals("original request failure", original.getErrorMessage());
        assertEquals(Boolean.TRUE, value(id, "rolledBack"));
        assertActivity(id, "SetSuccessfulRollbackStatus", 1);
        assertNoExternalCompensation(id);
    }

    private void assertPreprocessingFailure(Map<String, Object> variables) {
        String id = start(variables);
        assertActivity(id, "CatchExceptions", 1);
        assertActivity(id, "SetFailedRollbackStatus", 1);
        assertActivity(id, "LogSaveWorkflowException", 1);
        assertActivity(id, "SetSuccessfulRollbackStatus", 0);
        assertEquals(Boolean.FALSE, value(id, "rolledBack"));
        assertEquals("Caught exception in DoCreateVnfAndModulesRollback", value(id, "rollbackError"));
        assertNull(value(id, "rollbackData"));
        assertNull(value(id, "WorkflowException"));
        WorkflowException error = (WorkflowException) value(id, "DCVFMR_CaughtWorkflowException1");
        assertNotNull(error);
        assertEquals(2500, error.getErrorCode());
        assertEquals("Internal Error - Occured in DoCreateVnfAndModulesRollback PreProcessRequest",
                error.getErrorMessage());
        assertNoExternalCompensation(id);
    }

    private static Map<String, Object> inputs(RollbackData rollback, String disableRollback) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("rollbackData", rollback);
        variables.put("disableRollback", disableRollback);
        variables.put("msoRequestId", "request-1");
        return variables;
    }

    private String start(Map<String, Object> variables) {
        ProcessInstance instance =
                engine.getRuntimeService().startProcessInstanceByKey("DoCreateVnfAndModulesRollback", variables);
        assertEquals(0,
                engine.getRuntimeService().createProcessInstanceQuery().processInstanceId(instance.getId()).count());
        assertNotNull(engine.getHistoryService().createHistoricProcessInstanceQuery()
                .processInstanceId(instance.getId()).finished().singleResult());
        return instance.getId();
    }

    private void assertNoExternalCompensation(String id) {
        for (String activity : new String[] {"RollbackCreateVFModule", "Task_0p8fmrm", "SDNCAdapterTopologyDeactivate",
                "CallActivity_018fqnd"}) {
            assertActivity(id, activity, 0);
        }
    }

    private void assertActivity(String id, String activity, long count) {
        assertEquals(activity, count, engine.getHistoryService().createHistoricActivityInstanceQuery()
                .processInstanceId(id).activityId(activity).finished().count());
    }

    private Object value(String id, String name) {
        HistoricVariableInstance variable = engine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(id).variableName(name).singleResult();
        assertNotNull("Expected persisted variable " + name, variable);
        return variable.getValue();
    }
}
