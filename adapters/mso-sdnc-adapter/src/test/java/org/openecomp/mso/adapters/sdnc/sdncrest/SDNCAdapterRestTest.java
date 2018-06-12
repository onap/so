package org.openecomp.mso.adapters.sdnc.sdncrest;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.mso.adapters.sdnc.SDNCAdapterApplication;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.serviceinstancebeans.RequestReferences;
import org.openecomp.mso.serviceinstancebeans.ServiceInstancesResponse;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import ch.qos.logback.classic.spi.ILoggingEvent;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SDNCAdapterApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test","non-async"})
public class SDNCAdapterRestTest {
	
	@LocalServerPort
	private int port;
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(8089);
	protected TestRestTemplate restTemplate = new TestRestTemplate("test", "test");
	protected HttpHeaders headers = new HttpHeaders();	
	
	
	@Test
	public void SDNC_Sunny_Day_REST() throws JsonParseException, JsonMappingException, IOException{
		wireMockRule.stubFor(post(urlPathEqualTo("/mso/WorkflowMessage"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withStatus(org.apache.http.HttpStatus.SC_OK)));
		
		String uri =  "/adapters/rest/v1/sdnc";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendRequest(inputStream("/RestAdapterRequest.json"), uri, HttpMethod.POST);
		
		assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatusCode().value());
	}
	
	@Test
	public void SDNC_Sunny_Day_REST_CALLBACK() throws JsonParseException, JsonMappingException, IOException{

		String uri =  "/adapters/rest/SDNCNotify";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/RestCallback.xml"), uri, HttpMethod.POST);
		
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
	}
	
	@Test
	public void SDNC_Sunny_Day_REST_CALLBACK_ATT_Services() throws JsonParseException, JsonMappingException, IOException{
		wireMockRule.stubFor(post(urlPathEqualTo("/mso/WorkflowMessage/SDNCAResponse/27fa0fec-4b44-48da-a69b-eadcbfa93780-1526587067790%0A%09%09"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withStatus(org.apache.http.HttpStatus.SC_OK)));
		String uri =  "/adapters/rest/SDNCNotify/attservices";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/SDNCATTServicesCallbackRequest.xml"), uri, HttpMethod.POST);
		
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
		verify(postRequestedFor(urlEqualTo("/mso/WorkflowMessage/SDNCAResponse/27fa0fec-4b44-48da-a69b-eadcbfa93780-1526587067790%0A%09%09"))
				.withRequestBody(equalToJson("{\"SDNCServiceResponse\":{\"sdncRequestId\":\"27fa0fec-4b44-48da-a69b-eadcbfa93780-1526587067790\\n\\t\\t\",\"responseCode\":\"200\",\"responseMessage\":\"200: ActivateVnfVrouterSuccess\",\"ackFinalIndicator\":\"Y\",\"params\":{\"vnf-name\":\"USUCPM072AD0101UJRT01\"}}}")));
	}
	
	@Test
	public void SDNC_Sunny_Day_REST_EVENT() throws JsonParseException, JsonMappingException, IOException{
		wireMockRule.stubFor(post(urlPathEqualTo("/mso/WorkflowMessage/SDNCAEvent/USUCPMGO4AD0101UJZZ01"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withStatus(org.apache.http.HttpStatus.SC_OK)));
		String uri =  "/adapters/rest/SDNCNotify/event";
		headers.set("X-ECOMP-RequestID", "123456789456127");
		ResponseEntity<String> response = sendXMLRequest(inputStream("/SDNCEventRequest.xml"), uri, HttpMethod.POST);		
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());
		verify(postRequestedFor(urlEqualTo("/mso/WorkflowMessage/SDNCAEvent/USUCPMGO4AD0101UJZZ01"))
				.withRequestBody(equalToJson("{\"SDNCEvent\":{\"eventType\":\"UCPE-ACTIVATION\",\"eventCorrelatorType\":\"UCPE-HOST-NAME\",\"eventCorrelator\":\"USUCPMGO4AD0101UJZZ01\",\"params\":{\"success-indicator\":\"Y\"}}}")));
	}
	
	public String inputStream(String JsonInput)throws IOException{
		JsonInput = "src/test/resources/" + JsonInput;
		String input = new String(Files.readAllBytes(Paths.get(JsonInput)));
		return input;
	}
	
	public ResponseEntity<String> sendRequest(String requestJson, String uriPath, HttpMethod reqMethod){		 
		headers.set("Accept", MediaType.APPLICATION_JSON);
		headers.set("Content-Type",MediaType.APPLICATION_JSON);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(uriPath));
		
		HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);  
		ResponseEntity<String> response = restTemplate.exchange(builder.toUriString(),
				reqMethod, request, String.class);
		
		return response;
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
	

	
	protected String readJsonFileAsString(String fileLocation) throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = mapper.readTree(new File(fileLocation));
		return jsonNode.asText();
	}
	
	protected String createURLWithPort(String uri) {
		return "http://localhost:" + port + uri;
	}
}
