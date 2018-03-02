package org.openecomp.mso.apihandlerinfra.tenantisolation.mock;

import java.io.File;
import java.nio.file.Files;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.openecomp.mso.apihandlerinfra.Constants;
import org.openecomp.mso.properties.MsoPropertiesFactory;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class MockTest {
	
	@Rule
	public final WireMockRule wireMockRule;

	public MockTest() {
		wireMockRule = new WireMockRule(WireMockConfiguration.wireMockConfig().port(28090)); //.extensions(transformerArray));
	}
	
	@BeforeClass
	public static void setUp() throws Exception {
		MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();
		msoPropertiesFactory.removeAllMsoProperties();
		msoPropertiesFactory.initializeMsoProperties(Constants.MSO_PROP_APIHANDLER_INFRA, "src/test/resources/mso.apihandler-infra.properties");
	}

	public String getFileContentsAsString(String fileName) {

		String content = "";
		try {
			ClassLoader classLoader = this.getClass().getClassLoader();
			File file = new File(classLoader.getResource(fileName).getFile());
			content = new String(Files.readAllBytes(file.toPath()));
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Exception encountered reading " + fileName + ". Error: " + e.getMessage() + ". Make sure to specify the correct path.");
		}
		return content;
	}
}
