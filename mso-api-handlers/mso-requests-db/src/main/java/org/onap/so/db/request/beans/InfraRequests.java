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

import java.net.URI;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.onap.so.requestsdb.TimestampXMLAdapter;

import uk.co.blackpepper.bowman.annotation.ResourceId;

@MappedSuperclass
public abstract class InfraRequests implements java.io.Serializable {

    private static final long serialVersionUID = -5497583682393936143L;
    private static final String UNKNOWN = "unknown";


    @Id
    @Column(name = "REQUEST_ID", length = 45)
    private String requestId;
    @Column(name = "CLIENT_REQUEST_ID", length = 45, unique = true)
    private String clientRequestId;
    @Column(name = "ACTION", length = 45)
    private String action;
    @Column(name = "REQUEST_STATUS", length = 20)
    private String requestStatus;
    @Column(name = "STATUS_MESSAGE", length = 2000)
    private String statusMessage;
    @Column(name = "ROLLBACK_STATUS_MESSAGE", length = 2000)
    private String rollbackStatusMessage;
    @Column(name = "FLOW_STATUS", length = 2000)
    private String flowStatus;
    @Column(name = "RETRY_STATUS_MESSAGE", length = 2000)
    private String retryStatusMessage;
    @Column(name = "PROGRESS", precision = 11)
    private Long progress;

