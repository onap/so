package org.onap.so.db.request.data.controller;

import java.util.HashMap;

public class InstanceNameDuplicateCheckRequest {

    private HashMap<String, String> instanceIdMap;
    private String instanceName;
    private String requestScope;

    public InstanceNameDuplicateCheckRequest() {
    }

    public InstanceNameDuplicateCheckRequest(HashMap<String, String> instanceIdMap, String instanceName, String requestScope) {
        this.instanceIdMap = instanceIdMap;
        this.instanceName = instanceName;
        this.requestScope = requestScope;
    }

    public HashMap<String, String> getInstanceIdMap() {
        return instanceIdMap;
    }

    public void setInstanceIdMap(HashMap<String, String> instanceIdMap) {
        this.instanceIdMap = instanceIdMap;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getRequestScope() {
        return requestScope;
    }

    public void setRequestScope(String requestScope) {
        this.requestScope = requestScope;
    }
}
