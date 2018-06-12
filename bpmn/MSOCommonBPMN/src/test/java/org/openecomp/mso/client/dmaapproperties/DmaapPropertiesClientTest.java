package org.openecomp.mso.client.dmaapproperties;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpStatus;
import org.junit.Rule;
import org.junit.Test;
import org.openecomp.mso.BaseTest;
import org.openecomp.mso.client.avpn.dmaap.beans.AVPNDmaapBean;
import org.openecomp.mso.client.exception.MapperException;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class DmaapPropertiesClientTest extends BaseTest{
	
	@Autowired
	private DmaapPropertiesClient dmaapPropertiesClient;

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(28090));

	private final String file = "src/test/resources/org/openecomp/mso/client/avpn/dmaap/avpnDmaapAsyncRequestStatus.json";
	private String requestId = "rq1234d1-5a33-55df-13ab-12abad84e331";
	private String clientSource = "SPP";
	private String correlator = "myClientId123";
	private String serviceInstanceId = "bc305d54-75b4-431b-adb2-eb6b9e546014";
	private String startTime = "2017-11-17T09:30:47Z";
	private String finishTime = "2017-11-17T09:30:47Z";
	private String requestScope = "service";
	private String requestType = "createInstance";
	private String timestamp = "2017-11-17T09:30:47Z";
	private String requestState = "COMPLETE";
	private String statusMessage = "Success";
	private String percentProgress = "100";
	
	@Test
	public void testBuildRequestJson() throws MapperException, IOException {
		AVPNDmaapBean actualBean = dmaapPropertiesClient.buildRequestJson(requestId, clientSource, correlator, serviceInstanceId, startTime, finishTime, requestScope,
																			requestType, timestamp, requestState, statusMessage, percentProgress, true);

		AVPNDmaapBean expected = new ObjectMapper().readValue(new File(file), AVPNDmaapBean.class);

		assertNotNull(actualBean);
		assertThat(actualBean, sameBeanAs(expected));
	}

	@Test
	public void testDmaapPublishRequest() throws JsonProcessingException, MapperException {
		wireMockRule.stubFor(post(urlEqualTo("/events/org.onap.so.asyncStatusUpdate?timeout=20000"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(HttpStatus.SC_ACCEPTED)));

		dmaapPropertiesClient.dmaapPublishRequest(requestId, clientSource, correlator, serviceInstanceId, startTime, finishTime, requestScope,
													requestType, timestamp, requestState, statusMessage, percentProgress, false);
	}
}
