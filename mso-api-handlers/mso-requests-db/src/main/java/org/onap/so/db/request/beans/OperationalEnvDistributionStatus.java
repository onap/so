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
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;


@Entity
@Table(name = "activate_operational_env_per_distributionid_status")
public class OperationalEnvDistributionStatus implements Serializable {

    /**
     * Serialization id.
     */
    private static final long serialVersionUID = 7398393659281364650L;

    @Id
    @Column(name = "DISTRIBUTION_ID", length = 45)
    private String distributionId;
    @Column(name = "OPERATIONAL_ENV_ID", length = 45)
    private String operationalEnvId;
    @Column(name = "SERVICE_MODEL_VERSION_ID", length = 45)
    private String serviceModelVersionId;
    @Column(name = "REQUEST_ID", length = 45)
    private String requestId;
    @Column(name = "DISTRIBUTION_ID_STATUS", length = 45)
    private String distributionIdStatus;
    @Column(name = "DISTRIBUTION_ID_ERROR_REASON", length = 250)
    private String distributionIdErrorReason;
    @Column(name = "CREATE_TIME", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;
    @Column(name = "MODIFY_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifyTime;


    public OperationalEnvDistributionStatus() {

    }

    public OperationalEnvDistributionStatus(String distributionId, String operationalEnvId,
            String serviceModelVersionId) {
        this.distributionId = distributionId;
        this.operationalEnvId = operationalEnvId;
        this.serviceModelVersionId = serviceModelVersionId;
    }

    public String getDistributionId() {
        return distributionId;
    }

    public void setDistributionId(String distributionId) {
        this.distributionId = distributionId;
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

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getDistributionIdStatus() {
        return distributionIdStatus;
    }

    public void setDistributionIdStatus(String distributionIdStatus) {
        this.distributionIdStatus = distributionIdStatus;
    }

    public String getDistributionIdErrorReason() {
        return distributionIdErrorReason;
    }

    public void setDistributionIdErrorReason(String distributionIdErrorReason) {
        this.distributionIdErrorReason = distributionIdErrorReason;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
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
        if (!(other instanceof OperationalEnvDistributionStatus)) {
            return false;
        }
        OperationalEnvDistributionStatus castOther = (OperationalEnvDistributionStatus) other;
        return Objects.equals(getDistributionId(), castOther.getDistributionId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDistributionId());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("distributionId", getDistributionId())
                .append("operationalEnvId", getOperationalEnvId())
                .append("serviceModelVersionId", getServiceModelVersionId()).append("requestId", getRequestId())
                .append("distributionIdStatus", getDistributionIdStatus())
                .append("distributionIdErrorReason", getDistributionIdErrorReason())
                .append("createTime", getCreateTime()).append("modifyTime", getModifyTime()).toString();
    }

}
