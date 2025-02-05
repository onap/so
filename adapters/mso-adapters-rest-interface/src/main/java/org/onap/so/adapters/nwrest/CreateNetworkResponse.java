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
import org.onap.so.openstack.beans.NetworkRollback;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("createNetworkResponse")
@XmlRootElement(name = "createNetworkResponse")

public class CreateNetworkResponse extends NetworkResponseCommon {
    /**
     * 
     */
    private static final long serialVersionUID = -7730406297031948309L;

    private String networkId;
    private String neutronNetworkId;
    private String networkStackId;
    private String networkFqdn;
    private Boolean networkCreated;
    private Map<String, String> subnetMap;
    private NetworkRollback rollback = new NetworkRollback();

    public CreateNetworkResponse() {
        super();
    }

    public CreateNetworkResponse(String networkId, String neutronNetworkId, String networkStackId, String networkFqdn,
            Boolean networkCreated, Map<String, String> subnetIdMap, NetworkRollback rollback, String messageId) {
        super(messageId);
        this.networkId = networkId;
        this.neutronNetworkId = neutronNetworkId;
        this.networkStackId = networkStackId;
        this.networkFqdn = networkFqdn;
        this.networkCreated = networkCreated;
        this.subnetMap = subnetIdMap;
        this.rollback = rollback;
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

    public String getNetworkStackId() {
        return networkStackId;
    }

    public void setNetworkStackId(String networkStackId) {
        this.networkStackId = networkStackId;
    }

    public String getNetworkFqdn() {
        return networkFqdn;
    }

    public void setNetworkFqdn(String networkFqdn) {
        this.networkFqdn = networkFqdn;
    }

    public Boolean getNetworkCreated() {
        return networkCreated;
    }

    public void setNetworkCreated(Boolean networkCreated) {
        this.networkCreated = networkCreated;
    }

    public Map<String, String> getSubnetMap() {
        return subnetMap;
    }

    public void setSubnetMap(Map<String, String> subnetMap) {
        this.subnetMap = subnetMap;
    }

    public NetworkRollback getRollback() {
        return rollback;
    }

    public void setRollback(NetworkRollback rollback) {
        this.rollback = rollback;
    }
}
