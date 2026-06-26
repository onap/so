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

import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.openpojo.business.annotation.BusinessKey;
import uk.co.blackpepper.bowman.annotation.RemoteResource;

/**
 * EntityBean class for a CloudIdentity. This bean represents a cloud identity service instance (i.e. a DCP node) in the
 * NVP/AIC cloud. It will be loaded via CloudConfig object, of which it is a component.
 *
 */
@Entity
@RemoteResource("/cloudIdentity")
@Table(name = "identity_services")
public class CloudIdentity {

    @JsonProperty
    @BusinessKey
    @Id
    @Column(name = "ID")
    private String id;

    @JsonProperty("identity_url")
    @BusinessKey
    @Column(name = "IDENTITY_URL")
    private String identityUrl;

    @JsonProperty("mso_id")
    @BusinessKey
    @Column(name = "MSO_ID")
    private String msoId;

    @JsonProperty("mso_pass")
    @BusinessKey
    @Column(name = "MSO_PASS")
    private String msoPass;

    @JsonProperty("project_domain_name")
    @BusinessKey
    @Column(name = "PROJECT_DOMAIN_NAME")
    private String projectDomainName;

    @JsonProperty("admin_project_domain_name")
    @BusinessKey
    @Column(name = "ADMIN_PROJECT_DOMAIN_NAME", nullable = false)
    private String adminProjectDomainName = "Default";

    @JsonProperty("user_domain_name")
    @BusinessKey
    @Column(name = "USER_DOMAIN_NAME")
    private String userDomainName;

    @JsonProperty("admin_tenant")
    @BusinessKey
    @Column(name = "ADMIN_TENANT")
    private String adminTenant;

    @JsonProperty("member_role")
    @BusinessKey
    @Column(name = "MEMBER_ROLE")
    private String memberRole;

    @JsonProperty("tenant_metadata")
    @BusinessKey
    @Column(name = "TENANT_METADATA")
    private Boolean tenantMetadata;

    @JsonProperty("identity_server_type")
    @BusinessKey
    @Enumerated(EnumType.STRING)
    @Column(name = "IDENTITY_SERVER_TYPE")
    private ServerType identityServerType;

    @JsonProperty("identity_authentication_type")
    @BusinessKey
    @Enumerated(EnumType.STRING)
    @Column(name = "IDENTITY_AUTHENTICATION_TYPE")
    private AuthenticationType identityAuthenticationType;

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

    public CloudIdentity() {}

    @PrePersist
    protected void onCreate() {
        this.created = new Date();
        this.updated = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updated = new Date();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdentityUrl() {
        return this.identityUrl;
    }

    public void setIdentityUrl(String url) {
        this.identityUrl = url;
    }

    public String getMsoId() {
        return msoId;
    }

    public void setMsoId(String id) {
        this.msoId = id;
    }

    public String getMsoPass() {
        return msoPass;
    }

    public void setMsoPass(String pwd) {
        this.msoPass = pwd;
    }

    public String getAdminTenant() {
        return adminTenant;
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

    public void setAdminTenant(String tenant) {
        this.adminTenant = tenant;
    }

    public String getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(String role) {
        this.memberRole = role;
    }

    public Boolean getTenantMetadata() {
        return tenantMetadata;
    }

    public void setTenantMetadata(Boolean meta) {
        this.tenantMetadata = meta;
    }

    public ServerType getIdentityServerType() {
        return this.identityServerType;
    }

    public void setIdentityServerType(ServerType ist) {
        this.identityServerType = ist;
    }

    public String getIdentityServerTypeAsString() {
        return this.identityServerType.toString();
    }

    /**
     * @return the identityAuthenticationType
     */
    public AuthenticationType getIdentityAuthenticationType() {
        return identityAuthenticationType;
    }

    /**
     * @param identityAuthenticationType the identityAuthenticationType to set
     */
    public void setIdentityAuthenticationType(AuthenticationType identityAuthenticationType) {
        this.identityAuthenticationType = identityAuthenticationType;
    }

    public String getProjectDomainName() {
        return projectDomainName;
    }

    public void setProjectDomainName(String projectDomainName) {
        this.projectDomainName = projectDomainName;
    }

    public String getAdminProjectDomainName() {
        return adminProjectDomainName;
    }

    public void setAdminProjectDomainName(String adminProjectDomainName) {
        this.adminProjectDomainName = adminProjectDomainName;
    }

    public String getUserDomainName() {
        return userDomainName;
    }

    public void setUserDomainName(String userDomainName) {
        this.userDomainName = userDomainName;
    }

    @Override
    public CloudIdentity clone() {
        CloudIdentity cloudIdentityCopy = new CloudIdentity();

        cloudIdentityCopy.id = this.id;
        cloudIdentityCopy.identityUrl = this.identityUrl;
        cloudIdentityCopy.msoId = this.msoId;
        cloudIdentityCopy.msoPass = this.msoPass;
        cloudIdentityCopy.adminTenant = this.adminTenant;
        cloudIdentityCopy.memberRole = this.memberRole;
        cloudIdentityCopy.tenantMetadata = this.tenantMetadata;
        cloudIdentityCopy.identityServerType = this.identityServerType;
        cloudIdentityCopy.identityAuthenticationType = this.identityAuthenticationType;
        cloudIdentityCopy.projectDomainName = this.projectDomainName;
        cloudIdentityCopy.adminProjectDomainName = this.adminProjectDomainName;
        cloudIdentityCopy.userDomainName = this.userDomainName;

        return cloudIdentityCopy;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("id", getId())
                .append("identityUrl", getIdentityUrl()).append("msoId", getMsoId())
                .append("projectDomain", getProjectDomainName()).append("userDomain", getUserDomainName())
                .append("adminTenant", getAdminTenant()).append("memberRole", getMemberRole())
                .append("tenantMetadata", getTenantMetadata()).append("identityServerType", getIdentityServerType())
                .append("identityAuthenticationType", getIdentityAuthenticationType())
                .append("adminProjectDomainName", getAdminProjectDomainName()).toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (!getClass().equals(other.getClass())) {
            return false;
        }
        CloudIdentity castOther = (CloudIdentity) other;
        return new EqualsBuilder().append(getId(), castOther.getId())
                .append(getIdentityUrl(), castOther.getIdentityUrl()).append(getMsoId(), castOther.getMsoId())
                .append(getMsoPass(), castOther.getMsoPass()).append(getAdminTenant(), castOther.getAdminTenant())
                .append(getProjectDomainName(), castOther.getProjectDomainName())
                .append(getUserDomainName(), castOther.getUserDomainName())
                .append(getMemberRole(), castOther.getMemberRole())
                .append(getTenantMetadata(), castOther.getTenantMetadata())
                .append(getIdentityServerType(), castOther.getIdentityServerType())
                .append(getIdentityAuthenticationType(), castOther.getIdentityAuthenticationType())
                .append(getAdminProjectDomainName(), castOther.getAdminProjectDomainName()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(1, 31).append(getId()).append(getIdentityUrl()).append(getMsoId())
                .append(getMsoPass()).append(getProjectDomainName()).append(getUserDomainName())
                .append(getAdminTenant()).append(getMemberRole()).append(getTenantMetadata())
                .append(getIdentityServerType()).append(getIdentityAuthenticationType())
                .append(getAdminProjectDomainName()).toHashCode();
    }
}
