package org.openecomp.mso.client.sniro;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import org.junit.Rule;
import org.junit.Test;
import org.openecomp.mso.BaseIntegrationTest;
import org.openecomp.mso.client.exception.BadResponseException;
import org.openecomp.mso.client.sniro.beans.SniroConductorRequest;
import org.openecomp.mso.client.sniro.beans.SniroManagerRequest;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class SniroClientTestIT extends BaseIntegrationTest{

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(8446));

    @Autowired
    private SniroClient client;


    @Test(expected = Test.None.class)
    public void testPostDemands_success() throws BadResponseException, JsonProcessingException {
        String mockResponse = "{\"transactionId\": \"123456789\", \"requestId\": \"1234\", \"statusMessage\": \"corys cool\", \"requestStatus\": \"accepted\"}";

        wireMockRule.stubFor(post(urlEqualTo("/sniro/api/placement/v2"))
        			.willReturn(aResponse().withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(mockResponse)));

   	  client.postDemands(new SniroManagerRequest());

    }

    @Test(expected = BadResponseException.class)
    public void testPostDemands_error_failed() throws JsonProcessingException, BadResponseException {
    	String mockResponse = "{\"transactionId\": \"123456789\", \"requestId\": \"1234\", \"statusMessage\": \"missing data\", \"requestStatus\": \"failed\"}";

    	wireMockRule.stubFor(post(urlEqualTo("/sniro/api/placement/v2"))
    			.willReturn(aResponse().withStatus(200)
    					.withHeader("Content-Type", "application/json")
    					.withBody(mockResponse)));


    	client.postDemands(new SniroManagerRequest());

    	//TODO	assertEquals("missing data", );

    }

    @Test(expected = BadResponseException.class)
    public void testPostDemands_error_noMessage() throws JsonProcessingException, BadResponseException {
    	String mockResponse = "{\"transactionId\": \"123456789\", \"requestId\": \"1234\", \"statusMessage\": \"\", \"requestStatus\": \"failed\"}";

    	wireMockRule.stubFor(post(urlEqualTo("/sniro/api/placement/v2"))
    			.willReturn(aResponse().withStatus(200)
    					.withHeader("Content-Type", "application/json")
    					.withBody(mockResponse)));


    	client.postDemands(new SniroManagerRequest());

    }

    @Test(expected = BadResponseException.class)
    public void testPostDemands_error_noStatus() throws JsonProcessingException, BadResponseException {
    	String mockResponse = "{\"transactionId\": \"123456789\", \"requestId\": \"1234\", \"statusMessage\": \"missing data\", \"requestStatus\": null}";

    	wireMockRule.stubFor(post(urlEqualTo("/sniro/api/placement/v2"))
    			.willReturn(aResponse().withStatus(200)
    					.withHeader("Content-Type", "application/json")
    					.withBody(mockResponse)));


    	client.postDemands(new SniroManagerRequest());

    }

    @Test(expected = BadResponseException.class)
    public void testPostDemands_error_empty() throws JsonProcessingException, BadResponseException {
    	String mockResponse = "{ }";

    	wireMockRule.stubFor(post(urlEqualTo("/sniro/api/placement/v2"))
    			.willReturn(aResponse().withStatus(200)
    					.withHeader("Content-Type", "application/json")
    					.withBody(mockResponse)));


    	client.postDemands(new SniroManagerRequest());
    }

    @Test(expected = Test.None.class)
    public void testPostRelease_success() throws BadResponseException, JsonProcessingException {
        String mockResponse = "{\"status\": \"success\", \"message\": \"corys cool\"}";

        wireMockRule.stubFor(post(urlEqualTo("/v1/release-orders"))
        			.willReturn(aResponse().withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(mockResponse)));

   	  client.postRelease(new SniroConductorRequest());
    }

    @Test(expected = BadResponseException.class)
    public void testPostRelease_error_failed() throws BadResponseException, JsonProcessingException {
        String mockResponse = "{\"status\": \"failure\", \"message\": \"corys cool\"}";

        wireMockRule.stubFor(post(urlEqualTo("/v1/release-orders"))
        			.willReturn(aResponse().withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(mockResponse)));

   	  client.postRelease(new SniroConductorRequest());
    }

    @Test(expected = BadResponseException.class)
    public void testPostRelease_error_noStatus() throws BadResponseException, JsonProcessingException {
        String mockResponse = "{\"status\": \"\", \"message\": \"corys cool\"}";

        wireMockRule.stubFor(post(urlEqualTo("/v1/release-orders"))
        			.willReturn(aResponse().withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(mockResponse)));

   	  client.postRelease(new SniroConductorRequest());

    }

    @Test(expected = BadResponseException.class)
    public void testPostRelease_error_noMessage() throws BadResponseException, JsonProcessingException {
        String mockResponse = "{\"status\": \"failure\", \"message\": null}";

        wireMockRule.stubFor(post(urlEqualTo("/v1/release-orders"))
        			.willReturn(aResponse().withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(mockResponse)));

   	  client.postRelease(new SniroConductorRequest());

    }

    @Test(expected = BadResponseException.class)
    public void testPostRelease_error_empty() throws BadResponseException, JsonProcessingException {
        String mockResponse = "{ }";

        wireMockRule.stubFor(post(urlEqualTo("/v1/release-orders"))
        			.willReturn(aResponse().withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(mockResponse)));

   	  client.postRelease(new SniroConductorRequest());

    }

}
