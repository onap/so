package org.openecomp.mso.adapters.sdnc.sdncrest;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.openecomp.mso.adapters.sdnc.BaseTest;
import org.openecomp.mso.adapters.sdncrest.SDNCResponseCommon;
import org.springframework.beans.factory.annotation.Autowired;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.assertNotNull;

public class SDNCConnectorTest extends BaseTest {

    @Autowired
    private SDNCConnector sdncConnector;

    @Test
    public void sendTest() {
        String content = "<dummy><service-instance-id>1234</service-instance-id></dummy>";

        String response = "<errors xmlns=\"urn:ietf:params:xml:ns:yang:ietf-restconf\">\n" +
                "\t\t//   <error>\n" +
                "\t\t//     <error-type>protocol</error-type>\n" +
                "\t\t//     <error-tag>malformed-message</error-tag>\n" +
                "\t\t//     <error-message>Error parsing input: The element type \"input\" must be terminated by the matching end-tag \"&lt;/input&gt;\".</error-message>\n" +
                "\t\t//   </error>\n" +
                "\t\t// </errors>";

        TypedRequestTunables rt = new TypedRequestTunables("", "");
        rt.setTimeout("1000");
        rt.setReqMethod("POST");
        rt.setSdncUrl("http://localhost:8089/sdnc");

        wireMockRule.stubFor(post(urlPathEqualTo("/sdnc"))
                .willReturn(aResponse().withHeader("Content-Type", "application/xml").withBody(response).withStatus(HttpStatus.SC_MULTIPLE_CHOICES)));

        SDNCResponseCommon responseCommon = sdncConnector.send(content, rt);
        assertNotNull(responseCommon);
    }
}