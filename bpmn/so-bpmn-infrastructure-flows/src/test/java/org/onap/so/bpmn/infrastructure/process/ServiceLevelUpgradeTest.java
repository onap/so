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

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.fail;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import com.google.protobuf.Struct;

/**
 * Basic Integration test for ServiceLevelUpgrade.bpmn workflow.
 */
public class ServiceLevelUpgradeTest extends BaseBPMNTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final long WORKFLOW_WAIT_TIME = 1000L;

    private static final String TEST_PROCESSINSTANCE_KEY = "ServiceLevelUpgrade";
    private static final AAIVersion VERSION = AAIVersion.LATEST;
    private static final Map<String, Object> executionVariables = new HashMap();
    private static final String REQUEST_ID = "50ae41ad-049c-4fe2-9950-539f111120f5";
    private static final String SERVICE_INSTANCE_ID = "5df8b6de-2083-11e7-93ae-92361f002676";
    private final String[] actionNames = new String[10];
    private final String[] pnfNames = new String[10];
    private final String CLASSNAME = getClass().getSimpleName();
    private String requestObject;
    private String responseObject;

    @Autowired
    private GrpcNettyServer grpcNettyServer;

    @Before
    public void setUp() {
        actionNames[0] = "healthCheck";
        actionNames[1] = "healthCheck";
        actionNames[2] = "preCheck";
        actionNames[3] = "downloadNESw";
        actionNames[4] = "activateNESw";
        actionNames[5] = "postCheck";
        actionNames[6] = "preCheck";
        actionNames[7] = "downloadNESw";
        actionNames[8] = "activateNESw";
        actionNames[9] = "postCheck";

        pnfNames[0] = "PNFDemo";
        pnfNames[1] = "PNFDemo1";
        pnfNames[2] = "PNFDemo";
        pnfNames[3] = "PNFDemo";
        pnfNames[4] = "PNFDemo";
        pnfNames[5] = "PNFDemo";
        pnfNames[6] = "PNFDemo1";
        pnfNames[7] = "PNFDemo1";
        pnfNames[8] = "PNFDemo1";
        pnfNames[9] = "PNFDemo1";

        executionVariables.clear();

        requestObject = FileUtil.readResourceFile("request/" + CLASSNAME + ".json");
        responseObject = FileUtil.readResourceFile("response/" + CLASSNAME + ".json");

        executionVariables.put("bpmnRequest", requestObject);
        executionVariables.put("requestId", REQUEST_ID);
        executionVariables.put("serviceInstanceId", SERVICE_INSTANCE_ID);


        /**
         * This variable indicates that the flow was invoked asynchronously. It's injected by {@link WorkflowProcessor}.
         */
        executionVariables.put("isAsyncProcess", "true");
        executionVariables.put(ExecutionVariableNames.PRC_CUSTOMIZATION_UUID, "38dc9a92-214c-11e7-93ae-92361f002680");

        /**
         * Temporary solution to add pnfCorrelationId to context. this value is getting from the request to SO api
         * handler and then convert to CamudaInput
         */
        executionVariables.put(ExecutionVariableNames.PNF_CORRELATION_ID, "PNFDemo");
    }


    @Test
    public void workflow_validInput_expectedOutput() throws InterruptedException {

        mockCatalogDb();
        mockRequestDb();
        mockAai();
        grpcNettyServer.resetList();

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
        assertThat(pi).isEnded().hasPassedInOrder("Event_02mc8tr", "Activity_18vue7u", "Activity_09bqns0",
                "Activity_02vp5np", "Activity_0n17xou", "Gateway_1nr51kr", "Activity_0snmatn", "Activity_0e6w886",
                "Activity_1q4o9fx", "Gateway_02fectw", "Activity_1hp67qz", "Gateway_18ch73t", "Activity_0ft7fa2",
                "Gateway_1vq11i7", "Activity_0o2rrag", "Activity_1n4rk7m", "Activity_1lz38px", "Event_12983th");

        List<ExecutionServiceInput> detailedMessages = grpcNettyServer.getDetailedMessages();
        assertEquals(10, detailedMessages.size());
        int count = 0;
        String action = "";
        try {
            for (ExecutionServiceInput eSI : detailedMessages) {
                action = actionNames[count];
                if (action.equals(eSI.getActionIdentifiers().getActionName())
                        && eSI.getCommonHeader().getRequestId().equals(msoRequestId)) {
                    checkWithActionName(eSI, action, pnfNames[count]);
                    count++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("GenericPnfSoftwareUpgrade request exception", e);
        }
        assertTrue(count == actionNames.length);
    }

    private boolean isProcessInstanceEnded() {
        return runtimeService.createProcessInstanceQuery().processDefinitionKey(TEST_PROCESSINSTANCE_KEY)
                .singleResult() == null;
    }

    private void checkWithActionName(final ExecutionServiceInput executionServiceInput, final String action,
            final String pnfName) {

        logger.info("Checking the " + action + " request");
        ActionIdentifiers actionIdentifiers = executionServiceInput.getActionIdentifiers();

        /**
         * the fields of actionIdentifiers should match the one in the response/PnfHealthCheck_catalogdb.json.
         */
        assertEquals("test_pnf_software_upgrade_restconf", actionIdentifiers.getBlueprintName());
        assertEquals("1.0.0", actionIdentifiers.getBlueprintVersion());
        assertEquals(action, actionIdentifiers.getActionName());
        assertEquals("async", actionIdentifiers.getMode());

        CommonHeader commonHeader = executionServiceInput.getCommonHeader();
        assertEquals("SO", commonHeader.getOriginatorId());

        Struct payload = executionServiceInput.getPayload();
        Struct requeststruct = payload.getFieldsOrThrow(action + "-request").getStructValue();

        assertEquals(pnfName, requeststruct.getFieldsOrThrow("resolution-key").getStringValue());
        Struct propertiesStruct = requeststruct.getFieldsOrThrow(action + "-properties").getStructValue();

        assertEquals(pnfName, propertiesStruct.getFieldsOrThrow("pnf-name").getStringValue());
        assertEquals("d88da85c-d9e8-4f73-b837-3a72a431622b",
                propertiesStruct.getFieldsOrThrow("service-model-uuid").getStringValue());
        assertEquals("38dc9a92-214c-11e7-93ae-92361f002680",
                propertiesStruct.getFieldsOrThrow("pnf-customization-uuid").getStringValue());
    }

    private void mockAai() {

        final String sIUrl =
                "/business/customers/customer/ETE_Customer_807c7a02-249c-4db8-9fa9-bee973fe08ce/service-subscriptions/service-subscription/pNF/service-instances/service-instance/5df8b6de-2083-11e7-93ae-92361f002676";
        final String aaiPnfDemoEntry = FileUtil.readResourceFile("response/PnfDemo_aai.json");
        final String aaiPnfDemo1Entry = FileUtil.readResourceFile("response/PnfDemo1_aai.json");
        final String aaiServiceInstanceEntry = FileUtil.readResourceFile("response/Service_instance_aai.json");

        /**
         * PUT the PNF correlation ID PnfDemo to AAI.
         */
        wireMockServer.stubFor(put(urlEqualTo("/aai/" + VERSION + "/network/pnfs/pnf/PNFDemo")));

        /**
         * PUT the PNF correlation ID PnfDemo1 to AAI.
         */
        wireMockServer.stubFor(put(urlEqualTo("/aai/" + VERSION + "/network/pnfs/pnf/PNFDemo1")));

        /**
         * Get the PNF entry PnfDemo from AAI.
         */
        wireMockServer.stubFor(
                get(urlEqualTo("/aai/" + VERSION + "/network/pnfs/pnf/PNFDemo")).willReturn(okJson(aaiPnfDemoEntry)));

        /**
         * Get the PNF entry PnfDemo1 from AAI.
         */
        wireMockServer.stubFor(
                get(urlEqualTo("/aai/" + VERSION + "/network/pnfs/pnf/PNFDemo1")).willReturn(okJson(aaiPnfDemo1Entry)));

        /**
         * Post the pnf PnfDemo to AAI
         */
        wireMockServer.stubFor(post(urlEqualTo("/aai/" + VERSION + "/network/pnfs/pnf/PNFDemo")));

        /**
         * Post the pnf PnfDemo1 to AAI
         */
        wireMockServer.stubFor(post(urlEqualTo("/aai/" + VERSION + "/network/pnfs/pnf/PNFDemo1")));

        /**
         * Get the Service Instance to AAI.
         */
        wireMockServer.stubFor(get(urlEqualTo("/aai/" + VERSION + sIUrl)).willReturn(okJson(aaiServiceInstanceEntry)));

        /**
         * Post the Service Instance to AAI.
         */
        wireMockServer.stubFor(post(urlEqualTo("/aai/" + VERSION + sIUrl)));
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

        String catalogdbClientResponse = FileUtil.readResourceFile("response/" + CLASSNAME + "_catalogdb.json");


        /**
         * Return valid json for the model UUID in the request file.
         */
        wireMockServer
                .stubFor(get(urlEqualTo("/v2/serviceResources?serviceModelUuid=d88da85c-d9e8-4f73-b837-3a72a431622b"))
                        .willReturn(okJson(responseObject)));

        /**
         * Return valid json for the service model InvariantUUID as specified in the request file.
         */
        wireMockServer.stubFor(
                get(urlEqualTo("/v2/serviceResources?serviceModelInvariantUuid=fe41489e-1563-46a3-b90a-1db629e4375b"))
                        .willReturn(okJson(responseObject)));

        /**
         * Return valid spring data rest json for the service model UUID as specified in the request file.
         */
        wireMockServer.stubFor(get(urlEqualTo(
                "/pnfResourceCustomization/search/findPnfResourceCustomizationByModelUuid?SERVICE_MODEL_UUID=d88da85c-d9e8-4f73-b837-3a72a431622b"))
                        .willReturn(okJson(catalogdbClientResponse)));
    }

}
