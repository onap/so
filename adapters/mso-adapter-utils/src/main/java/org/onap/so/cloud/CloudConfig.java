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

import java.util.Optional;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.beans.CloudifyManager;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * JavaBean JSON class for a CloudConfig. This bean maps a JSON-format cloud configuration file to Java. The CloudConfig
 * contains information about Openstack cloud configurations. It includes: - CloudIdentity objects,representing DCP
 * nodes (Openstack Identity Service) - CloudSite objects, representing LCP nodes (Openstack Compute & other services)
 *
 * Note that this is only used to access Cloud Configurations loaded from a JSON config file, so there are no explicit
 * property setters.
 *
 * This class also contains methods to query cloud sites and/or identity services by ID.
 *
 */

@JsonRootName("cloud_config")
@Component
public class CloudConfig {

    private static final String CLOUD_SITE_VERSION = "2.5";
    private static final String DEFAULT_CLOUD_SITE_ID = "DEFAULT";

    @Autowired
    private CatalogDbClient catalogDbClient;

    /**
     * Get a specific CloudSites, based on an ID. The ID is first checked against the regions, and if no match is found
     * there, then against individual entries to try and find one with a CLLI that matches the ID and an AIC version of
     * 2.5.
     * 
     * @param id the ID to match
     * @return an Optional of CloudSite object.
     */
    public synchronized Optional<CloudSite> getCloudSite(String id) {
        if (id == null) {
            return Optional.empty();
        }
        CloudSite cloudSite = catalogDbClient.getCloudSite(id);

        if (cloudSite != null) {
            return Optional.of(cloudSite);
        } else {
            return getCloudSiteWithClli(id);
        }
    }

    /**
     * Get a specific CloudSites, based on a CLLI and (optional) version, which will be matched against the aic_version
     * field of the CloudSite.
     * 
     * @param clli the CLLI to match
     * @return a CloudSite, or null of no match found
     */
    private Optional<CloudSite> getCloudSiteWithClli(String clli) {
        Optional<CloudSite> cloudSiteOptional =
                Optional.ofNullable(catalogDbClient.getCloudSiteByClliAndAicVersion(clli, CLOUD_SITE_VERSION));
        if (cloudSiteOptional.isPresent()) {
            return cloudSiteOptional;
        } else {
            return getDefaultCloudSite(clli);
        }
    }

    private Optional<CloudSite> getDefaultCloudSite(String clli) {
        Optional<CloudSite> cloudSiteOpt = Optional.ofNullable(catalogDbClient.getCloudSite(DEFAULT_CLOUD_SITE_ID));
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
     * Get a specific CloudifyManager, based on an ID.
     * 
     * @param id the ID to match
     * @return a CloudifyManager, or null of no match found
     */
    public CloudifyManager getCloudifyManager(String id) {
        return catalogDbClient.getCloudifyManager(id);
    }
}
