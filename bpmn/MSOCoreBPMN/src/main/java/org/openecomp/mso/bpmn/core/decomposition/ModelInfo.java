package org.openecomp.mso.bpmn.core.decomposition;

import java.io.Serializable;

import org.codehaus.jackson.map.annotate.JsonRootName;

@JsonRootName("modelInfo")
public class ModelInfo  extends JsonWrapper implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String modelName = "";
	private String modelUuid = "";
	private String modelInvariantId = "";
	private String modelVersion = "";
	//additionally on resource level
	private String modelCustomizationUuid = "";
	private String modelInstanceName = "";
	
	//TODO - those were present in original "modelInfo" object structure. Confirm.
	private String modelCustomizationName = "";
	private String modelVersionId = "";
	private String modelType = "";
	
	public String getModelName() {
		return modelName;
	}
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
	public String getModelUuid() {
		return modelUuid;
	}
	public void setModelUuid(String modelUuid) {
		this.modelUuid = modelUuid;
	}
	public String getModelInvariantId() {
		return modelInvariantId;
	}
	public void setModelInvariantId(String modelInvariantId) {
		this.modelInvariantId = modelInvariantId;
	}
	public String getModelVersion() {
		return modelVersion;
	}
	public void setModelVersion(String modelVersion) {
		this.modelVersion = modelVersion;
	}
	public String getModelCustomizationUuid() {
		return modelCustomizationUuid;
	}
	public void setModelCustomizationUuid(String modelCustomizationUuid) {
		this.modelCustomizationUuid = modelCustomizationUuid;
	}
	public String getModelInstanceName() {
		return modelInstanceName;
	}
	public void setModelInstanceName(String modelInstanceName) {
		this.modelInstanceName = modelInstanceName;
	}
	public String getModelCustomizationName() {
		return modelCustomizationName;
	}
	public void setModelCustomizationName(String modelCustomizationName) {
		this.modelCustomizationName = modelCustomizationName;
	}
	public String getModelVersionId() {
		return modelVersionId;
	}
	public void setModelVersionId(String modelVersionId) {
		this.modelVersionId = modelVersionId;
	}
	public String getModelType() {
		return modelType;
	}
	public void setModelType(String modelType) {
		this.modelType = modelType;
	}
	
	//TODO add convenience methods
	
	
	//TODO - complete this manual
	public String toString(){

		String jsonString = "";
		
		//can try building manually
		jsonString = "{" +
				"\"modelName\":\"" + getModelName() + "\"," +
				"\"modelUuid\":\"" + getModelUuid() + "\"" +
				"\"modelInvariantId\":\"" + getModelInvariantId() + "\"" +
				"}";
		

		return jsonString;
	}	
}
