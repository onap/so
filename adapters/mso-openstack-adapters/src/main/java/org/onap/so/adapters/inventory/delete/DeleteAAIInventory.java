/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.inventory.delete;

import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.so.cloud.CloudConfig;
import org.onap.so.cloud.resource.beans.CloudInformation;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.heatbridge.HeatBridgeApi;
import org.onap.so.heatbridge.HeatBridgeImpl;
import org.onap.so.openstack.exceptions.MsoCloudSiteNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class DeleteAAIInventory {

    private static final Logger logger = LoggerFactory.getLogger(DeleteAAIInventory.class);

    private AAIResourcesClient aaiClient;

    @Autowired
    protected CloudConfig cloudConfig;

    @Autowired
    protected Environment env;

    public void heatbridge(CloudInformation cloudInformation) {
        try {
            logger.debug("Heatbridge delete executing");

            CloudSite cloudSite = cloudConfig.getCloudSite(cloudInformation.getRegionId())
                    .orElseThrow(() -> new MsoCloudSiteNotFound(cloudInformation.getRegionId()));
            CloudIdentity cloudIdentity = cloudSite.getIdentityService();
            HeatBridgeApi heatBridgeClient = new HeatBridgeImpl(new AAIResourcesClient(), cloudIdentity,
                    cloudInformation.getOwner(), cloudInformation.getRegionId(), cloudSite.getRegionId(),
                    cloudInformation.getTenantId(), cloudInformation.getNodeType());
            heatBridgeClient.authenticate();
            heatBridgeClient.deleteVfModuleData(cloudInformation.getVnfId(), cloudInformation.getVfModuleId());

        } catch (Exception ex) {
            logger.debug("Heatbrige failed for stackId: " + cloudInformation.getTemplateInstanceId(), ex);
        }
    }

    protected AAIResourcesClient getAaiClient() {
        if (aaiClient == null)
            return new AAIResourcesClient();
        else
            return aaiClient;
    }

    protected void setAaiClient(AAIResourcesClient aaiResource) {
        aaiClient = aaiResource;
    }
}
