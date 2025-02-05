package org.onap.so.db.catalog.beans;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.openpojo.business.annotation.BusinessKey;

@Entity
@Table(name = "activity_spec")
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

    public List<WorkflowActivitySpecSequence> getWorkflowActivitySpecSequence() {
        return workflowActivitySpecSequence;
    }

    public void setWorkflowActivitySpecSequence(List<WorkflowActivitySpecSequence> workflowActivitySpecSequence) {
        this.workflowActivitySpecSequence = workflowActivitySpecSequence;
    }

    public List<ActivitySpecUserParameters> getActivitySpecUserParameters() {
        return activitySpecUserParameters;
    }

    public void setActivitySpecUserParameters(List<ActivitySpecUserParameters> activitySpecUserParameters) {
        this.activitySpecUserParameters = activitySpecUserParameters;
    }

    public List<ActivitySpecActivitySpecCategories> getActivitySpecActivitySpecCategories() {
        return activitySpecActivitySpecCategories;
    }

    public void setActivitySpecActivitySpecCategories(
            List<ActivitySpecActivitySpecCategories> activitySpecActivitySpecCategories) {
        this.activitySpecActivitySpecCategories = activitySpecActivitySpecCategories;
    }

    public List<ActivitySpecActivitySpecParameters> getActivitySpecActivitySpecParameters() {
        return activitySpecActivitySpecParameters;
    }

    public void setActivitySpecActivitySpecParameters(
            List<ActivitySpecActivitySpecParameters> activitySpecActivitySpecParameters) {
        this.activitySpecActivitySpecParameters = activitySpecActivitySpecParameters;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).append("description", description)
                .append("version", version).append("created", created)
                .append("workflowActivitySpecSequence", workflowActivitySpecSequence)
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
