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

package org.onap.so.adapters.nwrest;



import java.io.Serializable;
import java.util.List;

public class ProviderVlanNetwork implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 6744949861614446315L;
    private String physicalNetworkName;
    private List<Integer> vlans;

    public ProviderVlanNetwork() {
        super();
    }

    public ProviderVlanNetwork(String physicalNetworkName, List<Integer> vlans) {
        super();
        this.physicalNetworkName = physicalNetworkName;
        this.vlans = vlans;
    }

    public String getPhysicalNetworkName() {
        return physicalNetworkName;
    }

    public void setPhysicalNetworkName(String physicalNetworkName) {
        this.physicalNetworkName = physicalNetworkName;
    }

    public List<Integer> getVlans() {
        return vlans;
    }

    public void setVlans(List<Integer> vlans) {
        this.vlans = vlans;
    }
}
