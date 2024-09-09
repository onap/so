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
import java.util.Map;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.openpojo.business.annotation.BusinessKey;
import uk.co.blackpepper.bowman.annotation.RemoteResource;

@Entity
@RemoteResource("/model")
@Table(name = "model")
public class Model implements Serializable {

    private static final long serialVersionUID = 768026109321305392L;

    @Id
    @Column(name = "ID", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "MODEL_CUSTOMIZATION_ID", length = 40)
    private String modelCustomizationId = null;
    @Column(name = "MODEL_CUSTOMIZATION_NAME", length = 40)
    private String modelCustomizationName = null;
    @Column(name = "MODEL_INVARIANT_ID", length = 40)
    private String modelInvariantId = null;
    @Column(name = "MODEL_NAME", length = 40)
    private String modelName = null;
    @BusinessKey
    @Column(name = "MODEL_TYPE", length = 20)
    private String modelType = null;
    @Column(name = "MODEL_VERSION", length = 20)
    private String modelVersion = null;
    @BusinessKey
    @Column(name = "MODEL_VERSION_ID", length = 40)
    private String modelVersionId = null;
    @Column(name = "CREATION_TIMESTAMP", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created = null;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "modelId")
    @MapKey(name = "action")
    private Map<String, ModelRecipe> recipes;

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @return the modelCustomizationId
     */
    public String getModelCustomizationId() {
        return modelCustomizationId;
    }

    /**
     * @param modelCustomizationId the modelCustomizationId to set
     */
    public void setModelCustomizationId(String modelCustomizationId) {
        this.modelCustomizationId = modelCustomizationId;
    }

    /**
     * @return the modelCustomizationName
     */
    public String getModelCustomizationName() {
        return modelCustomizationName;
    }

    /**
     * @param modelCustomizationName the modelCustomizationName to set
     */
    public void setModelCustomizationName(String modelCustomizationName) {
        this.modelCustomizationName = modelCustomizationName;
    }

    /**
     * @return the modelInvariantId
     */
    public String getModelInvariantId() {
        return modelInvariantId;
    }

    /**
     * @param modelInvariantId the modelInvariantId to set
     */
    public void setModelInvariantId(String modelInvariantId) {
        this.modelInvariantId = modelInvariantId;
    }

    /**
     * @return the modelName
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * @param modelName the modelName to set
     */
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    /**
     * @return the modelType
     */
    public String getModelType() {
        return modelType;
    }

    /**
     * @param modelType the modelType to set
     */
    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    /**
     * @return the modelVersion
     */
    public String getModelVersion() {
        return modelVersion;
    }

    /**
     * @param modelVersion the modelVersion to set
     */
    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    /**
     * @return the modelVersionId
     */
    public String getModelVersionId() {
        return modelVersionId;
    }

    /**
     * @param modelVersionId the modelVersionId to set
     */
    public void setModelVersionId(String modelVersionId) {
        this.modelVersionId = modelVersionId;
    }

    /**
     * @return the created
     */
    public Date getCreated() {
        return created;
    }

    /**
     * @return the recipes
     */
    public Map<String, ModelRecipe> getRecipes() {
        return recipes;
    }

    /**
     * @param recipes the recipes to set
     */
    public void setRecipes(Map<String, ModelRecipe> recipes) {
        this.recipes = recipes;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Model)) {
            return false;
        }
        Model castOther = (Model) other;
        return new EqualsBuilder().append(getModelType(), castOther.getModelType())
                .append(getModelVersionId(), castOther.getModelVersionId()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getModelType()).append(getModelVersionId()).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", getId()).append("modelCustomizationId", getModelCustomizationId())
                .append("modelCustomizationName", getModelCustomizationName())
                .append("modelInvariantId", getModelInvariantId()).append("modelName", getModelName())
                .append("modelType", getModelType()).append("modelVersion", getModelVersion())
                .append("modelVersionId", getModelVersionId()).append("created", getCreated())
                .append("recipes", getRecipes()).toString();
    }

}
