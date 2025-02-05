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
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.openpojo.business.annotation.BusinessKey;

@Entity
@Table(name = "service_recipe")
public class ServiceRecipe implements Serializable, Recipe {

    private static final long serialVersionUID = 768026109321305392L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @BusinessKey
    @Column(name = "SERVICE_MODEL_UUID")
    private String serviceModelUUID;

    @BusinessKey
    @Column(name = "ACTION")
    private String action;

    @Column(name = "description")
    private String description;

    @BusinessKey
    @Column(name = "ORCHESTRATION_URI")
    private String orchestrationUri;

    @Column(name = "SERVICE_PARAM_XSD")
    private String paramXsd;

    @Column(name = "RECIPE_TIMEOUT")
    private Integer recipeTimeout;

    @Column(name = "SERVICE_TIMEOUT_INTERIM")
    private Integer serviceTimeoutInterim;

    @Column(name = "CREATION_TIMESTAMP", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @BusinessKey
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "SERVICE_MODEL_UUID", referencedColumnName = "MODEL_UUID", insertable = false, updatable = false)
    private Service service;

    @PrePersist
    protected void onCreate() {
        this.created = new Date();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("serviceModelUUID", serviceModelUUID)
                .append("action", action).append("description", description)
                .append("orchestrationUri", orchestrationUri).append("serviceParamXSD", paramXsd)
                .append("recipeTimeout", recipeTimeout).append("serviceTimeoutInterim", serviceTimeoutInterim)
                .append("created", created).toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ServiceRecipe)) {
            return false;
        }
        ServiceRecipe castOther = (ServiceRecipe) other;
        return new EqualsBuilder().append(serviceModelUUID, castOther.serviceModelUUID).append(action, castOther.action)
                .append(orchestrationUri, castOther.orchestrationUri).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(serviceModelUUID).append(action).append(orchestrationUri).toHashCode();
    }

    // This 'default' CTR is now needed for backward compatibility since a new
    // CTR was added below
    public ServiceRecipe() {
        super();
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getServiceModelUUID() {
        return serviceModelUUID;
    }

    public void setServiceModelUUID(String serviceModelUUID) {
        this.serviceModelUUID = serviceModelUUID;
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
    public String getParamXsd() {
        return paramXsd;
    }

    public void setParamXsd(String paramXsd) {
        this.paramXsd = paramXsd;
    }

    @Override
    public Integer getRecipeTimeout() {
        return recipeTimeout;
    }

    public void setRecipeTimeout(Integer recipeTimeout) {
        this.recipeTimeout = recipeTimeout;
    }

    public Integer getServiceTimeoutInterim() {
        return serviceTimeoutInterim;
    }

    public void setServiceTimeoutInterim(Integer serviceTimeoutInterim) {
        this.serviceTimeoutInterim = serviceTimeoutInterim;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Date getCreated() {
        return created;
    }
}
