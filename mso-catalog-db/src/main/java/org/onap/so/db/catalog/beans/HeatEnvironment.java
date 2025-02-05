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
import java.text.DateFormat;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import com.openpojo.business.annotation.BusinessKey;

@Entity
@Table(name = "heat_environment")
public class HeatEnvironment implements Serializable {

    private static final long serialVersionUID = 768026109321305392L;

    @BusinessKey
    @Id
    @Column(name = "ARTIFACT_UUID")
    private String artifactUuid;

    @Column(name = "NAME")
    private String name = null;

    @Column(name = "DESCRIPTION")
    private String description = null;

    @Lob
    @Column(name = "BODY", columnDefinition = "LONGTEXT")
    private String environment = null;

    @Column(name = "ARTIFACT_CHECKSUM")
    private String artifactChecksum;

    @Column(name = "CREATION_TIMESTAMP", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @BusinessKey
    @Column(name = "VERSION")
    private String version;

    @PrePersist
    protected void onCreate() {
        this.created = new Date();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof HeatEnvironment)) {
            return false;
        }
        HeatEnvironment castOther = (HeatEnvironment) other;
        return new EqualsBuilder().append(artifactUuid, castOther.artifactUuid).append(version, castOther.version)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(artifactUuid).append(version).toHashCode();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getArtifactUuid() {
        return this.artifactUuid;
    }

    public void setArtifactUuid(String artifactUuid) {
        this.artifactUuid = artifactUuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEnvironment() {
        return this.environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Artifact UUID=" + this.artifactUuid);
        sb.append(", name=");
        sb.append(name);
        sb.append(", version=");
        sb.append(version);
        sb.append(", description=");
        sb.append(this.description == null ? "null" : this.description);
        sb.append(", body=");
        sb.append(this.environment == null ? "null" : this.environment);
        if (this.created != null) {
            sb.append(",creationTimestamp=");
            sb.append(DateFormat.getInstance().format(this.created));
        }
        return sb.toString();
    }
}
