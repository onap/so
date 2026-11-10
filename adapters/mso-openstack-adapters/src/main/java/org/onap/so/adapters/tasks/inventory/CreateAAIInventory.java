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

package org.onap.so.adapters.tasks.inventory;

import java.util.ArrayList;
import java.util.List;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.so.cloud.CloudConfig;
import org.onap.so.cloud.resource.beans.CloudInformation;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.heatbridge.HeatBridgeApi;
import org.onap.so.heatbridge.HeatBridgeException;
import org.onap.so.heatbridge.HeatBridgeImpl;
import org.onap.so.heatbridge.openstack.factory.OpenstackClientFactory;
import org.onap.so.openstack.exceptions.MsoCloudSiteNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import com.woorea.openstack.heat.model.Resource;
import com.woorea.openstack.nova.model.Flavor;
import com.woorea.openstack.nova.model.Image;
import com.woorea.openstack.nova.model.Server;
import com.woorea.openstack.quantum.model.Network;

@Component
public class CreateAAIInventory {

    private static final Logger logger = LoggerFactory.getLogger(CreateAAIInventory.class);

    private AAIResourcesClient aaiClient;

    @Autowired
    protected CloudConfig cloudConfig;

    private static final String MULTICLOUD_MODE = "MULTICLOUD";

    @Autowired
    protected Environment env;

    @Autowired
    protected OpenstackClientFactory openstackClientFactory;

    public void heatbridge(CloudInformation cloudInformation) throws HeatBridgeException, MsoCloudSiteNotFound {
        CloudSite cloudSite = cloudConfig.getCloudSite(cloudInformation.getRegionId())
                .orElseThrow(() -> new MsoCloudSiteNotFound(cloudInformation.getRegionId()));
        if (cloudSite.getOrchestrator() != null && MULTICLOUD_MODE.equalsIgnoreCase(cloudSite.getOrchestrator())) {
            logger.debug("Skipping Heatbridge as CloudSite orchestrator is: {}", MULTICLOUD_MODE);
            return;
        }
        String heatStackId = cloudInformation.getTemplateInstanceId().split("/")[1];

        HeatBridgeApi heatBridgeClient = createClient(getAaiClient(), cloudInformation);

        List<Resource> stackResources =
                heatBridgeClient.queryNestedHeatStackResources(cloudInformation.getTemplateInstanceId());

        List<Network> osNetworks = heatBridgeClient.getAllOpenstackProviderNetworks(stackResources);
        heatBridgeClient.buildAddNetworksToAaiAction(cloudInformation.getVnfId(), cloudInformation.getVfModuleId(),
                osNetworks);

        List<Server> osServers = heatBridgeClient.getAllOpenstackServers(stackResources);

        heatBridgeClient.createPserversAndPinterfacesIfNotPresentInAai(stackResources);

        List<Image> osImages = heatBridgeClient.extractOpenstackImagesFromServers(osServers);

        List<Flavor> osFlavors = heatBridgeClient.extractOpenstackFlavorsFromServers(osServers);

        logger.debug("Successfully queried heat stack{} for resources.", heatStackId);
        // os images
        if (osImages != null && !osImages.isEmpty()) {
            heatBridgeClient.buildAddImagesToAaiAction(osImages);
            logger.debug("Successfully built AAI actions to add images.");
        } else {
            logger.debug("No images to update to AAI.");
        }
        // flavors
        if (osFlavors != null && !osFlavors.isEmpty()) {
            heatBridgeClient.buildAddFlavorsToAaiAction(osFlavors);
            logger.debug("Successfully built AAI actions to add flavors.");
        } else {
            logger.debug("No flavors to update to AAI.");
        }

        // compute resources
        heatBridgeClient.buildAddVserversToAaiAction(cloudInformation.getVnfId(), cloudInformation.getVfModuleId(),
                osServers);
        logger.debug("Successfully queried compute resources and built AAI vserver actions.");

        // neutron resources
        heatBridgeClient.buildAddVserverLInterfacesToAaiAction(stackResources, new ArrayList<>(),
                cloudInformation.getOwner());
        logger.debug("Successfully queried neutron resources and built AAI actions to add l-interfaces to vservers.");

        heatBridgeClient.buildAddVolumes(stackResources);

        // Update AAI
        logger.debug("Current Dry Run Value: {}", env.getProperty("heatBridgeDryrun", Boolean.class, false));
        heatBridgeClient.submitToAai(env.getProperty("heatBridgeDryrun", Boolean.class, false));
    }

    public HeatBridgeApi createClient(AAIResourcesClient client, CloudInformation cloudInformation) {
        return new HeatBridgeImpl(client,
                openstackClientFactory.create(cloudInformation.getRegionId(), cloudInformation.getTenantId()),
                cloudInformation.getOwner(), cloudInformation.getRegionId(), cloudInformation.getTenantId(),
                cloudInformation.getNodeType());
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
