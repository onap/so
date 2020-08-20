/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.etsi.nfvo.ns.lcm.database.beans;

import static org.onap.so.etsi.nfvo.ns.lcm.database.beans.utils.Utils.toIndentedString;
import java.time.LocalDateTime;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "JOB_STATUS")
public class NfvoJobStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private JobStatusEnum status;

    @Column(name = "DESCRIPTION", columnDefinition = "LONGTEXT")
    private String description;

    @Column(name = "UPDATED_TIME")
    private LocalDateTime updatedTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "JOB_ID")
    private NfvoJob nfvoJob;

    public int getId() {
        return id;
    }

    public JobStatusEnum getStatus() {
        return status;
    }

    public void setStatus(final JobStatusEnum status) {
        this.status = status;
    }

    public NfvoJobStatus status(final JobStatusEnum status) {
        this.status = status;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public NfvoJobStatus description(final String description) {
        this.description = description;
        return this;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(final LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    public NfvoJobStatus updatedTime(final LocalDateTime addTime) {
        this.updatedTime = addTime;
        return this;
    }

    public NfvoJob getNfvoJob() {
        return nfvoJob;
    }

    public void setNfvoJob(final NfvoJob nfvoJob) {
        this.nfvoJob = nfvoJob;
    }

    public NfvoJobStatus nfvoJob(final NfvoJob nfvoJob) {
        this.nfvoJob = nfvoJob;
        return this;
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, status, updatedTime, description, nfvoJob.getJobId());
    }

    @Override
    public boolean equals(final Object obj) {

        if (obj instanceof NfvoJobStatus) {
            final NfvoJobStatus other = (NfvoJobStatus) obj;
            return Objects.equals(id, other.id) && Objects.equals(status, other.status)
                    && Objects.equals(updatedTime, other.updatedTime) && Objects.equals(description, other.description)
                    && Objects.equals(nfvoJob.getJobId(), other.nfvoJob.getJobId());
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("class NfvoJobStatus {\n");
        sb.append("    Id: ").append(toIndentedString(id)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    descp: ").append(toIndentedString(description)).append("\n");
        sb.append("    updatedTime: ").append(toIndentedString(updatedTime)).append("\n");
        sb.append("    jobId: ").append(nfvoJob != null ? toIndentedString(nfvoJob.getJobId()) : "").append("\n");
        sb.append("}");
        return sb.toString();
    }

}
