/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.apihandlerinfra;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ServiceRecipe;
import org.onap.so.db.request.beans.OperationStatus;
import org.onap.so.serviceinstancebeans.RequestError;
import org.onap.so.serviceinstancebeans.ServiceException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.http.Fault;

public class E2EServiceInstancesTest extends BaseTest {
    private final ObjectMapper mapper = new ObjectMapper();

    private final String e2eServInstancesUri = "/onap/so/infra/e2eServiceInstances/";


    @Before
    public void init() throws JsonProcessingException {
        wireMockServer.stubFor(post(urlPathEqualTo("/testOrchestrationUri")).willReturn(aResponse()
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).withStatus(HttpStatus.SC_OK)));
        wireMockServer.stubFor(post(urlPathEqualTo("/infraActiveRequests/")).withRequestBody(equalToJson(
                "{\"requestStatus\":\"FAILED\",\"statusMessage\":\"Error parsing request: No valid requestorId is specified\",\"progress\":100,\"startTime\":1533541051247,\"endTime\":1533541051247,\"source\":null,\"vnfId\":null,\"vnfName\":null,\"vnfType\":null,\"serviceType\":null,\"tenantId\":null,\"vnfParams\":null,\"vnfOutputs\":null,\"requestBody\":\"{\\r\\n   \\\"service\\\":{\\r\\n      \\\"name\\\":\\\"so_test4\\\",\\r\\n      \\\"description\\\":\\\"so_test2\\\",\\r\\n      \\\"serviceInvariantUuid\\\":\\\"60c3e96e-0970-4871-b6e0-3b6de7561519\\\",\\r\\n      \\\"serviceUuid\\\":\\\"592f9437-a9c0-4303-b9f6-c445bb7e9814\\\",\\r\\n      \\\"globalSubscriberId\\\":\\\"123457\\\",\\r\\n      \\\"serviceType\\\":\\\"voLTE\\\",\\r\\n      \\\"parameters\\\":{\\r\\n         \\\"resources\\\":[\\r\\n            {\\r\\n               \\\"resourceName\\\":\\\"vIMS\\\",\\r\\n               \\\"resourceInvariantUuid\\\":\\\"60c3e96e-0970-4871-b6e0-3b6de7561516\\\",\\r\\n               \\\"resourceUuid\\\":\\\"60c3e96e-0970-4871-b6e0-3b6de7561512\\\",\\r\\n               \\\"parameters\\\":{\\r\\n                  \\\"locationConstraints\\\":[\\r\\n                     {\\r\\n                        \\\"vnfProfileId\\\":\\\"zte-vBAS-1.0\\\",\\r\\n                        \\\"locationConstraints\\\":{\\r\\n                           \\\"vimId\\\":\\\"4050083f-465f-4838-af1e-47a545222ad0\\\"\\r\\n                        }\\r\\n                     },\\r\\n                     {\\r\\n                        \\\"vnfProfileId\\\":\\\"zte-vMME-1.0\\\",\\r\\n                        \\\"locationConstraints\\\":{\\r\\n                           \\\"vimId\\\":\\\"4050083f-465f-4838-af1e-47a545222ad0\\\"\\r\\n                        }\\r\\n                     }\\r\\n                  ]\\r\\n               }\\r\\n            },\\r\\n            {\\r\\n               \\\"resourceName\\\":\\\"vEPC\\\",\\r\\n               \\\"resourceInvariantUuid\\\":\\\"61c3e96e-0970-4871-b6e0-3b6de7561516\\\",\\r\\n               \\\"resourceUuid\\\":\\\"62c3e96e-0970-4871-b6e0-3b6de7561512\\\",\\r\\n               \\\"parameters\\\":{\\r\\n                  \\\"locationConstraints\\\":[\\r\\n                     {\\r\\n                        \\\"vnfProfileId\\\":\\\"zte-CSCF-1.0\\\",\\r\\n                        \\\"locationConstraints\\\":{\\r\\n                           \\\"vimId\\\":\\\"4050083f-465f-4838-af1e-47a545222ad1\\\"\\r\\n                        }\\r\\n                     }\\r\\n                  ]\\r\\n               }\\r\\n            },\\r\\n            {\\r\\n               \\\"resourceName\\\":\\\"underlayvpn\\\",\\r\\n               \\\"resourceInvariantUuid\\\":\\\"60c3e96e-0970-4871-b6e0-3b6de7561513\\\",\\r\\n               \\\"resourceUuid\\\":\\\"60c3e96e-0970-4871-b6e0-3b6de7561514\\\",\\r\\n               \\\"parameters\\\":{\\r\\n                  \\\"locationConstraints\\\":[\\r\\n\\r\\n                  ]\\r\\n               }\\r\\n            },\\r\\n            {\\r\\n               \\\"resourceName\\\":\\\"overlayvpn\\\",\\r\\n               \\\"resourceInvariantUuid\\\":\\\"60c3e96e-0970-4871-b6e0-3b6de7561517\\\",\\r\\n               \\\"resourceUuid\\\":\\\"60c3e96e-0970-4871-b6e0-3b6de7561518\\\",\\r\\n               \\\"parameters\\\":{\\r\\n                  \\\"locationConstraints\\\":[\\r\\n\\r\\n                  ]\\r\\n               }\\r\\n            }\\r\\n         ],\\r\\n         \\\"requestInputs\\\":{\\r\\n            \\\"externalDataNetworkName\\\":\\\"Flow_out_net\\\",\\r\\n            \\\"m6000_mng_ip\\\":\\\"181.18.20.2\\\",\\r\\n            \\\"externalCompanyFtpDataNetworkName\\\":\\\"Flow_out_net\\\",\\r\\n            \\\"externalPluginManageNetworkName\\\":\\\"plugin_net_2014\\\",\\r\\n            \\\"externalManageNetworkName\\\":\\\"mng_net_2017\\\",\\r\\n            \\\"sfc_data_network\\\":\\\"sfc_data_net_2016\\\",\\r\\n            \\\"NatIpRange\\\":\\\"210.1.1.10-210.1.1.20\\\",\\r\\n            \\\"location\\\":\\\"4050083f-465f-4838-af1e-47a545222ad0\\\",\\r\\n            \\\"sdncontroller\\\":\\\"9b9f02c0-298b-458a-bc9c-be3692e4f35e\\\"\\r\\n         }\\r\\n      }\\r\\n\\r\\n   }\\r\\n\\r\\n}\",\"responseBody\":null,\"lastModifiedBy\":\"APIH\",\"modifyTime\":null,\"volumeGroupId\":null,\"volumeGroupName\":null,\"vfModuleId\":null,\"vfModuleName\":null,\"vfModuleModelName\":null,\"cloudRegion\":null,\"callBackUrl\":null,\"correlator\":null,\"serviceInstanceId\":null,\"serviceInstanceName\":null,\"requestScope\":\"service\",\"requestAction\":\"createInstance\",\"networkId\":null,\"networkName\":null,\"networkType\":null,\"requestorId\":null,\"configurationId\":null,\"configurationName\":null,\"operationalEnvId\":null,\"operationalEnvName\":null,\"requestURI\":\"d167c9d0-1785-4e93-b319-996ebbcc3272\"}"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withStatus(HttpStatus.SC_OK)));
        Service defaultService = new Service();
        defaultService.setModelUUID("d88da85c-d9e8-4f73-b837-3a72a431622a");
        ServiceRecipe serviceRecipe = new ServiceRecipe();
        serviceRecipe.setServiceModelUUID(defaultService.getModelUUID());
        serviceRecipe.setRecipeTimeout(180);
        serviceRecipe.setOrchestrationUri("/testOrchestrationUri");

        wireMockServer.stubFor(get(urlPathEqualTo("/service/search/findFirstByModelNameOrderByModelVersionDesc"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        wireMockServer.stubFor(get(urlPathEqualTo("/serviceRecipe/search/findFirstByServiceModelUUIDAndAction"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(serviceRecipe)).withStatus(HttpStatus.SC_OK)));

    }

    public String inputStream(String JsonInput) throws IOException {
        JsonInput = "src/test/resources/E2EServiceInstancesTest" + JsonInput;
        return new String(Files.readAllBytes(Paths.get(JsonInput)));
    }

    public ResponseEntity<String> sendRequest(String requestJson, String uriPath, HttpMethod reqMethod) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(uriPath));
        HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

        return restTemplate.exchange(builder.toUriString(), reqMethod, request, String.class);
    }

    @Test
    public void createE2EServiceInstanceNoRequestInfo() throws IOException {
        String uri = e2eServInstancesUri + "v3";
        ResponseEntity<String> response = sendRequest(inputStream("/Request.json"), uri, HttpMethod.POST);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void updateE2EServiceInstanceJSONMappingError() throws IOException {
        String uri = e2eServInstancesUri + "v5/9b9f02c0-298b-458a-bc9c-be3692e4f35e";
        ResponseEntity<String> response = sendRequest(inputStream("/CompareModelRequest.json"), uri, HttpMethod.PUT);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertTrue(realResponse.getServiceException().getText().contains("Mapping of request to JSON object failed"));
    }

    @Test
    public void updateE2EServiceInstanceNoRequestorId() throws IOException {
        RequestError expectedResponse = new RequestError();
        ServiceException exception = new ServiceException();
        exception.setMessageId("SVC0002");
        exception.setText("Error parsing request.  Error parsing request: No valid requestorId is specified");
        expectedResponse.setServiceException(exception);

        String uri = e2eServInstancesUri + "v5/9b9f02c0-298b-458a-bc9c-be3692e4f35e";
        ResponseEntity<String> response = sendRequest(inputStream("/Request.json"), uri, HttpMethod.PUT);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertThat(realResponse, sameBeanAs(expectedResponse));
    }

    @Test
    public void deleteE2EServiceInstance() throws IOException {
        RequestError expectedResponse = new RequestError();
        ServiceException exception = new ServiceException();
        exception.setMessageId("SVC1000");
        exception.setText("No communication to catalog DB null");
        expectedResponse.setServiceException(exception);

        String uri = e2eServInstancesUri + "v5/9b9f02c0-298b-458a-bc9c-be3692e4f35e";
        ResponseEntity<String> response = sendRequest(inputStream("/DeleteRequest.json"), uri, HttpMethod.DELETE);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void deleteE2EServiceInstanceNotValid() throws IOException {
        String uri = e2eServInstancesUri + "v5/9b9f02c0-298b-458a-bc9c-be3692e4f35e";
        ResponseEntity<String> response = sendRequest(inputStream("/Request.json"), uri, HttpMethod.DELETE);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertTrue(realResponse.getServiceException().getText().contains("Mapping of request to JSON object failed"));
    }

    @Test
    public void getE2EServiceInstanceNullOperationalStatus() throws IOException {
        String uri = e2eServInstancesUri
                + "v5/9b9f02c0-298b-458a-bc9c-be3692e4f35e/operations/9b9f02c0-298b-458a-bc9c-be3692e4f35e";
        wireMockServer.stubFor(get(urlPathEqualTo("/operationStatus/search/findOneByServiceIdAndOperationId"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withStatus(HttpStatus.SC_OK)));
        ResponseEntity<String> response = sendRequest(inputStream("/Request.json"), uri, HttpMethod.GET);

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void scaleE2EServiceInstanceMappingError() throws IOException {
        String uri = e2eServInstancesUri + "v5/9b9f02c0-298b-458a-bc9c-be3692e4f35e/scale";
        ResponseEntity<String> response = sendRequest(inputStream("/Request.json"), uri, HttpMethod.POST);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertTrue(realResponse.getServiceException().getText().contains("Mapping of request to JSON object failed"));
    }

    @Test
    public void scaleE2EServiceInstance() throws IOException {
        String uri = e2eServInstancesUri + "v5/9b9f02c0-298b-458a-bc9c-be3692e4f35e/scale";
        ResponseEntity<String> response = sendRequest(inputStream("/ScaleRequest.json"), uri, HttpMethod.POST);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void updateE2EServiceInstance() throws IOException {
        String uri = e2eServInstancesUri + "v3/9b9f02c0-298b-458a-bc9c-be3692e4f35e";
        ResponseEntity<String> response = sendRequest(inputStream("/Request.json"), uri, HttpMethod.PUT);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void getE2EServiceInstance() throws IOException {
        OperationStatus status = new OperationStatus();
        status.setOperationId("operationId");
        status.setServiceId("9b9f02c0-298b-458a-bc9c-be3692e4f35e");
        wireMockServer.stubFor(get(urlPathEqualTo("/operationStatus/search/findOneByServiceIdAndOperationId"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(status)).withStatus(HttpStatus.SC_OK)));
        String uri = e2eServInstancesUri + "v3/9b9f02c0-298b-458a-bc9c-be3692e4f35e/operations/operationId";
        ResponseEntity<String> response = sendRequest("", uri, HttpMethod.GET);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
    }

    @Test
    public void compareModelWithTargetVersionBadRequest() throws IOException {
        String uri = e2eServInstancesUri + "v5/9b9f02c0-298b-458a-bc9c-be3692e4f35e/modeldifferences";
        ResponseEntity<String> response = sendRequest(inputStream("/Request.json"), uri, HttpMethod.POST);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertTrue(realResponse.getServiceException().getText().contains("Mapping of request to JSON object failed"));
    }

    @Test
    public void compareModelWithTargetVersion() throws IOException {
        wireMockServer.stubFor(
                post(urlPathEqualTo("/mso/async/services/CompareModelofE2EServiceInstance")).willReturn(aResponse()
                        .withHeader("Content-Type", "application/json").withBodyFile("Camunda/SuccessfulResponse.json")
                        .withStatus(org.apache.http.HttpStatus.SC_ACCEPTED)));

        String expectedResponse = "success";
        String uri = e2eServInstancesUri + "v5/9b9f02c0-298b-458a-bc9c-be3692e4f35e/modeldifferences";
        ResponseEntity<String> response = sendRequest(inputStream("/CompareModelRequest.json"), uri, HttpMethod.POST);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
        String actualResponse = response.getBody();
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void compareModelWithTargetVersionEmptyResponse() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/CompareModelofE2EServiceInstance"))
                .willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));

        RequestError expectedResponse = new RequestError();
        ServiceException exception = new ServiceException();
        exception.setMessageId("SVC1000");
        exception.setText("Failed calling bpmn Client from http://localhost:" + env.getProperty("wiremock.server.port")
                + "/mso/async/services/CompareModelofE2EServiceInstance failed to connect or respond");
        expectedResponse.setServiceException(exception);

        String uri = e2eServInstancesUri + "v5/9b9f02c0-298b-458a-bc9c-be3692e4f35e/modeldifferences";
        ResponseEntity<String> response = sendRequest(inputStream("/CompareModelRequest.json"), uri, HttpMethod.POST);

        assertEquals(Response.Status.BAD_GATEWAY.getStatusCode(), response.getStatusCode().value());
        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertThat(realResponse, sameBeanAs(expectedResponse));
    }

    @Test
    public void compareModelWithTargetVersionBadBpelResponse() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/CompareModelofE2EServiceInstance")).willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBodyFile("Camunda/TestResponse.json")
                        .withStatus(org.apache.http.HttpStatus.SC_BAD_GATEWAY)));

        String uri = e2eServInstancesUri + "v5/9b9f02c0-298b-458a-bc9c-be3692e4f35e/modeldifferences";
        ResponseEntity<String> response = sendRequest(inputStream("/CompareModelRequest.json"), uri, HttpMethod.POST);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertTrue(realResponse.getServiceException().getText()
                .contains("Request Failed due to BPEL error with HTTP Status"));
    }

    @Test
    public void compareModelWithTargetVersionNoBPELResponse() throws IOException {
        wireMockServer.stubFor(post(urlPathEqualTo("/mso/async/services/CompareModelofE2EServiceInstance"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("{}")
                        .withStatus(org.apache.http.HttpStatus.SC_BAD_GATEWAY)));

        String uri = e2eServInstancesUri + "v5/9b9f02c0-298b-458a-bc9c-be3692e4f35e/modeldifferences";
        ResponseEntity<String> response = sendRequest(inputStream("/CompareModelRequest.json"), uri, HttpMethod.POST);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
        RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
        assertTrue(realResponse.getServiceException().getText()
                .contains("Request Failed due to BPEL error with HTTP Status"));
    }
}
