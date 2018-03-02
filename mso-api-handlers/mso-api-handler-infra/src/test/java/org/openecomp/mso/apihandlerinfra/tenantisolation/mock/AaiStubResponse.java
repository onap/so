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
