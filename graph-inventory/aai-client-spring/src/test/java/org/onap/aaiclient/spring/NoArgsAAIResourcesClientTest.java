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

package org.onap.aaiclient.spring;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.onap.aai.domain.yang.Customer;
import org.onap.aai.domain.yang.Pserver;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.ServiceInstances;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAISimplePluralUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.onap.aaiclient.spring.api.AAIListResultWrapper;
import org.onap.aaiclient.spring.api.AAIResultWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.SneakyThrows;

@SpringBootTest
@AutoConfigureWireMock(port = 45123)
public class NoArgsAAIResourcesClientTest {

    private static final String AAI_VERSION = "v24";

    @Test
    @SneakyThrows
    public void verifyGetForNoArgsClient() {
        AAIResourcesClient resourcesClient = new AAIResourcesClient();
        AAIResourceUri path = AAIUriFactory
                .createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().pserver("pserver-hostname-test"));

        WireMock.stubFor(get(urlPathEqualTo("/aai/" + AAI_VERSION + path.build().toString()))
                .withHeader("Authorization", matching(".*")).withHeader("X-FromAppId", matching(".*"))
                .withHeader("X-TransactionId", matching(".*"))
                .withHeader("Content-Type", equalTo(MediaType.APPLICATION_JSON.toString()))
                .willReturn(aResponse().withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                        .withBodyFile("pserver.json").withStatus(HttpStatus.OK.value())));

        AAIResultWrapper<Pserver> result = resourcesClient.get(path, Pserver.class);
        assertEquals("pserver-hostname-test", result.getResult().getHostname());
    }

}
