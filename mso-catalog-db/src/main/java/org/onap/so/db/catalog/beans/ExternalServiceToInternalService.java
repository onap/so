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

package org.onap.so.db.catalog.beans;

import java.io.Serializable;
/*
 * import javax.persistence.Column; import javax.persistence.Entity; import javax.persistence.GeneratedValue; import
 * javax.persistence.GenerationType; import javax.persistence.Id; import javax.persistence.Table;
 */
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.openpojo.business.annotation.BusinessKey;
import uk.co.blackpepper.bowman.annotation.RemoteResource;

@Entity
@RemoteResource("/externalServiceToInternalService")
@Table(name = "external_service_to_internal_model_mapping")
public class ExternalServiceToInternalService implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3825246668050173010L;

    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @BusinessKey
    @Column(name = "SERVICE_NAME")
    private String serviceName;

    @BusinessKey
    @Column(name = "PRODUCT_FLAVOR")
    private String productFlavor;

    @BusinessKey
    @Column(name = "SUBSCRIPTION_SERVICE_TYPE")
    private String subscriptionServiceType;

    @BusinessKey
    @Column(name = "SERVICE_MODEL_UUID")
    private String serviceModelUUID;

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ExternalServiceToInternalService)) {
            return false;
        }
        ExternalServiceToInternalService castOther = (ExternalServiceToInternalService) other;
        return new EqualsBuilder().append(serviceName, castOther.serviceName)
                .append(productFlavor, castOther.productFlavor)
                .append(subscriptionServiceType, castOther.subscriptionServiceType)
                .append(serviceModelUUID, castOther.serviceModelUUID).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(serviceName).append(productFlavor).append(subscriptionServiceType)
                .append(serviceModelUUID).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("serviceName", serviceName)
                .append("productFlavor", productFlavor).append("subscriptionServiceType", subscriptionServiceType)
                .append("serviceModelUUID", serviceModelUUID).toString();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getProductFlavor() {
        return productFlavor;
    }

    public void setProductFlavor(String productFlavor) {
        this.productFlavor = productFlavor;
    }

    public String getSubscriptionServiceType() {
        return subscriptionServiceType;
    }

    public void setSubscriptionServiceType(String subscriptionServiceType) {
        this.subscriptionServiceType = subscriptionServiceType;
    }

    public String getServiceModelUUID() {
        return serviceModelUUID;
    }

    public void setServiceModelUUID(String serviceModelUUID) {
        this.serviceModelUUID = serviceModelUUID;
    }
}
