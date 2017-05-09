package org.openecomp.mso.bpmn.core.decomposition;

import java.io.Serializable;
import java.util.List;

public class HomingSolution  extends JsonWrapper implements Serializable  {

	private static final long serialVersionUID = 1L;
	
	private String infraServiceInstanceId;
	private String aicCloudRegionId;
	private String ucpeId;
	private List<String> entitlementPool;
	private List<String> entitlementKeyGroup;
	
	/*
	 * GET && SET
	 */
	public String getInfraServiceInstanceId() {
		return infraServiceInstanceId;
	}
	public void setInfraServiceInstanceId(String infraServiceInstanceId) {
		this.infraServiceInstanceId = infraServiceInstanceId;
	}
	public String getAicCloudRegionId() {
		return aicCloudRegionId;
	}
	public void setAicCloudRegionId(String aicCloudRegionId) {
		this.aicCloudRegionId = aicCloudRegionId;
	}
	public String getUcpeId() {
		return ucpeId;
	}
	public void setUcpeId(String ucpeId) {
		this.ucpeId = ucpeId;
	}
	public List<String> getEntitlementPool() {
		return entitlementPool;
	}
	public void setEntitlementPool(List<String> entitlementPool) {
		this.entitlementPool = entitlementPool;
	}
	public List getEntitlementKeyGroup() {
		return entitlementKeyGroup;
	}
	public void setEntitlementKeyGroup(List entitlementKeyGroup) {
		this.entitlementKeyGroup = entitlementKeyGroup;
	}
}
