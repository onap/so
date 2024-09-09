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
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * The Resource operation status <br>
 * <p>
 * </p>
 * 
 * @author
 * @version ONAP Amsterdam Release 2017-08-28
 */

@IdClass(ResourceOperationStatusId.class)
@Entity
@Table(name = "resource_operation_status")
public class ResourceOperationStatus implements Serializable {

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
    @Id
    @Column(name = "RESOURCE_TEMPLATE_UUID")
    private String resourceTemplateUUID;

    @Column(name = "OPER_TYPE", length = 256)
    private String operType;

    @Column(name = "RESOURCE_INSTANCE_ID", length = 256)
    private String resourceInstanceID;

    @Column(name = "JOB_ID", length = 256)
    private String jobId;

    @Column(name = "STATUS", length = 256)
    private String status;

    @Column(name = "PROGRESS", length = 256)
    private String progress = "0";

    @Column(name = "ERROR_CODE", length = 256)
    private String errorCode;

    @Column(name = "STATUS_DESCRIPOTION", length = 256)
    private String statusDescription;

    public ResourceOperationStatus() {

    }

    public ResourceOperationStatus(String serviceId, String operationId, String resourceTemplateUUID) {
        this.serviceId = serviceId;
        this.operationId = operationId;
        this.resourceTemplateUUID = resourceTemplateUUID;
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


    public String getResourceTemplateUUID() {
        return resourceTemplateUUID;
    }


    public void setResourceTemplateUUID(String resourceTemplateUUId) {
        this.resourceTemplateUUID = resourceTemplateUUId;
    }


    public String getJobId() {
        return jobId;
    }


    public void setJobId(String jobId) {
        this.jobId = jobId;
    }


    public String getStatus() {
        return status;
    }


    public void setStatus(String status) {
        this.status = status;
    }


    public String getProgress() {
        return progress;
    }


    public void setProgress(String progress) {
        this.progress = progress;
    }


    public String getErrorCode() {
        return errorCode;
    }


    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }


    public String getStatusDescription() {
        return statusDescription;
    }


    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }



    public String getResourceInstanceID() {
        return resourceInstanceID;
    }



    public void setResourceInstanceID(String resourceInstanceID) {
        this.resourceInstanceID = resourceInstanceID;
    }


    public String getOperType() {
        return operType;
    }


    public void setOperType(String operType) {
        this.operType = operType;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ResourceOperationStatus)) {
            return false;
        }
        ResourceOperationStatus castOther = (ResourceOperationStatus) other;
        return Objects.equals(getServiceId(), castOther.getServiceId())
                && Objects.equals(getOperationId(), castOther.getOperationId())
                && Objects.equals(getResourceTemplateUUID(), castOther.getResourceTemplateUUID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServiceId(), getOperationId(), getResourceTemplateUUID());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("serviceId", getServiceId()).append("operationId", getOperationId())
                .append("resourceTemplateUUID", getResourceTemplateUUID()).append("operType", getOperType())
                .append("resourceInstanceID", getResourceInstanceID()).append("jobId", getJobId())
                .append("status", getStatus()).append("progress", getProgress()).append("errorCode", getErrorCode())
                .append("statusDescription", getStatusDescription()).toString();
    }
}
