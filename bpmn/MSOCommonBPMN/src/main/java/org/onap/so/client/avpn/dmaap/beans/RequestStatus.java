/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.avpn.dmaap.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "requestStatus")
public class RequestStatus {

    @JsonProperty("timestamp")
    private String timestamp;
    @JsonProperty("requestState")
    private String requestState;
    @JsonProperty("statusMessage")
    private String statusMessage;
    @JsonProperty("percentProgress")
    private String percentProgress;
    @JsonProperty("wasRolledBack")
    private Boolean wasRolledBack;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getRequestState() {
        return requestState;
    }

    public void setRequestState(String requestState) {
        this.requestState = requestState;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getPercentProgress() {
        return percentProgress;
    }

    public void setPercentProgress(String percentProgress) {
        this.percentProgress = percentProgress;
    }

    public Boolean isWasRolledBack() {
        return wasRolledBack;
    }

    public void setWasRolledBack(Boolean wasRolledBack) {
        this.wasRolledBack = wasRolledBack;
    }
}
