package org.onap.so.adapters.sdnc.sdncrest;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.adapters.sdnc.impl.Constants;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RunWith(MockitoJUnitRunner.class)
public class BPRestCallbackUnitTest {
    @Mock
    private Environment env;

    @Mock
    private RestTemplate restTemplate;

    @Spy
    @InjectMocks
    private BPRestCallback bpRestCallback;

    private HttpEntity<String> requestEntity;
    private String message;
    private HttpHeaders headers;
    private URI uri;

    @Before
    public void setUp() throws IOException {
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        message = input("BPRestCallbackRequest.json");
        requestEntity = new HttpEntity<>(message, headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("http://localhost:8000/sdnc");
        uri = builder.build(true).toUri();
    }

    public String input(String JsonInput) throws IOException {
        JsonInput = "src/test/resources/" + JsonInput;
        return new String(Files.readAllBytes(Paths.get(JsonInput)));
    }

    @Test
    public void sendTest() {
        ResponseEntity<String> postResponse = new ResponseEntity<String>("response", HttpStatus.OK);
        doReturn(restTemplate).when(bpRestCallback).setRestTemplate(60000);
        doReturn(false).when(bpRestCallback).setAuthorizationHeader(headers);
        when(restTemplate.postForEntity(uri, requestEntity, String.class)).thenReturn(postResponse);
        boolean response = bpRestCallback.send("http://localhost:8000/sdnc", message);
        assertTrue(response);
    }

    @Test
    public void sendNoAuthHeaderTest() {
        doReturn(true).when(bpRestCallback).setAuthorizationHeader(headers);
        doReturn(restTemplate).when(bpRestCallback).setRestTemplate(60000);
        boolean response = bpRestCallback.send("http://localhost:8000/sdnc", message);
        assertTrue(response);
    }

    @Test
    public void sendErrorTest() {
        doReturn(false).when(bpRestCallback).setAuthorizationHeader(headers);
        doReturn(restTemplate).when(bpRestCallback).setRestTemplate(60000);
        when(restTemplate.postForEntity(uri, requestEntity, String.class))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, null, null, null));
        boolean response = bpRestCallback.send("http://localhost:8000/sdnc", message);
        assertTrue(response);
    }

    @Test
    public void sendResponse3xxTest() {
        ResponseEntity<String> postResponse = new ResponseEntity<String>("response", HttpStatus.MULTIPLE_CHOICES);
        doReturn(false).when(bpRestCallback).setAuthorizationHeader(headers);
        doReturn(restTemplate).when(bpRestCallback).setRestTemplate(60000);
        when(restTemplate.postForEntity(uri, requestEntity, String.class)).thenReturn(postResponse);
        boolean response = bpRestCallback.send("http://localhost:8000/sdnc", message);
        assertTrue(response);
    }

    @Test
    public void sendResponseNullMessageTest() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntityNoMessage = new HttpEntity<>(null, httpHeaders);
        ResponseEntity<String> postResponse = new ResponseEntity<String>("response", HttpStatus.OK);
        doReturn(false).when(bpRestCallback).setAuthorizationHeader(httpHeaders);
        doReturn(restTemplate).when(bpRestCallback).setRestTemplate(60000);
        when(restTemplate.postForEntity(uri, requestEntityNoMessage, String.class)).thenReturn(postResponse);
        boolean response = bpRestCallback.send("http://localhost:8000/sdnc", null);
        assertTrue(response);
    }

    @Test
    public void postThrowsExceptionTest() {
        doReturn(false).when(bpRestCallback).setAuthorizationHeader(headers);
        doReturn(restTemplate).when(bpRestCallback).setRestTemplate(60000);
        when(restTemplate.postForEntity(uri, requestEntity, String.class))
                .thenThrow(new ResourceAccessException("ResourceAccessException"));
        boolean response = bpRestCallback.send("http://localhost:8000/sdnc", message);
        assertFalse(response);
    }

    @Test
    public void setAuthorizationHeaderTest() {
        HttpHeaders authHeaders = new HttpHeaders();
        when(env.getProperty(Constants.BPEL_AUTH_PROP))
                .thenReturn("5E12ACACBD552A415E081E29F2C4772F9835792A51C766CCFDD7433DB5220B59969CB2798C");
        when(env.getProperty(Constants.ENCRYPTION_KEY_PROP)).thenReturn("07a7159d3bf51a0e53be7a8f89699be7");
        boolean result = bpRestCallback.setAuthorizationHeader(authHeaders);
        assertFalse(result);
    }

    @Test
    public void setAuthorizationHeaderErrorTest() {
        HttpHeaders authHeaders = new HttpHeaders();
        when(env.getProperty(Constants.BPEL_AUTH_PROP)).thenReturn("test");
        when(env.getProperty(Constants.ENCRYPTION_KEY_PROP)).thenReturn("test");
        boolean result = bpRestCallback.setAuthorizationHeader(authHeaders);
        assertTrue(result);
    }
}
