/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.so.adapters.vnfmadapter.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.onap.so.client.RestTemplateConfig.CONFIGURABLE_REST_TEMPLATE;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import com.google.gson.Gson;
import java.net.URI;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.adapters.vnfmadapter.VnfmAdapterApplication;
import org.onap.so.adapters.vnfmadapter.extclients.aai.model.EsrSystemInfo;
import org.onap.so.adapters.vnfmadapter.extclients.aai.model.EsrVnfm;
import org.onap.so.adapters.vnfmadapter.extclients.aai.model.EsrVnfmList;
import org.onap.so.adapters.vnfmadapter.extclients.aai.model.EsrvnfmEsrsysteminfolist;
import org.onap.so.adapters.vnfmadapter.extclients.aai.model.GenericVnf;
import org.onap.so.adapters.vnfmadapter.extclients.aai.model.GenericvnfRelationshiplist;
import org.onap.so.adapters.vnfmadapter.extclients.aai.model.Relationship;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201;
import org.onap.so.adapters.vnfmadapter.rest.exceptions.VnfmNotFoundException;
import org.onap.vnfmadapter.v1.model.CreateVnfRequest;
import org.onap.vnfmadapter.v1.model.CreateVnfResponse;
import org.onap.vnfmadapter.v1.model.Tenant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = VnfmAdapterApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class VnfmAdapterControllerTest {

    @LocalServerPort
    private int port;
    @Autowired
    @Qualifier(CONFIGURABLE_REST_TEMPLATE)
    private RestTemplate testRestTemplate;
    private MockRestServiceServer mockRestServer;
    @Autowired
    VnfmAdapterController controller;
    Gson gson = new Gson();

    @Before
    public void setUp() throws Exception {
        mockRestServer = MockRestServiceServer.bindTo(testRestTemplate).build();
    }

    @Test
    public void createVnf_ValidRequest_Returns202AndJobId() throws Exception {
        final Tenant tenant =
                new Tenant().cloudOwner("myTestCloudOwner").regionName("myTestRegion").tenantId("myTestTenantId");
        final CreateVnfRequest createVnfRequest = new CreateVnfRequest().name("myTestName").tenant(tenant);

        final GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId("myTestVnfId");
        genericVnf.setNfType("vnfmType2");
        mockRestServer
                .expect(requestTo(
                        new URI("https://aai.onap:8443/aai/v15/network/generic-vnfs/generic-vnf/myTestVnfId")))
                .andRespond(withSuccess(gson.toJson(genericVnf), MediaType.APPLICATION_JSON));

        final EsrSystemInfo esrSystemInfo1 = new EsrSystemInfo();
        esrSystemInfo1.setServiceUrl("http://vnfm1:8080");
        esrSystemInfo1.setType("vnfmType1");
        esrSystemInfo1.setSystemType("VNFM");
        final EsrvnfmEsrsysteminfolist esrSystemInfoList1 =
                new EsrvnfmEsrsysteminfolist().esrSystemInfo(new ArrayList<>());
        esrSystemInfoList1.getEsrSystemInfo().add(esrSystemInfo1);

        final EsrVnfm esrVnfm1 = new EsrVnfm();
        esrVnfm1.setVnfmId("vnfm1");
        esrVnfm1.setEsrSystemInfoList(esrSystemInfoList1);
        esrVnfm1.setResourceVersion("1234");

        final EsrSystemInfo esrSystemInfo2 = new EsrSystemInfo();
        esrSystemInfo2.setServiceUrl("http://vnfm2:8080");
        esrSystemInfo2.setType("vnfmType2");
        esrSystemInfo2.setSystemType("VNFM");
        final EsrvnfmEsrsysteminfolist esrSystemInfoList2 =
                new EsrvnfmEsrsysteminfolist().esrSystemInfo(new ArrayList<>());
        esrSystemInfoList2.getEsrSystemInfo().add(esrSystemInfo2);

        final EsrVnfm esrVnfm2 = new EsrVnfm();
        esrVnfm2.setVnfmId("vnfm2");
        esrVnfm2.setEsrSystemInfoList(esrSystemInfoList2);
        esrVnfm2.setResourceVersion("1234");

        final EsrVnfmList esrVnfmList = new EsrVnfmList().esrVnfm(new ArrayList<>());
        esrVnfmList.getEsrVnfm().add(esrVnfm1);
        esrVnfmList.getEsrVnfm().add(esrVnfm2);

        mockRestServer.expect(requestTo(new URI("https://aai.onap:8443/aai/v15/external-system/esr-vnfm-list")))
                .andRespond(withSuccess(gson.toJson(esrVnfmList), MediaType.APPLICATION_JSON));

        mockRestServer.expect(requestTo(new URI(
                "https://aai.onap:8443/aai/v15/external-system/esr-vnfm-list/esr-vnfm/vnfm1/esr-system-info-list")))
                .andRespond(withSuccess(gson.toJson(esrSystemInfoList1), MediaType.APPLICATION_JSON));
        mockRestServer.expect(requestTo(new URI(
                "https://aai.onap:8443/aai/v15/external-system/esr-vnfm-list/esr-vnfm/vnfm2/esr-system-info-list")))
                .andRespond(withSuccess(gson.toJson(esrSystemInfoList2), MediaType.APPLICATION_JSON));


        final String expectedVnfId = "{\"vnf-id\": \"myTestVnfId\"}";
        final String expectedRelationship =
                "{\"relationship-list\":{\"relationship\":[{\"related-to\":\"esr-vnfm\",\"relationship-label\":\"tosca.relationships.DependsOn\",\"related-link\":\"//aai.onap:8443/aai/v15/external-system/esr-vnfm-list/esr-vnfm/vnfm2\",\"relationship-data\":[{\"relationship-key\":\"esr-vnfm.vnfm-id\",\"relationship-value\":\"vnfm2\"}]}]}}";
        mockRestServer
                .expect(requestTo(
                        new URI("https://aai.onap:8443/aai/v15/network/generic-vnfs/generic-vnf/myTestVnfId")))
                .andExpect(content().json(expectedVnfId)).andExpect(content().json(expectedRelationship))
                .andRespond(withSuccess());


        final ResponseEntity<CreateVnfResponse> response =
                controller.vnfCreate("myTestVnfId", createVnfRequest, "asadas", "so", "1213");
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody().getJobId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createVnf_VnfAlreadyExistsOnVnfm_ThrowsIllegalArgumentException() throws Exception {
        final Tenant tenant =
                new Tenant().cloudOwner("myTestCloudOwner").regionName("myTestRegion").tenantId("myTestTenantId");
        final CreateVnfRequest createVnfRequest = new CreateVnfRequest().name("myTestName").tenant(tenant);

        final GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId("myTestVnfId");
        genericVnf.setNfType("vnfmType");
        genericVnf.setSelflink("http://vnfm:8080/vnfs/myTestVnfIdOnVnfm");
        mockRestServer
                .expect(requestTo(
                        new URI("https://aai.onap:8443/aai/v15/network/generic-vnfs/generic-vnf/myTestVnfId")))
                .andRespond(withSuccess(gson.toJson(genericVnf), MediaType.APPLICATION_JSON));

        final EsrSystemInfo esrSystemInfo = new EsrSystemInfo();
        esrSystemInfo.setServiceUrl("http://vnfm:8080");
        esrSystemInfo.setType("vnfmType");
        esrSystemInfo.setSystemType("VNFM");
        final EsrvnfmEsrsysteminfolist esrSystemInfoList =
                new EsrvnfmEsrsysteminfolist().esrSystemInfo(new ArrayList<>());
        esrSystemInfoList.getEsrSystemInfo().add(esrSystemInfo);

        final EsrVnfm esrVnfm = new EsrVnfm();
        esrVnfm.setVnfmId("vnfm");
        esrVnfm.setEsrSystemInfoList(esrSystemInfoList);
        esrVnfm.setResourceVersion("1234");

        final EsrVnfmList esrVnfmList = new EsrVnfmList().esrVnfm(new ArrayList<>());
        esrVnfmList.getEsrVnfm().add(esrVnfm);

        final InlineResponse201 reponse = new InlineResponse201();
        mockRestServer.expect(requestTo(new URI("http://vnfm:8080/vnfs/myTestVnfIdOnVnfm")))
                .andRespond(withSuccess(gson.toJson(reponse), MediaType.APPLICATION_JSON));


        mockRestServer.expect(requestTo(new URI("https://aai.onap:8443/aai/v15/external-system/esr-vnfm-list")))
                .andRespond(withSuccess(gson.toJson(esrVnfmList), MediaType.APPLICATION_JSON));


        controller.vnfCreate("myTestVnfId", createVnfRequest, "asadas", "so", "1213");
    }

    @Test(expected = VnfmNotFoundException.class)
    public void createVnf_NoMatchingVnfmFound_ThrowsException() throws Exception {
        final Tenant tenant =
                new Tenant().cloudOwner("myTestCloudOwner").regionName("myTestRegion").tenantId("myTestTenantId");
        final CreateVnfRequest createVnfRequest = new CreateVnfRequest().name("myTestName").tenant(tenant);

        final GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId("myTestVnfId");
        genericVnf.setNfType("anotherType");
        mockRestServer
                .expect(requestTo(
                        new URI("https://aai.onap:8443/aai/v15/network/generic-vnfs/generic-vnf/myTestVnfId")))
                .andRespond(withSuccess(gson.toJson(genericVnf), MediaType.APPLICATION_JSON));

        final EsrSystemInfo esrSystemInfo1 = new EsrSystemInfo();
        esrSystemInfo1.setServiceUrl("http://vnfm1:8080");
        esrSystemInfo1.setType("vnfmType1");
        esrSystemInfo1.setSystemType("VNFM");
        final EsrvnfmEsrsysteminfolist esrSystemInfoList1 =
                new EsrvnfmEsrsysteminfolist().esrSystemInfo(new ArrayList<>());
        esrSystemInfoList1.getEsrSystemInfo().add(esrSystemInfo1);

        final EsrVnfm esrVnfm1 = new EsrVnfm();
        esrVnfm1.setVnfmId("vnfm1");
        esrVnfm1.setEsrSystemInfoList(esrSystemInfoList1);
        esrVnfm1.setResourceVersion("1234");

        final EsrSystemInfo esrSystemInfo2 = new EsrSystemInfo();
        esrSystemInfo2.setServiceUrl("http://vnfm2:8080");
        esrSystemInfo2.setType("vnfmType2");
        esrSystemInfo2.setSystemType("VNFM");
        final EsrvnfmEsrsysteminfolist esrSystemInfoList2 =
                new EsrvnfmEsrsysteminfolist().esrSystemInfo(new ArrayList<>());
        esrSystemInfoList2.getEsrSystemInfo().add(esrSystemInfo2);

        final EsrVnfm esrVnfm2 = new EsrVnfm();
        esrVnfm2.setVnfmId("vnfm2");
        esrVnfm2.setEsrSystemInfoList(esrSystemInfoList2);
        esrVnfm2.setResourceVersion("1234");

        final EsrVnfmList esrVnfmList = new EsrVnfmList().esrVnfm(new ArrayList<>());
        esrVnfmList.getEsrVnfm().add(esrVnfm1);
        esrVnfmList.getEsrVnfm().add(esrVnfm2);

        mockRestServer.expect(requestTo(new URI("https://aai.onap:8443/aai/v15/external-system/esr-vnfm-list")))
                .andRespond(withSuccess(gson.toJson(esrVnfmList), MediaType.APPLICATION_JSON));

        mockRestServer.expect(requestTo(new URI(
                "https://aai.onap:8443/aai/v15/external-system/esr-vnfm-list/esr-vnfm/vnfm1/esr-system-info-list")))
                .andRespond(withSuccess(gson.toJson(esrSystemInfoList1), MediaType.APPLICATION_JSON));
        mockRestServer.expect(requestTo(new URI(
                "https://aai.onap:8443/aai/v15/external-system/esr-vnfm-list/esr-vnfm/vnfm2/esr-system-info-list")))
                .andRespond(withSuccess(gson.toJson(esrSystemInfoList2), MediaType.APPLICATION_JSON));

        controller.vnfCreate("myTestVnfId", createVnfRequest, "asadas", "so", "1213");
    }

    @Test
    public void createVnf_VnfmAlreadyAssociatedWithVnf_Returns202AndJobId() throws Exception {
        final Tenant tenant =
                new Tenant().cloudOwner("myTestCloudOwner").regionName("myTestRegion").tenantId("myTestTenantId");
        final CreateVnfRequest createVnfRequest = new CreateVnfRequest().name("myTestName").tenant(tenant);

        final GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId("myTestVnfId");
        genericVnf.setNfType("vnfmType2");

        final Relationship relationshipToVnfm = new Relationship();
        relationshipToVnfm.setRelatedLink("/aai/v15/external-system/esr-vnfm-list/esr-vnfm/vnfm1");
        relationshipToVnfm.setRelatedTo("esr-vnfm");

        final GenericvnfRelationshiplist relationshipList = new GenericvnfRelationshiplist();
        relationshipList.addRelationshipItem(relationshipToVnfm);
        genericVnf.setRelationshipList(relationshipList);
        mockRestServer
                .expect(requestTo(
                        new URI("https://aai.onap:8443/aai/v15/network/generic-vnfs/generic-vnf/myTestVnfId")))
                .andRespond(withSuccess(gson.toJson(genericVnf), MediaType.APPLICATION_JSON));

        final EsrSystemInfo esrSystemInfo1 = new EsrSystemInfo();
        esrSystemInfo1.setServiceUrl("http://vnfm1:8080");
        esrSystemInfo1.setType("vnfmType1");
        esrSystemInfo1.setSystemType("VNFM");
        final EsrvnfmEsrsysteminfolist esrSystemInfoList1 =
                new EsrvnfmEsrsysteminfolist().esrSystemInfo(new ArrayList<>());
        esrSystemInfoList1.getEsrSystemInfo().add(esrSystemInfo1);

        final EsrVnfm esrVnfm1 = new EsrVnfm();
        esrVnfm1.setVnfmId("vnfm1");
        esrVnfm1.setEsrSystemInfoList(esrSystemInfoList1);
        esrVnfm1.setResourceVersion("1234");

        mockRestServer
                .expect(requestTo(
                        new URI("https://aai.onap:8443/aai/v15/external-system/esr-vnfm-list/esr-vnfm/vnfm1")))
                .andRespond(withSuccess(gson.toJson(esrVnfm1), MediaType.APPLICATION_JSON));

        final ResponseEntity<CreateVnfResponse> response =
                controller.vnfCreate("myTestVnfId", createVnfRequest, "asadas", "so", "1213");
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody().getJobId());
    }

    @Test
    public void createVnf_UnauthorizedUser_Returns401() throws Exception {
        final TestRestTemplate restTemplateWrongPassword = new TestRestTemplate("test", "wrongPassword");
        final Tenant tenant =
                new Tenant().cloudOwner("myTestCloudOwner").regionName("myTestRegion").tenantId("myTestTenantId");
        final CreateVnfRequest createVnfRequest = new CreateVnfRequest().name("myTestName").tenant(tenant);

        final RequestEntity<CreateVnfRequest> request =
                RequestEntity.post(new URI("http://localhost:" + port + "/so/vnfm-adapter/v1/vnfs/myVnfId"))
                        .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                        .header("X-ONAP-RequestId", "myRequestId").header("X-ONAP-InvocationID", "myInvocationId")
                        .body(createVnfRequest);
        final ResponseEntity<CreateVnfResponse> response =
                restTemplateWrongPassword.exchange(request, CreateVnfResponse.class);
        assertEquals(401, response.getStatusCode().value());
    }

}
