package org.onap.so.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.net.URI;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import lombok.SneakyThrows;


@ActiveProfiles("basic")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class AuthenticationTest {

    private static final String USERNAME = "test-user";
    private static final String PASSWORD = "test-password";
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("logging.level.org.springframework.security", () -> "DEBUG");
        registry.add("spring.security.usercredentials[0].username", () -> USERNAME);
        registry.add("spring.security.usercredentials[0].role", () -> "test-role");
        registry.add("spring.security.usercredentials[0].password", () -> encoder.encode(PASSWORD));
    }

    @LocalServerPort
    int port;

    @Test
    @SneakyThrows
    public void thatEndpointsAreAuthenticated() {
        String baseUrl = "http://localhost:" + port;
        TestRestTemplate restTemplate = new TestRestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-ECOMP-RequestID", UUID.randomUUID().toString());
        headers.set("X-ECOMP-InstanceID", "test");
        headers.setBasicAuth(USERNAME, PASSWORD);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange(new URI(baseUrl + "/"), HttpMethod.GET, entity, String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @SneakyThrows
    public void thatUnauthorizedRequestFails() {
        String baseUrl = "http://localhost:" + port;
        TestRestTemplate restTemplate = new TestRestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-ECOMP-RequestID", UUID.randomUUID().toString());
        headers.set("X-ECOMP-InstanceID", "test");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange(new URI(baseUrl + "/"), HttpMethod.GET, entity, String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @SneakyThrows
    public void thatManageIsAccessible() {
        String baseUrl = "http://localhost:" + port;
        TestRestTemplate restTemplate = new TestRestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-ECOMP-RequestID", UUID.randomUUID().toString());
        headers.set("X-ECOMP-InstanceID", "test");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange(new URI(baseUrl + "/manage/health"), HttpMethod.GET, entity, String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
