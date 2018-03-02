package org.openecomp.mso.apihandlerinfra.tenantisolation.helpers;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.mso.apihandlerinfra.tenantisolation.CloudOrchestrationRequest;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.OperationalEnvironment;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.RequestDetails;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.RequestInfo;
import org.openecomp.mso.apihandlerinfra.tenantisolationbeans.RequestParameters;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AAIClientObjectBuilderTest {

	private String expectedAAIObject = "{\"operational-environment-name\":\"TEST_ECOMP_ENVIRONMENT\",\"operational-environment-type\":\"ECOMP\",\"operational-environment-status\":\"Active\",\"tenant-context\":\"TEST\",\"workload-context\":\"ECOMP_TEST\"}";
	private CloudOrchestrationRequest request;
	private ObjectMapper mapper = new ObjectMapper();
	
	@Before
    public void testSetUp() {
		request = getCloudOrchestrationRequest();
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
	public void testGetAaiClientObjectBuilder() throws Exception {
		AAIClientObjectBuilder builder = new AAIClientObjectBuilder(request);
		assertEquals(expectedAAIObject, mapper.writeValueAsString(builder.buildAAIOperationalEnvironment("Active")));
	}
	
	
}
