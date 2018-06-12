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
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.*;

import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.mso.apihandler.common.ErrorNumbers;
import org.openecomp.mso.apihandlerinfra.ApiHandlerApplication;
import org.openecomp.mso.apihandlerinfra.BaseTest;
import org.openecomp.mso.apihandlerinfra.exceptions.ApiException;
import org.openecomp.mso.apihandlerinfra.exceptions.ValidateException;
import org.openecomp.mso.apihandlerinfra.logging.ErrorLoggerInfo;
import org.openecomp.mso.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.OperationalEnvironment;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.RequestDetails;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.RequestInfo;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.RequestParameters;
import org.openecomp.mso.db.request.beans.InfraActiveRequests;
import org.openecomp.mso.db.request.data.repository.InfraActiveRequestsRepository;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.tomakehurst.wiremock.junit.WireMockRule;


public class CreateEcompOperationalEnvironmentTest extends BaseTest{
	
	@Autowired
	private CreateEcompOperationalEnvironment createEcompOpEn;
	@Autowired
	private InfraActiveRequestsRepository infraActiveRequestsRepository;
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(28090));
	 
	public CloudOrchestrationRequest getCloudOrchestrationRequest() {
		CloudOrchestrationRequest request = new CloudOrchestrationRequest();
		RequestDetails reqDetails = new RequestDetails();
		RequestInfo reqInfo = new RequestInfo();
		RequestParameters reqParams = new RequestParameters();
		reqParams.setTenantContext("TEST");
		reqParams.setWorkloadContext("ECOMP_TEST");
		reqParams.setOperationalEnvironmentType(OperationalEnvironment.ECOMP);
		reqInfo.setInstanceName("TEST_ECOMP_ENVIRONMENT");
		reqDetails.setRequestInfo(reqInfo);
		reqDetails.setRequestParameters(reqParams);
		request.setRequestDetails(reqDetails);
		request.setOperationalEnvironmentId("operationalEnvId");
		
		return request;
	}
	
	@Test
	public void testProcess() throws ApiException {
		wireMockRule.stubFor(put(urlPathMatching("/aai/v12/cloud-infrastructure/operational-environments/.*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_ACCEPTED)));
		wireMockRule.stubFor(post(urlPathMatching("/events/.*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_ACCEPTED)));
		
		InfraActiveRequests iar = new InfraActiveRequests();
		iar.setRequestId("123");
		iar.setOperationalEnvName("myOpEnv");
		iar.setRequestScope("create");
		iar.setRequestStatus("PENDING");
		iar.setRequestAction("UNKNOWN");
		infraActiveRequestsRepository.saveAndFlush(iar);
		
		createEcompOpEn.execute("123", getCloudOrchestrationRequest());
		
		InfraActiveRequests infraActiveRequest = infraActiveRequestsRepository.findOneByRequestId("123");
		assertNotNull(infraActiveRequest);
		assertTrue(infraActiveRequest.getStatusMessage().contains("SUCCESS"));	
		assertTrue(infraActiveRequest.getRequestStatus().equals("COMPLETE"));
	}
	
	@Test
	public void testProcessException() {
		wireMockRule.stubFor(put(urlPathMatching("/aai/v12/cloud-infrastructure/operational-environments/.*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_ACCEPTED)));
		wireMockRule.stubFor(post(urlPathMatching("/events/.*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_NOT_FOUND)));
        ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_GENERAL_EXCEPTION, MsoLogger.ErrorCode.DataError).build();
        ValidateException expectedException = new ValidateException.Builder("Could not publish DMaap", HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER)
                .errorInfo(errorLoggerInfo).build();
        
        InfraActiveRequests iar = new InfraActiveRequests();
		iar.setRequestId("123");
		iar.setOperationalEnvName("myOpEnv");
		iar.setRequestScope("create");
		iar.setRequestStatus("PENDING");
		iar.setRequestAction("UNKNOWN");
		infraActiveRequestsRepository.saveAndFlush(iar);
	
		try {
            createEcompOpEn.execute("123", getCloudOrchestrationRequest());
        }catch(ApiException e){
            assertThat(e, sameBeanAs((ApiException) expectedException).ignoring("cause"));
        }

		InfraActiveRequests infraActiveRequest = infraActiveRequestsRepository.findOneByRequestId("123");
		assertNotNull(infraActiveRequest);
		assertTrue(infraActiveRequest.getStatusMessage().contains("FAILURE"));	
		assertTrue(infraActiveRequest.getRequestStatus().equals("FAILED"));
	}

}
