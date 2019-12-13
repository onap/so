/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix
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

package org.onap.so.bpmn.infrastructure.decisionpoint.impl.camunda;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.MODEL_UUID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.MSO_REQUEST_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_CORRELATION_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_UUID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PRC_BLUEPRINT_NAME;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PRC_BLUEPRINT_VERSION;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PRC_CUSTOMIZATION_UUID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PRC_INSTANCE_NAME;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.SERVICE_INSTANCE_ID;
import com.google.protobuf.Struct;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers;
import org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.so.BaseIntegrationTest;
import org.onap.so.GrpcNettyServer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ControllerExecutionDETestIT extends BaseIntegrationTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ControllerExecutionDE controllerExecutionDE;

    @Autowired
    private GrpcNettyServer grpcNettyServer;

    private GenericVnf genericVnf;

    private static String TEST_MODEL_UUID = "6bc0b04d-1873-4721-b53d-6615225b2a28";
    private static String TEST_SERVICE_INSTANCE_ID = "test_service_id";
    private static String TEST_PROCESS_KEY = "processKey1";
    private static String TEST_MSO_REQUEST_ID = "ff874603-4222-11e7-9252-005056850d2e";

    private static String TEST_CDS_ACTION = "config-assign";
    private static String TEST_APPC_ACTION = "HealthCheck";

    private static String TEST_PNF_RESOURCE_INSTANCE_NAME = "PNF_demo_resource";
    private static String TEST_PNF_CORRELATION_ID = "PNFDemo";
    private static String TEST_PNF_RESOURCE_BLUEPRINT_NAME = "blueprintOnap";
    private static String TEST_PNF_RESOURCE_BLUEPRINT_VERSION = "1.0.1";
    private static String TEST_PNF_RESOURCE_CUSTOMIZATION_UUID = "9acb3a83-8a52-412c-9a45-901764938144";
    private static String TEST_PNF_UUID = "5df8b6de-2083-11e7-93ae-92361f002671";

    @Before
    public void setUp() {
        delegateExecution.setVariable(MODEL_UUID, TEST_MODEL_UUID);
        delegateExecution.setVariable(SERVICE_INSTANCE_ID, TEST_SERVICE_INSTANCE_ID);
        delegateExecution.setVariable(MSO_REQUEST_ID, TEST_MSO_REQUEST_ID);
        delegateExecution.setVariable("testProcessKey", TEST_PROCESS_KEY);

        grpcNettyServer.cleanMessage();
    }

    @Test
    public void testExecution_cdsConfigAssign_actionExecuted() {

        configureCdsConfigAssign();

        controllerExecutionDE.execute(delegateExecution);
        List<ExecutionServiceInput> detailedMessages = grpcNettyServer.getDetailedMessages();
        assertThat(detailedMessages).hasSize(1);
        try {
            checkConfigAssign(detailedMessages.get(0));
        } catch (Exception e) {
            e.printStackTrace();
            fail("ConfigAssign request exception", e);
        }
    }

    private void configureCdsConfigAssign() {
        delegateExecution.setVariable("actor", "cds");
        delegateExecution.setVariable("action", TEST_CDS_ACTION);
        delegateExecution.setVariable("scope", "pnf");

        delegateExecution.setVariable(PNF_CORRELATION_ID, TEST_PNF_CORRELATION_ID);
        delegateExecution.setVariable(PNF_UUID, TEST_PNF_UUID);
        delegateExecution.setVariable(PRC_INSTANCE_NAME, TEST_PNF_RESOURCE_INSTANCE_NAME);
        delegateExecution.setVariable(PRC_CUSTOMIZATION_UUID, TEST_PNF_RESOURCE_CUSTOMIZATION_UUID);
        delegateExecution.setVariable(PRC_BLUEPRINT_NAME, TEST_PNF_RESOURCE_BLUEPRINT_NAME);
        delegateExecution.setVariable(PRC_BLUEPRINT_VERSION, TEST_PNF_RESOURCE_BLUEPRINT_VERSION);
    }

    private void checkConfigAssign(ExecutionServiceInput executionServiceInput) {

        logger.info("Checking the configAssign request");
        ActionIdentifiers actionIdentifiers = executionServiceInput.getActionIdentifiers();

        /**
         * the fields of actionIdentifiers should match the one in the
         * response/createVcpeResCustServiceSimplifiedTest_catalogdb.json.
         */
        assertThat(actionIdentifiers.getBlueprintName()).isEqualTo(TEST_PNF_RESOURCE_BLUEPRINT_NAME);
        assertThat(actionIdentifiers.getBlueprintVersion()).isEqualTo(TEST_PNF_RESOURCE_BLUEPRINT_VERSION);
        assertThat(actionIdentifiers.getActionName()).isEqualTo(TEST_CDS_ACTION);
        assertThat(actionIdentifiers.getMode()).isEqualTo("sync");

        CommonHeader commonHeader = executionServiceInput.getCommonHeader();
        assertThat(commonHeader.getOriginatorId()).isEqualTo("SO");
        assertThat(commonHeader.getRequestId()).isEqualTo(TEST_MSO_REQUEST_ID);

        Struct payload = executionServiceInput.getPayload();
        Struct requeststruct = payload.getFieldsOrThrow("config-assign-request").getStructValue();

        assertThat(requeststruct.getFieldsOrThrow("resolution-key").getStringValue())
                .isEqualTo(TEST_PNF_CORRELATION_ID);
        Struct propertiesStruct = requeststruct.getFieldsOrThrow("config-assign-properties").getStructValue();

        assertThat(propertiesStruct.getFieldsOrThrow("pnf-name").getStringValue()).isEqualTo(TEST_PNF_CORRELATION_ID);
        assertThat(propertiesStruct.getFieldsOrThrow("service-model-uuid").getStringValue()).isEqualTo(TEST_MODEL_UUID);
        assertThat(propertiesStruct.getFieldsOrThrow("pnf-customization-uuid").getStringValue())
                .isEqualTo(TEST_PNF_RESOURCE_CUSTOMIZATION_UUID);
    }

}
