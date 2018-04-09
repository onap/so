/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.cloudify.base.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class CloudifyClientTokenProviderTest {
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void createTokenProvider() {
		wireMockRule.stubFor(get(urlPathEqualTo("/testUrl")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody("{\"role\": \"user\", \"value\": \"tokenVal\"}").withStatus(HttpStatus.SC_OK)));
		int port = wireMockRule.port();

		CloudifyClientTokenProvider cctp = new CloudifyClientTokenProvider("http://localhost:"+port+"/testUrl", "user", "pswd");
		String token = cctp.getToken();
		assertEquals("tokenVal", token);
		token = cctp.getToken();
		assertEquals("tokenVal", token);
		cctp.expireToken();
	}
}
