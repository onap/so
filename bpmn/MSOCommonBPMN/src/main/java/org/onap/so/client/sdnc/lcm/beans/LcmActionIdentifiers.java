/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.client.sdnc.lcm.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"serviceInstanceId", "vnfId", "vfModuleId", "vnfcName", "vserverId", "pnfName"})
public class LcmActionIdentifiers {

    @JsonProperty("service-instance-id")
    private String serviceInstanceId;

    @JsonProperty("vnf-id")
    private String vnfId;

    @JsonProperty("vf-module-id")
    private String vfModuleId;

    @JsonProperty("vnfc-name")
    private String vnfcName;

    @JsonProperty("vserver-id")
    private String vserverId;

    @JsonProperty("pnf-name")
    private String pnfName;

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public void setServiceInstanceId(String value) {
        this.serviceInstanceId = value;
    }

    public String getVnfId() {
        return vnfId;
    }

    public void setVnfId(String value) {
        this.vnfId = value;
    }

    public String getVfModuleId() {
        return vfModuleId;
    }

    public void setVfModuleId(String value) {
        this.vfModuleId = value;
    }

    public String getVnfcName() {
        return vnfcName;
    }

    public void setVnfcName(String value) {
        this.vnfcName = value;
    }

    public String getVserverId() {
        return vserverId;
    }

    public void setVserverId(String value) {
        this.vserverId = value;
    }

    public String getPnfName() {
        return pnfName;
    }

    public void setPnfName(String value) {
        this.pnfName = value;
    }

}
