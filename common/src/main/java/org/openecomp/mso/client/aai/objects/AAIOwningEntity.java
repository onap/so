package org.openecomp.mso.client.aai.objects;

import org.openecomp.mso.client.aai.AAIObjectType;
import org.openecomp.mso.client.aai.entities.AAIEntityObject;
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri;
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AAIOwningEntity implements AAIEntityObject {
	
	@JsonProperty("owning-entity-name")
	private String owningEntityName;
	
	@JsonProperty("owning-entity-id")
	private String owningEntityId;
	
	public String getOwningEntityName() {
		return owningEntityName;
	}

	public void setOwningEntityName(String owningEntityName) {
		this.owningEntityName = owningEntityName;
	}
	
	public String getOwningEntityId() {
		return owningEntityId;
	}

	public void setOwningEntityId(String owningEntityId) {
		this.owningEntityId = owningEntityId;
	}
	
	public AAIOwningEntity withOwningEntity(String owningEntityName, String owningEntityId) {
		this.setOwningEntityName(owningEntityName);
		this.setOwningEntityId(owningEntityId);
		return this;
	}

	@Override
	public AAIResourceUri getUri() {
		final AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.OWNING_ENTITY, this.owningEntityId);
		return uri;
	}
	
	
}
