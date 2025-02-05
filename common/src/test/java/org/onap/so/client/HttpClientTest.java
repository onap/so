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

package org.onap.so.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.Rule;
import org.junit.Test;
import org.onap.so.logging.filter.base.ONAPComponents;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class HttpClientTest {


    private final HttpClientFactory httpClientFactory = new HttpClientFactory();
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort().dynamicHttpsPort());

    @Test
    public void testPost_success() throws MalformedURLException {

        wireMockRule.stubFor(post(urlEqualTo("/services/sdnc/post"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("")));

        URL url = new URL("http://localhost:" + wireMockRule.port() + "/services/sdnc/post");
        HttpClient client = httpClientFactory.newJsonClient(url, ONAPComponents.BPMN);

        client.addBasicAuthHeader(
                "97FF88AB352DA16E00DDD81E3876431DEF8744465DACA489EB3B3BE1F10F63EDA1715E626D0A4827A3E19CD88421BF",
                "123");
        client.addAdditionalHeader("Accept", "application/json");

        client.post("{}");

        verify(exactly(1), postRequestedFor(urlEqualTo("/services/sdnc/post")));
    }

    @Test
    public void testPost_nullHeader() throws MalformedURLException {

        wireMockRule.stubFor(post(urlEqualTo("/services/sdnc/post"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("")));

        URL url = new URL("http://localhost:" + wireMockRule.port() + "/services/sdnc/post");
        HttpClient client = httpClientFactory.newJsonClient(url, ONAPComponents.BPMN);

        client.addAdditionalHeader("id", null);

        client.post("{}");

        verify(exactly(1),
                postRequestedFor(urlEqualTo("/services/sdnc/post")).withHeader("Accept", equalTo("application/json")));
    }

    @Test
    public void testPost_nullBasicAuth() throws MalformedURLException {

        wireMockRule.stubFor(post(urlEqualTo("/services/sdnc/post"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("")));

        URL url = new URL("http://localhost:" + wireMockRule.port() + "/services/sdnc/post");
        HttpClient client = httpClientFactory.newJsonClient(url, ONAPComponents.BPMN);

        client.addBasicAuthHeader("", "12345");

        client.post("{}");

        verify(exactly(1),
                postRequestedFor(urlEqualTo("/services/sdnc/post")).withHeader("Accept", equalTo("application/json")));
    }

    @Test
    public void testPostUsingXmlClient_success() throws MalformedURLException {

        wireMockRule.stubFor(post(urlEqualTo("/services/sdnc/post"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/xml").withBody("")));

        URL url = new URL("http://localhost:" + wireMockRule.port() + "/services/sdnc/post");
        HttpClient client = httpClientFactory.newXmlClient(url, ONAPComponents.BPMN);

        client.addBasicAuthHeader(
                "97FF88AB352DA16E00DDD81E3876431DEF8744465DACA489EB3B3BE1F10F63EDA1715E626D0A4827A3E19CD88421BF",
                "123");
        client.addAdditionalHeader("Accept", "application/xml");

        client.post("{}");

        verify(exactly(1), postRequestedFor(urlEqualTo("/services/sdnc/post")));
    }

    @Test
    public void testPostUsingXmlClient_nullHeader() throws MalformedURLException {

        wireMockRule.stubFor(post(urlEqualTo("/services/sdnc/post"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/xml").withBody("")));

        URL url = new URL("http://localhost:" + wireMockRule.port() + "/services/sdnc/post");
        HttpClient client = httpClientFactory.newXmlClient(url, ONAPComponents.BPMN);

        client.accept = "application/xml";
        client.addAdditionalHeader("id", null);

        client.post("{}");

        verify(exactly(1),
                postRequestedFor(urlEqualTo("/services/sdnc/post")).withHeader("Accept", equalTo("application/xml")));
    }

    @Test
    public void testPostUsingXmlClient_nullBasicAuth() throws MalformedURLException {

        wireMockRule.stubFor(post(urlEqualTo("/services/sdnc/post"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/xml").withBody("")));

        URL url = new URL("http://localhost:" + wireMockRule.port() + "/services/sdnc/post");
        HttpClient client = httpClientFactory.newXmlClient(url, ONAPComponents.BPMN);

        client.accept = "application/xml";
        client.addBasicAuthHeader("", "12345");

        client.post("{}");

        verify(exactly(1),
                postRequestedFor(urlEqualTo("/services/sdnc/post")).withHeader("Accept", equalTo("application/xml")));
    }

    @Test
    public void testPostUsingTextXmlClient_success() throws MalformedURLException {

        wireMockRule.stubFor(post(urlEqualTo("/services/sdnc/post"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/xml").withBody("")));

        URL url = new URL("http://localhost:" + wireMockRule.port() + "/services/sdnc/post");
        HttpClient client = httpClientFactory.newTextXmlClient(url, ONAPComponents.BPMN);

        client.addBasicAuthHeader(
                "97FF88AB352DA16E00DDD81E3876431DEF8744465DACA489EB3B3BE1F10F63EDA1715E626D0A4827A3E19CD88421BF",
                "123");
        client.addAdditionalHeader("Accept", "text/xml");

        client.post("{}");

        verify(exactly(1), postRequestedFor(urlEqualTo("/services/sdnc/post")));
    }

    @Test
    public void testPostUsingTextXmlClient_nullHeader() throws MalformedURLException {

        wireMockRule.stubFor(post(urlEqualTo("/services/sdnc/post"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/xml").withBody("")));

        URL url = new URL("http://localhost:" + wireMockRule.port() + "/services/sdnc/post");
        HttpClient client = httpClientFactory.newTextXmlClient(url, ONAPComponents.BPMN);

        client.addAdditionalHeader("id", null);

        client.post("{}");

        verify(exactly(1),
                postRequestedFor(urlEqualTo("/services/sdnc/post")).withHeader("Accept", equalTo("text/xml")));
    }

    @Test
    public void testPostUsingTextXmlClient_nullBasicAuth() throws MalformedURLException {

        wireMockRule.stubFor(post(urlEqualTo("/services/sdnc/post"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/xml").withBody("")));

        URL url = new URL("http://localhost:" + wireMockRule.port() + "/services/sdnc/post");
        HttpClient client = httpClientFactory.newTextXmlClient(url, ONAPComponents.BPMN);

        client.addBasicAuthHeader("", "12345");

        client.post("{}");

        verify(exactly(1),
                postRequestedFor(urlEqualTo("/services/sdnc/post")).withHeader("Accept", equalTo("text/xml")));
    }
}
