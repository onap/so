package org.openecomp.mso.requestsdb;

import java.io.Serializable;
import java.sql.Timestamp;

public class WatchdogComponentDistributionStatus implements Serializable {


	/**
	 * Serialization id.
	 */
	private static final long serialVersionUID = -4344508954204488669L;
	
	private String distributionId;
	private String componentName;
	private String componentDistributionStatus;
	private Timestamp createTime;
	private Timestamp modifyTime;
	
	
	public String getDistributionId() {
		return distributionId;
	}
	
	public void setDistributionId(String distributionId) {
		this.distributionId = distributionId;
	}
	
	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	public String getComponentDistributionStatus() {
		return componentDistributionStatus;
	}

	public void setComponentDistributionStatus(String componentDistributionStatus) {
		this.componentDistributionStatus = componentDistributionStatus;
	}
	
	public Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	
	public Timestamp getModifyTime() {
		return modifyTime;
	}
	
	public void setModifyTime(Timestamp modifyTime) {
		this.modifyTime = modifyTime;
	}

}
