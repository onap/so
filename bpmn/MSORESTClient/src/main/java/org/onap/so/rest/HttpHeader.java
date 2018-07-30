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

/**
 * An immutable class used to wrap an http header.
 *
 * @version 1.0
 * @since 1.0
 */
public class HttpHeader {
    private final String name;
    private final String value;

    /**
     * Create an http header using the specified name and value
     *
     * @param name name of http header
     * @param value value of http header
     */
    public HttpHeader(final String name, final String value) {
        if (name == null) {
            throw new IllegalArgumentException("Name may not be null.");
        }

        this.name = name;
        this.value = value;
    }

    /**
     * Gets the header name.
     * 
     * @return header name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the header value.
     * 
     * @return header value 
     */
    public String getValue() {
        return this.value;
    }
}
