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

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ServiceInstancesRequest implements Serializable {

    private static final long serialVersionUID = -4959169541182257787L;
    @JsonProperty("requestDetails")
    private RequestDetails requestDetails;
    @JsonProperty("serviceInstanceId")
    private String serviceInstanceId;
    @JsonProperty("vnfInstanceId")
    private String vnfInstanceId;
    @JsonProperty("pnfName")
    private String pnfName;
    @JsonProperty("networkInstanceId")
    private String networkInstanceId;
    @JsonProperty("volumeGroupInstanceId")
    private String volumeGroupInstanceId;
    @JsonProperty("vfModuleInstanceId")
    private String vfModuleInstanceId;
    @JsonProperty("configurationId")
    private String configurationId;
    @JsonProperty("instanceGroupId")
    private String instanceGroupId;

    public RequestDetails getRequestDetails() {
        return requestDetails;
    }

    public void setRequestDetails(RequestDetails requestDetails) {
        this.requestDetails = requestDetails;
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    public String getVnfInstanceId() {
        return vnfInstanceId;
    }

    public void setVnfInstanceId(String vnfInstanceId) {
        this.vnfInstanceId = vnfInstanceId;
    }

    public String getPnfName() {
        return pnfName;
    }

    public void setPnfName(String pnfName) {
        this.pnfName = pnfName;
    }

    public String getNetworkInstanceId() {
        return networkInstanceId;
    }

    public void setNetworkInstanceId(String networkInstanceId) {
        this.networkInstanceId = networkInstanceId;
    }

    public String getVolumeGroupInstanceId() {
        return volumeGroupInstanceId;
    }

    public void setVolumeGroupInstanceId(String volumeGroupInstanceId) {
        this.volumeGroupInstanceId = volumeGroupInstanceId;
    }

    public String getVfModuleInstanceId() {
        return vfModuleInstanceId;
    }

    public void setVfModuleInstanceId(String vfModuleInstanceId) {
        this.vfModuleInstanceId = vfModuleInstanceId;
    }

    public String getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(String configurationId) {
        this.configurationId = configurationId;
    }

    public String getInstanceGroupId() {
        return instanceGroupId;
    }

    public void setInstanceGroupId(String instanceGroupId) {
        this.instanceGroupId = instanceGroupId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ServiceInstancesRequest{");
        sb.append("requestDetails=").append(requestDetails);
        sb.append(", serviceInstanceId='").append(serviceInstanceId).append('\'');
        sb.append(", vnfInstanceId='").append(vnfInstanceId).append('\'');
        sb.append(", pnfName='").append(pnfName).append('\'');
        sb.append(", networkInstanceId='").append(networkInstanceId).append('\'');
        sb.append(", volumeGroupInstanceId='").append(volumeGroupInstanceId).append('\'');
        sb.append(", vfModuleInstanceId='").append(vfModuleInstanceId).append('\'');
        sb.append(", configurationId='").append(configurationId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
