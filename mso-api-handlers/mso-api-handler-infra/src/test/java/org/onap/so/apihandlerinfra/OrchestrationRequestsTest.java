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

package org.onap.so.apihandlerinfra;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.data.repository.InfraActiveRequestsRepository;
import org.onap.so.exceptions.ValidationException;
import org.onap.so.serviceinstancebeans.GetOrchestrationListResponse;
import org.onap.so.serviceinstancebeans.GetOrchestrationResponse;
import org.onap.so.serviceinstancebeans.Request;
import org.onap.so.serviceinstancebeans.RequestError;
import org.onap.so.serviceinstancebeans.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


@Transactional
public class OrchestrationRequestsTest extends BaseTest {

	@Autowired
	private InfraActiveRequestsRepository iar;
	
	private static final String CHECK_HTML = "<!DOCTYPE html><html><head><meta charset=\"ISO-8859-1\"><title></title></head><body></body></html>";
	public static final Response RESPONSE = Response.status(HttpStatus.SC_OK).entity(CHECK_HTML).build();
	private static final GetOrchestrationListResponse ORCHESTRATION_LIST = generateOrchestrationList();
	private static final String INVALID_REQUEST_ID = "invalid-request-id";

	private static GetOrchestrationListResponse generateOrchestrationList() {
		GetOrchestrationListResponse list = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			list = mapper.readValue(new File("src/test/resources/OrchestrationRequest/OrchestrationList.json"),
					GetOrchestrationListResponse.class);
		} catch (JsonParseException jpe) {
			jpe.printStackTrace();
		} catch (JsonMappingException jme) {
			jme.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return list;
	}
	
	@Test
	public void testGetOrchestrationRequest() {
				
		// TEST VALID REQUEST
		GetOrchestrationResponse testResponse = new GetOrchestrationResponse();
		
		Request request = ORCHESTRATION_LIST.getRequestList().get(1).getRequest();
		testResponse.setRequest(request);
		String testRequestId = request.getRequestId();

		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		HttpEntity<Request> entity = new HttpEntity<Request>(null, headers);
		
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(createURLWithPort("/onap/so/infra/orchestrationRequests/v7/" + testRequestId));

		ResponseEntity<GetOrchestrationResponse> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET,
				entity, GetOrchestrationResponse.class);

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
		assertThat(response.getBody(),
		sameBeanAs(testResponse).ignoring("request.startTime").ignoring("request.requestStatus.finishTime"));
        assertEquals("application/json", response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
        assertEquals("0", response.getHeaders().get("X-MinorVersion").get(0));
        assertEquals("0", response.getHeaders().get("X-PatchVersion").get(0));
        assertEquals("7.0.0", response.getHeaders().get("X-LatestVersion").get(0));
        assertEquals("00032ab7-na18-42e5-965d-8ea592502018", response.getHeaders().get("X-TransactionID").get(0));
	}

	@Test
	public void testGetOrchestrationRequestRequestDetails() {
		//Test request with modelInfo request body
		GetOrchestrationResponse testResponse = new GetOrchestrationResponse();
		
		Request request = ORCHESTRATION_LIST.getRequestList().get(0).getRequest();
		testResponse.setRequest(request);
		String testRequestId = request.getRequestId();

		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		HttpEntity<Request> entity = new HttpEntity<Request>(null, headers);
		
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(createURLWithPort("/onap/so/infra/orchestrationRequests/v7/" + testRequestId));

		ResponseEntity<GetOrchestrationResponse> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET,
				entity, GetOrchestrationResponse.class);

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
		assertThat(response.getBody(),
		sameBeanAs(testResponse).ignoring("request.startTime").ignoring("request.requestStatus.finishTime"));
        assertEquals("application/json", response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
        assertEquals("0", response.getHeaders().get("X-MinorVersion").get(0));
        assertEquals("0", response.getHeaders().get("X-PatchVersion").get(0));
        assertEquals("7.0.0", response.getHeaders().get("X-LatestVersion").get(0));
        assertEquals("00032ab7-3fb3-42e5-965d-8ea592502017", response.getHeaders().get("X-TransactionID").get(0));
	}

	@Test
    public void testGetOrchestrationRequestNoRequestID() {

        HttpEntity<Request> entity = new HttpEntity<Request>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(createURLWithPort("/onap/so/infra/orchestrationRequests/v6/"));

        ResponseEntity<GetOrchestrationListResponse> response = restTemplate.exchange(builder.toUriString(),
                HttpMethod.GET, entity, GetOrchestrationListResponse.class);        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
    }

