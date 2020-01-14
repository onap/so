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

package org.onap.so.adapters.vevnfm.subscription;

import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.LccnSubscriptionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SubscribeSender {

    private static final Logger logger = LoggerFactory.getLogger(SubscribeSender.class);

    @Value("${vnfm.host}")
    private String vnfmHost;

    @Value("${vnfm.subscription}")
    private String vnfmSubscription;

    @Autowired
    @Qualifier("subscriberRestTemplate")
    private RestTemplate restTemplate;

    public boolean send(final LccnSubscriptionRequest request) {
        final ResponseEntity<String> response = restTemplate.exchange(getUrl(),
                HttpMethod.POST, getHeaders(request), String.class);

        final HttpStatus statusCode = response.getStatusCode();
        final String body = response.getBody();

        logger.info("The VNFM replayed with the code {} and the body {}", statusCode, body);

        return HttpStatus.CREATED == statusCode;
    }

    private String getUrl() {
        return vnfmHost + vnfmSubscription;
    }

    private HttpEntity<LccnSubscriptionRequest> getHeaders(final LccnSubscriptionRequest request) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(request, headers);
    }
}
