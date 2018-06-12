package org.openecomp.mso.bpmn.servicedecomposition.bbobjects;

import java.io.Serializable;

import javax.persistence.Id;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openecomp.mso.db.catalog.beans.OrchestrationStatus;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.openecomp.mso.bpmn.servicedecomposition.ShallowCopy;

@JsonRootName("pnf")
public class Pnf implements Serializable, ShallowCopy<Pnf> {

	private static final long serialVersionUID = -2544848120774529501L;

	@Id
    @JsonProperty("pnf-id")
    private String pnfId;

    @JsonProperty("pnf-name")
    private String pnfName;

    @JsonProperty("role")
    private String role;

	@JsonProperty("orchestration-status")
	private OrchestrationStatus orchestrationStatus;

    @JsonProperty("cloud-region")
    private CloudRegion cloudRegion;


	public String getPnfId() {
		return pnfId;
	}

	public void setPnfId(String pnfId) {
		this.pnfId = pnfId;
	}

	public String getPnfName() {
		return pnfName;
	}

	public void setPnfName(String pnfName) {
		this.pnfName = pnfName;
	}

	/**
	 * Distinguishes Primary or Secondary
	 */
	public String getRole() {
		return role;
	}

	/**
	 * Distinguishes Primary or Secondary
	 */
	public void setRole(String role) {
		this.role = role;
	}

	public OrchestrationStatus getOrchestrationStatus() {
		return orchestrationStatus;
	}

	public void setOrchestrationStatus(OrchestrationStatus orchestrationStatus) {
		this.orchestrationStatus = orchestrationStatus;
	}

	public CloudRegion getCloudRegion() {
		return cloudRegion;
	}

	public void setCloudRegion(CloudRegion cloudRegion) {
		this.cloudRegion = cloudRegion;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof Pnf)) {
			return false;
		}
		Pnf castOther = (Pnf) other;
		return new EqualsBuilder().append(pnfId, castOther.pnfId).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(pnfId).toHashCode();
	}
}