    @Column(name = "START_TIME")
    private Timestamp startTime;
    @Column(name = "END_TIME")
    private Timestamp endTime;
    @Column(name = "SOURCE", length = 45)
    private String source;
    @Column(name = "VNF_ID", length = 45)
    private String vnfId;
    @Column(name = "VNF_NAME", length = 80)
    private String vnfName;
    @Column(name = "VNF_TYPE", length = 200)
    private String vnfType;
    @Column(name = "SERVICE_TYPE", length = 45)
    private String serviceType;
    @Column(name = "AIC_NODE_CLLI", length = 11)
    private String aicNodeClli;
    @Column(name = "TENANT_ID", length = 45)
    private String tenantId;
    @Column(name = "PROV_STATUS", length = 20)
    private String provStatus;
    @Column(name = "VNF_PARAMS")
    private String vnfParams;
    @Column(name = "VNF_OUTPUTS")
    private String vnfOutputs;
    @Column(name = "REQUEST_BODY")
    private String requestBody;
    @Column(name = "RESPONSE_BODY")
    private String responseBody;
    @Column(name = "LAST_MODIFIED_BY", length = 50)
    private String lastModifiedBy;
    @Column(name = "MODIFY_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifyTime;
    @Column(name = "REQUEST_TYPE", length = 20)
    private String requestType;
    @Column(name = "VOLUME_GROUP_ID", length = 45)
    private String volumeGroupId;
    @Column(name = "VOLUME_GROUP_NAME", length = 45)
    private String volumeGroupName;
    @Column(name = "VF_MODULE_ID", length = 45)
    private String vfModuleId;
    @Column(name = "VF_MODULE_NAME", length = 200)
    private String vfModuleName;
    @Column(name = "VF_MODULE_MODEL_NAME", length = 200)
    private String vfModuleModelName;
    @Column(name = "AAI_SERVICE_ID", length = 50)
    private String aaiServiceId;
    @Column(name = "AIC_CLOUD_REGION", length = 11)
    private String aicCloudRegion;
    @Column(name = "CALLBACK_URL", length = 200)
    private String callBackUrl;
    @Column(name = "CORRELATOR", length = 80)
    private String correlator;
    @Column(name = "SERVICE_INSTANCE_ID", length = 45)
    private String serviceInstanceId;
    @Column(name = "SERVICE_INSTANCE_NAME", length = 80)
    private String serviceInstanceName;
    @Column(name = "REQUEST_SCOPE", length = 45)
    private String requestScope;
    @Column(name = "REQUEST_ACTION", length = 45)
    private String requestAction;
    @Column(name = "NETWORK_ID", length = 45)
    private String networkId;
    @Column(name = "NETWORK_NAME", length = 80)
    private String networkName;
    @Column(name = "NETWORK_TYPE", length = 80)
    private String networkType;
    @Column(name = "REQUESTOR_ID", length = 80)
    private String requestorId;
    @Column(name = "CONFIGURATION_ID", length = 45)
    private String configurationId;
    @Column(name = "CONFIGURATION_NAME", length = 200)
    private String configurationName;
    @Column(name = "OPERATIONAL_ENV_ID", length = 45)
    private String operationalEnvId;
    @Column(name = "OPERATIONAL_ENV_NAME", length = 200)
    private String operationalEnvName;
    @Column(name = "REQUEST_URL", length = 500)
    private String requestUrl;    
    
    @ResourceId
    public URI getRequestURI() {
        return URI.create(this.requestId);
    }

    public String getRequestId() {
        return this.requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getClientRequestId() {
        return clientRequestId;
    }

    public void setClientRequestId(String clientRequestId) {
        this.clientRequestId = clientRequestId;
    }

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getRequestStatus() {
        return this.requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public String getStatusMessage() {
        return this.statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
    
    public String getRollbackStatusMessage() {
        return this.rollbackStatusMessage;
    }

    public void setRollbackStatusMessage(String rollbackStatusMessage) {
        this.rollbackStatusMessage = rollbackStatusMessage;
    }
    
    public String getFlowStatus() {
        return this.flowStatus;
    }

    public void setFlowStatus(String flowStatus) {
        this.flowStatus = flowStatus;
    }
    
    public String getRetryStatusMessage() {
        return this.retryStatusMessage;
    }

    public void setRetryStatusMessage(String retryStatusMessage) {
        this.retryStatusMessage = retryStatusMessage;
    }

    public Long getProgress() {
        return this.progress;
    }

    public void setProgress(Long progress) {
        this.progress = progress;
    }

    @XmlJavaTypeAdapter(TimestampXMLAdapter.class)
    public Timestamp getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    @XmlJavaTypeAdapter(TimestampXMLAdapter.class)
    public Timestamp getEndTime() {
        return this.endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getVnfId() {
        return this.vnfId;
    }

    public void setVnfId(String vnfId) {
        this.vnfId = vnfId;
    }

    public String getVnfName() {
        return this.vnfName;
    }

    public void setVnfName(String vnfName) {
        this.vnfName = vnfName;
    }

    public String getVnfType() {
        return this.vnfType;
    }

    public void setVnfType(String vnfType) {
        this.vnfType = vnfType;
    }

    public String getServiceType() {
        return this.serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getAicNodeClli() {
        return this.aicNodeClli;
    }

    public void setAicNodeClli(String aicNodeClli) {
        this.aicNodeClli = aicNodeClli;
    }

    public String getTenantId() {
        return this.tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getProvStatus() {
        return this.provStatus;
    }

    public void setProvStatus(String provStatus) {
        this.provStatus = provStatus;
    }

    public String getVnfParams() {
        return this.vnfParams;
    }

    public void setVnfParams(String vnfParams) {
        this.vnfParams = vnfParams;
    }

    public String getVnfOutputs() {
        return this.vnfOutputs;
    }

    public void setVnfOutputs(String vnfOutputs) {
        this.vnfOutputs = vnfOutputs;
    }

    public String getRequestBody() {
        return this.requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getResponseBody() {
        return this.responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public String getLastModifiedBy() {
        return this.lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Date getModifyTime() {
        return this.modifyTime;
    }

    public String getRequestType() {
        return this.requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getVolumeGroupId() {
        return this.volumeGroupId;
    }

    public void setVolumeGroupId(String volumeGroupId) {
        this.volumeGroupId = volumeGroupId;
    }

    public String getVolumeGroupName() {
        return this.volumeGroupName;
    }

    public void setVolumeGroupName(String volumeGroupName) {
        this.volumeGroupName = volumeGroupName;
    }

    public String getVfModuleId() {
        return this.vfModuleId;
    }

    public void setVfModuleId(String vfModuleId) {
        this.vfModuleId = vfModuleId;
    }

    public String getVfModuleName() {
        return this.vfModuleName;
    }

    public void setVfModuleName(String vfModuleName) {
        this.vfModuleName = vfModuleName;
    }

    public String getVfModuleModelName() {
        return this.vfModuleModelName;
    }

    public void setVfModuleModelName(String vfModuleModelName) {
        this.vfModuleModelName = vfModuleModelName;
    }

    public String getAaiServiceId() {
        return this.aaiServiceId;
    }

    public void setAaiServiceId(String aaiServiceId) {
        this.aaiServiceId = aaiServiceId;
    }

    public String getAicCloudRegion() {
        return this.aicCloudRegion;
    }

    public void setAicCloudRegion(String aicCloudRegion) {
        this.aicCloudRegion = aicCloudRegion;
    }

    public String getCallBackUrl() {
        return callBackUrl;
    }

    public void setCallBackUrl(String callBackUrl) {
        this.callBackUrl = callBackUrl;
    }

    public String getCorrelator() {
        return correlator;
    }

    public void setCorrelator(String correlator) {
        this.correlator = correlator;
    }

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

    public String getRequestScope() {
        return requestScope;
    }

    public void setRequestScope(String requestScope) {
        this.requestScope = requestScope;
    }

    public String getRequestAction() {
        return requestAction;
    }

    public void setRequestAction(String requestAction) {
        this.requestAction = requestAction;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public String getRequestorId() {
        return requestorId;
    }

    public void setRequestorId(String requestorId) {
        this.requestorId = requestorId;
    }

    public String getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(String configurationId) {
        this.configurationId = configurationId;
    }

    public String getConfigurationName() {
        return configurationName;
    }

    public void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }

    public String getOperationalEnvId() {
        return operationalEnvId;
    }

    public void setOperationalEnvId(String operationalEnvId) {
        this.operationalEnvId = operationalEnvId;
    }

    public String getOperationalEnvName() {
        return operationalEnvName;
    }

    public void setOperationalEnvName(String operationalEnvName) {
        this.operationalEnvName = operationalEnvName;
    }

    public String getRequestUrl() {
        return this.requestUrl;
    }
    
    public void setRequestUrl(String requestUrl) {
         this.requestUrl = requestUrl;
    }    
    
    @PrePersist
    protected void onCreate() {
        if (requestScope == null)
            requestScope = UNKNOWN;
        if (requestAction == null)
            requestAction = UNKNOWN;
        this.modifyTime = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        if (requestScope == null)
            requestScope = UNKNOWN;
        if (requestAction == null)
            requestAction = UNKNOWN;
        this.modifyTime = new Date();
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof InfraRequests)) {
            return false;
        }
        InfraRequests castOther = (InfraRequests) other;
        return Objects.equals(getRequestId(), castOther.getRequestId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRequestId());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("requestId", getRequestId())
                .append("clientRequestId", getClientRequestId()).append("action", getAction())
                .append("requestStatus", getRequestStatus()).append("statusMessage", getStatusMessage()).append("rollbackStatusMessage", getRollbackStatusMessage())
                .append("flowStatus", getFlowStatus()).append("retryStatusMessage", getRetryStatusMessage())
                .append("progress", getProgress()).append("startTime", getStartTime()).append("endTime", getEndTime())
                .append("source", getSource()).append("vnfId", getVnfId()).append("vnfName", getVnfName())
                .append("vnfType", getVnfType()).append("serviceType", getServiceType())
                .append("aicNodeClli", getAicNodeClli()).append("tenantId", getTenantId())
                .append("provStatus", getProvStatus()).append("vnfParams", getVnfParams())
                .append("vnfOutputs", getVnfOutputs()).append("requestBody", getRequestBody())
                .append("responseBody", getResponseBody()).append("lastModifiedBy", getLastModifiedBy())
                .append("modifyTime", getModifyTime()).append("requestType", getRequestType())
                .append("volumeGroupId", getVolumeGroupId()).append("volumeGroupName", getVolumeGroupName())
                .append("vfModuleId", getVfModuleId()).append("vfModuleName", getVfModuleName())
                .append("vfModuleModelName", getVfModuleModelName()).append("aaiServiceId", getAaiServiceId())
                .append("aicCloudRegion", getAicCloudRegion()).append("callBackUrl", getCallBackUrl())
                .append("correlator", getCorrelator()).append("serviceInstanceId", getServiceInstanceId())
                .append("serviceInstanceName", getServiceInstanceName()).append("requestScope", getRequestScope())
                .append("requestAction", getRequestAction()).append("networkId", getNetworkId())
                .append("networkName", getNetworkName()).append("networkType", getNetworkType())
                .append("requestorId", getRequestorId()).append("configurationId", getConfigurationId())
                .append("configurationName", getConfigurationName()).append("operationalEnvId", getOperationalEnvId())
                .append("operationalEnvName", getOperationalEnvName())
                .append("requestUrl", getRequestUrl()).toString();
    }
}
