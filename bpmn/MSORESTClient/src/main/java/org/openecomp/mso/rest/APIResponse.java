/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

package org.openecomp.mso.rest;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
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
    private final BasicHeader[] headers;

    /**
     * Internal method used to create http headers using the specified
     * HttpResponse object.
     *
     * @param httpResponse used to create headers
     * @return http headers
     */
    private BasicHeader[] buildHeaders(final HttpResponse httpResponse) {
        final Header[] headers = httpResponse.getAllHeaders();

        BasicHeader[] BasicHeaders = new BasicHeader[headers.length];
        for (int i = 0; i < headers.length; ++i) {
            final Header header = headers[i];
            final String name = header.getName();
            final String value = header.getValue(); 
            final BasicHeader BasicHeader = new BasicHeader(name, value);
            BasicHeaders[i] = BasicHeader;
        } 

        return BasicHeaders;
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
        return this.responseBody;
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
     * @return an array of all the BasicHeaders 
     */
    public BasicHeader[] getAllHeaders() {
        // avoid exposing internals, create copy
        BasicHeader[] copy = new BasicHeader[this.headers.length];
        for (int i = 0; i < this.headers.length; ++i) {
            copy[i] = headers[i];
        }
        return copy;
    }

    public String getFirstHeader(String name) {
        for (BasicHeader header : headers) {
            if (header.getName().equals(name)) {
                return header.getValue();
            }
        }
        return null;
    }
}
