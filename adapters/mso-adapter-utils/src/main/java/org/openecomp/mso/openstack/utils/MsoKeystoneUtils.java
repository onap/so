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

package org.openecomp.mso.openstack.utils;


import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import java.util.Optional;
import org.openecomp.mso.cloud.CloudIdentity;
import org.openecomp.mso.cloud.CloudSite;
import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.openstack.beans.MsoTenant;
import org.openecomp.mso.openstack.exceptions.MsoAdapterException;
import org.openecomp.mso.openstack.exceptions.MsoCloudSiteNotFound;
import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.openstack.exceptions.MsoOpenstackException;
import org.openecomp.mso.openstack.exceptions.MsoTenantAlreadyExists;
import com.woorea.openstack.base.client.OpenStackBaseException;
import com.woorea.openstack.base.client.OpenStackConnectException;
import com.woorea.openstack.base.client.OpenStackRequest;
import com.woorea.openstack.base.client.OpenStackResponseException;
import com.woorea.openstack.keystone.Keystone;
import com.woorea.openstack.keystone.model.Access;
import com.woorea.openstack.keystone.model.Metadata;
import com.woorea.openstack.keystone.model.Role;
import com.woorea.openstack.keystone.model.Roles;
import com.woorea.openstack.keystone.model.Tenant;
import com.woorea.openstack.keystone.model.User;
import com.woorea.openstack.keystone.utils.KeystoneUtils;
import com.woorea.openstack.keystone.model.Authentication;

public class MsoKeystoneUtils extends MsoTenantUtils {

    // Cache the Keystone Clients statically. Since there is just one MSO user, there is no
    // benefit to re-authentication on every request (or across different flows). The
    // token will be used until it expires.
    //
    // The cache key is "cloudId"
    private static Map <String, KeystoneCacheEntry> adminClientCache = new HashMap<>();

