package org.openecomp.mso.client.aai;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri;
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
public class AAIResourcesClientTest {

	
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(8443));
	
	@BeforeClass
	public static void setUp() {
		System.setProperty("mso.config.path", "src/test/resources");
	}
	
	@Test
	public void verifyNotExists() {
		AAIResourceUri path = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "test");
		wireMockRule.stubFor(get(
				urlPathEqualTo("/aai/v11" + path.build()))
				.willReturn(
					aResponse()
					.withHeader("Content-Type", "text/plain")
					.withBody("hello")
					.withStatus(404)));
		AAIResourcesClient client = new AAIResourcesClient();
		boolean result = client.exists(path);
		assertEquals("path not found", false, result);
	}
	
	@Test
	public void verifyDelete() {
		AAIResourceUri path = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "test2");
		wireMockRule.stubFor(get(
				urlPathEqualTo("/aai/v11" + path.build()))
				.willReturn(
					aResponse()
					.withHeader("Content-Type", "application/json")
					.withBodyFile("aai/resources/mockObject.json")
					.withStatus(200)));
		wireMockRule.stubFor(delete(
				urlPathEqualTo("/aai/v11" + path.build()))
				.withQueryParam("resource-version", equalTo("1234"))
				.willReturn(
					aResponse()
					.withStatus(204)));
		AAIResourcesClient client = new AAIResourcesClient();
		client.delete(path);
	}
	
	@Test
	public void verifyConnect() {
		AAIResourceUri path = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "test2");
		AAIResourceUri path2 = AAIUriFactory.createResourceUri(AAIObjectType.GENERIC_VNF, "test3");
		wireMockRule.stubFor(put(
				urlPathEqualTo("/aai/v11" + path.build() + "/relationship-list/relationship"))
				.willReturn(
					aResponse()
					.withHeader("Content-Type", "application/json")
					.withStatus(200)));
		
		AAIResourceUri pathClone = path.clone();
		AAIResourcesClient client = new AAIResourcesClient();
		client.connect(path, path2);
		assertEquals("uri not modified", pathClone.build().toString(), path.build().toString());
	}
}
