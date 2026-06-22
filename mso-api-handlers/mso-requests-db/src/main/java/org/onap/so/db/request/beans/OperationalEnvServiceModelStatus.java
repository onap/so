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

package org.onap.so.db.request.beans;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author PB6115
 *
 */
@IdClass(OperationalEnvServiceModelStatusId.class)
@Entity
@Table(name = "activate_operational_env_service_model_distribution_status")
public class OperationalEnvServiceModelStatus implements Serializable {

    /**
     * Serialization id.
     */
    private static final long serialVersionUID = 8197084996598869656L;

    @Id
    @Column(name = "REQUEST_ID", length = 45)
    private String requestId;
    @Id
    @Column(name = "OPERATIONAL_ENV_ID", length = 45)
    private String operationalEnvId;
    @Id
    @Column(name = "SERVICE_MODEL_VERSION_ID", length = 45)
    private String serviceModelVersionId;
    @Column(name = "SERVICE_MOD_VER_FINAL_DISTR_STATUS", length = 45)
    private String serviceModelVersionDistrStatus;
    @Column(name = "RECOVERY_ACTION", length = 30)
    private String recoveryAction;
    @Column(name = "RETRY_COUNT_LEFT")
    private Integer retryCount;
    @Column(name = "WORKLOAD_CONTEXT", length = 80)
    private String workloadContext;
    @Column(name = "CREATE_TIME", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;
    @Column(name = "MODIFY_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifyTime;
    @Column(name = "VNF_OPERATIONAL_ENV_ID", length = 45)
    private String vnfOperationalEnvId;

    public OperationalEnvServiceModelStatus() {

    }

    public OperationalEnvServiceModelStatus(String requestId, String operationalEnvId, String serviceModelVersionId) {
        this.requestId = requestId;
        this.operationalEnvId = operationalEnvId;
        this.serviceModelVersionId = serviceModelVersionId;
    }

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

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public String getWorkloadContext() {
        return workloadContext;
    }

    public void setWorkloadContext(String workloadContext) {
        this.workloadContext = workloadContext;
    }

    public Date getCreateTime() {
        return createTime;
    }


    public Date getModifyTime() {
        return modifyTime;
    }

    public String getVnfOperationalEnvId() {
        return vnfOperationalEnvId;
    }

    public void setVnfOperationalEnvId(String vnfOperationalEnvId) {
        this.vnfOperationalEnvId = vnfOperationalEnvId;
    }

    @PrePersist
    protected void onCreate() {
        this.createTime = this.modifyTime = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        this.modifyTime = new Date();
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof OperationalEnvServiceModelStatus)) {
            return false;
        }
        OperationalEnvServiceModelStatus castOther = (OperationalEnvServiceModelStatus) other;
        return Objects.equals(getRequestId(), castOther.getRequestId())
                && Objects.equals(getOperationalEnvId(), castOther.getOperationalEnvId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRequestId(), getOperationalEnvId());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("requestId", getRequestId())
                .append("operationalEnvId", getOperationalEnvId())
                .append("serviceModelVersionId", getServiceModelVersionId())
                .append("serviceModelVersionDistrStatus", getServiceModelVersionDistrStatus())
                .append("recoveryAction", getRecoveryAction()).append("retryCount", getRetryCount())
                .append("workloadContext", getWorkloadContext()).append("createTime", getCreateTime())
                .append("modifyTime", getModifyTime()).append("vnfOperationalEnvId", getVnfOperationalEnvId())
                .toString();
    }


}
