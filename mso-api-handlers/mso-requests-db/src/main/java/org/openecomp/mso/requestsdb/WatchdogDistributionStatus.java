package org.openecomp.mso.requestsdb;

import java.io.Serializable;
import java.sql.Timestamp;

public class WatchdogDistributionStatus implements Serializable {

	/**
	 * Serialization id.
	 */
	private static final long serialVersionUID = -4449711060885719079L;


	private String distributionId;
	private String distributionIdStatus;
	private Timestamp createTime;
	private Timestamp modifyTime;
	
	
	public String getDistributionId() {
		return distributionId;
	}
	
	public void setDistributionId(String distributionId) {
		this.distributionId = distributionId;
	}
	
	public String getDistributionIdStatus() {
		return distributionIdStatus;
	}
	
	public void setDistributionIdStatus(String distributionIdStatus) {
		this.distributionIdStatus = distributionIdStatus;
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
