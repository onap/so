package org.openecomp.mso.client.adapter.requests.db.entities;

public class UpdateInfraRequest {
	
	private String requestId;
	private String lastModifiedBy;
	private String statusMessage;
	private String responseBody;
    private RequestStatusType requestStatus;
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
    
    public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public String getLastModifiedBy() {
		return lastModifiedBy;
	}
	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}
	public String getStatusMessage() {
		return statusMessage;
	}
	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}
	public String getResponseBody() {
		return responseBody;
	}
	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}
	public RequestStatusType getRequestStatus() {
		return requestStatus;
	}
	public void setRequestStatus(RequestStatusType requestStatus) {
		this.requestStatus = requestStatus;
	}
	public String getProgress() {
		return progress;
	}
	public void setProgress(String progress) {
		this.progress = progress;
	}
	public String getVnfOutputs() {
		return vnfOutputs;
	}
	public void setVnfOutputs(String vnfOutputs) {
		this.vnfOutputs = vnfOutputs;
	}
	public String getServiceInstanceId() {
		return serviceInstanceId;
	}
	public void setServiceInstanceId(String serviceInstanceId) {
		this.serviceInstanceId = serviceInstanceId;
	}
	public String getNetworkId() {
		return networkId;
	}
	public void setNetworkId(String networkId) {
		this.networkId = networkId;
	}
	public String getVnfId() {
		return vnfId;
	}
	public void setVnfId(String vnfId) {
		this.vnfId = vnfId;
	}
	public String getVfModuleId() {
		return vfModuleId;
	}
	public void setVfModuleId(String vfModuleId) {
		this.vfModuleId = vfModuleId;
	}
	public String getVolumeGroupId() {
		return volumeGroupId;
	}
	public void setVolumeGroupId(String volumeGroupId) {
		this.volumeGroupId = volumeGroupId;
	}
	public String getServiceInstanceName() {
		return serviceInstanceName;
	}
	public void setServiceInstanceName(String serviceInstanceName) {
		this.serviceInstanceName = serviceInstanceName;
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
	public String getVfModuleName() {
		return vfModuleName;
	}
	public void setVfModuleName(String vfModuleName) {
		this.vfModuleName = vfModuleName;
	}
}
