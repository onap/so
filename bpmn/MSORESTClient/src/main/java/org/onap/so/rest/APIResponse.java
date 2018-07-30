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

package org.onap.so.rest;

import java.io.IOException;
import java.util.Arrays;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

/**
 * An immutable class that encapsulates an API response.
 * 
 * @version 1.0
 * @since 1.0
 */
public class APIResponse {
    private final int statusCode;
    private final byte[] responseBody;
    private final HttpHeader[] headers;

    /**
     * Internal method used to create http headers using the specified
     * HttpResponse object.
     *
     * @param httpResponse used to create headers
     * @return http headers
     */
    private static HttpHeader[] buildHeaders(final HttpResponse httpResponse) {
        final Header[] headers = httpResponse.getAllHeaders();

        HttpHeader[] httpHeaders = new HttpHeader[headers.length];
        for (int i = 0; i < headers.length; ++i) {
            final Header header = headers[i];
            final String name = header.getName();
            final String value = header.getValue(); 
            final HttpHeader httpHeader = new HttpHeader(name, value);
            httpHeaders[i] = httpHeader;
        } 

        return httpHeaders;
    }

    /**
     * Create an APIResponse object using the specified HttpResponse object.
     *
     * @param httpResponse used to create the APIResponse
     *
     * @throws RESTException if unable to read from the HttpResponse object
     */
    public APIResponse(final HttpResponse httpResponse) throws RESTException {
        try {
            this.statusCode = httpResponse.getStatusLine().getStatusCode();

            if (httpResponse.getEntity() == null)
            {
                this.responseBody = null;
            }
            else
            {
                this.responseBody = EntityUtils.toByteArray(httpResponse.getEntity());
            }

            this.headers = buildHeaders(httpResponse);
        } catch (IOException ioe) {
            throw new RESTException(ioe);
        }
    }

    /**
     * Gets the http status code returned by the api server.
     * <p>
     * For example, status code 200 represents 'OK.' 
     *
     * @return status code
     */
    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     * Gets the http response body as a byte array.
     *
     * @return http response body
     */
    public byte[] getResponseBodyAsByteArray() {
        // avoid exposing internals, create copy
        if (this.responseBody != null) {
            return Arrays.copyOf(this.responseBody, this.responseBody.length);
        } else {
            return null;
        }
    }

    /**
     * Gets the http response body as a string.
     *
     * @return http response body
     */
    public String getResponseBodyAsString() {
        if (this.responseBody != null) {
            return new String(this.responseBody);
        } else {
            return "";
        }
    }

    /**
     * Gets a list of all the headers returned by the API response.
     *
     * @return an array of all the HttpHeaders 
     */
    public HttpHeader[] getAllHeaders() {
        // avoid exposing internals, create copy
        return Arrays.copyOf(this.headers, this.headers.length);
    }

    public String getFirstHeader(String name) {
        for (HttpHeader header : headers) {
            if (header.getName().equals(name)) {
                return header.getValue();
            }
        }
        return null;
    }
}
