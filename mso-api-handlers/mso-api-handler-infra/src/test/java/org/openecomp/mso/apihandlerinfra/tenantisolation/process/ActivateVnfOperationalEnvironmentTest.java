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
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.mso.apihandlerinfra.ApiHandlerApplication;
import org.openecomp.mso.apihandlerinfra.BaseTest;
import org.openecomp.mso.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.openecomp.mso.apihandlerinfra.tenantisolation.exceptions.SDCClientCallFailed;
import org.openecomp.mso.apihandlerinfra.tenantisolation.helpers.AAIClientHelper;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.Manifest;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.RecoveryAction;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.RequestDetails;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.RequestParameters;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.ServiceModelList;
import org.openecomp.mso.client.aai.entities.AAIResultWrapper;
import org.openecomp.mso.client.aai.objects.AAIOperationalEnvironment;
import org.openecomp.mso.db.request.beans.InfraActiveRequests;
import org.openecomp.mso.db.request.beans.OperationalEnvDistributionStatus;
import org.openecomp.mso.db.request.beans.OperationalEnvServiceModelStatus;
import org.openecomp.mso.db.request.data.repository.InfraActiveRequestsRepository;
import org.openecomp.mso.db.request.data.repository.OperationalEnvDistributionStatusRepository;
import org.openecomp.mso.db.request.data.repository.OperationalEnvServiceModelStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.tomakehurst.wiremock.junit.WireMockRule;


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
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(28090));
	
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

		wireMockRule.stubFor(get(urlPathMatching("/aai/v12/cloud-infrastructure/operational-environments/.*"))
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
		
		wireMockRule.stubFor(get(urlPathMatching("/aai/v12/cloud-infrastructure/operational-environments/.*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withBodyFile("vnfoperenv/ecompOperationalEnvironment.json").withStatus(HttpStatus.SC_ACCEPTED)));
		wireMockRule.stubFor(post(urlPathMatching("/sdc/v1/catalog/services/TEST_serviceModelVersionId/distr.*"))
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
		
		wireMockRule.stubFor(post(urlPathMatching("/sdc/v1/catalog/services/TEST_serviceModelVersionId/distr.*"))
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
		
		wireMockRule.stubFor(post(urlPathMatching("/sdc/v1/catalog/services/TEST_serviceModelVersionId/distr.*"))
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
