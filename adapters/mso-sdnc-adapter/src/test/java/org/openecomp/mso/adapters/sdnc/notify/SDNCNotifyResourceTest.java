package org.openecomp.mso.adapters.sdnc.notify;

import org.junit.Test;
import org.openecomp.mso.adapters.sdnc.BaseTest;
import org.openecomp.mso.adapters.sdnc.FileUtil;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

public class SDNCNotifyResourceTest extends BaseTest  {

    @LocalServerPort
    private int port;

    HttpHeaders headers = new HttpHeaders();


    @Test
    public void SDNCNotifyTest() throws IOException {
        headers.set("Content-Type", "application/xml");
        String content = FileUtil.readResourceFile("RestCallback.xml");
        HttpEntity<String> entity = new HttpEntity<String>(content,headers);
        ResponseEntity<String> response = restTemplate.exchange("http://localhost:" + port +
                "/adapters/rest/SDNCNotify", HttpMethod.POST, entity, String.class);

        assertNotNull(response);
    }
}