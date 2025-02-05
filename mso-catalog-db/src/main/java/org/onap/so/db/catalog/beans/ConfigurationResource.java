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
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.openpojo.business.annotation.BusinessKey;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "configuration")
public class ConfigurationResource implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6675926401792679171L;

    @BusinessKey
    @Id
    @Column(name = "MODEL_UUID")
    private String modelUUID;

    @Column(name = "MODEL_INVARIANT_UUID")
    private String modelInvariantUUID;

    @Column(name = "MODEL_VERSION")
    private String modelVersion;

    @Column(name = "MODEL_NAME")
    private String modelName;

    @Column(name = "TOSCA_NODE_TYPE")
    private String toscaNodeType;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "CREATION_TIMESTAMP", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "configurationResource")
    private Set<ConfigurationResourceCustomization> configurationResourceCustomization;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "configurationResource")
    private Set<CvnfcConfigurationCustomization> cvnfcConfigurationCustomization;

    @PrePersist
    protected void onCreate() {
        this.created = new Date();
    }

    public String getModelUUID() {
        return modelUUID;
    }

    public void setModelUUID(String modelUUID) {
        this.modelUUID = modelUUID;
    }

    public String getModelInvariantUUID() {
        return modelInvariantUUID;
    }

    public void setModelInvariantUUID(String modelInvariantUUID) {
        this.modelInvariantUUID = modelInvariantUUID;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getToscaNodeType() {
        return toscaNodeType;
    }

    public void setToscaNodeType(String toscaNodeType) {
        this.toscaNodeType = toscaNodeType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreated() {
        return created;
    }

    public Set<ConfigurationResourceCustomization> getConfigurationResourceCustomization() {
        return configurationResourceCustomization;
    }

    public void setConfigurationResourceCustomization(
            Set<ConfigurationResourceCustomization> configurationResourceCustomization) {
        this.configurationResourceCustomization = configurationResourceCustomization;
    }

    public Set<CvnfcConfigurationCustomization> getCvnfcConfigurationCustomization() {
        return cvnfcConfigurationCustomization;
    }

    public void setCvnfcConfigurationCustomization(
            Set<CvnfcConfigurationCustomization> cvnfcConfigurationCustomization) {
        this.cvnfcConfigurationCustomization = cvnfcConfigurationCustomization;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("modelUUID", modelUUID).append("modelInvariantUUID", modelInvariantUUID)
                .append("modelVersion", modelVersion).append("modelName", modelName)
                .append("toscaNodeType", toscaNodeType).append("description", description).append("created", created)
                .append("configurationResourceCustomization", configurationResourceCustomization).toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ConfigurationResource)) {
            return false;
        }
        ConfigurationResource castOther = (ConfigurationResource) other;
        return new EqualsBuilder().append(modelUUID, castOther.modelUUID).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(modelUUID).toHashCode();
    }
}
