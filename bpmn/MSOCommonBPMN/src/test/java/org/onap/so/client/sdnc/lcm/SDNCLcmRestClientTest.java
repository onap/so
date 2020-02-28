/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.client.sdnc.lcm;

import org.apache.http.HttpStatus;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import org.onap.logging.filter.base.ONAPComponents;
import org.onap.so.BaseTest;
import org.onap.so.client.restproperties.SDNCLcmPropertiesImpl;
import org.onap.so.client.sdnc.common.SDNCConstants;
import org.onap.so.client.sdnc.lcm.beans.*;

public class SDNCLcmRestClientTest extends BaseTest {

    protected SDNCLcmMessageBuilderTest sdncLcmMessageBuilderTest = new SDNCLcmMessageBuilderTest();

    protected static final String SDNC_HOST_PROP = "sdnc.host";
    protected static final String SDNC_PATH_PROP = "sdnc.lcm.path";

    String operation = "test-operation";

    public SDNCLcmRestClient buildSDNCLcmRestClient() {
        String testHost = "http://localhost:" + wireMockPort;

        System.setProperty(SDNC_HOST_PROP, testHost);
        System.setProperty(SDNC_PATH_PROP, SDNCConstants.LCM_API_BASE_PATH);

        SDNCLcmProperties sdncLcmProperties = new SDNCLcmPropertiesImpl();
        SDNCLcmClientBuilder sdncLcmClientBuilder = new SDNCLcmClientBuilder(sdncLcmProperties);

        try {
            return sdncLcmClientBuilder.newSDNCLcmRestClient(operation);
        } catch (Exception e) {
            fail("Create SDNCLcmRestClient error: " + e.toString());
            System.clearProperty(SDNC_HOST_PROP);
            System.clearProperty(SDNC_PATH_PROP);
            return null;
        }
    }

    @Test
    public final void testSDNCLcmRestClientSendRequest() {
        SDNCLcmRestClient sdncLcmRestClient = buildSDNCLcmRestClient();

        assertNotEquals(null, sdncLcmRestClient);
        assertEquals(ONAPComponents.SDNC, sdncLcmRestClient.getTargetEntity());

        String expectedLcmOutput = LcmOutputTest.getExpectedLcmOutput();
        String expectedLcmRestResponse = "{" + "\"output\":" + LcmOutputTest.getExpectedLcmOutput() + "}";
        wireMockServer.stubFor(post(urlPathEqualTo(SDNCConstants.LCM_API_BASE_PATH + operation))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(expectedLcmRestResponse)
                        .withStatus(HttpStatus.SC_OK)));

        LcmInput lcmInput = sdncLcmMessageBuilderTest.buildLcmRestRequestForPnf().getInput();
        LcmOutput lcmOutput = sdncLcmRestClient.sendRequest(lcmInput);

        System.clearProperty(SDNC_HOST_PROP);
        System.clearProperty(SDNC_PATH_PROP);

        try {
            String lcmOutputString = sdncLcmMessageBuilderTest.convertToSting(lcmOutput);
            assertEquals(expectedLcmOutput, lcmOutputString);
        } catch (Exception e) {
            fail("Convert LcmOutput to String error: " + e.toString());
        }
    }
}
