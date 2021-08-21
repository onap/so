package org.onap.so.client.adapter.cnf.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = "true")
public class HealthCheckErrorResponse {

    @JsonProperty("error")
    private String errorResponse;

    public String getErrorResponse() {
        return errorResponse;
    }

    public void setErrorResponse(String errorResponse) {
        this.errorResponse = errorResponse;
    }

    @Override
    public String toString() {
        return "HealthCheckErrorResponse [errorResponse=" + errorResponse + "]";
    }

}
