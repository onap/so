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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.mso.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.openecomp.mso.apihandlerinfra.tenantisolation.dmaap.DmaapOperationalEnvClient;
import org.openecomp.mso.apihandlerinfra.tenantisolation.helpers.AAIClientHelper;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.OperationalEnvironment;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.RequestDetails;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.RequestInfo;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.RequestParameters;
import org.openecomp.mso.client.aai.objects.AAIOperationalEnvironment;
import org.openecomp.mso.requestsdb.RequestsDBHelper;

public class CreateEcompOperationalEnvironmentTest {
	
	@Mock private AAIClientHelper mockAaiClientHelper;
	@Mock private DmaapOperationalEnvClient mockDmaapClient;
	@Mock private RequestsDBHelper mockRequestsDBHelper;
	
	private CloudOrchestrationRequest request;
	private CreateEcompOperationalEnvironment spyProcess;
	
	public CreateEcompOperationalEnvironmentTest() {
		super();
	}
	 
	@Before
     public void testSetUp() {
		MockitoAnnotations.initMocks(this);
		request = getCloudOrchestrationRequest();
	    CreateEcompOperationalEnvironment process  = new CreateEcompOperationalEnvironment(request, "123");
		spyProcess = spy(process);
		when(spyProcess.getAaiHelper()).thenReturn(mockAaiClientHelper);
		when(spyProcess.getDmaapClient()).thenReturn(mockDmaapClient);
		when(spyProcess.getRequestDb()).thenReturn(mockRequestsDBHelper);
	}
	
	public CloudOrchestrationRequest getCloudOrchestrationRequest() {
		CloudOrchestrationRequest cor = new CloudOrchestrationRequest();
		RequestDetails reqDetails = new RequestDetails();
		RequestInfo reqInfo = new RequestInfo();
		RequestParameters reqParams = new RequestParameters();
		reqParams.setTenantContext("TEST");
		reqParams.setWorkloadContext("ECOMP_TEST");
		reqParams.setOperationalEnvironmentType(OperationalEnvironment.ECOMP);
		reqInfo.setInstanceName("TEST_ECOMP_ENVIRONMENT");
		reqDetails.setRequestInfo(reqInfo);
		reqDetails.setRequestParameters(reqParams);
		cor.setRequestDetails(reqDetails);
		return cor;
	}
	
	@Test
	public void testProcess() throws Exception {
		spyProcess.execute();
		verify(mockAaiClientHelper, times(1)).createOperationalEnvironment(any(AAIOperationalEnvironment.class));
		verify(mockDmaapClient, times(1)).dmaapPublishOperationalEnvRequest(any(String.class), any(String.class), any(String.class), any(String.class), any(String.class), any(String.class) );
		verify(mockRequestsDBHelper, times(1)).updateInfraSuccessCompletion(any(String.class), any(String.class), any(String.class));
	}

}
