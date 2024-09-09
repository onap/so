/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.db.catalog.beans;

import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.openpojo.business.annotation.BusinessKey;
import uk.co.blackpepper.bowman.annotation.RemoteResource;


/**
 * EntityBean class for ProcessingFlags. This bean represents a set of flags governing request processing.
 *
 */
@RemoteResource("/processingFlags")
@Entity
@Table(name = "processing_flags")
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class ProcessingFlags {

    @Id
    @Column(name = "ID", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonProperty("flag")
    @BusinessKey
    @Column(name = "FLAG")
    private String flag;

    @JsonProperty("value")
    @BusinessKey
    @Column(name = "VALUE")
    private String value;

    @JsonProperty("endpoint")
    @BusinessKey
    @Column(name = "ENDPOINT")
    private String endpoint;

    @JsonProperty("description")
    @Lob
    @Column(name = "DESCRIPTION", columnDefinition = "LONGTEXT")
    private String description = null;

    @JsonProperty("creation_timestamp")
    @BusinessKey
    @Column(name = "CREATION_TIMESTAMP", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @JsonProperty("update_timestamp")
    @BusinessKey
    @Column(name = "UPDATE_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    private Date updated;

    public ProcessingFlags() {

    }

    @PrePersist
    protected void onCreate() {
        this.created = new Date();
        // this.updated = new Date();
    }

    public ProcessingFlags(ProcessingFlags flags) {
        this.flag = flags.getFlag();
        this.value = flags.getValue();
        this.endpoint = flags.getEndpoint();
        this.description = flags.getDescription();
    }

    public Integer getId() {
        return this.id;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
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

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("flag", getFlag())
                .append("value", getValue()).append("endpoint", getEndpoint()).append("description", getDescription())
                .toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (!getClass().equals(other.getClass())) {
            return false;
        }
        ProcessingFlags castOther = (ProcessingFlags) other;
        return new EqualsBuilder().append(getFlag(), castOther.getFlag()).append(getValue(), castOther.getValue())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(1, 31).append(getFlag()).append(getValue()).append(getEndpoint()).toHashCode();
    }
}
