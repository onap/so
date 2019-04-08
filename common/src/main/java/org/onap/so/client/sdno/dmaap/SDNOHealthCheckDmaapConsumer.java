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

package org.onap.so.client.sdno.dmaap;

import java.io.IOException;
import java.util.Optional;
import org.onap.so.client.dmaap.DmaapConsumer;
import org.onap.so.client.exceptions.SDNOException;
import org.onap.so.jsonpath.JsonPathUtil;

public class SDNOHealthCheckDmaapConsumer extends DmaapConsumer {

    private final String uuid;
    private boolean continuePolling = true;
    private final static String healthDiagnosticPath = "body.output.*";

    public SDNOHealthCheckDmaapConsumer() throws IOException {
        this("none");
    }

    public SDNOHealthCheckDmaapConsumer(String uuid) throws IOException {
        super();
        this.uuid = uuid;
    }

    @Override
    public String getAuth() {
        return msoProperties.get("sdno.health-check.dmaap.auth");
    }

    @Override
    public String getKey() {
        return msoProperties.get("mso.msoKey");
    }

    @Override
    public String getTopic() {
        return msoProperties.get("sdno.health-check.dmaap.subscriber.topic");
    }

    @Override
    public Optional<String> getHost() {
        return Optional.ofNullable(msoProperties.get("sdno.health-check.dmaap.subscriber.host"));
    }

    @Override
    public boolean continuePolling() {
        return continuePolling;
    }

    @Override
    public void stopProcessingMessages() {
        continuePolling = false;
    }

    @Override
    public void processMessage(String message) throws Exception {
        if (isHealthDiagnostic(message, this.getRequestId())) {
            if (!healthDiagnosticSuccessful(message)) {
                Optional<String> statusMessage = this.getStatusMessage(message);
                if (statusMessage.isPresent()) {
                    throw new SDNOException("failed with message " + statusMessage.get());
                } else {
                    throw new SDNOException("failed with no status message");
                }
            } else {
                logger.info("successful health diagnostic found for request: {}", getRequestId());
                stopProcessingMessages();
            }
        }
    }

    @Override
    public boolean isAccepted(String message) {
        if (isResultInfo(message)) {
            Optional<String> code = isAccepted(message, this.getRequestId());
            if (code.isPresent()) {
                if ("202".equals(code.get())) {
                    return true;
                } else {
                    // TODO check other statuses 400 and 500
                }
            } else {
                // TODO throw error
            }
        }

        return false;
    }

    @Override
    public boolean isFailure(String message) {
        if (isResultInfo(message)) {
            Optional<String> code = isFailure(message, this.getRequestId());
            if (code.isPresent()) {
                if ("500".equals(code.get())) {
                    return true;
                } else {
                    // TODO check other statuses 400 and 500
                }
            } else {
                // TODO throw error
            }
        }

        return false;
    }

    @Override
    public String getRequestId() {
        return uuid;
    }

    protected Optional<String> isAccepted(String json, String uuid) {
        return JsonPathUtil.getInstance().locateResult(json,
                String.format("$.result-info[?(@.status=='ACCEPTED' && @.request-id=='%s')].code", uuid));
    }

    protected Optional<String> isFailure(String json, String uuid) {
        return JsonPathUtil.getInstance().locateResult(json,
                String.format("$.result-info[?(@.status=='FAILURE' && @.request-id=='%s')].code", uuid));
    }

    protected boolean isResultInfo(String json) {
        return JsonPathUtil.getInstance().pathExists(json, "$[?(@.result-info)]");
    }

    protected boolean isHealthDiagnostic(String json, String uuid) {
        return JsonPathUtil.getInstance().pathExists(json,
                String.format("$[?(@.result-info.request-id=='%s')].%s", uuid, healthDiagnosticPath));
    }

    protected boolean healthDiagnosticSuccessful(String json) {
        return JsonPathUtil.getInstance().pathExists(json,
                "$." + healthDiagnosticPath + "[?(@.response-status=='Success')]");
    }

    protected Optional<String> getStatusMessage(String json) {
        return JsonPathUtil.getInstance().locateResult(json, "$." + healthDiagnosticPath + ".error-message");
    }

    @Override
    public int getMaximumElapsedTime() {
        return 600000;
    }
}
