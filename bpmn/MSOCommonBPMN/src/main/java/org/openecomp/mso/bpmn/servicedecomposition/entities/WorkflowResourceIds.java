package org.openecomp.mso.bpmn.servicedecomposition.entities;

import java.io.Serializable;

public class WorkflowResourceIds implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8591599114353940105L;
	private String serviceInstanceId;
	private String vnfId;
	private String networkId;
	private String volumeGroupId;
	private String vfModuleId;
	
	public String getServiceInstanceId() {
		return serviceInstanceId;
	}
	public void setServiceInstanceId(String serviceInstanceId) {
		this.serviceInstanceId = serviceInstanceId;
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
	@Override
	public String toString() {
		return "WorkflowResourceIds [serviceInstanceId=" + serviceInstanceId + ", vnfId=" + vnfId + ", networkId="
				+ networkId + ", volumeGroupId=" + volumeGroupId + ", vfModuleId=" + vfModuleId + "]";
	}
	
}
