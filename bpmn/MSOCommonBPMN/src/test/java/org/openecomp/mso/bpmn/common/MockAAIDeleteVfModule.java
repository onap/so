package org.openecomp.mso.bpmn.common;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

public class MockAAIDeleteVfModule {
	
	public MockAAIDeleteVfModule()
	{
		stubFor(delete(urlMatching(
				"/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c721/vf-modules/vf-module/973ed047-d251-4fb9-bf1a-65b8949e0a73/[?]resource-version=0000073"))
						.willReturn(aResponse().withStatus(200)));
		stubFor(delete(urlMatching(
				"/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c720/vf-modules/vf-module/973ed047-d251-4fb9-bf1a-65b8949e0a75/[?]resource-version=0000075"))
						.willReturn(aResponse().withStatus(200)));
		stubFor(delete(urlMatching(
				"/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c718/vf-modules/vf-module/973ed047-d251-4fb9-bf1a-65b8949e0a78/[?]resource-version=0000078"))
						.willReturn(aResponse().withStatus(200)));
		stubFor(delete(urlMatching(
				"/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c719/vf-modules/vf-module/973ed047-d251-4fb9-bf1a-65b8949e0a77/[?]resource-version=0000077"))
						.willReturn(aResponse().withStatus(500).withHeader("Content-Type", "text/xml")
								.withBodyFile("aaiFault.xml")));
		stubFor(get(urlMatching("/aai/v[0-9]+/network/network-policies/network-policy\\?network-policy-fqdn=.*"))
				.willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/xml")
						.withBodyFile("VfModularity/QueryNetworkPolicy_AAIResponse_Success.xml")));

		stubFor(delete(urlMatching("/aai/v[0-9]+/network/network-policies/network-policy/.*"))
				.willReturn(aResponse().withStatus(200)));
	}
}
