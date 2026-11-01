package org.onap.so.adapters.sdncrest;

public class SdncRestRequest {
    private String requestId;
    private String serviceInstanceId;
    private String msoAction;
    private String svcAction;
    private String svcOperation;
    private String callbackUrl;
    private String requestDataXml;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    public String getMsoAction() {
        return msoAction;
    }

    public void setMsoAction(String msoAction) {
        this.msoAction = msoAction;
    }

    public String getSvcAction() {
        return svcAction;
    }

    public void setSvcAction(String svcAction) {
        this.svcAction = svcAction;
    }

    public String getSvcOperation() {
        return svcOperation;
    }

    public void setSvcOperation(String svcOperation) {
        this.svcOperation = svcOperation;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getRequestDataXml() {
        return requestDataXml;
    }

    public void setRequestDataXml(String requestDataXml) {
        this.requestDataXml = requestDataXml;
    }
}
