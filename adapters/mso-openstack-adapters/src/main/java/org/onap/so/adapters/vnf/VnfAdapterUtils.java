/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.adapters.vnf;

import java.util.Optional;
import org.onap.so.cloud.CloudConfig;
import org.onap.so.db.catalog.beans.CloudSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VnfAdapterUtils {
    private static Logger logger = LoggerFactory.getLogger(VnfAdapterUtils.class);

    private static final String HEAT_MODE = "HEAT";
    private static final String MULTICLOUD_MODE = "MULTICLOUD";

    @Autowired
    private CloudConfig cloudConfig;

    /*
     * Choose which implementation of VNF Adapter to use, based on the orchestration mode. Currently, the two supported
     * orchestrators are HEAT and CLOUDIFY.
     */
    public boolean isMulticloudMode(String mode, String cloudSiteId) {
        // First, determine the orchestration mode to use.
        // If was explicitly provided as a parameter, use that. Else if specified for
        // the
        // cloudsite, use that. Otherwise, the default is the (original) HEAT-based
        // impl.

        logger.debug("Entered GetVnfAdapterImpl: mode={}, cloudSite={}", mode, cloudSiteId);

        if (mode == null) {
            // Didn't get an explicit mode type requested.
            // Use the CloudSite to determine which Impl to use, based on whether the target
            // cloutSite
            // has a CloudifyManager assigned to it
            Optional<CloudSite> cloudSite = cloudConfig.getCloudSite(cloudSiteId);
            if (cloudSite.isPresent()) {
                logger.debug("Got CloudSite: {}", cloudSite.toString());
                if (MULTICLOUD_MODE.equalsIgnoreCase(cloudSite.get().getOrchestrator())) {
                    logger.debug("GetVnfAdapterImpl: mode={}", MULTICLOUD_MODE);
                    return true;
                }
            }
        }

        return false;
    }

}
