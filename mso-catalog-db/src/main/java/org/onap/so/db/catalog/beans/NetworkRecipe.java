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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import com.openpojo.business.annotation.BusinessKey;
import uk.co.blackpepper.bowman.annotation.RemoteResource;

@Entity
@RemoteResource("/networkRecipe")
@Table(name = "network_recipe")
public class NetworkRecipe implements Serializable, Recipe {
    private static final long serialVersionUID = 768026109321305392L;

    @Id
    @Column(name = "id")
    private Integer id;

    @BusinessKey
    @Column(name = "ACTION")
    protected String action;

    @Column(name = "DESCRIPTION")
    private String description;

    @BusinessKey
    @Column(name = "ORCHESTRATION_URI")
    protected String orchestrationUri;

    @Column(name = "RECIPE_TIMEOUT")
    private Integer recipeTimeout;

    @Column(name = "VERSION_STR")
    private String versionStr;

    @BusinessKey
    @Column(name = "SERVICE_TYPE")
    private String serviceType;

    @BusinessKey
    @Column(name = "MODEL_NAME")
    private String modelName;

    @Column(name = "NETWORK_PARAM_XSD")
    private String paramXsd;

    @Column(name = "CREATION_TIMESTAMP", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @PrePersist
    protected void onCreate() {
        this.created = new Date();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof NetworkRecipe)) {
            return false;
        }
        NetworkRecipe castOther = (NetworkRecipe) other;
        return new EqualsBuilder().append(action, castOther.action).append(orchestrationUri, castOther.orchestrationUri)
                .append(serviceType, castOther.serviceType).append(modelName, castOther.modelName).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(action).append(orchestrationUri).append(serviceType).append(modelName)
                .toHashCode();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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

    public Integer getRecipeTimeout() {
        return recipeTimeout;
    }

    public void setRecipeTimeout(Integer recipeTimeout) {
        this.recipeTimeout = recipeTimeout;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public Date getCreated() {
        return created;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getParamXsd() {
        return paramXsd;
    }

    public void setParamXsd(String paramXsd) {
        this.paramXsd = paramXsd;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(",modelName=" + modelName);
        sb.append(",networkParamXSD=" + paramXsd);
        return sb.toString();
    }

    public String getVersionStr() {
        return versionStr;
    }

    public void setVersionStr(String versionStr) {
        this.versionStr = versionStr;
    }
}
