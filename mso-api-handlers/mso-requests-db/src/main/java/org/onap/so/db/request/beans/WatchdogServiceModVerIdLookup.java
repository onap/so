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
import java.util.Optional;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.apache.commons.lang3.builder.ToStringBuilder;

@IdClass(WatchdogServiceModVerIdLookupId.class)
@Entity
@Table(name = "watchdog_service_mod_ver_id_lookup")
public class WatchdogServiceModVerIdLookup implements Serializable {

    /**
     * Serialization id.
     */
    private static final long serialVersionUID = 7783869906430250355L;

    @Id
    @Column(name = "DISTRIBUTION_ID", length = 45)
    private String distributionId;
    @Id
    @Column(name = "SERVICE_MODEL_VERSION_ID", length = 45)
    private String serviceModelVersionId;
    @Column(name = "DISTRIBUTION_NOTIFICATION")
    private String distributionNotification;
    @Column(name = "CONSUMER_ID", length = 200)
    private String consumerId;
    @Column(name = "CREATE_TIME", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    public WatchdogServiceModVerIdLookup() {

    }

    /**
     * 
     * @param distributionId - Distribution ID
     * @param serviceModelVersionId -- service UUID
     * @param distributionNotification -- Notification content from ASDC
     * @param consumerId -- Consumer ID associated with subscription.
     */
    public WatchdogServiceModVerIdLookup(String distributionId, String serviceModelVersionId,
            Optional<String> distributionNotification, String consumerId) {
        this.distributionId = distributionId;
        this.serviceModelVersionId = serviceModelVersionId;
        this.distributionNotification = distributionNotification.orElse(null);
        this.consumerId = consumerId;
    }

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

    public Date getCreateTime() {
        return createTime;
    }

    @PrePersist
    protected void onCreate() {
        this.createTime = new Date();
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof WatchdogServiceModVerIdLookup)) {
            return false;
        }
        WatchdogServiceModVerIdLookup castOther = (WatchdogServiceModVerIdLookup) other;
        return Objects.equals(getDistributionId(), castOther.getDistributionId())
                && Objects.equals(getServiceModelVersionId(), castOther.getServiceModelVersionId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDistributionId(), getServiceModelVersionId());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("distributionId", getDistributionId())
                .append("serviceModelVersionId", getServiceModelVersionId()).append("createTime", getCreateTime())
                .append("distributionNotification", getDistributionNotification()).append("consumerId", getConsumerId())
                .toString();
    }

    public String getDistributionNotification() {
        return distributionNotification;
    }

    public void setDistributionNotification(String distributionNotification) {
        this.distributionNotification = distributionNotification;
    }

    public String getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }
}
