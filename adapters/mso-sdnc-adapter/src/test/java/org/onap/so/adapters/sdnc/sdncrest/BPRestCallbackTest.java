package org.onap.so.adapters.sdnc.sdncrest;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.onap.so.adapters.sdnc.BaseTest;
import org.springframework.beans.factory.annotation.Autowired;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BPRestCallbackTest extends BaseTest {

    @Autowired
    private BPRestCallback bpRestCallback;

    @Test
    public void sendTest(){
        String response = "<errors xmlns=\"urn:ietf:params:xml:ns:yang:ietf-restconf\">\n" +
                "\t\t//   <error>\n" +
                "\t\t//     <error-type>protocol</error-type>\n" +
                "\t\t//     <error-tag>malformed-message</error-tag>\n" +
                "\t\t//     <error-message>Error parsing input: The element type \"input\" must be terminated by the matching end-tag \"&lt;/input&gt;\".</error-message>\n" +
                "\t\t//   </error>\n" +
                "\t\t// </errors>";

        stubFor(post(urlPathEqualTo("/sdnc"))
                .willReturn(aResponse().withHeader("Content-Type", "application/xml").withBody(response).withStatus(HttpStatus.SC_MULTIPLE_CHOICES)));

        boolean responseCommon = bpRestCallback.send("http://localhost:" + wireMockPort + "/sdnc", "Test");
        assertNotNull(responseCommon);
        assertEquals(true,responseCommon);
    }
}
