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
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.onap.so.client.RestTemplateConfig.CONFIGURABLE_REST_TEMPLATE;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import com.google.gson.Gson;
import java.net.URI;
import java.util.Optional;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.hamcrest.MockitoHamcrest;
import org.onap.aai.domain.yang.EsrSystemInfo;
import org.onap.aai.domain.yang.EsrSystemInfoList;
import org.onap.aai.domain.yang.EsrVnfm;
import org.onap.aai.domain.yang.EsrVnfmList;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.RelationshipData;
import org.onap.aai.domain.yang.RelationshipList;
import org.onap.so.adapters.vnfmadapter.extclients.SdcPackageProvider;
import org.onap.so.adapters.vnfmadapter.lcn.JSON;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse200;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse2001;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201.InstantiationStateEnum;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201Links;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201LinksSelf;
import org.onap.so.adapters.vnfmadapter.rest.exceptions.VnfmNotFoundException;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.AAIVersion;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.vnfmadapter.v1.model.CreateVnfRequest;
import org.onap.vnfmadapter.v1.model.CreateVnfResponse;
import org.onap.vnfmadapter.v1.model.DeleteVnfResponse;
import org.onap.vnfmadapter.v1.model.OperationEnum;
import org.onap.vnfmadapter.v1.model.OperationStateEnum;
import org.onap.vnfmadapter.v1.model.QueryJobResponse;
import org.onap.vnfmadapter.v1.model.Tenant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneOffset;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")

public class VnfmAdapterControllerTest {

    private static final OffsetDateTime JAN_1_2019_12_00 =
            OffsetDateTime.of(LocalDateTime.of(2019, 1, 1, 12, 0), ZoneOffset.UTC);
    private static final OffsetDateTime JAN_1_2019_1_00 =
            OffsetDateTime.of(LocalDateTime.of(2019, 1, 1, 1, 0), ZoneOffset.UTC);
    private static final String CLOUD_OWNER = "myTestCloudOwner";
    private static final String REGION = "myTestRegion";
    private static final String TENANT_ID = "myTestTenantId";

    @LocalServerPort
    private int port;
    @Autowired
    @Qualifier(CONFIGURABLE_REST_TEMPLATE)
    private RestTemplate testRestTemplate;
    private MockRestServiceServer mockRestServer;

    @MockBean
    AAIResourcesClient aaiResourcesClient;

    @MockBean
    SdcPackageProvider sdcPackageProvider;

    @Autowired
    VnfmAdapterController controller;
    Gson gson = new JSON().getGson();

    @Before
    public void setUp() throws Exception {
        mockRestServer = MockRestServiceServer.bindTo(testRestTemplate).build();
    }

