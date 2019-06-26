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

package org.onap.so.serviceinstancebeans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(Include.NON_DEFAULT)
public class RequestStatus {

    @JsonProperty("requestState")
    protected String requestState;
    @JsonProperty("statusMessage")
    protected String statusMessage;
    @JsonProperty("percentProgress")
    protected Integer percentProgress;
    @JsonProperty("timestamp")
    protected String timeStamp;
    @JsonProperty("extSystemErrorSource")
    protected String extSystemErrorSource;
    @JsonProperty("rollbackExtSystemErrorSource")
    protected String rollbackExtSystemErrorSource;

    public String getExtSystemErrorSource() {
        return extSystemErrorSource;
    }

    public void setExtSystemErrorSource(String extSystemErrorSource) {
        this.extSystemErrorSource = extSystemErrorSource;
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

    public Integer getPercentProgress() {
        return percentProgress;
    }

    public void setPercentProgress(Integer percentProgress) {
        this.percentProgress = percentProgress;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getRollbackExtSystemErrorSource() {
        return rollbackExtSystemErrorSource;
    }

    public void setRollbackExtSystemErrorSource(String rollbackExtSystemErrorSource) {
        this.rollbackExtSystemErrorSource = rollbackExtSystemErrorSource;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("requestState", requestState).append("statusMessage", statusMessage)
                .append("percentProgress", percentProgress).append("timestamp", timeStamp)
                .append("extSystemErrorSource", extSystemErrorSource)
                .append("rollbackExtSystemErrorSource", rollbackExtSystemErrorSource).toString();
    }
}
