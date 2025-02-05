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

package org.onap.so.bpmn.common.baseclient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import java.util.Map;
import jakarta.ws.rs.core.UriBuilder;
import org.junit.Test;
import org.onap.so.BaseTest;
import org.onap.so.client.BaseClient;
import org.springframework.core.ParameterizedTypeReference;
import wiremock.org.apache.http.entity.ContentType;


public class BaseClientTest extends BaseTest {

    @Test
    public void verifyString() {
        BaseClient<String, String> client = new BaseClient<>();
        String response = "{\"hello\" : \"world\"}";
        client.setTargetUrl(
                UriBuilder.fromUri("http://localhost/test").port(Integer.parseInt(wireMockPort)).build().toString());
        wireMockServer.stubFor(get(urlEqualTo("/test")).willReturn(aResponse().withStatus(200).withBody(response)
                .withHeader("Content-Type", ContentType.APPLICATION_JSON.toString())));

        String result = client.get("", new ParameterizedTypeReference<String>() {});
        assertThat(result, equalTo(response));
    }

    @Test
    public void verifyMap() {
        BaseClient<String, Map<String, Object>> client = new BaseClient<>();
        String response = "{\"hello\" : \"world\"}";
        client.setTargetUrl(
                UriBuilder.fromUri("http://localhost/test").port(Integer.parseInt(wireMockPort)).build().toString());
        wireMockServer.stubFor(get(urlEqualTo("/test")).willReturn(aResponse().withStatus(200).withBody(response)
                .withHeader("Content-Type", ContentType.APPLICATION_JSON.toString())));

        Map<String, Object> result = client.get("", new ParameterizedTypeReference<Map<String, Object>>() {});
        assertThat("world", equalTo(result.get("hello")));
    }
}
