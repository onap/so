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

package org.onap.so.logging.filter.base;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.HttpHeaders;
import java.util.Enumeration;

public abstract class AbstractServletFilter {

    protected String getSecureRequestHeaders(HttpServletRequest httpRequest) {
        StringBuilder sb = new StringBuilder();
        String header;
        for (Enumeration<String> e = httpRequest.getHeaderNames(); e.hasMoreElements();) {
            header = e.nextElement();
            sb.append(header);
            sb.append(":");
            if (header.equalsIgnoreCase(HttpHeaders.AUTHORIZATION)) {
                sb.append(Constants.REDACTED);
            } else {
                sb.append(httpRequest.getHeader(header));
            }
            sb.append(";");
        }
        return sb.toString();
    }

    protected String formatResponseHeaders(HttpServletResponse response) {
        StringBuilder sb = new StringBuilder();
        for (String headerName : response.getHeaderNames()) {
            sb.append(headerName);
            sb.append(":");
            sb.append(response.getHeader(headerName));
            sb.append(";");
        }
        return sb.toString();
    }
}
