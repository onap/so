package org.onap.so.db.catalog.beans;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.openpojo.business.annotation.BusinessKey;
import uk.co.blackpepper.bowman.annotation.RemoteResource;

@Entity
@RemoteResource("/activitySpecCategories")
@Table(name = "activity_spec_categories")
public class ActivitySpecCategories implements Serializable {

    private static final long serialVersionUID = -6251150462067699643L;

    @Id
    @Column(name = "ID", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer ID;

    @BusinessKey
    @Column(name = "NAME")
    private String name;

    public Integer getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).toString();
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