	private static MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);
	String msoPropID;
	
    public MsoKeystoneUtils (String msoPropID) {
		super(msoPropID);
		this.msoPropID = msoPropID;
		LOGGER.debug("MsoKeyStoneUtils:" + msoPropID);
	}

    /**
     * Create a tenant with the specified name in the given cloud. If the tenant already exists,
     * an Exception will be thrown. The MSO User will also be added to the "member" list of
     * the new tenant to perform subsequent Nova/Heat commands in the tenant. If the MSO User
     * association fails, the entire transaction will be rolled back.
     * <p>
     * For the AIC Cloud (DCP/LCP): it is not clear that cloudId is needed, as all admin
     * requests go to the centralized identity service in DCP. However, if some artifact
     * must exist in each local LCP instance as well, then it will be needed to access the
     * correct region.
     * <p>
     *
     * @param tenantName The tenant name to create
     * @param cloudSiteId The cloud identifier (may be a region) in which to create the tenant.
     * @return the tenant ID of the newly created tenant
     * @throws MsoTenantAlreadyExists Thrown if the requested tenant already exists
     * @throws MsoOpenstackException Thrown if the Openstack API call returns an exception
     */
    public String createTenant (String tenantName,
                                String cloudSiteId,
                                Map <String, String> metadata,
                                boolean backout) throws MsoException {
        // Obtain the cloud site information where we will create the tenant
        Optional<CloudSite> cloudSiteOpt = cloudConfig.getCloudSite(cloudSiteId);
        if (!cloudSiteOpt.isPresent()) {
        	LOGGER.error(MessageEnum.RA_CREATE_TENANT_ERR, "MSOCloudSite not found", "", "", MsoLogger.ErrorCode.DataError, "MSOCloudSite not found");
            throw new MsoCloudSiteNotFound (cloudSiteId);
        }
        Keystone keystoneAdminClient = getKeystoneAdminClient(cloudSiteOpt.get());
        Tenant tenant = null;
        try {
            // Check if the tenant already exists
            tenant = findTenantByName (keystoneAdminClient, tenantName);

            if (tenant != null) {
                // Tenant already exists. Throw an exception
            	LOGGER.error(MessageEnum.RA_TENANT_ALREADY_EXIST, tenantName, cloudSiteId, "", "", MsoLogger.ErrorCode.DataError, "Tenant already exists");
                throw new MsoTenantAlreadyExists (tenantName, cloudSiteId);
            }

            // Does not exist, create a new one
            tenant = new Tenant ();
            tenant.setName (tenantName);
            tenant.setDescription ("SDN Tenant (via MSO)");
            tenant.setEnabled (true);

            OpenStackRequest <Tenant> request = keystoneAdminClient.tenants ().create (tenant);
            tenant = executeAndRecordOpenstackRequest (request, msoProps);
        } catch (OpenStackBaseException e) {
            // Convert Keystone OpenStackResponseException to MsoOpenstackException
            throw keystoneErrorToMsoException (e, "CreateTenant");
        } catch (RuntimeException e) {
            // Catch-all
            throw runtimeExceptionToMsoException (e, "CreateTenant");
        }

        // Add MSO User to the tenant as a member and
        // apply tenant metadata if supported by the cloud site
        try {
            CloudIdentity cloudIdentity = cloudSiteOpt.get().getIdentityService ();

            User msoUser = findUserByNameOrId (keystoneAdminClient, cloudIdentity.getMsoId ());
            Role memberRole = findRoleByNameOrId (keystoneAdminClient, cloudIdentity.getMemberRole ());

            OpenStackRequest <Void> request = keystoneAdminClient.tenants ().addUser (tenant.getId (),
                                                                                      msoUser.getId (),
                                                                                      memberRole.getId ());
            executeAndRecordOpenstackRequest (request, msoProps);

            if (cloudIdentity.hasTenantMetadata () && metadata != null && !metadata.isEmpty ()) {
                Metadata tenantMetadata = new Metadata ();
                tenantMetadata.setMetadata (metadata);

                OpenStackRequest <Metadata> metaRequest = keystoneAdminClient.tenants ()
                                                                             .createOrUpdateMetadata (tenant.getId (),
                                                                                                      tenantMetadata);
                executeAndRecordOpenstackRequest (metaRequest, msoProps);
            }
        } catch (Exception e) {
            // Failed to attach MSO User to the new tenant. Can't operate without access,
            // so roll back the tenant.
        	if (!backout)
        	{
        		LOGGER.warn(MessageEnum.RA_CREATE_TENANT_ERR, "Create Tenant errored, Tenant deletion suppressed", "Openstack", "", MsoLogger.ErrorCode.DataError, "Create Tenant error, Tenant deletion suppressed");
        	}
        	else
        	{
        		try {
        			OpenStackRequest <Void> request = keystoneAdminClient.tenants ().delete (tenant.getId ());
        			executeAndRecordOpenstackRequest (request, msoProps);
        		} catch (Exception e2) {
        			// Just log this one. We will report the original exception.
        			LOGGER.error (MessageEnum.RA_CREATE_TENANT_ERR, "Nested exception rolling back tenant", "Openstack", "", MsoLogger.ErrorCode.DataError, "Create Tenant error, Nested exception rolling back tenant", e2);
        		}
        	}
        	

            // Propagate the original exception on user/role/tenant mapping
            if (e instanceof OpenStackBaseException) {
                // Convert Keystone Exception to MsoOpenstackException
                throw keystoneErrorToMsoException ((OpenStackBaseException) e, "CreateTenantUser");
            } else {
                MsoAdapterException me = new MsoAdapterException (e.getMessage (), e);
                me.addContext ("CreateTenantUser");
                throw me;
            }
        }
        return tenant.getId ();
    }

    /**
     * Query for a tenant by ID in the given cloud. If the tenant exists,
     * return an MsoTenant object. If not, return null.
     * <p>
     * For the AIC Cloud (DCP/LCP): it is not clear that cloudId is needed, as all admin
     * requests go to the centralized identity service in DCP. However, if some artifact
     * must exist in each local LCP instance as well, then it will be needed to access the
     * correct region.
     * <p>
     *
     * @param tenantId The Openstack ID of the tenant to query
     * @param cloudSiteId The cloud identifier (may be a region) in which to query the tenant.
     * @return the tenant properties of the queried tenant, or null if not found
     * @throws MsoOpenstackException Thrown if the Openstack API call returns an exception
     */
    public MsoTenant queryTenant (String tenantId, String cloudSiteId) throws MsoException {
        // Obtain the cloud site information where we will query the tenant
        CloudSite cloudSite = cloudConfig.getCloudSite(cloudSiteId).orElseThrow(
                () -> new MsoCloudSiteNotFound(cloudSiteId));

        Keystone keystoneAdminClient = getKeystoneAdminClient (cloudSite);

        // Check if the tenant exists and return its Tenant Id
        try {
            Tenant tenant = findTenantById (keystoneAdminClient, tenantId);
            if (tenant == null) {
                return null;
            }

            Map <String, String> metadata = new HashMap<>();
            if (cloudSite.getIdentityService ().hasTenantMetadata ()) {
                OpenStackRequest <Metadata> request = keystoneAdminClient.tenants ().showMetadata (tenant.getId ());
                Metadata tenantMetadata = executeAndRecordOpenstackRequest (request, msoProps);
                if (tenantMetadata != null) {
                    metadata = tenantMetadata.getMetadata ();
                }
            }
            return new MsoTenant (tenant.getId (), tenant.getName (), metadata);
        } catch (OpenStackBaseException e) {
            // Convert Keystone OpenStackResponseException to MsoOpenstackException
            throw keystoneErrorToMsoException (e, "QueryTenant");
        } catch (RuntimeException e) {
            // Catch-all
            throw runtimeExceptionToMsoException (e, "QueryTenant");
        }
    }

    /**
     * Query for a tenant with the specified name in the given cloud. If the tenant exists,
     * return an MsoTenant object. If not, return null. This query is useful if the client
     * knows it has the tenant name, skipping an initial lookup by ID that would always fail.
     * <p>
     * For the AIC Cloud (DCP/LCP): it is not clear that cloudId is needed, as all admin
     * requests go to the centralized identity service in DCP. However, if some artifact
     * must exist in each local LCP instance as well, then it will be needed to access the
     * correct region.
     * <p>
     *
     * @param tenantName The name of the tenant to query
     * @param cloudSiteId The cloud identifier (may be a region) in which to query the tenant.
     * @return the tenant properties of the queried tenant, or null if not found
     * @throws MsoOpenstackException Thrown if the Openstack API call returns an exception
     */
    public MsoTenant queryTenantByName (String tenantName, String cloudSiteId) throws MsoException {
        // Obtain the cloud site information where we will query the tenant
        CloudSite cloudSite = cloudConfig.getCloudSite(cloudSiteId).orElseThrow(
                () -> new MsoCloudSiteNotFound(cloudSiteId));
        Keystone keystoneAdminClient = getKeystoneAdminClient (cloudSite);

        try {
            Tenant tenant = findTenantByName (keystoneAdminClient, tenantName);
            if (tenant == null) {
                return null;
            }

            Map <String, String> metadata = new HashMap<>();
            if (cloudSite.getIdentityService ().hasTenantMetadata ()) {
                OpenStackRequest <Metadata> request = keystoneAdminClient.tenants ().showMetadata (tenant.getId ());
                Metadata tenantMetadata = executeAndRecordOpenstackRequest (request, msoProps);
                if (tenantMetadata != null) {
                    metadata = tenantMetadata.getMetadata ();
                }
            }
            return new MsoTenant (tenant.getId (), tenant.getName (), metadata);
        } catch (OpenStackBaseException e) {
            // Convert Keystone OpenStackResponseException to MsoOpenstackException
            throw keystoneErrorToMsoException (e, "QueryTenantName");
        } catch (RuntimeException e) {
            // Catch-all
            throw runtimeExceptionToMsoException (e, "QueryTenantName");
        }
    }

    /**
     * Delete the specified Tenant (by ID) in the given cloud. This method returns true or
     * false, depending on whether the tenant existed and was successfully deleted, or if
     * the tenant already did not exist. Both cases are treated as success (no Exceptions).
     * <p>
     * Note for the AIC Cloud (DCP/LCP): all admin requests go to the centralized identity
     * service in DCP. So deleting a tenant from one cloudSiteId will remove it from all
     * sites managed by that identity service.
     * <p>
     *
     * @param tenantId The Openstack ID of the tenant to delete
     * @param cloudSiteId The cloud identifier from which to delete the tenant.
     * @return true if the tenant was deleted, false if the tenant did not exist.
     * @throws MsoOpenstackException If the Openstack API call returns an exception.
     */
    public boolean deleteTenant (String tenantId, String cloudSiteId) throws MsoException {
        // Obtain the cloud site information where we will query the tenant
        CloudSite cloudSite = cloudConfig.getCloudSite(cloudSiteId).orElseThrow(
                () -> new MsoCloudSiteNotFound(cloudSiteId));
        Keystone keystoneAdminClient = getKeystoneAdminClient (cloudSite);

        try {
            // Check that the tenant exists. Also, need the ID to delete
            Tenant tenant = findTenantById (keystoneAdminClient, tenantId);
            if (tenant == null) {
            	LOGGER.error(MessageEnum.RA_TENANT_NOT_FOUND, tenantId, cloudSiteId, "", "", MsoLogger.ErrorCode.DataError, "Tenant not found");
                return false;
            }

            OpenStackRequest <Void> request = keystoneAdminClient.tenants ().delete (tenant.getId ());
            executeAndRecordOpenstackRequest (request, msoProps);
            LOGGER.debug ("Deleted Tenant " + tenant.getId () + " (" + tenant.getName () + ")");

            // Clear any cached clients. Not really needed, ID will not be reused.
            MsoHeatUtils.expireHeatClient (tenant.getId (), cloudSiteId);
            MsoNeutronUtils.expireNeutronClient (tenant.getId (), cloudSiteId);
        } catch (OpenStackBaseException e) {
            // Convert Keystone OpenStackResponseException to MsoOpenstackException
            throw keystoneErrorToMsoException (e, "Delete Tenant");
        } catch (RuntimeException e) {
            // Catch-all
            throw runtimeExceptionToMsoException (e, "DeleteTenant");
        }

        return true;
    }

    // -------------------------------------------------------------------
    // PRIVATE UTILITY FUNCTIONS FOR USE WITHIN THIS CLASS

    /*
     * Get a Keystone Admin client for the Openstack Identity service.
     * This requires an 'admin'-level userId + password along with an 'admin' tenant
     * in the target cloud. These values will be retrieved from properties based
     * on the specified cloud ID.
     * <p>
     * On successful authentication, the Keystone object will be cached for the cloudId
     * so that it can be reused without going back to Openstack every time.
     *
     * @param cloudId
     *
     * @return an authenticated Keystone object
     */
    public Keystone getKeystoneAdminClient (CloudSite cloudSite) throws MsoException {
        CloudIdentity cloudIdentity = cloudSite.getIdentityService ();

        String cloudId = cloudIdentity.getId ();
        String adminTenantName = cloudIdentity.getAdminTenant ();
        String region = cloudSite.getRegionId ();

        // Check first in the cache of previously authorized clients
        KeystoneCacheEntry entry = adminClientCache.get (cloudId);
        if (entry != null) {
            if (!entry.isExpired ()) {
                return entry.getKeystoneClient ();
            } else {
                // Token is expired. Remove it from cache.
                adminClientCache.remove (cloudId);
            }
        }

        Keystone keystone = new Keystone (cloudIdentity.getKeystoneUrl (region, msoPropID));

        // Must authenticate against the 'admin' tenant to get the services endpoints
        Access access = null;
        String token = null;
        try {
        	Authentication credentials = cloudIdentity.getAuthentication ();
            OpenStackRequest <Access> request = keystone.tokens ()
                                                        .authenticate (credentials)
                                                        .withTenantName (adminTenantName);
            access = executeAndRecordOpenstackRequest (request, msoProps);
            token = access.getToken ().getId ();
        } catch (OpenStackResponseException e) {
            if (e.getStatus () == 401) {
                // Authentication error. Can't access admin tenant - something is mis-configured
                String error = "MSO Authentication Failed for " + cloudIdentity.getId ();
                alarmLogger.sendAlarm ("MsoAuthenticationError", MsoAlarmLogger.CRITICAL, error);
                throw new MsoAdapterException (error);
            } else {
                throw keystoneErrorToMsoException (e, "TokenAuth");
            }
        } catch (OpenStackConnectException e) {
            // Connection to Openstack failed
            throw keystoneErrorToMsoException (e, "TokenAuth");
        }

        // Get the Identity service URL. Throws runtime exception if not found per region.
        String adminUrl = null;
        try {
            adminUrl = KeystoneUtils.findEndpointURL (access.getServiceCatalog (), "identity", region, "admin");
        } catch (RuntimeException e) {
            String error = "Identity service not found: region=" + region + ",cloud=" + cloudIdentity.getId ();
            alarmLogger.sendAlarm ("MsoConfigurationError", MsoAlarmLogger.CRITICAL, error);
            LOGGER.error(MessageEnum.IDENTITY_SERVICE_NOT_FOUND, region, cloudIdentity.getId(), "Openstack", "", MsoLogger.ErrorCode.DataError, "Exception in findEndpointURL");
            throw new MsoAdapterException (error, e);
        }

        // A new Keystone object is required for the new URL. Use the auth token from above.
        // Note: this doesn't go back to Openstack, it's just a local object.
        keystone = new Keystone (adminUrl);
        keystone.token (token);

        // Cache to avoid re-authentication for every call.
        KeystoneCacheEntry cacheEntry = new KeystoneCacheEntry (adminUrl, token, access.getToken ().getExpires ());
        adminClientCache.put (cloudId, cacheEntry);

        return keystone;
    }

    /*
     * Find a tenant (or query its existance) by its Id.
     *
     * @param adminClient an authenticated Keystone object
     *
     * @param tenantName the tenant ID to query
     *
     * @return a Tenant object or null if not found
     */
    private Tenant findTenantById (Keystone adminClient, String tenantId) {
        if (tenantId == null) {
            return null;
        }

        try {
            OpenStackRequest <Tenant> request = adminClient.tenants ().show (tenantId);
            return executeAndRecordOpenstackRequest (request, msoProps);
        } catch (OpenStackResponseException e) {
            if (e.getStatus () == 404) {
                return null;
            } else {
            	LOGGER.error(MessageEnum.RA_CONNECTION_EXCEPTION, "Openstack Error, GET Tenant by Id (" + tenantId + "): " + e, "Openstack", "", MsoLogger.ErrorCode.DataError, "Exception in Openstack GET tenant by Id");
                throw e;
            }
        }
    }

    /*
     * Find a tenant (or query its existance) by its Name. This method avoids an
     * initial lookup by ID when it's known that we have the tenant Name.
     *
     * @param adminClient an authenticated Keystone object
     *
     * @param tenantName the tenant name to query
     *
     * @return a Tenant object or null if not found
     */
    public Tenant findTenantByName (Keystone adminClient, String tenantName) {
        if (tenantName == null) {
            return null;
        }

        try {
            OpenStackRequest <Tenant> request = adminClient.tenants ().show ("").queryParam ("name", tenantName);
            return executeAndRecordOpenstackRequest (request, msoProps);
        } catch (OpenStackResponseException e) {
            if (e.getStatus () == 404) {
                return null;
            } else {
            	LOGGER.error (MessageEnum.RA_CONNECTION_EXCEPTION, "Openstack Error, GET Tenant By Name (" + tenantName + "): " + e, "Openstack", "", MsoLogger.ErrorCode.DataError, "Exception in Openstack GET Tenant By Name");
                throw e;
            }
        }
    }

    /*
     * Look up an Openstack User by Name or Openstack ID. Check the ID first, and if that
     * fails, try the Name.
     *
     * @param adminClient an authenticated Keystone object
     *
     * @param userName the user name or ID to query
     *
     * @return a User object or null if not found
     */
    private User findUserByNameOrId (Keystone adminClient, String userNameOrId) {
        if (userNameOrId == null) {
            return null;
        }

        try {
            OpenStackRequest <User> request = adminClient.users ().show (userNameOrId);
            return executeAndRecordOpenstackRequest (request, msoProps);
        } catch (OpenStackResponseException e) {
            if (e.getStatus () == 404) {
                // Not found by ID. Search for name
                return findUserByName (adminClient, userNameOrId);
            } else {
            	LOGGER.error (MessageEnum.RA_CONNECTION_EXCEPTION, "Openstack Error, GET User (" + userNameOrId + "): " + e, "Openstack", "", MsoLogger.ErrorCode.DataError, "Exception in Openstack GET User");
                throw e;
            }
        }
    }

    /*
     * Look up an Openstack User by Name. This avoids initial Openstack query by ID
     * if we know we have the User Name.
     *
     * @param adminClient an authenticated Keystone object
     *
     * @param userName the user name to query
     *
     * @return a User object or null if not found
     */
    public User findUserByName (Keystone adminClient, String userName) {
        if (userName == null) {
            return null;
        }

        try {
            OpenStackRequest <User> request = adminClient.users ().show ("").queryParam ("name", userName);
            return executeAndRecordOpenstackRequest (request, msoProps);
        } catch (OpenStackResponseException e) {
            if (e.getStatus () == 404) {
                return null;
            } else {
            	LOGGER.error (MessageEnum.RA_CONNECTION_EXCEPTION, "Openstack Error, GET User By Name (" + userName + "): " + e, "Openstack", "", MsoLogger.ErrorCode.DataError, "Exception in Openstack GET User By Name");
                throw e;
            }
        }
    }

    /*
     * Look up an Openstack Role by Name or Id. There is no direct query for Roles, so
     * need to retrieve a full list from Openstack and look for a match. By default,
     * Openstack should have a "_member_" role for normal VM-level privileges and an
     * "admin" role for expanded privileges (e.g. administer tenants, users, and roles).
     * <p>
     *
     * @param adminClient an authenticated Keystone object
     *
     * @param roleNameOrId the Role name or ID to look up
     *
     * @return a Role object
     */
    private  Role findRoleByNameOrId (Keystone adminClient, String roleNameOrId) {
        if (roleNameOrId == null) {
            return null;
        }

        // Search by name or ID. Must search in list
        OpenStackRequest <Roles> request = adminClient.roles ().list ();
        Roles roles = executeAndRecordOpenstackRequest (request, msoProps);

        for (Role role : roles) {
            if (roleNameOrId.equals (role.getName ()) || roleNameOrId.equals (role.getId ())) {
                return role;
            }
        }

        return null;
    }

    private static class KeystoneCacheEntry implements Serializable {

        private static final long serialVersionUID = 1L;

        private String keystoneUrl;
        private String token;
        private Calendar expires;

        public KeystoneCacheEntry (String url, String token, Calendar expires) {
            this.keystoneUrl = url;
            this.token = token;
            this.expires = expires;
        }

        public Keystone getKeystoneClient () {
            Keystone keystone = new Keystone (keystoneUrl);
            keystone.token (token);
            return keystone;
        }

        public boolean isExpired () {
            if (expires == null) {
                return true;
            }

            return System.currentTimeMillis() > expires.getTimeInMillis();
        }
    }

    /**
     * Clean up the Admin client cache to remove expired entries.
     */
    public static void adminCacheCleanup () {
        for (String cacheKey : adminClientCache.keySet ()) {
            if (adminClientCache.get (cacheKey).isExpired ()) {
                adminClientCache.remove (cacheKey);
                LOGGER.debug ("Cleaned Up Cached Admin Client for " + cacheKey);
            }
        }
    }

    /**
     * Reset the Admin client cache.
     * This may be useful if cached credentials get out of sync.
     */
    public static void adminCacheReset () {
        adminClientCache = new HashMap<>();
    }

	@Override
	public String getKeystoneUrl(String regionId, String msoPropID, CloudIdentity cloudIdentity) {
		return cloudIdentity.getIdentityUrl();
	}
}
