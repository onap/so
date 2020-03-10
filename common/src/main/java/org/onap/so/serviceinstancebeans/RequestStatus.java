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
    @JsonProperty("resourceStatusMessage")
    protected String resourceStatusMessage;
    @JsonProperty("percentProgress")
    protected Integer percentProgress;
    @JsonProperty("timestamp")
    protected String timeStamp;
    @JsonProperty("extSystemErrorSource")
    protected String extSystemErrorSource;
    @JsonProperty("rollbackExtSystemErrorSource")
    protected String rollbackExtSystemErrorSource;
    @JsonProperty("flowStatus")
    protected String flowStatus;
    @JsonProperty("retryStatusMessage")
    protected String retryStatusMessage;
    @JsonProperty("rollbackStatusMessage")
    protected String rollbackStatusMessage;

    public String getFlowStatus() {
        return flowStatus;
    }

    public void setFlowStatus(String flowStatus) {
        this.flowStatus = flowStatus;
    }

    public String getRetryStatusMessage() {
        return retryStatusMessage;
    }

    public void setRetryStatusMessage(String retryStatusMessage) {
        this.retryStatusMessage = retryStatusMessage;
    }

    public String getRollbackStatusMessage() {
        return rollbackStatusMessage;
    }

    public void setRollbackStatusMessage(String rollbackStatusMessage) {
        this.rollbackStatusMessage = rollbackStatusMessage;
    }

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

    public String getResourceStatusMessage() {
        return resourceStatusMessage;
    }

    public void setResourceStatusMessage(String resourceStatusMessage) {
        this.resourceStatusMessage = resourceStatusMessage;
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
                .append("resourceStatusMessage", resourceStatusMessage).append("percentProgress", percentProgress)
                .append("timestamp", timeStamp).append("extSystemErrorSource", extSystemErrorSource)
                .append("rollbackExtSystemErrorSource", rollbackExtSystemErrorSource).append("flowStatus", flowStatus)
                .append("retryStatusMessage", retryStatusMessage).append("rollbackStatusMessage", rollbackStatusMessage)
                .toString();
    }
}
