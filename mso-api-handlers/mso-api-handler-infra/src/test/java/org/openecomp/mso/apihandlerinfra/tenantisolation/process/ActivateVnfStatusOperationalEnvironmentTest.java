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

package org.openecomp.mso.apihandlerinfra.tenantisolation.process;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.*;

import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.openecomp.mso.apihandler.common.ErrorNumbers;
import org.openecomp.mso.apihandlerinfra.ApiHandlerApplication;
import org.openecomp.mso.apihandlerinfra.BaseTest;
import org.openecomp.mso.apihandlerinfra.exceptions.ApiException;
import org.openecomp.mso.apihandlerinfra.exceptions.RecipeNotFoundException;
import org.openecomp.mso.apihandlerinfra.exceptions.ValidateException;
import org.openecomp.mso.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.openecomp.mso.apihandlerinfra.tenantisolation.helpers.ActivateVnfDBHelper;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.Distribution;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.DistributionStatus;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.Status;
import org.openecomp.mso.db.request.beans.InfraActiveRequests;
import org.openecomp.mso.db.request.beans.OperationalEnvDistributionStatus;
import org.openecomp.mso.db.request.beans.OperationalEnvServiceModelStatus;
import org.openecomp.mso.db.request.data.repository.InfraActiveRequestsRepository;
import org.openecomp.mso.db.request.data.repository.OperationalEnvDistributionStatusRepository;
import org.openecomp.mso.db.request.data.repository.OperationalEnvServiceModelStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import java.io.IOException;

public class ActivateVnfStatusOperationalEnvironmentTest extends BaseTest{

