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

package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity;

/**
 * Created by 10112215 on 2017/9/20.
 */
public class NetworkRequestInputEntity {
    @JsonProperty("GENERIC-RESOURCE-API:network-name")
    private String networkName;

    @JsonProperty("GENERIC-RESOURCE-API:tenant")
    private String tenant;

    @JsonProperty("GENERIC-RESOURCE-API:aic-cloud-region")
    private String aicCloudRegion;

    @JsonProperty("GENERIC-RESOURCE-API:aic-clli")
    private String aicClli;

    @JsonProperty("GENERIC-RESOURCE-API:network-input-parameters")
    private NetworkInputPaarametersEntity networkInputPaarameters;

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getAicCloudRegion() {
        return aicCloudRegion;
    }

    public void setAicCloudRegion(String aicCloudRegion) {
        this.aicCloudRegion = aicCloudRegion;
    }

    public String getAicClli() {
        return aicClli;
    }

    public void setAicClli(String aicClli) {
        this.aicClli = aicClli;
    }

    public NetworkInputPaarametersEntity getNetworkInputPaarameters() {
        return networkInputPaarameters;
    }

    public void setNetworkInputPaarameters(NetworkInputPaarametersEntity networkInputPaarameters) {
        this.networkInputPaarameters = networkInputPaarameters;
    }
}
