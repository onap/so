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
public class NetworkTopologyOperationInputEntity {
    @JsonProperty("GENERIC-RESOURCE-API:sdnc-request-header")
    private SdncRequestHeaderEntity sdncRequestHeader;

    @JsonProperty("GENERIC-RESOURCE-API:request-information")
    private RequestInformationEntity requestInformation;

    @JsonProperty("GENERIC-RESOURCE-API:service-information")
    private ServiceInformationEntity serviceInformation;

    @JsonProperty("GENERIC-RESOURCE-API:network-information")
    private NetworkInformationEntity networkInformation;

    @JsonProperty("GENERIC-RESOURCE-API:network-request-input")
    private NetworkRequestInputEntity networkRequestInput;

    public SdncRequestHeaderEntity getSdncRequestHeader() {
        return sdncRequestHeader;
    }

    public void setSdncRequestHeader(SdncRequestHeaderEntity sdncRequestHeader) {
        this.sdncRequestHeader = sdncRequestHeader;
    }

    public RequestInformationEntity getRequestInformation() {
        return requestInformation;
    }

    public void setRequestInformation(RequestInformationEntity requestInformation) {
        this.requestInformation = requestInformation;
    }

    public ServiceInformationEntity getServiceInformation() {
        return serviceInformation;
    }

    public void setServiceInformation(ServiceInformationEntity serviceInformation) {
        this.serviceInformation = serviceInformation;
    }

    public NetworkInformationEntity getNetworkInformation() {
        return networkInformation;
    }

    public void setNetworkInformation(NetworkInformationEntity networkInformation) {
        this.networkInformation = networkInformation;
    }

    public NetworkRequestInputEntity getNetworkRequestInput() {
        return networkRequestInput;
    }

    public void setNetworkRequestInput(NetworkRequestInputEntity networkRequestInput) {
        this.networkRequestInput = networkRequestInput;
    }
}
