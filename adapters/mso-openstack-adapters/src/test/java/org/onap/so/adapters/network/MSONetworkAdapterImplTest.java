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

package org.onap.so.adapters.network;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.onap.so.adapters.vnf.BaseRestTestUtils;
import org.onap.so.cloud.CloudConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackDeleteNeutronNetwork;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackDeleteStack_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackDeleteStack_500;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetAllNeutronNetworks_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetAllNeutronNetworks_404;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetNeutronNetwork;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetNeutronNetwork_404;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStackCreated_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStackDeleteOrUpdateComplete_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStack_404;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStack_500;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackPostNeutronNetwork_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackPostStack_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackPutNeutronNetwork;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackPutNeutronNetwork_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackPutStack;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackResponseAccess;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenstackGet;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenstackPost;

public class MSONetworkAdapterImplTest extends BaseRestTestUtils {

	public static final String NETWORK_ID = "43173f6a-d699-414b-888f-ab243dda6dfe";
	public static final String NETWORK_NAME = "vUSP-23804-T-01-dpa2b_EVUSP-CORE-VIF-TSIG0_net_0";

	@Autowired
	private CloudConfig cloudConfig;

	@Test
	public void createNetworkByModelNameNeutronModeGetNetworkException() throws IOException{

		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");

		mockOpenStackResponseAccess(wireMockPort);

		mockOpenstackGet("/mockPublicUrl/v2.0/networks",HttpStatus.SC_INTERNAL_SERVER_ERROR);

		String uri =  "/services/NetworkAdapter";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/CreateNetwork_NEUTRON_Mode.xml"), uri, HttpMethod.POST);
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
	}

	@Test
	public void createNetworkByModelNameNeutronModeCreateNetworkException() throws IOException{

		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");

		mockOpenStackResponseAccess(wireMockPort);

		mockOpenStackGetNeutronNetwork_404("dvspg-VCE_VPE-mtjnj40avbc");

		mockOpenStackGetAllNeutronNetworks_404();

		mockOpenstackPost("/mockPublicUrl/v2.0/networks", HttpStatus.SC_INTERNAL_SERVER_ERROR);

		String uri =  "/services/NetworkAdapter";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/CreateNetwork_NEUTRON_Mode.xml"), uri, HttpMethod.POST);
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
	}

	@Test
	public void createNetworkByModelNameNeutronMode() throws IOException{

		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");

		mockOpenStackResponseAccess(wireMockPort);

		mockOpenStackGetNeutronNetwork_404("dvspg-VCE_VPE-mtjnj40avbc");

		mockOpenStackGetAllNeutronNetworks_404();

		mockOpenStackPostNeutronNetwork_200("OpenstackCreateNeutronNetworkResponse.json");

		String uri =  "/services/NetworkAdapter";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/CreateNetwork_NEUTRON_Mode.xml"), uri, HttpMethod.POST);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
	}

	@Test
	public void createNetworkByModelNameAlreadyExistNeutronMode() throws IOException{

		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");

		mockOpenStackResponseAccess(wireMockPort);

		mockOpenStackGetAllNeutronNetworks_200("OpenstackGetNeutronNetworks.json");

		String uri =  "/services/NetworkAdapter";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/CreateNetwork_NEUTRON_Mode.xml"), uri, HttpMethod.POST);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
	}

	@Test
	public void createNetworkByModelNameAlreadyExistNeutronModeFailIfExistTrue() throws IOException{

		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");

		mockOpenStackResponseAccess(wireMockPort);

		mockOpenStackGetAllNeutronNetworks_200("OpenstackGetNeutronNetworks.json");

		String uri =  "/services/NetworkAdapter";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/CreateNetwork_NEUTRON_Mode_Fail_If_Exist_True.xml"), uri, HttpMethod.POST);
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
	}

