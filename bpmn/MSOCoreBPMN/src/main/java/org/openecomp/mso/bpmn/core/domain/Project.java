package org.openecomp.mso.bpmn.core.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * This class is used to store instance
 * data of projects for ServiceDecomposition
 *
 * @author bb3476
 *
 */
@JsonRootName("project")
public class Project extends JsonWrapper implements Serializable {

	private static final long serialVersionUID = 1L;
	private String projectName;

	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
}