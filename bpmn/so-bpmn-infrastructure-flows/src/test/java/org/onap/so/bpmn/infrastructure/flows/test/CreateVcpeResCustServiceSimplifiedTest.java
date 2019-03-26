/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.so.bpmn.infrastructure.flows.test;


import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareAssertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.BaseBPMNTest;
import org.onap.so.GrpcNettyServer;
import org.onap.so.bpmn.mock.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Basic Integration test for createVcpeResCustService_Simplified.bpmn workflow.
 */
public class CreateVcpeResCustServiceSimplifiedTest extends BaseBPMNTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String TEST_PROCESSINSTANCE_KEY = "CreateVcpeResCustService_simplified";

    private String testBusinessKey;
    private String requestObject;
    private String responseObject;

    @Autowired
    private GrpcNettyServer grpcNettyServer;

    @Before
    public void setUp() throws IOException {

        requestObject = FileUtil.readResourceFile("request/" + getClass().getSimpleName() + ".json");
        responseObject = FileUtil.readResourceFile("response/" + getClass().getSimpleName() + ".json");

        variables.put("bpmnRequest", requestObject);

        /**
         * This variable indicates that the flow was invoked asynchronously.
         * It's injected by {@link WorkflowProcessor}.
         */
        variables.put("isAsyncProcess", "true");

        /**
         * Temporary solution to add pnfCorrelationId to context.
         * this value is getting from the request to SO api handler and then convert to CamudaInput
         */
        variables.put("pnfCorrelationId", "PNFDemo");

        /**
         * Create mso-request-id.
         */
        String msoRequestId = UUID.randomUUID().toString();

        variables.put("mso-request-id", msoRequestId);

        /**
         * Create Business key for the process instance
         */
        testBusinessKey = UUID.randomUUID().toString();

        logger.info("Test the process instance: {} with business key: {}", TEST_PROCESSINSTANCE_KEY, testBusinessKey);

    }

    @Test
    public void workflow_validInput_expectedOuput() {

        mockCatalogDb();
        mockAai();
        mockDmaapForPnf();

        ProcessInstance pi = runtimeService
            .startProcessInstanceByKey(TEST_PROCESSINSTANCE_KEY, testBusinessKey, variables);
        assertThat(pi).isNotNull();

        Execution execution = runtimeService.createExecutionQuery().processDefinitionKey("CreateAndActivatePnfResource")
            .activityId("WaitForDmaapPnfReadyNotification").singleResult();

        if (!execution.isSuspended() && !execution.isEnded()) {
            try {

                runtimeService.signal(execution.getId());
            } catch (Exception e) {
                logger.info(e.getMessage(), e);
            }
        }

        assertThat(pi).isStarted().hasPassedInOrder(
            "createVCPE_startEvent",
            "preProcessRequest_ScriptTask",
            "sendSyncAckResponse_ScriptTask",
            "ScriptTask_0cdtchu",
            "DecomposeService",
            "ScriptTask_0lpv2da",
            "ScriptTask_1y241p8",
            "CallActivity_1vc4jeh",
            "ScriptTask_1y5lvl7",
            "GeneratePnfUuid",
            "Task_14l19kv",
            "Pnf_Con",
            "setPONR_ScriptTask",
            "postProcessAndCompletionRequest_ScriptTask",
            "callCompleteMsoProcess_CallActivity",
            "ScriptTask_2",
            "CreateVCPE_EndEvent"
        );

        assertThat(pi).isEnded();

        List<String> messagesDelivered = grpcNettyServer.getMessagesDelivered();
        assertThat(messagesDelivered).containsSequence("config-assign", "config-deploy");
    }

    /**
     * Mock the Dmaap Rest interface for Pnf topic.
     */
    private void mockDmaapForPnf() {

        String pnfResponse = "[{\"correlationId\": \"PNFDemo\",\"key1\":\"value1\"}]";

        /**
         * Get the events from PNF topic
         */
        stubFor(get(urlPathMatching("/events/pnfReady/consumerGroup.*")).willReturn(okJson(pnfResponse)));
    }

    private void mockAai() {

        String aaiResponse = "{\n"
            + "  \"results\": [\n"
            + "    {\n"
            + "      \"resource-type\": \"service-instance\",\n"
            + "      \"resource-link\": \"https://localhost:8443/aai/v15/business/customers/customer/ADemoCustomerInCiti/service-subscriptions/service-subscription/vCPE/service-instances/service-instance/key3\"\n"
            + "    }\n"
            + "  ]\n"
            + "}";

        /**
         * Get the AAI entry for globalCustomerId as specified in the request file.
         */
        stubFor(get(urlPathMatching("/aai/v15/business/customers/customer/ADemoCustomerInCiti.*")).willReturn(ok()));

        /**
         * PUT the service to AAI with globalCustomerId, service type as specified in the request file.
         * Service instance id is generated during runtime, REGEX is used to represent the information.
         */
        stubFor(put(urlPathMatching(
            "/aai/v15/business/customers/customer/ADemoCustomerInCiti/service-subscriptions/service-subscription/vCPE/service-instances/service-instance/.*")));

        stubFor(get(urlPathMatching(
            "/aai/v15/business/customers/customer/ADemoCustomerInCiti/service-subscriptions/service-subscription/vCPE/service-instances/service-instance/.*"))
            .willReturn(okJson(aaiResponse)));

        /**
         * Get the service from AAI
         */
        stubFor(get(urlPathMatching("/aai/v15/nodes/service-instances/service-instance/.*"))
            .willReturn(okJson(aaiResponse)));

        /**
         * Put the project as specified in the request file to AAI.
         */
        stubFor(put(urlEqualTo("/aai/v15/business/projects/project/Project-Demonstration")));

        /**
         * GET the project as specified in the request file to AAI.
         */
        stubFor(get(urlPathMatching("/aai/v15/business/projects/project/Project-Demonstration")).willReturn(ok()));

        /**
         * PUT the PNF correlation ID to AAI.
         */
        stubFor(put(urlEqualTo("/aai/v15/network/pnfs/pnf/PNFDemo")));

        /**
         * Put the PNF relationship
         */
        stubFor(
            put(urlEqualTo("/aai/v15/business/projects/project/Project-Demonstration/relationship-list/relationship")));
    }

    /**
     * Mock the catalobdb rest interface.
     */
    private void mockCatalogDb() {

        String catalogdbClientResponse = FileUtil
            .readResourceFile("response/" + getClass().getSimpleName() + "_catalogdb.json");
        /**
         * Return valid json for the model UUID in the request file.
         */
        stubFor(get(urlEqualTo("/v2/serviceResources?serviceModelUuid=f2daaac6-5017-4e1e-96c8-6a27dfbe1421"))
            .willReturn(okJson(responseObject)));

        /**
         * Return valid json for the service model InvariantUUID as specified in the request file.
         */
        stubFor(get(urlEqualTo("/v2/serviceResources?serviceModelInvariantUuid=539b7a2f-9524-4dbf-9eee-f2e05521df3f"))
            .willReturn(okJson(responseObject)));

        /**
         * Return valid spring data rest json for the service model UUID as specified in the request file.
         */
        stubFor(get(urlEqualTo(
            "/pnfResourceCustomization/search/findPnfResourceCustomizationByModelUuid?SERVICE_MODEL_UUID=f2daaac6-5017-4e1e-96c8-6a27dfbe1421"))
            .willReturn(okJson(catalogdbClientResponse)));
    }
}
