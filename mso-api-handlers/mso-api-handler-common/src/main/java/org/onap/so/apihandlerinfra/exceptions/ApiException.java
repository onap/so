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

package org.onap.so.apihandlerinfra.exceptions;


import java.util.List;
import org.onap.so.apihandlerinfra.logging.ErrorLoggerInfo;
import org.springframework.http.HttpStatus;

public abstract class ApiException extends Exception {
    /**
    * 
    */
    private static final long serialVersionUID = 683162058616691134L;
    private int httpResponseCode = 500;
    private String messageID;
    private ErrorLoggerInfo errorLoggerInfo;
    private HttpStatus originalHttpResponseCode;
    private List<String> variables;

    public ApiException(Builder builder) {
        super(builder.message, builder.cause);
        this.httpResponseCode = builder.httpResponseCode;
        this.messageID = builder.messageID;
        this.variables = builder.variables;
        this.errorLoggerInfo = builder.errorLoggerInfo;
        this.originalHttpResponseCode = builder.originalHttpResponseCode;
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApiException(String message, int httpResponseCode) {
        super(message);
        this.httpResponseCode = httpResponseCode;
    }

    public ApiException(String message, int httpResponseCode, HttpStatus original) {
        super(message);
        this.httpResponseCode = httpResponseCode;
        this.originalHttpResponseCode = original;
    }

    public ApiException(String message) {
        super(message);
    }

    public String getMessageID() {
        return messageID;
    }

    public int getHttpResponseCode() {
        return httpResponseCode;
    }

    public ErrorLoggerInfo getErrorLoggerInfo() {
        return errorLoggerInfo;
    }


    public List<String> getVariables() {
        return variables;
    }

    public HttpStatus getOriginalHttpResponseCode() {
        return originalHttpResponseCode;
    }

    public static class Builder<T extends Builder<T>> {
        private String message;
        private Throwable cause = null;
        private int httpResponseCode;
        private String messageID;
        private HttpStatus originalHttpResponseCode;
        private ErrorLoggerInfo errorLoggerInfo = null;
        private List<String> variables = null;

        public Builder(String message, int httpResponseCode, String messageID) {
            this.message = message;
            this.httpResponseCode = httpResponseCode;
            this.messageID = messageID;
        }

        public Builder(String message, int httpResponseCode, String messageID, HttpStatus originalHttpResponseCode) {
            this.message = message;
            this.httpResponseCode = httpResponseCode;
            this.messageID = messageID;
            this.originalHttpResponseCode(originalHttpResponseCode);
        }

        // @SuppressWarnings("unchecked")
        public T message(String message) {
            this.message = message;
            return (T) this;
        }

        // @SuppressWarnings("unchecked")
        public T cause(Throwable cause) {
            this.cause = cause;
            return (T) this;
        }

        // @SuppressWarnings("unchecked")
        public T httpResponseCode(int httpResponseCode) {
            this.httpResponseCode = httpResponseCode;
            return (T) this;
        }

        // @SuppressWarnings("unchecked")
        public T messageID(String messageID) {
            this.messageID = messageID;
            return (T) this;
        }

        // @SuppressWarnings("unchecked")
        public T errorInfo(ErrorLoggerInfo errorLoggerInfo) {
            this.errorLoggerInfo = errorLoggerInfo;
            return (T) this;
        }

        // @SuppressWarnings("unchecked")
        public T variables(List<String> variables) {
            this.variables = variables;
            return (T) this;
        }

        // @SuppressWarnings("unchecked")
        public T originalHttpResponseCode(HttpStatus originalHttpResponseCode) {
            this.originalHttpResponseCode = originalHttpResponseCode;
            return (T) this;
        }
    }
}
