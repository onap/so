package org.onap.so.client.adapter.cnf.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = "true")
public class HealthcheckInstanceRequest {

    @JsonProperty("requestedInstances")
    private List<HealthcheckInstance> instances;

    @JsonProperty("callbackUrl")
    private String callbackUrl;

    public List<HealthcheckInstance> getInstances() {
        return instances;
    }

    public void setInstances(List<HealthcheckInstance> instances) {
        this.instances = instances;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    @Override
    public String toString() {
        return "CheckInstanceRequest{" + "instances=" + instances + ", callbackUrl='" + callbackUrl + '\'' + '}';
    }
}
