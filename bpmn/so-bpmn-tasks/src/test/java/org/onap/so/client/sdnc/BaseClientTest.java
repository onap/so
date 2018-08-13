package org.onap.so.client.sdnc;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import wiremock.org.apache.http.entity.ContentType;
public class BaseClientTest {

	
	@Rule
	public WireMockRule wm = new WireMockRule(options().dynamicPort());
	
	@Test
	public void verifyString() {
		BaseClient<String, String> client = new BaseClient<>();
		String response = "{\"hello\" : \"world\"}";
		client.setTargetUrl(UriBuilder.fromUri("http://localhost/test").port(wm.port()).build().toString());
		wm.stubFor(get(urlEqualTo("/test"))
                .willReturn(aResponse().withStatus(200).withBody(response).withHeader("Content-Type", ContentType.APPLICATION_JSON.toString())));
		
		String result = client.get("", new ParameterizedTypeReference<String>() {});
		assertThat(result, equalTo(response));
	}
	
	@Test
	public void verifyMap() {
		BaseClient<String, Map<String, Object>> client = new BaseClient<>();
		String response = "{\"hello\" : \"world\"}";
		client.setTargetUrl(UriBuilder.fromUri("http://localhost/test").port(wm.port()).build().toString());
		wm.stubFor(get(urlEqualTo("/test"))
                .willReturn(aResponse().withStatus(200).withBody(response).withHeader("Content-Type", ContentType.APPLICATION_JSON.toString())));
		
		Map<String, Object> result = client.get("", new ParameterizedTypeReference<Map<String, Object>>() {});
		assertThat("world", equalTo(result.get("hello")));
	}
}
