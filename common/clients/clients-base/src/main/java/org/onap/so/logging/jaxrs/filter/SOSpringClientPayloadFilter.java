/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2024 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.logging.jaxrs.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.onap.logging.filter.spring.SpringClientPayloadFilter;
import org.onap.so.logger.LogMasking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Spring 6 compatible override of SpringClientPayloadFilter that also redacts credential headers and masks request
 * bodies before they are logged. Replaces logging-analytics' SpringClientPayloadFilter. The base class was compiled
 * against Spring 5 where ClientHttpResponse.getStatusCode() returned HttpStatus; in Spring 6 it returns HttpStatusCode.
 */
public class SOSpringClientPayloadFilter extends SpringClientPayloadFilter {

    private static final Set<String> SENSITIVE_HEADERS =
            Set.of("authorization", "proxy-authorization", "cookie", "set-cookie", "x-auth-token", "x-subject-token");

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void logRequest(HttpRequest request, byte[] body) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("===========================request begin=============================================");
            logger.debug("URI          : {} {}", request.getMethod(), request.getURI());
            logger.debug("Headers      : {}", redact(request.getHeaders()));
            logger.debug("Request body : {}", LogMasking.mask(new String(body, StandardCharsets.UTF_8)));
            logger.debug("===========================request end===============================================");
        }
    }

    @Override
    protected void logResponse(ClientHttpResponse response) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("============================response begin==========================================");
            logger.debug("Status code  : {}", response.getStatusCode());
            logger.debug("Status text  : {}", response.getStatusText());
            logger.debug("Headers      : {}", redact(response.getHeaders()));
            logger.debug("============================response end============================================");
        }
    }

    static HttpHeaders redact(HttpHeaders headers) {
        HttpHeaders copy = new HttpHeaders();
        headers.forEach((name, values) -> copy.put(name,
                SENSITIVE_HEADERS.contains(name.toLowerCase(Locale.ROOT)) ? List.of(LogMasking.REDACTED) : values));
        return copy;
    }
}
