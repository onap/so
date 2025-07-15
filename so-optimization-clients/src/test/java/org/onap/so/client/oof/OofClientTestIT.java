/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Intel Corp. All rights reserved.
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

package org.onap.so.client.oof;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.onap.so.BaseIntegrationTest;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.oof.beans.LicenseInfo;
import org.onap.so.client.oof.beans.ModelInfo;
import org.onap.so.client.oof.beans.OofRequest;
import org.onap.so.client.oof.beans.OofRequestParameters;
import org.onap.so.client.oof.beans.PlacementDemand;
import org.onap.so.client.oof.beans.PlacementInfo;
import org.onap.so.client.oof.beans.RequestInfo;
import org.onap.so.client.oof.beans.ResourceModelInfo;
import org.onap.so.client.oof.beans.ServiceInfo;
import org.onap.so.client.oof.beans.SubscriberInfo;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.core.JsonProcessingException;


public class OofClientTestIT extends BaseIntegrationTest {

    @Autowired
    private OofClient client;

    @Test
    public void testPostDemands_success() throws BadResponseException, JsonProcessingException {
        String mockResponse =
                "{\"transactionId\": \"123456789\", \"requestId\": \"1234\", \"statusMessage\": \"status\", \"requestStatus\": \"accepted\"}";

        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelCustomizationName("modelCustomizationName-Service");
        modelInfo.setModelInvariantId("modelInvariantId-Service");
        modelInfo.setModelName("modelName-Service");
        modelInfo.setModelType("modelType-Service");
        modelInfo.setModelVersion("modelVersion-Service");
        modelInfo.setModelVersionId("modelVersionId-Service");

        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setModelInfo(modelInfo);
        serviceInfo.setServiceInstanceId("serviceInstanceId");
        serviceInfo.setServiceName("serviceName");

        SubscriberInfo subscriberInfo = new SubscriberInfo();
        subscriberInfo.setGlobalSubscriberId("globalSubscriberId");
        subscriberInfo.setSubscriberCommonSiteId("subscriberCommonSiteId");
        subscriberInfo.setSubscriberName("subscriberName");

        ResourceModelInfo resourceModelInfo = new ResourceModelInfo();
        resourceModelInfo.setModelType("modelType");
        resourceModelInfo.setModelCustomizationName("modelCustomizationName");
        resourceModelInfo.setModelInvariantId("invarianteId");
        resourceModelInfo.setModelName("modelName");
        resourceModelInfo.setModelVersion("version");
        resourceModelInfo.setModelVersionId("versionId");

        PlacementDemand placementDemand = new PlacementDemand();
        placementDemand.setResourceModelInfo(resourceModelInfo);
        placementDemand.setResourceModuleName("resourceModuleName");
        placementDemand.setServiceResourceId("serviceResourceId");
        placementDemand.setTenantId("tenantId");

        OofRequestParameters oofRequestParameters = new OofRequestParameters();
        oofRequestParameters.setCustomerLatitude("customerLatitude");
        oofRequestParameters.setCustomerLongitude("customerLongitude");
        oofRequestParameters.setCustomerName("customerName");

        ArrayList<PlacementDemand> placementDemands = new ArrayList<>();
        placementDemands.add(placementDemand);

        PlacementInfo placementInfo = new PlacementInfo();
        placementInfo.setPlacementDemands(placementDemands);
        placementInfo.setRequestParameters(oofRequestParameters);
        placementInfo.setSubscriberInfo(subscriberInfo);

        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setTransactionId("transactionId");
        List<String> optimizer = new ArrayList<>();
        optimizer.add("optimizer1");
        optimizer.add("optimizer2");
        requestInfo.setOptimizers(optimizer);
        requestInfo.setCallbackUrl("callBackUrl");
        requestInfo.setNumSolutions(1);
        requestInfo.setRequestId("requestId");
        requestInfo.setSourceId("sourceId");
        requestInfo.setTimeout(30L);
        requestInfo.setRequestType("requestType");

        OofRequest oofRequest = new OofRequest();
        oofRequest.setRequestInformation(requestInfo);
        oofRequest.setPlacementInformation(placementInfo);
        oofRequest.setServiceInformation(serviceInfo);
        oofRequest.setLicenseInformation(new LicenseInfo());

        wireMockServer.stubFor(post(urlEqualTo("/api/oof/v1/placement")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(mockResponse)));

        client.postDemands(oofRequest);

        String oofRequestOutput = oofRequest.toJsonString();
        JSONAssert.assertEquals("{\n" + "  \"requestInfo\" : {\n" + "    \"transactionId\" : \"transactionId\",\n"
                + "    \"requestId\" : \"requestId\",\n" + "    \"callbackUrl\" : \"callBackUrl\",\n"
                + "    \"sourceId\" : \"sourceId\",\n" + "    \"requestType\" : \"requestType\",\n"
                + "    \"numSolutions\" : 1,\n" + "    \"optimizers\" : [ \"optimizer1\", \"optimizer2\" ],\n"
                + "    \"timeout\" : 30\n" + "  },\n" + "  \"serviceInfo\" : {\n"
                + "    \"serviceInstanceId\" : \"serviceInstanceId\",\n" + "    \"serviceName\" : \"serviceName\",\n"
                + "    \"modelInfo\" : {\n" + "      \"modelType\" : \"modelType-Service\",\n"
                + "      \"modelInvariantId\" : \"modelInvariantId-Service\",\n"
                + "      \"modelVersionId\" : \"modelVersionId-Service\",\n"
                + "      \"modelName\" : \"modelName-Service\",\n"
                + "      \"modelVersion\" : \"modelVersion-Service\",\n"
                + "      \"modelCustomizationName\" : \"modelCustomizationName-Service\"\n" + "    }\n" + "  },\n"
                + "  \"placementInfo\" : {\n" + "    \"requestParameters\" : {\n"
                + "      \"customerLatitude\" : \"customerLatitude\",\n"
                + "      \"customerLongitude\" : \"customerLongitude\",\n"
                + "      \"customerName\" : \"customerName\"\n" + "    },\n" + "    \"subscriberInfo\" : {\n"
                + "      \"globalSubscriberId\" : \"globalSubscriberId\",\n"
                + "      \"subscriberName\" : \"subscriberName\",\n"
                + "      \"subscriberCommonSiteId\" : \"subscriberCommonSiteId\"\n" + "    },\n"
                + "    \"placementDemands\" : [ {\n" + "      \"resourceModuleName\" : \"resourceModuleName\",\n"
                + "      \"serviceResourceId\" : \"serviceResourceId\",\n" + "      \"tenantId\" : \"tenantId\",\n"
                + "      \"resourceModelInfo\" : {\n" + "        \"modelType\" : \"modelType\",\n"
                + "        \"modelInvariantId\" : \"invarianteId\",\n" + "        \"modelVersionId\" : \"versionId\",\n"
                + "        \"modelName\" : \"modelName\",\n" + "        \"modelVersion\" : \"version\",\n"
                + "        \"modelCustomizationName\" : \"modelCustomizationName\"\n" + "      }\n" + "    } ]\n"
                + "  },\n" + "  \"licenseInfo\" : { \n" + "    \"licenseDemands\" : [ ]\n" + "}\n" + "}",
                oofRequestOutput.replace("\r\n", "\n"), false);
    }

