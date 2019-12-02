/*
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.pnf.delegate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.onap.aai.domain.yang.Pnf;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.infrastructure.pnf.management.PnfManagement;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.cds.beans.PreCheckPropertiesForPnf;
import org.onap.so.client.cds.beans.PreCheckRequestPnf;
import org.onap.so.client.exception.ExceptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ExceptionBuilder.class, PreparePreCheckDelegate.class})
public class PreparePreCheckDelegateTest {

    private static final String TEST_MODEL_UUID = "6bc0b04d-1873-4721-b53d-6615225b2a28";
    private static final String TEST_SERVICE_INSTANCE_ID = "test_service_id";
    private static final String PROCESS_KEY = "testProcessKey";
    private static final String TEST_PROCESS_KEY = "processKey1";
    private static final String TEST_PNF_RESOURCE_INSTANCE_NAME = "PNF_demo_resource";
    private static final String TEST_PNF_CORRELATION_ID = "PNFDemo";
    private static final String TEST_PNF_RESOURCE_BLUEPRINT_NAME = "blueprintOnap";
    private static final String TEST_PNF_RESOURCE_BLUEPRINT_VERSION = "1.0.1";
    private static final String TEST_PNF_RESOURCE_CUSTOMIZATION_UUID = "9acb3a83-8a52-412c-9a45-901764938144";
    private static final String TEST_MSO_REQUEST_ID = "ff874603-4222-11e7-9252-005056850d2e";
    private static final String TEST_PNF_UUID = "5df8b6de-2083-11e7-93ae-92361f002671";
    private static final String TEST_IPV4_ADDRESS = "1.1.1.1";
    private static final String TEST_IPV6_ADDRESS = "::1/128";
    private static final String TEST_SOFTWARE_VERSION = "1.0.0";

    @Autowired
    @InjectMocks
    private PreparePreCheckDelegate preparePrecheckDelegate;

    @MockBean
    private PnfManagement pnfManagement;

    private DelegateExecution execution = new DelegateExecutionFake();

    @Before
    public void setUp() throws IOException {
        execution.setVariable(PROCESS_KEY, TEST_PROCESS_KEY);
        execution.setVariable(PNF_CORRELATION_ID, TEST_PNF_CORRELATION_ID);
        execution.setVariable(MODEL_UUID, TEST_MODEL_UUID);
        execution.setVariable(SERVICE_INSTANCE_ID, TEST_SERVICE_INSTANCE_ID);
        execution.setVariable(MSO_REQUEST_ID, TEST_MSO_REQUEST_ID);
        execution.setVariable(PNF_UUID, TEST_PNF_UUID);
        execution.setVariable(PRC_INSTANCE_NAME, TEST_PNF_RESOURCE_INSTANCE_NAME);
        execution.setVariable(PRC_CUSTOMIZATION_UUID, TEST_PNF_RESOURCE_CUSTOMIZATION_UUID);
        execution.setVariable(PRC_BLUEPRINT_NAME, TEST_PNF_RESOURCE_BLUEPRINT_NAME);
        execution.setVariable(PRC_BLUEPRINT_VERSION, TEST_PNF_RESOURCE_BLUEPRINT_VERSION);
        execution.setVariable(NF_SOFTWARE_VERSION, TEST_SOFTWARE_VERSION);
        mockAai();
    }

    private void mockAai() throws IOException {
        Pnf pnf = new Pnf();
        pnf.setIpaddressV4Oam(TEST_IPV4_ADDRESS);
        pnf.setIpaddressV6Oam(TEST_IPV6_ADDRESS);
        when(pnfManagement.getEntryFor(TEST_PNF_CORRELATION_ID)).thenReturn(Optional.of(pnf));
    }

    @Test
    public void testExecution_validPnf_executionObjectCreated() {
        try {
            preparePrecheckDelegate.execute(execution);
            Object executionObject = execution.getVariable(EXECUTION_OBJECT);
            assertThat(executionObject).isNotNull();
            assertThat(executionObject).isInstanceOf(AbstractCDSPropertiesBean.class);
            checkCDSPropertiesBean((AbstractCDSPropertiesBean) executionObject);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception thrown" + e.getMessage());
        }
    }

    @Test
    public void testExecution_failedAaiConnection_exceptionThrown() {
        try {
            /**
             * Mock the IOException from AAI.
             */
            when(pnfManagement.getEntryFor(TEST_PNF_CORRELATION_ID)).thenThrow(new IOException("Connection failed"));
        } catch (IOException e) {
            e.printStackTrace();
            fail("Wrong exception: " + e.getMessage());
        }
        assertThatThrownBy(() -> preparePrecheckDelegate.execute(execution)).isInstanceOf(BpmnError.class);
        assertThat(execution.getVariable("WorkflowExceptionErrorMessage")).asString()
                .contains("Unable to fetch from AAI");
        assertThat(execution.getVariable("WorkflowException")).isInstanceOf(WorkflowException.class);
    }

    @Test
    public void testExecution_aaiEntryNotExist_exceptionThrown() {
        try {
            /**
             * Mock the AAI without PNF.
             */
            when(pnfManagement.getEntryFor(TEST_PNF_CORRELATION_ID)).thenReturn(Optional.empty());
        } catch (IOException e) {
            e.printStackTrace();
            fail("Wrong exception: " + e.getMessage());
        }
        assertThatThrownBy(() -> preparePrecheckDelegate.execute(execution)).isInstanceOf(BpmnError.class);
        assertThat(execution.getVariable("WorkflowExceptionErrorMessage")).asString()
                .contains("AAI entry for PNF: " + TEST_PNF_CORRELATION_ID + " does not exist");
        assertThat(execution.getVariable("WorkflowException")).isInstanceOf(WorkflowException.class);
    }

    private void checkCDSPropertiesBean(AbstractCDSPropertiesBean executionObject) {
        assertThat(executionObject.getBlueprintName()).matches(TEST_PNF_RESOURCE_BLUEPRINT_NAME);
        assertThat(executionObject.getBlueprintVersion()).matches(TEST_PNF_RESOURCE_BLUEPRINT_VERSION);
        assertThat(executionObject.getRequestId()).matches(TEST_MSO_REQUEST_ID);
        assertThat(executionObject.getSubRequestId()).matches(TEST_PNF_UUID);
        assertThat(executionObject.getMode()).matches("sync");
        assertThat(executionObject.getActionName()).matches("preCheck");
        assertThat(executionObject.getOriginatorId()).matches("SO");

        assertThat(executionObject.getRequestObject()).isNotNull();
        String requestObject = executionObject.getRequestObject();

        checkRequestJson(requestObject);
    }

    private void checkRequestJson(String requestObject) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode tree = mapper.readTree(requestObject);
            PreCheckRequestPnf preCheckRequestPnf =
                    mapper.treeToValue(tree.at("/pre-check-request"), PreCheckRequestPnf.class);
            assertThat(preCheckRequestPnf.getResolutionKey()).matches(TEST_PNF_CORRELATION_ID);

            PreCheckPropertiesForPnf properties = preCheckRequestPnf.getPreCheckPropertiesForPnf();
            assertThat(properties.getServiceInstanceId()).matches(TEST_SERVICE_INSTANCE_ID);
            assertThat(properties.getPnfName()).matches(TEST_PNF_CORRELATION_ID);
            assertThat(properties.getPnfId()).matches(TEST_PNF_UUID);
            assertThat(properties.getPnfCustomizationUuid()).matches(TEST_PNF_RESOURCE_CUSTOMIZATION_UUID);
            assertThat(properties.getServiceModelUuid()).matches(TEST_MODEL_UUID);
            assertThat(properties.getSoftwareVersion()).matches(TEST_SOFTWARE_VERSION);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Check request body is json message" + e.getMessage());
        }
    }
}
