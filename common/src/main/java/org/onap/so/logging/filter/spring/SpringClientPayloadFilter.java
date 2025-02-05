/*-
 * ============LICENSE_START=======================================================
 * ONAP - Logging
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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


package org.onap.so.logging.filter.spring;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

public class SpringClientPayloadFilter implements ClientHttpRequestInterceptor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        logRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        logResponse(response);
        return response;
    }

    protected void logRequest(HttpRequest request, byte[] body) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("===========================request begin================================================");
            logger.debug("URI         : {}", request.getURI());
            logger.debug("Method      : {}", request.getMethod());
            logger.debug("Headers     : {}", request.getHeaders());
            logger.debug("Request body: {}", new String(body, StandardCharsets.UTF_8));
            logger.debug("==========================request end================================================");
        }
    }

    protected void logResponse(ClientHttpResponse response) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("============================response begin==========================================");
            logger.debug("Status code  : {}", response.getStatusCode());
            logger.debug("Status text  : {}", response.getStatusText());
            logger.debug("Headers      : {}", response.getHeaders());
            logger.debug("Response body: {}", StreamUtils.copyToString(response.getBody(), Charset.defaultCharset()));
            logger.debug("=======================response end=================================================");
        }
    }
}
