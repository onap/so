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
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Ignore;
import org.junit.Test;
import org.onap.so.serviceinstancebeans.RequestError;
import org.onap.so.serviceinstancebeans.ServiceException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.http.Fault;

public class E2EServiceInstancesTest extends BaseTest {
private ObjectMapper mapper = new ObjectMapper();	
	
	private final String e2eServInstancesUri = "/e2eServiceInstances/";
	
	public String inputStream(String JsonInput)throws IOException{
		JsonInput = "src/test/resources/E2EServiceInstancesTest" + JsonInput;
		String input = new String(Files.readAllBytes(Paths.get(JsonInput)));
		return input;
	}
	public ResponseEntity<String> sendRequest(String requestJson, String uriPath, HttpMethod reqMethod){		 
		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type",MediaType.APPLICATION_JSON);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(uriPath));
		HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);  
		ResponseEntity<String> response = restTemplate.exchange(builder.toUriString(),
				reqMethod, request, String.class);
		
		return response;
	}
	//Currently returning a 500 response
	@Ignore
	@Test
	public void createE2EServiceInstanceNoRequestInfo() throws JsonParseException, JsonMappingException, IOException{
		String uri = e2eServInstancesUri + "v5";
		ResponseEntity<String> response = sendRequest(inputStream("/Request.json"), uri, HttpMethod.POST);
		
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
	}
	@Test
	public void updateE2EServiceInstanceJSONMappingError() throws JsonParseException, JsonMappingException, IOException{
		String uri = e2eServInstancesUri + "v5/9b9f02c0-298b-458a-bc9c-be3692e4f35e";
		ResponseEntity<String> response = sendRequest(inputStream("/CompareModelRequest.json"), uri, HttpMethod.PUT);
		
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
		RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
		assertTrue(realResponse.getServiceException().getText().contains("Mapping of request to JSON object failed"));
	}
	@Test
	public void updateE2EServiceInstanceNoRequestorId() throws JsonParseException, JsonMappingException, IOException{
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
	public void deleteE2EServiceInstanceNoRecipe() throws JsonParseException, JsonMappingException, IOException{
		RequestError expectedResponse = new RequestError();
		ServiceException exception = new ServiceException();
		exception.setMessageId("SVC1000");
		exception.setText("No communication to catalog DB null");
		expectedResponse.setServiceException(exception);
		
		String uri = e2eServInstancesUri + "v5/9b9f02c0-298b-458a-bc9c-be3692e4f35e";
		ResponseEntity<String> response = sendRequest(inputStream("/DeleteRequest.json"), uri, HttpMethod.DELETE);
		
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatusCode().value());
		RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
		assertThat(realResponse, sameBeanAs(expectedResponse));
	}
	@Test
	public void deleteE2EServiceInstanceNotValid() throws JsonParseException, JsonMappingException, IOException{
		String uri = e2eServInstancesUri + "v5/9b9f02c0-298b-458a-bc9c-be3692e4f35e";
		ResponseEntity<String> response = sendRequest(inputStream("/Request.json"), uri, HttpMethod.DELETE);
		
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
		RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
		assertTrue(realResponse.getServiceException().getText().contains("Mapping of request to JSON object failed"));
	}
	@Test
	public void getE2EServiceInstanceNullOperationalStatus() throws JsonParseException, JsonMappingException, IOException{
		String uri = e2eServInstancesUri + "v5/9b9f02c0-298b-458a-bc9c-be3692e4f35e/operations/9b9f02c0-298b-458a-bc9c-be3692e4f35e";
		ResponseEntity<String> response = sendRequest(inputStream("/Request.json"), uri, HttpMethod.GET);
		
		assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatusCode().value());
	}
	@Test
	public void scaleE2EServiceInstanceMappingError() throws JsonParseException, JsonMappingException, IOException{
		String uri = e2eServInstancesUri + "v5/9b9f02c0-298b-458a-bc9c-be3692e4f35e/scale";
		ResponseEntity<String> response = sendRequest(inputStream("/Request.json"), uri, HttpMethod.POST);
		
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
		RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
		assertTrue(realResponse.getServiceException().getText().contains("Mapping of request to JSON object failed"));
	}
	@Test
	public void scaleE2EServiceInstance() throws JsonParseException, JsonMappingException, IOException{
		RequestError expectedResponse = new RequestError();
		ServiceException exception = new ServiceException();
		exception.setMessageId("SVC1000");
		exception.setText("No communication to catalog DB null");
		expectedResponse.setServiceException(exception);
		
		String uri = e2eServInstancesUri + "v5/9b9f02c0-298b-458a-bc9c-be3692e4f35e/scale";
		ResponseEntity<String> response = sendRequest(inputStream("/ScaleRequest.json"), uri, HttpMethod.POST);
		
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatusCode().value());
		RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
		assertThat(realResponse, sameBeanAs(expectedResponse));
	}
	@Test
	public void compareModelWithTargetVersionBadRequest() throws JsonParseException, JsonMappingException, IOException{
		String uri = e2eServInstancesUri + "v5/9b9f02c0-298b-458a-bc9c-be3692e4f35e/modeldifferences";
		ResponseEntity<String> response = sendRequest(inputStream("/Request.json"), uri, HttpMethod.POST);
		
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
		RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
		assertTrue(realResponse.getServiceException().getText().contains("Mapping of request to JSON object failed"));
	}
	@Test
	public void compareModelWithTargetVersion() throws JsonParseException, JsonMappingException, IOException{
		stubFor(post(urlPathEqualTo("/mso/async/services/CompareModelofE2EServiceInstance"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBodyFile("Camunda/SuccessfulResponse.json").withStatus(org.apache.http.HttpStatus.SC_ACCEPTED)));
		
		String expectedResponse = "success";
		String uri = e2eServInstancesUri + "v5/9b9f02c0-298b-458a-bc9c-be3692e4f35e/modeldifferences";
		ResponseEntity<String> response = sendRequest(inputStream("/CompareModelRequest.json"), uri, HttpMethod.POST);
		
		assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
		String actualResponse = response.getBody();
		assertEquals(expectedResponse, actualResponse);
	}
	@Test
	public void compareModelWithTargetVersionEmptyResponse() throws JsonParseException, JsonMappingException, IOException{
		stubFor(post(urlPathEqualTo("/mso/async/services/CompareModelofE2EServiceInstance"))
				.willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));
		
		RequestError expectedResponse = new RequestError();
		ServiceException exception = new ServiceException();
		exception.setMessageId("SVC1000");
		exception.setText("Failed calling bpmn localhost:" + env.getProperty("wiremock.server.port") + " failed to respond");
		expectedResponse.setServiceException(exception);
		
		String uri = e2eServInstancesUri + "v5/9b9f02c0-298b-458a-bc9c-be3692e4f35e/modeldifferences";
		ResponseEntity<String> response = sendRequest(inputStream("/CompareModelRequest.json"), uri, HttpMethod.POST);
		
		assertEquals(Response.Status.BAD_GATEWAY.getStatusCode(), response.getStatusCode().value());
		RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
		assertThat(realResponse, sameBeanAs(expectedResponse));
	}
	@Test
	public void compareModelWithTargetVersionBadBpelResponse() throws JsonParseException, JsonMappingException, IOException{
		stubFor(post(urlPathEqualTo("/mso/async/services/CompareModelofE2EServiceInstance"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBodyFile("Camunda/TestResponse.json").withStatus(org.apache.http.HttpStatus.SC_BAD_GATEWAY)));
		
		String uri = e2eServInstancesUri + "v5/9b9f02c0-298b-458a-bc9c-be3692e4f35e/modeldifferences";
		ResponseEntity<String> response = sendRequest(inputStream("/CompareModelRequest.json"), uri, HttpMethod.POST);
		
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
		RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
		assertTrue(realResponse.getServiceException().getText().contains("Request Failed due to BPEL error with HTTP Status"));
	}
	@Test
	public void compareModelWithTargetVersionNoBPELResponse() throws JsonParseException, JsonMappingException, IOException{
		stubFor(post(urlPathEqualTo("/mso/async/services/CompareModelofE2EServiceInstance"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBody("{}").withStatus(org.apache.http.HttpStatus.SC_BAD_GATEWAY)));

		String uri = e2eServInstancesUri + "v5/9b9f02c0-298b-458a-bc9c-be3692e4f35e/modeldifferences";
		ResponseEntity<String> response = sendRequest(inputStream("/CompareModelRequest.json"), uri, HttpMethod.POST);
		
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
		RequestError realResponse = mapper.readValue(response.getBody(), RequestError.class);
		assertTrue(realResponse.getServiceException().getText().contains("Request Failed due to BPEL error with HTTP Status"));
	}
}
