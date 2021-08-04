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
 * Model class for service intent instance terminate request
 */
public class ServiceIntentDeletionRequest extends ServiceIntentCommonRequest {

    @JsonProperty("serviceInstanceID")
    private String serviceInstanceID;

    public String getServiceInstanceID() {
        return serviceInstanceID;
    }

    public void setServiceInstanceID(String serviceInstanceID) {
        this.serviceInstanceID = serviceInstanceID;
    }

    @Override
    public String toString() {
        return "ServiceIntentDeletionRequest [serviceInstanceID=" + getServiceInstanceID() + ", globalSubscriberId="
                + getGlobalSubscriberId() + ", subscriptionServiceType=" + getSubscriptionServiceType()
                + ", serviceType=" + getServiceType() + ", additionalProperties=" + getAdditionalProperties() + "]";
    }

}
