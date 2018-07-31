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
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.sniro.beans.SniroConductorRequest;
import org.onap.so.client.sniro.beans.SniroManagerRequest;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import static com.github.tomakehurst.wiremock.client.WireMock.*;


public class SniroClientTestIT{

    @Autowired
    private SniroClient client;


    @Test(expected = Test.None.class)
    public void testPostDemands_success() throws BadResponseException, JsonProcessingException {
        String mockResponse = "{\"transactionId\": \"123456789\", \"requestId\": \"1234\", \"statusMessage\": \"corys cool\", \"requestStatus\": \"accepted\"}";

        stubFor(post(urlEqualTo("/sniro/api/placement/v2"))
        			.willReturn(aResponse().withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(mockResponse)));

   	  client.postDemands(new SniroManagerRequest());

    }

    @Test(expected = BadResponseException.class)
    public void testPostDemands_error_failed() throws JsonProcessingException, BadResponseException {
    	String mockResponse = "{\"transactionId\": \"123456789\", \"requestId\": \"1234\", \"statusMessage\": \"missing data\", \"requestStatus\": \"failed\"}";

    	stubFor(post(urlEqualTo("/sniro/api/placement/v2"))
    			.willReturn(aResponse().withStatus(200)
    					.withHeader("Content-Type", "application/json")
    					.withBody(mockResponse)));


    	client.postDemands(new SniroManagerRequest());

    	//TODO	assertEquals("missing data", );

    }

    @Test(expected = BadResponseException.class)
    public void testPostDemands_error_noMessage() throws JsonProcessingException, BadResponseException {
    	String mockResponse = "{\"transactionId\": \"123456789\", \"requestId\": \"1234\", \"statusMessage\": \"\", \"requestStatus\": \"failed\"}";

    	stubFor(post(urlEqualTo("/sniro/api/placement/v2"))
    			.willReturn(aResponse().withStatus(200)
    					.withHeader("Content-Type", "application/json")
    					.withBody(mockResponse)));


    	client.postDemands(new SniroManagerRequest());

    }

    @Test(expected = BadResponseException.class)
    public void testPostDemands_error_noStatus() throws JsonProcessingException, BadResponseException {
    	String mockResponse = "{\"transactionId\": \"123456789\", \"requestId\": \"1234\", \"statusMessage\": \"missing data\", \"requestStatus\": null}";

    	stubFor(post(urlEqualTo("/sniro/api/placement/v2"))
    			.willReturn(aResponse().withStatus(200)
    					.withHeader("Content-Type", "application/json")
    					.withBody(mockResponse)));


    	client.postDemands(new SniroManagerRequest());

    }

    @Test(expected = BadResponseException.class)
    public void testPostDemands_error_empty() throws JsonProcessingException, BadResponseException {
    	String mockResponse = "{ }";

    	stubFor(post(urlEqualTo("/sniro/api/placement/v2"))
    			.willReturn(aResponse().withStatus(200)
    					.withHeader("Content-Type", "application/json")
    					.withBody(mockResponse)));


    	client.postDemands(new SniroManagerRequest());
    }

    @Test(expected = Test.None.class)
    public void testPostRelease_success() throws BadResponseException, JsonProcessingException {
        String mockResponse = "{\"status\": \"success\", \"message\": \"corys cool\"}";

        stubFor(post(urlEqualTo("/v1/release-orders"))
        			.willReturn(aResponse().withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(mockResponse)));

   	  client.postRelease(new SniroConductorRequest());
    }

    @Test(expected = BadResponseException.class)
    public void testPostRelease_error_failed() throws BadResponseException, JsonProcessingException {
        String mockResponse = "{\"status\": \"failure\", \"message\": \"corys cool\"}";

        stubFor(post(urlEqualTo("/v1/release-orders"))
        			.willReturn(aResponse().withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(mockResponse)));

   	  client.postRelease(new SniroConductorRequest());
    }

    @Test(expected = BadResponseException.class)
    public void testPostRelease_error_noStatus() throws BadResponseException, JsonProcessingException {
        String mockResponse = "{\"status\": \"\", \"message\": \"corys cool\"}";

        stubFor(post(urlEqualTo("/v1/release-orders"))
        			.willReturn(aResponse().withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(mockResponse)));

   	  client.postRelease(new SniroConductorRequest());

    }

    @Test(expected = BadResponseException.class)
    public void testPostRelease_error_noMessage() throws BadResponseException, JsonProcessingException {
        String mockResponse = "{\"status\": \"failure\", \"message\": null}";

        stubFor(post(urlEqualTo("/v1/release-orders"))
        			.willReturn(aResponse().withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(mockResponse)));

   	  client.postRelease(new SniroConductorRequest());

    }

    @Test(expected = BadResponseException.class)
    public void testPostRelease_error_empty() throws BadResponseException, JsonProcessingException {
        String mockResponse = "{ }";

        stubFor(post(urlEqualTo("/v1/release-orders"))
        			.willReturn(aResponse().withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(mockResponse)));

   	  client.postRelease(new SniroConductorRequest());

    }

}