	@Test
    public void testGetOrchestrationRequestFilter() {
		List<String> values = new ArrayList<>();
		values.add("EQUALS");
		values.add("vfModule");
		
		Map<String, List<String>> orchestrationMap = new HashMap<>();
		orchestrationMap.put("modelType", values);
		
		List<InfraActiveRequests> requests = iar.getOrchestrationFiltersFromInfraActive(orchestrationMap);
        HttpEntity<Request> entity = new HttpEntity<Request>(null, headers);
        headers.set("Accept", MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(createURLWithPort("/onap/so/infra/orchestrationRequests/v6?filter=modelType:EQUALS:vfModule"));

        ResponseEntity<GetOrchestrationListResponse> response = restTemplate.exchange(builder.toUriString(),
                HttpMethod.GET, entity, GetOrchestrationListResponse.class);        
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        assertEquals(requests.size(), response.getBody().getRequestList().size());
    }
	
	@Test
	public void testUnlockOrchestrationRequest()
			throws JsonParseException, JsonMappingException, IOException, ValidationException {

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
		String requestJSON = new String(Files.readAllBytes(Paths.get("src/test/resources/OrchestrationRequest/Request.json")));

		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(requestJSON, headers);

		UriComponentsBuilder builder;
		ResponseEntity<String> response;
		RequestError expectedRequestError;
		RequestError actualRequestError;
		ServiceException se;

		// Test invalid JSON		
		expectedRequestError = new RequestError();
		se = new ServiceException();
		se.setMessageId(ErrorNumbers.SVC_DETAILED_SERVICE_ERROR);
		se.setText("Orchestration RequestId 0017f68c-eb2d-45bb-b7c7-ec31b37dc349 has a status of UNLOCKED and can not be unlocked");
		expectedRequestError.setServiceException(se);

		builder = UriComponentsBuilder.fromHttpUrl(
				createURLWithPort("/onap/so/infra/orchestrationRequests/v6/0017f68c-eb2d-45bb-b7c7-ec31b37dc349/unlock"));

		response = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, entity, String.class);
		actualRequestError = mapper.readValue(response.getBody(), RequestError.class);

		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
		assertThat(actualRequestError, sameBeanAs(expectedRequestError));
	}
	
