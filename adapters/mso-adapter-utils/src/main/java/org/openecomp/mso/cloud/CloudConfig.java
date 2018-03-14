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

package org.openecomp.mso.cloud;

import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.openstack.exceptions.MsoCloudIdentityNotFound;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JavaBean JSON class for a CloudConfig. This bean maps a JSON-format cloud
 * configuration file to Java. The CloudConfig contains information about
 * Openstack cloud configurations. It includes: 
 * - CloudIdentity objects,representing DCP nodes (Openstack Identity Service) 
 * - CloudSite objects, representing LCP nodes (Openstack Compute & other services)
 *
 * Note that this is only used to access Cloud Configurations loaded from a JSON
 * config file, so there are no explicit property setters.
 *
 * This class also contains methods to query cloud sites and/or identity
 * services by ID.
 *
 */

@JsonRootName("cloud_config")
public class CloudConfig {

    private static final String CLOUD_SITE_VERSION = "2.5";
    private static final String DEFAULT_CLOUD_SITE_ID = "default";
    private boolean validCloudConfig = false;
    private static ObjectMapper mapper = new ObjectMapper();
    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA);
    protected String configFilePath;
    protected int refreshTimerInMinutes;
    @JsonProperty("identity_services")
    private Map<String, CloudIdentity> identityServices = new HashMap<>();
    @JsonProperty("cloud_sites")
    private Map <String, CloudSite> cloudSites = new HashMap <String, CloudSite> ();
    @JsonProperty("cloudify_managers")
    private Map <String, CloudifyManager> cloudifyManagers = new HashMap <String, CloudifyManager> ();

    public CloudConfig() {
        mapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    }

    /**
     * Get a map of all identity services that have been loaded.
     */
    public synchronized Map<String, CloudIdentity> getIdentityServices() {
        return identityServices;
    }

    /**
     * Get a map of all cloud sites that have been loaded.
     */
    public Map<String, CloudSite> getCloudSites() {
        return Collections.unmodifiableMap(cloudSites);
    }

	/**
	 * Get a Map of all CloudifyManagers that have been loaded.
	 * @return the Map
	 */
    public synchronized Map <String, CloudifyManager> getCloudifyManagers () {
        return cloudifyManagers;
    }

    /**
     * Get a specific CloudSites, based on an ID. The ID is first checked
     * against the regions, and if no match is found there, then against
     * individual entries to try and find one with a CLLI that matches the ID
     * and an AIC version of 2.5.
     *
     * @param id the ID to match
     * @return an Optional of CloudSite object.
     */
    public synchronized Optional<CloudSite> getCloudSite(String id) {
        if (id == null) {
            return Optional.empty();
        }
        if (cloudSites.containsKey(id)) {
            return Optional.ofNullable(cloudSites.get(id));
        }
        return null;
    }

     private CloudSite getCloudSiteWithClli(String clli) {
        Optional <CloudSite> cloudSiteOptional = cloudSites.values().stream().filter(cs ->
                cs.getClli() != null && clli.equals(cs.getClli()) && (CLOUD_SITE_VERSION.equals(cs.getAic_version())))
                .findAny();
        return cloudSiteOptional.orElse(getDefaultCloudSite(clli));
    }

    private CloudSite getDefaultCloudSite(String clli) {
        Optional<CloudSite> cloudSiteOpt = cloudSites.values().stream()
                .filter(cs -> cs.getId().equalsIgnoreCase(DEFAULT_CLOUD_SITE_ID)).findAny();
        if (cloudSiteOpt.isPresent()) {
            CloudSite defaultCloudSite = cloudSiteOpt.get();
            defaultCloudSite.setRegionId(clli);
            defaultCloudSite.setId(clli);
            return defaultCloudSite;
        } else {
            return null;
        }
    }

    /**
     * Get a specific CloudIdentity, based on an ID.
     * 
     * @param id
     *            the ID to match
     * @return a CloudIdentity, or null of no match found
     */
    public synchronized CloudIdentity getIdentityService(String id) {
        if (identityServices.containsKey(id)) {
            return identityServices.get(id);
        }
        return null;
    }

	/**
	 * Get a specific CloudifyManager, based on an ID.
	 * @param id the ID to match
	 * @return a CloudifyManager, or null of no match found
	 */
	public synchronized CloudifyManager getCloudifyManager (String id) {
		if (cloudifyManagers.containsKey (id)) {
			return cloudifyManagers.get (id);
		}
		return null;
	}

    protected synchronized void reloadPropertiesFile() throws IOException, MsoCloudIdentityNotFound {
        this.loadCloudConfig(this.configFilePath, this.refreshTimerInMinutes);
    }

    protected synchronized void loadCloudConfig(String configFile, int refreshTimer)
            throws IOException, MsoCloudIdentityNotFound {

        FileReader reader = null;
        configFilePath = configFile;
        this.refreshTimerInMinutes = refreshTimer;
        this.validCloudConfig=false;

        try {
            reader = new FileReader(configFile);
            // Parse the JSON input into a CloudConfig

            CloudConfig cloudConfig = mapper.readValue(reader, CloudConfig.class);

            this.cloudSites = cloudConfig.cloudSites;
            this.identityServices = cloudConfig.identityServices;
	        this.cloudifyManagers = cloudConfig.cloudifyManagers;

            // Copy Cloud Identity IDs to CloudIdentity objects
            for (Entry<String, CloudIdentity> entry : cloudConfig.getIdentityServices().entrySet()) {
                entry.getValue().setId(entry.getKey());
            }

	        // Copy Cloduify IDs to CloudifyManager objects
	        for (Entry <String, CloudifyManager> entry : cloudConfig.getCloudifyManagers ().entrySet ()) {
	            entry.getValue ().setId (entry.getKey ());
	        }

            // Copy Cloud Site IDs to CloudSite objects, and set up internal
            // pointers to their corresponding identity service.
            for (Entry<String, CloudSite> entry : cloudConfig.getCloudSites().entrySet()) {
                CloudSite s = entry.getValue();
                s.setId(entry.getKey());
                CloudIdentity cloudIdentity = cloudConfig.getIdentityService(s.getIdentityServiceId());
                s.setIdentityService(cloudIdentity);
                if (cloudIdentity == null) {
                    throw new MsoCloudIdentityNotFound(s.getId()+" Cloud site refers to a non-existing identity service: "+s.getIdentityServiceId());
                }
                CloudifyManager cloudifyManager = cloudConfig.getCloudifyManager(s.getCloudifyId());
                s.setCloudifyManager(cloudifyManager);
            }
            this.validCloudConfig=true;
            
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                LOGGER.debug("Exception while closing reader for file:" + configFilePath, e);
            }
        }
    }

    public String getConfigFilePath() {
        return configFilePath;
    }

    /**
     * @return the validCouldConfig
     */
    public synchronized boolean isValidCloudConfig() {
        return validCloudConfig;
    }

    @Override
    public synchronized CloudConfig clone() {
        CloudConfig ccCopy = new CloudConfig();
        for (Entry<String, CloudIdentity> e : identityServices.entrySet()) {

            ccCopy.identityServices.put(e.getKey(), e.getValue().clone());
        }

        for (Entry<String, CloudSite> e : cloudSites.entrySet()) {

            ccCopy.cloudSites.put(e.getKey(), e.getValue().clone());
        }

		for (Entry<String,CloudifyManager> e:cloudifyManagers.entrySet()) {

			ccCopy.cloudifyManagers.put(e.getKey(), e.getValue().clone());
		}

        ccCopy.configFilePath = this.configFilePath;
        ccCopy.refreshTimerInMinutes = this.refreshTimerInMinutes;
        ccCopy.validCloudConfig = this.validCloudConfig;
        return ccCopy;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cloudSites == null) ? 0 : cloudSites.hashCode());
        result = prime * result + ((configFilePath == null) ? 0 : configFilePath.hashCode());
        result = prime * result + ((identityServices == null) ? 0 : identityServices.hashCode());
        result = prime * result + refreshTimerInMinutes;
        result = prime * result + (validCloudConfig ? 1231 : 1237);
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CloudConfig other = (CloudConfig) obj;
        if (cloudSites == null) {
            if (other.cloudSites != null)
                return false;
        } else if (!cloudSites.equals(other.cloudSites))
            return false;
        if (configFilePath == null) {
            if (other.configFilePath != null)
                return false;
        } else if (!configFilePath.equals(other.configFilePath))
            return false;
        if (identityServices == null) {
            if (other.identityServices != null)
                return false;
        } else if (!identityServices.equals(other.identityServices))
            return false;
        if (refreshTimerInMinutes != other.refreshTimerInMinutes)
            return false;
        if (validCloudConfig != other.validCloudConfig)
            return false;
        return true;
    }

  
}
