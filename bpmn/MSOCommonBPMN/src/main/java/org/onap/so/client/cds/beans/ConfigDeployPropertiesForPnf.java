/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 TechMahindra
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

package org.onap.so.client.cds.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"service-instance-id", "pnf-id", "pnf-name", "pnf-ipv4-address", "pnf-ipv6-address",
        "service-model-uuid", "pnf-customization-uuid"})

public class ConfigDeployPropertiesForPnf {

    @JsonProperty("service-instance-id")
    private String serviceInstanceId;

    @JsonProperty("pnf-id")
    private String pnfId;

    @JsonProperty("pnf-name")
    private String pnfName;

    /**
     * Config Deploy require IP address of PNF.
     */
    @JsonProperty("pnf-ipv4-address")
    private String pnfIpV4Address;

    @JsonProperty("pnf-ipv6-address")
    private String pnfIpV6Address;

    @JsonProperty("service-model-uuid")
    private String serviceModelUuid;

    @JsonProperty("pnf-customization-uuid")
    private String pnfCustomizationUuid;

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    public String getPnfId() {
        return pnfId;
    }

    public void setPnfId(String pnfId) {
        this.pnfId = pnfId;
    }

    public String getPnfName() {
        return pnfName;
    }

    public void setPnfName(String pnfName) {
        this.pnfName = pnfName;
    }

    public String getServiceModelUuid() {
        return serviceModelUuid;
    }

    public void setServiceModelUuid(String serviceModelUuid) {
        this.serviceModelUuid = serviceModelUuid;
    }

    public String getPnfCustomizationUuid() {
        return pnfCustomizationUuid;
    }

    public void setPnfCustomizationUuid(String pnfCustomizationUuid) {
        this.pnfCustomizationUuid = pnfCustomizationUuid;
    }

    public String getPnfIpV4Address() {
        return pnfIpV4Address;
    }

    public void setPnfIpV4Address(String pnfIpV4Address) {
        this.pnfIpV4Address = pnfIpV4Address;
    }

    public String getPnfIpV6Address() {
        return pnfIpV6Address;
    }

    public void setPnfIpV6Address(String pnfIpV6Address) {
        this.pnfIpV6Address = pnfIpV6Address;
    }


    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"service-instance-id\":").append("\"").append(serviceInstanceId).append("\"");
        sb.append(", \"pnf-id\":").append("\"").append(pnfId).append("\"");
        sb.append(", \"pnf-name\":").append("\"").append(pnfName).append("\"");
        sb.append(", \"pnf-ipv4-address\":").append("\"").append(pnfIpV4Address).append("\"");
        sb.append(", \"pnf-ipv6-address\":").append("\"").append(pnfIpV6Address).append("\"");
        sb.append(", \"service-model-uuid\":").append("\"").append(serviceModelUuid).append("\"");
        sb.append(", \"pnf-customization-uuid\":").append("\"").append(pnfCustomizationUuid).append("\"");

        sb.append('}');

        return sb.toString();
    }

}
