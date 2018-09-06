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

package org.onap.so.bpmn.infrastructure.pnf.delegate;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.CORRELATION_ID;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = "/applicationContext_forPnfTesting.xml")
public class CreateAndActivatePnfResourceTest {

    private static final String TIMEOUT_10_S = "PT10S";
    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    @Rule
    public ProcessEngineRule processEngineRule;

    @Autowired
    private AaiConnectionTestImpl aaiConnection;

    @Autowired
    private DmaapClientTestImpl dmaapClientTestImpl;

    @Test
    @Deployment(resources = {"process/CreateAndActivatePnfResource.bpmn"})
    public void shouldWaitForMessageFromDmaapAndUpdateAaiEntryWhenAaiEntryExists() {
        // given
        aaiConnection.reset();
        BpmnAwareTests.init(processEngineRule.getProcessEngine());
        Map<String, Object> variables = new HashMap<>();
        variables.put("timeoutForPnfEntryNotification", TIMEOUT_10_S);
        variables.put(CORRELATION_ID, AaiConnectionTestImpl.ID_WITH_ENTRY);
        // when
        ProcessInstance instance = runtimeService
                .startProcessInstanceByKey("CreateAndActivatePnfResource", "businessKey", variables);
        BpmnAwareAssertions.assertThat(instance).isWaitingAt("WaitForDmaapPnfReadyNotification").isWaitingFor("WorkflowMessage");
        dmaapClientTestImpl.sendMessage();

        // then
        BpmnAwareAssertions.assertThat(instance).isEnded().hasPassedInOrder(
                "CreateAndActivatePnf_StartEvent",
                "CheckInputs",
                "CheckAiiForCorrelationId",
                "DoesAaiContainInfoAboutPnf",
                "AaiEntryExists",
                "InformDmaapClient",
                "WaitForDmaapPnfReadyNotification",
                "AaiEntryUpdated"
        );
    }

    @Test
    @Deployment(resources = {"process/CreateAndActivatePnfResource.bpmn"})
    public void shouldCreateAaiEntryWaitForMessageFromDmaapAndUpdateAaiEntryWhenNoAaiEntryExists() {
        // given
        aaiConnection.reset();
        BpmnAwareTests.init(processEngineRule.getProcessEngine());
        Map<String, Object> variables = new HashMap<>();
        variables.put("timeoutForPnfEntryNotification", TIMEOUT_10_S);
        variables.put(CORRELATION_ID, AaiConnectionTestImpl.ID_WITHOUT_ENTRY);
        // when
        ProcessInstance instance = runtimeService
                .startProcessInstanceByKey("CreateAndActivatePnfResource", "businessKey", variables);
        BpmnAwareAssertions.assertThat(instance).isWaitingAt("WaitForDmaapPnfReadyNotification").isWaitingFor("WorkflowMessage");
        dmaapClientTestImpl.sendMessage();

        // then
        BpmnAwareAssertions.assertThat(instance).isEnded().hasPassedInOrder(
                "CreateAndActivatePnf_StartEvent",
                "CheckInputs",
                "CheckAiiForCorrelationId",
                "DoesAaiContainInfoAboutPnf",
                "CreateAndActivatePnf_CreateAaiEntry",
                "AaiEntryExists",
                "InformDmaapClient",
                "WaitForDmaapPnfReadyNotification",
                "AaiEntryUpdated"
        );
        Assertions.assertThat(aaiConnection.getCreated()).containsOnlyKeys(AaiConnectionTestImpl.ID_WITHOUT_ENTRY);
    }
}
