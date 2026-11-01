package org.onap.so.adapters.sdncrest;

public class SdncRestCallback {
    private String requestId;
    private String responseCode;
    private String responseMessage;
    private String requestDataXml;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getRequestDataXml() {
        return requestDataXml;
    }

    public void setRequestDataXml(String requestDataXml) {
        this.requestDataXml = requestDataXml;
    }
}
