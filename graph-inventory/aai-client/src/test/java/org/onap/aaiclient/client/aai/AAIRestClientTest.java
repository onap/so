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

package org.onap.aaiclient.client.aai;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import org.javatuples.Pair;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aaiclient.client.defaultproperties.DefaultAAIPropertiesImpl;
import org.onap.aaiclient.client.graphinventory.GraphInventoryPatchConverter;
import org.onap.aaiclient.client.graphinventory.exceptions.GraphInventoryPatchDepthExceededException;
import org.onap.so.client.RestClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class AAIRestClientTest {

    @Mock
    private AAIProperties props;

    private ObjectMapper mapper = new AAICommonObjectMapperProvider().getMapper();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

    @Test
    public void failPatchOnComplexObject() throws URISyntaxException {
        AAIRestClient client = new AAIRestClient(props, new URI(""), new MultivaluedHashMap<>());
        this.thrown.expect(GraphInventoryPatchDepthExceededException.class);
        this.thrown.expectMessage(containsString("Object exceeds allowed depth for update action"));
        client.patch(
                "{ \"hello\" : \"world\", \"nestedSimple\" : [\"value1\" , \"value2\"], \"relationship-list\" : [{\"key\" : \"value\"}], \"nested\" : { \"key\" : \"value\" }}");
    }

    @Test
    public void verifyPatchValidation() throws URISyntaxException {
        AAIRestClient client = new AAIRestClient(props, new URI(""), new MultivaluedHashMap<>());
        AAIRestClient spy = spy(client);
        GraphInventoryPatchConverter patchValidatorMock = mock(GraphInventoryPatchConverter.class);
        doReturn(patchValidatorMock).when(spy).getPatchConverter();
        String payload = "{}";
        doReturn(Response.ok().build()).when(spy).method(eq("PATCH"), any());
        spy.patch(payload);
        verify(patchValidatorMock, times(1)).convertPatchFormat(eq((Object) payload));
    }

    @Test
    public void verifyAdditionalHeadersTest() throws URISyntaxException {
        AAIRestClient client = new AAIRestClient(new DefaultAAIPropertiesImpl(wireMockRule.port()), new URI("/test"),
                new MultivaluedHashMap<String, Pair<String, String>>(
                        ImmutableMap.of("ALL", Pair.with("test", "value"), "GET", Pair.with("get test", "value"))));
        wireMockRule.stubFor(get(urlPathEqualTo("/test")).willReturn(aResponse().withStatus(200)));
        client.get();
        wireMockRule.verify(getRequestedFor(urlPathEqualTo("/test")).withHeader("X-FromAppId", equalTo("MSO"))
                .withHeader("X-TransactionId", matching(".*")).withHeader("test", equalTo("value"))
                .withHeader("get test", equalTo("value")));
    }


    @Test
    public void cacheGetTest() throws URISyntaxException {

        wireMockRule.stubFor(get(urlPathMatching("/cached"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/plain").withBody("value")));

        AAIProperties props = new AAIProperties() {

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
            public boolean isCachingEnabled() {
                return true;
            }

            @Override
            public AAIVersion getDefaultVersion() {
                return AAIVersion.LATEST;
            }

            @Override
            public String getAuth() {
                return null;
            }

            @Override
            public String getKey() {
                return null;
            }

        };
        RestClient client = new AAIRestClient(props, new URI("/cached"), new MultivaluedHashMap<>());

        Response response = client.get();

        response.readEntity(String.class);
        response = client.get();
        response.readEntity(String.class);
        verify(1, getRequestedFor(urlEqualTo("/cached")));

    }

    @Test
    public void cachePutTest() throws URISyntaxException {

        wireMockRule.stubFor(put(urlPathMatching("/cached/1")).willReturn(aResponse().withStatus(200)));

        wireMockRule.stubFor(get(urlPathMatching("/cached/1"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("{}")));

        AAIProperties props = new AAIProperties() {

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
            public boolean isCachingEnabled() {
                return true;
            }

            @Override
            public AAIVersion getDefaultVersion() {
                return AAIVersion.LATEST;
            }

            @Override
            public String getAuth() {
                return null;
            }

            @Override
            public String getKey() {
                return null;
            }

        };

        RestClient client = new AAIRestClient(props, new URI("/cached/1"), new MultivaluedHashMap<>());


        Response response = client.get();

        response.readEntity(String.class);
        client.put("wow");

        response = client.get();
        response.readEntity(String.class);
        verify(2, getRequestedFor(urlEqualTo("/cached/1")));

    }
}
