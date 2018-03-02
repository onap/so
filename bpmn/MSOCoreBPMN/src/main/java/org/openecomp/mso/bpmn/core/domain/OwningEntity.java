package org.openecomp.mso.bpmn.core.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * This class is used to store instance
 * data of owningEntity for ServiceDecomposition
 *
 * @author bb3476
 *
 */
@JsonRootName("owningEntity")
public class OwningEntity extends JsonWrapper implements Serializable {

	private static final long serialVersionUID = 1L;
	private String owningEntityId;
	private String owningEntityName;
	public String getOwningEntityId() {
		return owningEntityId;
	}
	public void setOwningEntityId(String owningEntityId) {
		this.owningEntityId = owningEntityId;
	}
	public String getOwningEntityName() {
		return owningEntityName;
	}
	public void setOwningEntityName(String owningEntityName) {
		this.owningEntityName = owningEntityName;
	}
	
}