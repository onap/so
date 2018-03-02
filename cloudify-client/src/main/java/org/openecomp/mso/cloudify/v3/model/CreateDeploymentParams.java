package org.openecomp.mso.cloudify.v3.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Map;

public class CreateDeploymentParams implements Serializable {

	private static final long serialVersionUID = 1L;
	
    @JsonProperty("blueprint_id")
    private String blueprintId;

    @JsonProperty("inputs")
    private Map<String, Object> inputs;

	public String getBlueprintId() {
		return blueprintId;
	}

	public void setBlueprintId(String blueprintId) {
		this.blueprintId = blueprintId;
	}

	public Map<String, Object> getInputs() {
		return inputs;
	}

	public void setInputs(Map<String, Object> inputs) {
		this.inputs = inputs;
	}

    @Override
    public String toString() {
        return "CreateDeploymentBody{" +
                "blueprintId='" + blueprintId + '\'' +
                ", inputs=" + inputs +
                '}';
    }

}
