package org.onap.aaiclient.client;

import java.net.URI;
import org.onap.aai.domain.yang.GraphNode;
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.api.AAIListResultWrapper;
import org.onap.aaiclient.client.api.AAIResultWrapper;
import org.onap.aaiclient.client.api.ResourcesClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AAIResourcesClient implements ResourcesClient {

    private final RestTemplate restTemplate;

    @Override
    public <T extends GraphNode> AAIResultWrapper<T> get(AAIResourceUri uri, Class<T> responseType) {
        var res = restTemplate.getForEntity(uri.build().toString(), responseType).getBody();
        return new AAIResultWrapper<T>(res);
    }

    @Override
    public <T> AAIListResultWrapper<T> get(AAIPluralResourceUri uri, Class<T> responseType) {
        var res = restTemplate.getForEntity(uri.build().toString(), responseType).getBody();
        return new AAIListResultWrapper<T>(res);
    }
}
