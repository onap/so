/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Wipro Limited.
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

package org.onap.so.apihandlerinfra.onap3gppserviceinstancebeans;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model class for 3GPP service instance create request
 */
public class Allocate3gppService {

    @JsonProperty("name")
    private String name;

    @JsonProperty("modelInvariantUuid")
    private String modelInvariantUuid;

    @JsonProperty("modelUuid")
    private String modelUuid;

    @JsonProperty("globalSubscriberId")
    private String globalSubscriberId;

    @JsonProperty("subscriptionServiceType")
    private String subscriptionServiceType;

    @JsonProperty("networkType")
    private String networkType;

    @JsonProperty("sst")
    private String sST;

    @JsonProperty("additionalProperties")
    private Map<String, Object> additionalProperties = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModelInvariantUuid() {
        return modelInvariantUuid;
    }

    public void setModelInvariantUuid(String modelInvariantUuid) {
        this.modelInvariantUuid = modelInvariantUuid;
    }

    public String getModelUuid() {
        return modelUuid;
    }

    public void setModelUuid(String modelUuid) {
        this.modelUuid = modelUuid;
    }

    public String getGlobalSubscriberId() {
        return globalSubscriberId;
    }

    public void setGlobalSubscriberId(String globalSubscriberId) {
        this.globalSubscriberId = globalSubscriberId;
    }

    public String getSubscriptionServiceType() {
        return subscriptionServiceType;
    }

    public void setSubscriptionServiceType(String subscriptionServiceType) {
        this.subscriptionServiceType = subscriptionServiceType;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public String getsST() {
        return sST;
    }

    public void setsST(String sST) {
        this.sST = sST;
    }

    @Override
    public String toString() {
        return "Allocate3gppService [name=" + name + ", modelInvariantUuid=" + modelInvariantUuid + ", modelUuid="
                + modelUuid + ", globalSubscriberId=" + globalSubscriberId + ", subscriptionServiceType="
                + subscriptionServiceType + ", networkType=" + networkType + ", additionalProperties="
                + additionalProperties + "]";
    }

}
