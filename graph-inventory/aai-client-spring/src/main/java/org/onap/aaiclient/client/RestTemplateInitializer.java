package org.onap.aaiclient.client;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import java.net.MalformedURLException;
import org.onap.aaiclient.client.aai.AAIProperties;
import org.onap.so.client.RestPropertiesLoader;

@Slf4j
public class RestTemplateInitializer {

    private static final RestTemplate restTemplate;

    static {
        AAIProperties aaiProperties = RestPropertiesLoader.getInstance().getNewImpl(AAIProperties.class);
        // AAIProperties aaiProperties = new AAIPropertiesImpl();
        restTemplate = new RestTemplate();
        try {
            restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(aaiProperties.getEndpoint().toString()));
        } catch (MalformedURLException e) {
            log.error("RestTemplate could not be initialized. The value of 'aai.endpoint' is not a valid url", e);
        }
    }

    public static RestTemplate create() {
        return restTemplate;
    }
}
