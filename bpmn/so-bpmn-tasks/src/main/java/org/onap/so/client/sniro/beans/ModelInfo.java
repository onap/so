package org.onap.so.client.sniro.beans;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonPropertyOrder({
    "modelName",
    "modelVersionId",
    "modelVersion",
    "modelInvariantId"
})
@JsonRootName("modelInfo")
public class ModelInfo implements Serializable{

	private static final long serialVersionUID = 1488642558601651075L;

	@JsonProperty("modelInvariantId")
	private String modelInvariantId;
	@JsonProperty("modelVersionId")
	private String modelVersionId;
	@JsonProperty("modelName")
	private String modelName;
	@JsonProperty("modelVersion")
	private String modelVersion;


	public String getModelInvariantId(){
		return modelInvariantId;
	}

	public void setModelInvariantId(String modelInvariantId){
		this.modelInvariantId = modelInvariantId;
	}

	public String getModelVersionId(){
		return modelVersionId;
	}

	public void setModelVersionId(String modelVersionId){
		this.modelVersionId = modelVersionId;
	}

	public String getModelName(){
		return modelName;
	}

	public void setModelName(String modelName){
		this.modelName = modelName;
	}

	public String getModelVersion(){
		return modelVersion;
	}

	public void setModelVersion(String modelVersion){
		this.modelVersion = modelVersion;
	}

}
