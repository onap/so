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


import java.net.URI;
import java.util.Date;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.openpojo.business.annotation.BusinessKey;
import uk.co.blackpepper.bowman.annotation.RemoteResource;
import uk.co.blackpepper.bowman.annotation.ResourceId;

/**
 * EntityBean class for a CloudSite. This bean represents a cloud location (i.e. and LCP node) in the NVP/AIC cloud. It
 * will be loaded via CloudConfig object, of which it is a component
 *
 */
@Entity
@RemoteResource("/cloudSite")
@Table(name = "cloud_sites")
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class CloudSite {

    @JsonProperty
    @BusinessKey
    @Id
    @Column(name = "ID")
    private String id;

    @JsonProperty("region_id")
    @BusinessKey
    @Column(name = "REGION_ID")
    private String regionId;

    @JsonProperty("aic_version")
    @BusinessKey
    @Column(name = "CLOUD_VERSION")
    private String cloudVersion;

    @JsonProperty("clli")
    @BusinessKey
    @Column(name = "CLLI")
    private String clli;

    @JsonProperty("platform")
    @BusinessKey
    @Column(name = "PLATFORM")
    private String platform;

    @JsonProperty("orchestrator")
    @BusinessKey
    @Column(name = "ORCHESTRATOR")
    private String orchestrator;

    @JsonProperty("cloudify_id")
    @BusinessKey
    @Column(name = "CLOUDIFY_ID")
    private String cloudifyId;

    @JsonProperty("cloud_owner")
    @BusinessKey
    @Column(name = "CLOUD_OWNER")
    private String cloudOwner;

    // Derived property (set by CloudConfig loader based on identityServiceId)
    @BusinessKey
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDENTITY_SERVICE_ID")
    private CloudIdentity identityService;

    @BusinessKey
    @JsonProperty("identity_service_id")
    private transient String identityServiceId;

    @JsonProperty("last_updated_by")
    @BusinessKey
    @Column(name = "LAST_UPDATED_BY")
    private String lastUpdatedBy;

    @JsonProperty("creation_timestamp")
    @BusinessKey
    @Column(name = "CREATION_TIMESTAMP", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @JsonProperty("update_timestamp")
    @BusinessKey
    @Column(name = "UPDATE_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;

    @JsonProperty("support_fabric")
    @BusinessKey
    @Column(name = "SUPPORT_FABRIC", nullable = false)
    private Boolean supportFabric = true;

    @Transient
    private URI uri;

    public CloudSite() {

    }

    @PrePersist
    protected void onCreate() {
        this.created = new Date();
        this.updated = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updated = new Date();
    }

    public CloudSite(CloudSite site) {
        this.cloudVersion = site.getCloudVersion();
        this.clli = site.getClli();
        this.id = site.getId();
        this.identityService = site.getIdentityService();
        this.orchestrator = site.getOrchestrator();
        this.platform = site.getPlatform();
        this.regionId = site.getRegionId();
        this.identityServiceId = site.getIdentityServiceId();
        this.supportFabric = site.getSupportFabric();
    }


    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ResourceId
    public URI getUri() {
        return this.uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public String getIdentityServiceId() {
        return identityServiceId == null ? (identityService == null ? null : identityService.getId())
                : identityServiceId;
    }

    public String getCloudVersion() {
        return cloudVersion;
    }

    public void setCloudVersion(String cloudVersion) {
        this.cloudVersion = cloudVersion;
    }

    public String getClli() {
        return clli;
    }

    public void setClli(String clli) {
        this.clli = clli;
    }

    public String getCloudifyId() {
        return cloudifyId;
    }

    public void setCloudifyId(String cloudifyId) {
        this.cloudifyId = cloudifyId;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public Date getCreated() {
        return created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getOrchestrator() {
        return orchestrator;
    }

    public void setOrchestrator(String orchestrator) {
        this.orchestrator = orchestrator;
    }

    public CloudIdentity getIdentityService() {
        return identityService;
    }

    public void setIdentityService(CloudIdentity identity) {
        this.identityService = identity;
    }

    public Boolean getSupportFabric() {
        return supportFabric;
    }

    public void setSupportFabric(Boolean supportFabric) {
        this.supportFabric = supportFabric;
    }

    @Deprecated
    public void setIdentityServiceId(String identityServiceId) {
        this.identityServiceId = identityServiceId;
    }

    public String getCloudOwner() {
        return cloudOwner;
    }

    public void setCloudOwner(String cloudOwner) {
        this.cloudOwner = cloudOwner;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("regionId", getRegionId())
                .append("identityServiceId", getIdentityServiceId()).append("cloudVersion", getCloudVersion())
                .append("clli", getClli()).append("cloudifyId", getCloudifyId()).append("platform", getPlatform())
                .append("orchestrator", getOrchestrator()).append("cloud-owner", getCloudOwner()).toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (!getClass().equals(other.getClass())) {
            return false;
        }
        CloudSite castOther = (CloudSite) other;
        return new EqualsBuilder().append(getRegionId(), castOther.getRegionId())
                .append(getIdentityServiceId(), castOther.getIdentityServiceId())
                .append(getCloudVersion(), castOther.getCloudVersion()).append(getClli(), castOther.getClli())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(1, 31).append(getRegionId()).append(getIdentityServiceId()).append(getCloudVersion())
                .append(getClli()).toHashCode();
    }
}
