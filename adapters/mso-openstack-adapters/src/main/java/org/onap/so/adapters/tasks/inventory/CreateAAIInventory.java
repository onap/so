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
import org.apache.commons.collections.CollectionUtils;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.so.cloud.CloudConfig;
import org.onap.so.cloud.resource.beans.CloudInformation;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.heatbridge.HeatBridgeApi;
import org.onap.so.heatbridge.HeatBridgeException;
import org.onap.so.heatbridge.HeatBridgeImpl;
import org.onap.so.openstack.exceptions.MsoCloudSiteNotFound;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Image;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.heat.Resource;
import org.openstack4j.model.network.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class CreateAAIInventory {

    private static final Logger logger = LoggerFactory.getLogger(CreateAAIInventory.class);

    private AAIResourcesClient aaiClient;

    @Autowired
    protected CloudConfig cloudConfig;

    private static final String MULTICLOUD_MODE = "MULTICLOUD";

    @Autowired
    protected Environment env;

    public void heatbridge(CloudInformation cloudInformation) throws HeatBridgeException, MsoCloudSiteNotFound {
        CloudSite cloudSite = cloudConfig.getCloudSite(cloudInformation.getRegionId())
                .orElseThrow(() -> new MsoCloudSiteNotFound(cloudInformation.getRegionId()));
        if (cloudSite.getOrchestrator() != null && MULTICLOUD_MODE.equalsIgnoreCase(cloudSite.getOrchestrator())) {
            logger.debug("Skipping Heatbridge as CloudSite orchestrator is: {}", MULTICLOUD_MODE);
            return;
        }
        CloudIdentity cloudIdentity = cloudSite.getIdentityService();
        String heatStackId = cloudInformation.getTemplateInstanceId().split("/")[1];

        List<String> oobMgtNetNames = new ArrayList<>();

        HeatBridgeApi heatBridgeClient = createClient(getAaiClient(), cloudSite, cloudIdentity, cloudInformation);

        heatBridgeClient.authenticate();

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
        List<String> oobMgtNetIds = new ArrayList<>();

        // if no network-id list is provided, however network-name list is
        if (!CollectionUtils.isEmpty(oobMgtNetNames)) {
            oobMgtNetIds = heatBridgeClient.extractNetworkIds(oobMgtNetNames);
        }
        heatBridgeClient.buildAddVserverLInterfacesToAaiAction(stackResources, oobMgtNetIds,
                cloudInformation.getOwner());
        logger.debug("Successfully queried neutron resources and built AAI actions to add l-interfaces to vservers.");

        heatBridgeClient.buildAddVolumes(stackResources);

        // Update AAI
        logger.debug("Current Dry Run Value: {}", env.getProperty("heatBridgeDryrun", Boolean.class, false));
        heatBridgeClient.submitToAai(env.getProperty("heatBridgeDryrun", Boolean.class, false));
    }

    public HeatBridgeApi createClient(AAIResourcesClient client, CloudSite cloudSite, CloudIdentity cloudIdentity,
            CloudInformation cloudInformation) {
        return new HeatBridgeImpl(client, cloudIdentity, cloudInformation.getOwner(), cloudInformation.getRegionId(),
                cloudSite.getRegionId(), cloudInformation.getTenantId(), cloudInformation.getNodeType());
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
