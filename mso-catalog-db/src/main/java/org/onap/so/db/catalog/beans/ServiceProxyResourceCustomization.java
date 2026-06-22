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
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
@RemoteResource("/serviceProxyResourceCustomization")
@Table(name = "service_proxy_customization")
public class ServiceProxyResourceCustomization implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2822457299134903084L;

    @BusinessKey
    @Id
    @Column(name = "MODEL_CUSTOMIZATION_UUID")
    private String modelCustomizationUUID;

    @Column(name = "MODEL_INSTANCE_NAME")
    private String modelInstanceName;

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

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "SOURCE_SERVICE_MODEL_UUID")
    private Service sourceService;

    @PrePersist
    protected void onCreate() {
        this.created = new Date();
    }

    public String getModelCustomizationUUID() {
        return modelCustomizationUUID;
    }

    public void setModelCustomizationUUID(String modelCustomizationUUID) {
        this.modelCustomizationUUID = modelCustomizationUUID;
    }

    public String getModelInstanceName() {
        return modelInstanceName;
    }

    public void setModelInstanceName(String modelInstanceName) {
        this.modelInstanceName = modelInstanceName;
    }

    public String getToscaNodeType() {
        return toscaNodeType;
    }

    public void setToscaNodeType(String toscaNodeType) {
        this.toscaNodeType = toscaNodeType;
    }

    public Date getCreated() {
        return created;
    }

    @LinkedResource
    public Service getSourceService() {
        return sourceService;
    }

    public void setSourceService(Service sourceService) {
        this.sourceService = sourceService;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this).append("modelCustomizationUUID", modelCustomizationUUID)
                .append("modelInstanceName", modelInstanceName).append("toscaNodeType", toscaNodeType)
                .append("modelUUID", modelUUID).append("modelInvariantUUID", modelInvariantUUID)
                .append("modelName", modelName).append("description", description).append("created", created)
                .append("sourceService", sourceService).toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ServiceProxyResourceCustomization)) {
            return false;
        }
        ServiceProxyResourceCustomization castOther = (ServiceProxyResourceCustomization) other;
        return new EqualsBuilder().append(modelCustomizationUUID, castOther.modelCustomizationUUID).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(modelCustomizationUUID).toHashCode();
    }
}
