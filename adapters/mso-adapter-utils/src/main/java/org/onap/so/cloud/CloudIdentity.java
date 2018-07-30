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

package org.onap.so.cloud;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openpojo.business.annotation.BusinessKey;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Comparator;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * JavaBean JSON class for a CloudIdentity. This bean represents a cloud identity
 * service instance (i.e. a DCP node) in the NVP/AIC cloud. It will be loaded via
 * CloudConfig object, of which it is a component (a CloudConfig JSON configuration
 * file may contain multiple CloudIdentity definitions).
 *
 * Note that this is only used to access Cloud Configurations loaded from a
 * JSON config file, so there are no explicit setters.
 *
 */
public class CloudIdentity {
	
    @JsonProperty
    @BusinessKey
    private String id;
    @JsonProperty("identity_url")
    @BusinessKey
    private String identityUrl;
    @JsonProperty("mso_id")
    @BusinessKey
    private String msoId;
    @JsonProperty("mso_pass")
    @BusinessKey
    private String msoPass;
    @JsonProperty("admin_tenant")
    @BusinessKey
    private String adminTenant;
    @JsonProperty("member_role")
    @BusinessKey
    private String memberRole;
    @JsonProperty("tenant_metadata")
    @BusinessKey
    private Boolean tenantMetadata;
    @JsonProperty("identity_server_type")
    @BusinessKey
    private ServerType identityServerType;
    @JsonProperty("identity_authentication_type")
    @BusinessKey
    private AuthenticationType identityAuthenticationType;
    
    public CloudIdentity() {}

    public String getId () {
        return id;
    }

    public void setId (String id) {
        this.id = id;
    }

    public String getIdentityUrl() {
    	return this.identityUrl;
    }
    public void setIdentityUrl(String url) {
    	this.identityUrl = url;
    }

    public String getMsoId () {
        return msoId;
    }

    public void setMsoId (String id) {
        this.msoId = id;
    }

    public String getMsoPass () {
        return msoPass;
    }

    public void setMsoPass (String pwd) {
        this.msoPass = pwd;
    }

    public String getAdminTenant () {
        return adminTenant;
    }

    public void setAdminTenant (String tenant) {
        this.adminTenant = tenant;
    }

    public String getMemberRole () {
        return memberRole;
    }

    public void setMemberRole (String role) {
        this.memberRole = role;
    }

    public Boolean hasTenantMetadata () {
        return tenantMetadata;
    }

    public void setTenantMetadata (Boolean meta) {
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

		return cloudIdentityCopy;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("id", getId())
				.append("identityUrl", getIdentityUrl()).append("msoId", getMsoId())
				.append("adminTenant", getAdminTenant()).append("memberRole", getMemberRole())
				.append("tenantMetadata", hasTenantMetadata()).append("identityServerType", getIdentityServerType())
				.append("identityAuthenticationType", getIdentityAuthenticationType()).toString();
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
				.append(getMemberRole(), castOther.getMemberRole())
				.append(hasTenantMetadata(), castOther.hasTenantMetadata())
				.append(getIdentityServerType(), castOther.getIdentityServerType())
				.append(getIdentityAuthenticationType(), castOther.getIdentityAuthenticationType()).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(1, 31).append(getId()).append(getIdentityUrl()).append(getMsoId())
				.append(getMsoPass()).append(getAdminTenant()).append(getMemberRole()).append(hasTenantMetadata())
				.append(getIdentityServerType()).append(getIdentityAuthenticationType()).toHashCode();
	}
}