package org.openecomp.mso.bpmn.servicedecomposition.modelinfo;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ModelInfoMetadata implements Serializable{

	private static final long serialVersionUID = -2182850364281359289L;

	@JsonProperty("model-customization-uuid")
	private String modelCustomizationUuid;
	@JsonProperty("model-invariant-uuid")
	private String modelInvariantUuid;
	@JsonProperty("model-uuid")
	private String modelUuid;
	@JsonProperty("model-version")
	private String modelVersion;
	@JsonProperty("model-instance-name")
	private String modelInstanceName;
	@JsonProperty("model-name")
	private String modelName;


	public String getModelCustomizationUuid() {
		return modelCustomizationUuid;
	}
	public void setModelCustomizationUuid(String modelCustomizationUuid) {
		this.modelCustomizationUuid = modelCustomizationUuid;
	}
	public String getModelInvariantUuid() {
		return modelInvariantUuid;
	}
	public void setModelInvariantUuid(String modelInvariantUuid) {
		this.modelInvariantUuid = modelInvariantUuid;
	}
	public String getModelUuid() {
		return modelUuid;
	}
	public void setModelUuid(String modelUuid) {
		this.modelUuid = modelUuid;
	}
	public String getModelVersion() {
		return modelVersion;
	}
	public void setModelVersion(String modelVersion) {
		this.modelVersion = modelVersion;
	}
	public String getModelInstanceName() {
		return modelInstanceName;
	}
	public void setModelInstanceName(String modelInstanceName) {
		this.modelInstanceName = modelInstanceName;
	}
	public String getModelName() {
		return modelName;
	}
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}


}
