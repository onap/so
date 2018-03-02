/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.cloudify.connector.http;

import java.net.URI;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.annotation.Immutable;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;

/**
 * Custom {@link org.apache.http.client.RedirectStrategy} implementation
 * that automatically redirects all HEAD, GET and DELETE requests.
 * The {@link org.apache.http.client.DefaultRedirectStrategy} only
 * redirects GET and HEAD automatically, per the HTTP specification
 * (POST and PUT typically have bodies and thus cannot be redirected).
 * 
 * A custom strategy is needed for the Openstack API, which can also send
 * 302 on a DELETE (by name) request, expecting the client to follow the
 * redirect to perform the actual deletion. 
 */
@Immutable
public class HttpClientRedirectStrategy extends DefaultRedirectStrategy {

    /**
     * Redirectable methods.
     */
    private static final String[] REDIRECT_METHODS = new String[] {
        HttpGet.METHOD_NAME,
        HttpDelete.METHOD_NAME,
        HttpHead.METHOD_NAME
    };

    /**
     * Determine if the request should be redirected.
     * This may not actually be needed, since the REDIRECT_METHODS
     * array has been updated with the DELETE.
     */
    @Override
    protected boolean isRedirectable(final String method) {
        for (final String m: REDIRECT_METHODS) {
            if (m.equalsIgnoreCase(method)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Override the default redirect handling method.  As implemented
     * in HttpClient, it does not preserve the method on 301 or 302
     * responses, always redirecting to a GET.
     */
    @Override
    public HttpUriRequest getRedirect(
            final HttpRequest request,
            final HttpResponse response,
            final HttpContext context) throws ProtocolException {
    	
        final URI uri = getLocationURI(request, response, context);
        final String method = request.getRequestLine().getMethod();
        if (method.equalsIgnoreCase(HttpHead.METHOD_NAME)) {
            return new HttpHead(uri);
        } else if (method.equalsIgnoreCase(HttpGet.METHOD_NAME)) {
            return new HttpGet(uri);
        } else {

            final int status = response.getStatusLine().getStatusCode();
            
        	HttpUriRequest newRequest = null;
        	if (status == HttpStatus.SC_TEMPORARY_REDIRECT || status == HttpStatus.SC_MOVED_TEMPORARILY) {
                newRequest = RequestBuilder.copy(request).setUri(uri).build();
            } else {
                newRequest =  new HttpGet(uri);
            }
        	return newRequest;
        }
    }
}
