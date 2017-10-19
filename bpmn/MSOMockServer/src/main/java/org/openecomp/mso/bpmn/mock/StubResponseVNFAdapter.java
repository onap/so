/*
 * ============LICENSE_START======================================================= 
 * ONAP - SO 
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

package org.openecomp.mso.bpmn.mock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

/**
 * Please describe the StubResponseVNF.java class
 */
public class StubResponseVNFAdapter {

	public static void mockVNFAdapter() {
		stubFor(post(urlEqualTo("/vnfs/VnfAdapterAsync"))
				.willReturn(aResponse()
						.withStatus(200)));
	}

	public static void mockVNFAdapter(String responseFile) {
		stubFor(post(urlEqualTo("/vnfs/VnfAdapterAsync"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile(responseFile)));
	}

	public static void mockVNFAdapter_500() {
		stubFor(post(urlEqualTo("/vnfs/VnfAdapterAsync"))
				.willReturn(aResponse()
						.withStatus(500)));
	}

	public static void mockVNFAdapterTransformer(String transformer, String responseFile) {
		MockResource mockResource = new MockResource();
		mockResource.updateProperties("vnf_delay", "300");
		stubFor(post(urlEqualTo("/vnfs/VnfAdapterAsync"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/soap+xml")
						.withTransformers(transformer)
						.withBodyFile(responseFile)));
	}

	public static void mockVNFAdapterTransformer(String transformer, String responseFile, String requestContaining) {
		MockResource mockResource = new MockResource();
		mockResource.updateProperties("vnf_delay", "300");
		stubFor(post(urlEqualTo("/vnfs/VnfAdapterAsync"))
				.withRequestBody(containing(requestContaining))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/soap+xml")
						.withTransformers(transformer)
						.withBodyFile(responseFile)));
	}
	
	public static void mockVNFPost(String vfModuleId, int statusCode, String vnfId) {
		stubFor(post(urlEqualTo("/vnfs/v1/vnfs/" + vnfId + "/vf-modules" + vfModuleId))
				.willReturn(aResponse()
				.withStatus(statusCode)
				.withHeader("Content-Type", "application/xml")));
	}
	
	public static void mockVNFPut(String vfModuleId, int statusCode) {
		stubFor(put(urlEqualTo("/vnfs/v1/vnfs/vnfId/vf-modules" + vfModuleId))
				.willReturn(aResponse()
				.withStatus(statusCode)
				.withHeader("Content-Type", "application/xml")));
	}
	
	public static void mockVNFPut(String vnfId, String vfModuleId, int statusCode) {
		stubFor(put(urlEqualTo("/vnfs/v1/vnfs/" + vnfId + "/vf-modules" + vfModuleId))
				.willReturn(aResponse()
				.withStatus(statusCode)
				.withHeader("Content-Type", "application/xml")));
	}
	
	public static void mockVNFDelete(String vnfId, String vfModuleId, int statusCode) {
		stubFor(delete(urlEqualTo("/vnfs/v1/vnfs/" + vnfId + "/vf-modules" + vfModuleId))
				.willReturn(aResponse()
				.withStatus(statusCode)
				.withHeader("Content-Type", "application/xml")));
	}
	
	public static void mockVNFRollbackDelete(String vfModuleId, int statusCode) {
		stubFor(delete(urlEqualTo("/vnfs/v1/vnfs/vnfId/vf-modules" + vfModuleId + "/rollback"))
				.willReturn(aResponse()
				.withStatus(statusCode)
				.withHeader("Content-Type", "application/xml")));
	}
	
	public static void mockPutVNFVolumeGroup(String volumeGroupId, int statusCode) {
		stubFor(put(urlEqualTo("/vnfs/v1/volume-groups/" + volumeGroupId))
				.willReturn(aResponse()
					.withStatus(statusCode)
					.withHeader("Content-Type", "application/xml")));
	}
	
	public static void mockPutVNFVolumeGroupRollback(String volumeGroupId, int statusCode) {
		stubFor(delete(urlMatching("/vnfs/v1/volume-groups/" + volumeGroupId + "/rollback"))
				.willReturn(aResponse()
				.withStatus(statusCode)
				.withHeader("Content-Type", "application/xml")));
	}
	public static void mockPostVNFVolumeGroup(int statusCode) {
		stubFor(post(urlEqualTo("/vnfs/v1/volume-groups"))
				.willReturn(aResponse()
					.withStatus(statusCode)
					.withHeader("Content-Type", "application/xml")));
	}
	
	public static void mockVNFAdapterRest(String vnfId) {
		stubFor(post(urlEqualTo("/vnfs/v1/vnfs/" + vnfId + "/vf-modules"))
				.willReturn(aResponse()
						.withStatus(200)));
	}

	public static void mockVNFAdapterRest_500(String vnfId) {
		stubFor(post(urlEqualTo("/vnfs/v1/vnfs/" + vnfId + "/vf-modules"))
				.willReturn(aResponse()
						.withStatus(500)));
	}
	
	public static void mockVfModuleDelete(String volumeGroupId) {
		stubFor(delete(urlMatching("/vnfs/v1/volume-groups/"+ volumeGroupId))
				.willReturn(aResponse()
				.withStatus(202)
				.withHeader("Content-Type", "application/xml")));
	}
	
	public static void mockVfModuleDelete(String volumeGroupId, int statusCode) {
		stubFor(delete(urlMatching("/vnfs/v1/volume-groups/78987"))
				.willReturn(aResponse()
				.withStatus(statusCode)
				.withHeader("Content-Type", "application/xml")));
	}
}
