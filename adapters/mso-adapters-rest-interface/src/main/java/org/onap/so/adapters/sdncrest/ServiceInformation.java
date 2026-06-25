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

package org.onap.so.adapters.sdncrest;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlElement;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Service Information specified by the SDNC "agnostic" API.
 */
public class ServiceInformation implements Serializable {
    private static final long serialVersionUID = 1L;

    // The subscription's service type for the target service instance.
    private String serviceType;

    // Identifies the target service instance for this particular SDNC request.
    // NOTE: this could be a child of the parent model instance, i.e. this
    // service instance ID may be different from the service instance ID
    // associated with the transaction MSO has with the system that invoked it.
    private String serviceInstanceId;

    // The subscriber name.
    private String subscriberName;

    // The subscriber global ID (customer ID).
    private String subscriberGlobalId;

    public ServiceInformation(String serviceType, String serviceInstanceId, String subscriberName,
            String subscriberGlobalId) {
        this.serviceType = serviceType;
        this.serviceInstanceId = serviceInstanceId;
        this.subscriberName = subscriberName;
        this.subscriberGlobalId = subscriberGlobalId;
    }

    public ServiceInformation() {}

    @JsonProperty("serviceType")
    @XmlElement(name = "serviceType")
    public String getServiceType() {
        return serviceType;
    }

    @JsonProperty("serviceType")
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    @JsonProperty("serviceInstanceId")
    @XmlElement(name = "serviceInstanceId")
    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    @JsonProperty("serviceInstanceId")
    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    @JsonProperty("subscriberName")
    @XmlElement(name = "subscriberName")
    public String getSubscriberName() {
        return subscriberName;
    }

    @JsonProperty("subscriberName")
    public void setSubscriberName(String subscriberName) {
        this.subscriberName = subscriberName;
    }

    @JsonProperty("subscriberGlobalId")
    @XmlElement(name = "subscriberGlobalId")
    public String getSubscriberGlobalId() {
        return subscriberGlobalId;
    }

    @JsonProperty("subscriberGlobalId")
    public void setSubscriberGlobalId(String subscriberGlobalId) {
        this.subscriberGlobalId = subscriberGlobalId;
    }
}
