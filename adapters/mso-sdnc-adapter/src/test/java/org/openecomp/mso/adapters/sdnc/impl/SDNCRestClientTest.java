package org.openecomp.mso.adapters.sdnc.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.assertNotNull;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.openecomp.mso.adapters.sdnc.BaseTest;
import org.springframework.beans.factory.annotation.Autowired;


public class SDNCRestClientTest extends BaseTest {

    @Autowired
    private SDNCRestClient sdncClient;

    @Test
    public void getSdncRespTestException() {

        RequestTunables rt = new RequestTunables("", "", "", "");
        rt.setTimeout("1000");
        rt.setReqMethod("POST");
        rt.setSdncUrl("http://localhost:8089/sdnc");

        wireMockRule.stubFor(post(urlPathEqualTo("/sdnc"))
                .willReturn(aResponse().withHeader("Content-Type", "application/xml").withBody("").withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)));

        SDNCResponse response = sdncClient.getSdncResp("", rt);
        assertNotNull(response);
    }
}