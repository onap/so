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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.openpojo.business.annotation.BusinessKey;
import uk.co.blackpepper.bowman.annotation.RemoteResource;

@Entity
@RemoteResource("/modelRecipe")
@Table(name = "model_recipe")
public class ModelRecipe implements Serializable {

    private static final long serialVersionUID = 768026109321305392L;

    @Id
    @Column(name = "ID", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @BusinessKey
    @Column(name = "MODEL_ID", nullable = false)
    private Integer modelId;
    @BusinessKey
    @Column(name = "ACTION", length = 50)
    private String action = null;
    @Column(name = "SCHEMA_VERSION", length = 20)
    private String schemaVersion = null;
    @Column(name = "DESCRIPTION", length = 1200)
    private String description = null;
    @Column(name = "ORCHESTRATION_URI", length = 256)
    private String orchestrationUri = null;
    @Column(name = "MODEL_PARAM_XSD", length = 2048)
    private String modelParamXSD = null;
    @Column(name = "RECIPE_TIMEOUT")
    private Integer recipeTimeout;
    @Column(name = "CREATION_TIMESTAMP", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created = null;

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @return the modelId
     */
    public Integer getModelId() {
        return modelId;
    }

    /**
     * @param modelId the modelId to set
     */
    public void setModelId(Integer modelId) {
        this.modelId = modelId;
    }

    /**
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * @param action the action to set
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * @return the versionStr
     */
    public String getSchemaVersion() {
        return schemaVersion;
    }

    /**
     * @param schemaVersion the versionStr to set
     */
    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the orchestrationUri
     */
    public String getOrchestrationUri() {
        return orchestrationUri;
    }

    /**
     * @param orchestrationUri the orchestrationUri to set
     */
    public void setOrchestrationUri(String orchestrationUri) {
        this.orchestrationUri = orchestrationUri;
    }

    /**
     * @return the modelParamXSD
     */
    public String getModelParamXSD() {
        return modelParamXSD;
    }

    /**
     * @param modelParamXSD the modelParamXSD to set
     */
    public void setModelParamXSD(String modelParamXSD) {
        this.modelParamXSD = modelParamXSD;
    }

    /**
     * @return the recipeTimeout
     */
    public Integer getRecipeTimeout() {
        return recipeTimeout;
    }

    /**
     * @param recipeTimeout the recipeTimeout to set
     */
    public void setRecipeTimeout(Integer recipeTimeout) {
        this.recipeTimeout = recipeTimeout;
    }

    /**
     * @return the created
     */
    public Date getCreated() {
        return created;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ModelRecipe)) {
            return false;
        }
        ModelRecipe castOther = (ModelRecipe) other;
        return new EqualsBuilder().append(getModelId(), castOther.getModelId())
                .append(getAction(), castOther.getAction()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getModelId()).append(getAction()).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", getId()).append("modelId", getModelId())
                .append("action", getAction()).append("schemaVersion", getSchemaVersion())
                .append("description", getDescription()).append("orchestrationUri", getOrchestrationUri())
                .append("modelParamXSD", getModelParamXSD()).append("recipeTimeout", getRecipeTimeout())
                .append("created", getCreated()).toString();
    }
}
