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

package org.openecomp.mso.client.sdno.dmaap;

import java.io.IOException;
import java.util.Optional;
import javax.ws.rs.NotSupportedException;
import org.openecomp.mso.client.dmaap.DmaapConsumer;
import org.openecomp.mso.jsonpath.JsonPathUtil;

public class PnfReadyEventConsumer extends DmaapConsumer {

    private static final String JSON_PATH_CORRELATION_ID = "$.pnfRegistrationFields.correlationId";

    private boolean continuePolling = true;
    private String correlationId;

    public PnfReadyEventConsumer(String correlationId) throws IOException {
        this.correlationId = correlationId;
    }

    @Override
    public boolean continuePolling() {
        return continuePolling;
    }

    @Override
    public void processMessage(String message) {
    }

    @Override
    public boolean isAccepted(String message) {
        Optional<String> correlationIdOpt = JsonPathUtil.getInstance().locateResult(message, JSON_PATH_CORRELATION_ID);
        if (correlationIdOpt.isPresent()) {
            continuePolling = false;
            return correlationIdOpt.get().equals(correlationId);
        }
        return false;
    }

    @Override
    public boolean isFailure(String message) {
        throw new NotSupportedException();
    }

    @Override
    public void stopProcessingMessages() {
        continuePolling = false;
    }

    @Override
    public String getRequestId() {
        throw new NotSupportedException();
    }

    @Override
    public String getUserName() {
        throw new NotSupportedException();
    }

    @Override
    public String getPassword() {
        throw new NotSupportedException();
    }

    @Override
    public String getTopic() {
        throw new NotSupportedException();
    }

    @Override
    public Optional<String> getHost() {
        throw new NotSupportedException();
    }
}
