package org.onap.so.appc.orchestrator.service.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.onap.appc.client.lcm.model.Action;

public class ApplicationControllerTaskRequest implements Serializable {

    private static final long serialVersionUID = -3150320542857627682L;

    private Action action;
    private String requestorId;
    private String controllerType;
    private String identityUrl;
    private String operationsTimeout;
    private String bookName;
    private String nodeList;
    private String fileParameters;
    private String existingSoftwareVersion;
    private String newSoftwareVersion;
    private ApplicationControllerVnf applicationControllerVnf;
    private Map<String, String> configParams = new HashMap<String, String>();

    public Map<String, String> getConfigParams() {
        return configParams;
    }

    public void setConfigParams(Map<String, String> configParams) {
        this.configParams = configParams;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getControllerType() {
        return controllerType;
    }

    public void setControllerType(String controllerType) {
        this.controllerType = controllerType;
    }

    public ApplicationControllerVnf getApplicationControllerVnf() {
        return applicationControllerVnf;
    }

    public void setApplicationControllerVnf(ApplicationControllerVnf applicationControllerVnf) {
        this.applicationControllerVnf = applicationControllerVnf;
    }

    public String getIdentityUrl() {
        return identityUrl;
    }

    public void setIdentityUrl(String identityUrl) {
        this.identityUrl = identityUrl;
    }

    public String getOperationsTimeout() {
        return operationsTimeout;
    }

    public void setOperationsTimeout(String operationsTimeout) {
        this.operationsTimeout = operationsTimeout;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getNodeList() {
        return nodeList;
    }

    public void setNodeList(String nodeList) {
        this.nodeList = nodeList;
    }

    public String getFileParameters() {
        return fileParameters;
    }

    public void setFileParameters(String fileParameters) {
        this.fileParameters = fileParameters;
    }

    public String getExistingSoftwareVersion() {
        return existingSoftwareVersion;
    }

    public void setExistingSoftwareVersion(String existingSoftwareVersion) {
        this.existingSoftwareVersion = existingSoftwareVersion;
    }

    public String getNewSoftwareVersion() {
        return newSoftwareVersion;
    }

    public void setNewSoftwareVersion(String newSoftwareVersion) {
        this.newSoftwareVersion = newSoftwareVersion;
    }

    public String getRequestorId() {
        return requestorId;
    }

    public void setRequestorId(String requestorId) {
        this.requestorId = requestorId;
    }



}
