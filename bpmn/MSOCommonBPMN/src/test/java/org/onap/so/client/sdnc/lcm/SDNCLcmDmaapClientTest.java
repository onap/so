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

import java.util.List;
import org.apache.http.HttpStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import org.onap.so.BaseTest;
import org.onap.so.client.restproperties.SDNCLcmPropertiesImpl;
import org.onap.so.client.sdnc.lcm.beans.*;

public class SDNCLcmDmaapClientTest extends BaseTest {

    protected SDNCLcmMessageBuilderTest sdncLcmMessageBuilderTest = new SDNCLcmMessageBuilderTest();

    protected static final String DMAAP_HOST_PROP = SDNCLcmPropertiesImpl.DMAAP_HOST;
    protected static final String DMAAP_WRITE_TOPIC_PROP = SDNCLcmPropertiesImpl.LCM_DMAAP_WRITE_TOPIC;
    protected static final String DMAAP_READ_TOPIC_PROP = SDNCLcmPropertiesImpl.LCM_DMAAP_READ_TOPIC;
    protected static final String DMAAP_PARTITION_PROP = SDNCLcmPropertiesImpl.LCM_DMAAP_PARTITION;

    protected String testWriteTopic = "TEST-WRITE-TOPIC";
    protected String testReadTopic = "TEST-READ-TOPIC";
    protected String testPartition = "TESTMSO";
    protected final String defaultConsumerName = "consumer1";

    private void clearSystemProperty() {
        System.clearProperty(DMAAP_HOST_PROP);
        System.clearProperty(DMAAP_WRITE_TOPIC_PROP);
        System.clearProperty(DMAAP_READ_TOPIC_PROP);
        System.clearProperty(DMAAP_PARTITION_PROP);
    }

    public SDNCLcmDmaapClient buildSDNCLcmDmaapClient() {
        String testHost = "http://localhost:" + wireMockPort;

        System.setProperty(DMAAP_HOST_PROP, testHost);
        System.setProperty(DMAAP_WRITE_TOPIC_PROP, testWriteTopic);
        System.setProperty(DMAAP_READ_TOPIC_PROP, testReadTopic);
        System.setProperty(DMAAP_PARTITION_PROP, testPartition);

        SDNCLcmProperties sdncLcmProperties = new SDNCLcmPropertiesImpl();
        SDNCLcmClientBuilder sdncLcmClientBuilder = new SDNCLcmClientBuilder(sdncLcmProperties);

        try {
            return sdncLcmClientBuilder.newSDNCLcmDmaapClient();
        } catch (Exception e) {
            clearSystemProperty();
            fail("Create SDNCLcmDmaapClient error: " + e.toString());
            return null;
        }
    }

    @Test
    public final void testSDNCLcmDmaapClientSendRequest() {
        SDNCLcmDmaapClient sdncLcmDmaapClient = buildSDNCLcmDmaapClient();

        assertNotEquals(null, sdncLcmDmaapClient);

        String testDmaapWritePath = "/events/" + testWriteTopic;
        String expectedWriteResponse = "{\"serverTimeMs\":2,\"count\":1}";
        wireMockServer.stubFor(post(urlPathEqualTo(testDmaapWritePath))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(expectedWriteResponse)
                        .withStatus(HttpStatus.SC_OK)));

        LcmInput lcmInput = sdncLcmMessageBuilderTest.buildLcmInputForPnf();
        LcmDmaapRequest lcmDmaapRequest =
                SDNCLcmMessageBuilder.buildLcmDmaapRequest(sdncLcmMessageBuilderTest.getOperation(), lcmInput);

        try {
            sdncLcmDmaapClient.sendRequest(lcmDmaapRequest);
        } catch (Exception e) {
            clearSystemProperty();
            fail("SDNCLcmDmaapClient sends request error: " + e.toString());
            return;
        }

        String testDmaapReadPath = "/events/" + testReadTopic + "/" + testPartition + "/" + defaultConsumerName;

        String expectedLcmDmaapResponse = LcmDmaapResponseTest.getExpectedLcmDmaapResponse();
        String expectedResponseListItem;
        try {
            expectedResponseListItem = sdncLcmMessageBuilderTest.convertToSting(expectedLcmDmaapResponse);
        } catch (JsonProcessingException e) {
            clearSystemProperty();
            fail("Convert LcmDmaapResponse String to List item error: " + e.toString());
            return;
        }
        String expectedReadResponse = "[" + expectedResponseListItem + "]";
        wireMockServer.stubFor(get(urlPathEqualTo(testDmaapReadPath))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(expectedReadResponse)
                        .withStatus(HttpStatus.SC_OK)));

        List<LcmDmaapResponse> LcmDmaapResponseList = sdncLcmDmaapClient.getResponse();

        clearSystemProperty();

        if (LcmDmaapResponseList.size() < 1) {
            clearSystemProperty();
            fail("Can not get LcmDmaapResponse list");
            return;
        }

        LcmOutput lcmOutput = LcmDmaapResponseList.get(0).getBody().getOutput();

        String expectedLcmOutput = LcmOutputTest.getExpectedLcmOutput();
        try {
            String lcmOutputString = sdncLcmMessageBuilderTest.convertToSting(lcmOutput);
            assertEquals(expectedLcmOutput, lcmOutputString);
        } catch (Exception e) {
            fail("Convert LcmOutput to String error: " + e.toString());
        }
    }
}
