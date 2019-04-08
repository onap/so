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

public class DuplicateRequestException extends ApiException {

    private static final String duplicateFailMessage = "Error: Locked instance - This %s (%s) "
            + "already has a request being worked with a status of %s (RequestId - %s). The existing request must finish or be cleaned up before proceeding.";

    private DuplicateRequestException(Builder builder) {
        super(builder);
    }


    public static class Builder extends ApiException.Builder<Builder> {


        public Builder(String requestScope, String instance, String requestStatus, String requestID,
                int httpResponseCode, String messageID) {
            super(String.format(duplicateFailMessage, requestScope, instance, requestStatus, requestID),
                    httpResponseCode, messageID);

        }


        public DuplicateRequestException build() {

            return new DuplicateRequestException(this);
        }
    }

}
