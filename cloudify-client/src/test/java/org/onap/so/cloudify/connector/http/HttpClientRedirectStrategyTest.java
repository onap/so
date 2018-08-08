package org.onap.so.cloudify.connector.http;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
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
}