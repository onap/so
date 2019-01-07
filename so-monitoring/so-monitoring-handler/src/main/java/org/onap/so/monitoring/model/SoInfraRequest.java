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

import static org.onap.so.monitoring.utils.ObjectEqualsUtils.isEqual;

/**
 * @author waqas.ikram@ericsson.com
 */
public class SoInfraRequest {

    private final String requestId;
    private final String serviceInstanceId;
    private final String serviceIstanceName;
    private final String networkId;
    private final String requestStatus;
    private final String serviceType;
    private final String startTime;
    private final String endTime;

    public SoInfraRequest(final SoInfraRequestBuilder requestBuilder) {
        this.requestId = requestBuilder.getRequestId();
        this.serviceInstanceId = requestBuilder.getServiceInstanceId();
        this.serviceIstanceName = requestBuilder.getServiceIstanceName();
        this.networkId = requestBuilder.getNetworkId();
        this.requestStatus = requestBuilder.getRequestStatus();
        this.serviceType = requestBuilder.getServiceType();
        this.startTime = requestBuilder.getStartTime();
        this.endTime = requestBuilder.getEndTime();
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
        result = prime * result + ((networkId == null) ? 0 : networkId.hashCode());
        result = prime * result + ((requestId == null) ? 0 : requestId.hashCode());
        result = prime * result + ((requestStatus == null) ? 0 : requestStatus.hashCode());
        result = prime * result + ((serviceInstanceId == null) ? 0 : serviceInstanceId.hashCode());
        result = prime * result + ((serviceIstanceName == null) ? 0 : serviceIstanceName.hashCode());
        result = prime * result + ((serviceType == null) ? 0 : serviceType.hashCode());
        result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof SoInfraRequest) {
            final SoInfraRequest other = (SoInfraRequest) obj;
            return isEqual(requestId, other.requestId) && isEqual(serviceInstanceId, other.serviceInstanceId)
                    && isEqual(serviceIstanceName, other.serviceIstanceName) && isEqual(networkId, other.networkId)
                    && isEqual(requestStatus, other.requestStatus) && isEqual(serviceType, other.serviceType)
                    && isEqual(startTime, other.startTime) && isEqual(endTime, other.endTime);
        }
        return false;
    }

}
