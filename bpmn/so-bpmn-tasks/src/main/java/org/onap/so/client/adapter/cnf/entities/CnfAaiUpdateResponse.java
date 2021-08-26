package org.onap.so.client.adapter.cnf.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CnfAaiUpdateResponse {

    @JsonProperty("status")
    private String status;

    @JsonProperty("statusMessage")
    private String statusMessage;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    @Override
    public String toString() {
        return "CnfAaiUpdateResponse [status=" + status + ", statusMessage=" + statusMessage + "]";
    }

}
