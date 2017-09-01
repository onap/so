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
package org.openecomp.mso.requestsdb;

/**
 * The Resource operation status
 * <br>
 * <p>
 * </p>
 * 
 * @author
 * @version     ONAP Amsterdam Release  2017-08-28
 */
public class ResourceOperationStatus {

    private String serviceId;
    
    private String operationId;
    
    private String resourceTemplateUUID;
    
    private String operType;
    
    private String resourceInstanceID;
    
    private String jobId;
    
    private String status;
    
    private String progress = "0";
    
    private String errorCode;
    
    private String statusDescription;

    public ResourceOperationStatus(){
        
    }
    
    public ResourceOperationStatus(String serviceId, String operationId, String resourceTemplateUUID)
    {
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
    
    
}
