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
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.openpojo.business.annotation.BusinessKey;

@Entity
@Table(name = "heat_files")
public class HeatFiles implements Serializable {

    private static final long serialVersionUID = 768026109321305392L;

    @BusinessKey
    @Id
    @Column(name = "ARTIFACT_UUID")
    private String artifactUuid;

    @Column(name = "DESCRIPTION")
    private String description = null;

    @Column(name = "NAME")
    private String fileName;

    @Lob
    @Column(name = "BODY", columnDefinition = "LONGTEXT")
    private String fileBody;

    @Column(name = "CREATION_TIMESTAMP", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name = "ARTIFACT_CHECKSUM")
    private String artifactChecksum;

    @Column(name = "VERSION")
    private String version;

    @PrePersist
    protected void onCreate() {
        this.created = new Date();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("artifactUuid", artifactUuid).append("description", description)
                .append("fileName", fileName).append("fileBody", fileBody).append("created", created)
                .append("artifactChecksum", artifactChecksum).toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof HeatFiles)) {
            return false;
        }
        HeatFiles castOther = (HeatFiles) other;
        return new EqualsBuilder().append(artifactUuid, castOther.artifactUuid).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(artifactUuid).toHashCode();
    }

    public String getArtifactUuid() {
        return this.artifactUuid;
    }

    public void setArtifactUuid(String artifactUuid) {
        this.artifactUuid = artifactUuid;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileBody() {
        return this.fileBody;
    }

    public void setFileBody(String fileBody) {
        this.fileBody = fileBody;
    }

    public Date getCreated() {
        return created;
    }

    public String getAsdcUuid() {
        return this.artifactUuid;
    }

    public void setAsdcUuid(String artifactUuid) {
        this.artifactUuid = artifactUuid;
    }

    public String getArtifactChecksum() {
        return artifactChecksum;
    }

    public void setArtifactChecksum(String artifactChecksum) {
        this.artifactChecksum = artifactChecksum;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
