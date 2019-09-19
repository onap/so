/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client;

import java.util.ArrayList;
import java.util.List;
import org.onap.logging.filter.spring.SpringClientPayloadFilter;
import org.onap.so.logging.jaxrs.filter.SOSpringClientFilter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class BaseClient<I, O> {

    private HttpHeaders httpHeader;
    private String targetUrl;

    public HttpHeaders getHttpHeader() {
        return httpHeader;
    }

    public HttpHeaders setDefaultHttpHeaders(String auth) {
        httpHeader = new HttpHeaders();
        httpHeader.set("Authorization", auth);
        httpHeader.setContentType(MediaType.APPLICATION_JSON);
        List<MediaType> acceptMediaTypes = new ArrayList<MediaType>();
        acceptMediaTypes.add(MediaType.APPLICATION_JSON);
        acceptMediaTypes.add(MediaType.TEXT_PLAIN);
        httpHeader.setAccept(acceptMediaTypes);
        return httpHeader;
    }

    public void setHttpHeader(HttpHeaders httpHeader) {
        this.httpHeader = httpHeader;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public O get(I data, ParameterizedTypeReference<O> typeRef, Object... uriVariables) throws RestClientException {
        return run(data, HttpMethod.GET, typeRef, uriVariables);
    }

    public O post(I data, ParameterizedTypeReference<O> typeRef, Object... uriVariables) throws RestClientException {
        return run(data, HttpMethod.POST, typeRef, uriVariables);
    }

    public O run(I data, HttpMethod method, ParameterizedTypeReference<O> typeRef, Object... uriVariables)
            throws RestClientException {
        HttpEntity<I> requestEntity = new HttpEntity<I>(data, getHttpHeader());
        RestTemplate restTemplate = new RestTemplate();
        restTemplate
                .setRequestFactory(new BufferingClientHttpRequestFactory(new HttpComponentsClientHttpRequestFactory()));
        restTemplate.getInterceptors().add(new SOSpringClientFilter());
        restTemplate.getInterceptors().add((new SpringClientPayloadFilter()));
        ResponseEntity<O> responseEntity =
                restTemplate.exchange(getTargetUrl(), method, requestEntity, typeRef, uriVariables);
        return responseEntity.getBody();
    }

}
