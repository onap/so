/*
 * © 2014 AT&T Intellectual Property. All rights reserved. Used under license from AT&T Intellectual Property.
 */
package org.openecomp.mso.bpmn.mock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

/**
 * Please describe the StubResponseSNIRO.java class
 *
 * @author cb645j
 */
public class StubResponseSNIRO {

	public static void setupAllMocks() {

	}

	public static void mockSNIRO() {
		stubFor(post(urlEqualTo("/sniro/api/v2/placement"))
				.willReturn(aResponse()
						.withStatus(202)
						.withHeader("Content-Type", "application/json")));
	}

	public static void mockSNIRO(String responseFile) {
		stubFor(post(urlEqualTo("/sniro/api/v2/placement"))
				.willReturn(aResponse()
						.withStatus(202)
						.withHeader("Content-Type", "application/json")
						.withBodyFile(responseFile)));
	}

	public static void mockSNIRO_400() {
		stubFor(post(urlEqualTo("/sniro/api/v2/placement"))
				.willReturn(aResponse()
						.withStatus(400)
						.withHeader("Content-Type", "application/json")));
	}

	public static void mockSNIRO_500() {
		stubFor(post(urlEqualTo("/sniro/api/v2/placement"))
				.willReturn(aResponse()
						.withStatus(500)
						.withHeader("Content-Type", "application/json")));
	}

}
