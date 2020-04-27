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

public class ClientConnectionException extends ApiException {

    /**
    * 
    */
    private static final long serialVersionUID = 1L;
    private static final String clientFailMessage = "Client from $URL failed to connect or respond";

    private ClientConnectionException(Builder builder) {
        super(builder);
    }

    public static class Builder extends ApiException.Builder<Builder> {

        public Builder(String message, int httpResponseCode, String messageID) {
            super(clientFailMessage.replaceAll("\\$URL", message), httpResponseCode, messageID);
        }

        public ClientConnectionException build() {
            return new ClientConnectionException(this);
        }
    }

}
