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
package org.onap.so.monitoring.camunda.model;

import static org.onap.so.monitoring.utils.ObjectEqualsUtils.isEqual;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author waqas.ikram@ericsson.com
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SoActiveInfraRequests {

    private String requestId;
    private String serviceInstanceId;
    private String networkId;
    private String startTime;
    private String endTime;
    private String requestStatus;
    private String serviceInstanceName;
    private String serviceType;

    /**
     * @return the requestId
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * @param requestId the requestId to set
     */
    public void setRequestId(final String requestId) {
        this.requestId = requestId;
    }

    /**
     * @return the serviceInstanceId
     */
    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    /**
     * @param serviceInstanceId the serviceInstanceId to set
     */
    public void setServiceInstanceId(final String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    /**
     * @return the networkId
     */
    public String getNetworkId() {
        return networkId;
    }

    /**
     * @param networkId the networkId to set
     */
    public void setNetworkId(final String networkId) {
        this.networkId = networkId;
    }

    /**
     * @return the startTime
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(final String startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the endTime
     */
    public String getEndTime() {
        return endTime;
    }

    /**
     * @param endTime the endTime to set
     */
    public void setEndTime(final String endTime) {
        this.endTime = endTime;
    }

    /**
     * @return the requestStatus
     */
    public String getRequestStatus() {
        return requestStatus;
    }

    /**
     * @param requestStatus the requestStatus to set
     */
    public void setRequestStatus(final String requestStatus) {
        this.requestStatus = requestStatus;
    }

    /**
     * @return the serviceInstanceName
     */
    public String getServiceInstanceName() {
        return serviceInstanceName;
    }

    /**
     * @param serviceInstanceName the serviceInstanceName to set
     */
    public void setServiceInstanceName(final String serviceInstanceName) {
        this.serviceInstanceName = serviceInstanceName;
    }

    /**
     * @return the serviceType
     */
    public String getServiceType() {
        return serviceType;
    }

    /**
     * @param serviceType the serviceType to set
     */
    public void setServiceType(final String serviceType) {
        this.serviceType = serviceType;
    }

    @JsonIgnore
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
        result = prime * result + ((networkId == null) ? 0 : networkId.hashCode());
        result = prime * result + ((requestId == null) ? 0 : requestId.hashCode());
        result = prime * result + ((requestStatus == null) ? 0 : requestStatus.hashCode());
        result = prime * result + ((serviceInstanceId == null) ? 0 : serviceInstanceId.hashCode());
        result = prime * result + ((serviceInstanceName == null) ? 0 : serviceInstanceName.hashCode());
        result = prime * result + ((serviceType == null) ? 0 : serviceType.hashCode());
        result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
        return result;
    }

    @JsonIgnore
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof SoActiveInfraRequests) {
            SoActiveInfraRequests other = (SoActiveInfraRequests) obj;
            return isEqual(requestId, other.requestId) && isEqual(serviceInstanceId, other.serviceInstanceId)
                    && isEqual(networkId, other.networkId) && isEqual(startTime, other.startTime)
                    && isEqual(endTime, other.endTime) && isEqual(requestStatus, other.requestStatus)
                    && isEqual(serviceInstanceName, other.serviceInstanceName)
                    && isEqual(serviceType, other.serviceType);
        }

        return false;
    }

    @JsonIgnore
    @Override
    public String toString() {
        return "SoActiveInfraRequests [requestId=" + requestId + ", serviceInstanceId=" + serviceInstanceId
                + ", networkId=" + networkId + ", startTime=" + startTime + ", endTime=" + endTime + ", requestStatus="
                + requestStatus + ", serviceInstanceName=" + serviceInstanceName + ", serviceType=" + serviceType + "]";
    }



}
