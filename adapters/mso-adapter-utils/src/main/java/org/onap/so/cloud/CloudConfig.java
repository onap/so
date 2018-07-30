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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;

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

@Configuration
@JsonRootName("cloud_config")
@ConfigurationProperties(prefix="cloud_config")
public class CloudConfig {
	
    private static final String CLOUD_SITE_VERSION = "2.5";
    private static final String DEFAULT_CLOUD_SITE_ID = "default";
    
    @JsonProperty("identity_services")
    private Map<String, CloudIdentity> identityServices = new HashMap<>();
    
    @JsonProperty("cloud_sites")
    private Map <String, CloudSite> cloudSites = new HashMap<>();
    
    @JsonProperty("cloudify_managers")
    private Map <String, CloudifyManager> cloudifyManagers = new HashMap<>();

    @PostConstruct
    private void init() {
    	for (Entry<String, CloudIdentity> entry : identityServices.entrySet()) {
    		entry.getValue().setId(entry.getKey());
    	}
    	
    	for (Entry<String, CloudSite> entry : cloudSites.entrySet()) {
    		entry.getValue().setId(entry.getKey());
    	}
    	
    	for (Entry<String, CloudifyManager> entry : cloudifyManagers.entrySet()) {
    		entry.getValue().setId(entry.getKey());
    	}
    }
    
    /**
     * Get a map of all identity services that have been loaded.
     */
    public Map<String, CloudIdentity> getIdentityServices() {
        return identityServices;
    }

    /**
     * Get a map of all cloud sites that have been loaded.
     */
    public Map<String, CloudSite> getCloudSites() {
        return cloudSites;
    }

	/**
	 * Get a Map of all CloudifyManagers that have been loaded.
	 * @return the Map
	 */
    public Map<String,CloudifyManager> getCloudifyManagers() {
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
        } else {
        	return getCloudSiteWithClli(id);
        }
    }
    
    public String getCloudSiteId(CloudSite cloudSite) {
       for(Entry<String, CloudSite> entry : this.getCloudSites().entrySet()){
    	   if(entry.getValue().equals(cloudSite))
    		   return entry.getKey();
       }
       return null;
    }

    /**
     * Get a specific CloudSites, based on a CLLI and (optional) version, which
     * will be matched against the aic_version field of the CloudSite.
     * 
     * @param clli
     *            the CLLI to match
     * @param version
     *            the version to match; may be null in which case any version
     *            matches
     * @return a CloudSite, or null of no match found
     */
    private Optional<CloudSite> getCloudSiteWithClli(String clli) {
        Optional <CloudSite> cloudSiteOptional = cloudSites.values().stream().filter(cs ->
                cs.getClli() != null && clli.equals(cs.getClli()) && (CLOUD_SITE_VERSION.equals(cs.getAicVersion())))
                .findAny();
        if (cloudSiteOptional.isPresent()) {
        	return cloudSiteOptional;
        } else {
        	return getDefaultCloudSite(clli);
        }
    }

    private Optional<CloudSite> getDefaultCloudSite(String clli) {
        Optional<CloudSite> cloudSiteOpt = cloudSites.values().stream()
                .filter(cs -> cs.getId().equalsIgnoreCase(DEFAULT_CLOUD_SITE_ID)).findAny();
        if (cloudSiteOpt.isPresent()) {
            CloudSite defaultCloudSite = cloudSiteOpt.get();
            CloudSite clone = new CloudSite(defaultCloudSite);
            clone.setRegionId(clli);
            clone.setId(clli);
            return Optional.of(clone);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Get a specific CloudIdentity, based on an ID.
     * 
     * @param id
     *            the ID to match
     * @return a CloudIdentity, or null of no match found
     */
    public CloudIdentity getIdentityService(String id) {
    		return identityServices.get(id);
    }

	/**
	 * Get a specific CloudifyManager, based on an ID.
	 * @param id the ID to match
	 * @return a CloudifyManager, or null of no match found
	 */
	public CloudifyManager getCloudifyManager (String id) {
			return cloudifyManagers.get(id);
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("identityServices", getIdentityServices()).append("cloudSites", getCloudSites()).toString();
	}
	
	@Override
	public boolean equals(final Object other) {
		if (other == null) {
			return false;
		}
		if (!getClass().equals(other.getClass())) {
			return false;
		}
		CloudConfig castOther = (CloudConfig) other;
		return new EqualsBuilder().append(getIdentityServices(), castOther.getIdentityServices())
				.append(getCloudSites(), castOther.getCloudSites()).isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(1, 31).append(getIdentityServices()).append(getCloudSites()).toHashCode();
	}
}
