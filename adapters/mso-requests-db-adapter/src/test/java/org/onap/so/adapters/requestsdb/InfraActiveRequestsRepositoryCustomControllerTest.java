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

package org.onap.so.adapters.requestsdb;


import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.adapters.requestsdb.application.MSORequestDBApplication;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.data.controller.InstanceNameDuplicateCheckRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MSORequestDBApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
public class InfraActiveRequestsRepositoryCustomControllerTest {

    @LocalServerPort
    private int port;
    
    @Value("${mso.adapters.requestDb.auth}")
    private String msoAdaptersAuth;

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

    private InfraActiveRequests infraActiveRequests;
    private InfraActiveRequests infraActiveRequestsResponse;
    private HttpHeaders headers;
    private TestRestTemplate restTemplate;

    private void verifyInfraActiveRequests() {
        assertEquals(infraActiveRequests.getRequestId(), infraActiveRequestsResponse.getRequestId());
        assertEquals(infraActiveRequests.getServiceInstanceId(), infraActiveRequestsResponse.getServiceInstanceId());
        assertEquals(infraActiveRequests.getServiceInstanceName(), infraActiveRequestsResponse.getServiceInstanceName());
        assertEquals(infraActiveRequests.getVnfId(), infraActiveRequestsResponse.getVnfId());
        assertEquals(infraActiveRequests.getVnfName(), infraActiveRequestsResponse.getVnfName());
        assertEquals(infraActiveRequests.getVfModuleId(), infraActiveRequestsResponse.getVfModuleId());
        assertEquals(infraActiveRequests.getVfModuleName(), infraActiveRequestsResponse.getVfModuleName());
        assertEquals(infraActiveRequests.getVolumeGroupId(), infraActiveRequestsResponse.getVolumeGroupId());
        assertEquals(infraActiveRequests.getVolumeGroupName(), infraActiveRequestsResponse.getVolumeGroupName());
        assertEquals(infraActiveRequests.getNetworkId(), infraActiveRequestsResponse.getNetworkId());
        assertEquals(infraActiveRequests.getNetworkName(), infraActiveRequestsResponse.getNetworkName());
        assertEquals(infraActiveRequests.getConfigurationId(), infraActiveRequestsResponse.getConfigurationId());
        assertEquals(infraActiveRequests.getConfigurationName(), infraActiveRequestsResponse.getConfigurationName());
        assertEquals(infraActiveRequests.getAaiServiceId(), infraActiveRequestsResponse.getAaiServiceId());
        assertEquals(infraActiveRequests.getTenantId(), infraActiveRequestsResponse.getTenantId());
        assertEquals(infraActiveRequests.getRequestScope(), infraActiveRequestsResponse.getRequestScope());
        assertEquals(infraActiveRequests.getRequestorId(), infraActiveRequestsResponse.getRequestorId());
        assertEquals(infraActiveRequests.getSource(), infraActiveRequestsResponse.getSource());
        assertEquals(infraActiveRequests.getOperationalEnvId(), infraActiveRequestsResponse.getOperationalEnvId());
        assertEquals(infraActiveRequests.getOperationalEnvName(), infraActiveRequestsResponse.getOperationalEnvName());
        assertEquals(infraActiveRequests.getRequestStatus(), infraActiveRequestsResponse.getRequestStatus());
        assertEquals(infraActiveRequests.getAction(), infraActiveRequestsResponse.getAction());
    }

    @Before
    public void setup() {

        headers = new HttpHeaders();
        restTemplate = new TestRestTemplate("test", "test");

        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);
        headers.set("Authorization", msoAdaptersAuth);

        infraActiveRequests = new InfraActiveRequests();

        infraActiveRequests.setRequestId(UUID.randomUUID().toString());
        infraActiveRequests.setOperationalEnvId(UUID.randomUUID().toString());
        infraActiveRequests.setServiceInstanceId(UUID.randomUUID().toString());
        infraActiveRequests.setServiceInstanceName("serviceInstanceNameTest");
        infraActiveRequests.setVnfId(UUID.randomUUID().toString());
        infraActiveRequests.setVnfName("vnfInstanceNameTest");
        infraActiveRequests.setVfModuleId(UUID.randomUUID().toString());
        infraActiveRequests.setVfModuleName("vfModuleInstanceNameTest");
        infraActiveRequests.setVolumeGroupId(UUID.randomUUID().toString());
        infraActiveRequests.setVolumeGroupName("volumeGroupInstanceNameTest");
        infraActiveRequests.setNetworkId(UUID.randomUUID().toString());
        infraActiveRequests.setNetworkName("networkInstanceNameTest");
        infraActiveRequests.setConfigurationId(UUID.randomUUID().toString());
        infraActiveRequests.setConfigurationName("configurationInstanceNameTest");
        infraActiveRequests.setAicCloudRegion("1");
        infraActiveRequests.setTenantId(UUID.randomUUID().toString());
        infraActiveRequests.setRequestScope("operationalEnvironment");
        infraActiveRequests.setRequestorId(UUID.randomUUID().toString());
        infraActiveRequests.setSource("sourceTest");
        infraActiveRequests.setOperationalEnvName(UUID.randomUUID().toString());
        infraActiveRequests.setRequestStatus("IN_PROGRESS");
        infraActiveRequests.setAction("create");

