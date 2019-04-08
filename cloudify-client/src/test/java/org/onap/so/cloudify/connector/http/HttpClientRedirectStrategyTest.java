/*
 * ============LICENSE_START======================================================= ONAP : SO
 * ================================================================================ Copyright (C) 2018 Nokia.
 * ============================================================================= Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.so.cloudify.connector.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;
import org.junit.Test;

public class HttpClientRedirectStrategyTest {

    private HttpClientRedirectStrategy httpClientRedirectStrategy = new HttpClientRedirectStrategy();

    @Test
    public void isRedirectable_shouldReturnFalse_forNonRedirectableHttpMethods() {
        assertThat(httpClientRedirectStrategy.isRedirectable(HttpPost.METHOD_NAME)).isFalse();
        assertThat(httpClientRedirectStrategy.isRedirectable(HttpPatch.METHOD_NAME)).isFalse();
        assertThat(httpClientRedirectStrategy.isRedirectable(HttpPut.METHOD_NAME)).isFalse();
        assertThat(httpClientRedirectStrategy.isRedirectable(HttpOptions.METHOD_NAME)).isFalse();
        assertThat(httpClientRedirectStrategy.isRedirectable(HttpTrace.METHOD_NAME)).isFalse();
    }

    @Test
    public void isRedirectable_shouldReturnTrue_forRedirectableHttpMethods() {
        assertThat(httpClientRedirectStrategy.isRedirectable(HttpGet.METHOD_NAME)).isTrue();
        assertThat(httpClientRedirectStrategy.isRedirectable(HttpDelete.METHOD_NAME)).isTrue();
        assertThat(httpClientRedirectStrategy.isRedirectable(HttpHead.METHOD_NAME)).isTrue();
    }

    @Test
    public void getRedirect_shouldReturnHttpHeadUriRequest() throws URISyntaxException, ProtocolException {
        assertHttpUriRequestFor(HttpHead.METHOD_NAME, HttpHead.class);
    }

    @Test
    public void getRedirect_shouldReturnHttpGetUriRequest() throws URISyntaxException, ProtocolException {
        assertHttpUriRequestFor(HttpGet.METHOD_NAME, HttpGet.class);
    }

    private void assertHttpUriRequestFor(String methodName, Class<? extends HttpUriRequest> expectedHttpMethodClass)
            throws URISyntaxException, ProtocolException {
        // GIVEN
        HttpRequest request = mock(HttpRequest.class, RETURNS_DEEP_STUBS);
        given(request.getRequestLine().getMethod()).willReturn(methodName);
        HttpResponse response = null;
        HttpContext context = null;
        URI expectedUri = new URI("http://localhost/host");
        // WHEN
        HttpUriRequest httpUriRequest =
                new TestableHttpClientRedirectStrategy(expectedUri).getRedirect(request, response, context);
        // THEN
        assertThat(httpUriRequest).isInstanceOf(expectedHttpMethodClass);
        assertThat(httpUriRequest.getURI()).isEqualTo(expectedUri);
    }

    @Test
    public void getRedirect_shouldReturnHttpGetUri_byDefault() throws URISyntaxException, ProtocolException {
        // GIVEN
        HttpRequest request = mock(HttpRequest.class, RETURNS_DEEP_STUBS);
        given(request.getRequestLine().getMethod()).willReturn(HttpPost.METHOD_NAME);
        HttpResponse response = mock(HttpResponse.class, RETURNS_DEEP_STUBS);
        given(response.getStatusLine().getStatusCode()).willReturn(HttpStatus.SC_ACCEPTED);
        URI expectedUri = new URI("http://localhost/host");
        HttpContext context = null;
        // WHEN
        HttpUriRequest httpUriRequest =
                new TestableHttpClientRedirectStrategy(expectedUri).getRedirect(request, response, context);
        // THEN
        assertThat(httpUriRequest).isInstanceOf(HttpGet.class);
        assertThat(httpUriRequest.getURI()).isEqualTo(expectedUri);
    }

    @Test
    public void getRedirect_shouldCopyHttpRequestAndSetNewUri_forMovedTemporarilyStatus()
            throws URISyntaxException, ProtocolException {
        assertHttpRequestIsCopied(HttpStatus.SC_MOVED_TEMPORARILY);
    }

    @Test
    public void getRedirect_shouldCopyHttpRequestAndSetNewUri_forTemporaryRedirectStatus()
            throws URISyntaxException, ProtocolException {
        assertHttpRequestIsCopied(HttpStatus.SC_TEMPORARY_REDIRECT);
    }

    private void assertHttpRequestIsCopied(int expectedHttpStatus) throws URISyntaxException, ProtocolException {
        // GIVEN
        HttpRequest request = mock(HttpRequest.class, RETURNS_DEEP_STUBS);
        given(request.getRequestLine().getMethod()).willReturn(HttpGet.METHOD_NAME);
        given(request.getRequestLine().getUri()).willReturn("http://hostname");
        HttpResponse response = mock(HttpResponse.class, RETURNS_DEEP_STUBS);
        given(response.getStatusLine().getStatusCode()).willReturn(expectedHttpStatus);
        URI expectedUri = new URI("http://localhost/host");
        HttpContext context = null;
        // WHEN
        HttpUriRequest httpUriRequest =
                new TestableHttpClientRedirectStrategy(expectedUri).getRedirect(request, response, context);
        // THEN
        assertThat(httpUriRequest).isInstanceOf(HttpGet.class);
        assertThat(httpUriRequest.getURI()).isEqualTo(expectedUri);
    }

    private static class TestableHttpClientRedirectStrategy extends HttpClientRedirectStrategy {

        private final URI expectedUri;

        public TestableHttpClientRedirectStrategy(URI expectedUri) {
            this.expectedUri = expectedUri;
        }

        @Override
        public URI getLocationURI(HttpRequest request, HttpResponse response, HttpContext context) {
            return expectedUri;
        }
    }
}
