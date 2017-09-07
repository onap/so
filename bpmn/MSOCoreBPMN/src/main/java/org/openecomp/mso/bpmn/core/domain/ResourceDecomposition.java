package org.openecomp.mso.bpmn.core.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Abstract superclass for all individual decomposition resources
 * 
 */
//@JsonIgnoreProperties
public abstract class ResourceDecomposition extends JsonWrapper  implements Serializable {

	private static final long serialVersionUID = 1L;
	
	protected String resourceType; // Enum of vnf or network or allotted resource
	private ModelInfo modelInfo;

	//private List modules;
	private ResourceInstance instanceData = new ResourceInstance();
	
	// GET and SET
	public ModelInfo getModelInfo() {
		return modelInfo;
	}
	public void setModelInfo(ModelInfo modelInfo) {
		this.modelInfo = modelInfo;
	}

	public ResourceInstance getInstanceData() {
		return instanceData;
	}
	public void setInstanceData(ResourceInstance instanceData) {
		this.instanceData = instanceData;
	}
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	//Utility methods
	@JsonIgnore
	public ModelInfo getResourceModel() {
		return modelInfo;
	}
	@JsonIgnore
	public String getResourceInstanceId() {
		return this.getInstanceData().getInstanceId();
	}
	@JsonIgnore
	public String getResourceInstanceName() {
		return this.getInstanceData().getInstanceName();
	}
//	@JsonIgnore
//	public String getResourceHomingSolution() {
//	}
	
	public void setResourceInstanceId(String newInstanceId){
		this.getInstanceData().setInstanceId(newInstanceId);
	}
	public void setResourceInstanceName(String newInstanceName){
		this.getInstanceData().setInstanceName(newInstanceName);
	}
//	@JsonIgnore
//	public String setResourceHomingSolution() {
//	}
}
