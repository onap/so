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
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.GLOBAL_CUSTOMER_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_UUID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.SERVICE_INSTANCE_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.SERVICE_TYPE;

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
    private static final String DEFAULT_GLOBAL_CUSTOMER_ID = "id123";
    private static final String DEFAULT_SERVICE_TYPE = "service1";
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "instance123";

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
        variables.put(GLOBAL_CUSTOMER_ID, DEFAULT_GLOBAL_CUSTOMER_ID);
        variables.put(SERVICE_TYPE, DEFAULT_SERVICE_TYPE);
        variables.put(SERVICE_INSTANCE_ID, DEFAULT_SERVICE_INSTANCE_ID);
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
        variables.put(GLOBAL_CUSTOMER_ID, DEFAULT_GLOBAL_CUSTOMER_ID);
        variables.put(SERVICE_TYPE, DEFAULT_SERVICE_TYPE);
        variables.put(SERVICE_INSTANCE_ID, DEFAULT_SERVICE_INSTANCE_ID);
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
