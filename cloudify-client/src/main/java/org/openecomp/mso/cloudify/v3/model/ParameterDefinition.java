package org.openecomp.mso.cloudify.v3.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ParameterDefinition implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@JsonProperty("type")
	private String type;
	@JsonProperty("description")
	private String description;
	@JsonProperty("default")
	private Object defaultValue;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Object getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}
}
