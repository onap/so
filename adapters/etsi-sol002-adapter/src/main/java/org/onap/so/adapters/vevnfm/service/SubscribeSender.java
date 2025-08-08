/*-
 * ============LICENSE_START=======================================================
 * SO
 * ================================================================================
 * Copyright (C) 2020 Samsung. All rights reserved.
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

package org.onap.so.adapters.vevnfm.service;

import org.onap.aai.domain.yang.EsrSystemInfo;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm.model.LccnSubscriptionRequest;
import org.onap.so.adapters.vevnfm.configuration.ConfigProperties;
import org.onap.so.adapters.vevnfm.exception.VeVnfmException;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;

@Service
public class SubscribeSender {

    public static final String SLASH = "/";

    private static final Logger logger = LoggerFactory.getLogger(SubscribeSender.class);

    private final String vnfmSubscription;
    private final HttpRestServiceProvider restProvider;

    @Autowired
    public SubscribeSender(final ConfigProperties configProperties, final HttpRestServiceProvider restProvider) {
        this.vnfmSubscription = configProperties.getVnfmSubscription();
        this.restProvider = restProvider;
    }

    public String send(final EsrSystemInfo info, final LccnSubscriptionRequest request) {
        final ResponseEntity<SubscribeToManoResponse> response =
                restProvider.postHttpRequest(request, getUrl(info), SubscribeToManoResponse.class);

        final HttpStatus statusCode = response.getStatusCode();
        final SubscribeToManoResponse body = response.getBody();

        logger.info("The VNFM replied with the code {} and the body {}", statusCode, body);

        if (HttpStatus.CREATED != statusCode) {
            throw new VeVnfmException("The status code was different than " + HttpStatus.CREATED);
        }

        return body.id;
    }

    public boolean check(final EsrSystemInfo info, final String id) {
        final ResponseEntity<SubscribeToManoResponse> response =
                restProvider.getHttpResponse(getUrl(info) + SLASH + id, SubscribeToManoResponse.class);
        return response.getBody() != null && response.getBody().id.equals(id);
    }

    private String getUrl(final EsrSystemInfo info) {
        return info.getServiceUrl() + vnfmSubscription;
    }

    @ToString
    static class SubscribeToManoResponse {
        @JsonProperty("id")
        String id;
        @JsonProperty("callbackUri")
        String callbackUri;
    }
}
