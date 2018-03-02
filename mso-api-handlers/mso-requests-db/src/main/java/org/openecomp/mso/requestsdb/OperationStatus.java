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

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * The service operation status 
 * <br>
 * <p>
 * </p>
 * 
 * @author
 * @version     ONAP Amsterdam Release  2017-08-28
 */
public class OperationStatus implements Serializable{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String serviceId;
    
    private String operationId;
    
    private String operation;
    
    private String userId;
    
    private String result;
    
    private String operationContent;
    
    private String progress = "0";
    
    private String reason;

    private Timestamp operateAt;
    
    private Timestamp finishedAt;

    
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

    
    public Timestamp getOperateAt() {
        return operateAt;
    }

    
    public void setOperateAt(Timestamp operateAt) {
        this.operateAt = operateAt;
    }

    
    public Timestamp getFinishedAt() {
        return finishedAt;
    }

    
    public void setFinishedAt(Timestamp finishedAt) {
        this.finishedAt = finishedAt;
    }


    /**
     * <br>
     * 
     * @return
     * @since   ONAP Amsterdam Release 
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((operationId == null) ? 0 : operationId.hashCode());
        result = prime * result + ((serviceId == null) ? 0 : serviceId.hashCode());
        return result;
    }


    /**
     * <br>
     * 
     * @param obj
     * @return
     * @since   ONAP Amsterdam Release 
     */
    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        OperationStatus other = (OperationStatus)obj;
        if(operationId == null) {
            if(other.operationId != null)
                return false;
        } else if(!operationId.equals(other.operationId))
            return false;
        if(serviceId == null) {
            if(other.serviceId != null)
                return false;
        } else if(!serviceId.equals(other.serviceId))
            return false;
        return true;
    }


}
