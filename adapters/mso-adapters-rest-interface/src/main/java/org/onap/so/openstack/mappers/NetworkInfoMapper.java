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

package org.onap.so.openstack.mappers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.javatuples.Pair;
import org.onap.so.openstack.beans.NetworkInfo;
import org.onap.so.openstack.beans.NetworkStatus;
import com.woorea.openstack.quantum.model.Network;
import com.woorea.openstack.quantum.model.Segment;

public class NetworkInfoMapper {

    private final Network network;
    private final Map<String, NetworkStatus> networkStatusMap = new HashMap<>();

    public NetworkInfoMapper(Network network) {
        this.network = network;
        configureNetworkStatusMap();
    }

    /**
     * Capture the data from a Neutron Network object.
     *
     * For MSO, there are assumptions regarding all networks. - Everything will be a provider network - All provider
     * networks are VLANs - Multiple VLANs are supported, and indicated by multi-provider segments. Each will have the
     * same physical network & network type "vlan".
     *
     * @param network
     */
    public NetworkInfo map() {
        final NetworkInfo info = new NetworkInfo();
        if (network == null) {
            info.setStatus(NetworkStatus.NOTFOUND);
        } else {
            info.setName(network.getName());
            info.setId(network.getId());
            info.setStatus(this.mapStatus(network.getStatus()));
            Pair<Optional<String>, List<Integer>> result = locateVlanInformation(network);
            Optional<String> value0 = result.getValue0();
            if (value0.isPresent()) {
                info.setProvider(value0.get());
            }
            info.setVlans(result.getValue1());
            info.setSubnets(network.getSubnets());
        }
        return info;
    }

    protected NetworkStatus mapStatus(String status) {
        return networkStatusMap.getOrDefault(status, NetworkStatus.UNKNOWN);
    }

    protected Pair<Optional<String>, List<Integer>> locateVlanInformation(Network network) {
        final List<Integer> vlans = new ArrayList<>();
        Optional<String> provider = Optional.empty();
        if (network.getProviderPhysicalNetwork() != null) {
            provider = Optional.ofNullable(network.getProviderPhysicalNetwork());
            if ("vlan".equals(network.getProviderNetworkType())) {
                vlans.add(network.getProviderSegmentationId());
            }
        } else if (network.getSegments() != null && !network.getSegments().isEmpty()) {
            Segment s = network.getSegments().get(0);
            provider = Optional.ofNullable(s.getProviderPhysicalNetwork());
            if ("vlan".equals(s.getProviderNetworkType())) {
                for (Segment s1 : network.getSegments()) {
                    vlans.add(s1.getProviderSegmentationId());
                }
            }
        }

        return Pair.with(provider, vlans);
    }

    private void configureNetworkStatusMap() {
        networkStatusMap.put("ACTIVE", NetworkStatus.ACTIVE);
        networkStatusMap.put("DOWN", NetworkStatus.DOWN);
        networkStatusMap.put("BUILD", NetworkStatus.BUILD);
        networkStatusMap.put("ERROR", NetworkStatus.ERROR);
    }
}
