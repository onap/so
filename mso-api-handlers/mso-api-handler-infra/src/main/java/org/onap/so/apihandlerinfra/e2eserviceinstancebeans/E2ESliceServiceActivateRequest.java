package org.onap.so.apihandlerinfra.e2eserviceinstancebeans;

public class E2ESliceServiceActivateRequest {
    private String globalSubscriberId;

    private String serviceType;

    public String getGlobalSubscriberId() {
        return globalSubscriberId;
    }

    public void setGlobalSubscriberId(String globalSubscriberId) {
        this.globalSubscriberId = globalSubscriberId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
}
