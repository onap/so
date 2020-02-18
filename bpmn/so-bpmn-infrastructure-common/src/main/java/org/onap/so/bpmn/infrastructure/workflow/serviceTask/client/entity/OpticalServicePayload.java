/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Fujitsu Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OpticalServicePayload {

    @JsonProperty("opticalservice:service-layer")
    private String serviceLayer;

    @JsonProperty("opticalservice:service-a-end")
    private OpticalServiceEnd serviceAEnd;

    @JsonProperty("opticalservice:service-protocol")
    private String protocol;

    @JsonProperty("opticalservice:domain-type")
    private String domainType;

    @JsonProperty("opticalservice:service-rate")
    private String serviceRate;

    @JsonProperty("opticalservice:service-z-end")
    private OpticalServiceEnd serviceZEnd;

    @JsonProperty("opticalservice:due-date")
    private String dueDate;

    @JsonProperty("opticalservice:service-name")
    private String serviceName;

    @JsonProperty("opticalservice:coding-func")
    private String codingFunc;

    public String getServiceLayer() {
        return serviceLayer;
    }

    public void setServiceLayer(String serviceLayer) {
        this.serviceLayer = serviceLayer;
    }

    public OpticalServiceEnd getServiceAEnd() {
        return serviceAEnd;
    }

    public void setServiceAEnd(OpticalServiceEnd serviceAEnd) {
        this.serviceAEnd = serviceAEnd;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getDomainType() {
        return domainType;
    }

    public void setDomainType(String domainType) {
        this.domainType = domainType;
    }

    public String getServiceRate() {
        return serviceRate;
    }

    public void setServiceRate(String serviceRate) {
        this.serviceRate = serviceRate;
    }

    public OpticalServiceEnd getServiceZEnd() {
        return serviceZEnd;
    }

    public void setServiceZEnd(OpticalServiceEnd serviceZEnd) {
        this.serviceZEnd = serviceZEnd;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getCodingFunc() {
        return codingFunc;
    }

    public void setCodingFunc(String codingFunc) {
        this.codingFunc = codingFunc;
    }

}