	@Autowired
	private OperationalEnvDistributionStatusRepository distributionDbRepository;
	@Autowired
	private OperationalEnvServiceModelStatusRepository serviceModelDbRepository;
	@Autowired
	private ActivateVnfStatusOperationalEnvironment activateVnfStatus;
	@Autowired
	private InfraActiveRequestsRepository infraActiveRequestsRepository;
	@Autowired
	private ActivateVnfDBHelper dbHelper;
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(28090));

    @Rule
    public ExpectedException thrown = ExpectedException.none();

	private String requestId = "TEST_requestId";
	private String requestIdOrig = "TEST_requestIdOrig";	
	private String operationalEnvironmentId = "TEST_operationalEnvironmentId";	
	private CloudOrchestrationRequest request = new CloudOrchestrationRequest();
	private String workloadContext = "TEST_workloadContext";
	private String recoveryActionRetry  = "RETRY";
	private String recoveryActionAbort  = "ABORT";
	private String recoveryActionSkip  = "SKIP";
	private String serviceModelVersionId = "TEST_serviceModelVersionId";
	private String serviceModelVersionId1 = "TEST_serviceModelVersionId1";	
	private int retryCountThree = 3;
	private int retryCountTwo = 2;	
	private int retryCountZero = 0;	
	private String sdcDistributionId = "TEST_distributionId";
	private String sdcDistributionId1 = "TEST_distributionId1";	
	private String statusOk = Status.DISTRIBUTION_COMPLETE_OK.toString();
	private String statusError = DistributionStatus.DISTRIBUTION_COMPLETE_ERROR.toString();
	private String statusSent = "SENT";
	 
	@After
	public void after() throws Exception {
		distributionDbRepository.deleteAll();
		serviceModelDbRepository.deleteAll();		
	}

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
		serviceModelDbRepository.saveAndFlush(serviceModelDb);
		
		serviceModelDb.setRequestId(requestIdOrig);
		serviceModelDb.setServiceModelVersionId(serviceModelVersionId1);
		serviceModelDb.setWorkloadContext(workloadContext);
		serviceModelDb.setRecoveryAction(recoveryActionRetry);
		serviceModelDb.setOperationalEnvId(operationalEnvironmentId);
		serviceModelDb.setRetryCount(retryCountZero);
		serviceModelDb.setServiceModelVersionDistrStatus(statusOk);
		serviceModelDbRepository.saveAndFlush(serviceModelDb);	
		
		InfraActiveRequests iar = new InfraActiveRequests();
		iar.setRequestId(requestIdOrig);
		iar.setRequestStatus("PENDING");
		infraActiveRequestsRepository.saveAndFlush(iar);
		
		activateVnfStatus.checkOrUpdateOverallStatus(operationalEnvironmentId, requestIdOrig, serviceModelDbRepository);
		
		// overall is success
		InfraActiveRequests infraActiveRequest = infraActiveRequestsRepository.findOne(requestIdOrig);
		assertNotNull(infraActiveRequest);
		assertTrue(infraActiveRequest.getStatusMessage().contains("SUCCESSFUL"));
		assertTrue(infraActiveRequest.getRequestStatus().contains("COMPLETE"));
		
		// cleanup
		infraActiveRequestsRepository.delete(requestIdOrig);		
	}

	@Test
	public void checkOrUpdateOverallStatusTest_Error() throws Exception {
		
		OperationalEnvServiceModelStatus serviceModelDb = new OperationalEnvServiceModelStatus();
		serviceModelDb.setRequestId(requestIdOrig);
		serviceModelDb.setServiceModelVersionId(serviceModelVersionId);
		serviceModelDb.setWorkloadContext(workloadContext);
		serviceModelDb.setRecoveryAction(recoveryActionRetry);
		serviceModelDb.setOperationalEnvId(operationalEnvironmentId);
		serviceModelDb.setRetryCount(retryCountZero);
		serviceModelDb.setServiceModelVersionDistrStatus(statusError);
		serviceModelDbRepository.saveAndFlush(serviceModelDb);
		
		InfraActiveRequests iar = new InfraActiveRequests();
		iar.setRequestId(requestIdOrig);
		iar.setRequestStatus("PENDING");
		infraActiveRequestsRepository.saveAndFlush(iar);

        try {
            activateVnfStatus.checkOrUpdateOverallStatus(operationalEnvironmentId, requestIdOrig, serviceModelDbRepository);
        }catch(ApiException e){
            assertThat(e.getMessage(), startsWith("Overall Activation process is a Failure. "));
            assertEquals(e.getHttpResponseCode(), HttpStatus.SC_BAD_REQUEST);
            assertEquals(e.getMessageID(), ErrorNumbers.SVC_DETAILED_SERVICE_ERROR);
        }
		
		// overall is failure
		InfraActiveRequests infraActiveRequest = infraActiveRequestsRepository.findOne(requestIdOrig);
		assertNotNull(infraActiveRequest);
		assertTrue(infraActiveRequest.getStatusMessage().contains("FAILURE"));
		assertTrue(infraActiveRequest.getRequestStatus().contains("FAILED"));
		
		// cleanup		
		infraActiveRequestsRepository.delete(requestIdOrig);
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
		serviceModelDbRepository.saveAndFlush(serviceModelDb);
		
		activateVnfStatus.checkOrUpdateOverallStatus(operationalEnvironmentId, requestIdOrig, serviceModelDbRepository);
		
		// do nothing, waiting for more
		assertNull(infraActiveRequestsRepository.findOne(requestIdOrig));
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
		serviceModelDbRepository.saveAndFlush(serviceModelDb);

		OperationalEnvDistributionStatus distributionDb = new OperationalEnvDistributionStatus();
		distributionDb.setDistributionId(sdcDistributionId);
		distributionDb.setRequestId(requestIdOrig);
		distributionDb.setOperationalEnvId(operationalEnvironmentId);
		distributionDb.setDistributionIdStatus(statusSent);
		distributionDb.setServiceModelVersionId(serviceModelVersionId);
		distributionDb.setDistributionIdErrorReason(null);
		distributionDbRepository.saveAndFlush(distributionDb);
		
		
		
		// prepare distribution obj
		Distribution distribution = new Distribution();
		distribution.setStatus(Status.DISTRIBUTION_COMPLETE_OK);
		request.setDistribution(distribution);
		request.setDistributionId(sdcDistributionId);
		request.setOperationalEnvironmentId(operationalEnvironmentId);
		
		InfraActiveRequests iar = new InfraActiveRequests();
		iar.setRequestId(requestIdOrig);
		iar.setRequestStatus("PENDING");
		infraActiveRequestsRepository.saveAndFlush(iar);
		
		activateVnfStatus.execute(requestId, request, distributionDbRepository, serviceModelDbRepository);		
		
		// status ok
		OperationalEnvDistributionStatus distStatus = distributionDbRepository.findOne(sdcDistributionId);
		assertNotNull(distStatus);
		assertEquals(operationalEnvironmentId, distStatus.getOperationalEnvId());
		assertEquals(statusOk, distStatus.getDistributionIdStatus());
		assertEquals("", distStatus.getDistributionIdErrorReason());		
		
		// status ok		
		OperationalEnvServiceModelStatus servStatus = serviceModelDbRepository.findOneByOperationalEnvIdAndServiceModelVersionId(operationalEnvironmentId, serviceModelVersionId);
		assertNotNull(servStatus);
		assertEquals(operationalEnvironmentId, servStatus.getOperationalEnvId());
		assertEquals(statusOk, servStatus.getServiceModelVersionDistrStatus());		
		assertEquals(new Integer(retryCountZero), servStatus.getRetryCount());
		
		InfraActiveRequests infraActiveRequest = infraActiveRequestsRepository.findOne(requestIdOrig);
		assertNotNull(infraActiveRequest);
		assertTrue(infraActiveRequest.getStatusMessage().contains("SUCCESSFUL"));
		assertTrue(infraActiveRequest.getRequestStatus().contains("COMPLETE"));
		
		// cleanup		
		infraActiveRequestsRepository.delete(requestIdOrig);
		
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
		serviceModelDbRepository.saveAndFlush(serviceModelDb);
		
		OperationalEnvDistributionStatus distributionDb = new OperationalEnvDistributionStatus();
		distributionDb.setDistributionId(sdcDistributionId);
		distributionDb.setRequestId(requestIdOrig);
		distributionDb.setOperationalEnvId(operationalEnvironmentId);
		distributionDb.setDistributionIdStatus(statusError);
		distributionDb.setServiceModelVersionId(serviceModelVersionId);
		distributionDb.setDistributionIdErrorReason(null);
		distributionDbRepository.saveAndFlush(distributionDb);
		
		
		
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
		
		wireMockRule.stubFor(post(urlPathMatching("/sdc/v1/catalog/services/.*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(jsonObject.toString()).withStatus(HttpStatus.SC_ACCEPTED)));
		
		activateVnfStatus.execute(requestId, request, distributionDbRepository, serviceModelDbRepository);	
		
		// old distributionId, status error
		OperationalEnvDistributionStatus distStatus = distributionDbRepository.findOne(sdcDistributionId);
		assertNotNull(distStatus);
		assertEquals(operationalEnvironmentId, distStatus.getOperationalEnvId());
		assertEquals(statusError, distStatus.getDistributionIdStatus());
		assertEquals("Unable to process.", distStatus.getDistributionIdErrorReason());		
		
		// new distributionId, status sent
		OperationalEnvDistributionStatus newDistStatus = distributionDbRepository.findOne(sdcDistributionId1);
		assertNotNull(distStatus);
		assertEquals(operationalEnvironmentId, newDistStatus.getOperationalEnvId());
		assertEquals(statusSent, newDistStatus.getDistributionIdStatus());
		assertEquals("", newDistStatus.getDistributionIdErrorReason());		

		// count is less 1, status sent
		OperationalEnvServiceModelStatus servStatus = serviceModelDbRepository.findOneByOperationalEnvIdAndServiceModelVersionId(operationalEnvironmentId, serviceModelVersionId);
		assertNotNull(servStatus);
		assertEquals(operationalEnvironmentId, servStatus.getOperationalEnvId());
		assertEquals(statusSent, servStatus.getServiceModelVersionDistrStatus());		
		assertEquals(new Integer(retryCountTwo), servStatus.getRetryCount());		
		
		// no update 
		assertNull(infraActiveRequestsRepository.findOne(requestIdOrig));
		
	}

	@Test
	public void executionTest_ERROR_Status_And_RETRY_And_RetryZero() throws Exception {
		
		OperationalEnvServiceModelStatus serviceModelDb = new OperationalEnvServiceModelStatus();
		serviceModelDb.setRequestId(requestIdOrig);
		serviceModelDb.setServiceModelVersionId(serviceModelVersionId);
		serviceModelDb.setWorkloadContext(workloadContext);
		serviceModelDb.setRecoveryAction(recoveryActionRetry);
		serviceModelDb.setOperationalEnvId(operationalEnvironmentId);
		serviceModelDb.setRetryCount(retryCountZero);
		serviceModelDb.setServiceModelVersionDistrStatus(statusError);
		serviceModelDbRepository.saveAndFlush(serviceModelDb);
		
		OperationalEnvDistributionStatus distributionDb = new OperationalEnvDistributionStatus();
		distributionDb.setDistributionId(sdcDistributionId);
		distributionDb.setRequestId(requestIdOrig);
		distributionDb.setOperationalEnvId(operationalEnvironmentId);
		distributionDb.setDistributionIdStatus(statusError);
		distributionDb.setServiceModelVersionId(serviceModelVersionId);
		distributionDb.setDistributionIdErrorReason(null);
		distributionDbRepository.saveAndFlush(distributionDb);
		
	
		
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
		infraActiveRequestsRepository.saveAndFlush(iar);
		
		wireMockRule.stubFor(post(urlPathMatching("/sdc/v1/catalog/services/.*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(jsonObject.toString()).withStatus(HttpStatus.SC_ACCEPTED)));

        try {
            activateVnfStatus.execute(requestId, request, distributionDbRepository, serviceModelDbRepository);
        }catch(ApiException e){
            assertThat(e.getMessage(), startsWith("Overall Activation process is a Failure. "));
            assertEquals(e.getHttpResponseCode(), HttpStatus.SC_BAD_REQUEST);
            assertEquals(e.getMessageID(), ErrorNumbers.SVC_DETAILED_SERVICE_ERROR);
        }

		OperationalEnvDistributionStatus distStatus = distributionDbRepository.findOne(sdcDistributionId);
		assertNotNull(distStatus);
		assertEquals(operationalEnvironmentId, distStatus.getOperationalEnvId());
		assertEquals(statusError, distStatus.getDistributionIdStatus());
		assertEquals(null, distStatus.getDistributionIdErrorReason());		
		
		OperationalEnvServiceModelStatus servStatus = serviceModelDbRepository.findOneByOperationalEnvIdAndServiceModelVersionId(operationalEnvironmentId, serviceModelVersionId);
		assertNotNull(servStatus);
		assertEquals(operationalEnvironmentId, servStatus.getOperationalEnvId());
		assertEquals(statusError, servStatus.getServiceModelVersionDistrStatus());		
		assertEquals(new Integer(retryCountZero), servStatus.getRetryCount());		

		// Retry count is zero, no more retry. all retry failed. 
		InfraActiveRequests infraActiveRequest = infraActiveRequestsRepository.findOne(requestIdOrig);
		assertNotNull(infraActiveRequest);
		assertTrue(infraActiveRequest.getStatusMessage().contains("FAILURE"));
		assertTrue(infraActiveRequest.getRequestStatus().contains("FAILED"));
		
		// cleanup		
		infraActiveRequestsRepository.delete(requestIdOrig);		
		
	}	
	
	@Test
	public void executionTest_ERROR_Status_And_RETRY_And_ErrorSdc() throws Exception {
		
		OperationalEnvServiceModelStatus serviceModelDb = new OperationalEnvServiceModelStatus();
		serviceModelDb.setRequestId(requestIdOrig);
		serviceModelDb.setServiceModelVersionId(serviceModelVersionId);
		serviceModelDb.setWorkloadContext(workloadContext);
		serviceModelDb.setRecoveryAction(recoveryActionRetry);
		serviceModelDb.setOperationalEnvId(operationalEnvironmentId);
		serviceModelDb.setRetryCount(retryCountThree);
		serviceModelDb.setServiceModelVersionDistrStatus(statusError);
		serviceModelDbRepository.saveAndFlush(serviceModelDb);
		
		OperationalEnvDistributionStatus distributionDb = new OperationalEnvDistributionStatus();
		distributionDb.setDistributionId(sdcDistributionId);
		distributionDb.setRequestId(requestIdOrig);
		distributionDb.setOperationalEnvId(operationalEnvironmentId);
		distributionDb.setDistributionIdStatus(statusError);
		distributionDb.setServiceModelVersionId(serviceModelVersionId);
		distributionDb.setDistributionIdErrorReason(null);
		distributionDbRepository.saveAndFlush(distributionDb);
		
		
		
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
		infraActiveRequestsRepository.saveAndFlush(iar);
		
		wireMockRule.stubFor(post(urlPathMatching("/sdc/v1/catalog/services/.*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(jsonMessages.toString()).withStatus(HttpStatus.SC_CONFLICT)));

		try {
            activateVnfStatus.execute(requestId, request, distributionDbRepository, serviceModelDbRepository);
        }catch(ApiException e){
            assertThat(e.getMessage(), startsWith("Failure calling SDC: statusCode: "));
            assertEquals(e.getHttpResponseCode(), HttpStatus.SC_BAD_REQUEST);
            assertEquals(e.getMessageID(), ErrorNumbers.SVC_DETAILED_SERVICE_ERROR);
        }
		// status as-is / no changes
		OperationalEnvDistributionStatus distStatus = distributionDbRepository.findOne(sdcDistributionId);
		assertNotNull(distStatus);
		assertEquals(operationalEnvironmentId, distStatus.getOperationalEnvId());
		assertEquals(statusError, distStatus.getDistributionIdStatus());
		assertEquals(null, distStatus.getDistributionIdErrorReason());		
		
		// status as-is / no changes		
		OperationalEnvServiceModelStatus servStatus = serviceModelDbRepository.findOneByOperationalEnvIdAndServiceModelVersionId(operationalEnvironmentId, serviceModelVersionId);
		assertNotNull(servStatus);
		assertEquals(operationalEnvironmentId, servStatus.getOperationalEnvId());
		assertEquals(statusError, servStatus.getServiceModelVersionDistrStatus());		
		assertEquals(new Integer(retryCountThree), servStatus.getRetryCount());
		
		InfraActiveRequests infraActiveRequest = infraActiveRequestsRepository.findOne(requestIdOrig);
		assertNotNull(infraActiveRequest);
		assertTrue(infraActiveRequest.getStatusMessage().contains("FAILURE"));
		assertTrue(infraActiveRequest.getRequestStatus().contains("FAILED"));	
		assertTrue(infraActiveRequest.getStatusMessage().contains("Undefined Error Message!"));
		
		// cleanup		
		infraActiveRequestsRepository.delete(requestIdOrig);
		
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
		serviceModelDbRepository.saveAndFlush(serviceModelDb);
		
		OperationalEnvDistributionStatus distributionDb = new OperationalEnvDistributionStatus();
		distributionDb.setDistributionId(sdcDistributionId);
		distributionDb.setRequestId(requestIdOrig);
		distributionDb.setOperationalEnvId(operationalEnvironmentId);
		distributionDb.setDistributionIdStatus(statusError);
		distributionDb.setServiceModelVersionId(serviceModelVersionId);
		distributionDb.setDistributionIdErrorReason(null);
		distributionDbRepository.saveAndFlush(distributionDb);
		
		
		
		// prepare distribution obj
		Distribution distribution = new Distribution();
		distribution.setStatus(Status.DISTRIBUTION_COMPLETE_ERROR);
		request.setDistribution(distribution);
		request.setDistributionId(sdcDistributionId);
		request.setOperationalEnvironmentId(operationalEnvironmentId);
		
		InfraActiveRequests iar = new InfraActiveRequests();
		iar.setRequestId(requestIdOrig);
		iar.setRequestStatus("PENDING");
		infraActiveRequestsRepository.saveAndFlush(iar);

		activateVnfStatus.execute(requestId, request, distributionDbRepository, serviceModelDbRepository);			

		InfraActiveRequests infraActiveRequest = infraActiveRequestsRepository.findOne(requestIdOrig);
		assertNotNull(infraActiveRequest);
		assertTrue(infraActiveRequest.getStatusMessage().contains("SUCCESSFUL"));
		assertTrue(infraActiveRequest.getRequestStatus().contains("COMPLETE"));	
		
		// cleanup		
		infraActiveRequestsRepository.delete(requestIdOrig);
		
	}	
	
	@Test
	public void executionTest_ERROR_Status_And_ABORT() throws Exception {
		
		OperationalEnvServiceModelStatus serviceModelDb = new OperationalEnvServiceModelStatus();
		serviceModelDb.setRequestId(requestIdOrig);
		serviceModelDb.setServiceModelVersionId(serviceModelVersionId);
		serviceModelDb.setWorkloadContext(workloadContext);
		serviceModelDb.setRecoveryAction(recoveryActionAbort);
		serviceModelDb.setOperationalEnvId(operationalEnvironmentId);
		serviceModelDb.setRetryCount(retryCountThree);
		serviceModelDb.setServiceModelVersionDistrStatus(statusError);
		serviceModelDbRepository.saveAndFlush(serviceModelDb);
		
		OperationalEnvDistributionStatus distributionDb = new OperationalEnvDistributionStatus();
		distributionDb.setDistributionId(sdcDistributionId);
		distributionDb.setRequestId(requestIdOrig);
		distributionDb.setOperationalEnvId(operationalEnvironmentId);
		distributionDb.setDistributionIdStatus(statusError);
		distributionDb.setServiceModelVersionId(serviceModelVersionId);
		distributionDb.setDistributionIdErrorReason(null);
		distributionDbRepository.saveAndFlush(distributionDb);
		
		
		
		// prepare distribution obj
		Distribution distribution = new Distribution();
		distribution.setStatus(Status.DISTRIBUTION_COMPLETE_ERROR);
		request.setDistribution(distribution);
		request.setDistributionId(sdcDistributionId);
		request.setOperationalEnvironmentId(operationalEnvironmentId);
		
		InfraActiveRequests iar = new InfraActiveRequests();
		iar.setRequestId(requestIdOrig);
		iar.setRequestStatus("PENDING");
		infraActiveRequestsRepository.saveAndFlush(iar);

        try {
            activateVnfStatus.execute(requestId, request, distributionDbRepository, serviceModelDbRepository);
        }catch(ApiException e){
            assertThat(e.getMessage(), startsWith("Overall Activation process is a Failure. "));
            assertEquals(e.getHttpResponseCode(), HttpStatus.SC_BAD_REQUEST);
            assertEquals(e.getMessageID(), ErrorNumbers.SVC_DETAILED_SERVICE_ERROR);
        }
		
		InfraActiveRequests infraActiveRequest = infraActiveRequestsRepository.findOne(requestIdOrig);
		assertNotNull(infraActiveRequest);
		assertTrue(infraActiveRequest.getStatusMessage().contains("FAILURE"));
		assertTrue(infraActiveRequest.getRequestStatus().contains("FAILED"));
		
		// cleanup		
		infraActiveRequestsRepository.delete(requestIdOrig);
		
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
		
		wireMockRule.stubFor(post(urlPathMatching("/sdc/v1/catalog/services/TEST_serviceModelVersionId/distr.*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(jsonObject.toString()).withStatus(HttpStatus.SC_ACCEPTED)));
		
		JSONObject jsonResponse = activateVnfStatus.callSDClientForRetry(distributionDb, serviceModelDb, distribution,
															distributionDbRepository, serviceModelDbRepository); 
		
		assertEquals("TEST_distributionId1", jsonResponse.get("distributionId")); 
		assertEquals("Success", jsonResponse.get("message"));
		assertEquals("202", jsonResponse.get("statusCode"));		

		// insert new record, status sent
		OperationalEnvDistributionStatus distStatus = distributionDbRepository.findOne(sdcDistributionId1);
		assertNotNull(distStatus);
		assertEquals(operationalEnvironmentId, distStatus.getOperationalEnvId());
		assertEquals(statusSent, distStatus.getDistributionIdStatus());		
		
		// insert new record, status sent		
		OperationalEnvServiceModelStatus servStatus = serviceModelDbRepository.findOneByOperationalEnvIdAndServiceModelVersionId(operationalEnvironmentId, serviceModelVersionId);
		assertNotNull(servStatus);
		assertEquals(statusSent, servStatus.getServiceModelVersionDistrStatus());
		assertEquals(operationalEnvironmentId, servStatus.getOperationalEnvId());
		
	}		
}
