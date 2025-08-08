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

package org.onap.so.bpmn.infrastructure.process;

import com.google.protobuf.Struct;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers;
import org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.so.BaseBPMNTest;
import org.onap.so.GrpcNettyServer;
import org.onap.so.bpmn.mock.FileUtil;
import org.onap.aaiclient.client.aai.AAIVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;


/**
 * Basic Integration test for createVcpeResCustService_Simplified.bpmn workflow.
 */
@Ignore
public class CreateVcpeResCustServiceSimplifiedTest extends BaseBPMNTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final long WORKFLOW_WAIT_TIME = 1000L;
    private static final int DMAAP_DELAY_TIME_MS = 2000;

    private static final String TEST_PROCESSINSTANCE_KEY = "CreateVcpeResCustService_simplified";
    private static final AAIVersion VERSION = AAIVersion.LATEST;
    private static final Map<String, Object> executionVariables = new HashMap();
    private String testBusinessKey;
    private String requestObject;
    private String responseObject;

    @Autowired
    private GrpcNettyServer grpcNettyServer;

    @Before
    public void setUp() {

        requestObject = FileUtil.readResourceFile("request/" + getClass().getSimpleName() + ".json");
        responseObject = FileUtil.readResourceFile("response/" + getClass().getSimpleName() + ".json");

        executionVariables.clear();

        executionVariables.put("bpmnRequest", requestObject);

        /**
         * This variable indicates that the flow was invoked asynchronously. It's injected by {@link WorkflowProcessor}.
         */
        executionVariables.put("isAsyncProcess", "true");

        /**
         * Temporary solution to add pnfCorrelationId to context. this value is getting from the request to SO api
         * handler and then convert to CamudaInput
         */
        executionVariables.put("pnfCorrelationId", "PNFDemo");

        /**
         * Create mso-request-id.
         */


        /**
         * Create Business key for the process instance
         */
        testBusinessKey = UUID.randomUUID().toString();

        logger.info("Test the process instance: {} with business key: {}", TEST_PROCESSINSTANCE_KEY, testBusinessKey);

    }

    @Test
    public void workflow_validInput_expectedOuput() throws InterruptedException {

        mockCatalogDb();
        mockRequestDb();
        mockAai();
        mockDmaapForPnf();

        grpcNettyServer.resetList();

        final String msoRequestId = UUID.randomUUID().toString();
        executionVariables.put("mso-request-id", msoRequestId);

        ProcessInstance pi =
                runtimeService.startProcessInstanceByKey(TEST_PROCESSINSTANCE_KEY, testBusinessKey, executionVariables);

        int waitCount = 10;
        while (!isProcessInstanceEnded() && waitCount >= 0) {
            Thread.sleep(WORKFLOW_WAIT_TIME);
            waitCount--;
        }

        assertThat(pi).isEnded().hasPassedInOrder("createVCPE_startEvent", "preProcessRequest_ScriptTask",
                "sendSyncAckResponse_ScriptTask", "ScriptTask_0cdtchu", "DecomposeService", "ScriptTask_0lpv2da",
                "ScriptTask_1y241p8", "CallActivity_1vc4jeh", "ScriptTask_1y5lvl7", "GeneratePnfUuid", "Task_14l19kv",
                "Pnf_Con", "setPONR_ScriptTask", "postProcessAndCompletionRequest_ScriptTask",
                "callCompleteMsoProcess_CallActivity", "ScriptTask_2", "CreateVCPE_EndEvent");

        List<ExecutionServiceInput> detailedMessages = grpcNettyServer.getDetailedMessages();
        assertThat(detailedMessages.size() == 2);
        int count = 0;
        try {
            for (ExecutionServiceInput eSI : detailedMessages) {
                if ("config-assign".equals(eSI.getActionIdentifiers().getActionName())) {
                    checkConfigAssign(eSI, msoRequestId);
                    count++;
                }
                if ("config-deploy".equals(eSI.getActionIdentifiers().getActionName())) {
                    checkConfigDeploy(eSI, msoRequestId);
                    count++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("ConfigAssign/deploy request exception", e);
        }
        assertThat(count == 2);
    }

    private boolean isProcessInstanceEnded() {
        return runtimeService.createProcessInstanceQuery().processDefinitionKey(TEST_PROCESSINSTANCE_KEY)
                .singleResult() == null;
    }

    private void checkConfigAssign(ExecutionServiceInput executionServiceInput, String msoRequestId) {

        logger.info("Checking the configAssign request");
        ActionIdentifiers actionIdentifiers = executionServiceInput.getActionIdentifiers();

        /**
         * the fields of actionIdentifiers should match the one in the
         * response/createVcpeResCustServiceSimplifiedTest_catalogdb.json.
         */
        assertThat(actionIdentifiers.getBlueprintName()).isEqualTo("test_configuration_restconf");
        assertThat(actionIdentifiers.getBlueprintVersion()).isEqualTo("1.0.0");
        assertThat(actionIdentifiers.getActionName()).isEqualTo("config-assign");
        assertThat(actionIdentifiers.getMode()).isEqualTo("sync");

        CommonHeader commonHeader = executionServiceInput.getCommonHeader();
        assertThat(commonHeader.getOriginatorId()).isEqualTo("SO");
        assertThat(commonHeader.getRequestId()).isEqualTo(msoRequestId);

        Struct payload = executionServiceInput.getPayload();
        Struct requeststruct = payload.getFieldsOrThrow("config-assign-request").getStructValue();

        assertThat(requeststruct.getFieldsOrThrow("resolution-key").getStringValue()).isEqualTo("PNFDemo");
        Struct propertiesStruct = requeststruct.getFieldsOrThrow("config-assign-properties").getStructValue();

        assertThat(propertiesStruct.getFieldsOrThrow("pnf-name").getStringValue()).isEqualTo("PNFDemo");
        assertThat(propertiesStruct.getFieldsOrThrow("service-model-uuid").getStringValue())
                .isEqualTo("f2daaac6-5017-4e1e-96c8-6a27dfbe1421");
        assertThat(propertiesStruct.getFieldsOrThrow("pnf-customization-uuid").getStringValue())
                .isEqualTo("68dc9a92-214c-11e7-93ae-92361f002680");
    }

    private void checkConfigDeploy(ExecutionServiceInput executionServiceInput, String msoRequestId) {

        logger.info("Checking the configDeploy request");
        ActionIdentifiers actionIdentifiers = executionServiceInput.getActionIdentifiers();

        /**
         * the fields of actionIdentifiers should match the one in the
         * response/createVcpeResCustServiceSimplifiedTest_catalogdb.json.
         */
        assertThat(actionIdentifiers.getBlueprintName()).isEqualTo("test_configuration_restconf");
        assertThat(actionIdentifiers.getBlueprintVersion()).isEqualTo("1.0.0");
        assertThat(actionIdentifiers.getActionName()).isEqualTo("config-deploy");
        assertThat(actionIdentifiers.getMode()).isEqualTo("async");

        CommonHeader commonHeader = executionServiceInput.getCommonHeader();
        assertThat(commonHeader.getOriginatorId()).isEqualTo("SO");
        assertThat(commonHeader.getRequestId()).isEqualTo(msoRequestId);

        Struct payload = executionServiceInput.getPayload();
        Struct requeststruct = payload.getFieldsOrThrow("config-deploy-request").getStructValue();

        assertThat(requeststruct.getFieldsOrThrow("resolution-key").getStringValue()).isEqualTo("PNFDemo");
        Struct propertiesStruct = requeststruct.getFieldsOrThrow("config-deploy-properties").getStructValue();

        assertThat(propertiesStruct.getFieldsOrThrow("pnf-name").getStringValue()).isEqualTo("PNFDemo");
        assertThat(propertiesStruct.getFieldsOrThrow("service-model-uuid").getStringValue())
                .isEqualTo("f2daaac6-5017-4e1e-96c8-6a27dfbe1421");
        assertThat(propertiesStruct.getFieldsOrThrow("pnf-customization-uuid").getStringValue())
                .isEqualTo("68dc9a92-214c-11e7-93ae-92361f002680");

        /**
         * IP addresses match the OAM ip addresses from AAI.
         */
        assertThat(propertiesStruct.getFieldsOrThrow("pnf-ipv4-address").getStringValue()).isEqualTo("1.1.1.1");
        assertThat(propertiesStruct.getFieldsOrThrow("pnf-ipv6-address").getStringValue()).isEqualTo("::/128");
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
                .stubFor(get(urlEqualTo("/v2/serviceResources?serviceModelUuid=f2daaac6-5017-4e1e-96c8-6a27dfbe1421"))
                        .willReturn(okJson(responseObject)));

        /**
         * Return valid json for the service model InvariantUUID as specified in the request file.
         */
        wireMockServer.stubFor(
                get(urlEqualTo("/v2/serviceResources?serviceModelInvariantUuid=539b7a2f-9524-4dbf-9eee-f2e05521df3f"))
                        .willReturn(okJson(responseObject)));

        /**
         * Return valid spring data rest json for the service model UUID as specified in the request file.
         */
        wireMockServer.stubFor(get(urlEqualTo(
                "/pnfResourceCustomization/search/findPnfResourceCustomizationByModelUuid?SERVICE_MODEL_UUID=f2daaac6-5017-4e1e-96c8-6a27dfbe1421"))
                        .willReturn(okJson(catalogdbClientResponse)));
    }

    private void mockRequestDb() {
        wireMockServer.stubFor(post(urlEqualTo("/dbadapters/RequestsDbAdapter")).willReturn(ok()));
    }

}
