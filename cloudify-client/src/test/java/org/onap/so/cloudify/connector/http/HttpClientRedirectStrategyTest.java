/*
 * ============LICENSE_START=======================================================
 * ONAP : SO
 * ================================================================================
 * Copyright (C) 2018 Nokia.
 * =============================================================================
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