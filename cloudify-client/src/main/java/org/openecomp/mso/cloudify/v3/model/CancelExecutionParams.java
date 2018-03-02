package org.openecomp.mso.cloudify.v3.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CancelExecutionParams implements Serializable {

	private static final long serialVersionUID = 1L;
	
    @JsonProperty("action")
    private String action;
    
    public final static String CANCEL_ACTION = "cancel";
    public final static String FORCE_CANCEL_ACTION = "force-cancel";
    
    public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}


	@Override
    public String toString() {
        return "CancelExecutionParams{" +
                "action='" + action + '\'' +
                '}';
    }

}
