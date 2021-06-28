/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Orange. All rights reserved.
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
package org.onap.so.security;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * @author Sheel Bajpai (sheel.bajpai@orange.com)
 *
 */

public class HttpHeaderForwarderRequestInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {

        Map<String, List<String>> headerMap = HttpHeaderForwarderHandlerInterceptor.getHeaders();
        if (headerMap != null && !headerMap.isEmpty())
            request.getHeaders().putAll(HttpHeaderForwarderHandlerInterceptor.getHeaders());
        return execution.execute(request, body);
    }

}
