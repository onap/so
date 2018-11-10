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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertThat;
import java.util.UUID;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandlerinfra.BaseTest;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.onap.so.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.onap.so.apihandlerinfra.tenantisolationbeans.OperationalEnvironment;
import org.onap.so.apihandlerinfra.tenantisolationbeans.RequestDetails;
import org.onap.so.apihandlerinfra.tenantisolationbeans.RequestInfo;
import org.onap.so.apihandlerinfra.tenantisolationbeans.RequestParameters;
import org.onap.so.client.aai.AAIVersion;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;


public class CreateEcompOperationalEnvironmentTest extends BaseTest{
	
	@Autowired
	private CreateEcompOperationalEnvironment createEcompOpEn;
	private final ObjectMapper mapper = new ObjectMapper();

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
	public void testProcess() throws ApiException, JsonProcessingException {
		stubFor(put(urlPathMatching("/aai/" + AAIVersion.LATEST + "/cloud-infrastructure/operational-environments/.*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_ACCEPTED)));
		stubFor(post(urlPathMatching("/events/.*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_ACCEPTED)));
		
		InfraActiveRequests iar = new InfraActiveRequests();
		iar.setRequestId("123");
		iar.setOperationalEnvName("myOpEnv");
		iar.setRequestScope("create");
		iar.setRequestStatus("PENDING");
		iar.setRequestAction("UNKNOWN");
		stubFor(get(urlPathEqualTo("/infraActiveRequests/123"))
				.willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
						.withBody(mapper.writeValueAsString(iar))
						.withStatus(HttpStatus.SC_OK)));
		stubFor(post(urlPathEqualTo("/infraActiveRequests/"))
				.withRequestBody(containing("{\"requestId\":\"123\",\"clientRequestId\":null,\"action\":null,\"requestStatus\":\"COMPLETE\",\"statusMessage\":\"SUCCESSFUL, operationalEnvironmentId - operationalEnvId; Success Message: SUCCESSFULLY Created ECOMP OperationalEnvironment.\",\"rollbackStatusMessage\":null,\"flowStatus\":null,\"retryStatusMessage\":null,\"progress\":100"))
				.willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
						.withStatus(HttpStatus.SC_OK)));
		
		createEcompOpEn.execute("123", getCloudOrchestrationRequest());
	}
	
	@Test
	public void testProcessException() throws JsonProcessingException {
		stubFor(put(urlPathMatching("/aai/" + AAIVersion.LATEST + "/cloud-infrastructure/operational-environments/.*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_ACCEPTED)));
		stubFor(post(urlPathMatching("/events/.*"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_NOT_FOUND)));
        ErrorLoggerInfo errorLoggerInfo = new ErrorLoggerInfo.Builder(MessageEnum.APIH_GENERAL_EXCEPTION, MsoLogger.ErrorCode.DataError).build();
        ValidateException expectedException = new ValidateException.Builder("Could not publish DMaap", HttpStatus.SC_BAD_REQUEST, ErrorNumbers.SVC_BAD_PARAMETER)
                .errorInfo(errorLoggerInfo).build();
        
        InfraActiveRequests iar = new InfraActiveRequests();
        String uuid = UUID.randomUUID().toString();
		iar.setRequestId(uuid);
		iar.setOperationalEnvName("myOpEnv");
		iar.setRequestScope("create");
		iar.setRequestStatus("PENDING");
		iar.setRequestAction("UNKNOWN");
		stubFor(get(urlPathEqualTo("/infraActiveRequests/"+uuid))
				.willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
						.withBody(mapper.writeValueAsString(iar))
						.withStatus(HttpStatus.SC_OK)));
		stubFor(post(urlPathEqualTo("/infraActiveRequests/"))
				.withRequestBody(containing("{\"requestId\":\""+uuid+ "\",\"clientRequestId\":null,\"action\":null,\"requestStatus\":\"FAILED\",\"statusMessage\":\"FAILURE, operationalEnvironmentId - operationalEnvId; Error message:"))
				.willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
						.withStatus(HttpStatus.SC_OK)));

		try {
            createEcompOpEn.execute(uuid, getCloudOrchestrationRequest());
        }catch(ApiException e){
            assertThat(e, sameBeanAs((ApiException) expectedException).ignoring("cause"));
        }
	}

}
