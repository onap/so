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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.openpojo.business.annotation.BusinessKey;
import uk.co.blackpepper.bowman.annotation.LinkedResource;
import uk.co.blackpepper.bowman.annotation.RemoteResource;

@Entity
@RemoteResource("/heatTemplate")
@Table(name = "heat_template")
public class HeatTemplate implements Serializable {

    private static final long serialVersionUID = 768026109321305392L;

    @BusinessKey
    @Id
    @Column(name = "ARTIFACT_UUID", length = 200)
    private String artifactUuid;

    @Column(name = "NAME", length = 200)
    private String templateName;

    @Lob
    @Column(name = "BODY", columnDefinition = "LONGTEXT")
    private String templateBody = null;

    @Column(name = "TIMEOUT_MINUTES", length = 20)
    private Integer timeoutMinutes;

    @BusinessKey
    @Column(name = "VERSION")
    private String version;

    @Column(name = "DESCRIPTION", length = 1200)
    private String description;

    @Column(name = "ARTIFACT_CHECKSUM", length = 200)
    private String artifactChecksum;

    @Column(name = "CREATION_TIMESTAMP", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "heatTemplateArtifactUuid")
    private Set<HeatTemplateParam> parameters;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "heat_nested_template", joinColumns = @JoinColumn(name = "PARENT_HEAT_TEMPLATE_UUID"),
            inverseJoinColumns = @JoinColumn(name = "CHILD_HEAT_TEMPLATE_UUID"))
    private List<HeatTemplate> childTemplates;

    public enum TemplateStatus {
        PARENT, CHILD, PARENT_COMPLETE
    }

    @PrePersist
    protected void onCreate() {
        this.created = new Date();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("artifactUuid", artifactUuid).append("templateName", templateName)
                .append("templateBody", templateBody).append("timeoutMinutes", timeoutMinutes)
                .append("version", version).append("description", description)
                .append("artifactChecksum", artifactChecksum).append("created", created)
                .append("parameters", parameters).append("childTemplates", childTemplates).toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof HeatTemplate)) {
            return false;
        }
        HeatTemplate castOther = (HeatTemplate) other;
        return new EqualsBuilder().append(artifactUuid, castOther.artifactUuid).append(version, castOther.version)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(artifactUuid).append(version).toHashCode();
    }

    @LinkedResource
    public List<HeatTemplate> getChildTemplates() {
        if (childTemplates == null)
            childTemplates = new ArrayList<>();
        return childTemplates;
    }

    public void setChildTemplates(List<HeatTemplate> childTemplates) {
        this.childTemplates = childTemplates;
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

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateBody() {
        return templateBody;
    }

    public void setTemplateBody(String templateBody) {
        this.templateBody = templateBody;
    }

    public Integer getTimeoutMinutes() {
        return timeoutMinutes;
    }

    public void setTimeoutMinutes(Integer timeout) {
        this.timeoutMinutes = timeout;
    }

    @LinkedResource
    public Set<HeatTemplateParam> getParameters() {
        if (parameters == null)
            parameters = new HashSet<>();
        return parameters;
    }

    public void setParameters(Set<HeatTemplateParam> parameters) {
        this.parameters = parameters;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHeatTemplate() {
        return this.templateBody;
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
}
