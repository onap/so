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
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;
import org.onap.so.apihandlerinfra.BaseTest;
import org.onap.so.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.onap.so.apihandlerinfra.tenantisolation.helpers.AAIClientHelper;
import org.onap.so.apihandlerinfra.tenantisolationbeans.Manifest;
import org.onap.so.apihandlerinfra.tenantisolationbeans.RecoveryAction;
import org.onap.so.apihandlerinfra.tenantisolationbeans.RequestDetails;
import org.onap.so.apihandlerinfra.tenantisolationbeans.RequestParameters;
import org.onap.so.apihandlerinfra.tenantisolationbeans.ServiceModelList;
import org.onap.so.client.aai.AAIVersion;
import org.onap.so.client.aai.entities.AAIResultWrapper;
import org.onap.so.client.aai.objects.AAIOperationalEnvironment;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.beans.OperationalEnvDistributionStatus;
import org.onap.so.db.request.beans.OperationalEnvServiceModelStatus;
import org.onap.so.db.request.data.repository.InfraActiveRequestsRepository;
import org.onap.so.db.request.data.repository.OperationalEnvDistributionStatusRepository;
import org.onap.so.db.request.data.repository.OperationalEnvServiceModelStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;


public class ActivateVnfOperationalEnvironmentTest extends BaseTest{

	@Autowired
	private OperationalEnvDistributionStatusRepository distributionDbRepository;
	@Autowired
	private OperationalEnvServiceModelStatusRepository serviceModelDbRepository;
	@Autowired
	private ActivateVnfOperationalEnvironment activateVnf;
	@Autowired
	private InfraActiveRequestsRepository infraActiveRequestsRepository;	
	@Autowired
	private AAIClientHelper clientHelper;

	String requestId = "TEST_requestId";
	String operationalEnvironmentId = "EMOE-001";	
	CloudOrchestrationRequest request = new CloudOrchestrationRequest();
	String workloadContext = "PVT";
	String recoveryActionRetry  = "RETRY";
	String serviceModelVersionId = "TEST_serviceModelVersionId";	
	int retryCount = 3;
	String sdcDistributionId = "TEST_distributionId";
	String statusSent = "SENT";

	@After
	public void after() throws Exception {
		distributionDbRepository.deleteAll();
		serviceModelDbRepository.deleteAll();		
	}

