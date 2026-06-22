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
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;

@IdClass(WatchdogComponentDistributionStatusId.class)
@Entity
@Table(name = "watchdog_per_component_distribution_status")
public class WatchdogComponentDistributionStatus implements Serializable {


    /**
     * Serialization id.
     */
    private static final long serialVersionUID = -4344508954204488669L;
    @Id
    @Column(name = "DISTRIBUTION_ID", length = 45)
    private String distributionId;
    @Id
    @Column(name = "COMPONENT_NAME", length = 45)
    private String componentName;
    @Column(name = "COMPONENT_DISTRIBUTION_STATUS", length = 45)
    private String componentDistributionStatus;
    @Column(name = "CREATE_TIME", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;
    @Column(name = "MODIFY_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifyTime;

    public WatchdogComponentDistributionStatus() {

    }

    public WatchdogComponentDistributionStatus(String distributionId, String componentName) {
        this.distributionId = distributionId;
        this.componentName = componentName;
    }

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
        if (!(other instanceof WatchdogComponentDistributionStatus)) {
            return false;
        }
        WatchdogComponentDistributionStatus castOther = (WatchdogComponentDistributionStatus) other;
        return Objects.equals(getDistributionId(), castOther.getDistributionId())
                && Objects.equals(getComponentName(), castOther.getComponentName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDistributionId(), getComponentName());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("distributionId", getDistributionId())
                .append("componentName", getComponentName())
                .append("componentDistributionStatus", getComponentDistributionStatus())
                .append("createTime", getCreateTime()).append("modifyTime", getModifyTime()).toString();
    }

}
