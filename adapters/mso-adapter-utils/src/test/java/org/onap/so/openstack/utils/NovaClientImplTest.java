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

package org.onap.so.openstack.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.config.beans.PoConfig;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.woorea.openstack.nova.Nova;
import com.woorea.openstack.nova.model.Image;

@RunWith(MockitoJUnitRunner.class)
public class NovaClientImplTest {

    private WireMockServer wireMock;

    @Mock
    private NovaClient client;

    @Mock
    private PoConfig poConfig;

    @InjectMocks
    private NovaClientImpl novaClient;

    @Before
    public void setUp() throws Exception {
        wireMock = new WireMockServer(options().dynamicPort());
        wireMock.start();
        Nova nova = new Nova("http://localhost:" + wireMock.port() + "/compute");
        nova.token("token");
        when(client.getNovaClient("cloudSite", "tenant")).thenReturn(nova);
    }

    @After
    public void tearDown() {
        wireMock.stop();
    }

    @Test
    public void queryImageById() throws Exception {
        wireMock.stubFor(get(urlPathEqualTo("/compute/images/img-1")).willReturn(okJson("{\"image\":{\"id\":\"img-1\","
                + "\"name\":\"ubuntu\",\"links\":[{\"rel\":\"self\",\"href\":\"http://nova/v2.1/images/img-1\"}]}}")));

        Image image = novaClient.queryImageById("cloudSite", "tenant", "img-1");

        assertEquals("ubuntu", image.getName());
        assertEquals("http://nova/v2.1/images/img-1", image.getLinks().get(0).getHref());
    }
}
