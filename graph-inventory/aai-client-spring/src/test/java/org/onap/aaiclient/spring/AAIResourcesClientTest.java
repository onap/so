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
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.SneakyThrows;
import wiremock.org.apache.http.entity.ContentType;

public class AAIResourcesClientTest {

    private WireMockServer wireMockServer;
    public AAIResourcesClient resourcesClient;

    private static final String AAI_VERSION = "v24";

    @BeforeEach
    public void setup() {
        wireMockServer = new WireMockServer(0); // Use port 0 to select a random available port
        wireMockServer.start();
        // WireMock.configureFor("localhost", wireMockServer.port());

        // Configure WireMock to use the randomly selected port
        WireMock.configureFor("localhost", wireMockServer.port());
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        RestTemplate restTemplate =
                new RestTemplateBuilder().defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                        .rootUri("http://localhost:" + wireMockServer.port() + "/aai/" + AAI_VERSION)
                        .additionalMessageConverters(new MappingJackson2HttpMessageConverter(mapper)).build();
        resourcesClient = new AAIResourcesClient(restTemplate);
    }

    @AfterEach
    public void stop() {
        wireMockServer.stop();
    }

    @Test
    @SneakyThrows
    public void verifyGetWithoutRelationships() {
        AAIResourceUri path = AAIUriFactory
                .createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().pserver("pserver-hostname-test"));

        WireMock.stubFor(get(urlPathEqualTo("/aai/" + AAI_VERSION + path.build().toString()))
                .willReturn(aResponse().withHeader("Content-Type", ContentType.APPLICATION_JSON.toString())
                        .withBodyFile("pserver.json").withStatus(HttpStatus.OK.value())));

        AAIResultWrapper<Pserver> result = resourcesClient.get(path, Pserver.class);
        assertEquals("pserver-hostname-test", result.getResult().getHostname());

    }

    @Test
    @SneakyThrows
    public void verifyGetWithRelationships() {
        AAIResourceUri path = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer("some-customer")
                .serviceSubscription("some-service").serviceInstance("32398696-2630-4466-a9e7-31c1efe7375c"));

        WireMock.stubFor(get(urlPathEqualTo("/aai/" + AAI_VERSION + path.build().toString()))
                .willReturn(aResponse().withHeader("Content-Type", ContentType.APPLICATION_JSON.toString())
                        .withBodyFile("service-instance.json").withStatus(HttpStatus.OK.value())));

        AAIResultWrapper<ServiceInstance> result = resourcesClient.get(path, ServiceInstance.class);
        assertEquals("Service_Ete_Name74d12c9e-9b9d-43df-a8ce-a805460b8db9",
                result.getResult().getServiceInstanceName());
        List<Relationship> relationships = result.getResult().getRelationshipList().getRelationship();

        assertEquals(1, relationships.size());
        assertEquals("owning-entity", relationships.get(0).getRelatedTo());
        assertEquals("org.onap.relationships.inventory.BelongsTo", relationships.get(0).getRelationshipLabel());
        assertEquals("46bbd369-58c4-4e27-9b09-00463669359f",
                relationships.get(0).getRelationshipData().get(0).getRelationshipValue());
    }

    @Test
    @SneakyThrows
    public void verifyListGetWithRelationships() {
        AAISimplePluralUri path = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business()
                .customer("some-customer").serviceSubscription("some-service").serviceInstances());

        WireMock.stubFor(get(urlPathEqualTo("/aai/" + AAI_VERSION + path.build().toString()))
                .willReturn(aResponse().withHeader("Content-Type", ContentType.APPLICATION_JSON.toString())
                        .withBodyFile("service-instances.json").withStatus(HttpStatus.OK.value())));

        AAIListResultWrapper<ServiceInstances> result = resourcesClient.get(path, ServiceInstances.class);
        assertEquals("32398696-2630-4466-a9e7-31c1efe7375c",
                result.getResult().getServiceInstance().get(0).getServiceInstanceId());
        assertEquals("1752594958741", result.getResult().getServiceInstance().get(0).getResourceVersion());
    }

    @Test
    public void verifyExistsIsTrue() {
        AAIResourceUri path = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer("some-customer")
                .serviceSubscription("some-service").serviceInstance("32398696-2630-4466-a9e7-31c1efe7375c"));

        WireMock.stubFor(get(urlPathEqualTo("/aai/" + AAI_VERSION + path.build().toString()))
                .willReturn(aResponse().withHeader("Content-Type", ContentType.APPLICATION_JSON.toString())
                        .withBodyFile("service-instance.json").withStatus(HttpStatus.OK.value())));

        boolean exists = resourcesClient.exists(path);
        assertTrue(exists);
    }

    @Test
    public void verifyExistsIsFalse() {
        AAIResourceUri path = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer("some-customer")
                .serviceSubscription("some-service").serviceInstance("32398696-2630-4466-a9e7-31c1efe7375c"));

        WireMock.stubFor(get(urlPathEqualTo("/aai/" + AAI_VERSION + path.build().toString()))
                .willReturn(aResponse().withHeader("Content-Type", ContentType.APPLICATION_JSON.toString())
                        .withBodyFile("serviceInstanceNotFound.json").withStatus(HttpStatus.NOT_FOUND.value())));

        boolean exists = resourcesClient.exists(path);
        assertFalse(exists);
    }

}
