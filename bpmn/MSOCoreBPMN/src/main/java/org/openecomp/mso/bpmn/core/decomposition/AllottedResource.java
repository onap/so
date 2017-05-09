package org.openecomp.mso.bpmn.core.decomposition;

import org.codehaus.jackson.map.annotate.JsonRootName;

@JsonRootName("allottedResource")
public class AllottedResource extends ResourceDecomposition {

	private static final long serialVersionUID = 1L;

	/*
	 * set resourceType for this object
	 */
	public AllottedResource(){
		resourceType = "allottedResource";
	}

	/*
	 * fields specific to VNF resource type
	 */
	private String allottedResourceType;
	private String allottedResourceRole;
	
	/*
	 * GET and SET
	 */
	public String getAllottedResourceType() {
		return allottedResourceType;
	}
	public void setAllottedResourceType(String allottedResourceType) {
		this.allottedResourceType = allottedResourceType;
	}
	public String getAllottedResourceRole() {
		return allottedResourceRole;
	}
	public void setAllottedResourceRole(String allottedResourceRole) {
		this.allottedResourceRole = allottedResourceRole;
	}
}
