/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix
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

package org.onap.so.client.cds;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonParser;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PnfCDSRequestProviderTest {

    @InjectMocks
    private PnfCDSRequestProvider pnfCDSRequestProvider;

    private static final String DOWNLOAD_ACTION = "downloadNESw";
    private static final String ACTIVATE_ACTION = "activateNESw";
    private static final String TEST_MODEL_UUID = "6bc0b04d-1873-4721-b53d-6615225b2a28";
    private static final String TEST_SERVICE_INSTANCE_ID = "test_service_id";
    private static final String TEST_PROCESS_KEY = "processKey1";
    private static final String TEST_PNF_RESOURCE_INSTANCE_NAME = "PNF_demo_resource";
    private static final String TEST_PNF_CORRELATION_ID = "PNFDemo";
    private static final String TEST_PNF_RESOURCE_CUSTOMIZATION_UUID = "9acb3a83-8a52-412c-9a45-901764938144";
    private static final String TEST_MSO_REQUEST_ID = "ff874603-4222-11e7-9252-005056850d2e";
    private static final String TEST_PNF_UUID = "5df8b6de-2083-11e7-93ae-92361f002671";
    private static final String TEST_SOFTWARE_VERSION = "demo-sw-ver2.0.0";
    private static final String PNF_CORRELATION_ID = "pnfCorrelationId";
    private static final String PNF_UUID = "pnfUuid";
    private static final String SERVICE_INSTANCE_ID = "serviceInstanceId";
    private static final String MSO_REQUEST_ID = "msoRequestId";
    private static final String MODEL_UUID = "modelUuid";
    private static final String PRC_CUSTOMIZATION_UUID = "PRC_customizationUuid";
    private static final String PRC_INSTANCE_NAME = "PRC_instanceName";
    private static final String PRC_TARGET_SOFTWARE_VERSION = "targetSoftwareVersion";
    private static final String SCOPE = "scope";
    private static final String ACTION = "action";
    private static final String PROCESS_KEY = "testProcessKey";
    private static final String PRC_BLUEPRINT_NAME = "PRC_blueprintName";
    private static final String PRC_BLUEPRINT_VERSION = "PRC_blueprintVersion";
    private static final String TEST_PNF_RESOURCE_BLUEPRINT_NAME = "blueprintOnap";
    private static final String TEST_PNF_RESOURCE_BLUEPRINT_VERSION = "1.0.1";

    @Test
    public void testBuildRequestPayloadDownloadActionPnf() {
        try {
            runTest(DOWNLOAD_ACTION);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testBuildRequestPayloadActivateActionPnf() {
        try {
            runTest(ACTIVATE_ACTION);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    private void runTest(String action) throws Exception {
        // given
        DelegateExecution execution = prepareDelegateExecutionObj(PayloadConstants.PNF_SCOPE, action);

        // when
        pnfCDSRequestProvider.setExecutionObject(execution);
        String payload = pnfCDSRequestProvider.buildRequestPayload(action).get();
        System.out.println(payload);

        // verify
        ObjectMapper mapper = new ObjectMapper();
        JsonNode payloadJson = mapper.readTree(payload);
        JsonNode requestNode = payloadJson.findValue(action + "-request");
        JsonNode propertiesNode = payloadJson.findValue(action + "-properties");

        assertNotNull(payload);
        assertTrue(verfiyJsonFromString(payload));
        assertThat(requestNode.get("resolution-key").asText()).isEqualTo(TEST_PNF_CORRELATION_ID);
        assertThat(propertiesNode.get("service-instance-id").asText()).isEqualTo(TEST_SERVICE_INSTANCE_ID);
        assertThat(propertiesNode.get("service-model-uuid").asText()).isEqualTo(TEST_MODEL_UUID);
        assertThat(propertiesNode.get("pnf-id").asText()).isEqualTo(TEST_PNF_UUID);
        assertThat(propertiesNode.get("pnf-customization-uuid").asText())
                .isEqualTo(TEST_PNF_RESOURCE_CUSTOMIZATION_UUID);
        assertThat(propertiesNode.get("target-software-version").asText()).isEqualTo(TEST_SOFTWARE_VERSION);
        assertThat(pnfCDSRequestProvider.getBlueprintName()).isEqualTo(TEST_PNF_RESOURCE_BLUEPRINT_NAME);
        assertThat(pnfCDSRequestProvider.getBlueprintVersion()).isEqualTo(TEST_PNF_RESOURCE_BLUEPRINT_VERSION);
    }

    private DelegateExecution prepareDelegateExecutionObj(String scope, String action) {
        DelegateExecution execution = new DelegateExecutionFake();
        execution.setVariable(PROCESS_KEY, TEST_PROCESS_KEY);
        execution.setVariable(PNF_CORRELATION_ID, TEST_PNF_CORRELATION_ID);
        execution.setVariable(MODEL_UUID, TEST_MODEL_UUID);
        execution.setVariable(SERVICE_INSTANCE_ID, TEST_SERVICE_INSTANCE_ID);
        execution.setVariable(MSO_REQUEST_ID, TEST_MSO_REQUEST_ID);
        execution.setVariable(PNF_UUID, TEST_PNF_UUID);
        execution.setVariable(PRC_INSTANCE_NAME, TEST_PNF_RESOURCE_INSTANCE_NAME);
        execution.setVariable(PRC_CUSTOMIZATION_UUID, TEST_PNF_RESOURCE_CUSTOMIZATION_UUID);
        execution.setVariable(PRC_TARGET_SOFTWARE_VERSION, TEST_SOFTWARE_VERSION);
        execution.setVariable(SCOPE, scope);
        execution.setVariable(ACTION, action);
        execution.setVariable(PRC_BLUEPRINT_NAME, TEST_PNF_RESOURCE_BLUEPRINT_NAME);
        execution.setVariable(PRC_BLUEPRINT_VERSION, TEST_PNF_RESOURCE_BLUEPRINT_VERSION);
        return execution;
    }

    private boolean verfiyJsonFromString(String payload) {
        JsonParser parser = new JsonParser();
        return parser.parse(payload).isJsonObject();
    }

}
