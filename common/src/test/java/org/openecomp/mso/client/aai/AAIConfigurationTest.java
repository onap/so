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

package org.openecomp.mso.client.aai;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.openecomp.mso.client.aai.entities.uri.AAIUri;
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory;
import org.openecomp.mso.serviceinstancebeans.ModelInfo;
import org.openecomp.mso.serviceinstancebeans.RequestDetails;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class AAIConfigurationTest {

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(8443));
	
	@Ignore
	@Test
	public void verifyCreate() {
		AAIConfigurationClient aaiConfiguration = new AAIConfigurationClient();
		ModelInfo modelInfo = new ModelInfo();
		modelInfo.setModelInvariantId("testInvariantID");
		modelInfo.setModelVersionId("testVersionID");
		modelInfo.setModelCustomizationId("testCustomizationID");
		RequestDetails requestDetails = new RequestDetails();
		requestDetails.setModelInfo(modelInfo);
		String configurationType = "test";
		String configurationSubType = "test";
		aaiConfiguration.createConfiguration(requestDetails, UUID.randomUUID().toString(), configurationType, configurationSubType);
	}
	
	@Test
	public void verifyNotExists() {
		AAIUri path = AAIUriFactory.createResourceUri(AAIObjectType.CONFIGURATION, "test2");
		wireMockRule.stubFor(get(
				urlPathEqualTo("/aai/v11" + path.build()))
				.willReturn(
					aResponse()
					.withHeader("Content-Type", "text/plain")
					.withBody("hello")
					.withStatus(404)));
		AAIConfigurationClient aaiConfiguration = new AAIConfigurationClient();
		boolean result = aaiConfiguration.configurationExists("test2");
		assertEquals("path not found", false, result);
	}
}
