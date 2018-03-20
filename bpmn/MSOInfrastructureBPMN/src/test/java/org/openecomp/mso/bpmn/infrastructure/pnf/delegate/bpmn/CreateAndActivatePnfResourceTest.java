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

import static org.assertj.core.api.Assertions.entry;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.assertThat;
import static org.openecomp.mso.bpmn.infrastructure.pnf.delegate.AaiConnectionTestImpl.DEFAULT_IP;
import static org.openecomp.mso.bpmn.infrastructure.pnf.delegate.AaiConnectionTestImpl.ID_WITH_ENTRY_AND_IP;
import static org.openecomp.mso.bpmn.infrastructure.pnf.delegate.AaiConnectionTestImpl.ID_WITH_ENTRY_NO_IP;
import static org.openecomp.mso.bpmn.infrastructure.pnf.delegate.AaiConnectionTestImpl.ID_WITHOUT_ENTRY;
import static org.openecomp.mso.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.CORRELATION_ID;
import static org.openecomp.mso.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.IP_ADDRESS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.mso.bpmn.infrastructure.pnf.delegate.AaiConnectionTestImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = "/applicationContext_forPnfTesting.xml")
public class CreateAndActivatePnfResourceTest {

    public static final String TIMEOUT_10_S = "PT10S";
    public static final String IP_FROM_DMAAP = "2.3.4.5";
    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    @Rule
    public ProcessEngineRule processEngineRule;

    @Autowired
    private AaiConnectionTestImpl aaiConnection;

    @Test
    @Deployment(resources = {"process/CreateAndActivatePnfResource.bpmn"})
    public void shouldSaveCurrentIpToVariableIfItAlreadyExistsInAai() throws Exception {
        // given
        aaiConnection.reset();
        BpmnAwareTests.init(processEngineRule.getProcessEngine());
        Map<String, Object> variables = new HashMap<>();
        variables.put("timeoutForPnfEntryNotification", TIMEOUT_10_S);
        variables.put(CORRELATION_ID, ID_WITH_ENTRY_AND_IP);
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
        assertThat(getIpVariable(instance)).isEqualTo(DEFAULT_IP);
    }

    @Test
    @Deployment(resources = {"process/CreateAndActivatePnfResource.bpmn"})
    public void shouldWaitForMessageFromDmaapAndUpdateAaiEntryWhenIpIsMissingInAaiEntry() throws Exception {
        // given
        aaiConnection.reset();
        BpmnAwareTests.init(processEngineRule.getProcessEngine());
        Map<String, Object> variables = new HashMap<>();
        variables.put("timeoutForPnfEntryNotification", TIMEOUT_10_S);
        variables.put(CORRELATION_ID, ID_WITH_ENTRY_NO_IP);
        // when
        ProcessInstance instance = runtimeService
                .startProcessInstanceByKey("CreateAndActivatePnfResource", "businessKey", variables);
        assertThat(instance).isWaitingAt("WaitForDmaapPnfReadyNotification").isWaitingFor("WorkflowMessage");
        runtimeService.createMessageCorrelation("WorkflowMessage")
                .processInstanceBusinessKey("businessKey")
                .setVariable(IP_ADDRESS, IP_FROM_DMAAP)
                .correlateWithResult();
        // then
        assertThat(instance).isEnded().hasPassedInOrder(
                "CreateAndActivatePnf_StartEvent",
                "CheckAiiForCorrelationId",
                "DoesAaiContainInfoAboutPnf",
                "DoesAaiContainInfoAboutIp",
                "AaiEntryExists",
                "WaitForDmaapPnfReadyNotification"
        );
        assertThat(getIpVariable(instance)).isEqualTo(IP_FROM_DMAAP);
    }

    @Test
    @Deployment(resources = {"process/CreateAndActivatePnfResource.bpmn"})
    public void shouldCreateAaiEntryWaitForMessageFromDmaapAndUpdateAaiEntryWhenNoAaiEntry() throws Exception {
        // given
        aaiConnection.reset();
        BpmnAwareTests.init(processEngineRule.getProcessEngine());
        Map<String, Object> variables = new HashMap<>();
        variables.put("timeoutForPnfEntryNotification", TIMEOUT_10_S);
        variables.put(CORRELATION_ID, ID_WITHOUT_ENTRY);
        // when
        ProcessInstance instance = runtimeService
                .startProcessInstanceByKey("CreateAndActivatePnfResource", variables);
        assertThat(instance).isWaitingAt("WaitForDmaapPnfReadyNotification").isWaitingFor("WorkflowMessage");
        runtimeService.createMessageCorrelation("WorkflowMessage")
                .setVariable(IP_ADDRESS, IP_FROM_DMAAP)
                .correlateWithResult();
        // then
        assertThat(instance).isEnded().hasPassedInOrder(
                "CreateAndActivatePnf_StartEvent",
                "CheckAiiForCorrelationId",
                "DoesAaiContainInfoAboutPnf",
                "CreateAndActivatePnf_CreateAaiEntry",
                "AaiEntryExists",
                "WaitForDmaapPnfReadyNotification"
        );
        assertThat(getIpVariable(instance)).isEqualTo(IP_FROM_DMAAP);
        assertThat(aaiConnection.getCreated()).containsExactly(ID_WITHOUT_ENTRY);
    }

    private Object getIpVariable(ProcessInstance instance) {
        Optional<HistoricVariableInstance> variable = getVariables(instance).stream()
                .filter(p -> p.getName().equals(IP_ADDRESS)).findAny();
        if (!variable.isPresent()) {
            throw new RuntimeException("Variable 'ipAddress' not found");
        }
        return variable.get().getValue();
    }

    private List<HistoricVariableInstance> getVariables(ProcessInstance instance) {
        return processEngineRule.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(instance.getProcessInstanceId()).taskIdIn().list();
    }
}
