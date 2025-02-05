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

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.onap.so.openstack.beans.NetworkInfo;
import org.onap.so.openstack.beans.NetworkStatus;
import com.woorea.openstack.quantum.model.Network;
import com.woorea.openstack.quantum.model.Segment;

public class NetworkInfoMapperTest {

    @Test
    public void checkNetworkStatusMap() {
        NetworkInfoMapper mapper = new NetworkInfoMapper(new Network());
        assertEquals(NetworkStatus.ACTIVE, mapper.mapStatus("ACTIVE"));
        assertEquals(NetworkStatus.BUILD, mapper.mapStatus("BUILD"));
        assertEquals(NetworkStatus.ERROR, mapper.mapStatus("ERROR"));
        assertEquals(NetworkStatus.DOWN, mapper.mapStatus("DOWN"));
        assertEquals(NetworkStatus.UNKNOWN, mapper.mapStatus("NOT IN MAP"));
        assertEquals(NetworkStatus.UNKNOWN, mapper.mapStatus(null));
    }

    @Test
    public void checkLocateVlanInformationNoSegments() {
        Network network = new Network();
        network.setProviderPhysicalNetwork("test-physical-network");
        network.setProviderNetworkType("vlan");
        network.setProviderSegmentationId(2);
        NetworkInfoMapper mapper = new NetworkInfoMapper(network);
        NetworkInfo result = mapper.map();
        assertEquals("test-physical-network", result.getProvider());
        assertEquals(1, result.getVlans().size());
        assertEquals(2, result.getVlans().get(0).intValue());
    }

    @Test
    public void checkLocateVlanInformationSegments() {
        Network network = new Network();
        addSegments(network);

        NetworkInfoMapper mapper = new NetworkInfoMapper(network);
        NetworkInfo result = mapper.map();
        assertEquals("type1", result.getProvider());
        assertEquals(2, result.getVlans().size());
        assertEquals(Arrays.asList(1, 2).toString(), result.getVlans().toString());
    }

    @Test
    public void checkLocateVlanInformationSegmentsAndPhysical() {
        Network network = new Network();
        addSegments(network);
        network.setProviderPhysicalNetwork("test-physical-network");
        network.setProviderNetworkType("vlan");
        network.setProviderSegmentationId(2);
        NetworkInfoMapper mapper = new NetworkInfoMapper(network);
        NetworkInfo result = mapper.map();
        assertEquals("test-physical-network", result.getProvider());
        assertEquals(1, result.getVlans().size());
        assertEquals(2, result.getVlans().get(0).intValue());
    }

    @Test
    public void nullNetwork() {
        NetworkInfoMapper mapper = new NetworkInfoMapper(null);
        assertEquals(NetworkStatus.NOTFOUND, mapper.map().getStatus());
    }

    @Test
    public void mapFields() {
        Network network = new Network();
        network.setId("id");
        network.setName("name");
        network.setSubnets(Arrays.asList("string1", "string2"));
        NetworkInfoMapper mapper = new NetworkInfoMapper(network);
        NetworkInfo mapped = mapper.map();
        assertEquals("name", mapped.getName());
        assertEquals("id", mapped.getId());
        assertEquals(network.getSubnets(), mapped.getSubnets());
    }

    private Network addSegments(Network network) {
        List<Segment> segments = new ArrayList<>();
        Segment segment1 = new Segment();
        segment1.setProviderPhysicalNetwork("type1");
        segment1.setProviderNetworkType("vlan");
        segment1.setProviderSegmentationId(1);
        segments.add(segment1);
        Segment segment2 = new Segment();
        segment2.setProviderPhysicalNetwork("type2");
        segment2.setProviderNetworkType("vlan");
        segment2.setProviderSegmentationId(2);
        segments.add(segment2);
        network.setSegments(segments);
        return network;
    }
}
