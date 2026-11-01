package org.onap.so.db.request.client;

import com.fasterxml.jackson.annotation.JsonInclude;

public final class RequestDbCommands {
    private RequestDbCommands() {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InfraRequestUpdate {
        private String lastModifiedBy;
        private String statusMessage;
        private String responseBody;
        private String requestStatus;
        private String progress;
        private String vnfOutputs;
        private String serviceInstanceId;
        private String networkId;
        private String vnfId;
        private String vfModuleId;
        private String volumeGroupId;
        private String serviceInstanceName;
        private String configurationId;
        private String configurationName;
        private String vfModuleName;

        public String getLastModifiedBy() {
            return lastModifiedBy;
        }

        public void setLastModifiedBy(String value) {
            lastModifiedBy = value;
        }

        public String getStatusMessage() {
            return statusMessage;
        }

        public void setStatusMessage(String value) {
            statusMessage = value;
        }

        public String getResponseBody() {
            return responseBody;
        }

        public void setResponseBody(String value) {
            responseBody = value;
        }

        public String getRequestStatus() {
            return requestStatus;
        }

        public void setRequestStatus(String value) {
            requestStatus = value;
        }

        public String getProgress() {
            return progress;
        }

        public void setProgress(String value) {
            progress = value;
        }

        public String getVnfOutputs() {
            return vnfOutputs;
        }

        public void setVnfOutputs(String value) {
            vnfOutputs = value;
        }

        public String getServiceInstanceId() {
            return serviceInstanceId;
        }

        public void setServiceInstanceId(String value) {
            serviceInstanceId = value;
        }

        public String getNetworkId() {
            return networkId;
        }

        public void setNetworkId(String value) {
            networkId = value;
        }

        public String getVnfId() {
            return vnfId;
        }

        public void setVnfId(String value) {
            vnfId = value;
        }

        public String getVfModuleId() {
            return vfModuleId;
        }

        public void setVfModuleId(String value) {
            vfModuleId = value;
        }

        public String getVolumeGroupId() {
            return volumeGroupId;
        }

        public void setVolumeGroupId(String value) {
            volumeGroupId = value;
        }

        public String getServiceInstanceName() {
            return serviceInstanceName;
        }

        public void setServiceInstanceName(String value) {
            serviceInstanceName = value;
        }

        public String getConfigurationId() {
            return configurationId;
        }

        public void setConfigurationId(String value) {
            configurationId = value;
        }

        public String getConfigurationName() {
            return configurationName;
        }

        public void setConfigurationName(String value) {
            configurationName = value;
        }

        public String getVfModuleName() {
            return vfModuleName;
        }

        public void setVfModuleName(String value) {
            vfModuleName = value;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InstanceNfvoMappingUpdate {
        private String nfvoName;
        private String endpoint;
        private String username;
        private String password;
        private String apiRoot;

        public String getNfvoName() {
            return nfvoName;
        }

        public void setNfvoName(String value) {
            nfvoName = value;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String value) {
            endpoint = value;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String value) {
            username = value;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String value) {
            password = value;
        }

        public String getApiRoot() {
            return apiRoot;
        }

        public void setApiRoot(String value) {
            apiRoot = value;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ServiceOperationWrite {
        private String serviceId;
        private String operationId;
        private String operationType;
        private String userId;
        private String result;
        private String operationContent;
        private String progress;
        private String reason;

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String value) {
            serviceId = value;
        }

        public String getOperationId() {
            return operationId;
        }

        public void setOperationId(String value) {
            operationId = value;
        }

        public String getOperationType() {
            return operationType;
        }

        public void setOperationType(String value) {
            operationType = value;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String value) {
            userId = value;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String value) {
            result = value;
        }

        public String getOperationContent() {
            return operationContent;
        }

        public void setOperationContent(String value) {
            operationContent = value;
        }

        public String getProgress() {
            return progress;
        }

        public void setProgress(String value) {
            progress = value;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String value) {
            reason = value;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ResourceOperationInit {
        private String serviceId;
        private String operationId;
        private String operationType;
        private String resourceTemplateUUIDs;

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String value) {
            serviceId = value;
        }

        public String getOperationId() {
            return operationId;
        }

        public void setOperationId(String value) {
            operationId = value;
        }

        public String getOperationType() {
            return operationType;
        }

        public void setOperationType(String value) {
            operationType = value;
        }

        public String getResourceTemplateUUIDs() {
            return resourceTemplateUUIDs;
        }

        public void setResourceTemplateUUIDs(String value) {
            resourceTemplateUUIDs = value;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ResourceOperationUpdate {
        private String operType;
        private String resourceInstanceID;
        private String jobId;
        private String status;
        private String progress;
        private String errorCode;
        private String statusDescription;

        public String getOperType() {
            return operType;
        }

        public void setOperType(String value) {
            operType = value;
        }

        public String getResourceInstanceID() {
            return resourceInstanceID;
        }

        public void setResourceInstanceID(String value) {
            resourceInstanceID = value;
        }

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String value) {
            jobId = value;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String value) {
            status = value;
        }

        public String getProgress() {
            return progress;
        }

        public void setProgress(String value) {
            progress = value;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String value) {
            errorCode = value;
        }

        public String getStatusDescription() {
            return statusDescription;
        }

        public void setStatusDescription(String value) {
            statusDescription = value;
        }
    }

    public static class SiteState {
        private boolean enabled;

        public SiteState() {}

        public SiteState(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean value) {
            enabled = value;
        }
    }
}
