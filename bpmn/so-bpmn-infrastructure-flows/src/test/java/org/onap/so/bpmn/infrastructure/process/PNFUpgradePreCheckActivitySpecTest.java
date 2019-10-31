package org.onap.so.bpmn.infrastructure.process;


/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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


import com.google.protobuf.Struct;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers;
import org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.so.BaseBPMNTest;
import org.onap.so.client.aai.AAIVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.onap.so.GrpcNettyServer;
import org.onap.so.bpmn.mock.FileUtil;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.fail;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat;

/**
 * Basic Integration test for PNFUpgradePreCheckActivitySpec.bpmn workflow.
 */
public class PNFUpgradePreCheckActivitySpecTest extends BaseBPMNTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final long WORKFLOW_WAIT_TIME = 1000L;
    private static final int DMAAP_DELAY_TIME_MS = 2000;

    private static final String TEST_PROCESSINSTANCE_KEY = "PNFUpgradePreCheckActivitySpec";
    private static final AAIVersion VERSION = AAIVersion.LATEST;
    private String testBusinessKey;
    private String requestObject;
    private String responseObject;
    private String msoRequestId;

    @Autowired
    private GrpcNettyServer grpcNettyServer;

    /**
     * Service model info json.
     */
    private static String TEST_SERVICE_MODEL_INFO = "{\n" + "      \"modelType\":\"service\",\n"
            + "      \"modelInvariantUuid\":\"439b7a2f-9524-4dbf-9eee-f2e05521df3f\",\n"
            + "      \"modelInvariantId\":\"439b7a2f-9524-4dbf-9eee-f2e05521df3f\",\n"
            + "      \"modelUuid\":\"42daaac6-5017-4e1e-96c8-6a27dfbe1421\",\n"
            + "      \"modelName\":\"PNF_demo_resource\",\n" + "      \"modelVersion\":\"1.0\"\n" + "}";

    @Before
    public void setUp() throws IOException {

        requestObject = FileUtil.readResourceFile("request/" + getClass().getSimpleName() + ".json");
        responseObject = FileUtil.readResourceFile("response/" + getClass().getSimpleName() + ".json");

        variables.put("bpmnRequest", requestObject);

        /**
         * This variable indicates that the flow was invoked asynchronously. It's injected by {@link WorkflowProcessor}.
         */
        variables.put("isAsyncProcess", "true");

        /**
         * Temporary solution to add pnfCorrelationId to context. this value is getting from the request to SO api
         * handler and then convert to CamudaInput
         */
        variables.put("pnfCorrelationId", "PNFDemo");

        /**
         * Create mso-request-id.
         */
        msoRequestId = UUID.randomUUID().toString();

        variables.put("msoRequestId", msoRequestId);

        /**
         * Create Business key for the process instance
         */
        testBusinessKey = UUID.randomUUID().toString();

        logger.info("Test the process instance: {} with business key: {}", TEST_PROCESSINSTANCE_KEY, testBusinessKey);

        variables.put("serviceModelInfo", TEST_SERVICE_MODEL_INFO);

        UUID uuid = UUID.randomUUID();
        logger.debug("Generated UUID for pnf: {}, version: {}, variant: {}", uuid, uuid.version(), uuid.variant());
        variables.put("pnfUuid", uuid.toString());
    }


    @Test
    public void workflow_validInput_expectedOuput() throws InterruptedException {

        mockCatalogDb();
        mockRequestDb();
        mockAai();
        mockDmaapForPnf();

        ProcessInstance pi =
                runtimeService.startProcessInstanceByKey(TEST_PROCESSINSTANCE_KEY, testBusinessKey, variables);

        int waitCount = 10;
        while (!isProcessInstanceEnded() && waitCount >= 0) {
            Thread.sleep(WORKFLOW_WAIT_TIME);
            waitCount--;
        }

        assertThat(pi).isEnded().hasPassedInOrder("preCheck_StartEvent", "Task_1wq99hf", "Task_09tvt8z", "Task_0hu3542",
                "ExclusiveGateway_0pac7y9", "preCheck_EndEvent");

        List<ExecutionServiceInput> detailedMessages = grpcNettyServer.getDetailedMessages();
        Assertions.assertThat(detailedMessages).hasSize(1);
        try {
            checkPreCheck(detailedMessages.get(0));
        } catch (Exception e) {
            e.printStackTrace();
            fail("PreCheck request exception", e);
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
         * response/PNFUpgradePreCheckActivitySpecTest_catalogdb.json.
         */
        Assertions.assertThat(actionIdentifiers.getBlueprintName()).isEqualTo("test_preCheck_restconf");
        Assertions.assertThat(actionIdentifiers.getBlueprintVersion()).isEqualTo("1.0.0");
        Assertions.assertThat(actionIdentifiers.getActionName()).isEqualTo("preCheck");
        Assertions.assertThat(actionIdentifiers.getMode()).isEqualTo("sync");

        CommonHeader commonHeader = executionServiceInput.getCommonHeader();
        Assertions.assertThat(commonHeader.getOriginatorId()).isEqualTo("SO");
        Assertions.assertThat(commonHeader.getRequestId()).isEqualTo(msoRequestId);

        Struct payload = executionServiceInput.getPayload();
        Struct requeststruct = payload.getFieldsOrThrow("preCheck-request").getStructValue();

        Assertions.assertThat(requeststruct.getFieldsOrThrow("resolution-key").getStringValue()).isEqualTo("PNFDemo");
        Struct propertiesStruct = requeststruct.getFieldsOrThrow("preCheck-properties").getStructValue();

        Assertions.assertThat(propertiesStruct.getFieldsOrThrow("pnf-name").getStringValue()).isEqualTo("PNFDemo");
        Assertions.assertThat(propertiesStruct.getFieldsOrThrow("service-model-uuid").getStringValue())
                .isEqualTo("42daaac6-5017-4e1e-96c8-6a27dfbe1421");
        Assertions.assertThat(propertiesStruct.getFieldsOrThrow("pnf-customization-uuid").getStringValue())
                .isEqualTo("48dc9a92-214c-11e7-93ae-92361f002680");
        Assertions.assertThat(propertiesStruct.getFieldsOrThrow("software-version").getStringValue())
                .isEqualTo("1.0.0");

        /**
         * IP addresses match the OAM ip addresses from AAI.
         */
        Assertions.assertThat(propertiesStruct.getFieldsOrThrow("pnf-ipv4-address").getStringValue())
                .isEqualTo("1.1.1.1");
        Assertions.assertThat(propertiesStruct.getFieldsOrThrow("pnf-ipv6-address").getStringValue())
                .isEqualTo("::/128");
    }

    /**
     * Mock the Dmaap Rest interface for Pnf topic.
     */
    private void mockDmaapForPnf() {

        String pnfResponse = "[{\"correlationId\": \"PNFDemo\",\"key1\":\"value1\"}]";

        /**
         * Get the events from PNF topic
         */
        wireMockServer.stubFor(get(urlPathMatching("/events/pnfReady/consumerGroup.*"))
                .willReturn(okJson(pnfResponse).withFixedDelay(DMAAP_DELAY_TIME_MS)));
    }

    private void mockAai() {

        String aaiResponse = "{\n" + "  \"results\": [\n" + "    {\n"
                + "      \"resource-type\": \"service-instance\",\n"
                + "      \"resource-link\": \"https://localhost:8443/aai/" + VERSION
                + "/business/customers/customer/ADemoCustomerInCiti/service-subscriptions/service-subscription/vCPE/service-instances/service-instance/key3\"\n"
                + "    }\n" + "  ]\n" + "}";

        String aaiPnfEntry = "{  \n" + "   \"pnf-name\":\"PNFDemo\",\n" + "   \"pnf-id\":\"testtest\",\n"
                + "   \"in-maint\":true,\n" + "   \"resource-version\":\"1541720264047\",\n"
                + "   \"ipaddress-v4-oam\":\"1.1.1.1\",\n" + "   \"ipaddress-v6-oam\":\"::/128\"\n" + "}";

        /**
         * Get the AAI entry for globalCustomerId as specified in the request file.
         */
        wireMockServer
                .stubFor(get(urlPathMatching("/aai/" + VERSION + "/business/customers/customer/ADemoCustomerInCiti.*"))
                        .willReturn(ok()));

        /**
         * PUT the service to AAI with globalCustomerId, service type as specified in the request file. Service instance
         * id is generated during runtime, REGEX is used to represent the information.
         */
        wireMockServer.stubFor(put(urlPathMatching("/aai/" + VERSION
                + "/business/customers/customer/ADemoCustomerInCiti/service-subscriptions/service-subscription/vCPE/service-instances/service-instance/.*")));

        wireMockServer.stubFor(get(urlPathMatching("/aai/" + VERSION
                + "/business/customers/customer/ADemoCustomerInCiti/service-subscriptions/service-subscription/vCPE/service-instances/service-instance/.*"))
                        .willReturn(okJson(aaiResponse)));

        /**
         * Get the service from AAI
         */
        wireMockServer.stubFor(get(urlPathMatching("/aai/" + VERSION + "/nodes/service-instances/service-instance/.*"))
                .willReturn(okJson(aaiResponse)));

        /**
         * Put the project as specified in the request file to AAI.
         */
        wireMockServer.stubFor(put(urlEqualTo("/aai/" + VERSION + "/business/projects/project/Project-Demonstration")));

        /**
         * GET the project as specified in the request file to AAI.
         */
        wireMockServer
                .stubFor(get(urlPathMatching("/aai/" + VERSION + "/business/projects/project/Project-Demonstration"))
                        .willReturn(ok()));

        /**
         * PUT the PNF correlation ID to AAI.
         */
        wireMockServer.stubFor(put(urlEqualTo("/aai/" + VERSION + "/network/pnfs/pnf/PNFDemo")));

        /**
         * Get the PNF entry from AAI.
         */
        wireMockServer.stubFor(
                get(urlEqualTo("/aai/" + VERSION + "/network/pnfs/pnf/PNFDemo")).willReturn(okJson(aaiPnfEntry)));

        /**
         * Put the PNF relationship
         */
        wireMockServer.stubFor(put(urlEqualTo("/aai/" + VERSION
                + "/business/projects/project/Project-Demonstration/relationship-list/relationship")));
    }

    /**
     * Mock the catalobdb rest interface.
     */
    private void mockCatalogDb() {

        String catalogdbClientResponse =
                FileUtil.readResourceFile("response/" + getClass().getSimpleName() + "_catalogdb.json");

        /**
         * Return valid json for the model UUID in the request file.
         */
        wireMockServer
                .stubFor(get(urlEqualTo("/v2/serviceResources?serviceModelUuid=42daaac6-5017-4e1e-96c8-6a27dfbe1421"))
                        .willReturn(okJson(responseObject)));

        /**
         * Return valid json for the service model InvariantUUID as specified in the request file.
         */
        wireMockServer.stubFor(
                get(urlEqualTo("/v2/serviceResources?serviceModelInvariantUuid=439b7a2f-9524-4dbf-9eee-f2e05521df3f"))
                        .willReturn(okJson(responseObject)));

        /**
         * Return valid spring data rest json for the service model UUID as specified in the request file.
         */
        wireMockServer.stubFor(get(urlEqualTo(
                "/pnfResourceCustomization/search/findPnfResourceCustomizationByModelUuid?SERVICE_MODEL_UUID=42daaac6-5017-4e1e-96c8-6a27dfbe1421"))
                        .willReturn(okJson(catalogdbClientResponse)));
    }

    private void mockRequestDb() {
        wireMockServer.stubFor(post(urlEqualTo("/dbadapters/RequestsDbAdapter")).willReturn(ok()));
    }

}