	@Test
	public void testUnlockOrchestrationRequest_invalid_Json()
			throws JsonParseException, JsonMappingException, IOException, ValidationException {
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
		String requestJSON = new String(Files.readAllBytes(Paths.get("src/test/resources/OrchestrationRequest/Request.json")));

		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(requestJSON, headers);

		UriComponentsBuilder builder;
		ResponseEntity<String> response;
		RequestError expectedRequestError;
		RequestError actualRequestError;
		ServiceException se;
		
		// Test invalid requestId
		expectedRequestError = new RequestError();
		se = new ServiceException();
		se.setMessageId(ErrorNumbers.SVC_DETAILED_SERVICE_ERROR);
		se.setText("Null response from RequestDB when searching by RequestId");
		expectedRequestError.setServiceException(se);

		builder = UriComponentsBuilder.fromHttpUrl(
				createURLWithPort("/onap/so/infra/orchestrationRequests/v6/" + INVALID_REQUEST_ID + "/unlock"));

		response = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, entity, String.class);
		actualRequestError = mapper.readValue(response.getBody(), RequestError.class);

		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatusCode().value());
		assertThat(actualRequestError, sameBeanAs(expectedRequestError));
	}

	@Test
	public void testUnlockOrchestrationRequest_Valid_Status()
			throws JsonParseException, JsonMappingException, IOException, ValidationException {
		
		ObjectMapper mapper = new ObjectMapper();
		String requestJSON = new String(Files.readAllBytes(Paths.get("src/test/resources/OrchestrationRequest/Request.json")));

		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(requestJSON, headers);

		UriComponentsBuilder builder;
		ResponseEntity<String> response;
		Request request;

		// Test valid status
		request = ORCHESTRATION_LIST.getRequestList().get(1).getRequest();
		builder = UriComponentsBuilder.fromHttpUrl(
				createURLWithPort("/onap/so/infra/orchestrationRequests/v7/" + "5ffbabd6-b793-4377-a1ab-082670fbc7ac" + "/unlock"));

		response = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, entity, String.class);

		String status = iar.findOneByRequestId("5ffbabd6-b793-4377-a1ab-082670fbc7ac").getRequestStatus();
	
		assertEquals("UNLOCKED", status);
		assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatusCode().value());
		assertEquals(response.getBody(), null);
	}
	
	@Test
	public void testUnlockOrchestrationRequest_invalid_Status()
			throws JsonParseException, JsonMappingException, IOException, ValidationException {
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
		String requestJSON = new String(Files.readAllBytes(Paths.get("src/test/resources/OrchestrationRequest/Request.json")));

		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(requestJSON, headers);

		UriComponentsBuilder builder;
		ResponseEntity<String> response;
		Request request;
		RequestError expectedRequestError;
		RequestError actualRequestError;
		ServiceException se;
		// Update UNLOCKED Request
		request = ORCHESTRATION_LIST.getRequestList().get(1).getRequest();
		request.getRequestStatus().setRequestState(Status.UNLOCKED.toString());
		request.getRequestStatus().setStatusMessage(null);
		request.getRequestStatus().setPercentProgress(null);
		request.setRequestDetails(null);
		request.setRequestScope(null);
		request.setRequestType(null);

		// Test invalid status
		request = ORCHESTRATION_LIST.getRequestList().get(0).getRequest();
		expectedRequestError = new RequestError();
		se = new ServiceException();
		se.setMessageId(ErrorNumbers.SVC_DETAILED_SERVICE_ERROR);
		se.setText("Orchestration RequestId " + request.getRequestId() + " has a status of "
				+ request.getRequestStatus().getRequestState() + " and can not be unlocked");
		expectedRequestError.setServiceException(se);

		builder = UriComponentsBuilder.fromHttpUrl(
				createURLWithPort("/onap/so/infra/orchestrationRequests/v6/" + request.getRequestId() + "/unlock"));

		response = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, entity, String.class);
		actualRequestError = mapper.readValue(response.getBody(), RequestError.class);

		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode().value());
		assertThat(actualRequestError, sameBeanAs(expectedRequestError));
	}
	
	@Test
	public void testGetOrchestrationRequestRequestDetailsWhiteSpace() {
		InfraActiveRequests requests = new InfraActiveRequests();
		requests.setAction("create");
		requests.setRequestBody("  ");
		requests.setRequestId("requestId");
		requests.setRequestScope("service");
		requests.setRequestType("createInstance");
		iar.save(requests);

		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		HttpEntity<Request> entity = new HttpEntity<Request>(null, headers);
		
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(createURLWithPort("/onap/so/infra/orchestrationRequests/v7/requestId"));

		ResponseEntity<GetOrchestrationResponse> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET,
				entity, GetOrchestrationResponse.class);

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        assertEquals("application/json", response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
        assertEquals("0", response.getHeaders().get("X-MinorVersion").get(0));
        assertEquals("0", response.getHeaders().get("X-PatchVersion").get(0));
        assertEquals("7.0.0", response.getHeaders().get("X-LatestVersion").get(0));
        assertEquals("requestId", response.getHeaders().get("X-TransactionID").get(0));
	}
	
	@Test
	public void testGetOrchestrationRequestRequestDetailsAlaCarte() throws IOException {
		InfraActiveRequests requests = new InfraActiveRequests();
		
		String requestJSON = new String(Files.readAllBytes(Paths.get("src/test/resources/OrchestrationRequest/AlaCarteRequest.json")));
		
		requests.setAction("create");
		requests.setRequestBody(requestJSON);
		requests.setRequestId("requestId");
		requests.setRequestScope("service");
		requests.setRequestType("createInstance");
		iar.save(requests);

		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type", MediaType.APPLICATION_JSON);
		HttpEntity<Request> entity = new HttpEntity<Request>(null, headers);
		
		UriComponentsBuilder builder = UriComponentsBuilder
				.fromHttpUrl(createURLWithPort("/onap/so/infra/orchestrationRequests/v7/requestId"));

		ResponseEntity<GetOrchestrationResponse> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET,
				entity, GetOrchestrationResponse.class);

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
        assertEquals("application/json", response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
        assertEquals("0", response.getHeaders().get("X-MinorVersion").get(0));
        assertEquals("0", response.getHeaders().get("X-PatchVersion").get(0));
        assertEquals("7.0.0", response.getHeaders().get("X-LatestVersion").get(0));
        assertEquals("requestId", response.getHeaders().get("X-TransactionID").get(0));
	}
}