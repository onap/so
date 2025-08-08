/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.sniro;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import org.junit.Test;
import org.onap.so.BaseIntegrationTest;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.sniro.beans.SniroConductorRequest;
import org.onap.so.client.sniro.beans.SniroManagerRequest;
import org.springframework.beans.factory.annotation.Autowired;


public class SniroClientTestIT extends BaseIntegrationTest {

    @Autowired
    private SniroClient client;


    @Test(expected = Test.None.class)
    public void testPostDemands_success() throws BadResponseException {
        String mockResponse =
                "{\"transactionId\": \"123456789\", \"requestId\": \"1234\", \"statusMessage\": \"corys cool\", \"requestStatus\": \"accepted\"}";

        wireMockServer.stubFor(post(urlEqualTo("/sniro/api/placement/v2")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(mockResponse)));

        client.postDemands(new SniroManagerRequest());

    }

    @Test(expected = BadResponseException.class)
    public void testPostDemands_error_failed() throws BadResponseException {
        String mockResponse =
                "{\"transactionId\": \"123456789\", \"requestId\": \"1234\", \"statusMessage\": \"missing data\", \"requestStatus\": \"failed\"}";

        wireMockServer.stubFor(post(urlEqualTo("/sniro/api/placement/v2")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(mockResponse)));


        client.postDemands(new SniroManagerRequest());

        // TODO assertEquals("missing data", );

    }

    @Test(expected = BadResponseException.class)
    public void testPostDemands_error_noMessage() throws BadResponseException {
        String mockResponse =
                "{\"transactionId\": \"123456789\", \"requestId\": \"1234\", \"statusMessage\": \"\", \"requestStatus\": \"failed\"}";

        wireMockServer.stubFor(post(urlEqualTo("/sniro/api/placement/v2")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(mockResponse)));


        client.postDemands(new SniroManagerRequest());

    }

    @Test(expected = BadResponseException.class)
    public void testPostDemands_error_noStatus() throws BadResponseException {
        String mockResponse =
                "{\"transactionId\": \"123456789\", \"requestId\": \"1234\", \"statusMessage\": \"missing data\", \"requestStatus\": null}";

        wireMockServer.stubFor(post(urlEqualTo("/sniro/api/placement/v2")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(mockResponse)));


        client.postDemands(new SniroManagerRequest());

    }

    @Test(expected = BadResponseException.class)
    public void testPostDemands_error_empty() throws BadResponseException {
        String mockResponse = "{ }";

        wireMockServer.stubFor(post(urlEqualTo("/sniro/api/placement/v2")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(mockResponse)));


        client.postDemands(new SniroManagerRequest());
    }

    @Test(expected = Test.None.class)
    public void testPostRelease_success() throws BadResponseException {
        String mockResponse = "{\"status\": \"success\", \"message\": \"corys cool\"}";

        wireMockServer.stubFor(post(urlEqualTo("/v1/release-orders")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(mockResponse)));

        client.postRelease(new SniroConductorRequest());
    }

    @Test(expected = BadResponseException.class)
    public void testPostRelease_error_failed() throws BadResponseException {
        String mockResponse = "{\"status\": \"failure\", \"message\": \"corys cool\"}";

        wireMockServer.stubFor(post(urlEqualTo("/v1/release-orders")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(mockResponse)));

        client.postRelease(new SniroConductorRequest());
    }

    @Test(expected = BadResponseException.class)
    public void testPostRelease_error_noStatus() throws BadResponseException {
        String mockResponse = "{\"status\": \"\", \"message\": \"corys cool\"}";

        wireMockServer.stubFor(post(urlEqualTo("/v1/release-orders")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(mockResponse)));

        client.postRelease(new SniroConductorRequest());

    }

    @Test(expected = BadResponseException.class)
    public void testPostRelease_error_noMessage() throws BadResponseException {
        String mockResponse = "{\"status\": \"failure\", \"message\": null}";

        wireMockServer.stubFor(post(urlEqualTo("/v1/release-orders")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(mockResponse)));

        client.postRelease(new SniroConductorRequest());

    }

    @Test(expected = BadResponseException.class)
    public void testPostRelease_error_empty() throws BadResponseException {
        String mockResponse = "{ }";

        wireMockServer.stubFor(post(urlEqualTo("/v1/release-orders")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(mockResponse)));

        client.postRelease(new SniroConductorRequest());

    }

}
