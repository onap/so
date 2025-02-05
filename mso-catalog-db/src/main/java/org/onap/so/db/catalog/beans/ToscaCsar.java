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

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.openpojo.business.annotation.BusinessKey;

@Entity
@Table(name = "tosca_csar")
public class ToscaCsar implements Serializable {

    private static final long serialVersionUID = 768026109321305392L;

    @BusinessKey
    @Id
    @Column(name = "ARTIFACT_UUID")
    private String artifactUUID;

    @Column(name = "NAME")
    private String name;

    @Column(name = "ARTIFACT_CHECKSUM")
    private String artifactChecksum;

    @Column(name = "URL")
    private String url;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "CREATION_TIMESTAMP", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @BusinessKey
    @Column(name = "Version")
    private String version;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "csar")
    @JsonIgnore
    private List<Service> services;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("artifactUUID", artifactUUID).append("name", name)
                .append("artifactChecksum", artifactChecksum).append("url", url).append("description", description)
                .append("created", created).append("version", version).append("services", services).toString();
    }

    @PrePersist
    protected void onCreate() {
        this.created = new Date();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ToscaCsar)) {
            return false;
        }
        ToscaCsar castOther = (ToscaCsar) other;
        return new EqualsBuilder().append(artifactUUID, castOther.artifactUUID).append(version, castOther.version)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(artifactUUID).append(version).toHashCode();
    }

    public ToscaCsar() {}

    public String getArtifactUUID() {
        return artifactUUID;
    }

    public void setArtifactUUID(String artifactUUID) {
        this.artifactUUID = artifactUUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getArtifactChecksum() {
        return artifactChecksum;
    }

    public void setArtifactChecksum(String artifactChecksum) {
        this.artifactChecksum = artifactChecksum;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }
}
