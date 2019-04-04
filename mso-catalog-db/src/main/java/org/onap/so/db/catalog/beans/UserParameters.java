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
@Table(name = "USER_PARAMETERS")
public class UserParameters implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5036895978102778877L;


	@Id
	@Column(name = "ID", nullable = false)
	private Integer ID;
	
	@BusinessKey
	@Column(name = "NAME")
	private String name;
	
	@Column(name = "PAYLOAD_LOCATION")
	private String payloadLocation;
	
	@Column(name = "LABEL")
	private String label;
	
	@Column(name = "TYPE")
	private String type;
	
	@Column(name = "DESCRIPTION")
	private String description;
	
	@Column(name = "IS_REQUIRED")
	private Boolean isRequried;
	
	@Column(name = "MAX_LENGTH")
	private Integer maxLength;
	
	@Column(name = "ALLOWABLE_CHARS")
	private String allowableChars;
	
	@Column(name = "CREATION_TIMESTAMP", updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "userParameters")
	private List<ActivitySpecUserParameters> activitySpecUserParameters;
	
	@PrePersist
	protected void onCreate() {
		this.created = new Date();
	}
	
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
	public List<ActivitySpecUserParameters> getActivitySpecUserParameters() {
		return activitySpecUserParameters;
	}

	public void setActivitySpecUserParameters(
			List<ActivitySpecUserParameters> activitySpecUserParameters) {
		this.activitySpecUserParameters = activitySpecUserParameters;
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("name", name)
										.append("ActivitySpecUserParameters", activitySpecUserParameters).toString();
	}
	public String getPayloadLocation() {
		return payloadLocation;
	}

	public void setPayloadLocation(String payloadLocation) {
		this.payloadLocation = payloadLocation;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getIsRequried() {
		return isRequried;
	}

	public void setIsRequried(Boolean isRequried) {
		this.isRequried = isRequried;
	}

	public Integer getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(Integer maxLength) {
		this.maxLength = maxLength;
	}

	public String getAllowableChars() {
		return allowableChars;
	}

	public void setAllowableChars(String allowableChars) {
		this.allowableChars = allowableChars;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof UserParameters)) {
			return false;
		}
		UserParameters castOther = (UserParameters) other;
		return new EqualsBuilder().append(name, castOther.name).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(name).toHashCode();
	}
}
