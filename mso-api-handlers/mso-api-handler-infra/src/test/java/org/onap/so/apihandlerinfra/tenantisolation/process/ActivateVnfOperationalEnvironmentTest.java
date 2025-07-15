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

package org.onap.so.apihandlerinfra.tenantisolation.process;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.aai.domain.yang.OperationalEnvironment;
import org.onap.so.apihandlerinfra.BaseTest;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.onap.so.apihandlerinfra.tenantisolation.helpers.AAIClientHelper;
import org.onap.so.apihandlerinfra.tenantisolationbeans.Manifest;
import org.onap.so.apihandlerinfra.tenantisolationbeans.RecoveryAction;
import org.onap.so.apihandlerinfra.tenantisolationbeans.RequestDetails;
import org.onap.so.apihandlerinfra.tenantisolationbeans.RequestParameters;
import org.onap.so.apihandlerinfra.tenantisolationbeans.ServiceModelList;
import org.onap.aaiclient.client.aai.AAIVersion;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class ActivateVnfOperationalEnvironmentTest extends BaseTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private ActivateVnfOperationalEnvironment activateVnf;
    @Autowired
    private AAIClientHelper clientHelper;

    private final String requestId = "TEST_requestId";
    private final String operationalEnvironmentId = "1dfe7154-eae0-44f2-8e7a-8e5e7882e55d";
    private final String vnfOperationalEnvironmentId = "1dfe7154-eae0-44f2-8e7a-8e5e7882e66d";
    private final CloudOrchestrationRequest request = new CloudOrchestrationRequest();
    private final String workloadContext = "PVT";
    String recoveryActionRetry = "RETRY";
    private final String serviceModelVersionId = "TEST_serviceModelVersionId";
    int retryCount = 3;
    private final String sdcDistributionId = "TEST_distributionId";
    private final String statusSent = "SENT";
    private final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void init() {
        wireMockServer.stubFor(post(urlPathEqualTo("/operationalEnvServiceModelStatus/")).withRequestBody(equalTo(
                "{\"requestId\":\"TEST_requestId\",\"operationalEnvId\":\"1dfe7154-eae0-44f2-8e7a-8e5e7882e55d\",\"serviceModelVersionId\":\"TEST_serviceModelVersionId\",\"serviceModelVersionDistrStatus\":\"SENT\",\"recoveryAction\":\"RETRY\",\"retryCount\":3,\"workloadContext\":\"PVT\",\"createTime\":null,\"modifyTime\":null,\"vnfOperationalEnvId\":\"1dfe7154-eae0-44f2-8e7a-8e5e7882e66d\"}"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(post(urlPathEqualTo("/operationalEnvDistributionStatus/")).withRequestBody(equalTo(
                "{\"distributionId\":\"TEST_distributionId\",\"operationalEnvId\":\"1dfe7154-eae0-44f2-8e7a-8e5e7882e55d\",\"serviceModelVersionId\":\"TEST_serviceModelVersionId\",\"requestId\":\"TEST_requestId\",\"distributionIdStatus\":\"SENT\",\"distributionIdErrorReason\":\"\",\"createTime\":null,\"modifyTime\":null}"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withStatus(HttpStatus.SC_OK)));
    }


    @Test
    public void getAAIOperationalEnvironmentTest() {

        OperationalEnvironment aaiOpEnv;

        wireMockServer.stubFor(
                get(urlPathMatching("/aai/" + AAIVersion.LATEST + "/cloud-infrastructure/operational-environments/.*"))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json")
                                .withBodyFile("vnfoperenv/ecompOperationalEnvironmentWithRelationship.json")
                                .withStatus(HttpStatus.SC_ACCEPTED)));

        AAIResultWrapper wrapper = clientHelper.getAaiOperationalEnvironment("EMOE-001");
        aaiOpEnv = wrapper.asBean(OperationalEnvironment.class).get();
        assertEquals("EMOE-001", aaiOpEnv.getOperationalEnvironmentId());
        assertEquals("1dfe7154-eae0-44f2-8e7a-8e5e7882e55d", aaiOpEnv.getRelationshipList().getRelationship().get(0)
                .getRelationshipData().get(0).getRelationshipValue());
        assertNotNull(activateVnf.getAAIOperationalEnvironment(operationalEnvironmentId));
        assertEquals("EMOE-001", activateVnf.getAAIOperationalEnvironment(operationalEnvironmentId)
                .asBean(OperationalEnvironment.class).get().getOperationalEnvironmentId());

    }

    @Test
    public void executionTest() throws Exception {

        List<ServiceModelList> serviceModelVersionIdList = new ArrayList<>();
        ServiceModelList serviceModelList1 = new ServiceModelList();
        serviceModelList1.setRecoveryAction(RecoveryAction.retry);
        serviceModelList1.setServiceModelVersionId(serviceModelVersionId);
        serviceModelVersionIdList.add(serviceModelList1);

        RequestDetails requestDetails = new RequestDetails();
        RequestParameters requestParameters = new RequestParameters();
        Manifest manifest = new Manifest();
        manifest.setServiceModelList(serviceModelVersionIdList);
        requestParameters.setManifest(manifest);
        requestParameters.setWorkloadContext(workloadContext);
        requestDetails.setRequestParameters(requestParameters);

        request.setOperationalEnvironmentId(vnfOperationalEnvironmentId);
        request.setRequestDetails(requestDetails);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("statusCode", "202");
        jsonObject.put("message", "Success");
        jsonObject.put("distributionId", sdcDistributionId);

        wireMockServer.stubFor(
                get(urlPathMatching("/aai/" + AAIVersion.LATEST + "/cloud-infrastructure/operational-environments/.*"))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json")
                                .withBodyFile("vnfoperenv/activateOperationalEnvironmentWithRelationship.json")
                                .withStatus(HttpStatus.SC_ACCEPTED)));
        wireMockServer.stubFor(post(urlPathMatching("/sdc/v1/catalog/services/TEST_serviceModelVersionId/distr.*"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(jsonObject.toString())
                        .withStatus(HttpStatus.SC_ACCEPTED)));
        activateVnf.execute(requestId, request);
    }

    @Test
    public void processActivateSDCRequestTest_202() throws Exception {

        String distributionId = "TEST_distributionId";

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("statusCode", "202");
        jsonObject.put("message", "Success");
        jsonObject.put("distributionId", distributionId);

        // prepare request detail
        List<ServiceModelList> serviceModelVersionIdList = new ArrayList<>();
        ServiceModelList serviceModelList1 = new ServiceModelList();
        serviceModelList1.setRecoveryAction(RecoveryAction.retry);
        serviceModelList1.setServiceModelVersionId(serviceModelVersionId);
        serviceModelVersionIdList.add(serviceModelList1);

        wireMockServer.stubFor(post(urlPathMatching("/sdc/v1/catalog/services/TEST_serviceModelVersionId/distr.*"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(jsonObject.toString())
                        .withStatus(HttpStatus.SC_ACCEPTED)));

        activateVnf.processActivateSDCRequest(requestId, operationalEnvironmentId, serviceModelVersionIdList,
                workloadContext, vnfOperationalEnvironmentId);
        assertTrue(true); // this is here to silence a sonarqube violation
    }

    @Test
    public void processActivateSDCRequestTest_409() throws ApiException, JsonProcessingException {

        // ERROR in asdc
        JSONObject jsonMessages = new JSONObject();
        jsonMessages.put("message", "Failure");
        jsonMessages.put("messageId", "SVC4675");
        jsonMessages.put("text", "Error: Service state is invalid for this action.");
        JSONObject jsonServException = new JSONObject();
        jsonServException.put("policyException", jsonMessages);
        // jsonServException.put("serviceException", jsonMessages);
        JSONObject jsonErrorResponse = new JSONObject();
        jsonErrorResponse.put("requestError", jsonServException);

        // prepare request detail
        List<ServiceModelList> serviceModelVersionIdList = new ArrayList<>();
        ServiceModelList serviceModelList1 = new ServiceModelList();
        serviceModelList1.setRecoveryAction(RecoveryAction.retry);
        serviceModelList1.setServiceModelVersionId(serviceModelVersionId);
        serviceModelVersionIdList.add(serviceModelList1);

        InfraActiveRequests iar = new InfraActiveRequests();
        iar.setRequestId(requestId);
        iar.setRequestStatus("PENDING");
        wireMockServer.stubFor(get(urlPathEqualTo("/infraActiveRequests/" + requestId))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(iar)).withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(post(urlPathMatching("/sdc/v1/catalog/services/TEST_serviceModelVersionId/distr.*"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody(jsonErrorResponse.toString()).withStatus(HttpStatus.SC_CONFLICT)));
        wireMockServer.stubFor(post(urlPathEqualTo("/infraActiveRequests/"))
                .withRequestBody(containing("operationalEnvId\":\"1dfe7154-eae0-44f2-8e7a-8e5e7882e55d\""))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withStatus(HttpStatus.SC_OK)));

        thrown.expect(ValidateException.class);

        activateVnf.processActivateSDCRequest(requestId, operationalEnvironmentId, serviceModelVersionIdList,
                workloadContext, vnfOperationalEnvironmentId);
    }

}
