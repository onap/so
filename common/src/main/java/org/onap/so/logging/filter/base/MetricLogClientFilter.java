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

import jakarta.annotation.Priority;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Providers;

@Priority(0)
public class MetricLogClientFilter
        extends AbstractMetricLogFilter<ClientRequestContext, ClientResponseContext, MultivaluedMap<String, Object>>
        implements ClientRequestFilter, ClientResponseFilter {

    @Context
    private Providers providers;

    @Override
    public void filter(ClientRequestContext clientRequest) {
        pre(clientRequest, clientRequest.getHeaders());
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) {
        post(requestContext, responseContext);
    }

    @Override
    protected void addHeader(MultivaluedMap<String, Object> requestHeaders, String headerName, String headerValue) {
        requestHeaders.add(headerName, headerValue);
    }

    @Override
    protected String getTargetServiceName(ClientRequestContext request) {
        return request.getUri().toString();
    }

    @Override
    protected int getHttpStatusCode(ClientResponseContext response) {
        return response.getStatus();
    }

    @Override
    protected String getResponseCode(ClientResponseContext response) {
        return String.valueOf(response.getStatus());
    }

    @Override
    protected String getTargetEntity(ClientRequestContext request) {
        return Constants.DefaultValues.UNKNOWN_TARGET_ENTITY;
    }

}
