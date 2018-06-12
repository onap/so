package org.openecomp.mso.adapters.sdnc.impl;

import org.junit.Test;
import org.openecomp.mso.adapters.sdnc.BaseTest;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertNotNull;

public class SDNCAdapterRestImplTest extends BaseTest {

    HttpHeaders headers = new HttpHeaders();

    @LocalServerPort
    private int port;

    @Test
    public void MSORequestTest() {
        headers.set("Content-Type", "application/xml");
        headers.set("mso-request-id", "1234");
        headers.set("mso-request-action", "create");
        headers.set("mso-request-operation", "post");
        String request = "<dummy><service-instance-id>1234</service-instance-id></dummy>";
        SDNCResponse sdncResponse = new SDNCResponse("");
        sdncResponse.setRespCode(200);
        HttpEntity<String> entity = new HttpEntity<String>(request, headers);
        ResponseEntity<String> response = restTemplate.exchange("http://localhost:" + port + "/adapters/rest/MSORequest", HttpMethod.POST, entity, String.class);
        assertNotNull(response);
    }
}