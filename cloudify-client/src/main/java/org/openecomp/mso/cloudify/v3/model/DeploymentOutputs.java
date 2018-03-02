package org.openecomp.mso.cloudify.v3.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown = true)
//@JsonRootName("outputs")
public class DeploymentOutputs implements Serializable {

	private static final long serialVersionUID = 1L;
	
    @JsonProperty("deployment_id")
    private String deploymentId;
    
    @JsonProperty("outputs")
    private Map<String, Object> outputs = null;
    

    // ObjectMapper instance to parse Json object outputs
    @JsonIgnore
	private static ObjectMapper mapper = new ObjectMapper();


    public Map<String, Object> getOutputs() {
    	return this.outputs;
    }
    public void setOutputs(Map<String, Object> outputs) {
    	this.outputs = outputs;
    }
    
	public String getDeploymentId() {
		return deploymentId;
	}
	public void setDeploymentId(String deploymentId) {
		this.deploymentId = deploymentId;
	}
	
	/*
	 * Return an  output as a Json-mapped Object of the provided type.
	 * This is useful for json-object outputs.
	 */
	public <T> T getMapValue (Map<String,Object> map, String key, Class<T> type)
	{
		if (map.containsKey(key)) {
			try {
				String s = mapper.writeValueAsString(map.get(key));
				return (mapper.readValue(s, type));
			}
			catch (IOException e) {
				return null;
			}
		}
		return null;
	}

	@Override
    public String toString() {
        return "DeploymentOutputs{" +
                "deploymentId='" + deploymentId + '\'' +
                ", outputs='" + outputs + '\'' +
                '}';
    }

}
