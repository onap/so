/*
 * ============LICENSE_START======================================================= Copyright (C) 2019 Nordix
 * Foundation. ================================================================================ Licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 *
 * SPDX-License-Identifier: Apache-2.0 ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.pnf.delegate;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.aai.domain.yang.Pnf;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.bpmn.infrastructure.pnf.management.PnfManagement;
import org.onap.so.client.cds.beans.*;
import org.onap.so.client.exception.ExceptionBuilder;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.io.IOException;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ExceptionBuilder.class, GenericRequestObjectCreater.class})
public class GenericRequestObjectCreaterTest {

    private static final String TEST_MODEL_UUID = "6bc0b04d-1873-4721-b53d-6615225b2a28";
    private static final String TEST_SERVICE_INSTANCE_ID = "test_service_id";
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
    private static final String TEST_DEPLOY_ACTION = "config-deploy";
    private static final String TEST_ASSIGN_ACTION = "config-assign";
    private static final String TEST_SOFTWARE_VERSION = "c120ed333d3";
    private static final String TEST_FTP_URL = "ftp://test.url";
    private static final String TEST_FTP_USERNAME = "testusername";
    private static final String TEST_FTP_PASSWORD = "ftptestpassword";
    private static final String EXPECTED_DEPLOY_JSON =
            "{\"config-deploy-request\":" + "{\"config-deploy-properties\":" + "{\"pnf-ipv6-address\":\"::1/128\","
                    + "\"service-instance-id\":\"test_service_id\"," + "\"pnf-ipv4-address\":\"1.1.1.1\","
                    + "\"pnf-customization-uuid\":\"9acb3a83-8a52-412c-9a45-901764938144\","
                    + "\"pnf-id\":\"5df8b6de-2083-11e7-93ae-92361f002671\","
                    + "\"pnf-name\":\"PNFDemo\",\"service-model-uuid\":\"6bc0b04d-1873-4721-b53d-6615225b2a28\"},"
                    + "\"resolution-key\":\"PNFDemo\"" + "}" + "}";
    private static final String EXPECTED_ASSIGN_JSON = "{\"config-assign-request\"" + ":{\"config-assign-properties\""
            + ":{\"service-instance-id\":\"test_service_id\""
            + ",\"pnf-customization-uuid\":\"9acb3a83-8a52-412c-9a45-901764938144\""
            + ",\"pnf-id\":\"5df8b6de-2083-11e7-93ae-92361f002671\"" + ",\"pnf-name\":\"PNFDemo\""
            + ",\"service-model-uuid\":\"6bc0b04d-1873-4721-b53d-6615225b2a28\"}"
            + ",\"resolution-key\":\"PNFDemo\"}}\n";

    @Autowired
    private GenericRequestObjectCreater genericRequestObjectCreater;

    @MockBean
    private PnfManagement pnfManagement;

    private DelegateExecution execution = new DelegateExecutionFake();

    @Before
    public void setUp() throws IOException {
        execution.setVariable("testProcessKey", TEST_PROCESS_KEY);
        execution.setVariable(PNF_CORRELATION_ID, TEST_PNF_CORRELATION_ID);
        execution.setVariable(MODEL_UUID, TEST_MODEL_UUID);
        execution.setVariable(SERVICE_INSTANCE_ID, TEST_SERVICE_INSTANCE_ID);
        execution.setVariable(MSO_REQUEST_ID, TEST_MSO_REQUEST_ID);
        execution.setVariable(PNF_UUID, TEST_PNF_UUID);
        execution.setVariable(PRC_INSTANCE_NAME, TEST_PNF_RESOURCE_INSTANCE_NAME);
        execution.setVariable(PRC_CUSTOMIZATION_UUID, TEST_PNF_RESOURCE_CUSTOMIZATION_UUID);
        execution.setVariable(PRC_BLUEPRINT_NAME, TEST_PNF_RESOURCE_BLUEPRINT_NAME);
        execution.setVariable(PRC_BLUEPRINT_VERSION, TEST_PNF_RESOURCE_BLUEPRINT_VERSION);
        execution.setVariable(SOFTWARE_VERSION, TEST_SOFTWARE_VERSION);
        execution.setVariable(SOFTWARE_FTP_URL, TEST_FTP_URL);
        execution.setVariable(SOFTWARE_FTP_USERNAME, TEST_FTP_USERNAME);
        execution.setVariable(SOFTWARE_FTP_PASSWORD, TEST_FTP_PASSWORD);
        mockAai();
    }

    private void mockAai() throws IOException {
        Pnf pnf = new Pnf();
        pnf.setIpaddressV4Oam(TEST_IPV4_ADDRESS);
        pnf.setIpaddressV6Oam(TEST_IPV6_ADDRESS);
        when(pnfManagement.getEntryFor(TEST_PNF_CORRELATION_ID)).thenReturn(Optional.of(pnf));
    }

    @Test
    public void test_dynamic_yaml_file_load_for_config_deploy() {

        String output = genericRequestObjectCreater.getdynamicRequestObject(execution,
                GenericRequestObjectCreater.Action.DEPLOY);
        System.out.println(output);
        JSONAssert.assertEquals(EXPECTED_DEPLOY_JSON, output, false);
    }

    @Test
    public void test_dynamic_yaml_file_load_for_download() {
        String expectedJsonString = "{\"sw-download-request\":" + "{\"sw-download-properties\":"
                + "{\"pnf-ipv6-address\":\"::1/128\"," + "\"service-instance-id\":\"test_service_id\","
                + "\"pnf-ipv4-address\":\"1.1.1.1\","
                + "\"pnf-customization-uuid\":\"9acb3a83-8a52-412c-9a45-901764938144\","
                + "\"ftp-url\":\"ftp://test.url\"," + "\"ftp-username\":\"testusername\","
                + "\"pnf-id\":\"5df8b6de-2083-11e7-93ae-92361f002671\"," + "\"sofware-version\":\"c120ed333d3\","
                + "\"pnf-name\":\"PNFDemo\"," + "\"service-model-uuid\":\"6bc0b04d-1873-4721-b53d-6615225b2a28\","
                + "\"ftp-password\":\"ftptestpassword\"}," + "\"resolution-key\":\"PNFDemo\"" + "}" + "}";
        String output = genericRequestObjectCreater.getdynamicRequestObject(execution,
                GenericRequestObjectCreater.Action.DOWNLOAD);
        System.out.println(output);
        JSONAssert.assertEquals(expectedJsonString, output, false);
    }

    @Test
    public void test_dynamic_yaml_file_load_for_activate() {
        String expectedJsonString = "{\"sw-activate-request\":" + "{\"sw-activate-properties\":"
                + "{\"pnf-ipv6-address\":\"::1/128\"," + "\"service-instance-id\":\"test_service_id\","
                + "\"pnf-ipv4-address\":\"1.1.1.1\","
                + "\"pnf-customization-uuid\":\"9acb3a83-8a52-412c-9a45-901764938144\","
                + "\"pnf-id\":\"5df8b6de-2083-11e7-93ae-92361f002671\"," + "\"sofware-version\":\"c120ed333d3\","
                + "\"pnf-name\":\"PNFDemo\"," + "\"service-model-uuid\":\"6bc0b04d-1873-4721-b53d-6615225b2a28\"},"
                + "\"resolution-key\":\"PNFDemo\"" + "}" + "}";
        String output = genericRequestObjectCreater.getdynamicRequestObject(execution,
                GenericRequestObjectCreater.Action.ACTIVATE);
        System.out.println(output);
        JSONAssert.assertEquals(expectedJsonString, output, false);
    }

    @Test
    public void testExecution_validPnf_deploy_executionObjectCreated() {
        try {
            execution.setVariable("actionName", TEST_DEPLOY_ACTION);
            genericRequestObjectCreater.execute(execution);
            Object executionObject = execution.getVariable(EXECUTION_OBJECT);
            assertThat(executionObject).isNotNull();
            assertThat(executionObject).isInstanceOf(AbstractCDSPropertiesBean.class);
            checkCDSPropertiesBean((AbstractCDSPropertiesBean) executionObject,
                    GenericRequestObjectCreater.Action.DEPLOY, EXPECTED_DEPLOY_JSON);
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
            fail("Exception thrown" + e.getMessage());
        }
        execution.setVariable("actionName", TEST_DEPLOY_ACTION);
        assertThatThrownBy(() -> genericRequestObjectCreater.execute(execution)).isInstanceOf(BpmnError.class);
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
            fail("Exception thrown" + e.getMessage());
        }
        execution.setVariable("actionName", TEST_DEPLOY_ACTION);
        assertThatThrownBy(() -> genericRequestObjectCreater.execute(execution)).isInstanceOf(BpmnError.class);
        assertThat(execution.getVariable("WorkflowExceptionErrorMessage")).asString()
                .contains("AAI entry for PNF: " + TEST_PNF_CORRELATION_ID + " does not exist");
        assertThat(execution.getVariable("WorkflowException")).isInstanceOf(WorkflowException.class);
    }

    @Test
    public void test_dynamic_yaml_file_load_for_config_assign() {

        JSONAssert.assertEquals(EXPECTED_ASSIGN_JSON, genericRequestObjectCreater.getdynamicRequestObject(execution,
                GenericRequestObjectCreater.Action.ASSIGN), false);
    }

    @Test
    public void testExecution_validPnf_assign_executionObjectCreated() {
        try {
            execution.setVariable("actionName", TEST_ASSIGN_ACTION);
            genericRequestObjectCreater.execute(execution);
            Object executionObject = execution.getVariable(EXECUTION_OBJECT);
            assertThat(executionObject).isNotNull();
            assertThat(executionObject).isInstanceOf(AbstractCDSPropertiesBean.class);
            checkCDSPropertiesBean((AbstractCDSPropertiesBean) executionObject,
                    GenericRequestObjectCreater.Action.ASSIGN, EXPECTED_ASSIGN_JSON);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception thrown" + e.getMessage());
        }
    }

    private void checkCDSPropertiesBean(AbstractCDSPropertiesBean executionObject,
            GenericRequestObjectCreater.Action action, String expectedJson) {
        assertThat(executionObject.getBlueprintName()).matches(TEST_PNF_RESOURCE_BLUEPRINT_NAME);
        assertThat(executionObject.getBlueprintVersion()).matches(TEST_PNF_RESOURCE_BLUEPRINT_VERSION);
        assertThat(executionObject.getRequestId()).matches(TEST_MSO_REQUEST_ID);
        assertThat(executionObject.getSubRequestId()).matches(TEST_PNF_UUID);
        assertThat(executionObject.getMode()).matches(action.getMode().getType());
        assertThat(executionObject.getActionName()).matches(action.getType());
        assertThat(executionObject.getOriginatorId()).matches("SO");

        assertThat(executionObject.getRequestObject()).isNotNull();
        String requestObject = executionObject.getRequestObject();

        JSONAssert.assertEquals(expectedJson, requestObject, false);
    }
}
