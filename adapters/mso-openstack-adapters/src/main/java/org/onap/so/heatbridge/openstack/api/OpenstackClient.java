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
import java.util.Map;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.heat.Resource;
import org.openstack4j.model.network.Network;
import org.openstack4j.model.network.Port;
import org.openstack4j.model.network.Subnet;

public interface OpenstackClient {

    /**
     * Get a server object by server ID
     * 
     * @param serverId Unique server-name (simple name) or server-id (UUID)
     * @return Server object
     */
    Server getServerById(String serverId);

    /**
     * Get a port object by port ID
     * 
     * @param portId Unique UUID of the port.
     * @return Port object.
     */
    Port getPortById(String portId);

    /**
     * Returns a list of all ports we have the right to see
     * 
     * @return List of all Openstack ports
     */
    List<Port> getAllPorts();

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
     * List networks by filtering parameters
     * 
     * @param filterParams key-value pairs for filtering params
     * @return List of filtered Network objects
     */
    List<Network> listNetworksByFilter(Map<String, String> filterParams);

    /**
     * Get a subnet object by subnet ID
     * 
     * @param subnetId Unique UUID of the subnet.
     * @return Subnet object.
     */
    Subnet getSubnetById(String subnetId);
}
