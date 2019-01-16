/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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
 * 
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.monitoring.model;

import java.sql.Timestamp;

/**
 * @author waqas.ikram@ericsson.com
 */
public class SoInfraRequestBuilder {

    private String requestId;
    private String serviceInstanceId;
    private String serviceIstanceName;
    private String networkId;
    private String requestStatus;
    private String serviceType;
    private String startTime;
    private String endTime;

    public SoInfraRequestBuilder setRequestId(final String requestId) {
        this.requestId = requestId;
        return this;
    }

    public SoInfraRequestBuilder setServiceInstanceId(final String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
        return this;
    }

    public SoInfraRequestBuilder setServiceIstanceName(final String serviceIstanceName) {
        this.serviceIstanceName = serviceIstanceName;
        return this;
    }

    public SoInfraRequestBuilder setNetworkId(final String networkId) {
        this.networkId = networkId;
        return this;
    }

    public SoInfraRequestBuilder setRequestStatus(final String requestStatus) {
        this.requestStatus = requestStatus;
        return this;
    }

    public SoInfraRequestBuilder setServiceType(final String serviceType) {
        this.serviceType = serviceType;
        return this;
    }

    public SoInfraRequestBuilder setEndTime(final String endTime) {
        this.endTime = endTime;
        return this;
    }

    public SoInfraRequestBuilder setEndTime(final Timestamp endTime) {
        this.endTime = endTime != null ? endTime.toString() : null;
        return this;
    }


    public SoInfraRequestBuilder setStartTime(final String startTime) {
        this.startTime = startTime;
        return this;
    }

    public SoInfraRequestBuilder setStartTime(final Timestamp startTime) {
        this.startTime = startTime != null ? startTime.toString() : null;
        return this;
    }

    public SoInfraRequest build() {
        return new SoInfraRequest(this);
    }

    /**
     * @return the requestId
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * @return the serviceInstanceId
     */
    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    /**
     * @return the serviceIstanceName
     */
    public String getServiceIstanceName() {
        return serviceIstanceName;
    }

    /**
     * @return the networkId
     */
    public String getNetworkId() {
        return networkId;
    }

    /**
     * @return the requestStatus
     */
    public String getRequestStatus() {
        return requestStatus;
    }

    /**
     * @return the serviceType
     */
    public String getServiceType() {
        return serviceType;
    }

    /**
     * @return the startTime
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * @return the endTime
     */
    public String getEndTime() {
        return endTime;
    }
}
