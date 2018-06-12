package org.openecomp.mso.bpmn.infrastructure.workflow.tasks;

public class Resource {

	private String resourceId;
	private String resourceType;
	private boolean generated;
	
	public Resource(String resourceType, String resourceId, boolean generated){
		this.resourceId = resourceId;
		this.resourceType = resourceType;
		this.generated = generated;
	}
	
	public String getResourceId() {
		return resourceId;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	public String getResourceType() {
		return resourceType;
	}
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
	public boolean isGenerated() {
		return generated;
	}
	public void setGenerated(boolean generated) {
		this.generated = generated;
	}
	
}
