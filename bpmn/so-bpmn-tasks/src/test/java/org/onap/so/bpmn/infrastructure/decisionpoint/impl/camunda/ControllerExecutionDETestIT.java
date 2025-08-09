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

import com.google.protobuf.Struct;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers;
import org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.so.BaseIntegrationTest;
import org.onap.so.GrpcNettyServer;
import org.onap.aaiclient.client.aai.AAIVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.*;

@RunWith(Parameterized.class)
public class ControllerExecutionDETestIT extends BaseIntegrationTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @ClassRule
    public static final SpringClassRule springClassRule = new SpringClassRule();

    @Rule
    public final SpringMethodRule smr = new SpringMethodRule();

    private static final String DOWNLOAD_ACTION = "downloadNESw";
    private static final String ACTIVATE_ACTION = "activateNESw";
    private static final String PRECHECK_ACTION = "precheck";
    private static final String POSTCHECK_ACTION = "postcheck";
    private static final String ASSIGN_ACTION = "config-assign";
    private static final String DEPLOY_ACTION = "config-deploy";
    private static final String CDS_ACTOR = "cds";

    @Autowired
    private ControllerExecutionDE controllerExecutionDE;

    @Autowired
    private GrpcNettyServer grpcNettyServer;

    private static String TEST_MODEL_UUID = "6bc0b04d-1873-4721-b53d-6615225b2a28";
    private static String TEST_SERVICE_INSTANCE_ID = "test_service_id";
    private static String TEST_PROCESS_KEY = "processKey1";
    private static String TEST_MSO_REQUEST_ID = "ff874603-4222-11e7-9252-005056850d2e";

    private static final AAIVersion VERSION = AAIVersion.LATEST;

    private static String TEST_PNF_RESOURCE_INSTANCE_NAME = "PNF_demo_resource";
    private static String TEST_PNF_CORRELATION_ID = "PNFDemo";
    private static String TEST_PNF_RESOURCE_BLUEPRINT_NAME = "blueprintOnap";
    private static String TEST_PNF_RESOURCE_BLUEPRINT_VERSION = "1.0.1";
    private static String TEST_PNF_RESOURCE_CUSTOMIZATION_UUID = "9acb3a83-8a52-412c-9a45-901764938144";
    private static String TEST_PNF_UUID = "5df8b6de-2083-11e7-93ae-92361f002671";
    private static String TEST_SOFTWARE_VERSION = "demo-sw-ver2.0.0";

    private String description;
    private String action;
    private String scope;

    public ControllerExecutionDETestIT(String desc, String action, String scope) {
        this.description = desc;
        this.action = action;
        this.scope = scope;

    }

    @Parameterized.Parameters(name = "index {0}")
    public static Collection<String[]> data() {
        return Arrays.asList(
                new String[][] {{"Test JSON for action:" + ACTIVATE_ACTION + " scope:pnf", ACTIVATE_ACTION, "pnf"},
                        {"Test JSON for action:" + DOWNLOAD_ACTION + " scope:pnf", DOWNLOAD_ACTION, "pnf"},
                        {"Test JSON for action:" + ASSIGN_ACTION + " scope:pnf", ASSIGN_ACTION, "pnf"},
                        {"Test JSON for action:" + DEPLOY_ACTION + " scope:pnf", DEPLOY_ACTION, "pnf"},
                        {"Test JSON for action:" + PRECHECK_ACTION + " scope:pnf", PRECHECK_ACTION, "pnf"},
                        {"Test JSON for action:" + POSTCHECK_ACTION + " scope:pnf", POSTCHECK_ACTION, "pnf"}});
    }

    @Before
    public void setUp() {
        delegateExecution.setVariable("testProcessKey", TEST_PROCESS_KEY);
        delegateExecution.setVariable(PNF_CORRELATION_ID, TEST_PNF_CORRELATION_ID);
        delegateExecution.setVariable(MODEL_UUID, TEST_MODEL_UUID);
        delegateExecution.setVariable(SERVICE_INSTANCE_ID, TEST_SERVICE_INSTANCE_ID);
        delegateExecution.setVariable(MSO_REQUEST_ID, TEST_MSO_REQUEST_ID);
        delegateExecution.setVariable(PNF_UUID, TEST_PNF_UUID);
        delegateExecution.setVariable(PRC_INSTANCE_NAME, TEST_PNF_RESOURCE_INSTANCE_NAME);
        delegateExecution.setVariable(PRC_CUSTOMIZATION_UUID, TEST_PNF_RESOURCE_CUSTOMIZATION_UUID);
        delegateExecution.setVariable(PRC_BLUEPRINT_NAME, TEST_PNF_RESOURCE_BLUEPRINT_NAME);
        delegateExecution.setVariable(PRC_BLUEPRINT_VERSION, TEST_PNF_RESOURCE_BLUEPRINT_VERSION);
        delegateExecution.setVariable("targetSoftwareVersion", TEST_SOFTWARE_VERSION);

        delegateExecution.setVariable("actor", CDS_ACTOR);
        delegateExecution.setVariable("action", this.action);
        delegateExecution.setVariable("scope", this.scope);


        /**
         * Get the PNF entry from AAI.
         */
        if (action.equalsIgnoreCase(DEPLOY_ACTION)) {
            final String aaiPnfEntry = "{  \n" + "   \"pnf-name\":\"PNFDemo\",\n" + "   \"pnf-id\":\"testtest\",\n"
                    + "   \"in-maint\":true,\n" + "   \"resource-version\":\"1541720264047\",\n"
                    + "   \"ipaddress-v4-oam\":\"1.1.1.1\",\n" + "   \"ipaddress-v6-oam\":\"::/128\"\n" + "}";
            wireMockServer.stubFor(
                    get(urlEqualTo("/aai/" + VERSION + "/network/pnfs/pnf/PNFDemo")).willReturn(okJson(aaiPnfEntry)));
        }

        grpcNettyServer.cleanMessage();
    }

    @Test
    public void testExecution_cds_actions() {

        controllerExecutionDE.execute(delegateExecution);
        List<ExecutionServiceInput> detailedMessages = grpcNettyServer.getDetailedMessages();
        assertThat(detailedMessages).hasSize(1);
        try {
            verifyRequestContentForAction(detailedMessages.get(0));
        } catch (Exception e) {
            e.printStackTrace();
            fail(this.action + " request exception", e);
        }
    }

    private void verifyRequestContentForAction(ExecutionServiceInput executionServiceInput) {

        logger.info("Checking the {} request", this.action);
        ActionIdentifiers actionIdentifiers = executionServiceInput.getActionIdentifiers();

        assertThat(actionIdentifiers.getBlueprintName()).isEqualTo(TEST_PNF_RESOURCE_BLUEPRINT_NAME);
        assertThat(actionIdentifiers.getBlueprintVersion()).isEqualTo(TEST_PNF_RESOURCE_BLUEPRINT_VERSION);
        assertThat(actionIdentifiers.getActionName()).isEqualTo(this.action);

        CommonHeader commonHeader = executionServiceInput.getCommonHeader();
        assertThat(commonHeader.getOriginatorId()).isEqualTo("SO");
        assertThat(commonHeader.getRequestId()).isEqualTo(TEST_MSO_REQUEST_ID);

        Struct payload = executionServiceInput.getPayload();
        Struct requeststruct = payload.getFieldsOrThrow(this.action + "-request").getStructValue();

        assertThat(requeststruct.getFieldsOrThrow("resolution-key").getStringValue())
                .isEqualTo(TEST_PNF_CORRELATION_ID);

        Struct propertiesStruct = requeststruct.getFieldsOrThrow(this.action + "-properties").getStructValue();
        assertThat(propertiesStruct.getFieldsOrThrow("pnf-name").getStringValue()).isEqualTo(TEST_PNF_CORRELATION_ID);
        assertThat(propertiesStruct.getFieldsOrThrow("service-model-uuid").getStringValue()).isEqualTo(TEST_MODEL_UUID);
        assertThat(propertiesStruct.getFieldsOrThrow("pnf-customization-uuid").getStringValue())
                .isEqualTo(TEST_PNF_RESOURCE_CUSTOMIZATION_UUID);
        if (action.equalsIgnoreCase(DEPLOY_ACTION)) {
            assertThat(actionIdentifiers.getMode()).isEqualTo("async");
            assertThat(propertiesStruct.getFieldsOrThrow("pnf-ipv4-address").getStringValue()).isEqualTo("1.1.1.1");
            assertThat(propertiesStruct.getFieldsOrThrow("pnf-ipv6-address").getStringValue()).isEqualTo("::/128");
        } else if (!action.equalsIgnoreCase(ASSIGN_ACTION)) {
            assertThat(actionIdentifiers.getMode()).isEqualTo("sync");
            assertThat(propertiesStruct.getFieldsOrThrow("target-software-version").getStringValue())
                    .isEqualTo(TEST_SOFTWARE_VERSION);
        } else {
            assertThat(actionIdentifiers.getMode()).isEqualTo("sync");
        }
    }

}
