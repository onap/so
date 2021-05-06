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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import com.google.common.collect.ImmutableSet;

/**
 * @author Sheel Bajpai (sheel.bajpai@orange.com)
 *
 */

public class HttpHeaderForwarderHandlerInterceptor extends HandlerInterceptorAdapter {

    private static final ThreadLocal<Map<String, List<String>>> HEADERS_THREAD_LOCAL = new ThreadLocal<>();

    private static final Set<String> FORWARDED_HEADER_NAMES = ImmutableSet.of("authorization", "x-request-id",
            "x-b3-traceid", "x-b3-spanid", "x-b3-parentspanid", "x-b3-sampled", "x-b3-flags", "x-ot-span-context");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        Map<String, List<String>> headerMap = Collections.list(request.getHeaderNames()).stream()
                .map(String::toLowerCase).filter(FORWARDED_HEADER_NAMES::contains)
                .collect(Collectors.toMap(Function.identity(), h -> Collections.list(request.getHeaders(h))));

        HEADERS_THREAD_LOCAL.set(headerMap);
        return super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        HEADERS_THREAD_LOCAL.remove();
    }

    static Map<String, List<String>> getHeaders() {
        return HEADERS_THREAD_LOCAL.get();
    }

}
