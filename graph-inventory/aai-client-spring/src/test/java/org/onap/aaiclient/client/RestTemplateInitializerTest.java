package org.onap.aaiclient.client;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

public class RestTemplateInitializerTest {

    @Test
    @Disabled("Test not yet working")
    public void thatRestTemplateCanBeRetrieved() {
        RestTemplate restTemplate = RestTemplateInitializer.create();
        assertNotNull(restTemplate);

        // there is no direct way to assert the configured base url of the template
        // here we are provoking an exception that contains the base url in it's
        // message. Obviously this has to match the aai.endpoint that is defined
        // in the aai.properties file
        Exception exception = assertThrows(ResourceAccessException.class, () -> {
            restTemplate.getForEntity("", String.class);
        });

        assertTrue(exception.getMessage().contains("http://localhost:8443"));
    }
}
