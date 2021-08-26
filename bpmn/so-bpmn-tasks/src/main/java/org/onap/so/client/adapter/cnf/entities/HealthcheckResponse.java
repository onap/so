package org.onap.so.client.adapter.cnf.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = "true")
public class HealthcheckResponse {

    @JsonProperty("result")
    private List<HealthcheckInstanceResponse> instanceResponse;

    public List<HealthcheckInstanceResponse> getInstanceResponse() {
        return instanceResponse;
    }

    public void setInstanceResponse(List<HealthcheckInstanceResponse> instanceResponse) {
        this.instanceResponse = instanceResponse;
    }

    @Override
    public String toString() {
        return "HealthcheckResponse{" + "instanceResponse=" + instanceResponse + '}';
    }

}
