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

/*-
 * Copyright (C) 2018 Bell Canada. All rights reserved.
 *
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
 */

package org.onap.so.heatbridge.openstack.api;

import java.util.List;
import com.woorea.openstack.cinder.model.Volume;
import com.woorea.openstack.heat.model.Resource;
import com.woorea.openstack.nova.model.Flavor;
import com.woorea.openstack.nova.model.Image;
import com.woorea.openstack.nova.model.Server;
import com.woorea.openstack.quantum.model.Network;
import com.woorea.openstack.quantum.model.Port;
import com.woorea.openstack.quantum.model.Subnet;

public interface OpenstackClient {

    /**
     * Get a server object by server ID
     * 
     * @param serverId Unique server-name (simple name) or server-id (UUID)
     * @return Server object
     */
    Server getServerById(String serverId);

    /**
     * Get an image object by image ID
     *
     * @param imageId Unique UUID of the image.
     * @return Image object, or null when the image no longer exists.
     */
    Image getImageById(String imageId);

    /**
     * Get a flavor object by flavor ID
     *
     * @param flavorId Unique ID of the flavor.
     * @return Flavor object, or null when the flavor no longer exists.
     */
    Flavor getFlavorById(String flavorId);

    /**
     * Get a port object by port ID
     * 
     * @param portId Unique UUID of the port.
     * @return Port object.
     */
    Port getPortById(String portId);

    /**
     * Returns a list of all the resources for the stack
     * 
     * @param stackId Stack name or unique UUID
     * @param nestingDepth The recursion level for which resources will be listed.
     * @return List of Openstack Stack resources
     */
    List<Resource> getStackBasedResources(String stackId, int nestingDepth);

    /**
     * Get a network instance by network ID
     * 
     * @param networkId Unique UUID of the network.
     * @return Network object.
     */
    Network getNetworkById(String networkId);

    /**
     * Get a subnet object by subnet ID
     * 
     * @param subnetId Unique UUID of the subnet.
     * @return Subnet object.
     */
    Subnet getSubnetById(String subnetId);

    /**
     * Get a volume object by volume ID
     * 
     * @return Volume object.
     */
    Volume getVolumeById(String volumeId);
}
