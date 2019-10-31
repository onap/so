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

import org.camunda.bpm.engine.ProcessEngineConfiguration;
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
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.catalog.beans.PnfResourceCustomization;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.*;
import static org.onap.so.client.cds.PayloadConstants.PRC_TARGET_SOFTWARE_VERSION;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {CatalogDbClient.class, ExceptionBuilder.class, NfSoftwareUpgradeCheckerDelegate.class,
        ProcessEngineConfiguration.class})
public class NfSoftwareUpgradeCheckerDelegateTest {

    private static final String TEST_PROCESS_KEY = "processKey1";
    private static final String PROCESS_KEY = "testProcessKey";
    private static final String TEST_PNF_RESOURCE_INSTANCE_NAME = "PNF_demo_resource";
    private static final String TEST_PNF_RESOURCE_BLUEPRINT_NAME = "blueprintOnap";
    private static final String TEST_PNF_RESOURCE_BLUEPRINT_VERSION = "1.0.1";
    private static final String TEST_PNF_RESOURCE_CUSTOMIZATION_UUID = "9acb3a83-8a52-412c-9a45-901764938144";
    private static final String TEST_PNF_CORRELATION_ID = "PNFDemo";
    private static final String TEST_PNF_UUID = "FakeID";
    private static final String TEST_PRC_CONTROLLER_ACTOR = "cds";
    private static final String TEST_TARGET_SOFTWARE_VERSION = "demo-sw-ver2.0.0";

    private static String TEST_BPMN_REQUEST = "{\"requestDetails\":{" +
            "\"requestInfo\":{" +
            "\"source\":\"VID\"," +
            "\"suppressRollback\":false," +
            "\"requestorId\":\"demo\"," +
            "\"productFamilyId\":\"SWUPid\"}," +
            "\"modelInfo\":{" +
            "\"modelType\":\"service\",\"modelInvariantUuid\":\"439b7a2f-9524-4dbf-9eee-f2e05521df3f\"," +
            "\"modelInvariantId\":\"439b7a2f-9524-4dbf-9eee-f2e05521df3f\"," +
            "\"modelUuid\":\"42daaac6-5017-4e1e-96c8-6a27dfbe1421\",\"modelName\":\"PNF_int_service_2\"," +
            "\"modelVersion\":\"1.0\"},\"requestParameters\":{\"userParams\":[{\"name\":\"aic_zone\"," +
            "\"value\":\"nova\"},{\"name\":\"pnfId\",\"value\":\"PNFDemo\"},{\"name\":\"target-software-version\",\"value\":\"demo-sw-ver2.0.0\"}]," +
            "\"subscriptionServiceType\":\"SWUP\",\"aLaCarte\":false,\"pnfCorrelationId\":\"PNFDemo\"}," +
            "\"cloudConfiguration\":{\"lcpCloudRegionId\":\"regionOne\",\"tenantId\":\"09a63533072f4a579d5c99c3b8fe94c6\"}," +
            "\"subscriberInfo\":{\"globalSubscriberId\":\"ADemoCustomerInEric\"},\"project\":{\"projectName\":\"Project-Demonstration\"}," +
            "\"owningEntity\":{\"owningEntityId\":\"5eae949c-1c50-4780-b8b5-7cbeb08856b4\",\"owningEntityName\":\"OE-Demonstration\"}}}";
    /**
     * Testing model UUID, should be the same as specified in the TEST_SERVICE_MODEL_INFO.
     */
    private static final String TEST_MODEL_UUID = "42daaac6-5017-4e1e-96c8-6a27dfbe1421";

    @MockBean
    private PnfManagement pnfManagement;

    @MockBean
    private CatalogDbClient catalogDbClient;

    @MockBean
    private ProcessEngineConfiguration processEngineConfiguration;

    @Autowired
    @InjectMocks
    private NfSoftwareUpgradeCheckerDelegate nfSoftwareUpgradeCheckerDelegate;

    private DelegateExecution execution = new DelegateExecutionFake();

    @Before
    public void setUp() throws IOException {
        List<PnfResourceCustomization> pnfResourceCustomizations = new ArrayList<>();
        pnfResourceCustomizations.add(buildPnfResourceCustomization());
        given(catalogDbClient.getPnfResourceCustomizationByModelUuid(TEST_MODEL_UUID))
                .willReturn(pnfResourceCustomizations);
        execution.setVariable(PROCESS_KEY, TEST_PROCESS_KEY);
        execution.setVariable("bpmnRequest", TEST_BPMN_REQUEST);
        mockAai();
    }

    @Test
    public void testExecution_validCatalogDb_skipVariableSet() {
        try {
            nfSoftwareUpgradeCheckerDelegate.execute(execution);
            assertThat(execution.getVariable(MODEL_UUID)).isEqualTo(TEST_MODEL_UUID);
            assertThat(execution.getVariable(PRC_TARGET_SOFTWARE_VERSION)).isEqualTo(TEST_TARGET_SOFTWARE_VERSION);
            assertThat(execution.getVariable(PRC_BLUEPRINT_NAME)).isEqualTo(TEST_PNF_RESOURCE_BLUEPRINT_NAME);
            assertThat(execution.getVariable(PRC_BLUEPRINT_VERSION)).isEqualTo(TEST_PNF_RESOURCE_BLUEPRINT_VERSION);
            assertThat(execution.getVariable(PRC_CUSTOMIZATION_UUID)).isEqualTo(TEST_PNF_RESOURCE_CUSTOMIZATION_UUID);
            assertThat(execution.getVariable(PRC_INSTANCE_NAME)).isEqualTo(TEST_PNF_RESOURCE_INSTANCE_NAME);
            assertThat(execution.getVariable(PNF_CORRELATION_ID)).isEqualTo(TEST_PNF_CORRELATION_ID);
            assertThat(execution.getVariable(PNF_UUID)).isEqualTo(TEST_PNF_UUID);
            assertThat(execution.getVariable(PRC_CONTROLLER_ACTOR)).isEqualTo(TEST_PRC_CONTROLLER_ACTOR);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception thrown" + e.getMessage());
        }
    }

