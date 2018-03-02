package org.openecomp.mso.bpmn.infrastructure.aai;

public class AAIServiceInstance {
	String serviceInstanceName;		
	String serviceType;
	String serviceRole;
	String orchestrationStatus;	
	String modelInvariantUuid;	
	String modelVersionId;		
	String environmentContext;
	String workloadContext;
	public AAIServiceInstance(String serviceInstanceName, String serviceType, String serviceRole,
			String orchestrationStatus, String modelInvariantUuid, String modelVersionId, String environmentContext,
			String workloadContext) {
		this.serviceInstanceName = serviceInstanceName;
		this.serviceType = serviceType;
		this.serviceRole = serviceRole;
		this.orchestrationStatus = orchestrationStatus;
		this.modelInvariantUuid = modelInvariantUuid;
		this.modelVersionId = modelVersionId;
		this.environmentContext = environmentContext;
		this.workloadContext = workloadContext;
	}
	public String getServiceInstanceName() {
		return serviceInstanceName;
	}
	public void setServiceInstanceName(String serviceInstanceName) {
		this.serviceInstanceName = serviceInstanceName;
	}
	public String getServiceType() {
		return serviceType;
	}
	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}
	public String getServiceRole() {
		return serviceRole;
	}
	public void setServiceRole(String serviceRole) {
		this.serviceRole = serviceRole;
	}
	public String getOrchestrationStatus() {
		return orchestrationStatus;
	}
	public void setOrchestrationStatus(String orchestrationStatus) {
		this.orchestrationStatus = orchestrationStatus;
	}
	public String getModelInvariantUuid() {
		return modelInvariantUuid;
	}
	public void setModelInvariantUuid(String modelInvariantUuid) {
		this.modelInvariantUuid = modelInvariantUuid;
	}
	public String getModelVersionId() {
		return modelVersionId;
	}
	public void setModelVersionId(String modelVersionId) {
		this.modelVersionId = modelVersionId;
	}
	public String getEnvironmentContext() {
		return environmentContext;
	}
	public void setEnvironmentContext(String environmentContext) {
		this.environmentContext = environmentContext;
	}
	public String getWorkloadContext() {
		return workloadContext;
	}
	public void setWorkloadContext(String workloadContext) {
		this.workloadContext = workloadContext;
	}
	

}
