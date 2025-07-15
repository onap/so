package org.onap.aaiclient.client.configuration;

import java.net.MalformedURLException;
import org.onap.aaiclient.client.AAIResourcesClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableConfigurationProperties(AAIConfigurationProperties.class)
public class AAIClientAutoConfiguration {

    private static final String RESTTEMPLATE_QUALIFIER = "aaiClientRestTemplate";

    @Bean(name = RESTTEMPLATE_QUALIFIER)
    RestTemplate aaiClientRestTemplate(RestTemplateBuilder builder, AAIConfigurationProperties properties) {
        String rootUri = null; // null is also the default value inside
                               // the builder when rootUri is not specified
        try {
            rootUri = properties.getEndpoint().toString();
        } catch (MalformedURLException e) {
            log.warn("aai.endpoint does not have a valid uri", e);
        }
        return builder.rootUri(rootUri).build();
    }

    @Bean
    AAIResourcesClient aaiResourcesClient(@Qualifier(RESTTEMPLATE_QUALIFIER) RestTemplate restTemplate) {
        return new AAIResourcesClient(restTemplate);
    }

}
