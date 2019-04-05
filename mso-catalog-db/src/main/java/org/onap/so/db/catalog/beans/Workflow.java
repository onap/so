package org.onap.so.db.catalog.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.openpojo.business.annotation.BusinessKey;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import uk.co.blackpepper.bowman.annotation.LinkedResource;

@Entity
@Table(name = "workflow")
public class Workflow implements Serializable {

	private static final long serialVersionUID = 1485794141983033264L;

	@Id
	@Column(name = "ID", nullable = false, updatable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer ID;
	
	@Column(name = "ARTIFACT_UUID")
	private String artifactUUID;
	
	@Column(name = "ARTIFACT_NAME")
	private String artifactName;
	
	@Column(name = "NAME")
	private String name;
	
	@Column(name = "OPERATION_NAME")
	private String operationName;
	
	@Column(name = "VERSION")
	private Double version;
	
	@Column(name = "DESCRIPTION")
	private String description;
	
	@Lob
	@Column(name = "BODY", columnDefinition = "LONGTEXT")
	private String body = null;
	
	@Column(name = "RESOURCE_TARGET")
	private String resourceTarget;
	
	@Column(name = "SOURCE")
	private String source;
	
	@Column(name = "TIMEOUT_MINUTES")
	private Integer timeoutMinutes;
	
	@Column(name = "ARTIFACT_CHECKSUM")
	private String artifactChecksum;
	
	@Column(name = "CREATION_TIMESTAMP", updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "workflow")
	private List<VnfResourceWorkflow> vnfResourceWorkflow;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "workflow")
	private List<WorkflowActivitySpecSequence> workflowActivitySpecSequence;
	
	@PrePersist
	protected void onCreate() {
		this.created = new Date();
	}

	public Integer getID() {
		return ID;
	}

	public String getArtifactUUID() {
		return artifactUUID;
	}

	public void setArtifactUUID(String artifactUUID) {
		this.artifactUUID = artifactUUID;
	}

	public String getArtifactName() {
		return artifactName;
	}

	public void setArtifactName(String artifactName) {
		this.artifactName = artifactName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public Double getVersion() {
		return version;
	}

	public void setVersion(Double version) {
		this.version = version;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getResourceTarget() {
		return resourceTarget;
	}

	public void setResourceTarget(String resourceTarget) {
		this.resourceTarget = resourceTarget;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Integer getTimeoutMinutes() {
		return timeoutMinutes;
	}

	public void setTimeoutMinutes(Integer timeoutMinutes) {
		this.timeoutMinutes = timeoutMinutes;
	}

	public String getArtifactChecksum() {
		return artifactChecksum;
	}

	public void setArtifactChecksum(String artifactChecksum) {
		this.artifactChecksum = artifactChecksum;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	@LinkedResource
	public List<VnfResourceWorkflow> getVnfResourceWorkflow() {
		return vnfResourceWorkflow;
	}

	public void setVnfResourceWorkflow(
			List<VnfResourceWorkflow> vnfResourceWorkflow) {
		this.vnfResourceWorkflow = vnfResourceWorkflow;
	}
	
	@LinkedResource
	public List<WorkflowActivitySpecSequence> getWorkflowActivitySpecSequence() {
		return workflowActivitySpecSequence;
	}

	public void setWorkflowActivitySpecSequence(
			List<WorkflowActivitySpecSequence> workflowActivitySpecSequence) {
		this.workflowActivitySpecSequence = workflowActivitySpecSequence;
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("ID", ID).append("artifactUUID", artifactUUID)
				.append("artifactName", artifactName).append("name", name)
				.append("operationName", operationName).append("version", version).append("description", description)
				.append("body",body).append("resourceTarget",resourceTarget).append("source", source)
				.append("timeoutMinutes", timeoutMinutes).append("artifactChecksum", artifactChecksum)
				.append("created", created).append("vnfResourceWorkflow", vnfResourceWorkflow)
				.append("WorkflowActivitySpecSequence",workflowActivitySpecSequence).toString();
	}
	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof Workflow)) {
			return false;
		}
		Workflow castOther = (Workflow) other;
		return new EqualsBuilder().append(ID, castOther.ID).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(ID).toHashCode();
	}
}
