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
 * A custom exception class. 
 *
 * @version 1.0
 *
 */
public class RESTException extends Exception {
    private static final long serialVersionUID = -6874042744590915838L;
    // http status code
    private final int statusCode;

    // error message
    private final String errorMessage;
    
    /**
     * {@inheritDoc}
     * @see Exception#RESTException(String)
     */
    public RESTException(final String errorMessage) {
        this(-1, errorMessage);
    }

    /**
     * {@inheritDoc}
     * @see Exception#RESTException(Throwable)
     */
    public RESTException(final Throwable cause) {
        super(cause);
        this.statusCode = -1;
        this.errorMessage = cause.getMessage();
    }

    /**
     * Creates a RESTException with the specified status code and error 
     * message.
     *
     * @param statusCode http status code
     * @param errorMessage http error message
     */
    public RESTException(final int statusCode, final String errorMessage) {
        super(statusCode + ":" + errorMessage);
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
    }

    /**
     * Gets the status code or -1 if none has been set.
     *
     * @return status code
     */
    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     * Gets the error message.
     *
     * @return error message
     */
    public String getErrorMessage() {
        return this.errorMessage; 
    }
}
