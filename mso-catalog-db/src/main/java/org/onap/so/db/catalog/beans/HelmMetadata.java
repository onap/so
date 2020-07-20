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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import com.openpojo.business.annotation.BusinessKey;

@Entity
@Table(name = "helm_metadata")
public class HelmMetadata {

    @BusinessKey
    @Id
    @Column(name = "ARTIFACT_UUID", length = 200)
    private String artifactUuid;

    @Column(name = "ARTIFACT_NAME")
    private String artifactName;

    @Column(name = "ARTIFACT_URL")
    private String artifactURL;

    @Column(name = "ARTIFACT_URI")
    private String artifactURI;

    @Column(name = "DESCRIPTION", length = 1200)
    private String description;

    @Column(name = "ARTIFACT_CHECKSUM", length = 200)
    private String artifactChecksum;

    @BusinessKey
    @Column(name = "VERSION")
    private String version;

    @Column(name = "TIMEOUT_MINUTES", length = 20)
    private Integer timeoutMinutes;

    @Column(name = "CREATION_TIMESTAMP", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @PrePersist
    protected void onCreate() {
        this.created = new Date();
    }


    public String getArtifactUuid() {
        return artifactUuid;
    }

    public void setArtifactUuid(String artifactUuid) {
        this.artifactUuid = artifactUuid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Integer getTimeoutMinutes() {
        return timeoutMinutes;
    }

    public void setTimeoutMinutes(Integer timeoutMinutes) {
        this.timeoutMinutes = timeoutMinutes;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }


    public String getArtifactName() {
        return artifactName;
    }


    public void setArtifactName(String artifactName) {
        this.artifactName = artifactName;
    }


    public String getArtifactURL() {
        return artifactURL;
    }


    public void setArtifactURL(String artifactURL) {
        this.artifactURL = artifactURL;
    }


    public String getArtifactURI() {
        return artifactURI;
    }


    public void setArtifactURI(String artifactURI) {
        this.artifactURI = artifactURI;
    }



}
