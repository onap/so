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

package org.onap.aaiclient.client.aai;

import java.io.IOException;
import java.util.Optional;
import jakarta.annotation.Priority;
import jakarta.ws.rs.ext.Provider;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.client.ResponseExceptionMapper;
import org.onap.aaiclient.client.aai.entities.AAIError;
import org.slf4j.MDC;
import com.fasterxml.jackson.databind.ObjectMapper;

@Provider
@Priority(Integer.MIN_VALUE)
public class AAIClientResponseExceptionMapper extends ResponseExceptionMapper {

    private final String requestId;

    public AAIClientResponseExceptionMapper() {
        this.requestId = MDC.get(ONAPLogConstants.MDCs.REQUEST_ID);
    }

    @Override
    public Optional<String> extractMessage(String entity) {

        String errorString = "Error calling A&AI. Request-Id=" + this.getRequestId() + " ";
        try {
            AAIError error = new ObjectMapper().readValue(entity, AAIError.class);
            AAIErrorFormatter formatter = new AAIErrorFormatter(error);
            return Optional.of(errorString + formatter.getMessage());
        } catch (IOException e) {
            return Optional.of(errorString + entity);
        }
    }

    protected String getRequestId() {
        return this.requestId;
    }
}
