/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

import org.apache.commons.lang3.builder.ToStringBuilder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_DEFAULT)
public class InstanceReferences {

    protected String serviceInstanceId;
    protected String serviceInstanceName;
    protected String vnfInstanceId;
    protected String vnfInstanceName;
    protected String vfModuleInstanceId;
    protected String vfModuleInstanceName;
    protected String volumeGroupInstanceId;
    protected String volumeGroupInstanceName;
    protected String networkInstanceId;
    protected String networkInstanceName;
    protected String requestorId;
    protected String instanceGroupId;
    protected String instanceGroupName;


    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    public String getServiceInstanceName() {
        return serviceInstanceName;
    }

    public void setServiceInstanceName(String serviceInstanceName) {
        this.serviceInstanceName = serviceInstanceName;
    }

    public String getVnfInstanceId() {
        return vnfInstanceId;
    }

    public void setVnfInstanceId(String vnfInstanceId) {
        this.vnfInstanceId = vnfInstanceId;
    }

    public String getVnfInstanceName() {
        return vnfInstanceName;
    }

    public void setVnfInstanceName(String vnfInstanceName) {
        this.vnfInstanceName = vnfInstanceName;
    }

    public String getVfModuleInstanceId() {
        return vfModuleInstanceId;
    }

    public void setVfModuleInstanceId(String vfModuleInstanceId) {
        this.vfModuleInstanceId = vfModuleInstanceId;
    }

    public String getVfModuleInstanceName() {
        return vfModuleInstanceName;
    }

    public void setVfModuleInstanceName(String vfModuleInstanceName) {
        this.vfModuleInstanceName = vfModuleInstanceName;
    }

    public String getVolumeGroupInstanceId() {
        return volumeGroupInstanceId;
    }

    public void setVolumeGroupInstanceId(String volumeGroupInstanceId) {
        this.volumeGroupInstanceId = volumeGroupInstanceId;
    }

    public String getVolumeGroupInstanceName() {
        return volumeGroupInstanceName;
    }

    public void setVolumeGroupInstanceName(String volumeGroupInstanceName) {
        this.volumeGroupInstanceName = volumeGroupInstanceName;
    }

    public String getNetworkInstanceId() {
        return networkInstanceId;
    }

    public void setNetworkInstanceId(String networkInstanceId) {
        this.networkInstanceId = networkInstanceId;
    }

    public String getNetworkInstanceName() {
        return networkInstanceName;
    }

    public void setNetworkInstanceName(String networkInstanceName) {
        this.networkInstanceName = networkInstanceName;
    }

    public String getRequestorId() {
        return requestorId;
    }

    public void setRequestorId(String requestorId) {
        this.requestorId = requestorId;
    }

    public String getInstanceGroupId() {
        return instanceGroupId;
    }

    public void setInstanceGroupId(String instanceGroupId) {
        this.instanceGroupId = instanceGroupId;
    }

    public String getInstanceGroupName() {
        return instanceGroupName;
    }

    public void setInstanceGroupName(String instanceGroupName) {
        this.instanceGroupName = instanceGroupName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("serviceInstanceId", serviceInstanceId)
                .append("serviceInstanceName", serviceInstanceName).append("vnfInstanceId", vnfInstanceId)
                .append("vnfInstanceName", vnfInstanceName).append("vfModuleInstanceId", vfModuleInstanceId)
                .append("vfModuleInstanceName", vfModuleInstanceName)
                .append("volumeGroupInstanceId", volumeGroupInstanceId)
                .append("volumeGroupInstanceName", volumeGroupInstanceName)
                .append("networkInstanceId", networkInstanceId).append("networkInstanceName", networkInstanceName)
                .append("requestorId", requestorId).append("instanceGroupId", instanceGroupId)
                .append("instanceGroupName", instanceGroupName).toString();
    }
}
