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

package org.onap.so.bpmn.mock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.http.HttpStatus;

public class StubOpenStack {
	private static final String NETWORK_NAME = "vUSP-23804-T-01-dpa2b_EVUSP-CORE-VIF-TSIG0_net_0";
	private static final String NETWORK_ID = "da886914-efb2-4917-b335-c8381528d90b";
	private static final String NETWORK_NAME_2 = "stackname";
	private static final String NETWORK_ID_2 = "stackId";
	
	public static void mockOpenStackResponseAccess(int port) throws IOException {
		stubFor(post(urlPathEqualTo("/v2.0/tokens")).willReturn(aResponse().withHeader("Content-Type", "application/json")
			.withBody(getBodyFromFile("OpenstackResponse_Access.json", port, "/mockPublicUrl"))
				.withStatus(HttpStatus.SC_OK)));
	}

	public static void mockOpenStackResponseAccessMulticloud(int port) throws IOException {
		stubFor(post(urlPathEqualTo("/v2.0/tokens")).willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBody(getBodyFromFile("OpenstackResponse_AccessMulticloud.json", port, "/mockPublicUrl"))
				.withStatus(HttpStatus.SC_OK)));
	}

	public static void mockOpenStackResponseAccessQueryNetwork(int port) throws IOException {
		stubFor(post(urlPathEqualTo("/v2.0/tokens"))
				.withRequestBody(containing("tenantId"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBody(getBodyFromFile("OpenstackResponse_Access_queryNetwork.json", port, "/mockPublicUrl"))
				.withStatus(HttpStatus.SC_OK)));
	}
	
	public static void mockOpenStackResponseAccessAdmin(int port) throws IOException {
		stubFor(post(urlPathEqualTo("/v2.0/tokens")).willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBody(getBodyFromFile("OpenstackResponse_Access_Admin.json", port, "/mockPublicUrl"))
					.withStatus(HttpStatus.SC_OK)));
	}
	
	public static void mockOpenStackPublicUrlStackByName_200(int port) throws IOException {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/"+NETWORK_NAME)).willReturn(aResponse()
			.withHeader("X-Openstack-Request-Id", "openstackRquest")
				.withHeader("location", "http://localhost:" + port + "/mockPublicUrl/stacks/"+NETWORK_NAME)
					.withBody(getBodyFromFile("OpenstackResponse_StackId.json", port, "/mockPublicUrl/stacks/" + NETWORK_NAME))
						.withStatus(HttpStatus.SC_OK)));
	}
	
	public static void mockOpenStackPublicUrlStackByID_200(int port) throws IOException {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/"+NETWORK_ID)).willReturn(aResponse()
			.withHeader("X-Openstack-Request-Id", "openstackRquest")
				.withHeader("location", "http://localhost:" + port + "/mockPublicUrl/stacks/"+NETWORK_NAME)
					.withBody(getBodyFromFile("OpenstackResponse_StackId.json", port, "/mockPublicUrl/stacks/" + NETWORK_NAME))
						.withStatus(HttpStatus.SC_OK)));
	}
	
	public static void mockOpenStackGetPublicUrlStackByNameAndID_200(int port) throws IOException {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/"+NETWORK_NAME+"/"+NETWORK_ID)).willReturn(aResponse()
			.withHeader("X-Openstack-Request-Id", "openstackRquest")
				.withHeader("location", "http://localhost:" + port + "/mockPublicUrl/stacks/"+NETWORK_NAME)
					.withBody(getBodyFromFile("OpenstackResponse_StackId.json", port, "/mockPublicUrl/stacks/" + NETWORK_NAME))
						.withStatus(HttpStatus.SC_OK)));
	}
	
	public static void mockOpenStackGetPublicUrlStackByNameAndID_204(int port) throws IOException {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/"+NETWORK_NAME+"/"+NETWORK_ID)).willReturn(aResponse()
			.withHeader("X-Openstack-Request-Id", "openstackRquest")
				.withHeader("location", "http://localhost:" + port + "/mockPublicUrl/stacks/"+NETWORK_NAME+"/"+NETWORK_ID)
					.withBody(getBodyFromFile("OpenstackResponse_StackId.json", port, "/mockPublicUrl/stacks/" + NETWORK_NAME))
						.withStatus(HttpStatus.SC_NOT_FOUND)));
	}
	
	public static void mockOpenStackPutPublicUrlStackByNameAndID_200() {
		stubFor(put(urlPathEqualTo("/mockPublicUrl/stacks/"+NETWORK_NAME+"/"+NETWORK_ID)).willReturn(aResponse()
			.withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_OK)));
	}

	public static void mockOpenStackPutPublicUrlStackByNameAndID_NETWORK2_200() {
		stubFor(put(urlPathEqualTo("/mockPublicUrl/stacks/"+NETWORK_NAME_2+"/"+NETWORK_ID_2)).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_OK)));
	}
	
	public static void mockOpenStackDeletePublicUrlStackByNameAndID_204() {
		stubFor(delete(urlPathEqualTo("/mockPublicUrl/stacks/"+NETWORK_NAME+"/"+NETWORK_ID)).willReturn(aResponse()
			.withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_NO_CONTENT)));
	}
	
	public static void mockOpenStackPostPublicUrlWithBodyFile_200() {
		stubFor(post(urlPathEqualTo("/mockPublicUrl/stacks"))
			.willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBodyFile("OpenstackResponse_Stack.json").withStatus(HttpStatus.SC_OK)));
	}
	
	public static void mockOpenStackGetStackCreatedAppC_200() {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/APP-C-24595-T-IST-04AShared_untrusted_vDBE_net_3/stackId"))
			.willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBodyFile("OpenstackResponse_Stack_Created.json").withStatus(HttpStatus.SC_OK)));
	}
	
	public static void mockOpenStackGetStackAppC_404() {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/APP-C-24595-T-IST-04AShared_untrusted_vDBE_net_3"))
			.willReturn(aResponse().withStatus(HttpStatus.SC_NOT_FOUND)));
	}
	
	public static void mockOpenStackGetStackCreatedVUSP_200() {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/vUSP-23804-T-01-dpa2b_EVUSP-CORE-VIF-TSIG0_net_0/stackId"))
			.willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBodyFile("OpenstackResponse_Stack_Created.json").withStatus(HttpStatus.SC_OK)));
	}
	
	public static void mockOpenStackGetStackVUSP_404() {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/vUSP-23804-T-01-dpa2b_EVUSP-CORE-VIF-TSIG0_net_0"))
			.willReturn(aResponse().withStatus(HttpStatus.SC_NOT_FOUND)));
	}
	
	public static void mockOpenStackPostStack_200(String filename) {
		stubFor(post(urlPathEqualTo("/mockPublicUrl/stacks")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
				.withBodyFile(filename).withStatus(HttpStatus.SC_OK)));
	}

	public static void mockOpenStackPostNeutronNetwork_200(String filename) {
		stubFor(post(urlPathEqualTo("/mockPublicUrl/v2.0/networks")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
				.withBodyFile(filename).withStatus(HttpStatus.SC_OK)));
	}

	public static void mockOpenStackPutNeutronNetwork_200(String filename,String networkId) {
		stubFor(put(urlPathEqualTo("/mockPublicUrl/v2.0/networks/"+networkId)).willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
				.withBodyFile(filename).withStatus(HttpStatus.SC_OK)));
	}

	public static void mockOpenStackPutNeutronNetwork(String networkId, int responseCode) {
		stubFor(put(urlPathEqualTo("/mockPublicUrl/v2.0/networks/"+networkId)).willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
				.withStatus(responseCode)));
	}

	public static void mockOpenStackGetAllNeutronNetworks_200(String filename){
		stubFor(get(urlPathEqualTo("/mockPublicUrl/v2.0/networks")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
				.withBodyFile(filename).withStatus(HttpStatus.SC_OK)));
	}
	
	public static void mockOpenStackGetNeutronNetwork_404(String networkName) {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/v2.0/networks/"+networkName)).willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
				.withStatus(HttpStatus.SC_NOT_FOUND)));
	}

	public static void mockOpenStackGetAllNeutronNetworks_404() {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/v2.0/networks")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
				.withStatus(HttpStatus.SC_NOT_FOUND)));
	}

	public static void mockOpenstackGetWithResponse(String url,int responseCode, String responseFile) {
		stubFor(get(urlPathEqualTo(url)).willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
				.withBodyFile(responseFile)
				.withStatus(responseCode)));
	}
	
	public static void mockOpenstackPostWithResponse(String url,int responseCode, String responseFile) {
		stubFor(post(urlPathEqualTo(url)).willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
				.withBodyFile(responseFile)
				.withStatus(responseCode)));
	}

	public static void mockOpenstackGet(String url,int responseCode) {
		stubFor(get(urlPathEqualTo(url)).willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
				.withStatus(responseCode)));
	}

	public static void mockOpenstackPost(String url,int responseCode) {
		stubFor(post(urlPathEqualTo(url)).willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
				.withStatus(responseCode)));
	}

	public static void mockOpenStackGetStackVfModule_200() {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001/stackId"))
			.willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBodyFile("OpenstackResponse_Stack_Created_VfModule.json")
					.withStatus(HttpStatus.SC_OK)));
	}
	
	public static void mockOpenStackGetStackVfModule_404() {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001"))
			.willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withStatus(HttpStatus.SC_NOT_FOUND)));
	}
	
	public static void mockOpenStackPostStacks_200() {
		stubFor(post(urlPathEqualTo("/mockPublicUrl/stacks"))
			.willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBodyFile("OpenstackResponse_Stack.json").withStatus(HttpStatus.SC_OK)));
	}
	
	public static void mockOpenStackGetStacks_404() {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/3d7ff7b4-720b-4604-be0a-1974fc58ed96"))
			.willReturn(aResponse().withStatus(HttpStatus.SC_NOT_FOUND)));
	}
	
	public static void mockOpenStackGetStacksWithBody_200(String replaceWith) throws IOException {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001"))
			.willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBody(getBodyFromFileVnfAdapter(replaceWith))
					.withStatus(HttpStatus.SC_OK)));
	}

	public static void mockOpenStackGetStackWithBody_200(String replaceWith) throws IOException {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001/stackId"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBody(getBodyFromFileVnfAdapter(replaceWith))
						.withStatus(HttpStatus.SC_OK)));
	}
	
	public static void mockOpenStackGetStacksWithBody_404() throws IOException {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001"))
			.willReturn(aResponse().withHeader("Content-Type", "application/json")
				.withBody(getBodyFromFileVnfAdapter(null))
					.withStatus(HttpStatus.SC_NOT_FOUND)));
	}
	
	public static void mockOpenStackGetStacksStackId_404() {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/stackId"))
			.willReturn(aResponse().withStatus(HttpStatus.SC_NOT_FOUND)));
	}
	
	public static void mockOpenStackGetStacksVfModule_200(int port) throws IOException {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001")).willReturn(aResponse()
			.withHeader("X-Openstack-Request-Id", "openstackRquest")
				.withBody(getBodyFromFile("OpenstackResponse_VnfStackId.json", port, "/mockPublicUrl/stacks/stackId")).withStatus(HttpStatus.SC_OK)));
	}
	
	public static void mockOpenStackGetStacksVfModuleWithLocationHeader_200(int port) throws IOException {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001")).willReturn(aResponse()
			.withHeader("X-Openstack-Request-Id", "openstackRquest")
				.withHeader("location", "http://localhost:" + port + "/mockPublicUrl/stacks/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001")
					.withBody(getBodyFromFile("OpenstackResponse_VnfStackId.json", port, "/mockPublicUrl/stacks/stackId")).withStatus(HttpStatus.SC_OK)));
	}
	
	public static void mockOpenStackGetStacksBaseStack_200(int port) throws IOException {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/baseVfModuleStackId")).willReturn(aResponse()
			.withHeader("X-Openstack-Request-Id", "openstackRquest")
				.withHeader("location", "http://localhost:" + port + "/mockPublicUrl/stacks/baseVfModuleStackId")
					.withBody(getBodyFromFile("OpenstackResponse_VnfBaseStackId.json", port, "/mockPublicUrl/stacks/stackId")).withStatus(HttpStatus.SC_OK)));
	}
	
	public static void mockOpenStackPutStacks_200() {
		stubFor(put(urlPathEqualTo("/mockPublicUrl/stacks/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001")).willReturn(aResponse()
			.withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_OK)));
	}

	public static void mockOpenStackPutStack(String networkId,int responseCode) {
		stubFor(put(urlPathEqualTo("/mockPublicUrl/stacks/"+networkId))
				.willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
				.withStatus(responseCode)));
	}
	
	public static void mockOpenStackGetStacksStackId_200(int port) throws IOException {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/StackId")).willReturn(aResponse()
			.withHeader("X-Openstack-Request-Id", "openstackRquest")
				.withHeader("location", "http://localhost:" + port + "/mockPublicUrl/stacks/stackId")
					.withBody(getBodyFromFile("OpenstackResponse_StackId.json", port, "/mockPublicUrl/stacks/stackId")).withStatus(HttpStatus.SC_OK)));
	}
	
	public static void mockOpenStackDeleteStacks() {
		stubFor(delete(urlPathEqualTo("/mockPublicUrl/stacks/vUSP-23804-T-01-dpa2b_EVUSP-CORE-VIF-TSIG0_net_0/da886914-efb2-4917-b335-c8381528d90b"))
			.willReturn(aResponse().withHeader("X-Openstack-Request-Id", "openstackRquest")));
	}
	
	public static void mockOpenStackGetStacksVUSP_404() {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/vUSP-23804-T-01-dpa2b_EVUSP-CORE-VIF-TSIG0_net_0/da886914-efb2-4917-b335-c8381528d90b"))
			.willReturn(aResponse()
				.withHeader("X-Openstack-Request-Id", "openstackRquest")
					.withStatus(HttpStatus.SC_NOT_FOUND)));
	}
	
	public static void mockOpenStackGetStackCreated_200(String filename, String networkName) {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/" + networkName))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBodyFile(filename).withStatus(HttpStatus.SC_OK)));
	}

	public static void mockOpenStackGetStack_404(String networkName) {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/" + networkName))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withStatus(HttpStatus.SC_NOT_FOUND)));
	}

	public static void mockOpenStackGetStack_500(String networkName) {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/" + networkName))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
	}

	public static void mockOpenStackGetStackDeleteOrUpdateComplete_200(String filename) {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/" + NETWORK_NAME_2 + "/" + NETWORK_ID_2))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBodyFile(filename).withStatus(HttpStatus.SC_OK)));
	}

	public static void mockOpenStackGetNeutronNetwork(String filename,String networkId,int status) {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/v2.0/networks/"+ networkId))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBodyFile(filename).withStatus(status)));
	}

	public static void mockOpenStackGetNeutronNetwork(String networkId,int status) {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/v2.0/networks/"+ networkId))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withStatus(status)));
	}
	
	public static void mockOpenStackDeleteStack_200() {
		stubFor(delete(urlPathEqualTo("/mockPublicUrl/stacks/" + NETWORK_NAME_2 + "/" + NETWORK_ID_2))
				.willReturn(aResponse().withStatus(HttpStatus.SC_OK)));
	}

	public static void mockOpenStackDeleteStack_500() {
		stubFor(delete(urlPathEqualTo("/mockPublicUrl/stacks/" + NETWORK_NAME_2 + "/" + NETWORK_ID_2))
				.willReturn(aResponse().withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
	}

	public static void mockOpenStackDeleteNeutronNetwork(String networkId,int responseCode) {
		stubFor(delete(urlPathEqualTo("/mockPublicUrl/v2.0/networks/" + networkId))
				.willReturn(aResponse().withStatus(responseCode)));
	}
	
	public static void mockOpenStackPostMetadata_200() {
		stubFor(post(urlPathEqualTo("/mockPublicUrl/tenants/tenantId/metadata")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
						.withBodyFile("OpenstackResponse_Metadata.json").withStatus(HttpStatus.SC_OK)));
	}
	
	public static void mockOpenStackGetMetadata_200() {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/tenants/tenantId/metadata")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
						.withBodyFile("OpenstackResponse_Metadata.json").withStatus(HttpStatus.SC_OK)));
	}
	
	public static void mockOpenStackPostTenantWithBodyFile_200() throws IOException {
		stubFor(post(urlPathEqualTo("/mockPublicUrl/tenants"))
				.withRequestBody(equalToJson(readFile("src/test/resources/__files/OpenstackRequest_Tenant.json"))).willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
						.withBodyFile("OpenstackResponse_Tenant.json").withStatus(HttpStatus.SC_OK)));
	}
	
	public static void mockOpenStackPostTenant_200() throws IOException {
		stubFor(post(urlPathEqualTo("/mockPublicUrl/tenants")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_OK)));
	}
	
	public static void mockOpenStackGetTenantByName_200(String tenantName) {
		stubFor(get(urlEqualTo("/mockPublicUrl/tenants/?name=" + tenantName)).willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
						.withBodyFile("OpenstackResponse_Tenant.json").withStatus(HttpStatus.SC_OK)));
	}
	
	public static void mockOpenStackGetTenantByName_404(String tenantName) {
		stubFor(get(urlEqualTo("/mockPublicUrl/tenants/?name=" + tenantName)).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_NOT_FOUND)));
	}
	
	public static void mockOpenStackGetTenantById_200(String tenantId) {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/tenants/" + tenantId)).willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
						.withBodyFile("OpenstackResponse_Tenant.json").withStatus(HttpStatus.SC_OK)));
	}
	
	public static void mockOpenStackGetTenantById_404(String tenantId) {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/tenants/" + tenantId)).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_NOT_FOUND)));
	}
	
	public static void mockOpenStackDeleteTenantById_200(String tenantId) {
		stubFor(delete(urlPathEqualTo("/mockPublicUrl/tenants/" + tenantId)).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_OK)));
	}
	
	public static void mockOpenStackGetUser_200(String user) {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/users/" + user)).willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
						.withBodyFile("OpenstackResponse_User.json").withStatus(HttpStatus.SC_OK)));
	}
	
	public static void mockOpenStackGetRoles_200(String roleFor) {
		stubFor(get(urlPathEqualTo("/mockPublicUrl/" + roleFor + "/roles")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
						.withBodyFile("OpenstackResponse_Roles.json").withStatus(HttpStatus.SC_OK)));
	}
	
	public static void mockOpenStackPutRolesAdmin_200(String roleFor) {
		stubFor(put(urlPathEqualTo("/mockPublicUrl/tenants/tenantId/users/msoId/roles/" + roleFor + "/admin")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
						.withBody("").withStatus(HttpStatus.SC_OK)));
	}
	
	public static void mockValetCreatePostResponse_200(String requestId, String body) {
		stubFor(post(urlEqualTo("/api/valet/placement/v1/?requestId=" + requestId))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                		.withBody(body).withStatus(HttpStatus.SC_OK)));
	}
	
	public static void mockValetCreatePutResponse_200(String requestId, String body) {
		stubFor(put(urlEqualTo("/api/valet/placement/v1/?requestId=" + requestId))
              .willReturn(aResponse().withHeader("Content-Type", "application/json")
            		  .withBody(body).withStatus(HttpStatus.SC_OK)));
	}
	
	public static void mockValetDeleteDeleteResponse_200(String requestId, String body) {
		stubFor(delete(urlEqualTo("/api/valet/placement/v1/?requestId=" + requestId))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                		.withBody(body).withStatus(HttpStatus.SC_OK)));
	}
	
	public static void mockValetConfirmPutRequest_200(String requestId, String body) {
		stubFor(put(urlPathEqualTo("/api/valet/placement/v1/" + requestId + "/confirm/"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                		.withBody(body).withStatus(HttpStatus.SC_OK)));
	}
	
	public static void mockValetRollbackPutRequest_200(String requestId, String body) {
		stubFor(put(urlPathEqualTo("/api/valet/placement/v1/" + requestId + "/rollback/"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                		.withBody(body).withStatus(HttpStatus.SC_OK)));
	}
	
	private static String getBodyFromFileVnfAdapter(String replaceWith) throws IOException {
		String temp = readFile("src/test/resources/__files/OpenstackResponse_Stack_Created_VfModule.json");
		if (replaceWith == null) {
			return temp;
		}
		return temp.replaceAll("CREATE_COMPLETE", replaceWith);
	}
	
	private static String readFile(String fileName) throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			return sb.toString();
		}
	}
	
	public static String getBodyFromFile(String fileName, int port, String urlPath) throws IOException {
		return readFile("src/test/resources/__files/" + fileName).replaceAll("port", "http://localhost:" + port + urlPath);
	}
}
