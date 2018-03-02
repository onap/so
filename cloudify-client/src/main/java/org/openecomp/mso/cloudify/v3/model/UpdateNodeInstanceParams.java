package org.openecomp.mso.cloudify.v3.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Map;

public class UpdateNodeInstanceParams implements Serializable {

	private static final long serialVersionUID = 1L;
	
    @JsonProperty("state")
    private String state;

    @JsonProperty("version")
    private String version;

    @JsonProperty("runtime_properties")
    private Map<String, Object> runtimeProperties;


    public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Map<String, Object> getRuntimeProperties() {
		return runtimeProperties;
	}

	public void setRuntimeProperties(Map<String, Object> runtimeProperties) {
		this.runtimeProperties = runtimeProperties;
	}


	@Override
    public String toString() {
        return "UpdateNodeInstanceParams{" +
                "state='" + state + '\'' +
                "version='" + version + '\'' +
                ", runtimeProperties=" + runtimeProperties +
                '}';
    }

}
