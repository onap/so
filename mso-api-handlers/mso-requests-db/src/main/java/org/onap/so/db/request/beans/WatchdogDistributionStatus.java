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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
@Table(name = "watchdog_distributionid_status")
public class WatchdogDistributionStatus implements Serializable {

    /**
     * Serialization id.
     */
    private static final long serialVersionUID = -4449711060885719079L;

    @Id
    @Column(name = "DISTRIBUTION_ID", length = 45)
    private String distributionId;
    @Column(name = "DISTRIBUTION_ID_STATUS", length = 45)
    private String distributionIdStatus;
    @Column(name = "CREATE_TIME", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;
    @Column(name = "MODIFY_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifyTime;
    @Column(name = "LOCK_VERSION")
    private int version;

    public WatchdogDistributionStatus() {

    }

    public WatchdogDistributionStatus(String distributionId) {
        this.distributionId = distributionId;
    }

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

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
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
        if (!(other instanceof WatchdogDistributionStatus)) {
            return false;
        }
        WatchdogDistributionStatus castOther = (WatchdogDistributionStatus) other;
        return Objects.equals(getDistributionId(), castOther.getDistributionId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDistributionId());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("distributionId", getDistributionId())
                .append("distributionIdStatus", getDistributionIdStatus()).append("createTime", getCreateTime())
                .append("modifyTime", getModifyTime()).toString();
    }

}
