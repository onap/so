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
package org.onap.so.db.catalog.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.so.db.catalog.beans.*;
import org.onap.so.rest.catalog.beans.Vnf;
// import org.onap.so.db.catalog.beans.OrchestrationFlow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

public class CatalogDbClientTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CatalogDbClient catalogDbClient;

    @Value("${mso.catalog.db.spring.endpoint:#{null}}")
    private String endpoint;

    @Value("${mso.db.auth:#{null}}")
    private String msoAdaptersAuth;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        catalogDbClient.init();
    }

    @Test
    public void testGetServiceByID() {
        String modelUUID = "test-model-uuid";
        Service expectedService = new Service();
        expectedService.setModelUUID(modelUUID);

        WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec<?> requestHeadersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpecMock = mock(WebClient.ResponseSpec.class);
        // when(webClient.get()).thenReturn((RequestHeadersUriSpec<?>) requestHeadersUriSpecMock);
        // when(requestHeadersUriSpecMock.uri(any(URI.class))).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(Service.class)).thenReturn(Mono.just(expectedService));

        Service actualService = catalogDbClient.getServiceByID(modelUUID);
        assertEquals(expectedService, actualService);
    }

    @Test
    public void testGetVnfResourceByModelUUID() {
        String modelUUID = "test-model-uuid";
        VnfResource expectedVnfResource = new VnfResource();
        expectedVnfResource.setModelUUID(modelUUID);

        WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec<?> requestHeadersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpecMock = mock(WebClient.ResponseSpec.class);
        // when(webClient.get()).thenReturn(requestHeadersUriSpecMock);
        // when(requestHeadersUriSpecMock.uri(any(URI.class))).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(VnfResource.class)).thenReturn(Mono.just(expectedVnfResource));

        VnfResource actualVnfResource = catalogDbClient.getVnfResourceByModelUUID(modelUUID);
        assertEquals(expectedVnfResource, actualVnfResource);
    }

    @Test
    public void testGetNetworkResourceByModelName() {
        String networkType = "test-network-type";
        NetworkResource expectedNetworkResource = new NetworkResource();

        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
                eq(NetworkResource.class))).thenReturn(new ResponseEntity<>(expectedNetworkResource, HttpStatus.OK));

        NetworkResource actualNetworkResource = catalogDbClient.getNetworkResourceByModelName(networkType);
        assertEquals(expectedNetworkResource, actualNetworkResource);
    }

    @Test
    public void testGetBuildingBlockDetail() {
        String buildingBlockName = "test-building-block-name";
        BuildingBlockDetail expectedBuildingBlockDetail = new BuildingBlockDetail();
        expectedBuildingBlockDetail.setBuildingBlockName(buildingBlockName);

        WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec<?> requestHeadersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpecMock = mock(WebClient.ResponseSpec.class);
        // when(webClient.get()).thenReturn(requestHeadersUriSpecMock);
        // when(requestHeadersUriSpecMock.uri(any(URI.class))).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(BuildingBlockDetail.class)).thenReturn(Mono.just(expectedBuildingBlockDetail));

        BuildingBlockDetail actualBuildingBlockDetail = catalogDbClient.getBuildingBlockDetail(buildingBlockName);
        assertEquals(expectedBuildingBlockDetail, actualBuildingBlockDetail);
    }

    @Test
    public void testGetOrchestrationFlowByAction() {
        String action = "test-action";
        // List<OrchestrationFlow> expectedOrchestrationFlows = List.of(new OrchestrationFlow());

        WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec<?> requestHeadersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpecMock = mock(WebClient.ResponseSpec.class);
        // when(webClient.get()).thenReturn(requestHeadersUriSpecMock);
        // when(requestHeadersUriSpecMock.uri(any(URI.class))).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
        // when(responseSpecMock.bodyToFlux(OrchestrationFlow.class)).thenReturn(Flux.fromIterable(expectedOrchestrationFlows));

        // List<OrchestrationFlow> actualOrchestrationFlows = catalogDbClient.getOrchestrationFlowByAction(action);
        // assertEquals(expectedOrchestrationFlows, actualOrchestrationFlows);
    }

    @Test
    public void testGetCloudSite() {
        String id = "test-id";
        CloudSite expectedCloudSite = new CloudSite();

        WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec<?> requestHeadersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpecMock = mock(WebClient.ResponseSpec.class);
        // when(webClient.get()).thenReturn(requestHeadersUriSpecMock);
        // when(requestHeadersUriSpecMock.uri(any(URI.class))).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(CloudSite.class)).thenReturn(Mono.just(expectedCloudSite));

        CloudSite actualCloudSite = catalogDbClient.getCloudSite(id);
        assertEquals(expectedCloudSite, actualCloudSite);
    }

    @Test
    public void testPostCloudSite() {
        CloudSite cloudSite = new CloudSite();
        CloudSite expectedCloudSite = new CloudSite();

        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(CloudSite.class)))
                .thenReturn(new ResponseEntity<>(expectedCloudSite, HttpStatus.OK));

        CloudSite actualCloudSite = catalogDbClient.postCloudSite(cloudSite);
        assertEquals(expectedCloudSite, actualCloudSite);
    }

    @Test
    public void testUpdateCloudSite() {
        CloudSite cloudSite = new CloudSite();
        CloudSite expectedCloudSite = new CloudSite();

        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.PUT), any(HttpEntity.class), eq(CloudSite.class)))
                .thenReturn(new ResponseEntity<>(expectedCloudSite, HttpStatus.OK));

        CloudSite actualCloudSite = catalogDbClient.updateCloudSite(cloudSite);
        assertEquals(expectedCloudSite, actualCloudSite);
    }

    @Test
    public void testDeleteCloudSite() {
        String cloudSiteId = "test-cloud-site-id";

        doNothing().when(restTemplate).exchange(any(URI.class), eq(HttpMethod.DELETE), any(HttpEntity.class),
                eq(Void.class));

        assertDoesNotThrow(() -> catalogDbClient.deleteCloudSite(cloudSiteId));
    }

    @Test
    public void testGetCloudSites() {
        List<CloudSite> expectedCloudSites = List.of(new CloudSite());

        WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec<?> requestHeadersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpecMock = mock(WebClient.ResponseSpec.class);
        // when(webClient.get()).thenReturn(requestHeadersUriSpecMock);
        // when(requestHeadersUriSpecMock.uri(any(URI.class))).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToFlux(CloudSite.class)).thenReturn(Flux.fromIterable(expectedCloudSites));

        List<CloudSite> actualCloudSites = catalogDbClient.getCloudSites();
        assertEquals(expectedCloudSites, actualCloudSites);
    }
}
