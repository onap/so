/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

/**
 * The service operation status <br>
 * <p>
 * </p>
 * 
 * @author
 * @version ONAP Amsterdam Release 2017-08-28
 */

@IdClass(OperationStatusId.class)
@Entity
@Table(name = "operation_status")
public class OperationStatus implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "SERVICE_ID")
    private String serviceId;
    @Id
    @Column(name = "OPERATION_ID", length = 256)
    private String operationId;

    @Column(name = "SERVICE_NAME", length = 256)
    private String serviceName;

    @Column(name = "OPERATION_TYPE", length = 256)
    private String operation;

    @Column(name = "USER_ID", length = 256)
    private String userId;

    @Column(name = "RESULT", length = 256)
    private String result;

    @Column(name = "OPERATION_CONTENT", length = 256)
    private String operationContent;

    @Column(name = "PROGRESS", length = 256)
    private String progress = "0";

    @Column(name = "REASON", length = 256)
    private String reason;

    @Column(name = "OPERATE_AT", length = 256, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date operateAt;

    @Column(name = "FINISHED_AT", length = 256)
    @Temporal(TemporalType.TIMESTAMP)
    private Date finishedAt;

    public OperationStatus() {

    }

    public OperationStatus(String serviceId, String operationId) {
        this.serviceId = serviceId;
        this.operationId = operationId;
    }


    public String getServiceId() {
        return serviceId;
    }


    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }


    public String getOperationId() {
        return operationId;
    }


    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }


    public String getOperation() {
        return operation;
    }


    public void setOperation(String operation) {
        this.operation = operation;
    }


    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getUserId() {
        return userId;
    }


    public void setUserId(String userId) {
        this.userId = userId;
    }


    public String getResult() {
        return result;
    }


    public void setResult(String result) {
        this.result = result;
    }


    public String getOperationContent() {
        return operationContent;
    }


    public void setOperationContent(String operationContent) {
        this.operationContent = operationContent;
    }


    public String getProgress() {
        return progress;
    }


    public void setProgress(String progress) {
        this.progress = progress;
    }


    public String getReason() {
        return reason;
    }


    public void setReason(String reason) {
        this.reason = reason;
    }


    public Date getOperateAt() {
        return operateAt;
    }

    public Date getFinishedAt() {
        return finishedAt;
    }

    @PrePersist
    protected void onCreate() {
        this.finishedAt = this.operateAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        this.finishedAt = new Date();
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof OperationStatus)) {
            return false;
        }
        OperationStatus castOther = (OperationStatus) other;
        return Objects.equals(getServiceId(), castOther.getServiceId())
                && Objects.equals(getOperationId(), castOther.getOperationId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServiceId(), getOperationId());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("serviceId", getServiceId()).append("operationId", getOperationId())
                .append("operation", getOperation()).append("userId", getUserId()).append("result", getResult())
                .append("operationContent", getOperationContent()).append("progress", getProgress())
                .append("reason", getReason()).append("operateAt", getOperateAt()).append("finishedAt", getFinishedAt())
                .toString();
    }



}
