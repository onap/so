/***
 * Copyright (C) 2019 Verizon. All Rights Reserved Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.onap.so.adapters.vfc.model;

import org.onap.so.adapters.vfc.constant.CommonConstant;

public class NsLcmOpOcc {
    private String id;
    private CommonConstant.operationState operationState;
    private String statusEnteredTime;
    private String nsInstanceId;
    private CommonConstant.lcmOperationType lcmOperationType;
    private String startTime;
    private Boolean isAutomaticInvocation;
    private String operationParams;
    private Boolean isCancelPending;
    private CommonConstant.cancelMode cancelMode;
    private ProblemDetails error;
    private Links links;

    public CommonConstant.lcmOperationType getLcmOperationType() {
        return lcmOperationType;
    }

    public void setLcmOperationType(CommonConstant.lcmOperationType lcmOperationType) {
        this.lcmOperationType = lcmOperationType;
    }

    public CommonConstant.cancelMode getCancelMode() {
        return cancelMode;
    }

    public void setCancelMode(CommonConstant.cancelMode cancelMode) {
        this.cancelMode = cancelMode;
    }

    public CommonConstant.operationState getOperationState() {
        return operationState;
    }

    public void setOperationState(CommonConstant.operationState operationState) {
        this.operationState = operationState;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatusEnteredTime() {
        return statusEnteredTime;
    }

    public void setStatusEnteredTime(String statusEnteredTime) {
        this.statusEnteredTime = statusEnteredTime;
    }

    public String getNsInstanceId() {
        return nsInstanceId;
    }

    public void setNsInstanceId(String nsInstanceId) {
        this.nsInstanceId = nsInstanceId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public Boolean getAutomaticInvocation() {
        return isAutomaticInvocation;
    }

    public void setAutomaticInvocation(Boolean automaticInvocation) {
        isAutomaticInvocation = automaticInvocation;
    }

    public String getOperationParams() {
        return operationParams;
    }

    public void setOperationParams(String operationParams) {
        this.operationParams = operationParams;
    }

    public Boolean getCancelPending() {
        return isCancelPending;
    }

    public void setCancelPending(Boolean cancelPending) {
        isCancelPending = cancelPending;
    }

    public ProblemDetails getError() {
        return error;
    }

    public void setError(ProblemDetails error) {
        this.error = error;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }
}
