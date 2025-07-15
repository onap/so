/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.aaiclient.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.BadRequestException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.GenericVnfs;
import org.onap.aai.domain.yang.Pserver;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.ServiceInstances;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAISimplePluralUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.api.AAIListResultWrapper;
import org.onap.aaiclient.client.api.AAIResultWrapper;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.admin.NotFoundException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import lombok.SneakyThrows;
import wiremock.org.apache.http.entity.ContentType;

// @WireMockTest
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
        // assertEquals(null, result.getRelationships().);
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

    // @Test
    // public void verifyNotExists() {
    // // AAIResourceUri path = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf("test"));
    // URI path = new URI("/network/generic-vnfs/generic-vnf/test"); // TODO: Replace with builder

    // wireMockRule.stubFor(get(urlPathEqualTo("/aai/" + AAIVersion.LATEST + path.toString()))
    // .willReturn(aResponse().withHeader("Content-Type", "text/plain").withBody("hello").withStatus(404)));
    // AAIResourcesClient client = aaiClient;
    // boolean result = client.exists(path);
    // assertEquals("path not found", false, result);
    // }

    // @Test
    // public void verifyDelete() {
    // AAIResourceUri path = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf("test2"));
    // wireMockRule.stubFor(get(urlPathEqualTo("/aai/" + AAIVersion.LATEST + path.build()))
    // .willReturn(aResponse().withHeader("Content-Type", "application/json")
    // .withBodyFile("aai/resources/mockObject.json").withStatus(200)));
    // wireMockRule.stubFor(delete(urlPathEqualTo("/aai/" + AAIVersion.LATEST + path.build()))
    // .withQueryParam("resource-version", equalTo("1234")).willReturn(aResponse().withStatus(204)));
    // AAIResourcesClient client = aaiClient;
    // client.delete(path);
    // }

    // @Test
    // public void verifyDeleteIfExists() {
    // AAIResourceUri path = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf("test2"));
    // wireMockRule.stubFor(get(urlPathEqualTo("/aai/" + AAIVersion.LATEST + path.build()))
    // .willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(404)));
    // AAIResourcesClient client = aaiClient;
    // client.deleteIfExists(path);
    // }

    // @Test
    // public void verifyDeleteIfExists_exists() {
    // AAIResourceUri path = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf("test2"));
    // wireMockRule.stubFor(get(urlPathEqualTo("/aai/" + AAIVersion.LATEST + path.build()))
    // .willReturn(aResponse().withHeader("Content-Type", "application/json")
    // .withBodyFile("aai/resources/mockObject.json").withStatus(200)));
    // wireMockRule.stubFor(delete(urlPathEqualTo("/aai/" + AAIVersion.LATEST + path.build()))
    // .withQueryParam("resource-version", equalTo("1234")).willReturn(aResponse().withStatus(204)));
    // AAIResourcesClient client = aaiClient;
    // client.deleteIfExists(path);
    // }

    // @Test
    // public void verifyBasicAuth() {
    // AAIResourceUri path = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf("test3"));
    // wireMockRule.stubFor(get(urlPathEqualTo("/aai/" + AAIVersion.LATEST + path.build().toString()))
    // .withHeader("Authorization", equalTo("Basic dGVzdDp0ZXN0"))
    // .willReturn(aResponse().withHeader("Content-Type", "application/json")
    // .withBodyFile("aai/resources/mockObject.json").withStatus(200)));
    // AAIResourcesClient client = aaiClient;
    // client.get(path);
    // }

    // @Test
    // public void verifyConnect() {
    // AAIResourceUri path = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf("test2"));
    // AAIResourceUri path2 = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf("test3"));
    // wireMockRule.stubFor(
    // put(urlPathEqualTo("/aai/" + AAIVersion.LATEST + path.build() + "/relationship-list/relationship"))
    // .willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(200)));

    // AAIResourceUri pathClone = path.clone();
    // AAIResourcesClient client = aaiClient;
    // client.connect(path, path2);
    // assertEquals("uri not modified", pathClone.build().toString(), path.build().toString());
    // }

    // @Test
    // public void verifyDisconnect() {
    // AAIResourceUri path = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf("test2"));
    // AAIResourceUri path2 = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf("test3"));

    // wireMockRule.stubFor(
    // delete(urlPathEqualTo("/aai/" + AAIVersion.LATEST + path.build() + "/relationship-list/relationship"))
    // .willReturn(aResponse().withStatus(204)));

    // AAIResourceUri pathClone = path.clone();
    // AAIResourcesClient client = aaiClient;
    // client.disconnect(path, path2);
    // assertEquals("uri not modified", pathClone.build().toString(), path.build().toString());
    // }

    // @Test
    // public void verifyPatch() {
    // AAIResourceUri path = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf("test2"));

    // wireMockRule.stubFor(post(urlPathEqualTo("/aai/" + AAIVersion.LATEST + path.build()))
    // .willReturn(aResponse().withStatus(200)));

    // AAIResourcesClient client = aaiClient;

    // client.update(path, "{}");
    // }

    // @Test
    // public void verifyNotExistsGet() {
    // AAIResourceUri path = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf("test"));
    // wireMockRule.stubFor(get(urlPathEqualTo("/aai/" + AAIVersion.LATEST + path.build()))
    // .willReturn(aResponse().withHeader("Content-Type", "text/plain").withBody("hello").withStatus(404)));
    // AAIResourcesClient client = aaiClient;
    // AAIResultWrapper result = client.get(path);
    // assertEquals("is empty", true, result.isEmpty());
    // }

    // @Test
    // public void verifyNotExistsGetException() {
    // AAIResourceUri path = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf("test"));
    // wireMockRule.stubFor(get(urlPathEqualTo("/aai/" + AAIVersion.LATEST + path.build()))
    // .willReturn(aResponse().withHeader("Content-Type", "text/plain").withBody("hello").withStatus(404)));
    // AAIResourcesClient client = aaiClient;
    // thrown.expect(NotFoundException.class);
    // thrown.expectMessage(containsString(path.build() + " not found in A&AI"));
    // AAIResultWrapper result = client.get(path, NotFoundException.class);
    // }

    // @Test
    // public void verifyFailedCallException() {
    // AAIResourceUri path = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf("test"));
    // wireMockRule.stubFor(get(urlPathEqualTo("/aai/" + AAIVersion.LATEST + path.build())).willReturn(aResponse()
    // .withHeader("Content-Type", "text/plain").withBodyFile("aai/error-message.json").withStatus(400)));
    // AAIResourcesClient client = aaiClient;

    // thrown.expect(BadRequestException.class);
    // thrown.expectMessage(containsString(
    // "Invalid input performing PUT on url (msg=Precondition Required:resource-version not passed for update of url"));
    // AAIResultWrapper result = client.get(path);
    // }

    // @Test
    // public void buildRelationshipTest() {
    // AAIResourcesClient client = aaiClient;
    // AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf("test"));
    // Relationship relationship = new Relationship();
    // relationship.setRelatedLink(uri.build().toString());
    // Relationship actual = client.buildRelationship(uri);
    // assertThat("expect equal no label", actual, sameBeanAs(relationship));

    // relationship.setRelationshipLabel(AAIEdgeLabel.USES.toString());
    // actual = client.buildRelationship(uri, AAIEdgeLabel.USES);
    // assertThat("expect equal has label", actual, sameBeanAs(relationship));

    // }

    // @Test
    // public void testGetOne() {
    // GenericVnf vnf = new GenericVnf();
    // vnf.setVnfId("my-vnf-id");
    // GenericVnfs vnfs = new GenericVnfs();
    // vnfs.getGenericVnf().add(vnf);
    // AAIPluralResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnfs());
    // RestClient restClientMock = mock(RestClient.class);
    // doReturn(restClientMock).when(client).createClient(uri);
    // when(restClientMock.get(GenericVnfs.class)).thenReturn(Optional.of(vnfs));

    // Optional<GenericVnf> result = aaiClient.getOne(GenericVnfs.class, GenericVnf.class, uri);

    // assertEquals("my-vnf-id", result.get().getVnfId());
    // }

    // @Test
    // public void testGetOneMultipleResults() {
    // GenericVnf vnf = new GenericVnf();
    // vnf.setVnfId("my-vnf-id");
    // GenericVnf vnf2 = new GenericVnf();
    // vnf.setVnfId("my-vnf-id2");
    // GenericVnfs vnfs = new GenericVnfs();
    // vnfs.getGenericVnf().add(vnf);
    // vnfs.getGenericVnf().add(vnf2);
    // AAIPluralResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnfs());
    // RestClient restClientMock = mock(RestClient.class);
    // doReturn(restClientMock).when(client).createClient(uri);
    // when(restClientMock.get(GenericVnfs.class)).thenReturn(Optional.of(vnfs));

    // thrown.expect(GraphInventoryMultipleItemsException.class);
    // aaiClient.getOne(GenericVnfs.class, GenericVnf.class, uri);
    // }

    // @Test
    // public void testGetFirstMultipleResults() {
    // GenericVnf vnf = new GenericVnf();
    // vnf.setVnfId("my-vnf-id");
    // GenericVnf vnf2 = new GenericVnf();
    // vnf2.setVnfId("my-vnf-id2");
    // GenericVnfs vnfs = new GenericVnfs();
    // vnfs.getGenericVnf().add(vnf);
    // vnfs.getGenericVnf().add(vnf2);
    // AAIPluralResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnfs());
    // RestClient restClientMock = mock(RestClient.class);
    // doReturn(restClientMock).when(client).createClient(uri);
    // when(restClientMock.get(GenericVnfs.class)).thenReturn(Optional.of(vnfs));

    // Optional<GenericVnf> result = aaiClient.getFirst(GenericVnfs.class, GenericVnf.class, uri);

    // assertEquals("my-vnf-id", result.get().getVnfId());
    // }

    // @Test
    // public void testGetOneNoResults() {
    // GenericVnf vnf = new GenericVnf();
    // vnf.setVnfId("my-vnf-id");
    // GenericVnfs vnfs = new GenericVnfs();
    // vnfs.getGenericVnf().add(vnf);
    // AAIPluralResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnfs());
    // RestClient restClientMock = mock(RestClient.class);
    // doReturn(restClientMock).when(client).createClient(uri);
    // when(restClientMock.get(GenericVnfs.class)).thenReturn(Optional.empty());

    // Optional<GenericVnf> result = aaiClient.getOne(GenericVnfs.class, GenericVnf.class, uri);

    // assertFalse(result.isPresent());
    // }

    // @Test
    // public void testGetFirstNoResults() {
    // GenericVnf vnf = new GenericVnf();
    // vnf.setVnfId("my-vnf-id");
    // GenericVnfs vnfs = new GenericVnfs();
    // vnfs.getGenericVnf().add(vnf);
    // AAIPluralResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnfs());
    // RestClient restClientMock = mock(RestClient.class);
    // doReturn(restClientMock).when(client).createClient(uri);
    // when(restClientMock.get(GenericVnfs.class)).thenReturn(Optional.empty());

    // Optional<GenericVnf> result = aaiClient.getFirst(GenericVnfs.class, GenericVnf.class, uri);

    // assertFalse(result.isPresent());
    // }

    // @Test
    // public void testGetFirstWrongPluralClass() {
    // GenericVnf vnf = new GenericVnf();
    // AAIPluralResourceUri uri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnfs());
    // RestClient restClientMock = mock(RestClient.class);
    // doReturn(restClientMock).when(client).createClient(uri);
    // when(restClientMock.get(GenericVnf.class)).thenReturn(Optional.of(vnf));

    // Optional<GenericVnf> result = aaiClient.getFirst(GenericVnf.class, GenericVnf.class, uri);

    // assertFalse(result.isPresent());
    // }

}
