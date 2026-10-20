package org.onap.so.client.orchestration;

import org.onap.logging.filter.spring.SpringClientPayloadFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import io.micrometer.observation.ObservationRegistry;

@Configuration
public class RestTemplateApiClientConfig {
    public static final String REST_TEMPLATE_API_HANDLER = "restTemplateApiHandler";

    @Bean(REST_TEMPLATE_API_HANDLER)
    public RestTemplate restTemplate(ObjectProvider<ObservationRegistry> observationRegistry) {
        final RestTemplate restTemplate = new RestTemplate();
        restTemplate.setObservationRegistry(observationRegistry.getIfAvailable(() -> ObservationRegistry.NOOP));
        restTemplate
                .setRequestFactory(new BufferingClientHttpRequestFactory(new HttpComponentsClientHttpRequestFactory()));
        restTemplate.getInterceptors().add((new SpringClientPayloadFilter()));
        return restTemplate;
    }

}
