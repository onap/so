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

package org.onap.so.adapters.vnf;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.adapters.openstack.MsoOpenstackAdaptersApplication;
import org.onap.so.cloud.CloudConfig;
import org.onap.so.db.catalog.beans.AuthenticationType;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.beans.ServerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.reset;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MsoOpenstackAdaptersApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
public class BaseRestTestUtils {
	@Value("${wiremock.server.port}")
    protected int wireMockPort;
	@Autowired
	CloudConfig cloudConfig;

	@Autowired
	@Qualifier("JettisonStyle")
	protected TestRestTemplate restTemplate;

	protected HttpHeaders headers = new HttpHeaders();	
	
	@LocalServerPort
	private int port;

	public ObjectMapper mapper;

	public String orchestrator = "orchestrator";
	public String cloudEndpoint = "/v2.0";

	
	protected String readJsonFileAsString(String fileLocation) throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = mapper.readTree(new File(fileLocation));
		return jsonNode.asText();
	}
	
	protected String createURLWithPort(String uri) {
		return "http://localhost:" + port + uri;
	}
	
	protected String readFile(String fileName) throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			return sb.toString();
		}
	}

	/***
	 * Before each test execution, updating IdentityUrl port value to the ramdom wireMockPort
	 * Since URL will be used as a rest call and required to be mocked in unit tests
	 */
	@Before
	public void setUp() throws Exception {
		reset();
		mapper = new ObjectMapper();
		CloudIdentity identity = new CloudIdentity();
		identity.setId("MTN13");
		identity.setMsoId("m93945");
		identity.setMsoPass("93937EA01B94A10A49279D4572B48369");
		identity.setAdminTenant("admin");
		identity.setMemberRole("admin");
		identity.setTenantMetadata(new Boolean(true));
		identity.setIdentityUrl("http://localhost:" + wireMockPort + cloudEndpoint);

		identity.setIdentityAuthenticationType(AuthenticationType.USERNAME_PASSWORD);

		CloudSite cloudSite = new CloudSite();
		cloudSite.setId("MTN13");
		cloudSite.setCloudVersion("3.0");
		cloudSite.setClli("MDT13");
		cloudSite.setRegionId("mtn13");
		cloudSite.setOrchestrator(orchestrator);
		identity.setIdentityServerType(ServerType.KEYSTONE);
		cloudSite.setIdentityService(identity);

		stubFor(get(urlPathEqualTo("/cloudSite/MTN13")).willReturn(aResponse()
				.withBody(getBody(mapper.writeValueAsString(cloudSite),wireMockPort, ""))
				.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
				.withStatus(HttpStatus.SC_OK)));
		stubFor(get(urlPathEqualTo("/cloudSite/DEFAULT")).willReturn(aResponse()
				.withBody(getBody(mapper.writeValueAsString(cloudSite),wireMockPort, ""))
				.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON)
				.withStatus(HttpStatus.SC_OK)));
		stubFor(get(urlPathEqualTo("/cloudIdentity/MTN13")).willReturn(aResponse()
				.withBody(getBody(mapper.writeValueAsString(identity),wireMockPort, ""))
				.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON)
				.withStatus(HttpStatus.SC_OK)));
			cloudConfig.getCloudSite("MTN13").get().getIdentityService().setIdentityUrl("http://localhost:" + wireMockPort + cloudEndpoint);
	}

	protected static String getBody(String body, int port, String urlPath) throws IOException {
		return body.replaceAll("port", "http://localhost:" + port + urlPath);
	}
	
	@Test
	public void testNothing(){
		
	}

}
