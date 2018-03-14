/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

/**
 * 
 */
package org.openecomp.mso.requestsdb;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author PB6115
 *
 */
public class OperationalEnvServiceModelStatus implements Serializable {

    /**
	 * Serialization id.
	 */
	private static final long serialVersionUID = 8197084996598869656L;
	
	private String requestId;
	private String operationalEnvId;
	private String serviceModelVersionId;
	private String serviceModelVersionDistrStatus;
	private String recoveryAction;
	private int retryCount;
	private String workloadContext;
	private Timestamp createTime;
	private Timestamp modifyTime;
	
	public String getRequestId() {
		return requestId;
	}
	
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	
	public String getOperationalEnvId() {
		return operationalEnvId;
	}
	
	public void setOperationalEnvId(String operationalEnvId) {
		this.operationalEnvId = operationalEnvId;
	}
	
	public String getServiceModelVersionId() {
		return serviceModelVersionId;
	}
	
	public void setServiceModelVersionId(String serviceModelVersionId) {
		this.serviceModelVersionId = serviceModelVersionId;
	}
	
	public String getServiceModelVersionDistrStatus() {
		return serviceModelVersionDistrStatus;
	}
	
	public void setServiceModelVersionDistrStatus(String serviceModelVersionDistrStatus) {
		this.serviceModelVersionDistrStatus = serviceModelVersionDistrStatus;
	}
	
	public String getRecoveryAction() {
		return recoveryAction;
	}
	
	public void setRecoveryAction(String recoveryAction) {
		this.recoveryAction = recoveryAction;
	}
	
	public int getRetryCount() {
		return retryCount;
	}
	
	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public String getWorkloadContext() {
		return workloadContext;
	}

	public void setWorkloadContext(String workloadContext) {
		this.workloadContext = workloadContext;
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
