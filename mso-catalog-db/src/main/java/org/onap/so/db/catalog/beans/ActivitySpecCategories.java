package org.onap.so.db.catalog.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
@Table(name = "ACTIVITY_SPEC_CATEGORIES")
public class ActivitySpecCategories implements Serializable {

	private static final long serialVersionUID = -6251150462067699643L;

	@Id
	@Column(name = "ID", nullable = false)
	private Integer ID;
	
	@BusinessKey
	@Column(name = "NAME")
	private String name;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "activitySpecCategories")
	private List<ActivitySpecActivitySpecCategories> activitySpecActivitySpecCategories;
	

	public Integer getID() {
		return ID;
	}

	public void setID(Integer iD) {
		ID = iD;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@LinkedResource
	public List<ActivitySpecActivitySpecCategories> getActivitySpecActivitySpecCategories() {
		return activitySpecActivitySpecCategories;
	}

	public void setActivitySpecActivitySpecCategories(
			List<ActivitySpecActivitySpecCategories> activitySpecActivitySpecCategories) {
		this.activitySpecActivitySpecCategories = activitySpecActivitySpecCategories;
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("name", name)
										.append("activitySpecActivitySpecCategories", activitySpecActivitySpecCategories).toString();
	}
	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof ActivitySpecCategories)) {
			return false;
		}
		ActivitySpecCategories castOther = (ActivitySpecCategories) other;
		return new EqualsBuilder().append(name, castOther.name).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(name).toHashCode();
	}
}
