/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies.
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

package org.onap.so.apihandlerinfra.serviceintentinstancebeans;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model class for Service Intent Creation request
 */
public class ServiceIntentCreationRequest extends ServiceIntentCommonRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("modelInvariantUuid")
    private String modelInvariantUuid;

    @JsonProperty("modelUuid")
    private String modelUuid;

    @JsonProperty("sst")
    private String sST;

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

    public String getsST() {
        return sST;
    }

    public void setsST(String sST) {
        this.sST = sST;
    }

    @Override
    public String toString() {
        return "ServiceIntentCreationRequest [name=" + name + ", modelInvariantUuid=" + modelInvariantUuid
                + ", modelUuid=" + modelUuid + ", globalSubscriberId=" + getGlobalSubscriberId()
                + ", subscriptionServiceType=" + getSubscriptionServiceType() + ", serviceType=" + getServiceType()
                + ", additionalProperties=" + getAdditionalProperties() + "]";
    }

}
