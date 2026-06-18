/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG.
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
package org.onap.so.heatbridge.openstack.connector;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.openstack4j.core.transport.Config;
import org.openstack4j.core.transport.HttpMethod;
import org.openstack4j.core.transport.HttpRequest;
import org.openstack4j.core.transport.HttpResponse;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class HttpClientExecutorServiceTest {

    @Rule
    public WireMockRule wireMock = new WireMockRule(8089);

    @Test
    public void testGetExecutorDisplayName() {
        HttpClientExecutorService executor = new HttpClientExecutorService();
        assertEquals("Apache HttpClient Connector", executor.getExecutorDisplayName());
    }

    @Test
    public void testExecuteGetRequestWithQueryParams() throws IOException {
        wireMock.stubFor(get(urlPathEqualTo("/v3/servers")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody("{\"servers\":[]}")));

        HttpClientExecutorService executor = new HttpClientExecutorService();

        HttpRequest<String> request =
                HttpRequest.builder(String.class).endpoint("http://localhost:8089").path("/v3/servers")
                        .method(HttpMethod.GET).queryParam("name", "test-vm").queryParam("status", "ACTIVE").build();

        HttpResponse response = executor.execute(request);

        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType());
        response.close();

        wireMock.verify(getRequestedFor(urlPathEqualTo("/v3/servers")).withQueryParam("name", equalTo("test-vm"))
                .withQueryParam("status", equalTo("ACTIVE")));
    }

    @Test
    public void testExecuteGetRequestWithMultipleQueryParamValues() throws IOException {
        wireMock.stubFor(get(urlPathEqualTo("/v3/search")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody("{\"results\":[]}")));

        HttpClientExecutorService executor = new HttpClientExecutorService();

        HttpRequest<String> request =
                HttpRequest.builder(String.class).endpoint("http://localhost:8089").path("/v3/search")
                        .method(HttpMethod.GET).queryParam("tag", "compute").queryParam("tag", "storage").build();

        HttpResponse response = executor.execute(request);

        assertNotNull(response);
        assertEquals(200, response.getStatus());
        response.close();

        wireMock.verify(getRequestedFor(urlPathEqualTo("/v3/search")).withQueryParam("tag", equalTo("compute"))
                .withQueryParam("tag", equalTo("storage")));
    }

    @Test
    public void testExecuteGetRequestWithHeaders() throws IOException {
        wireMock.stubFor(get(urlPathEqualTo("/v3/servers/abc123"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withHeader("X-Response-Id", "resp-123").withBody("{\"server\":{}}")));

        HttpClientExecutorService executor = new HttpClientExecutorService();

        HttpRequest<String> request = HttpRequest.builder(String.class).endpoint("http://localhost:8089")
                .path("/v3/servers/abc123").method(HttpMethod.GET).header("X-Auth-Token", "token-abc-123")
                .header("X-Request-Id", "req-456").build();

        HttpResponse response = executor.execute(request);

        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertEquals("resp-123", response.header("X-Response-Id"));
        response.close();

        wireMock.verify(getRequestedFor(urlPathEqualTo("/v3/servers/abc123"))
                .withHeader("X-Auth-Token", equalTo("token-abc-123")).withHeader("X-Request-Id", equalTo("req-456")));
    }

    @Test
    public void testExecutePostRequestWithBody() throws IOException {
        wireMock.stubFor(post(urlPathEqualTo("/v3/servers")).willReturn(aResponse().withStatus(201)
                .withHeader("Content-Type", "application/json").withBody("{\"server\":{\"id\":\"new-123\"}}")));

        HttpClientExecutorService executor = new HttpClientExecutorService();

        String requestBody = "{\"server\":{\"name\":\"test-vm\",\"imageRef\":\"image-123\"}}";

        HttpRequest<String> request =
                HttpRequest.builder(String.class).endpoint("http://localhost:8089").path("/v3/servers")
                        .method(HttpMethod.POST).header("Content-Type", "application/json").json(requestBody).build();

        HttpResponse response = executor.execute(request);

        assertNotNull(response);
        assertEquals(201, response.getStatus());
        response.close();

        wireMock.verify(
                postRequestedFor(urlPathEqualTo("/v3/servers")).withHeader("Content-Type", equalTo("application/json"))
                        .withRequestBody(containing("test-vm")).withRequestBody(containing("image-123")));
    }

    @Test
    public void testExecutePutRequest() throws IOException {
        wireMock.stubFor(put(urlPathEqualTo("/v3/servers/abc123")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody("{\"server\":{}}")));

        HttpClientExecutorService executor = new HttpClientExecutorService();

        String requestBody = "{\"server\":{\"name\":\"updated-vm\"}}";

        HttpRequest<String> request =
                HttpRequest.builder(String.class).endpoint("http://localhost:8089").path("/v3/servers/abc123")
                        .method(HttpMethod.PUT).header("Content-Type", "application/json").json(requestBody).build();

        HttpResponse response = executor.execute(request);

        assertNotNull(response);
        assertEquals(200, response.getStatus());
        response.close();

        wireMock.verify(putRequestedFor(urlPathEqualTo("/v3/servers/abc123"))
                .withHeader("Content-Type", equalTo("application/json")).withRequestBody(containing("updated-vm")));
    }

    @Test
    public void testExecuteDeleteRequest() throws IOException {
        wireMock.stubFor(delete(urlPathEqualTo("/v3/servers/abc123")).willReturn(aResponse().withStatus(204)));

        HttpClientExecutorService executor = new HttpClientExecutorService();

        HttpRequest<String> request = HttpRequest.builder(String.class).endpoint("http://localhost:8089")
                .path("/v3/servers/abc123").method(HttpMethod.DELETE).build();

        HttpResponse response = executor.execute(request);

        assertNotNull(response);
        assertEquals(204, response.getStatus());
        response.close();

        wireMock.verify(deleteRequestedFor(urlPathEqualTo("/v3/servers/abc123")));
    }

    @Test
    public void testResponseClosesClientAndResponse() throws IOException {
        wireMock.stubFor(get(urlEqualTo("/test"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/plain").withBody("test")));

        HttpClientExecutorService executor = new HttpClientExecutorService();

        HttpRequest<String> request = HttpRequest.builder(String.class).endpoint("http://localhost:8089").path("/test")
                .method(HttpMethod.GET).build();

        HttpResponse response = executor.execute(request);

        assertNotNull(response);
        assertEquals(200, response.getStatus());

        // Verify close doesn't throw exception
        response.close();

        // Second close should be idempotent
        response.close();
    }

    @Test(expected = RuntimeException.class)
    public void testExecuteThrowsExceptionOnInvalidUri() {
        HttpClientExecutorService executor = new HttpClientExecutorService();

        // Invalid URI (malformed)
        HttpRequest<String> request = HttpRequest.builder(String.class).endpoint("ht!tp://invalid uri").path("/test")
                .method(HttpMethod.GET).build();

        executor.execute(request);
    }
}
