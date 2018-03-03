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

package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity;

/**
 * Created by 10112215 on 2017/9/20.
 */
public class RequestInformationEntity {
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestAction() {
        return requestAction;
    }

    public void setRequestAction(String requestAction) {
        this.requestAction = requestAction;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getNotificationUrl() {
        return notificationUrl;
    }

    public void setNotificationUrl(String notificationUrl) {
        this.notificationUrl = notificationUrl;
    }

    public String getOrderUnmber() {
        return orderUnmber;
    }

    public void setOrderUnmber(String orderUnmber) {
        this.orderUnmber = orderUnmber;
    }

    public String getOrerVersion() {
        return orerVersion;
    }

    public void setOrerVersion(String orerVersion) {
        this.orerVersion = orerVersion;
    }

    @JsonProperty("GENERIC-RESOURCE-API:request-id")
    private String requestId;

    @JsonProperty("GENERIC-RESOURCE-API:request-action")
    private String requestAction;

    @JsonProperty("GENERIC-RESOURCE-API:source")
    private String source;

    @JsonProperty("GENERIC-RESOURCE-API:notification-url")
    private String notificationUrl;

    @JsonProperty("GENERIC-RESOURCE-API:order-number")
    private String orderUnmber;

    @JsonProperty("GENERIC-RESOURCE-API:order-version")
    private String orerVersion;
}
