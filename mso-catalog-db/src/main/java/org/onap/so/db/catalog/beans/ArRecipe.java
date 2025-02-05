/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
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
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.openpojo.business.annotation.BusinessKey;

@Entity
@Table(name = "ar_recipe")
public class ArRecipe implements Recipe, Serializable {
    private static final long serialVersionUID = 768026109321305392L;

    @Id
    @Column(name = "ID", nullable = false)
    private Integer id;
    @BusinessKey
    @Column(name = "MODEL_NAME", nullable = false)
    private String modelName;
    @BusinessKey
    @Column(name = "ACTION", nullable = false)
    private String action;

    @Column(name = "VERSION_STR", nullable = false)
    private String version;
    @BusinessKey
    @Column(name = "SERVICE_TYPE")
    private String serviceType;

    @Column(name = "DESCRIPTION")
    private String description;
    @BusinessKey
    @Column(name = "ORCHESTRATION_URI", nullable = false)
    private String orchestrationUri;

    @Column(name = "AR_PARAM_XSD")
    private String paramXsd;

    @Column(name = "RECIPE_TIMEOUT")
    private Integer recipeTimeout;

    @Column(name = "CREATION_TIMESTAMP", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOrchestrationUri() {
        return orchestrationUri;
    }

    public void setOrchestrationUri(String orchestrationUri) {
        this.orchestrationUri = orchestrationUri;
    }

    public String getParamXsd() {
        return paramXsd;
    }

    public void setParamXsd(String paramXsd) {
        this.paramXsd = paramXsd;
    }

    public Integer getRecipeTimeout() {
        return recipeTimeout;
    }

    public void setRecipeTimeout(Integer recipeTimeout) {
        this.recipeTimeout = recipeTimeout;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("modelName", getModelName()).append("action", getAction())
                .append("version", getVersion()).append("serviceType", getServiceType())
                .append("description", getDescription()).append("orchestrationUri", getOrchestrationUri())
                .append("paramXSD", getParamXsd()).append("recipeTimeout", getRecipeTimeout())
                .append("created", getCreated()).toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ArRecipe)) {
            return false;
        }
        ArRecipe castOther = (ArRecipe) other;
        return new EqualsBuilder().append(getId(), castOther.getId()).append(getModelName(), castOther.getModelName())
                .append(getAction(), castOther.getAction()).append(getServiceType(), castOther.getServiceType())
                .append(getOrchestrationUri(), castOther.getOrchestrationUri()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getId()).append(getModelName()).append(getAction()).append(getServiceType())
                .append(getOrchestrationUri()).toHashCode();
    }
}
