package org.openecomp.mso.bpmn.core.domain;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonRootName;


@JsonRootName("configResource")
public class ConfigResource extends Resource {

	private static final long serialVersionUID = 1L;

	/*
	 * set resourceType for this object
	 */
	public ConfigResource(){
		resourceType = ResourceType.CONFIGURATION;
		setResourceId(UUID.randomUUID().toString());
	}

	/*
	 * fields specific to Config Resource resource type
	 */
	
	/*
	 * GET and SET
	 */

	private String toscaNodeType;

	public String getToscaNodeType() {
		return toscaNodeType;
	}

	public void setToscaNodeType(String toscaNodeType) {
		this.toscaNodeType = toscaNodeType;
	}
	

}