/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

package org.openecomp.mso.cloud;

import java.security.GeneralSecurityException;

import org.codehaus.jackson.annotate.JsonProperty;
import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import com.woorea.openstack.keystone.model.authentication.RackspaceAuthentication;
import com.woorea.openstack.keystone.model.authentication.UsernamePassword;
import org.openecomp.mso.utils.CryptoUtils;
import com.woorea.openstack.keystone.model.Authentication;

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

    private static MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);
    
    public enum IdentityServerType {KEYSTONE};

    public enum IdentityAuthenticationType { USERNAME_PASSWORD, RACKSPACE_APIKEY };
    
    @JsonProperty
    private String id;
    @JsonProperty("identity_url")
    private String identityUrl;
    @JsonProperty("mso_id")
    private String msoId;
    @JsonProperty("mso_pass")
    private String msoPass;
    @JsonProperty("admin_tenant")
    private String adminTenant;
    @JsonProperty("member_role")
    private String memberRole;
    @JsonProperty("tenant_metadata")
    private Boolean tenantMetadata;
    @JsonProperty("identity_server_type")
    private IdentityServerType identityServerType;
    @JsonProperty("identity_authentication_type")
    private IdentityAuthenticationType identityAuthenticationType;
    
    private static String cloudKey = "aa3871669d893c7fb8abbcda31b88b4f";

    public CloudIdentity () {
    }

    public String getId () {
        return id;
    }

    public void setId (String id) {
        this.id = id;
    }

    //DEPRECATED
    public String getKeystoneUrl () throws MsoException {
    	if (this.identityServerType.equals(IdentityServerType.KEYSTONE))
    		return this.identityUrl;
    	else
    		return null;
    }
    
    public String getKeystoneUrl (String regionId, String msoPropID) throws MsoException {
    	if (IdentityServerType.KEYSTONE.equals(this.identityServerType)) {
    		return this.identityUrl;
    	}
    	else {
    		return null;
    	}
    }
    
    public Authentication getAuthentication () throws MsoException {
    	if (IdentityAuthenticationType.RACKSPACE_APIKEY.equals(this.identityAuthenticationType)) {
    		return new RackspaceAuthentication (this.getMsoId (),this.getMsoPass ());
    	}
    	else {
    		// Use default case
    		return new UsernamePassword (this.getMsoId (),this.getMsoPass ());
    	}
    		
    }

    public void setKeystoneUrl (String url) {
    	if (IdentityServerType.KEYSTONE.equals(this.identityServerType)) {
    		this.identityUrl = url;	
    	}
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
        try {
            return CryptoUtils.decrypt (msoPass, cloudKey);
        } catch (GeneralSecurityException e) {
            LOGGER.error (MessageEnum.RA_GENERAL_EXCEPTION, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Exception in getMsoPass", e);
            return null;
        }
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

    public boolean hasTenantMetadata () {
        return tenantMetadata;
    }

    public void setTenantMetadata (boolean meta) {
        this.tenantMetadata = meta;
    }
    
    public IdentityServerType getIdentityServerType() {
    	return this.identityServerType;
    }
    public void setIdentityServerType(IdentityServerType ist) {
    	this.identityServerType = ist;
    }
    public String getIdentityServerTypeAsString() {
    	return this.identityServerType.toString();
    }
    /**
	 * @return the identityAuthenticationType
	 */
	public IdentityAuthenticationType getIdentityAuthenticationType() {
		return identityAuthenticationType;
	}

	/**
	 * @param identityAuthenticationType the identityAuthenticationType to set
	 */
	public void setIdentityAuthenticationType(IdentityAuthenticationType identityAuthenticationType) {
		this.identityAuthenticationType = identityAuthenticationType;
	}
	
	@Override
    public String toString () {
        StringBuilder stringBuilder = new StringBuilder ();
        stringBuilder.append ("Cloud Identity Service: id=")
                     .append (id)
                     .append (", identityUrl=")
                     .append (this.identityUrl)
                     .append (", msoId=")
                     .append (msoId)
                     .append (", adminTenant=")
                     .append (adminTenant)
                     .append (", memberRole=")
                     .append (memberRole)
                     .append (", tenantMetadata=")
                     .append (tenantMetadata)
                     .append (", identityServerType=")
                     .append (identityServerType.toString())
                     .append (", identityAuthenticationType=")
                     .append (identityAuthenticationType.toString());
        
        return stringBuilder.toString ();
    }

    public static String encryptPassword (String msoPass) {
        try {
            return CryptoUtils.encrypt (msoPass, cloudKey);
        } catch (GeneralSecurityException e) {
            LOGGER.error (MessageEnum.RA_GENERAL_EXCEPTION, "", "", MsoLogger.ErrorCode.BusinessProcesssError, "Exception in encryptPassword", e);
            return null;
        }
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((adminTenant == null) ? 0 : adminTenant.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((identityUrl == null) ? 0 : identityUrl.hashCode());
		result = prime * result + ((memberRole == null) ? 0 : memberRole.hashCode());
		result = prime * result + ((msoId == null) ? 0 : msoId.hashCode());
		result = prime * result + ((msoPass == null) ? 0 : msoPass.hashCode());
		result = prime * result + ((tenantMetadata == null) ? 0 : tenantMetadata.hashCode());
		result = prime * result + ((identityServerType == null) ? 0 : identityServerType.hashCode());
		result = prime * result + ((identityAuthenticationType == null) ? 0 : identityAuthenticationType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CloudIdentity other = (CloudIdentity) obj;
		if (adminTenant == null) {
			if (other.adminTenant != null)
				return false;
		} else if (!adminTenant.equals(other.adminTenant))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (identityUrl == null) {
			if (other.identityUrl != null)
				return false;
		} else if (!identityUrl.equals(other.identityUrl))
			return false;
		if (memberRole == null) {
			if (other.memberRole != null)
				return false;
		} else if (!memberRole.equals(other.memberRole))
			return false;
		if (msoId == null) {
			if (other.msoId != null)
				return false;
		} else if (!msoId.equals(other.msoId))
			return false;
		if (msoPass == null) {
			if (other.msoPass != null)
				return false;
		} else if (!msoPass.equals(other.msoPass))
			return false;
		if (tenantMetadata == null) {
			if (other.tenantMetadata != null)
				return false;
		} else if (!tenantMetadata.equals(other.tenantMetadata))
			return false;
		if (identityServerType == null) {
			if (other.getIdentityServerType() != null)
				return false;
		} else if (!identityServerType.equals(other.getIdentityServerType()))
			return false;
		if (identityAuthenticationType == null) {
			if (other.getIdentityAuthenticationType() != null)
				return false;
		} else if (!identityAuthenticationType.equals(other.getIdentityAuthenticationType()))
			return false;
		
		return true;
	}
}
