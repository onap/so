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

public class RequestDbFailureException extends ApiException {

    private static final String REQUEST_DB_FAIL_MESSAGE = "Unable to %s due to error contacting requestDb: %s";

    private RequestDbFailureException(Builder builder) {
        super(builder);
    }

    public static class Builder extends ApiException.Builder<Builder> {


        public Builder(String action, String error, int httpResponseCode, String messageID) {
            super(String.format(REQUEST_DB_FAIL_MESSAGE, action, error), httpResponseCode, messageID);
        }

        public RequestDbFailureException build() {
            return new RequestDbFailureException(this);
        }
    }

}
