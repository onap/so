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
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

/**
 * Please describe the StubResponseNetwork.java class
 *
 */
public class StubResponseNetworkAdapter {

	private static final String EOL = "\n";

	public static void setupAllMocks() {

	}


	public static void MockNetworkAdapter() {
		stubFor(post(urlEqualTo("/networks/NetworkAdapter"))
			.willReturn(aResponse()
			.withStatus(200)));
	}

	public static void MockNetworkAdapter(String response) {
		stubFor(post(urlEqualTo("/networks/NetworkAdapter"))
			.willReturn(aResponse()
			.withStatus(200)
			.withHeader("Content-Type", "text/xml")
			.withBodyFile(response)));
	}

	public static void MockNetworkAdapter_500() {
		stubFor(post(urlEqualTo("/networks/NetworkAdapter"))
			.willReturn(aResponse()
			.withStatus(500)));
	}

	public static void MockNetworkAdapterPost(String responseFile, String requestContaining) {
		stubFor(post(urlEqualTo("/networks/NetworkAdapter"))
			.withRequestBody(containing(requestContaining))				
			.willReturn(aResponse()
			.withStatus(200)
			.withHeader("Content-Type", "text/xml")
			.withBodyFile(responseFile)));
	}	
	
	public static void MockNetworkAdapter(String networkId, int statusCode, String responseFile) {
		stubFor(delete(urlEqualTo("/networks/NetworkAdapter/" + networkId))
				  .willReturn(aResponse()
				  .withStatus(statusCode)
				  .withHeader("Content-Type", "application/xml")
				  .withBodyFile(responseFile)));
	}
	
	public static void MockNetworkAdapterContainingRequest(String requestContaining, int statusCode, String responseFile) {
		stubFor(post(urlEqualTo("/networks/NetworkAdapter"))
				  .withRequestBody(containing(requestContaining))
				  .willReturn(aResponse()
				  .withStatus(statusCode)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile(responseFile)));
	}
	
	public static void MockPutNetworkAdapter(String networkId, String requestContaining, int statusCode, String responseFile) {
		stubFor(put(urlEqualTo("/networks/NetworkAdapter/" + networkId))
				  .withRequestBody(containing(requestContaining))
				  .willReturn(aResponse()
				  .withStatus(statusCode)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile(responseFile)));
	}
	
	public static void MockNetworkAdapterRestRollbackDelete(String responseFile, String networkId) {
		stubFor(delete(urlEqualTo("/networks/NetworkAdapter/"+networkId+"/rollback"))
			.willReturn(aResponse()
			.withStatus(200)
			.withHeader("Content-Type", "text/xml")
			.withBodyFile(responseFile)));
	}	

	public static void MockNetworkAdapterRestPut(String responseFile, String networkId) {
		stubFor(put(urlEqualTo("/networks/NetworkAdapter/"+networkId))
			.willReturn(aResponse()
			.withStatus(200)
			.withHeader("Content-Type", "text/xml")
			.withBodyFile(responseFile)));
	}		
	
}
