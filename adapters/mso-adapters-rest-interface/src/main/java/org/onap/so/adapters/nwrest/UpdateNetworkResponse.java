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



import java.util.Map;
import jakarta.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("updateNetworkResponse")
@XmlRootElement(name = "updateNetworkResponse")
public class UpdateNetworkResponse extends NetworkResponseCommon {

    /**
     * 
     */
    private static final long serialVersionUID = -7528214382414366136L;
    private String networkId;
    private String neutronNetworkId;
    private Map<String, String> subnetMap;

    public UpdateNetworkResponse() {
        /* Empty Constructor */
    }

    public UpdateNetworkResponse(String networkId, String neutronNetworkId, Map<String, String> subnetMap,
            String messageId) {
        super(messageId);
        this.networkId = networkId;
        this.neutronNetworkId = neutronNetworkId;
        this.subnetMap = subnetMap;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public String getNeutronNetworkId() {
        return neutronNetworkId;
    }

    public void setNeutronNetworkId(String neutronNetworkId) {
        this.neutronNetworkId = neutronNetworkId;
    }

    public Map<String, String> getSubnetMap() {
        return subnetMap;
    }

    public void setSubnetMap(Map<String, String> subnetMap) {
        this.subnetMap = subnetMap;
    }
}
