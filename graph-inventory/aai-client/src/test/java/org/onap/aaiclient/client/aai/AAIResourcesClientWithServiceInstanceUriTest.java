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

import static org.junit.Assert.assertThrows;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.aaiclient.client.aai.entities.uri.AAIClientUriFactory;
import org.onap.aaiclient.client.aai.entities.uri.ServiceInstanceUri;
import org.onap.aaiclient.client.defaultproperties.DefaultAAIPropertiesImpl;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

@RunWith(MockitoJUnitRunner.class)
public class AAIResourcesClientWithServiceInstanceUriTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

    @Spy
    public AAIClient client;

    @InjectMocks
    public AAIResourcesClient aaiClient = new AAIResourcesClient();

    private ServiceInstanceUri uri;

    @Before
    public void setUp() {
        doReturn(new DefaultAAIPropertiesImpl(wireMockRule.port())).when(client).getRestProperties();
        wireMockRule.stubFor(get(urlMatching("/aai/v[0-9]+/nodes.*")).willReturn(
                aResponse().withStatus(404).withHeader("Content-Type", "application/json").withHeader("Mock", "true")));

        uri = spy((ServiceInstanceUri) AAIClientUriFactory.createResourceUri(Types.SERVICE_INSTANCE.getFragment("id")));
        doReturn(aaiClient).when(uri).getResourcesClient();
    }

    @Test
    public void getWithClass() {
        AAIResourcesClient client = aaiClient;
        Optional<String> result = client.get(String.class, uri);

        assertThat(result.isPresent(), equalTo(false));
    }

    @Test
    public void getFullResponse() {
        AAIResourcesClient client = aaiClient;
        Response result = client.getFullResponse(uri);
        assertThat(result.getStatus(), equalTo(Status.NOT_FOUND.getStatusCode()));
    }

    @Test
    public void getWithGenericType() {
        AAIResourcesClient client = aaiClient;
        Optional<List<String>> result = client.get(new GenericType<List<String>>() {}, uri);
        assertThat(result.isPresent(), equalTo(false));
    }

    @Test
    public void getAAIWrapper() {
        AAIResourcesClient client = aaiClient;
        AAIResultWrapper result = client.get(uri);
        assertThat(result.isEmpty(), equalTo(true));
    }

    @Test
    public void getWithException() {
        AAIResourcesClient client = aaiClient;
        assertThrows(IllegalArgumentException.class, () -> client.get(uri, IllegalArgumentException.class));
    }

    @Test
    public void existsTest() {
        AAIResourcesClient client = aaiClient;
        doReturn(uri).when(uri).clone();
        boolean result = client.exists(uri);
        assertThat(result, equalTo(false));
    }

}
