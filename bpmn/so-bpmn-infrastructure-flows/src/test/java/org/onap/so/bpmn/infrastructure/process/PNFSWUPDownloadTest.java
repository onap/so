/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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

package org.onap.so.bpmn.infrastructure.process;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.Struct;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers;
import org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.so.BaseBPMNTest;
import org.onap.so.GrpcNettyServer;
import org.onap.so.bpmn.mock.FileUtil;
import org.onap.so.client.aai.AAIVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static org.assertj.core.api.Assertions.fail;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat;

/**
 * Basic Integration test for PNFSWUPDownloadTest.bpmn workflow.
 */
public class PNFSWUPDownloadTest extends BaseBPMNTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final long WORKFLOW_WAIT_TIME = 1000L;

    private static final String TEST_PROCESSINSTANCE_KEY = "PNFSWUPDownload";
    private static final AAIVersion VERSION = AAIVersion.LATEST;
    private String testBusinessKey;
    private JsonObject requestObject;
    private String responseObject;
    private String msoRequestId;

    @Autowired
    private GrpcNettyServer grpcNettyServer;

    @Before
    public void setUp() throws IOException {

        requestObject = (JsonObject) new JsonParser().parse(FileUtil.readResourceFile("request/PNFSoftwareUpgradeTest.json"));
        responseObject = FileUtil.readResourceFile("response/PNFSoftwareUpgradeTest.json");

        variables.put("bpmnRequest", requestObject.toString());

        /**
         * This variable indicates that the flow was invoked asynchronously. It's injected by {@link WorkflowProcessor}.
         */
        variables.put("isAsyncProcess", "true");
        variables.put("resource_customization_uuid", "38dc9a92-214c-11e7-93ae-92361f002680");

        /**
         * Temporary solution to add pnfCorrelationId to context. this value is getting from the request to SO api
         * handler and then convert to CamudaInput
         */
        variables.put("pnfCorrelationId", "PNFDemo");

        /**
         * Create mso-request-id.
         */
        msoRequestId = UUID.randomUUID().toString();

        variables.put("mso-request-id", msoRequestId);

        /**
         * Create Business key for the process instance
         */
        testBusinessKey = UUID.randomUUID().toString();

        logger.info("Test the process instance: {} with business key: {}", TEST_PROCESSINSTANCE_KEY, testBusinessKey);

    }


    @Test
    public void workflow_validInput_expectedOutput() throws InterruptedException {

        mockCatalogDb();
        mockAai();

        ProcessInstance pi =
                runtimeService.startProcessInstanceByKey(TEST_PROCESSINSTANCE_KEY, testBusinessKey, variables);

        int waitCount = 10;
        while (!isProcessInstanceEnded() && waitCount >= 0) {
            Thread.sleep(WORKFLOW_WAIT_TIME);
            waitCount--;
        }

        // Layout is to reflect the bpmn visual layout
        assertThat(pi).isEnded().hasPassedInOrder(
                "download_StartEvent", "ServiceTask_1mpt2eq",
                "ServiceTask_1nl90ao", "ExclusiveGateway_1rj84ne",
                "ServiceTask_0yavde3", "ExclusiveGateway_1ja7grm",
                "ServiceTask_1wxo7xz", "ExclusiveGateway_08lusga",
                "download_EndEvent");

        List<ExecutionServiceInput> detailedMessages = grpcNettyServer.getDetailedMessages();
        Assertions.assertThat(detailedMessages).hasSize(3);
        try {
            checkPreCheck(detailedMessages.get(0));
        } catch (Exception e) {
            e.printStackTrace();
            fail("PNFSWUPDownload request exception", e);
        }
    }

    private boolean isProcessInstanceEnded() {
        return runtimeService.createProcessInstanceQuery().processDefinitionKey(TEST_PROCESSINSTANCE_KEY)
                .singleResult() == null;
    }

    private void checkPreCheck(ExecutionServiceInput executionServiceInput) {

        logger.info("Checking the preCheck request");
        ActionIdentifiers actionIdentifiers = executionServiceInput.getActionIdentifiers();

        /**
         * the fields of actionIdentifiers should match the one in the
         * response/PNFSoftwareUpgrade_catalogdb.json.
         */
        Assertions.assertThat(actionIdentifiers.getBlueprintName()).isEqualTo("test_pnf_software_upgrade_restconf");
        Assertions.assertThat(actionIdentifiers.getBlueprintVersion()).isEqualTo("1.0.0");
        Assertions.assertThat(actionIdentifiers.getActionName()).isEqualTo("preCheck");
        Assertions.assertThat(actionIdentifiers.getMode()).isEqualTo("async");

        CommonHeader commonHeader = executionServiceInput.getCommonHeader();
        Assertions.assertThat(commonHeader.getOriginatorId()).isEqualTo("SO");
        Assertions.assertThat(commonHeader.getRequestId()).isEqualTo(msoRequestId);

        Struct payload = executionServiceInput.getPayload();
        Struct requeststruct = payload.getFieldsOrThrow("preCheck-request").getStructValue();

        Assertions.assertThat(requeststruct.getFieldsOrThrow("resolution-key").getStringValue()).isEqualTo("PNFDemo");
        Struct propertiesStruct = requeststruct.getFieldsOrThrow("preCheck-properties").getStructValue();

        Assertions.assertThat(propertiesStruct.getFieldsOrThrow("pnf-name").getStringValue()).isEqualTo("PNFDemo");
        Assertions.assertThat(propertiesStruct.getFieldsOrThrow("service-model-uuid").getStringValue())
                .isEqualTo("32daaac6-5017-4e1e-96c8-6a27dfbe1421");
        Assertions.assertThat(propertiesStruct.getFieldsOrThrow("pnf-customization-uuid").getStringValue())
                .isEqualTo("38dc9a92-214c-11e7-93ae-92361f002680");
        Assertions.assertThat(propertiesStruct.getFieldsOrThrow("target-software-version").getStringValue()).isEqualTo("demo-sw-ver2.0.0");
    }

    private void mockAai() {

        String aaiPnfEntry = "{  \n" + "   \"pnf-name\":\"PNFDemo\",\n" + "   \"pnf-id\":\"testtest\",\n"
                + "   \"in-maint\":true,\n" + "   \"resource-version\":\"1541720264047\",\n" + "   \"swVersion\":\"demo-1.1\",\n"
                + "   \"ipaddress-v4-oam\":\"1.1.1.1\",\n" + "   \"ipaddress-v6-oam\":\"::/128\"\n" + "}";

        /**
         * PUT the PNF correlation ID to AAI.
         */
        wireMockServer.stubFor(put(urlEqualTo("/aai/" + VERSION + "/network/pnfs/pnf/PNFDemo")));

        /**
         * Get the PNF entry from AAI.
         */
        wireMockServer.stubFor(
                get(urlEqualTo("/aai/" + VERSION + "/network/pnfs/pnf/PNFDemo")).willReturn(okJson(aaiPnfEntry)));
    }

    /**
     * Mock the catalobdb rest interface.
     */
    private void mockCatalogDb() {

        String catalogdbClientResponse =
                FileUtil.readResourceFile("response/PNFSoftwareUpgradeTest_catalogdb.json");


        /**
         * Return valid json for the model UUID in the request file.
         */
        wireMockServer
                .stubFor(get(urlEqualTo("/v2/serviceResources?serviceModelUuid=32daaac6-5017-4e1e-96c8-6a27dfbe1421"))
                        .willReturn(okJson(responseObject)));

        /**
         * Return valid json for the service model InvariantUUID as specified in the request file.
         */
        wireMockServer.stubFor(
                get(urlEqualTo("/v2/serviceResources?serviceModelInvariantUuid=339b7a2f-9524-4dbf-9eee-f2e05521df3f"))
                        .willReturn(okJson(responseObject)));

        /**
         * Return valid spring data rest json for the service model UUID as specified in the request file.
         */
        wireMockServer.stubFor(get(urlEqualTo(
                "/pnfResourceCustomization/search/findPnfResourceCustomizationByModelUuid?SERVICE_MODEL_UUID=32daaac6-5017-4e1e-96c8-6a27dfbe1421"))
                .willReturn(okJson(catalogdbClientResponse)));
    }

}
