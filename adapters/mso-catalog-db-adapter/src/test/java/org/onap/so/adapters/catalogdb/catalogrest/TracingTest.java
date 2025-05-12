/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright © 2025 Deutsche Telekom AG Intellectual Property. All rights reserved.
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
package org.onap.so.adapters.catalogdb.catalogrest;

import org.junit.Test;
import org.onap.so.adapters.catalogdb.CatalogDbAdapterBaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.github.tomakehurst.wiremock.client.WireMock;

@AutoConfigureWireMock(port = 0)
@TestPropertySource(
        properties = {"spring.sleuth.enabled=true", "spring.zipkin.base-url=http://localhost:${wiremock.server.port}"})
public class TracingTest extends CatalogDbAdapterBaseTest {

    @Autowired
    RestTemplate restTemplate;

    @Test
    public void thatTracesAreExported() throws InterruptedException {
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/api/v2/spans"))
                .willReturn(WireMock.aResponse().withStatus(HttpStatus.OK.value())));

        try {
            restTemplate.getForObject("http://localhost:" + port + "/foo", String.class);
        } catch (RestClientException e) {
            // this provokes a 404. For the test it's not important what is returned here
        }
        Thread.sleep(1000);
        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/api/v2/spans")));
    }

}
