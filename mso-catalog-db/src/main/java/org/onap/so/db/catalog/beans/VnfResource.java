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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import com.openpojo.business.annotation.BusinessKey;
import uk.co.blackpepper.bowman.annotation.LinkedResource;
import uk.co.blackpepper.bowman.annotation.RemoteResource;

@Entity
@RemoteResource("/vnfResource")
@Table(name = "vnf_resource")
public class VnfResource implements Serializable {

    private static final long serialVersionUID = 768026109321305392L;

    @BusinessKey
    @Id
    @Column(name = "MODEL_UUID")
    private String modelUUID;

    @Column(name = "MODEL_INVARIANT_UUID")
    private String modelInvariantUUID;

    @Column(name = "MODEL_NAME")
    private String modelName;

    @Column(name = "MODEL_VERSION")
    private String modelVersion;

    @Column(name = "TOSCA_NODE_TYPE")
    private String toscaNodeType;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "ORCHESTRATION_MODE")
    private String orchestrationMode;

    @Column(name = "AIC_VERSION_MIN")
    private String aicVersionMin;

    @Column(name = "AIC_VERSION_MAX")
    private String aicVersionMax;

    @Column(name = "RESOURCE_CATEGORY")
    private String category;

    @Column(name = "RESOURCE_SUB_CATEGORY")
    private String subCategory;

    @Column(name = "CREATION_TIMESTAMP", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "HEAT_TEMPLATE_ARTIFACT_UUID")
    private HeatTemplate heatTemplates;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "vnfResource")
    private List<VnfResourceWorkflow> vnfResourceWorkflow;

    @PrePersist
    protected void onCreate() {
        this.created = new Date();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("modelUUID", modelUUID).append("modelInvariantUUID", modelInvariantUUID)
                .append("modelName", modelName).append("modelVersion", modelVersion)
                .append("toscaNodeType", toscaNodeType).append("description", description)
                .append("orchestrationMode", orchestrationMode).append("aicVersionMin", aicVersionMin)
                .append("aicVersionMax", aicVersionMax).append("created", created)
                .append("heatTemplates", heatTemplates).append("vnfResourceWorkflow", vnfResourceWorkflow).toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof VnfResource)) {
            return false;
        }
        VnfResource castOther = (VnfResource) other;
        return new EqualsBuilder().append(modelUUID, castOther.modelUUID).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(modelUUID).toHashCode();
    }

    public String getOrchestrationMode() {
        return orchestrationMode;
    }

    public void setOrchestrationMode(String orchestrationMode) {
        this.orchestrationMode = orchestrationMode;
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

    public String getAicVersionMin() {
        return this.aicVersionMin;
    }

    public void setAicVersionMin(String aicVersionMin) {
        this.aicVersionMin = aicVersionMin;
    }

    public String getAicVersionMax() {
        return this.aicVersionMax;
    }

    public void setAicVersionMax(String aicVersionMax) {
        this.aicVersionMax = aicVersionMax;
    }

    /**
     * @return Returns the category.
     */
    public String getCategory() {
        return category;
    }

    /**
     * @param category The category to set.
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * @return Returns the subCategory.
     */
    public String getSubCategory() {
        return subCategory;
    }

    /**
     * @param subCategory The subCategory to set.
     */
    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public String getModelInvariantUUID() {
        return this.modelInvariantUUID;
    }

    public void setModelInvariantUUID(String modelInvariantUUID) {
        this.modelInvariantUUID = modelInvariantUUID;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelUUID() {
        return modelUUID;
    }

    public void setModelUUID(String modelUUID) {
        this.modelUUID = modelUUID;
    }

    public String getModelInvariantId() {
        return this.modelInvariantUUID;
    }

    public String getToscaNodeType() {
        return toscaNodeType;
    }

    public void setToscaNodeType(String toscaNodeType) {
        this.toscaNodeType = toscaNodeType;
    }

    @LinkedResource
    public HeatTemplate getHeatTemplates() {
        return heatTemplates;
    }

    public void setHeatTemplates(HeatTemplate heatTemplates) {
        this.heatTemplates = heatTemplates;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    @LinkedResource
    public List<VnfResourceWorkflow> getVnfResourceWorkflow() {
        if (vnfResourceWorkflow == null)
            vnfResourceWorkflow = new ArrayList<>();
        return vnfResourceWorkflow;
    }

    public void setVnfResourceWorkflow(List<VnfResourceWorkflow> vnfResourceWorkflow) {
        this.vnfResourceWorkflow = vnfResourceWorkflow;
    }

}
