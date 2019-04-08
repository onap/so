/*
 * ============LICENSE_START======================================================= ONAP - SO
 * ================================================================================ Copyright (C) 2017 AT&T Intellectual
 * Property. All rights reserved. ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.appc.payload.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestParametersConfigScaleOut {

    @JsonProperty("vnf-host-ip-address")
    private String vnfHostIpAddress;
    @JsonProperty("vf-module-id")
    private String vfModuleId;

    public String getVnfHostIpAddress() {
        return vnfHostIpAddress;
    }

    public void setVnfHostIpAddress(String vnfHostIpAddress) {
        this.vnfHostIpAddress = vnfHostIpAddress;
    }

    public String getVfModuleId() {
        return vfModuleId;
    }

    public void setVfModuleId(String vfModuleId) {
        this.vfModuleId = vfModuleId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RequestParametersConfigScaleOut{");
        sb.append("vnf-host-ip-address=").append(vnfHostIpAddress);
        sb.append(", vf-module-id='").append(vfModuleId);
        sb.append('}');
        return sb.toString();
    }
}
