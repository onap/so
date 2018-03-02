package org.openecomp.mso.cloudify.v3.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateExecutionParams implements Serializable {

	private static final long serialVersionUID = 1L;
	
    @JsonProperty("status")
    private String status;
    
    public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}


	@Override
    public String toString() {
        return "UpdateExecutionParams{" +
                "status='" + status + '\'' +
                '}';
    }

}
