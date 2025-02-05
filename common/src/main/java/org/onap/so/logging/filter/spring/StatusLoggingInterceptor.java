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

import org.onap.so.logging.filter.base.AbstractServletFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Providers;

// @Primary
@Component("StatusLoggingInterceptorSO")
public class StatusLoggingInterceptor extends AbstractServletFilter implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(StatusLoggingInterceptor.class);

    @Context
    private Providers providers;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (logger.isDebugEnabled()) {
            logRequestInformation(request);
        }
        return true;
    }

    protected void logRequestInformation(HttpServletRequest request) {
        logger.debug("===========================request begin================================================");
        logger.debug("URI         : {}", request.getRequestURI());
        logger.debug("Method      : {}", request.getMethod());
        logger.debug("Headers     : {}", getSecureRequestHeaders(request));
        logger.debug("===========================request end==================================================");
    }

    // TODO previously no response information was being logged, I followed the format in SpringClientPayloadFilter
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("============================response begin==========================================");
            logger.debug("Status code  : {}", response.getStatus());
            logger.debug("Status text  : {}", Response.Status.fromStatusCode(response.getStatus()));
            logger.debug("Headers      : {}", formatResponseHeaders(response));
            logger.debug("============================response end============================================");
        }
    }
}