    @Test
    public void createVnf_ValidRequest_Returns202AndJobId() throws Exception {
        final Tenant tenant = new Tenant().cloudOwner(CLOUD_OWNER).regionName(REGION).tenantId(TENANT_ID);
        final CreateVnfRequest createVnfRequest = new CreateVnfRequest().name("myTestName").tenant(tenant);

        setUpGenericVnfInMockAai("vnfmType2");
        setUpVnfmsInMockAai();
        setUpVimInMockAai();

        final String expectedsubscriptionRequest =
                "{\"filter\":{\"vnfInstanceSubscriptionFilter\":{\"vnfInstanceIds\":[\"vnfId\"]},\"notificationTypes\":[\"VnfLcmOperationOccurrenceNotification\"]},\"callbackUri\":\"https://so-vnfm-adapter.onap:30406/so/vnfm-adapter/v1/lcn/VnfLcmOperationOccurrenceNotification\",\"authentication\":{\"authType\":[\"OAUTH2_CLIENT_CREDENTIALS\", \"BASIC\", \"TLS_CERT\"],\"paramsOauth2ClientCredentials\":{\"clientId\":\"vnfm\",\"clientPassword\":\"password1$\",\"tokenEndpoint\":\"https://so-vnfm-adapter.onap:30406/oauth/token\"},\"paramsBasic\":{\"userName\":\"vnfm\",\"password\":\"password1$\"}}}";
        final InlineResponse2001 subscriptionResponse = new InlineResponse2001();

        final InlineResponse201 createResponse = createCreateResponse();
        mockRestServer.expect(requestTo("http://vnfm2:8080/vnf_instances"))
                .andRespond(withSuccess(gson.toJson(createResponse), MediaType.APPLICATION_JSON));

        mockRestServer.expect(requestTo("http://vnfm2:8080/subscriptions"))
                .andExpect(content().json(expectedsubscriptionRequest))
                .andRespond(withSuccess(gson.toJson(subscriptionResponse), MediaType.APPLICATION_JSON));

        mockRestServer.expect(requestTo("http://vnfm2:8080/vnf_instances/vnfId/instantiate"))
                .andRespond(withStatus(HttpStatus.ACCEPTED).contentType(MediaType.APPLICATION_JSON)
                        .location(new URI("http://vnfm2:8080/vnf_lcm_op_occs/123456")));

        final InlineResponse200 firstOperationQueryResponse = createOperationQueryResponse(
                org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse200.OperationEnum.INSTANTIATE,
                org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse200.OperationStateEnum.PROCESSING);
        mockRestServer.expect(requestTo("http://vnfm2:8080/vnf_lcm_op_occs/123456"))
                .andRespond(withSuccess(gson.toJson(firstOperationQueryResponse), MediaType.APPLICATION_JSON));

        final InlineResponse200 secondOperationQueryReponse = createOperationQueryResponse(
                org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse200.OperationEnum.INSTANTIATE,
                org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse200.OperationStateEnum.COMPLETED);
        mockRestServer.expect(requestTo("http://vnfm2:8080/vnf_lcm_op_occs/123456"))
                .andRespond(withSuccess(gson.toJson(secondOperationQueryReponse), MediaType.APPLICATION_JSON));

        // Invoke the create request

        final ResponseEntity<CreateVnfResponse> createVnfResponse =
                controller.vnfCreate("myTestVnfId", createVnfRequest, "asadas", "so", "1213");
        assertEquals(HttpStatus.ACCEPTED, createVnfResponse.getStatusCode());
        assertNotNull(createVnfResponse.getBody().getJobId());

        final ArgumentCaptor<GenericVnf> genericVnfArgument = ArgumentCaptor.forClass(GenericVnf.class);
        final ArgumentCaptor<AAIResourceUri> uriArgument = ArgumentCaptor.forClass(AAIResourceUri.class);

        verify(aaiResourcesClient).update(uriArgument.capture(), genericVnfArgument.capture());

        assertEquals("/network/generic-vnfs/generic-vnf/myTestVnfId", uriArgument.getValue().build().toString());

        assertEquals("myTestVnfId", genericVnfArgument.getValue().getVnfId());

        final ArgumentCaptor<AAIResourceUri> uriArgument1Connect = ArgumentCaptor.forClass(AAIResourceUri.class);
        final ArgumentCaptor<AAIResourceUri> uriArgument2Connect = ArgumentCaptor.forClass(AAIResourceUri.class);
        verify(aaiResourcesClient, timeout(1000)).connect(uriArgument1Connect.capture(), uriArgument2Connect.capture());
        assertEquals("/external-system/esr-vnfm-list/esr-vnfm/vnfm2",
                uriArgument1Connect.getAllValues().get(0).build().toString());
        assertEquals("/network/generic-vnfs/generic-vnf/myTestVnfId",
                uriArgument2Connect.getAllValues().get(0).build().toString());

        // check the job status

        final ResponseEntity<QueryJobResponse> firstJobQueryResponse =
                controller.jobQuery(createVnfResponse.getBody().getJobId(), "", "so", "1213");
        assertEquals(OperationEnum.INSTANTIATE, firstJobQueryResponse.getBody().getOperation());
        assertEquals(OperationStateEnum.PROCESSING, firstJobQueryResponse.getBody().getOperationState());
        assertEquals(JAN_1_2019_12_00, firstJobQueryResponse.getBody().getStartTime());
        assertEquals(JAN_1_2019_1_00, firstJobQueryResponse.getBody().getStateEnteredTime());

        final ResponseEntity<QueryJobResponse> secondJobQueryResponse =
                controller.jobQuery(createVnfResponse.getBody().getJobId(), "", "so", "1213");
        assertEquals(OperationEnum.INSTANTIATE, secondJobQueryResponse.getBody().getOperation());
        assertEquals(OperationStateEnum.COMPLETED, secondJobQueryResponse.getBody().getOperationState());
        assertEquals(JAN_1_2019_12_00, secondJobQueryResponse.getBody().getStartTime());
        assertEquals(JAN_1_2019_1_00, secondJobQueryResponse.getBody().getStateEnteredTime());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createVnf_VnfAlreadyExistsOnVnfm_ThrowsIllegalArgumentException() throws Exception {
        final Tenant tenant = new Tenant().cloudOwner(CLOUD_OWNER).regionName(REGION).tenantId(TENANT_ID);
        final CreateVnfRequest createVnfRequest = new CreateVnfRequest().name("myTestName").tenant(tenant);

        final GenericVnf genericVnf = setUpGenericVnfInMockAai("vnfmType1");
        addSelfLinkToGenericVnf(genericVnf);
        addRelationshipFromGenericVnfToVnfm(genericVnf, "vnfm1");
        setUpVnfmsInMockAai();

        final InlineResponse201 reponse = new InlineResponse201();
        mockRestServer.expect(requestTo(new URI("http://vnfm:8080/vnfs/myTestVnfIdOnVnfm")))
                .andRespond(withSuccess(gson.toJson(reponse), MediaType.APPLICATION_JSON));

        controller.vnfCreate("myTestVnfId", createVnfRequest, "asadas", "so", "1213");
    }

    @Test(expected = VnfmNotFoundException.class)
    public void createVnf_NoMatchingVnfmFound_ThrowsException() throws Exception {
        final Tenant tenant = new Tenant().cloudOwner(CLOUD_OWNER).regionName(REGION).tenantId(TENANT_ID);
        final CreateVnfRequest createVnfRequest = new CreateVnfRequest().name("myTestName").tenant(tenant);

        setUpGenericVnfInMockAai("anotherType");
        setUpVnfmsInMockAai();

        controller.vnfCreate("myTestVnfId", createVnfRequest, "asadas", "so", "1213");
    }

    @Test
    public void createVnf_VnfmAlreadyAssociatedWithVnf_Returns202AndJobId() throws Exception {
        final Tenant tenant = new Tenant().cloudOwner(CLOUD_OWNER).regionName(REGION).tenantId(TENANT_ID);
        final CreateVnfRequest createVnfRequest = new CreateVnfRequest().name("myTestName").tenant(tenant);

        final GenericVnf genericVnf = setUpGenericVnfInMockAai("vnfmType2");
        addRelationshipFromGenericVnfToVnfm(genericVnf, "vnfm2");
        setUpVnfmsInMockAai();
        setUpVimInMockAai();

        final InlineResponse201 createResponse = createCreateResponse();
        mockRestServer.expect(requestTo("http://vnfm2:8080/vnf_instances"))
                .andRespond(withSuccess(gson.toJson(createResponse), MediaType.APPLICATION_JSON));

        mockRestServer.expect(requestTo("http://vnfm2:8080/subscriptions")).andRespond(withBadRequest());

        mockRestServer.expect(requestTo("http://vnfm2:8080/vnf_instances/vnfId/instantiate"))
                .andRespond(withStatus(HttpStatus.ACCEPTED).contentType(MediaType.APPLICATION_JSON)
                        .location(new URI("http://vnfm2:8080/vnf_lcm_op_occs/123456")));

        final ResponseEntity<CreateVnfResponse> response =
                controller.vnfCreate("myTestVnfId", createVnfRequest, "asadas", "so", "1213");
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody().getJobId());
    }

