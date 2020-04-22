/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

/*
 * Copyright (C) 2018 Bell Canada. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.onap.so.heatbridge;

import java.util.List;
import org.onap.so.heatbridge.openstack.api.OpenstackClient;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Image;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.heat.Resource;

/**
 * Defines the contract to extract Heat Stack Resources from Openstack and inventory it to AAI. This API is used only to
 * "create" objects in AAI.
 */
public interface HeatBridgeApi {

    /**
     * Authenticate with Openstack Keystone. The auth information is read from SO cloud configuration file.
     *
     * @return Openstack client object with keystone token
     * @throws HeatBridgeException upon failure to authenticate with keystone
     */
    OpenstackClient authenticate() throws HeatBridgeException;

    /**
     * Query all the stack based resources from Openstack Heat service
     *
     * @param heatStackId Heat stack UUID
     * @return A list of stack based resources
     */
    List<Resource> queryNestedHeatStackResources(String heatStackId);

    /**
     * Get a filtered list of resource IDs by resource type
     *
     * @param stackResources A list of stack based resources
     * @param resourceType Resource type to filter by
     * @return A list of stack resources matching the specified resource-type
     */
    List<String> extractStackResourceIdsByResourceType(List<Resource> stackResources, String resourceType);

    /**
     * Get network IDs for a given list of network names. It is assumed that there is a one to one mapping between the
     * name and ID.
     * 
     * @param networkNameList List of network names
     * @return List of matching network IDs
     */
    List<String> extractNetworkIds(List<String> networkNameList);

    /**
     * Query the Openstack server objects from the list of stack resources
     *
     * @param stackResources A list of stack based resources
     * @return A list of Openstack Server objects
     */
    List<Server> getAllOpenstackServers(List<Resource> stackResources);

    /**
     * Extract Openstack Image objects from a a list of Server objects
     *
     * @param servers A list of Openstack Server objects
     * @return A list of Openstack Image objects
     */
    List<Image> extractOpenstackImagesFromServers(List<Server> servers);

    /**
     * Extract Openstack Flavor objects from a a list of Server objects
     *
     * @param servers A list of Openstack Server objects
     * @return A list of Openstack Flavor objects
     */
    List<Flavor> extractOpenstackFlavorsFromServers(List<Server> servers);

    /**
     * Query and build AAI actions for Openstack Image resources to AAI's image objects
     *
     * @param images List of Openstack Image objects
     * @throws HeatBridgeException when failing to add images to AAI
     */
    void buildAddImagesToAaiAction(List<Image> images) throws HeatBridgeException;

    /**
     * Query and build AAI actions for Openstack Flavor resources to AAI's flavor objects
     *
     * @param flavors List of Openstack Flavor objects
     * @throws HeatBridgeException when failing to add flavors to AAI
     */
    void buildAddFlavorsToAaiAction(List<Flavor> flavors) throws HeatBridgeException;

    /**
     * Query and build AAI actions for Openstack Compute resources to AAI's vserver objects
     *
     * @param genericVnfId AAI generic-vnf-id
     * @param vfModuleId AAI vf-module-id
     * @param servers Openstack Server list
     */
    void buildAddVserversToAaiAction(String genericVnfId, String vfModuleId, List<Server> servers);

    /**
     * Query and build AAI actions for Openstack Neutron resources associated with a Compute resource to AAI's
     * l-interface objects
     *
     * @param stackResources Openstack Heat stack resource list
     * @param oobMgtNetIds List of OOB network IDs list
     */
    void buildAddVserverLInterfacesToAaiAction(List<Resource> stackResources, List<String> oobMgtNetIds);

    /**
     * Query and build AAI actions for Openstack Compute resources to AAI's pserver and pinterface objects
     *
     * @param stackResources Openstack StackResources list
     */
    void createPserversAndPinterfacesIfNotPresentInAai(final List<Resource> stackResources) throws HeatBridgeException;

    /**
     * Execute AAI restful API to update the Openstack resources
     * 
     * @param dryrun - this will simply log the aai transaction to log if enabled and not write any data
     * @throws HeatBridgeException when failing to add openstack resource PoJos to AAI
     */
    void submitToAai(boolean dryrun) throws HeatBridgeException;

    /**
     * Delete heatbridge data for a given vf-module
     *
     * @throws HeatBridgeException when failing to remove heatbridge data from AAI for a given vf-module
     */
    void deleteVfModuleData(String vnfId, String vfModuleId) throws HeatBridgeException;
}
