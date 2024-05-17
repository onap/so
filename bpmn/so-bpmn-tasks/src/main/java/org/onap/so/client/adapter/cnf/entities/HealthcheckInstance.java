package org.onap.so.client.adapter.cnf.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = "true")
public class HealthcheckInstance {

    public HealthcheckInstance() {}

    public HealthcheckInstance(String instanceId) {
        this.instanceId = instanceId;
    }

    @JsonProperty("instanceId")
    private String instanceId;

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public String toString() {
        return "InstanceRequest{" + "instanceId='" + instanceId + '\'' + '}';
    }
}

