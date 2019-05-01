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

package org.onap.so.rest.catalog.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.builder.ToStringBuilder;


public class HeatTemplate implements Serializable {


    private String artifactUuid;

    private String templateName;

    private String templateBody = null;

    private Integer timeoutMinutes;

    private String version;

    private String description;

    private String artifactChecksum;

    private Date created;

    private Set<HeatTemplateParam> parameters;

    private List<HeatTemplate> childTemplates;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("artifactUuid", artifactUuid).append("templateName", templateName)
                .append("templateBody", templateBody).append("timeoutMinutes", timeoutMinutes)
                .append("version", version).append("description", description)
                .append("artifactChecksum", artifactChecksum).append("created", created)
                .append("parameters", parameters).append("childTemplates", childTemplates).toString();
    }

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