    @Test
    @Ignore
    public void createVnf_UnauthorizedUser_Returns401() throws Exception {
        final TestRestTemplate restTemplateWrongPassword = new TestRestTemplate("test", "wrongPassword");
        final Tenant tenant = new Tenant().cloudOwner(CLOUD_OWNER).regionName(REGION).tenantId(TENANT_ID);
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

    @Test
    public void deleteVnf_ValidRequest_Returns202AndJobId() throws Exception {
        final TestRestTemplate restTemplate = new TestRestTemplate("test", "test");

        final GenericVnf genericVnf = setUpGenericVnfInMockAai("vnfmType1");
        addSelfLinkToGenericVnf(genericVnf);
        addRelationshipFromGenericVnfToVnfm(genericVnf, "vnfm1");
        setUpVnfmsInMockAai();

        mockRestServer.expect(requestTo("http://vnfm:8080/vnfs/myTestVnfIdOnVnfm/terminate"))
                .andRespond(withStatus(HttpStatus.ACCEPTED).contentType(MediaType.APPLICATION_JSON)
                        .location(new URI("http://vnfm1:8080/vnf_lcm_op_occs/1234567")));

        final InlineResponse200 firstOperationQueryResponse = createOperationQueryResponse(
                org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse200.OperationEnum.TERMINATE,
                org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse200.OperationStateEnum.PROCESSING);
        mockRestServer.expect(requestTo("http://vnfm1:8080/vnf_lcm_op_occs/1234567"))
                .andRespond(withSuccess(gson.toJson(firstOperationQueryResponse), MediaType.APPLICATION_JSON));

        final InlineResponse200 secondOperationQueryReponse = createOperationQueryResponse(
                org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse200.OperationEnum.TERMINATE,
                org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse200.OperationStateEnum.COMPLETED);
        mockRestServer.expect(requestTo("http://vnfm1:8080/vnf_lcm_op_occs/1234567"))
                .andRespond(withSuccess(gson.toJson(secondOperationQueryReponse), MediaType.APPLICATION_JSON));

        final RequestEntity<Void> request = RequestEntity
                .delete(new URI("http://localhost:" + port + "/so/vnfm-adapter/v1/vnfs/myTestVnfId"))
                .accept(MediaType.APPLICATION_JSON).header("X-ONAP-RequestId", "myRequestId")
                .header("X-ONAP-InvocationID", "myInvocationId").header("Content-Type", "application/json").build();
        final ResponseEntity<DeleteVnfResponse> deleteVnfResponse =
                restTemplate.exchange(request, DeleteVnfResponse.class);
        assertEquals(202, deleteVnfResponse.getStatusCode().value());
        assertNotNull(deleteVnfResponse.getBody().getJobId());

        final ResponseEntity<QueryJobResponse> firstJobQueryResponse =
                controller.jobQuery(deleteVnfResponse.getBody().getJobId(), "", "so", "1213");
        assertEquals(OperationEnum.TERMINATE, firstJobQueryResponse.getBody().getOperation());
        assertEquals(OperationStateEnum.PROCESSING, firstJobQueryResponse.getBody().getOperationState());
        assertEquals(JAN_1_2019_12_00, firstJobQueryResponse.getBody().getStartTime());
        assertEquals(JAN_1_2019_1_00, firstJobQueryResponse.getBody().getStateEnteredTime());

        final ResponseEntity<QueryJobResponse> secondJobQueryResponse =
                controller.jobQuery(deleteVnfResponse.getBody().getJobId(), "", "so", "1213");
        assertEquals(OperationEnum.TERMINATE, secondJobQueryResponse.getBody().getOperation());
        assertEquals(OperationStateEnum.PROCESSING, secondJobQueryResponse.getBody().getOperationState());
        assertEquals(JAN_1_2019_12_00, secondJobQueryResponse.getBody().getStartTime());
        assertEquals(JAN_1_2019_1_00, secondJobQueryResponse.getBody().getStateEnteredTime());
    }

    @Test
    public void deleteVnf_VnfAlreadyTerminated_Returns202AndJobId() throws Exception {
        final TestRestTemplate restTemplate = new TestRestTemplate("test", "test");

        final GenericVnf genericVnf = setUpGenericVnfInMockAai("vnfmType1");
        addSelfLinkToGenericVnf(genericVnf);
        addRelationshipFromGenericVnfToVnfm(genericVnf, "vnfm1");
        setUpVnfmsInMockAai();

        mockRestServer.expect(requestTo("http://vnfm:8080/vnfs/myTestVnfIdOnVnfm/terminate"))
                .andRespond(withStatus(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_JSON));

        final InlineResponse201 reponse = new InlineResponse201();
        reponse.setInstantiationState(InstantiationStateEnum.NOT_INSTANTIATED);
        mockRestServer.expect(requestTo(new URI("http://vnfm:8080/vnfs/myTestVnfIdOnVnfm")))
                .andRespond(withSuccess(gson.toJson(reponse), MediaType.APPLICATION_JSON));

        mockRestServer.expect(requestTo("http://vnfm:8080/vnfs/myTestVnfIdOnVnfm"))
                .andRespond(withStatus(HttpStatus.NO_CONTENT).contentType(MediaType.APPLICATION_JSON));

        final RequestEntity<Void> request = RequestEntity
                .delete(new URI("http://localhost:" + port + "/so/vnfm-adapter/v1/vnfs/myTestVnfId"))
                .accept(MediaType.APPLICATION_JSON).header("X-ONAP-RequestId", "myRequestId")
                .header("X-ONAP-InvocationID", "myInvocationId").header("Content-Type", "application/json").build();
        final ResponseEntity<DeleteVnfResponse> deleteVnfResponse =
                restTemplate.exchange(request, DeleteVnfResponse.class);
        assertEquals(202, deleteVnfResponse.getStatusCode().value());
        assertNotNull(deleteVnfResponse.getBody().getJobId());

        final ResponseEntity<QueryJobResponse> jobQueryResponse =
                controller.jobQuery(deleteVnfResponse.getBody().getJobId(), "", "so", "1213");
        assertEquals(OperationStateEnum.COMPLETED, jobQueryResponse.getBody().getOperationState());
    }

    @Test
    public void deleteVnf_GenericVnfNotFound_Returns404() throws Exception {
        final TestRestTemplate restTemplate = new TestRestTemplate("test", "test");

        final RequestEntity<Void> request = RequestEntity
                .delete(new URI("http://localhost:" + port + "/so/vnfm-adapter/v1/vnfs/myNonExistingVnfId"))
                .accept(MediaType.APPLICATION_JSON).header("X-ONAP-RequestId", "myRequestId")
                .header("X-ONAP-InvocationID", "myInvocationId").header("Content-Type", "application/json").build();
        final ResponseEntity<DeleteVnfResponse> deleteVnfResponse =
                restTemplate.exchange(request, DeleteVnfResponse.class);
        assertEquals(404, deleteVnfResponse.getStatusCode().value());
        assertNull(deleteVnfResponse.getBody().getJobId());
    }

    @Test
    public void deleteVnf_NoAssignedVnfm_Returns400() throws Exception {
        final TestRestTemplate restTemplate = new TestRestTemplate("test", "test");

        setUpGenericVnfInMockAai("vnfmType");

        final RequestEntity<Void> request = RequestEntity
                .delete(new URI("http://localhost:" + port + "/so/vnfm-adapter/v1/vnfs/myTestVnfId"))
                .accept(MediaType.APPLICATION_JSON).header("X-ONAP-RequestId", "myRequestId")
                .header("X-ONAP-InvocationID", "myInvocationId").header("Content-Type", "application/json").build();
        final ResponseEntity<DeleteVnfResponse> deleteVnfResponse =
                restTemplate.exchange(request, DeleteVnfResponse.class);
        assertEquals(400, deleteVnfResponse.getStatusCode().value());
        assertNull(deleteVnfResponse.getBody().getJobId());
    }

    @Test
    public void deleteVnf_ErrorStatusCodeFromVnfm_Returns500() throws Exception {
        final TestRestTemplate restTemplate = new TestRestTemplate("test", "test");

        final GenericVnf genericVnf = setUpGenericVnfInMockAai("vnfmType1");
        addSelfLinkToGenericVnf(genericVnf);
        addRelationshipFromGenericVnfToVnfm(genericVnf, "vnfm1");
        setUpVnfmsInMockAai();

        mockRestServer.expect(requestTo("http://vnfm:8080/vnfs/myTestVnfIdOnVnfm/terminate"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON));

        final RequestEntity<Void> request = RequestEntity
                .delete(new URI("http://localhost:" + port + "/so/vnfm-adapter/v1/vnfs/myTestVnfId"))
                .accept(MediaType.APPLICATION_JSON).header("X-ONAP-RequestId", "myRequestId")
                .header("X-ONAP-InvocationID", "myInvocationId").header("Content-Type", "application/json").build();
        final ResponseEntity<DeleteVnfResponse> deleteVnfResponse =
                restTemplate.exchange(request, DeleteVnfResponse.class);
        assertEquals(500, deleteVnfResponse.getStatusCode().value());
        assertNull(deleteVnfResponse.getBody().getJobId());

    }

    private InlineResponse200 createOperationQueryResponse(
            final org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse200.OperationEnum operation,
            final org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse200.OperationStateEnum operationState) {
        final InlineResponse200 response = new InlineResponse200();
        response.setId("9876");
        response.setOperation(operation);
        response.setOperationState(operationState);
        response.setStartTime(JAN_1_2019_12_00);
        response.setStateEnteredTime(JAN_1_2019_1_00);
        response.setVnfInstanceId("myVnfInstanceId");
        return response;
    }

    private GenericVnf createGenericVnf(final String type) {
        final GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId("myTestVnfId");
        genericVnf.setNfType(type);
        return genericVnf;
    }

    private GenericVnf setUpGenericVnfInMockAai(final String type) {
        final GenericVnf genericVnf = createGenericVnf(type);

        doReturn(Optional.of(genericVnf)).when(aaiResourcesClient).get(eq(GenericVnf.class),
                MockitoHamcrest.argThat(new AaiResourceUriMatcher("/network/generic-vnfs/generic-vnf/myTestVnfId")));
        return genericVnf;
    }

    private void addSelfLinkToGenericVnf(final GenericVnf vnf) {
        vnf.setSelflink("http://vnfm:8080/vnfs/myTestVnfIdOnVnfm");
    }

    private void addRelationshipFromGenericVnfToVnfm(final GenericVnf genericVnf, final String vnfmId) {
        final Relationship relationshipToVnfm = new Relationship();
        relationshipToVnfm
                .setRelatedLink("/aai/" + AAIVersion.LATEST + "/external-system/esr-vnfm-list/esr-vnfm/" + vnfmId);
        relationshipToVnfm.setRelatedTo("esr-vnfm");
        final RelationshipData relationshipData = new RelationshipData();
        relationshipData.setRelationshipKey("esr-vnfm.vnfm-id");
        relationshipData.setRelationshipValue(vnfmId);
        relationshipToVnfm.getRelationshipData().add(relationshipData);

        final RelationshipList relationshipList = new RelationshipList();
        relationshipList.getRelationship().add(relationshipToVnfm);
        genericVnf.setRelationshipList(relationshipList);
    }

    private void setUpVnfmsInMockAai() {
        final EsrSystemInfo esrSystemInfo1 = new EsrSystemInfo();
        esrSystemInfo1.setServiceUrl("http://vnfm1:8080");
        esrSystemInfo1.setType("vnfmType1");
        esrSystemInfo1.setSystemType("VNFM");
        final EsrSystemInfoList esrSystemInfoList1 = new EsrSystemInfoList();
        esrSystemInfoList1.getEsrSystemInfo().add(esrSystemInfo1);

        final EsrVnfm esrVnfm1 = new EsrVnfm();
        esrVnfm1.setVnfmId("vnfm1");
        esrVnfm1.setEsrSystemInfoList(esrSystemInfoList1);
        esrVnfm1.setResourceVersion("1234");

        final EsrSystemInfo esrSystemInfo2 = new EsrSystemInfo();
        esrSystemInfo2.setServiceUrl("http://vnfm2:8080");
        esrSystemInfo2.setType("vnfmType2");
        esrSystemInfo2.setSystemType("VNFM");
        final EsrSystemInfoList esrSystemInfoList2 = new EsrSystemInfoList();
        esrSystemInfoList2.getEsrSystemInfo().add(esrSystemInfo2);

        final EsrVnfm esrVnfm2 = new EsrVnfm();
        esrVnfm2.setVnfmId("vnfm2");
        esrVnfm2.setEsrSystemInfoList(esrSystemInfoList2);
        esrVnfm2.setResourceVersion("1234");

        final EsrVnfmList esrVnfmList = new EsrVnfmList();
        esrVnfmList.getEsrVnfm().add(esrVnfm1);
        esrVnfmList.getEsrVnfm().add(esrVnfm2);

        doReturn(Optional.of(esrVnfm1)).when(aaiResourcesClient).get(eq(EsrVnfm.class), MockitoHamcrest
                .argThat(new AaiResourceUriMatcher("/external-system/esr-vnfm-list/esr-vnfm/vnfm1?depth=1")));

        doReturn(Optional.of(esrVnfm2)).when(aaiResourcesClient).get(eq(EsrVnfm.class), MockitoHamcrest
                .argThat(new AaiResourceUriMatcher("/external-system/esr-vnfm-list/esr-vnfm/vnfm2?depth=1")));

        doReturn(Optional.of(esrVnfmList)).when(aaiResourcesClient).get(eq(EsrVnfmList.class),
                MockitoHamcrest.argThat(new AaiResourceUriMatcher("/external-system/esr-vnfm-list")));

        doReturn(Optional.of(esrSystemInfoList1)).when(aaiResourcesClient).get(eq(EsrSystemInfoList.class),
                MockitoHamcrest.argThat(new AaiResourceUriMatcher(
                        "/external-system/esr-vnfm-list/esr-vnfm/vnfm1/esr-system-info-list")));
        doReturn(Optional.of(esrSystemInfoList2)).when(aaiResourcesClient).get(eq(EsrSystemInfoList.class),
                MockitoHamcrest.argThat(new AaiResourceUriMatcher(
                        "/external-system/esr-vnfm-list/esr-vnfm/vnfm2/esr-system-info-list")));
    }

    private void setUpVimInMockAai() {
        final EsrSystemInfo esrSystemInfo = new EsrSystemInfo();
        esrSystemInfo.setServiceUrl("http://myVim:8080");
        esrSystemInfo.setType("openstack");
        esrSystemInfo.setSystemType("VIM");
        esrSystemInfo.setCloudDomain("myDomain");
        esrSystemInfo.setUserName("myUser");
        esrSystemInfo.setPassword("myPassword");

        final EsrSystemInfoList esrSystemInfoList = new EsrSystemInfoList();
        esrSystemInfoList.getEsrSystemInfo().add(esrSystemInfo);

        doReturn(Optional.of(esrSystemInfoList)).when(aaiResourcesClient).get(eq(EsrSystemInfoList.class),
                MockitoHamcrest.argThat(new AaiResourceUriMatcher("/cloud-infrastructure/cloud-regions/cloud-region/"
                        + CLOUD_OWNER + "/" + REGION + "/esr-system-info-list")));
    }

    private InlineResponse201 createCreateResponse() {
        final InlineResponse201 createResponse = new InlineResponse201();
        createResponse.setVnfdId("myTestVnfd");
        final InlineResponse201Links links = new InlineResponse201Links();
        final InlineResponse201LinksSelf self = new InlineResponse201LinksSelf();
        self.setHref("http://vnfm2:8080/vnf_instances/vnfId");
        links.setSelf(self);
        createResponse.setLinks(links);
        createResponse.setId("vnfId");
        return createResponse;
    }


    private class AaiResourceUriMatcher extends BaseMatcher<AAIResourceUri> {

        final String uriAsString;

        public AaiResourceUriMatcher(final String uriAsString) {
            this.uriAsString = uriAsString;
        }

        @Override
        public boolean matches(final Object item) {
            if (item instanceof AAIResourceUri) {
                if (uriAsString.endsWith("...")) {
                    return ((AAIResourceUri) item).build().toString()
                            .startsWith(uriAsString.substring(0, uriAsString.indexOf("...")));
                }
                return ((AAIResourceUri) item).build().toString().equals(uriAsString);
            }
            return false;
        }

        @Override
        public void describeTo(final Description description) {}

    }

}
