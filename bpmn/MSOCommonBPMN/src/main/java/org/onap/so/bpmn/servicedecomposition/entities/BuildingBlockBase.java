package org.onap.so.bpmn.servicedecomposition.entities;

import org.onap.so.serviceinstancebeans.RequestDetails;
import java.io.Serializable;

public class BuildingBlockBase implements Serializable {

    private static final long serialVersionUID = 3L;
    private Boolean aLaCarte;
    private String apiVersion;
    private Boolean isResume;
    private String resourceId;
    private String requestId;
    private String requestAction;
    private RequestDetails requestDetails;
    private WorkflowResourceIds workflowResourceIds;
    private String vnfType;

    public void setaLaCarte(Boolean aLaCarte) {
        this.aLaCarte = aLaCarte;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public void setResume(Boolean resume) {
        isResume = resume;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setRequestAction(String requestAction) {
        this.requestAction = requestAction;
    }

    public void setRequestDetails(RequestDetails requestDetails) {
        this.requestDetails = requestDetails;
    }

    public void setWorkflowResourceIds(WorkflowResourceIds workflowResourceIds) {
        this.workflowResourceIds = workflowResourceIds;
    }

    public void setVnfType(String vnfType) {
        this.vnfType = vnfType;
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