    @Test
    public void testExecution_failedAaiConnection_exceptionThrown() {
        try {
            /*
             * Mock the IOException from AAI.
             */
            when(pnfManagement.getEntryFor(TEST_PNF_CORRELATION_ID)).thenThrow(new IOException("Connection failed"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertThatThrownBy(() -> nfSoftwareUpgradeCheckerDelegate.execute(execution)).isInstanceOf(BpmnError.class);
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
        }
        assertThatThrownBy(() -> nfSoftwareUpgradeCheckerDelegate.execute(execution)).isInstanceOf(BpmnError.class);
        assertThat(execution.getVariable("WorkflowExceptionErrorMessage")).asString()
                .contains("AAI entry for PNF: " + TEST_PNF_CORRELATION_ID + " does not exist");
        assertThat(execution.getVariable("WorkflowException")).isInstanceOf(WorkflowException.class);
    }

    @Test
    public void testExecution_EmptyPnfResourceCustomization_exceptionThrown() {
        given(catalogDbClient.getPnfResourceCustomizationByModelUuid("42daaac6-5017-4e1e-96c8-6a27dfbe1421"))
                .willReturn(Collections.EMPTY_LIST);

        assertThatThrownBy(() -> nfSoftwareUpgradeCheckerDelegate.execute(execution)).isInstanceOf(BpmnError.class);
        assertThat(execution.getVariable("WorkflowExceptionErrorMessage")).asString()
                .contains("Unable to find the PNF resource customizations of model service UUID")
                .contains("42daaac6-5017-4e1e-96c8-6a27dfbe1421");
        assertThat(execution.getVariable("WorkflowException")).isInstanceOf(WorkflowException.class);
    }

    @Test
    public void testExecution_NonExistingServiceModelInfo_exceptionThrown() {
        String NO_MODEL_INFO = "{\"requestDetails\":{" +
                "\"requestInfo\":{" +
                "\"source\":\"VID\"," +
                "\"suppressRollback\":false," +
                "\"requestorId\":\"demo\"," +
                "\"productFamilyId\":\"SWUPid\"}," +
                "\"requestParameters\":{\"userParams\":[{\"name\":\"aic_zone\"," +
                "\"value\":\"nova\"},{\"name\":\"pnfId\",\"value\":\"PNFDemo\"},{\"name\":\"target-software-version\",\"value\":\"demo-sw-ver2.0.0\"}]," +
                "\"subscriptionServiceType\":\"SWUP\",\"aLaCarte\":false,\"pnfCorrelationId\":\"PNFDemo\"}," +
                "\"cloudConfiguration\":{\"lcpCloudRegionId\":\"regionOne\",\"tenantId\":\"09a63533072f4a579d5c99c3b8fe94c6\"}," +
                "\"subscriberInfo\":{\"globalSubscriberId\":\"ADemoCustomerInEric\"},\"project\":{\"projectName\":\"Project-Demonstration\"}," +
                "\"owningEntity\":{\"owningEntityId\":\"5eae949c-1c50-4780-b8b5-7cbeb08856b4\",\"owningEntityName\":\"OE-Demonstration\"}}}";

        execution.setVariable("bpmnRequest", NO_MODEL_INFO);

        assertThatThrownBy(() -> nfSoftwareUpgradeCheckerDelegate.execute(execution)).isInstanceOf(BpmnError.class);
        assertThat(execution.getVariable("WorkflowExceptionErrorMessage")).asString()
                .contains("Unable to find parameter " + SERVICE_MODEL_INFO);
        assertThat(execution.getVariable("WorkflowException")).isInstanceOf(WorkflowException.class);
    }

    private void mockAai() throws IOException {
        Pnf pnf = new Pnf();
        pnf.setPnfId(TEST_PNF_UUID);
        when(pnfManagement.getEntryFor(TEST_PNF_CORRELATION_ID)).thenReturn(Optional.of(pnf));
    }

    private PnfResourceCustomization buildPnfResourceCustomization() {
        PnfResourceCustomization pnfResourceCustomization = new PnfResourceCustomization();
        pnfResourceCustomization.setSkipPostInstConf(true);
        pnfResourceCustomization.setBlueprintName(TEST_PNF_RESOURCE_BLUEPRINT_NAME);
        pnfResourceCustomization.setBlueprintVersion(TEST_PNF_RESOURCE_BLUEPRINT_VERSION);
        pnfResourceCustomization.setModelInstanceName(TEST_PNF_RESOURCE_INSTANCE_NAME);
        pnfResourceCustomization.setModelCustomizationUUID(TEST_PNF_RESOURCE_CUSTOMIZATION_UUID);
        pnfResourceCustomization.setControllerActor(TEST_PRC_CONTROLLER_ACTOR);
        return pnfResourceCustomization;
    }
}