	@Test
	public void getAAIOperationalEnvironmentTest() throws Exception {

		AAIOperationalEnvironment aaiOpEnv = null;

		stubFor(get(urlPathMatching("/aai/" + AAIVersion.LATEST + "/cloud-infrastructure/operational-environments/.*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("vnfoperenv/ecompOperationalEnvironment.json").withStatus(HttpStatus.SC_ACCEPTED)));
		
		AAIResultWrapper wrapper = clientHelper.getAaiOperationalEnvironment("EMOE-001");
		aaiOpEnv = wrapper.asBean(AAIOperationalEnvironment.class).get();
		assertEquals("EMOE-001", aaiOpEnv.getOperationalEnvironmentId());			
		assertNotNull(activateVnf.getAAIOperationalEnvironment(operationalEnvironmentId));	
		assertEquals( "EMOE-001", activateVnf.getAAIOperationalEnvironment(operationalEnvironmentId).getOperationalEnvironmentId());		
		
	}	
		
	@Test
	public void executionTest() throws Exception {

		List<ServiceModelList> serviceModelVersionIdList = new ArrayList<ServiceModelList>();
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
		
		request.setOperationalEnvironmentId(operationalEnvironmentId);
		request.setRequestDetails(requestDetails);
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("statusCode", "202");
		jsonObject.put("message", "Success");
		jsonObject.put("distributionId", sdcDistributionId);
		
		stubFor(get(urlPathMatching("/aai/" + AAIVersion.LATEST + "/cloud-infrastructure/operational-environments/.*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("vnfoperenv/ecompOperationalEnvironment.json").withStatus(HttpStatus.SC_ACCEPTED)));
		stubFor(post(urlPathMatching("/sdc/v1/catalog/services/TEST_serviceModelVersionId/distr.*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(jsonObject.toString()).withStatus(HttpStatus.SC_ACCEPTED)));
		
		
		activateVnf.execute(requestId, request, distributionDbRepository, serviceModelDbRepository);
		
		// insert record, status sent
		OperationalEnvDistributionStatus distStatus = distributionDbRepository.findOne(sdcDistributionId);
		assertNotNull(distStatus);
		assertEquals(operationalEnvironmentId, distStatus.getOperationalEnvId());
		assertEquals(statusSent, distStatus.getDistributionIdStatus());
		
		// insert record, status sent		
		OperationalEnvServiceModelStatus servStatus = serviceModelDbRepository.findOneByOperationalEnvIdAndServiceModelVersionId(operationalEnvironmentId, serviceModelVersionId);
		assertNotNull(servStatus);
		assertEquals(statusSent, servStatus.getServiceModelVersionDistrStatus());
		assertEquals(operationalEnvironmentId, servStatus.getOperationalEnvId());

	}			
	
	@Test
	public void processActivateSDCRequestTest_202() throws Exception {

		String distributionId = "TEST_distributionId";
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("statusCode", "202");
		jsonObject.put("message", "Success");
		jsonObject.put("distributionId", distributionId);
		
		// prepare request detail
		List<ServiceModelList> serviceModelVersionIdList = new ArrayList<ServiceModelList>();
		ServiceModelList serviceModelList1 = new ServiceModelList(); 
		serviceModelList1.setRecoveryAction(RecoveryAction.retry);
		serviceModelList1.setServiceModelVersionId(serviceModelVersionId);
		serviceModelVersionIdList.add(serviceModelList1);
		
		stubFor(post(urlPathMatching("/sdc/v1/catalog/services/TEST_serviceModelVersionId/distr.*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(jsonObject.toString()).withStatus(HttpStatus.SC_ACCEPTED)));
		
		activateVnf.processActivateSDCRequest(requestId, operationalEnvironmentId, serviceModelVersionIdList, workloadContext, 
													distributionDbRepository, serviceModelDbRepository);
		
		// insert record, status sent		
		OperationalEnvDistributionStatus distStatus = distributionDbRepository.findOne(sdcDistributionId);
		assertNotNull(distStatus);
		assertEquals(operationalEnvironmentId, distStatus.getOperationalEnvId());
		assertEquals(statusSent, distStatus.getDistributionIdStatus());		
		
		// insert record, status sent		
		OperationalEnvServiceModelStatus servStatus = serviceModelDbRepository.findOneByOperationalEnvIdAndServiceModelVersionId(operationalEnvironmentId, serviceModelVersionId);
		assertNotNull(servStatus);
		assertEquals(statusSent, servStatus.getServiceModelVersionDistrStatus());
		assertEquals(operationalEnvironmentId, servStatus.getOperationalEnvId());
		
	}	
	
	@Test 
	public void processActivateSDCRequestTest_409() throws Exception {

		// ERROR in asdc
		JSONObject jsonMessages = new JSONObject();
		jsonMessages.put("message", "Failure");
		jsonMessages.put("messageId", "SVC4675");
		jsonMessages.put("text", "Error: Service state is invalid for this action.");
		JSONObject jsonServException = new JSONObject();
		jsonServException.put("policyException", jsonMessages);
		//jsonServException.put("serviceException", jsonMessages);
		JSONObject jsonErrorResponse = new JSONObject();
		jsonErrorResponse.put("requestError", jsonServException);
		
		// prepare request detail
		List<ServiceModelList> serviceModelVersionIdList = new ArrayList<ServiceModelList>();
		ServiceModelList serviceModelList1 = new ServiceModelList(); 
		serviceModelList1.setRecoveryAction(RecoveryAction.retry);
		serviceModelList1.setServiceModelVersionId(serviceModelVersionId);
		serviceModelVersionIdList.add(serviceModelList1);
		
		InfraActiveRequests iar = new InfraActiveRequests();
		iar.setRequestId(requestId);
		iar.setRequestStatus("PENDING");
		infraActiveRequestsRepository.saveAndFlush(iar);
		
		stubFor(post(urlPathMatching("/sdc/v1/catalog/services/TEST_serviceModelVersionId/distr.*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(jsonErrorResponse.toString()).withStatus(HttpStatus.SC_CONFLICT)));
		
		try {
			activateVnf.processActivateSDCRequest(requestId, operationalEnvironmentId, serviceModelVersionIdList, workloadContext, 
													distributionDbRepository, serviceModelDbRepository);
			
		} catch (Exception  ex) {
			
			// insert record, status sent
			OperationalEnvServiceModelStatus servStatus = serviceModelDbRepository.findOneByOperationalEnvIdAndServiceModelVersionId(operationalEnvironmentId, serviceModelVersionId);
			assertNotNull(servStatus);
			assertEquals(statusSent, servStatus.getServiceModelVersionDistrStatus());
			
			InfraActiveRequests infraActiveRequest = infraActiveRequestsRepository.findOne(requestId);
			assertNotNull(infraActiveRequest);
			assertTrue(infraActiveRequest.getStatusMessage().contains("FAILURE"));
			assertTrue(infraActiveRequest.getRequestStatus().contains("FAILED"));			
			
		}  
		
		infraActiveRequestsRepository.delete(requestId);
	}		
	
}
