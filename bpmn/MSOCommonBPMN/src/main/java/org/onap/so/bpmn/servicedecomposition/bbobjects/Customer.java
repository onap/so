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

package org.onap.so.bpmn.servicedecomposition.bbobjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Id;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onap.so.bpmn.servicedecomposition.ShallowCopy;

@JsonRootName("customer")
public class Customer implements Serializable, ShallowCopy<Customer> {

    private static final long serialVersionUID = 2006599484099139618L;

    @Id
    @JsonProperty("global-customer-id")
    private String globalCustomerId;
    @JsonProperty("subscriber-name")
    private String subscriberName;
    @JsonProperty("subscriber-type")
    private String subscriberType;
    @JsonProperty("subscriber-common-site-id")
    private String subscriberCommonSiteId;
    @JsonProperty("service-subscription")
    private ServiceSubscription serviceSubscription;
    @JsonProperty("customer-latitude")
    private String customerLatitude;
    @JsonProperty("customer-longitude")
    private String customerLongitude;
    @JsonProperty("vpn-bindings")
    private List<VpnBinding> vpnBindings = new ArrayList<>();

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

    public String getSubscriberType() {
        return subscriberType;
    }

    public void setSubscriberType(String subscriberType) {
        this.subscriberType = subscriberType;
    }

    public String getSubscriberCommonSiteId() {
        return subscriberCommonSiteId;
    }

    public void setSubscriberCommonSiteId(String subscriberCommonSiteId) {
        this.subscriberCommonSiteId = subscriberCommonSiteId;
    }

    public ServiceSubscription getServiceSubscription() {
        return serviceSubscription;
    }

    public void setServiceSubscription(ServiceSubscription serviceSubscription) {
        this.serviceSubscription = serviceSubscription;
    }

    public String getCustomerLatitude() {
        return customerLatitude;
    }

    public void setCustomerLatitude(String customerLatitude) {
        this.customerLatitude = customerLatitude;
    }

    public String getCustomerLongitude() {
        return customerLongitude;
    }

    public void setCustomerLongitude(String customerLongitude) {
        this.customerLongitude = customerLongitude;
    }

    public List<VpnBinding> getVpnBindings() {
        return vpnBindings;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Customer)) {
            return false;
        }
        Customer castOther = (Customer) other;
        return new EqualsBuilder().append(globalCustomerId, castOther.globalCustomerId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(globalCustomerId).toHashCode();
    }
}
