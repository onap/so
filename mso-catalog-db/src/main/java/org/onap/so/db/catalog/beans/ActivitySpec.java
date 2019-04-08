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
@Table(name = "ACTIVITY_SPEC")
public class ActivitySpec implements Serializable {

	private static final long serialVersionUID = 6902290480087262973L;

	@Id
	@Column(name = "ID", nullable = false, updatable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer ID;
	
	@BusinessKey
	@Column(name = "NAME")
	private String name;
	
	@Column(name = "DESCRIPTION")
	private String description;
	
	@BusinessKey
	@Column(name = "VERSION")
	private Double version;
	
	@Column(name = "CREATION_TIMESTAMP", updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "activitySpec")
	private List<WorkflowActivitySpecSequence> workflowActivitySpecSequence;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "activitySpec")
	private List<ActivitySpecActivitySpecCategories> activitySpecActivitySpecCategories;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "activitySpec")
	private List<ActivitySpecUserParameters> activitySpecUserParameters;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "activitySpec")
	private List<ActivitySpecActivitySpecParameters> activitySpecActivitySpecParameters;
	
	@PrePersist
	protected void onCreate() {
		this.created = new Date();
	}

	public Integer getID() {
		return ID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	public Double getVersion() {
		return version;
	}

	public void setVersion(Double version) {
		this.version = version;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	@LinkedResource
	public List<WorkflowActivitySpecSequence> getWorkflowActivitySpecSequence() {
		return workflowActivitySpecSequence;
	}

	public void setWorkflowActivitySpecSequence(
			List<WorkflowActivitySpecSequence> workflowActivitySpecSequence) {
		this.workflowActivitySpecSequence = workflowActivitySpecSequence;
	}
	
	@LinkedResource
	public List<ActivitySpecUserParameters> getActivitySpecUserParameters() {
		return activitySpecUserParameters;
	}

	public void setActivitySpecUserParameters(
			List<ActivitySpecUserParameters> activitySpecUserParameters) {
		this.activitySpecUserParameters = activitySpecUserParameters;
	}
	
	@LinkedResource
	public List<ActivitySpecActivitySpecCategories> getActivitySpecActivitySpecCategories() {
		return activitySpecActivitySpecCategories;
	}

	public void setActivitySpecActivitySpecCategories(
			List<ActivitySpecActivitySpecCategories> activitySpecActivitySpecCategories) {
		this.activitySpecActivitySpecCategories = activitySpecActivitySpecCategories;
	}
	
	@LinkedResource
	public List<ActivitySpecActivitySpecParameters> getActivitySpecActivitySpecParameters() {
		return activitySpecActivitySpecParameters;
	}

	public void setActivitySpecActivitySpecParameters(
			List<ActivitySpecActivitySpecParameters> activitySpecActivitySpecParameters) {
		this.activitySpecActivitySpecParameters = activitySpecActivitySpecParameters;
	}
	
	
	
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("name", name)
				.append("description", description).append("version", version)
				.append("created", created).append("workflowActivitySpecSequence", workflowActivitySpecSequence)
				.append("activitySpecActivitySpecCategories", activitySpecActivitySpecCategories).toString();
	}
	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof ActivitySpec)) {
			return false;
		}
		ActivitySpec castOther = (ActivitySpec) other;
		return new EqualsBuilder().append(name, castOther.name).append(version, castOther.version).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(name).append(version).toHashCode();
	}
}
