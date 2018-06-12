package org.openecomp.mso.db.catalog.beans;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.openpojo.business.annotation.BusinessKey;

@Entity
@Table(name = "orchestration_status_state_transition_directive")
public class OrchestrationStatusStateTransitionDirective implements Serializable {
	private static final long serialVersionUID = -4626396955833442376L;

	@BusinessKey
	@Id
	@Column(name = "id")
	@GeneratedValue
	private Integer id;

	@Enumerated(EnumType.STRING)
	@Column(name = "RESOURCE_TYPE")
	private ResourceType resourceType;

	@Enumerated(EnumType.STRING)
	@Column(name = "ORCHESTRATION_STATUS")
	private OrchestrationStatus orchestrationStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "TARGET_ACTION")
	private OrchestrationAction targetAction;

	@Enumerated(EnumType.STRING)
	@Column(name = "FLOW_DIRECTIVE")
	private OrchestrationStatusValidationDirective flowDirective;

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", id).append("resourceType", resourceType)
				.append("orchestrationStatus", orchestrationStatus).append("targetAction", targetAction)
				.append("flowDirective", flowDirective).toString();
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof OrchestrationStatusStateTransitionDirective)) {
			return false;
		}
		OrchestrationStatusStateTransitionDirective castOther = (OrchestrationStatusStateTransitionDirective) other;
		return new EqualsBuilder().append(id, castOther.id).isEquals();
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

	public ResourceType getResourceType() {
		return resourceType;
	}

	public void setResourceType(ResourceType resourceType) {
		this.resourceType = resourceType;
	}

	public OrchestrationStatus getOrchestrationStatus() {
		return orchestrationStatus;
	}

	public void setOrchestrationStatus(OrchestrationStatus orchestrationStatus) {
		this.orchestrationStatus = orchestrationStatus;
	}

	public OrchestrationAction getTargetAction() {
		return targetAction;
	}

	public void setTargetAction(OrchestrationAction targetAction) {
		this.targetAction = targetAction;
	}

	public OrchestrationStatusValidationDirective getFlowDirective() {
		return flowDirective;
	}

	public void setFlowDirective(OrchestrationStatusValidationDirective flowDirective) {
		this.flowDirective = flowDirective;
	}
}
