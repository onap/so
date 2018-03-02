package org.openecomp.mso.client.aai;

import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.openecomp.mso.client.aai.entities.uri.AAIUri;
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory;
import org.openecomp.mso.serviceinstancebeans.ModelInfo;
import org.openecomp.mso.serviceinstancebeans.RequestDetails;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class AAIConfigurationTest {

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(8443));
	
	@BeforeClass
	public static void setUp() {
		System.setProperty("mso.config.path", "src/test/resources");
		System.setProperty("javax.net.ssl.keyStore", "C:/etc/ecomp/mso/config/msoClientKeyStore.jks");
		System.setProperty("javax.net.ssl.keyStorePassword", "mso4you");
		System.setProperty("javax.net.ssl.trustStore", "C:/etc/ecomp/mso/config/msoTrustStore.jks");
		System.setProperty("javax.net.ssl.trustStorePassword", "mso_Domain2.0_4you");
	}
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
