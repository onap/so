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
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_CORRELATION_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_UUID;

import java.util.HashMap;
import java.util.Map;

import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.MapEntry;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.BaseIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;

public class CreateAndActivatePnfResourceTest extends BaseIntegrationTest {

    private static final String VALID_UUID = UUID.nameUUIDFromBytes("testUuid".getBytes()).toString();
    private static final String SERVICE_INSTANCE_ID = "serviceForInstance";

    private Map<String, Object> variables;

    @Autowired
    private PnfManagementTestImpl pnfManagementTest;

    @Autowired
    private DmaapClientTestImpl dmaapClientTestImpl;

    @Before
    public void setup() {
        pnfManagementTest.reset();
        variables = new HashMap<>();
        variables.put("serviceInstanceId", SERVICE_INSTANCE_ID);
        variables.put(PNF_UUID, VALID_UUID);
    }

    @Test
    public void shouldWaitForMessageFromDmaapAndUpdateAaiEntryWhenAaiEntryExists() {
        // given
        variables.put(PNF_CORRELATION_ID, PnfManagementTestImpl.ID_WITH_ENTRY);
        // when
        ProcessInstance instance = runtimeService
                .startProcessInstanceByKey("CreateAndActivatePnfResource", "businessKey", variables);
        assertThat(instance).isWaitingAt("WaitForDmaapPnfReadyNotification").isWaitingFor("WorkflowMessage");
        dmaapClientTestImpl.sendMessage();

        // then
        assertThat(instance).isEnded().hasPassedInOrder(
                "CreateAndActivatePnf_StartEvent",
                "CheckInputs",
                "CheckAiiForPnfCorrelationId",
                "DoesAaiContainInfoAboutPnf",
                "AaiEntryExists",
                "InformDmaapClient",
                "WaitForDmaapPnfReadyNotification",
                "CreateRelationId",
                "AaiEntryUpdated"
        );
        Assertions.assertThat(pnfManagementTest.getServiceAndPnfRelationMap()).
                containsOnly(MapEntry.entry(SERVICE_INSTANCE_ID, PnfManagementTestImpl.ID_WITH_ENTRY));
    }

    @Test
    public void shouldCreateAaiEntryWaitForMessageFromDmaapAndUpdateAaiEntryWhenNoAaiEntryExists() {
        // given
        variables.put(PNF_CORRELATION_ID, PnfManagementTestImpl.ID_WITHOUT_ENTRY);
        // when
        ProcessInstance instance = runtimeService
                .startProcessInstanceByKey("CreateAndActivatePnfResource", "businessKey", variables);
        assertThat(instance).isWaitingAt("WaitForDmaapPnfReadyNotification").isWaitingFor("WorkflowMessage");
        dmaapClientTestImpl.sendMessage();

        // then
        assertThat(instance).isEnded().hasPassedInOrder(
                "CreateAndActivatePnf_StartEvent",
                "CheckInputs",
                "CheckAiiForPnfCorrelationId",
                "DoesAaiContainInfoAboutPnf",
                "CreatePnfEntryInAai",
                "AaiEntryExists",
                "InformDmaapClient",
                "WaitForDmaapPnfReadyNotification",
                "CreateRelationId",
                "AaiEntryUpdated"
        );
        Assertions.assertThat(pnfManagementTest.getCreated()).containsOnlyKeys(PnfManagementTestImpl.ID_WITHOUT_ENTRY);
        Assertions.assertThat(pnfManagementTest.getServiceAndPnfRelationMap()).
                containsOnly(MapEntry.entry(SERVICE_INSTANCE_ID, PnfManagementTestImpl.ID_WITHOUT_ENTRY));
    }
}
