/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License")
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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.DynamicUpdate;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@DynamicUpdate
@Table(name = "orchestration_task")
public class OrchestrationTask implements Serializable {

    private static final long serialVersionUID = -9158494554305291596L;

    @Id
    @Column(name = "TASK_ID", nullable = false, updatable = false)
    private String taskId;

    @Column(name = "REQUEST_ID", nullable = false, updatable = false)
    private String requestId;

    @Column(name = "NAME")
    private String name;

    @Column(name = "CREATED_TIME")
    private Date createdTime;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "IS_MANUAL")
    private String isManual;

    @Column(name = "PARAMS")
    private String params;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIsManual() {
        return isManual;
    }

    public void setIsManual(String isManual) {
        this.isManual = isManual;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String parameters) {
        this.params = parameters;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof OrchestrationTask)) {
            return false;
        }
        OrchestrationTask castOther = (OrchestrationTask) other;
        return Objects.equals(getTaskId(), castOther.getTaskId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getTaskId());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("taskId", getTaskId()).toString();
    }

    @PrePersist
    protected void onCreate() {
        this.createdTime = new Date();
    }

}