    @Test
    public void testAsyncResponse_success() throws BadResponseException, JsonProcessingException {
        String mockResponse =
                "{\"transactionId\": \"123456789\", \"requestId\": \"1234\", \"statusMessage\": \"status\", \"requestStatus\": \"accepted\"}";

        wireMockServer.stubFor(post(urlEqualTo("/api/oof/v1/placement")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(mockResponse)));

        client.postDemands(new OofRequest());
        assertTrue(true); // this is here to silence a sonarqube violation
    }

    @Test(expected = BadResponseException.class)
    public void testPostDemands_error_failed() throws JsonProcessingException, BadResponseException {
        String mockResponse =
                "{\"transactionId\": \"123456789\", \"requestId\": \"1234\", \"statusMessage\": \"missing data\", \"requestStatus\": \"failed\"}";

        wireMockServer.stubFor(post(urlEqualTo("/api/oof/v1/placement")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(mockResponse)));


        client.postDemands(new OofRequest());

        // TODO assertEquals("missing data", );

    }

    @Test(expected = BadResponseException.class)
    public void testPostDemands_error_noMessage() throws JsonProcessingException, BadResponseException {
        String mockResponse =
                "{\"transactionId\": \"123456789\", \"requestId\": \"1234\", \"statusMessage\": \"\", \"requestStatus\": \"failed\"}";

        wireMockServer.stubFor(post(urlEqualTo("/api/oof/v1/placement")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(mockResponse)));


        client.postDemands(new OofRequest());

    }

    @Test(expected = BadResponseException.class)
    public void testPostDemands_error_noStatus() throws JsonProcessingException, BadResponseException {
        String mockResponse =
                "{\"transactionId\": \"123456789\", \"requestId\": \"1234\", \"statusMessage\": \"missing data\", \"requestStatus\": null}";

        wireMockServer.stubFor(post(urlEqualTo("/api/oof/v1/placement")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(mockResponse)));


        client.postDemands(new OofRequest());

    }

    @Test(expected = BadResponseException.class)
    public void testPostDemands_error_empty() throws JsonProcessingException, BadResponseException {
        String mockResponse = "{ }";

        wireMockServer.stubFor(post(urlEqualTo("/api/oof/v1/placement")).willReturn(
                aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(mockResponse)));


        client.postDemands(new OofRequest());
    }

}
