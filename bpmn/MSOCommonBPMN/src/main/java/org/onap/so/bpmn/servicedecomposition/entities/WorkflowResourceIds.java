/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Nokia
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

package org.onap.so.bpmn.servicedecomposition.entities;

import java.io.Serializable;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class WorkflowResourceIds implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8591599114353940105L;
    private String serviceInstanceId;
    private String pnfId;
    private String vnfId;
    private String networkId;
    private String volumeGroupId;
    private String vfModuleId;
    private String networkCollectionId;
    private String configurationId;
    private String instanceGroupId;


    public WorkflowResourceIds() {
        super();
    }

    public WorkflowResourceIds(WorkflowResourceIds workflowResourceIds) {
        this.serviceInstanceId = workflowResourceIds.serviceInstanceId;
        this.pnfId = workflowResourceIds.pnfId;
        this.vnfId = workflowResourceIds.vnfId;
        this.networkId = workflowResourceIds.networkId;
        this.volumeGroupId = workflowResourceIds.volumeGroupId;
        this.vfModuleId = workflowResourceIds.vfModuleId;
        this.networkCollectionId = workflowResourceIds.networkCollectionId;
        this.configurationId = workflowResourceIds.configurationId;
        this.instanceGroupId = workflowResourceIds.instanceGroupId;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this).append("serviceInstanceId", serviceInstanceId).append("pnfId", pnfId)
                .append("vnfId", vnfId).append("networkId", networkId).append("volumeGroupId", volumeGroupId)
                .append("vfModuleId", vfModuleId).append("networkCollectionId", networkCollectionId)
                .append("configurationId", configurationId).append("instanceGroupId", instanceGroupId).toString();
    }


    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    public String getPnfId() {
        return pnfId;
    }

    public void setPnfId(String pnfId) {
        this.pnfId = pnfId;
    }

    public String getVnfId() {
        return vnfId;
    }

    public void setVnfId(String vnfId) {
        this.vnfId = vnfId;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public String getVolumeGroupId() {
        return volumeGroupId;
    }

    public void setVolumeGroupId(String volumeGroupId) {
        this.volumeGroupId = volumeGroupId;
    }

    public String getVfModuleId() {
        return vfModuleId;
    }

    public void setVfModuleId(String vfModuleId) {
        this.vfModuleId = vfModuleId;
    }

    public String getNetworkCollectionId() {
        return networkCollectionId;
    }

    public void setNetworkCollectionId(String networkCollectionId) {
        this.networkCollectionId = networkCollectionId;
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
}
