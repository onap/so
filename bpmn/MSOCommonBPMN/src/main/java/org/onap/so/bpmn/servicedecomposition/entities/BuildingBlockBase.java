package org.onap.so.bpmn.servicedecomposition.entities;

import java.io.Serializable;
import org.onap.so.serviceinstancebeans.RequestDetails;

public abstract class BuildingBlockBase<T extends BuildingBlockBase<T>> implements Serializable {

    private static final long serialVersionUID = 4671883098039479717L;

    private Boolean aLaCarte;
    private String apiVersion;
    private Boolean isResume;
    private String resourceId;
    private String requestId;
    private String requestAction;
    private RequestDetails requestDetails;
    private WorkflowResourceIds workflowResourceIds;
    private String vnfType;

    public T setaLaCarte(Boolean aLaCarte) {
        this.aLaCarte = aLaCarte;
        return (T) this;
    }

    public T setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return (T) this;
    }

    public T setResume(Boolean resume) {
        isResume = resume;
        return (T) this;
    }

    public T setResourceId(String resourceId) {
        this.resourceId = resourceId;
        return (T) this;
    }

    public T setRequestId(String requestId) {
        this.requestId = requestId;
        return (T) this;
    }

    public T setRequestAction(String requestAction) {
        this.requestAction = requestAction;
        return (T) this;
    }

    public T setRequestDetails(RequestDetails requestDetails) {
        this.requestDetails = requestDetails;
        return (T) this;
    }

    public T setWorkflowResourceIds(WorkflowResourceIds workflowResourceIds) {
        this.workflowResourceIds = workflowResourceIds;
        return (T) this;
    }

    public T setVnfType(String vnfType) {
        this.vnfType = vnfType;
        return (T) this;
    }

    public Boolean isResume() {
        return isResume;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getRequestAction() {
        return requestAction;
    }

    public Boolean isaLaCarte() {
        return aLaCarte;
    }

    public String getVnfType() {
        return vnfType;
    }

    public WorkflowResourceIds getWorkflowResourceIds() {
        return workflowResourceIds;
    }

    public RequestDetails getRequestDetails() {
        return requestDetails;
    }

}
