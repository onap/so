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

package org.onap.so.adapters.network.mappers;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.onap.so.adapters.network.beans.ContrailSubnet;
import org.onap.so.adapters.network.beans.ContrailSubnetHostRoute;
import org.onap.so.adapters.network.beans.ContrailSubnetHostRoutes;
import org.onap.so.adapters.network.beans.ContrailSubnetPool;
import org.onap.so.openstack.beans.HostRoute;
import org.onap.so.openstack.beans.Pool;
import org.onap.so.openstack.beans.Subnet;

public class ContrailSubnetMappersTest {



    @Test
    public void contrailSubnetHostRouteMapperTest() {
        HostRoute hostRoute = new HostRoute();
        hostRoute.setNextHop("hop");
        hostRoute.setPrefix("prefix");
        ContrailSubnetHostRouteMapper mapper = new ContrailSubnetHostRouteMapper(hostRoute);
        ContrailSubnetHostRoute cshr = mapper.map();
        assertEquals("hop", cshr.getNextHop());
        assertEquals("prefix", cshr.getPrefix());
    }

    @Test
    public void contrailSubnetPoolMapperTest() {
        Pool pool = new Pool();
        pool.setStart("start");
        pool.setEnd("end");
        ContrailSubnetPoolMapper mapper = new ContrailSubnetPoolMapper(pool);
        ContrailSubnetPool csPool = mapper.map();
        assertEquals("start", csPool.getStart());
        assertEquals("end", csPool.getEnd());
    }

    @Test
    public void checkIsNullOrEmpty() {
        ContrailSubnetMapper mapper = new ContrailSubnetMapper(null);
        assertEquals(true, mapper.isNullOrEmpty(""));
        assertEquals(true, mapper.isNullOrEmpty(null));
        assertEquals(false, mapper.isNullOrEmpty("hello"));
    }

    @Test
    public void createSubnetTestValidCidr() {
        Subnet subnet = new Subnet();
        subnet.setCidr("test/value");
        ContrailSubnetMapper mapper = new ContrailSubnetMapper(subnet);
        ContrailSubnet result = mapper.map();

        assertEquals("test", result.getSubnet().getIpPrefix());
        assertEquals("value", result.getSubnet().getIpPrefixLen());
    }

    @Test
    public void createSubnetTestInvalidCidr() {
        Subnet subnet = new Subnet();
        subnet.setCidr("test");
        ContrailSubnetMapper mapper = new ContrailSubnetMapper(subnet);
        ContrailSubnet result = mapper.map();
        assertEquals(null, result.getSubnet().getIpPrefix());
        assertEquals(null, result.getSubnet().getIpPrefixLen());

    }

    @Test
    public void createSubnetTestNullCidr() {
        Subnet subnet = new Subnet();
        ContrailSubnetMapper mapper = new ContrailSubnetMapper(subnet);
        ContrailSubnet result = mapper.map();
        assertEquals(null, result.getSubnet().getIpPrefix());
        assertEquals(null, result.getSubnet().getIpPrefixLen());
    }

    @Test
    public void createContrailSubnetPoolTest() {
        List<Pool> pools = new ArrayList<>();
        Pool pool1 = new Pool();
        pool1.setStart("start1");
        pool1.setEnd("end1");
        Pool pool2 = new Pool();
        pool2.setStart("start2");
        pool2.setEnd("end2");
        pools.add(pool1);
        pools.add(pool2);

        Subnet subnet = new Subnet();
        subnet.setAllocationPools(pools);
        ContrailSubnetMapper mapper = new ContrailSubnetMapper(subnet);
        ContrailSubnet result = mapper.map();
        List<ContrailSubnetPool> cspools = result.getAllocationPools();
        assertEquals(2, cspools.size());
        assertEquals("start2", cspools.get(1).getStart());
        assertEquals("end2", cspools.get(1).getEnd());
    }

    @Test
    public void createContrailSubnetPoolInvalidTest() {
        List<Pool> pools = new ArrayList<>();
        Pool pool1 = new Pool();
        pool1.setStart("start1");
        pool1.setEnd("end1");
        Pool pool2 = new Pool();
        pool2.setStart("start2");
        pools.add(pool1);
        pools.add(pool2);

        Subnet subnet = new Subnet();
        subnet.setAllocationPools(pools);
        ContrailSubnetMapper mapper = new ContrailSubnetMapper(subnet);
        ContrailSubnet result = mapper.map();
        List<ContrailSubnetPool> cspools = result.getAllocationPools();
        assertEquals(1, cspools.size());
        assertEquals("start1", cspools.get(0).getStart());
        assertEquals("end1", cspools.get(0).getEnd());
    }

