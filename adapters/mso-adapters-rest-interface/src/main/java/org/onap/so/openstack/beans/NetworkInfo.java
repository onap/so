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

package org.onap.so.openstack.beans;



import java.util.ArrayList;
import java.util.List;

/*
 * This Java bean class relays Network details (including status) to ActiveVOS processes.
 *
 * This bean is returned by all Network-specific adapter operations (create, query, delete)
 */

public class NetworkInfo {
    // Set defaults for everything
    private String name = "";
    private String id = "";
    private NetworkStatus status = NetworkStatus.UNKNOWN;
    private String provider = "";
    private List<Integer> vlans = new ArrayList<>();
    private List<String> subnets = new ArrayList<>();
    private String shared = "";

    public NetworkInfo() {}

    public NetworkInfo(String name, NetworkStatus status) {
        this.name = name;
        this.id = name; // Don't have an ID, so just use name
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public NetworkStatus getStatus() {
        return status;
    }

    public void setStatus(NetworkStatus status) {
        this.status = status;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public List<Integer> getVlans() {
        return vlans;
    }

    public void setVlans(List<Integer> vlans) {
        this.vlans = vlans;
    }

    public List<String> getSubnets() {
        return subnets;
    }

    public void setSubnets(List<String> subnets) {
        this.subnets = subnets;
    }

    public String getShared() {
        return shared;
    }

    public void setShared(String shared) {
        this.shared = shared;
    }

    @Override
    public String toString() {
        return "NetworkInfo{" + "name='" + name + '\'' + ", id='" + id + '\'' + ", status=" + status + ", provider='"
                + provider + '\'' + ", vlans=" + vlans + ", subnets=" + subnets + '}';
    }
}

