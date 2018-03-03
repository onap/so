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

/**
 * Please describe the StubResponseSDNC.java class
 */
public class StubResponseSDNCAdapter {

	public static void setupAllMocks() {

	}

	public static void mockSDNCAdapter_500() {
		stubFor(post(urlEqualTo("/SDNCAdapter"))
				.willReturn(aResponse()
						.withStatus(500)));
	}		
	
	public static void mockSDNCAdapter_500(String requestContaining) {
		stubFor(post(urlEqualTo("/SDNCAdapter"))
		  .withRequestBody(containing(requestContaining))
		  .willReturn(aResponse()
		  .withStatus(500)));
	}		
	
	public static void mockSDNCAdapter(int statusCode) {
		stubFor(post(urlEqualTo("/SDNCAdapter"))
				.willReturn(aResponse()
						.withStatus(statusCode)));
	}
	
	public static void mockSDNCAdapter(String endpoint, int statusCode, String responseFile) {
		stubFor(post(urlEqualTo(endpoint))	
				  .willReturn(aResponse()
				  .withStatus(statusCode)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile(responseFile)));
	}

	public static void mockSDNCAdapter(String responseFile) {
		stubFor(post(urlEqualTo("/SDNCAdapter"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile(responseFile)));
	}
	
	public static void mockSDNCAdapter(String endpoint, String requestContaining, int statusCode, String responseFile) {
		stubFor(post(urlEqualTo(endpoint))
				.withRequestBody(containing(requestContaining))
				.willReturn(aResponse()
					.withStatus(statusCode)
					.withHeader("Content-Type", "text/xml")
					.withBodyFile(responseFile)));
	}

	public static void mockSDNCAdapterSimulator(String responseFile) {
		MockResource mockResource = new MockResource();
		mockResource.updateProperties("sdnc_delay", "300");
		stubFor(post(urlEqualTo("/SDNCAdapter"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/soap+xml")
						.withTransformers("sdnc-adapter-transformer")
						.withBodyFile(responseFile)));
	}

	public static void mockSDNCAdapterSimulator(String responseFile, String requestContaining) {
		MockResource mockResource = new MockResource();
		mockResource.updateProperties("sdnc_delay", "300");
		stubFor(post(urlEqualTo("/SDNCAdapter"))
				.withRequestBody(containing(requestContaining))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "application/soap+xml")
						.withTransformers("sdnc-adapter-transformer")
						.withBodyFile(responseFile)));
	}

	public static void mockSDNCAdapterRest() {
		stubFor(post(urlEqualTo("/SDNCAdapter/v1/sdnc/services"))
				.willReturn(aResponse()
						.withStatus(202)
						.withHeader("Content-Type", "application/json")));
	}

	public static void mockSDNCAdapterRest_500() {
		stubFor(post(urlEqualTo("/SDNCAdapter/v1/sdnc/services"))
				.willReturn(aResponse()
						.withStatus(500)
						.withHeader("Content-Type", "application/json")));
	}

	public static void mockSDNCAdapterRest(String requestContaining) {
		stubFor(post(urlEqualTo("/SDNCAdapter/v1/sdnc/services"))
				.withRequestBody(containing(requestContaining))
				.willReturn(aResponse()
						.withStatus(202)
						.withHeader("Content-Type", "application/json")));
	}

	public static void mockSDNCAdapterRest_500(String requestContaining) {
		stubFor(post(urlEqualTo("/SDNCAdapter/v1/sdnc/services"))
				.withRequestBody(containing(requestContaining))
				.willReturn(aResponse()
						.withStatus(500)
						.withHeader("Content-Type", "application/json")));
	}

	public static void mockSDNCAdapterTopology(String responseFile, String requestContaining) {
		MockResource mockResource = new MockResource();
		mockResource.updateProperties("sdnc_delay", "300");		
		stubFor(post(urlEqualTo("/SDNCAdapter"))
				.withRequestBody(containing(requestContaining))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withTransformers("network-topology-operation-transformer")
						.withBodyFile(responseFile)));
	}

	
}
