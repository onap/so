package org.onap.so.bpmn.core.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;


public class OperationStatus implements Serializable {



    /**
    * 
    */
    private static final long serialVersionUID = 5182494454360263137L;
    private String serviceId;
    private String operationId;
    private String serviceName;
    private String operation;
    private String userId;
    private String result;
    private String operationContent;
    private String progress = "0";
    private String reason;
    private String accessServiceId;
    private Date operateAt;
    private Date finishedAt;
    private String syncStatus;

    public OperationStatus() {

    }

    public OperationStatus(String serviceId, String operationId, String operation, String userId, String result,
            String progress, String reason, String accessServiceId) {
        super();
        this.serviceId = serviceId;
        this.operationId = operationId;
        this.operation = operation;
        this.userId = userId;
        this.result = result;
        this.progress = progress;
        this.reason = reason;
        this.accessServiceId = accessServiceId;
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

    public String getAccessServiceId() {
        return accessServiceId;
    }

    public void setAccessServiceId(String accessServiceId) {
        this.accessServiceId = accessServiceId;
    }

    public Date getOperateAt() {
        return operateAt;
    }

    public Date getFinishedAt() {
        return finishedAt;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
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


}
