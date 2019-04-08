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
import javax.persistence.IdClass;
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
@Table(name = "ACTIVITY_SPEC_PARAMETERS")
public class ActivitySpecParameters implements Serializable {
	
	private static final long serialVersionUID = 3627711377147710046L;

	@Id
	@Column(name = "ID", nullable = false, updatable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer ID;
	
	@BusinessKey
	@Column(name = "NAME")
	private String name;
	
	@Column(name = "TYPE")
	private String type;
	
	@BusinessKey
	@Column(name = "DIRECTION")
	private String direction;
	
	@Column(name = "DESCRIPTION")
	private String description;
	
	@Column(name = "CREATION_TIMESTAMP", updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "activitySpecParameters")
	private List<ActivitySpecActivitySpecParameters> activitySpecActivitySpecParameters;
	
	@PrePersist
	protected void onCreate() {
		this.created = new Date();
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
		return new ToStringBuilder(this).append("name", name).append("direction", direction)
										.append("activitySpecActivitySpecParameters", activitySpecActivitySpecParameters).toString();
	}
	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof ActivitySpecParameters)) {
			return false;
		}
		ActivitySpecParameters castOther = (ActivitySpecParameters) other;
		return new EqualsBuilder().append(ID, castOther.ID).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(ID).toHashCode();
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}
}