	@Test
	public void createNetworkByModelNameHeatMode() throws IOException{

		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");

		mockOpenStackResponseAccess(wireMockPort);

		mockOpenStackGetStack_404("dvspg-VCE_VPE-mtjnj40avbc");

		mockOpenStackPostStack_200("OpenstackResponse_Stack.json");

		mockOpenStackGetStackCreated_200("OpenstackResponse_Stack_Created.json", "dvspg-VCE_VPE-mtjnj40avbc/stackId");

		String uri =  "/services/NetworkAdapter";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/CreateNetwork.xml"), uri, HttpMethod.POST);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
	}

	@Test
	public void createNetworkByModelNameAlreadyExistHeatMode() throws IOException{

		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");

		mockOpenStackResponseAccess(wireMockPort);

		mockOpenStackPostStack_200("OpenstackResponse_Stack.json");

		mockOpenStackGetStackCreated_200("OpenstackResponse_Stack_Created.json", "dvspg-VCE_VPE-mtjnj40avbc");

		String uri =  "/services/NetworkAdapter";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/CreateNetwork.xml"), uri, HttpMethod.POST);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
	}

	@Test
	public void createNetworkByModelNameAlreadyExistHeatModeFailIfExistTrue() throws IOException{

		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");

		mockOpenStackResponseAccess(wireMockPort);

		mockOpenStackGetStackCreated_200("OpenstackResponse_Stack_Created.json", "dvspg-VCE_VPE-mtjnj40avbc");

		String uri =  "/services/NetworkAdapter";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/CreateNetwork_Fail_If_Exist_True.xml"), uri, HttpMethod.POST);
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
	}


	@Test
	public void createNetworkByModelNameHeatModeQueryNetworkException() throws IOException{

		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");

		mockOpenStackResponseAccess(wireMockPort);

		mockOpenstackGet("/mockPublicUrl/stacks/dvspg-VCE_VPE-mtjnj40avbc",HttpStatus.SC_INTERNAL_SERVER_ERROR);

		String uri =  "/services/NetworkAdapter";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/CreateNetwork.xml"), uri, HttpMethod.POST);
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
	}

	@Test
	public void createNetworkByModelNameHeatModeCreateNetworkException() throws IOException{

		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");

		mockOpenStackResponseAccess(wireMockPort);

		mockOpenStackGetStack_404("dvspg-VCE_VPE-mtjnj40avbc");

		mockOpenstackPost("/mockPublicUrl/stacks",HttpStatus.SC_INTERNAL_SERVER_ERROR);

		String uri =  "/services/NetworkAdapter";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/CreateNetwork.xml"), uri, HttpMethod.POST);
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
	}

	@Test
	public void createNetworkByModelNameCloudSiteNotPresentError() throws IOException{

		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");

		mockOpenStackResponseAccess(wireMockPort);

		mockOpenStackPostStack_200("OpenstackResponse_Stack.json");

		mockOpenStackGetStackCreated_200("OpenstackResponse_Stack_Created.json", "dvspg-VCE_VPE-mtjnj40avbc");

		String uri =  "/services/NetworkAdapter";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/CreateNetwork_InvalidCloudSiteId.xml"), uri, HttpMethod.POST);
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
	}

	@Test
	public void deleteNetworkHeatModeSuccess() throws IOException{

		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");

		mockOpenStackResponseAccess(wireMockPort);

		mockOpenStackGetStackDeleteOrUpdateComplete_200("OpenstackResponse_Stack_DeleteComplete.json");

		mockOpenStackDeleteStack_200();

		mockOpenStackGetStackCreated_200("OpenstackResponse_Stack_Created.json", "43173f6a-d699-414b-888f-ab243dda6dfe");

		String uri =  "/services/NetworkAdapter";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/DeleteNetwork.xml"), uri, HttpMethod.POST);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
	}

	@Test
	public void deleteNetworkDeleteStackException() throws IOException{

		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");

		mockOpenStackResponseAccess(wireMockPort);

		mockOpenStackGetStackDeleteOrUpdateComplete_200("OpenstackResponse_Stack_DeleteComplete.json");

		mockOpenStackDeleteStack_500();

		mockOpenStackGetStackCreated_200("OpenstackResponse_Stack_Created.json", "43173f6a-d699-414b-888f-ab243dda6dfe");

		String uri =  "/services/NetworkAdapter";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/DeleteNetwork.xml"), uri, HttpMethod.POST);
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
	}

