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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.mso.cloud.CloudConfig;
import org.openecomp.mso.cloud.CloudConfigFactory;
import org.openecomp.mso.cloud.CloudIdentity;
import org.openecomp.mso.cloud.CloudSite;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoAlarmLogger;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.openstack.beans.NetworkInfo;
import org.openecomp.mso.openstack.exceptions.MsoAdapterException;
import org.openecomp.mso.openstack.exceptions.MsoCloudSiteNotFound;
import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.openstack.exceptions.MsoIOException;
import org.openecomp.mso.openstack.exceptions.MsoNetworkAlreadyExists;
import org.openecomp.mso.openstack.exceptions.MsoNetworkNotFound;
import org.openecomp.mso.openstack.exceptions.MsoOpenstackException;
import com.woorea.openstack.base.client.OpenStackBaseException;
import com.woorea.openstack.base.client.OpenStackConnectException;
import com.woorea.openstack.base.client.OpenStackRequest;
import com.woorea.openstack.base.client.OpenStackResponseException;
import com.woorea.openstack.keystone.Keystone;
import com.woorea.openstack.keystone.model.Access;
import com.woorea.openstack.keystone.utils.KeystoneUtils;
import com.woorea.openstack.quantum.Quantum;
import com.woorea.openstack.quantum.model.Network;
import com.woorea.openstack.quantum.model.Networks;
import com.woorea.openstack.quantum.model.Segment;
import com.woorea.openstack.keystone.model.Authentication;

public class MsoNeutronUtils extends MsoCommonUtils
{
	// Cache Neutron Clients statically.  Since there is just one MSO user, there is no
	// benefit to re-authentication on every request (or across different flows).  The
	// token will be used until it expires.
	//
	// The cache key is "tenantId:cloudId"
	private static Map<String,NeutronCacheEntry> neutronClientCache = new HashMap<>();

	// Fetch cloud configuration each time (may be cached in CloudConfig class)
	private CloudConfig cloudConfig;

