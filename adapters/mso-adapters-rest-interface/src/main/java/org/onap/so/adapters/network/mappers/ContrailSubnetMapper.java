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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.onap.so.adapters.network.beans.ContrailSubnet;
import org.onap.so.adapters.network.beans.ContrailSubnetHostRoute;
import org.onap.so.adapters.network.beans.ContrailSubnetHostRoutes;
import org.onap.so.adapters.network.beans.ContrailSubnetIp;
import org.onap.so.adapters.network.beans.ContrailSubnetPool;
import org.onap.so.openstack.beans.HostRoute;
import org.onap.so.openstack.beans.Pool;
import org.onap.so.openstack.beans.Subnet;

public class ContrailSubnetMapper {

    private final Subnet inputSubnet;

    public ContrailSubnetMapper(Subnet inputSubnet) {
        this.inputSubnet = inputSubnet;
    }

    public ContrailSubnet map() {

        final ContrailSubnet result = new ContrailSubnet();
        if (inputSubnet != null) {
            final String subnetname = this.getSubnetName(inputSubnet);
            result.setSubnetName(subnetname);
            result.setEnableDhcp(inputSubnet.getEnableDHCP());
            result.setDefaultGateway(inputSubnet.getGatewayIp());

            Optional<ContrailSubnetIp> csIp = createSubnet(inputSubnet);
            if (csIp.isPresent()) {
                result.setSubnet(csIp.get());
            }
            Optional<List<ContrailSubnetPool>> pools = this.createContrailSubnetPool(inputSubnet);
            if (pools.isPresent()) {
                result.setAllocationPools(pools.get());
            }
            Optional<ContrailSubnetHostRoutes> routes = this.createContrailSubnetHostRoutes(inputSubnet);
            if (routes.isPresent()) {
                result.setHostRoutes(routes.get());
            }
        }

        return result;
    }

    protected String getSubnetName(Subnet subnet) {
        final String result;
        if (!isNullOrEmpty(subnet.getSubnetName())) {
            result = subnet.getSubnetName();
        } else {
            result = subnet.getSubnetId();
        }

        return result;
    }

    protected Optional<List<ContrailSubnetPool>> createContrailSubnetPool(final Subnet subnet) {
        Optional<List<ContrailSubnetPool>> result = Optional.empty();
        if (subnet.getAllocationPools() != null) {
            List<ContrailSubnetPool> pools = new ArrayList<>();
            for (Pool pool : subnet.getAllocationPools()) {
                if (!isNullOrEmpty(pool.getStart()) && !isNullOrEmpty(pool.getEnd())) {

                    pools.add(new ContrailSubnetPoolMapper(pool).map());
                }
            }
            if (!pools.isEmpty()) {
                result = Optional.of(pools);
            }
        }

        return result;
    }

    protected Optional<ContrailSubnetHostRoutes> createContrailSubnetHostRoutes(final Subnet subnet) {
        Optional<ContrailSubnetHostRoutes> result = Optional.empty();
        if (subnet.getHostRoutes() != null) {
            ContrailSubnetHostRoutes hostRoutesObj = new ContrailSubnetHostRoutes();
            List<ContrailSubnetHostRoute> hrList = new ArrayList<>();
            for (HostRoute hr : subnet.getHostRoutes()) {
                if (!isNullOrEmpty(hr.getPrefix()) || !isNullOrEmpty(hr.getNextHop())) {
                    hrList.add(new ContrailSubnetHostRouteMapper(hr).map());
                }
            }
            if (!hrList.isEmpty()) {
                hostRoutesObj.setHostRoutes(hrList);
                result = Optional.of(hostRoutesObj);
            }
        }

        return result;

    }

    protected Optional<ContrailSubnetIp> createSubnet(final Subnet subnet) {
        Optional<ContrailSubnetIp> result = Optional.empty();
        if (!isNullOrEmpty(subnet.getCidr())) {
            int idx = subnet.getCidr().indexOf("/");
            final ContrailSubnetIp csIp = new ContrailSubnetIp();
            if (idx != -1) {
                csIp.setIpPrefix(subnet.getCidr().substring(0, idx));
                csIp.setIpPrefixLen(subnet.getCidr().substring(idx + 1));
                result = Optional.of(csIp);
            }
        }

        return result;
    }

    protected boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
