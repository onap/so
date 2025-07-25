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

package org.onap.so.configuration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * @author waqas.ikram@est.tech
 */
public class BasicHttpHeadersProvider implements HttpHeadersProvider {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    private final HttpHeaders headers = new HttpHeaders();

    public BasicHttpHeadersProvider(final String authorization) {
        headers.add(AUTHORIZATION_HEADER, authorization);
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    public BasicHttpHeadersProvider() {
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @Override
    public HttpHeaders getHttpHeaders() {
        return headers;
    }

    @Override
    public String toString() {
        return "BasicHttpHeadersProvider [headers=" + headers + "]";
    }

}
