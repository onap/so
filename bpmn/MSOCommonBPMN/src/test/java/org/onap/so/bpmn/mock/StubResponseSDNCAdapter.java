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

package org.onap.so.bpmn.mock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

import com.github.tomakehurst.wiremock.WireMockServer;

/**
 * Please describe the StubResponseSDNC.java class
 */
public class StubResponseSDNCAdapter {

	public static void setupAllMocks() {

	}

	public static void mockSDNCAdapter_500(WireMockServer wireMockServer) {
		wireMockServer.stubFor(post(urlEqualTo("/SDNCAdapter"))
				.willReturn(aResponse()
						.withStatus(500)));
	}		
	
	public static void mockSDNCAdapter_500(WireMockServer wireMockServer, String requestContaining) {
		wireMockServer.stubFor(post(urlEqualTo("/SDNCAdapter"))
		  .withRequestBody(containing(requestContaining))
		  .willReturn(aResponse()
		  .withStatus(500)));
	}		
	
	public static void mockSDNCAdapter(WireMockServer wireMockServer, int statusCode) {
		wireMockServer.stubFor(post(urlMatching(".*/SDNCAdapter"))
				.willReturn(aResponse()
						.withStatus(statusCode)));
	}
	
	public static void mockSDNCAdapter(WireMockServer wireMockServer, String endpoint, int statusCode, String responseFile) {
		wireMockServer.stubFor(post(urlEqualTo(endpoint))	
				  .willReturn(aResponse()
				  .withStatus(statusCode)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile(responseFile)));
	}

	public static void mockSDNCAdapter(WireMockServer wireMockServer, String responseFile) {
		wireMockServer.stubFor(post(urlEqualTo("/SDNCAdapter"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile(responseFile)));
	}
	
	public static void mockSDNCAdapter(WireMockServer wireMockServer, String endpoint, String requestContaining, int statusCode, String responseFile) {
		wireMockServer.stubFor(post(urlEqualTo(endpoint))
				.withRequestBody(containing(requestContaining))
				.willReturn(aResponse()
					.withStatus(statusCode)
					.withHeader("Content-Type", "text/xml")
					.withBodyFile(responseFile)));
	}

	public static void mockSDNCAdapterRest(WireMockServer wireMockServer) {
		wireMockServer.stubFor(post(urlEqualTo("/SDNCAdapter/v1/sdnc/services"))
				.willReturn(aResponse()
						.withStatus(202)
						.withHeader("Content-Type", "application/json")));
	}

	public static void mockSDNCAdapterRest_500(WireMockServer wireMockServer) {
		wireMockServer.stubFor(post(urlEqualTo("/SDNCAdapter/v1/sdnc/services"))
				.willReturn(aResponse()
						.withStatus(500)
						.withHeader("Content-Type", "application/json")));
	}

	public static void mockSDNCAdapterRest(WireMockServer wireMockServer, String requestContaining) {
		wireMockServer.stubFor(post(urlEqualTo("/SDNCAdapter/v1/sdnc/services"))
				.withRequestBody(containing(requestContaining))
				.willReturn(aResponse()
						.withStatus(202)
						.withHeader("Content-Type", "application/json")));
	}

	public static void mockSDNCAdapterRest_500(WireMockServer wireMockServer, String requestContaining) {
		wireMockServer.stubFor(post(urlEqualTo("/SDNCAdapter/v1/sdnc/services"))
				.withRequestBody(containing(requestContaining))
				.willReturn(aResponse()
						.withStatus(500)
						.withHeader("Content-Type", "application/json")));
	}

}