        HttpEntity<String> entity = new HttpEntity(infraActiveRequests, headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort("/infraActiveRequests"));
        ResponseEntity<String> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.POST, entity, String.class);

        assertEquals(201, response.getStatusCodeValue());
    }


    @Test
    public void getCloudOrchestrationFiltersFromInfraActiveTest() {

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("operationalEnvironmentId", infraActiveRequests.getOperationalEnvId());
        requestMap.put("operationalEnvironmentName", infraActiveRequests.getOperationalEnvName());
        requestMap.put("resourceType", "operationalEnvironment");

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestMap, headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort("/infraActiveRequests") + "/getCloudOrchestrationFiltersFromInfraActive");

        ResponseEntity<List<InfraActiveRequests>> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.POST, entity, new ParameterizedTypeReference<List<InfraActiveRequests>>() {
                });

        List<InfraActiveRequests> iarr = response.getBody();
        assertEquals(200, response.getStatusCodeValue());

        assertTrue(iarr.size() == 1);
        infraActiveRequestsResponse = iarr.get(0);

        verifyInfraActiveRequests();

    }

    @Test
    public void getOrchestrationFiltersFromInfraActiveTest() {

        Map<String, List<String>> requestMap = new HashMap<>();
        List<String> values = new ArrayList<>();
        values.add("EQUALS");
        values.add(infraActiveRequests.getServiceInstanceId());
        requestMap.put("serviceInstanceId", values);

        values = new ArrayList<>();
        values.add("EQUALS");
        values.add(infraActiveRequests.getServiceInstanceName());
        requestMap.put("serviceInstanceName", values);

        HttpEntity<Map<String, List<String>>> entityList = new HttpEntity(requestMap, headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort("/infraActiveRequests") + "/getOrchestrationFiltersFromInfraActive");

        ResponseEntity<List<InfraActiveRequests>> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.POST, entityList, new ParameterizedTypeReference<List<InfraActiveRequests>>() {
                });

        List<InfraActiveRequests> iarr = response.getBody();

        assertEquals(200, response.getStatusCodeValue());

        assertTrue(iarr.size() == 1);
        infraActiveRequestsResponse = iarr.get(0);

        verifyInfraActiveRequests();
    }

    @Test
    public void checkVnfIdStatusTest() {

        
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort("/infraActiveRequests" + "/checkVnfIdStatus/" + infraActiveRequests.getOperationalEnvId()));
        HttpEntity<String> entity = new HttpEntity(HttpEntity.EMPTY, headers);

        ResponseEntity<InfraActiveRequests> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,entity , InfraActiveRequests.class);

        infraActiveRequestsResponse = response.getBody();

        assertEquals(200, response.getStatusCodeValue());

        verifyInfraActiveRequests();
    }

    @Test
    public void checkInstanceNameDuplicateTest() {

        InstanceNameDuplicateCheckRequest instanceNameDuplicateCheckRequest = new InstanceNameDuplicateCheckRequest((HashMap<String, String>) null,
                infraActiveRequests.getOperationalEnvName(),
                infraActiveRequests.getRequestScope());

        HttpEntity<InstanceNameDuplicateCheckRequest> entityList = new HttpEntity(instanceNameDuplicateCheckRequest, headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort("/infraActiveRequests") + "/checkInstanceNameDuplicate");

        ResponseEntity<InfraActiveRequests> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.POST, entityList, new ParameterizedTypeReference<InfraActiveRequests>() {
                });

        infraActiveRequestsResponse = response.getBody();

        assertEquals(200, response.getStatusCodeValue());

        verifyInfraActiveRequests();
    }

    @Test
    public void checkInstanceNameDuplicateViaTest() {

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("operationalEnvironmentId", infraActiveRequests.getOperationalEnvId());

        InstanceNameDuplicateCheckRequest instanceNameDuplicateCheckRequest = new InstanceNameDuplicateCheckRequest((HashMap<String, String>) requestMap,
                null,
                infraActiveRequests.getRequestScope());

        HttpEntity<InstanceNameDuplicateCheckRequest> entityList = new HttpEntity(instanceNameDuplicateCheckRequest, headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort("/infraActiveRequests") + "/checkInstanceNameDuplicate");

        ResponseEntity<InfraActiveRequests> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.POST, entityList, new ParameterizedTypeReference<InfraActiveRequests>() {
                });

        infraActiveRequestsResponse = response.getBody();

        assertEquals(200, response.getStatusCodeValue());

        verifyInfraActiveRequests();
    }
}
