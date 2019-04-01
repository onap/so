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

package org.onap.so.bpmn.common;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

import com.github.tomakehurst.wiremock.WireMockServer;

public class MockAAIDeleteGenericVnf {
	public MockAAIDeleteGenericVnf(WireMockServer wireMockServer){
		wireMockServer.stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c721/[?]resource-version=0000021"))
				.willReturn(aResponse()
						.withStatus(200)));
		wireMockServer.stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c718/[?]resource-version=0000018"))
				.willReturn(aResponse()
						.withStatus(500)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("aaiFault.xml")));
	}
}
