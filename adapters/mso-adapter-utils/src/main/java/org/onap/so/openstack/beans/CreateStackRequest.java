package org.onap.so.openstack.beans;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateStackRequest {

    @JsonProperty("parameters")
    private Map<String, Object> parameters;

    @JsonProperty("environment")
    private String environment;

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    @Override
    public String toString() {
        return "CreateStackRequest [parameters=" + parameters + ", environment=" + environment + "]";
    }


}
