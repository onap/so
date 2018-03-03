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
public class ServiceInformationEntity {
    @JsonProperty("GENERIC-RESOURCE-API:service-id")
    private String serviceId;

    @JsonProperty("GENERIC-RESOURCE-API:subscription-service-type")
    private String subscriptionServiceType;

    @JsonProperty("GENERIC-RESOURCE-API:onap-model-information")
    private OnapModelInformationEntity onapModelInformation;

    @JsonProperty("GENERIC-RESOURCE-API:service-instance-id")
    private String serviceInstanceId;

    @JsonProperty("GENERIC-RESOURCE-API:global-customer-id")
    private String globalCustomerId;

    @JsonProperty("GENERIC-RESOURCE-API:subscriber-name")
    private String subscriberName;

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getSubscriptionServiceType() {
        return subscriptionServiceType;
    }

    public void setSubscriptionServiceType(String subscriptionServiceType) {
        this.subscriptionServiceType = subscriptionServiceType;
    }

    public OnapModelInformationEntity getOnapModelInformation() {
        return onapModelInformation;
    }

    public void setOnapModelInformation(OnapModelInformationEntity onapModelInformation) {
        this.onapModelInformation = onapModelInformation;
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    public String getGlobalCustomerId() {
        return globalCustomerId;
    }

    public void setGlobalCustomerId(String globalCustomerId) {
        this.globalCustomerId = globalCustomerId;
    }

    public String getSubscriberName() {
        return subscriberName;
    }

    public void setSubscriberName(String subscriberName) {
        this.subscriberName = subscriberName;
    }
}
