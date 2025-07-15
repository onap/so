/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright © 2025 Deutsche Telekom AG Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aaiclient.spring;

import java.util.LinkedHashMap;
import org.onap.aai.domain.yang.GraphNode;
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.spring.api.AAIListResultWrapper;
import org.onap.aaiclient.spring.api.AAIResultWrapper;
import org.onap.aaiclient.spring.api.ResourcesClient;
import org.onap.aaiclient.spring.compat.RestTemplateInitializer;
import org.springframework.web.client.HttpClientErrorException.NotFound;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import lombok.RequiredArgsConstructor;

/**
 * New RestTemplate based implementation of the aai-client that supports tracing.
 */
@RequiredArgsConstructor
public class AAIResourcesClient implements ResourcesClient {

    private final RestTemplate restTemplate;

    // This is here for backwards compatibility towards the old client
    // Also in the Camunda flows, it's unlikely that dependency injection
    // can be used to inject the RestTemplate
    public AAIResourcesClient() {
        this.restTemplate = RestTemplateInitializer.create();
    }

    @Override
    public <T extends GraphNode> AAIResultWrapper<T> get(AAIResourceUri uri, Class<T> responseType) {
        var res = restTemplate.getForEntity(uri.build().toString(), responseType).getBody();
        return new AAIResultWrapper<T>(res, this);
    }

    @Override
    public <T> AAIListResultWrapper<T> get(AAIPluralResourceUri uri, Class<T> responseType) {
        var res = restTemplate.getForEntity(uri.build().toString(), responseType).getBody();
        return new AAIListResultWrapper<T>(res);
    }

    @Override
    public boolean exists(AAIResourceUri uri) {
        try {
            restTemplate.getForEntity(uri.build().toString(), LinkedHashMap.class);
            return true;
        } catch (NotFound e) {
            return false;
        }
    }

    @Override
    public void delete(AAIResourceUri uri) {
        ResponseEntity<LinkedHashMap<String, Object>> nodeResponse = restTemplate.exchange(uri.build().toString(),
                HttpMethod.GET, null, new ParameterizedTypeReference<LinkedHashMap<String, Object>>() {});
        String resourceVersion = (String) nodeResponse.getBody().get("resource-version");
        restTemplate.delete(uri.clone().resourceVersion(resourceVersion).build().toString());
    }

    @Override
    public <T extends GraphNode> void create(AAIResourceUri uri, T object) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.put(uri.build().toString(), new HttpEntity<T>(object, headers));
    }
}