	@Test
	public void deleteNetworkError() throws IOException{

		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");

		mockOpenStackResponseAccess(wireMockPort);

		mockOpenStackGetStackDeleteOrUpdateComplete_200("OpenstackResponse_Stack_DeleteComplete.json");

		mockOpenStackDeleteStack_200();

		mockOpenStackGetStackCreated_200("OpenstackResponse_Stack_Created.json", "43173f6a-d699-414b-888f-ab243dda6dfe");

		String uri =  "/services/NetworkAdapter";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/DeleteNetwork.xml").replace("mtn13",""), uri, HttpMethod.POST);
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
	}


	@Test
	public void deleteNetworkNeureonMode() throws IOException{

		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");

		mockOpenStackResponseAccess(wireMockPort);

		mockOpenStackGetNeutronNetwork("GetNeutronNetwork.json",NETWORK_ID,HttpStatus.SC_OK);

		mockOpenStackDeleteNeutronNetwork(NETWORK_ID,HttpStatus.SC_OK);
		
		String uri =  "/services/NetworkAdapter";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/DeleteNetwork.xml").replace("CONTRAIL30_BASIC","CONTRAIL31_GNDIRECT"), uri, HttpMethod.POST);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
	}

	@Test
	public void deleteNetworkNeutronModeDeleteStackException() throws IOException{

		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");

		mockOpenStackResponseAccess(wireMockPort);

		mockOpenStackGetNeutronNetwork("GetNeutronNetwork.json",NETWORK_ID,HttpStatus.SC_OK);

		mockOpenStackDeleteNeutronNetwork(NETWORK_ID,HttpStatus.SC_INTERNAL_SERVER_ERROR);

		//mockOpenStackGetStackCreated_200("OpenstackResponse_Stack_Created.json", NETWORK_ID);

		String uri =  "/services/NetworkAdapter";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/DeleteNetwork.xml").replace("CONTRAIL30_BASIC","CONTRAIL31_GNDIRECT"), uri, HttpMethod.POST);
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
	}

	@Test
	public void updateNetworkNeutronModeSuccess() throws IOException{

		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");

		mockOpenStackResponseAccess(wireMockPort);

		mockOpenStackGetNeutronNetwork("GetNeutronNetwork.json",NETWORK_ID,HttpStatus.SC_OK);
		mockOpenStackPutNeutronNetwork_200("OpenstackCreateNeutronNetworkResponse.json",NETWORK_ID);

		String uri =  "/services/NetworkAdapter";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/UpdateNetwork.xml").replace("CONTRAIL30_BASIC","CONTRAIL31_GNDIRECT"), uri, HttpMethod.POST);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
	}

	@Test
	public void updateNetworkNeutronUpdateException() throws IOException{

		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");

		mockOpenStackResponseAccess(wireMockPort);

		mockOpenStackGetNeutronNetwork("GetNeutronNetwork.json",NETWORK_ID,HttpStatus.SC_OK);
		mockOpenStackPutNeutronNetwork(NETWORK_ID,HttpStatus.SC_INTERNAL_SERVER_ERROR);

		String uri =  "/services/NetworkAdapter";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/UpdateNetwork.xml").replace("CONTRAIL30_BASIC","CONTRAIL31_GNDIRECT"), uri, HttpMethod.POST);
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
	}

	@Test
	public void updateNetworkHeatUpdateException() throws IOException{

		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");

		mockOpenStackResponseAccess(wireMockPort);

		mockOpenStackGetStackCreated_200("OpenstackResponse_Stack_Created.json", NETWORK_NAME);

		mockOpenStackPutStack(NETWORK_ID,HttpStatus.SC_INTERNAL_SERVER_ERROR);

		String uri =  "/services/NetworkAdapter";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/UpdateNetwork.xml"), uri, HttpMethod.POST);
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
	}

	@Test
	public void updateNetworkHeatQueryException() throws IOException{

		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");

		mockOpenStackResponseAccess(wireMockPort);

		mockOpenStackGetStack_500(NETWORK_NAME);

		String uri =  "/services/NetworkAdapter";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/UpdateNetwork.xml"), uri, HttpMethod.POST);
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
	}

