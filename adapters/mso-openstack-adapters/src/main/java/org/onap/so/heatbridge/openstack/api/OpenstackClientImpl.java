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
import java.util.Objects;
import java.util.stream.Collectors;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.heat.Resource;
import org.openstack4j.model.network.Network;
import org.openstack4j.model.network.Port;
import org.openstack4j.model.network.Subnet;
import org.openstack4j.model.storage.block.Volume;

abstract class OpenstackClientImpl implements OpenstackClient {
    @Override
    public Server getServerById(String serverId) {
        return getClient().compute().servers().get(serverId);
    }

    @Override
    public Port getPortById(String portId) {
        return getClient().networking().port().get(portId);
    }

    @Override
    public List<Port> getAllPorts() {
        return (List<Port>) getClient().networking().port().list();
    }

    @Override
    public List<Resource> getStackBasedResources(String stackId, int nestingDepth) {
        return getClient().heat().resources().list(stackId, nestingDepth).stream().filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Network getNetworkById(String networkId) {
        return getClient().networking().network().get(networkId);
    }

    @Override
    public List<Network> listNetworksByFilter(Map<String, String> filterParams) {
        return (List<Network>) getClient().networking().network().list(filterParams);
    }

    @Override
    public Subnet getSubnetById(String subnetId) {
        return getClient().networking().subnet().get(subnetId);
    }

    @Override
    public Volume getVolumeById(String id) {
        return getClient().blockStorage().volumes().get(id);
    }

    /**
     * Retrieves the specific client to utilize.
     * 
     * @return The specific client to utilize
     */
    protected abstract OSClient getClient();

}
