/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.valet;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.onap.so.bpmn.mock.StubOpenStack.mockValetConfirmPutRequest_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockValetCreatePostResponse_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockValetCreatePutResponse_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockValetDeleteDeleteResponse_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockValetRollbackPutRequest_200;

import java.io.File;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.adapters.valet.beans.ValetConfirmResponse;
import org.onap.so.adapters.valet.beans.ValetCreateResponse;
import org.onap.so.adapters.valet.beans.ValetDeleteResponse;
import org.onap.so.adapters.valet.beans.ValetRollbackResponse;
import org.onap.so.adapters.valet.beans.ValetUpdateResponse;
import org.onap.so.adapters.vnf.BaseRestTestUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ValetClientTest extends BaseRestTestUtils {
	@Autowired
	protected ValetClient client;
	
	private ObjectMapper mapper = new ObjectMapper();

	@Before
	public void init() {
		client.baseUrl = "http://localhost:" + wireMockPort;
	}
	
	@Test
	public void testCallValetCreateRequest() throws Exception {	
		ValetCreateResponse vcr = mapper.readValue(new File("src/test/resources/__files/ValetCreateRequest.json"), ValetCreateResponse.class);
		GenericValetResponse<ValetCreateResponse> expected = new GenericValetResponse<ValetCreateResponse>(HttpStatus.SC_OK, ValetClient.NO_STATUS_RETURNED, vcr);
		
		mockValetCreatePostResponse_200("requestId", mapper.writeValueAsString(vcr));
		
		GenericValetResponse<ValetCreateResponse> actual = client.callValetCreateRequest("requestId", "regionId", "ownerId", "tenantId", "serviceInstanceId", "vnfId", "vnfName", "vfModuleId", "vfModuleName", "keystoneUrl", null);

		assertThat(actual, sameBeanAs(expected));
	}
	
	@Test
	public void testCallValetUpdateRequest() throws Exception {	
		ValetUpdateResponse vur = mapper.readValue(new File("src/test/resources/__files/ValetCreateRequest.json"), ValetUpdateResponse.class);
		GenericValetResponse<ValetUpdateResponse> expected = new GenericValetResponse<ValetUpdateResponse>(HttpStatus.SC_OK, ValetClient.NO_STATUS_RETURNED, vur);
		
		mockValetCreatePutResponse_200("requestId", mapper.writeValueAsString(vur));
		
		GenericValetResponse<ValetUpdateResponse> actual = client.callValetUpdateRequest("requestId", "regionId", "ownerId", "tenantId", "serviceInstanceId", "vnfId", "vnfName", "vfModuleId", "vfModuleName", "keystoneUrl", null);

		assertThat(actual, sameBeanAs(expected));
	}
	
	@Test
	public void testCallValetDeleteRequest() throws Exception {
		ValetDeleteResponse vdr = mapper.readValue(new File("src/test/resources/__files/ValetDeleteRequest.json"), ValetDeleteResponse.class);
		GenericValetResponse<ValetDeleteResponse> expected = new GenericValetResponse<ValetDeleteResponse>(HttpStatus.SC_OK, ValetClient.NO_STATUS_RETURNED, vdr);
		
		mockValetDeleteDeleteResponse_200("requestId", mapper.writeValueAsString(vdr));
		
		GenericValetResponse<ValetDeleteResponse> actual = client.callValetDeleteRequest("requestId", "regionId", "ownerId", "tenantId", "vfModuleId", "vfModuleName");

		assertThat(actual, sameBeanAs(expected));
	}
	
	@Test
	public void testCallValetConfirmRequest() throws Exception {		
		ValetConfirmResponse vcr = new ValetConfirmResponse();
		GenericValetResponse<ValetConfirmResponse> expected = new GenericValetResponse<ValetConfirmResponse>(HttpStatus.SC_OK, ValetClient.NO_STATUS_RETURNED, vcr);
		
		mockValetConfirmPutRequest_200("requestId", "{}");
		
		GenericValetResponse<ValetConfirmResponse> actual = client.callValetConfirmRequest("requestId", "stackId");

		assertThat(actual, sameBeanAs(expected));
	}
	
	@Test
	public void testCallValetRollbackRequest() throws Exception {		
		ValetRollbackResponse vrr = new ValetRollbackResponse();	
		GenericValetResponse<ValetRollbackResponse> expected = new GenericValetResponse<ValetRollbackResponse>(HttpStatus.SC_OK, ValetClient.NO_STATUS_RETURNED, vrr);
		
		mockValetRollbackPutRequest_200("requestId", "{}");
		
		GenericValetResponse<ValetRollbackResponse> actual = client.callValetRollbackRequest("requestId", "stackId", true, "error");

		assertThat(actual, sameBeanAs(expected));
	}
}
