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

package org.onap.so.bpmn.infrastructure.decisionpoint.impl.buildingblock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.appc.client.lcm.model.Action;
import org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers;
import org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.so.BaseIntegrationTest;
import org.onap.so.GrpcNettyServer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ControllerExecutionBBTestIT extends BaseIntegrationTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ControllerExecutionBB controllerExecutionBB;

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
        execution.setVariable(MODEL_UUID, TEST_MODEL_UUID);
        execution.setVariable(SERVICE_INSTANCE_ID, TEST_SERVICE_INSTANCE_ID);
        execution.setVariable(MSO_REQUEST_ID, TEST_MSO_REQUEST_ID);
        execution.setVariable("testProcessKey", TEST_PROCESS_KEY);

        grpcNettyServer.cleanMessage();
    }

    @Test
    @Ignore
    // TODO: re-activate this test case after the SO-CDS generic buildingblock implementation in place
    public void testExecution_cdsConfigAssign_actionExecuted() {

        configureCdsConfigAssign();

        controllerExecutionBB.execute(execution);
        List<ExecutionServiceInput> detailedMessages = grpcNettyServer.getDetailedMessages();
        assertThat(detailedMessages).hasSize(1);
        try {
            checkConfigAssign(detailedMessages.get(0));
        } catch (Exception e) {
            e.printStackTrace();
            fail("ConfigAssign request exception", e);
        }
    }

    @Test
    @Ignore
    // TODO: re-activate this test case after the SDNC controller is fully implemented.
    public void testExecution_sdncUpgradeHealthCheck_actionExecuted() {
        configureSdncUpgrade();
        controllerExecutionBB.execute(execution);

        verify(appCClient, times(1)).runAppCCommand(eq(Action.UpgradePreCheck), eq(TEST_MSO_REQUEST_ID), eq(null),
                any(Optional.class), any(HashMap.class), eq("sdnc"));
    }

    private void configureSdncUpgrade() {
        execution.setVariable("actor", "sdnc");
        execution.setVariable("action", "UpgradePreCheck");
        execution.setVariable("payload", "{ \n" + "   \"action\":[ \n" + "      { \n"
                + "         \"UpgradePreCheck\":{ \n" + "            \"payload\":{ \n"
                + "               \"pnf-flag\":\"true\",\n" + "               \"pnf-name\":\"5gDU0001\",\n"
                + "               \"pnfId\":\"5gDU0001\",\n"
                + "               \"ipaddress-v4-oam\":\"192.168.35.83\",\n"
                + "               \"oldSwVersion\":\"v1\",\n" + "               \"targetSwVersion\":\"v2\",\n"
                + "               \"ruleName\":\"r001\",\n" + "               \"Id\":\"10\",\n"
                + "               \"additionalData\":\"{}\"\n" + "            }\n" + "         }\n" + "      },\n"
                + "      { \n" + "         \"UpgradeSoftware\":{ \n" + "            \"payload\":{ \n"
                + "               \"pnf-flag\":\"true\",\n" + "               \"pnfId\":\"5gDU0001\",\n"
                + "               \"ipaddress-v4-oam\":\"192.168.35.83\",\n"
                + "               \"swToBeDownloaded\":[ \n" + "                  { \n"
                + "                     \"swLocation\":\"http://192.168.35.96:10080/ran_du_pkg1-v2.zip\",\n"
                + "                     \"swFileSize\":353,\n" + "                     \"swFileCompression\":\"ZIP\",\n"
                + "                     \"swFileFormat\":\"zip\"\n" + "                  }\n" + "               ]\n"
                + "            }\n" + "         }\n" + "      }\n" + "   ]\n" + "}");

        setServiceInstance();
        genericVnf = setGenericVnf();

        RequestContext requestContext = setRequestContext();
        requestContext.setMsoRequestId(TEST_MSO_REQUEST_ID);
        gBBInput.setRequestContext(requestContext);
    }

    private void configureCdsConfigAssign() {
        execution.setVariable("actor", "cds");
        execution.setVariable("scope", "pnf");
        execution.setVariable("action", TEST_CDS_ACTION);

        execution.setVariable(PNF_CORRELATION_ID, TEST_PNF_CORRELATION_ID);
        execution.setVariable(PNF_UUID, TEST_PNF_UUID);
        execution.setVariable(PRC_INSTANCE_NAME, TEST_PNF_RESOURCE_INSTANCE_NAME);
        execution.setVariable(PRC_CUSTOMIZATION_UUID, TEST_PNF_RESOURCE_CUSTOMIZATION_UUID);
        execution.setVariable(PRC_BLUEPRINT_NAME, TEST_PNF_RESOURCE_BLUEPRINT_NAME);
        execution.setVariable(PRC_BLUEPRINT_VERSION, TEST_PNF_RESOURCE_BLUEPRINT_VERSION);
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