	@Test
	public void updateNetworkHeatStackNotFound() throws IOException{

		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");

		mockOpenStackResponseAccess(wireMockPort);

		mockOpenStackGetStack_404(NETWORK_NAME);

		String uri =  "/services/NetworkAdapter";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/UpdateNetwork.xml"), uri, HttpMethod.POST);
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
	}

	@Test
	public void updateNetworkNeutronQueryException() throws IOException{

		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");

		mockOpenStackResponseAccess(wireMockPort);

		mockOpenStackGetNeutronNetwork(NETWORK_ID,HttpStatus.SC_INTERNAL_SERVER_ERROR);

		String uri =  "/services/NetworkAdapter";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/UpdateNetwork.xml").replace("CONTRAIL30_BASIC","CONTRAIL31_GNDIRECT"), uri, HttpMethod.POST);
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
	}

	@Test
	public void updateNetworkNeutronStackNotFound() throws IOException{

		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");

		mockOpenStackResponseAccess(wireMockPort);

		mockOpenStackGetNeutronNetwork(NETWORK_ID,HttpStatus.SC_NOT_FOUND);

		String uri =  "/services/NetworkAdapter";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/UpdateNetwork.xml").replace("CONTRAIL30_BASIC","CONTRAIL31_GNDIRECT"), uri, HttpMethod.POST);
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
	}

	@Test
	public void queryNetworkHeatModesuccess() throws IOException{

		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");

		mockOpenStackResponseAccess(wireMockPort);

		mockOpenStackGetStackCreated_200("OpenstackResponse_Stack_Created.json", NETWORK_ID);

		String uri =  "/services/NetworkAdapter";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/QueryNetwork.xml").replace("CONTRAIL30_BASIC","CONTRAIL31_GNDIRECT"), uri, HttpMethod.POST);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
	}

	@Test
	public void queryNetworkHeatModeQueryException() throws IOException{

		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");

		mockOpenStackResponseAccess(wireMockPort);

		mockOpenStackGetStack_500(NETWORK_ID);

		String uri =  "/services/NetworkAdapter";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/QueryNetwork.xml").replace("CONTRAIL30_BASIC","CONTRAIL31_GNDIRECT"), uri, HttpMethod.POST);
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
	}

	@Test
	public void queryNetworkNeutronModeSuccess() throws IOException{

		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");

		mockOpenStackResponseAccess(wireMockPort);

		mockOpenStackGetNeutronNetwork("GetNeutronNetwork.json",NETWORK_ID,HttpStatus.SC_OK);

		String uri =  "/services/NetworkAdapter";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/QueryNetwork.xml").replace("CONTRAIL30_BASIC","CONTRAIL31_GNDIRECT"), uri, HttpMethod.POST);
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
	}

	@Test
	public void queryNetworkNeutronModeException() throws IOException{

		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wireMockPort + "/v2.0");

		mockOpenStackResponseAccess(wireMockPort);

		mockOpenStackGetNeutronNetwork(NETWORK_ID,HttpStatus.SC_INTERNAL_SERVER_ERROR);

		String uri =  "/services/NetworkAdapter";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/QueryNetwork.xml").replace("CONTRAIL30_BASIC","CONTRAIL31_GNDIRECT"), uri, HttpMethod.POST);
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode().value());
	}

	public ResponseEntity<String> sendXMLRequest(String requestJson, String uriPath, HttpMethod reqMethod){
		headers.set("Accept", MediaType.APPLICATION_XML);
		headers.set("Content-Type", MediaType.APPLICATION_XML);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(uriPath));

		HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);
		ResponseEntity<String> response = restTemplate.exchange(builder.toUriString(),
				reqMethod, request, String.class);

		return response;
	}

	public String inputStream(String JsonInput)throws IOException{
		JsonInput = "src/test/resources/" + JsonInput;
		String input = new String(Files.readAllBytes(Paths.get(JsonInput)));
		return input;
	}
}
