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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onap.so.bpmn.servicedecomposition.ShallowCopy;
import jakarta.persistence.Id;

@JsonRootName("service-subscription")
public class ServiceSubscription implements Serializable, ShallowCopy<ServiceSubscription> {

    private static final long serialVersionUID = 9064449329296611436L;

    @Id
    @JsonProperty("service-type")
    private String serviceType;
    @JsonProperty("temp-ub-sub-account-id")
    private String tempUbSubAccountId;
    @JsonProperty("service-instances")
    private List<ServiceInstance> serviceInstances = new ArrayList<>();

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getTempUbSubAccountId() {
        return tempUbSubAccountId;
    }

    public void setTempUbSubAccountId(String tempUbSubAccountId) {
        this.tempUbSubAccountId = tempUbSubAccountId;
    }

    public List<ServiceInstance> getServiceInstances() {
        return serviceInstances;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ServiceSubscription)) {
            return false;
        }
        ServiceSubscription castOther = (ServiceSubscription) other;
        return new EqualsBuilder().append(serviceType, castOther.serviceType).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(serviceType).toHashCode();
    }
}
