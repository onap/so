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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.openpojo.business.annotation.BusinessKey;
import uk.co.blackpepper.bowman.annotation.RemoteResource;

@Entity
@RemoteResource("/vnfRecipe")
@Table(name = "vnf_recipe")
public class VnfRecipe implements Serializable, Recipe {

    private static final long serialVersionUID = 768026109321305392L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @BusinessKey
    @Column(name = "NF_ROLE")
    private String nfRole;

    @Column(name = "VNF_PARAM_XSD")
    private String paramXsd;

    @Column(name = "VF_MODULE_ID")
    private String vfModuleId;

    @BusinessKey
    @Column(name = "ACTION")
    protected String action;

    @Column(name = "description")
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

    @Column(name = "CREATION_TIMESTAMP", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @PrePersist
    protected void onCreate() {
        this.created = new Date();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("vnfType", nfRole).append("paramXsd", paramXsd)
                .append("vfModuleId", vfModuleId).append("action", action).append("description", description)
                .append("orchestrationUri", orchestrationUri).append("recipeTimeout", recipeTimeout)
                .append("serviceType", serviceType).append("created", created).toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof VnfRecipe)) {
            return false;
        }
        VnfRecipe castOther = (VnfRecipe) other;
        return new EqualsBuilder().append(nfRole, castOther.nfRole).append(action, castOther.action)
                .append(orchestrationUri, castOther.orchestrationUri).append(serviceType, castOther.serviceType)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(nfRole).append(action).append(orchestrationUri).append(serviceType)
                .toHashCode();
    }

    public String getNfRole() {
        return nfRole;
    }

    public void setNfRole(String nfRole) {
        this.nfRole = nfRole;
    }

    @Override
    public String getParamXsd() {
        return paramXsd;
    }

    public void setParamXsd(String paramXsd) {
        this.paramXsd = paramXsd;
    }

    public String getVfModuleId() {
        return vfModuleId;
    }

    public void setVfModuleId(String vfModuleId) {
        this.vfModuleId = vfModuleId;
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

    @Override
    public String getOrchestrationUri() {
        return orchestrationUri;
    }

    public void setOrchestrationUri(String orchestrationUri) {
        this.orchestrationUri = orchestrationUri;
    }

    @Override
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

    public String getVersionStr() {
        return versionStr;
    }

    public void setVersionStr(String versionStr) {
        this.versionStr = versionStr;
    }
}
