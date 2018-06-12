package org.openecomp.mso.adapters.valet;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.CoreMatchers.equalTo;

import java.io.File;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.mso.adapters.valet.beans.ValetConfirmResponse;
import org.openecomp.mso.adapters.valet.beans.ValetCreateResponse;
import org.openecomp.mso.adapters.valet.beans.ValetDeleteResponse;
import org.openecomp.mso.adapters.valet.beans.ValetRollbackResponse;
import org.openecomp.mso.adapters.valet.beans.ValetUpdateResponse;
import org.openecomp.mso.adapters.vnf.BaseRestTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ValetClientTest extends BaseRestTestUtils {
	@Autowired
	protected ValetClient client;
	
	private ObjectMapper mapper = new ObjectMapper();

	@Before
	public void init() {
		client.baseUrl = "http://localhost:" + wireMockRule.port();
	}
	
	@Test
	public void testCallValetCreateRequest() throws Exception {	
		ValetCreateResponse vcr = mapper.readValue(new File("src/test/resources/__files/ValetCreateRequest.json"), ValetCreateResponse.class);
		GenericValetResponse<ValetCreateResponse> expected = new GenericValetResponse<ValetCreateResponse>(HttpStatus.SC_OK, ValetClient.NO_STATUS_RETURNED, vcr);
		
		wireMockRule.stubFor(post(urlEqualTo("/api/valet/placement/v1/?requestId=requestId")) //
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(mapper.writeValueAsString(vcr)).withStatus(HttpStatus.SC_OK)));
		
		GenericValetResponse<ValetCreateResponse> actual = client.callValetCreateRequest("requestId", "regionId", "tenantId", "serviceInstanceId", "vnfId", "vnfName", "vfModuleId", "vfModuleName", "keystoneUrl", null);

		assertThat(actual, sameBeanAs(expected));
	}
	
	@Test
	public void testCallValetUpdateRequest() throws Exception {	
		ValetUpdateResponse vur = mapper.readValue(new File("src/test/resources/__files/ValetCreateRequest.json"), ValetUpdateResponse.class);
		GenericValetResponse<ValetUpdateResponse> expected = new GenericValetResponse<ValetUpdateResponse>(HttpStatus.SC_OK, ValetClient.NO_STATUS_RETURNED, vur);
		
		wireMockRule.stubFor(put(urlEqualTo("/api/valet/placement/v1/?requestId=requestId")) //
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(mapper.writeValueAsString(vur)).withStatus(HttpStatus.SC_OK)));
		
		GenericValetResponse<ValetUpdateResponse> actual = client.callValetUpdateRequest("requestId", "regionId", "tenantId", "serviceInstanceId", "vnfId", "vnfName", "vfModuleId", "vfModuleName", "keystoneUrl", null);

		assertThat(actual, sameBeanAs(expected));
	}
	
	@Test
	public void testCallValetDeleteRequest() throws Exception {
		ValetDeleteResponse vdr = mapper.readValue(new File("src/test/resources/__files/ValetDeleteRequest.json"), ValetDeleteResponse.class);
		GenericValetResponse<ValetDeleteResponse> expected = new GenericValetResponse<ValetDeleteResponse>(HttpStatus.SC_OK, ValetClient.NO_STATUS_RETURNED, vdr);
		
		wireMockRule.stubFor(delete(urlEqualTo("/api/valet/placement/v1/?requestId=requestId")) //
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(mapper.writeValueAsString(vdr)).withStatus(HttpStatus.SC_OK)));
		
		GenericValetResponse<ValetDeleteResponse> actual = client.callValetDeleteRequest("requestId", "regionId", "tenantId", "vfModuleId", "vfModuleName");

		assertThat(actual, sameBeanAs(expected));
	}
	
	@Test
	public void testCallValetConfirmRequest() throws Exception {		
		ValetConfirmResponse vcr = new ValetConfirmResponse();
		GenericValetResponse<ValetConfirmResponse> expected = new GenericValetResponse<ValetConfirmResponse>(HttpStatus.SC_OK, ValetClient.NO_STATUS_RETURNED, vcr);
		
		wireMockRule.stubFor(put(urlPathEqualTo("/api/valet/placement/v1/requestId/confirm/")) //
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("{}").withStatus(HttpStatus.SC_OK)));
		
		GenericValetResponse<ValetConfirmResponse> actual = client.callValetConfirmRequest("requestId", "stackId");

		assertThat(actual, sameBeanAs(expected));
	}
	
	@Test
	public void testCallValetRollbackRequest() throws Exception {		
		ValetRollbackResponse vrr = new ValetRollbackResponse();	
		GenericValetResponse<ValetRollbackResponse> expected = new GenericValetResponse<ValetRollbackResponse>(HttpStatus.SC_OK, ValetClient.NO_STATUS_RETURNED, vrr);
		
		wireMockRule.stubFor(put(urlPathEqualTo("/api/valet/placement/v1/requestId/rollback/")) //
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("{}").withStatus(HttpStatus.SC_OK)));
		
		GenericValetResponse<ValetRollbackResponse> actual = client.callValetRollbackRequest("requestId", "stackId", true, "error");

		assertThat(actual, sameBeanAs(expected));
	}
}
