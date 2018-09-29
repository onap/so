package org.onap.so.bpmn.common.recipe;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.onap.so.BaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BpmnRestClientTest extends BaseTest{

    @Autowired
    private BpmnRestClient bpmnRestClient;

    @Test
    public void postTest() throws IOException, Exception{
        stubFor(post(urlPathMatching("/testRecipeUri"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(org.springframework.http.HttpStatus.OK.value()).withBody("{}")));

        HttpResponse httpResponse = bpmnRestClient.post(
                "http://localhost:" + wireMockPort +"/testRecipeUri",
                "test-req-id",
                1000,
                "testRequestAction",
                "1234",
                "testServiceType",
                "testRequestDetails",
                "testRecipeparamXsd");

        assertNotNull(httpResponse);
        assertEquals(HttpStatus.SC_OK,httpResponse.getStatusLine().getStatusCode());
    }
}
