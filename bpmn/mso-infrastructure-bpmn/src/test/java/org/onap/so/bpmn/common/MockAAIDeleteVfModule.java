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
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

import com.github.tomakehurst.wiremock.WireMockServer;

public class MockAAIDeleteVfModule {
	
	public MockAAIDeleteVfModule(WireMockServer wireMockServer)
	{
		wireMockServer.stubFor(delete(urlMatching(
				"/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c721/vf-modules/vf-module/973ed047-d251-4fb9-bf1a-65b8949e0a73/[?]resource-version=0000073"))
						.willReturn(aResponse().withStatus(200)));
		wireMockServer.stubFor(delete(urlMatching(
				"/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c720/vf-modules/vf-module/973ed047-d251-4fb9-bf1a-65b8949e0a75/[?]resource-version=0000075"))
						.willReturn(aResponse().withStatus(200)));
		wireMockServer.stubFor(delete(urlMatching(
				"/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c718/vf-modules/vf-module/973ed047-d251-4fb9-bf1a-65b8949e0a78/[?]resource-version=0000078"))
						.willReturn(aResponse().withStatus(200)));
		wireMockServer.stubFor(delete(urlMatching(
				"/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c719/vf-modules/vf-module/973ed047-d251-4fb9-bf1a-65b8949e0a77/[?]resource-version=0000077"))
						.willReturn(aResponse().withStatus(500).withHeader("Content-Type", "text/xml")
								.withBodyFile("aaiFault.xml")));
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/network-policies/network-policy\\?network-policy-fqdn=.*"))
				.willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
						.withBodyFile("VfModularity/QueryNetworkPolicy_AAIResponse_Success.xml")));

		wireMockServer.stubFor(delete(urlMatching("/aai/v[0-9]+/network/network-policies/network-policy/.*"))
				.willReturn(aResponse().withStatus(200)));
	}
}
