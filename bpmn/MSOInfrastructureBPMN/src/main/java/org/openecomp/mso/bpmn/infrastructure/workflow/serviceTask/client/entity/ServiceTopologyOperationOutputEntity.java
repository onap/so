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

import com.fasterxml.jackson.annotation.JsonProperty;

public class ServiceTopologyOperationOutputEntity {
    @JsonProperty("GENERIC-RESOURCE-API:svc-request-id")
    private String svcRequestId;

    @JsonProperty("GENERIC-RESOURCE-API:response-code")
    private String responseCode;

    @JsonProperty("GENERIC-RESOURCE-API:response-message")
    private String responseMessage;

    @JsonProperty("GENERIC-RESOURCE-API:ack-final-indicator")
    private String ackFinalIndicator;

    @JsonProperty("GENERIC-RESOURCE-API:service-response-information")
    private ServiceResponseInformationEntity serviceResponseInformation;

    public String getSvcRequestId() {
        return svcRequestId;
    }

    public void setSvcRequestId(String svcRequestId) {
        this.svcRequestId = svcRequestId;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getAckFinalIndicator() {
        return ackFinalIndicator;
    }

    public void setAckFinalIndicator(String ackFinalIndicator) {
        this.ackFinalIndicator = ackFinalIndicator;
    }

    public ServiceResponseInformationEntity getServiceResponseInformation() {
        return serviceResponseInformation;
    }

    public void setServiceResponseInformation(ServiceResponseInformationEntity serviceResponseInformation) {
        this.serviceResponseInformation = serviceResponseInformation;
    }
}
