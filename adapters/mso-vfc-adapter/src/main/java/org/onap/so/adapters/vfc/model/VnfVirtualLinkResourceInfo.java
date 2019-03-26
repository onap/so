/*
 * Copyright (C) 2019 Verizon. All Rights Reserved Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.onap.so.adapters.vfc.model;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public class VnfVirtualLinkResourceInfo {
    @NotNull
    private String id;
    @NotNull
    private String virtualLinkDescId;
    @NotNull
    private ResourceHandle networkResource;
    private String reservationId;
    private List<VnfLinkPortInfo> vnfLinkPorts;
    Map<String, Object> metadata;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVirtualLinkDescId() {
        return virtualLinkDescId;
    }

    public void setVirtualLinkDescId(String virtualLinkDescId) {
        this.virtualLinkDescId = virtualLinkDescId;
    }

    public ResourceHandle getNetworkResource() {
        return networkResource;
    }

    public void setNetworkResource(ResourceHandle networkResource) {
        this.networkResource = networkResource;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public List<VnfLinkPortInfo> getVnfLinkPorts() {
        return vnfLinkPorts;
    }

    public void setVnfLinkPorts(List<VnfLinkPortInfo> vnfLinkPorts) {
        this.vnfLinkPorts = vnfLinkPorts;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
