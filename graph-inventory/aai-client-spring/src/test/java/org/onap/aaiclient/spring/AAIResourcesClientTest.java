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
import org.junit.jupiter.api.BeforeEach;
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

@SpringBootTest(properties = {"aai.endpoint=http://localhost:${wiremock.server.port}", "aai.default-version=v24"})
@AutoConfigureWireMock(port = 0)
public class AAIResourcesClientTest {

    @Autowired
    public AAIResourcesClient resourcesClient;

    private static final String AAI_VERSION = "v24";


    @Test
    @SneakyThrows
    public void verifyGetWithoutRelationships() {
        AAIResourceUri path = AAIUriFactory
                .createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().pserver("pserver-hostname-test"));

        WireMock.stubFor(get(urlPathEqualTo("/aai/" + AAI_VERSION + path.build().toString()))
                .willReturn(aResponse().withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
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
                .willReturn(aResponse().withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
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
                .willReturn(aResponse().withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
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
                .willReturn(aResponse().withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                        .withBodyFile("service-instance.json").withStatus(HttpStatus.OK.value())));

        boolean exists = resourcesClient.exists(path);
        assertTrue(exists);
    }

    @Test
    public void verifyExistsIsFalse() {
        AAIResourceUri path = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer("some-customer")
                .serviceSubscription("some-service").serviceInstance("32398696-2630-4466-a9e7-31c1efe7375c"));

        WireMock.stubFor(get(urlPathEqualTo("/aai/" + AAI_VERSION + path.build().toString()))
                .willReturn(aResponse().withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                        .withBodyFile("serviceInstanceNotFound.json").withStatus(HttpStatus.NOT_FOUND.value())));

        boolean exists = resourcesClient.exists(path);
        assertFalse(exists);
    }

    @Test
    public void verifyDelete() {
        AAIResourceUri path = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer("some-customer")
                .serviceSubscription("some-service").serviceInstance("32398696-2630-4466-a9e7-31c1efe7375c"));

        stubFor(get(urlPathEqualTo("/aai/" + AAI_VERSION + path.build().toString()))
                .willReturn(aResponse().withHeader("Content-Type", MediaType.APPLICATION_JSON.toString())
                        .withBodyFile("service-instance.json").withStatus(HttpStatus.OK.value())));

        stubFor(delete(urlPathEqualTo("/aai/" + AAI_VERSION + path.build().toString()))
                .willReturn(aResponse().withStatus(HttpStatus.ACCEPTED.value())));

        resourcesClient.delete(path);
    }

    @Test
    @SneakyThrows
    public void verifyCreate() {
        Customer customer = new Customer();
        customer.setGlobalCustomerId("some-customer");
        customer.setSubscriberName("some-subscriber");
        customer.setSubscriberType("some-subscriber-type");
        AAIResourceUri path = AAIUriFactory
                .createResourceUri(AAIFluentTypeBuilder.business().customer(customer.getGlobalCustomerId()));

        String expectedBody = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/customer.json")),
                StandardCharsets.UTF_8);
        stubFor(put(urlPathEqualTo("/aai/" + AAI_VERSION + path.build().toString()))
                .withHeader("X-FromAppId", matching(".*")).withHeader("X-TransactionId", matching(".*"))
                .withHeader("Content-Type", equalTo(MediaType.APPLICATION_JSON.toString()))
                .withRequestBody(equalToJson(expectedBody))
                .willReturn(aResponse().withStatus(HttpStatus.CREATED.value()).withHeader("Content-Type",
                        MediaType.APPLICATION_JSON.toString())));

        resourcesClient.create(path, customer);
    }

}
