/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.bpmn.infrastructure.pnf.delegate.bpmn;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = "/applicationContext_forPnfTesting.xml")
public class CreateAndActivatePnfResourceTest {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    @Rule
    public ProcessEngineRule processEngineRule;

    @Test
    @Deployment(resources = {"process/CreateAndActivatePnfResource.bpmn"})
    public void shouldSaveCurrentIpToVariableIfItAlreadyExistsInAai() throws Exception {
        // given
        BpmnAwareTests.init(processEngineRule.getProcessEngine());
        Map<String, Object> variables = new HashMap<>();
        variables.put("timeoutForPnfEntryNotification", "PT10S");
        variables.put("correlationId", "correctId");
        // when
        ProcessInstance instance = runtimeService
                .startProcessInstanceByKey("CreateAndActivatePnfResource", variables);
        // then
        assertThat(instance).hasPassedInOrder(
                "CreateAndActivatePnf_StartEvent",
                "CheckAiiForCorrelationId",
                "DoesAaiContainInfoAboutPnf",
                "DoesAaiContainInfoAboutIp"
        );
        assertThat(getIpVariable(instance)).isEqualTo("1.2.3.4");
    }

    @Test
    @Deployment(resources = {"process/CreateAndActivatePnfResource.bpmn"})
    public void shouldWaitForMessageFromDmaapAndUpdateAaiEntryWhenIpIsMissingInAaiEntry() throws Exception {
        // given
        BpmnAwareTests.init(processEngineRule.getProcessEngine());
        Map<String, Object> variables = new HashMap<>();
        variables.put("timeoutForPnfEntryNotification", "PT10S");
        variables.put("correlationId", "correctIdNoIp");
        // when
        ProcessInstance instance = runtimeService
                .startProcessInstanceByKey("CreateAndActivatePnfResource", variables);
        assertThat(instance).isWaitingAt("WaitForDmaapPnfReadyNotification").isWaitingFor("WorkflowMessage");
        runtimeService.createMessageCorrelation("WorkflowMessage")
                .setVariable("ipAddress", "2.3.4.5")
                .correlateWithResult();
        // then
        assertThat(instance).isEnded().hasPassedInOrder(
                "CreateAndActivatePnf_StartEvent",
                "CheckAiiForCorrelationId",
                "DoesAaiContainInfoAboutPnf",
                "DoesAaiContainInfoAboutIp",
                "AaiEntryExists",
                "WaitForDmaapPnfReadyNotification",
                "UpdateAaiEntry"
        );
        assertThat(getIpVariable(instance)).isEqualTo("2.3.4.5");
        // todo: check communication with AAI
    }

    @Test
    @Deployment(resources = {"process/CreateAndActivatePnfResource.bpmn"})
    public void shouldCreateAaiEntryWaitForMessageFromDmaapAndUpdateAaiEntryWhenNoAaiEntry() throws Exception {
        // given
        BpmnAwareTests.init(processEngineRule.getProcessEngine());
        Map<String, Object> variables = new HashMap<>();
        variables.put("timeoutForPnfEntryNotification", "PT10S");
        variables.put("correlationId", "incorrectId");
        // when
        ProcessInstance instance = runtimeService
                .startProcessInstanceByKey("CreateAndActivatePnfResource", variables);
        assertThat(instance).isWaitingAt("WaitForDmaapPnfReadyNotification").isWaitingFor("WorkflowMessage");
        runtimeService.createMessageCorrelation("WorkflowMessage")
                .setVariable("ipAddress", "2.3.4.5")
                .correlateWithResult();
        // then
        assertThat(instance).isEnded().hasPassedInOrder(
                "CreateAndActivatePnf_StartEvent",
                "CheckAiiForCorrelationId",
                "DoesAaiContainInfoAboutPnf",
                "CreateAndActivatePnf_CreateAaiEntry",
                "AaiEntryExists",
                "WaitForDmaapPnfReadyNotification",
                "UpdateAaiEntry"
        );
        assertThat(getIpVariable(instance)).isEqualTo("2.3.4.5");
        // todo: check communication with AAI
    }

    private Object getIpVariable(ProcessInstance instance) {
        Optional<HistoricVariableInstance> variable = getVariables(instance).stream()
                .filter(p -> p.getName().equals("ipAddress")).findAny();
        if (!variable.isPresent()) {
            throw new RuntimeException("Variable " + "ipAddress" + " not found");
        }
        return variable.get().getValue();
    }

    private List<HistoricVariableInstance> getVariables(ProcessInstance instance) {
        return processEngineRule.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(instance.getProcessInstanceId()).taskIdIn().list();
    }
}
