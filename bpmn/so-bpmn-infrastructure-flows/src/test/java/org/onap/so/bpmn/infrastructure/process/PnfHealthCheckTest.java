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

import com.google.protobuf.Struct;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Before;
import org.junit.Test;
import org.onap.aaiclient.client.aai.AAIVersion;
import org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers;
import org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.so.BaseBPMNTest;
import org.onap.so.GrpcNettyServer;
import org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames;
import org.onap.so.bpmn.mock.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.fail;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Basic Integration test for GenericPnfHealthCheck.bpmn workflow.
 */
public class PnfHealthCheckTest extends BaseBPMNTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final long WORKFLOW_WAIT_TIME = 1000L;

    private static final String TEST_PROCESSINSTANCE_KEY = "GenericPnfHealthCheck";
    private static final AAIVersion VERSION = AAIVersion.LATEST;
    private static final Map<String, Object> executionVariables = new HashMap<>();
    private static final String REQUEST_ID = "50ae41ad-049c-4fe2-9950-539f111120f5";
    private static final String ACTION_NAME = "healthCheck";
    private final String CLASS_NAME = getClass().getSimpleName();
    private String requestObject;
    private String responseObject;

    @Autowired
    private GrpcNettyServer grpcNettyServer;

    @Before
    public void setUp() {
        executionVariables.clear();
        grpcNettyServer.getDetailedMessages().clear();

        requestObject = FileUtil.readResourceFile("request/" + CLASS_NAME + ".json");
        responseObject = FileUtil.readResourceFile("response/" + CLASS_NAME + ".json");

        executionVariables.put("bpmnRequest", requestObject);
        executionVariables.put("requestId", REQUEST_ID);

        /**
         * This variable indicates that the flow was invoked asynchronously. It's injected by {@link WorkflowProcessor}.
         */
        executionVariables.put("isAsyncProcess", "true");
        executionVariables.put(ExecutionVariableNames.PRC_CUSTOMIZATION_UUID, "38dc9a92-214c-11e7-93ae-92361f002680");

        /**
         * Temporary solution to add pnfCorrelationId to context. this value is getting from the request to SO api
         * handler and then convert to CamundaInput
         */
        executionVariables.put(ExecutionVariableNames.PNF_CORRELATION_ID, "PNFDemo");
    }


    @Test
    public void workflow_validInput_expectedOutput() throws InterruptedException {

        mockCatalogDb();
        mockRequestDb();
        mockAai();

        final String msoRequestId = UUID.randomUUID().toString();
        executionVariables.put(ExecutionVariableNames.MSO_REQUEST_ID, msoRequestId);

        final String testBusinessKey = UUID.randomUUID().toString();
        logger.info("Test the process instance: {} with business key: {}", TEST_PROCESSINSTANCE_KEY, testBusinessKey);

        ProcessInstance pi =
                runtimeService.startProcessInstanceByKey(TEST_PROCESSINSTANCE_KEY, testBusinessKey, executionVariables);

        int waitCount = 10;
        while (!isProcessInstanceEnded() && waitCount >= 0) {
            Thread.sleep(WORKFLOW_WAIT_TIME);
            waitCount--;
        }

        // Layout is to reflect the bpmn visual layout
        assertThat(pi).isEnded().hasPassedInOrder("pnfHealthCheck_startEvent", "ServiceTask_042uz7m",
                "ScriptTask_10klpg9", "ServiceTask_0slpaht", "ExclusiveGateway_0x6h0yi", "ScriptTask_1igtc83",
                "CallActivity_0o1mi8u", "pnfHealthCheck_endEvent");

        List<ExecutionServiceInput> detailedMessages = grpcNettyServer.getDetailedMessages();
        logger.debug("Size of detailedMessage is {}", detailedMessages.size());
        assertTrue(detailedMessages.size() == 1);
        int count = 0;
        try {
            for (ExecutionServiceInput eSI : detailedMessages) {
                if (ACTION_NAME.equals(eSI.getActionIdentifiers().getActionName())
                        && eSI.getCommonHeader().getRequestId().equals(msoRequestId)) {
                    checkWithActionName(eSI, ACTION_NAME);
                    count++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("PNFHealthCheck request exception", e);
        }
        assertTrue(count == 1);
    }

    private boolean isProcessInstanceEnded() {
        return runtimeService.createProcessInstanceQuery().processDefinitionKey(TEST_PROCESSINSTANCE_KEY)
                .singleResult() == null;
    }

    private void checkWithActionName(ExecutionServiceInput executionServiceInput, String action) {

        logger.info("Checking the {} request", action);
        ActionIdentifiers actionIdentifiers = executionServiceInput.getActionIdentifiers();

        /**
         * the fields of actionIdentifiers should match the one in the response/PnfHealthCheck_catalogdb.json.
         */
        assertEquals("test_pnf_health_check_restconf", actionIdentifiers.getBlueprintName());
        assertEquals("1.0.0", actionIdentifiers.getBlueprintVersion());
        assertEquals(action, actionIdentifiers.getActionName());
        assertEquals("async", actionIdentifiers.getMode());

        CommonHeader commonHeader = executionServiceInput.getCommonHeader();
        assertEquals("SO", commonHeader.getOriginatorId());

        Struct payload = executionServiceInput.getPayload();
        Struct requeststruct = payload.getFieldsOrThrow(action + "-request").getStructValue();

        assertEquals("PNFDemo", requeststruct.getFieldsOrThrow("resolution-key").getStringValue());
        Struct propertiesStruct = requeststruct.getFieldsOrThrow(action + "-properties").getStructValue();

        assertEquals("PNFDemo", propertiesStruct.getFieldsOrThrow("pnf-name").getStringValue());
        assertEquals("32daaac6-5017-4e1e-96c8-6a27dfbe1421",
                propertiesStruct.getFieldsOrThrow("service-model-uuid").getStringValue());
        assertEquals("38dc9a92-214c-11e7-93ae-92361f002680",
                propertiesStruct.getFieldsOrThrow("pnf-customization-uuid").getStringValue());
    }

    private void mockAai() {

        String aaiPnfEntry =
                "{  \n" + "   \"pnf-name\":\"PNFDemo\",\n" + "   \"pnf-id\":\"testtest\",\n" + "   \"in-maint\":true,\n"
                        + "   \"resource-version\":\"1541720264047\",\n" + "   \"swVersion\":\"demo-1.1\",\n"
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

        /**
         * Post the pnf to AAI
         */
        wireMockServer.stubFor(post(urlEqualTo("/aai/" + VERSION + "/network/pnfs/pnf/PNFDemo")));
    }

    private void mockRequestDb() {
        /**
         * Update Request DB
         */
        wireMockServer.stubFor(put(urlEqualTo("/infraActiveRequests/" + REQUEST_ID)));

    }

    /**
     * Mock the catalobdb rest interface.
     */
    private void mockCatalogDb() {

        String catalogdbClientResponse = FileUtil.readResourceFile("response/" + CLASS_NAME + "_catalogdb.json");


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
