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

package org.onap.so.adapters.sdncrest;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlElement;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request Information specified by the SDNC "agnostic" API.
 */
public class RequestInformation implements Serializable {
    private static final long serialVersionUID = 1L;

    // Identifies the transaction MSO has with the calling system.
    private String requestId;

    // Identifies the calling system, e.g. CCD.
    private String source;

    // The calling system's endpoint for receiving notifications from MSO.
    private String notificationUrl;

    // NOTE: these are defined in the SDNC AID, but not used by MSO:
    // request-action
    // request-sub-action

    // Identifies the request action
    private String requestAction;

    // Identifies the request sub action
    private String requestSubAction;

    @JsonProperty("orderNumber")
    @XmlElement(name = "orderNumber")
    private String orderNumber;

    @JsonProperty("orderVersion")
    @XmlElement(name = "orderVersion")
    private String orderVersion;

    public RequestInformation(String requestId, String source, String notificationUrl) {
        this.requestId = requestId;
        this.source = source;
        this.notificationUrl = notificationUrl;
    }

    public RequestInformation() {}

    @JsonProperty("requestId")
    @XmlElement(name = "requestId")
    public String getRequestId() {
        return requestId;
    }

    @JsonProperty("requestId")
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @JsonProperty("source")
    @XmlElement(name = "source")
    public String getSource() {
        return source;
    }

    @JsonProperty("source")
    public void setSource(String source) {
        this.source = source;
    }

    @JsonProperty("notificationUrl")
    @XmlElement(name = "notificationUrl")
    public String getNotificationUrl() {
        return notificationUrl;
    }

    @JsonProperty("notificationUrl")
    public void setNotificationUrl(String notificationUrl) {
        this.notificationUrl = notificationUrl;
    }

    @JsonProperty("requestAction")
    @XmlElement(name = "requestAction")
    public String getRequestAction() {
        return requestAction;
    }

    @JsonProperty("requestAction")
    public void setRequestAction(String requestAction) {
        this.requestAction = requestAction;
    }

    @JsonProperty("requestSubAction")
    @XmlElement(name = "requestSubAction")
    public String getRequestSubAction() {
        return requestSubAction;
    }

    @JsonProperty("requestSubAction")
    public void setRequestSubAction(String requestSubAction) {
        this.requestSubAction = requestSubAction;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getOrderVersion() {
        return orderVersion;
    }

    public void setOrderVersion(String orderVersion) {
        this.orderVersion = orderVersion;
    }


}
