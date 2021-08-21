package org.onap.so.client.adapter.cnf.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = "true")
public class StatusCheckResponse {

    @JsonProperty("result")
    private List<StatusCheckInstanceResponse> instanceResponse;

    public List<StatusCheckInstanceResponse> getInstanceResponse() {
        return instanceResponse;
    }

    public void setInstanceResponse(List<StatusCheckInstanceResponse> instanceResponse) {
        this.instanceResponse = instanceResponse;
    }

    @Override
    public String toString() {
        return "StatusCheckResponse{" + "instanceResponse=" + instanceResponse + '}';
    }

}
