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
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import javax.net.ssl.SSLException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import org.javatuples.Pair;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.logging.filter.base.ONAPComponents;
import org.onap.so.logging.filter.base.ONAPComponentsList;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

@RunWith(MockitoJUnitRunner.class)
public class RestClientTest {


    private final HttpClientFactory httpClientFactory = new HttpClientFactory();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(
            WireMockConfiguration.options().dynamicPort().extensions(new ResponseTemplateTransformer(false)));

    @Test
    public void retries() throws Exception {
        RestClient spy = buildSpy();
        doThrow(new WebApplicationException(new SocketTimeoutException())).when(spy).buildRequest(any(String.class),
                ArgumentMatchers.isNull());
        try {
            spy.get();
        } catch (Exception e) {
            // ignore this exception for this test
        }
        verify(spy, times(3)).buildRequest(any(String.class), ArgumentMatchers.isNull());

    }

    @Test
    public void retryOnChunkedNetworkIssue() throws Exception {
        RestClient spy = buildSpy();
        doThrow(new ResponseProcessingException(null, "something something", new SSLException("wow"))).when(spy)
                .buildRequest(any(String.class), ArgumentMatchers.isNull());
        try {
            spy.get();
        } catch (Exception e) {
            // ignore this exception for this test
        }
        verify(spy, times(3)).buildRequest(any(String.class), ArgumentMatchers.isNull());

    }

    @Test
    public void exceptionDoNotRetry() throws Exception {
        RestClient spy = buildSpy();
        doThrow(new WebApplicationException(new NotFoundException())).when(spy).buildRequest(any(String.class),
                ArgumentMatchers.isNull());
        try {
            spy.get();
        } catch (Exception e) {
            // we expect an exception, ignore it
        }
        verify(spy, times(1)).buildRequest(any(String.class), ArgumentMatchers.isNull());

    }

    @Test
    public void timeoutTest() throws URISyntaxException {
        wireMockRule.stubFor(get("/chunked/delayed")
                .willReturn(aResponse().withStatus(200).withBody("Hello world!").withChunkedDribbleDelay(2, 300)));


        RestProperties props = new RestProperties() {


            @Override
            public Integer getRetries() {
                return Integer.valueOf(0);
            }

            @Override
            public URL getEndpoint() throws MalformedURLException {
                return new URL(String.format("http://localhost:%s", wireMockRule.port()));
            }

            @Override
            public String getSystemName() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Long getReadTimeout() {
                return Long.valueOf(100);
            }
        };
        RestClient client = new RestClient(props, Optional.of(new URI("/chunked/delayed"))) {

            @Override
            protected void initializeHeaderMap(MultivaluedMap<String, Pair<String, String>> headerMap) {
                // TODO Auto-generated method stub

            }

            @Override
            protected ONAPComponentsList getTargetEntity() {
                return ONAPComponents.EXTERNAL;
            }

        };

        thrown.expect(ProcessingException.class);
        client.get();

    }

    private RestClient buildSpy() throws MalformedURLException, IllegalArgumentException, UriBuilderException {
        RestClient client = httpClientFactory.newJsonClient(UriBuilder.fromUri("http://localhost/test").build().toURL(),
                ONAPComponents.BPMN);

        return spy(client);
    }
}
