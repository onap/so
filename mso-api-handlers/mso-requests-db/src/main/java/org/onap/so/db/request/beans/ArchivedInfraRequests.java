/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.db.request.beans;

import java.util.Objects;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
@Table(name = "archived_infra_requests")
public class ArchivedInfraRequests extends InfraRequests {

    private static final long serialVersionUID = 7132783898142451603L;

    public ArchivedInfraRequests() {}

    public ArchivedInfraRequests(String requestId, String action) {
        setRequestId(requestId);
    }

    public ArchivedInfraRequests(String requestId) {
        setRequestId(requestId);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ArchivedInfraRequests)) {
            return false;
        }
        ArchivedInfraRequests castOther = (ArchivedInfraRequests) other;
        return Objects.equals(getRequestId(), castOther.getRequestId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRequestId());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("requestId", getRequestId()).append("requestStatus", getRequestStatus())
                .append("statusMessage", getStatusMessage()).append("progress", getProgress())
                .append("startTime", getStartTime()).append("endTime", getEndTime()).append("source", getSource())
                .append("vnfId", getVnfId()).append("vnfName", getVnfName()).append("vnfType", getVnfType())
                .append("serviceType", getServiceType()).append("tenantId", getTenantId())
                .append("vnfParams", getVnfParams()).append("vnfOutputs", getVnfOutputs())
                .append("requestBody", getRequestBody()).append("responseBody", getResponseBody())
                .append("lastModifiedBy", getLastModifiedBy()).append("modifyTime", getModifyTime())
                .append("volumeGroupId", getVolumeGroupId()).append("volumeGroupName", getVolumeGroupName())
                .append("vfModuleId", getVfModuleId()).append("vfModuleName", getVfModuleName())
                .append("vfModuleModelName", getVfModuleModelName()).append("cloudRegion", getCloudRegion())
                .append("callBackUrl", getCallBackUrl()).append("correlator", getCorrelator())
                .append("serviceInstanceId", getServiceInstanceId())
                .append("serviceInstanceName", getServiceInstanceName()).append("requestScope", getRequestScope())
                .append("requestAction", getRequestAction()).append("networkId", getNetworkId())
                .append("networkName", getNetworkName()).append("networkType", getNetworkType())
                .append("requestorId", getRequestorId()).append("configurationId", getConfigurationId())
                .append("configurationName", getConfigurationName()).append("operationalEnvId", getOperationalEnvId())
                .append("operationalEnvName", getOperationalEnvName()).append("requestUrl", getRequestUrl())
                .append("tenantName", getTenantName()).append("productFamilyName", getProductFamilyName())
                .append("workflowName", getWorkflowName()).append("operationName", getOperationName()).toString();
    }

}
