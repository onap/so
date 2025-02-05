/*-
 * ============LICENSE_START=======================================================
 * ONAP - Logging
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.so.logging.filter.base;

import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.MDC;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuditLogServletFilter extends AbstractAuditLogFilter<HttpServletRequest, HttpServletResponse>
        implements Filter {

    @Override
    public void destroy() {
        // this method does nothing
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain)
            throws IOException, ServletException {
        try {
            if (request != null && request instanceof HttpServletRequest) {
                pre((HttpServletRequest) request);
            }
            filterChain.doFilter(request, response);
        } finally {
            if (request != null && request instanceof HttpServletRequest) {
                post((HttpServletRequest) request, (HttpServletResponse) response);
            }
            MDC.clear();
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // this method does nothing
    }

    protected void pre(HttpServletRequest request) {
        SimpleMap headers = new SimpleServletHeadersMap(request);
        pre(headers, request, request);
    }

    @Override
    protected void setServiceName(HttpServletRequest request) {
        MDC.put(ONAPLogConstants.MDCs.SERVICE_NAME, request.getRequestURI());
    }

    private void post(HttpServletRequest request, HttpServletResponse response) {
        post(response);
    }

    @Override
    protected int getResponseCode(HttpServletResponse response) {
        return response.getStatus();
    }

}
