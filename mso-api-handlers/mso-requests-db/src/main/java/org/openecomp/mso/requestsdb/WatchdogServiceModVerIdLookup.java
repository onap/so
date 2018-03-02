package org.openecomp.mso.requestsdb;

import java.io.Serializable;
import java.sql.Timestamp;

public class WatchdogServiceModVerIdLookup implements Serializable {

	/**
	 * Serialization id.
	 */
	private static final long serialVersionUID = 7783869906430250355L;
	
	private String distributionId;
	private String serviceModelVersionId;
	private Timestamp createTime;
	
	
	public String getDistributionId() {
		return distributionId;
	}
	
	public void setDistributionId(String distributionId) {
		this.distributionId = distributionId;
	}
	
	public String getServiceModelVersionId() {
		return serviceModelVersionId;
	}

	public void setServiceModelVersionId(String serviceModelVersionId) {
		this.serviceModelVersionId = serviceModelVersionId;
	}
	
	public Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

}
