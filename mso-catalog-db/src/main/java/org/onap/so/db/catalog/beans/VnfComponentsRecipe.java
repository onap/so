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
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.openpojo.business.annotation.BusinessKey;
import uk.co.blackpepper.bowman.annotation.RemoteResource;

@Entity
@RemoteResource("/vnfComponentsRecipe")
@Table(name = "vnf_components_recipe")
public class VnfComponentsRecipe implements Serializable, Recipe {

    private static final long serialVersionUID = 768026109321305392L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @BusinessKey
    @Column(name = "ACTION")
    private String action;

    @Column(name = "DESCRIPTION")
    private String description;

    @BusinessKey
    @Column(name = "ORCHESTRATION_URI")
    private String orchestrationUri;

    @Column(name = "RECIPE_TIMEOUT")
    private Integer recipeTimeout;

    @BusinessKey
    @Column(name = "VNF_TYPE")
    private String vnfType;

    @Column(name = "VNF_COMPONENT_PARAM_XSD")
    private String paramXsd;

    @Column(name = "VNF_COMPONENT_TYPE")
    private String vnfComponentType;

    @BusinessKey
    @Column(name = "VF_MODULE_MODEL_UUID")
    private String vfModuleModelUUID;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("action", action).append("description", description)
                .append("orchestrationUri", orchestrationUri).append("recipeTimeout", recipeTimeout)
                .append("vnfType", vnfType).append("paramXsd", paramXsd).append("vnfComponentType", vnfComponentType)
                .append("vfModuleModelUUID", vfModuleModelUUID).toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof VnfComponentsRecipe)) {
            return false;
        }
        VnfComponentsRecipe castOther = (VnfComponentsRecipe) other;
        return new EqualsBuilder().append(action, castOther.action).append(orchestrationUri, castOther.orchestrationUri)
                .append(vnfType, castOther.vnfType).append(vfModuleModelUUID, castOther.vfModuleModelUUID).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(action).append(orchestrationUri).append(vnfType).append(vfModuleModelUUID)
                .toHashCode();
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOrchestrationUri(String orchestrationUri) {
        this.orchestrationUri = orchestrationUri;
    }

    public void setRecipeTimeout(Integer recipeTimeout) {
        this.recipeTimeout = recipeTimeout;
    }

    public String getVnfType() {
        return vnfType;
    }

    public void setVnfType(String vnfType) {
        this.vnfType = vnfType;
    }

    @Override
    public String getParamXsd() {
        return paramXsd;
    }

    public void setParamXsd(String paramXsd) {
        this.paramXsd = paramXsd;
    }

    public String getVnfComponentType() {
        return vnfComponentType;
    }

    public void setVnfComponentType(String vnfComponentType) {
        this.vnfComponentType = vnfComponentType;
    }

    public String getVfModuleModelUUID() {
        return vfModuleModelUUID;
    }

    public void setVfModuleModelUUID(String vfModuleModelUUID) {
        this.vfModuleModelUUID = vfModuleModelUUID;
    }

    @Override
    public String getOrchestrationUri() {
        return orchestrationUri;
    }

    @Override
    public Integer getRecipeTimeout() {
        return recipeTimeout;
    }
}