    @Test
    public void createContrailSubnetPoolEmptyTest() {

        Subnet subnet = new Subnet();
        ContrailSubnetMapper mapper = new ContrailSubnetMapper(subnet);
        ContrailSubnet result = mapper.map();
        List<ContrailSubnetPool> cspools = result.getAllocationPools();
        assertEquals(true, cspools.isEmpty());

    }

    @Test
    public void createContrailSubnetHostRoutesTest() {
        List<HostRoute> hostRoutes = new ArrayList<>();
        HostRoute hostRoute1 = new HostRoute();
        hostRoute1.setNextHop("next-hop1");
        hostRoute1.setPrefix("prefix1");
        HostRoute hostRoute2 = new HostRoute();
        hostRoute2.setNextHop("next-hop2");
        hostRoute2.setPrefix("prefix2");
        hostRoutes.add(hostRoute1);
        hostRoutes.add(hostRoute2);

        Subnet subnet = new Subnet();
        subnet.setHostRoutes(hostRoutes);
        ContrailSubnetMapper mapper = new ContrailSubnetMapper(subnet);
        ContrailSubnet result = mapper.map();
        ContrailSubnetHostRoutes routes = result.getHostRoutes();
        assertEquals(2, routes.getHostRoutes().size());
        assertEquals("next-hop2", routes.getHostRoutes().get(1).getNextHop());
        assertEquals("prefix2", routes.getHostRoutes().get(1).getPrefix());
    }

    @Test
    public void createContrailSubnetHostRoutesMissingFieldTest() {
        List<HostRoute> hostRoutes = new ArrayList<>();
        HostRoute hostRoute1 = new HostRoute();
        hostRoute1.setNextHop("next-hop1");
        HostRoute hostRoute2 = new HostRoute();
        hostRoute2.setNextHop("next-hop2");
        hostRoute2.setPrefix("prefix2");
        hostRoutes.add(hostRoute1);
        hostRoutes.add(hostRoute2);

        Subnet subnet = new Subnet();
        subnet.setHostRoutes(hostRoutes);
        ContrailSubnetMapper mapper = new ContrailSubnetMapper(subnet);
        ContrailSubnet result = mapper.map();
        ContrailSubnetHostRoutes routes = result.getHostRoutes();
        assertEquals(2, routes.getHostRoutes().size());
        assertEquals("next-hop1", routes.getHostRoutes().get(0).getNextHop());
        assertEquals("prefix2", routes.getHostRoutes().get(1).getPrefix());
    }

    @Test
    public void createContrailSubnetHostRoutesEmptyTest() {
        List<HostRoute> hostRoutes = new ArrayList<>();
        Subnet subnet = new Subnet();
        subnet.setHostRoutes(hostRoutes);
        ContrailSubnetMapper mapper = new ContrailSubnetMapper(subnet);
        ContrailSubnet result = mapper.map();
        ContrailSubnetHostRoutes routes = result.getHostRoutes();
        assertEquals(true, routes.getHostRoutes().isEmpty());
    }

    @Test
    public void getSubnetNameTest() {
        Subnet subnet = new Subnet();
        subnet.setSubnetName("name");
        ContrailSubnetMapper mapper = new ContrailSubnetMapper(subnet);
        assertEquals("if subnetName is populated map", "name", mapper.getSubnetName(subnet));
        subnet = new Subnet();
        subnet.setSubnetId("id");
        mapper = new ContrailSubnetMapper(subnet);
        assertEquals("choose id when name is null", "id", mapper.getSubnetName(subnet));
        subnet = new Subnet();
        mapper = new ContrailSubnetMapper(subnet);
        assertEquals("expect null", null, mapper.getSubnetName(subnet));
    }

    @Test
    public void mapRemainingFields() {
        Subnet subnet = new Subnet();
        subnet.setEnableDHCP(true);
        subnet.setGatewayIp("gateway-ip");
        ContrailSubnetMapper mapper = new ContrailSubnetMapper(subnet);
        ContrailSubnet result = mapper.map();
        assertEquals(true, result.isEnableDhcp());
        assertEquals("gateway-ip", result.getDefaultGateway());
    }
}
