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

package org.openecomp.mso.apihandlerinfra.tenantisolation.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.openecomp.mso.apihandlerinfra.Constants;
import org.openecomp.mso.apihandlerinfra.MsoPropertiesUtils;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.properties.MsoPropertiesFactory;
import org.openecomp.mso.rest.RESTClient;
import org.openecomp.mso.rest.RESTConfig;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class AsdcClientHelperTest {

	MsoJavaProperties properties = MsoPropertiesUtils.loadMsoProperties();
	AsdcClientHelper asdcClientUtils = new AsdcClientHelper(properties);
	
	String serviceModelVersionId = "TEST_uuid1";
	String operationalEnvironmentId = "TEST_operationalEnvironmentId";
	String workloadContext = "TEST_workloadContext";

	@Rule
	public final WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.wireMockConfig().port(28090)); //.extensions(transformerArray));
	
	@BeforeClass
	public static void setUp() throws Exception {
		MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();
		msoPropertiesFactory.removeAllMsoProperties();
		msoPropertiesFactory.initializeMsoProperties(Constants.MSO_PROP_APIHANDLER_INFRA, "src/test/resources/mso.apihandler-infra.properties");
	}	
	
	@After
	public void tearDown() throws Exception {
		
	}	
	
	@Test
	public void getPropertiesTest() {
		
		String asdcInstanceId = asdcClientUtils.getAsdcInstanceId(); 
		Assert.assertEquals("Asdc InstanceId - " , "test", asdcInstanceId);
		
		String asdcEndpoint = asdcClientUtils.getAsdcEndpoint(); 
		Assert.assertEquals("Asdc Endpoint - " , "http://localhost:28090", asdcEndpoint);

		String userid = asdcClientUtils.getAsdcUserId();
		Assert.assertEquals("userid - " , "cs0008", userid);
		
	}	
	
	@Test
	public void buildUriBuilderTest() {
		
		try {  
		String url = asdcClientUtils.buildUriBuilder(serviceModelVersionId, operationalEnvironmentId);
			assertEquals("http://localhost:28090/sdc/v1/catalog/services/TEST_uuid1/distribution/TEST_operationalEnvironmentId/activate", url);
			
		} catch (Exception e) {
			fail("Exception caught: " + e.getMessage());

		}	
	}	

	@Test
	public void buildJsonWorkloadContextTest() {
		
		try {  
			String jsonPayload = asdcClientUtils.buildJsonWorkloadContext(workloadContext);
			assertEquals("{\"workloadContext\":\"TEST_workloadContext\"}", jsonPayload);
			
		} catch (Exception e) {
			fail("Exception caught: " + e.getMessage());

		}	
	}		
	
	@Test
	public void setRestClientTest() {
		
		try {
			String url = asdcClientUtils.buildUriBuilder(serviceModelVersionId, operationalEnvironmentId);
			RESTConfig config = new RESTConfig(url);
			RESTClient client = asdcClientUtils.setRestClient(config);
			LinkedHashMap<String, List<String>> headers = client.getHeaders();
			assertEquals("[cs0008]", headers.get("USER_ID").toString());
			
		} catch (Exception e) {
			fail("Exception caught: " + e.getMessage());

		}	
	}
	
	@Test
	public void enhanceJsonResponseTest_Success() {
		
		try {
			// build success response data
			JSONObject asdcResponseJsonObj = new JSONObject();
			asdcResponseJsonObj.put("distributionId", "TEST_distributionId");

			int statusCode = 202;
			asdcResponseJsonObj = asdcClientUtils.enhanceJsonResponse(asdcResponseJsonObj, statusCode);
			
			assertEquals("202", asdcResponseJsonObj.getString("statusCode"));
			assertEquals("", asdcResponseJsonObj.getString("messageId"));
			assertEquals("Success", asdcResponseJsonObj.getString("message"));
			assertEquals("TEST_distributionId", asdcResponseJsonObj.getString("distributionId"));
			
		} catch (Exception e) {
			fail("Exception caught: " + e.getMessage());

		}	
	}	
	
	@Test
	public void enhanceJsonResponseTest_Error() {
		
		try {
			
			// build error response data
			JSONObject jsonMessages = new JSONObject();
			jsonMessages.put("messageId", "SVC4675");
			jsonMessages.put("text", "Error: Service state is invalid for this action.");
			JSONObject jsonServException = new JSONObject();
			jsonServException.put("serviceException", jsonMessages);
			JSONObject jsonErrorRequest = new JSONObject();
			jsonErrorRequest.put("requestError", jsonServException);

			String responseData =  jsonErrorRequest.toString();
			
			JSONObject asdcResponseJsonObj = new JSONObject(responseData);
			int statusCode = 409;
			asdcResponseJsonObj = asdcClientUtils.enhanceJsonResponse(asdcResponseJsonObj, statusCode);
			
			assertEquals("409", asdcResponseJsonObj.getString("statusCode"));
			assertEquals("SVC4675", asdcResponseJsonObj.getString("messageId"));
			assertEquals("Error: Service state is invalid for this action.", asdcResponseJsonObj.getString("message"));

			
		} catch (Exception e) {
			fail("Exception caught: " + e.getMessage());

		}	
	}		

	@Test
	public void enhanceJsonResponseTest_Error_policyException() {
		
		try {
			
			// build error response data
			JSONObject jsonMessages = new JSONObject();
			jsonMessages.put("messageId", "POL5003");
			jsonMessages.put("text", "Error: Not authorized to use the API.");
			JSONObject jsonServException = new JSONObject();
			jsonServException.put("policyException", jsonMessages);
			JSONObject jsonErrorRequest = new JSONObject();
			jsonErrorRequest.put("requestError", jsonServException);

			String responseData =  jsonErrorRequest.toString();
			
			JSONObject asdcResponseJsonObj = new JSONObject(responseData);
			int statusCode = 403;
			asdcResponseJsonObj = asdcClientUtils.enhanceJsonResponse(asdcResponseJsonObj, statusCode);
			
			assertEquals("403", asdcResponseJsonObj.getString("statusCode"));
			assertEquals("POL5003", asdcResponseJsonObj.getString("messageId"));
			assertEquals("Error: Not authorized to use the API.", asdcResponseJsonObj.getString("message"));

			
		} catch (Exception e) {
			fail("Exception caught: " + e.getMessage());

		}	
	}			
	
}
