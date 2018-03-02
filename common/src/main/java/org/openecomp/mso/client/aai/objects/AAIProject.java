package org.openecomp.mso.client.aai.objects;

import org.openecomp.mso.client.aai.AAIObjectType;
import org.openecomp.mso.client.aai.entities.AAIEntityObject;
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri;
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AAIProject implements AAIEntityObject {
	
	@JsonProperty("project-name")
	private String projectName;
	
	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
	public AAIProject withProjectName(String projectName) {
		this.setProjectName(projectName);
		return this;
	}

	@Override
	public AAIResourceUri getUri() {
		final AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.PROJECT, this.projectName);
		return uri;
	}
	
	
}
