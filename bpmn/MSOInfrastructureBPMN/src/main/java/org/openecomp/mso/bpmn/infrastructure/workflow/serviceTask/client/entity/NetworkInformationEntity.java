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
public class NetworkInformationEntity {
    @JsonProperty("GENERIC-RESOURCE-API:network-id")
    private String networkId;

    @JsonProperty("GENERIC-RESOURCE-API:network-type")
    private String networkType;

    @JsonProperty("GENERIC-RESOURCE-API:onap-model-information")
    private OnapModelInformationEntity onapModelInformation;

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public OnapModelInformationEntity getOnapModelInformation() {
        return onapModelInformation;
    }

    public void setOnapModelInformation(OnapModelInformationEntity onapModelInformation) {
        this.onapModelInformation = onapModelInformation;
    }
}
