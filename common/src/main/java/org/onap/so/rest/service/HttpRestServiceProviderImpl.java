/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.so.rest.service;

import org.onap.so.configuration.rest.HttpHeadersProvider;
import org.onap.so.rest.exceptions.HttpResouceNotFoundException;
import org.onap.so.rest.exceptions.InvalidRestRequestException;
import org.onap.so.rest.exceptions.RestProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.google.common.base.Optional;

/**
 * A Service to perform HTTP requests
 *
 * @author waqas.ikram@est.tech
 */
public class HttpRestServiceProviderImpl implements HttpRestServiceProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRestServiceProviderImpl.class);
    private final RestTemplate restTemplate;
    private final HttpHeaders defaultHttpHeaders;

    public HttpRestServiceProviderImpl(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.defaultHttpHeaders = new HttpHeaders();
    }

    public HttpRestServiceProviderImpl(final RestTemplate restTemplate, final HttpHeaders defaultHttpHeaders) {
        this.restTemplate = restTemplate;
        this.defaultHttpHeaders = defaultHttpHeaders;
    }

    /**
     * 
     * @deprecated this constructor is deprecated in favor of using {@link HttpRestServiceProviderImpl(RestTemplate
     *             restTemplate, HttpHeaders defaultHttpHeaders)}
     */
    @Deprecated
    public HttpRestServiceProviderImpl(final RestTemplate restTemplate, final HttpHeadersProvider httpHeadersProvider) {
        this.restTemplate = restTemplate;
        this.defaultHttpHeaders = httpHeadersProvider.getHttpHeaders();
    }

    @Override
    public <T> Optional<T> get(final String url, final Class<T> clazz) {
        final ResponseEntity<T> response = getHttpResponse(url, clazz);
        return createOptional(response, url, HttpMethod.GET);
    }

    @Override
    public <T> Optional<T> get(final String url, final HttpHeaders headers, final Class<T> clazz) {
        final ResponseEntity<T> response = invokeHttpRequest(new HttpEntity<>(headers), HttpMethod.GET, url, clazz);
        return createOptional(response, url, HttpMethod.GET);
    }

    @Override
    public <T> ResponseEntity<T> getHttpResponse(final String url, final Class<T> clazz) {
        final HttpEntity<?> request = new HttpEntity<>(getDefaultHttpHeaders());
        return invokeHttpRequest(request, HttpMethod.GET, url, clazz);
    }

    @Override
    public <T> ResponseEntity<T> getHttpResponse(final String url, final HttpHeaders headers, final Class<T> clazz) {
        final HttpEntity<?> request = new HttpEntity<>(headers);
        return invokeHttpRequest(request, HttpMethod.GET, url, clazz);
    }

    @Override
    public <T> Optional<T> post(final Object object, final String url, final Class<T> clazz) {
        final ResponseEntity<T> response = postHttpRequest(object, url, clazz);
        return createOptional(response, url, HttpMethod.POST);
    }

    @Override
    public <T> ResponseEntity<T> postHttpRequest(final Object object, final String url, final Class<T> clazz) {
        final HttpEntity<?> request = new HttpEntity<>(object, getDefaultHttpHeaders());
        return invokeHttpRequest(request, HttpMethod.POST, url, clazz);
    }

    @Override
    public <T> ResponseEntity<T> postHttpRequest(final Object object, final String url, final HttpHeaders headers,
            final Class<T> clazz) {
        final HttpEntity<?> request = new HttpEntity<>(object, headers);
        return invokeHttpRequest(request, HttpMethod.POST, url, clazz);
    }

    @Override
    public <T> Optional<T> put(final Object object, final String url, final Class<T> clazz) {
        final ResponseEntity<T> response = putHttpRequest(object, url, clazz);
        return createOptional(response, url, HttpMethod.PUT);
    }

    @Override
    public <T> ResponseEntity<T> putHttpRequest(final Object object, final String url, final Class<T> clazz) {
        final HttpEntity<?> request = new HttpEntity<>(object, getDefaultHttpHeaders());
        return invokeHttpRequest(request, HttpMethod.PUT, url, clazz);
    }

    private <T> Optional<T> createOptional(final ResponseEntity<T> response, final String url,
            final HttpMethod httpMethod) {
        if (!response.getStatusCode().equals(HttpStatus.OK) && !response.getStatusCode().equals(HttpStatus.CREATED)) {
            final String message = "Unable to invoke HTTP " + httpMethod + " using URL: " + url + ", Response Code: "
                    + response.getStatusCode();
            LOGGER.error(message);
            return Optional.absent();
        }

        if (response.hasBody()) {
            return Optional.of(response.getBody());
        }

        return Optional.absent();
    }

    private <T> ResponseEntity<T> invokeHttpRequest(final HttpEntity<?> request, final HttpMethod httpMethod,
            final String url, final Class<T> clazz) {
        LOGGER.trace("Will invoke HTTP {} using URL: {}", httpMethod, url);
        try {
            return restTemplate.exchange(url, httpMethod, request, clazz);

        } catch (final HttpStatusCodeException httpStatusCodeException) {
            LOGGER.error("Unable to invoke HTTP " + httpMethod + " using url: " + url + ", Response: "
                    + httpStatusCodeException.getStatusCode().value());
            final int rawStatusCode = httpStatusCodeException.getStatusCode().value();
            if (rawStatusCode == HttpStatus.BAD_REQUEST.value()) {
                throw new InvalidRestRequestException("No result found for given url: " + url);
            } else if (rawStatusCode == HttpStatus.NOT_FOUND.value()) {
                throw new HttpResouceNotFoundException("No result found for given url: " + url);
            }
            throw new RestProcessingException("Unable to invoke HTTP " + httpMethod + " using URL: " + url,
                    httpStatusCodeException, rawStatusCode);

        } catch (final RestClientException restClientException) {
            LOGGER.error("Unable to invoke HTTP POST using url: {}", url, restClientException);
            throw new RestProcessingException("Unable to invoke HTTP " + httpMethod + " using URL: " + url,
                    restClientException);
        }
    }

    @Override
    public <T> ResponseEntity<T> deleteHttpRequest(final String url, final Class<T> clazz) {
        try {
            final HttpEntity<?> request = new HttpEntity<>(getDefaultHttpHeaders());
            return restTemplate.exchange(url, HttpMethod.DELETE, request, clazz);

        } catch (final HttpStatusCodeException httpStatusCodeException) {
            final String message = "Unable to invoke HTTP " + HttpMethod.DELETE + " using url: " + url + ", Response: "
                    + httpStatusCodeException.getStatusCode().value();
            LOGGER.error(message, httpStatusCodeException);
            final int rawStatusCode = httpStatusCodeException.getStatusCode().value();
            if (rawStatusCode == HttpStatus.BAD_REQUEST.value()) {
                throw new InvalidRestRequestException("No result found for given url: " + url);
            } else if (rawStatusCode == HttpStatus.NOT_FOUND.value()) {
                throw new HttpResouceNotFoundException("No result found for given url: " + url);
            }
            throw new RestProcessingException("Unable to invoke HTTP " + HttpMethod.DELETE + " using URL: " + url,
                    httpStatusCodeException, rawStatusCode);
        } catch (final RestClientException restClientException) {
            LOGGER.error("Unable to invoke HTTP DELETE using url: " + url, restClientException);
            throw new InvalidRestRequestException("Unable to invoke HTTP DELETE using URL: " + url,
                    restClientException);
        }
    }

    private HttpHeaders getDefaultHttpHeaders() {
        return defaultHttpHeaders;
    }
}