	private static MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);
	private String msoPropID;

	public enum NetworkType {
		BASIC, PROVIDER, MULTI_PROVIDER
	};

	public MsoNeutronUtils(String msoPropID, CloudConfigFactory cloudConfigFactory) {
		cloudConfig = cloudConfigFactory.getCloudConfig();
		this.msoPropID = msoPropID;
	}

	/**
	 * Create a network with the specified parameters in the given cloud/tenant.
	 *
	 * If a network already exists with the same name, an exception will be thrown.  Note that
	 * this is an MSO-imposed restriction.  Openstack does not require uniqueness on network names.
	 * <p>
	 * @param cloudSiteId The cloud identifier (may be a region) in which to create the network.
	 * @param tenantId The tenant in which to create the network
	 * @param type The type of network to create (Basic, Provider, Multi-Provider)
	 * @param networkName The network name to create
	 * @param provider The provider network name (for Provider or Multi-Provider networks)
	 * @param vlans A list of VLAN segments for the network (for Provider or Multi-Provider networks)
	 * @return a NetworkInfo object which describes the newly created network
	 * @throws MsoNetworkAlreadyExists Thrown if a network with the same name already exists
	 * @throws MsoOpenstackException Thrown if the Openstack API call returns an exception
	 * @throws MsoCloudSiteNotFound Thrown if the cloudSite is invalid or unknown
	 */
	public NetworkInfo createNetwork (String cloudSiteId, String tenantId, NetworkType type, String networkName, String provider, List<Integer> vlans)
            throws MsoException
	{
		// Obtain the cloud site information where we will create the stack
        CloudSite cloudSite = cloudConfig.getCloudSite(cloudSiteId).orElseThrow(
                () -> new MsoCloudSiteNotFound(cloudSiteId));

		Quantum neutronClient = getNeutronClient (cloudSite, tenantId);

		// Check if a network already exists with this name
		// Openstack will allow duplicate name, so require explicit check
		Network network = findNetworkByName (neutronClient, networkName);

		if (network != null) {
			// Network already exists.  Throw an exception
			LOGGER.error(MessageEnum.RA_NETWORK_ALREADY_EXIST, networkName, cloudSiteId, tenantId, "Openstack", "", MsoLogger.ErrorCode.DataError, "Network already exists");
			throw new MsoNetworkAlreadyExists (networkName, tenantId, cloudSiteId);
		}

		// Does not exist, create a new one
		network = new Network();
		network.setName(networkName);
		network.setAdminStateUp(true);

		if (type == NetworkType.PROVIDER) {
			if (provider != null && vlans != null && vlans.size() > 0) {
				network.setProviderPhysicalNetwork (provider);
				network.setProviderNetworkType("vlan");
				network.setProviderSegmentationId (vlans.get(0));
			}
		} else if (type == NetworkType.MULTI_PROVIDER) {
			if (provider != null && vlans != null && vlans.size() > 0) {
				List<Segment> segments = new ArrayList<>(vlans.size());
				for (int vlan : vlans) {
					Segment segment = new Segment();
					segment.setProviderPhysicalNetwork (provider);
					segment.setProviderNetworkType("vlan");
					segment.setProviderSegmentationId (vlan);

					segments.add(segment);
				}
				network.setSegments(segments);
			}
		}

		try {
			OpenStackRequest<Network> request = neutronClient.networks().create(network);
			Network newNetwork = executeAndRecordOpenstackRequest(request);
			return new NetworkInfo(newNetwork);
		}
		catch (OpenStackBaseException e) {
			// Convert Neutron exception to an MsoOpenstackException
			MsoException me = neutronExceptionToMsoException (e, "CreateNetwork");
			throw me;
		}
		catch (RuntimeException e) {
			// Catch-all
			MsoException me = runtimeExceptionToMsoException(e, "CreateNetwork");
			throw me;
		}
	}


	/**
	 * Query for a network with the specified name or ID in the given cloud.  If the network exists,
	 * return an NetworkInfo object.  If not, return null.
	 * <p>
	 * Whenever possible, the network ID should be used as it is much more efficient.  Query by
	 * name requires retrieval of all networks for the tenant and search for matching name.
	 * <p>
	 * @param networkNameOrId The network to query
	 * @param tenantId The Openstack tenant to look in for the network
	 * @param cloudSiteId The cloud identifier (may be a region) in which to query the network.
	 * @return a NetworkInfo object describing the queried network, or null if not found
	 * @throws MsoOpenstackException Thrown if the Openstack API call returns an exception
	 * @throws MsoCloudSiteNotFound
	 */
    public NetworkInfo queryNetwork(String networkNameOrId, String tenantId, String cloudSiteId) throws MsoException
	{
		LOGGER.debug("In queryNetwork");

		// Obtain the cloud site information
        CloudSite cloudSite = cloudConfig.getCloudSite(cloudSiteId).orElseThrow(
                () -> new MsoCloudSiteNotFound(cloudSiteId));

		Quantum neutronClient = getNeutronClient (cloudSite, tenantId);
		// Check if the network exists and return its info
		try {
			Network network = findNetworkByNameOrId (neutronClient, networkNameOrId);
			if (network == null) {
				LOGGER.debug ("Query Network: " + networkNameOrId + " not found in tenant " + tenantId);
				return null;
			}
			return new NetworkInfo(network);
		}
		catch (OpenStackBaseException e) {
			// Convert Neutron exception to an MsoOpenstackException
			MsoException me = neutronExceptionToMsoException (e, "QueryNetwork");
			throw me;
		}
		catch (RuntimeException e) {
			// Catch-all
			MsoException me = runtimeExceptionToMsoException(e, "QueryNetwork");
			throw me;
		}
	}

	/**
	 * Delete the specified Network (by ID) in the given cloud.
	 * If the network does not exist, success is returned.
	 * <p>
	 * @param networkId Openstack ID of the network to delete
	 * @param tenantId The Openstack tenant.
	 * @param cloudSiteId The cloud identifier (may be a region) from which to delete the network.
	 * @return true if the network was deleted, false if the network did not exist
	 * @throws MsoOpenstackException If the Openstack API call returns an exception, this local
	 * exception will be thrown.
	 * @throws MsoCloudSiteNotFound
	 */
    public boolean deleteNetwork(String networkId, String tenantId, String cloudSiteId) throws MsoException
	{
		// Obtain the cloud site information where we will create the stack
        CloudSite cloudSite = cloudConfig.getCloudSite(cloudSiteId).orElseThrow(
                () -> new MsoCloudSiteNotFound(cloudSiteId));
		Quantum neutronClient = getNeutronClient (cloudSite, tenantId);
		try {
			// Check that the network exists.
			Network network = findNetworkById (neutronClient, networkId);
			if (network == null) {
				LOGGER.info(MessageEnum.RA_DELETE_NETWORK_EXC, networkId, cloudSiteId, tenantId, "Openstack", "");
				return false;
			}

			OpenStackRequest<Void> request = neutronClient.networks().delete(network.getId());
			executeAndRecordOpenstackRequest(request);

			LOGGER.debug ("Deleted Network " + network.getId() + " (" + network.getName() + ")");
		}
		catch (OpenStackBaseException e) {
			// Convert Neutron exception to an MsoOpenstackException
			MsoException me = neutronExceptionToMsoException (e, "Delete Network");
			throw me;
		}
		catch (RuntimeException e) {
			// Catch-all
			MsoException me = runtimeExceptionToMsoException(e, "DeleteNetwork");
			throw me;
		}

		return true;
	}


	/**
	 * Update a network with the specified parameters in the given cloud/tenant.
	 *
	 * Specifically, this call is intended to update the VLAN segments on a
	 * multi-provider network.  The provider segments will be replaced with the
	 * supplied list of VLANs.
	 * <p>
	 * Note that updating the 'segments' array is not normally supported by Neutron.
	 * This method relies on a Platform Orchestration extension (using SDN controller
	 * to manage the virtual networking).
	 *
	 * @param cloudSiteId The cloud site ID (may be a region) in which to update the network.
	 * @param tenantId Openstack ID of the tenant in which to update the network
	 * @param networkId The unique Openstack ID of the network to be updated
	 * @param type The network type (Basic, Provider, Multi-Provider)
	 * @param provider The provider network name.  This should not change.
	 * @param vlans The list of VLAN segments to replace
	 * @return a NetworkInfo object which describes the updated network
	 * @throws MsoNetworkNotFound Thrown if the requested network does not exist
	 * @throws MsoOpenstackException Thrown if the Openstack API call returns an exception
	 * @throws MsoCloudSiteNotFound
	 */
	public NetworkInfo updateNetwork (String cloudSiteId, String tenantId, String networkId, NetworkType type, String provider, List<Integer> vlans)
            throws MsoException
	{
		// Obtain the cloud site information where we will create the stack
        CloudSite cloudSite = cloudConfig.getCloudSite(cloudSiteId).orElseThrow(
                () -> new MsoCloudSiteNotFound(cloudSiteId));
		Quantum neutronClient = getNeutronClient (cloudSite, tenantId);
		// Check that the network exists
		Network network = findNetworkById (neutronClient, networkId);

		if (network == null) {
			// Network not found.  Throw an exception
			LOGGER.error(MessageEnum.RA_NETWORK_NOT_FOUND, networkId, cloudSiteId, tenantId, "Openstack", "", MsoLogger.ErrorCode.DataError, "Network not found");
			throw new MsoNetworkNotFound (networkId, tenantId, cloudSiteId);
		}

		// Overwrite the properties to be updated
		if (type == NetworkType.PROVIDER) {
			if (provider != null && vlans != null && vlans.size() > 0) {
				network.setProviderPhysicalNetwork (provider);
				network.setProviderNetworkType("vlan");
				network.setProviderSegmentationId (vlans.get(0));
			}
		} else if (type == NetworkType.MULTI_PROVIDER) {
			if (provider != null && vlans != null && vlans.size() > 0) {
				List<Segment> segments = new ArrayList<>(vlans.size());
				for (int vlan : vlans) {
					Segment segment = new Segment();
					segment.setProviderPhysicalNetwork (provider);
					segment.setProviderNetworkType("vlan");
					segment.setProviderSegmentationId (vlan);

					segments.add(segment);
				}
				network.setSegments(segments);
			}
		}

		try {
			OpenStackRequest<Network> request = neutronClient.networks().update(network);
			Network newNetwork = executeAndRecordOpenstackRequest(request);
			return new NetworkInfo(newNetwork);
		}
		catch (OpenStackBaseException e) {
			// Convert Neutron exception to an MsoOpenstackException
			MsoException me = neutronExceptionToMsoException (e, "UpdateNetwork");
			throw me;
		}
		catch (RuntimeException e) {
			// Catch-all
			MsoException me = runtimeExceptionToMsoException(e, "UpdateNetwork");
			throw me;
		}
	}


	// -------------------------------------------------------------------
	// PRIVATE UTILITY FUNCTIONS FOR USE WITHIN THIS CLASS

	/**
	 * Get a Neutron (Quantum) client for the Openstack Network service.
	 * This requires a 'member'-level userId + password, which will be retrieved from
	 * properties based on the specified cloud Id.  The tenant in which to operate
	 * must also be provided.
	 * <p>
	 * On successful authentication, the Quantum object will be cached for the
	 * tenantID + cloudId so that it can be reused without reauthenticating with
	 *  Openstack every time.
	 *
	 * @param cloudSite - a cloud site definition
	 * @param tenantId - Openstack tenant ID
	 * @return an authenticated Quantum object
	 */
    private Quantum getNeutronClient(CloudSite cloudSite, String tenantId) throws MsoException
	{
		String cloudId = cloudSite.getId();

		// Check first in the cache of previously authorized clients
		String cacheKey = cloudId + ":" + tenantId;
		if (neutronClientCache.containsKey(cacheKey)) {
			if (! neutronClientCache.get(cacheKey).isExpired()) {
				LOGGER.debug ("Using Cached HEAT Client for " + cacheKey);
				Quantum neutronClient = neutronClientCache.get(cacheKey).getNeutronClient();
				return neutronClient;
			}
			else {
				// Token is expired.  Remove it from cache.
				neutronClientCache.remove(cacheKey);
				LOGGER.debug ("Expired Cached Neutron Client for " + cacheKey);
			}
		}

		// Obtain an MSO token for the tenant from the identity service
		CloudIdentity cloudIdentity = cloudSite.getIdentityService();
		Keystone keystoneTenantClient = new Keystone (cloudIdentity.getKeystoneUrl(cloudId, msoPropID));
		Access access = null;
		try {
			Authentication credentials = cloudIdentity.getAuthentication ();
			OpenStackRequest<Access> request = keystoneTenantClient.tokens().authenticate(credentials).withTenantId(tenantId);
			access = executeAndRecordOpenstackRequest(request);
		}
		catch (OpenStackResponseException e) {
			if (e.getStatus() == 401) {
				// Authentication error.
				String error = "Authentication Failure: tenant=" + tenantId + ",cloud=" + cloudIdentity.getId();
				alarmLogger .sendAlarm("MsoAuthenticationError", MsoAlarmLogger.CRITICAL, error);
				throw new MsoAdapterException(error);
			}
			else {
				MsoException me = keystoneErrorToMsoException(e, "TokenAuth");
				throw me;
			}
		}
		catch (OpenStackConnectException e) {
			// Connection to Openstack failed
			MsoIOException me = new MsoIOException (e.getMessage(), e);
			me.addContext("TokenAuth");
			throw me;
		}
		catch (RuntimeException e) {
			// Catch-all
			MsoException me = runtimeExceptionToMsoException(e, "TokenAuth");
			throw me;
		}

		String region = cloudSite.getRegionId();
		String neutronUrl = null;
		try {
			neutronUrl = KeystoneUtils.findEndpointURL(access.getServiceCatalog(), "network", region, "public");
			if (! neutronUrl.endsWith("/")) {
                neutronUrl += "/v2.0/";
            }
		} catch (RuntimeException e) {
			// This comes back for not found (probably an incorrect region ID)
			String error = "Network service not found: region=" + region + ",cloud=" + cloudIdentity.getId();
			alarmLogger.sendAlarm("MsoConfigurationError", MsoAlarmLogger.CRITICAL, error);
			throw new MsoAdapterException (error, e);
		}

		Quantum neutronClient = new Quantum(neutronUrl);
		neutronClient.token(access.getToken().getId());

		neutronClientCache.put(cacheKey, new NeutronCacheEntry(neutronUrl, access.getToken().getId(), access.getToken().getExpires()));
		LOGGER.debug ("Caching Neutron Client for " + cacheKey);

		return neutronClient;
	}

	/**
	 * Forcibly expire a Neutron client from the cache.  This call is for use by
	 * the KeystoneClient in case where a tenant is deleted.  In that case,
	 * all cached credentials must be purged so that fresh authentication is
	 * done on subsequent calls.
	 */
	public static void expireNeutronClient (String tenantId, String cloudId) {
		String cacheKey = cloudId + ":" + tenantId;
		if (neutronClientCache.containsKey(cacheKey)) {
			neutronClientCache.remove(cacheKey);
			LOGGER.debug ("Deleted Cached Neutron Client for " + cacheKey);
		}
	}


	/*
	 * Find a tenant (or query its existence) by its Name or Id.  Check first against the
	 * ID.  If that fails, then try by name.
	 *
	 * @param adminClient an authenticated Keystone object
	 * @param tenantName the tenant name or ID to query
	 * @return a Tenant object or null if not found
	 */
	public Network findNetworkByNameOrId (Quantum neutronClient, String networkNameOrId)
	{
		if (networkNameOrId == null) {
            return null;
        }

		Network network = findNetworkById(neutronClient, networkNameOrId);

		if (network == null) {
            network = findNetworkByName(neutronClient, networkNameOrId);
        }

		return network;
	}

	/*
	 * Find a network (or query its existence) by its Id.
	 *
	 * @param neutronClient an authenticated Quantum object
	 * @param networkId the network ID to query
	 * @return a Network object or null if not found
	 */
	private static Network findNetworkById (Quantum neutronClient, String networkId)
	{
		if (networkId == null) {
            return null;
        }

		try {
			OpenStackRequest<Network> request = neutronClient.networks().show(networkId);
			Network network = executeAndRecordOpenstackRequest(request);
			return network;
		}
		catch (OpenStackResponseException e) {
			if (e.getStatus() == 404) {
				return null;
			} else {
				LOGGER.error (MessageEnum.RA_CONNECTION_EXCEPTION, "OpenStack", "Openstack Error, GET Network By ID (" + networkId + "): " + e, "Openstack", "", MsoLogger.ErrorCode.DataError, "Exception in Openstack");
				throw e;
			}
		}
	}

	/*
	 * Find a network (or query its existence) by its Name.  This method avoids an
	 * initial lookup by ID when it's known that we have the network Name.
	 *
	 * Neutron does not support 'name=*' query parameter for Network query (show).
	 * The only way to query by name is to retrieve all networks and look for the
	 * match.  While inefficient, this capability will be provided as it is needed
	 * by MSO, but should be avoided in favor of ID whenever possible.
	 *
	 * TODO:
	 * Network names are not required to be unique, though MSO will attempt to enforce
	 * uniqueness.  This call probably needs to return an error (instead of returning
	 * the first match).
	 *
	 * @param neutronClient an authenticated Quantum object
	 * @param networkName the network name to query
	 * @return a Network object or null if not found
	 */
	public Network findNetworkByName (Quantum neutronClient, String networkName)
	{
		if (networkName == null) {
            return null;
        }

		try {
			OpenStackRequest<Networks> request = neutronClient.networks().list();
			Networks networks = executeAndRecordOpenstackRequest(request);
			for (Network network : networks.getList()) {
				if (network.getName().equals(networkName)) {
					LOGGER.debug ("Found match on network name: " + networkName);
					return network;
				}
			}
			LOGGER.debug ("findNetworkByName - no match found for " + networkName);
			return null;
		}
		catch (OpenStackResponseException e) {
			if (e.getStatus() == 404) {
				return null;
			} else {
				LOGGER.error (MessageEnum.RA_CONNECTION_EXCEPTION, "OpenStack", "Openstack Error, GET Network By Name (" + networkName + "): " + e, "OpenStack", "", MsoLogger.ErrorCode.DataError, "Exception in OpenStack");
				throw e;
			}
		}
	}


	/*
	 * An entry in the Neutron Client Cache.  It saves the Neutron client object
	 * along with the token expiration.  After this interval, this cache
	 * item will no longer be used.
	 */
	private static class NeutronCacheEntry implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String neutronUrl;
		private String token;
		private Calendar expires;

		public NeutronCacheEntry (String neutronUrl, String token, Calendar expires) {
			this.neutronUrl = neutronUrl;
			this.token = token;
			this.expires = expires;
		}

		public Quantum getNeutronClient () {
			Quantum neutronClient = new Quantum(neutronUrl);
			neutronClient.token(token);
			return neutronClient;
		}

		public boolean isExpired() {
			return expires == null || System.currentTimeMillis() > expires.getTimeInMillis();
		}
	}

	/**
	 * Clean up the Neutron client cache to remove expired entries.
	 */
	public static void neutronCacheCleanup () {
		for (String cacheKey : neutronClientCache.keySet()) {
			if (neutronClientCache.get(cacheKey).isExpired()) {
				neutronClientCache.remove(cacheKey);
				LOGGER.debug ("Cleaned Up Cached Neutron Client for " + cacheKey);
			}
		}
	}

	/**
	 * Reset the Neutron client cache.
	 * This may be useful if cached credentials get out of sync.
	 */
	public static void neutronCacheReset () {
		neutronClientCache = new HashMap<>();
	}
}
