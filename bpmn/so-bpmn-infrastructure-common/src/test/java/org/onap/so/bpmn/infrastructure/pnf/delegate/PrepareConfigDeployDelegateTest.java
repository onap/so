/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.pnf.delegate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.EXECUTION_OBJECT;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.MODEL_UUID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.MSO_REQUEST_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_CORRELATION_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_UUID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.SERVICE_INSTANCE_ID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityNotFoundException;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.cds.beans.ConfigDeployPropertiesForPnf;
import org.onap.so.client.cds.beans.ConfigDeployRequestPnf;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.catalog.beans.PnfResourceCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {CatalogDbClient.class, ExceptionBuilder.class, PrepareConfigDeployDelegate.class})
public class PrepareConfigDeployDelegateTest {

    private static String TEST_MODEL_UUID = "6bc0b04d-1873-4721-b53d-6615225b2a28";
    private static String TEST_SERVICE_INSTANCE_ID = "test_service_id";
    private static String NONEXIST_MODEL_UUID = "a55323a2-d34c-4e05-b0c3-426df15294ba";
    private static String TEST_PROCESS_KEY = "processKey1";
    private static String TEST_PNF_RESOURCE_INSTANCE_NAME = "PNF_demo_resource";
    private static String TEST_PNF_CORRELATION_ID = "PNFDemo";
    private static String TEST_PNF_RESOURCE_BLUEPRINT_NAME = "blueprintOnap";
    private static String TEST_PNF_RESOURCE_BLUEPRINT_VERSION = "1.0.1";
    private static String TEST_PNF_RESOURCE_CUSTOMIZATION_UUID = "9acb3a83-8a52-412c-9a45-901764938144";
    private static String TEST_MSO_REQUEST_ID = "ff874603-4222-11e7-9252-005056850d2e";
    private static String TEST_PNF_UUID = "5df8b6de-2083-11e7-93ae-92361f002671";

    @MockBean
    private CatalogDbClient catalogDbClient;

    @Autowired
    private PrepareConfigDeployDelegate prepareConfigDeployDelegate;

    private DelegateExecution execution = new DelegateExecutionFake();

    @Before
    public void setUp() throws Exception {
        Service service = buildService();
        given(catalogDbClient.getServiceByID(TEST_MODEL_UUID)).willReturn(service);
        execution.setVariable("testProcessKey", TEST_PROCESS_KEY);
        execution.setVariable(PNF_CORRELATION_ID, TEST_PNF_CORRELATION_ID);
        execution.setVariable(MODEL_UUID, TEST_MODEL_UUID);
        execution.setVariable(SERVICE_INSTANCE_ID, TEST_SERVICE_INSTANCE_ID);
        execution.setVariable(MSO_REQUEST_ID, TEST_MSO_REQUEST_ID);
        execution.setVariable(PNF_UUID, TEST_PNF_UUID);
    }

    private Service buildService() {
        Service service = new Service();
        service.setModelUUID(TEST_MODEL_UUID);
        List<PnfResourceCustomization> pnfResourceCustomizationList = new ArrayList<>();
        pnfResourceCustomizationList.add(buildPnfResourceCustomization());
        service.setPnfCustomizations(pnfResourceCustomizationList);
        return service;
    }

    private PnfResourceCustomization buildPnfResourceCustomization() {
        PnfResourceCustomization pnfResourceCustomization = new PnfResourceCustomization();
        pnfResourceCustomization.setSkipPostInstConf(true);
        pnfResourceCustomization.setBlueprintName(TEST_PNF_RESOURCE_BLUEPRINT_NAME);
        pnfResourceCustomization.setBlueprintVersion(TEST_PNF_RESOURCE_BLUEPRINT_VERSION);
        pnfResourceCustomization.setModelInstanceName(TEST_PNF_RESOURCE_INSTANCE_NAME);
        pnfResourceCustomization.setModelCustomizationUUID(TEST_PNF_RESOURCE_CUSTOMIZATION_UUID);
        return pnfResourceCustomization;
    }

    @Test
    public void testExecution_validServiceValidPnf_executionObjectCreated() {
        try {
            prepareConfigDeployDelegate.execute(execution);
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
    public void testExecution_validServiceEmptyPnf_exceptionThrown() {
        Service service = buildService();
        service.setPnfCustomizations(Collections.EMPTY_LIST);
        given(catalogDbClient.getServiceByID(TEST_MODEL_UUID)).willReturn(service);
        assertThatThrownBy(() -> prepareConfigDeployDelegate.execute(execution)).isInstanceOf(BpmnError.class);
        assertThat(execution.getVariable("WorkflowExceptionErrorMessage")).asString()
            .contains("unable to find the PNF resource customization");
        assertThat(execution.getVariable("WorkflowException")).isInstanceOf(WorkflowException.class);
    }

    @Test
    public void testExecution_invalidCatalogdb_exceptionThrown() {
        given(catalogDbClient.getServiceByID(TEST_MODEL_UUID))
            .willThrow(new EntityNotFoundException("Entity not found"));
        assertThatThrownBy(() -> prepareConfigDeployDelegate.execute(execution)).isInstanceOf(BpmnError.class);
        assertThat(execution.getVariable("WorkflowExceptionErrorMessage")).asString()
            .contains("Entity not found");
        assertThat(execution.getVariable("WorkflowException")).isInstanceOf(WorkflowException.class);
    }

    private void checkCDSPropertiesBean(AbstractCDSPropertiesBean executionObject) {
        assertThat(executionObject.getBlueprintName()).matches(TEST_PNF_RESOURCE_BLUEPRINT_NAME);
        assertThat(executionObject.getBlueprintVersion()).matches(TEST_PNF_RESOURCE_BLUEPRINT_VERSION);
        assertThat(executionObject.getRequestId()).matches(TEST_MSO_REQUEST_ID);
        assertThat(executionObject.getSubRequestId()).matches(TEST_PNF_UUID);
        assertThat(executionObject.getMode()).matches("sync");
        assertThat(executionObject.getActionName()).matches("config-deploy");
        assertThat(executionObject.getOriginatorId()).matches("SO");

        assertThat(executionObject.getRequestObject()).isNotNull();
        String requestObject = executionObject.getRequestObject();

        checkRequestJson(requestObject);
    }

    private void checkRequestJson(String requestObject) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode tree = mapper.readTree(requestObject);
            ConfigDeployRequestPnf configDeployRequestPnf = mapper
                .treeToValue(tree.at("/config-deploy-request"), ConfigDeployRequestPnf.class);
            assertThat(configDeployRequestPnf.getResolutionKey()).matches(TEST_PNF_RESOURCE_INSTANCE_NAME);

            ConfigDeployPropertiesForPnf properties = configDeployRequestPnf
                .getConfigDeployPropertiesForPnf();
            assertThat(properties.getServiceInstanceId()).matches(TEST_SERVICE_INSTANCE_ID);
            assertThat(properties.getPnfName()).matches(TEST_PNF_RESOURCE_INSTANCE_NAME);
            assertThat(properties.getPnfId()).matches(TEST_PNF_CORRELATION_ID);
            assertThat(properties.getPnfCustomizationUuid()).matches(TEST_PNF_RESOURCE_CUSTOMIZATION_UUID);
            assertThat(properties.getServiceModelUuid()).matches(TEST_MODEL_UUID);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Check request body is json message" + e.getMessage());
        }
    }
}