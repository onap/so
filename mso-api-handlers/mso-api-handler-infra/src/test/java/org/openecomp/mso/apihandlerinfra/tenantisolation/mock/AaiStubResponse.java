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

package org.openecomp.mso.apihandlerinfra.tenantisolation.mock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import javax.ws.rs.core.MediaType;

public class AaiStubResponse {
	
	public static String DEFAULT_ERROR_RESPONSE = "{ \"requestError\":{ \"serviceException\" : {\"messageId\": \"500\",\"text\": \"Test error message!\"}}}";
	
	public static void setupAllMocks() {}
	
	public static void MockGetRequest(String link, int returnCode, String response) {
		stubFor(get(urlPathEqualTo(link))
			.willReturn(aResponse()
				.withHeader("Content-Type", MediaType.APPLICATION_JSON)
				.withHeader("Accept", MediaType.APPLICATION_JSON)
				.withStatus(returnCode)
				.withBody(response)));
	}
	
	public static void MockPutRequest(String link, int returnCode, String response) {
		stubFor(put(urlPathEqualTo(link))
			.willReturn(aResponse()
				.withStatus(returnCode)
				.withHeader("Content-Type", MediaType.APPLICATION_JSON)
				.withBody(response)));
	}
	
	public static void MockPostRequest(String link, int returnCode) {
		stubFor(post(urlPathEqualTo(link))
			.willReturn(aResponse()
				.withHeader("Content-Type", MediaType.APPLICATION_JSON)
				.withHeader("X-HTTP-Method-Override", "PATCH")
				.withStatus(returnCode)));
	}
}
