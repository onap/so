/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright 2018 Nokia
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

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.CORRELATION_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_UUID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.SERVICE_INSTANCE_ID;

import java.util.HashMap;
import java.util.Map;

import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.onap.so.BaseIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;

public class CreateAndActivatePnfResourceTest extends BaseIntegrationTest {

    private static final String TIMEOUT_10_S = "PT10S";
    private static final String VALID_UUID = UUID.nameUUIDFromBytes("testUuid".getBytes()).toString();
    private static final String SERVICE_INSTANCE_ID_VALUE = "da7d07d9-b71c-4128-809d-2ec01c807169";

    @Autowired
    private AaiConnectionTestImpl aaiConnection;

    @Autowired
    private DmaapClientTestImpl dmaapClientTestImpl;

    @Test
    public void shouldWaitForMessageFromDmaapAndUpdateAaiEntryWhenAaiEntryExists() {
        // given
        aaiConnection.reset();       
        Map<String, Object> variables = new HashMap<>();
        variables.put("timeoutForPnfEntryNotification", TIMEOUT_10_S);
        variables.put(CORRELATION_ID, AaiConnectionTestImpl.ID_WITH_ENTRY);
        variables.put(PNF_UUID, VALID_UUID);
        variables.put(SERVICE_INSTANCE_ID, SERVICE_INSTANCE_ID_VALUE);
        // when
        ProcessInstance instance = runtimeService
                .startProcessInstanceByKey("CreateAndActivatePnfResource", "businessKey", variables);
        assertThat(instance).isWaitingAt("WaitForDmaapPnfReadyNotification").isWaitingFor("WorkflowMessage");
        dmaapClientTestImpl.sendMessage();

        // then
        assertThat(instance).isEnded().hasPassedInOrder(
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
    public void shouldCreateAaiEntryWaitForMessageFromDmaapAndUpdateAaiEntryWhenNoAaiEntryExists() {
        // given
        aaiConnection.reset();
       
        Map<String, Object> variables = new HashMap<>();
        variables.put("timeoutForPnfEntryNotification", TIMEOUT_10_S);
        variables.put(CORRELATION_ID, AaiConnectionTestImpl.ID_WITHOUT_ENTRY);
        variables.put(PNF_UUID, VALID_UUID);
        variables.put(SERVICE_INSTANCE_ID, SERVICE_INSTANCE_ID_VALUE);
        // when
        ProcessInstance instance = runtimeService
                .startProcessInstanceByKey("CreateAndActivatePnfResource", "businessKey", variables);
        assertThat(instance).isWaitingAt("WaitForDmaapPnfReadyNotification").isWaitingFor("WorkflowMessage");
        dmaapClientTestImpl.sendMessage();

        // then
        assertThat(instance).isEnded().hasPassedInOrder(
                "CreateAndActivatePnf_StartEvent",
                "CheckInputs",
                "CheckAiiForCorrelationId",
                "DoesAaiContainInfoAboutPnf",
                "CreatePnfEntryInAai",
                "AaiEntryExists",
                "InformDmaapClient",
                "WaitForDmaapPnfReadyNotification",
                "AaiEntryUpdated"
        );
        Assertions.assertThat(aaiConnection.getCreated()).containsOnlyKeys(AaiConnectionTestImpl.ID_WITHOUT_ENTRY);
    }
}
