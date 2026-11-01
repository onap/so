package org.onap.so.adapters.sdnc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import org.junit.Test;
import org.onap.so.adapters.sdnc.impl.SDNCRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class SdncHttpRoutingTest extends BaseTest {
    @LocalServerPort
    private int port;

    @MockBean
    private SDNCRestClient worker;

    @Autowired
    private ObjectMapper mapper;

    private final String body = "{\"requestId\":\"operation-1\",\"serviceInstanceId\":\"service-1\","
            + "\"svcAction\":\"assign\",\"svcOperation\":\"vnf-topology-operation\","
            + "\"callbackUrl\":\"http://localhost/callback\","
            + "\"requestDataXml\":\"<RequestData><value>test</value></RequestData>\"}";

    @Test
    public void authenticatedUnwrappedJsonReachesMvcAfterCxfInitialization() {
        assertTrue(mapper.isEnabled(DeserializationFeature.UNWRAP_ROOT_VALUE));

        ResponseEntity<String> response =
                restTemplate.postForEntity(url("/adapters/rest/v2/sdnc/requests"), request(body), String.class);

        assertEquals(response.getBody(), HttpStatus.ACCEPTED, response.getStatusCode());
        verify(worker).executeRestRequest(any());
        verify(worker, never()).executeRequest(any());
        assertTrue(mapper.isEnabled(DeserializationFeature.UNWRAP_ROOT_VALUE));
    }

    @Test
    public void anonymousRequestCannotDispatchTheWorker() {
        ResponseEntity<String> response = new TestRestTemplate().postForEntity(url("/adapters/rest/v2/sdnc/requests"),
                request(body), String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(worker, never()).executeRestRequest(any());
    }

    @Test
    public void existingSoapWsdlRemainsReachable() {
        ResponseEntity<String> response = restTemplate.getForEntity(url("/adapters/SDNCAdapter?wsdl"), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("SDNCAdapterService"));
    }

    private HttpEntity<String> request(String content) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return new HttpEntity<>(content, headers);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
