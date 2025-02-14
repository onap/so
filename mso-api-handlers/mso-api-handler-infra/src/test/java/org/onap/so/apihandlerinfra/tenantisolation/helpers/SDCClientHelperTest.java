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

package org.onap.so.apihandlerinfra.tenantisolation.helpers;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.onap.so.apihandlerinfra.BaseTest;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.springframework.beans.factory.annotation.Autowired;

public class SDCClientHelperTest extends BaseTest {

    String serviceModelVersionId = "TEST_uuid1";
    String operationalEnvironmentId = "TEST_operationalEnvironmentId";
    String workloadContext = "TEST_workloadContext";

    @Autowired
    private SDCClientHelper sdcClientUtils;

    @Test
    public void postActivateOperationalEnvironment_Test() throws ApiException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("statusCode", "202");
        jsonObject.put("message", "Success");
        jsonObject.put("distributionId", "TEST_distributionId");

        wireMockServer.stubFor(post(urlPathMatching("/sdc/v1/catalog/services/TEST_uuid1/distr.*"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(jsonObject.toString())
                        .withStatus(HttpStatus.SC_ACCEPTED)));

        JSONObject jsonResponse = sdcClientUtils.postActivateOperationalEnvironment(serviceModelVersionId,
                operationalEnvironmentId, workloadContext);

        assertEquals("202", jsonResponse.get("statusCode"));
        assertEquals("Success", jsonResponse.get("message"));

    }

    @Test
    public void postActivateOperationalEnvironment_InvalidJson_Test() throws ApiException {

        // ERROR in asdc response, invalid json object
        JSONObject jsonErrorResponse = new JSONObject();
        jsonErrorResponse.put("requestError", "");

        wireMockServer.stubFor(post(urlPathMatching("/sdc/v1/catalog/services/TEST_uuid1/distr.*"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody(jsonErrorResponse.toString()).withStatus(HttpStatus.SC_BAD_REQUEST)));

        JSONObject jsonResponse = sdcClientUtils.postActivateOperationalEnvironment(serviceModelVersionId,
                operationalEnvironmentId, workloadContext);

        assertEquals("500", jsonResponse.get("statusCode"));
        assertEquals("", jsonResponse.get("messageId"));
        assertEquals(
                " Encountered Error while calling SDC POST Activate. JSONObject[\"requestError\"] is not a JSONObject (class java.lang.String : ).",
                jsonResponse.get("message"));

    }

    @Test
    public void buildUriBuilderTest() {

        try {
            String url = sdcClientUtils.buildUriBuilder(serviceModelVersionId, operationalEnvironmentId);
            assertEquals(
                    "http://localhost:" + env.getProperty("wiremock.server.port")
                            + "/sdc/v1/catalog/services/TEST_uuid1/distribution/TEST_operationalEnvironmentId/activate",
                    url);

        } catch (Exception e) {
            fail("Exception caught: " + e.getMessage());

        }
    }


    @Test
    public void buildJsonWorkloadContextTest() throws JSONException {

        String jsonPayload = sdcClientUtils.buildJsonWorkloadContext(workloadContext);
        assertEquals("{\"workloadContext\":\"TEST_workloadContext\"}", jsonPayload);

    }

    @Test
    public void enhanceJsonResponseTest_Success() throws JSONException {

        // build success response data
        JSONObject sdcResponseJsonObj = new JSONObject();
        sdcResponseJsonObj.put("distributionId", "TEST_distributionId");

        int statusCode = 202;
        sdcResponseJsonObj = sdcClientUtils.enhanceJsonResponse(sdcResponseJsonObj, statusCode);

        assertEquals("202", sdcResponseJsonObj.getString("statusCode"));
        assertEquals("", sdcResponseJsonObj.getString("messageId"));
        assertEquals("Success", sdcResponseJsonObj.getString("message"));
        assertEquals("TEST_distributionId", sdcResponseJsonObj.getString("distributionId"));

    }

    @Test
    public void enhanceJsonResponseTest_Error() throws JSONException {

        // build error response data
        JSONObject jsonMessages = new JSONObject();
        jsonMessages.put("messageId", "SVC4675");
        jsonMessages.put("text", "Error: Service state is invalid for this action.");
        JSONObject jsonServException = new JSONObject();
        jsonServException.put("serviceException", jsonMessages);
        JSONObject jsonErrorRequest = new JSONObject();
        jsonErrorRequest.put("requestError", jsonServException);

        String responseData = jsonErrorRequest.toString();

        JSONObject sdcResponseJsonObj = new JSONObject(responseData);
        int statusCode = 409;
        sdcResponseJsonObj = sdcClientUtils.enhanceJsonResponse(sdcResponseJsonObj, statusCode);

        assertEquals("409", sdcResponseJsonObj.getString("statusCode"));
        assertEquals("SVC4675", sdcResponseJsonObj.getString("messageId"));
        assertEquals("Error: Service state is invalid for this action.", sdcResponseJsonObj.getString("message"));

    }

    @Test
    public void enhanceJsonResponseTest_Error_policyException() throws JSONException {

        // build error response data
        JSONObject jsonMessages = new JSONObject();
        jsonMessages.put("messageId", "POL5003");
        jsonMessages.put("text", "Error: Not authorized to use the API.");
        JSONObject jsonServException = new JSONObject();
        jsonServException.put("policyException", jsonMessages);
        JSONObject jsonErrorRequest = new JSONObject();
        jsonErrorRequest.put("requestError", jsonServException);

        String responseData = jsonErrorRequest.toString();

        JSONObject sdcResponseJsonObj = new JSONObject(responseData);
        int statusCode = 403;
        sdcResponseJsonObj = sdcClientUtils.enhanceJsonResponse(sdcResponseJsonObj, statusCode);

        assertEquals("403", sdcResponseJsonObj.getString("statusCode"));
        assertEquals("POL5003", sdcResponseJsonObj.getString("messageId"));
        assertEquals("Error: Not authorized to use the API.", sdcResponseJsonObj.getString("message"));

    }

    @Test
    public void enhanceJsonResponseTest_Error_UnexpectedFormat() throws JSONException {

        // build error response data
        JSONObject jsonMessages = new JSONObject();
        jsonMessages.put("messageId", "POL5003");
        jsonMessages.put("text", "Error: Not authorized to use the API.");
        JSONObject jsonServException = new JSONObject();
        jsonServException.put("policyException", jsonMessages);
        JSONObject jsonErrorRequest = new JSONObject();
        jsonErrorRequest.put("unexpectedResponseTag", jsonServException);

        String responseData = jsonErrorRequest.toString();

        JSONObject sdcResponseJsonObj = new JSONObject(responseData);
        int statusCode = 403;
        sdcResponseJsonObj = sdcClientUtils.enhanceJsonResponse(sdcResponseJsonObj, statusCode);

        assertEquals("500", sdcResponseJsonObj.getString("statusCode"));
        assertEquals("Undefined Error Message!", sdcResponseJsonObj.getString("messageId"));
        assertEquals("Unexpected response format from SDC.", sdcResponseJsonObj.getString("message"));

    }

}
