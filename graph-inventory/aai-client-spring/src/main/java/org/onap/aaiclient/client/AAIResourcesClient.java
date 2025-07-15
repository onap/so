package org.onap.aaiclient.client;

import java.net.URI;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AAIResourcesClient implements IResourcesClient {

    private final RestTemplate restTemplate;

    @Override
    public <T> AAIResultWrapper<T> get(String uri, Class<T> responseType) {
        // RestTemplate restTemplate = new RestTemplateBuilder()
        // .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        // .build();
        var res = restTemplate.getForEntity(uri, responseType).getBody();
        return new AAIResultWrapper<>(res, null);
    }
}
