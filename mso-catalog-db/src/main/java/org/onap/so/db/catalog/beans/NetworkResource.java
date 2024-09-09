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
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jakarta.persistence.CascadeType;
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
import com.openpojo.business.annotation.BusinessKey;
import uk.co.blackpepper.bowman.annotation.LinkedResource;
import uk.co.blackpepper.bowman.annotation.RemoteResource;

@Entity
@RemoteResource("/networkResource")
@Table(name = "network_resource")
public class NetworkResource implements Serializable {

    private static final long serialVersionUID = 768026109321305392L;

    @BusinessKey
    @Id
    @Column(name = "MODEL_UUID")
    private String modelUUID;

    @Column(name = "ORCHESTRATION_MODE", nullable = false)
    private String orchestrationMode = "HEAT";

    @Column(name = "DESCRIPTION")
    private String description = null;

    @Column(name = "NEUTRON_NETWORK_TYPE")
    private String neutronNetworkType = null;

    @Column(name = "AIC_VERSION_MIN")
    private String aicVersionMin = null;

    @Column(name = "AIC_VERSION_MAX")
    private String aicVersionMax = null;

    @Column(name = "MODEL_NAME")
    private String modelName;

    @Column(name = "MODEL_INVARIANT_UUID")
    private String modelInvariantUUID;

    @Column(name = "MODEL_VERSION")
    private String modelVersion;

    @Column(name = "TOSCA_NODE_TYPE")
    private String toscaNodeType;

    @Column(name = "RESOURCE_CATEGORY")
    private String category;

    @Column(name = "RESOURCE_SUB_CATEGORY")
    private String subCategory;

    @Column(name = "CREATION_TIMESTAMP", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "networkResource")
    private List<NetworkResourceCustomization> networkResourceCustomization;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "networkResource")
    private List<CollectionNetworkResourceCustomization> collectionNetworkResourceCustomization;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "HEAT_TEMPLATE_ARTIFACT_UUID")
    private HeatTemplate heatTemplate;

    @PrePersist
    protected void onCreate() {
        this.created = new Date();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof NetworkResource)) {
            return false;
        }
        NetworkResource castOther = (NetworkResource) other;
        return new EqualsBuilder().append(modelUUID, castOther.modelUUID).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(modelUUID).toHashCode();
    }

    @LinkedResource
    public List<NetworkResourceCustomization> getNetworkResourceCustomization() {
        return networkResourceCustomization;
    }

    public void addNetworkResourceCustomization(NetworkResourceCustomization networkResourceCustomization) {
        if (this.networkResourceCustomization == null)
            this.networkResourceCustomization = new ArrayList<>();

        this.networkResourceCustomization.add(networkResourceCustomization);
    }

    public void setNetworkResourceCustomization(List<NetworkResourceCustomization> networkResourceCustomization) {
        this.networkResourceCustomization = networkResourceCustomization;
    }

    @LinkedResource
    public List<CollectionNetworkResourceCustomization> getCollectionNetworkResourceCustomization() {
        return collectionNetworkResourceCustomization;
    }

    public void setCollectionNetworkResourceCustomization(
            List<CollectionNetworkResourceCustomization> collectionNetworkResourceCustomization) {
        this.collectionNetworkResourceCustomization = collectionNetworkResourceCustomization;
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

    public String getNeutronNetworkType() {
        return neutronNetworkType;
    }

    public void setNeutronNetworkType(String neutronNetworkType) {
        this.neutronNetworkType = neutronNetworkType;
    }

    public Date getCreated() {
        return created;
    }

    public String getAicVersionMin() {
        return aicVersionMin;
    }

    public void setAicVersionMin(String aicVersionMin) {
        this.aicVersionMin = aicVersionMin;
    }

    public String getAicVersionMax() {
        return aicVersionMax;
    }

    public void setAicVersionMax(String aicVersionMax) {
        this.aicVersionMax = aicVersionMax;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelInvariantUUID() {
        return modelInvariantUUID;
    }

    public void setModelInvariantUUID(String modelInvariantUUID) {
        this.modelInvariantUUID = modelInvariantUUID;
    }

    public String getToscaNodeType() {
        return toscaNodeType;
    }

    public void setToscaNodeType(String toscaNodeType) {
        this.toscaNodeType = toscaNodeType;
    }

    public String getModelUUID() {
        return modelUUID;
    }

    public void setModelUUID(String modelUUID) {
        this.modelUUID = modelUUID;
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

    @LinkedResource
    public HeatTemplate getHeatTemplate() {
        return heatTemplate;
    }

    public void setHeatTemplate(HeatTemplate heatTemplate) {
        this.heatTemplate = heatTemplate;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("NETWORK Resource:");
        sb.append("modelVersion=");
        sb.append(modelVersion);
        sb.append(",mode=");
        sb.append(orchestrationMode);
        sb.append(",neutronType=");
        sb.append(neutronNetworkType);
        sb.append(",aicVersionMin=");
        sb.append(aicVersionMin);
        sb.append(",aicVersionMax=");
        sb.append(aicVersionMax);
        sb.append(",modelName=");
        sb.append(modelName);
        sb.append(",modelInvariantUUID=");
        sb.append(modelInvariantUUID);
        sb.append(",toscaNodeType=");
        sb.append(toscaNodeType);
        sb.append(",modelUUID=");
        sb.append(modelUUID);

        if (created != null) {
            sb.append(",created=");
            sb.append(DateFormat.getInstance().format(created));
        }

        return sb.toString();
    }
}
