package org.openecomp.mso.db.catalog.beans;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.openecomp.mso.db.catalog.beans.macro.OrchestrationFlow;

import com.openpojo.business.annotation.BusinessKey;

@Entity
@Table(name = "building_block_detail")
public class BuildingBlockDetail implements Serializable {
	private static final long serialVersionUID = -2375223199178059155L;
	
	@BusinessKey
	@Id
	@Column(name = "id")
	@GeneratedValue
	private Integer id;
	
	@BusinessKey
	@Column(name = "BUILDING_BLOCK_NAME")
	private String buildingBlockName;
	
	@BusinessKey
	@Enumerated(EnumType.STRING)
	@Column(name = "RESOURCE_TYPE")
	private ResourceType resourceType;

	@BusinessKey
	@Enumerated(EnumType.STRING)
	@Column(name = "TARGET_ACTION")
	private OrchestrationAction targetAction;
	
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("buildingBlockName", buildingBlockName)
				.append("resourceType", resourceType).append("targetAction", targetAction).toString();		
	}
	
	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof BuildingBlockDetail)) {
			return false;
		}
		BuildingBlockDetail castOther = (BuildingBlockDetail) other;
		return new EqualsBuilder().append(buildingBlockName, castOther.buildingBlockName).isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(id).toHashCode();
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getBuildingBlockName() {
		return buildingBlockName;
	}

	public void setBuildingBlockName(String buildingBlockName) {
		this.buildingBlockName = buildingBlockName;
	}

	public ResourceType getResourceType() {
		return resourceType;
	}

	public void setResourceType(ResourceType resourceType) {
		this.resourceType = resourceType;
	}

	public OrchestrationAction getTargetAction() {
		return targetAction;
	}

	public void setTargetAction(OrchestrationAction targetAction) {
		this.targetAction = targetAction;
	}
}
