package org.openecomp.mso.adapters.network;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.openecomp.mso.adapters.vnf.BaseRestTestUtils;
import org.openecomp.mso.cloud.CloudConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class MSONetworkAdapterImplTest extends BaseRestTestUtils {
	
	@Autowired
	private CloudConfig cloudConfig;
	
	@Test
	public void Create_Network_By_Model_Name() throws JsonParseException, JsonMappingException, IOException{ 
		int wiremockPort = wireMockRule.port();
		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wiremockPort + "/v2.0");
		
		String access = this.readFile("src/test/resources/__files/OpenstackResponse_Access.json");
		String replaced = access.replaceAll("port", "http://localhost:" + wiremockPort + "/mockPublicUrl");
		
		wireMockRule.stubFor(post(urlPathEqualTo("/v2.0/tokens")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody(replaced).withStatus(HttpStatus.SC_OK)));

		wireMockRule.stubFor(post(urlPathEqualTo("/mockPublicUrl/stacks"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBodyFile("OpenstackResponse_Stack.json").withStatus(HttpStatus.SC_OK)));

		wireMockRule.stubFor(
				get(urlPathEqualTo("/mockPublicUrl/stacks/dvspg-VCE_VPE-mtjnj40avbc"))
						.willReturn(aResponse().withHeader("Content-Type", "application/json")
								.withBodyFile("OpenstackResponse_Stack_Created.json").withStatus(HttpStatus.SC_OK)));
		
		String uri =  "/services/NetworkAdapter";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/CreateNetwork.xml"), uri, HttpMethod.POST);		
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
		
	}
	
	@Test
	public void deleteNetwork() throws JsonParseException, JsonMappingException, IOException{ 
		int wiremockPort = wireMockRule.port();
		cloudConfig.getIdentityService("MTN13").setIdentityUrl("http://localhost:" + wiremockPort + "/v2.0");
		
		String access = this.readFile("src/test/resources/__files/OpenstackResponse_Access.json");
		String replaced = access.replaceAll("port", "http://localhost:" + wiremockPort + "/mockPublicUrl");
		
		wireMockRule.stubFor(post(urlPathEqualTo("/v2.0/tokens")).willReturn(aResponse()
				.withHeader("Content-Type", "application/json").withBody(replaced).withStatus(HttpStatus.SC_OK)));

		wireMockRule.stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/sharan/stackId"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBodyFile("OpenstackResponse_Stack_DeleteComplete.json").withStatus(HttpStatus.SC_OK)));

		wireMockRule.stubFor(delete(urlPathEqualTo("/mockPublicUrl/stacks/sharan/stackId"))
				.willReturn(aResponse().withStatus(HttpStatus.SC_OK)));

		wireMockRule.stubFor(
				get(urlPathEqualTo("/mockPublicUrl/stacks/43173f6a-d699-414b-888f-ab243dda6dfe"))
						.willReturn(aResponse().withHeader("Content-Type", "application/json")
								.withBodyFile("OpenstackResponse_Stack_Created.json").withStatus(HttpStatus.SC_OK)));
		
		String uri =  "/services/NetworkAdapter";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/DeleteNetwork.xml"), uri, HttpMethod.POST);		
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
		
	}
	
	public ResponseEntity<String> sendXMLRequest(String requestJson, String uriPath, HttpMethod reqMethod){		 
		headers.set("Accept", MediaType.APPLICATION_XML);
		headers.set("Content-Type",MediaType.APPLICATION_XML);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(uriPath));
		
		HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);  
		ResponseEntity<String> response = restTemplate.exchange(builder.toUriString(),
				reqMethod, request, String.class);
		
		return response;
	}
	
	public String inputStream(String JsonInput)throws IOException{
		JsonInput = "src/test/resources/" + JsonInput;
		String input = new String(Files.readAllBytes(Paths.get(JsonInput)));
		return input;
	}
}
