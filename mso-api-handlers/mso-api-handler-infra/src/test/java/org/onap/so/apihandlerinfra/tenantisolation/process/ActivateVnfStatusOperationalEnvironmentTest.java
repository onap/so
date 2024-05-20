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
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.aaiclient.client.aai.AAIVersion;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandlerinfra.BaseTest;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.onap.so.apihandlerinfra.tenantisolationbeans.Distribution;
import org.onap.so.apihandlerinfra.tenantisolationbeans.DistributionStatus;
import org.onap.so.apihandlerinfra.tenantisolationbeans.Status;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.beans.OperationalEnvDistributionStatus;
import org.onap.so.db.request.beans.OperationalEnvServiceModelStatus;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ActivateVnfStatusOperationalEnvironmentTest extends BaseTest {

    @Autowired
    private ActivateVnfStatusOperationalEnvironment activateVnfStatus;
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final String requestId = "TEST_requestId";
    private final String requestIdOrig = "TEST_requestIdOrig";
    private final String operationalEnvironmentId = "TEST_operationalEnvironmentId";
    private final String vnfOperationalEnvironmentId = "VNF_operationalEnvironmentId";
    private final CloudOrchestrationRequest request = new CloudOrchestrationRequest();
    private final String workloadContext = "TEST_workloadContext";
    private final String recoveryActionRetry = "RETRY";
    private final String recoveryActionAbort = "ABORT";
    private final String recoveryActionSkip = "SKIP";
    private final String serviceModelVersionId = "TEST_serviceModelVersionId";
    private final String serviceModelVersionId1 = "TEST_serviceModelVersionId1";
    private final int retryCountThree = 3;
    private final int retryCountTwo = 2;
    private final int retryCountZero = 0;
    private final String sdcDistributionId1 = "TEST_distributionId1";
    private final String sdcDistributionId = "TEST_distributionId";
    private final String statusOk = Status.DISTRIBUTION_COMPLETE_OK.toString();
    private final String statusError = DistributionStatus.DISTRIBUTION_COMPLETE_ERROR.toString();
    private final String statusSent = "SENT";
    String json = "{\"operational-environment-status\" : \"INACTIVE\"}";

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void checkOrUpdateOverallStatusTest_Ok() throws Exception {

        // two entries, both status Ok & retry 0
        OperationalEnvServiceModelStatus serviceModelDb = new OperationalEnvServiceModelStatus();
        serviceModelDb.setRequestId(requestIdOrig);
        serviceModelDb.setServiceModelVersionId(serviceModelVersionId);
        serviceModelDb.setWorkloadContext(workloadContext);
        serviceModelDb.setRecoveryAction(recoveryActionRetry);
        serviceModelDb.setOperationalEnvId(operationalEnvironmentId);
        serviceModelDb.setRetryCount(retryCountZero);
        serviceModelDb.setServiceModelVersionDistrStatus(statusOk);
        serviceModelDb.setVnfOperationalEnvId(vnfOperationalEnvironmentId);

        serviceModelDb.setRequestId(requestIdOrig);
        serviceModelDb.setServiceModelVersionId(serviceModelVersionId1);
        serviceModelDb.setWorkloadContext(workloadContext);
        serviceModelDb.setRecoveryAction(recoveryActionRetry);
        serviceModelDb.setOperationalEnvId(operationalEnvironmentId);
        serviceModelDb.setRetryCount(retryCountZero);
        serviceModelDb.setServiceModelVersionDistrStatus(statusOk);
        serviceModelDb.setVnfOperationalEnvId(vnfOperationalEnvironmentId);

        InfraActiveRequests iar = new InfraActiveRequests();
        iar.setRequestId(requestIdOrig);
        iar.setRequestStatus("PENDING");

        wireMockServer.stubFor(get(urlPathEqualTo(
                "/operationalEnvServiceModelStatus/search/findOneByOperationalEnvIdAndServiceModelVersionIdAndRequestId"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(mapper.writeValueAsString(serviceModelDb)).withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(get(urlPathEqualTo("/infraActiveRequests/" + requestIdOrig))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(iar)).withStatus(HttpStatus.SC_OK)));

        activateVnfStatus.checkOrUpdateOverallStatus(operationalEnvironmentId, requestIdOrig);

        // overall is success
    }

    @Test
    public void checkOrUpdateOverallStatusTest_Error() throws JsonProcessingException {

        OperationalEnvServiceModelStatus serviceModelDb = new OperationalEnvServiceModelStatus();
        serviceModelDb.setRequestId(requestIdOrig);
        serviceModelDb.setServiceModelVersionId(serviceModelVersionId);
        serviceModelDb.setWorkloadContext(workloadContext);
        serviceModelDb.setRecoveryAction(recoveryActionRetry);
        serviceModelDb.setOperationalEnvId(operationalEnvironmentId);
        serviceModelDb.setRetryCount(retryCountZero);
        serviceModelDb.setServiceModelVersionDistrStatus(statusError);
        serviceModelDb.setVnfOperationalEnvId(vnfOperationalEnvironmentId);
        wireMockServer.stubFor(get(urlPathEqualTo(
                "/operationalEnvServiceModelStatus/search/findOneByOperationalEnvIdAndServiceModelVersionIdAndRequestId"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(mapper.writeValueAsString(serviceModelDb)).withStatus(HttpStatus.SC_OK)));

        InfraActiveRequests iar = new InfraActiveRequests();
        iar.setRequestId(requestIdOrig);
        iar.setRequestStatus("PENDING");

        wireMockServer.stubFor(get(urlPathEqualTo("/infraActiveRequests/" + requestIdOrig))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(iar)).withStatus(HttpStatus.SC_OK)));

        try {
            activateVnfStatus.checkOrUpdateOverallStatus(operationalEnvironmentId, requestIdOrig);
        } catch (ApiException e) {
            assertThat(e.getMessage(), startsWith("Overall Activation process is a Failure. "));
            assertEquals(e.getHttpResponseCode(), HttpStatus.SC_BAD_REQUEST);
            assertEquals(e.getMessageID(), ErrorNumbers.SVC_DETAILED_SERVICE_ERROR);
        }

    }

    @Test
    public void checkOrUpdateOverallStatusTest_Waiting() throws Exception {

        OperationalEnvServiceModelStatus serviceModelDb = new OperationalEnvServiceModelStatus();
        serviceModelDb.setRequestId(requestIdOrig);
        serviceModelDb.setServiceModelVersionId(serviceModelVersionId);
        serviceModelDb.setWorkloadContext(workloadContext);
        serviceModelDb.setRecoveryAction(recoveryActionRetry);
        serviceModelDb.setOperationalEnvId(operationalEnvironmentId);
        serviceModelDb.setRetryCount(retryCountTwo);
        serviceModelDb.setServiceModelVersionDistrStatus(statusError);
        serviceModelDb.setVnfOperationalEnvId(vnfOperationalEnvironmentId);
        wireMockServer.stubFor(get(urlPathEqualTo(
                "/operationalEnvServiceModelStatus/search/findOneByOperationalEnvIdAndServiceModelVersionIdAndRequestId"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(mapper.writeValueAsString(serviceModelDb)).withStatus(HttpStatus.SC_OK)));

        activateVnfStatus.checkOrUpdateOverallStatus(operationalEnvironmentId, requestIdOrig);
    }

    @Test
    public void executionTest_Ok() throws Exception {

        OperationalEnvServiceModelStatus serviceModelDb = new OperationalEnvServiceModelStatus();
        serviceModelDb.setRequestId(requestIdOrig);
        serviceModelDb.setServiceModelVersionId(serviceModelVersionId);
        serviceModelDb.setWorkloadContext(workloadContext);
        serviceModelDb.setRecoveryAction(recoveryActionRetry);
        serviceModelDb.setOperationalEnvId(operationalEnvironmentId);
        serviceModelDb.setRetryCount(retryCountThree);
        serviceModelDb.setServiceModelVersionDistrStatus(statusSent);
        serviceModelDb.setOperationalEnvId(operationalEnvironmentId);
        serviceModelDb.setVnfOperationalEnvId(vnfOperationalEnvironmentId);

        OperationalEnvDistributionStatus distributionDb = new OperationalEnvDistributionStatus();
        distributionDb.setDistributionId(sdcDistributionId);
        distributionDb.setRequestId(requestIdOrig);
        distributionDb.setOperationalEnvId(operationalEnvironmentId);
        distributionDb.setDistributionIdStatus(statusSent);
        distributionDb.setServiceModelVersionId(serviceModelVersionId);
        distributionDb.setDistributionIdErrorReason(null);

        // prepare distribution obj
        Distribution distribution = new Distribution();
        distribution.setStatus(Status.DISTRIBUTION_COMPLETE_OK);
        request.setDistribution(distribution);
        request.setDistributionId(sdcDistributionId);
        request.setOperationalEnvironmentId(operationalEnvironmentId);

        InfraActiveRequests iar = new InfraActiveRequests();
        iar.setRequestId(requestIdOrig);
        iar.setRequestStatus("PENDING");

        wireMockServer.stubFor(get(urlPathEqualTo(
                "/operationalEnvServiceModelStatus/search/findOneByOperationalEnvIdAndServiceModelVersionIdAndRequestId"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(mapper.writeValueAsString(serviceModelDb)).withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(get(urlPathEqualTo("/operationalEnvDistributionStatus/" + sdcDistributionId))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(distributionDb)).withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(get(urlPathEqualTo("/infraActiveRequests/" + requestIdOrig))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(iar)).withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(post(urlPathEqualTo("/operationalEnvServiceModelStatus/")).withRequestBody(equalTo(
                "{\"requestId\":\"TEST_requestIdOrig\",\"operationalEnvId\":\"TEST_operationalEnvironmentId\",\"serviceModelVersionId\":\"TEST_serviceModelVersionId\",\"serviceModelVersionDistrStatus\":\"DISTRIBUTION_COMPLETE_OK\",\"recoveryAction\":\"RETRY\",\"retryCount\":0,\"workloadContext\":\"TEST_workloadContext\",\"createTime\":null,\"modifyTime\":null,\"vnfOperationalEnvId\":\"VNF_operationalEnvironmentId\"}"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(post(urlPathEqualTo("/operationalEnvDistributionStatus/")).withRequestBody(equalTo(
                "{\"distributionId\":\"TEST_distributionId\",\"operationalEnvId\":\"TEST_operationalEnvironmentId\",\"serviceModelVersionId\":\"TEST_serviceModelVersionId\",\"requestId\":\"TEST_requestIdOrig\",\"distributionIdStatus\":\"DISTRIBUTION_COMPLETE_OK\",\"distributionIdErrorReason\":\"\",\"createTime\":null,\"modifyTime\":null}"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlPathEqualTo("/aai/" + AAIVersion.LATEST
                + "/cloud-infrastructure/operational-environments/operational-environment/VNF_operationalEnvironmentId"))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)
                                .withStatus(HttpStatus.SC_ACCEPTED)));
        wireMockServer.stubFor(put(urlPathEqualTo("/aai/" + AAIVersion.LATEST
                + "/cloud-infrastructure/operational-environments/operational-environment/VNF_operationalEnvironmentId"))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json")
                                .withStatus(HttpStatus.SC_ACCEPTED)));

        activateVnfStatus.execute(requestId, request);
    }

    @Test
    public void executionTest_ERROR_Status_And_RETRY() throws Exception {

        OperationalEnvServiceModelStatus serviceModelDb = new OperationalEnvServiceModelStatus();
        serviceModelDb.setRequestId(requestIdOrig);
        serviceModelDb.setServiceModelVersionId(serviceModelVersionId);
        serviceModelDb.setWorkloadContext(workloadContext);
        serviceModelDb.setRecoveryAction(recoveryActionRetry);
        serviceModelDb.setOperationalEnvId(operationalEnvironmentId);
        serviceModelDb.setRetryCount(retryCountThree);
        serviceModelDb.setServiceModelVersionDistrStatus(statusError);
        serviceModelDb.setVnfOperationalEnvId(vnfOperationalEnvironmentId);
        wireMockServer.stubFor(get(urlPathEqualTo(
                "/operationalEnvServiceModelStatus/search/findOneByOperationalEnvIdAndServiceModelVersionIdAndRequestId"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(mapper.writeValueAsString(serviceModelDb)).withStatus(HttpStatus.SC_OK)));

        OperationalEnvDistributionStatus distributionDb = new OperationalEnvDistributionStatus();
        distributionDb.setDistributionId(sdcDistributionId);
        distributionDb.setRequestId(requestIdOrig);
        distributionDb.setOperationalEnvId(operationalEnvironmentId);
        distributionDb.setDistributionIdStatus(statusError);
        distributionDb.setServiceModelVersionId(serviceModelVersionId);
        distributionDb.setDistributionIdErrorReason(null);



        // prepare new distribution obj
        Distribution distribution = new Distribution();
        distribution.setStatus(Status.DISTRIBUTION_COMPLETE_ERROR);
        distribution.setErrorReason("Unable to process.");
        request.setDistribution(distribution);
        request.setDistributionId(sdcDistributionId);
        request.setOperationalEnvironmentId(operationalEnvironmentId);

        // prepare sdc return data
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("statusCode", "202");
        jsonObject.put("message", "Success");
        jsonObject.put("distributionId", sdcDistributionId1);

        wireMockServer.stubFor(post(urlPathMatching("/sdc/v1/catalog/services/.*"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(jsonObject.toString())
                        .withStatus(HttpStatus.SC_ACCEPTED)));
        wireMockServer.stubFor(get(urlPathEqualTo(
                "/operationalEnvServiceModelStatus/search/findOneByOperationalEnvIdAndServiceModelVersionIdAndRequestId"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(mapper.writeValueAsString(serviceModelDb)).withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(get(urlPathEqualTo("/operationalEnvDistributionStatus/" + sdcDistributionId))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(distributionDb)).withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(post(urlPathEqualTo("/operationalEnvDistributionStatus/")).withRequestBody(equalTo(
                "{\"distributionId\":\"TEST_distributionId\",\"operationalEnvId\":\"TEST_operationalEnvironmentId\",\"serviceModelVersionId\":\"TEST_serviceModelVersionId\",\"requestId\":\"TEST_requestIdOrig\",\"distributionIdStatus\":\"DISTRIBUTION_COMPLETE_ERROR\",\"distributionIdErrorReason\":\"Unable to process.\",\"createTime\":null,\"modifyTime\":null,\"handler\":{}}"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(post(urlPathEqualTo("/operationalEnvDistributionStatus/")).willReturn(aResponse()
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(post(urlPathEqualTo("/operationalEnvServiceModelStatus/")).withRequestBody(equalTo(
                "{\"requestId\":\"TEST_requestIdOrig\",\"operationalEnvId\":\"TEST_operationalEnvironmentId\",\"serviceModelVersionId\":\"TEST_serviceModelVersionId\",\"serviceModelVersionDistrStatus\":\"SENT\",\"recoveryAction\":\"RETRY\",\"retryCount\":2,\"workloadContext\":\"TEST_workloadContext\",\"createTime\":null,\"modifyTime\":null,\"vnfOperationalEnvId\":\"VNF_operationalEnvironmentId\"}"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(
                get(urlPathMatching("/aai/" + AAIVersion.LATEST + "/cloud-infrastructure/operational-environments/.*"))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)
                                .withStatus(HttpStatus.SC_ACCEPTED)));
        wireMockServer.stubFor(
                put(urlPathMatching("/aai/" + AAIVersion.LATEST + "/cloud-infrastructure/operational-environments/.*"))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json")
                                .withStatus(HttpStatus.SC_ACCEPTED)));

        activateVnfStatus.execute(requestId, request);
    }

    @Test
    public void executionTest_ERROR_Status_And_RETRY_And_RetryZero() throws JsonProcessingException {

        OperationalEnvServiceModelStatus serviceModelDb = new OperationalEnvServiceModelStatus();
        serviceModelDb.setRequestId(requestIdOrig);
        serviceModelDb.setServiceModelVersionId(serviceModelVersionId);
        serviceModelDb.setWorkloadContext(workloadContext);
        serviceModelDb.setRecoveryAction(recoveryActionRetry);
        serviceModelDb.setOperationalEnvId(operationalEnvironmentId);
        serviceModelDb.setRetryCount(retryCountZero);
        serviceModelDb.setServiceModelVersionDistrStatus(statusError);
        serviceModelDb.setVnfOperationalEnvId(vnfOperationalEnvironmentId);

        OperationalEnvDistributionStatus distributionDb = new OperationalEnvDistributionStatus();
        distributionDb.setDistributionId(sdcDistributionId);
        distributionDb.setRequestId(requestIdOrig);
        distributionDb.setOperationalEnvId(operationalEnvironmentId);
        distributionDb.setDistributionIdStatus(statusError);
        distributionDb.setServiceModelVersionId(serviceModelVersionId);
        distributionDb.setDistributionIdErrorReason(null);



        // prepare distribution obj
        Distribution distribution = new Distribution();
        distribution.setStatus(Status.DISTRIBUTION_COMPLETE_ERROR);
        request.setDistribution(distribution);
        request.setDistributionId(sdcDistributionId);
        request.setOperationalEnvironmentId(operationalEnvironmentId);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("statusCode", "202");
        jsonObject.put("message", "Success");
        jsonObject.put("distributionId", sdcDistributionId);

        InfraActiveRequests iar = new InfraActiveRequests();
        iar.setRequestId(requestIdOrig);
        iar.setRequestStatus("PENDING");

        wireMockServer.stubFor(get(urlPathEqualTo(
                "/operationalEnvServiceModelStatus/search/findOneByOperationalEnvIdAndServiceModelVersionIdAndRequestId"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(mapper.writeValueAsString(serviceModelDb)).withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(get(urlPathEqualTo("/operationalEnvDistributionStatus/" + sdcDistributionId))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(distributionDb)).withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(get(urlPathEqualTo("/infraActiveRequests/" + requestIdOrig))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(iar)).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(post(urlPathMatching("/sdc/v1/catalog/services/.*"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(jsonObject.toString())
                        .withStatus(HttpStatus.SC_ACCEPTED)));

        wireMockServer.stubFor(post(urlPathEqualTo("/operationalEnvServiceModelStatus/")).withRequestBody(equalTo(
                "{\"requestId\":\"TEST_requestIdOrig\",\"operationalEnvId\":\"TEST_operationalEnvironmentId\",\"serviceModelVersionId\":\"TEST_serviceModelVersionId\",\"serviceModelVersionDistrStatus\":\"SENT\",\"recoveryAction\":\"RETRY\",\"retryCount\":2,\"workloadContext\":\"TEST_workloadContext\",\"createTime\":null,\"modifyTime\":null,\"handler\":{}}"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(
                get(urlPathMatching("/aai/" + AAIVersion.LATEST + "/cloud-infrastructure/operational-environments/.*"))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)
                                .withStatus(HttpStatus.SC_ACCEPTED)));
        wireMockServer.stubFor(
                put(urlPathMatching("/aai/" + AAIVersion.LATEST + "/cloud-infrastructure/operational-environments/.*"))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json")
                                .withStatus(HttpStatus.SC_ACCEPTED)));

        try {
            activateVnfStatus.execute(requestId, request);
        } catch (ApiException e) {
            assertThat(e.getMessage(), startsWith("Overall Activation process is a Failure. "));
            assertEquals(e.getHttpResponseCode(), HttpStatus.SC_BAD_REQUEST);
            assertEquals(e.getMessageID(), ErrorNumbers.SVC_DETAILED_SERVICE_ERROR);
        }


    }

    @Test
    public void executionTest_ERROR_Status_And_RETRY_And_ErrorSdc() throws JsonProcessingException {

        OperationalEnvServiceModelStatus serviceModelDb = new OperationalEnvServiceModelStatus();
        serviceModelDb.setRequestId(requestIdOrig);
        serviceModelDb.setServiceModelVersionId(serviceModelVersionId);
        serviceModelDb.setWorkloadContext(workloadContext);
        serviceModelDb.setRecoveryAction(recoveryActionRetry);
        serviceModelDb.setOperationalEnvId(operationalEnvironmentId);
        serviceModelDb.setRetryCount(retryCountThree);
        serviceModelDb.setServiceModelVersionDistrStatus(statusError);
        serviceModelDb.setVnfOperationalEnvId(vnfOperationalEnvironmentId);

        OperationalEnvDistributionStatus distributionDb = new OperationalEnvDistributionStatus();
        distributionDb.setDistributionId(sdcDistributionId);
        distributionDb.setRequestId(requestIdOrig);
        distributionDb.setOperationalEnvId(operationalEnvironmentId);
        distributionDb.setDistributionIdStatus(statusError);
        distributionDb.setServiceModelVersionId(serviceModelVersionId);
        distributionDb.setDistributionIdErrorReason(null);

        // prepare distribution obj
        Distribution distribution = new Distribution();
        distribution.setStatus(Status.DISTRIBUTION_COMPLETE_ERROR);
        distribution.setErrorReason("Unable to process.");
        request.setDistribution(distribution);
        request.setDistributionId(sdcDistributionId);
        request.setOperationalEnvironmentId(operationalEnvironmentId);

        // ERROR in sdc
        JSONObject jsonMessages = new JSONObject();
        jsonMessages.put("statusCode", "409");
        jsonMessages.put("message", "Undefined Error Message!");
        jsonMessages.put("messageId", "SVC4675");
        jsonMessages.put("text", "Error: Service state is invalid for this action.");
        JSONObject jsonServException = new JSONObject();
        jsonServException.put("serviceException", jsonMessages);
        JSONObject jsonErrorRequest = new JSONObject();
        jsonErrorRequest.put("requestError", jsonServException);

        InfraActiveRequests iar = new InfraActiveRequests();
        iar.setRequestId(requestIdOrig);
        iar.setRequestStatus("PENDING");
        wireMockServer.stubFor(get(urlPathEqualTo(
                "/operationalEnvServiceModelStatus/search/findOneByOperationalEnvIdAndServiceModelVersionIdAndRequestId"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(mapper.writeValueAsString(serviceModelDb)).withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(get(urlPathEqualTo("/operationalEnvDistributionStatus/" + sdcDistributionId))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(distributionDb)).withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(get(urlPathEqualTo("/infraActiveRequests/" + requestIdOrig))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(iar)).withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(post(urlPathEqualTo("/infraActiveRequests/"))
                .withRequestBody(containing("operationalEnvId\":\"VNF_operationalEnvironmentId\""))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(post(urlPathMatching("/sdc/v1/catalog/services/.*"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(jsonMessages.toString())
                        .withStatus(HttpStatus.SC_CONFLICT)));

        wireMockServer.stubFor(
                get(urlPathMatching("/aai/" + AAIVersion.LATEST + "/cloud-infrastructure/operational-environments/.*"))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)
                                .withStatus(HttpStatus.SC_ACCEPTED)));
        wireMockServer.stubFor(
                put(urlPathMatching("/aai/" + AAIVersion.LATEST + "/cloud-infrastructure/operational-environments/.*"))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json")
                                .withStatus(HttpStatus.SC_ACCEPTED)));

        try {
            activateVnfStatus.execute(requestId, request);
        } catch (ApiException e) {
            assertThat(e.getMessage(), startsWith("Failure calling SDC: statusCode: "));
            assertEquals(e.getHttpResponseCode(), HttpStatus.SC_BAD_REQUEST);
            assertEquals(e.getMessageID(), ErrorNumbers.SVC_DETAILED_SERVICE_ERROR);
        }
    }

    @Test
    public void executionTest_ERROR_Status_And_SKIP() throws Exception {

        OperationalEnvServiceModelStatus serviceModelDb = new OperationalEnvServiceModelStatus();
        serviceModelDb.setRequestId(requestIdOrig);
        serviceModelDb.setServiceModelVersionId(serviceModelVersionId);
        serviceModelDb.setWorkloadContext(workloadContext);
        serviceModelDb.setRecoveryAction(recoveryActionSkip);
        serviceModelDb.setOperationalEnvId(operationalEnvironmentId);
        serviceModelDb.setRetryCount(retryCountThree);
        serviceModelDb.setServiceModelVersionDistrStatus(statusError);
        serviceModelDb.setVnfOperationalEnvId(vnfOperationalEnvironmentId);

        OperationalEnvDistributionStatus distributionDb = new OperationalEnvDistributionStatus();
        distributionDb.setDistributionId(sdcDistributionId);
        distributionDb.setRequestId(requestIdOrig);
        distributionDb.setOperationalEnvId(operationalEnvironmentId);
        distributionDb.setDistributionIdStatus(statusError);
        distributionDb.setServiceModelVersionId(serviceModelVersionId);
        distributionDb.setDistributionIdErrorReason(null);

        // prepare distribution obj
        OperationalEnvDistributionStatus distributionStatus = new OperationalEnvDistributionStatus(sdcDistributionId,
                operationalEnvironmentId, serviceModelVersionId);
        distributionStatus.setDistributionIdStatus(Status.DISTRIBUTION_COMPLETE_ERROR.name());

        Distribution distribution = new Distribution();
        distribution.setStatus(Status.DISTRIBUTION_COMPLETE_ERROR);
        request.setDistribution(distribution);
        request.setDistributionId(sdcDistributionId);
        request.setOperationalEnvironmentId(operationalEnvironmentId);
        InfraActiveRequests iar = new InfraActiveRequests();
        iar.setRequestId(requestIdOrig);
        iar.setRequestStatus("PENDING");

        wireMockServer.stubFor(get(urlPathEqualTo(
                "/operationalEnvServiceModelStatus/search/findOneByOperationalEnvIdAndServiceModelVersionIdAndRequestId"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(mapper.writeValueAsString(serviceModelDb)).withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(get(urlPathEqualTo("/operationalEnvDistributionStatus/" + sdcDistributionId))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(distributionDb)).withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(get(urlPathEqualTo("/infraActiveRequests/" + requestIdOrig))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(iar)).withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(post(urlPathEqualTo("/operationalEnvServiceModelStatus/")).withRequestBody(equalTo(
                "{\"requestId\":\"TEST_requestIdOrig\",\"operationalEnvId\":\"TEST_operationalEnvironmentId\",\"serviceModelVersionId\":\"TEST_serviceModelVersionId\",\"serviceModelVersionDistrStatus\":\"DISTRIBUTION_COMPLETE_OK\",\"recoveryAction\":\"SKIP\",\"retryCount\":0,\"workloadContext\":\"TEST_workloadContext\",\"createTime\":null,\"modifyTime\":null,\"vnfOperationalEnvId\":\"VNF_operationalEnvironmentId\"}"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(post(urlPathEqualTo("/operationalEnvDistributionStatus/")).withRequestBody(equalTo(
                "{\"distributionId\":\"TEST_distributionId\",\"operationalEnvId\":\"TEST_operationalEnvironmentId\",\"serviceModelVersionId\":\"TEST_serviceModelVersionId\",\"requestId\":\"TEST_requestIdOrig\",\"distributionIdStatus\":\"DISTRIBUTION_COMPLETE_OK\",\"distributionIdErrorReason\":\"\",\"createTime\":null,\"modifyTime\":null}"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(
                get(urlPathMatching("/aai/" + AAIVersion.LATEST + "/cloud-infrastructure/operational-environments/.*"))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)
                                .withStatus(HttpStatus.SC_ACCEPTED)));
        wireMockServer.stubFor(
                put(urlPathMatching("/aai/" + AAIVersion.LATEST + "/cloud-infrastructure/operational-environments/.*"))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json")
                                .withStatus(HttpStatus.SC_ACCEPTED)));

        activateVnfStatus.execute(requestId, request);

    }

    @Test
    public void executionTest_ERROR_Status_And_ABORT() throws JsonProcessingException {

        OperationalEnvServiceModelStatus serviceModelDb = new OperationalEnvServiceModelStatus();
        serviceModelDb.setRequestId(requestIdOrig);
        serviceModelDb.setServiceModelVersionId(serviceModelVersionId);
        serviceModelDb.setWorkloadContext(workloadContext);
        serviceModelDb.setRecoveryAction(recoveryActionAbort);
        serviceModelDb.setOperationalEnvId(operationalEnvironmentId);
        serviceModelDb.setRetryCount(retryCountThree);
        serviceModelDb.setServiceModelVersionDistrStatus(statusError);
        serviceModelDb.setVnfOperationalEnvId(vnfOperationalEnvironmentId);

        OperationalEnvDistributionStatus distributionDb = new OperationalEnvDistributionStatus();
        distributionDb.setDistributionId(sdcDistributionId);
        distributionDb.setRequestId(requestIdOrig);
        distributionDb.setOperationalEnvId(operationalEnvironmentId);
        distributionDb.setDistributionIdStatus(statusError);
        distributionDb.setServiceModelVersionId(serviceModelVersionId);
        distributionDb.setDistributionIdErrorReason(null);



        // prepare distribution obj
        Distribution distribution = new Distribution();
        distribution.setStatus(Status.DISTRIBUTION_COMPLETE_ERROR);
        request.setDistribution(distribution);
        request.setDistributionId(sdcDistributionId);
        request.setOperationalEnvironmentId(operationalEnvironmentId);

        InfraActiveRequests iar = new InfraActiveRequests();
        iar.setRequestId(requestIdOrig);
        iar.setRequestStatus("PENDING");

        wireMockServer.stubFor(get(urlPathEqualTo(
                "/operationalEnvServiceModelStatus/search/findOneByOperationalEnvIdAndServiceModelVersionIdAndRequestId"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody(mapper.writeValueAsString(serviceModelDb)).withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(get(urlPathEqualTo("/operationalEnvDistributionStatus/" + sdcDistributionId))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(distributionDb)).withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(get(urlPathEqualTo("/infraActiveRequests/" + requestIdOrig))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(iar)).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(post(urlPathEqualTo("/operationalEnvDistributionStatus/")).withRequestBody(containing(
                "{\"distributionId\":\"TEST_distributionId\",\"operationalEnvId\":\"TEST_operationalEnvironmentId\",\"serviceModelVersionId\":\"TEST_serviceModelVersionId\",\"requestId\":\"TEST_requestIdOrig\",\"distributionIdStatus\":\"DISTRIBUTION_COMPLETE_ERROR\""))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(post(urlPathEqualTo("/operationalEnvServiceModelStatus/")).withRequestBody(containing(
                "{\"requestId\":\"TEST_requestIdOrig\",\"operationalEnvId\":\"TEST_operationalEnvironmentId\",\"serviceModelVersionId\":\"TEST_serviceModelVersionId\",\"serviceModelVersionDistrStatus\":\"DISTRIBUTION_COMPLETE_ERROR\""))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(
                get(urlPathMatching("/aai/" + AAIVersion.LATEST + "/cloud-infrastructure/operational-environments/.*"))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)
                                .withStatus(HttpStatus.SC_ACCEPTED)));
        wireMockServer.stubFor(
                put(urlPathMatching("/aai/" + AAIVersion.LATEST + "/cloud-infrastructure/operational-environments/.*"))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json")
                                .withStatus(HttpStatus.SC_ACCEPTED)));

        try {
            activateVnfStatus.execute(requestId, request);
        } catch (ApiException e) {
            assertThat(e.getMessage(), startsWith("Overall Activation process is a Failure. "));
            assertEquals(e.getHttpResponseCode(), HttpStatus.SC_BAD_REQUEST);
            assertEquals(e.getMessageID(), ErrorNumbers.SVC_DETAILED_SERVICE_ERROR);
        }

    }

    @Test
    @Ignore
    public void callSDClientForRetryTest_202() throws Exception {
        OperationalEnvServiceModelStatus serviceModelDb = new OperationalEnvServiceModelStatus();
        serviceModelDb.setRequestId(requestIdOrig);
        serviceModelDb.setServiceModelVersionId(serviceModelVersionId);
        serviceModelDb.setWorkloadContext(workloadContext);
        serviceModelDb.setRecoveryAction(recoveryActionRetry);
        serviceModelDb.setOperationalEnvId(operationalEnvironmentId);
        serviceModelDb.setRetryCount(retryCountThree);
        serviceModelDb.setServiceModelVersionDistrStatus(statusSent);

        OperationalEnvDistributionStatus distributionDb = new OperationalEnvDistributionStatus();
        distributionDb.setDistributionId(sdcDistributionId);
        distributionDb.setRequestId(requestIdOrig);
        distributionDb.setOperationalEnvId(operationalEnvironmentId);
        distributionDb.setDistributionIdStatus(statusSent);
        distributionDb.setServiceModelVersionId(serviceModelVersionId);
        distributionDb.setDistributionIdErrorReason(null);



        JSONObject jsonObject = new JSONObject();
        jsonObject.put("statusCode", "202");
        jsonObject.put("message", "Success");
        jsonObject.put("distributionId", sdcDistributionId1);

        // prepare distribution obj
        Distribution distribution = new Distribution();
        distribution.setStatus(Status.DISTRIBUTION_COMPLETE_OK);
        request.setDistribution(distribution);
        request.setDistributionId(sdcDistributionId);
        request.setOperationalEnvironmentId(operationalEnvironmentId);

        wireMockServer.stubFor(post(urlPathMatching("/sdc/v1/catalog/services/TEST_serviceModelVersionId/distr.*"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(jsonObject.toString())
                        .withStatus(HttpStatus.SC_ACCEPTED)));

        JSONObject jsonResponse = activateVnfStatus.callSDClientForRetry(distributionDb, serviceModelDb, distribution);

        assertEquals("TEST_distributionId1", jsonResponse.get("distributionId"));
        assertEquals("Success", jsonResponse.get("message"));
        assertEquals("202", jsonResponse.get("statusCode"));

    }
}
