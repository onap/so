package org.onap.so.db.catalog.beans;

import java.io.Serializable;
import java.util.Date;
/*
 * import javax.persistence.Column; import javax.persistence.Entity; import javax.persistence.GeneratedValue; import
 * javax.persistence.GenerationType; import javax.persistence.Id; import javax.persistence.PrePersist; import
 * javax.persistence.Table; import javax.persistence.Temporal; import javax.persistence.TemporalType;
 */
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.openpojo.business.annotation.BusinessKey;
import uk.co.blackpepper.bowman.annotation.RemoteResource;

@Entity
@RemoteResource("/activitySpecParameters")
@Table(name = "activity_spec_parameters")
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

    @PrePersist
    protected void onCreate() {
        this.created = new Date();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).append("direction", direction).toString();
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
