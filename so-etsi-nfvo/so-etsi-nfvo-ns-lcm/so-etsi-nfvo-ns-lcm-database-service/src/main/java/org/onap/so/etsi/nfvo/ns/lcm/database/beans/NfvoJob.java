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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.utils.Utils;


@Entity
@Table(name = "JOB")
public class NfvoJob {

    @Id
    @Column(name = "JOB_ID")
    private String jobId;

    @Column(name = "JOB_TYPE")
    private String jobType;

    @Enumerated(EnumType.STRING)
    @Column(name = "JOB_ACTION")
    private JobAction jobAction;

    @Column(name = "RESOURCE_ID")
    private String resourceId;

    @Column(name = "RESOURCE_NAME")
    private String resourceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private JobStatusEnum status;

    @Column(name = "START_TIME")
    private LocalDateTime startTime;

    @Column(name = "END_TIME")
    private LocalDateTime endTime;

    @Column(name = "PROGRESS")
    private int progress;

    @Column(name = "PROCESS_INSTANCE_ID")
    private String processInstanceId;

    @OneToMany(mappedBy = "nfvoJob", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<NfvoJobStatus> nfvoJobStatuses = new ArrayList<>();

    public NfvoJob() {
        this.jobId = UUID.randomUUID().toString();
    }

    public void setJobId(final String jobId) {
        this.jobId = jobId;
    }

    public String getJobId() {
        return jobId;
    }

    public NfvoJob jobId(final String jobId) {
        this.jobId = jobId;
        return this;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(final String jobType) {
        this.jobType = jobType;
    }

    public NfvoJob jobType(final String jobType) {
        this.jobType = jobType;
        return this;
    }

    public JobAction getJobAction() {
        return jobAction;
    }

    public void setJobAction(final JobAction jobAction) {
        this.jobAction = jobAction;
    }

    public NfvoJob jobAction(final JobAction jobAction) {
        this.jobAction = jobAction;
        return this;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(final String resourceId) {
        this.resourceId = resourceId;
    }

    public NfvoJob resourceId(final String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    public JobStatusEnum getStatus() {
        return status;
    }

    public void setStatus(final JobStatusEnum status) {
        this.status = status;
    }

    public NfvoJob status(final JobStatusEnum status) {
        this.status = status;
        return this;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(final LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public NfvoJob startTime(final LocalDateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(final LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public NfvoJob endTime(final LocalDateTime endTime) {
        this.endTime = endTime;
        return this;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(final int progress) {
        this.progress = progress;
    }

    public NfvoJob progress(final int progress) {
        this.progress = progress;
        return this;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(final String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public NfvoJob processInstanceId(final String processInstanceId) {
        this.processInstanceId = processInstanceId;
        return this;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(final String resourceName) {
        this.resourceName = resourceName;
    }

    public NfvoJob resourceName(final String resourceName) {
        this.resourceName = resourceName;
        return this;
    }

    public List<NfvoJobStatus> getNfvoJobStatuses() {
        return nfvoJobStatuses;
    }

    public void setJobStatuses(final List<NfvoJobStatus> nfvoJobStatuses) {
        this.nfvoJobStatuses = nfvoJobStatuses;
    }

    public NfvoJob nfvoJobStatuses(final List<NfvoJobStatus> nfvoJobStatuses) {
        this.nfvoJobStatuses = nfvoJobStatuses;
        return this;
    }

    public NfvoJob nfvoJobStatus(final NfvoJobStatus nfvoJobStatus) {
        nfvoJobStatus.setNfvoJob(this);
        this.nfvoJobStatuses.add(nfvoJobStatus);
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId, processInstanceId, jobType, jobAction, startTime, endTime, status, progress,
                resourceId, resourceName, nfvoJobStatuses);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof NfvoJob) {
            final NfvoJob other = (NfvoJob) obj;
            return Objects.equals(jobId, other.jobId) && Objects.equals(processInstanceId, other.processInstanceId)
                    && Objects.equals(jobType, other.jobType) && Objects.equals(jobAction, other.jobAction)
                    && Objects.equals(progress, other.progress) && Objects.equals(status, other.status)
                    && Objects.equals(startTime, other.startTime) && Objects.equals(endTime, other.endTime)
                    && Objects.equals(status, other.status) && Objects.equals(resourceId, other.resourceId)
                    && Objects.equals(resourceName, other.resourceName)
                    && Utils.isEquals(nfvoJobStatuses, other.nfvoJobStatuses);
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("class NfvoJob {\n");
        sb.append("    jobId: ").append(toIndentedString(jobId)).append("\n");
        sb.append("    processInstanceId: ").append(toIndentedString(processInstanceId)).append("\n");
        sb.append("    jobType: ").append(toIndentedString(jobType)).append("\n");
        sb.append("    jobAction: ").append(toIndentedString(jobAction)).append("\n");
        sb.append("    progress: ").append(toIndentedString(progress)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    startTime: ").append(toIndentedString(startTime)).append("\n");
        sb.append("    endTime: ").append(toIndentedString(endTime)).append("\n");
        sb.append("    resId: ").append(toIndentedString(resourceId)).append("\n");
        sb.append("    resName: ").append(toIndentedString(resourceName)).append("\n");
        sb.append("    nfvoJobStatuses: ").append(toIndentedString(nfvoJobStatuses)).append("\n");

        sb.append("}");
        return sb.toString();
    }

}
