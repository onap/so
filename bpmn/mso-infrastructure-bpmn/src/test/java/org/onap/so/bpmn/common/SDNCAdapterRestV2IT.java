/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.common;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.so.BaseIntegrationTest;
import org.onap.so.bpmn.mock.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for SDNCAdapterRestV2.bpmn.
 *
 * This version of SDNCAdapterRest allows for interim notifications to be sent for any non-final response received from
 * SDNC.
 */

public class SDNCAdapterRestV2IT extends BaseIntegrationTest {

    private final CallbackSet callbacks = new CallbackSet();

    Logger logger = LoggerFactory.getLogger(SDNCAdapterRestV2IT.class);


    /**
     * Constructor. Insert callbacks.
     *
     * @throws IOException
     */
    public SDNCAdapterRestV2IT() throws IOException {
        String sdncCallbackFinal = FileUtil.readResourceFile("__files/SDNCAdapterRestCallbackFinal.json");
        String sdncCallbackNonFinal = FileUtil.readResourceFile("__files/SDNCAdapterRestCallbackNonFinal.json");
        callbacks.put("nonfinal", sdncCallbackNonFinal);
        callbacks.put("final", sdncCallbackFinal);
    }

    /**
     * Test the success path through the subflow.
     */
    @Test
    @Ignore
    public void success() {
        logStart();
        mocks();

        String businessKey = UUID.randomUUID().toString();
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("mso-request-id", "a4383a52-b9de-4bc4-bedf-02f3f9466535");
        variables.put("mso-service-instance-id", "fd8bcdbb-b799-43ce-a7ff-ed8f2965a3b5");
        variables.put("isDebugLogEnabled", "true");
        variables.put("SDNCREST_Request", FileUtil.readResourceFile("__files/SDNCAdapterRestV2Request.json"));
        variables.put("SDNCREST_InterimNotification1",
                FileUtil.readResourceFile("__files/SDNCInterimNotification1.json"));

        invokeSubProcess("SDNCAdapterRestV2", businessKey, variables);

        injectSDNCRestCallbacks(callbacks, "nonfinal");

        // First non-final response will have done a notification
        Object interimNotification = getVariableFromHistory(businessKey, "SDNCREST_interimNotification");
        Assert.assertNotNull(interimNotification);

        injectSDNCRestCallbacks(callbacks, "nonfinal");

        // Second non-final response will not have done a notification
        interimNotification = getVariableFromHistory(businessKey, "SDNCREST_interimNotification");
        Assert.assertNull(interimNotification);

        injectSDNCRestCallbacks(callbacks, "final");

        interimNotification = this.getVariableFromHistory(businessKey, "SDNCREST_interimNotification");
        Assert.assertNull(interimNotification);

        waitForProcessEnd(businessKey, 10000);

        Assert.assertTrue(isProcessEnded(businessKey));

        logEnd();
    }

    /**
     * Defines WireMock stubs needed by these tests.
     */
    private void mocks() {
        wireMockServer.stubFor(post(urlEqualTo("/SDNCAdapter/v1/sdnc"))
                .willReturn(aResponse().withStatus(202).withHeader("Content-Type", "application/json")));
    }
}
